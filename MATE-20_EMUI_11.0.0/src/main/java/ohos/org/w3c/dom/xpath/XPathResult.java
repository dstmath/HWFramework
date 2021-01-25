package ohos.org.w3c.dom.xpath;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;

public interface XPathResult {
    public static final short ANY_TYPE = 0;
    public static final short ANY_UNORDERED_NODE_TYPE = 8;
    public static final short BOOLEAN_TYPE = 3;
    public static final short FIRST_ORDERED_NODE_TYPE = 9;
    public static final short NUMBER_TYPE = 1;
    public static final short ORDERED_NODE_ITERATOR_TYPE = 5;
    public static final short ORDERED_NODE_SNAPSHOT_TYPE = 7;
    public static final short STRING_TYPE = 2;
    public static final short UNORDERED_NODE_ITERATOR_TYPE = 4;
    public static final short UNORDERED_NODE_SNAPSHOT_TYPE = 6;

    boolean getBooleanValue() throws XPathException;

    boolean getInvalidIteratorState();

    double getNumberValue() throws XPathException;

    short getResultType();

    Node getSingleNodeValue() throws XPathException;

    int getSnapshotLength() throws XPathException;

    String getStringValue() throws XPathException;

    Node iterateNext() throws XPathException, DOMException;

    Node snapshotItem(int i) throws XPathException;
}
