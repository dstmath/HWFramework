package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.util.Map;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.javax.xml.transform.SourceLocator;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.DeclHandler;
import ohos.org.xml.sax.ext.LexicalHandler;
import ohos.org.xml.sax.helpers.AttributesImpl;

public class AdaptiveResultTreeImpl extends SimpleResultTreeImpl {
    private static final String EMPTY_STRING = "".intern();
    private static int _documentURIIndex;
    private final AttributesImpl _attributes = new AttributesImpl();
    private boolean _buildIdIndex;
    private SAXImpl _dom;
    private int _initSize;
    private String _openElementName;
    private DTMWSFilter _wsfilter;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public SerializationHandler getOutputDomBuilder() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void startDocument() throws SAXException {
    }

    public AdaptiveResultTreeImpl(XSLTCDTMManager xSLTCDTMManager, int i, DTMWSFilter dTMWSFilter, int i2, boolean z) {
        super(xSLTCDTMManager, i);
        this._wsfilter = dTMWSFilter;
        this._initSize = i2;
        this._buildIdIndex = z;
    }

    public DOM getNestedDOM() {
        return this._dom;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocument() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocument();
        }
        return super.getDocument();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getStringValue() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getStringValue();
        }
        return super.getStringValue();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getIterator() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getIterator();
        }
        return super.getIterator();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getChildren(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getChildren(i);
        }
        return super.getChildren(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getTypedChildren(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getTypedChildren(i);
        }
        return super.getTypedChildren(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getAxisIterator(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getAxisIterator(i);
        }
        return super.getAxisIterator(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getTypedAxisIterator(int i, int i2) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getTypedAxisIterator(i, i2);
        }
        return super.getTypedAxisIterator(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getNthDescendant(int i, int i2, boolean z) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNthDescendant(i, i2, z);
        }
        return super.getNthDescendant(i, i2, z);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getNamespaceAxisIterator(int i, int i2) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNamespaceAxisIterator(i, i2);
        }
        return super.getNamespaceAxisIterator(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getNodeValueIterator(DTMAxisIterator dTMAxisIterator, int i, String str, boolean z) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNodeValueIterator(dTMAxisIterator, i, str, z);
        }
        return super.getNodeValueIterator(dTMAxisIterator, i, str, z);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator orderNodes(DTMAxisIterator dTMAxisIterator, int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.orderNodes(dTMAxisIterator, i);
        }
        return super.orderNodes(dTMAxisIterator, i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeName(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNodeName(i);
        }
        return super.getNodeName(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeNameX(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNodeNameX(i);
        }
        return super.getNodeNameX(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getNamespaceName(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNamespaceName(i);
        }
        return super.getNamespaceName(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getExpandedTypeID(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getExpandedTypeID(i);
        }
        return super.getExpandedTypeID(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNamespaceType(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNamespaceType(i);
        }
        return super.getNamespaceType(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getParent(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getParent(i);
        }
        return super.getParent(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getAttributeNode(int i, int i2) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getAttributeNode(i, i2);
        }
        return super.getAttributeNode(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getStringValueX(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getStringValueX(i);
        }
        return super.getStringValueX(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void copy(int i, SerializationHandler serializationHandler) throws TransletException {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.copy(i, serializationHandler);
        } else {
            super.copy(i, serializationHandler);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void copy(DTMAxisIterator dTMAxisIterator, SerializationHandler serializationHandler) throws TransletException {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.copy(dTMAxisIterator, serializationHandler);
        } else {
            super.copy(dTMAxisIterator, serializationHandler);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String shallowCopy(int i, SerializationHandler serializationHandler) throws TransletException {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.shallowCopy(i, serializationHandler);
        }
        return super.shallowCopy(i, serializationHandler);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public boolean lessThan(int i, int i2) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.lessThan(i, i2);
        }
        return super.lessThan(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void characters(int i, SerializationHandler serializationHandler) throws TransletException {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.characters(i, serializationHandler);
        } else {
            super.characters(i, serializationHandler);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public Node makeNode(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.makeNode(i);
        }
        return super.makeNode(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public Node makeNode(DTMAxisIterator dTMAxisIterator) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.makeNode(dTMAxisIterator);
        }
        return super.makeNode(dTMAxisIterator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public NodeList makeNodeList(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.makeNodeList(i);
        }
        return super.makeNodeList(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public NodeList makeNodeList(DTMAxisIterator dTMAxisIterator) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.makeNodeList(dTMAxisIterator);
        }
        return super.makeNodeList(dTMAxisIterator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getLanguage(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getLanguage(i);
        }
        return super.getLanguage(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getSize() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getSize();
        }
        return super.getSize();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getDocumentURI(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentURI(i);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("adaptive_rtf");
        int i2 = _documentURIIndex;
        _documentURIIndex = i2 + 1;
        sb.append(i2);
        return sb.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void setFilter(StripFilter stripFilter) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.setFilter(stripFilter);
        } else {
            super.setFilter(stripFilter);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void setupMapping(String[] strArr, String[] strArr2, int[] iArr, String[] strArr3) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.setupMapping(strArr, strArr2, iArr, strArr3);
        } else {
            super.setupMapping(strArr, strArr2, iArr, strArr3);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public boolean isElement(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.isElement(i);
        }
        return super.isElement(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public boolean isAttribute(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.isAttribute(i);
        }
        return super.isAttribute(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String lookupNamespace(int i, String str) throws TransletException {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.lookupNamespace(i, str);
        }
        return super.lookupNamespace(i, str);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public final int getNodeIdent(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNodeIdent(i);
        }
        return super.getNodeIdent(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public final int getNodeHandle(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNodeHandle(i);
        }
        return super.getNodeHandle(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DOM getResultTreeFrag(int i, int i2) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getResultTreeFrag(i, i2);
        }
        return super.getResultTreeFrag(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNSType(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNSType(i);
        }
        return super.getNSType(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getUnparsedEntityURI(String str) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getUnparsedEntityURI(str);
        }
        return super.getUnparsedEntityURI(str);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public Map<String, Integer> getElementsWithIDs() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getElementsWithIDs();
        }
        return super.getElementsWithIDs();
    }

    private void maybeEmitStartElement() throws SAXException {
        String str = this._openElementName;
        if (str != null) {
            int indexOf = str.indexOf(58);
            if (indexOf < 0) {
                SAXImpl sAXImpl = this._dom;
                String str2 = this._openElementName;
                sAXImpl.startElement(null, str2, str2, this._attributes);
            } else {
                this._dom.startElement(this._dom.getNamespaceURI(this._openElementName.substring(0, indexOf)), this._openElementName.substring(indexOf + 1), this._openElementName, this._attributes);
            }
            this._openElementName = null;
        }
    }

    private void prepareNewDOM() throws SAXException {
        this._dom = (SAXImpl) this._dtmManager.getDTM(null, true, this._wsfilter, true, false, false, this._initSize, this._buildIdIndex);
        this._dom.startDocument();
        for (int i = 0; i < this._size; i++) {
            String str = this._textArray[i];
            this._dom.characters(str.toCharArray(), 0, str.length());
        }
        this._size = 0;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void endDocument() throws SAXException {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.endDocument();
        } else {
            super.endDocument();
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(String str) throws SAXException {
        if (this._dom != null) {
            characters(str.toCharArray(), 0, str.length());
        } else {
            super.characters(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        if (this._dom != null) {
            maybeEmitStartElement();
            this._dom.characters(cArr, i, i2);
            return;
        }
        super.characters(cArr, i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public boolean setEscaping(boolean z) throws SAXException {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.setEscaping(z);
        }
        return super.setEscaping(z);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str) throws SAXException {
        if (this._dom == null) {
            prepareNewDOM();
        }
        maybeEmitStartElement();
        this._openElementName = str;
        this._attributes.clear();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str, String str2, String str3) throws SAXException {
        startElement(str3);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        startElement(str3);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void endElement(String str) throws SAXException {
        maybeEmitStartElement();
        this._dom.endElement(null, null, str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void endElement(String str, String str2, String str3) throws SAXException {
        endElement(str3);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2) {
        String str3;
        int indexOf = str.indexOf(58);
        String str4 = EMPTY_STRING;
        if (indexOf > 0) {
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1);
            str4 = this._dom.getNamespaceURI(substring);
            str3 = substring2;
        } else {
            str3 = str;
        }
        addAttribute(str4, str3, str, "CDATA", str2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addUniqueAttribute(String str, String str2, int i) throws SAXException {
        addAttribute(str, str2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2, String str3, String str4, String str5) {
        if (this._openElementName != null) {
            this._attributes.addAttribute(str, str2, str3, str4, str5);
        } else {
            BasisLibrary.runTimeError("STRAY_ATTRIBUTE_ERR", str3);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void namespaceAfterStartElement(String str, String str2) throws SAXException {
        if (this._dom == null) {
            prepareNewDOM();
        }
        this._dom.startPrefixMapping(str, str2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedLexicalHandler
    public void comment(String str) throws SAXException {
        if (this._dom == null) {
            prepareNewDOM();
        }
        maybeEmitStartElement();
        char[] charArray = str.toCharArray();
        this._dom.comment(charArray, 0, charArray.length);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void comment(char[] cArr, int i, int i2) throws SAXException {
        if (this._dom == null) {
            prepareNewDOM();
        }
        maybeEmitStartElement();
        this._dom.comment(cArr, i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void processingInstruction(String str, String str2) throws SAXException {
        if (this._dom == null) {
            prepareNewDOM();
        }
        maybeEmitStartElement();
        this._dom.processingInstruction(str, str2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setFeature(String str, boolean z) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.setFeature(str, z);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setProperty(String str, Object obj) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.setProperty(str, obj);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisTraverser getAxisTraverser(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getAxisTraverser(i);
        }
        return super.getAxisTraverser(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean hasChildNodes(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.hasChildNodes(i);
        }
        return super.hasChildNodes(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstChild(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getFirstChild(i);
        }
        return super.getFirstChild(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getLastChild(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getLastChild(i);
        }
        return super.getLastChild(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getAttributeNode(int i, String str, String str2) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getAttributeNode(i, str, str2);
        }
        return super.getAttributeNode(i, str, str2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstAttribute(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getFirstAttribute(i);
        }
        return super.getFirstAttribute(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstNamespaceNode(int i, boolean z) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getFirstNamespaceNode(i, z);
        }
        return super.getFirstNamespaceNode(i, z);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextSibling(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNextSibling(i);
        }
        return super.getNextSibling(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getPreviousSibling(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getPreviousSibling(i);
        }
        return super.getPreviousSibling(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextAttribute(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNextAttribute(i);
        }
        return super.getNextAttribute(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextNamespaceNode(int i, int i2, boolean z) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNextNamespaceNode(i, i2, z);
        }
        return super.getNextNamespaceNode(i, i2, z);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getOwnerDocument(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getOwnerDocument(i);
        }
        return super.getOwnerDocument(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocumentRoot(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentRoot(i);
        }
        return super.getDocumentRoot(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public XMLString getStringValue(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getStringValue(i);
        }
        return super.getStringValue(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getStringValueChunkCount(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getStringValueChunkCount(i);
        }
        return super.getStringValueChunkCount(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public char[] getStringValueChunk(int i, int i2, int[] iArr) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getStringValueChunk(i, i2, iArr);
        }
        return super.getStringValueChunk(i, i2, iArr);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getExpandedTypeID(String str, String str2, int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getExpandedTypeID(str, str2, i);
        }
        return super.getExpandedTypeID(str, str2, i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalNameFromExpandedNameID(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getLocalNameFromExpandedNameID(i);
        }
        return super.getLocalNameFromExpandedNameID(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNamespaceFromExpandedNameID(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNamespaceFromExpandedNameID(i);
        }
        return super.getNamespaceFromExpandedNameID(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalName(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getLocalName(i);
        }
        return super.getLocalName(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getPrefix(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getPrefix(i);
        }
        return super.getPrefix(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNamespaceURI(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNamespaceURI(i);
        }
        return super.getNamespaceURI(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeValue(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNodeValue(i);
        }
        return super.getNodeValue(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public short getNodeType(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNodeType(i);
        }
        return super.getNodeType(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public short getLevel(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getLevel(i);
        }
        return super.getLevel(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isSupported(String str, String str2) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.isSupported(str, str2);
        }
        return super.isSupported(str, str2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentBaseURI() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentBaseURI();
        }
        return super.getDocumentBaseURI();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setDocumentBaseURI(String str) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.setDocumentBaseURI(str);
        } else {
            super.setDocumentBaseURI(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentSystemIdentifier(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentSystemIdentifier(i);
        }
        return super.getDocumentSystemIdentifier(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentEncoding(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentEncoding(i);
        }
        return super.getDocumentEncoding(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentStandalone(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentStandalone(i);
        }
        return super.getDocumentStandalone(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentVersion(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentVersion(i);
        }
        return super.getDocumentVersion(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean getDocumentAllDeclarationsProcessed() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentAllDeclarationsProcessed();
        }
        return super.getDocumentAllDeclarationsProcessed();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationSystemIdentifier() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentTypeDeclarationSystemIdentifier();
        }
        return super.getDocumentTypeDeclarationSystemIdentifier();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationPublicIdentifier() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDocumentTypeDeclarationPublicIdentifier();
        }
        return super.getDocumentTypeDeclarationPublicIdentifier();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getElementById(String str) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getElementById(str);
        }
        return super.getElementById(str);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean supportsPreStripping() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.supportsPreStripping();
        }
        return super.supportsPreStripping();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isNodeAfter(int i, int i2) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.isNodeAfter(i, i2);
        }
        return super.isNodeAfter(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isCharacterElementContentWhitespace(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.isCharacterElementContentWhitespace(i);
        }
        return super.isCharacterElementContentWhitespace(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isDocumentAllDeclarationsProcessed(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.isDocumentAllDeclarationsProcessed(i);
        }
        return super.isDocumentAllDeclarationsProcessed(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isAttributeSpecified(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.isAttributeSpecified(i);
        }
        return super.isAttributeSpecified(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchCharactersEvents(int i, ContentHandler contentHandler, boolean z) throws SAXException {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.dispatchCharactersEvents(i, contentHandler, z);
        } else {
            super.dispatchCharactersEvents(i, contentHandler, z);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchToEvents(int i, ContentHandler contentHandler) throws SAXException {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.dispatchToEvents(i, contentHandler);
        } else {
            super.dispatchToEvents(i, contentHandler);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public Node getNode(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getNode(i);
        }
        return super.getNode(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean needsTwoThreads() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.needsTwoThreads();
        }
        return super.needsTwoThreads();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public ContentHandler getContentHandler() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getContentHandler();
        }
        return super.getContentHandler();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public LexicalHandler getLexicalHandler() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getLexicalHandler();
        }
        return super.getLexicalHandler();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public EntityResolver getEntityResolver() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getEntityResolver();
        }
        return super.getEntityResolver();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTDHandler getDTDHandler() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDTDHandler();
        }
        return super.getDTDHandler();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public ErrorHandler getErrorHandler() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getErrorHandler();
        }
        return super.getErrorHandler();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DeclHandler getDeclHandler() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getDeclHandler();
        }
        return super.getDeclHandler();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void appendChild(int i, boolean z, boolean z2) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.appendChild(i, z, z2);
        } else {
            super.appendChild(i, z, z2);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void appendTextChild(String str) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.appendTextChild(str);
        } else {
            super.appendTextChild(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public SourceLocator getSourceLocatorFor(int i) {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            return sAXImpl.getSourceLocatorFor(i);
        }
        return super.getSourceLocatorFor(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void documentRegistration() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.documentRegistration();
        } else {
            super.documentRegistration();
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void documentRelease() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.documentRelease();
        } else {
            super.documentRelease();
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl, ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void release() {
        SAXImpl sAXImpl = this._dom;
        if (sAXImpl != null) {
            sAXImpl.release();
            this._dom = null;
        }
        super.release();
    }
}
