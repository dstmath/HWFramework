package org.apache.harmony.xml.parsers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import libcore.io.IoUtils;
import org.apache.harmony.xml.dom.CDATASectionImpl;
import org.apache.harmony.xml.dom.DOMImplementationImpl;
import org.apache.harmony.xml.dom.DocumentImpl;
import org.apache.harmony.xml.dom.DocumentTypeImpl;
import org.apache.harmony.xml.dom.TextImpl;
import org.kxml2.io.KXmlParser;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class DocumentBuilderImpl extends DocumentBuilder {
    private static DOMImplementationImpl dom = DOMImplementationImpl.getInstance();
    private boolean coalescing;
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;
    private boolean ignoreComments;
    private boolean ignoreElementContentWhitespace;
    private boolean namespaceAware;

    DocumentBuilderImpl() {
    }

    public void reset() {
        this.coalescing = false;
        this.entityResolver = null;
        this.errorHandler = null;
        this.ignoreComments = false;
        this.ignoreElementContentWhitespace = false;
        this.namespaceAware = false;
    }

    public DOMImplementation getDOMImplementation() {
        return dom;
    }

    public boolean isNamespaceAware() {
        return this.namespaceAware;
    }

    public boolean isValidating() {
        return false;
    }

    public Document newDocument() {
        return dom.createDocument(null, null, null);
    }

    public Document parse(InputSource source) throws SAXException, IOException {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        String inputEncoding = source.getEncoding();
        String systemId = source.getSystemId();
        DocumentImpl document = new DocumentImpl(dom, null, null, null, inputEncoding);
        document.setDocumentURI(systemId);
        AutoCloseable parser = new KXmlParser();
        try {
            parser.keepNamespaceAttributes();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, this.namespaceAware);
            if (source.getByteStream() != null) {
                parser.setInput(source.getByteStream(), inputEncoding);
            } else if (source.getCharacterStream() != null) {
                parser.setInput(source.getCharacterStream());
            } else if (systemId != null) {
                URLConnection urlConnection = new URL(systemId).openConnection();
                urlConnection.connect();
                parser.setInput(urlConnection.getInputStream(), inputEncoding);
            } else {
                throw new SAXParseException("InputSource needs a stream, reader or URI", null);
            }
            if (parser.nextToken() == 1) {
                throw new SAXParseException("Unexpected end of document", null);
            }
            parse(parser, document, document, 1);
            parser.require(1, null, null);
            IoUtils.closeQuietly(parser);
            return document;
        } catch (XmlPullParserException ex) {
            Throwable detail = ex.getDetail();
            if (detail instanceof IOException) {
                throw ((IOException) detail);
            } else if (detail instanceof RuntimeException) {
                throw ((RuntimeException) detail);
            } else {
                LocatorImpl locator = new LocatorImpl();
                locator.setPublicId(source.getPublicId());
                locator.setSystemId(systemId);
                locator.setLineNumber(ex.getLineNumber());
                locator.setColumnNumber(ex.getColumnNumber());
                SAXParseException newEx = new SAXParseException(ex.getMessage(), locator);
                if (this.errorHandler != null) {
                    this.errorHandler.error(newEx);
                }
                throw newEx;
            }
        } catch (Throwable th) {
            IoUtils.closeQuietly(parser);
        }
    }

    private void parse(KXmlParser parser, DocumentImpl document, Node node, int endToken) throws XmlPullParserException, IOException {
        int token = parser.getEventType();
        while (token != endToken && token != 1) {
            if (token == 8) {
                String text = parser.getText();
                int dot = text.indexOf(32);
                node.appendChild(document.createProcessingInstruction(dot != -1 ? text.substring(0, dot) : text, dot != -1 ? text.substring(dot + 1) : ""));
            } else if (token == 10) {
                document.appendChild(new DocumentTypeImpl(document, parser.getRootElementName(), parser.getPublicId(), parser.getSystemId()));
            } else if (token == 9) {
                if (!this.ignoreComments) {
                    node.appendChild(document.createComment(parser.getText()));
                }
            } else if (token == 7) {
                if (!(this.ignoreElementContentWhitespace || document == node)) {
                    appendText(document, node, token, parser.getText());
                }
            } else if (token == 4 || token == 5) {
                appendText(document, node, token, parser.getText());
            } else if (token == 6) {
                String entity = parser.getName();
                EntityResolver entityResolver = this.entityResolver;
                String resolved = resolvePredefinedOrCharacterEntity(entity);
                if (resolved != null) {
                    appendText(document, node, token, resolved);
                } else {
                    node.appendChild(document.createEntityReference(entity));
                }
            } else if (token == 2) {
                String name;
                Element element;
                int i;
                String attrName;
                String attrValue;
                Attr attr;
                if (this.namespaceAware) {
                    String namespace = parser.getNamespace();
                    name = parser.getName();
                    String prefix = parser.getPrefix();
                    if ("".equals(namespace)) {
                        namespace = null;
                    }
                    element = document.createElementNS(namespace, name);
                    element.setPrefix(prefix);
                    node.appendChild(element);
                    for (i = 0; i < parser.getAttributeCount(); i++) {
                        String attrNamespace = parser.getAttributeNamespace(i);
                        String attrPrefix = parser.getAttributePrefix(i);
                        attrName = parser.getAttributeName(i);
                        attrValue = parser.getAttributeValue(i);
                        if ("".equals(attrNamespace)) {
                            attrNamespace = null;
                        }
                        attr = document.createAttributeNS(attrNamespace, attrName);
                        attr.setPrefix(attrPrefix);
                        attr.setValue(attrValue);
                        element.setAttributeNodeNS(attr);
                    }
                    token = parser.nextToken();
                    parse(parser, document, element, 3);
                    parser.require(3, namespace, name);
                } else {
                    name = parser.getName();
                    element = document.createElement(name);
                    node.appendChild(element);
                    for (i = 0; i < parser.getAttributeCount(); i++) {
                        attrName = parser.getAttributeName(i);
                        attrValue = parser.getAttributeValue(i);
                        attr = document.createAttribute(attrName);
                        attr.setValue(attrValue);
                        element.setAttributeNode(attr);
                    }
                    token = parser.nextToken();
                    parse(parser, document, element, 3);
                    parser.require(3, "", name);
                }
            }
            token = parser.nextToken();
        }
    }

    private void appendText(DocumentImpl document, Node parent, int token, String text) {
        if (!text.isEmpty()) {
            Node cDATASectionImpl;
            if (this.coalescing || token != 5) {
                Node lastChild = parent.getLastChild();
                if (lastChild != null && lastChild.getNodeType() == (short) 3) {
                    ((Text) lastChild).appendData(text);
                    return;
                }
            }
            if (token == 5) {
                cDATASectionImpl = new CDATASectionImpl(document, text);
            } else {
                cDATASectionImpl = new TextImpl(document, text);
            }
            parent.appendChild(cDATASectionImpl);
        }
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    public void setIgnoreComments(boolean value) {
        this.ignoreComments = value;
    }

    public void setCoalescing(boolean value) {
        this.coalescing = value;
    }

    public void setIgnoreElementContentWhitespace(boolean value) {
        this.ignoreElementContentWhitespace = value;
    }

    public void setNamespaceAware(boolean value) {
        this.namespaceAware = value;
    }

    private String resolvePredefinedOrCharacterEntity(String entityName) {
        if (entityName.startsWith("#x")) {
            return resolveCharacterReference(entityName.substring(2), 16);
        }
        if (entityName.startsWith("#")) {
            return resolveCharacterReference(entityName.substring(1), 10);
        }
        if ("lt".equals(entityName)) {
            return "<";
        }
        if ("gt".equals(entityName)) {
            return ">";
        }
        if ("amp".equals(entityName)) {
            return "&";
        }
        if ("apos".equals(entityName)) {
            return "'";
        }
        if ("quot".equals(entityName)) {
            return "\"";
        }
        return null;
    }

    private String resolveCharacterReference(String value, int base) {
        try {
            int codePoint = Integer.parseInt(value, base);
            if (Character.isBmpCodePoint(codePoint)) {
                return String.valueOf((char) codePoint);
            }
            return new String(Character.toChars(codePoint));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
