package com.rda.protocol;

import android.provider.Telephony;
import android.util.Log;

import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by mingangwang on 2015/1/16.
 */
public class Cmd implements Serializable {
    private String CmdName;
    private ArrayList<String> Args = new ArrayList<String>();

    public Cmd(){

    }

    public void setCmdArgs0(String name) {
        setCmdName(name);
    }
    public void setCmdArgs1(String name, String arg) {
        setCmdName(name);
        addArg(arg);
    }
    public void setCmdArgs2(String name, String arg0, String arg1) {
        setCmdName(name);
        addArg(arg0);
        addArg(arg1);
    }
    public void setCmdArgs3(String name, String arg0, String arg1, String arg2) {
        setCmdName(name);
        addArg(arg0);
        addArg(arg1);
        addArg(arg2);
    }
    public void setCmdArgs4(String name, String arg0, String arg1, String arg2, String arg3) {
        setCmdName(name);
        addArg(arg0);
        addArg(arg1);
        addArg(arg2);
        addArg(arg3);
    }

    public String getCmdName() {
        return CmdName;
    }

    public void setCmdName(String cmdName) {
        CmdName = cmdName;
    }

    public void addArg(String arg) {
        this.Args.add(arg);
    }

    public String getArg(int pos) {
        return this.Args.get(pos);
    }

    @Override
    public String toString() {
        String out = "";
        for (int i = 0; i < Args.size(); i++){
            if(i != 0)
                out += " ";
            out += Args.get(i);
        }

        return CmdName + "[" + out + "]";
    }

    public static final String DEV_TYPE_WATCH  = "D";
    public static final String DEV_TYPE_CLIENT = "C";

    // msg response for indication of successful or not
    public static final String RSP_SUCCESS = "SUCCESS";
    public static final String RSP_ERROR   = "ERROR";

    public class CommonInd {
        //public String CmdName;
    }
    public class CommonRsp {
        //public String CmdName;
        public String Ack; // SUCCESS or ERRORS
    }


    //SEND_PING
    public static final String SEND_PING_CMD = "PING";

    public Cmd PingCmd() {
        setCmdArgs0(SEND_PING_CMD);
        return this;
    }

    public static final String REQ_LOGIN_CMD = "REQ_LOGIN";
    public static final String RSP_LOGIN_CMD = "RSP_LOGIN";
    /*
             device/client -> gateway
               REQ_LOGIN_CMD
                   arg0: ClientID        //用户ID
                   arg1: ClientType    //终端类型"C" or "D"，是client还是device
                   arg2: ClientPwd     //nil for Device/password for Client
             gateway -> device/client
               RSP_LOGIN_CMD
                   arg0: SUCCESS/ERROR
                   arg1: uuid
                   arg2: MsgServerAddr
             device/client -> MsgServer
               REQ_LOGIN_CMD
                   arg0: ClientID        //用户ID
                   arg1: uuid
             MsgServer -> device/client
               RSP_LOGIN_CMD
                   arg0: SUCCESS/ERROR
    */
    public Cmd LoginGatewayCmd(String User, String Type, String Pass){
        setCmdArgs3(REQ_LOGIN_CMD, User, Type, Pass);
        return this;
    }
    public class LoginRsp extends CommonRsp {
        public String UUID;
        public String SAddr;
    }
    public Cmd LoginServerCmd(String User, String UUID){
        setCmdArgs2(REQ_LOGIN_CMD, User, UUID);
        return this;
    }

    public static final String REQ_LOGOUT_CMD = "REQ_LOGOUT";
    public static final String RSP_LOGOUT_CMD = "RSP_LOGOUT";
	/*
	   device/client -> MsgServer
	       REQ_LOGOUT_CMD

	   MsgServer -> device/client
	       RSP_LOGOUT_CMD
	       arg0: SUCCESS/ERROR
	*/
    public class LogtouRsp extends  CommonRsp {}
    public Cmd LogoutCmd() {
        setCmdArgs0(REQ_LOGOUT_CMD);
        return this;
    }

    public static final String REQ_SEND_P2P_MSG_CMD     = "REQ_SEND_P2P_MSG";
    public static final String IND_SEND_P2P_MSG_CMD     = "IND_SEND_P2P_MSG";
    public static final String RSP_SEND_P2P_MSG_CMD     = "RSP_SEND_P2P_MSG";
    public static final String ROUTE_SEND_P2P_MSG_CMD   = "ROUTE_SEND_P2P_MSG";

