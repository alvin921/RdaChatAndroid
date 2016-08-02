package com.rda.activity;

import android.app.Application;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jauker.widget.BadgeView;
import com.rda.Config.Config;
import com.rda.MyApp;
import com.rda.libnet.Session;
import com.rda.protocol.ChatData;
import com.rda.protocol.Cmd;
import com.rda.protocol.GlobData;
import com.rda.protocol.MsgData;
import com.rda.protocol.Protocol;
import com.rda.services.TcpClientService;
import com.rda.util.DebugInfo;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.rda.chat.R;

public class TabChatFragment extends Fragment {
    private static final String TAG = "TabChatFragment";
    private ListView msgList = null;
    private TextView tvTitle = null;
    public TextView getTitleView() { return tvTitle; }
    private GlobData gData;
    private ArrayList<ChatData> chatList;
    private ChatListAdapter adapter;
    public ChatListAdapter getAdapter() { return adapter; }
    int[] images = new int[] { R.drawable.ic_launcher };
    boolean connectOK = false;


    @Override
    public void onDestroyView(){
        Log.d(TAG, new DebugInfo().toString());
        super.onDestroyView();
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
        Log.d(TAG, new DebugInfo().toString());
        View view = inflater.inflate(R.layout.tab_chat, container, false);
        msgList = (ListView) view.findViewById(R.id.tab01ListView);
        tvTitle = (TextView)view.findViewById(R.id.tv_title);

        MyApp app = (MyApp)this.getActivity().getApplication();
        gData = app.getData();
        gData.setConnected(false);
        chatList = gData.getChatList();

        adapter = new ChatListAdapter(getActivity(), chatList);
        msgList.setAdapter(adapter);

        msgList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("Name", chatList.get(position).getName());
                intent.putExtra("Type", chatList.get(position).getType());
                startActivity(intent);
            }
        });

        return view;
	}
    public class ChatListAdapter extends BaseAdapter {
        private int[] colors = new int[] { 0xff3cb371, 0xffa0a0a0 };
        private Context mContext;
        private LayoutInflater mInflater;
        ArrayList<ChatData> data;
        Map<String, BadgeView> map = new HashMap<String, BadgeView>();

        public ChatListAdapter(Context context, ArrayList<ChatData> data) {
            this.mContext = context;
            this.data = data;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.list_cell_2_line_image_text_text, null);//根据布局文件实例化view
            RelativeLayout layout = (RelativeLayout)convertView;

            ChatData cd = data.get(position);

            ImageView img = (ImageView) convertView.findViewById(R.id.ItemImage);
            img.setImageResource(R.drawable.touxiang);

            TextView title = (TextView) convertView.findViewById(R.id.ItemTitle);//找某个控件
            title.setText(cd.getName());//给该控件设置数据(数据从集合类中来)

            BadgeView view = map.get(cd.getName());
            if(view != null){
                map.remove(cd.getName());
                layout.removeView(view);
            }
            if(cd.getSize() > 0){
                MsgData md = cd.getMsgData(cd.getSize()-1);
                TextView text = (TextView) convertView.findViewById(R.id.ItemText);//找某个控件
                text.setText(md.getName()+":"+md.getMsg());//给该控件设置数据(数据从集合类中来)
            }
            if(cd.getUnread() > 0){
                view = new BadgeView(getActivity());
                view.setBadgeCount(cd.getUnread());
                layout.addView(view);
                map.put(cd.getName(), view);
            }

            return convertView;
        }
    }


}
