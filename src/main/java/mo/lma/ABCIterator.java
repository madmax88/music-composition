package mo.lma;


import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by madmax on 4/9/17.
 */
public class ABCIterator implements DataSetIterator {
    private ArrayList<Integer>[] exampleDataSets;
    private HashMap<Character, Integer> characterMap;
    private int currentDataSet;
    private int charIdxNumber;
    private int exampleLength;

    // exampleLength should be the maximum length of each file.
    // the remaining portion of files < the maximum length must be spaces.
    public ABCIterator(File[] locations, int exampleLength) throws IOException {
        exampleDataSets = new ArrayList[locations.length];
        currentDataSet = 0;
        charIdxNumber = 0;
        this.exampleLength = exampleLength;

        // create a new data set for each File in the training example
        for (int i = 0; i < exampleDataSets.length; i++) {
            exampleDataSets[i] = new ArrayList<Integer>();

            loadDataFromFile(locations[i]);
            currentDataSet++;
        }

        // make sure we reset our dataset
        currentDataSet = 0;
    }

    public void loadDataFromFile(File f) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(f));

        long length = 0;
        String currentLine = null;
        while ((currentLine = bufferedReader.readLine()) != null) {
            length += currentLine.length();
            for (int idx = 0; idx < currentLine.length(); idx++) {
                if (! characterMap.containsKey(currentLine.charAt(idx)))
                    characterMap.put(currentLine.charAt(idx), charIdxNumber++);

                exampleDataSets[currentDataSet].add(characterMap.get(
                        currentLine.charAt(idx)));
            }
        }
        for(; length < App.maxLength; length++)
            exampleDataSets[currentDataSet].add(characterMap.get(' '));
    }

    public HashMap<Character, Integer> getCharacterSet() {
        return characterMap;
    }

    public DataSet next(int i) {
        if (i < 0 || i > exampleDataSets.length)
            throw new NoSuchElementException();

        int batchSize = exampleDataSets[i].size();

        INDArray inputs = Nd4j.create(new int[]{batchSize, characterMap.size(), exampleLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{batchSize, characterMap.size(), exampleLength}, 'f');

        for (int k = 0; k < batchSize; k++) {

            int c = 0;
            for (int j = 1; j < exampleLength; j++, c++) {

            }
        }

        return null;
    }

    public int totalExamples() {
        return 0;
    }

    public int inputColumns() {
        return 0;
    }

    public int totalOutcomes() {
        return 0;
    }

    public boolean resetSupported() {
        return false;
    }

    public boolean asyncSupported() {
        return false;
    }

    public void reset() {

    }

    public int batch() {
        return 0;
    }

    public int cursor() {
        return 0;
    }

    public int numExamples() {
        return 0;
    }

    public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) {

    }

    public DataSetPreProcessor getPreProcessor() {
        return null;
    }

    public List<String> getLabels() {
        return null;
    }

    public boolean hasNext() {
        return false;
    }

    public DataSet next() {
        return null;
    }

    public void remove() {

    }
}