package com.example.abhsstudypad;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AbhsService extends Service {
    @Override
    public IBinder onBind(Intent intent)
    {
        return  null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flages, int startId)
    {
        Log.e("-----------服务", "启动了");

        //新建定时任务
        Runnable runnable = new Runnable() {
            //run方法中是定时执行的操作
            public void run() {

                try {
                    Thread.sleep(8000);
                    Log.e("-----------服务", "循环执行");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 定时执行
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        // * 参数一:command：执行线程
        // * 参数二:initialDelay：初始化延时
        // * 参数三:period：两次开始执行最小间隔时间
        // * 参数四:unit：计时单位
        service.scheduleAtFixedRate(runnable, 1, 2, TimeUnit.SECONDS);
        return super.onStartCommand(intent, flages, startId);
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
    }
}