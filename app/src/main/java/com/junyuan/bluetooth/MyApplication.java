package com.junyuan.bluetooth;

import android.app.Application;

/**
 * Created by 张峰林
 * Created on 2016/10/25.
 * Description：
 */
public class MyApplication  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BlueLibs.initBle(getApplicationContext());
    }
}
