package com.example.abhsstudypad.webview;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.*;
import android.view.*;
import android.webkit.*;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.abhsstudypad.R;

import java.util.*;

/**
 * Created by guoshuyu on 2017/6/16.
 */

public class AbhsWebView extends WebView {

    static String TAG = "AbhsWebView";

    ActionMode mActionMode;

    List<String> mActionList = new ArrayList<>();

    ActionSelectListener mActionSelectListener;

    public AbhsWebView(Context context) {
        super(context);
    }

    public AbhsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private PermissionRequest myRequest;

    private Context mContext;

    private Activity mActivity;

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;

    public AbhsWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 设置属性
    public PermissionRequest setAttr(Context context,Activity activity)
    {
        mContext = context;
        mActivity = activity;

        // 清除缓存
        this.clearCache(true);

        // // 清除自动完成填充的表单数据
        this.clearFormData();

        // 打开调试模式
        //this.setWebContentsDebuggingEnabled(true);

        // 关闭调试模式
        this.setWebContentsDebuggingEnabled(false);

        //支持js
        this.getSettings().setJavaScriptEnabled(true);

        //使用缓存，否则localstorage等无法使用
        this.getSettings().setDomStorageEnabled(true);
        this.getSettings().setAppCacheMaxSize(1024*1024*8);
        String appCachePath = mContext.getCacheDir().getAbsolutePath();
        this.getSettings().setAppCachePath(appCachePath);
        this.getSettings().setAllowFileAccess(true);
        this.getSettings().setAppCacheEnabled(true);

        //设置自适应屏幕，两者合用
        this.getSettings().setUseWideViewPort(true); //将图片调整到适合webview的大小
        this.getSettings().setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        // 允许混用HTTP HTTPS
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
           this.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 视频自动播放
        this.getSettings().setMediaPlaybackRequiresUserGesture(false);

        // 打开麦克风权限
        this.setWebChromeClient(new WebChromeClient() {
            @Override

            public void onPermissionRequest(final PermissionRequest request) {
                myRequest = request;

                for (String permission : request.getResources()) {
                    switch (permission) {
                        case "android.webkit.resource.AUDIO_CAPTURE": {
                            askForPermission(request.getOrigin().toString(), Manifest.permission.RECORD_AUDIO, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                            break;
                        }
                    }
                }
            }
        });

        // 长安选中文字后弹出的自定义菜单
        List<String> list = new ArrayList<>();
        //list.add("Item1");
        //list.add("Item2");
        //list.add("APIWeb");
        // 设置在WEB页打开的URL不要调用浏览器 而是在WEBVIEW中打开
        this.setWebViewClient(new AbhsWebViewClient());

        //设置item
        this.setActionList(list);

        //链接js注入接口，使能选中返回数据
        this.linkJSInterface();

        this.getSettings().setBuiltInZoomControls(false);
        this.getSettings().setDisplayZoomControls(false);
        this.getSettings().setSupportZoom(false);

        //增加点击回调
        this.setActionSelectListener(new ActionSelectListener() {
            @Override
            public void onClick(String title, String selectText) {
                if(title.equals("APIWeb")) {
                    Intent intent = new Intent(mActivity, APIWebViewActivity.class);
                    mActivity.startActivity(intent);
                    return;
                }
                Toast.makeText(mActivity, "Click Item: " + title + "。\n\nValue: " + selectText, Toast.LENGTH_LONG).show();
            }
        });

        //加载url
        final WebView temp = this;
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                //temp.loadUrl("https://192.168.1.9/");

                //temp.loadUrl("https://eng.abhseducation.com/");

                // 设备号
                String sn = Build.SERIAL;
                //sn = "ac001415c3c0c6a204e";
                // 加载url sn = "ac001415c3c0c6a204e"

                temp.loadUrl("file:///android_asset/index.html?sn=" + sn);


                //temp.loadUrl(mActivity.getString(R.string.abhs_home) + "?sn=" + sn);
            }
        }, 1000);

        return myRequest;
    }

    public void askForPermission(String origin, String permission, int requestCode) {
        Log.d("WebView", "inside askForPermission for" + origin + "with" + permission);

        if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {

            } else {
                ActivityCompat.requestPermissions(mActivity, new String[]{permission},requestCode);
            }
        } else {
            myRequest.grant(myRequest.getResources());

        }
    }


    /**
     * 处理item，处理点击
     * @param actionMode
     */
    private ActionMode resolveActionMode(ActionMode actionMode) {
        if (actionMode != null) {
            final Menu menu = actionMode.getMenu();
            mActionMode = actionMode;
            menu.clear();
            for (int i = 0; i < mActionList.size(); i++) {
                menu.add(mActionList.get(i));
            }
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        getSelectedData((String) item.getTitle());
                        releaseAction();
                        return true;
                    }
                });
            }
        }
        mActionMode = actionMode;
        return actionMode;
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        ActionMode actionMode = super.startActionMode(callback);
        return resolveActionMode(actionMode);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        ActionMode actionMode = super.startActionMode(callback, type);
        return resolveActionMode(actionMode);
    }

    private void releaseAction() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    /**
     * 点击的时候，获取网页中选择的文本，回掉到原生中的js接口
     * @param title 传入点击的item文本，一起通过js返回给原生接口
     */
    private void getSelectedData(String title) {

        String js = "(function getSelectedText() {" +
                "var txt;" +
                "var title = \"" + title + "\";" +
                "if (window.getSelection) {" +
                "txt = window.getSelection().toString();" +
                "} else if (window.document.getSelection) {" +
                "txt = window.document.getSelection().toString();" +
                "} else if (window.document.selection) {" +
                "txt = window.document.selection.createRange().text;" +
                "}" +
                "JSInterface.callback(txt,title);" +
                "})()";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript("javascript:" + js, null);
        } else {
            loadUrl("javascript:" + js);
        }
    }

    public void linkJSInterface() {
        addJavascriptInterface(new ActionSelectInterface(this), "JSInterface");
    }

    /**
     * 设置弹出action列表
     * @param actionList
     */
    public void setActionList(List<String> actionList) {
        mActionList = actionList;
    }

    /**
     * 设置点击回掉
     * @param actionSelectListener
     */
    public void setActionSelectListener(ActionSelectListener actionSelectListener) {
        this.mActionSelectListener = actionSelectListener;
    }

    /**
     * 隐藏消失Action
     */
    public void dismissAction() {
        releaseAction();
    }


    /**
     * js选中的回掉接口
     */
    private class ActionSelectInterface {

        AbhsWebView mContext;

        ActionSelectInterface(AbhsWebView c) {
            mContext = c;
        }

        @JavascriptInterface
        public void callback(final String value, final String title) {

            Log.e("-----------js", "value:" + value);
            Log.e("-----------js", "title:" + title);

            if(mActionSelectListener != null) {
                mActionSelectListener.onClick(title, value);
            }
        }
    }
}