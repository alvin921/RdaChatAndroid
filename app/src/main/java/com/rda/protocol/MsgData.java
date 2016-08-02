package com.rda.protocol;

/**
 * Created by mingangwang on 2016/7/21.
 */

enum MsgState {
    MSG_SENDING, MSG_SENT_KO, MSG_SENT, MSG_READ
}
public class MsgData {
    private String FromID;
    private String Msg;
    private MsgState State;
    // time

    public MsgData(String name, String msg) {
        this.FromID = name;
        this.Msg = msg;
    }

    public String getName() {
        return FromID;
    }

    public String getMsg() {
        return Msg;
    }
}
