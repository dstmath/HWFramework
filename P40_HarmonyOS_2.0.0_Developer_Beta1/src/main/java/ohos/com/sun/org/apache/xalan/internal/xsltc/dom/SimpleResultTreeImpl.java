package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.util.Map;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault;
import ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringDefault;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.javax.xml.transform.SourceLocator;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.DeclHandler;
import ohos.org.xml.sax.ext.LexicalHandler;

public class SimpleResultTreeImpl extends EmptySerializer implements DOM, DTM {
    private static final DTMAxisIterator EMPTY_ITERATOR = new DTMAxisIteratorBase() {
        /* class ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl.AnonymousClass1 */

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator cloneIterator() {
            return this;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getLast() {
            return 0;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getPosition() {
            return 0;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void gotoMark() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            return this;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void setMark() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void setRestartable(boolean z) {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            return this;
        }
    };
    private static final String EMPTY_STR = "";
    public static final int NUMBER_OF_NODES = 2;
    public static final int RTF_ROOT = 0;
    public static final int RTF_TEXT = 1;
    private static int _documentURIIndex = 0;
    private int _documentID;
    private BitArray _dontEscape = null;
    protected XSLTCDTMManager _dtmManager;
    private boolean _escaping = true;
    protected int _size = 0;
    private String _text;
    protected String[] _textArray;

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void appendChild(int i, boolean z, boolean z2) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void appendTextChild(String str) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchCharactersEvents(int i, ContentHandler contentHandler, boolean z) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchToEvents(int i, ContentHandler contentHandler) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void documentRegistration() {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void documentRelease() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getAttributeNode(int i, int i2) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getAttributeNode(int i, String str, String str2) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisTraverser getAxisTraverser(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public ContentHandler getContentHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DeclHandler getDeclHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean getDocumentAllDeclarationsProcessed() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentBaseURI() {
        return "";
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentEncoding(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentStandalone(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentSystemIdentifier(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationPublicIdentifier() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationSystemIdentifier() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentVersion(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getElementById(String str) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public Map<String, Integer> getElementsWithIDs() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getExpandedTypeID(String str, String str2, int i) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstAttribute(int i) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstNamespaceNode(int i, boolean z) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getLanguage(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public LexicalHandler getLexicalHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalName(int i) {
        return "";
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalNameFromExpandedNameID(int i) {
        return "";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNSType(int i) {
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getNamespaceAxisIterator(int i, int i2) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNamespaceFromExpandedNameID(int i) {
        return "";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getNamespaceName(int i) {
        return "";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNamespaceType(int i) {
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNamespaceURI(int i) {
        return "";
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextAttribute(int i) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextNamespaceNode(int i, int i2, boolean z) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextSibling(int i) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeNameX(int i) {
        return "";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getNodeValueIterator(DTMAxisIterator dTMAxisIterator, int i, String str, boolean z) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getNthDescendant(int i, int i2, boolean z) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public SerializationHandler getOutputDomBuilder() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getPrefix(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getPreviousSibling(int i) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DOM getResultTreeFrag(int i, int i2) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DOM getResultTreeFrag(int i, int i2, boolean z) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getSize() {
        return 2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public SourceLocator getSourceLocatorFor(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public char[] getStringValueChunk(int i, int i2, int[] iArr) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getStringValueChunkCount(int i) {
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getUnparsedEntityURI(String str) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public boolean isAttribute(int i) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isAttributeSpecified(int i) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isCharacterElementContentWhitespace(int i) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isDocumentAllDeclarationsProcessed(int i) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public boolean isElement(int i) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isSupported(String str, String str2) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public boolean lessThan(int i, int i2) {
        if (i == -1) {
            return false;
        }
        if (i2 == -1) {
            return true;
        }
        return i < i2;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String lookupNamespace(int i, String str) throws TransletException {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public Node makeNode(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public Node makeNode(DTMAxisIterator dTMAxisIterator) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public NodeList makeNodeList(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public NodeList makeNodeList(DTMAxisIterator dTMAxisIterator) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void migrateTo(DTMManager dTMManager) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean needsTwoThreads() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator orderNodes(DTMAxisIterator dTMAxisIterator, int i) {
        return dTMAxisIterator;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setDocumentBaseURI(String str) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setFeature(String str, boolean z) {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void setFilter(StripFilter stripFilter) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setProperty(String str, Object obj) {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void setupMapping(String[] strArr, String[] strArr2, int[] iArr, String[] strArr3) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void startDocument() throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean supportsPreStripping() {
        return false;
    }

    public final class SimpleIterator extends DTMAxisIteratorBase {
        static final int DIRECTION_DOWN = 1;
        static final int DIRECTION_UP = 0;
        static final int NO_TYPE = -1;
        int _currentNode;
        int _direction = 1;
        int _type = -1;

        public SimpleIterator() {
        }

        public SimpleIterator(int i) {
            this._direction = i;
        }

        public SimpleIterator(int i, int i2) {
            this._direction = i;
            this._type = i2;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._direction == 1) {
                while (true) {
                    int i = this._currentNode;
                    if (i >= 2) {
                        return -1;
                    }
                    int i2 = this._type;
                    if (i2 == -1) {
                        SimpleResultTreeImpl simpleResultTreeImpl = SimpleResultTreeImpl.this;
                        this._currentNode = i + 1;
                        return returnNode(simpleResultTreeImpl.getNodeHandle(i));
                    } else if (!((i == 0 && i2 == 0) || (this._currentNode == 1 && this._type == 3))) {
                        this._currentNode++;
                    }
                }
                SimpleResultTreeImpl simpleResultTreeImpl2 = SimpleResultTreeImpl.this;
                int i3 = this._currentNode;
                this._currentNode = i3 + 1;
                return returnNode(simpleResultTreeImpl2.getNodeHandle(i3));
            }
            while (true) {
                int i4 = this._currentNode;
                if (i4 < 0) {
                    return -1;
                }
                int i5 = this._type;
                if (i5 == -1) {
                    SimpleResultTreeImpl simpleResultTreeImpl3 = SimpleResultTreeImpl.this;
                    this._currentNode = i4 - 1;
                    return returnNode(simpleResultTreeImpl3.getNodeHandle(i4));
                } else if (!((i4 == 0 && i5 == 0) || (this._currentNode == 1 && this._type == 3))) {
                    this._currentNode--;
                }
            }
            SimpleResultTreeImpl simpleResultTreeImpl4 = SimpleResultTreeImpl.this;
            int i6 = this._currentNode;
            this._currentNode = i6 - 1;
            return returnNode(simpleResultTreeImpl4.getNodeHandle(i6));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            int nodeIdent = SimpleResultTreeImpl.this.getNodeIdent(i);
            this._startNode = nodeIdent;
            if (!this._includeSelf && nodeIdent != -1) {
                int i2 = this._direction;
                if (i2 == 1) {
                    nodeIdent++;
                } else if (i2 == 0) {
                    nodeIdent--;
                }
            }
            this._currentNode = nodeIdent;
            return this;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void setMark() {
            this._markedNode = this._currentNode;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void gotoMark() {
            this._currentNode = this._markedNode;
        }
    }

    public final class SingletonIterator extends DTMAxisIteratorBase {
        static final int NO_TYPE = -1;
        int _currentNode;
        int _type = -1;

        public SingletonIterator() {
        }

        public SingletonIterator(int i) {
            this._type = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void setMark() {
            this._markedNode = this._currentNode;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void gotoMark() {
            this._currentNode = this._markedNode;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            int nodeIdent = SimpleResultTreeImpl.this.getNodeIdent(i);
            this._startNode = nodeIdent;
            this._currentNode = nodeIdent;
            return this;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._currentNode == -1) {
                return -1;
            }
            this._currentNode = -1;
            int i = this._type;
            if (i == -1) {
                return SimpleResultTreeImpl.this.getNodeHandle(this._currentNode);
            }
            if ((this._currentNode == 0 && i == 0) || (this._currentNode == 1 && this._type == 3)) {
                return SimpleResultTreeImpl.this.getNodeHandle(this._currentNode);
            }
            return -1;
        }
    }

    public SimpleResultTreeImpl(XSLTCDTMManager xSLTCDTMManager, int i) {
        this._dtmManager = xSLTCDTMManager;
        this._documentID = i;
        this._textArray = new String[4];
    }

    public DTMManagerDefault getDTMManager() {
        return this._dtmManager;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocument() {
        return this._documentID;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getStringValue() {
        return this._text;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getIterator() {
        return new SingletonIterator(getDocument());
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getChildren(int i) {
        return new SimpleIterator().setStartNode(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getTypedChildren(int i) {
        return new SimpleIterator(1, i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getAxisIterator(int i) {
        if (i != 0) {
            if (i == 1) {
                return new SimpleIterator(0).includeSelf();
            }
            if (i == 3 || i == 4) {
                return new SimpleIterator(1);
            }
            if (i == 5) {
                return new SimpleIterator(1).includeSelf();
            }
            if (i != 10) {
                if (i != 13) {
                    return EMPTY_ITERATOR;
                }
                return new SingletonIterator();
            }
        }
        return new SimpleIterator(0);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getTypedAxisIterator(int i, int i2) {
        if (i != 0) {
            if (i == 1) {
                return new SimpleIterator(0, i2).includeSelf();
            }
            if (i == 3 || i == 4) {
                return new SimpleIterator(1, i2);
            }
            if (i == 5) {
                return new SimpleIterator(1, i2).includeSelf();
            }
            if (i != 10) {
                if (i != 13) {
                    return EMPTY_ITERATOR;
                }
                return new SingletonIterator(i2);
            }
        }
        return new SimpleIterator(0, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeName(int i) {
        return getNodeIdent(i) == 1 ? PsuedoNames.PSEUDONAME_TEXT : "";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getExpandedTypeID(int i) {
        int nodeIdent = getNodeIdent(i);
        if (nodeIdent == 1) {
            return 3;
        }
        return nodeIdent == 0 ? 0 : -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getParent(int i) {
        if (getNodeIdent(i) == 1) {
            return getNodeHandle(0);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getStringValueX(int i) {
        int nodeIdent = getNodeIdent(i);
        if (nodeIdent == 0 || nodeIdent == 1) {
            return this._text;
        }
        return "";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void copy(int i, SerializationHandler serializationHandler) throws TransletException {
        characters(i, serializationHandler);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void copy(DTMAxisIterator dTMAxisIterator, SerializationHandler serializationHandler) throws TransletException {
        while (true) {
            int next = dTMAxisIterator.next();
            if (next != -1) {
                copy(next, serializationHandler);
            } else {
                return;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String shallowCopy(int i, SerializationHandler serializationHandler) throws TransletException {
        characters(i, serializationHandler);
        return null;
    }

    /* JADX WARN: Type inference failed for: r5v3, types: [java.lang.Throwable, ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void characters(int i, SerializationHandler serializationHandler) throws TransletException {
        int nodeIdent = getNodeIdent(i);
        if (nodeIdent == 0 || nodeIdent == 1) {
            boolean z = false;
            boolean z2 = false;
            for (int i2 = 0; i2 < this._size; i2++) {
                try {
                    if (this._dontEscape != null && (z = this._dontEscape.getBit(i2))) {
                        z2 = serializationHandler.setEscaping(false);
                    }
                    serializationHandler.characters(this._textArray[i2]);
                    if (z) {
                        serializationHandler.setEscaping(z2);
                    }
                } catch (SAXException e) {
                    throw new TransletException((Exception) e);
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getDocumentURI(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("simple_rtf");
        int i2 = _documentURIIndex;
        _documentURIIndex = i2 + 1;
        sb.append(i2);
        return sb.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNodeIdent(int i) {
        if (i != -1) {
            return i - this._documentID;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNodeHandle(int i) {
        if (i != -1) {
            return i + this._documentID;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void endDocument() throws SAXException {
        if (this._size == 1) {
            this._text = this._textArray[0];
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < this._size; i++) {
            stringBuffer.append(this._textArray[i]);
        }
        this._text = stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(String str) throws SAXException {
        int i = this._size;
        String[] strArr = this._textArray;
        if (i >= strArr.length) {
            String[] strArr2 = new String[(strArr.length * 2)];
            System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
            this._textArray = strArr2;
        }
        if (!this._escaping) {
            if (this._dontEscape == null) {
                this._dontEscape = new BitArray(8);
            }
            if (this._size >= this._dontEscape.size()) {
                BitArray bitArray = this._dontEscape;
                bitArray.resize(bitArray.size() * 2);
            }
            this._dontEscape.setBit(this._size);
        }
        String[] strArr3 = this._textArray;
        int i2 = this._size;
        this._size = i2 + 1;
        strArr3[i2] = str;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        int i3 = this._size;
        String[] strArr = this._textArray;
        if (i3 >= strArr.length) {
            String[] strArr2 = new String[(strArr.length * 2)];
            System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
            this._textArray = strArr2;
        }
        if (!this._escaping) {
            if (this._dontEscape == null) {
                this._dontEscape = new BitArray(8);
            }
            if (this._size >= this._dontEscape.size()) {
                BitArray bitArray = this._dontEscape;
                bitArray.resize(bitArray.size() * 2);
            }
            this._dontEscape.setBit(this._size);
        }
        String[] strArr3 = this._textArray;
        int i4 = this._size;
        this._size = i4 + 1;
        strArr3[i4] = new String(cArr, i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public boolean setEscaping(boolean z) throws SAXException {
        boolean z2 = this._escaping;
        this._escaping = z;
        return z2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean hasChildNodes(int i) {
        return getNodeIdent(i) == 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstChild(int i) {
        if (getNodeIdent(i) == 0) {
            return getNodeHandle(1);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getLastChild(int i) {
        return getFirstChild(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getOwnerDocument(int i) {
        return getDocument();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocumentRoot(int i) {
        return getDocument();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public XMLString getStringValue(int i) {
        return new XMLStringDefault(getStringValueX(i));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeValue(int i) {
        if (getNodeIdent(i) == 1) {
            return this._text;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public short getNodeType(int i) {
        int nodeIdent = getNodeIdent(i);
        if (nodeIdent == 1) {
            return 3;
        }
        return nodeIdent == 0 ? (short) 0 : -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public short getLevel(int i) {
        int nodeIdent = getNodeIdent(i);
        if (nodeIdent == 1) {
            return 2;
        }
        return nodeIdent == 0 ? (short) 1 : -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isNodeAfter(int i, int i2) {
        return lessThan(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public Node getNode(int i) {
        return makeNode(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void release() {
        if (this._documentID != 0) {
            this._dtmManager.release(this, true);
            this._documentID = 0;
        }
    }
}
