package mo.serialization;

import mo.lma.ABCIterator;
import mo.lma.CharacterSampler;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONWriter;

/**
 *
 * @author Alexander
 */
public class AppState
{

    private MultiLayerNetwork network;
    private CharacterSampler sampler;
    private ABCIterator iterator;
    private int epoch;

    /**
     * This Class stores the state of the app during training and can restore it
     *
     * @param network The current network
     * @param sampler The character sampler
     * @param iterator
     * @param epoch
     */
    public AppState(MultiLayerNetwork network, CharacterSampler sampler,
            ABCIterator iterator, int epoch)
    {
        this.network = network;
        this.sampler = sampler;
        this.iterator = iterator;
    }

    public MultiLayerNetwork getNetwork()
    {
        return network;
    }

    public CharacterSampler getSampler()
    {
        return sampler;
    }

    public ABCIterator getIterator()
    {
        return iterator;
    }

    public int getEpoch()
    {
        return epoch;
    }

    public void write(String file)
    {
        Gson gson = new Gson();
        gson.toJson(this, AppState.class);
        try
        {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.print(gson.toJson(this, AppState.class));
            writer.close();
        } catch (IOException e)
        {
        }

    }

    public void read(String file)
    {
        Gson gson = new Gson();
        try
        {
            Scanner reader = new Scanner(new File(file));
            StringBuilder string = new StringBuilder(1024);
            String line = reader.nextLine();
            for (;reader.hasNext(); line = reader.nextLine())
            {
                string.append(line + "\n");
            }
            AppState state = gson.fromJson(string.toString(), AppState.class);
            network = state.network;
            sampler = state.sampler;
            iterator = state.iterator;
            epoch = state.epoch;
        } catch (FileNotFoundException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
}
