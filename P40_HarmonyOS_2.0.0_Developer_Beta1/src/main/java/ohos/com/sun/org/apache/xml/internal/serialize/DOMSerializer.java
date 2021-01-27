package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.IOException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.Element;

public interface DOMSerializer {
    void serialize(Document document) throws IOException;

    void serialize(DocumentFragment documentFragment) throws IOException;

    void serialize(Element element) throws IOException;
}
