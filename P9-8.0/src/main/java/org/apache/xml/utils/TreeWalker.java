package org.apache.xml.utils;

import java.io.File;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.ref.dom2dtm.DOM2DTM.CharacterNodeHandler;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.LocatorImpl;

public class TreeWalker {
    private ContentHandler m_contentHandler = null;
    protected DOMHelper m_dh;
    private LocatorImpl m_locator = new LocatorImpl();
    boolean nextIsRaw = false;

    public ContentHandler getContentHandler() {
        return this.m_contentHandler;
    }

    public void setContentHandler(ContentHandler ch) {
        this.m_contentHandler = ch;
    }

    public TreeWalker(ContentHandler contentHandler, DOMHelper dh, String systemId) {
        this.m_contentHandler = contentHandler;
        this.m_contentHandler.setDocumentLocator(this.m_locator);
        if (systemId != null) {
            this.m_locator.setSystemId(systemId);
        } else {
            try {
                this.m_locator.setSystemId(System.getProperty("user.dir") + File.separator + "dummy.xsl");
            } catch (SecurityException e) {
            }
        }
        this.m_dh = dh;
    }

    public TreeWalker(ContentHandler contentHandler, DOMHelper dh) {
        this.m_contentHandler = contentHandler;
        this.m_contentHandler.setDocumentLocator(this.m_locator);
        try {
            this.m_locator.setSystemId(System.getProperty("user.dir") + File.separator + "dummy.xsl");
        } catch (SecurityException e) {
        }
        this.m_dh = dh;
    }

    public TreeWalker(ContentHandler contentHandler) {
        this.m_contentHandler = contentHandler;
        if (this.m_contentHandler != null) {
            this.m_contentHandler.setDocumentLocator(this.m_locator);
        }
        try {
            this.m_locator.setSystemId(System.getProperty("user.dir") + File.separator + "dummy.xsl");
        } catch (SecurityException e) {
        }
        this.m_dh = new DOM2Helper();
    }

    public void traverse(Node pos) throws SAXException {
        this.m_contentHandler.startDocument();
        traverseFragment(pos);
        this.m_contentHandler.endDocument();
    }

    public void traverseFragment(Node pos) throws SAXException {
        Node top = pos;
        while (pos != null) {
            startNode(pos);
            Node nextNode = pos.getFirstChild();
            while (nextNode == null) {
                endNode(pos);
                if (top.equals(pos)) {
                    break;
                }
                nextNode = pos.getNextSibling();
                if (nextNode == null) {
                    pos = pos.getParentNode();
                    if (pos == null || top.equals(pos)) {
                        if (pos != null) {
                            endNode(pos);
                        }
                        nextNode = null;
                    }
                }
            }
            pos = nextNode;
        }
    }

    public void traverse(Node pos, Node top) throws SAXException {
        this.m_contentHandler.startDocument();
        while (pos != null) {
            startNode(pos);
            Node nextNode = pos.getFirstChild();
            while (nextNode == null) {
                endNode(pos);
                if (top != null && top.equals(pos)) {
                    break;
                }
                nextNode = pos.getNextSibling();
                if (nextNode == null) {
                    pos = pos.getParentNode();
                    if (pos == null || (top != null && top.equals(pos))) {
                        nextNode = null;
                        break;
                    }
                }
            }
            pos = nextNode;
        }
        this.m_contentHandler.endDocument();
    }

    private final void dispatachChars(Node node) throws SAXException {
        if (this.m_contentHandler instanceof CharacterNodeHandler) {
            ((CharacterNodeHandler) this.m_contentHandler).characters(node);
            return;
        }
        String data = ((Text) node).getData();
        this.m_contentHandler.characters(data.toCharArray(), 0, data.length());
    }

    protected void startNode(Node node) throws SAXException {
        if (this.m_contentHandler instanceof NodeConsumer) {
            ((NodeConsumer) this.m_contentHandler).setOriginatingNode(node);
        }
        if (node instanceof Locator) {
            Locator loc = (Locator) node;
            this.m_locator.setColumnNumber(loc.getColumnNumber());
            this.m_locator.setLineNumber(loc.getLineNumber());
            this.m_locator.setPublicId(loc.getPublicId());
            this.m_locator.setSystemId(loc.getSystemId());
        } else {
            this.m_locator.setColumnNumber(0);
            this.m_locator.setLineNumber(0);
        }
        switch (node.getNodeType()) {
            case (short) 1:
                NamedNodeMap atts = ((Element) node).getAttributes();
                int nAttrs = atts.getLength();
                for (int i = 0; i < nAttrs; i++) {
                    Node attr = atts.item(i);
                    String attrName = attr.getNodeName();
                    if (attrName.equals("xmlns") || attrName.startsWith(Constants.ATTRNAME_XMLNS)) {
                        int index = attrName.indexOf(":");
                        this.m_contentHandler.startPrefixMapping(index < 0 ? "" : attrName.substring(index + 1), attr.getNodeValue());
                    }
                }
                String ns = this.m_dh.getNamespaceOfNode(node);
                if (ns == null) {
                    ns = "";
                }
                this.m_contentHandler.startElement(ns, this.m_dh.getLocalNameOfNode(node), node.getNodeName(), new AttList(atts, this.m_dh));
                return;
            case (short) 3:
                if (this.nextIsRaw) {
                    this.nextIsRaw = false;
                    this.m_contentHandler.processingInstruction("javax.xml.transform.disable-output-escaping", "");
                    dispatachChars(node);
                    this.m_contentHandler.processingInstruction("javax.xml.transform.enable-output-escaping", "");
                    return;
                }
                dispatachChars(node);
                return;
            case (short) 4:
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
            case (short) 5:
                EntityReference eref = (EntityReference) node;
                if (this.m_contentHandler instanceof LexicalHandler) {
                    ((LexicalHandler) this.m_contentHandler).startEntity(eref.getNodeName());
                    return;
                }
                return;
            case (short) 7:
                ProcessingInstruction pi = (ProcessingInstruction) node;
                if (pi.getNodeName().equals("xslt-next-is-raw")) {
                    this.nextIsRaw = true;
                    return;
                } else {
                    this.m_contentHandler.processingInstruction(pi.getNodeName(), pi.getData());
                    return;
                }
            case (short) 8:
                String data = ((Comment) node).getData();
                if (this.m_contentHandler instanceof LexicalHandler) {
                    this.m_contentHandler.comment(data.toCharArray(), 0, data.length());
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected void endNode(Node node) throws SAXException {
        switch (node.getNodeType()) {
            case (short) 1:
                String ns = this.m_dh.getNamespaceOfNode(node);
                if (ns == null) {
                    ns = "";
                }
                this.m_contentHandler.endElement(ns, this.m_dh.getLocalNameOfNode(node), node.getNodeName());
                NamedNodeMap atts = ((Element) node).getAttributes();
                int nAttrs = atts.getLength();
                for (int i = 0; i < nAttrs; i++) {
                    String attrName = atts.item(i).getNodeName();
                    if (attrName.equals("xmlns") || attrName.startsWith(Constants.ATTRNAME_XMLNS)) {
                        int index = attrName.indexOf(":");
                        this.m_contentHandler.endPrefixMapping(index < 0 ? "" : attrName.substring(index + 1));
                    }
                }
                return;
            case (short) 5:
                EntityReference eref = (EntityReference) node;
                if (this.m_contentHandler instanceof LexicalHandler) {
                    this.m_contentHandler.endEntity(eref.getNodeName());
                    return;
                }
                return;
            default:
                return;
        }
    }
}
