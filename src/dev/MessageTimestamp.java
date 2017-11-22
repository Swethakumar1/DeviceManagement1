package dev;

import java.util.Date;

public class MessageTimestamp {
	private String message;
	private long receivedTime;
	
	public MessageTimestamp(String message){
		this.message = message;
		this.receivedTime = new Date().getTime();
	}

	public String getMessage() {
		return message;
	}

	public long getReceivedTime() {
		return receivedTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageTimestamp other = (MessageTimestamp) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
 }
