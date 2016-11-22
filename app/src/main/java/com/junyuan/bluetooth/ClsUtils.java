package com.junyuan.bluetooth;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;

public class ClsUtils {

	// private static String TAG = "ClsUtils";

	// 取消配对
	static public boolean cancelBondProcess(Class<?> btClass,
											BluetoothDevice device)

			throws Exception {
		Method createBondMethod = btClass.getMethod("cancelBondProcess");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	// 取消用户输入
	static public boolean cancelPairingUserInput(Class<?> btClass,
												 BluetoothDevice device)

			throws Exception {
		Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
		// cancelBondProcess()
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	public static byte[] convertPinToBytes(String pin) {
		if (pin == null) {
			return null;
		}
		byte[] pinBytes;
		try {
			pinBytes = pin.getBytes("UTF-8");
		} catch (UnsupportedEncodingException uee) {
			// Log.e(TAG, "UTF-8 not supported?!?"); // this should not happen
			return null;
		}
		if (pinBytes.length <= 0 || pinBytes.length > 16) {
			return null;
		}
		return pinBytes;
	}

	static public boolean createBond(Class<?> btClass, BluetoothDevice btDevice)
			throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	public static void printAllInform(Class<?> clsShow) {
		try {
			// 取得所有方法
			Method[] hideMethod = clsShow.getMethods();
			int i = 0;
			for (; i < hideMethod.length; i++) {
				// Log.d(TAG, "method name" + hideMethod[i].getName() + ";and
				// the i is:" + i);
			}
			// 取得所有常量
			Field[] allFields = clsShow.getFields();
			// for (i = 0; i < allFields.length; i++) {
			// Log.d(TAG, "Field name" + allFields[i].getName());
			// }
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public boolean removeBond(Class<?> btClass, BluetoothDevice btDevice)
			throws Exception {
		Method removeBondMethod = btClass.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	static public boolean setPin(Class<?> btClass, BluetoothDevice btDevice,
								 String str) throws Exception {
		boolean returnValue = false;
		try {
			Method removeBondMethod = btClass.getDeclaredMethod("setPin",
					new Class[] { byte[].class });
			returnValue = (Boolean) removeBondMethod.invoke(btDevice,
					new Object[] { convertPinToBytes(str) });
			// Log.e(TAG, "setPin returnValue " + returnValue);
		} catch (SecurityException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnValue;

	}

}
