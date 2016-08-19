package com.tvbrowser0728;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;

import tv.webkit.WebView;
import tv.webkit.WebViewClient;

public class TvBrowserActivity extends AppCompatActivity {
    private static String LOGTAG = "TvBrowserActivity";
    private int mOrientation;
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(com.tvbrowser0728.R.layout.activity_tv_browser);
        mContext = TvBrowserActivity.this;
        Log.d(LOGTAG, "onCreate: mContext = " + mContext);
        Log.d(LOGTAG, "onCreate: mContextgetResources = " + mContext.getResources());
        //打印出资源列表
        String[] files = null;
        try {
            files = getAssets().list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i];
            Log.i("fileName001", " fileName001: " + fileName);
        }
        init();

    }

    public void init()
    {
//        WebView mWebView = new WebView(this);
        WebView mWebView = (tv.webkit.WebView)findViewById(R.id.mWebView);
        Log.d(LOGTAG, "liu bin add init001");
        Log.d(LOGTAG, "init: context = " + mWebView.getContext());
        mWebView.loadUrl("http://www.baidu.com");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        Log.d(LOGTAG, "liu bin add init002");
    }
}
