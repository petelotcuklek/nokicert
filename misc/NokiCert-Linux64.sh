#!/bin/sh
CLASSPATH=${CLASSPATH}:bluecove.jar
CLASSPATH=${CLASSPATH}:linux64/bluecove-gpl.jar
CLASSPATH=${CLASSPATH}:linux64/bluecove-bluez.jar
CLASSPATH=${CLASSPATH}:linux64/swt.jar
CLASSPATH=${CLASSPATH}:nokicert.jar

java -cp ${CLASSPATH} net.tuxed.nokicert.NokiCertGUI
