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

import net.tuxed.gjokii.Gjokii;
import net.tuxed.gjokii.GjokiiException;
import net.tuxed.misc.Utils;

public class NokiRootMain {

	private static final int NO_MODE = -1;
	private static final int LIST_APPS = 1;
	private static final int IDENTIFY = 5;
	private static final int REBOOT = 6;

	private static PrintStream ps = null;
	private static Gjokii g = null;

	public static void main(String args[]) {
		ps = new PrintStream(System.out);
		String homeDir = System.getProperty("user.home");
		/* we check all these locations for a configuration file */
		String[] configFileLocations = new String[] {
				homeDir + File.separator + "gjokiirc",
				homeDir + File.separator + ".gjokiirc", "gjokiirc", ".gjokiirc" };

		File configFile = null;
		for (int i = 0; i < configFileLocations.length; i++) {
			if (new File(configFileLocations[i]).exists()) {
				configFile = new File(configFileLocations[i]);
				ps.println("(I) Using configuration file "
						+ configFile.getPath());
			}
		}

		int channelNumber = -1;
		String deviceAddress = null;
		String phoneFilePathName = null;

		int mode = -1;
		/*
		 * try to get the device hardware address and channel from configuration
		 * file
		 */
		if (configFile == null) {
			ps
					.println("(W) unable to find configuration file, use command line parameter overrides");
		} else {
			try {
				deviceAddress = Utils.getConfigEntry(configFile, "device");
				channelNumber = Integer.parseInt(Utils.getConfigEntry(
						configFile, "channel"));
			} catch (NumberFormatException e) {
				ps.println("(W) broken configuration file");
			} catch (IOException e) {
				ps.println("(W) broken configuration file");
			}
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--device") || args[i].equals("-d")) {
				deviceAddress = args[++i];
			}

			if (args[i].equals("--channel") || args[i].equals("-c")) {
				try {
					channelNumber = Integer.parseInt(args[++i]);
				} catch (NumberFormatException e) {
					/*
					 * the channelNumber will remain -1, we'll detect this later
					 * anyway
					 */
				}
			}

			if (args[i].equals("--list-apps") || args[i].equals("-a")) {
				mode = LIST_APPS;
			}

			if (args[i].equals("--identify") || args[i].equals("-i")) {
				mode = IDENTIFY;
			}

			if (args[i].equals("--reboot") || args[i].equals("-r")) {
				mode = REBOOT;
			}

			if (args[i].equals("--help") || args[i].equals("-h")) {
				showHelp();
				System.exit(0);
			}
		}
		if (mode == NO_MODE) {
			System.err.println("(E) no operation specified, see --help:\n");
			System.exit(1);
		}
		if (deviceAddress == null || channelNumber == -1) {
			System.err
					.println("(E) no device and/or channel specified, see --help:\n");
			System.exit(1);
		}

		try {
			/* FIXME: add verbose command line parsing */
			g = new Gjokii(deviceAddress, channelNumber, false);
			NokiRoot n = new NokiRoot(g, ps);

			switch (mode) {
			case IDENTIFY:
				ps.println("(I) Phone Information:\n\t"
						+ g.getInfo().replace("\n", "\n\t") + "\n\t"
						+ g.getIMEI());
				break;
			case REBOOT:
				ps.println("(I) Rebooting Phone...");
				g.reboot();
				break;
			case LIST_APPS:
				ps.println("(I) List Applications...");
				ps.println(n.listApplicationDomains());
				break;
			}
			g.close();
		} catch (GjokiiException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void showHelp() {
		String output = "NokiCert Help\n-----------\n";
		output += "Basic:\n";
		output += "  -d, --device <hwaddr>      Specify the Bluetooth device (e.g.: 001122334455)\n";
		output += "  -c, --channel <channel>    Specify the Bluetooth channel (e.g.: 15)\n";
		output += "Operations:\n";
		output += "  -i, --identify             Print phone identification\n";
		output += "  -r, --reboot               Reboot the phone\n";
		output += "  -a, --list-apps            List installed applications\n";
		output += "  -v, --verbose              Increase verbosity\n";
		output += "  -h, --help                 Show this help message\n";
		ps.println(output);
	}
}
