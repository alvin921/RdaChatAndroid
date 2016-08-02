package com.rda.libnet;

import android.util.Log;

import com.google.gson.Gson;
import com.rda.protocol.Cmd;
import com.rda.protocol.Protocol;
import com.rda.util.DebugInfo;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by mingangwang on 2016/07/24.
 */

/*
NO_CONNECTION is the default status.
1) NO_CONNECTION------>CONNECTING
    when connect() is called, transfer to CONNECTING
3) CONNECTING------>CONNECTED
    successfully connected to server
4) CONNECTING------>CLOSED
    connecting failed
5) CONNECTED------->CLOSED
    session closed by server or due to network lost
6) CLOSED------->NO_CONNECTION
    Once connecting failed or closed, session auto attempt to reconnect until attempt MAX_CONNECT_ATTEMPT times, then goes to NO_CONNECTION.
 */

public class Session {
    public static final String TAG = "Session";
    public static final int SESSION_NO_CONNECTION = 0;
    public static final int SESSION_CONNECTING = 1;
    public static final int SESSION_CONNECTED = 2;
    public static final int SESSION_CLOSED = 3;
    public static final int MAX_CONNECT_ATTEMPT = 10;

    private Gson gson = new Gson();
    private SocketChannel channel = null;
    private Selector selector;
    private SelectionKey selection_key;
    private final List<ByteBuffer> sendBufs = new LinkedList<ByteBuffer>();
    private ByteBuffer  receiveBuf = ByteBuffer.allocate(128 << 10);
    private long preSendTime;
    private long preReceiveTime;
    private Thread  ioThread;
    private Thread hbThread;
    private boolean closeFlag;
    private InetSocketAddress addr;
    private ArrayBlockingQueue<Cmd> queue;
    private int status;
    private int attempt;
    int heartbeat_seconds;

    public Session(String host, ArrayBlockingQueue<Cmd> queue) throws IOException {
        String[] tmp = host.split(":");
        this.addr = new InetSocketAddress(tmp[0],  Integer.parseInt(tmp[1]));
        status = SESSION_NO_CONNECTION;
        attempt = 0;
        this.queue = queue;
    }

    private synchronized void closeInternal() {
        //queue.clear();
        //closeFlag = true;
        try{
            if(channel != null){
                channel.close();
                channel = null;
            }
            if(selector != null){
                selector.close();;
                selector = null;
            }
            if(ioThread != null){
                ioThread = null;
            }
            if(hbThread != null)
                hbThread = null;
        } catch (IOException e){
            e.printStackTrace();
        }
        status = SESSION_CLOSED;
    }
    public void close()  {
        Log.i(TAG, "close");

        if(ioThread != null || hbThread != null){
            closeFlag = true;
        }else{
            closeInternal();
        }
    }
    public synchronized Selector getSelector()
    {
        return this.selector;
    }

    public synchronized int getStatus() {
        return status;
    }

    public synchronized boolean shouldAttemptConnect() {
        if(attempt < MAX_CONNECT_ATTEMPT)
            return true;
        status = SESSION_NO_CONNECTION;
        return false;
    }

    public void connect(int blockUntil) throws IOException {
        close();

        if(attempt>=MAX_CONNECT_ATTEMPT)
            attempt = 0;
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        receiveBuf.limit(0);
        receiveBuf.order(ByteOrder.BIG_ENDIAN);
        this.selector = Selector.open();
        channel.connect(addr);
        selection_key = channel.register(selector, interestOps());
        status = SESSION_CONNECTING;
        attempt++;
        if(blockUntil > 0){
            boolean timeout=false;
            int nwait=0;
            while(!channel.finishConnect()){
                try {
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    e.printStackTrace();
                    timeout = true;
                    break;
                }
                nwait++;
                if(nwait >= blockUntil/100) {
                    timeout = true;
                    break;
                }
            }
            if(channel.isConnected()) {
                status = SESSION_CONNECTED;
                selection_key.interestOps(interestOps());
            }else {
                status = SESSION_CLOSED;
            }
            if(timeout){
                throw new IOException();
            }
        }
    }

    int interestOps() {
        int ops = SelectionKey.OP_READ;

        if (channel.isConnectionPending()) {
            ops |= SelectionKey.OP_CONNECT;
        }
        if (!sendBufs.isEmpty()) {
            ops |= SelectionKey.OP_WRITE;
        }

        return ops;
    }

