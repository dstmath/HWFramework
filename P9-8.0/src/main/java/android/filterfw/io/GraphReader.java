package android.filterfw.io;

import android.content.Context;
import android.filterfw.core.FilterGraph;
import android.filterfw.core.KeyValueMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

public abstract class GraphReader {
    protected KeyValueMap mReferences = new KeyValueMap();

    public abstract FilterGraph readGraphString(String str) throws GraphIOException;

    public abstract KeyValueMap readKeyValueAssignments(String str) throws GraphIOException;

    public FilterGraph readGraphResource(Context context, int resourceId) throws GraphIOException {
        InputStreamReader reader = new InputStreamReader(context.getResources().openRawResource(resourceId));
        StringWriter writer = new StringWriter();
        char[] buffer = new char[1024];
        while (true) {
            try {
                int bytesRead = reader.read(buffer, 0, 1024);
                if (bytesRead <= 0) {
                    return readGraphString(writer.toString());
                }
                writer.write(buffer, 0, bytesRead);
            } catch (IOException e) {
                throw new RuntimeException("Could not read specified resource file!");
            }
        }
    }

    public void addReference(String name, Object object) {
        this.mReferences.put(name, object);
    }

    public void addReferencesByMap(KeyValueMap refs) {
        this.mReferences.putAll(refs);
    }

    public void addReferencesByKeysAndValues(Object... references) {
        this.mReferences.setKeyValues(references);
    }
}
