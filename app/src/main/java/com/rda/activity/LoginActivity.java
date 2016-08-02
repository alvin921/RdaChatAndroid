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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rda.Config.Config;
import com.rda.MyApp;
import com.rda.libnet.Session;
import com.rda.protocol.Cmd;
import com.rda.protocol.GlobData;
import com.rda.protocol.Person;
import com.rda.protocol.Protocol;
import com.rda.services.TcpClientService;

import java.io.IOException;

import com.rda.chat.R;

public class LoginActivity extends MyActivity {
    private static final String TAG = "LoginActivity";
    private Button btnLogin = null;
    private EditText etAddr = null;
    private EditText etName = null;
    private EditText etPassword = null;
    boolean mBound = false;
    GlobData gData;
    TcpClientService mService;
    private LoginResReceiver receiver;

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setActivityName(TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        gData = ((MyApp)getApplication()).getData();
        mService = gData.getService();

        btnLogin = (Button) this.findViewById(R.id.btn_login);
        Button btnExit = (Button) this.findViewById(R.id.btn_exit);
        etAddr = (EditText) findViewById(R.id.addr_edit);
        etName = (EditText) findViewById(R.id.username_edit);
        etPassword = (EditText) findViewById(R.id.password_edit);

        receiver = new LoginResReceiver();
        IntentFilter filter = new IntentFilter(TcpClientService.TAG);
        registerReceiver(receiver, filter);

        etAddr.setText(Config.GATEWAY_ADDR);
        etName.setText(Config.getCachedName(LoginActivity.this));
        //etAddr.setText(Config.GATEWAY_ADDR.toCharArray(), 0, Config.GATEWAY_ADDR.length());
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etName.getText())) {
                    Toast.makeText(LoginActivity.this, "user name can_not_be_empty", Toast.LENGTH_LONG).show();
                    return;
                }
                Config.cacheName(LoginActivity.this, etName.getText().toString());
                Config.cachePassword(LoginActivity.this, etPassword.getText().toString());
                Config.setString(LoginActivity.this, Config.KEY_GW_ADDR, etAddr.getText().toString());
                mService.start(etAddr.getText().toString(), 0);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MyApp.getInstance().exit();
                ((MyApp)getApplication()).exit();
            }
        });
    }

    public class LoginResReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            int status = intent.getIntExtra("status", Session.SESSION_CONNECTED);
            if(status == Session.SESSION_NO_CONNECTION){
                Log.d(TAG, "SESSION_NO_CONNECTION");
                Toast.makeText(LoginActivity.this, "NO_CONNECTION: "+Config.getString(LoginActivity.this, Config.KEY_GW_ADDR), Toast.LENGTH_LONG).show();
                if(gData.isConnected()){
                    mService.stop();
                }
                gData.setConnected(false);
            }else if(status == Session.SESSION_CONNECTING){
                Log.d(TAG, "SESSION_CONNECTING");
                gData.setConnected(false);
                Toast.makeText(LoginActivity.this, "Connecting to "+Config.getString(LoginActivity.this, Config.KEY_GW_ADDR), Toast.LENGTH_LONG).show();
            }else if(status == Session.SESSION_CLOSED){
                Log.d(TAG, "SESSION_CLOSED");
                gData.setConnected(false);
                Toast.makeText(LoginActivity.this, "Connected to "+Config.getString(LoginActivity.this, Config.KEY_GW_ADDR), Toast.LENGTH_LONG).show();
            }else{ // Connected
                Log.d(TAG, "SESSION_CONNECTED");
                Cmd cmd = (Cmd)intent.getSerializableExtra("cmd");
                if(cmd == null){
                    if(!gData.isConnected()) {
                        gData.setConnected(false);
                        try {
                            Log.d(TAG, "connected and send login request");
                            mService.send(new Cmd().LoginGatewayCmd(Config.getCachedName(LoginActivity.this), Person.PERSON_TYPE_GUARDER, Config.getCachedPassword(LoginActivity.this)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
               Log.i(TAG, cmd.toString());

                switch (cmd.getCmdName()) {
                    case Cmd.RSP_LOGIN_CMD: {
                        Cmd.LoginRsp rsp = (Cmd.LoginRsp) cmd.parse();
                        if (rsp.Ack.compareToIgnoreCase(Cmd.RSP_SUCCESS) == 0) {
                            Log.d(TAG, "Login success.");
                            Config.setString(LoginActivity.this, Config.KEY_UUID, rsp.UUID);
                            Config.setString(LoginActivity.this, Config.KEY_SERVER_ADDR, rsp.SAddr);

                            mService.stop();
                            Intent i = new Intent(LoginActivity.this, ChatMainActivity.class);
                            startActivity(i);
                            LoginActivity.this.finish();
                        } else {
                            Log.d(TAG, "Login failed.");
                            Toast.makeText(LoginActivity.this, "Login failed: " + cmd.getArg(0), Toast.LENGTH_LONG).show();
                        }
                        break;
                    }
                }
            }
        }
    }

}
