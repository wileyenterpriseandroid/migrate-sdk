
# Getting started with the project migrate SDK:

You can download the Project Migrate binary SDK on the migrate github wiki page:

```
https://github.com/wileyenterpriseandroid/migrate/wiki
```

Specifically on the migrate releases page:

```
https://github.com/wileyenterpriseandroid/migrate-sdk/releases/
```

If you have not already downloaded and unpacked the latest SDK, please do so now.

Installation:
----------

The projectmigrate SDK has two major pieces:

- Client:

A client in the form of an apk that contains the migrate content provider and
sync adapter. Install the client using the included install script:

```
$SDK_DIR/install_client.sh
```

Or execute the following command lines:

```
adb install -r ./migrate-browser.apk
adb install -r ./migrate-client.apk
```


- Service:

The migrate backend service supports generic migrate synchronization persistence.
Its recommended to simply use the projectmigrate beta service at the following URL:

```
http://projectmigrate.com/
```

This url is the default when configuring a migrate client account in Android. You
dont need to do anything other than install the migrate-client.apk to use this
instance of the migrate backend.

Alternatively, you can also install the migrate server locally using the install script:

```
$SDK_DIR/install_server.sh
```

Or by copying installing apache tomcat and then copying migrate.war to:

```
$CATALINA_HOME/webapps
```

and the then restarting tomcat.


Configuration:
----------

After you have installed the SDK, configure a client account one on your device
or emulator using:

```
Settings -> Accounts/Add Account(+) -> ProjectMigrate SyncAdapter
```

Fill out the sign-in screen using the appropriate migrate url, as following:

```
http://projectmigrate.com/
```

For a local tomcat installation use:

```
http://10.0.2.2:8080/migrate/
```

Schema configuration:
----------

Any migrate based applications that you write will need to make use of a schema.
You can either create the schema using the web front end of the migrate service,
or you can generate it from a class. Please see, samples, MigrateContacts/README
for instructions on generating a migrate schema and making it available for sync
with the migrate backend service.

Samples:
----------

After you have installed and configured the projectmigrate components, the easiest
way to get started working with the SDK is to explore the SDK samples listed below:

samples/MigrateContacts

----------

Enterprise Android

For comprehensive background and explanation of projectmigrate, we recommend that you buy a copy of
Enterprise Android (@Amazon.com: search for "Enterprise Android"):

```
http://www.amazon.com/Enterprise-Android-Programming-Database-Applications-ebook/dp/B00FX89KXM/ref=sr_1_1?ie=UTF8&qid=1403662172&sr=8-1&keywords=enterprise+android
```

The book Enterprise Android also has examples that explore projectmigrate, you can download
these examples from the following location:

```
git clone https://github.com/wileyenterpriseandroid/Examples.git
```

----------

If you have any questions about the Migrate SDK or the Enterprise Android Examples,
please post them here:

```
http://p2p.wrox.com/book-enterprise-android-programming-android-database-applications-enterprise-751/
```
