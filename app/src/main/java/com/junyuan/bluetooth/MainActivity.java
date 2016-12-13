package com.junyuan.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Button con, discon, send, senddelay;
    private BleUtils bleUtils;
    TextView tv_info;
    private BatteryBroadcastReceiver batteryBroadcastReceiver;
    TextView fasong;
    TextView huidiao;
    BluetoothDevice devices;
    String Mac;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_info = (TextView) findViewById(R.id.tv_info);
        bleUtils = new BleUtils();
        bleUtils.initBle(this,tv_info);
      //  BlueLibs.initBle(MainActivity.this);
        con = (Button) findViewById(R.id.btnConnect);
        discon = (Button) findViewById(R.id.btnDisConnect);
        send = (Button) findViewById(R.id.btnSend);
        senddelay = (Button) findViewById(R.id.btnSendDelay);

        fasong = (TextView) findViewById(R.id.fasong);

        huidiao = (TextView) findViewById(R.id.huidiao);

        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // int i = BleUtils.conncetDevice2(devices,1500);
                int connect = BlueLibs.Connect(Mac, 30000, false);
                Log.e("123","connect"+connect+"-----------");
               /* Intent linkbleIntent = new Intent();
                linkbleIntent.setAction("linkble");
                sendBroadcast(linkbleIntent);*/
              /*  new Thread(){

                    @Override
                    public void run() {
                        int i = BleUtils.conncetDevice(devices);
                        Log.e("123","connect"+i+"-----------");
                    }
                }.start();*/

            }
        });

        discon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = BlueLibs.DisConnect();
                Log.e("123"," 断开连接"+i+"===================");


            }
        });



        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bleUtils.sendData("haha", 0);
            byte[] otg_code = new byte[8];
              /*  Random Random=new Random();
                int i = Random.nextInt(20);*/
                otg_code[0] = (byte) 0x00;
                otg_code[1] = (byte) 0x00;
                otg_code[2] = (byte) 0x05;
                otg_code[3] = (byte) 0x80;
                otg_code[4] = (byte) 0x50;
                otg_code[5] = (byte) 0x00;
                otg_code[6] = (byte) 0x00;
                otg_code[7] = (byte) 0x00;

             /*   otg_code[0] = (byte) 0x00;
                otg_code[1] = (byte) 0x00;
                otg_code[2] = (byte) 0x05;
                otg_code[3] = (byte) 0x00;
                otg_code[4] = (byte) 0x84;
                otg_code[5] = (byte) 0x00;
                otg_code[6] = (byte) 0x00;
                otg_code[7] = (byte) 0x08;*/
           /*otg_code[0] = (byte) 0x00;
            otg_code[1] = (byte) 0x84;
            otg_code[2] = (byte) 0x00;
            otg_code[3] = (byte) 0x00;
            otg_code[4] = (byte) 0x08;*/


                byte[] transmit = BlueLibs.Transmit(getRandombyte(100), 8000);
                if(transmit!=null){
                    Toast.makeText(MainActivity.this,Util.byteToHexString(transmit),Toast.LENGTH_SHORT).show();
                    Log.e("dabaozi","发送指令"+transmit[transmit.length-2]+"-----------"+transmit[transmit.length-1]+"-----------");
                }

            }
        });

        senddelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  bleUtils.sendData("delay", 1000);
                //搜索
            Intent intent=new Intent(MainActivity.this,ScanBleDevice.class);
            startActivityForResult(intent,100);
            }
        });

        batteryBroadcastReceiver = new BatteryBroadcastReceiver();
        IntentFilter filter = new IntentFilter("BackData");
        registerReceiver(batteryBroadcastReceiver, filter);
        fasong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] send = BleUtils.send("111",15000);

                Log.e("123","MAinActivity="+ Arrays.toString(send)+"---");
             /*   new Thread(){
                    @Override
                    public void run() {
                        Log.e("123","kaishi=====");

                        try {
                            byte[] send = BleUtils.send("111");

                            Log.e("123","MAinActivity="+send[0]+send[1]+"---");
                        }catch (Exception e){
                            Log.e("123","fasong Exception");
                        }

                    }
                }.start();*/

            }
        });

        huidiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleUtils.HUIDIao();
            }
        });
        Log.e("123", Thread.currentThread().getName() + "--main-当前线程--");
    }



    class BatteryBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String backData = intent.getStringExtra("BackData");

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleUtils.closeBle();
        unregisterReceiver(batteryBroadcastReceiver);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == 100){

            devices = (BluetoothDevice)data.getParcelableExtra("devices");
             Mac = devices.getAddress();
            Log.e("123","name="+ devices.getName());


        }
    }



    public static byte[] getRandombyte(int x) {
        byte[] b=new byte[x];
        for(int i = 0; i < x; i++){
            b[i]=(byte) i;
        }

        return b;

    }

}
