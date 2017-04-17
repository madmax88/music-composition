package mo.lma;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Scanner;
import org.deeplearning4j.util.ModelSerializer;

/**
 *
 * @author Alexander
 */
public class AppState
{

    private MultiLayerNetwork network = null;
    Type CharMap = new TypeToken<HashMap<Character, Integer>>(){}.getType();
    private HashMap<Character, Integer> characterMap = null;

    /**
     * This Class stores the state of the app during training and can restore it
     *
     * @param network The current network
     * @param characterMap 
     */
    public AppState(MultiLayerNetwork network, HashMap<Character, Integer> characterMap)
    {
        this.network = network;
        this.characterMap = characterMap;
    }

    public MultiLayerNetwork getNetwork()
    {
        return network;
    }

    public HashMap<Character, Integer> getCharacterMap()
    {
        return characterMap;
    }
}
