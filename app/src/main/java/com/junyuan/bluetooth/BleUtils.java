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
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by JY on 2016/10/8.
 */

public class BleUtils {

    public static BleUtils instance;

    private BluetoothAdapter mBluetoothAdapter;
    boolean mScanning = false;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothDevice mDevice;
    private Context context;
    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic mCharacteristic;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {//开始连接蓝牙设备
                conncetDevice();
                Log.e("111111111111111","--------");
            } else if (msg.what == 1) {//状态更新：已连接
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
     */
    public void initBle(Context context) {
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
    public void scanBleDevices(boolean enable) {
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
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
          //  if (device.getName() != null && device.getName().equals("CCDD")) {
                mDevice = device;
            Log.e("device=",device.getName()+"----");
            Log.e("device=",device.getAddress()+"----");
                mHandler.sendEmptyMessage(0);
           // }
        }
    };

    /**
     * 蓝牙连接后的回调方法，包括连接状态、发现服务、接收数据
     */
    private class GattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d("123", "连接状态改变" + newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {//连接成功
                Log.e("123","连接成功");
                gatt.discoverServices();
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
                    if (characteristic.getUuid().toString().equals("00002a00-0000-1000-8000-00805f9b34fb")) {
                        gatt.setCharacteristicNotification(characteristic, true);//设置开启接受蓝牙数据
                        Log.e("123","00002a00-0000-1000-8000-00805f9b34fb");
                        mCharacteristic = characteristic;
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("123", "onCharacteristicChanged");
            Log.e("123","----");
            Log.d("123", "收到蓝牙发来数据：" + new String(characteristic.getValue()));
            String backData = new String(characteristic.getValue());
                Log.e("123","=========="+backData);
         /*   Intent intent = new Intent();
            intent.setAction("BackData");
            intent.putExtra("BackData", backData);
            context.sendBroadcast(intent);*/
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            String s = new String(characteristic.getValue());
           // Log.e("123","onCharacteristicWrite--"+status+"---"+s);
            String s1 = Util.byteToHexString(characteristic.getValue());
            Log.e("123",s1+"----");
        }
    }


    /**
     * 连接搜索到的蓝牙设备
     */
    private void conncetDevice() {
        scanBleDevices(false);
        if (mGatt == null) {
            mGatt = mDevice.connectGatt(context, false, new GattCallback());
        }
        mGatt.connect();
        Log.d("123", "连接设备：" + mGatt.getDevice().getName());
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
                if (mGatt != null && mCharacteristic != null) {
                    byte[] otg_code = new byte[5];
                    otg_code[0] = (byte) 0x00;
                    otg_code[1] = (byte) 0x84;
                    otg_code[2] = (byte) 0x00;
                    otg_code[3] = (byte) 0x00;
                    otg_code[4] = (byte) 0x08;
                    mCharacteristic.setValue(otg_code);
                    boolean isSend = mGatt.writeCharacteristic(mCharacteristic);
                    boolean b = mGatt.readCharacteristic(mCharacteristic);
                    Log.d("123", "是否发送成功" + isSend);
                    Log.d("123","readCharacteristic="+b);
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

}
