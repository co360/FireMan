package com.dtxfdj.fireman.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;

public class JSInterface {
    Context mContext;

    public JSInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void registerJPushAlias(int sequence, String webMessage) {
        JPushInterface.setAlias(mContext, sequence, webMessage);
//        JPushInterface.getAlias(mContext, sequence);
    }

    @JavascriptInterface
    public void showToast(String webMessage) {
        Toast.makeText(mContext, webMessage, Toast.LENGTH_SHORT).show();
    }
}
