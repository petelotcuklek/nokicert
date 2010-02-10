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

import java.util.Arrays;

/**
 * NokiCert Utils.
 * 
 * @author F. Kooman <fkooman@tuxed.net>
 * 
 */
public class NokiCertUtils {
	public final static int APPS_SIGNING = 1;
	public final static int CROSS_CERTIFICATION = 2;
	public final static int SERVER_AUTHENTIC = 4;

	private NokiCertUtils() {
	}

	/**
	 * Converts a specific key usage byte array from the S40 phone to a keyUsage
	 * type
	 * 
	 * @param keyUsage
	 *            the key usage pattern
	 * @return the keyUsage type
	 */
	public static int keyUsageBytesToType(byte[] keyUsage) {
		if (Arrays.equals(new byte[] { (byte) 0x2b, 0x06, 0x01, 0x05, 0x05,
				0x07, 0x03, 0x03 }, keyUsage)) {
			return APPS_SIGNING;
		} else if (Arrays.equals(new byte[] { (byte) 0x2b, 0x06, 0x01, 0x04,
				0x01, 0x5e, 0x01, 0x31, 0x04, 0x01 }, keyUsage)) {
			return CROSS_CERTIFICATION;
		} else if (Arrays.equals(new byte[] { (byte) 0x2b, 0x06, 0x01, 0x05,
				0x05, 0x07, 0x03, 0x01 }, keyUsage)) {
			return SERVER_AUTHENTIC;
		} else {
			return -1;
		}
	}

	/**
	 * Convert keyUsage type to human readable String
	 * 
	 * @param keyUsage
	 *            the keyUsage type
	 * @return the human readable keyUsage
	 */
	public static String keyUsageTypeToString(int keyUsage) {
		String output = "{";
		if ((keyUsage & APPS_SIGNING) == APPS_SIGNING)
			output += "Apps. Signing, ";
		if ((keyUsage & CROSS_CERTIFICATION) == CROSS_CERTIFICATION)
			output += "Cross Certification, ";
		if ((keyUsage & SERVER_AUTHENTIC) == SERVER_AUTHENTIC)
			output += "Server authentic., ";
		/*
		 * remove the last ", " from the output string, needed because not all
		 * are required
		 */
		output = output.substring(0, output.length() - 2);
		output += "}";
		return output;
	}
}
