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

import net.tuxed.misc.Utils;

/**
 * This class analyze the JMR attribute file
 * 
 * @author F. Kooman <fkooman@tuxed.net>
 * 
 */
public class AttributeAnalyzer {
	/**
	 * suiteNumber, { attributeNumber, { dataOffset, data }}
	 */
	private TreeMap<Byte, TreeMap<Byte, Object[]>> suiteMap = new TreeMap<Byte, TreeMap<Byte, Object[]>>();

	/**
	 * Analyze given file for specified platform
	 * 
	 * @param f
	 *            the file to analyze
	 */
	public AttributeAnalyzer(File f) {
		/* first we split the list in its various entries */
		try {
			int offsetInFile = 0;
			FileInputStream fis = new FileInputStream(f);
			while (fis.available() > 4) {
				byte[] lengthBytes = new byte[4];
				offsetInFile += fis.read(lengthBytes);
				int size = Utils.byteArrayToIntLE(lengthBytes, 0);
				byte[] dataBytes = new byte[size - 4];
				fis.read(dataBytes);

				// System.out.println(Utils.hexDump(dataBytes));

				byte suiteNumber = dataBytes[0];
				int numberOfFields = Utils.byteArrayToIntLE(dataBytes, 4);
				TreeMap<Byte, Object[]> attribMap = new TreeMap<Byte, Object[]>();
				int offset = (numberOfFields + 1) * 8;

				for (int i = 0; i < numberOfFields; i++) {
					int attrSize = Utils.byteArrayToIntLE(dataBytes, i * 8 + 8);
					byte attrType = dataBytes[i * 8 + 12];
					byte[] content = new byte[attrSize];
					System.arraycopy(dataBytes, offset, content, 0, attrSize);
					attribMap.put(attrType, new Object[] {
							offsetInFile + offset, content });
					offset += attrSize;
				}
				suiteMap.put(suiteNumber, attribMap);
				offsetInFile += size - 4;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the map contained in the object
	 * 
	 * @return the map
	 */
	public TreeMap<Byte, TreeMap<Byte, Object[]>> getMap() {
		return suiteMap;
	}

	@Override
	public String toString() {
		String output = "";
		/* for every suite */
		for (Entry<Byte, TreeMap<Byte, Object[]>> e : suiteMap.entrySet()) {
			byte suiteNumber = e.getKey();
			output += "[" + suiteNumber + "]:\n";
			/* for every attribute */
			for (Entry<Byte, Object[]> g : e.getValue().entrySet()) {
				byte key = g.getKey();
				byte[] byteAttributeValue = (byte[]) g.getValue()[1];
				String attributeValue = null;
				/*
				 * we can convert some attribute values to human readable text
				 * like strings or some permission bits and security domain.
				 * 
				 * Watch out: 0x0f is suite version on S40_E3 and 0x11 is suite
				 * version on S40_E5. Maybe this is not safe if these keys mean
				 * something else on different platforms and for example string
				 * conversion doesn't work...
				 */
				if (key == 0x07 || key == 0x08 || key == 0x0f || key == 0x16
						|| key == 0x11 || key == 0x09 || key == 0x0a) {
					attributeValue = Utils.bytesToStringLE(byteAttributeValue);
				} else if (key == 0x04) {
					attributeValue = NokiRootUtils.domainToString(
							byteAttributeValue[0], true);
				} else if (key == 0x19 || key == 0x1a || key == 0x1b
						|| key == 0x1c || key == 0x1d || key == 0x1e
						|| key == 0x1f || key == 0x20) {
					attributeValue = NokiRootUtils
							.permissionToString(byteAttributeValue[0]);
				} else {
					attributeValue = Utils
							.byteArrayToString(byteAttributeValue);
				}
				output += "  " + NokiRootUtils.typeToString(key) + " : "
						+ attributeValue + "\n";
			}
		}
		return output;
	}
}
