package eu.europa.esig.dss.signature.policy.asn1;

import java.io.IOException;
import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;

import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.signature.policy.PBADPdfEntry;

public class ASN1PBADPdfEntry extends ASN1Object implements PBADPdfEntry {
	private String name;
	private byte[] value;
	
	public ASN1PBADPdfEntry(ASN1Sequence as) {
		name = DERUTF8String.getInstance(as.getObjectAt(0)).getString();
		if (as.size() > 1) {
			value = readValue(as.getObjectAt(1));
		}
	}
	
	public ASN1PBADPdfEntry(String name) {
		if (name == null || name.trim().equals("")) {
			throw new IllegalArgumentException("Empty name is not valid");
		}
		this.name = name;
	}
	
	public ASN1PBADPdfEntry(String name, byte[] value) {
		this(name);
		this.value = value;
	}
	
	public ASN1PBADPdfEntry(String name, String value) throws IOException {
		this(name, value == null? null: value.getBytes());
	}

	/* (non-Javadoc)
	 * @see eu.europa.dss.signature.policy.asn1.PBADPdfEntry#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see eu.europa.dss.signature.policy.asn1.PBADPdfEntry#getValue()
	 */
	@Override
	public byte[] getValue() {
		return value;
	}
	
	@Override
	public ASN1Primitive toASN1Primitive() {
		try {
			return ASN1Utils.createASN1Sequence(new DERUTF8String(name), writeValue());
		} catch (IOException e) {
			throw new DSSException("Error parsing PBADPdfEntry");
		}
	}

	private DEROctetString writeValue() throws IOException {
		if (value == null) {
			return null;
		}
		
		switch (name) {
		case "ValidationValues": 
			return new DEROctetString(new ASN1Enumerated(new BigInteger(value)));
		case "Type":
		case "Filter":
		case "SubFilter":
			return new DEROctetString(DERUTF8String.getInstance(value));
		default:
			return new DEROctetString(value);
		}
	}

	private byte[] readValue(ASN1Encodable asn1Encodable) {
		if (asn1Encodable == null) {
			return null;
		}
		

		byte[] octets = ASN1OctetString.getInstance(asn1Encodable).getOctets();
		switch (name) {
		case "ValidationValues": 
			return ASN1Enumerated.getInstance(value).getValue().toByteArray();
		case "Type":
		case "Filter":
		case "SubFilter":
			return DERUTF8String.getInstance(octets).getString().getBytes();
		default:
			return octets;
		}
	}
}