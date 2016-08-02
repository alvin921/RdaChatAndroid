package com.rda.protocol;

import com.rda.services.TcpClientService;

import java.util.ArrayList;

/**
 * Created by mingangwang on 2016/7/21.
 */
public class GlobData {
    private Me me;
    private ArrayList<ChatData> ChatList = new ArrayList<ChatData>();
    private ArrayList<Topic> TopicList = new ArrayList<Topic>();
    private TcpClientService mService;
    private boolean connected;
    private boolean logined;

    public synchronized boolean isConnected(){ return connected; }
    public synchronized void setConnected(boolean flag) { connected = flag; }

    public synchronized boolean isLogined(){ return logined; }
    public synchronized void setLogined(boolean flag) { logined = flag; }

    public GlobData() {}
    public TcpClientService getService() { return mService;}
    public void setService(TcpClientService s) { mService = s; }


    public void setMe(Me p) { this.me = p; }
    public Me getMe() { return this.me; }

    public ArrayList<ChatData> getChatList() { return this.ChatList; }
    public ArrayList<Topic> getTopicList() { return this.TopicList; }


    public void addChatData(ChatData c) { this.ChatList.add(c); }
    public ChatData getChatData(int pos) { return this.ChatList.get(pos); }
    public ChatData getChatData(String ID) {
        int size = ChatList.size();
        for(int i=0; i<size; i++){
            ChatData cd = ChatList.get(i);
            if(cd.getName().compareToIgnoreCase(ID) == 0){
                return cd;
            }
        }
        return null;
    }
    public void delChatData(int pos) { this.ChatList.remove(pos); }
    public void delChatData(String ID) {
        int size = ChatList.size();
        for(int i=0; i<size; i++){
            ChatData cd = ChatList.get(i);
            if(cd.getName().compareToIgnoreCase(ID) == 0){
                ChatList.remove(i);
                return;
            }
        }
     }
    public int numChatData() { return this.ChatList.size(); }


    public void addTopic(Topic c) { this.TopicList.add(c); }
    public Topic getTopic(int pos) { return this.TopicList.get(pos); }
    public Topic getTopic(String ID) {
        int size = TopicList.size();
        for(int i=0; i<size; i++){
            Topic cd = TopicList.get(i);
            if(cd.getName().compareToIgnoreCase(ID) == 0){
                return cd;
            }
        }
        return null;
    }
    public void delTopic(int pos) { this.TopicList.remove(pos); }
    public void delTopic(String ID) {
        int size = TopicList.size();
        for(int i=0; i<size; i++){
            Topic cd = TopicList.get(i);
            if(cd.getName().compareToIgnoreCase(ID) == 0){
                TopicList.remove(i);
                return;
            }
        }
    }
    public int numTopic() { return this.TopicList.size(); }
}
