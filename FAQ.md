

## What is it? ##
This tool makes it possible to install additional certificates on Nokia S40 phones that make it possible to run signed (by a CA of your choice) MIDlet suites in the trusted third party security domain. By default only a limited number of CAs are included in the phone. The included CAs all require (yearly) payments (around $200 a year) to obtain a signing certificate.

## Why did you create this? ##
I was interested in analyzing the security of the Nokia S40 platform as part of writing my thesis.

## Is this legal? ##
Well, IANAL, but as far as I know when you buy a phone it becomes your
property and you can do whatever you want with it. I'm not sure this holds
when you take the DMCA into consideration.

## What platforms are supported? ##
Basically everything that supports Java. We tested this on Windows (32 bit),
Mac OS X 10.6 and Linux (Ubuntu and Fedora, 32 and 64 bit).

Mac OS X 10.5 seems to have a problem. The `-d32` option doesn't seem to work there (to run Java in 32 bit mode). Not sure how to fix this as I don't have a Mac OS X 10.5 installation available. Upstream Bluecove only has a 32 bit library for Mac, so 64 bit Java does not work yet.

## What certificates are supported? ##
Basically all [DER encoded](http://en.wikipedia.org/wiki/Distinguished_Encoding_Rules) X.509 certificates. Some phones don't support certificates that have a public modulus of 4096 bits. The CAcert root CA has such a 4096 bits modulus.

To convert PEM formatted certificates to DER format one can use `openssl`:
```
$ openssl x509 -inform PEM -in cert.pem -outform DER -out cert.der
```

Starting with version 0.3.1 NokiCert can directly use PEM formatted certificates, so there is no need to convert to DER.

## Is there a command line version? ##
Yes, there is. Run the class `net.tuxed.nokicert.NokiCertMain`.

## What are the requirements? ##
You need to have a Bluetooth adapter supported by your operating system.

## What phones are supported? ##
I would say most Nokia S40 phones (both using little endian and big endian
CPUs) should be supported. See SupportedPhones for an up to date list of
phones.

## I installed a certificate but it is not added to the list or the phone says it is broken, what's up? ##
Some (older) phones do not support certificates with a modulus of 4096 bits.
Try to install a certificate with a smaller modulus (i.e. 1024 bits) and see
if that works.

## Does this application support the Symbian/S60 platform? ##
I don't know, I don't have access to such a device.

## I get Java back traces on Linux, what is up? ##
The BlueCove library is packaged a bit strange. It requires `libbluetooth.so`
which usually only part of the development package of the Bluetooth library
(package `libbluetooth-dev` on Debian/Ubuntu, `bluez-libs-devel` on Fedora). Installing this will make it work.

## Something went terribly wrong, your program ate my cat! ##
As the GPL license states, you used this program without any warranty!

## I'm running on Windows 64 bit and it doesn't work?! ##
Install the 32 bit Java Runtime Environment (JRE).

## How do I create a signed MIDlet suite? ##
Software like [Eclipse MTJ](http://www.eclipse.org/dsdp/mtj/) or [NetBeans Java ME](http://netbeans.org/features/javame/) can do that for you when creating Jave ME applications. If you prefer Ant and the command line you can take a look at [MIDletSuiteCreator](http://code.google.com/p/nfcip-java/downloads/list).

## Do you have an example signed MIDlet suite? ##
Yes. If you install the [CAcert certificate](http://www.cacert.org/index.php?id=3) in your phone, you can try the NokiCert Demo MIDlet suite that can be found in the [Downloads](http://code.google.com/p/nokicert/downloads/list) section. It is signed by a (already revoked) code signing certificate I obtained through CAcert. However, Nokia phones don't check the revocation list so it keeps working :)

To see that it is signed and the signature accepted by the phone look at the `Details` of the MIDlet suite. It should say `Certificate: yes` in the scroll down list of details.