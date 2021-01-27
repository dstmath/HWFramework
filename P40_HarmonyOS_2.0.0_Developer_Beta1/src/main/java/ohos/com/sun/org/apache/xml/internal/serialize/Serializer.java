package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DocumentHandler;

public interface Serializer {
    ContentHandler asContentHandler() throws IOException;

    DOMSerializer asDOMSerializer() throws IOException;

    DocumentHandler asDocumentHandler() throws IOException;

    void setOutputByteStream(OutputStream outputStream);

    void setOutputCharStream(Writer writer);

    void setOutputFormat(OutputFormat outputFormat);
}
