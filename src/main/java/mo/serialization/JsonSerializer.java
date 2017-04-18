package mo.serialization;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.Hash;
import mo.lma.AppState;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
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

        // we have to encode the characters themselves, because not all characters are valid JSON keys
        HashMap<String, Integer> encodedMap = new HashMap<>();

        // TODO:
        // This is crap.
        // We really ought to have an object that represents these character, i.e. a SerializableCharacter
        // that provides the proper encoding/decoding procedures.
        appState.getCharacterMap().forEach((Character key, Integer value) -> {
            if (key.equals(' '))
                encodedMap.put("SPACE", value);
            else if (key.equals('\n'))
                encodedMap.put("NEWLINE", value);
            else if (key.equals('\t'))
                encodedMap.put("TAB", value);
            else
                encodedMap.put(key.toString(), value);
        });

        // cm is for character map
        try (PrintWriter writer = new PrintWriter(new File(path.getAbsolutePath(), "cm.json"))) {
            Gson gson = new Gson();
            writer.print(gson.toJson(encodedMap));
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
        HashMap<Character, Integer> characterMap = new HashMap<>();

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
            String next = null;

            while (reader.hasNext() && (next = reader.next()) != null) {
                string.append(next);
            }

            reader.close();

            Type stringMap = new TypeToken<HashMap<String, Integer>>(){}.getType();
            HashMap<String, Integer> encodedMap = gson.fromJson(string.toString(), stringMap);

            encodedMap.forEach((String s, Integer v) -> {
                if (s.equals("SPACE"))
                    characterMap.put(' ', v);
                else if (s.equals("TAB"))
                    characterMap.put('\t', v);
                else if (s.equals("NEWLINE"))
                    characterMap.put('\n', v);
                else
                    characterMap.put(s.charAt(0), v);
            });

            MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(new File(path, "net.net").getAbsoluteFile());

            appState = new AppState(network, characterMap);
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
