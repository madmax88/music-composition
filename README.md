# music-composition 
-----------------------------

A bot to auto-generate music.

## Building
To build, run `mvn package`.  

Tests can be run with `mvn test`.

Finally, the generated jar can be found in `target/<app-name-and-version>`.

## Running

I have encountered some issues with the classpath. This build does not produce a fat jar with all of the necessary dependencies included, simply because there are many and it takes too much time to build and too much space to store. To make sure the jvm can resolve the classes at runtime, you can run `mvn dependency:build-classpath` and place the output into some environmental variable, say `cp`. Then, launch Java with `java -cp "$cp:target/<app-name>" <other-args>`

Alternatively, most IDE's seem to do the right thing.

Here are the command line flags that are supported by the program:

1. `--mode <train|sample>`: the mode to run the program in
2. `-t`: shorthand for `--mode train`
3. `-s`: shorthand for `--mode sample`
4. `--input <path>` or `-i <path>`: path to read data from
5. `--ouput <path>` or `-o <path>`: path to store data to

So, if you wanted to train a network you would run `<program-name> --mode train --input <directory-with-abc-files> --output <network-output-directory>`.
Similarly, to run a trained network, you would run `<program-name> --mode sample --input <network-output-directory>`.

## Note
Before committing, run `mvn clean` to remove build files from the working directory. 
