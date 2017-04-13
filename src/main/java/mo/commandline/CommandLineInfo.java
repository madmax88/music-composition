package mo.commandline;

import sun.tools.jar.CommandLine;

import java.io.File;

public class CommandLineInfo {
    public enum Mode {SAMPLE, TRAIN};

    private Mode mode;
    private File inputLocation, outputLocation;

    public CommandLineInfo(Mode mode, File inputLocation, File outputLocation) {
        this.mode = mode;
        this.inputLocation = inputLocation;
        this.outputLocation = outputLocation;
    }

    public Mode getMode() {
        return mode;
    }

    public File getInputLocation() {
        return inputLocation;
    }

    public File getOutputLocation() {
        return outputLocation;
    }
}
