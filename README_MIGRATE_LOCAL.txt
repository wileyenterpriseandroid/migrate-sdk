Optionally run the migrate service using Tomcat:
-----

This file explains how to run and configure an instance of migrate on
your local machine, if you dont want to use the migrate instance at:

http://beta.projectmigrate.com.com/

-----

Follow the instructions below only if you want to setup your own
local migrate instance (advanced and optional):

Make sure that you have a mysql instance running with root
credentials as:

User: root
Pass: mysql

Copy the migrate.war binary to the Tomcat webapps directory:

cp $MIGRATE_SDK/migrate.war $CATALINA_HOME/webapps
$CATALINA_HOME/bin/shutdown.sh
$CATALINA_HOME/bin/startup.sh

Verify that the service is running by loading the following URL in a browser:
http://localhost:8080/

This location should display information about Migrate.

Debugging Tomcat
------

To debug problems with Tomcat, view the contents of the file:

$CATALINA_HOME/logs/catalina.out

or the log file named with the current date.  Aside from simply running the
'cat' command in a shell, you can also have the contents printed live to a
console in a shell using the tail command as follows:

tail $CATALINA_HOME/logs/catalina.out
