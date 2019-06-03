// Copyright 2019 The dtxfdj. All rights reserved.
// Author: baidaogui.

package com.dtxfdj.fireman;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dtxfdj.fireman.jsinterface.JSInterface;
import com.dtxfdj.fireman.startpage.SlideShowView;
import com.dtxfdj.fireman.utils.CommonUtils;
import com.dtxfdj.fireman.utils.PreferencesUtils;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient.CustomViewCallback;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

//    private final static String DEFAULT_URL = "http://dh.123.sogou.com";

//    private final static String DEFAULT_URL = "http://10.129.192.204/html/alert.html";
//    private final static String DEFAULT_URL = "https://wxpay.wxutil.com/mch/pay/h5.v2.php";
    private final static String DEFAULT_URL = "http://39.106.90.54/#/";
    // user: 15010929796 ps: 122716

    String[] mCantGoBackUrls = {
            "partyWork/searchDues",
            "partyWork/member",
            "organization/organization",
            "news/hot",
            "news/stones",
            "news/trends",
            "knowledge/common",
            "knowledge/history",
            "knowledge/ebook",
            "knowledge/video",
            "mine/personalInfo",
    };

    private final String START_PAGE_SHOW_PREFENRENCE_KEY = "enable_start_page";

    private final static boolean mEnableUrlEditor = false;

    public static boolean isForeground = false;

    private WebView mWebView;

    // implement fullscreen function
    private View mCustomView;
    private FrameLayout mFullscreenContainer;
    private CustomViewCallback mCustomViewCallback;

    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        initWebView();
        initUrlEditor();
        showStartPageOnNeccesary();
    }

    @Override
    public void onPause() {
        isForeground = false;
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        isForeground = true;
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }

    private void initWebView() {
        mWebView = findViewById(R.id.content_webview);
        mWebView.requestFocusFromTouch();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setSupportMultipleWindows(false);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setAppCachePath(
                mWebView.getContext().getCacheDir().getAbsolutePath());

        mWebView.addJavascriptInterface(new JSInterface(getApplication()), "JSInterface");
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, String url) {
                if (CommonUtils.isValidUrl(url)) {
                    loadUrl(url);
                } else if (!CommonUtils.handleNoneBrowserUrl(MainActivity.this, url)) {
                    // goBack from weixin pay's redirect page
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    }
                }
                // avoid open url with android default browser
                return true;
            }

            @Override
            public void onReceivedSslError(final WebView view,
                                           final SslErrorHandler handler,
                                           final SslError error) {
                handler.proceed();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                    GeolocationPermissionsCallback callback) {
                callback.invoke(origin, true, true);
            }

            @Override
            public void onShowCustomView(View view,
                    CustomViewCallback callback) {
                onShowDefaultCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                onHideDefaulCustomView();
            }

            @Override
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams) {
                return false;
            }
        });
        loadUrl(DEFAULT_URL);
    }

    private void initUrlEditor() {
        if (!mEnableUrlEditor) {
            return;
        }
        View editorContainer = findViewById(R.id.editor_container);
        editorContainer.setVisibility(View.VISIBLE);

        mEditText = findViewById(R.id.url_edit);
        Button btn = findViewById(R.id.url_go);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoadUrl();
            }
        });
        mEditText.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(
                    TextView v, int actionId, KeyEvent event) {
                if ((actionId != EditorInfo.IME_ACTION_GO)
                        && (event == null
                            || event.getKeyCode() != KeyEvent.KEYCODE_ENTER
                            || event.getAction() != KeyEvent.ACTION_DOWN)) {
                    return false;
                }
                startLoadUrl();
                return true;
            }
        });

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setKeyboardVisibilityForUrl(hasFocus);
                if (!hasFocus)
                    mEditText.setText(mWebView.getUrl());
            }
        });
    }

    public void showStartPageOnNeccesary() {
        if (!PreferencesUtils.getInstance().loadBoolean(
                this, START_PAGE_SHOW_PREFENRENCE_KEY, true)) {
            return;
        }
        PreferencesUtils.getInstance().saveBoolean(
                this, START_PAGE_SHOW_PREFENRENCE_KEY, false);
        SlideShowView imgView = findViewById(R.id.start_img);
        if (imgView != null) {
            imgView.show();
        }
    }

    private void loadUrl(String url) {
        Map extraHeaders = new HashMap();
        if (url.startsWith("https://wx.tenpay.com")) {
            extraHeaders.put("Referer", "https://wxpay.wxutil.com");
        }
        mWebView.loadUrl(url, extraHeaders);
    }

    private void startLoadUrl() {
        if (!mEnableUrlEditor) {
            return;
        }
        String url = mEditText.getText().toString();
        try {
            URI uri = new URI(url);
            if (uri.getScheme() == null) {
                url = "http://" + uri.toString();
            } else {
                url = uri.toString();
            }
        } catch (URISyntaxException e) {
            // Ignore syntax errors.
        }
        loadUrl(url);
        if (mEditText != null) {
            mEditText.clearFocus();
        }
        setKeyboardVisibilityForUrl(false);
        mWebView.requestFocus();

    }

    private void setKeyboardVisibilityForUrl(boolean visible) {
        if (!mEnableUrlEditor) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (visible) {
            imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    private boolean canGoBack() {
        for (int idx = 0; idx < mCantGoBackUrls.length; idx++) {
            String url = mCantGoBackUrls[idx];
            if (mWebView.getUrl().endsWith(url)) {
                return false;
            }
        }
        return mWebView.canGoBack();
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && canGoBack()
                && mCustomView == null) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    static class FullscreenHolder extends FrameLayout {
        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(0xFF000000);
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    private void onShowDefaultCustomView(
            View view, CustomViewCallback callback) {
        if (mCustomView != null) {
            return;
        }
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        mFullscreenContainer = new FullscreenHolder(this);
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        mFullscreenContainer.addView(view, params);
        decor.addView(mFullscreenContainer, params);
        mCustomView = view;
        setFullscreen(true);
        mCustomViewCallback = callback;
    }

    private void onHideDefaulCustomView() {
        if (mFullscreenContainer == null || mCustomViewCallback == null) {
            return;
        }
        setFullscreen(false);
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        decor.removeView(mFullscreenContainer);
        mFullscreenContainer.removeView(mCustomView);
        mFullscreenContainer.setVisibility(View.GONE);
        mFullscreenContainer = null;
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        mCustomViewCallback = null;
    }

    private void setFullscreen(boolean enabled) {
        View decor = getWindow().getDecorView();
        if (enabled) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        int systemUiVisibility = decor.getSystemUiVisibility();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (enabled) {
            systemUiVisibility |= flags;
        } else {
            systemUiVisibility &= ~flags;
        }
        decor.setSystemUiVisibility(systemUiVisibility);
    }
}