    // status of p2p msg
    public static final String IND_ACK_P2P_STATUS_CMD   = "IND_ACK_P2P_STATUS";
    public static final String ROUTE_ACK_P2P_STATUS_CMD = "ROUTE_ACK_P2P_STATUS";
    public static final String P2P_ACK_FALSE   = "FALSE";   // msg server received
    public static final String P2P_ACK_SENT    = "SENT";    // sent
    public static final String P2P_ACK_REACHED = "REACHED"; // msg reach the peer(Send2ID)
    public static final String P2P_ACK_READ    = "READ";    // receiver read this msg
    /*
       device/client -> MsgServer
           REQ_SEND_P2P_MSG_CMD
           arg0: Sent2ID       //接收方用户ID
           arg1: Msg           //消息内容

           IND_ACK_P2P_STATUS_CMD
           arg0: uuid // 发送方知道uuid对应的已发送的消息已送达
           arg1: SENT/READ // 发送方知道uuid对应的消息状态：已送达/已读
           arg2: fromID        //发送方用户ID

       返回给消息发送者的消息
       MsgServer -> device/client
           RSP_SEND_P2P_MSG_CMD
           arg0: SUCCESS/FAILED
           arg1: uuid // MsgServer分配的消息uuid，发送方根据此uuid确定该消息状态

           IND_ACK_P2P_STATUS_CMD
           arg0: uuid // 发送方知道uuid对应的已发送的消息已送达
           arg1: SENT/READ // 发送方知道uuid对应的消息状态：已送达/已读

       发送给消息接受者的消息
       MsgServer -> device/client
           IND_SEND_P2P_MSG_CMD
           arg0: Msg           //消息内容
           arg1: FromID        //发送方用户ID
           arg2: uuid          //MsgServer分配的消息uuid，可选，如果提供了则须IND_ACK_P2P_MSG_CMD(ClientID, uuid)

    */
    public Cmd P2PMsgCmd(String id, String msg) {
        setCmdArgs2(REQ_SEND_P2P_MSG_CMD, id, msg);
        return this;
    }

    public class P2PMsgRsp extends CommonRsp {
        public String UUID;
    }

    public class P2PMsgInd extends CommonInd {
        public String Msg;
        public String FromID;
        public String UUID;
    }

    public Cmd P2PStatusCmd(String uuid, String status, String id) {
        setCmdArgs3(IND_ACK_P2P_STATUS_CMD, uuid, status, id);
        return this;
    }
    public class P2PStatusInd extends  CommonInd {
        public String UUID;
        public String Status;
    }

    public static final String REQ_CREATE_TOPIC_CMD = "REQ_CREATE_TOPIC";
    public static final String RSP_CREATE_TOPIC_CMD = "RSP_CREATE_TOPIC";
	/*
	   client -> MsgServer
	       REQ_CREATE_TOPIC_CMD
	       arg0: TopicName     //群组名
	       arg1: ClientName    //用户在Topic中的Name, 比如老爸/老妈

	   MsgServer -> client
	       RSP_CREATE_TOPIC_CMD
	       arg0: SUCCESS/ERROR
	       arg1: TopicName
	*/
    public Cmd CreateTopicCmd(String topicName, String clientName) {
        setCmdArgs2(REQ_CREATE_TOPIC_CMD, topicName, clientName);
        return this;
    }
    public class CreateTopicRsp extends CommonRsp {
        public String TopicName;
        public String Alias;
    }

    public static final String REQ_ADD_2_TOPIC_CMD = "REQ_ADD_2_TOPIC";
    public static final String RSP_ADD_2_TOPIC_CMD = "RSP_ADD_2_TOPIC";
	/*
	   client -> MsgServer
	       REQ_ADD_2_TOPIC_CMD
	       arg0: TopicName     //群组名
	       arg1: NewClientID          //用户ID
	       arg2: NewClientName    //用户在Topic中的Name, 对于device, 可以是儿子/女儿

	   MsgServer -> client
	       RSP_ADD_2_TOPIC_CMD
	       arg0: SUCCESS/ERROR
	       arg1: TopicName
	       arg2: ClientID
	       arg3: ClientType
	*/
    public Cmd Add2TopicCmd(String topicName, String clientID, String clientName) {
        setCmdArgs3(REQ_ADD_2_TOPIC_CMD, topicName, clientID, clientName);
        return this;
    }
    public class Add2TopicRsp extends CommonRsp {
        public String TopicName;
        public String ID;
        public String Type;
    }
    public static final String REQ_KICK_TOPIC_CMD = "REQ_KICK_TOPIC";
    public static final String RSP_KICK_TOPIC_CMD = "RSP_KICK_TOPIC";
    /*
        client -> MsgServer
           REQ_KICK_TOPIC_CMD
           arg0: TopicName     //群组名
           arg1: NewClientID   //待移除的成员用户ID

       MsgServer -> client
           RSP_KICK_TOPIC_CMD
           arg0: SUCCESS/ERROR
    */
    public Cmd KickTopicCmd (String topicName, String clientID) {
        setCmdArgs2(REQ_KICK_TOPIC_CMD, topicName, clientID);
        return this;
    }

