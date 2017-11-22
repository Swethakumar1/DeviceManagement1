package dev;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.google.gson.Gson; 

public class DeviceManagementBinder extends AbstractBinder  {

	@Override
	protected void configure() {
		bindAsContract(AWSIoTDeviceOperations.class).in(Singleton.class);
		bindAsContract(DeviceCertificateManagement.class).in(Singleton.class);
		bindAsContract(Gson.class);
	}
}
