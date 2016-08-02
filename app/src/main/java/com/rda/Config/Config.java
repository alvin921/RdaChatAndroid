package com.rda.Config;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class Config {

	
	public static final String SERVER_URL = "http://demo.eoeschool.com/api/v1/nimings/io";
//	public static final String SERVER_URL = "http://10.0.0.52:8080/TestServer/api.jsp";


    public static final String KEY_NAME = "name";
    public static final String KEY_PASSWORD = "password";
	public static final String KEY_GW_ADDR = "server_addr";
	public static final String KEY_SERVER_ADDR = "server_addr";
	public static final String KEY_UUID = "uuid";
	public static final String KEY_ACTION = "action";
	public static final String KEY_PHONE_MD5 = "phone_md5";
	public static final String KEY_STATUS = "status";
	public static final String KEY_CODE = "code";
	public static final String KEY_CONTACTS = "contatcs";
	public static final String KEY_PAGE = "page";
	public static final String KEY_PERPAGE = "perpage";
	public static final String KEY_TIMELINE = "items";
	public static final String KEY_MSG_ID = "msgId";
	public static final String KEY_MSG = "msg";
	public static final String KEY_COMMENTS = "items";
	public static final String KEY_CONTENT = "content";

	public static final int RESULT_STATUS_SUCCESS = 1;
	public static final int RESULT_STATUS_FAIL = 0;
	public static final int RESULT_STATUS_INVALID_TOKEN = 2;

	public static final String APP_ID = "com.rdamicro.chat";
	public static final String CHARSET = "utf-8";

	public static final String ACTION_GET_CODE = "send_pass";
	public static final String ACTION_LOGIN = "login";
	public static final String ACTION_UPLOAD_CONTACTS = "upload_contacts";
	public static final String ACTION_TIMELINE = "timeline";
	public static final String ACTION_GET_COMMENT = "get_comment";
	public static final String ACTION_PUB_COMMENT = "pub_comment";
	public static final String ACTION_PUBLISH = "publish";

	public static final int ACTIVITY_RESULT_NEED_REFRESH = 10000;

    public static final String GATEWAY_ADDR = "192.168.10.100:17000";

	public static String getString(Context context, String key){
		return context.getSharedPreferences(APP_ID, Context.MODE_PRIVATE).getString(key, null);
	}

	public static void remove(Context context, String key){
		Editor e = context.getSharedPreferences(APP_ID, Context.MODE_PRIVATE).edit();
		e.remove(key);
		e.commit();
	}

	public static void setString(Context context, String key, String value){
		Editor e = context.getSharedPreferences(APP_ID, Context.MODE_PRIVATE).edit();
		e.putString(key, value);
		e.commit();
	}

	public static String getCachedName(Context context){
		return getString(context, KEY_NAME);
	}
	
	public static void cacheName(Context context,String token){
		setString(context, KEY_NAME, token);
	}

	public static String getCachedPassword(Context context){
		return getString(context, KEY_PASSWORD);
	}
	
	public static void cachePassword(Context context,String password){
		setString(context, KEY_PASSWORD, password);
	}
}
