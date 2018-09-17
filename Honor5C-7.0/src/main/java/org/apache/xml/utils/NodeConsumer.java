package org.apache.xml.utils;

import org.w3c.dom.Node;

public interface NodeConsumer {
    void setOriginatingNode(Node node);
}
