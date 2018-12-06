package com.example.x5bridgewebviewdemo;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebViewClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * js和android通信桥梁WebView，封装好js和安卓通信机制  继承自X5WebView（性能高）
 *
 */

public class X5BridgeWebView extends com.tencent.smtt.sdk.WebView {

    /***
     * js调用android方法的映射字符串
     **/
    private static final String JS_INTERFACE = "jsInterface";
    public AndroidToJsResultBack mResultBack;

    public X5BridgeWebView(Context context) {
        super(context);
        this.setWebViewClient(client);
        initWebViewSettings();
        setResultBack(mResultBack);
    }

    public static  void  initX5WebViewCore(){
        //耗时操作，在子线程中完成
        new Thread(new Runnable() {
            @Override
            public void run() {
                QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
                    @Override
                    public void onCoreInitFinished() {

                    }

                    @Override
                    public void onViewInitFinished(boolean b) {
                        //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                        Log.d("show", " onViewInitFinished is " +b);
                    }
                };
                QbSdk.initX5Environment(APPAplication.getInstance().getApplicationContext(),cb);
            }
        }).start();

    }



    
    private WebViewClient client = new WebViewClient() {
        /**
         * 防止加载网页时调起系统浏览器
         */
        public boolean shouldOverrideUrlLoading(com.tencent.smtt.sdk.WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };
    public X5BridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWebViewClient(client);
        initWebViewSettings();
        setResultBack(mResultBack);
    }

    public AndroidToJsResultBack getResultBack() {
        return mResultBack;
    }
    

    public X5BridgeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setWebViewClient(client);
        initWebViewSettings();
    }

    public void setResultBack(AndroidToJsResultBack resultBack) {
        mResultBack = resultBack;
    }

    private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
        // settings 的设计

        //关闭保存密码，防止密码明文泄露
        webSetting.setSavePassword(false);

        //处理html 性能较好
        this.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient());
        //处理Js 性能较好
        this.setWebChromeClient(new com.tencent.smtt.sdk.WebChromeClient());
    }

    /**
     * 注册js和android通信桥梁对象
     *
     * @param obj 桥梁类对象,该对象提供方法让js调用,默认开启JavaScriptEnabled=true
     */
    public void addBridgeInterface(Object obj) {
        this.getSettings().setJavaScriptEnabled(true);
        this.addJavascriptInterface(new MyJavaScriptMethod(obj), JS_INTERFACE);
    }

    /**
     * 注册js和android通信桥梁对象
     * @param obj 桥梁类对象,该对象提供方法让js调用
     * @param url 默认开启JavaScriptEnabled=true
     */
    public void addBridgeInterface(Object obj, String url) {
        this.getSettings().setJavaScriptEnabled(true);
        this.addJavascriptInterface(new MyJavaScriptMethod(obj), JS_INTERFACE);
        this.loadUrl(url);
    }

    /**
     *   android 调用Js方法
     *   android 4.4 以下使用webview 的loadurl()方法  无返回值
     *   android 4.4 以下使用webview 的evaluateJavascript（)方法  js中方法return 值 即为JS返回android的值
     * @param jsMethod  js中的方法名
     * @param params  js中方法的参数（默认json字符串）
     * @param callback  js返回android调用的结果
     */
    public void androidToJs(final String jsMethod, final String params, final AndroidToJsResultBack callback) {
        if (TextUtils.isEmpty(jsMethod)) return;

        this.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < 18) {
                    loadUrl("javascript:" + jsMethod + "(" + params + ")");
                    callback.getResultFromJs("");
                } else {
                    evaluateJavascript("javascript:" + jsMethod + "(" + params + ")", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            callback.getResultFromJs(s);
                        }
                    });

                }
            }
        });



    }

    /**
     * 内置js桥梁类
     *
     */
    public class MyJavaScriptMethod {

        private Object mTarget;
        public MyJavaScriptMethod(Object targer) {
            this.mTarget = targer;
        }

        /**
         * 内置桥梁方法
         * @param method 方法名
         * @param json   js传递参数，json格式
         */
        @JavascriptInterface
        public void invokeMethod(String method, String[] json) {
            Class<?>[] params = new Class[]{String[].class};
            try {
                Method targetMethod = this.mTarget.getClass().getDeclaredMethod(method, params);
                targetMethod.invoke(mTarget, new Object[]{json});//反射调用js传递过来的方法，传参

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }


}
