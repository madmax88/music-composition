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
        characterMap = new HashMap<Character, Integer>();

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

    // returns the next i examples from the dataset
    public DataSet next(int i) {
        if (i < 0 || i + currentDataSet >= exampleDataSets.length)
            throw new NoSuchElementException();

        // dataSets stores the number of files left to consume examples from
        int dataSets = Math.min(i, exampleDataSets.length - currentDataSet);

        INDArray inputs = Nd4j.create(new int[]{dataSets, characterMap.size(), exampleLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{dataSets, characterMap.size(), exampleLength}, 'f');

        for (int k = 0; k < dataSets; k++) {

            int c = 0;
            int currIdx = exampleDataSets[k + currentDataSet].get(0);
            for (int j = 1; j < exampleLength; j++, c++) {
                int nexIdx = exampleDataSets[k + currentDataSet].get(j);
                inputs.putScalar(new int[] {k, currIdx, c}, 1.0);
                labels.putScalar(new int[] {k, nexIdx, c}, 1.0);
                currIdx = nexIdx;
            }
        }

        currentDataSet += i;
        return new DataSet(inputs, labels);
    }

    public int totalExamples() {
        return exampleDataSets.length;
    }

    public int inputColumns() {
        return characterMap.size();
    }

    public int totalOutcomes() {
        return characterMap.size();
    }

    public boolean resetSupported() {
        return true;
    }

    public boolean asyncSupported() {
        return false;
    }

    public void reset() {
        currentDataSet = 0;
    }

    public int batch() {
        return exampleDataSets.length;
    }

    public int cursor() {
        return currentDataSet;
    }

    public int numExamples() {
        return totalExamples();
    }

    public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) {
        throw new UnsupportedOperationException();
    }

    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException();
    }

    public List<String> getLabels() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return currentDataSet < exampleDataSets.length;
    }

    public DataSet next() {
        return next(batch());
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}