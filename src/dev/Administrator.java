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

@Path("/admin")
public class Administrator {
	@Inject AWSIoTDeviceOperations deviceOperations;
	@Inject DeviceCertificateManagement deviceCertManagement;
	@Inject Gson gson;
	
	@GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test(){
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse("Success", "Test service works."))).build();
	}
		
	@GET
	@Path("/getcertificateid")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCertificate(@QueryParam("deviceId") final String deviceId){
		List<Device> devices = null;
		try{
			devices = deviceCertManagement.getDevices(deviceId);
		} catch(SQLException ex){}
		
		
		if (devices == null)
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Invalid Device Id."))).build();
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Success, "CertificateId: " + devices.get(0).getCertId()))).build();
	}
	
	@GET
	@Path("/disconnectall")
	@Produces(MediaType.APPLICATION_JSON)
	public Response disconnectAllDevices(){
		List<String> devices = deviceOperations.disconnectAllConnectedDevices();		
		return Response.status(200).entity(gson.toJson(devices)).build();
	}
			
	@GET
	@Path("/getconnecteddevices")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response getConnectedDevices(){
		List<String> devices = null;
		try{
			devices = deviceCertManagement.getAllDevices();
		} catch (SQLException ex){}
		
		for (Iterator<String> iterator = devices.iterator(); iterator.hasNext();){
			String device = (String)iterator.next();
			if (!deviceOperations.getMqttClientMap().containsKey(device)){	
				iterator.remove();
			}
		}
		
		return Response.status(200).entity(gson.toJson(gson.toJson(devices))).build();
	}
		
	@GET
	@Path("/getconnecteddevicesforcertificate")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response getConnectedDevices(@QueryParam("certificateId") final String certificateId){
		List<String> devices = null;
		try{
			devices = deviceCertManagement.getDevicesAssociatedWithCert(certificateId);
		} catch (SQLException ex){}
		
		for (Iterator<String> iterator = devices.iterator(); iterator.hasNext();){
			String device = (String)iterator.next();
			if (!deviceOperations.getMqttClientMap().containsKey(device)){	
				iterator.remove();
			}
		}
		
		return Response.status(200).entity(gson.toJson(gson.toJson(devices))).build();
	}
		
	@GET
	@Path("/getdisconnecteddevices")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response getDisconnectedDevices(){
		List<String> devices = null;
		try{
			devices = deviceCertManagement.getAllDevices();
		} catch (SQLException ex){}
		
		for (Iterator<String> iterator = devices.iterator(); iterator.hasNext();){
			String device = (String)iterator.next();
			if (deviceOperations.getMqttClientMap().containsKey(device)){	
				iterator.remove();
			}
		}
		
		return Response.status(200).entity(gson.toJson(gson.toJson(devices))).build();
	}
	
	@GET
	@Path("/getdisconnecteddevicesforcertificate")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response getDisconnectedDevices(@QueryParam("certificateId") final  String certificateId){
		List<String> devices = null;
		try{
			devices = deviceCertManagement.getDevicesAssociatedWithCert(certificateId);
		} catch (SQLException ex){}
		
		for (Iterator<String> iterator = devices.iterator(); iterator.hasNext();){
			String device = (String)iterator.next();
			if (deviceOperations.getMqttClientMap().containsKey(device)){	
				iterator.remove();
			}
		}
		
		return Response.status(200).entity(gson.toJson(gson.toJson(devices))).build();
	}
		
	@GET
	@Path("/deactivatecertificate")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response deactivateCertificate(@QueryParam("certificateId") final  String certificateId){
		AwsIoTDeviceCertificate deviceCertificate = null;
		try{
			deviceCertificate = deviceCertManagement.retrieveCert(certificateId);
		} catch (SQLException ex){
			ex.printStackTrace();
		}
		
		if (deviceCertificate == null || deviceCertificate.getStatus().equalsIgnoreCase("inactive")){
			System.out.println("Invalid operation.");
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Invalid certificateId."))).build();
		}
						
		// detach policies from existing certificate
		deviceOperations.detachAllPoliciesFromCertificate(deviceCertificate);
		
		// get devices associated with certificate
		List<String> deviceIds = deviceOperations.getDevicesAssociatedWithCertificate(deviceCertificate);
		
		// detach things from existing certificate
		deviceOperations.detachAllThingsFromCertificate(deviceCertificate, deviceIds);		
		
		// Deactivate certificate with certificateId.
		deviceOperations.updateCertificate(deviceCertificate);
		
		// generate new certificate.
		List<AwsIoTDeviceCertificate> iotDeviceCerts = new ArrayList<AwsIoTDeviceCertificate>();
		iotDeviceCerts.add(deviceOperations.getAwsIoTDeviceCertificate());
		
		// attach policies to new certificate.
		deviceOperations.attachDefaultPolicyToCertificate(iotDeviceCerts.get(0), Constants.connectPolicy);
		deviceOperations.attachDefaultPolicyToCertificate(iotDeviceCerts.get(0), Constants.publishPolicy);
		deviceOperations.attachDefaultPolicyToCertificate(iotDeviceCerts.get(0), Constants.subscribePolicy);
		deviceOperations.attachDefaultPolicyToCertificate(iotDeviceCerts.get(0), Constants.receivePolicy);
		
		// attach things to new certificate.
		deviceOperations.attachThingsToCertificate(iotDeviceCerts.get(0), deviceIds);
		
		// Insert new certificate into DB.
		this.insertCertificate(iotDeviceCerts);
		
		// update old certificate status.
		this.updateCertificate(deviceCertificate, "inactive");
		
		// Update devices connected to AWS using new certificate.
		this.updateDeviceCertificate(deviceCertificate, iotDeviceCerts.get(0));
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Success, "Certificate " 
				+ certificateId + " successfully deactivated." 
				+ "New certificate generated with Certificate id: " 
				+ iotDeviceCerts.get(0).getCertId()))).build();
	}
		
	
	@GET
	@Path("/deletedevice")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response deleteDevice(@QueryParam("deviceId") final String deviceId){
		if (deviceOperations.getMqttClientMap().containsKey(deviceId) && !deviceOperations.disconnectDevice(deviceId)){
			System.out.println("Error disconnecting device. Cannot delete.");
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Device: " 
					+ deviceId + " disconnection unsuccessful. Cannot delete."))).build();
		}
		
		System.out.println("Proceeding to delete device");
		List<Device> devices = null;
		AwsIoTDeviceCertificate deviceCert = null;
		
		// Retrieve device
		try{
	        devices = deviceCertManagement.getDevices(deviceId);
	        deviceCert = deviceCertManagement.retrieveCert(devices.get(0).getCertId());
		} catch (SQLException ex){
			ex.printStackTrace();
		}
		
		if (devices == null || deviceCert == null || !deviceOperations.describeThing(deviceId))
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Invalid Device and/or Certificate."))).build();
		
		deviceOperations.detachThingFromCertificate(deviceCert, devices.get(0));
		
		deviceOperations.deleteDevice(devices.get(0).getDeviceId());
		
		System.out.println("Device: " + deviceId + " successfully deleted.");
		
		try{
			deviceCertManagement.deleteDevices(devices);
		}catch (SQLException ex){
			ex.printStackTrace();
		}
		
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Success, "Device: " 
				+ deviceId + " successfully deleted."))).build();
	}
	
	@GET
	@Path("/allowconnectpolicyfordevice")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response allowConnectPolicyDevice(@QueryParam("deviceId") final String deviceId){
		List<Device> devices = null;
		try{
			devices = deviceCertManagement.getDevices(deviceId);
		}catch (SQLException ex){}
		
		if (devices.size() == 0 || devices.size() > 1){
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Invalid DeviceId. Can't update Connect Policy for device."))).build();
		}
		
		List<String> res = new ArrayList<String>();
		res.add(deviceId);
		deviceOperations.updatePolicy(res, Constants.connectPolicy, Constants.connectAction, true);
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Success, "Successfully updated Connect Policy for device."))).build();
	}
	
	@GET
	@Path("/denyconnectpolicyfordevice")
	@Produces(MediaType.APPLICATION_JSON)
	
	public Response denyConnectPolicyDevice(@QueryParam("deviceId") final String deviceId){
		List<Device> devices = null;
		try{
			devices = deviceCertManagement.getDevices(deviceId);
				
		}catch (SQLException ex){
			ex.printStackTrace();
		}
		
		if (devices.size() == 0 || devices.size() > 1){
			return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Failure, "Invalid DeviceId. Can't update Connect Policy for device."))).build();
		}
		
		List<String> res = new ArrayList<String>();
		res.add(deviceId);
		deviceOperations.updatePolicy(res, Constants.connectPolicy, Constants.connectAction, false);
		return Response.status(200).entity(gson.toJson(new ServiceCallResponse(Constants.Success, "Successfully updated Connect Policy for device."))).build();
	}
		
	private void insertCertificate(List<AwsIoTDeviceCertificate> iotDeviceCerts){
		try{
			deviceCertManagement.insertCertificates(iotDeviceCerts);
		}
		catch(SQLException ex){
			ex.printStackTrace();
		}
	}
	
	private void updateCertificate(AwsIoTDeviceCertificate iotDeviceCert, String status){
		try{
			deviceCertManagement.updateCertificateStatus(iotDeviceCert.getCertId(), status);
		}
		catch(SQLException ex){
			ex.printStackTrace();
		}
	}
	
	private void updateDeviceCertificate(AwsIoTDeviceCertificate oIotDeviceCert, AwsIoTDeviceCertificate nIotDeviceCert){
		try{
			deviceCertManagement.updateDeviceCertificate(oIotDeviceCert.getCertId(), nIotDeviceCert.getCertId());
		}
		catch(SQLException ex){
			ex.printStackTrace();
		}
	}
}

class ServiceCallResponse{
	public String effect;
	public String message;
	public ServiceCallResponse(String effect, String message){
		this.effect = effect;
		this.message = message;
	}
}