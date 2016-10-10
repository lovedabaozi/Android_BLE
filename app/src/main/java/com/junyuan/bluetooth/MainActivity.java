package com.junyuan.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button con, discon, send, senddelay;
    private BleUtils bleUtils;
    private BatteryBroadcastReceiver batteryBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bleUtils = new BleUtils();
        bleUtils.initBle(this);

        con = (Button) findViewById(R.id.btnConnect);
        discon = (Button) findViewById(R.id.btnDisConnect);
        send = (Button) findViewById(R.id.btnSend);
        senddelay = (Button) findViewById(R.id.btnSendDelay);

        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent linkbleIntent = new Intent();
                linkbleIntent.setAction("linkble");
                sendBroadcast(linkbleIntent);
            }
        });

        discon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleUtils.disconnect();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleUtils.sendData("haha", 0);
            }
        });

        senddelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleUtils.sendData("delay", 1000);
            }
        });

        batteryBroadcastReceiver = new BatteryBroadcastReceiver();
        IntentFilter filter = new IntentFilter("BackData");
        registerReceiver(batteryBroadcastReceiver, filter);


    }

    class BatteryBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String backData = intent.getStringExtra("BackData");
            Log.d("123", "MoreMusicActivity的广播接收到：" + backData);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleUtils.closeBle();
        unregisterReceiver(batteryBroadcastReceiver);
    }
}
