package mo.lma;

import java.io.File;
import java.io.FileFilter;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.Scanner;

public class App {

    static long maxLength = 1024;
    static File[] songs;
   
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.print("Enter a path to train from: ");

        Scanner scanner = new Scanner(System.in);
        String directoryPath = scanner.nextLine();

        findMaxFinalLength(directoryPath);

        // the length will never require a long to store it, for ABC files.
        ABCIterator it = new ABCIterator(songs, (int) maxLength);

        int numOut = it.totalOutcomes();
        int lstmLayerSize = 200;
        int tbpttLength = 50;

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
                .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
                .pretrain(false).backprop(true)
                .build();

        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();

        DataSet ds = it.next();
        network.fit(ds);

        System.out.println(ds.getFeatures());
    }
    
    /**
     * Finds the maximum input file length and stores it in maxLength.
     * @param directory 
     */
    private static void findMaxFinalLength(String directory)
    {
        File dir = new File(directory);
        FileFilter getABCFiles = new FileFilter() {

            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith(".abc");
            }
        };
        songs = dir.listFiles(getABCFiles);
        maxLength = 0;
        for(File song : songs)
        {
            maxLength = (song.length() > maxLength) ? song.length() : maxLength;
        }
    }
}
