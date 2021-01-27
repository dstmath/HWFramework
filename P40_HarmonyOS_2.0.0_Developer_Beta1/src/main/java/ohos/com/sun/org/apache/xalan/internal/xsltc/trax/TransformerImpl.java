package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMCache;
import ohos.com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.Translet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.DOMWSFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.TransletOutputHandlerFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import ohos.com.sun.org.apache.xml.internal.utils.XMLReaderManager;
import ohos.javax.xml.parsers.DocumentBuilder;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.Transformer;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.URIResolver;
import ohos.javax.xml.transform.dom.DOMResult;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.javax.xml.transform.sax.SAXResult;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.transform.stax.StAXResult;
import ohos.javax.xml.transform.stax.StAXSource;
import ohos.javax.xml.transform.stream.StreamResult;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.ext.LexicalHandler;

public final class TransformerImpl extends Transformer implements DOMCache, ErrorListener {
    private static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";
    private static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
    private String _accessExternalDTD;
    private DOM _dom;
    private XSLTCDTMManager _dtmManager;
    private String _encoding;
    private ErrorListener _errorListener;
    private int _indentNumber;
    private boolean _isIdentity;
    private boolean _isSecureProcessing;
    private String _method;
    private OutputStream _ostream;
    private boolean _overrideDefaultParser;
    private Map<String, Object> _parameters;
    private Properties _properties;
    private Properties _propertiesClone;
    private XMLReaderManager _readerManager;
    private XMLSecurityManager _securityManager;
    private String _sourceSystemId;
    private TransformerFactoryImpl _tfactory;
    private TransletOutputHandlerFactory _tohFactory;
    private AbstractTranslet _translet;
    private URIResolver _uriResolver;

    static class MessageHandler extends ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.MessageHandler {
        private ErrorListener _errorListener;

        public MessageHandler(ErrorListener errorListener) {
            this._errorListener = errorListener;
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.MessageHandler
        public void displayMessage(String str) {
            ErrorListener errorListener = this._errorListener;
            if (errorListener == null) {
                System.err.println(str);
                return;
            }
            try {
                errorListener.warning(new TransformerException(str));
            } catch (TransformerException unused) {
            }
        }
    }

    protected TransformerImpl(Properties properties, int i, TransformerFactoryImpl transformerFactoryImpl) {
        this(null, properties, i, transformerFactoryImpl);
        this._isIdentity = true;
    }

    protected TransformerImpl(Translet translet, Properties properties, int i, TransformerFactoryImpl transformerFactoryImpl) {
        this._translet = null;
        this._method = null;
        this._encoding = null;
        this._sourceSystemId = null;
        this._errorListener = this;
        this._uriResolver = null;
        this._tohFactory = null;
        this._dom = null;
        this._tfactory = null;
        this._ostream = null;
        this._dtmManager = null;
        this._isIdentity = false;
        this._isSecureProcessing = false;
        this._accessExternalDTD = "all";
        this._parameters = null;
        this._translet = (AbstractTranslet) translet;
        this._properties = createOutputProperties(properties);
        this._propertiesClone = (Properties) this._properties.clone();
        this._indentNumber = i;
        this._tfactory = transformerFactoryImpl;
        this._overrideDefaultParser = this._tfactory.overrideDefaultParser();
        this._accessExternalDTD = (String) this._tfactory.getAttribute("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD");
        this._securityManager = (XMLSecurityManager) this._tfactory.getAttribute("http://apache.org/xml/properties/security-manager");
        this._readerManager = XMLReaderManager.getInstance(this._overrideDefaultParser);
        this._readerManager.setProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", this._accessExternalDTD);
        this._readerManager.setFeature(Constants.FEATURE_SECURE_PROCESSING, this._isSecureProcessing);
        this._readerManager.setProperty("http://apache.org/xml/properties/security-manager", this._securityManager);
    }

    public boolean isSecureProcessing() {
        return this._isSecureProcessing;
    }

    public void setSecureProcessing(boolean z) {
        this._isSecureProcessing = z;
        this._readerManager.setFeature(Constants.FEATURE_SECURE_PROCESSING, this._isSecureProcessing);
    }

