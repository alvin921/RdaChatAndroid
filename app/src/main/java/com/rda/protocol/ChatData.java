package com.rda.protocol;

import java.util.ArrayList;

/**
 * Created by mingangwang on 2016/7/21.
 */

    /*
    public enum ChatType {
        CHAT_TYPE_P2P,//(0),
        CHAT_TYPE_TOPIC//(1),
        //private int nValue;
        //private ChatType(int v) { nValue = v; }
    }
    */

public class ChatData {
    private String ID;
    private boolean Type;
    private int nUnread;
    private ArrayList<MsgData> List = new ArrayList<MsgData>();

    public static final boolean CHAT_TYPE_P2P = false;
    public static final boolean CHAT_TYPE_TOPIC = true;

    public ChatData(String id, boolean type) {
        ID = id;
        Type = type;
    }

    public String getName() { return ID; }
    public boolean getType() { return Type; }

    public void setUnread(int n) { nUnread = n; }
    public int getUnread() { return nUnread; }
    public int incUnread() { return ++nUnread; }

    public int getSize() { return List.size(); }
    public MsgData getMsgData(int pos) { return List.get(pos); }
    public boolean addMsgData(MsgData m) { incUnread(); return List.add(m); }
    public boolean delMsgData(MsgData m) { return List.remove(m); }
}