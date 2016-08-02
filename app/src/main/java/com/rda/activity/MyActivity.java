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
import com.rda.protocol.Person;
import com.rda.services.TcpClientService;
import com.rda.util.DebugInfo;

import java.io.IOException;



/**
 * Created by mingangwang on 2016/8/1.
 */
public class MyActivity extends Activity {
    private String activityName = "MyActivity";
    public void setActivityName(String name) { activityName = name; }
    public String getActivityName() { return activityName; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(activityName, new DebugInfo().toString());
        super.onCreate(savedInstanceState);
        MyApp.getInstance().pushActivity(this);
    }
    @Override
    protected void onDestroy() {
        Log.d(activityName, new DebugInfo().toString());
        super.onDestroy();
        MyApp.getInstance().popActivity(this);

    }

    @Override
    protected void onStart() {
        Log.d(activityName, new DebugInfo().toString());
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(activityName, new DebugInfo().toString());
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Log.d(activityName, new DebugInfo().toString());
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(activityName, new DebugInfo().toString());
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(activityName, new DebugInfo().toString());
        super.onStop();
    }
}
