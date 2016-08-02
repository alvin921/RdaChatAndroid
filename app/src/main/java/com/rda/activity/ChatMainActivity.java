package com.rda.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jauker.widget.BadgeView;
import com.rda.Config.Config;
import com.rda.MyApp;
import com.rda.libnet.Session;
import com.rda.protocol.Cmd;
import com.rda.protocol.GlobData;
import com.rda.protocol.Me;
import com.rda.protocol.Person;
import com.rda.services.TcpClientService;
import com.rda.util.DebugInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rda.chat.R;

public class ChatMainActivity extends FragmentActivity {
	private static final String TAG = "ChatMainActivity";
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mDatas;

    private BadgeView mBadgeView;

    ViewPager mViewPager;
    TextView mChatTextView;
    TextView mTopicTextView;
    TextView mContactTextView;
    TextView mMeTextView;
    LinearLayout mChatLinearLayout;

	private ImageView mTabline;
	private int mScreen1_4;

	private int mCurrentPageIndex;

	private GlobData gData;
	private MsgResReceiver receiver;


	TabChatFragment tabChat;
	TabTopicFragment tabTopic;
	TabContactFragment tabContact;
	TabMeFragment tabMe;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, new DebugInfo().toString());
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		MyApp.getInstance().pushActivity(this);

		gData = ((MyApp)getApplication()).getData();
		Me me = new Me(Config.getCachedName(this), Config.getCachedName(this), Person.PERSON_TYPE_GUARDER);
		gData.setMe(me);

		gData.getService().start(Config.getString(this, Config.KEY_SERVER_ADDR), 20);

		initTabLine();
		initView();


		receiver = new MsgResReceiver();
		IntentFilter filter = new IntentFilter(TcpClientService.TAG);
		registerReceiver(receiver, filter);
	}
	@Override
	protected void onDestroy() {
		Log.d(TAG, new DebugInfo().toString());
		super.onDestroy();
		MyApp.getInstance().popActivity(this);
		unregisterReceiver(receiver);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			AlertDialog alertDialog = new AlertDialog.Builder(ChatMainActivity.this)
					.setTitle(R.string.app_name)
					.setIcon(R.drawable.ic_launcher)
					//.setView(layout)
					.setMessage("确定退出吗?")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
							((MyApp)getApplication()).exit();
						}
					})
					.setNegativeButton("取消", null)
					.create();
			alertDialog.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    @Override
    protected void onStart() {
        Log.d(TAG, new DebugInfo().toString());
        super.onStart();

    }

    @Override
    protected void onStop() {
		Log.d(TAG, new DebugInfo().toString());
        super.onStop();
    }
	public class MsgResReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "onReceive");
			if (intent.getAction().equals(TcpClientService.TAG)) {
				int status = intent.getIntExtra("status", Session.SESSION_CONNECTED);
				if(status == Session.SESSION_NO_CONNECTION){
					Log.d(TAG, "SESSION_NO_CONNECTION");
					if(mCurrentPageIndex==0 && tabChat != null && tabChat.getTitleView() != null)
					tabChat.getTitleView().setText("无连接");
					if(gData.isConnected()){
						gData.getService().stop();
					}
					gData.setConnected(false);
				}else if(status == Session.SESSION_CONNECTING){
					Log.d(TAG, "SESSION_CONNECTING");
					if(mCurrentPageIndex==0 && tabChat != null && tabChat.getTitleView() != null)
					tabChat.getTitleView().setText("连接中...");
					gData.setConnected(false);
				}else if(status == Session.SESSION_CLOSED){
					Log.d(TAG, "SESSION_CLOSED");
					if(mCurrentPageIndex==0 && tabChat != null && tabChat.getTitleView() != null)
					tabChat.getTitleView().setText("连接断开...");
					gData.setConnected(false);
				}else{
					Cmd cmd = (Cmd)intent.getSerializableExtra("cmd");
					if(cmd == null){
						if(!gData.isConnected()){
							Log.d(TAG, "SESSION_CONNECTED");
							gData.setConnected(true);
							if(mCurrentPageIndex==0 && tabChat != null && tabChat.getTitleView() != null)
							tabChat.getTitleView().setText("聊天");
							try{
								gData.getService().send(new Cmd().LoginServerCmd(Config.getString(ChatMainActivity.this, Config.KEY_NAME), Config.getString(ChatMainActivity.this, Config.KEY_UUID)));
							}catch (IOException e){
								e.printStackTrace();
							}
						}
						return;
					}
					Log.i(TAG, cmd.toString());

					switch (cmd.getCmdName()) {
						case Cmd.RSP_LOGIN_CMD: {
							Cmd.LoginRsp rsp = (Cmd.LoginRsp)cmd.parse();
							Log.i(TAG, "Login Msg server: "+rsp.Ack);
							if(rsp.Ack.compareTo(Cmd.RSP_SUCCESS) == 0){
								gData.setLogined(true);
							}else{
								gData.setLogined(false);
								((MyApp)getApplication()).popAllActivityExept(MainActivity.class);

								Toast.makeText(ChatMainActivity.this, Config.getString(ChatMainActivity.this, Config.KEY_SERVER_ADDR)+"login failed", Toast.LENGTH_LONG).show();
								Config.setString(ChatMainActivity.this, Config.KEY_SERVER_ADDR, null);
								Config.setString(ChatMainActivity.this, Config.KEY_UUID, null);
								startActivity(new Intent(ChatMainActivity.this, LoginActivity.class));
							}
							break;
						}
						case Cmd.IND_SEND_P2P_MSG_CMD:
							Log.i(TAG, "IND_SEND_P2P_MSG_CMD");
							tabChat.getAdapter().notifyDataSetChanged();
							updateUnread();
							break;
						case Cmd.IND_SEND_TOPIC_MSG_CMD:
							Log.i(TAG, "IND_SEND_TOPIC_MSG_CMD");
							tabChat.getAdapter().notifyDataSetChanged();
							updateUnread();
							break;
					}
				}
			}
		}
	}

    private void initTabLine()
	{
		mTabline = (ImageView) findViewById(R.id.id_iv_tabline);
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		mScreen1_4 = outMetrics.widthPixels / 4;
		LayoutParams lp = mTabline.getLayoutParams();
		lp.width = mScreen1_4;
		mTabline.setLayoutParams(lp);
	}
	public void updateUnread(){
		int n=0;
		for(int i=0; i < gData.numChatData(); i++){
			n += gData.getChatData(i).getUnread();
		}
		if (mBadgeView != null)
		{
			mChatLinearLayout.removeView(mBadgeView);
		}
		mBadgeView = new BadgeView(this);
		mBadgeView.setBadgeCount(n);
		mChatLinearLayout.addView(mBadgeView);

		Log.d(TAG, new DebugInfo().toString()+n);
	}
	private void initView()
	{
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        mChatTextView = (TextView) findViewById(R.id.id_tv_chat);
        mTopicTextView = (TextView) findViewById(R.id.id_tv_topic);
        mContactTextView = (TextView) findViewById(R.id.id_tv_contact);
        mMeTextView = (TextView) findViewById(R.id.id_tv_me);
        mChatLinearLayout = (LinearLayout) findViewById(R.id.id_ll_chat);
        mDatas = new ArrayList<Fragment>();

		mChatTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mViewPager.setCurrentItem(0);
			}
		});
		mTopicTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mViewPager.setCurrentItem(1);
			}
		});
		mContactTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mViewPager.setCurrentItem(2);
			}
		});
		mMeTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mViewPager.setCurrentItem(3);
			}
		});

		tabChat = new TabChatFragment();
		tabTopic = new TabTopicFragment();
		tabContact = new TabContactFragment();
		tabMe = new TabMeFragment();

		mDatas.add(tabChat);
		mDatas.add(tabTopic);
		mDatas.add(tabContact);
        mDatas.add(tabMe);

		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
		{
			@Override
			public int getCount()
			{
				return mDatas.size();
			}

			@Override
			public Fragment getItem(int arg0)
			{
				return mDatas.get(arg0);
			}
		};
		mViewPager.setAdapter(mAdapter);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				resetTextView();
				switch (position)
				{
				case 0:
					//updateUnread();
					mChatTextView.setTextColor(Color.parseColor("#008000"));
					break;
				case 1:
					mContactTextView.setTextColor(Color.parseColor("#008000"));
					break;
                case 2:
                    mTopicTextView.setTextColor(Color.parseColor("#008000"));
                    break;
                case 3:
                    mMeTextView.setTextColor(Color.parseColor("#008000"));
                    break;
				}

				mCurrentPageIndex = position;

			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPx)
			{
				Log.d(TAG, "mCurrentPageIndex="+mCurrentPageIndex);
				Log.d(TAG, position + " , " + positionOffset + " , " + positionOffsetPx);

				LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) mTabline.getLayoutParams();
