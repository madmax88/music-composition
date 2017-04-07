# music-composition 
-----------------------------

A bot to auto-generate music.

## Building
To build, run `mvn package`.  

Tests can be run with `mvn test`.

Finally, the generated jar can be found in `target/<app-name-and-version>`.

## Running

I have encountered some issues with the classpath. This build does not produce a fat jar with all of the necessary dependencies included, simply because there are many and it takes too much time to build and too much space to store. To make sure the jvm can resolve the classes at runtime, you can run `mvn dependency:build-classpath` and place the output into some environmental variable, say `cp`. Then, launch Java with `java -cp "$cp:target/<app-name>" <other-args>` 

## Note
Before committing, run `mvn clean` to remove build files from the working directory. 

 
