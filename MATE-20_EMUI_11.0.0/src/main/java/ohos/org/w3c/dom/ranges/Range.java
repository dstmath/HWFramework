package ohos.org.w3c.dom.ranges;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.Node;

public interface Range {
    public static final short END_TO_END = 2;
    public static final short END_TO_START = 3;
    public static final short START_TO_END = 1;
    public static final short START_TO_START = 0;

    DocumentFragment cloneContents() throws DOMException;

    Range cloneRange() throws DOMException;

    void collapse(boolean z) throws DOMException;

    short compareBoundaryPoints(short s, Range range) throws DOMException;

    void deleteContents() throws DOMException;

    void detach() throws DOMException;

    DocumentFragment extractContents() throws DOMException;

    boolean getCollapsed() throws DOMException;

    Node getCommonAncestorContainer() throws DOMException;

    Node getEndContainer() throws DOMException;

    int getEndOffset() throws DOMException;

    Node getStartContainer() throws DOMException;

    int getStartOffset() throws DOMException;

    void insertNode(Node node) throws DOMException, RangeException;

    void selectNode(Node node) throws RangeException, DOMException;

    void selectNodeContents(Node node) throws RangeException, DOMException;

    void setEnd(Node node, int i) throws RangeException, DOMException;

    void setEndAfter(Node node) throws RangeException, DOMException;

    void setEndBefore(Node node) throws RangeException, DOMException;

    void setStart(Node node, int i) throws RangeException, DOMException;

    void setStartAfter(Node node) throws RangeException, DOMException;

    void setStartBefore(Node node) throws RangeException, DOMException;

    void surroundContents(Node node) throws DOMException, RangeException;

    String toString() throws DOMException;
}
