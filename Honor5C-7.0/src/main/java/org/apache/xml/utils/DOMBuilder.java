package org.apache.xml.utils;

import java.io.Writer;
import java.util.Stack;
import java.util.Vector;
import org.apache.xalan.templates.Constants;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class DOMBuilder implements ContentHandler, LexicalHandler {
    protected Node m_currentNode;
    public Document m_doc;
    public DocumentFragment m_docFrag;
    protected Stack m_elemStack;
    protected boolean m_inCData;
    protected Node m_nextSibling;
    protected Vector m_prefixMappings;
    protected Node m_root;

    public DOMBuilder(Document doc, Node node) {
        this.m_currentNode = null;
        this.m_root = null;
        this.m_nextSibling = null;
        this.m_docFrag = null;
        this.m_elemStack = new Stack();
        this.m_prefixMappings = new Vector();
        this.m_inCData = false;
        this.m_doc = doc;
        this.m_root = node;
        this.m_currentNode = node;
        if (node instanceof Element) {
            this.m_elemStack.push(node);
        }
    }

    public DOMBuilder(Document doc, DocumentFragment docFrag) {
        this.m_currentNode = null;
        this.m_root = null;
        this.m_nextSibling = null;
        this.m_docFrag = null;
        this.m_elemStack = new Stack();
        this.m_prefixMappings = new Vector();
        this.m_inCData = false;
        this.m_doc = doc;
        this.m_docFrag = docFrag;
    }

    public DOMBuilder(Document doc) {
        this.m_currentNode = null;
        this.m_root = null;
        this.m_nextSibling = null;
        this.m_docFrag = null;
        this.m_elemStack = new Stack();
        this.m_prefixMappings = new Vector();
        this.m_inCData = false;
        this.m_doc = doc;
    }

    public Node getRootDocument() {
        return this.m_docFrag != null ? this.m_docFrag : this.m_doc;
    }

    public Node getRootNode() {
        return this.m_root;
    }

    public Node getCurrentNode() {
        return this.m_currentNode;
    }

    public void setNextSibling(Node nextSibling) {
        this.m_nextSibling = nextSibling;
    }

    public Node getNextSibling() {
        return this.m_nextSibling;
    }

    public Writer getWriter() {
        return null;
    }

    protected void append(Node newNode) throws SAXException {
        Node currentNode = this.m_currentNode;
        if (currentNode != null) {
            if (currentNode != this.m_root || this.m_nextSibling == null) {
                currentNode.appendChild(newNode);
            } else {
                currentNode.insertBefore(newNode, this.m_nextSibling);
            }
        } else if (this.m_docFrag == null) {
            boolean ok = true;
            short type = newNode.getNodeType();
            if (type == (short) 3) {
                String data = newNode.getNodeValue();
                if (data == null || data.trim().length() <= 0) {
                    ok = false;
                } else {
                    throw new SAXException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CANT_OUTPUT_TEXT_BEFORE_DOC, null));
                }
            } else if (type == (short) 1 && this.m_doc.getDocumentElement() != null) {
                throw new SAXException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CANT_HAVE_MORE_THAN_ONE_ROOT, null));
            }
            if (!ok) {
                return;
            }
            if (this.m_nextSibling != null) {
                this.m_doc.insertBefore(newNode, this.m_nextSibling);
            } else {
                this.m_doc.appendChild(newNode);
            }
        } else if (this.m_nextSibling != null) {
            this.m_docFrag.insertBefore(newNode, this.m_nextSibling);
        } else {
            this.m_docFrag.appendChild(newNode);
        }
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(String ns, String localName, String name, Attributes atts) throws SAXException {
        Element elem;
        if (ns == null || ns.length() == 0) {
            elem = this.m_doc.createElementNS(null, name);
        } else {
            elem = this.m_doc.createElementNS(ns, name);
        }
        append(elem);
        try {
            int i;
            int nAtts = atts.getLength();
            if (nAtts != 0) {
                for (i = 0; i < nAtts; i++) {
                    if (atts.getType(i).equalsIgnoreCase("ID")) {
                        setIDAttribute(atts.getValue(i), elem);
                    }
                    String attrNS = atts.getURI(i);
                    if (SerializerConstants.EMPTYSTRING.equals(attrNS)) {
                        attrNS = null;
                    }
                    String attrQName = atts.getQName(i);
                    if (attrQName.startsWith(Constants.ATTRNAME_XMLNS) || attrQName.equals(SerializerConstants.XMLNS_PREFIX)) {
                        attrNS = SerializerConstants.XMLNS_URI;
                    }
                    elem.setAttributeNS(attrNS, attrQName, atts.getValue(i));
                }
            }
            int nDecls = this.m_prefixMappings.size();
            for (i = 0; i < nDecls; i += 2) {
                String prefix = (String) this.m_prefixMappings.elementAt(i);
                if (prefix != null) {
                    elem.setAttributeNS(SerializerConstants.XMLNS_URI, prefix, (String) this.m_prefixMappings.elementAt(i + 1));
                }
            }
            this.m_prefixMappings.clear();
            this.m_elemStack.push(elem);
            this.m_currentNode = elem;
        } catch (Exception de) {
            throw new SAXException(de);
        }
    }

    public void endElement(String ns, String localName, String name) throws SAXException {
        this.m_elemStack.pop();
        this.m_currentNode = this.m_elemStack.isEmpty() ? null : (Node) this.m_elemStack.peek();
    }

    public void setIDAttribute(String id, Element elem) {
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        Node childNode = null;
        if (!isOutsideDocElem() || !XMLCharacterRecognizer.isWhiteSpace(ch, start, length)) {
            if (this.m_inCData) {
                cdata(ch, start, length);
                return;
            }
            String s = new String(ch, start, length);
            if (this.m_currentNode != null) {
                childNode = this.m_currentNode.getLastChild();
            }
            if (childNode == null || childNode.getNodeType() != (short) 3) {
                append(this.m_doc.createTextNode(s));
            } else {
                ((Text) childNode).appendData(s);
            }
        }
    }

    public void charactersRaw(char[] ch, int start, int length) throws SAXException {
        if (!isOutsideDocElem() || !XMLCharacterRecognizer.isWhiteSpace(ch, start, length)) {
            String s = new String(ch, start, length);
            append(this.m_doc.createProcessingInstruction("xslt-next-is-raw", "formatter-to-dom"));
            append(this.m_doc.createTextNode(s));
        }
    }

    public void startEntity(String name) throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void entityReference(String name) throws SAXException {
        append(this.m_doc.createEntityReference(name));
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (!isOutsideDocElem()) {
            append(this.m_doc.createTextNode(new String(ch, start, length)));
        }
    }

    private boolean isOutsideDocElem() {
        return this.m_docFrag == null && this.m_elemStack.size() == 0 && (this.m_currentNode == null || this.m_currentNode.getNodeType() == (short) 9);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        append(this.m_doc.createProcessingInstruction(target, data));
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        append(this.m_doc.createComment(new String(ch, start, length)));
    }

    public void startCDATA() throws SAXException {
        this.m_inCData = true;
        append(this.m_doc.createCDATASection(SerializerConstants.EMPTYSTRING));
    }

    public void endCDATA() throws SAXException {
        this.m_inCData = false;
    }

    public void cdata(char[] ch, int start, int length) throws SAXException {
        if (!isOutsideDocElem() || !XMLCharacterRecognizer.isWhiteSpace(ch, start, length)) {
            ((CDATASection) this.m_currentNode.getLastChild()).appendData(new String(ch, start, length));
        }
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (prefix == null || prefix.equals(SerializerConstants.EMPTYSTRING)) {
            prefix = SerializerConstants.XMLNS_PREFIX;
        } else {
            prefix = Constants.ATTRNAME_XMLNS + prefix;
        }
        this.m_prefixMappings.addElement(prefix);
        this.m_prefixMappings.addElement(uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }
}
