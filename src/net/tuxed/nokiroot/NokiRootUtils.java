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

import net.tuxed.misc.Utils;

public class NokiRootUtils {
	/**
	 * Converts a security domain from the S40 phone to a human readable text
	 * 
	 * @param t
	 *            the security domain number
	 * @param printNumber
	 *            whether or not to include the number of the domain in the
	 *            output
	 * @return the human readable security domain
	 */
	public static String domainToString(int t, boolean printNumber) {
		String o = printNumber ? "[" + Utils.byteToString(t) + "] " : "";
		switch (t) {
		case (byte) 0x20:
			return o + "Manufacturer            ";
		case (byte) 0x02:
			return o + "Identified Third Party  ";
		case (byte) 0x01:
			return o + "Unidentified Third Party";
		case (byte) 0x10:
			return o + "Operator                ";
		default:
			return o + "Unknown Domain          ";
		}
	}

	/**
	 * Convert permission byte to human readable permission.
	 * 
	 * @param t
	 *            the permission
	 * @return the associated human readable permission
	 */
	public static String permissionToString(int t) {
		String o = "[" + Utils.byteToString(t) + "] ";
		switch (t) {
		case (byte) 0x01:
			return o + "Not allowed";
		case (byte) 0x02:
			return o + "Ask every time";
		case (byte) 0x04:
			return o + "Ask first time only";
		case (byte) 0x06:
			return o + "Always allowed (Unidentified Third Party)";
		case (byte) 0x08:
			return o + "Always allowed (Identified Third Party)";
		default:
			return o + "Unknown Permission";
		}
	}

	/**
	 * Convert an attribute type to human readable format.
	 * 
	 * @param t
	 *            the attribute type
	 * @return the human readable attribute
	 */
	public static String typeToString(int t) {
		String o = "[" + Utils.byteToString(t) + "] ";
		switch (t) {
		case (byte) 0x04:
			return o + "Security Domain                      ";
		case (byte) 0x05:
			return o + "SHA-1 Modulus Issuer                 ";
		case (byte) 0x06:
			return o + "MIDlet Suite JAR file SHA-1 (1)      ";
		case (byte) 0x07:
			return o + "Organization Issuer                  ";
		case (byte) 0x08:
			return o + "Organization Subject                 ";
		case (byte) 0x09:
			return o + "Country Issuer Certificate           ";
		case (byte) 0x0a:
			return o + "Country Subject Certificate          ";
		case (byte) 0x0b:
			return o + "MIDlet Suite JAD file SHA-1          ";
		case (byte) 0x0c:
			return o + "MIDlet Suite JAR file SHA-1 (2)      ";
		case (byte) 0x0d:
			return o + "Subject Certificate Fingerprint      ";
		case (byte) 0x0e:
			return o + "Issuer Certificate Fingerprint       ";
		case (byte) 0x0f:
			return o + "MIDlet Suite Version Number (S40_E3) ";
		case (byte) 0x11:
			return o + "MIDlet Suite Version Number (S40_E5) ";
		case (byte) 0x15:
			return o + "Padding (?)                          ";
		case (byte) 0x16:
			return o + "Subject Distinguished Name           ";
		case (byte) 0x19:
			return o + "(Permission) Network access          ";
		case (byte) 0x1a:
			return o + "(Permission) Messaging               ";
		case (byte) 0x1b:
			return o + "(Permission) Connectivity            ";
		case (byte) 0x1c:
			return o + "(Permission) Multimedia recording    ";
		case (byte) 0x1d:
			return o + "(Permission) Read user data          ";
		case (byte) 0x1e:
			return o + "(Permission) Add and edit data       ";
		case (byte) 0x1f:
			return o + "(Permission) Auto-start              ";
		case (byte) 0x20:
			return o + "(Permission) Smart card              ";
		default:
			return o + "Unknown Type                         ";
		}
	}
}