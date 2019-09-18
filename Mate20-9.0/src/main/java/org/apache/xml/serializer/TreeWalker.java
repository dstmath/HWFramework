package org.apache.xml.serializer;

import java.io.File;
import org.apache.xalan.templates.Constants;
import org.apache.xml.serializer.utils.AttList;
import org.apache.xml.serializer.utils.DOM2Helper;
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

public final class TreeWalker {
    private final SerializationHandler m_Serializer;
    private final ContentHandler m_contentHandler;
    protected final DOM2Helper m_dh;
    private final LocatorImpl m_locator;
    boolean nextIsRaw;

    public ContentHandler getContentHandler() {
        return this.m_contentHandler;
    }

    public TreeWalker(ContentHandler ch) {
        this(ch, null);
    }

    public TreeWalker(ContentHandler contentHandler, String systemId) {
        this.m_locator = new LocatorImpl();
        this.nextIsRaw = false;
        this.m_contentHandler = contentHandler;
        if (this.m_contentHandler instanceof SerializationHandler) {
            this.m_Serializer = (SerializationHandler) this.m_contentHandler;
        } else {
            this.m_Serializer = null;
        }
        this.m_contentHandler.setDocumentLocator(this.m_locator);
        if (systemId != null) {
            this.m_locator.setSystemId(systemId);
        } else {
            try {
                LocatorImpl locatorImpl = this.m_locator;
                locatorImpl.setSystemId(System.getProperty("user.dir") + File.separator + "dummy.xsl");
            } catch (SecurityException e) {
            }
        }
        if (this.m_contentHandler != null) {
            this.m_contentHandler.setDocumentLocator(this.m_locator);
        }
        try {
            LocatorImpl locatorImpl2 = this.m_locator;
            locatorImpl2.setSystemId(System.getProperty("user.dir") + File.separator + "dummy.xsl");
        } catch (SecurityException e2) {
        }
        this.m_dh = new DOM2Helper();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0032, code lost:
        r1 = null;
     */
    public void traverse(Node top) throws SAXException {
        this.m_contentHandler.startDocument();
        Node pos = top;
        while (pos != null) {
            startNode(pos);
            Node nextNode = pos.getFirstChild();
            while (true) {
                if (nextNode != null) {
                    break;
                }
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
                    }
                }
            }
            pos = nextNode;
        }
        this.m_contentHandler.endDocument();
    }

    public void traverse(Node pos, Node top) throws SAXException {
        this.m_contentHandler.startDocument();
        while (pos != null) {
            startNode(pos);
            Node nextNode = pos.getFirstChild();
            while (true) {
                if (nextNode != null) {
                    break;
                }
                endNode(pos);
                if (top != null && top.equals(pos)) {
                    break;
                }
                nextNode = pos.getNextSibling();
                if (nextNode == null) {
                    pos = pos.getParentNode();
                    if (pos == null || (top != null && top.equals(pos))) {
                        nextNode = null;
                    }
                }
            }
            pos = nextNode;
        }
        this.m_contentHandler.endDocument();
    }

    private final void dispatachChars(Node node) throws SAXException {
        if (this.m_Serializer != null) {
            this.m_Serializer.characters(node);
            return;
        }
        String data = ((Text) node).getData();
        this.m_contentHandler.characters(data.toCharArray(), 0, data.length());
    }

    /* access modifiers changed from: protected */
    public void startNode(Node node) throws SAXException {
        String prefix;
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
            case 1:
                Element elem_node = (Element) node;
                String uri = elem_node.getNamespaceURI();
                if (uri != null) {
                    String prefix2 = elem_node.getPrefix();
                    if (prefix2 == null) {
                        prefix2 = "";
                    }
                    this.m_contentHandler.startPrefixMapping(prefix2, uri);
                }
                NamedNodeMap atts = elem_node.getAttributes();
                int nAttrs = atts.getLength();
                for (int i = 0; i < nAttrs; i++) {
                    Node attr = atts.item(i);
                    String attrName = attr.getNodeName();
                    int colon = attrName.indexOf(58);
                    if (attrName.equals("xmlns") || attrName.startsWith(Constants.ATTRNAME_XMLNS)) {
                        if (colon < 0) {
                            prefix = "";
                        } else {
                            prefix = attrName.substring(colon + 1);
                        }
                        this.m_contentHandler.startPrefixMapping(prefix, attr.getNodeValue());
                    } else if (colon > 0) {
                        String prefix3 = attrName.substring(0, colon);
                        String uri2 = attr.getNamespaceURI();
                        if (uri2 != null) {
                            this.m_contentHandler.startPrefixMapping(prefix3, uri2);
                        }
                    }
                }
                String ns = this.m_dh.getNamespaceOfNode(node);
                if (ns == null) {
                    ns = "";
                }
                this.m_contentHandler.startElement(ns, this.m_dh.getLocalNameOfNode(node), node.getNodeName(), new AttList(atts, this.m_dh));
                return;
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
                EntityReference eref = (EntityReference) node;
                if (this.m_contentHandler instanceof LexicalHandler) {
                    ((LexicalHandler) this.m_contentHandler).startEntity(eref.getNodeName());
                    return;
                }
                return;
            case 7:
                ProcessingInstruction pi = (ProcessingInstruction) node;
                if (pi.getNodeName().equals("xslt-next-is-raw")) {
                    this.nextIsRaw = true;
                    return;
                } else {
                    this.m_contentHandler.processingInstruction(pi.getNodeName(), pi.getData());
                    return;
                }
            case 8:
                String data = ((Comment) node).getData();
                if (this.m_contentHandler instanceof LexicalHandler) {
                    ((LexicalHandler) this.m_contentHandler).comment(data.toCharArray(), 0, data.length());
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void endNode(Node node) throws SAXException {
        String prefix;
        short nodeType = node.getNodeType();
        if (nodeType == 1) {
            String ns = this.m_dh.getNamespaceOfNode(node);
            if (ns == null) {
                ns = "";
            }
            this.m_contentHandler.endElement(ns, this.m_dh.getLocalNameOfNode(node), node.getNodeName());
            if (this.m_Serializer == null) {
                Element elem_node = (Element) node;
                NamedNodeMap atts = elem_node.getAttributes();
                for (int i = atts.getLength() - 1; i >= 0; i--) {
                    String attrName = atts.item(i).getNodeName();
                    int colon = attrName.indexOf(58);
                    if (attrName.equals("xmlns") || attrName.startsWith(Constants.ATTRNAME_XMLNS)) {
                        if (colon < 0) {
                            prefix = "";
                        } else {
                            prefix = attrName.substring(colon + 1);
                        }
                        this.m_contentHandler.endPrefixMapping(prefix);
                    } else if (colon > 0) {
                        this.m_contentHandler.endPrefixMapping(attrName.substring(0, colon));
                    }
                }
                if (elem_node.getNamespaceURI() != null) {
                    String prefix2 = elem_node.getPrefix();
                    if (prefix2 == null) {
                        prefix2 = "";
                    }
                    this.m_contentHandler.endPrefixMapping(prefix2);
                }
            }
        } else if (nodeType != 9) {
            switch (nodeType) {
                case 5:
                    EntityReference eref = (EntityReference) node;
                    if (this.m_contentHandler instanceof LexicalHandler) {
                        ((LexicalHandler) this.m_contentHandler).endEntity(eref.getNodeName());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }
}
