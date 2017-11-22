package dev;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

public class PolicyManagement {
    private final String pVersion = "2012-10-17";
	private final String clientArn = "arn:aws:iot:us-west-2:765789663805:";
	private final String[] actionEffect;
	private final String[] actions;
	private Gson gson; // to serialize json. 
	
	public PolicyManagement(){
		gson = new Gson();
		actionEffect = new String[]{"Allow", "Deny"};
		actions = new String[]{"iot:Connect", "iot:Publish", "iot:Subscribe", "iot:Receive"};
	}
	
	public String generateDefaultPolicy(String action){
		List<String> resources = new ArrayList<String>();
		resources.add("*");
		PolicyStatement ps1 = new PolicyStatement(actionEffect[0], action, resources);
		List<PolicyStatement> policies = new ArrayList<PolicyStatement>();
		policies.add(ps1);
		Policy policy = new Policy(pVersion, policies);
		return gson.toJson(policy);
	}
	
	  public String allowPolicy(String policyJson, String action, List<String> resources){
		Policy policy = gson.fromJson(policyJson, Policy.class);
		List<PolicyStatement> ps = policy.getStatement();
		PolicyStatement p = null;
		String spl = "";
		switch(action){
			case "iot:Connect":
				spl = "client/";
				break;
			case "iot:Publish":
				spl = "topic/";
				break;
			case "iot:Subscribe":
			case "iot:Receive":
				spl = "topicfilter/";
				break;
		}
		
		if (ps.size() == 2){
			if (ps.get(0).getEffect().equals(actionEffect[1]))
				p = ps.get(0);
			else 
				p = ps.get(1);
		
			Set<String> set = p.getResource();			
			for (String r : resources){
				String nRes = clientArn+spl+r;
				if (set.contains(nRes))
					set.remove(nRes);
			}
			
			if (set.size() > 0)
				p.setResource(set);
			else
				ps.remove(p);
		}
		
		return gson.toJson(policy);
	}
    
    public String denyPolicy(String policyJson, String action, List<String> resources){
		Policy policy = gson.fromJson(policyJson, Policy.class);
		List<PolicyStatement> ps = policy.getStatement();
		
		String spl = "";
		switch(action){
			case "iot:Connect":
				spl = "client/";
				break;
			case "iot:Publish":
				spl = "topic/";
				break;
			case "iot:Subscribe":
			case "iot:Receive":
				spl = "topicfilter/";
				break;
		}
			
		List<String> nres = new ArrayList<String>();
		for (String resource : resources){
			nres.add(clientArn + spl + resource);
		}
		
		if (ps.size() == 1){
			PolicyStatement ps1 = new PolicyStatement(actionEffect[1], action, nres);
			ps.add(ps1);
		}
		else{
			PolicyStatement p = null;
			if (ps.get(0).getEffect().equals(actionEffect[1]))
				p = ps.get(0);
			else 
				p = ps.get(1);
			
			p.getResource().addAll(nres);		
		}
		
		return gson.toJson(policy);
	}
    
    public List<Device> getResources(String policyJson, String action, List<Device> devices){
    	Policy policy = gson.fromJson(policyJson, Policy.class);
		List<PolicyStatement> ps = policy.getStatement();
		PolicyStatement p = null;
		String spl =  "client/"; 
		
		if (ps.size() == 2){
			if (ps.get(0).getEffect().equals(actionEffect[1]))
				p = ps.get(0);
			else 
				p = ps.get(1);
		
			Set<String> set = p.getResource();		
			for (Iterator<Device> iterator = devices.iterator(); iterator.hasNext();){
				Device device = (Device)iterator.next();
				if (set.contains(clientArn+spl+device.getDeviceId())){
					//System.out.println("Device: " + device.getDeviceId() + " not connected.");
					iterator.remove();
				}
			}
		}
		
		return devices;
    }
}

class Policy{
	private String Version;
	private List<PolicyStatement> Statement;
	
	public Policy(String version, List<PolicyStatement> policies){		
		this.Version = version;
		this.Statement = policies;
	}

	public String getVersion() {
		return Version;
	}

	public List<PolicyStatement> getStatement() {
		return Statement;
	}
}

class PolicyStatement{
	private String Effect;
	private List<String> Action;
	private Set<String> Resource;
	
	public PolicyStatement(String effect, String action, List<String> resources){
		this.Effect = effect;
		Action = new ArrayList<String>();
		Action.add(action);
		Resource = new HashSet<String>();
		Resource.addAll(resources);
	}

	public Set<String> getResource() {
		return Resource;
	}
	
	public void setResource(Set<String> resources) {
		Resource = resources;
	}

	public String getEffect() {
		return Effect;
	}
	
	public List<String> getAction() {
		return Action;
	}
}