    void handleRead(SelectionKey key, ArrayBlockingQueue<Cmd> queue) throws IOException{
        //Log.d("Session", "handleRead");
        SocketChannel channel = (SocketChannel) key.channel();
        int pos = receiveBuf.position();
        receiveBuf.position(receiveBuf.limit());
        receiveBuf.limit(receiveBuf.capacity());

        int r = channel.read(receiveBuf);
        if (r < 0) {

        }

        if (r > 0) {
            preReceiveTime = System.currentTimeMillis();
        }

        receiveBuf.flip();
        receiveBuf.position(pos);

        while (receiveBuf.remaining() >= 4) {
            pos = receiveBuf.position();
            int size = receiveBuf.getInt();

            if (size > Protocol.MAX_PACKET_SIZE) {
                //throw new InvalidPacketException();
                //e.printStackTrace();
            }

            if (receiveBuf.remaining() < size) {
                receiveBuf.position(pos);

                if (receiveBuf.capacity() < size + 4) {
                    ByteBuffer buf = ByteBuffer.allocate(size + 4);
                    buf.order(ByteOrder.BIG_ENDIAN);
                    System.arraycopy(receiveBuf.array(), receiveBuf.position(), buf.array(), 0, receiveBuf.remaining());
                    buf.limit(receiveBuf.remaining());
                    receiveBuf = buf;
                }
                break;
            }

            int limit = receiveBuf.limit();
            receiveBuf.limit(receiveBuf.position() + size);

            try {
                Protocol p = new Protocol();
                p.decoder(receiveBuf, size, queue);
            }
            catch (Exception e) {
                //throw new InvalidPacketException();
                e.printStackTrace();
            }

            receiveBuf.limit(limit);
        }

        if (receiveBuf.position() != 0) {
            int remain = receiveBuf.remaining();
            System.arraycopy(receiveBuf.array(), receiveBuf.position(), receiveBuf.array(), 0, remain);
            receiveBuf.position(0);
            receiveBuf.limit(remain);
        }
    }

    synchronized void handleSend() throws IOException {
        //Log.d("Session", "handleSend");
        if (sendBufs.size() > 0) {
            long writeNum = channel.write(sendBufs.toArray(new ByteBuffer[sendBufs.size()]));
            Iterator<ByteBuffer> iter = sendBufs.iterator();

            while (iter.hasNext()) {
                ByteBuffer b = iter.next();

                if (b.remaining() == 0) {
                    iter.remove();
                }else {
                    break;
                }
            }

            if (sendBufs.isEmpty() && selection_key != null) {
                selection_key.interestOps(interestOps());
            }
        }
    }

    public synchronized void sendPacket(Cmd cmd) throws IOException {
        //Log.d("Session", "sendPacket");
        boolean isEmpty = sendBufs.isEmpty();
        Protocol p = new Protocol();
        p.encoder(cmd);
        sendBufs.add(p.getBuf());

        if (isEmpty && selection_key != null) {
            selection_key.interestOps(interestOps());
            selector.wakeup();
        }

    }
    public Thread startHBThread(int timeout) {
        heartbeat_seconds = timeout;
        hbThread = new Thread(new Runnable() {
            @Override
            public void run(){
                Log.d(TAG, "HBThread begin");
                closeFlag = false;
                while (!closeFlag){
                    // 发送一个心跳包看服务器是否正常
                    try {
                        Log.d(TAG, "sending ping...");
                        sendPacket(new Cmd().PingCmd());
                    }catch (IOException e) {
                        e.printStackTrace();
                        closeFlag = true;
                        try{
                            queue.put(new Cmd().PingCmd());
                        }catch(InterruptedException ee){
                            ee.printStackTrace();
                        }
                    }
                    if(closeFlag){
                        break;
                    }
                    try{
                        Thread.sleep(heartbeat_seconds * 1000);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "HBThread end");
            }
        });
        hbThread.start();
        return hbThread;
    }

    public Thread startIOThread()  {
        Runnable ioTask = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "IOThread begin");
                boolean flagExcept = false;
                closeFlag = false;
                while (!closeFlag) {
                    int count=0;
                    try {
                        count = selector.select(1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Iterator ite = selector.selectedKeys().iterator();
                    while (ite.hasNext()) {
                        SelectionKey key = (SelectionKey) ite.next();
                        ite.remove();
                        if (key.isConnectable()) {
                            Log.d("Session", "isConnectable");
                            SocketChannel channel = (SocketChannel) key.channel();

                            if (channel.isConnectionPending()) {
                                try {
                                    if(channel.finishConnect() == true){
                                        status = SESSION_CONNECTED;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    flagExcept = true;
                                    status = SESSION_CLOSED;
                                }
                            }
                            //try {
                            //    channel.configureBlocking(false);
                            //} catch (IOException e) {
                            //    e.printStackTrace();
                            //}
                            //try {
                            //    channel.register(selector, SelectionKey.OP_WRITE);
                            //} catch (ClosedChannelException e) {
                            //    e.printStackTrace();
                            //}
                        } else if (key.isReadable()) {
                            //Log.d("Session", "isReadable");
                            try {
                                handleRead(key, queue);
                            } catch (IOException e) {
                                e.printStackTrace();
                                // TODO: notify uplevel that socket channel is closed by server peer
                                flagExcept = true;
                            }
                        } else if (key.isWritable()) {
                            Log.d("Session", "isWritable");
                            try {
                                handleSend();
                            } catch (IOException e) {
                                e.printStackTrace();
                                // TODO: notify uplevel that socket channel is closed by server peer
                                flagExcept = true;
                            }
                        }
                        if(flagExcept){
                            try{
                                // put PingCmd in the queue to indicate that session is closed.
                                queue.put(new Cmd().PingCmd());
                            }catch (InterruptedException e) {
                                e.printStackTrace();
                            }finally {
                                closeFlag = true;
                                break;
                            }
                        }
                        synchronized(this) {
                            if (closeFlag) {
                                break;
                            }
                        }
                    }
                }
                closeInternal();
                status = SESSION_NO_CONNECTION;
                attempt = 0;
                Log.d(TAG, "IOThread end");
            }
        };

        ioThread = new Thread(ioTask);
        ioThread.start();
        return ioThread;
    }
}
