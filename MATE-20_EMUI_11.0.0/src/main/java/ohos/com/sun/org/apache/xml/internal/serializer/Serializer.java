package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;
import ohos.org.xml.sax.ContentHandler;

public interface Serializer {
    ContentHandler asContentHandler() throws IOException;

    DOMSerializer asDOMSerializer() throws IOException;

    Properties getOutputFormat();

    OutputStream getOutputStream();

    Writer getWriter();

    boolean reset();

    void setOutputFormat(Properties properties);

    void setOutputStream(OutputStream outputStream);

    void setWriter(Writer writer);
}
