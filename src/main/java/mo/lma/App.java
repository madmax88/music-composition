package mo.lma;

import mo.commandline.CommandLineHandler;
import mo.commandline.CommandLineInfo;
import mo.serialization.JsonSerializer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class App {

    private static File[] songs;
    public static final int TIME_BACK_PROPAGATION_LENGTH = 200;
   
    public static void main(String[] args) throws IOException, InterruptedException {
        CommandLineInfo commandLineInfo = CommandLineHandler.handleCommandLine(args);

        if (commandLineInfo.getMode().equals(CommandLineInfo.Mode.SAMPLE)) {
            sampleMain(commandLineInfo);
        } else {
            trainingMain(commandLineInfo);
        }
    }

    private static void trainingMain(CommandLineInfo commandLineInfo) throws IOException {
        getFiles(commandLineInfo.getInputLocation());

        // the length will never require a long to store it, for ABC files.
        ABCIterator it = new ABCIterator(songs);

        int numOut = it.totalOutcomes();
        int lstmLayerSize = commandLineInfo.getSize();

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .learningRate(0.1)
                .rmsDecay(0.95)
                .seed(12345)
                .regularization(true)
                .l2(0.001)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.RMSPROP)
                .list()
                .layer(0, new GravesLSTM.Builder().nIn(it.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(lstmLayerSize).nOut(numOut).build())
                .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(TIME_BACK_PROPAGATION_LENGTH)
                .tBPTTBackwardLength(TIME_BACK_PROPAGATION_LENGTH)
                .pretrain(false).backprop(true)
                .build();

        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();

        CharacterSampler sampler = new CharacterSampler(it.getCharacterSet(), it.getCharacterIndicies(),
                network);

        AppState appState = new AppState(network, it.getCharacterSet());

        for (int i = 0; i < 1000; i++) {
            if (! it.hasNext())
                it.reset();

            System.out.println("Training epoch: " + i + ". ");

            DataSet ds = it.next();
            network.fit(ds);

            System.out.println("Sampled output:\n" + sampler.sampleCharacters("Gdg gBG|", 200));
            System.out.println("------------------");
        }

        new JsonSerializer().write(appState, commandLineInfo.getOutputLocation());
    }

    private static void sampleMain(CommandLineInfo commandLineInfo) {
        int sampleSize = commandLineInfo.getSize();
        AppState appState = null;

        try {
            appState = new JsonSerializer().read(commandLineInfo.getInputLocation());
        } catch (IllegalArgumentException a) {
            System.err.println("The system cannot find the path specified. Exiting.");
            System.exit(1);
        }

        Character[] charArr = new Character[appState.getCharacterMap().size()];
        appState.getCharacterMap().forEach((Character k, Integer v) -> charArr[v] = k);

        MultiLayerNetwork network = appState.getNetwork();
        CharacterSampler sampler = new CharacterSampler(appState.getCharacterMap(),
                                                        new ArrayList<Character>(Arrays.asList(charArr)),
                                                        network);

        System.out.println(sampler.sampleCharacters("G2d d2g |", sampleSize));
    }
    
    /**
     * Finds the maximum input file length and stores it in maxLength.
     * @param directory 
     */
    private static void getFiles(File directory)
    {
        FileFilter getABCFiles = new FileFilter() {

            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith(".abc");
            }
        };
        songs = directory.listFiles(getABCFiles);
    }
}
