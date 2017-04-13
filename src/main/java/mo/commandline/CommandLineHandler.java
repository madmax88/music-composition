package mo.commandline;

import org.apache.commons.cli.*;

import java.io.File;

public class CommandLineHandler {

    public static CommandLineInfo handleCommandLine(String[] args) {
        Options options = new Options();

        Option help = new Option("h", "help", false, "print this message");
        options.addOption(help);

        Option mode = Option.builder().longOpt("mode")
                    .argName("mode")
                    .hasArg()
                    .desc("mode to run the program in")
                    .argName("train|sample")
                    .build();
        options.addOption(mode);

        Option trainingBooleanMode = new Option("t", "run in training mode");
        options.addOption(trainingBooleanMode);

        Option samplingBooleanMode = new Option("s", "run in sampling mode");
        options.addOption(samplingBooleanMode);

        Option output = new Option("o", "output", true, "the file to write output to");
        options.addOption(output);

        Option input = new Option("i", "input", true, "input for the mode.");
        input.setRequired(true);
        options.addOption(input);

        boolean isTraining = false;
        boolean trainingOptionSpecified = false;
        File outputLocation = null, inputLocation = null;

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            // if there is a parse error, print the problem and the usage and die.
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("musgen", options);
            System.exit(1);
        }

        // print help message
        if (cmd.hasOption("help")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("musgen", options);
            System.exit(1);
        }

        if (cmd.hasOption("mode")) {
            // make sure we weren't supplied contradicting runtime info
            if (cmd.hasOption("s") || cmd.hasOption("t")) {
                System.out.println("Error: conflicting mode arguments.");
                System.exit(1);
            }

            String modeDirective = cmd.getOptionValue("mode");

            if (modeDirective.equals("train"))
                isTraining = true;
            else if (modeDirective.equals("sample"))
                isTraining = false;
            else {
                System.out.println("Unrecognized mode option: " + modeDirective + ".");
                new HelpFormatter().printHelp("musgen", options);
                System.exit(1);
            }

            trainingOptionSpecified = true;
        }

        // check for short sampling option
        if (cmd.hasOption("s")) {
            if (cmd.hasOption("t")) {
                System.out.println("Error: conflicting mode arguments.");
                System.exit(1);
            }

            trainingOptionSpecified = true;
            isTraining = false;
        }

        // check for short training option
        if (cmd.hasOption("t")) {
            isTraining = true;
            trainingOptionSpecified = true;
        }

        // ensure that some runtime mode information is provided
        if (! trainingOptionSpecified) {
            System.out.println("Error: no mode option specified. Exiting.");
            new HelpFormatter().printHelp("musgen", options);
            System.exit(1);
        }

        // check for output location. If none is given, default to output-<timestamp> in the current directory.
        if (cmd.hasOption("o"))
            outputLocation = new File(cmd.getOptionValue("o"));
        else
            outputLocation = new File("output-" + System.currentTimeMillis());

        // see if we were given an input location
        if (cmd.hasOption("i"))
            inputLocation = new File(cmd.getOptionValue("i"));
        else {
            System.out.println("Error: must be supplied with input. Exiting.");
            new HelpFormatter().printHelp("musgen", options);
            System.exit(1);
        }

        return new CommandLineInfo(isTraining ? CommandLineInfo.Mode.TRAIN : CommandLineInfo.Mode.SAMPLE,
                                   inputLocation,
                                   outputLocation);

    }
}
