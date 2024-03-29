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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.tuxed.gjokii.Gjokii;
import net.tuxed.gjokii.GjokiiException;
import net.tuxed.misc.Utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This is a multi platform GUI application for NokiCert to install certificates
 * on Nokia phones.
 * 
 * @author F. Kooman <fkooman@tuxed.net>
 * 
 */
public class NokiCertGUI {
	private Shell mainWindow = null;
	private Shell configMainWindow = null;
	private Group certUsageGroup = null;
	private Group selectGroup = null;
	private Group listGroup = null;
	private Button appsSigning = null;
	private Button crossCertification = null;
	private Button serverAuthentic = null;
	private static Button selectCertButton = null;
	private Button installCertButton = null;
	private Button infoCertButton = null;
	private static Button getListButton = null;
	private String certFilePathName = null;
	private Text text = null;
	private static Gjokii gjokii = null;
	private static NokiCert nokiCert = null;
	private static Label statusLabel = null;

	private void createConfigWindow(final File configFile) {
		configMainWindow = new Shell();
		configMainWindow.setText("Configuration");
		configMainWindow.setLayout(new GridLayout(2, false));
		Label l = new Label(configMainWindow, SWT.NONE);
		l.setText("Bluetooth Hardware Address");
		final Text t = new Text(configMainWindow, SWT.BORDER);
		t.setText("001122334455");
		Label l2 = new Label(configMainWindow, SWT.NONE);
		l2.setText("Channel");
		final Text t2 = new Text(configMainWindow, SWT.BORDER);
		t2.setText("15");
		Button b = new Button(configMainWindow, SWT.NONE);
		b.setText("Continue");
		b.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				String device = t.getText();
				String channel = t2.getText();

				/* device should be valid hardware address */
				if (!device.matches("[0-9a=fA-F]{12}")) {
					System.err.println("invalid hardware address");
					System.exit(1);
				}
				/* channel should be integer */
				try {
					Integer.parseInt(channel);
				} catch (NumberFormatException e2) {
					System.err.println("invalid channel number");
					System.exit(1);
				}
				try {
					FileWriter fw = new FileWriter(configFile);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter pw = new PrintWriter(bw);
					pw.println("# automatically generated by NokiCert");
					pw.println("device = " + device);
					pw.println("channel = " + channel);
					pw.flush();
					pw.close();
				} catch (IOException e1) {
				}
				configMainWindow.close();
			}
		});
	}

	private void createMainWindow() {
		mainWindow = new Shell();
		mainWindow.setText("Nokia S40 X.509 Certificate Manager");
		// mainWindow.setSize(new Point(619, 273));
		GridLayout g = new GridLayout(1, false);
		mainWindow.setLayout(g);

		listGroup = new Group(mainWindow, SWT.NONE);
		listGroup.setText("Installed Certificates");
		listGroup.setLayout(new GridLayout(1, false));
		getListButton = new Button(listGroup, SWT.NONE);
		getListButton.setText("Get List");
		getListButton.setEnabled(false);
		getListButton
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						statusLabel
								.setText("Status: getting certifcate list...");
						final Shell dialog = new Shell(mainWindow,
								SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
						dialog.setText("Installed Certificates");
						dialog.setLayout(new GridLayout(1, false));
						Label l = new Label(dialog, SWT.BORDER);
						Button b = new Button(dialog, SWT.NONE);
						b.setText("OK");
						b.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
							public void widgetSelected(
									org.eclipse.swt.events.SelectionEvent e) {
								dialog.close();
							}
						});
						try {
							String certList = nokiCert.listCertificates();
							if (certList != null && certList.length() != 0)
								l.setText(certList);
							else
								l.setText("No certificates installed!");
						} catch (GjokiiException e1) {
							statusLabel.setText("Status: " + e1.getMessage());
						}
						dialog.pack();
						dialog.open();
					}
				});

		selectGroup = new Group(mainWindow, SWT.NONE);
		selectGroup.setText("Add a Certificate");
		selectGroup.setLayout(new GridLayout(3, false));

		text = new Text(selectGroup, SWT.BORDER);
		text.setText("                                                  ");
		text.setEditable(false);
		text.setEnabled(true);

		selectCertButton = new Button(selectGroup, SWT.NONE);
		selectCertButton.setText("Select certificate...");
		selectCertButton.setEnabled(false);
		selectCertButton
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						FileDialog dialog = new FileDialog(mainWindow, SWT.OPEN);
						String[] filterNames = new String[] {
								"X.509 DER encoded Certificates",
								"All Files (*)" };
						String[] filterExtensions = new String[] {
								"*.cer;*.der;*.crt;*.pem", "*" };
						String filterPath = System.getProperty("user.home");
						dialog.setFilterNames(filterNames);
						dialog.setFilterExtensions(filterExtensions);
						dialog.setFilterPath(filterPath);
						certFilePathName = dialog.open();
						CertParser x = null;
						try {
							if (certFilePathName != null) {
								x = new CertParser(new File(certFilePathName));
								text.setText(x.getSubjectCommonName());
								infoCertButton.setEnabled(true);
								installCertButton.setEnabled(true);
								// certUsageGroup.setEnabled(true);
								for (int i = 0; i < certUsageGroup
										.getChildren().length; i++) {
									certUsageGroup.getChildren()[i]
											.setEnabled(true);
								}
								statusLabel.setText("Status: read certificate");
							} else {
								infoCertButton.setEnabled(false);
								installCertButton.setEnabled(false);
								// certUsageGroup.setEnabled(false);

								text.setText("");
								statusLabel
										.setText("Status: no certificate selected");
							}
						} catch (GjokiiException e1) {
							statusLabel
									.setText("Status: unable to read certificate");
							infoCertButton.setEnabled(false);
							installCertButton.setEnabled(false);
							// certUsageGroup.setEnabled(false);

						}
					}
				});

		infoCertButton = new Button(selectGroup, SWT.NONE);
		infoCertButton.setText("Info");
		infoCertButton.setEnabled(false);
		infoCertButton
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {

						final Shell dialog = new Shell(mainWindow,
								SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
						dialog.setText("X.509 Certificate Information");
						dialog.setLayout(new GridLayout(1, false));
						Label l = new Label(dialog, SWT.BORDER);
						Button b = new Button(dialog, SWT.NONE);
						b.setText("OK");
						b.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
							public void widgetSelected(
									org.eclipse.swt.events.SelectionEvent e) {
								dialog.close();
							}
						});
						CertParser x = null;
						try {
							if (certFilePathName != null) {
								x = new CertParser(new File(certFilePathName));
								l.setText(x.toString());
							}
						} catch (GjokiiException e1) {
						}
						dialog.pack();
						dialog.open();
					}
				});
		certUsageGroup = new Group(selectGroup, SWT.NONE);
		certUsageGroup.setText("Certificate Usage");
		certUsageGroup.setLayout(new GridLayout(1, false));
		// certUsageGroup.setEnabled(false);
		appsSigning = new Button(certUsageGroup, SWT.CHECK | SWT.UP);
		appsSigning.setText("Apps. signing");
		appsSigning.setSelection(true);
		crossCertification = new Button(certUsageGroup, SWT.CHECK);
		crossCertification.setText("Cross-certification");
		serverAuthentic = new Button(certUsageGroup, SWT.CHECK);
		serverAuthentic.setText("Server authentic.");

		for (int i = 0; i < certUsageGroup.getChildren().length; i++) {
			certUsageGroup.getChildren()[i].setEnabled(false);
		}
		text.setEnabled(false);

		installCertButton = new Button(selectGroup, SWT.NONE);
		installCertButton.setText("Install certificate");
		installCertButton.setEnabled(false);
		installCertButton
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						statusLabel
								.setText("Status: installing certificate...");
						int keyUsage = 0;
						if (appsSigning.getSelection())
							keyUsage |= NokiCertUtils.APPS_SIGNING;
						if (crossCertification.getSelection())
							keyUsage |= NokiCertUtils.CROSS_CERTIFICATION;
						if (serverAuthentic.getSelection())
							keyUsage |= NokiCertUtils.SERVER_AUTHENTIC;
						try {
							nokiCert.installCertificate(certFilePathName,
									keyUsage);
							statusLabel
									.setText("Status: certificate installed");

						} catch (GjokiiException ez) {
							statusLabel.setText("Status: " + ez.getMessage());
						}
					}
				});
		Composite c = new Composite(mainWindow, SWT.NONE);
		c.setLayout(new GridLayout(1, false));
		statusLabel = new Label(c, SWT.NONE);
		statusLabel
				.setText("Status: connecting...                                                               ");
	}

	public static void main(String[] args) {
		org.eclipse.swt.widgets.Display display = org.eclipse.swt.widgets.Display
				.getDefault();
		String configFile = System.getProperty("user.home") + File.separator
				+ ".gjokiirc";
		File cF = new File(configFile);
		if (!cF.exists()) {
			System.out.println("Config file does not exist, creating one!");
			NokiCertGUI test = new NokiCertGUI();
			test.createConfigWindow(cF);
			test.configMainWindow.pack();
			test.configMainWindow.open();
			while (!test.configMainWindow.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();
		}
		System.out.println("Using configuration file: " + configFile);
		String device;
		display = org.eclipse.swt.widgets.Display.getDefault();
		try {
			NokiCertGUI test = new NokiCertGUI();
			test.createMainWindow();
			test.mainWindow.pack();
			test.mainWindow.open();
			try {
				device = Utils.getConfigEntry(new File(configFile), "device");
				int channel = Integer.parseInt(Utils.getConfigEntry(new File(
						configFile), "channel"));
				gjokii = new Gjokii(device, channel, false);
				statusLabel.setText("Status: connected");
				nokiCert = new NokiCert(gjokii, System.out);
				getListButton.setEnabled(true);
				selectCertButton.setEnabled(true);
			} catch (NumberFormatException e) {
				statusLabel.setText("Error: broken configuration file: "
						+ configFile);
			} catch (GjokiiException e) {
				statusLabel.setText("Status: " + e.getMessage());
			} catch (RuntimeException e) {
				statusLabel.setText("Error: " + e.getMessage());
			}

			while (!test.mainWindow.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();
			try {
				gjokii.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
