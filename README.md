
# The Migrate SDK.

Its possible to work with the binary migrate SDK or the migrate source repositories:

- Binary SDK (Recommended):

You can obtain the Migrate binary SDK on the migate github wiki page:

https://github.com/wileyenterpriseandroid/migrate/wiki

Specifically on the migrate releases page:

https://github.com/wileyenterpriseandroid/migrate-sdk/releases/

The best way to get started working with the SDK is to explore the MigrateContacts
example from the book samples repository:

git clone https://github.com/wileyenterpriseandroid/Examples.git

Follow the instructions for using the example contained in MigrateContacts/README, which
contains detailed information about configuring the SDK and importing the MigrateContacts
example into Eclipse.

For comprehensive background and explanation, we recommend that you buy a copy of
Enterprise Android (@Amazon.com: search for "Enterprise Android"):

http://www.amazon.com/Enterprise-Android-Programming-Database-Applications-ebook/dp/B00FX89KXM/ref=sr_1_1?ie=UTF8&qid=1403662172&sr=8-1&keywords=enterprise+android

----------

If you have any questions about the migrate SDK or the Enterprise Android Examples,
please post them here:

http://p2p.wrox.com/book-enterprise-android-programming-android-database-applications-enterprise-751/

## Getting the code

If you, OPTIONALLY, choose to work with the migrate sdk and service code checkout the repos:

migrate-sdk
migrate
migrate-client
EnterpriseAndroidExamples

Note: We dont recommend that you try to work with the code until you 
have had some success working with the binary version of the SDK.

As listed on the following page:
https://github.com/wileyenterpriseandroid

The best way to download this code, along with the examples that support it,
is by using the Repo tool.  Get it like this:
```
curl https://dl-ssl.google.com/dl/googlesource/git-repo/repo > ~/bin/repo
```

Once repo is installed, download this source with the following commands:

```
repo init -u https://github.com/wileyenterpriseandroid/manifests.git
repo sync
```

You can download the source without using repo, with the following two commands:

```
git clone https://github.com/wileyenterpriseandroid/migrate-sdk.git
git clone https://github.com/wileyenterpriseandroid/Examples.git ea-examples
```

