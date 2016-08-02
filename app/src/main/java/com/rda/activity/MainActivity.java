package com.rda.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.rda.Config.Config;
import com.rda.MyApp;
import com.rda.protocol.GlobData;
import com.rda.services.TcpClientService;


public class MainActivity extends MyActivity {
	private static final String TAG = "MainActivity";
	private GlobData gData;


	boolean mBound = false;
	TcpClientService mService;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i(TAG, "onServiceConnected");
			TcpClientService.LocalBinder binder = (TcpClientService.LocalBinder) service;
			mService = binder.getService();
			((MyApp)getApplication()).getData().setService(mService);

			mBound = true;

			String clientID = Config.getCachedName(MainActivity.this);
			String password = Config.getCachedPassword(MainActivity.this);
			String uuid = Config.getString(MainActivity.this, Config.KEY_UUID);
			String saddr = Config.getString(MainActivity.this, Config.KEY_SERVER_ADDR);

			if (clientID != null && password != null && saddr != null && uuid != null) {
				startActivity(new Intent(MainActivity.this, ChatMainActivity.class));
			} else {
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};



	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setActivityName(TAG);
		super.onCreate(savedInstanceState);

		bindService(new Intent(MainActivity.this, TcpClientService.class), mConnection, Context.BIND_AUTO_CREATE);
	}


}
