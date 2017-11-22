package dev;

public class AwsIoTDeviceCertificate {
	private String certId;
	private String certArn;
	private String certPem;
	private String privateKey;
	private String publicKey;
	private String status;
	
	public AwsIoTDeviceCertificate(String certId, String certArn, String certPem, String publicKey, String privateKey, String status){
		this.certId = certId;
		this.certArn = certArn;
		this.certPem = certPem;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.status = status;
	}

	public String getCertId() {
		return certId;
	}

	public void setCertId(String certId) {
		this.certId = certId;
	}

	public String getCertArn() {
		return certArn;
	}

	public void setCertArn(String certArn) {
		this.certArn = certArn;
	}

	public String getCertPem() {
		return certPem;
	}

	public void setCertPem(String certPem) {
		this.certPem = certPem;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((certArn == null) ? 0 : certArn.hashCode());
		result = prime * result + ((certId == null) ? 0 : certId.hashCode());
		result = prime * result + ((certPem == null) ? 0 : certPem.hashCode());
		result = prime * result + ((privateKey == null) ? 0 : privateKey.hashCode());
		result = prime * result + ((publicKey == null) ? 0 : publicKey.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		AwsIoTDeviceCertificate other = (AwsIoTDeviceCertificate) obj;
		if (certArn == null) {
			if (other.certArn != null)
				return false;
		} else if (!certArn.equals(other.certArn))
			return false;
		if (certId == null) {
			if (other.certId != null)
				return false;
		} else if (!certId.equals(other.certId))
			return false;
		if (certPem == null) {
			if (other.certPem != null)
				return false;
		} else if (!certPem.equals(other.certPem))
			return false;
		if (privateKey == null) {
			if (other.privateKey != null)
				return false;
		} else if (!privateKey.equals(other.privateKey))
			return false;
		if (publicKey == null) {
			if (other.publicKey != null)
				return false;
		} else if (!publicKey.equals(other.publicKey))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}
}