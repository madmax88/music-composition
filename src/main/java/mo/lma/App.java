package mo.lma;

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

    public static void main(String[] args) throws IOException, InterruptedException {
        INDArray input = Nd4j.zeros(4, 2);
        INDArray labels = Nd4j.zeros(4, 2);
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
}
