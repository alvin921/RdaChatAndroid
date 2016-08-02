package com.rda.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.rda.Config.Config;
import com.rda.MyApp;
import com.rda.libnet.Session;
import com.rda.protocol.ChatData;
import com.rda.protocol.Cmd;
import com.rda.protocol.GlobData;
import com.rda.protocol.MsgData;
import com.rda.protocol.Person;
import com.rda.protocol.Topic;
import com.rda.util.DebugInfo;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by mingangwang on 2016/7/30.
 */
public class TcpClientService extends Service {
    //public static final String TAG = "com.rda.services.TcpClientService";
    public static final String TAG = "TcpClientService";

    private final IBinder mBinder = new LocalBinder();

    public synchronized Boolean getStopFlag() {
        return stopFlag;
    }

    public synchronized void setStopFlag(Boolean stopFlag) {
        this.stopFlag = stopFlag;
    }

    Boolean stopFlag = true;
    Session session = null;
    private static final int CMD_Q_SIZE = 100;
    private String serviceName = TAG;
    public String getServiceName() {return serviceName; }
    public void setServiceName(String name) { serviceName = name; }

    private ArrayBlockingQueue<Cmd> queue = new ArrayBlockingQueue<Cmd>(CMD_Q_SIZE);
    private Thread ioThread;
    private Thread hbThread;
    private Thread rdThread;
    private int heartbeat; // seconds
    private String host;
    private GlobData gData;

