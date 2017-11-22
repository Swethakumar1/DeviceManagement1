package dev;

import java.util.List;

public interface IDeviceManagement{
	public void connectDevicesInGroups(List<Integer> devicesPerGroup);
	
	public void connectDevicesWithExistingCertificate(String certificateId, int numberOfDevices);
	
	public void connectDevice(String deviceId);
			
	public void reconnectAllDevicesWithExistingCertificate(String certificateId);
	
	public void reconnectDevice(String deviceId);
	
	public void getConnectedDevices();
	
	public void getConnectedDevices(String certificateId);
	
	public void getDisconnectedDevices();
	
	public void getDisconnectedDevices(String certificateId);
	
	public void deactivateCertificate(String certificateId);
	
	public void disconnectDevice(String deviceId);
	
	public void disconnectDevices(String certificateId);
			
	public void publishTopic(String deviceId, String topic, String message);
	
	public void subscribeTopic(String deviceId, String topic);
	
	public void unsubscribe(String deviceId, String topic);
	
	public void deleteDevice(String deviceId);
	
	public void deleteDevices(String certificateId);
	
	public void disconnectAllDevices();
}