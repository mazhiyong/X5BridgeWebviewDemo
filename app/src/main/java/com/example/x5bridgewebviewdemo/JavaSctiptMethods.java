package com.example.x5bridgewebviewdemo;

import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * android 和 js 的交互（方法的互相调用）
 */
public class JavaSctiptMethods {
    private X5BridgeWebView webView;
    private Activity mActivity;

    public JavaSctiptMethods(Activity mContext, X5BridgeWebView webView) {
        this.mActivity = mContext;
        this.webView = webView;
    }



    /**
     * 统一分发js调用android分发 (jsSendToAndroid 方法js 中调用   根据action 类别分发不同事件给android 端)
     */
    public void jsSendToAndroid(String[] jsons) {

        final String str = jsons[0];
        try {
            JSONObject json = new JSONObject(str);
            //js传递过来的动作，比如callPhone代表拨号，share2QQ代表分享到QQ，其实就是H5和android通信协议（自定义的）
            String action = json.optString("action");
            String callback = json.optString("callback");
            JsSendAction.ACTION = action;
            JsSendCallback.CALLBACK_METHOD = callback;
            JsSendParam.JSON_PARAM = json;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
