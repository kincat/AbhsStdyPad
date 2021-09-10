package com.example.abhsstudypad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class NetworkChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int netWorkStates = NetworkUtil.getNetWorkStates(context);

        switch (netWorkStates) {
            case NetworkUtil.TYPE_NONE:
                //断网了

                Toast toast = Toast.makeText(context,"网络错误,请检查您的网络环境！",Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                break;
            case NetworkUtil.TYPE_MOBILE:
                //打开了移动网络
                break;
            case NetworkUtil.TYPE_WIFI:
                //打开了WIFI
                break;

            default:
                break;
        }
    }
}


class NetworkUtil {
    public static final int TYPE_NONE = -1;
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_WIFI = 1;

    private NetworkUtil() {
    }

    /**
     * 获取网络状态
     *
     * @param context
     * @return one of TYPE_NONE, TYPE_MOBILE, TYPE_WIFI
     * @permission android.permission.ACCESS_NETWORK_STATE
     */
    public static final int getNetWorkStates(Context context) {

        Log.e("-----------beg", "beg");

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivityManager != null) {
                Network networks = connectivityManager.getActiveNetwork();
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(networks);
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.e("-----------wifi", "wifi");

                        return  TYPE_WIFI;
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.e("-----------流量", "手机流量");

                        return TYPE_MOBILE;
                    }
                } else {
                    Log.e("------------没有网络", "没有网络");

                    return  TYPE_NONE;
                }
            }

        }

        return TYPE_NONE;
    }
}