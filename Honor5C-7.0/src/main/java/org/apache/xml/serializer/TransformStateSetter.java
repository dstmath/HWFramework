package org.apache.xml.serializer;

import javax.xml.transform.Transformer;
import org.w3c.dom.Node;

public interface TransformStateSetter {
    void resetState(Transformer transformer);

    void setCurrentNode(Node node);
}
