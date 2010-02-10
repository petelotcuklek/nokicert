/*
 *  This file is part of Gjokii.
 *
 *  Gjokii is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Gjokii is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Gjokii.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.tuxed.gjokii;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import net.tuxed.misc.Utils;

public class GjokiiMain {

	private static final int NO_MODE = -1;
	private static final int GET_FILE = 0;
	private static final int PUT_FILE = 1;
	private static final int DELETE_FILE = 2;
	private static final int LIST_DIRECTORY = 3;
	private static final int DUMP_FS = 4;
	private static final int IDENTIFY = 5;
	private static final int REBOOT = 6;

	private static PrintStream ps = null;
	private static Gjokii g = null;

	/**
	 * Get a directory listing from the phone
	 * 
	 * @param phoneDirPathName
	 *            the path to start from (should end with "/")
	 * @param recursive
	 *            whether or not to recursively list directories starting from
	 *            phoneDirPathName
	 */
	private static void dirList(String phoneDirPathName, boolean recursive)
			throws GjokiiException {
		dirList(phoneDirPathName, recursive, 0);
	}

	private static void dirList(String phoneDirPathName, boolean recursive,
			int depth) throws GjokiiException {
		ArrayList<DirectoryEntryInfo> directoryListing = g
				.getDirectoryList(phoneDirPathName);
		for (DirectoryEntryInfo d : directoryListing) {

			/* ugly spacing crap */
			String sizeSpacing = "";
			for (int i = 0; i < 40 - d.getEntryName().length(); i++)
				sizeSpacing += " ";
			String dateSpacing = "";
			for (int i = 0; i < 8 - (d.getEntrySize() + "").length(); i++)
				dateSpacing += " ";

			if (recursive) {
				for (int i = 0; i < depth; i++)
					ps.print("  ");
			}
			if (d.isDirectory()) {
				if (recursive) {
					ps.println("d  " + d.getEntryName());
					dirList(phoneDirPathName + d.getEntryName() + "/",
							recursive, depth + 1);
				} else {
					ps.println("d  " + d.getEntryName() + sizeSpacing
							+ d.getEntrySize() + dateSpacing + d.getEntryDate()
							+ " " + d.getEntryTime());
				}
			} else if (d.isFile()) {
				if (recursive) {
					ps.println("   " + d.getEntryName());
				} else {
					ps.println("   " + d.getEntryName() + sizeSpacing
							+ d.getEntrySize() + dateSpacing + d.getEntryDate()
							+ " " + d.getEntryTime());
				}
			} else {
				/* probably empty directory */
			}
		}
	}

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
		String phoneDirPathName = null;
		boolean recursive = false;

		boolean verbose = false;
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
					 * the channelNumber will remain -1, we'll find this out
					 * later anyway
					 */
				}
			}

			if (args[i].equals("--getfile") || args[i].equals("-g")) {
				mode = GET_FILE;
				phoneFilePathName = args[++i];
			}

			if (args[i].equals("--putfile") || args[i].equals("-p")) {
				mode = PUT_FILE;
				phoneFilePathName = args[++i];
			}

			if (args[i].equals("--deletefile") || args[i].equals("-D")) {
				mode = DELETE_FILE;
				phoneFilePathName = args[++i];
			}

			if (args[i].equals("--getdirlist") || args[i].equals("-l")) {
				mode = LIST_DIRECTORY;
				phoneDirPathName = args[++i];
			}

			if (args[i].equals("--dumpfs") || args[i].equals("-f")) {
				mode = DUMP_FS;
				phoneDirPathName = args[++i];
			}

			if (args[i].equals("--recursive") || args[i].equals("-R")) {
				recursive = true;
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

			if (args[i].equals("--verbose") || args[i].equals("-v")) {
				verbose = true;
			}
		}
		if (mode == NO_MODE) {
			System.err.println("(E) no mode specified, see --help:\n");
			System.exit(1);
		}
		if (deviceAddress == null || channelNumber == -1) {
			System.err
					.println("(E) no device and/or channel specified, see --help:\n");
			System.exit(1);
		}
		if ((mode == GET_FILE) && phoneFilePathName.length() == 0) {
			System.err.println("(E) no file specified, see --help:\n");
			System.exit(1);
		}
		if ((mode == LIST_DIRECTORY) && phoneDirPathName.length() == 0) {
			System.err.println("(E) no directory specified, see --help:\n");
			System.exit(1);
		}
		if ((mode == DUMP_FS) && phoneDirPathName.length() == 0) {
			System.err.println("(E) no directory specified, see --help:\n");
			System.exit(1);
		}
		if ((mode == PUT_FILE) && phoneFilePathName.length() == 0) {
			System.err.println("(E) no file specified, see --help:\n");
			System.exit(1);
		}
		if ((mode == DELETE_FILE) && phoneFilePathName.length() == 0) {
			System.err.println("(E) no file specified, see --help:\n");
			System.exit(1);
		}

		try {
			g = new Gjokii(deviceAddress, channelNumber, verbose);

			switch (mode) {
			case GET_FILE:
				ps.println("(I) Getting file " + phoneFilePathName + "...");
				g.getFile(phoneFilePathName);
				break;
			case PUT_FILE:
				ps.println("(I) Putting file " + phoneFilePathName + "...");
				g.putFile(phoneFilePathName);
				break;
			case DELETE_FILE:
				ps.println("(I) Deleting file " + phoneFilePathName + "...");
				g.deleteFile(phoneFilePathName);
				break;
			case IDENTIFY:
				ps.println("(I) Phone Information:\n\t"
						+ g.getInfo().replace("\n", "\n\t") + "\n\t"
						+ g.getIMEI());
				break;
			case REBOOT:
				ps.println("(I) Rebooting Phone...");
				g.reboot();
				break;
			case LIST_DIRECTORY:
				ps
						.println("(I) Directory Listing of " + phoneDirPathName
								+ ":");
				dirList(phoneDirPathName, recursive);
				break;
			case DUMP_FS:
				ps.println("(I) Dumping Directory " + phoneDirPathName + ":\n");
				g.dumpFileSystem(phoneDirPathName, recursive);
				break;
			}
			g.close();
		} catch (GjokiiException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void showHelp() {
		String output = "Gjokii Help\n-----------\n";
		output += "Basic:\n";
		output += "  -d, --device <hwaddr>      Specify the Bluetooth device (e.g.: 001122334455)\n";
		output += "  -c, --channel <channel>    Specify the Bluetooth channel (e.g.: 15)\n";
		output += "  -i, --identify             Print phone identification\n";
		output += "  -r, --reboot               Reboot the phone\n";
		output += "  -g, --getfile <file>       Get a file from the phone (e.g.: /a/b/c.ext)\n";
		output += "  -p, --putfile <file>       Put a file on the phone (e.g.: /a/b/c.ext)\n";
		output += "  -D, --deletefile <file>    Delete a file from the phone (e.g.: /a/b/c.ext)\n";
		output += "  -l, --getdirlist <dir>     Get a directory list (e.g.: /a/b/)\n";
		output += "  -f, --dumpfs <dir>         Dump the phone file system (e.g.: /a/b/)\n";
		output += "  -R, --recursive            Recursive (for --dumpfs and --getdirlist)\n";
		output += "  -v, --verbose              Increase verbosity\n";
		output += "  -h, --help                 Show this help message\n";
		ps.println(output);
	}
}