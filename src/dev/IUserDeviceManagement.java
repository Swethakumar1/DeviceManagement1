package dev;

public interface IUserDeviceManagement {
	public String getDeviceId();
		
	public String generateCertificate();
	
	public void connectDeviceToCertificate(String deviceId, String certificateId);
}
