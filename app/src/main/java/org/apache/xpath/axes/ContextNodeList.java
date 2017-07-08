package org.apache.xpath.axes;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public interface ContextNodeList {
    Object clone() throws CloneNotSupportedException;

    NodeIterator cloneWithReset() throws CloneNotSupportedException;

    Node getCurrentNode();

    int getCurrentPos();

    int getLast();

    boolean isFresh();

    void reset();

    void runTo(int i);

    void setCurrentPos(int i);

    void setLast(int i);

    void setShouldCacheNodes(boolean z);

    int size();
}
