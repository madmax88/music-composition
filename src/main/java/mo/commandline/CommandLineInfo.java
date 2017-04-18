package mo.commandline;

import java.io.File;

public class CommandLineInfo {
    public enum Mode {SAMPLE, TRAIN};

    private Mode mode;
    private File inputLocation, outputLocation;

    // either the size of the network or the size to sample
    private int size;

    public CommandLineInfo(Mode mode, File inputLocation, File outputLocation, int size) {
        this.mode = mode;
        this.inputLocation = inputLocation;
        this.outputLocation = outputLocation;
        this.size = size;
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

    public int getSize() {
        return size;
    }
}