    public boolean overrideDefaultParser() {
        return this._overrideDefaultParser;
    }

    public void setOverrideDefaultParser(boolean z) {
        this._overrideDefaultParser = z;
    }

    /* access modifiers changed from: protected */
    public AbstractTranslet getTranslet() {
        return this._translet;
    }

    public boolean isIdentity() {
        return this._isIdentity;
    }

    public void transform(Source source, Result result) throws TransformerException {
        if (!this._isIdentity) {
            AbstractTranslet abstractTranslet = this._translet;
            if (abstractTranslet != null) {
                transferOutputProperties(abstractTranslet);
            } else {
                throw new TransformerException(new ErrorMsg(ErrorMsg.JAXP_NO_TRANSLET_ERR).toString());
            }
        }
        SerializationHandler outputHandler = getOutputHandler(result);
        if (outputHandler != null) {
            if (this._uriResolver != null && !this._isIdentity) {
                this._translet.setDOMCache(this);
            }
            if (this._isIdentity) {
                transferOutputProperties(outputHandler);
            }
            transform(source, outputHandler, this._encoding);
            try {
                if (result instanceof DOMResult) {
                    ((DOMResult) result).setNode(this._tohFactory.getNode());
                } else if (!(result instanceof StAXResult)) {
                } else {
                    if (((StAXResult) result).getXMLEventWriter() != null) {
                        this._tohFactory.getXMLEventWriter().flush();
                    } else if (((StAXResult) result).getXMLStreamWriter() != null) {
                        this._tohFactory.getXMLStreamWriter().flush();
                    }
                }
            } catch (Exception unused) {
                System.out.println("Result writing error");
            }
        } else {
            throw new TransformerException(new ErrorMsg(ErrorMsg.JAXP_NO_HANDLER_ERR).toString());
        }
    }

    public SerializationHandler getOutputHandler(Result result) throws TransformerException {
        this._method = (String) this._properties.get(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_METHOD);
        this._encoding = this._properties.getProperty(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_ENCODING);
        this._tohFactory = TransletOutputHandlerFactory.newInstance(this._overrideDefaultParser);
        this._tohFactory.setEncoding(this._encoding);
        String str = this._method;
        if (str != null) {
            this._tohFactory.setOutputMethod(str);
        }
        int i = this._indentNumber;
        if (i >= 0) {
            this._tohFactory.setIndentNumber(i);
        }
        try {
            if (result instanceof SAXResult) {
                SAXResult sAXResult = (SAXResult) result;
                this._tohFactory.setHandler(sAXResult.getHandler());
                LexicalHandler lexicalHandler = sAXResult.getLexicalHandler();
                if (lexicalHandler != null) {
                    this._tohFactory.setLexicalHandler(lexicalHandler);
                }
                this._tohFactory.setOutputType(1);
                return this._tohFactory.getSerializationHandler();
            } else if (result instanceof StAXResult) {
                if (((StAXResult) result).getXMLEventWriter() != null) {
                    this._tohFactory.setXMLEventWriter(((StAXResult) result).getXMLEventWriter());
                } else if (((StAXResult) result).getXMLStreamWriter() != null) {
                    this._tohFactory.setXMLStreamWriter(((StAXResult) result).getXMLStreamWriter());
                }
                this._tohFactory.setOutputType(3);
                return this._tohFactory.getSerializationHandler();
            } else if (result instanceof DOMResult) {
                this._tohFactory.setNode(((DOMResult) result).getNode());
                this._tohFactory.setNextSibling(((DOMResult) result).getNextSibling());
                this._tohFactory.setOutputType(2);
                return this._tohFactory.getSerializationHandler();
            } else if (!(result instanceof StreamResult)) {
                return null;
            } else {
                StreamResult streamResult = (StreamResult) result;
                this._tohFactory.setOutputType(0);
                Writer writer = streamResult.getWriter();
                if (writer != null) {
                    this._tohFactory.setWriter(writer);
                    return this._tohFactory.getSerializationHandler();
                }
                OutputStream outputStream = streamResult.getOutputStream();
                if (outputStream != null) {
                    this._tohFactory.setOutputStream(outputStream);
                    return this._tohFactory.getSerializationHandler();
                }
                String systemId = result.getSystemId();
                if (systemId == null) {
                    throw new TransformerException(new ErrorMsg(ErrorMsg.JAXP_NO_RESULT_ERR).toString());
                } else if (systemId.startsWith("file:")) {
                    try {
                        URI uri = new URI(systemId);
                        try {
                            String host = uri.getHost();
                            String path = uri.getPath();
                            if (path == null) {
                                path = "";
                            }
                            if (host != null) {
                                systemId = "file://" + host + path;
                            } else {
                                systemId = "file://" + path;
                            }
                        } catch (Exception unused) {
                            systemId = "file:";
                        }
                    } catch (Exception unused2) {
                    }
                    this._ostream = new FileOutputStream(new URL(systemId).getFile());
                    this._tohFactory.setOutputStream(this._ostream);
                    return this._tohFactory.getSerializationHandler();
                } else if (systemId.startsWith("http:")) {
                    URLConnection openConnection = new URL(systemId).openConnection();
                    TransletOutputHandlerFactory transletOutputHandlerFactory = this._tohFactory;
                    OutputStream outputStream2 = openConnection.getOutputStream();
                    this._ostream = outputStream2;
                    transletOutputHandlerFactory.setOutputStream(outputStream2);
                    return this._tohFactory.getSerializationHandler();
                } else {
                    TransletOutputHandlerFactory transletOutputHandlerFactory2 = this._tohFactory;
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(systemId));
                    this._ostream = fileOutputStream;
                    transletOutputHandlerFactory2.setOutputStream(fileOutputStream);
                    return this._tohFactory.getSerializationHandler();
                }
            }
        } catch (UnknownServiceException e) {
            throw new TransformerException(e);
        } catch (ParserConfigurationException e2) {
            throw new TransformerException(e2);
        } catch (IOException e3) {
            throw new TransformerException(e3);
        }
    }

