package org.apache.xpath.axes;

import org.apache.xml.dtm.DTMManager;
import org.apache.xpath.NodeSetDTM;

public class RTFIterator extends NodeSetDTM {
    static final long serialVersionUID = 7658117366258528996L;

    public RTFIterator(int root, DTMManager manager) {
        super(root, manager);
    }
}
