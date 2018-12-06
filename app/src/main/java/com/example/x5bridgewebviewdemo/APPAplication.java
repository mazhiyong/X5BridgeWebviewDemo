package com.example.x5bridgewebviewdemo;

import android.app.Application;

public class APPAplication extends Application {

    private static APPAplication   instance;

    public static APPAplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance=APPAplication.this;
        X5BridgeWebView.initX5WebViewCore();
    }
}
