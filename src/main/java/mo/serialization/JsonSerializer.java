package mo.serialization;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import mo.lma.AppState;
import org.deeplearning4j.util.ModelSerializer;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Scanner;

public class JsonSerializer implements Serializer {
    Type CharMap = new TypeToken<HashMap<Character, Integer>>(){}.getType();

    @Override
    public String getVectorEncoding(AppState appState) {
        Gson gson = new Gson();

        return gson.toJson(appState.getCharacterMap(), CharMap.getClass());
    }

    @Override
    public void write(AppState appState, File path) {
        if (!path.exists() && !path.mkdir()) {
            throw new IllegalArgumentException("Error: path must be a writeable directory.");
        } else if (! path.isDirectory()) {
            throw new IllegalArgumentException("Error: path must be a directory.");
        }

        // cm is for character map
        try (PrintWriter writer = new PrintWriter(new File(path.getAbsolutePath(), "cm.json"))) {
            Gson gson = new Gson();
            writer.print(gson.toJson(appState.getCharacterMap(), CharMap));
            writer.close();
            ModelSerializer.writeModel(appState.getNetwork(), new File(path.getAbsolutePath(), "net.net").getAbsoluteFile(), true);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Error: path must be writeable.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Exception in write() that should never have happened...panicking.");
            System.exit(1);
        }
    }

    @Override
    public AppState read(File path) {
        Gson gson = new Gson();
        AppState appState = null;
        HashMap<Character, Integer> characterMap = null;

        if (! path.isDirectory()) {
            throw new IllegalArgumentException("Error: path must be a directory.");
        } else if (! new File(path.getAbsolutePath(), "cm.json").exists()) {
            throw new IllegalArgumentException("Error: bad storage directory (cm.json does not exist.)");
        } else if (! new File(path.getAbsolutePath(), "net.net").exists()) {
            throw new IllegalArgumentException("Error: bad storage directory (net.net does not exist.)");
        }

        try {
            Scanner reader = new Scanner(new File(path, "cm.json"));
            StringBuilder string = new StringBuilder(1024);
            String line = reader.nextLine();
            for (;reader.hasNext(); line = reader.nextLine())
            {
                string.append(line + "\n");
            }
            characterMap = gson.fromJson(string.toString(), CharMap);
            ModelSerializer.restoreMultiLayerNetwork(new File(path, "net.net").getAbsoluteFile());
        } catch (FileNotFoundException ex) {
            System.err.println("Panicking: ");
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Panicking: ");
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        return appState;
    }
}
