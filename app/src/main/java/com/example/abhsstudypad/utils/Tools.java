package com.example.abhsstudypad.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;




import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.abhsstudypad.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by huagnshuyuan on 2017/3/16.
 */
public class Tools {
    /**
     * 检查是否存在SDCard
     *
     * @return
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 2 * 获取版本号 3 * @return 当前应用的版本号 4
     */
    public static int getCurrentVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    0);
            String version = info.versionName;
            int versioncode = info.versionCode;

            return versioncode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 获取更新版本号
    public static JSONObject getNewVersion(final int currentVersion,Context context) {
        String data = Tools.doPOST(context.getString(R.string.abhs_apkupdateurl));

        // 如果没有检测到 则直接返回
        if(data == null || data  == "") {
            return null;
        }

        // 转为JSON对象
        JSONObject jsonObject = JSON.parseObject(data);

        // 服务器最新版本号
        int newVersion = Integer.parseInt(jsonObject.getString("Version")) ;

        // apk文件下载地址
        String apkUrl = jsonObject.getString("ApkUrl") ;

        String content = "\n" +
                "学习系统发现新版本\n" +
                "\n" +
                "新版本于2021-9-1日发布，更新信息：\n" +

                "1.增加了家长模式，可以让家长控制学习机是否退出学习模式。\n" +
                "2.系统增加物理化学学科的学习端支持。\n" +
                "3.性能优化，系统载入速度增加70%。\n";//更新内容

        // 版本号不同
        if (newVersion != currentVersion) {
            if (currentVersion < newVersion) {
                return jsonObject;
            }
        }

        return null;
    }



    private static final String DEF_CHATSET = "UTF-8";
    private static String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";
    private static int DEF_CONN_TIMEOUT = 30000;

    public static String doGET(String strUrl)  {

        // 设置可以在主线程访问HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            //strUrl = strUrl + "?" + urlencode(params);
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    public static String doPOST(String strUrl)  {

        Log.d("ddd", "doPOST: " + strUrl);

        // 设置可以在主线程访问HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            //DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            //out.writeBytes(urlencode(params));
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (Exception e) {

            Log.d("MainActivity", "Exception:" + e);
            // TODO: handle exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    // 将map型转为请求参数型
    private static String urlencode(Map<String, Object> params) {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : params.entrySet()) {
            try {
                sb.append(i.getKey()).append("=")
                        .append(URLEncoder.encode(i.getValue() + "", "UTF-8"))
                        .append("&");
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        return sb.toString();
    }



    // if (VERSION.SDK_INT > 16) {
    // Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
    // final RenderScript rs = RenderScript.create(context);
    // final Allocation input = Allocation.createFromBitmap(rs, sentBitmap,
    // Allocation.MipmapControl.MIPMAP_NONE,
    // Allocation.USAGE_SCRIPT);
    // final Allocation output = Allocation.createTyped(rs, input.getType());
    // final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs,
    // Element.U8_4(rs));
    // script.setRadius(radius /* e.g. 3.f */);
    // script.setInput(input);
    // script.forEach(output);
    // output.copyTo(bitmap);
    // return bitmap;
    // }
}
