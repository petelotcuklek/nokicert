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
package net.tuxed.nokiroot;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.tuxed.gjokii.Gjokii;
import net.tuxed.gjokii.GjokiiException;

/**
 * This class deals with modifying and listing the MIDlet suites and their
 * security domains on Nokia phones. It uses the Gjokii library for file
 * handling.
 * 
 * @author F. Kooman <fkooman@tuxed.net>
 * 
 */
public class NokiRoot {
	private Gjokii g;
	private PrintStream ps;

	private String attributeFileLocation = "/predefhiddenfolder/_jmr_suite_attrib_file__.jmr";
	private String listFileLocation = "/predefhiddenfolder/_jmr_suite_list_file__.jmr";

	/**
	 * Construct the NokiRoot object
	 * 
	 * @param g
	 *            the (open) Gjokii connection to the phone
	 * @param ps
	 *            the stream to write output to (can be System.out)
	 */
	public NokiRoot(Gjokii g, PrintStream ps) {
		this.g = g;
		this.ps = ps;
	}

	/**
	 * Retrieve the MIDlet suite attribute and list files from the phone.
	 * 
	 * @return File objects to both the attribute and list file.
	 * @throws GjokiiException
	 */
	public File[] getApplicationDomainFiles() throws GjokiiException {
		ps.println("(I) downloading application list from the phone...");
		File fAttr = null;
		File fList = null;

		try {
			fAttr = File.createTempFile("ATTR", null);
			fList = File.createTempFile("LIST", null);
		} catch (IOException e) {
			throw new GjokiiException("unable to create temporary files");
		}
		ps.println("(I) using temporary file: " + fAttr.getAbsolutePath());
		g.getFile(attributeFileLocation, fAttr);
		ps.println("(I) using temporary file: " + fList.getAbsolutePath());
		g.getFile(listFileLocation, fList);
		return new File[] { fAttr, fList };
	}

	/**
	 * Retrieve a (formatted) list of installed MIDlet suites and their security
	 * domain.
	 * 
	 * @return the list
	 * @throws GjokiiException
	 */
	public String listApplicationDomains() throws GjokiiException {
		File[] appFiles = getApplicationDomainFiles();

		AttributeAnalyzer aA = new AttributeAnalyzer(appFiles[0]);
		TreeMap<Byte, TreeMap<Byte, Object[]>> tA = aA.getMap();

		ListAnalyzer lA = new ListAnalyzer(appFiles[1]);
		TreeMap<Byte, Object[]> tL = lA.getMap();

		/*
		 * go through attribute map and select the MIDlet suites that have a
		 * "Security Domain" attribute and map it to the name of the MIDlet
		 * suite from the list map...
		 */
		for (Entry<Byte, TreeMap<Byte, Object[]>> e : tA.entrySet()) {
			for (Entry<Byte, Object[]> g : e.getValue().entrySet()) {
				if (g.getKey() == 0x04) {
					/* 0x04 is the code for Security Domain */
					byte[] secDomArr = (byte[]) g.getValue()[1];
					String securityDomain = NokiRootUtils.domainToString(
							secDomArr[0], false);
					int suiteNumber = e.getKey();
					String suiteName = (String) tL.get(e.getKey())[2];
					ps.println("#" + suiteNumber + "\t[" + securityDomain
							+ "] " + suiteName);
				}
			}
		}
		return "";
	}

	/**
	 * Modify the security domain of the MIDlet suite.
	 * 
	 * @param suiteNumber
	 *            the number of the MIDlet suite
	 * @param securityDomain
	 *            the security domain to modify to
	 * @throws GjokiiException
	 */
	public void modifyApplicationDomains(int suiteNumber, byte securityDomain)
			throws GjokiiException {
		/* has to be valid suiteNumber and securityDomain */
		File[] appFiles = getApplicationDomainFiles();

		AttributeAnalyzer aA = new AttributeAnalyzer(appFiles[0]);
		TreeMap<Byte, TreeMap<Byte, Object[]>> tA = aA.getMap();

		ListAnalyzer lA = new ListAnalyzer(appFiles[1]);
		TreeMap<Byte, Object[]> tL = lA.getMap();

		for (Entry<Byte, TreeMap<Byte, Object[]>> e : tA.entrySet()) {
			for (Entry<Byte, Object[]> g : e.getValue().entrySet()) {
				if (g.getKey() == 0x04 && e.getKey() == suiteNumber) {
					byte[] secDomArr = (byte[]) g.getValue()[1];
					int offsetInAttributeFile = (Integer) g.getValue()[0];
					String currentSecurityDomain = NokiRootUtils
							.domainToString(secDomArr[0], false);
					String newSecurityDomain = NokiRootUtils.domainToString(
							securityDomain, false);
					String suiteName = (String) tL.get(e.getKey())[2];
					ps.println("#" + suiteNumber + "\t["
							+ currentSecurityDomain + " --> "
							+ newSecurityDomain + "] " + suiteName);
					/* modify the file at the security domain offset */
					try {
						RandomAccessFile raf = new RandomAccessFile(
								appFiles[0], "rw");
						raf.seek(offsetInAttributeFile);
						raf.writeByte(securityDomain);
						raf.close();
					} catch (IOException ioe) {
						throw new GjokiiException(
								"unable to modify the attribute file");
					}
				}
			}
		}
		ps.println("(I) Uploading attribute file...");
		g.putFile(attributeFileLocation, appFiles[0]);
	}
}
