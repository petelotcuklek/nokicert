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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import net.tuxed.gjokii.GjokiiException;
import net.tuxed.misc.Utils;

/**
 * This class analyzes the certificate file list from the Nokia phones
 * 
 * @author F. Kooman <fkooman@tuxed.net>
 * 
 */
public class CertListParser {
	private ArrayList<Object[]> list;
	private boolean littleEndian;

	public CertListParser(File f) throws GjokiiException {
		littleEndian = false;
		list = new ArrayList<Object[]>();
		try {
			FileInputStream fis = new FileInputStream(f);
			while (fis.available() > 4) {
				int bytesRead = 0;
				byte[] lengthBytes = new byte[2];
				fis.read(lengthBytes);
				int size = Utils.byteArrayToShort(lengthBytes, 0) - 2;
				if (size >= f.length() || size < 0) {
					/* we seem to have a little endian length indicator */
					littleEndian = true;
					size = Utils.byteArrayToShortLE(lengthBytes, 0) - 2;
				}
				// System.out.println(Utils.byteArrayToString(lengthBytes));
				// System.out.println("Size of block: " + size);
				bytesRead += fis.skip(10);
				byte[] fingerprint = new byte[20];
				bytesRead += fis.read(fingerprint);
				byte[] hashOfModulus = new byte[20];
				bytesRead += fis.read(hashOfModulus);
				byte[] unknownField = new byte[20];
				bytesRead += fis.read(unknownField);
				byte[] hashOfSubject = new byte[20];
				bytesRead += fis.read(hashOfSubject);
				byte[] hashOfIssuer = new byte[20];
				bytesRead += fis.read(hashOfIssuer);
				int sizeOfFileName = fis.read();
				bytesRead++;
				byte[] fileName = new byte[sizeOfFileName - 1];
				bytesRead += fis.read(fileName);
				// System.out.println(new String(fileName));
				bytesRead += fis.skip(2);
				int keyUsage = 0;
				int keyUsageLength = fis.read();
				// System.out.println(keyUsageLength);
				bytesRead++;
				byte[] keyUsageBytes = new byte[keyUsageLength];
				bytesRead += fis.read(keyUsageBytes);
				// System.out.println(Utils.byteArrayToString(keyUsageBytes));
				/* read all usages */
				int offset = 1;
				while (offset < keyUsageBytes.length) {
					int curKeyUsageLength = keyUsageBytes[offset];
					if (curKeyUsageLength + offset > keyUsageBytes.length
							|| curKeyUsageLength < 0) {
						/*
						 * something seems wrong in key usage byte array, skip
						 * it
						 */
						break;
					}
					byte[] t = new byte[curKeyUsageLength];
					offset++;
					System.arraycopy(keyUsageBytes, offset, t, 0,
							curKeyUsageLength);
					keyUsage |= NokiCertUtils.keyUsageBytesToType(t);
					offset += curKeyUsageLength + 1;
				}
				// System.out.println("Skipping " + (size - bytesRead)
				// + " bytes...");
				fis.skip(size - bytesRead);
				list.add(new Object[] { fingerprint, hashOfModulus,
						unknownField, hashOfSubject, hashOfIssuer, fileName,
						keyUsage });
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		String output = "";
		/* for every entry */
		for (Object[] o : list) {
			// output += "Fingerprint: " + Utils.byteArrayToString((byte[])
			// o[0])
			// + "\n";
			// output += "Hash Of Modulus: "
			// + Utils.byteArrayToString((byte[]) o[1]) + "\n";
			// output += "Unknown Field  : "
			// + Utils.byteArrayToString((byte[]) o[2]) + "\n";
			// output += "Hash Of Subject: "
			// + Utils.byteArrayToString((byte[]) o[3]) + "\n";
			// output += "Hash Of Issuer : "
			// + Utils.byteArrayToString((byte[]) o[4]) + "\n";
			output += new String((byte[]) o[5]) + "    ("
					+ Utils.byteArrayToString((byte[]) o[0]) + ")\n";
			// output += "Key Usage: "
			// + GjokiiUtils.keyUsageTypeToString((Integer) o[6]) + "\n";
			// output += "\n";
		}
		return output;
	}

	public boolean hasLittleEndianSizeBytes() {
		return littleEndian;
	}
}
