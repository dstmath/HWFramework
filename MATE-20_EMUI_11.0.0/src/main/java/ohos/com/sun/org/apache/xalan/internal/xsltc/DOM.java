package ohos.com.sun.org.apache.xalan.internal.xsltc;

import java.util.Map;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;

public interface DOM {
    public static final int ADAPTIVE_RTF = 1;
    public static final int FIRST_TYPE = 0;
    public static final int NO_TYPE = -1;
    public static final int NULL = 0;
    public static final int RETURN_CURRENT = 0;
    public static final int RETURN_PARENT = 1;
    public static final int SIMPLE_RTF = 0;
    public static final int TREE_RTF = 2;

    void characters(int i, SerializationHandler serializationHandler) throws TransletException;

    void copy(int i, SerializationHandler serializationHandler) throws TransletException;

    void copy(DTMAxisIterator dTMAxisIterator, SerializationHandler serializationHandler) throws TransletException;

    int getAttributeNode(int i, int i2);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    DTMAxisIterator getAxisIterator(int i);

    DTMAxisIterator getChildren(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    int getDocument();

    String getDocumentURI(int i);

    Map<String, Integer> getElementsWithIDs();

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    int getExpandedTypeID(int i);

    DTMAxisIterator getIterator();

    String getLanguage(int i);

    int getNSType(int i);

    DTMAxisIterator getNamespaceAxisIterator(int i, int i2);

    String getNamespaceName(int i);

    int getNamespaceType(int i);

    int getNodeHandle(int i);

    int getNodeIdent(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    String getNodeName(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    String getNodeNameX(int i);

    DTMAxisIterator getNodeValueIterator(DTMAxisIterator dTMAxisIterator, int i, String str, boolean z);

    DTMAxisIterator getNthDescendant(int i, int i2, boolean z);

    SerializationHandler getOutputDomBuilder();

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    int getParent(int i);

    DOM getResultTreeFrag(int i, int i2);

    DOM getResultTreeFrag(int i, int i2, boolean z);

    int getSize();

    String getStringValue();

    String getStringValueX(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    DTMAxisIterator getTypedAxisIterator(int i, int i2);

    DTMAxisIterator getTypedChildren(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    String getUnparsedEntityURI(String str);

    boolean isAttribute(int i);

    boolean isElement(int i);

    boolean lessThan(int i, int i2);

    String lookupNamespace(int i, String str) throws TransletException;

    Node makeNode(int i);

    Node makeNode(DTMAxisIterator dTMAxisIterator);

    NodeList makeNodeList(int i);

    NodeList makeNodeList(DTMAxisIterator dTMAxisIterator);

    DTMAxisIterator orderNodes(DTMAxisIterator dTMAxisIterator, int i);

    void release();

    void setFilter(StripFilter stripFilter);

    void setupMapping(String[] strArr, String[] strArr2, int[] iArr, String[] strArr3);

    String shallowCopy(int i, SerializationHandler serializationHandler) throws TransletException;
}
