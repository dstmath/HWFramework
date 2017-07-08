package org.apache.xml.dtm.ref;

import org.apache.xml.dtm.DTM;
import org.apache.xml.utils.NodeConsumer;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.compiler.OpCodes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public class DTMTreeWalker {
    private ContentHandler m_contentHandler;
    protected DTM m_dtm;
    boolean nextIsRaw;

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
        this.m_contentHandler = null;
        this.nextIsRaw = false;
    }

    public DTMTreeWalker(ContentHandler contentHandler, DTM dtm) {
        this.m_contentHandler = null;
        this.nextIsRaw = false;
        this.m_contentHandler = contentHandler;
        this.m_dtm = dtm;
    }

    public void traverse(int pos) throws SAXException {
        int top = pos;
        while (-1 != pos) {
            startNode(pos);
            int nextNode = this.m_dtm.getFirstChild(pos);
            while (-1 == nextNode) {
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
                        nextNode = -1;
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
            while (-1 == nextNode) {
                endNode(pos);
                if (-1 != top && top == pos) {
                    break;
                }
                nextNode = this.m_dtm.getNextSibling(pos);
                if (-1 == nextNode) {
                    pos = this.m_dtm.getParent(pos);
                    if (-1 == pos || (-1 != top && top == pos)) {
                        nextNode = -1;
                        break;
                    }
                }
            }
            pos = nextNode;
        }
    }

    private final void dispatachChars(int node) throws SAXException {
        this.m_dtm.dispatchCharactersEvents(node, this.m_contentHandler, false);
    }

    protected void startNode(int node) throws SAXException {
        if (this.m_contentHandler instanceof NodeConsumer) {
        }
        switch (this.m_dtm.getNodeType(node)) {
            case OpCodes.OP_XPATH /*1*/:
                DTM dtm = this.m_dtm;
                int nsn = dtm.getFirstNamespaceNode(node, true);
                while (-1 != nsn) {
                    this.m_contentHandler.startPrefixMapping(dtm.getNodeNameX(nsn), dtm.getNodeValue(nsn));
                    nsn = dtm.getNextNamespaceNode(node, nsn, true);
                }
                String ns = dtm.getNamespaceURI(node);
                if (ns == null) {
                    ns = SerializerConstants.EMPTYSTRING;
                }
                AttributesImpl attrs = new AttributesImpl();
                for (int i = dtm.getFirstAttribute(node); i != -1; i = dtm.getNextAttribute(i)) {
                    attrs.addAttribute(dtm.getNamespaceURI(i), dtm.getLocalName(i), dtm.getNodeName(i), "CDATA", dtm.getNodeValue(i));
                }
                this.m_contentHandler.startElement(ns, this.m_dtm.getLocalName(node), this.m_dtm.getNodeName(node), attrs);
            case OpCodes.OP_AND /*3*/:
                if (this.nextIsRaw) {
                    this.nextIsRaw = false;
                    this.m_contentHandler.processingInstruction("javax.xml.transform.disable-output-escaping", SerializerConstants.EMPTYSTRING);
                    dispatachChars(node);
                    this.m_contentHandler.processingInstruction("javax.xml.transform.enable-output-escaping", SerializerConstants.EMPTYSTRING);
                    return;
                }
                dispatachChars(node);
            case OpCodes.OP_NOTEQUALS /*4*/:
                boolean isLexH = this.m_contentHandler instanceof LexicalHandler;
                LexicalHandler lexicalHandler = isLexH ? (LexicalHandler) this.m_contentHandler : null;
                if (isLexH) {
                    lexicalHandler.startCDATA();
                }
                dispatachChars(node);
                if (isLexH) {
                    lexicalHandler.endCDATA();
                }
            case OpCodes.OP_EQUALS /*5*/:
                if (this.m_contentHandler instanceof LexicalHandler) {
                    ((LexicalHandler) this.m_contentHandler).startEntity(this.m_dtm.getNodeName(node));
                }
            case OpCodes.OP_LT /*7*/:
                String name = this.m_dtm.getNodeName(node);
                if (name.equals("xslt-next-is-raw")) {
                    this.nextIsRaw = true;
                } else {
                    this.m_contentHandler.processingInstruction(name, this.m_dtm.getNodeValue(node));
                }
            case OpCodes.OP_GTE /*8*/:
                XMLString data = this.m_dtm.getStringValue(node);
                if (this.m_contentHandler instanceof LexicalHandler) {
                    data.dispatchAsComment(this.m_contentHandler);
                }
            case OpCodes.OP_GT /*9*/:
                this.m_contentHandler.startDocument();
            default:
        }
    }

    protected void endNode(int node) throws SAXException {
        switch (this.m_dtm.getNodeType(node)) {
            case OpCodes.OP_XPATH /*1*/:
                String ns = this.m_dtm.getNamespaceURI(node);
                if (ns == null) {
                    ns = SerializerConstants.EMPTYSTRING;
                }
                this.m_contentHandler.endElement(ns, this.m_dtm.getLocalName(node), this.m_dtm.getNodeName(node));
                int nsn = this.m_dtm.getFirstNamespaceNode(node, true);
                while (-1 != nsn) {
                    this.m_contentHandler.endPrefixMapping(this.m_dtm.getNodeNameX(nsn));
                    nsn = this.m_dtm.getNextNamespaceNode(node, nsn, true);
                }
            case OpCodes.OP_EQUALS /*5*/:
                if (this.m_contentHandler instanceof LexicalHandler) {
                    this.m_contentHandler.endEntity(this.m_dtm.getNodeName(node));
                }
            case OpCodes.OP_GT /*9*/:
                this.m_contentHandler.endDocument();
            default:
        }
    }
}
