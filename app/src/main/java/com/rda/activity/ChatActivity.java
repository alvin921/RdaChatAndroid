package com.rda.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rda.Config.Config;
import com.rda.MyApp;
import com.rda.libnet.Session;
import com.rda.protocol.ChatData;
import com.rda.protocol.Cmd;
import com.rda.protocol.GlobData;
import com.rda.protocol.MsgData;
import com.rda.services.TcpClientService;

import java.io.IOException;
import java.util.ArrayList;

import com.rda.chat.R;

/**
 * Created by mingangwang on 2016/7/25.
 */
public class ChatActivity extends MyActivity{
    private static final String TAG = "ChatActivity";
    private ListView msgList = null;
    private MsgResReceiver receiver;
    private GlobData gData;
    private ChatData chatData;
    private String chatName;
    private boolean chatType;
    private MsgCellAdapter adapter;

    private boolean mBound = false;
    //private TcpClientService mService;
    private EditText etMsg;

    private boolean fromProfile;

    public void switchChat(String Name, boolean Type){
        if(Name.compareTo(chatName) == 0)
            return;
        chatName = Name;
        chatType = Type;
        chatData = gData.getChatData(chatName);
        ((TextView)findViewById(R.id.tv_title)).setText(chatName);

        adapter.changeData(chatData);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setActivityName(TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        gData = ((MyApp) getApplication()).getData();
        msgList = (ListView) findViewById(R.id.msg_list);
        final Intent oldIntent = getIntent();
        Bundle bdl = oldIntent.getBundleExtra("");
        chatName = oldIntent.getStringExtra("Name");
        chatType = oldIntent.getBooleanExtra("Type", ChatData.CHAT_TYPE_TOPIC);
        fromProfile = oldIntent.getBooleanExtra("FromProfile", false);
        chatData = gData.getChatData(chatName); // maybe null
        if(chatData != null)
            chatData.setUnread(0);

        ((TextView)findViewById(R.id.tv_title)).setText(chatName);

        etMsg = (EditText)findViewById(R.id.edit_msg);

        //Intent intent = new Intent(ChatActivity.this, MsgService.class);
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        receiver = new MsgResReceiver();
        IntentFilter filter = new IntentFilter(TcpClientService.TAG);
        registerReceiver(receiver, filter);


        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = etMsg.getText().toString();
                if(msg == null || msg.length() == 0)
                    return;
                try {
                    if(chatType == ChatData.CHAT_TYPE_P2P){
                        gData.getService().send(new Cmd().P2PMsgCmd(chatName, msg));
                    }else{
                        gData.getService().send(new Cmd().TopicMsgCmd(chatName, msg));
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    if (chatData == null) {
                        chatData = new ChatData(chatName, chatType);
                        gData.addChatData(chatData);
                        adapter.changeData(chatData);
                    }else{
                        adapter.notifyDataSetChanged();
                    }
                    chatData.addMsgData(new MsgData(gData.getMe().getID(), msg));
                    chatData.setUnread(0);
                    etMsg.setText("");
                }
            }
        });

        findViewById(R.id.btn_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromProfile == false){
                    startActivity(new Intent(ChatActivity.this, (chatType == ChatData.CHAT_TYPE_P2P)?ProfileActivity.class:TopicProfileActivity.class)
                            .putExtra("Name", chatName)
                            .putExtra("FromChat", true)
                    );
                }else{
                    ChatActivity.this.finish();
                }
            }
        });

        // 绑定XML中的ListView，作为Item的容器

        adapter = new MsgCellAdapter(ChatActivity.this, chatData);
        msgList.setAdapter(adapter);

        msgList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent intent = new Intent(MsgListActivity.this, ChatActivity.class);

                //startActivity(intent);
            }
        });
    }


    public class MsgCellAdapter extends BaseAdapter {
        private int[] colors = new int[] { 0xff3cb371, 0xffa0a0a0 };
        private Context mContext;
        private LayoutInflater mInflater;
        ChatData data;

        public MsgCellAdapter(Context context, ChatData data) {
            this.mContext = context;
            this.data = data;
            this.mInflater = LayoutInflater.from(context);
        }
        public void changeData(ChatData data){
            this.data = data;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            if(data == null)return 0;
            return data.getSize();
        }

        @Override
        public Object getItem(int position) {
            return data.getMsgData(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.activity_chat_list_cell, null);//根据布局文件实例化view
            TextView title = (TextView) convertView.findViewById(R.id.ItemTitle);//找某个控件
            title.setText(data.getMsgData(position).getName());//给该控件设置数据(数据从集合类中来)
            TextView time = (TextView) convertView.findViewById(R.id.ItemText);//找某个控件
            time.setText(data.getMsgData(position).getMsg());//给该控件设置数据(数据从集合类中来)
            ImageView img = (ImageView) convertView.findViewById(R.id.ItemImage);
            img.setImageResource(R.drawable.touxiang);

            return convertView;
        }

        public void addItem(ChatData data, Cmd cmd){

            notifyDataSetChanged();
        }
    }

    public class MsgResReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive");
            if (intent.getAction().equals(TcpClientService.TAG)) {
                int status = intent.getIntExtra("status", Session.SESSION_CONNECTED);
                Cmd cmd = (Cmd)intent.getSerializableExtra("cmd");
                if(status != Session.SESSION_CONNECTED || cmd == null)return;

                Log.i(TAG, cmd.toString());

                switch (cmd.getCmdName()) {
                    case Cmd.IND_SEND_P2P_MSG_CMD: {
                        Log.i(TAG, "IND_SEND_P2P_MSG_CMD");
                        Cmd.P2PMsgInd data = (Cmd.P2PMsgInd) cmd.parse();
                        if (data.FromID.compareTo(chatName) == 0) {
                            if (chatData == null) {
                                chatData = gData.getChatData(chatName);
                                adapter.changeData(chatData);
                            }else{
                                adapter.notifyDataSetChanged();
                            }
                            chatData.setUnread(0);
                        }
                        break;
                    }
                    case Cmd.IND_SEND_TOPIC_MSG_CMD: {
                        Log.i(TAG, "IND_SEND_TOPIC_MSG_CMD");
                        Cmd.TopicMsgInd msg = (Cmd.TopicMsgInd) cmd.parse();
                        if (msg.TopicName.compareTo(chatName) == 0) {
                            if (chatData == null) {
                                chatData = gData.getChatData(chatName);
                                adapter.changeData(chatData);
                            }else{
                                adapter.notifyDataSetChanged();
                            }
                            chatData.setUnread(0);
                        }
                        break;
                    }
                }
            }
        }
    }
}
