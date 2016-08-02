package com.rda.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rda.Config.Config;
import com.rda.MyApp;
import com.rda.libnet.Session;
import com.rda.protocol.ChatData;
import com.rda.protocol.Cmd;
import com.rda.protocol.GlobData;
import com.rda.protocol.Topic;
import com.rda.services.TcpClientService;

import java.io.IOException;

import com.rda.chat.R;

/**
 * Created by mingangwang on 2016/7/25.
 */
public class TopicProfileActivity extends MyActivity{
    public static final String TAG = "TopicProfileActivity";
    private ListView listView = null;
    private MsgResReceiver receiver;
    private GlobData gData;
    private Topic mTopic;
    private String mTopicName;
    private TopicMemberAdapter adapter;
    private AlertDialog alertDialog=null;

    private TcpClientService mService;

    private String newName;
    private String newAlias;

    private boolean fromChat;

/*
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            MsgService.LocalBinder binder = (MsgService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
*/
    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setActivityName(TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_profile);

        gData = ((MyApp) getApplication()).getData();
        listView = (ListView) findViewById(R.id.list_view);

        ((TextView)findViewById(R.id.tv_title)).setText(mTopicName);

        //Intent intent = new Intent(ChatActivity.this, MsgService.class);
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        receiver = new MsgResReceiver();
        IntentFilter filter = new IntentFilter(TcpClientService.TAG);
        registerReceiver(receiver, filter);

        final Intent oldIntent = getIntent();
        mTopicName = oldIntent.getStringExtra("Name");
        fromChat = oldIntent.getBooleanExtra("FromChat", false);
        mTopic = gData.getTopic(mTopicName); // maybe null
        if(mTopic == null) {
            Log.d(TAG, "topic profile not exist");
            try{
                gData.getService().send(new Cmd().TopicProfileCmd(mTopicName));
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        findViewById(R.id.btn_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromChat){
                    TopicProfileActivity.this.finish();
                }else {
                    startActivity(new Intent(TopicProfileActivity.this, ChatActivity.class)
                            .putExtra("Name", mTopicName)
                            .putExtra("Type", ChatData.CHAT_TYPE_TOPIC)
                            .putExtra("FromProfile", true)
                    );
                }
            }
        });

        adapter = new TopicMemberAdapter(TopicProfileActivity.this, mTopic);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent intent = new Intent(MsgListActivity.this, ChatActivity.class);

                //startActivity(intent);
            }
        });

        ((ImageButton) findViewById(R.id.btn_add)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout = (LinearLayout) LayoutInflater.from(TopicProfileActivity.this).inflate(R.layout.dlg_new_member, null);
                alertDialog = new AlertDialog.Builder(TopicProfileActivity.this)
                        .setTitle(R.string.add_to_topic)
                        .setIcon(R.drawable.ic_launcher)
                        .setView(layout)
                        .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //LinearLayout layout = (LinearLayout)getView();
                                //EditText etName = (EditText)layout.findViewById(R.id.edit_name);
                                //EditText etAlias = (EditText)layout.findViewById(R.id.edit_alias);
                                EditText etName = (EditText)((AlertDialog)dialogInterface).findViewById(R.id.edit_name);
                                EditText etAlias = (EditText)((AlertDialog)dialogInterface).findViewById(R.id.edit_alias);
                                if (TextUtils.isEmpty(etName.getText())) {
                                    Toast.makeText(TopicProfileActivity.this, "member name can_not_be_empty", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                newName = etName.getText().toString();
                                if(TextUtils.isEmpty(etAlias.getText()))
                                    newAlias = "";//gData.getPerson().getName();
                                else
                                    newAlias = etAlias.getText().toString();
                                Log.i("NewTopicDialog", newName+","+newAlias);

                                ((AlertDialog)dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                ((AlertDialog)dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                                try {
                                    gData.getService().send(new Cmd().Add2TopicCmd(mTopicName, newName, newAlias));
                                }catch (IOException e){
                                    e.printStackTrace();
                                    dialogInterface.dismiss();
                                }
                                //dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });

    }
    public class TopicMemberAdapter extends BaseAdapter {
        private int[] colors = new int[] { 0xff3cb371, 0xffa0a0a0 };
        private Context mContext;
        private LayoutInflater mInflater;
        Topic data;

        public TopicMemberAdapter(Context context, Topic data) {
            this.mContext = context;
            this.data = data;
            this.mInflater = LayoutInflater.from(context);
        }
        public void setData(Topic data){
            this.data = data;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            if(data == null)
                return 0;
            return data.numMember();
        }

        @Override
        public Topic.Member getItem(int position) {
            return data.getMember(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.list_cell_1_line_image_text, null);//根据布局文件实例化view
            Topic.Member m = data.getMember(position);

            ImageView img = (ImageView) convertView.findViewById(R.id.ItemImage);
            img.setImageResource(R.drawable.touxiang);

            TextView title = (TextView) convertView.findViewById(R.id.ItemText);//找某个控件
            title.setText(m.getName());//给该控件设置数据(数据从集合类中来)

            return convertView;
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
                    case Cmd.RSP_GET_TOPIC_PROFILE_CMD: {
                        Cmd.TopicProfileRsp msg = (Cmd.TopicProfileRsp)cmd.parse();
                        if(msg.Ack.compareToIgnoreCase(Cmd.RSP_SUCCESS) == 0){
                            Log.d(TAG, "update topic profile and notify");
                            mTopic = msg.topic;
                            gData.addTopic(msg.topic);
                            adapter.setData(mTopic);
                        }else{
                            Log.d(TAG, "get topic profile failed");
                            Toast.makeText(TopicProfileActivity.this, "get topic profile failed: "+mTopicName+msg.Ack, Toast.LENGTH_LONG).show();
                            finish();
                        }
                        break;
                    }
                    case Cmd.RSP_ADD_2_TOPIC_CMD: {
                        Cmd.Add2TopicRsp data = (Cmd.Add2TopicRsp)cmd.parse();
                        Log.i(TAG, "RSP_ADD_2_TOPIC_CMD: "+data.Ack);
                        if(data.TopicName.compareToIgnoreCase(mTopicName) == 0){
                            if (data.Ack.compareToIgnoreCase(Cmd.RSP_SUCCESS) == 0) {
                                mTopic.addMember(data.ID, newAlias, data.Type);
                                adapter.notifyDataSetChanged();
                            }else {
                                Toast.makeText(TopicProfileActivity.this, "RSP_ADD_2_TOPIC_CMD: "+mTopicName+data.Ack, Toast.LENGTH_LONG).show();
                            }
                            alertDialog.dismiss();
                            alertDialog = null;
                            newName = null;
                            newAlias = null;
                        }
                        break;
                    }
                }
            }
        }
    }
}
