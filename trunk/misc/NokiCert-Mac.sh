#!/bin/sh
CLASSPATH=${CLASSPATH}:bluecove.jar
CLASSPATH=${CLASSPATH}:mac/swt.jar
CLASSPATH=${CLASSPATH}:nokicert.jar

java -XstartOnFirstThread -d32 -cp ${CLASSPATH} net.tuxed.nokicert.NokiCertGUI
