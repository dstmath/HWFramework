package org.apache.xml.dtm.ref;

import org.apache.xml.dtm.DTM;
import org.apache.xml.utils.NodeConsumer;
import org.apache.xml.utils.XMLString;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

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

    public void setcontentHandler(ContentHandler ch) {
        this.m_contentHandler = ch;
    }

    public DTMTreeWalker() {
    }

    public DTMTreeWalker(ContentHandler contentHandler, DTM dtm) {
        this.m_contentHandler = contentHandler;
        this.m_dtm = dtm;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        r2 = -1;
     */
    public void traverse(int top) throws SAXException {
        int pos = top;
        while (-1 != pos) {
            startNode(pos);
            int nextNode = this.m_dtm.getFirstChild(pos);
            while (true) {
                if (-1 != nextNode) {
                    break;
                }
                endNode(pos);
                if (top == pos) {
                    break;
                }
                nextNode = this.m_dtm.getNextSibling(pos);
                if (-1 == nextNode) {
                    pos = this.m_dtm.getParent(pos);
                    if (-1 == pos || top == pos) {
                        if (-1 != pos) {
                            endNode(pos);
                        }
                    }
                }
            }
            pos = nextNode;
        }
    }

    public void traverse(int pos, int top) throws SAXException {
        while (-1 != pos) {
            startNode(pos);
            int nextNode = this.m_dtm.getFirstChild(pos);
            while (true) {
                if (-1 != nextNode) {
                    break;
                }
                endNode(pos);
                if (-1 != top && top == pos) {
                    break;
                }
                nextNode = this.m_dtm.getNextSibling(pos);
                if (-1 == nextNode) {
                    pos = this.m_dtm.getParent(pos);
                    if (-1 == pos || (-1 != top && top == pos)) {
                        nextNode = -1;
                    }
                }
            }
            pos = nextNode;
        }
    }

    private final void dispatachChars(int node) throws SAXException {
        this.m_dtm.dispatchCharactersEvents(node, this.m_contentHandler, false);
    }

    /* access modifiers changed from: protected */
    public void startNode(int node) throws SAXException {
        boolean z = this.m_contentHandler instanceof NodeConsumer;
        switch (this.m_dtm.getNodeType(node)) {
            case 1:
                DTM dtm = this.m_dtm;
                int nsn = dtm.getFirstNamespaceNode(node, true);
                while (-1 != nsn) {
                    this.m_contentHandler.startPrefixMapping(dtm.getNodeNameX(nsn), dtm.getNodeValue(nsn));
                    nsn = dtm.getNextNamespaceNode(node, nsn, true);
                }
                String ns = dtm.getNamespaceURI(node);
                if (ns == null) {
                    ns = "";
                }
                AttributesImpl attrs = new AttributesImpl();
                int i = dtm.getFirstAttribute(node);
                while (true) {
                    int i2 = i;
                    if (i2 != -1) {
                        attrs.addAttribute(dtm.getNamespaceURI(i2), dtm.getLocalName(i2), dtm.getNodeName(i2), "CDATA", dtm.getNodeValue(i2));
                        i = dtm.getNextAttribute(i2);
                    } else {
                        this.m_contentHandler.startElement(ns, this.m_dtm.getLocalName(node), this.m_dtm.getNodeName(node), attrs);
                        return;
                    }
                }
            case 3:
                if (this.nextIsRaw) {
                    this.nextIsRaw = false;
                    this.m_contentHandler.processingInstruction("javax.xml.transform.disable-output-escaping", "");
                    dispatachChars(node);
                    this.m_contentHandler.processingInstruction("javax.xml.transform.enable-output-escaping", "");
                    return;
                }
                dispatachChars(node);
                return;
            case 4:
                boolean isLexH = this.m_contentHandler instanceof LexicalHandler;
                LexicalHandler lh = isLexH ? (LexicalHandler) this.m_contentHandler : null;
                if (isLexH) {
                    lh.startCDATA();
                }
                dispatachChars(node);
                if (isLexH) {
                    lh.endCDATA();
                    return;
                }
                return;
            case 5:
                if (this.m_contentHandler instanceof LexicalHandler) {
                    ((LexicalHandler) this.m_contentHandler).startEntity(this.m_dtm.getNodeName(node));
                    return;
                }
                return;
            case 7:
                String name = this.m_dtm.getNodeName(node);
                if (name.equals("xslt-next-is-raw")) {
                    this.nextIsRaw = true;
                    return;
                } else {
                    this.m_contentHandler.processingInstruction(name, this.m_dtm.getNodeValue(node));
                    return;
                }
            case 8:
                XMLString data = this.m_dtm.getStringValue(node);
                if (this.m_contentHandler instanceof LexicalHandler) {
                    data.dispatchAsComment((LexicalHandler) this.m_contentHandler);
                    return;
                }
                return;
            case 9:
                this.m_contentHandler.startDocument();
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void endNode(int node) throws SAXException {
        short nodeType = this.m_dtm.getNodeType(node);
        if (nodeType == 1) {
            String ns = this.m_dtm.getNamespaceURI(node);
            if (ns == null) {
                ns = "";
            }
            this.m_contentHandler.endElement(ns, this.m_dtm.getLocalName(node), this.m_dtm.getNodeName(node));
            int nsn = this.m_dtm.getFirstNamespaceNode(node, true);
            while (-1 != nsn) {
                this.m_contentHandler.endPrefixMapping(this.m_dtm.getNodeNameX(nsn));
                nsn = this.m_dtm.getNextNamespaceNode(node, nsn, true);
            }
        } else if (nodeType != 9) {
            switch (nodeType) {
                case 5:
                    if (this.m_contentHandler instanceof LexicalHandler) {
                        ((LexicalHandler) this.m_contentHandler).endEntity(this.m_dtm.getNodeName(node));
                        return;
                    }
                    return;
                default:
                    return;
            }
        } else {
            this.m_contentHandler.endDocument();
        }
    }
}
