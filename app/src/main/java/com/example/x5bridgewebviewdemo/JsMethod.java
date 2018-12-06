package com.example.x5bridgewebviewdemo;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class JsMethod extends JavaSctiptMethods {
    X5BridgeWebView mWebView;
    Activity mActivity;

    public JsMethod(Activity mContext, X5BridgeWebView webView) {
        super(mContext, webView);
        mWebView = webView;
        mActivity = mContext;
    }

    @Override
    public void jsSendToAndroid(String[] jsons) {
        super.jsSendToAndroid(jsons);
        if (!TextUtils.isEmpty(JsSendAction.ACTION)) {
            switch (JsSendAction.ACTION) {
                case "callPhone":
                    //js指令拨打电话
                    showPhoneDialog(jsons[0]);
                    //JsSendCallback.CALLBACK_METHOD=null  没有回调方法，不用向JS端返回结果
                case "getHotelData":
                    //js指令加载酒店详情界面
                    //js方法需要android 端返回结果
                    JSONObject json = new JSONObject();
                    try {
                        json.put("hotel_name", "维多利亚大酒店");
                        json.put("order_status", "已支付");
                        json.put("orderId", "201612291809626");
                        json.put("seller", "携程");
                        json.put("expire_time", "2017年1月6日 23:00");
                        json.put("price", "688.0");
                        json.put("back_price", "128.0");
                        json.put("pay_tpye", "支付宝支付");
                        json.put("room_size", "3间房");
                        json.put("room_count", "3");
                        json.put("in_date", "2017年1月6日 12:00");
                        json.put("out_date", "2017年1月8日 12:00");
                        json.put("contact", "赵子龙先生");
                        json.put("phone", "18888888888");
                        json.put("server_phone", "0755-85699309");
                        json.put("address", "深圳市宝安区兴东地铁站旁边");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (JsSendCallback.CALLBACK_METHOD != null && !JsSendCallback.CALLBACK_METHOD.isEmpty()) {
                        mWebView.androidToJs(JsSendCallback.CALLBACK_METHOD, json.toString(), new AndroidToJsResultBack() {
                            @Override
                            public void getResultFromJs(String result) {
                                //JS端获取数据后，反馈给android 端
                                Toast.makeText(mActivity, result, Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                    break;
                case "showdialog":
                    //js指令，android端弹出对话框
                    showDialog(jsons[0]);
                    break;

                case "playvideos":
                    String type = JsSendParam.JSON_PARAM.optString("type");
                    Log.i("show", "type:" + type);
                    switch (type) {
                        case "1":
                            enableX5FullscreenFunc();
                            break;
                        case "2":
                            disableX5FullscreenFunc();
                            break;
                        case "3":
                            enableLiteWndFunc();
                            break;
                        case "4":
                            disableX5FullscreenFunc();
                            break;
                        case "5":
                            enablePageVideoFunc();
                            break;
                        case "6":
                            disableX5FullscreenFunc();
                            break;

                    }

                    break;
            }
        }
    }


    /**
     * 底部弹出拨号对话框
     *
     * @param str
     */
    private void showPhoneDialog(final String str) {
        JSONObject json = null;
        try {
            json = new JSONObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final BottomUpDialog btmDlg = new BottomUpDialog(mActivity);
        btmDlg.setContent(json.optString("phone"));
        btmDlg.setOnPhoneClickListener(new BottomUpDialog.OnPhoneClickListener() {
            @Override
            public void onPhoneClick() {
                callphone(str);//拨号
                btmDlg.dismiss();
            }
        });
        btmDlg.show();
    }

    /**
     * 是否播放视频提示框
     *
     * @param str
     */
    private void showDialog(final String str) {
        new AlertDialog.Builder(mActivity).setTitle("是否使用X5WebView播放视频？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //js方法需要android 端返回指令结果
                        //回调方法参数空
                        if (JsSendCallback.CALLBACK_METHOD != null && !JsSendCallback.CALLBACK_METHOD.isEmpty()) {
                            mWebView.androidToJs(JsSendCallback.CALLBACK_METHOD, "", new AndroidToJsResultBack() {
                                @Override
                                public void getResultFromJs(String result) {
                                    //JS端获取数据后，反馈给android 端
                                    Log.i("show", "js端无返回值：" + result);

                                }
                            });
                        }
                    }
                }).setNegativeButton("否", null).create().show();
    }


    /**
     * 拨打电话
     *
     * @param json
     */
    public void callphone(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            System.out.println("Demo callphone方法被调用:" + jsonObject.toString());
            //解析json
            String phone = jsonObject.optString("phone");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//拨号：android 6.0运行时权限
                if (mActivity.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                    mActivity.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 100);
                }
            }
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            mActivity.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void enableX5FullscreenFunc() {

        if (mWebView.getX5WebViewExtension() != null) {
            Toast.makeText(mActivity, "开启X5全屏播放模式", Toast.LENGTH_LONG).show();
            Bundle data = new Bundle();

            data.putBoolean("standardFullScreen", false);// true表示标准全屏，false表示X5全屏；不设置默认false，

            data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，

            data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

            mWebView.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
                    data);
        }
    }


    private void disableX5FullscreenFunc() {
        if (mWebView.getX5WebViewExtension() != null) {
            Toast.makeText(mActivity, "恢复webkit初始状态", Toast.LENGTH_LONG).show();
            Bundle data = new Bundle();

            data.putBoolean("standardFullScreen", true);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，

            data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，

            data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

            mWebView.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
                    data);
        }
    }

    private void enableLiteWndFunc() {
        if (mWebView.getX5WebViewExtension() != null) {
            Toast.makeText(mActivity, "开启小窗模式", Toast.LENGTH_LONG).show();
            Bundle data = new Bundle();

            data.putBoolean("standardFullScreen", false);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，

            data.putBoolean("supportLiteWnd", true);// false：关闭小窗；true：开启小窗；不设置默认true，

            data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

            mWebView.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
                    data);
        }
    }

    private void enablePageVideoFunc() {
        if (mWebView.getX5WebViewExtension() != null) {
            Toast.makeText(mActivity, "页面内全屏播放模式", Toast.LENGTH_LONG).show();
            Bundle data = new Bundle();

            data.putBoolean("standardFullScreen", false);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，

            data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，

            data.putInt("DefaultVideoScreen", 1);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

            mWebView.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
                    data);
        }
    }

}
