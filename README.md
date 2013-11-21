eManga
======

![alt text](https://raw.github.com/Vrael/eManga/master/eManga.png "eManga print screen")

#Overview
eManga is an Android application where you can read your favourites mangas. 

This application doesn't contains any manga by default. All mangas are get from public internet sources and they can be consulted from any browser. So you will need to use internet for a full experience. 

This app will search instead you the mangas and then it will show you easily the mangas.

eManga is completely public and free. 

#Features

* A big library with many differents mangas
* A section with latest chapters
* A function history for you will can continue with a stop read
* Special concern in a fast load for pages to guarantee the best experience

#Installation

Before to start, you will need download the [dependencies](https://github.com/Vrael/eManga#dependencies). See in dependencies section.

1. Clone the repository (open a console and write)
```console
git clone https://github.com/Vrael/eManga.git
```

2. Move to eManga folder
```console
cd <path_to_folder>/eManga
```

3. Compile the source code with maven
```maven
mvn install
```

4. Connect your mobile or Android emulator and run
```mvn
mvn android:deploy
```

* You could recompile and redeploy all at the same time with
```
mvn clean install android:redeploy
```

#Dependencies
1. [Maven 3.1.1 or upper](http://maven.apache.org/download.cgi)
2. [Maven Android SDK Deployer](https://github.com/mosabua/maven-android-sdk-deployer)

#TODOs:
* settings options activity
* the possibility to go back to precedent pages the first time a chapter is open
* add more internet mangas sources
* improve the luncher icon
* support for multilanguage

#Notes
This application is under development yet. So any issue or problem is welcome. In the same way, anyone who wants to participate in the development and add new great and amazing ideas is invited too.
