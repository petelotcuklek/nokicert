/*
 *  This file is part of NokiCert.
 *
 *  NokiCert is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NokiCert is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NokiCert.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.tuxed.nokicert;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.security.auth.x500.X500Principal;

import net.tuxed.gjokii.GjokiiException;
import net.tuxed.misc.Utils;

/**
 * This class parses a (DER encoded) X.509 certificate and has the ability to
 * construct a certificate list entry for use on Nokia phones.
 * 
 * @author F. Kooman <fkooman@tuxed.net>
 */
public class CertParser {
	private final static byte[] APPS_SIGNING_BYTES = { (byte) 0x06,
			(byte) 0x08, (byte) 0x2b, (byte) 0x06, (byte) 0x01, (byte) 0x05,
			(byte) 0x05, (byte) 0x07, (byte) 0x03, (byte) 0x03 };
	private final static byte[] CROSS_CERTIFICATION_BYTES = { (byte) 0x06,
			(byte) 0x0a, (byte) 0x2b, (byte) 0x06, (byte) 0x01, (byte) 0x04,
			(byte) 0x01, (byte) 0x5e, (byte) 0x01, (byte) 0x31, (byte) 0x04,
			(byte) 0x01 };
	private final static byte[] SERVER_AUTHENTIC_BYTES = { (byte) 0x06,
			(byte) 0x08, (byte) 0x2b, (byte) 0x06, (byte) 0x01, (byte) 0x05,
			(byte) 0x05, (byte) 0x07, (byte) 0x03, (byte) 0x01 };

	private X509Certificate cert;
	private String fileName;

	private String ScountryCode;
	private String Sorganization;
	private String SdistinguishedName;
	private String Slocality;
	private String ScommonName;
	private String Sstate;
	private String SorgUnit;
	private String IcountryCode;
	private String Iorganization;
	private String IcommonName;
	private String IdistinguishedName;

	/**
	 * Constructs the X.509 certificate object from byte array.
	 * 
	 * @param data
	 *            the byte array containing the DER encoded X.509 certificate
	 */
	public CertParser(byte[] data) throws GjokiiException {
		InputStream inStream = new ByteArrayInputStream(data);
		parseCert(inStream);
	}

