#!/bin/sh

# OS
OS=`uname -s`
# Hardware Platform
HW=`uname -p`

CLASSPATH=${CLASSPATH}:bluecove.jar
CLASSPATH=${CLASSPATH}:nokicert.jar

if [ $OS == "Linux" ]
	then
	
	if [ $HW == "i686" ]
	then
		# i686
		CLASSPATH=${CLASSPATH}:linux32/bluecove-gpl.jar
		CLASSPATH=${CLASSPATH}:linux32/bluecove-bluez.jar
		CLASSPATH=${CLASSPATH}:linux32/swt.jar
	elif [ $HW == "x86_64" ]
	then	
		# x86_64
		CLASSPATH=${CLASSPATH}:linux64/bluecove-gpl.jar
		CLASSPATH=${CLASSPATH}:linux64/bluecove-bluez.jar
		CLASSPATH=${CLASSPATH}:linux64/swt.jar
	fi
	java -cp ${CLASSPATH} net.tuxed.nokicert.NokiCertGUI
elif [ $OS == "Darwin" ] 
then
	CLASSPATH=${CLASSPATH}:mac/swt.jar
	java -XstartOnFirstThread -d32 -cp ${CLASSPATH} net.tuxed.nokicert.NokiCertGUI
fi

