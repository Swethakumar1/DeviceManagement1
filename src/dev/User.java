package dev;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;

@Path("/user")
public class User {
	@Inject AWSIoTDeviceOperations deviceOperations;
	@Inject DeviceCertificateManagement deviceCertManagement;
	@Inject Gson gson;
	
	@GET
	@Path("/connectdevice")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response connectDevice(@QueryParam("deviceId") final String deviceId){
		
		// Check whether  the MAC address is  white-listed.
		boolean exists = false;
		try{
			exists = deviceCertManagement.getMacAddress(deviceId);
		} catch (SQLException ex){}
		
		if (!exists){
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "MAC address not authenticated."))).build(); 
		}
		
		List<Device> devices = null;
		AwsIoTDeviceCertificate deviceCert = null;
		
		// Retrieve device
		try{
	        devices = deviceCertManagement.getDevices(deviceId);
		} catch (SQLException ex){}
		
		if (devices.size() == 0){
			deviceCert = deviceOperations.getAwsIoTDeviceCertificate();
			List<AwsIoTDeviceCertificate> certs = new ArrayList<AwsIoTDeviceCertificate>();
			certs.add(deviceCert);
			
			devices = new ArrayList<Device>();
			Device device = new Device(deviceId, certs.get(0).getCertId());
			devices.add(device);
			deviceOperations.connect(deviceCert, devices, false);
			
			this.insertCertificate(certs);
			this.insertDevices(devices);
		}
		
		else{
			try{
        		deviceCert = deviceCertManagement.retrieveCert(devices.get(0).getCertId());
			} catch (SQLException ex){}
			
			if (deviceCert == null){
				return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Invalid device and/or certificate."))).build();
			}
			 
			// check connect policy  
			String policyJson = deviceOperations.getPolicyInfo(Constants.connectPolicy);
			devices = deviceOperations.getAllowedDevicesFromConnectPolicy(policyJson, devices);
			
			if (devices.size() == 0){
				return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Cannot connect device. Policy restrictions in place."))).build();
			}
			
			deviceOperations.connect(deviceCert, devices, true);
		}
		
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Success, "Successfully connected device."))).build();
	}
	
	@GET
	@Path("/disconnectdevice")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response disconnectDevice(@QueryParam("deviceId") final String deviceId){
		if (deviceId == null || deviceId.length() == 0 || !deviceOperations.getMqttClientMap().containsKey(deviceId)){
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Invalid DeviceId."))).build();
		}
		
		if (!deviceOperations.disconnectDevice(deviceId)){
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Device: " + deviceId + " disconnection unsuccessful."))).build();
		}
		
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Success, "Device: " + deviceId + " disconnection successful."))).build();
	}

	@GET
	@Path("/publishtotopic")
	@Produces(MediaType.APPLICATION_JSON)

	public Response publishTopic(@QueryParam("deviceId") final String deviceId, @QueryParam("topic") final String topic, @QueryParam("message") String message){
		if(!deviceOperations.getMqttClientMap().containsKey(deviceId)){
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, 
					"Device not connected. Cannot publish message to topic. Connect device to publish messages to topic.")))
					.build();
		}
		
		boolean status = deviceOperations.publishTopic(deviceId, topic, message);
		if (status){
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Success, "Published message to topic."))).build();
		}
		
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Could not publish message to topic."))).build();
	}
	
	@GET
	@Path("/subscribetotopic")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response subscribeTopic(@QueryParam("deviceId") final String deviceId, @QueryParam("topic") String topic){
		if(!deviceOperations.getMqttClientMap().containsKey(deviceId)){
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, 
					"Device not connected. Cannot subscribe to topic. Connect device to subscribe to topics.")))
					.build();			
		}
		
		boolean subscribeStatus = deviceOperations.subscribeTopic(deviceId, topic);
		if (subscribeStatus){
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Success, "Subscribe to topic."))).build();
		}
		
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Could not subscribe to topic."))).build();
	}

	@GET
	@Path("/receivemessage")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response receiveMessage(@QueryParam("deviceId") final String deviceId){
		return Response.status(200).entity(gson.toJson(deviceOperations.GetMessages(deviceId))).build();
	}
	
	private void insertCertificate(List<AwsIoTDeviceCertificate> iotDeviceCerts){
		try{
			deviceCertManagement.insertCertificates(iotDeviceCerts);
		}
		catch(SQLException ex){
			ex.printStackTrace();
		}
	}
	
	private void insertDevices(List<Device> devices){
		for (Iterator<Device> iterator = devices.iterator(); iterator.hasNext();){
			Device device = (Device)iterator.next();
			if (!deviceOperations.getMqttClientMap().containsKey(device.getDeviceId())){	
				System.out.println("Device: " + device.getDeviceId() + " not connected.");
				iterator.remove();
			}
		}
		
		try{
			deviceCertManagement.insertDevices(devices);
		} catch (SQLException ex){
			ex.printStackTrace();
		}
	}	
}