    /* access modifiers changed from: protected */
    public void setDOM(DOM dom) {
        this._dom = dom;
    }

    private DOM getDOM(Source source) throws TransformerException {
        DOM dom;
        DOMWSFilter dOMWSFilter = null;
        if (source != null) {
            try {
                if (this._translet != null && (this._translet instanceof StripFilter)) {
                    dOMWSFilter = new DOMWSFilter(this._translet);
                }
                boolean hasIdCall = this._translet != null ? this._translet.hasIdCall() : false;
                if (this._dtmManager == null) {
                    this._dtmManager = this._tfactory.createNewDTMManagerInstance();
                    this._dtmManager.setOverrideDefaultParser(this._overrideDefaultParser);
                }
                dom = (DOM) this._dtmManager.getDTM(source, false, dOMWSFilter, true, false, false, 0, hasIdCall);
            } catch (Exception e) {
                if (this._errorListener != null) {
                    postErrorToListener(e.getMessage());
                }
                throw new TransformerException(e);
            }
        } else if (this._dom == null) {
            return null;
        } else {
            dom = this._dom;
            this._dom = null;
        }
        if (!this._isIdentity) {
            this._translet.prepassDocument(dom);
        }
        return dom;
    }

    /* access modifiers changed from: protected */
    public TransformerFactoryImpl getTransformerFactory() {
        return this._tfactory;
    }

