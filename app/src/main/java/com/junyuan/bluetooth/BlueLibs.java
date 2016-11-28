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
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class BlueLibs {
    private static BluetoothGatt mGatt;
    private static BluetoothGattCharacteristic mCharacteristic;
    private static BluetoothGattCharacteristic Resultcharacteristic;
    private static  byte[] result=null;
    private  static BluetoothAdapter mBluetoothAdapter;
    private static Context mContext;
    private static int mStatus;
    private static boolean mScanning = false;
    private  static long SCAN_PERIOD = 10000;
    private static BluetoothGattCharacteristic mReadCharacteristic;
   private  static  final  String TAG= "BlueLibs";
    private   static  int tag;
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
            result=null;
          byte[] buffer = new byte[cmd.length + 3];

          /*  buffer[0] = 0x0b;
            buffer[1] = (byte) (cmd.length >> 8);
            buffer[2] = (byte) cmd.length;
            System.arraycopy(cmd, 0, buffer, 3, cmd.length);*/
            byte[] by=new byte[600];
            for (int x=0;x<600;x++){
                by[x]=0x00;
            }
            mCharacteristic.setValue(cmd);
            Log.e(TAG, "发送数据=" + Util.byteToHexString(mCharacteristic.getValue()));
            boolean isSend = mGatt.writeCharacteristic(mCharacteristic);

            tag = 0;
            Log.e(TAG, "是否发送成功==" + isSend);
            Log.e(TAG, "===========2======" + isSend);
            for (int x = 0; x < num; x++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e(TAG,x+"---------------------");
                if (result != null) {
                    Log.e(TAG," 第几次循环"+ x+"-------------");
                    break;
                }
            }
            Log.d(TAG, "是否发送成功" + isSend);
            if(result!=null){
                return result;
            }

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
      /*  try {

            // 配对
            boolean brv = pair(mDevice, "000000");// String.valueOf(BluetoothDevice.PAIRING_VARIANT_PIN));
            if (!brv) {
                throw new Exception("pair return false");
            }

        } catch (Exception e) {
            return -2;
        }*/
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
        }
        if(mStatus==2){
            mStatus=0;

            return  0;
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
        Log.e("onConnectionStateChange","连接状态==="+status);
            Log.d(TAG, "连接状态改变" + newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {//连接成功
                Log.e(TAG, "连接成功");
                mStatus = newState;
               // gatt.requestMtu(110);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Log.e(TAG,"----SDK_INT--");
                   // boolean b = gatt.requestMtu(110);
                  //  Log.e(TAG,b+"------");
                }
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
                    Log.d("dabaozi", "characteristic" + characteristic.getUuid().toString());
               if(characteristic.getUuid().toString().equals("0783b03e-8535-b5a0-7140-a304d2495cba")){
                   Log.e("dabaozi", "test-----"+characteristic.getUuid());
                        mCharacteristic = characteristic;
                   }
                 if(characteristic.getUuid().toString().equals("0783b03e-8535-b5a0-7140-a304d2495cb8")){
                     mReadCharacteristic=  characteristic;
                     gatt.setCharacteristicNotification(characteristic, true);//设置开启接受蓝牙数据
                     Log.e("dabaozi", "test-----"+characteristic.getUuid());
                 }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e("dabaozi","onCharacteristicRead-------"+Arrays.toString(characteristic.getValue()));
        }
        int length;
        byte[] all;
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

          /*  Resultcharacteristic = characteristic;
            Log.d("dabaozi", "onCharacteristicChanged");
            Log.e("---------", Arrays.toString(characteristic.getValue()));

            Log.e(TAG, "----");
            Log.d(TAG, "收到蓝牙发来数据：" + new String(characteristic.getValue()));
            String backData = new String(characteristic.getValue());
            Log.e(TAG, "==========" + backData);
*/
            byte[] value = characteristic.getValue();
        if(tag==0){
            Log.e("onChanged-get-------",value[1]+"----"+value[2]);

            length = (0xFF & value[1]) * 256 + (0xFF & value[2]);
            length=length+3;
            all = new byte[length];
            Log.e(TAG,"length=="+length);

        }
            Log.e("dabaozi====",Arrays.toString(characteristic.getValue()));

                System.arraycopy(characteristic.getValue(),0,all,tag,characteristic.getValue().length);
                tag=tag+characteristic.getValue().length;
            Log.e(TAG,Arrays.toString(all));
            if(length==tag){
                result=all;
            }

        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.e(TAG,"mtu==="+mtu);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG,"mtu==="+mtu);
               // this.supportedMTU = mtu;//local var to record MTU size
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
          //  Resultcharacteristic = characteristic;
            String s = new String(characteristic.getValue());
            String s1 = Util.byteToHexString(characteristic.getValue());
            Log.e(TAG, s1 + "-onCharacteristicWrite---");
            Log.e(TAG,characteristic.getValue().length+"指令长度");
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


    public static boolean pair(BluetoothDevice device, String strPsw)
            throws Exception {
        boolean result = false;
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            result = ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
            result = true;
            if (!result) {
                throw new Exception("SetPIN Error");
            } else {
                // Log.d(TAG, "SetPIN Suc");
                result = ClsUtils.createBond(device.getClass(), device);
                if (!result) {
                    throw new Exception("createBond Fail");
                }
            }

            int i = 0;
            for (i = 0; i < 50; i++) {
                // Log.d(TAG, "getBondState:" + i);
                Thread.sleep(100);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    // Log.d(TAG, "getBondState OK:" + i);
                    result = true;
                    break;
                }
            }

            if (i == 50) {
                result = false;
            }
        } else {
            result = true;
        }

        return result;
    }
}
