
# The Migrate SDK.

Its possible to work with the migrate SDK source repositories or the 
binary SDK:

- Binary SDK:

You can obtain the Migrate binary SDK on the migate github wiki page:

https://github.com/wileyenterpriseandroid/migrate/wiki

Specificaly on the migrate releases page:

https://github.com/wileyenterpriseandroid/migrate-sdk/releases/

## Getting the code

If you, OPTIONALLY, choose to work with the code checkout the repos:

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

