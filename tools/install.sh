#!/bin/bash

function scriptDir() {
  local dn=`dirname $0`
  local abs=`cd $dn; pwd`
  echo $abs
}

scriptDir=`scriptDir`

if [ -z "$TOMCAT_HOME" ] ; then
echo Please set TOMCAT_HOME
exit 0
fi

cd $TOMCAT_HOME
./bin/shutdown.sh

rm -rf ./webapps/migrate/
rm ./webapps/migrate.war

cp $scriptDir/migrate.war ./webapps

./bin/startup.sh

echo Uninstalling, then installing migate client apk, please make sure that the android emulator is running...

#adb uninstall com.migrate > /dev/null 2>&1
adb install -r ${scriptDir}/migrate-client.apk

#adb uninstall com.migrate.browser > /dev/null 2>&1
adb install -r ${scriptDir}/migrate-browser.apk
