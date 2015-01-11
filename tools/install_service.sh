#!/bin/bash

function scriptDir() {
  local dn=`dirname $0`
  local abs=`cd $dn; pwd`
  echo $abs
}

scriptDir=`scriptDir`

if [ -z "$CATALINA_HOME" ] ; then
echo Please set CATALINA_HOME
exit 0
fi

cd $CATALINA_HOME
./bin/shutdown.sh

rm -rf ./webapps/migrate/
rm ./webapps/migrate.war

cp $scriptDir/migrate.war ./webapps

./bin/startup.sh
