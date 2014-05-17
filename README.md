Mangapp
======

![alt text](https://raw.github.com/Vrael/Mangapp/master/Mangapp.png "Mangapp print screen")

#Overview
Mangapp is an Android application where you can read your favourites mangas. 

This application doesn't contain any manga by default. All mangas are loaded from public internet sources and they can be consulted from any browser. So you will need to use internet for a full experience. 

This app will search mangas instead of you and then it will display you them easily.

Mangapp is completely public and free. 

#Features

* A big library with many different mangas
* A section with latest chapters
* A history to resume the read from last point
* Special concern in a fast load for pages to guarantee the best experience

#Installation

Google Play:

![alt text](https://raw.github.com/Vrael/Mangapp/master/google_play.png "Mangapp in google play")

Github:

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
