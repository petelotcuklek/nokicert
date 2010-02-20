#!/bin/sh
if [ -z "$1" ]
then
	VERSION=1.0
else
	VERSION=$1
fi

# Relevant documents for src/bin package
SRC_DOCS="FAQ SupportedPhones Configuration BuildInstructions ChangeLog"
BIN_DOCS="FAQ SupportedPhones Configuration ChangeLog"

# BlueCove
BLUECOVE_URL=http://snapshot.bluecove.org/distribution/download/2.1.1-SNAPSHOT/2.1.1-SNAPSHOT.60/bluecove-2.1.1-SNAPSHOT.jar
BLUECOVE_GPL_URL=http://snapshot.bluecove.org/distribution/download/2.1.1-SNAPSHOT/2.1.1-SNAPSHOT.60/bluecove-gpl-2.1.1-SNAPSHOT.jar
BLUECOVE_BLUEZ_URL=http://snapshot.bluecove.org/distribution/download/2.1.1-SNAPSHOT/2.1.1-SNAPSHOT.60/bluecove-bluez-2.1.1-SNAPSHOT.jar

# SWT
SWT_WIN32_URL=http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/eclipse/downloads/drops/R-3.5.1-200909170800/swt-3.5.1-win32-win32-x86.zip
SWT_LINUX32_URL=http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/eclipse/downloads/drops/R-3.5.1-200909170800/swt-3.5.1-gtk-linux-x86.zip
SWT_LINUX64_URL=http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/eclipse/downloads/drops/R-3.5.1-200909170800/swt-3.5.1-gtk-linux-x86_64.zip
SWT_MAC_URL=http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/eclipse/downloads/drops/R-3.5.1-200909170800/swt-3.5.1-cocoa-macosx.zip

# Base64 Java Library
B64_URL=http://downloads.sourceforge.net/iharder/Base64-v2.3.7.zip

BLUECOVE_FILE=`basename ${BLUECOVE_URL}`
BLUECOVE_GPL_FILE=`basename ${BLUECOVE_GPL_URL}`
BLUECOVE_BLUEZ_FILE=`basename ${BLUECOVE_BLUEZ_URL}`

SWT_WIN32_FILE=`basename ${SWT_WIN32_URL}`
SWT_LINUX32_FILE=`basename ${SWT_LINUX32_URL}`
SWT_LINUX64_FILE=`basename ${SWT_LINUX64_URL}`
SWT_MAC_FILE=`basename ${SWT_MAC_URL}`

B64_FILE=`basename ${B64_URL}`

# For building it doesn't matter what platform of SWT/Bluecove is 
# used, we just use the WIN32 swt for compiling. For packaging we need
# to include the correct SWT and optionally Bluecove GPL/Bluez libraries

rm -rf /tmp/pkg_nokicert
mkdir -p /tmp/pkg_nokicert/nokicert-${VERSION}-bin
cp NokiCert.sh NokiCert.cmd /tmp/pkg_nokicert/nokicert-${VERSION}-bin
cd /tmp/pkg_nokicert
mkdir lib/
cd lib/

# Fetch all files
curl -O ${BLUECOVE_URL}
curl -O ${BLUECOVE_GPL_URL}
curl -O ${BLUECOVE_BLUEZ_URL}

mv ${BLUECOVE_FILE} bluecove.jar
mv ${BLUECOVE_GPL_FILE} bluecove-gpl.jar
mv ${BLUECOVE_BLUEZ_FILE} bluecove-bluez.jar

curl -O ${SWT_WIN32_URL}
curl -O ${SWT_LINUX32_URL}
curl -O ${SWT_LINUX64_URL}
curl -O ${SWT_MAC_URL}

echo "curl -O ${B64_URL}"
curl -L -O ${B64_URL}

# unpack one of them for building, doesn't matter which one
unzip ${SWT_WIN32_FILE} swt.jar

# unpack Base64 lib
unzip ${B64_FILE}
cp `basename ${B64_FILE} .zip`/Base64.java Base64.java
cd ../

# Wiki
svn export http://nokicert.googlecode.com/svn/wiki wiki-${VERSION}

# NokiCert
svn export http://nokicert.googlecode.com/svn/trunk/ nokicert-${VERSION}
cd nokicert-${VERSION}
# add relevant docs to source package
mkdir -p docs/
for f in $SRC_DOCS
do
	fold -s -w80 ../wiki-${VERSION}/$f.wiki > docs/$f
done
cd ..

zip -r nokicert-${VERSION}-src.zip nokicert-${VERSION}

cd nokicert-${VERSION}
# add Base64 java class in the project
mkdir -p src/net/sourceforge/iharder
echo "package net.sourceforge.iharder;" >src/net/sourceforge/iharder/Base64.java
cat ../lib/Base64.java >> src/net/sourceforge/iharder/Base64.java
rm ../lib/Base64.java
ant doc dist -Dversion=${VERSION} -Dbase64.jar=. -Dbluecove.jar=../lib/bluecove.jar -Dswt.jar=../lib/swt.jar
cd ..

# Packaging
mkdir -p nokicert-${VERSION}-bin/

cd nokicert-${VERSION}-bin
cp ../lib/bluecove.jar .
cp ../nokicert-${VERSION}/dist/nokicert-${VERSION}.jar nokicert.jar

# add relevant docs to binary package
cp ../nokicert-${VERSION}/misc/README.binary README
mkdir -p docs/
for f in $BIN_DOCS
do
        fold -s -w80 ../wiki-${VERSION}/$f.wiki > docs/$f
done

# Win32
mkdir -p win32
cd win32
unzip ../../lib/${SWT_WIN32_FILE} swt.jar
cd ..

# Mac
mkdir -p mac
cd mac
unzip ../../lib/${SWT_MAC_FILE} swt.jar
cd ..

# Linux-i686
mkdir -p linux-i686
cd linux-i686
cp ../../lib/bluecove-gpl.jar .
cp ../../lib/bluecove-bluez.jar .
unzip ../../lib/${SWT_LINUX32_FILE} swt.jar
cd ..

# Linux-x86_64
mkdir -p linux-x86_64
cd linux-x86_64
cp ../../lib/bluecove-gpl.jar .
cp ../../lib/bluecove-bluez.jar .
unzip ../../lib/${SWT_LINUX64_FILE} swt.jar
cd ..

cd ..

# create the binary zip
zip -r nokicert-${VERSION}-bin.zip nokicert-${VERSION}-bin

