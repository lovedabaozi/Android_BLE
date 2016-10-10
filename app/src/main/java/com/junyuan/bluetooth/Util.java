package com.junyuan.bluetooth;

import android.os.Environment;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class Util {
	/**
	 * byte数组转化16进制字符串
	 * @param b
	 * @return
	 */
	public static String byteToHexString(byte[] b)
	{
		if(b == null) return null;
		String a = "";
		for (int i = 0; i < b.length; i++)
		{
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1)
			{
				hex = '0' + hex;
			}

			a = a + hex;
		}

		return a;
	}
	/**
	 * byte数组转化16进制字符串
	 * @param b
	 * @return
	 */
	public static String byteToHexString(byte[] b, int len)
	{
		if(b == null) return null;
		String a = "";
		for (int i = 0; i < len; i++)
		{
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1)
			{
				hex = '0' + hex;
			}
			
			a = a + hex;
		}
		
		return a.toUpperCase(Locale.CHINA);
	}
	
	/**
	 * 日期格式化(yyyy-MM-dd HH:mm:ss)
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date){
		if(date == null){
			return null;
		}
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	/**
	 * 解析证书颁发者或者颁给者
	 * @param dn
	 * @return
	 */
	public static String parseInfo(String dn) {
		if(TextUtils.isEmpty(dn)) return null;
        String[] dns = dn.split(",");
		if(dns == null) return null;
        for (String str : dns) {
            if(str.contains("cn=") || str.contains("CN=")){
                return str.substring(str.indexOf("=") + 1);
            }
        }
        return null;
    }
	
	 /**
     * byte数组转int类型,数组长度必须为4
     * @param src
     * @return
     */
    public static int bytesToInt(byte[] src) {
    	if(src == null || src.length != 4){
    		return -1;
    	}
		int value = 0;	
		value = (int) ((src[0] & 0xFF) 
				| ((src[1] & 0xFF)<<8) 
				| ((src[2] & 0xFF)<<16) 
				| ((src[3] & 0xFF)<<24));
		return value;
	}
    
    public static String getFileErrorPath() {
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			String path = Environment.getExternalStorageDirectory().getPath() + "/haitaiChina/HTCLibLog/";
			return path;
		}
		return null;
	}
    
    public static boolean isNumberString(String str){
    	return Pattern.matches("^\\d+$", str);
    }
}
