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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import net.tuxed.gjokii.Gjokii;
import net.tuxed.gjokii.GjokiiException;

/**
 * This class deals with installing and listing certificates on Nokia phones. It
 * uses the Gjokii library for file handling.
 * 
 * @author F. Kooman <fkooman@tuxed.net>
 * 
 */
public class NokiCert {
	private Gjokii g;
	private PrintStream ps;
	private String certificateDirectoryFileLocation = "/predefhiddenfolder/certificates/auth/ext_info.sys";

	/**
	 * Construct the NokiCert object
	 * 
	 * @param g
	 *            the (open) Gjokii connection to the phone
	 * @param ps
	 *            the stream to write output to (can be System.out)
	 */
	public NokiCert(Gjokii g, PrintStream ps) {
		this.g = g;
		this.ps = ps;
	}

	/**
	 * Install a X.509 certificate on the phone.
	 * 
	 * @param certFilePathName
	 *            the full path name of the certificate file
	 * @param certUsage
	 *            the certificate usage bits for this certificate
	 * @throws GjokiiException
	 *             if an error occurs
	 */
	public void installCertificate(String certFilePathName, int certUsage)
			throws GjokiiException {
		File f = getCertificateListFile();
		File derFile = NokiCertUtils.convertToDER(new File(certFilePathName));
		g.getFile(certificateDirectoryFileLocation, f);
		CertListParser c = new CertListParser(f);
		String subjectCN;

		/* add the new certificate to the certificate directory file */
		try {
			CertParser x = new CertParser(derFile);
			subjectCN = x.getSubjectCommonName();
			byte[] certEntry = x.getCDFEntry(c.hasLittleEndianSizeBytes(),
					certUsage);
			FileOutputStream fos = new FileOutputStream(f, true);
			fos.write(certEntry);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			throw new GjokiiException("cannot find certificate directory file");
		} catch (IOException e) {
			throw new GjokiiException(
					"unable to write to certificate directory file");
		}
		/* upload the certificate */
		String certPath = "/predefhiddenfolder/certificates/auth/" + subjectCN;

		ps.println("(I) uploading certificate to the phone...");
		g.putFile(certPath, derFile);

		ps.println("(I) uploading CDF to the phone...");
		/* upload the new certificate directory file (CDF) */
		g.putFile(certificateDirectoryFileLocation, f);
	}

	/**
	 * Retrieve the certificate list file (CDF) from the phone.
	 * 
	 * @return file handle to certificate list file (CDF)
	 * @throws GjokiiException
	 */
	public File getCertificateListFile() throws GjokiiException {
		ps.println("(I) downloading CDF from the phone...");
		File f = null;
		/* get the current certificate directory file */
		try {
			f = File.createTempFile("CDF", null);
		} catch (IOException e) {
			throw new GjokiiException("unable to create temporary file");
		}
		ps.println("(I) using temporary file: " + f.getAbsolutePath());
		g.getFile(certificateDirectoryFileLocation, f);
		return f;
	}

	/**
	 * Retrieve a (formatted) list of installed certificates and their SHA-1
	 * hash.
	 * 
	 * @return the list
	 * @throws GjokiiException
	 */
	public String listCertificates() throws GjokiiException {
		File f = getCertificateListFile();
		CertListParser c = new CertListParser(f);
		return c.toString();
	}
}