/*
				if (mCurrentPageIndex == 0 && position == 0)// 0->1
				{
					lp.leftMargin = (int) (positionOffset * mScreen1_4 + mCurrentPageIndex * mScreen1_4);
				} else if (mCurrentPageIndex == 1 && position == 0)// 1->0
				{
					lp.leftMargin = (int) (mCurrentPageIndex * mScreen1_4 + (positionOffset - 1)	* mScreen1_4);
				} else if (mCurrentPageIndex == 1 && position == 1) // 1->2
				{
					lp.leftMargin = (int) (mCurrentPageIndex * mScreen1_4 + positionOffset * mScreen1_4);
				} else if (mCurrentPageIndex == 2 && position == 1) // 2->1
				{
					lp.leftMargin = (int) (mCurrentPageIndex * mScreen1_4 + ( positionOffset-1) * mScreen1_4);
				} else if (mCurrentPageIndex == 2 && position == 2) // 2->3
                {
                    lp.leftMargin = (int) (mCurrentPageIndex * mScreen1_4 + ( positionOffset) * mScreen1_4);
                } else if (mCurrentPageIndex == 3 && position == 1) // 3->2
                {
                    lp.leftMargin = (int) (mCurrentPageIndex * mScreen1_4 + ( positionOffset-1) * mScreen1_4);
                }
*/
				lp.leftMargin = (int) (position * mScreen1_4 + positionOffset * mScreen1_4);
                mTabline.setLayoutParams(lp);

			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{


			}
		});

	}

	protected void resetTextView()
	{
		mChatTextView.setTextColor(Color.BLACK);
		mTopicTextView.setTextColor(Color.BLACK);
		mContactTextView.setTextColor(Color.BLACK);
        mMeTextView.setTextColor(Color.BLACK);
	}

}
