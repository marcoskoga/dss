/**
 * DSS - Digital Signature Services
 * Copyright (C) 2015 European Commission, provided under the CEF programme
 *
 * This file is part of the "DSS - Digital Signature Services" project.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package eu.europa.esig.dss.cookbook.sources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;
import eu.europa.esig.dss.utils.Utils;

public class JavaKeyStoreTool {

	private static final Logger logger = LoggerFactory.getLogger(JavaKeyStoreTool.class);

	protected KeyStore ks = null;

	public JavaKeyStoreTool(final String ksUrlLocation, final String ksPassword) {
		InputStream ksStream = null;
		try {
			final URL ksLocation = new URL(ksUrlLocation);
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ksStream = ksLocation.openStream();
			ks.load(ksStream, (ksPassword == null) ? null : ksPassword.toCharArray());
		} catch (GeneralSecurityException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			Utils.closeQuietly(ksStream);
		}
	}

	public X509Certificate getCertificate(String certAlias, String password) {

		try {
			Certificate cert = ks.getCertificate(certAlias);
			if (cert == null) {
				return null;
			}
			if (!(cert instanceof X509Certificate)) {
				return null;
			}
			return (X509Certificate) cert;
		} catch (KeyStoreException e) {
			throw new DSSException(e);
		}
	}

	public KSPrivateKeyEntry getPrivateKey(String certAlias, String password) {

		try {

			final Key key = ks.getKey(certAlias, password.toCharArray());
			if (key == null) {
				return null;
			}
			if (!(key instanceof PrivateKey)) {
				return null;
			}
			final Certificate[] certificateChain = ks.getCertificateChain(certAlias);
			KeyStore.PrivateKeyEntry privateKey = new KeyStore.PrivateKeyEntry((PrivateKey) key, certificateChain);
			KSPrivateKeyEntry ksPrivateKey = new KSPrivateKeyEntry(certAlias, privateKey);
			return ksPrivateKey;
		} catch (GeneralSecurityException e) {
			throw new DSSException(e);
		}
	}
}