    public static final String REQ_JOIN_TOPIC_CMD = "REQ_JOIN_TOPIC";
    public static final String RSP_JOIN_TOPIC_CMD = "RSP_JOIN_TOPIC";
    /*
       client -> MsgServer
           REQ_JOIN_TOPIC_CMD
           arg0: TopicName     //群组名
           arg1: ClientName    //用户在Topic中的Name, 比如老爸/老妈

       MsgServer -> client
           RSP_JOIN_TOPIC_CMD
           arg0: SUCCESS/ERROR
    */
    public Cmd JoinTopicCmd (String topicName, String clientName) {
        setCmdArgs2(REQ_JOIN_TOPIC_CMD, topicName, clientName);
        return this;
    }

    public static final String REQ_QUIT_TOPIC_CMD = "REQ_QUIT_TOPIC";
    public static final String RSP_QUIT_TOPIC_CMD = "RSP_QUIT_TOPIC";
    /*
       client -> MsgServer
           REQ_QUIT_TOPIC_CMD
           arg0: TopicName     //群组名

       MsgServer -> client
           RSP_QUIT_TOPIC_CMD
           arg0: SUCCESS/ERROR
    */
    public Cmd QuitTopicCmd (String topicName) {
        setCmdArgs1(REQ_QUIT_TOPIC_CMD, topicName);
        return this;
    }

    public static final String REQ_SEND_TOPIC_MSG_CMD   = "REQ_SEND_TOPIC_MSG";
    public static final String RSP_SEND_TOPIC_MSG_CMD   = "RSP_SEND_TOPIC_MSG";
    public static final String ROUTE_SEND_TOPIC_MSG_CMD = "ROUTE_SEND_TOPIC_MSG";
    public static final String IND_SEND_TOPIC_MSG_CMD   = "IND_SEND_TOPIC_MSG";
    /*
       device/client -> MsgServer -> Router
           REQ_SEND_TOPIC_MSG_CMD
           arg0: Msg           //消息内容
           arg1: TopicName     //群组名, device无须提供

       返回给消息发送者的消息
       MsgServer -> device/client
           RSP_SEND_TOPIC_MSG_CMD
           arg0: SUCCESS/FAILED

       发送给消息接受者的消息
       MsgServer -> device/client
           IND_SEND_TOPIC_MSG_CMD
           arg0: Msg           //消息内容
           arg1: TopicName     //群组名
           arg2: ClientID      //发送方用户ID
           arg3: ClientType    //发送方终端类型，是client还是device
    */
    public Cmd TopicMsgCmd (String topicName, String msg) {
        if(topicName == null || topicName.isEmpty())
            setCmdArgs1(REQ_SEND_TOPIC_MSG_CMD, msg);
        else
            setCmdArgs2(REQ_SEND_TOPIC_MSG_CMD, msg, topicName);
        return this;
    }
    public class TopicMsgInd extends CommonInd {
        public String Msg;
        public String TopicName;
        public String ID;
        public String Type;
    }

    public static final String REQ_GET_TOPIC_LIST_CMD = "REQ_GET_TOPIC_LIST";
    public static final String RSP_GET_TOPIC_LIST_CMD = "RSP_GET_TOPIC_LIST";
    /*
       device/client -> MsgServer
           REQ_GET_TOPIC_LIST_CMD

       MsgServer -> device/client
           RSP_GET_TOPIC_LIST_CMD
           arg0: SUCCESS/ERROR
           arg1: TopicNum     // topic数目，后面跟随该数目的TopicName
           arg2: TopicName1
           arg3: TopicName2
           arg4: TopicName3
    */
    public Cmd TopicListCmd () {
        setCmdArgs0(REQ_GET_TOPIC_LIST_CMD);
        return this;
    }
    public class TopicListRsp extends CommonRsp {
        public ArrayList<String> List = new ArrayList<String>();
    }

