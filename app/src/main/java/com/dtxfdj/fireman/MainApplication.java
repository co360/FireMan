package com.dtxfdj.fireman;

import android.app.Application;
import cn.jpush.android.api.JPushInterface;

public class MainApplication extends Application {
    @Override
    public void onCreate() {    	     
         super.onCreate();

         // FIXME
         // 设置开启日志,发布时请关闭日志
         android.util.Log.e("bdg9", "bdg9, Debug: " + BuildConfig.DEBUG);
         if (BuildConfig.DEBUG) {
             android.util.Log.e("bdg9", "bdg9, 1 Debug: " + BuildConfig.DEBUG);
         } else {
             android.util.Log.e("bdg9", "bdg9, 2 Debug: " + BuildConfig.DEBUG);
         }
         JPushInterface.setDebugMode(BuildConfig.DEBUG);
         // 初始化 JPush
         JPushInterface.init(this);     		
    }
}
