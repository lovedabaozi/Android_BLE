package com.junyuan.bluetooth;

import java.util.ArrayList;

//import com.haitaichina.keydemo.api.ClsUtils;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothConnectActivityReceiver extends BroadcastReceiver {
	public static ArrayList<String> noteMacLists = new ArrayList<String>();
	String strPsw = "000000";
	private String TAG = "BluetoothConnectActivityReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction())) {
			Log.d(TAG, "receive broadcast ACTION_PAIRING_REQUEST");
			BluetoothDevice btDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// btDevice.setPairingConfirmation(true);
			// // byte[] pinBytes = BluetoothDevice.convertPinToBytes("1234");
			// // device.setPin(pinBytes);
			try {
				ClsUtils.setPin(btDevice.getClass(), btDevice, strPsw); //
				// �ֻ��������ɼ������
				ClsUtils.createBond(btDevice.getClass(), btDevice);
				ClsUtils.cancelPairingUserInput(btDevice.getClass(), btDevice);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent
				.getAction())) {
			Log.d(TAG, "receive broadcast ACTION_ACL_CONNECTED");
			BluetoothDevice btDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (btDevice.getName().startsWith("HT")
					|| btDevice.getName().startsWith("ht")) {
				Log.e("BLUELIB2", btDevice.getAddress());
				noteMacLists.add(btDevice.getAddress());
			}
		} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent
				.getAction())) {
			Log.d(TAG, "receive broadcast ACTION_ACL_DISCONNECTED");
			BluetoothDevice btDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (btDevice.getName().startsWith("HT")
					|| btDevice.getName().startsWith("ht")) {
				if (noteMacLists.contains(btDevice.getAddress())) {
					noteMacLists.remove(btDevice.getAddress());
				}
			}
		}
	}

}
