package dev;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class X509CertificateGenerator {
	
	public X509CertificateGenerator() {
	}
	
	public static Certificate generateCertificate(String pemCertificate)
	{
		if (pemCertificate == null || pemCertificate.length() == 0)
			throw new IllegalArgumentException("Empty stream. Cannot generate certificate.");
		
		Certificate certificate = null;
		
		try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(pemCertificate.getBytes()))){
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            certificate = certFactory.generateCertificate(bis);
		} catch (IOException | CertificateException ex) {
			ex.printStackTrace();
		}                     
		
		return certificate;          
	}                  
}