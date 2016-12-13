package com.junyuan.bluetooth;

public class DeviceBean {
	private String name;
	private String mac;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(obj instanceof DeviceBean){
			return ((DeviceBean)obj).getMac().equals(this.getMac());
		}
		return false;
	}
}	
