package org.w3c.dom.ls;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface LSParserFilter {
    public static final short FILTER_ACCEPT = (short) 1;
    public static final short FILTER_INTERRUPT = (short) 4;
    public static final short FILTER_REJECT = (short) 2;
    public static final short FILTER_SKIP = (short) 3;

    short acceptNode(Node node);

    int getWhatToShow();

    short startElement(Element element);
}
