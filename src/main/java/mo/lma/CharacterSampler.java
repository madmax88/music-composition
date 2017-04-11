package mo.lma;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class CharacterSampler {
    private HashMap<Character, Integer> characterMap;
    private ArrayList<Character> characterIndices;
    private MultiLayerNetwork network;

    public CharacterSampler(HashMap<Character, Integer> characterMap,
                            ArrayList<Character> characterIndices,
                            MultiLayerNetwork network) {
        this.characterMap = characterMap;
        this.characterIndices = characterIndices;
        this.network = network;
    }

    public int charToIndex(char c) {
        return characterMap.get(c);
    }

    public char indexToChar(int index) {
        if (index >= characterIndices.size() || index < 0)
            throw new NoSuchElementException();

        return characterIndices.get(index);
    }

    public String sampleCharacters(String initialization, int toSample) {
        INDArray input = Nd4j.zeros(1, characterIndices.size(), initialization.length());

        char[] init = initialization.toCharArray();
        for (int i = 0; i < init.length; i++) {
            input.putScalar(new int[] {0, charToIndex(init[i]), i}, 1.0f);
        }

        // since strings are copy on write :-)
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(initialization);

        network.rnnClearPreviousState();
        INDArray output = network.rnnTimeStep(input);
        output = output.tensorAlongDimension(output.size(2) - 1, 1, 0);

        for (int i = 0; i < toSample; i++) {
            INDArray nextInput = Nd4j.zeros(1, characterIndices.size());

            double[] distribution = new double[characterIndices.size()];

            for (int k = 0; k < distribution.length; k++)
                distribution[k] = output.getDouble(0, k);

            int sample = getSampleChar(distribution);
            nextInput.putScalar(new int[] {0, sample}, 1.0f);
            stringBuilder.append(indexToChar(sample));

            output = network.rnnTimeStep(nextInput);
        }

        return stringBuilder.toString();
    }

    public int getSampleChar(double[] distribution) {
        double d = Math.random();
        double sum = 0;

        for (int i = 0; i < distribution.length; i++) {
            sum += distribution[i];

            if (d <= sum) return i;
        }

        throw new IllegalArgumentException("Distribution is not valid.");
    }

}
