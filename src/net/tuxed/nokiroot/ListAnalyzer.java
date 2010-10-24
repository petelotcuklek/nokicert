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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map.Entry;

import net.tuxed.gjokii.GjokiiException;
import net.tuxed.misc.Utils;

/**
 * This class analyze the JMR list file to determine what MIDlet suites are
 * installed, what their number is and what the incremental sequence numbers
 * are.
 * 
 * @author F. Kooman <fkooman@tuxed.net>
 * 
 */
public class ListAnalyzer {
	private TreeMap<Byte, Object[]> map = new TreeMap<Byte, Object[]>();

	private int maxSeqNo1;
	private int maxSeqNo2;

	/**
	 * Analyze given file for specified platform
	 * 
	 * @param f
	 *            the file to analyze
	 * @param platform
	 *            the platform
	 * @throws GjokiiException
	 *             if the platform is not supported
	 */
	public ListAnalyzer(File f) throws GjokiiException {
		/* first we split the list in its various entries */
		try {
			FileInputStream fis = new FileInputStream(f);
			while (fis.available() > 4) {
				byte[] lengthBytes = new byte[4];
				fis.read(lengthBytes);
				int size = Utils.byteArrayToIntLE(lengthBytes, 0);
				byte[] dataBytes = new byte[size - 4];
				fis.read(dataBytes);

				byte suiteNumber = dataBytes[0];
				int seqNo1 = Utils.byteArrayToIntLE(dataBytes, 4);
				maxSeqNo1 = (seqNo1 > maxSeqNo1) ? seqNo1 : maxSeqNo1;
				int seqNo2 = -1;

				int noOfFields = 9; /* Nokia 6212 Classic */
				// int noOfFields = 5; /* Nokia 6131 NFC */

				if (noOfFields == 9) {
					seqNo2 = Utils.byteArrayToIntLE(dataBytes, 18);
					maxSeqNo2 = (seqNo2 > maxSeqNo2) ? seqNo2 : maxSeqNo2;
				}
				int offset = noOfFields * 4;
				int suiteNameSize = Utils.byteArrayToIntLE(dataBytes, offset);
				byte[] suiteNameArray = new byte[suiteNameSize];
				System.arraycopy(dataBytes, offset + 8, suiteNameArray, 0,
						suiteNameSize);
				String suiteName = Utils.bytesToStringLE(suiteNameArray);
				int vendorNameSize = Utils.byteArrayToIntLE(dataBytes,
						offset + 4);
				byte[] vendorNameArray = new byte[vendorNameSize];
				System.arraycopy(dataBytes, offset + 8 + suiteNameSize,
						vendorNameArray, 0, vendorNameSize);
				String vendorName = Utils.bytesToStringLE(vendorNameArray);
				map.put(suiteNumber, new Object[] { seqNo1, seqNo2, suiteName,
						vendorName });
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the maximum sequence number (1)
	 * 
	 * @return the maximum sequence number
	 */
	public int getMaxSeqNo1() {
		return maxSeqNo1;
	}

	/**
	 * Get the maximum sequence number (2)
	 * 
	 * @return the maximum sequence number
	 */

	public int getMaxSeqNo2() {
		return maxSeqNo2;
	}

	/**
	 * Get the maximum suite number included in the file
	 * 
	 * @return the maximum suite number
	 */
	public int getMaxSuiteNumber() {
		return map.lastKey();
	}

	/**
	 * Get the map contained in the object
	 * 
	 * @return the map
	 */
	public TreeMap<Byte, Object[]> getMap() {
		return map;
	}

	@Override
	public String toString() {
		String output = "";
		/* for every entry */
		for (Entry<Byte, Object[]> e : map.entrySet()) {
			byte suiteNumber = e.getKey();
			int seqNo1 = (Integer) e.getValue()[0];
			int seqNo2 = (Integer) e.getValue()[1];
			String suiteName = (String) e.getValue()[2];
			String suiteVendor = (String) e.getValue()[3];

			if (seqNo1 == 0) {
				output += "[" + suiteNumber + "]" + "\t(" + suiteName + ")\n";
			} else {
				output += "["
						+ suiteNumber
						+ "]"
						+ "\t("
						+ seqNo1
						+ "{"
						+ Utils.byteArrayToString(Utils
								.intToByteArrayLE(seqNo1))
						+ "},"
						+ seqNo2
						+ "{"
						+ Utils.byteArrayToString(Utils
								.intToByteArrayLE(seqNo2)) + "},suite="
						+ suiteName + ",vendor=" + suiteVendor + ")\n";
			}
		}
		return output;
	}
}
