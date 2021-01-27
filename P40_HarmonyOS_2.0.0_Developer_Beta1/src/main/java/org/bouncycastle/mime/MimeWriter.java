package org.bouncycastle.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class MimeWriter {
    protected final Headers headers;

    protected MimeWriter(Headers headers2) {
        this.headers = headers2;
    }

    protected static List<String> mapToLines(Map<String, String> map) {
        ArrayList arrayList = new ArrayList(map.size());
        for (String str : map.keySet()) {
            arrayList.add(str + ": " + map.get(str));
        }
        return arrayList;
    }

    public abstract OutputStream getContentStream() throws IOException;

    public Headers getHeaders() {
        return this.headers;
    }
}
