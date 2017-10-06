package eu.europa.esig.dss.signature.policy.validation;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.bouncycastle.cms.CMSException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.InMemoryDocument;
import eu.europa.esig.dss.TokenIdentifier;
import eu.europa.esig.dss.cades.validation.CAdESSignature;
import eu.europa.esig.dss.client.http.NativeHTTPDataLoader;
import eu.europa.esig.dss.pades.validation.PAdESSignature;
import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignaturePolicyProvider;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.TimestampToken;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.RevocationToken;
import eu.europa.esig.dss.x509.SignaturePolicy;
import eu.europa.esig.dss.x509.crl.CRLToken;

/**
 * @author davyd.santos
 *
 */
public class FullCAdESSignaturePolicyValidatorTest {
	
	@Test
	public void shouldNotValidateWhenNoPolicyAvailable() {
		FullCAdESSignaturePolicyValidator cadesValidator = new FullCAdESSignaturePolicyValidator();
		CAdESSignature signature = Mockito.mock(CAdESSignature.class);
		SignaturePolicy signaturePolicy = new SignaturePolicy();
		signaturePolicy.setPolicyContent(new InMemoryDocument(new byte[0]));
		Mockito.doReturn(signaturePolicy).when(signature).getPolicyId();
		cadesValidator.setSignature(signature);
		
		cadesValidator.validate();
		Assert.assertEquals("The errors found on signature policy validation are: at general: Unexpected error: No content found under signature policy", cadesValidator.getProcessingErrors());
	}
	
	@Test
	public void shouldValidateWhenPadesPbadAdrb() throws IOException, CMSException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {
		SignaturePolicyProvider signaturePolicyProvider = new SignaturePolicyProvider();
		signaturePolicyProvider.setDataLoader(new NativeHTTPDataLoader());
		
		SignedDocumentValidator validator = SignedDocumentValidator
				.fromDocument(new FileDocument(new File("src/test/resources/AD-RB.pdf")));

		validator.setCertificateVerifier(new CommonCertificateVerifier());
		List<AdvancedSignature> signatures = validator.getSignatures();
		PAdESSignature sig = (PAdESSignature) signatures.get(0);
		sig.checkSignaturePolicy(signaturePolicyProvider);
		
		CertificateTestUtils.loadIssuers(sig.getSigningCertificateToken(), sig.getCertPool());
		mockRevocation(sig.getSigningCertificateToken());
		FullCAdESSignaturePolicyValidator cadesValidator = new FullCAdESSignaturePolicyValidator(sig);
		cadesValidator.validate();
		Assert.assertTrue("FullCAdESSignaturePolicyValidator errors: " + cadesValidator.getProcessingErrors(), cadesValidator.getProcessingErrors().isEmpty());
	}
	
	@Test
	public void shouldValidateWhenPadesPbadAdrt() throws IOException, CMSException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {
		SignaturePolicyProvider signaturePolicyProvider = new SignaturePolicyProvider();
		signaturePolicyProvider.setDataLoader(new NativeHTTPDataLoader());
		
		SignedDocumentValidator validator = SignedDocumentValidator
				.fromDocument(new FileDocument(new File("src/test/resources/AD-RT-bry-ts-oid-uri.pdf")));

		validator.setCertificateVerifier(new CommonCertificateVerifier());
		List<AdvancedSignature> signatures = validator.getSignatures();
		PAdESSignature sig = (PAdESSignature) signatures.get(0);
		sig.checkSignaturePolicy(signaturePolicyProvider);
		
		CertificateTestUtils.loadIssuers(sig.getSigningCertificateToken(), sig.getCertPool());
		mockRevocation(sig.getSigningCertificateToken());
		for (TimestampToken ttk : sig.getSignatureTimestamps()) {
			CertificateTestUtils.loadIssuers(ttk.getIssuerToken(), sig.getCertPool());
			mockRevocation(ttk.getIssuerToken());
		}
		FullCAdESSignaturePolicyValidator cadesValidator = new FullCAdESSignaturePolicyValidator(sig);
		cadesValidator.validate();
		Assert.assertTrue("FullCAdESSignaturePolicyValidator errors: " + cadesValidator.getProcessingErrors(), cadesValidator.getProcessingErrors().isEmpty());
	}

	private void mockRevocation(CertificateToken certificateToken) {
		RevocationToken revTk = Mockito.mock(CRLToken.class);
		Mockito.doReturn("fake_contants".getBytes()).when(revTk).getEncoded();
		Mockito.doReturn(new TokenIdentifier(revTk)).when(revTk).getDSSId();
		Mockito.doReturn(true).when(revTk).getStatus();
		Mockito.doReturn(true).when(revTk).isValid();
		while(certificateToken != null) {
			certificateToken.addRevocationToken(revTk);
			certificateToken = certificateToken.getIssuerToken();
		}
	}
}
