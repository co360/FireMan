package com.dtxfdj.fireman;

import android.app.Application;
import cn.jpush.android.api.JPushInterface;

public class MainApplication extends Application {
    @Override
    public void onCreate() {    	     
         super.onCreate();

         // 初始化 JPush
         JPushInterface.setDebugMode(BuildConfig.DEBUG);
         JPushInterface.init(this);     		
    }
}
