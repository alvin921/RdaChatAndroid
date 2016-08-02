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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rda.MyApp;
import com.rda.libnet.Session;
import com.rda.protocol.ChatData;
import com.rda.protocol.Cmd;
import com.rda.protocol.GlobData;
import com.rda.protocol.Topic;
import com.rda.services.TcpClientService;

import com.rda.chat.R;

/**
 * Created by mingangwang on 2016/7/25.
 */
public class ProfileActivity extends MyActivity{
    private static final String TAG = "ProfileActivity";
    private ListView listView = null;
    private MsgResReceiver receiver;
    private GlobData gData;
    private String mID;
    private AlertDialog alertDialog=null;

    private String newName;
    private String newAlias;

    private boolean fromChat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setActivityName(TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        gData = ((MyApp) getApplication()).getData();
        listView = (ListView) findViewById(R.id.list_view);
        final Intent oldIntent = getIntent();
        Bundle bdl = oldIntent.getBundleExtra("");
        mID = oldIntent.getStringExtra("Name");
        fromChat = oldIntent.getBooleanExtra("FromChat", false);

        ((TextView)findViewById(R.id.tv_id)).setText(mID);
        ((TextView)findViewById(R.id.tv_name)).setText(mID);

        //Intent intent = new Intent(ChatActivity.this, MsgService.class);
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        receiver = new MsgResReceiver();
        IntentFilter filter = new IntentFilter(TcpClientService.TAG);
        registerReceiver(receiver, filter);


        findViewById(R.id.btn_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromChat){
                    ProfileActivity.this.finish();
                }else {
                    startActivity(new Intent(ProfileActivity.this, ChatActivity.class)
                            .putExtra("Name", mID)
                            .putExtra("Type", ChatData.CHAT_TYPE_TOPIC)
                            .putExtra("FromProfile", true)
                    );
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent intent = new Intent(MsgListActivity.this, ChatActivity.class);

                //startActivity(intent);
            }
        });

        ((ImageButton) findViewById(R.id.btn_more)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           }
        });

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
                    case Cmd.RSP_ADD_2_TOPIC_CMD: {
                    }
                    break;
                }
            }
        }
    }
}
