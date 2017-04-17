package mo.serialization;

import mo.lma.AppState;

import java.io.File;
import java.io.OutputStream;

public interface Serializer {

    public String getVectorEncoding(AppState appState);

    public void write(AppState appState, File path);

    public AppState read(File path);
}