	/**
	 * Constructs the X.509 certificate object from file.
	 * 
	 * @param f
	 *            the file containing the DER and PEM encoded X.509 certificate
	 */
	public CertParser(File f) throws GjokiiException {
		fileName = f.getName();
		InputStream inStream = null;
		try {
			inStream = new FileInputStream(f);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					inStream));
			String s = br.readLine();
			String base64cert = new String();
			if (s.matches("-----BEGIN CERTIFICATE-----")) {
				/* PEM formatted */
				do {
					s = br.readLine();
					base64cert += s;
				} while (!s.matches("-----END CERTIFICATE-----"));
				/* convert Base64 encoded string to binary */
				inStream = new ByteArrayInputStream(
						net.sourceforge.iharder.Base64.decode(base64cert));
			} else {
				/* assume DER formatted, restart InputStream */
				inStream = new FileInputStream(f);
			}
			parseCert(inStream);
		} catch (FileNotFoundException e) {
			throw new GjokiiException("unable to find certificate: "
					+ e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a certificate directory meta file entry for this certificate
	 * 
	 * @param littleEndian
	 *            The CDF uses little endian coding for the size of the entries
	 * @param certUsage
	 *            the usage flags for this certificate (APPS_SIGNING,
	 *            CROSS_CERTIFICATION, SERVER_AUTHENTIC). Use the OR operator
	 *            for specifying more than one.
	 * @return the CDF entry
	 * @throws GjokiiException
	 */
	public byte[] getCDFEntry(boolean littleEndian, int certUsage)
			throws GjokiiException {
		byte[] output;
		byte[] header = new byte[] { 0x01, 0x41, 0x02, 0x10 };
		byte[] fields = new byte[] { 0x14, 0x00, 0x14, 0x14 };
		output = Utils.appendToByteArray(header, fields);
		output = Utils.appendToByteArray(output, getFingerprint());
		output = Utils.appendToByteArray(output, getModulusHash());
		output = Utils.appendToByteArray(output, new byte[20]);
		output = Utils.appendToByteArray(output, getSubjectHash());
		output = Utils.appendToByteArray(output, getIssuerHash());
		output = Utils.appendToByteArray(output,
				new byte[] { (byte) ((byte) getFileName().length() + 1) });
		output = Utils.appendToByteArray(output, getFileName().getBytes());
		output = Utils.appendToByteArray(output, new byte[2]); /* separator */

		byte[] keyUsage = new byte[1]; /* first byte contains length */
		if ((certUsage & NokiCertUtils.APPS_SIGNING) == NokiCertUtils.APPS_SIGNING)
			keyUsage = Utils.appendToByteArray(keyUsage, APPS_SIGNING_BYTES);
		if ((certUsage & NokiCertUtils.CROSS_CERTIFICATION) == NokiCertUtils.CROSS_CERTIFICATION)
			keyUsage = Utils.appendToByteArray(keyUsage,
					CROSS_CERTIFICATION_BYTES);
		if ((certUsage & NokiCertUtils.SERVER_AUTHENTIC) == NokiCertUtils.SERVER_AUTHENTIC)
			keyUsage = Utils
					.appendToByteArray(keyUsage, SERVER_AUTHENTIC_BYTES);
		keyUsage[0] = (byte) (keyUsage.length - 1); /* set length */

		output = Utils.appendToByteArray(output, keyUsage);

		/* now make the total length a divisor of 4 */
		int padding = 4 - (output.length % 4);

		/* for some reason we need extra space in some situations?! */
		if (padding != 4)
			padding += 4;

		output = Utils.appendToByteArray(output, new byte[padding]); /* padding */

		/* prepend with total length, which is 4 bytes, we add that as well */
		short outputLength = (short) (output.length + 4);
		byte[] sizeBytes = null;

		if (littleEndian) {
			// if (Config.hasLittleEndianCDFSize(platform)) {
			/* the two? size bytes are little endian */
			sizeBytes = Utils.shortToByteArrayLE(outputLength);
		} else {
			/* the two? size bytes are big endian */
			sizeBytes = Utils.shortToByteArray(outputLength);
		}
		sizeBytes = Utils.appendToByteArray(sizeBytes, new byte[2]);
		output = Utils.appendToByteArray(sizeBytes, output);
		return output;
	}

	/**
	 * Get the file name of the certificate
	 * 
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Get the SHA1 hash of the certificate
	 * 
	 * @return the SHA1 hash of the certificate
	 */
	public byte[] getFingerprint() {
		byte[] data = null;
		MessageDigest hash = null;
		try {
			data = cert.getEncoded();
			hash = MessageDigest.getInstance("SHA1");
		} catch (CertificateEncodingException e) {
		} catch (NoSuchAlgorithmException e) {
		}
		hash.update(data);
		return hash.digest();
	}

	public String getIssuerCommonName() {
		return IcommonName;
	}

	/**
	 * Get the country code of the issuer
	 * 
	 * @return the country code of the issuer
	 */
	public String getIssuerCountryCode() {
		return IcountryCode;
	}

	/**
	 * Get the distinguished name of the issuer
	 * 
	 * @return the distinguished name of the issuer
	 */
	public String getIssuerDN() {
		return IdistinguishedName;
	}

	/**
	 * Get the SHA1 hash of the certificate issuer
	 * 
	 * @return the SHA1 hash of the certificate issuer
	 */
	public byte[] getIssuerHash() {
		byte[] data = null;
		MessageDigest hash = null;
		try {
			data = cert.getIssuerX500Principal().getEncoded();
			hash = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
		}
		hash.update(data);
		return hash.digest();
	}

	/**
	 * Get the organization of the issuer
	 * 
	 * @return the organization of the issuer
	 */
	public String getIssuerOrganization() {
		return Iorganization;
	}

	/**
	 * Get the SHA1 hash of the certificate modulus
	 * 
	 * @return the SHA1 hash
	 */
	public byte[] getModulusHash() {
		RSAPublicKey k = (RSAPublicKey) cert.getPublicKey();
		BigInteger m = k.getModulus();
		MessageDigest hash = null;
		try {
			hash = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
		}
		/*
		 * the modulus always seems to start with a 0x00 which we don't want
		 * when computing the hash.
		 */
		hash.update(m.toByteArray(), 1, m.toByteArray().length - 1);
		return hash.digest();
	}

	/**
	 * Returns the size of the public modulus in bits
	 * 
	 * @return the bit size of the public modulus
	 */
	public int getModulusSize() {
		RSAPublicKey k = (RSAPublicKey) cert.getPublicKey();
		BigInteger m = k.getModulus();
		return m.bitLength();
	}

	/**
	 * Nokia way of encoding the distinguished name of the subject required for
	 * the JMR entries.
	 * 
	 * It consists of only the fields C,ST,L,O,OU,CN (in that order) and only
	 * these. If a field does not exist in a certificate it is omitted. This can
	 * result in an empty DN.
	 * 
	 * @return the Nokia way of encoding a distinguished name of the subject
	 */
	public String getNokiaSubjectDN() {
		String output = "";
		if (ScountryCode != null)
			output += "C=" + ScountryCode + ";";
		if (Sstate != null)
			output += "ST=" + Sstate + ";";
		if (Slocality != null)
			output += "L=" + Slocality + ";";
		if (Sorganization != null)
			output += "O=" + Sorganization + ";";
		if (SorgUnit != null)
			output += "OU=" + SorgUnit + ";";
		if (ScommonName != null)
			output += "CN=" + ScommonName;

		/*
		 * if not all fields exist it is possible that the output ends with a
		 * semicolon, get rid of it here
		 */
		if (output.length() != 0 && output.endsWith(";"))
			output.substring(0, output.length() - 1);
		return output;
	}

	/**
	 * Get the common name (CN) of the subject
	 * 
	 * @return the common name
	 */
	public String getSubjectCommonName() {
		return ScommonName;
	}

	/**
	 * Get the country code of the subject
	 * 
	 * @return the country code of the subject
	 */
	public String getSubjectCountryCode() {
		return ScountryCode;
	}

	/**
	 * Get the distinguished name of the subject
	 * 
	 * @return the distinguished name of the subject
	 */
	public String getSubjectDN() {
		return SdistinguishedName;
	}

	/**
	 * Get the SHA1 hash of the certificate subject
	 * 
	 * @return the SHA1 hash of the certificate subject
	 */
	public byte[] getSubjectHash() {
		byte[] data = null;
		MessageDigest hash = null;
		try {
			data = cert.getSubjectX500Principal().getEncoded();
			hash = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
		}
		hash.update(data);
		return hash.digest();
	}

	/**
	 * Get the organization of the subject
	 * 
	 * @return the organization of the subject
	 */
	public String getSubjectOrganization() {
		return Sorganization;
	}

	/**
	 * Parse the certificate contained by the InputStream
	 * 
	 * @param is
	 *            the certificate InputStream
	 * @throws CertificateException
	 *             unable to parse the certificate stream
	 * @throws IOException
	 *             unable to close the stream
	 */
	private void parseCert(InputStream is) throws GjokiiException {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) cf.generateCertificate(is);
			is.close();
		} catch (CertificateException e) {
			throw new GjokiiException("unable to parse certificate: "
					+ e.getMessage());
		} catch (IOException e) {
			throw new GjokiiException("unable to close file stream: "
					+ e.getMessage());
		}
		X500Principal Sxp = cert.getSubjectX500Principal();
		SdistinguishedName = Sxp.getName("RFC1779");
		String[] Sdns = SdistinguishedName.split(",");
		for (int i = 0; i < Sdns.length; i++) {
			String s = Sdns[i].trim();
			if (s.startsWith("C=")) {
				ScountryCode = s.substring(2);
			} else if (s.startsWith("O=")) {
				Sorganization = s.substring(2);
			} else if (s.startsWith("L=")) {
				Slocality = s.substring(2);
			} else if (s.startsWith("ST=")) {
				Sstate = s.substring(3);
			} else if (s.startsWith("OU=")) {
				SorgUnit = s.substring(3);
			} else if (s.startsWith("CN=")) {
				ScommonName = s.substring(3);
			}
		}

		X500Principal Ixp = cert.getIssuerX500Principal();
		IdistinguishedName = Ixp.getName();
		String[] Idns = IdistinguishedName.split(",");
		for (int i = 0; i < Idns.length; i++) {
			String s = Idns[i].trim();
			if (s.startsWith("C=")) {
				IcountryCode = s.substring(2);
			} else if (s.startsWith("O=")) {
				Iorganization = s.substring(2);
			} else if (s.startsWith("CN=")) {
				IcommonName = s.substring(3);
			}
		}
	}

	public String toString() {
		String output = "";
		output += "Issuer: " + getIssuerCommonName() + "\n";
		output += "Subject: " + getSubjectCommonName() + "\n";
		output += "Fingerprint: " + Utils.byteArrayToString(getFingerprint())
				+ "\n";
		return output;
	}
}