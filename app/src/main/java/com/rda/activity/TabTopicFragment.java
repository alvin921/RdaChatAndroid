package com.rda.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.rda.protocol.Me;
import com.rda.protocol.Topic;
import com.rda.services.TcpClientService;

import java.io.IOException;
import java.util.ArrayList;

import com.rda.chat.R;

public class TabTopicFragment extends Fragment {
	private static final String TAG = "TabTopicFragment";
	private ListView listView = null;
	private MsgResReceiver receiver;
	private GlobData gData;
	private ArrayList<Topic> topicList;
	private TopicListAdapter adapter;
	private TcpClientService mService;
	int[] images = new int[] { R.drawable.touxiang };
	private String newTopicName=null;
	private String newAlias = null;
	private AlertDialog alertDialog=null;

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		getActivity().unregisterReceiver(receiver);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.tab_topic, container, false);
		listView = (ListView) view.findViewById(R.id.tab02ListView);

		MyApp app = (MyApp)this.getActivity().getApplication();
		gData = app.getData();
		topicList = gData.getTopicList();

		((ImageView) view.findViewById(R.id.iv_search)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});

		((ImageView) view.findViewById(R.id.iv_refresh)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try{
					gData.getService().send(new Cmd().TopicListCmd());
				}catch (IOException e){
					e.printStackTrace();
				}
			}
		});

		((ImageView) view.findViewById(R.id.iv_add)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LinearLayout layout = (LinearLayout)LayoutInflater.from(getActivity()).inflate(R.layout.dlg_new_topic, null);
				alertDialog = new AlertDialog.Builder(getActivity())
						.setTitle(R.string.input_new_topic)
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
									Toast.makeText(getActivity(), "topic name can_not_be_empty", Toast.LENGTH_LONG).show();
									return;
								}
								newTopicName = etName.getText().toString();
								if(TextUtils.isEmpty(etAlias.getText()))
									newAlias = Config.getCachedName(getActivity());//gData.getPerson().getName();
								else
									newAlias = etAlias.getText().toString();
								Log.i("NewTopicDialog", newTopicName+","+newAlias);
								try{
									gData.getService().send(new Cmd().CreateTopicCmd(newTopicName, newAlias));
								}catch(IOException e){
									e.printStackTrace();
								}


								((AlertDialog)dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
								((AlertDialog)dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
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

		adapter = new TopicListAdapter(getActivity(), gData.getMe());
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				/*
				Intent intent = new Intent(getActivity(), ChatActivity.class);
				intent.putExtra("Name", topicList.get(position).getName());
				intent.putExtra("Type", ChatData.CHAT_TYPE_TOPIC);
				*/
				Intent intent = new Intent(getActivity(), TopicProfileActivity.class);
				intent.putExtra("Name", gData.getMe().getTopic(position));
				startActivity(intent);
			}
		});

		receiver = new MsgResReceiver();
		IntentFilter filter = new IntentFilter(TcpClientService.TAG);
		getActivity().registerReceiver(receiver, filter);

		return view;
	}
	public class TopicListAdapter extends BaseAdapter {
		private int[] colors = new int[] { 0xff3cb371, 0xffa0a0a0 };
		private Context mContext;
		private LayoutInflater mInflater;
		private Me me;

		public TopicListAdapter(Context context, Me me) {
			this.mContext = context;
			this.me = me;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return me.numTopic();
		}

		@Override
		public Object getItem(int position) {
			return me.getTopic(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = mInflater.inflate(R.layout.list_cell_1_line_image_text, null);//根据布局文件实例化view

			ImageView img = (ImageView) convertView.findViewById(R.id.ItemImage);
			img.setImageResource(R.drawable.topic_profile);

			TextView title = (TextView) convertView.findViewById(R.id.ItemText);//找某个控件
			title.setText(me.getTopic(position));//给该控件设置数据(数据从集合类中来)

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
				if(status != Session.SESSION_CONNECTED || cmd == null)
					return;
				Log.i(TAG, cmd.toString());

				switch (cmd.getCmdName()) {
					case Cmd.RSP_GET_TOPIC_LIST_CMD: {
						Cmd.TopicListRsp msg = (Cmd.TopicListRsp)cmd.parse();
						if(msg.Ack.compareToIgnoreCase(Cmd.RSP_SUCCESS) == 0){
							adapter.notifyDataSetChanged();
						}
						break;
					}
					case Cmd.RSP_CREATE_TOPIC_CMD: {
						Cmd.CreateTopicRsp data = (Cmd.CreateTopicRsp)cmd.parse();
						Log.i(TAG, "RSP_CREATE_TOPIC_CMD: "+data.Ack);
						if (data.Ack.compareToIgnoreCase(Cmd.RSP_SUCCESS) == 0) {
							adapter.notifyDataSetChanged();
						}else {
							Toast.makeText(getActivity(), "RSP_CREATE_TOPIC_CMD: "+newTopicName+data.Ack, Toast.LENGTH_LONG).show();
						}
						alertDialog.dismiss();
						alertDialog = null;
						newTopicName = null;
						break;
					}
				}
			}
		}
	}
}
