package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import ohos.org.w3c.dom.Node;

public interface DOMSerializer {
    void serialize(Node node) throws IOException;
}
