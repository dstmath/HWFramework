package ohos.com.sun.org.apache.xml.internal.serializer;

import ohos.com.sun.org.apache.xml.internal.utils.AttList;
import ohos.com.sun.org.apache.xml.internal.utils.DOM2Helper;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.EntityReference;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.Text;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;
import ohos.org.xml.sax.helpers.LocatorImpl;

public final class TreeWalker {
    private final SerializationHandler m_Serializer;
    private final ContentHandler m_contentHandler;
    private final LocatorImpl m_locator;
    boolean nextIsRaw;

    public ContentHandler getContentHandler() {
        return this.m_contentHandler;
    }

    public TreeWalker(ContentHandler contentHandler) {
        this(contentHandler, null);
    }

    public TreeWalker(ContentHandler contentHandler, String str) {
        this.m_locator = new LocatorImpl();
        this.nextIsRaw = false;
        this.m_contentHandler = contentHandler;
        SerializationHandler serializationHandler = this.m_contentHandler;
        if (serializationHandler instanceof SerializationHandler) {
            this.m_Serializer = serializationHandler;
        } else {
            this.m_Serializer = null;
        }
        this.m_contentHandler.setDocumentLocator(this.m_locator);
        if (str != null) {
            this.m_locator.setSystemId(str);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002d, code lost:
        if (r0 == null) goto L_0x0032;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002f, code lost:
        endNode(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0032, code lost:
        r0 = null;
     */
    public void traverse(Node node) throws SAXException {
        this.m_contentHandler.startDocument();
        Node node2 = node;
        while (node2 != null) {
            startNode(node2);
            Node firstChild = node2.getFirstChild();
            while (true) {
                if (firstChild != null) {
                    break;
                }
                endNode(node2);
                if (node.equals(node2)) {
                    break;
                }
                firstChild = node2.getNextSibling();
                if (firstChild != null || ((node2 = node2.getParentNode()) != null && !node.equals(node2))) {
                }
            }
            node2 = firstChild;
        }
        this.m_contentHandler.endDocument();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0030, code lost:
        r3 = null;
     */
    public void traverse(Node node, Node node2) throws SAXException {
        this.m_contentHandler.startDocument();
        while (node != null) {
            startNode(node);
            Node firstChild = node.getFirstChild();
            while (true) {
                if (firstChild != null) {
                    break;
                }
                endNode(node);
                if (node2 != null && node2.equals(node)) {
                    break;
                }
                firstChild = node.getNextSibling();
                if (firstChild != null || ((node = node.getParentNode()) != null && (node2 == null || !node2.equals(node)))) {
                }
            }
            node = firstChild;
        }
        this.m_contentHandler.endDocument();
    }

    private final void dispatachChars(Node node) throws SAXException {
        SerializationHandler serializationHandler = this.m_Serializer;
        if (serializationHandler != null) {
            serializationHandler.characters(node);
            return;
        }
        String data = ((Text) node).getData();
        this.m_contentHandler.characters(data.toCharArray(), 0, data.length());
    }

    /* access modifiers changed from: protected */
    public void startNode(Node node) throws SAXException {
        String str;
        if (node instanceof Locator) {
            Locator locator = (Locator) node;
            this.m_locator.setColumnNumber(locator.getColumnNumber());
            this.m_locator.setLineNumber(locator.getLineNumber());
            this.m_locator.setPublicId(locator.getPublicId());
            this.m_locator.setSystemId(locator.getSystemId());
        } else {
            this.m_locator.setColumnNumber(0);
            this.m_locator.setLineNumber(0);
        }
        short nodeType = node.getNodeType();
        if (nodeType == 1) {
            Element element = (Element) node;
            String namespaceURI = element.getNamespaceURI();
            if (namespaceURI != null) {
                String prefix = element.getPrefix();
                if (prefix == null) {
                    prefix = "";
                }
                this.m_contentHandler.startPrefixMapping(prefix, namespaceURI);
            }
            NamedNodeMap attributes = element.getAttributes();
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                Node item = attributes.item(i);
                String nodeName = item.getNodeName();
                int indexOf = nodeName.indexOf(58);
                if (nodeName.equals("xmlns") || nodeName.startsWith("xmlns:")) {
                    if (indexOf < 0) {
                        str = "";
                    } else {
                        str = nodeName.substring(indexOf + 1);
                    }
                    this.m_contentHandler.startPrefixMapping(str, item.getNodeValue());
                } else if (indexOf > 0) {
                    String substring = nodeName.substring(0, indexOf);
                    String namespaceURI2 = item.getNamespaceURI();
                    if (namespaceURI2 != null) {
                        this.m_contentHandler.startPrefixMapping(substring, namespaceURI2);
                    }
                }
            }
            String namespaceOfNode = DOM2Helper.getNamespaceOfNode(node);
            if (namespaceOfNode == null) {
                namespaceOfNode = "";
            }
            this.m_contentHandler.startElement(namespaceOfNode, DOM2Helper.getLocalNameOfNode(node), node.getNodeName(), new AttList(attributes));
        } else if (nodeType != 11) {
            if (nodeType != 3) {
                if (nodeType == 4) {
                    LexicalHandler lexicalHandler = this.m_contentHandler;
                    boolean z = lexicalHandler instanceof LexicalHandler;
                    LexicalHandler lexicalHandler2 = z ? lexicalHandler : null;
                    if (z) {
                        lexicalHandler2.startCDATA();
                    }
                    dispatachChars(node);
                    if (z) {
                        lexicalHandler2.endCDATA();
                    }
                } else if (nodeType == 5) {
                    EntityReference entityReference = (EntityReference) node;
                    LexicalHandler lexicalHandler3 = this.m_contentHandler;
                    if (lexicalHandler3 instanceof LexicalHandler) {
                        lexicalHandler3.startEntity(entityReference.getNodeName());
                    }
                } else if (nodeType == 7) {
                    ProcessingInstruction processingInstruction = (ProcessingInstruction) node;
                    if (processingInstruction.getNodeName().equals("xslt-next-is-raw")) {
                        this.nextIsRaw = true;
                    } else {
                        this.m_contentHandler.processingInstruction(processingInstruction.getNodeName(), processingInstruction.getData());
                    }
                } else if (nodeType == 8) {
                    String data = ((Comment) node).getData();
                    LexicalHandler lexicalHandler4 = this.m_contentHandler;
                    if (lexicalHandler4 instanceof LexicalHandler) {
                        lexicalHandler4.comment(data.toCharArray(), 0, data.length());
                    }
                }
            } else if (this.nextIsRaw) {
                this.nextIsRaw = false;
                this.m_contentHandler.processingInstruction("javax.xml.transform.disable-output-escaping", "");
                dispatachChars(node);
                this.m_contentHandler.processingInstruction("javax.xml.transform.enable-output-escaping", "");
            } else {
                dispatachChars(node);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void endNode(Node node) throws SAXException {
        String str;
        short nodeType = node.getNodeType();
        if (nodeType == 1) {
            String namespaceOfNode = DOM2Helper.getNamespaceOfNode(node);
            if (namespaceOfNode == null) {
                namespaceOfNode = "";
            }
            this.m_contentHandler.endElement(namespaceOfNode, DOM2Helper.getLocalNameOfNode(node), node.getNodeName());
            if (this.m_Serializer == null) {
                Element element = (Element) node;
                NamedNodeMap attributes = element.getAttributes();
                for (int length = attributes.getLength() - 1; length >= 0; length--) {
                    String nodeName = attributes.item(length).getNodeName();
                    int indexOf = nodeName.indexOf(58);
                    if (nodeName.equals("xmlns") || nodeName.startsWith("xmlns:")) {
                        if (indexOf < 0) {
                            str = "";
                        } else {
                            str = nodeName.substring(indexOf + 1);
                        }
                        this.m_contentHandler.endPrefixMapping(str);
                    } else if (indexOf > 0) {
                        this.m_contentHandler.endPrefixMapping(nodeName.substring(0, indexOf));
                    }
                }
                if (element.getNamespaceURI() != null) {
                    String prefix = element.getPrefix();
                    if (prefix == null) {
                        prefix = "";
                    }
                    this.m_contentHandler.endPrefixMapping(prefix);
                }
            }
        } else if (nodeType != 9 && nodeType != 4 && nodeType == 5) {
            EntityReference entityReference = (EntityReference) node;
            LexicalHandler lexicalHandler = this.m_contentHandler;
            if (lexicalHandler instanceof LexicalHandler) {
                lexicalHandler.endEntity(entityReference.getNodeName());
            }
        }
    }
}
