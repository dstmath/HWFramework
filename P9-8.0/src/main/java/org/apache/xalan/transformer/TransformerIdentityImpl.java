package org.apache.xalan.transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.TreeWalker;
import org.apache.xml.utils.DOMBuilder;
import org.apache.xml.utils.DefaultErrorHandler;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xml.utils.XMLReaderManager;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public class TransformerIdentityImpl extends Transformer implements TransformerHandler, DeclHandler {
    URIResolver m_URIResolver;
    private ErrorListener m_errorListener;
    boolean m_flushedStartDoc;
    boolean m_foundFirstElement;
    private boolean m_isSecureProcessing;
    private OutputProperties m_outputFormat;
    private FileOutputStream m_outputStream;
    private Hashtable m_params;
    private Result m_result;
    private ContentHandler m_resultContentHandler;
    private DTDHandler m_resultDTDHandler;
    private DeclHandler m_resultDeclHandler;
    private LexicalHandler m_resultLexicalHandler;
    private Serializer m_serializer;
    private String m_systemID;

    public TransformerIdentityImpl(boolean isSecureProcessing) {
        this.m_flushedStartDoc = false;
        this.m_outputStream = null;
        this.m_errorListener = new DefaultErrorHandler(false);
        this.m_isSecureProcessing = false;
        this.m_outputFormat = new OutputProperties("xml");
        this.m_isSecureProcessing = isSecureProcessing;
    }

    public TransformerIdentityImpl() {
        this(false);
    }

    public void setResult(Result result) throws IllegalArgumentException {
        if (result == null) {
            throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_RESULT_NULL, null));
        }
        this.m_result = result;
    }

    public void setSystemId(String systemID) {
        this.m_systemID = systemID;
    }

    public String getSystemId() {
        return this.m_systemID;
    }

    public Transformer getTransformer() {
        return this;
    }

    public void reset() {
        this.m_flushedStartDoc = false;
        this.m_foundFirstElement = false;
        this.m_outputStream = null;
        clearParameters();
        this.m_result = null;
        this.m_resultContentHandler = null;
        this.m_resultDeclHandler = null;
        this.m_resultDTDHandler = null;
        this.m_resultLexicalHandler = null;
        this.m_serializer = null;
        this.m_systemID = null;
        this.m_URIResolver = null;
        this.m_outputFormat = new OutputProperties("xml");
    }

    private void createResultContentHandler(Result outputTarget) throws TransformerException {
        if (outputTarget instanceof SAXResult) {
            SAXResult saxResult = (SAXResult) outputTarget;
            this.m_resultContentHandler = saxResult.getHandler();
            this.m_resultLexicalHandler = saxResult.getLexicalHandler();
            if (this.m_resultContentHandler instanceof Serializer) {
                this.m_serializer = (Serializer) this.m_resultContentHandler;
            }
        } else if (outputTarget instanceof DOMResult) {
            short type;
            Document doc;
            DOMBuilder domBuilder;
            DOMResult domResult = (DOMResult) outputTarget;
            Node outputNode = domResult.getNode();
            Node nextSibling = domResult.getNextSibling();
            if (outputNode != null) {
                type = outputNode.getNodeType();
                doc = (short) 9 == type ? (Document) outputNode : outputNode.getOwnerDocument();
            } else {
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    if (this.m_isSecureProcessing) {
                        try {
                            dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
                        } catch (ParserConfigurationException e) {
                        }
                    }
                    doc = dbf.newDocumentBuilder().newDocument();
                    Object outputNode2 = doc;
                    type = doc.getNodeType();
                    ((DOMResult) outputTarget).setNode(doc);
                } catch (ParserConfigurationException pce) {
                    throw new TransformerException(pce);
                }
            }
            if ((short) 11 == type) {
                domBuilder = new DOMBuilder(doc, (DocumentFragment) outputNode2);
            } else {
                domBuilder = new DOMBuilder(doc, outputNode2);
            }
            if (nextSibling != null) {
                domBuilder.setNextSibling(nextSibling);
            }
            this.m_resultContentHandler = domBuilder;
            this.m_resultLexicalHandler = domBuilder;
        } else if (outputTarget instanceof StreamResult) {
            StreamResult sresult = (StreamResult) outputTarget;
            try {
                Serializer serializer = SerializerFactory.getSerializer(this.m_outputFormat.getProperties());
                this.m_serializer = serializer;
                if (sresult.getWriter() != null) {
                    serializer.setWriter(sresult.getWriter());
                } else if (sresult.getOutputStream() != null) {
                    serializer.setOutputStream(sresult.getOutputStream());
                } else if (sresult.getSystemId() != null) {
                    String fileURL = sresult.getSystemId();
                    if (fileURL.startsWith("file:///")) {
                        if (fileURL.substring(8).indexOf(":") > 0) {
                            fileURL = fileURL.substring(8);
                        } else {
                            fileURL = fileURL.substring(7);
                        }
                    } else if (fileURL.startsWith("file:/")) {
                        if (fileURL.substring(6).indexOf(":") > 0) {
                            fileURL = fileURL.substring(6);
                        } else {
                            fileURL = fileURL.substring(5);
                        }
                    }
                    this.m_outputStream = new FileOutputStream(fileURL);
                    serializer.setOutputStream(this.m_outputStream);
                } else {
                    throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_OUTPUT_SPECIFIED, null));
                }
                this.m_resultContentHandler = serializer.asContentHandler();
            } catch (IOException ioe) {
                throw new TransformerException(ioe);
            }
        } else {
            throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_TO_RESULT_TYPE, new Object[]{outputTarget.getClass().getName()}));
        }
        if (this.m_resultContentHandler instanceof DTDHandler) {
            this.m_resultDTDHandler = (DTDHandler) this.m_resultContentHandler;
        }
        if (this.m_resultContentHandler instanceof DeclHandler) {
            this.m_resultDeclHandler = (DeclHandler) this.m_resultContentHandler;
        }
        if (this.m_resultContentHandler instanceof LexicalHandler) {
            this.m_resultLexicalHandler = (LexicalHandler) this.m_resultContentHandler;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:122:0x023f A:{Splitter: B:93:0x01c4, PHI: r12 r14 , ExcHandler: org.apache.xml.utils.WrappedRuntimeException (r21_0 'wre' org.apache.xml.utils.WrappedRuntimeException)} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0264 A:{Splitter: B:80:0x0191, PHI: r12 r14 , ExcHandler: java.io.IOException (r11_2 'ioe' java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x023f A:{Splitter: B:93:0x01c4, PHI: r12 r14 , ExcHandler: org.apache.xml.utils.WrappedRuntimeException (r21_0 'wre' org.apache.xml.utils.WrappedRuntimeException)} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0264 A:{Splitter: B:80:0x0191, PHI: r12 r14 , ExcHandler: java.io.IOException (r11_2 'ioe' java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x023f A:{Splitter: B:93:0x01c4, PHI: r12 r14 , ExcHandler: org.apache.xml.utils.WrappedRuntimeException (r21_0 'wre' org.apache.xml.utils.WrappedRuntimeException)} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0264 A:{Splitter: B:80:0x0191, PHI: r12 r14 , ExcHandler: java.io.IOException (r11_2 'ioe' java.io.IOException)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:118:0x0236, code:
            r15 = move-exception;
     */
    /* JADX WARNING: Missing block: B:121:0x023e, code:
            throw new javax.xml.transform.TransformerException(r15);
     */
    /* JADX WARNING: Missing block: B:122:0x023f, code:
            r21 = move-exception;
     */
    /* JADX WARNING: Missing block: B:124:?, code:
            r19 = r21.getException();
     */
    /* JADX WARNING: Missing block: B:126:0x024a, code:
            if ((r19 instanceof org.apache.xml.utils.WrappedRuntimeException) != false) goto L_0x024c;
     */
    /* JADX WARNING: Missing block: B:127:0x024c, code:
            r19 = ((org.apache.xml.utils.WrappedRuntimeException) r19).getException();
     */
    /* JADX WARNING: Missing block: B:132:0x0264, code:
            r11 = move-exception;
     */
    /* JADX WARNING: Missing block: B:135:0x026c, code:
            throw new javax.xml.transform.TransformerException(r11);
     */
    /* JADX WARNING: Missing block: B:137:0x026e, code:
            if (r12 != false) goto L_0x0270;
     */
    /* JADX WARNING: Missing block: B:139:?, code:
            org.apache.xml.utils.XMLReaderManager.getInstance().releaseXMLReader(r14);
     */
    /* JADX WARNING: Missing block: B:141:0x027a, code:
            r15 = move-exception;
     */
    /* JADX WARNING: Missing block: B:144:0x0282, code:
            throw new javax.xml.transform.TransformerException(r15);
     */
    /* JADX WARNING: Missing block: B:146:0x028c, code:
            throw new javax.xml.transform.TransformerException(r21.getException());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void transform(Source source, Result outputTarget) throws TransformerException {
        ParserConfigurationException e;
        createResultContentHandler(outputTarget);
        if (((source instanceof StreamSource) && source.getSystemId() == null && ((StreamSource) source).getInputStream() == null && ((StreamSource) source).getReader() == null) || (((source instanceof SAXSource) && ((SAXSource) source).getInputSource() == null && ((SAXSource) source).getXMLReader() == null) || ((source instanceof DOMSource) && ((DOMSource) source).getNode() == null))) {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                String systemID = source.getSystemId();
                Source dOMSource = new DOMSource(builder.newDocument());
                if (systemID != null) {
                    try {
                        dOMSource.setSystemId(systemID);
                    } catch (ParserConfigurationException e2) {
                        e = e2;
                        source = dOMSource;
                        throw new TransformerException(e.getMessage());
                    }
                }
                source = dOMSource;
            } catch (ParserConfigurationException e3) {
                e = e3;
                throw new TransformerException(e.getMessage());
            }
        }
        try {
            if (source instanceof DOMSource) {
                DOMSource dsource = (DOMSource) source;
                this.m_systemID = dsource.getSystemId();
                Node dNode = dsource.getNode();
                if (dNode != null) {
                    try {
                        if (dNode.getNodeType() == (short) 2) {
                            startDocument();
                        }
                        if (dNode.getNodeType() == (short) 2) {
                            char[] chars = dNode.getNodeValue().toCharArray();
                            characters(chars, 0, chars.length);
                        } else {
                            new TreeWalker(this, this.m_systemID).traverse(dNode);
                        }
                        if (dNode.getNodeType() == (short) 2) {
                            endDocument();
                        }
                        if (this.m_outputStream != null) {
                            try {
                                this.m_outputStream.close();
                            } catch (IOException e4) {
                            }
                            this.m_outputStream = null;
                        }
                        return;
                    } catch (SAXException se) {
                        throw new TransformerException(se);
                    } catch (Throwable th) {
                        if (dNode.getNodeType() == (short) 2) {
                            endDocument();
                        }
                    }
                }
                throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_DOMSOURCE_INPUT, null));
            }
            InputSource xmlSource = SAXSource.sourceToInputSource(source);
            if (xmlSource == null) {
                throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_SOURCE_TYPE, new Object[]{source.getClass().getName()}));
            }
            if (xmlSource.getSystemId() != null) {
                this.m_systemID = xmlSource.getSystemId();
            }
            XMLReader reader = null;
            boolean managedReader = false;
            try {
                if (source instanceof SAXSource) {
                    reader = ((SAXSource) source).getXMLReader();
                }
                if (reader == null) {
                    reader = XMLReaderManager.getInstance().getXMLReader();
                    managedReader = true;
                } else {
                    reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
                }
            } catch (SAXException e5) {
            } catch (WrappedRuntimeException wre) {
            } catch (IOException ioe) {
            }
            reader.setContentHandler(this);
            if (this instanceof DTDHandler) {
                reader.setDTDHandler(this);
            }
            if (this instanceof LexicalHandler) {
                reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
            }
            if (this instanceof DeclHandler) {
                reader.setProperty("http://xml.org/sax/properties/declaration-handler", this);
            }
            try {
                if (this instanceof LexicalHandler) {
                    reader.setProperty("http://xml.org/sax/handlers/LexicalHandler", this);
                }
                if (this instanceof DeclHandler) {
                    reader.setProperty("http://xml.org/sax/handlers/DeclHandler", this);
                }
            } catch (SAXNotRecognizedException e6) {
            }
            reader.parse(xmlSource);
            if (managedReader) {
                XMLReaderManager.getInstance().releaseXMLReader(reader);
            }
            if (this.m_outputStream != null) {
                try {
                    this.m_outputStream.close();
                } catch (IOException e7) {
                }
                this.m_outputStream = null;
            }
        } catch (Throwable th2) {
            if (this.m_outputStream != null) {
                try {
                    this.m_outputStream.close();
                } catch (IOException e8) {
                }
                this.m_outputStream = null;
            }
        }
    }

    public void setParameter(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_SET_PARAM_VALUE, new Object[]{name}));
        }
        if (this.m_params == null) {
            this.m_params = new Hashtable();
        }
        this.m_params.put(name, value);
    }

    public Object getParameter(String name) {
        if (this.m_params == null) {
            return null;
        }
        return this.m_params.get(name);
    }

    public void clearParameters() {
        if (this.m_params != null) {
            this.m_params.clear();
        }
    }

    public void setURIResolver(URIResolver resolver) {
        this.m_URIResolver = resolver;
    }

    public URIResolver getURIResolver() {
        return this.m_URIResolver;
    }

    public void setOutputProperties(Properties oformat) throws IllegalArgumentException {
        if (oformat != null) {
            String method = (String) oformat.get(Constants.ATTRNAME_OUTPUT_METHOD);
            if (method != null) {
                this.m_outputFormat = new OutputProperties(method);
            } else {
                this.m_outputFormat = new OutputProperties();
            }
            this.m_outputFormat.copyFrom(oformat);
            return;
        }
        this.m_outputFormat = null;
    }

    public Properties getOutputProperties() {
        return (Properties) this.m_outputFormat.getProperties().clone();
    }

    public void setOutputProperty(String name, String value) throws IllegalArgumentException {
        if (OutputProperties.isLegalPropertyKey(name)) {
            this.m_outputFormat.setProperty(name, value);
            return;
        }
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name}));
    }

    public String getOutputProperty(String name) throws IllegalArgumentException {
        String value = this.m_outputFormat.getProperty(name);
        if (value != null || OutputProperties.isLegalPropertyKey(name)) {
            return value;
        }
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name}));
    }

    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException(XSLMessages.createMessage("ER_NULL_ERROR_HANDLER", null));
        }
        this.m_errorListener = listener;
    }

    public ErrorListener getErrorListener() {
        return this.m_errorListener;
    }

    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        if (this.m_resultDTDHandler != null) {
            this.m_resultDTDHandler.notationDecl(name, publicId, systemId);
        }
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        if (this.m_resultDTDHandler != null) {
            this.m_resultDTDHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
        }
    }

    public void setDocumentLocator(Locator locator) {
        try {
            if (this.m_resultContentHandler == null) {
                createResultContentHandler(this.m_result);
            }
            this.m_resultContentHandler.setDocumentLocator(locator);
        } catch (TransformerException te) {
            throw new WrappedRuntimeException(te);
        }
    }

    public void startDocument() throws SAXException {
        try {
            if (this.m_resultContentHandler == null) {
                createResultContentHandler(this.m_result);
            }
            this.m_flushedStartDoc = false;
            this.m_foundFirstElement = false;
        } catch (TransformerException te) {
            throw new SAXException(te.getMessage(), te);
        }
    }

    protected final void flushStartDoc() throws SAXException {
        if (!this.m_flushedStartDoc) {
            if (this.m_resultContentHandler == null) {
                try {
                    createResultContentHandler(this.m_result);
                } catch (TransformerException te) {
                    throw new SAXException(te);
                }
            }
            this.m_resultContentHandler.startDocument();
            this.m_flushedStartDoc = true;
        }
    }

    public void endDocument() throws SAXException {
        flushStartDoc();
        this.m_resultContentHandler.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        flushStartDoc();
        this.m_resultContentHandler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        flushStartDoc();
        this.m_resultContentHandler.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!(this.m_foundFirstElement || this.m_serializer == null)) {
            this.m_foundFirstElement = true;
            try {
                Serializer newSerializer = SerializerSwitcher.switchSerializerIfHTML(uri, localName, this.m_outputFormat.getProperties(), this.m_serializer);
                if (newSerializer != this.m_serializer) {
                    try {
                        this.m_resultContentHandler = newSerializer.asContentHandler();
                        if (this.m_resultContentHandler instanceof DTDHandler) {
                            this.m_resultDTDHandler = (DTDHandler) this.m_resultContentHandler;
                        }
                        if (this.m_resultContentHandler instanceof LexicalHandler) {
                            this.m_resultLexicalHandler = (LexicalHandler) this.m_resultContentHandler;
                        }
                        this.m_serializer = newSerializer;
                    } catch (IOException ioe) {
                        throw new SAXException(ioe);
                    }
                }
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        }
        flushStartDoc();
        this.m_resultContentHandler.startElement(uri, localName, qName, attributes);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        this.m_resultContentHandler.endElement(uri, localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        flushStartDoc();
        this.m_resultContentHandler.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        this.m_resultContentHandler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        flushStartDoc();
        this.m_resultContentHandler.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        flushStartDoc();
        this.m_resultContentHandler.skippedEntity(name);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        flushStartDoc();
        if (this.m_resultLexicalHandler != null) {
            this.m_resultLexicalHandler.startDTD(name, publicId, systemId);
        }
    }

    public void endDTD() throws SAXException {
        if (this.m_resultLexicalHandler != null) {
            this.m_resultLexicalHandler.endDTD();
        }
    }

    public void startEntity(String name) throws SAXException {
        if (this.m_resultLexicalHandler != null) {
            this.m_resultLexicalHandler.startEntity(name);
        }
    }

    public void endEntity(String name) throws SAXException {
        if (this.m_resultLexicalHandler != null) {
            this.m_resultLexicalHandler.endEntity(name);
        }
    }

    public void startCDATA() throws SAXException {
        if (this.m_resultLexicalHandler != null) {
            this.m_resultLexicalHandler.startCDATA();
        }
    }

    public void endCDATA() throws SAXException {
        if (this.m_resultLexicalHandler != null) {
            this.m_resultLexicalHandler.endCDATA();
        }
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        flushStartDoc();
        if (this.m_resultLexicalHandler != null) {
            this.m_resultLexicalHandler.comment(ch, start, length);
        }
    }

    public void elementDecl(String name, String model) throws SAXException {
        if (this.m_resultDeclHandler != null) {
            this.m_resultDeclHandler.elementDecl(name, model);
        }
    }

    public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
        if (this.m_resultDeclHandler != null) {
            this.m_resultDeclHandler.attributeDecl(eName, aName, type, valueDefault, value);
        }
    }

    public void internalEntityDecl(String name, String value) throws SAXException {
        if (this.m_resultDeclHandler != null) {
            this.m_resultDeclHandler.internalEntityDecl(name, value);
        }
    }

    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
        if (this.m_resultDeclHandler != null) {
            this.m_resultDeclHandler.externalEntityDecl(name, publicId, systemId);
        }
    }
}
