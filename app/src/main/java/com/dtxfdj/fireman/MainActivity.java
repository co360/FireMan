package com.dtxfdj.fireman;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient.CustomViewCallback;

import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private final static String DEFAULT_URL = "http://dh.123.sogou.com";
//    private final static String DEFAULT_URL = "http://10.129.192.204";
//    private final static String DEFAULT_URL = "http://m.youtube.com";
//    private final static String DEFAULT_URL = "http://39.106.90.54/#/";
    private final static int SHOW_START_PAGE_MS = 3000;

    Handler mHandler = new Handler();
    WebView mWebView;

    // implement fullscreen function
    private View mCustomView;
    private FrameLayout mFullscreenContainer;
    private CustomViewCallback mCustomViewCallback;

    private EditText mEditText;

    Runnable mDismissStartImg = new Runnable() {
        @Override
        public void run() {
            dismissStartPage();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        initWebView();
        mHandler.postDelayed(mDismissStartImg, SHOW_START_PAGE_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        mHandler.removeCallbacks(mDismissStartImg);
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

        mEditText = findViewById(R.id.url_edit);
        Button btn = findViewById(R.id.url_go);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoadUrl();
            }
        });
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId != EditorInfo.IME_ACTION_GO) && (event == null ||
                        event.getKeyCode() != KeyEvent.KEYCODE_ENTER ||
                        event.getAction() != KeyEvent.ACTION_DOWN)) {
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

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                // avoid open url with android default browser
                return true;
            }

//            @Override
//            public void onReceivedSslError(final WebView view,
//                                           final SslErrorHandler handler, final SslError error) {
//                handler.proceed();
//            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissionsCallback callback) {
                callback.invoke(origin, true, true);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                onShowDefaultCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                onHideDefaulCustomView();
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams) {
                return false;
            }
        });
        mWebView.loadUrl(DEFAULT_URL);
    }

    private void startLoadUrl() {
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
        mWebView.loadUrl(url);
        mEditText.clearFocus();
        setKeyboardVisibilityForUrl(false);
        mWebView.requestFocus();

    }

    private void setKeyboardVisibilityForUrl(boolean visible) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (visible) {
            imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && mWebView != null
                && mWebView.canGoBack()
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
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

    private void dismissStartPage() {
        ImageView imgView = findViewById(R.id.start_img);
        if (imgView != null) {
            imgView.setVisibility(View.GONE);
        }
    }
}