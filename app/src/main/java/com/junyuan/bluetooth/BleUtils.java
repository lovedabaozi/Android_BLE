package com.junyuan.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
/**
 * Created by  on 2016/10/8.
 */

public class BleUtils {
    static StringBuilder sb;
    public static BleUtils instance;
    TextView tv_info;
    private static BluetoothAdapter mBluetoothAdapter;
    static boolean mScanning = false;
    private static final long SCAN_PERIOD = 10000;
    private static BluetoothDevice mDevice;
    private static Context context;
    private static BluetoothGatt mGatt;
    private static BluetoothGattCharacteristic mCharacteristic;

    private static BluetoothGattCharacteristic Resultcharacteristic;
    static FutureTask<Integer> futureTask;

    private static int mStatus = -11;
    private static Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {//开始连接蓝牙设备

                //    int i = conncetDevice();
                //   Log.e("123","--conncetDevice------"+i);
            } else if (msg.what == 1) {//状态更新：已连接

               /* try {
                    new Thread(){
                        @Override
                        public void run() {
                            nCallable = new Callable<Integer>() {
                                @Override
                                public Integer  call() throws Exception {
                                    Log.e("123","Callable================");
                                    return 200;
                                }
                            };
                        }
                    }.start();
                }catch (Exception e){
                    Log.e("123","Callable EXcetion "+e.toString());
                }*/
                Toast.makeText(context, "设备已连接", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 2) {//状态更新：已断开
                Toast.makeText(context, "连接已断开", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });


    public BleUtils() {
        instance = this;
    }

    /**
     * 初始化蓝牙设备
     *
     * @param context
     * @param tv_info
     */
    public void initBle(Context context, TextView tv_info) {
        this.tv_info = tv_info;
        this.context = context;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("linkble");
        context.registerReceiver(bleReceiver, filter);

        sb = new StringBuilder();
    }


    public BroadcastReceiver bleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("123", "收到广播开始连接蓝牙");
            scanBleDevices(true);
        }
    };

    /**
     * 扫描并连接蓝牙设备
     *
     * @param enable
     */
    public static void scanBleDevices(boolean enable) {
        if (enable) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 搜索到蓝牙设备后的回调
     */
    private static BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //  if (device.getName() != null && device.getName().equals("CCDD")) {
            mDevice = device;
            Log.e("device=", device.getName() + "----");
            Log.e("device=", device.getAddress() + "----");

            mHandler.sendEmptyMessage(0);
            // }
        }
    };

    /**
     * 蓝牙连接后的回调方法，包括连接状态、发现服务、接收数据
     */
    private static class GattCallback extends BluetoothGattCallback {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d("123", "连接状态改变" + newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {//连接成功
                Log.e("123", "连接成功");

                Log.e("123", Thread.currentThread().getName() + "-当前线程--");
                mStatus = newState;

                // sb.append("连接成功\n");
                gatt.discoverServices();
              /*  ExecutorService executorService = Executors.newSingleThreadExecutor();

                futureTask = (FutureTask<Integer>) executorService.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return 200;
                    }
                });*/
                nCallable = new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        Log.e("123", "Callable================");
                        return 200;
                    }
                };

                mHandler.sendEmptyMessage(1);
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {//断开连接
                mHandler.sendEmptyMessage(2);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("123", "发现服务" + gatt.getServices().size());
            List<BluetoothGattService> Services = gatt.getServices();
            for (BluetoothGattService service : Services) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.d("123", "characteristic" + characteristic.getUuid().toString());
                    sb.append(characteristic.getUuid().toString() + "\n");
                    //   Toast.makeText(context,"发现服务="+characteristic.getUuid().toString(),Toast.LENGTH_SHORT).show();
                    //   if (characteristic.getUuid().toString().equals("0000ff01-0000-1000-8000-00805f9b34fb")) {
                    gatt.setCharacteristicNotification(characteristic, true);//设置开启接受蓝牙数据
                    Log.e("123", "0000ff01-0000-1000-8000-00805f9b34fb");
                    mCharacteristic = characteristic;


                    // }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Resultcharacteristic = characteristic;
            Log.d("123", "onCharacteristicChanged");
            Log.e("123", "----");
            Log.d("123", "收到蓝牙发来数据：" + new String(characteristic.getValue()));
            String backData = new String(characteristic.getValue());
            Log.e("123", "==========" + backData);
            sb.append(backData);
            mCallable = new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    Log.e("123", "onCharacteristicChanged=value" + characteristic.getValue());
                    return characteristic.getValue();
                }
            };
         /*   Intent intent = new Intent();
            intent.setAction("BackData");
            intent.putExtra("BackData", backData);
            context.sendBroadcast(intent);*/


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Resultcharacteristic = characteristic;
            String s = new String(characteristic.getValue());
            Log.e("123", "onCharacteristicWrite--" + status + "---" + s);
            String s1 = Util.byteToHexString(characteristic.getValue());
            Log.e("123", s1 + "-onCharacteristicWrite---");
            mCallable = new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    Log.e("123", "onCharacteristicChanged=value" + characteristic.getValue());
                    return characteristic.getValue();
                }
            };
            //  Toast.makeText(context, "接受到蓝牙返回数据=" + s1, Toast.LENGTH_SHORT).show();
            sb.append(s1);
        }
    }
    public static int conncetDevice2(BluetoothDevice mdevices,long time) {
        mStatus=-11;
        mGatt = mdevices.connectGatt(context, false, new GattCallback());
        //  }
        mGatt.connect();
        int num= (int)time/100;
        for (int x=0;x< num;x++){
            try {
                Thread.sleep(100);
            }catch (Exception e){

            }
            Log.e("123","连接次数=="+x+"------------");
            if(mStatus!=-11){
                break;
            }
        }

        return  mStatus;
    }

    /**
     * 连接搜索到的蓝牙设备
     */
    public static int conncetDevice(BluetoothDevice mdevices) {
    /*    try {
            nCallable = new Callable<Integer>() {
                @Override
                public Integer  call() throws Exception {
                    Log.e("123","Callable================");
                    return 200;
                }
            };
        }catch (Exception e){
            Log.e("123","Callable EXcetion "+e.toString());
        }*/

        // ExecutorService threadPool = Executors.newSingleThreadExecutor();
        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        //scanBleDevices(false);
        ////////////////////////////////

        // if (mGatt == null) {
        mGatt = mdevices.connectGatt(context, false, new GattCallback());
        //  }
        mGatt.connect();
        // if(mDevice!=null){
        // Log.d("123",mDevice.getName());
        //  }
        // Log.d("123", "连接设备：" + mGatt.getDevice().getName());
        //Future<Integer> future = threadPool.submit(nCallable);
        for (; ; ) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mStatus != -11) {
                break;
            }
        }

        if (nCallable != null) {

            Future<Integer> future = threadPool.submit(nCallable);
            // Future<Integer> future1 = threadPool.submit(nCallable);
            try {
                threadPool.shutdown();
                return future.get();
                // return future.get(0, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.e("123", "Exception" + e.toString());
                return -1;
            }
        } else {

            return -1;


        }


    }

    /**
     * 重新搜索蓝牙设备
     */
    public void reConnect() {
        scanBleDevices(true);
    }

    /**
     * 断开已连接的蓝牙设备
     */
    public void disconnect() {
        if (mGatt != null) {
            mGatt.disconnect();
        }
        tv_info.setText(sb);
    }

    /**
     * 发送蓝牙数据
     *
     * @param data 要发送的数据
     * @param time 延迟时间
     */
    public void sendData(final String data, long time) {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("123", "sendData----");
                if (mGatt != null && mCharacteristic != null) {
                    byte[] otg_code = new byte[5];
                    otg_code[0] = (byte) 0x00;
                    otg_code[1] = (byte) 0x84;
                    otg_code[2] = (byte) 0x00;
                    otg_code[3] = (byte) 0x00;
                    otg_code[4] = (byte) 0x08;
                    mCharacteristic.setValue(otg_code);
                    Log.e("123", "发送数据=" + Util.byteToHexString(mCharacteristic.getValue()));
                    boolean isSend = mGatt.writeCharacteristic(mCharacteristic);

                    SystemClock.sleep(500);
                    byte[] value = Resultcharacteristic.getValue();

                    Log.e("123", "返回结果=" + Util.byteToHexString(value));
                    boolean b = mGatt.readCharacteristic(mCharacteristic);
                    Log.d("123", "是否发送成功" + isSend);
                    Toast.makeText(context, "是否发送成功=" + isSend, Toast.LENGTH_SHORT).show();
                    Log.d("123", "readCharacteristic=" + b);
                }
            }
        }, time);
    }


    /**
     * 关闭蓝牙
     */
    public void closeBle() {
        Log.d("123", "关闭所有蓝牙模块");
        if (mGatt != null) {
//            mGatt.disconnect();
            mGatt.close();
        }
        if (bleReceiver != null) {
            context.unregisterReceiver(bleReceiver);
        }
    }

    static Callable<byte[]> mCallable;
    static Callable<Integer> nCallable;

    public static byte[] send(String data, long time) {

        if (mGatt != null && mCharacteristic != null) {
            int num = (int) (time / 100);
            Resultcharacteristic = null;
            byte[] otg_code = new byte[5];
            otg_code[0] = (byte) 0x00;
            otg_code[1] = (byte) 0x84;
            otg_code[2] = (byte) 0x00;
            otg_code[3] = (byte) 0x00;
            otg_code[4] = (byte) 0x08;
            mCharacteristic.setValue(otg_code);
            Log.e("123", "发送数据=" + Util.byteToHexString(mCharacteristic.getValue()));
            boolean isSend = mGatt.writeCharacteristic(mCharacteristic);
            Log.e("123", "是否发送成功==" + isSend);
            for (int x = 0; x < num; x++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("123",x+"---------------------");
                if (Resultcharacteristic != null) {

                    Log.e("123"," 第几次循环"+ x+"-------------");
                    break;
                }

            }
            Log.d("123", "是否发送成功" + isSend);
            return Resultcharacteristic.getValue();


//////////////////////////////////////////////////////////////
    /*        ExecutorService threadPool2 = Executors.newSingleThreadExecutor();
        if (mCallable != null) {
            Future<byte[]> future2 = threadPool2.submit(mCallable);
            try {
                return future2.get();
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
        return  null;*/
            ///////////////////////////////////////////////

        }
        return  null;
    }


    public static void HUIDIao() {
        Random Random = new Random();
        int i = Random.nextInt(100);
        Log.e("123", i + "-----huidiao-----");
        final byte[] by = new byte[1];
        by[0] = (byte) i;

        mCallable = new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return by;
            }
        };
    }

}
