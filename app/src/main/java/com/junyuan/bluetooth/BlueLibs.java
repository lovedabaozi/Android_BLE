package com.junyuan.bluetooth;

/**
 * Created by 张峰林
 * Created on 2016/10/24.
 * Description：
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;


public class BlueLibs {
    private static BluetoothGatt mGatt;
    private static BluetoothGattCharacteristic mCharacteristic;
    private static BluetoothGattCharacteristic Resultcharacteristic;
    private  static BluetoothAdapter mBluetoothAdapter;
    private static Context mContext;
    private static int mStatus;
    private static boolean mScanning = false;
    private  static long SCAN_PERIOD = 10000;

   private  static  final  String TAG= "BlueLibs";
    static Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    /**
     * 初始化BLe
     * @param context
     */
    public static void initBle(Context context){
        mContext=context;
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
    }
    /**
     * 发送指令的方法
     *   cmd: 指令
     *   time: 超时时间
     */
    public  static  byte[] Transmit(byte[ ] cmd,long time){

        if (mGatt != null && mCharacteristic != null&& cmd!=null) {
            int num = (int) (time / 100);
            Resultcharacteristic = null;
         /*   byte[] buffer = new byte[cmd.length + 3];

            buffer[0] = 0x0b;
            buffer[1] = (byte) (cmd.length >> 8);
            buffer[2] = (byte) cmd.length;
            System.arraycopy(cmd, 0, buffer, 3, cmd.length);*/
            mCharacteristic.setValue(cmd);
            Log.e(TAG, "发送数据=" + Util.byteToHexString(mCharacteristic.getValue()));
            boolean isSend = mGatt.writeCharacteristic(mCharacteristic);
            Log.e(TAG, "是否发送成功==" + isSend);
            for (int x = 0; x < num; x++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e(TAG,x+"---------------------");
                if (Resultcharacteristic != null) {
                    Log.e(TAG," 第几次循环"+ x+"-------------");
                    break;
                }
            }
            Log.d(TAG, "是否发送成功" + isSend);
            return Resultcharacteristic.getValue();
        }
        return  null;
    }

    /**
     *  连接key
     * @param mac  mac地址
     * @return
     */
    public  static int Connect(String mac, long time, boolean IsAuto){
        mStatus=-1;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice mDevice = adapter.getRemoteDevice(mac);
        if(mDevice==null){
            return  -1;
        }
        mGatt = mDevice.connectGatt(mContext, IsAuto, new GattCallback());
        mGatt.connect();
        int num= (int)time/100;
        for (int x=0;x< num;x++){
            try {
                Thread.sleep(100);
            }catch (Exception e){
                return  -1;
            }
            Log.e(TAG,"连接次数=="+x+"------------");
            if(mStatus!=-1){
                break;
            }
            if(mStatus==2){
                return  0;
            }
        }
        return  mStatus;
    }



    /**
     * 蓝牙连接后的回调方法，包括连接状态、发现服务、接收数据
     */
    static class GattCallback extends BluetoothGattCallback {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d(TAG, "连接状态改变" + newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {//连接成功
                Log.e(TAG, "连接成功");
                mStatus = newState;
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {//断开连接
                mStatus=newState;
                Log.e(TAG, "连接失败");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "发现服务" + gatt.getServices().size());
            List<BluetoothGattService> Services = gatt.getServices();
            for (BluetoothGattService service : Services) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.d(TAG, "characteristic" + characteristic.getUuid().toString());
                    //   Toast.makeText(context,"发现服务="+characteristic.getUuid().toString(),Toast.LENGTH_SHORT).show();
                    //   if (characteristic.getUuid().toString().equals("0000ff01-0000-1000-8000-00805f9b34fb")) {
                    gatt.setCharacteristicNotification(characteristic, true);//设置开启接受蓝牙数据
                    Log.e(TAG, "0000ff01-0000-1000-8000-00805f9b34fb");
                    mCharacteristic = characteristic;


                    // }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Resultcharacteristic = characteristic;
            Log.d(TAG, "onCharacteristicChanged");
            Log.e(TAG, "----");
            Log.d(TAG, "收到蓝牙发来数据：" + new String(characteristic.getValue()));
            String backData = new String(characteristic.getValue());
            Log.e(TAG, "==========" + backData);


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Resultcharacteristic = characteristic;
            String s = new String(characteristic.getValue());
            Log.e(TAG, "onCharacteristicWrite--" + status + "---" + s);
            String s1 = Util.byteToHexString(characteristic.getValue());
            Log.e(TAG, s1 + "-onCharacteristicWrite---");
        }
    }

    /**
     * 断开蓝牙设备
     * @return
     */
     public  static  int DisConnect(){
         try {
             if (mGatt != null) {
                 mGatt.disconnect();
             }
         }catch ( Exception e){
             return  -1;
         }
         return  0;

     }

    /**
     * 判断手机是否支持BLE
     * @return
     */
    public  static  boolean  isSupportBle(){
        if(mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return  true;
        }
        return  false;
    }

    /**
     *
     * @param enable   true: 开始扫描 ble蓝牙    flase： 关闭扫描ble蓝牙
     * @param mLeScanCallback  扫描回调
     */
    public  static  void scanBleDevices(boolean enable, final BluetoothAdapter.LeScanCallback mLeScanCallback){
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
}
