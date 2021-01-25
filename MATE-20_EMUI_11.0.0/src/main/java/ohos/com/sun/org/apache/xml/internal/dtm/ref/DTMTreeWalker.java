package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.utils.NodeConsumer;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;
import ohos.org.xml.sax.helpers.AttributesImpl;

public class DTMTreeWalker {
    private ContentHandler m_contentHandler = null;
    protected DTM m_dtm;
    boolean nextIsRaw = false;

    public void setDTM(DTM dtm) {
        this.m_dtm = dtm;
    }

    public ContentHandler getcontentHandler() {
        return this.m_contentHandler;
    }

    public void setcontentHandler(ContentHandler contentHandler) {
        this.m_contentHandler = contentHandler;
    }

    public DTMTreeWalker() {
    }

    public DTMTreeWalker(ContentHandler contentHandler, DTM dtm) {
        this.m_contentHandler = contentHandler;
        this.m_dtm = dtm;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        if (-1 == r0) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0029, code lost:
        endNode(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        r0 = -1;
     */
    public void traverse(int i) throws SAXException {
        int i2 = i;
        while (-1 != i2) {
            startNode(i2);
            int firstChild = this.m_dtm.getFirstChild(i2);
            while (true) {
                if (-1 != firstChild) {
                    break;
                }
                endNode(i2);
                if (i == i2) {
                    break;
                }
                firstChild = this.m_dtm.getNextSibling(i2);
                if (!(-1 == firstChild && (-1 == (i2 = this.m_dtm.getParent(i2)) || i == i2))) {
                }
            }
            i2 = firstChild;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002a, code lost:
        r4 = -1;
     */
    public void traverse(int i, int i2) throws SAXException {
        while (-1 != i) {
            startNode(i);
            int firstChild = this.m_dtm.getFirstChild(i);
            while (true) {
                if (-1 != firstChild) {
                    break;
                }
                endNode(i);
                if (-1 != i2 && i2 == i) {
                    break;
                }
                firstChild = this.m_dtm.getNextSibling(i);
                if (-1 != firstChild || (-1 != (i = this.m_dtm.getParent(i)) && (-1 == i2 || i2 != i))) {
                }
            }
            i = firstChild;
        }
    }

    private final void dispatachChars(int i) throws SAXException {
        this.m_dtm.dispatchCharactersEvents(i, this.m_contentHandler, false);
    }

    /* access modifiers changed from: protected */
    public void startNode(int i) throws SAXException {
        boolean z = this.m_contentHandler instanceof NodeConsumer;
        short nodeType = this.m_dtm.getNodeType(i);
        String str = "";
        if (nodeType == 1) {
            DTM dtm = this.m_dtm;
            int firstNamespaceNode = dtm.getFirstNamespaceNode(i, true);
            while (-1 != firstNamespaceNode) {
                this.m_contentHandler.startPrefixMapping(dtm.getNodeNameX(firstNamespaceNode), dtm.getNodeValue(firstNamespaceNode));
                firstNamespaceNode = dtm.getNextNamespaceNode(i, firstNamespaceNode, true);
            }
            String namespaceURI = dtm.getNamespaceURI(i);
            if (namespaceURI != null) {
                str = namespaceURI;
            }
            AttributesImpl attributesImpl = new AttributesImpl();
            for (int firstAttribute = dtm.getFirstAttribute(i); firstAttribute != -1; firstAttribute = dtm.getNextAttribute(firstAttribute)) {
                attributesImpl.addAttribute(dtm.getNamespaceURI(firstAttribute), dtm.getLocalName(firstAttribute), dtm.getNodeName(firstAttribute), "CDATA", dtm.getNodeValue(firstAttribute));
            }
            this.m_contentHandler.startElement(str, this.m_dtm.getLocalName(i), this.m_dtm.getNodeName(i), attributesImpl);
        } else if (nodeType != 11) {
            if (nodeType != 3) {
                if (nodeType == 4) {
                    LexicalHandler lexicalHandler = this.m_contentHandler;
                    boolean z2 = lexicalHandler instanceof LexicalHandler;
                    LexicalHandler lexicalHandler2 = z2 ? lexicalHandler : null;
                    if (z2) {
                        lexicalHandler2.startCDATA();
                    }
                    dispatachChars(i);
                    if (z2) {
                        lexicalHandler2.endCDATA();
                    }
                } else if (nodeType == 5) {
                    LexicalHandler lexicalHandler3 = this.m_contentHandler;
                    if (lexicalHandler3 instanceof LexicalHandler) {
                        lexicalHandler3.startEntity(this.m_dtm.getNodeName(i));
                    }
                } else if (nodeType == 7) {
                    String nodeName = this.m_dtm.getNodeName(i);
                    if (nodeName.equals("xslt-next-is-raw")) {
                        this.nextIsRaw = true;
                    } else {
                        this.m_contentHandler.processingInstruction(nodeName, this.m_dtm.getNodeValue(i));
                    }
                } else if (nodeType == 8) {
                    XMLString stringValue = this.m_dtm.getStringValue(i);
                    ContentHandler contentHandler = this.m_contentHandler;
                    if (contentHandler instanceof LexicalHandler) {
                        stringValue.dispatchAsComment((LexicalHandler) contentHandler);
                    }
                } else if (nodeType == 9) {
                    this.m_contentHandler.startDocument();
                }
            } else if (this.nextIsRaw) {
                this.nextIsRaw = false;
                this.m_contentHandler.processingInstruction("javax.xml.transform.disable-output-escaping", str);
                dispatachChars(i);
                this.m_contentHandler.processingInstruction("javax.xml.transform.enable-output-escaping", str);
            } else {
                dispatachChars(i);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void endNode(int i) throws SAXException {
        short nodeType = this.m_dtm.getNodeType(i);
        if (nodeType == 1) {
            String namespaceURI = this.m_dtm.getNamespaceURI(i);
            if (namespaceURI == null) {
                namespaceURI = "";
            }
            this.m_contentHandler.endElement(namespaceURI, this.m_dtm.getLocalName(i), this.m_dtm.getNodeName(i));
            int firstNamespaceNode = this.m_dtm.getFirstNamespaceNode(i, true);
            while (-1 != firstNamespaceNode) {
                this.m_contentHandler.endPrefixMapping(this.m_dtm.getNodeNameX(firstNamespaceNode));
                firstNamespaceNode = this.m_dtm.getNextNamespaceNode(i, firstNamespaceNode, true);
            }
        } else if (nodeType == 9) {
            this.m_contentHandler.endDocument();
        } else if (nodeType != 4 && nodeType == 5) {
            LexicalHandler lexicalHandler = this.m_contentHandler;
            if (lexicalHandler instanceof LexicalHandler) {
                lexicalHandler.endEntity(this.m_dtm.getNodeName(i));
            }
        }
    }
}
