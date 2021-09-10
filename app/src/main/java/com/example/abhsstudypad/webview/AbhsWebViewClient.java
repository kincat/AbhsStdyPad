package com.example.abhsstudypad.webview;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.*;
import android.widget.Toast;

public class AbhsWebViewClient extends WebViewClient {

    // 页面载入是否出错
    private boolean mLastLoadFailed = false;

    // 页面载入完成
    @Override
    public void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);

        // 页面正常
        if (!mLastLoadFailed) {
            AbhsWebView customActionWebView = (AbhsWebView) webView;
            customActionWebView.linkJSInterface();

            // 如果未绑定则显示设备号
            String js = "var list = document.getElementsByClassName('user_hi fl');" +
                    "if(list.length == 1 && list[0].innerHTML == '账号待激活！')" +
                    "{list[0].innerHTML = '设备号：" + android.os.Build.SERIAL + "';}";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript("javascript:" + js, null);
            } else {
                webView.loadUrl("javascript:" + js);
            }
        }
    }

    @Override
    public void onPageStarted(WebView webView, String url, Bitmap favicon) {
        super.onPageStarted(webView, url, favicon);
    }

    // 页面出错
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        mLastLoadFailed = true;

    }


    // 打开网页时不调用系统浏览器， 而是在本WebView中显示
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        view.loadUrl(url);   //在当前的webview中跳转到新的url
        Log.e("-----------22222222222222222222222", "url:" + url);
        return true;
    }
}
