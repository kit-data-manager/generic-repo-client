# Generic Repository Client

[![Build Status](https://api.travis-ci.org/kit-data-manager/generic-repo-client.png?branch=master)](https://travis-ci.org/kit-data-manager/generic-repo-client)

The Generic Repository Client is a command line tool that can be used to access repositories based on KIT Data Manager. It supports basic operations like data ingest, download and listing of digital objects. Furthermore, the creation of basic administrative metadata elements, e.g. studies, investigations and organization units, is supported. 

## How to build

In order to build the Generic Repository Client you'll need:

* Java SE Development Kit 8 or higher
* Apache Maven 3
* Build of the [KIT Data Manager 1.5 base](https://github.com/kit-data-manager/base) project

After building KIT Data Manager 1.5 base, change to the folder where the sources are located, /home/user/generic-repo-client/. In the file 'src/main/assemble/filter.release.properties' you'll find the default settings suggested while initializing the client before using it the first time. Please modify the content of this file according to your needs or leave it unchanged. Afterwards, just call:

```
user@localhost:/home/user/generic-repo-client/$ mvn assembly:assembly
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building GenericRepoClient 1.5.1
[INFO] ------------------------------------------------------------------------
[...]
user@localhost:/home/user/generic-repo-client/$
```

As soon as the assembly process has finished there will be a file named `GenericRepoClient-1.5-release.zip` located at /home/user/generic-repo-client/zip, which is the distribution package of the client containing everything you need to launch the tool. Extract the zip file to a directory of your choice and refer to the contained manual for further instructions.

## More Information

* [Project homepage](http://datamanager.kit.edu/index.php/kit-data-manager)
* [Manual](http://datamanager.kit.edu/dama/manual/index.html)

## License

The Generic Repository Client is licensed under the Apache License, Version 2.0.


