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
        if (source != null) {
            String inputEncoding = source.getEncoding();
            String systemId = source.getSystemId();
            DocumentImpl document = new DocumentImpl(dom, null, null, null, inputEncoding);
            document.setDocumentURI(systemId);
            KXmlParser parser = new KXmlParser();
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
                if (parser.nextToken() != 1) {
                    parse(parser, document, document, 1);
                    parser.require(1, null, null);
                    IoUtils.closeQuietly((AutoCloseable) parser);
                    return document;
                }
                throw new SAXParseException("Unexpected end of document", null);
            } catch (XmlPullParserException ex) {
                Throwable detail = ex.getDetail();
                if (detail instanceof IOException) {
                    throw ((IOException) detail);
                } else if (!(detail instanceof RuntimeException)) {
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
                } else {
                    throw ((RuntimeException) detail);
                }
            } catch (Throwable th) {
                IoUtils.closeQuietly((AutoCloseable) parser);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("source == null");
        }
    }

    /* JADX WARNING: type inference failed for: r20v0, types: [org.w3c.dom.Node] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void parse(KXmlParser parser, DocumentImpl document, Node r20, int endToken) throws XmlPullParserException, IOException {
        KXmlParser kXmlParser = parser;
        DocumentImpl documentImpl = document;
        DocumentImpl documentImpl2 = r20;
        int token = parser.getEventType();
        while (token != endToken && token != 1) {
            int i = 0;
            if (token == 8) {
                String text = parser.getText();
                int dot = text.indexOf(32);
                documentImpl2.appendChild(documentImpl.createProcessingInstruction(dot != -1 ? text.substring(0, dot) : text, dot != -1 ? text.substring(dot + 1) : ""));
            } else if (token == 10) {
                documentImpl.appendChild(new DocumentTypeImpl(documentImpl, parser.getRootElementName(), parser.getPublicId(), parser.getSystemId()));
            } else if (token == 9) {
                if (!this.ignoreComments) {
                    documentImpl2.appendChild(documentImpl.createComment(parser.getText()));
                }
            } else if (token == 7) {
                if (!this.ignoreElementContentWhitespace && documentImpl != documentImpl2) {
                    appendText(documentImpl, documentImpl2, token, parser.getText());
                }
            } else if (token == 4 || token == 5) {
                appendText(documentImpl, documentImpl2, token, parser.getText());
            } else if (token == 6) {
                String entity = parser.getName();
                EntityResolver entityResolver2 = this.entityResolver;
                String resolved = resolvePredefinedOrCharacterEntity(entity);
                if (resolved != null) {
                    appendText(documentImpl, documentImpl2, token, resolved);
                } else {
                    documentImpl2.appendChild(documentImpl.createEntityReference(entity));
                }
            } else if (token == 2) {
                if (this.namespaceAware) {
                    String namespace = parser.getNamespace();
                    String name = parser.getName();
                    String prefix = parser.getPrefix();
                    if ("".equals(namespace)) {
                        namespace = null;
                    }
                    Element element = documentImpl.createElementNS(namespace, name);
                    element.setPrefix(prefix);
                    documentImpl2.appendChild(element);
                    while (i < parser.getAttributeCount()) {
                        String attrNamespace = kXmlParser.getAttributeNamespace(i);
                        String attrPrefix = kXmlParser.getAttributePrefix(i);
                        String attrName = kXmlParser.getAttributeName(i);
                        String attrValue = kXmlParser.getAttributeValue(i);
                        if ("".equals(attrNamespace)) {
                            attrNamespace = null;
                        }
                        Attr attr = documentImpl.createAttributeNS(attrNamespace, attrName);
                        attr.setPrefix(attrPrefix);
                        attr.setValue(attrValue);
                        element.setAttributeNodeNS(attr);
                        i++;
                    }
                    int token2 = parser.nextToken();
                    parse(kXmlParser, documentImpl, element, 3);
                    kXmlParser.require(3, namespace, name);
                } else {
                    String name2 = parser.getName();
                    Element element2 = documentImpl.createElement(name2);
                    documentImpl2.appendChild(element2);
                    while (i < parser.getAttributeCount()) {
                        String attrName2 = kXmlParser.getAttributeName(i);
                        String attrValue2 = kXmlParser.getAttributeValue(i);
                        Attr attr2 = documentImpl.createAttribute(attrName2);
                        attr2.setValue(attrValue2);
                        element2.setAttributeNode(attr2);
                        i++;
                    }
                    int token3 = parser.nextToken();
                    parse(kXmlParser, documentImpl, element2, 3);
                    kXmlParser.require(3, "", name2);
                }
            }
            token = parser.nextToken();
        }
    }

    private void appendText(DocumentImpl document, Node parent, int token, String text) {
        Node node;
        if (!text.isEmpty()) {
            if (this.coalescing || token != 5) {
                Node lastChild = parent.getLastChild();
                if (lastChild != null && lastChild.getNodeType() == 3) {
                    ((Text) lastChild).appendData(text);
                    return;
                }
            }
            if (token == 5) {
                node = new CDATASectionImpl(document, text);
            } else {
                node = new TextImpl(document, text);
            }
            parent.appendChild(node);
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