    public static final String REQ_GET_TOPIC_PROFILE_CMD = "REQ_GET_TOPIC_PROFILE";
    public static final String RSP_GET_TOPIC_PROFILE_CMD = "RSP_GET_TOPIC_PROFILE";
    /*
               device/client -> MsgServer
                      REQ_GET_TOPIC_PROFILE_CMD
                       arg0: TopicName
                    如果ClientID不是TopicName的成员，则返回失败

               MsgServer -> device/client
                    RSP_GET_TOPIC_PROFILE_CMD
                        arg0: SUCCESS/ERROR
                        arg1: TopicName
                        arg2: Creator
                        arg3: MemberNum     // topic member数目，后面跟随该数目的member
                        arg4: Member1ID
                        arg5: Member1Name
                        arg6: Member1Type
                        arg7: Member2ID
    */
    public Cmd TopicProfileCmd (String topicName) {
        setCmdArgs1(REQ_GET_TOPIC_PROFILE_CMD, topicName);
        return this;
    }
    public class TopicProfileRsp extends CommonRsp {
        public Topic topic;
    }
    public Object parse() {
        switch(CmdName){
            case RSP_LOGIN_CMD:{
                LoginRsp p = new LoginRsp();
                p.Ack = this.getArg(0);
                if(this.Args.size() > 1){
                    p.UUID = this.getArg(1);
                    p.SAddr = getArg(2);
                }
                return p;
            }
            case RSP_LOGOUT_CMD:
            case RSP_JOIN_TOPIC_CMD:
            case RSP_KICK_TOPIC_CMD:
            case RSP_SEND_TOPIC_MSG_CMD:
            case RSP_QUIT_TOPIC_CMD:{
                CommonRsp p = new CommonRsp();
                p.Ack = this.getArg(0);
                return p;
            }
            case RSP_CREATE_TOPIC_CMD:{
                CreateTopicRsp p = new CreateTopicRsp();
                p.Ack = getArg(0);
                if(p.Ack.compareToIgnoreCase(RSP_SUCCESS) == 0) {
                    p.TopicName = getArg(1);
                    p.Alias = getArg(2);
                }
                return p;
            }
            case RSP_ADD_2_TOPIC_CMD:{
                Add2TopicRsp p = new Add2TopicRsp();
                p.Ack = getArg(0);
                p.TopicName = getArg(1);
                p.ID = getArg(2);
                p.Type = getArg(3);
                return p;
            }

            case RSP_GET_TOPIC_LIST_CMD:{
                TopicListRsp p = new TopicListRsp();
                p.Ack = getArg(0);
                Log.d("Cmd", "arg[0]="+getArg(0));
                if(p.Ack.compareToIgnoreCase(RSP_SUCCESS) == 0) {
                    Log.d("Cmd", "arg[1]="+getArg(1));
                    int num = Integer.parseInt(getArg(1));
                    for(int i = 0; i < num; i++){
                        p.List.add(getArg(i+2));
                    }
                }
                return p;
            }
            case RSP_GET_TOPIC_PROFILE_CMD:{
                TopicProfileRsp p = new TopicProfileRsp();
                p.Ack = getArg(0);
                if(p.Ack.compareToIgnoreCase(RSP_SUCCESS) == 0) {
                    //p.topic = Gson.fromJson(cmd, Cmd.class);
                    p.topic = new Topic(getArg(1), getArg(2));
                    int num = (this.Args.size()-3)/3;//Integer.getInteger(getArg(3));
                    for(int i = 0; i < num; i++){
                        p.topic.addMember(getArg(3+3*i), getArg(3+3*i+1), getArg(3+3*i+2));
                    }
                }
                return p;
            }
            case RSP_SEND_P2P_MSG_CMD:{
                P2PMsgRsp p = new P2PMsgRsp();
                p.Ack = this.getArg(0);
                p.UUID = this.getArg(1);
                return p;
            }

            case IND_ACK_P2P_STATUS_CMD:{
                P2PStatusInd p = new P2PStatusInd();
                p.UUID = getArg(0);
                p.Status = getArg(1);
                return p;
            }

            case IND_SEND_P2P_MSG_CMD:{
                P2PMsgInd p = new P2PMsgInd();
                p.Msg = this.getArg(0);
                p.FromID = this.getArg(1);
                p.UUID = this.getArg(2);
                return p;
            }

            case IND_SEND_TOPIC_MSG_CMD:{
                TopicMsgInd p = new TopicMsgInd();
                p.Msg = getArg(0);
                p.TopicName = getArg(1);
                p.ID = getArg(2);
                p.Type = getArg(3);
                return p;
            }

        }
        return null;
    }

}
