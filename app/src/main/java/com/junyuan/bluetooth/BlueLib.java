package com.junyuan.bluetooth;

/**
 * Created by 张峰林
 * Created on 2016/10/24.
 * Description：
 */

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class BlueLib {
	private static BluetoothGatt mGatt;
	private static BluetoothGattCharacteristic mCharacteristic;
	private static BluetoothGattCharacteristic Resultcharacteristic;
	private  static BluetoothAdapter mBluetoothAdapter;
	private static Context mContext;
	private static int mStatus;
	private static boolean isBleConnet=false;
	//-------------spp协议-------------
	private static BluetoothSocket bsocket = null;
	private static BluetoothAdapter adapter;
	private  static  final  String TAG= "BlueLibs";
	private static final String UUID_WRITE="0783b03e-8535-b5a0-7140-a304d2495cba";
	private static final String UUID_READ="0783b03e-8535-b5a0-7140-a304d2495cb8";


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
	public  static  byte[] Transmit(byte[ ] cmd){
		if(isBleConnet){
			long time=1500;
			if (mGatt != null && mCharacteristic != null&& cmd!=null) {
				int num = (int) (time / 100);
				Resultcharacteristic = null;

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
				if(Resultcharacteristic!=null){
					return Resultcharacteristic.getValue();
				}else{
					return null;
				}
			}else{
				return null;
			}
		}else{
			byte[] transmit = Transmitspp(cmd);
			return transmit;
		}

	}

	/**
	 *  连接key
	 * @param mac  mac地址
	 * @return
	 */
	public  static int Connect(String mac){
		long time=2500;
		boolean IsAuto=false;
		mStatus=-1;
		adapter = BluetoothAdapter.getDefaultAdapter();
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
			//TODO  发送指令  断开指令切换到 spp

			isBleConnet=true;
			return  0;
		}else{

			Log.e(TAG, "BLE 连接失败 切换到spp连接");
			//TODO   spp连接
			mStatus = Connectspp(mac);
			if(mStatus==0){
				isBleConnet=false;
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
			Log.e("onConnectionStateChange","连接状态==="+status);
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
					Log.d("dabaozi", "characteristic" + characteristic.getUuid().toString());
					if(characteristic.getUuid().toString().equals(UUID_WRITE)){

						mCharacteristic = characteristic;
					}
					if(characteristic.getUuid().toString().equals(UUID_READ)){
						gatt.setCharacteristicNotification(characteristic, true);//设置开启接受蓝牙数据

					}
				}
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);

		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Resultcharacteristic = characteristic;
			Log.d("dabaozi", "onCharacteristicChanged");

			Log.d(TAG, "收到蓝牙发来数据：" + new String(characteristic.getValue()));



		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);

		}
	}

	/**
	 * 断开蓝牙设备
	 * @return
	 */
	public  static  int DisConnect(){
		if(isBleConnet){
			try {
				if (mGatt != null) {
					mGatt.disconnect();
				}
			}catch ( Exception e){
				return  -1;
			}
			return  0;
		}else{
			int disConnectspp = DisConnectspp();
			return disConnectspp;
		}

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



	public static String List() {
		String macs = "";
		BluetoothDevice device = null;

		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		Iterator<BluetoothDevice> devices = adapter.getBondedDevices()
				.iterator();

		while (devices.hasNext()) {
			device = (BluetoothDevice) devices.next();

			if (device.getName().substring(0, 2).equalsIgnoreCase("HT")) {
				// macs.add(device.getAddress());
				if (!macs.equals("")) {
					macs += ";";
				}
				macs += device.getAddress();
			}
		}


		// return (String[])macs.toArray();
		return macs;
	}

	@SuppressLint("NewApi")
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
			for (i = 0; i < 20; i++) {
				// Log.d(TAG, "getBondState:" + i);
				Thread.sleep(1000);
				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					// Log.d(TAG, "getBondState OK:" + i);
					result = true;
					break;
				}
			}

			if (i == 20) {
				result = false;
			}
		} else {
			result = true;
		}

		return result;
	}

	//TODO----------------------------

	public static int Connectspp(String mac) {

		// 检查设备是否支持蓝牙
		//	BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		/*if (adapter == null) {
			// 设备不支持蓝牙
			 Log.e("conn", "getDefaultAdapter Error!");
			return -1;
		}*/

		BluetoothSocket tmpbs = null;

		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

		try {
			BluetoothDevice bd = adapter.getRemoteDevice(mac);

			// 配对
			boolean brv = pair(bd, "000000");// String.valueOf(BluetoothDevice.PAIRING_VARIANT_PIN));
			if (!brv) {
				throw new Exception("pair return false");
			}
			Log.e("conn", "getDefa---2--pter Error!");

			tmpbs = bd.createInsecureRfcommSocketToServiceRecord(uuid);

		} catch (Exception e) {
			return -2;
		}
		try {
			bsocket = tmpbs;
			bsocket.connect();
		} catch (Exception e) {
			Log.e("e----", "connect Error!" + e.getMessage());

			try {
				Thread.sleep(1);
				BluetoothDevice bd = adapter.getRemoteDevice(mac);
				Log.e("conn", "getDefa---3--pter Error!");
				// 配对
				boolean brv = pair(bd, "000000");// String.valueOf(BluetoothDevice.PAIRING_VARIANT_PIN));
				if (!brv) {
					throw new Exception("pair return false");
				}

				tmpbs = bd.createInsecureRfcommSocketToServiceRecord(uuid);

				bsocket = tmpbs;
				bsocket.connect();
			} catch (Exception e1) {
				Log.e("e1", "connect Error!" + e1.getMessage());
				return -5;
			}

		}
		return 0;
	}



	public static byte[] Transmitspp(final byte[] cmd) {

		Log.e(TAG, Util.byteToHexString(cmd));
		byte[] rvb = null;
		if (cmd.length == 21) {
			String scmd = new String(cmd);
			Log.e("BLUELIB", "scmd=" + scmd);

			if (scmd.startsWith("MAC:")) {
				String smac = scmd.substring(4, 21);
				if (BluetoothConnectActivityReceiver.noteMacLists
						.contains(smac)) {
					Log.e("BLUELIB",
							"BluetoothConnectActivityReceiver Closed Error!");
					return null;
				}
			}
		}

		byte[] buffer = new byte[cmd.length + 3];

		buffer[0] = 0x0b;
		buffer[1] = (byte) (cmd.length >> 8);
		buffer[2] = (byte) cmd.length;
		System.arraycopy(cmd, 0, buffer, 3, cmd.length);

		try {
			Log.e(TAG, "spp 写指令----");
			bsocket.getOutputStream().write(buffer);
		} catch (IOException e) {
			Log.e("BLUELIB", "bsocket Write Error!" + e.getMessage());
			return null;
		}

		byte[] header = new byte[3];

		int rvbl = 0;
		int rvbl2 = 0;
		int rlen = 0;
		int rlena = 0;

		try {
			InputStream is = bsocket.getInputStream();
			rvbl = is.read(header);
			if (rvbl != 3) {
				rvbl2 = is.read(header, rvbl, 3 - rvbl);
				if (rvbl + rvbl2 != 3) {
					throw new IOException("Header Length Error");
				}
			}

			rvbl = (0xFF & header[1]) * 256 + (0xFF & header[2]);

			rvb = new byte[rvbl];
			while (rlena < rvbl) {
				rlen = is.read(rvb, rlena, rvbl - rlena);
				rlena += rlen;
			}
		} catch (IOException e) {
			Log.e(TAG, "bsocket Read Error!" + e.getMessage());
			return null;
		}


		try {
			Log.e(TAG, rvb[0]+"--"+rvb[1]+"--");
		} catch (Exception e) {

		}

		return rvb;

	}

	public static int DisConnectspp() {
		try {

			{
				bsocket.close();
			}
		} catch (Exception e) {
			Log.e("BLUELIB", "bsocket Close Error!" + e.getMessage());
			return -1;
		}

		return 0;
	}

}
