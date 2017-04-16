package mo.serialization;

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

    public void write(String file)
    {
        Gson gson = new Gson();
        gson.toJson(this, AppState.class);
        try
        {
            PrintWriter writer = new PrintWriter(file + "/cm.json", "UTF-8");
            writer.print(gson.toJson(characterMap, CharMap));
            writer.close();
            ModelSerializer.writeModel(network, file + "/net.net", true);
        } catch (IOException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }

    public void read(String file)
    {
        Gson gson = new Gson();
        try
        {
            Scanner reader = new Scanner(new File(file + "/cm.json"));
            StringBuilder string = new StringBuilder(1024);
            String line = reader.nextLine();
            for (;reader.hasNext(); line = reader.nextLine())
            {
                string.append(line + "\n");
            }
            characterMap = gson.fromJson(string.toString(), CharMap);
            ModelSerializer.restoreMultiLayerNetwork(file + "/net.net");
        } catch (FileNotFoundException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
}