    /* access modifiers changed from: protected */
    public TransletOutputHandlerFactory getTransletOutputHandlerFactory() {
        return this._tohFactory;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a9  */
    private void transformIdentity(Source source, SerializationHandler serializationHandler) throws Exception {
        Throwable th;
        boolean z;
        InputSource inputSource;
        if (source != null) {
            this._sourceSystemId = source.getSystemId();
        }
        if (source instanceof StreamSource) {
            StreamSource streamSource = (StreamSource) source;
            InputStream inputStream = streamSource.getInputStream();
            Reader reader = streamSource.getReader();
            XMLReader xMLReader = this._readerManager.getXMLReader();
            try {
                xMLReader.setProperty(LEXICAL_HANDLER_PROPERTY, serializationHandler);
                xMLReader.setFeature(NAMESPACE_PREFIXES_FEATURE, true);
            } catch (SAXException unused) {
            }
            try {
                xMLReader.setContentHandler(serializationHandler);
                if (inputStream != null) {
                    inputSource = new InputSource(inputStream);
                    inputSource.setSystemId(this._sourceSystemId);
                } else if (reader != null) {
                    InputSource inputSource2 = new InputSource(reader);
                    inputSource2.setSystemId(this._sourceSystemId);
                    inputSource = inputSource2;
                } else if (this._sourceSystemId != null) {
                    inputSource = new InputSource(this._sourceSystemId);
                } else {
                    throw new TransformerException(new ErrorMsg(ErrorMsg.JAXP_NO_SOURCE_ERR).toString());
                }
                xMLReader.parse(inputSource);
            } finally {
                this._readerManager.releaseXMLReader(xMLReader);
            }
        } else if (source instanceof SAXSource) {
            SAXSource sAXSource = (SAXSource) source;
            XMLReader xMLReader2 = sAXSource.getXMLReader();
            InputSource inputSource3 = sAXSource.getInputSource();
            if (xMLReader2 == null) {
                try {
                    xMLReader2 = this._readerManager.getXMLReader();
                    z = false;
                } catch (Throwable th2) {
                    th = th2;
                    z = true;
                    if (!z) {
                    }
                    throw th;
                }
            } else {
                z = true;
            }
            try {
                xMLReader2.setProperty(LEXICAL_HANDLER_PROPERTY, serializationHandler);
                xMLReader2.setFeature(NAMESPACE_PREFIXES_FEATURE, true);
            } catch (SAXException unused2) {
            }
            try {
                xMLReader2.setContentHandler(serializationHandler);
                xMLReader2.parse(inputSource3);
                if (!z) {
                    this._readerManager.releaseXMLReader(xMLReader2);
                }
            } catch (Throwable th3) {
                th = th3;
                if (!z) {
                    this._readerManager.releaseXMLReader(xMLReader2);
                }
                throw th;
            }
        } else if (source instanceof StAXSource) {
            StAXSource stAXSource = (StAXSource) source;
            if (stAXSource.getXMLEventReader() != null) {
                StAXEvent2SAX stAXEvent2SAX = new StAXEvent2SAX(stAXSource.getXMLEventReader());
                stAXEvent2SAX.setContentHandler(serializationHandler);
                stAXEvent2SAX.parse();
                serializationHandler.flushPending();
            } else if (stAXSource.getXMLStreamReader() != null) {
                StAXStream2SAX stAXStream2SAX = new StAXStream2SAX(stAXSource.getXMLStreamReader());
                stAXStream2SAX.setContentHandler(serializationHandler);
                stAXStream2SAX.parse();
                serializationHandler.flushPending();
            }
        } else if (source instanceof DOMSource) {
            new DOM2TO(((DOMSource) source).getNode(), serializationHandler).parse();
        } else if (source instanceof XSLTCSource) {
            ((SAXImpl) ((XSLTCSource) source).getDOM(null, this._translet)).copy(serializationHandler);
        } else {
            throw new TransformerException(new ErrorMsg(ErrorMsg.JAXP_NO_SOURCE_ERR).toString());
        }
    }

    /* JADX WARN: Type inference failed for: r3v1, types: [java.lang.Throwable, ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void transform(Source source, SerializationHandler serializationHandler, String str) throws TransformerException {
        try {
            if (((source instanceof StreamSource) && source.getSystemId() == null && ((StreamSource) source).getInputStream() == null && ((StreamSource) source).getReader() == null) || (((source instanceof SAXSource) && ((SAXSource) source).getInputSource() == null && ((SAXSource) source).getXMLReader() == null) || ((source instanceof DOMSource) && ((DOMSource) source).getNode() == null))) {
                DocumentBuilder newDocumentBuilder = JdkXmlUtils.getDOMFactory(this._overrideDefaultParser).newDocumentBuilder();
                String systemId = source.getSystemId();
                Source dOMSource = new DOMSource(newDocumentBuilder.newDocument());
                if (systemId != null) {
                    dOMSource.setSystemId(systemId);
                }
                source = dOMSource;
            }
            if (this._isIdentity) {
                transformIdentity(source, serializationHandler);
            } else {
                this._translet.transform(getDOM(source), serializationHandler);
            }
            this._dtmManager = null;
            OutputStream outputStream = this._ostream;
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException unused) {
                }
                this._ostream = null;
            }
        } catch (TransletException e) {
            if (this._errorListener != null) {
                postErrorToListener(e.getMessage());
            }
            throw new TransformerException((Throwable) e);
        } catch (RuntimeException e2) {
            if (this._errorListener != null) {
                postErrorToListener(e2.getMessage());
            }
            throw new TransformerException(e2);
        } catch (Exception e3) {
            if (this._errorListener != null) {
                postErrorToListener(e3.getMessage());
            }
            throw new TransformerException(e3);
        } catch (Throwable th) {
            this._dtmManager = null;
            throw th;
        }
    }

    public ErrorListener getErrorListener() {
        return this._errorListener;
    }

    public void setErrorListener(ErrorListener errorListener) throws IllegalArgumentException {
        if (errorListener != null) {
            this._errorListener = errorListener;
            AbstractTranslet abstractTranslet = this._translet;
            if (abstractTranslet != null) {
                abstractTranslet.setMessageHandler(new MessageHandler(this._errorListener));
                return;
            }
            return;
        }
        throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.ERROR_LISTENER_NULL_ERR, "Transformer").toString());
    }

    private void postErrorToListener(String str) {
        try {
            this._errorListener.error(new TransformerException(str));
        } catch (TransformerException unused) {
        }
    }

    private void postWarningToListener(String str) {
        try {
            this._errorListener.warning(new TransformerException(str));
        } catch (TransformerException unused) {
        }
    }

    public Properties getOutputProperties() {
        return (Properties) this._properties.clone();
    }

    public String getOutputProperty(String str) throws IllegalArgumentException {
        if (validOutputProperty(str)) {
            return this._properties.getProperty(str);
        }
        throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_PROP_ERR, str).toString());
    }

    public void setOutputProperties(Properties properties) throws IllegalArgumentException {
        if (properties != null) {
            Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String str = (String) propertyNames.nextElement();
                if (!isDefaultProperty(str, properties)) {
                    if (validOutputProperty(str)) {
                        this._properties.setProperty(str, properties.getProperty(str));
                    } else {
                        throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_PROP_ERR, str).toString());
                    }
                }
            }
            return;
        }
        this._properties = this._propertiesClone;
    }

    public void setOutputProperty(String str, String str2) throws IllegalArgumentException {
        if (validOutputProperty(str)) {
            this._properties.setProperty(str, str2);
            return;
        }
        throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_PROP_ERR, str).toString());
    }

    private void transferOutputProperties(AbstractTranslet abstractTranslet) {
        Properties properties = this._properties;
        if (properties != null) {
            Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String str = (String) propertyNames.nextElement();
                String str2 = (String) this._properties.get(str);
                if (str2 != null) {
                    if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_ENCODING)) {
                        abstractTranslet._encoding = str2;
                    } else if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_METHOD)) {
                        abstractTranslet._method = str2;
                    } else if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC)) {
                        abstractTranslet._doctypePublic = str2;
                    } else if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM)) {
                        abstractTranslet._doctypeSystem = str2;
                    } else if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_MEDIATYPE)) {
                        abstractTranslet._mediaType = str2;
                    } else if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_STANDALONE)) {
                        abstractTranslet._standalone = str2;
                    } else if (str.equals("version")) {
                        abstractTranslet._version = str2;
                    } else {
                        boolean z = false;
                        if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_OMITXMLDECL)) {
                            if (str2.toLowerCase().equals("yes")) {
                                z = true;
                            }
                            abstractTranslet._omitHeader = z;
                        } else if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_INDENT)) {
                            if (str2.toLowerCase().equals("yes")) {
                                z = true;
                            }
                            abstractTranslet._indent = z;
                        } else if (str.equals("{http://xml.apache.org/xslt}indent-amount")) {
                            abstractTranslet._indentamount = Integer.parseInt(str2);
                        } else if (str.equals(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT)) {
                            abstractTranslet._indentamount = Integer.parseInt(str2);
                        } else if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS)) {
                            abstractTranslet._cdata = null;
                            StringTokenizer stringTokenizer = new StringTokenizer(str2);
                            while (stringTokenizer.hasMoreTokens()) {
                                abstractTranslet.addCdataElement(stringTokenizer.nextToken());
                            }
                        } else if (str.equals(OutputPropertiesFactory.ORACLE_IS_STANDALONE) && str2.equals("yes")) {
                            abstractTranslet._isStandalone = true;
                        }
                    }
                }
            }
        }
    }

    public void transferOutputProperties(SerializationHandler serializationHandler) {
        String str;
        Properties properties = this._properties;
        if (properties != null) {
            Enumeration<?> propertyNames = properties.propertyNames();
            String str2 = null;
            String str3 = null;
            while (propertyNames.hasMoreElements()) {
                String str4 = (String) propertyNames.nextElement();
                String str5 = (String) this._properties.get(str4);
                if (str5 != null) {
                    if (str4.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC)) {
                        str2 = str5;
                    } else if (str4.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM)) {
                        str3 = str5;
                    } else if (str4.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_MEDIATYPE)) {
                        serializationHandler.setMediaType(str5);
                    } else if (str4.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_STANDALONE)) {
                        serializationHandler.setStandalone(str5);
                    } else if (str4.equals("version")) {
                        serializationHandler.setVersion(str5);
                    } else {
                        boolean z = false;
                        if (str4.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_OMITXMLDECL)) {
                            if (str5.toLowerCase().equals("yes")) {
                                z = true;
                            }
                            serializationHandler.setOmitXMLDeclaration(z);
                        } else if (str4.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_INDENT)) {
                            if (str5.toLowerCase().equals("yes")) {
                                z = true;
                            }
                            serializationHandler.setIndent(z);
                        } else if (str4.equals("{http://xml.apache.org/xslt}indent-amount")) {
                            serializationHandler.setIndentAmount(Integer.parseInt(str5));
                        } else if (str4.equals(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT)) {
                            serializationHandler.setIndentAmount(Integer.parseInt(str5));
                        } else if (str4.equals(OutputPropertiesFactory.ORACLE_IS_STANDALONE)) {
                            if (str5.equals("yes")) {
                                serializationHandler.setIsStandalone(true);
                            }
                        } else if (str4.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS)) {
                            StringTokenizer stringTokenizer = new StringTokenizer(str5);
                            Vector vector = null;
                            while (stringTokenizer.hasMoreTokens()) {
                                String nextToken = stringTokenizer.nextToken();
                                int lastIndexOf = nextToken.lastIndexOf(58);
                                if (lastIndexOf > 0) {
                                    str = nextToken.substring(0, lastIndexOf);
                                    nextToken = nextToken.substring(lastIndexOf + 1);
                                } else {
                                    str = null;
                                }
                                if (vector == null) {
                                    vector = new Vector();
                                }
                                vector.addElement(str);
                                vector.addElement(nextToken);
                            }
                            serializationHandler.setCdataSectionElements(vector);
                        }
                    }
                }
            }
            if (str2 != null || str3 != null) {
                serializationHandler.setDoctype(str3, str2);
            }
        }
    }

    private Properties createOutputProperties(Properties properties) {
        Properties properties2 = new Properties();
        setDefaults(properties2, "xml");
        Properties properties3 = new Properties(properties2);
        if (properties != null) {
            Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String str = (String) propertyNames.nextElement();
                properties3.setProperty(str, properties.getProperty(str));
            }
        } else {
            properties3.setProperty(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_ENCODING, this._translet._encoding);
            if (this._translet._method != null) {
                properties3.setProperty(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_METHOD, this._translet._method);
            }
        }
        String property = properties3.getProperty(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_METHOD);
        if (property != null) {
            if (property.equals("html")) {
                setDefaults(properties2, "html");
            } else if (property.equals("text")) {
                setDefaults(properties2, "text");
            }
        }
        return properties3;
    }

    private void setDefaults(Properties properties, String str) {
        Properties defaultMethodProperties = OutputPropertiesFactory.getDefaultMethodProperties(str);
        Enumeration<?> propertyNames = defaultMethodProperties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String str2 = (String) propertyNames.nextElement();
            properties.setProperty(str2, defaultMethodProperties.getProperty(str2));
        }
    }

    private boolean validOutputProperty(String str) {
        if (str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_ENCODING) || str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_METHOD) || str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_INDENT) || str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC) || str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM) || str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS) || str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_MEDIATYPE) || str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_OMITXMLDECL) || str.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_STANDALONE) || str.equals("version") || str.equals(OutputPropertiesFactory.ORACLE_IS_STANDALONE) || str.charAt(0) == '{') {
            return true;
        }
        return false;
    }

    private boolean isDefaultProperty(String str, Properties properties) {
        return properties.get(str) == null;
    }

    public void setParameter(String str, Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.JAXP_INVALID_SET_PARAM_VALUE, str).toString());
        } else if (this._isIdentity) {
            if (this._parameters == null) {
                this._parameters = new HashMap();
            }
            this._parameters.put(str, obj);
        } else {
            this._translet.addParameter(str, obj);
        }
    }

    public void clearParameters() {
        Map<String, Object> map;
        if (!this._isIdentity || (map = this._parameters) == null) {
            this._translet.clearParameters();
        } else {
            map.clear();
        }
    }

    public final Object getParameter(String str) {
        if (!this._isIdentity) {
            return this._translet.getParameter(str);
        }
        Map<String, Object> map = this._parameters;
        if (map != null) {
            return map.get(str);
        }
        return null;
    }

    public URIResolver getURIResolver() {
        return this._uriResolver;
    }

    public void setURIResolver(URIResolver uRIResolver) {
        this._uriResolver = uRIResolver;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOMCache
    public DOM retrieveDocument(String str, String str2, Translet translet) {
        try {
            if (str2.length() == 0) {
                str2 = str;
            }
            Source resolve = this._uriResolver.resolve(str2, str);
            if (resolve == null) {
                return getDOM(new StreamSource(SystemIDResolver.getAbsoluteURI(str2, str)));
            }
            return getDOM(resolve);
        } catch (TransformerException e) {
            if (this._errorListener == null) {
                return null;
            }
            postErrorToListener("File not found: " + e.getMessage());
            return null;
        }
    }

    public void error(TransformerException transformerException) throws TransformerException {
        Throwable exception = transformerException.getException();
        if (exception != null) {
            System.err.println(new ErrorMsg(ErrorMsg.ERROR_PLUS_WRAPPED_MSG, transformerException.getMessageAndLocation(), exception.getMessage()));
        } else {
            System.err.println(new ErrorMsg(ErrorMsg.ERROR_MSG, transformerException.getMessageAndLocation()));
        }
        throw transformerException;
    }

    public void fatalError(TransformerException transformerException) throws TransformerException {
        Throwable exception = transformerException.getException();
        if (exception != null) {
            System.err.println(new ErrorMsg(ErrorMsg.FATAL_ERR_PLUS_WRAPPED_MSG, transformerException.getMessageAndLocation(), exception.getMessage()));
        } else {
            System.err.println(new ErrorMsg(ErrorMsg.FATAL_ERR_MSG, transformerException.getMessageAndLocation()));
        }
        throw transformerException;
    }

    public void warning(TransformerException transformerException) throws TransformerException {
        Throwable exception = transformerException.getException();
        if (exception != null) {
            System.err.println(new ErrorMsg(ErrorMsg.WARNING_PLUS_WRAPPED_MSG, transformerException.getMessageAndLocation(), exception.getMessage()));
        } else {
            System.err.println(new ErrorMsg(ErrorMsg.WARNING_MSG, transformerException.getMessageAndLocation()));
        }
    }

    public void reset() {
        this._method = null;
        this._encoding = null;
        this._sourceSystemId = null;
        this._errorListener = this;
        this._uriResolver = null;
        this._dom = null;
        this._parameters = null;
        this._indentNumber = 0;
        setOutputProperties(null);
        this._tohFactory = null;
        this._ostream = null;
    }
}
