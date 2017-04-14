package mo.lma;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class ABCIterator implements DataSetIterator {
    // stores the sources of data sets
    private File[] dataSetSources;

    // stores a map relating characters to an index to encode in a vector
    private HashMap<Character, Integer> characterMap;

    // characters[i] = c. characterMap[c] = i.
    private ArrayList<Character> characters;

    // the current data set that we are on. For now, a data set is a file.
    private int currentDataSet;

    // the next free integer to encode a character as
    private int charIdxNumber;

    // for now, the total number of files given as training
    private int exampleLength;

    // default batch size
    public static final int DEFAULT_BATCH_SIZE = 20;

    // exampleLength should be the maximum length of each file.
    // the remaining portion of files < the maximum length must be spaces.
    public ABCIterator(File[] locations) throws IOException {
        currentDataSet = 0;
        charIdxNumber = 0;
        characterMap = new HashMap<Character, Integer>();
        characters = new ArrayList<Character>();
        dataSetSources = locations;
        exampleLength = 0;

        initVectorEncoders(dataSetSources);

        // make sure we reset our dataset
        currentDataSet = 0;
    }

    public void addCharacterEncodeInformation(char c) {
        if (! characterMap.containsKey(c)) {
            characterMap.put(c, charIdxNumber++);
            characters.add(c);
        }
    }

    public void initVectorEncoders(File[] files) throws IOException {
        int largestFileSize = 0;

        for (File f : files) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
            int fileSize = 0;

            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                currentLine += '\n';

                for (char c : currentLine.toCharArray()) {
                    addCharacterEncodeInformation(c);
                }

                fileSize += currentLine.length();
            }

            if (fileSize > largestFileSize) {
                largestFileSize = fileSize;
            }

            bufferedReader.close();
        }

        exampleLength = largestFileSize;
    }

    public ArrayList<Integer> loadDataFromFile(File f) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
        ArrayList<Integer> toReturn = new ArrayList<>();

        long length = 0;
        String currentLine;
        while ((currentLine = bufferedReader.readLine()) != null) {
            currentLine += '\n';

            length += currentLine.length();
            for (int idx = 0; idx < currentLine.length(); idx++) {
                toReturn.add(characterMap.get(currentLine.charAt(idx)));
            }
        }

        for(; length < exampleLength; length++)
            toReturn.add(characterMap.get(' '));

        return toReturn;
    }

    public HashMap<Character, Integer> getCharacterSet() {
        return characterMap;
    }

    // returns the next i examples from the dataset
    public DataSet next(int i) {
        if (i < 0 || i > dataSetSources.length || currentDataSet > dataSetSources.length)
            throw new NoSuchElementException();

        // dataSets stores the number of files left to consume examples from
        int dataSets = Math.min(dataSetSources.length - currentDataSet, i);
        ArrayList<Integer>[] exampleData = new ArrayList[dataSets];

        for (int j = 0; j < dataSets; j++) {
            try {
                exampleData[j] = loadDataFromFile(dataSetSources[currentDataSet + j]);
            } catch (IOException e) {
                System.err.println("Error: file not found.");
                return null;
            }
        }

        INDArray inputs = Nd4j.create(new int[]{dataSets, characterMap.size(), exampleLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{dataSets, characterMap.size(), exampleLength}, 'f');

        for (int k = 0; k < dataSets; k++) {
            int c = 0;
            int currIdx = exampleData[k].get(0);
            for (int j = 1; j < exampleLength; j++, c++) {
                int nexIdx = exampleData[k].get(j);
                inputs.putScalar(new int[] {k, currIdx, c}, 1.0);
                labels.putScalar(new int[] {k, nexIdx, c}, 1.0);
                currIdx = nexIdx;
            }
        }

        currentDataSet += i;
        return new DataSet(inputs, labels);
    }

    public ArrayList<Character> getCharacterIndicies() {
        return characters;
    }

    public int totalExamples() {
        return dataSetSources.length;
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
        return DEFAULT_BATCH_SIZE;
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
        return currentDataSet < dataSetSources.length - 1;
    }

    public DataSet next() {
        return next(batch());
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}