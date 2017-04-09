package mo.lma;

import java.io.File;
import java.io.FileFilter;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;

public class App {

    static long maxLength = 1024;
   
    public static void main(String[] args) throws IOException, InterruptedException {
        INDArray input = Nd4j.zeros(4, 2);
        INDArray labels = Nd4j.zeros(4, 2);

        // [0 0] -> [1 0]
        input.putScalar(new int[] {0, 0}, 0);
        input.putScalar(new int[] {0, 1}, 0);
        labels.putScalar(new int[] {0, 0}, 1);
        labels.putScalar(new int[] {0, 1}, 0);

        // [1 0] -> [0 1]
        input.putScalar(new int[] {1, 0}, 1);
        input.putScalar(new int[] {1, 1}, 0);
        labels.putScalar(new int[] {1, 0}, 0);
        labels.putScalar(new int[] {1, 1}, 1);

        // [0 1] -> [0 1]
        input.putScalar(new int[] {2, 0}, 0);
        input.putScalar(new int[] {2, 1}, 1);
        labels.putScalar(new int[] {2, 0}, 0);
        labels.putScalar(new int[] {2, 1}, 1);

        // [1 1] -> [1 1]
        input.putScalar(new int[] {3, 0}, 1);
        input.putScalar(new int[] {3, 1}, 1);
        labels.putScalar(new int[] {3, 0}, 1);
        labels.putScalar(new int[] {3, 1}, 0);

        DataSet ds = new DataSet(input, labels);

        // define a network
       MultiLayerConfiguration xorNetworkConf =
               new NeuralNetConfiguration.Builder()
               .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
               .updater(Updater.NESTEROVS).momentum(0.9)
               .learningRate(0.1)
               .iterations(1000)
               .list(new DenseLayer.Builder()
                               .nIn(2).nOut(2)
                               .weightInit(WeightInit.DISTRIBUTION)
                               .dist(new UniformDistribution(0, 1))
                               .activation(Activation.SIGMOID).build(),
                       new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                               .activation(Activation.SOFTMAX).nIn(2).nOut(2)
                               .weightInit(WeightInit.DISTRIBUTION)
                               .dist(new UniformDistribution(0, 1))
                               .build())
               .backprop(true).build();

        MultiLayerNetwork net = new MultiLayerNetwork(xorNetworkConf);
        net.init();

        net.fit(ds);

        INDArray output = net.output(ds.getFeatureMatrix());
        System.out.println(output);
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
        File[] songs = dir.listFiles(getABCFiles);
        maxLength = 0;
        for(File song : songs)
        {
            maxLength = (song.length() > maxLength) ? song.length() : maxLength;
        }
    }
}
