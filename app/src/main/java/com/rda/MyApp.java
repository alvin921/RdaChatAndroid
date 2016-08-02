package com.rda;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.rda.activity.ChatActivity;
import com.rda.protocol.ChatData;
import com.rda.protocol.GlobData;

import java.util.Stack;

/**
 * Created by mingangwang on 2016/7/22.
 */
public class MyApp extends Application {
    private static Stack<Activity> activityStack;
    private static MyApp instance;

    private GlobData data;

    public synchronized static MyApp getInstance() {
        if (null == instance) {
            instance = new MyApp();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        data = new GlobData();
        instance = this;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    public GlobData getData(){
        return data;
    }
	/*
	public void setData(GlobData data){
		this.data = data;
	}
	*/

    public void exit() {
        try {
            while(!activityStack.empty()){
                popActivity(currentActivity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    //退出指定Activity或者栈顶activity
    public void popActivity(Activity activity){
        if(activity == null)
            activity = currentActivity();

        if(activity!=null){
            activityStack.remove(activity);
            activity.finish();
            activity=null;
        }
    }

    //获得当前栈顶Activity
    public Activity currentActivity(){
        return activityStack.peek();
    }

    //将当前Activity推入栈中
    public void pushActivity(Activity activity){
        if(activityStack==null){
            activityStack=new Stack<Activity>();
        }
        activityStack.add(activity);
    }
    //退出栈中所有Activity
    public void popAllActivityUntil(Class cls){
        while(true){
            Activity activity=currentActivity();
            if(activity==null){
                break;
            }
            if(cls != null && activity.getClass().equals(cls) ){
                break;
            }
            popActivity(activity);
        }
    }
    //退出栈中所有Activity
    public void popAllActivityExept(Class cls){
        while(true){
            Activity activity=currentActivity();
            if(activity==null){
                break;
            }
            if(cls == null || !activity.getClass().equals(cls) ){
                popActivity(activity);
            }
        }
    }
}
