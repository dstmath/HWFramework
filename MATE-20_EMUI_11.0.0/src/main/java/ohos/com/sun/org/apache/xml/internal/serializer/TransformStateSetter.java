package ohos.com.sun.org.apache.xml.internal.serializer;

import ohos.javax.xml.transform.Transformer;
import ohos.org.w3c.dom.Node;

public interface TransformStateSetter {
    void resetState(Transformer transformer);

    void setCurrentNode(Node node);
}
