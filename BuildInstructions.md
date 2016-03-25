

## Requirements ##
  * Ant
  * Java 6 (Sun or OpenJDK)
  * [BlueCove](http://www.bluecove.org) Bluetooth Java library (`bluecove.jar`)
    * For Linux you also need `bluecove-gpl.jar` and `bluecove-bluez.jar`
  * [Base64 Java library](http://iharder.sourceforge.net/current/java/base64/) (in package `net.sourceforge.iharder`)
  * [SWT](http://www.eclipse.org/swt) (graphics library for your platform)

## Building ##

Generic instructions:
```
$ ant doc dist \
    -Dbluecove.jar=/path/to/bluecove-VERSION.jar \
    -Dswt.jar=/path/to/swt-PLATFORM-VERSION.jar \
    -Dbase64.jar=/path/to/base64-VERSION.jar
```

This compiles both NokiCert and creates the API documentation.

## Running ##

The directory `misc` of the source package and SVN repository contains launch scripts
for both Windows and Linux/Mac/BSD platforms. They set the correct `CLASSPATH` before launching the `net.tuxed.nokicert.NokiCertGUI` class. These scripts are used by the binary release, so one will have to modify the paths to the supporting libraries.