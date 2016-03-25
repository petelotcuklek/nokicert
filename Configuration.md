

NokiCert requires the Bluetooth address of the phone and the Bluetooth channel of the Nokia PC-Suite. The parameters can be set on first launch of the graphical interface for NokiCert. The configuration can also be modified by changing the `.gjokiirc` file in the user's home directory when using Linux or Mac OS X, or `gjokiirc` in the users's home directory when using Windows.

The contents of the configuration file are like this:

```
device=001122334455
channel=15
```

Device is the Bluetooth hardware address. You can find this by pressing `*#2820#` on the phone key pad (when Bluetooth is enabled)

To find the PC Suite Bluetooth Channel one can use `sdptool` on Linux, not sure how to determine it on other platforms. For the phones that were tested it was always 15, however on other (older?) phones it may be different...