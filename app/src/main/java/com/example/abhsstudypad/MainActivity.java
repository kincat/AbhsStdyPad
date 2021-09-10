package com.example.abhsstudypad;

import android.Manifest;
import android.app.*;
import android.content.*;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.*;

import android.text.TextUtils;
import android.util.Log;
import android.view.*;

import android.webkit.*;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.alibaba.fastjson.*;

import com.example.abhsstudypad.utils.Tools;
import com.example.abhsstudypad.view.CommonProgressDialog;

import java.io.*;
import java.net.*;

import com.example.abhsstudypad.webview.*;
import com.yanzhenjie.alertdialog.AlertDialog;
import com.yanzhenjie.permission.*;


public class MainActivity extends Activity {

    private AbhsWebView mWebView;
    private static final String TAG = "MainActivity";
    private NetworkChangedReceiver networkChangeReceiver;

    private CommonProgressDialog pBar;

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private PermissionRequest myRequest;

    private IntentFilter intentfilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);

        // 设置横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 隐藏虚拟按键，并且全屏
        hideBottomUIMenu();

        //  设置视图
        setContentView(R.layout.activity_main);

        // 网络状态管理
        IntentFilter intentNetChangeFilter = new IntentFilter();
        intentNetChangeFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");//为过滤器添加 广播过滤
        networkChangeReceiver = new NetworkChangedReceiver();
        registerReceiver(networkChangeReceiver, intentNetChangeFilter);

        // 注册一个服务
        //Intent intentService = new Intent(this, AbhsService.class);
        //startService(intentService);

        // 获取当前安装的版本号，是否更新
        int currentVersion = Tools.getCurrentVersion(this);

        // 与服务器最新版本比较 如果需要 则提示下载更新
        JSONObject jsonObject  =  Tools.getNewVersion(currentVersion,MainActivity.this);
        if(jsonObject != null)
        {
            // 版本号不同
            ShowDialog(jsonObject.getString("Info"), jsonObject.getString("ApkUrl"));
        }


        // 浏览器内容加载
        mWebView = (AbhsWebView) findViewById(R.id.webview);

        //加载asset文件夹下html
        // webView.loadUrl("file:///android_asset/test.html");

        // 设置WEBVIEW属性并返回请求对象
        myRequest = mWebView.setAttr(getApplicationContext(),MainActivity.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        unregisterReceiver(networkChangeReceiver);
    }


    // 隐藏虚拟按键，并且全屏
    protected void hideBottomUIMenu(){

        //隐藏虚拟按键，并且全屏
        if(Build.VERSION.SDK_INT >11&& Build.VERSION.SDK_INT <19) {// lower api

            View v =this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);

        }else if(Build.VERSION.SDK_INT >=19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
       }
 }

    // 下载存储的文件名
    private static final String DOWNLOAD_NAME = "abhsStudy.apk";


    /**
     * 升级系统
     *
     * @param content
     * @param url
     */
    private void ShowDialog(String content,   final String url) {

        new android.app.AlertDialog.Builder(this)
                .setTitle("版本更新")
                .setMessage(content)
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        pBar = new CommonProgressDialog(MainActivity.this);
                        pBar.setCanceledOnTouchOutside(false);
                        pBar.setTitle("正在下载");
                        pBar.setCustomTitle(LayoutInflater.from(
                                MainActivity.this).inflate(
                                R.layout.title_dialog, null));
                        pBar.setMessage("正在下载");
                        pBar.setIndeterminate(true);
                        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pBar.setCancelable(true);
                        // downFile(URLData.DOWNLOAD_URL);
                        final DownloadTask downloadTask = new DownloadTask(MainActivity.this);

                        downloadTask.execute(url);
                        pBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                downloadTask.cancel(true);
                            }
                        });
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    /**
     * 下载应用
     *
     * @author Administrator
     */
    class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {

            //Log.d("MainActivity",  " 准备下载：" + sUrl );

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            File file = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // expect HTTP 200 OK, so we don't mistakenly save error
                // report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP "
                            + connection.getResponseCode() + " "
                            + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    String apkPath = context.getExternalCacheDir().getPath()+ File.separator + "app" + File.separator;
                    file = new File(apkPath, DOWNLOAD_NAME);

                    if (!file.exists()) {
                        // 判断父文件夹是否存在
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                    }

                } else {
                    Toast.makeText(MainActivity.this, "sd卡未挂载",
                            Toast.LENGTH_LONG).show();
                }

                // 讲下载的文件流写到本地
                input = connection.getInputStream();
                output = new FileOutputStream(file);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                //System.out.println("err:" + e.toString());
                return e.toString();

            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            pBar.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            pBar.setIndeterminate(false);
            pBar.setMax(100);
            pBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            pBar.dismiss();
            if (result != null) {

                // 申请多个权限。
                AndPermission.with(MainActivity.this)
                        .requestCode(REQUEST_CODE_PERMISSION_SD)
                        .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                        .rationale(rationaleListener
                        )
                        .send();


                Toast.makeText(context, "您未打开SD卡权限" + result, Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(context, "File downloaded" + result,
                //Toast.LENGTH_SHORT).
                //show();

                //调用，apkPath 入参就是 xml 中共享的路径
                String apkPath = context.getExternalCacheDir().getPath()+ File.separator + "app" + File.separator;
                installApk(context,apkPath );
            }

        }
    }

    private static final int REQUEST_CODE_PERMISSION_SD = 101;

    private static final int REQUEST_CODE_SETTING = 300;
    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            // 这里使用自定义对话框，如果不想自定义，用AndPermission默认对话框：
            // AndPermission.rationaleDialog(Context, Rationale).show();

            // 自定义对话框。
            AlertDialog.build(MainActivity.this)
                    .setTitle(R.string.title_dialog)
                    .setMessage(R.string.message_permission_rationale)
                    .setPositiveButton(R.string.btn_dialog_yes_permission, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.resume();
                        }
                    })

                    .setNegativeButton(R.string.btn_dialog_no_permission, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.cancel();
                        }
                    })
                    .show();
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /**
         * 转给AndPermission分析结果。
         *
         * @param object     要接受结果的Activity、Fragment。
         * @param requestCode  请求码。
         * @param permissions  权限数组，一个或者多个。
         * @param grantResults 请求结果。
         */
        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

        // 设置麦克风权限
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                Log.d("WebView", "PERMISSION FOR AUDIO");

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myRequest.grant(myRequest.getResources());

                } else {

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SETTING: {
                Toast.makeText(this, R.string.message_setting_back, Toast.LENGTH_LONG).show();

                //设置成功，再次请求更新
                JSONObject jsonObject  =  Tools.getNewVersion(Tools.getCurrentVersion(MainActivity.this),MainActivity.this);
                if(jsonObject != null)
                {
                    // 版本号不同
                    ShowDialog(jsonObject.getString("Info"), jsonObject.getString("ApkUrl"));
                }

                break;
            }
        }
    }

    // 安装APK
    private void installApk(Context context,String apkPath) {
        //安装应用
        if (TextUtils.isEmpty(apkPath)){
            Toast.makeText(context,"更新失败！未找到安装包", Toast.LENGTH_SHORT).show();
            return;
        }

        File apkFile = new File(apkPath  + DOWNLOAD_NAME);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        //Android 7.0 系统共享文件需要通过 FileProvider 添加临时权限，否则系统会抛出 FileUriExposedException .
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context,"com.skyrin.bingo.fileprovider",apkFile);
            intent.setDataAndType(contentUri,"application/vnd.android.package-archive");
        }else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(
                    Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }


    // 返回键的处理
    @Override
    public void onBackPressed() {
        // 设备号
        String sn = android.os.Build.SERIAL;
        //sn = "ac001415c3c0c6a204e";
        mWebView.loadUrl(MainActivity.this.getString(R.string.abhs_home) + "?sn=" + sn);
    }

    // 测试用 测试文件是否存在
    public static void isExist(Context context,String path) {
        File file = new File(path);
        //判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists()) {
            Toast.makeText(context,"文件夹不存在", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context,"文件夹ok", Toast.LENGTH_SHORT).show();
        }
    }
}