    public class LocalBinder extends Binder {
        public TcpClientService getService() {
            return TcpClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.w(serviceName, "OnBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.w(serviceName, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.w(TAG, new DebugInfo().toString());
        super.onCreate();
        gData = ((MyApp)getApplication()).getData();
    }

    @Override
    public void onDestroy() {
        Log.w(serviceName, new DebugInfo().toString());
        super.onDestroy();
    }

    void notify(Cmd cmd) {
        Intent intent = new Intent();
        int status = session.getStatus();
        intent.putExtra("status", status);
        if (cmd != null) {
            intent.putExtra("cmd", cmd);
            //intent.putExtra(Cmd.SELECT_MSG_SERVER_FOR_CLIENT_CMD, (String)cmd.getArg(0));
        }
        if (status == Session.SESSION_CLOSED) {
            Log.d(TAG, "status=SESSION_CLOSED");
        }else if (status == Session.SESSION_NO_CONNECTION) {
            Log.d(TAG, "status=SESSION_NO_CONNECTION");
        }else if (status == Session.SESSION_CONNECTING){
            Log.d(TAG, "status=SESSION_CONNECTING");
        }else if(status == Session.SESSION_CONNECTED) {
            Log.d(TAG, "status=SESSION_CONNECTED");
            if(cmd != null)
                Log.d(TAG, "cmd="+cmd.toString());
        }
        intent.setAction(serviceName);
        sendBroadcast(intent);
    }
    void doConnect() {
        Log.i(serviceName, "Connecting");
        try {
            notify(null);
            session.connect(10*1000); // wait 10s for connection
        } catch (IOException e) {
            System.out.println("error.....");
            e.printStackTrace();
        }
        notify(null);
    }


    public void send(Cmd cmd) throws IOException {
        //try {
            session.sendPacket(cmd);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }
    private void procLoginRsp(Cmd cmd) {
        Log.i(serviceName, cmd.getCmdName());
        Cmd.CommonRsp data = ( Cmd.CommonRsp)cmd.parse();
        if(data.Ack.compareToIgnoreCase(Cmd.RSP_SUCCESS) == 0){
            ;
        }
    }
    private void procP2PMsg(Cmd cmd) {
        Log.i(serviceName, "IND_SEND_P2P_MSG_CMD");
        Cmd.P2PMsgInd data = (Cmd.P2PMsgInd)cmd.parse();
        ChatData cd = gData.getChatData(data.FromID);
        if(cd == null){
            cd = new ChatData(data.FromID, ChatData.CHAT_TYPE_P2P);
            gData.addChatData(cd);
        }
        cd.addMsgData(new MsgData(data.FromID, data.Msg));
    }
    private void procTopicMsg(Cmd cmd) {
        Log.i(serviceName, "IND_SEND_P2P_MSG_CMD");
        Cmd.TopicMsgInd msg = (Cmd.TopicMsgInd)cmd.parse();
        ChatData cd = gData.getChatData(msg.TopicName);
        if(cd == null){
            cd = new ChatData(msg.TopicName, ChatData.CHAT_TYPE_TOPIC);
            gData.addChatData(cd);
        }
        cd.addMsgData(new MsgData(msg.ID, msg.Msg));
    }
    void doProcess(){
        // resolve host name--->ipaddr, if failed, notity NO_CONNECTION
        Log.d(serviceName, "doProcess begin");
        try {
            queue.clear();
            session = new Session(host, queue);
        } catch (IOException e) {
            Log.i(serviceName, "session create error");
            System.out.println("error.....");
            e.printStackTrace();
            notify(null);
            session = null;
            return;
        }
        // connect to server, if failed, attempt to reconnect
        while(session.getStatus() != Session.SESSION_CONNECTED && session.shouldAttemptConnect()){
            doConnect();
        }
        // after MAX_ATTEMPT_CONNECT times retries, notify NO_CONNECTION
        if(session.getStatus() != Session.SESSION_CONNECTED){
            Log.i(serviceName, "connect failed");
            notify(null);
            session.close();
            session = null;
            return;
        }
        // successfully CONNECTED
        Log.i(serviceName, "Connected");

        // create thread to send heartbeat packet
        ioThread = session.startIOThread();
        if(heartbeat > 0)
            hbThread = session.startHBThread(heartbeat);

        // process received Cmd
        stopFlag = false;
        while(!stopFlag){
            try {
                Cmd cmd = queue.take();
                Log.d(TAG, cmd.toString());
                System.out.println("从队列取走一个元素，队列剩余"+queue.size()+"个元素");
                switch (cmd.getCmdName()) {
                    case Cmd.SEND_PING_CMD: {
                        Log.d(TAG, new DebugInfo()+" session closed");
                        // stopFlag==true, means that user try to stop the service, otherwise, something wrong with low level session
                        if(!stopFlag) {
                            stopFlag = true;
                            notify(null);
                        }
                        break;
                    }
                    case Cmd.RSP_LOGIN_CMD:
                        procLoginRsp(cmd);
                        break;
                    case Cmd.IND_SEND_P2P_MSG_CMD:
                        procP2PMsg(cmd);
                        break;
                    case Cmd.IND_SEND_TOPIC_MSG_CMD:
                        procTopicMsg(cmd);
                        break;
                    case Cmd.RSP_GET_TOPIC_LIST_CMD: {
                        Cmd.TopicListRsp msg = (Cmd.TopicListRsp)cmd.parse();
                        if(msg.Ack.compareToIgnoreCase(Cmd.RSP_SUCCESS) == 0){
                            gData.getMe().setTopicList(msg.List);
                        }
                        break;
                    }
                    case Cmd.RSP_CREATE_TOPIC_CMD: {
                        Cmd.CreateTopicRsp msg = (Cmd.CreateTopicRsp)cmd.parse();
                        Log.i(TAG, "RSP_CREATE_TOPIC_CMD: "+msg.Ack);
                        if (msg.Ack.compareToIgnoreCase(Cmd.RSP_SUCCESS) == 0) {
                            Topic topic = new Topic(msg.TopicName, msg.Alias);
                            topic.addMember(gData.getMe().getID(), msg.Alias, Cmd.DEV_TYPE_CLIENT);
                            gData.getTopicList().add(topic);
                            gData.getMe().addTopic(msg.TopicName);
                        }
                        break;
                    }
                }
                if(cmd.getCmdName() != Cmd.SEND_PING_CMD){
                    notify(cmd);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        session.close();
        session = null;
        rdThread = null;
        ioThread = null;
        hbThread = null;
        Log.i(serviceName, "doProcess end");
    }

    public void start(String host, int heartbeat) {
        this.heartbeat = heartbeat;
        this.host = host;

        rdThread = new Thread(new Runnable() {
            @Override
            public void run(){
                doProcess();
            }
        });
        rdThread.start();
    }

    public void stop() {
        if(session != null){
            stopFlag = true;
            try{
                // put PingCmd in the queue to indicate that rdThread should exit
                queue.put(new Cmd().PingCmd());
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            // wait for rdThread exit;
            while(rdThread != null){
                try {
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
