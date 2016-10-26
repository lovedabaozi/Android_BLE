package com.junyuan.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by 张峰林
 * Created on 2016/10/21.
 * Description：
 */
public class ScanBleDevice  extends Activity implements AdapterView.OnItemClickListener {
     boolean mScanning = false;
    private  final long SCAN_PERIOD = 10000;
    private  BluetoothDevice mDevice;
    private  BluetoothAdapter mBluetoothAdapter;
    MyAdapter myAdapter;
    ArrayList<BluetoothDevice> BlueList;
    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanbledevice);
        init();
      scanBleDevices(true);
        //BlueLibs.scanBleDevices(true,mLeScanCallback);
    }

    private void init() {

        ListView list_devices = (ListView) findViewById(R.id.list_devices);

        BlueList = new ArrayList<>();
        myAdapter = new MyAdapter();
        list_devices.setAdapter(myAdapter);
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
        list_devices.setOnItemClickListener(this);
    }


    /**
     * 扫描蓝牙设备
     * @param enable   true：开始扫面    false： 停止扫面
     *
     */
    public  void scanBleDevices(boolean enable) {
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
    private  BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //  if (device.getName() != null && device.getName().equals("CCDD")) {
            mDevice = device;
            Log.e("device=",device.getName()+"----");
            Log.e("device=",device.getAddress()+"----");
            if(!BlueList.contains(device)){
                BlueList.add(device);
            }
            myAdapter.notifyDataSetChanged();
            // }
        }
    };



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent data = getIntent();
        Bundle Bundle=new Bundle();
        Bundle.putParcelable("devices",BlueList.get(position));
        data.putExtras(Bundle);
       // data.putExtra("device", BlueList.get(position));
        setResult(RESULT_OK, data );
        finish();
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return BlueList == null ? 0 : BlueList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return BlueList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView != null){
                holder = (ViewHolder) convertView.getTag();
            }else{
                holder = new ViewHolder();
                convertView = View.inflate(ScanBleDevice.this, R.layout.bt_item, null);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_device_name);
                holder.tv_mac = (TextView) convertView.findViewById(R.id.tv_device_mac);
                convertView.setTag(holder);
            }
            String name = BlueList.get(position).getName();
            if(TextUtils.isEmpty(name)){
                name = "未知名称";
            }
            holder.tv_name.setText(name);
            holder.tv_mac.setText(BlueList.get(position).getAddress());

            return convertView;
        }

        class ViewHolder {
            TextView tv_name;
            TextView tv_mac;
        }



    }


}
