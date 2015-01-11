#!/bin/bash

function scriptDir() {
  local dn=`dirname $0`
  local abs=`cd $dn; pwd`
  echo $abs
}

scriptDir=`scriptDir`

echo Uninstalling, then installing migate client apk, please make sure that the android emulator is running...

#adb uninstall com.migrate.browser > /dev/null 2>&1
adb install -r ${scriptDir}/migrate-browser.apk

#adb uninstall com.migrate > /dev/null 2>&1
adb install -r ${scriptDir}/migrate-client.apk
