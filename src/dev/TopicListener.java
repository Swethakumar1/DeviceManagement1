package dev;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

public class TopicListener extends AWSIotTopic {
	private ConcurrentHashMap<String, Set<MessageTimestamp>> topicMessageMap;
	public TopicListener(String topic, ConcurrentHashMap<String, Set<MessageTimestamp>> topicMessageMap){
		super(topic, AWSIotQos.QOS0);
		this.topicMessageMap = topicMessageMap;
	}
	
	@Override
	public void onMessage(AWSIotMessage iotMessage){
		System.out.println("Received message:  " + iotMessage.getStringPayload());
		this.topicMessageMap.computeIfAbsent(iotMessage.getTopic(), fn -> Collections.synchronizedSet(new HashSet<MessageTimestamp>()))
						.add(new MessageTimestamp(iotMessage.getStringPayload()));	
	}
	
	@Override
	public void onSuccess(){
		System.out.println("Successfully subscribed to topic.");
	}
}
     