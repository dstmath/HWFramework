package android.filterfw.io;

import android.content.Context;
import android.filterfw.core.FilterGraph;
import android.filterfw.core.KeyValueMap;
import android.provider.DocumentsContract.Document;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

public abstract class GraphReader {
    protected KeyValueMap mReferences;

    public abstract FilterGraph readGraphString(String str) throws GraphIOException;

    public abstract KeyValueMap readKeyValueAssignments(String str) throws GraphIOException;

    public GraphReader() {
        this.mReferences = new KeyValueMap();
    }

    public FilterGraph readGraphResource(Context context, int resourceId) throws GraphIOException {
        InputStreamReader reader = new InputStreamReader(context.getResources().openRawResource(resourceId));
        StringWriter writer = new StringWriter();
        char[] buffer = new char[Document.FLAG_SUPPORTS_REMOVE];
        while (true) {
            try {
                int bytesRead = reader.read(buffer, 0, Document.FLAG_SUPPORTS_REMOVE);
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
