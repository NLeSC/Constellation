package ibis.constellation;

import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

import java.io.IOException;

public interface ObjectData {

    public void writeData(WriteMessage m) throws IOException;

    public void readData(ReadMessage m) throws IOException;

}
