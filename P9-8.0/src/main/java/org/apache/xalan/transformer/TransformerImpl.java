package org.apache.xalan.transformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
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
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xalan.extensions.ExtensionsTable;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.AVT;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemAttributeSet;
import org.apache.xalan.templates.ElemForEach;
import org.apache.xalan.templates.ElemSort;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemTextLiteral;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.StylesheetComposed;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xalan.templates.WhiteSpaceInfo;
import org.apache.xalan.templates.XUnresolvedVariable;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.SerializerTrace;
import org.apache.xml.serializer.ToSAXHandler;
import org.apache.xml.serializer.ToTextStream;
import org.apache.xml.serializer.ToXMLSAXHandler;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.DOMBuilder;
import org.apache.xml.utils.DOMHelper;
import org.apache.xml.utils.DefaultErrorHandler;
import org.apache.xml.utils.NodeVector;
import org.apache.xml.utils.ObjectPool;
import org.apache.xml.utils.ObjectStack;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xml.utils.ThreadControllerWrapper;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.Arg;
import org.apache.xpath.ExtensionsProvider;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.SelfIteratorNoPredicate;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

public class TransformerImpl extends Transformer implements Runnable, DTMWSFilter, ExtensionsProvider, SerializerTrace {
    Stack m_attrSetStack = null;
    CountersTable m_countersTable = null;
    ObjectStack m_currentFuncResult = new ObjectStack();
    Stack m_currentMatchTemplates = new Stack();
    NodeVector m_currentMatchedNodes = new NodeVector();
    ObjectStack m_currentTemplateElements = new ObjectStack(4096);
    BoolStack m_currentTemplateRuleIsNull = new BoolStack();
    private int m_doc;
    private ErrorListener m_errorHandler = new DefaultErrorHandler(false);
    private Exception m_exceptionThrown = null;
    private ExtensionsTable m_extensionsTable = null;
    private boolean m_hasBeenReset = false;
    private boolean m_hasTransformThreadErrorCatcher = false;
    private boolean m_incremental = false;
    ContentHandler m_inputContentHandler;
    private KeyManager m_keyManager = new KeyManager();
    private Stack m_modes = new Stack();
    private MsgMgr m_msgMgr;
    private boolean m_optimizer = true;
    private ContentHandler m_outputContentHandler = null;
    private OutputProperties m_outputFormat;
    private FileOutputStream m_outputStream = null;
    private Result m_outputTarget = null;
    private boolean m_quietConflictWarnings = true;
    private Boolean m_reentryGuard = new Boolean(true);
    private SerializationHandler m_serializationHandler;
    private boolean m_shouldReset = true;
    private boolean m_source_location = false;
    private ObjectPool m_stringWriterObjectPool = new ObjectPool(StringWriter.class);
    private StylesheetRoot m_stylesheetRoot = null;
    private ObjectPool m_textResultHandlerObjectPool = new ObjectPool(ToTextStream.class);
    private OutputProperties m_textformat = new OutputProperties("text");
    private Thread m_transformThread;
    private String m_urlOfSource = null;
    Vector m_userParams;
    private XPathContext m_xcontext;

    public TransformerImpl(StylesheetRoot stylesheet) {
        this.m_optimizer = stylesheet.getOptimizer();
        this.m_incremental = stylesheet.getIncremental();
        this.m_source_location = stylesheet.getSource_location();
        setStylesheet(stylesheet);
        XPathContext xPath = new XPathContext((Object) this);
        xPath.setIncremental(this.m_incremental);
        xPath.getDTMManager().setIncremental(this.m_incremental);
        xPath.setSource_location(this.m_source_location);
        xPath.getDTMManager().setSource_location(this.m_source_location);
        if (stylesheet.isSecureProcessing()) {
            xPath.setSecureProcessing(true);
        }
        setXPathContext(xPath);
        getXPathContext().setNamespaceContext(stylesheet);
    }

    public ExtensionsTable getExtensionsTable() {
        return this.m_extensionsTable;
    }

    void setExtensionsTable(StylesheetRoot sroot) throws TransformerException {
        try {
            if (sroot.getExtensions() != null && !sroot.isSecureProcessing()) {
                this.m_extensionsTable = new ExtensionsTable(sroot);
            }
        } catch (TransformerException te) {
            te.printStackTrace();
        }
    }

    public boolean functionAvailable(String ns, String funcName) throws TransformerException {
        return getExtensionsTable().functionAvailable(ns, funcName);
    }

    public boolean elementAvailable(String ns, String elemName) throws TransformerException {
        return getExtensionsTable().elementAvailable(ns, elemName);
    }

    public Object extFunction(String ns, String funcName, Vector argVec, Object methodKey) throws TransformerException {
        return getExtensionsTable().extFunction(ns, funcName, argVec, methodKey, getXPathContext().getExpressionContext());
    }

    public Object extFunction(FuncExtFunction extFunction, Vector argVec) throws TransformerException {
        return getExtensionsTable().extFunction(extFunction, argVec, getXPathContext().getExpressionContext());
    }

    public void reset() {
        if (!this.m_hasBeenReset && this.m_shouldReset) {
            this.m_hasBeenReset = true;
            if (this.m_outputStream != null) {
                try {
                    this.m_outputStream.close();
                } catch (IOException e) {
                }
            }
            this.m_outputStream = null;
            this.m_countersTable = null;
            this.m_xcontext.reset();
            this.m_xcontext.getVarStack().reset();
            resetUserParameters();
            this.m_currentTemplateElements.removeAllElements();
            this.m_currentMatchTemplates.removeAllElements();
            this.m_currentMatchedNodes.removeAllElements();
            this.m_serializationHandler = null;
            this.m_outputTarget = null;
            this.m_keyManager = new KeyManager();
            this.m_attrSetStack = null;
            this.m_countersTable = null;
            this.m_currentTemplateRuleIsNull = new BoolStack();
            this.m_doc = -1;
            this.m_transformThread = null;
            this.m_xcontext.getSourceTreeManager().reset();
        }
    }

    public Thread getTransformThread() {
        return this.m_transformThread;
    }

    public void setTransformThread(Thread t) {
        this.m_transformThread = t;
    }

    public boolean hasTransformThreadErrorCatcher() {
        return this.m_hasTransformThreadErrorCatcher;
    }

    public void transform(Source source) throws TransformerException {
        transform(source, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d5 A:{SYNTHETIC, Splitter: B:37:0x00d5} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x019c A:{SYNTHETIC, Splitter: B:90:0x019c} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00de A:{Catch:{ all -> 0x0155, WrappedRuntimeException -> 0x00e5, SAXParseException -> 0x0144, SAXException -> 0x015c }} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00f0 A:{Catch:{ all -> 0x0192 }} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d5 A:{SYNTHETIC, Splitter: B:37:0x00d5} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00de A:{Catch:{ all -> 0x0155, WrappedRuntimeException -> 0x00e5, SAXParseException -> 0x0144, SAXException -> 0x015c }} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x019c A:{SYNTHETIC, Splitter: B:90:0x019c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void transform(Source source, boolean shouldRelease) throws TransformerException {
        DTMManager mgr;
        ParserConfigurationException e;
        DTM dtm;
        Exception e2;
        WrappedRuntimeException wre;
        Throwable throwable;
        Throwable th;
        Throwable spe;
        Throwable se;
        try {
            if (getXPathContext().getNamespaceContext() == null) {
                getXPathContext().setNamespaceContext(getStylesheet());
            }
            String base = source.getSystemId();
            if (base == null) {
                base = this.m_stylesheetRoot.getBaseIdentifier();
            }
            if (base == null) {
                String currentDir = "";
                try {
                    currentDir = System.getProperty("user.dir");
                } catch (SecurityException e3) {
                }
                if (currentDir.startsWith(File.separator)) {
                    base = "file://" + currentDir;
                } else {
                    base = "file:///" + currentDir;
                }
                base = base + File.separatorChar + source.getClass().getName();
            }
            setBaseURLOfSource(base);
            mgr = this.m_xcontext.getDTMManager();
            if (((source instanceof StreamSource) && source.getSystemId() == null && ((StreamSource) source).getInputStream() == null && ((StreamSource) source).getReader() == null) || (((source instanceof SAXSource) && ((SAXSource) source).getInputSource() == null && ((SAXSource) source).getXMLReader() == null) || ((source instanceof DOMSource) && ((DOMSource) source).getNode() == null))) {
                try {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    String systemID = source.getSystemId();
                    Source dOMSource = new DOMSource(builder.newDocument());
                    if (systemID != null) {
                        try {
                            dOMSource.setSystemId(systemID);
                        } catch (ParserConfigurationException e4) {
                            e = e4;
                            source = dOMSource;
                            fatalError(e);
                            dtm = mgr.getDTM(source, false, this, true, true);
                            dtm.setDocumentBaseURI(base);
                            transformNode(dtm.getDocument());
                            if (shouldRelease) {
                            }
                            e2 = getExceptionThrown();
                            if (e2 != null) {
                            }
                            this.m_hasTransformThreadErrorCatcher = false;
                            reset();
                        } catch (WrappedRuntimeException e5) {
                            wre = e5;
                            source = dOMSource;
                            try {
                                throwable = wre.getException();
                                while (throwable instanceof WrappedRuntimeException) {
                                    throwable = ((WrappedRuntimeException) throwable).getException();
                                }
                                fatalError(throwable);
                                this.m_hasTransformThreadErrorCatcher = false;
                                reset();
                            } catch (Throwable th2) {
                                th = th2;
                                this.m_hasTransformThreadErrorCatcher = false;
                                reset();
                                throw th;
                            }
                        } catch (SAXParseException e6) {
                            spe = e6;
                            source = dOMSource;
                            fatalError(spe);
                            this.m_hasTransformThreadErrorCatcher = false;
                            reset();
                        } catch (SAXException e7) {
                            se = e7;
                            source = dOMSource;
                            this.m_errorHandler.fatalError(new TransformerException(se));
                            this.m_hasTransformThreadErrorCatcher = false;
                            reset();
                        } catch (Throwable th3) {
                            th = th3;
                            source = dOMSource;
                            this.m_hasTransformThreadErrorCatcher = false;
                            reset();
                            throw th;
                        }
                    }
                    source = dOMSource;
                } catch (ParserConfigurationException e8) {
                    e = e8;
                    fatalError(e);
                    dtm = mgr.getDTM(source, false, this, true, true);
                    dtm.setDocumentBaseURI(base);
                    transformNode(dtm.getDocument());
                    if (shouldRelease) {
                    }
                    e2 = getExceptionThrown();
                    if (e2 != null) {
                    }
                    this.m_hasTransformThreadErrorCatcher = false;
                    reset();
                }
            }
            dtm = mgr.getDTM(source, false, this, true, true);
            dtm.setDocumentBaseURI(base);
            transformNode(dtm.getDocument());
            if (shouldRelease) {
                mgr.release(dtm, true);
            }
            e2 = getExceptionThrown();
            if (e2 != null) {
                if (e2 instanceof TransformerException) {
                    throw ((TransformerException) e2);
                } else if (e2 instanceof WrappedRuntimeException) {
                    fatalError(((WrappedRuntimeException) e2).getException());
                } else {
                    throw new TransformerException(e2);
                }
            } else if (this.m_serializationHandler != null) {
                this.m_serializationHandler.endDocument();
            }
            this.m_hasTransformThreadErrorCatcher = false;
            reset();
        } catch (WrappedRuntimeException e9) {
            wre = e9;
            throwable = wre.getException();
            while (throwable instanceof WrappedRuntimeException) {
            }
            fatalError(throwable);
            this.m_hasTransformThreadErrorCatcher = false;
            reset();
        } catch (SAXParseException e10) {
            spe = e10;
            fatalError(spe);
            this.m_hasTransformThreadErrorCatcher = false;
            reset();
        } catch (SAXException e11) {
            se = e11;
            this.m_errorHandler.fatalError(new TransformerException(se));
            this.m_hasTransformThreadErrorCatcher = false;
            reset();
        } catch (Throwable th4) {
            if (shouldRelease) {
                mgr.release(dtm, true);
            }
        }
    }

    private void fatalError(Throwable throwable) throws TransformerException {
        if (throwable instanceof SAXParseException) {
            this.m_errorHandler.fatalError(new TransformerException(throwable.getMessage(), new SAXSourceLocator((SAXParseException) throwable)));
        } else {
            this.m_errorHandler.fatalError(new TransformerException(throwable));
        }
    }

    public void setBaseURLOfSource(String base) {
        this.m_urlOfSource = base;
    }

    public String getOutputProperty(String qnameString) throws IllegalArgumentException {
        String value = getOutputFormat().getProperty(qnameString);
        if (value != null || OutputProperties.isLegalPropertyKey(qnameString)) {
            return value;
        }
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{qnameString}));
    }

    public String getOutputPropertyNoDefault(String qnameString) throws IllegalArgumentException {
        String value = (String) getOutputFormat().getProperties().get(qnameString);
        if (value != null || OutputProperties.isLegalPropertyKey(qnameString)) {
            return value;
        }
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{qnameString}));
    }

    public void setOutputProperty(String name, String value) throws IllegalArgumentException {
        synchronized (this.m_reentryGuard) {
            if (this.m_outputFormat == null) {
                this.m_outputFormat = (OutputProperties) getStylesheet().getOutputComposed().clone();
            }
            if (OutputProperties.isLegalPropertyKey(name)) {
                this.m_outputFormat.setProperty(name, value);
            } else {
                throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name}));
            }
        }
    }

    public void setOutputProperties(Properties oformat) throws IllegalArgumentException {
        synchronized (this.m_reentryGuard) {
            if (oformat != null) {
                String method = (String) oformat.get(Constants.ATTRNAME_OUTPUT_METHOD);
                if (method != null) {
                    this.m_outputFormat = new OutputProperties(method);
                } else if (this.m_outputFormat == null) {
                    this.m_outputFormat = new OutputProperties();
                }
                this.m_outputFormat.copyFrom(oformat);
                this.m_outputFormat.copyFrom(this.m_stylesheetRoot.getOutputProperties());
            } else {
                this.m_outputFormat = null;
            }
        }
    }

    public Properties getOutputProperties() {
        return (Properties) getOutputFormat().getProperties().clone();
    }

    public SerializationHandler createSerializationHandler(Result outputTarget) throws TransformerException {
        return createSerializationHandler(outputTarget, getOutputFormat());
    }

    public SerializationHandler createSerializationHandler(Result outputTarget, OutputProperties format) throws TransformerException {
        SerializationHandler serializationHandler;
        if (outputTarget instanceof DOMResult) {
            short type;
            Document doc;
            DOMBuilder handler;
            Node outputNode = ((DOMResult) outputTarget).getNode();
            Node nextSibling = ((DOMResult) outputTarget).getNextSibling();
            if (outputNode != null) {
                type = outputNode.getNodeType();
                if ((short) 9 == type) {
                    doc = (Document) outputNode;
                } else {
                    doc = outputNode.getOwnerDocument();
                }
            } else {
                doc = DOMHelper.createDocument(this.m_stylesheetRoot.isSecureProcessing());
                Object outputNode2 = doc;
                type = doc.getNodeType();
                ((DOMResult) outputTarget).setNode(doc);
            }
            if ((short) 11 == type) {
                handler = new DOMBuilder(doc, (DocumentFragment) outputNode2);
            } else {
                handler = new DOMBuilder(doc, outputNode2);
            }
            if (nextSibling != null) {
                handler.setNextSibling(nextSibling);
            }
            SerializationHandler toXMLSAXHandler = new ToXMLSAXHandler(handler, handler, format.getProperty("encoding"));
        } else if (outputTarget instanceof SAXResult) {
            ContentHandler handler2 = ((SAXResult) outputTarget).getHandler();
            if (handler2 == null) {
                throw new IllegalArgumentException("handler can not be null for a SAXResult");
            }
            LexicalHandler lexHandler;
            if (handler2 instanceof LexicalHandler) {
                lexHandler = (LexicalHandler) handler2;
            } else {
                lexHandler = null;
            }
            String encoding = format.getProperty("encoding");
            String method = format.getProperty(Constants.ATTRNAME_OUTPUT_METHOD);
            ToXMLSAXHandler toXMLSAXHandler2 = new ToXMLSAXHandler(handler2, lexHandler, encoding);
            toXMLSAXHandler2.setShouldOutputNSAttr(false);
            Object xoh = toXMLSAXHandler2;
            String publicID = format.getProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC);
            String systemID = format.getProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM);
            if (systemID != null) {
                toXMLSAXHandler2.setDoctypeSystem(systemID);
            }
            if (publicID != null) {
                toXMLSAXHandler2.setDoctypePublic(publicID);
            }
            if (handler2 instanceof TransformerClient) {
                XalanTransformState state = new XalanTransformState();
                ((TransformerClient) handler2).setTransformState(state);
                toXMLSAXHandler2.setTransformState(state);
            }
        } else if (outputTarget instanceof StreamResult) {
            StreamResult sresult = (StreamResult) outputTarget;
            try {
                SerializationHandler serializer = (SerializationHandler) SerializerFactory.getSerializer(format.getProperties());
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
                    serializationHandler = serializer;
                } else {
                    throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_OUTPUT_SPECIFIED, null));
                }
                serializationHandler = serializer;
            } catch (IOException ioe) {
                throw new TransformerException(ioe);
            }
        } else {
            throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_TO_RESULT_TYPE, new Object[]{outputTarget.getClass().getName()}));
        }
        serializationHandler.setTransformer(this);
        serializationHandler.setSourceLocator(getStylesheet());
        return serializationHandler;
    }

    public void transform(Source xmlSource, Result outputTarget) throws TransformerException {
        transform(xmlSource, outputTarget, true);
    }

    public void transform(Source xmlSource, Result outputTarget, boolean shouldRelease) throws TransformerException {
        synchronized (this.m_reentryGuard) {
            setSerializationHandler(createSerializationHandler(outputTarget));
            this.m_outputTarget = outputTarget;
            transform(xmlSource, shouldRelease);
        }
    }

    public void transformNode(int node, Result outputTarget) throws TransformerException {
        setSerializationHandler(createSerializationHandler(outputTarget));
        this.m_outputTarget = outputTarget;
        transformNode(node);
    }

    public void transformNode(int node) throws TransformerException {
        setExtensionsTable(getStylesheet());
        synchronized (this.m_serializationHandler) {
            this.m_hasBeenReset = false;
            XPathContext xctxt = getXPathContext();
            DTM dtm = xctxt.getDTM(node);
            try {
                pushGlobalVars(node);
                StylesheetRoot stylesheet = getStylesheet();
                int n = stylesheet.getGlobalImportCount();
                for (int i = 0; i < n; i++) {
                    StylesheetComposed imported = stylesheet.getGlobalImport(i);
                    int includedCount = imported.getIncludeCountComposed();
                    for (int j = -1; j < includedCount; j++) {
                        Stylesheet included = imported.getIncludeComposed(j);
                        included.runtimeInit(this);
                        for (ElemTemplateElement child = included.getFirstChildElem(); child != null; child = child.getNextSiblingElem()) {
                            child.runtimeInit(this);
                        }
                    }
                }
                DTMIterator dtmIter = new SelfIteratorNoPredicate();
                dtmIter.setRoot(node, xctxt);
                xctxt.pushContextNodeList(dtmIter);
                applyTemplateToNode(null, null, node);
                xctxt.popContextNodeList();
                if (this.m_serializationHandler != null) {
                    this.m_serializationHandler.endDocument();
                }
                reset();
            } catch (Exception e) {
                Exception se = e;
                while (se instanceof WrappedRuntimeException) {
                    try {
                        Exception e2 = ((WrappedRuntimeException) se).getException();
                        if (e2 != null) {
                            se = e2;
                        }
                    } catch (Throwable th) {
                        reset();
                    }
                }
                if (this.m_serializationHandler != null) {
                    try {
                        if (se instanceof SAXParseException) {
                            this.m_serializationHandler.fatalError((SAXParseException) se);
                        } else if (se instanceof TransformerException) {
                            Exception te = (TransformerException) se;
                            SAXSourceLocator sl = new SAXSourceLocator(te.getLocator());
                            this.m_serializationHandler.fatalError(new SAXParseException(te.getMessage(), sl, te));
                        } else {
                            this.m_serializationHandler.fatalError(new SAXParseException(se.getMessage(), new SAXSourceLocator(), se));
                        }
                    } catch (Exception e3) {
                    }
                }
                if (se instanceof TransformerException) {
                    this.m_errorHandler.fatalError((TransformerException) se);
                } else if (se instanceof SAXParseException) {
                    this.m_errorHandler.fatalError(new TransformerException(se.getMessage(), new SAXSourceLocator((SAXParseException) se), se));
                } else {
                    this.m_errorHandler.fatalError(new TransformerException(se));
                }
                reset();
            } catch (Throwable th2) {
                xctxt.popContextNodeList();
            }
        }
        return;
    }

    public ContentHandler getInputContentHandler() {
        return getInputContentHandler(false);
    }

    public ContentHandler getInputContentHandler(boolean doDocFrag) {
        if (this.m_inputContentHandler == null) {
            this.m_inputContentHandler = new TransformerHandlerImpl(this, doDocFrag, this.m_urlOfSource);
        }
        return this.m_inputContentHandler;
    }

    public void setOutputFormat(OutputProperties oformat) {
        this.m_outputFormat = oformat;
    }

    public OutputProperties getOutputFormat() {
        if (this.m_outputFormat == null) {
            return getStylesheet().getOutputComposed();
        }
        return this.m_outputFormat;
    }

    public void setParameter(String name, String namespace, Object value) {
        VariableStack varstack = getXPathContext().getVarStack();
        QName qname = new QName(namespace, name);
        XObject xobject = XObject.create(value, getXPathContext());
        Vector vars = this.m_stylesheetRoot.getVariablesAndParamsComposed();
        int i = vars.size();
        while (true) {
            i--;
            if (i >= 0) {
                ElemVariable variable = (ElemVariable) vars.elementAt(i);
                if (variable.getXSLToken() == 41 && variable.getName().equals(qname)) {
                    varstack.setGlobalVariable(i, xobject);
                }
            } else {
                return;
            }
        }
    }

    public void setParameter(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_SET_PARAM_VALUE, new Object[]{name}));
        }
        StringTokenizer tokenizer = new StringTokenizer(name, "{}", false);
        try {
            String s1 = tokenizer.nextToken();
            String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
            if (this.m_userParams == null) {
                this.m_userParams = new Vector();
            }
            if (s2 == null) {
                replaceOrPushUserParam(new QName(s1), XObject.create(value, getXPathContext()));
                setParameter(s1, null, value);
                return;
            }
            replaceOrPushUserParam(new QName(s1, s2), XObject.create(value, getXPathContext()));
            setParameter(s2, s1, value);
        } catch (NoSuchElementException e) {
        }
    }

    private void replaceOrPushUserParam(QName qname, XObject xval) {
        for (int i = this.m_userParams.size() - 1; i >= 0; i--) {
            if (((Arg) this.m_userParams.elementAt(i)).getQName().equals(qname)) {
                this.m_userParams.setElementAt(new Arg(qname, xval, true), i);
                return;
            }
        }
        this.m_userParams.addElement(new Arg(qname, xval, true));
    }

    public Object getParameter(String name) {
        try {
            QName qname = QName.getQNameFromString(name);
            if (this.m_userParams == null) {
                return null;
            }
            for (int i = this.m_userParams.size() - 1; i >= 0; i--) {
                Arg arg = (Arg) this.m_userParams.elementAt(i);
                if (arg.getQName().equals(qname)) {
                    return arg.getVal().object();
                }
            }
            return null;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private void resetUserParameters() {
        try {
            if (this.m_userParams != null) {
                for (int i = this.m_userParams.size() - 1; i >= 0; i--) {
                    Arg arg = (Arg) this.m_userParams.elementAt(i);
                    QName name = arg.getQName();
                    setParameter(name.getLocalPart(), name.getNamespace(), arg.getVal().object());
                }
            }
        } catch (NoSuchElementException e) {
        }
    }

    public void setParameters(Properties params) {
        clearParameters();
        Enumeration names = params.propertyNames();
        while (names.hasMoreElements()) {
            String name = params.getProperty((String) names.nextElement());
            StringTokenizer tokenizer = new StringTokenizer(name, "{}", false);
            try {
                String s1 = tokenizer.nextToken();
                String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
                if (s2 == null) {
                    setParameter(s1, null, params.getProperty(name));
                } else {
                    setParameter(s2, s1, params.getProperty(name));
                }
            } catch (NoSuchElementException e) {
            }
        }
    }

    public void clearParameters() {
        synchronized (this.m_reentryGuard) {
            this.m_xcontext.setVarStack(new VariableStack());
            this.m_userParams = null;
        }
    }

    protected void pushGlobalVars(int contextNode) throws TransformerException {
        VariableStack vs = this.m_xcontext.getVarStack();
        Vector vars = getStylesheet().getVariablesAndParamsComposed();
        int i = vars.size();
        vs.link(i);
        while (true) {
            i--;
            if (i >= 0) {
                XObject xobj = new XUnresolvedVariable((ElemVariable) vars.elementAt(i), contextNode, this, vs.getStackFrame(), 0, true);
                if (vs.elementAt(i) == null) {
                    vs.setGlobalVariable(i, xobj);
                }
            } else {
                return;
            }
        }
    }

    public void setURIResolver(URIResolver resolver) {
        synchronized (this.m_reentryGuard) {
            this.m_xcontext.getSourceTreeManager().setURIResolver(resolver);
        }
    }

    public URIResolver getURIResolver() {
        return this.m_xcontext.getSourceTreeManager().getURIResolver();
    }

    public void setContentHandler(ContentHandler handler) {
        if (handler == null) {
            throw new NullPointerException(XSLMessages.createMessage(XSLTErrorResources.ER_NULL_CONTENT_HANDLER, null));
        }
        this.m_outputContentHandler = handler;
        if (this.m_serializationHandler == null) {
            ToXMLSAXHandler h = new ToXMLSAXHandler();
            h.setContentHandler(handler);
            h.setTransformer(this);
            this.m_serializationHandler = h;
            return;
        }
        this.m_serializationHandler.setContentHandler(handler);
    }

    public ContentHandler getContentHandler() {
        return this.m_outputContentHandler;
    }

    public int transformToRTF(ElemTemplateElement templateParent) throws TransformerException {
        return transformToRTF(templateParent, this.m_xcontext.getRTFDTM());
    }

    public int transformToGlobalRTF(ElemTemplateElement templateParent) throws TransformerException {
        return transformToRTF(templateParent, this.m_xcontext.getGlobalRTFDTM());
    }

    private int transformToRTF(ElemTemplateElement templateParent, DTM dtmFrag) throws TransformerException {
        XPathContext xctxt = this.m_xcontext;
        ContentHandler rtfHandler = dtmFrag.getContentHandler();
        SerializationHandler savedRTreeHandler = this.m_serializationHandler;
        ToSAXHandler h = new ToXMLSAXHandler();
        h.setContentHandler(rtfHandler);
        h.setTransformer(this);
        this.m_serializationHandler = h;
        SerializationHandler rth = this.m_serializationHandler;
        try {
            rth.startDocument();
            rth.flushPending();
            executeChildTemplates(templateParent, true);
            rth.flushPending();
            int resultFragment = dtmFrag.getDocument();
            rth.endDocument();
            this.m_serializationHandler = savedRTreeHandler;
            return resultFragment;
        } catch (SAXException se) {
            try {
                throw new TransformerException(se);
            } catch (Throwable th) {
                this.m_serializationHandler = savedRTreeHandler;
            }
        } catch (Throwable th2) {
            rth.endDocument();
        }
    }

    public String transformToString(ElemTemplateElement elem) throws TransformerException {
        ElemTemplateElement firstChild = elem.getFirstChildElem();
        if (firstChild == null) {
            return "";
        }
        if (elem.hasTextLitOnly() && this.m_optimizer) {
            return ((ElemTextLiteral) firstChild).getNodeValue();
        }
        SerializationHandler savedRTreeHandler = this.m_serializationHandler;
        StringWriter sw = (StringWriter) this.m_stringWriterObjectPool.getInstance();
        this.m_serializationHandler = (ToTextStream) this.m_textResultHandlerObjectPool.getInstance();
        if (this.m_serializationHandler == null) {
            this.m_serializationHandler = (SerializationHandler) SerializerFactory.getSerializer(this.m_textformat.getProperties());
        }
        this.m_serializationHandler.setTransformer(this);
        this.m_serializationHandler.setWriter(sw);
        try {
            executeChildTemplates(elem, true);
            this.m_serializationHandler.endDocument();
            String result = sw.toString();
            sw.getBuffer().setLength(0);
            try {
                sw.close();
            } catch (Exception e) {
            }
            this.m_stringWriterObjectPool.freeInstance(sw);
            this.m_serializationHandler.reset();
            this.m_textResultHandlerObjectPool.freeInstance(this.m_serializationHandler);
            this.m_serializationHandler = savedRTreeHandler;
            return result;
        } catch (SAXException se) {
            throw new TransformerException(se);
        } catch (Throwable th) {
            sw.getBuffer().setLength(0);
            try {
                sw.close();
            } catch (Exception e2) {
            }
            this.m_stringWriterObjectPool.freeInstance(sw);
            this.m_serializationHandler.reset();
            this.m_textResultHandlerObjectPool.freeInstance(this.m_serializationHandler);
            this.m_serializationHandler = savedRTreeHandler;
        }
    }

    public boolean applyTemplateToNode(ElemTemplateElement xslInstruction, ElemTemplate template, int child) throws TransformerException {
        ElemTemplateElement template2;
        DTM dtm = this.m_xcontext.getDTM(child);
        short nodeType = dtm.getNodeType(child);
        boolean isDefaultTextRule = false;
        boolean isApplyImports = xslInstruction == null ? false : xslInstruction.getXSLToken() == 72;
        if (template == null || isApplyImports) {
            int maxImportLevel;
            int endImportLevel = 0;
            if (isApplyImports) {
                maxImportLevel = template.getStylesheetComposed().getImportCountComposed() - 1;
                endImportLevel = template.getStylesheetComposed().getEndImportCountComposed();
            } else {
                maxImportLevel = -1;
            }
            if (isApplyImports && maxImportLevel == -1) {
                template2 = null;
            } else {
                XPathContext xctxt = this.m_xcontext;
                try {
                    xctxt.pushNamespaceContext(xslInstruction);
                    QName mode = getMode();
                    if (isApplyImports) {
                        template2 = this.m_stylesheetRoot.getTemplateComposed(xctxt, child, mode, maxImportLevel, endImportLevel, this.m_quietConflictWarnings, dtm);
                    } else {
                        template2 = this.m_stylesheetRoot.getTemplateComposed(xctxt, child, mode, this.m_quietConflictWarnings, dtm);
                    }
                    xctxt.popNamespaceContext();
                } catch (Throwable th) {
                    xctxt.popNamespaceContext();
                }
            }
            if (template2 == null) {
                switch (nodeType) {
                    case (short) 1:
                    case (short) 11:
                        template2 = this.m_stylesheetRoot.getDefaultRule();
                        break;
                    case (short) 2:
                    case (short) 3:
                    case (short) 4:
                        template2 = this.m_stylesheetRoot.getDefaultTextRule();
                        isDefaultTextRule = true;
                        break;
                    case (short) 9:
                        template2 = this.m_stylesheetRoot.getDefaultRootRule();
                        break;
                    default:
                        return false;
                }
            }
        }
        try {
            pushElemTemplateElement(template2);
            this.m_xcontext.pushCurrentNode(child);
            pushPairCurrentMatched(template2, child);
            if (!isApplyImports) {
                this.m_xcontext.pushContextNodeList(new NodeSetDTM(child, this.m_xcontext.getDTMManager()));
            }
            if (isDefaultTextRule) {
                switch (nodeType) {
                    case (short) 2:
                        dtm.dispatchCharactersEvents(child, getResultTreeHandler(), false);
                        break;
                    case (short) 3:
                    case (short) 4:
                        ClonerToResultTree.cloneToResultTree(child, nodeType, dtm, getResultTreeHandler(), false);
                        break;
                }
            }
            this.m_xcontext.setSAXLocator(template2);
            this.m_xcontext.getVarStack().link(template2.m_frameSize);
            executeChildTemplates(template2, true);
            if (!isDefaultTextRule) {
                this.m_xcontext.getVarStack().unlink();
            }
            this.m_xcontext.popCurrentNode();
            if (!isApplyImports) {
                this.m_xcontext.popContextNodeList();
            }
            popCurrentMatched();
            popElemTemplateElement();
            return true;
        } catch (Throwable se) {
            throw new TransformerException(se);
        } catch (Throwable th2) {
            if (!isDefaultTextRule) {
                this.m_xcontext.getVarStack().unlink();
            }
            this.m_xcontext.popCurrentNode();
            if (!isApplyImports) {
                this.m_xcontext.popContextNodeList();
            }
            popCurrentMatched();
            popElemTemplateElement();
        }
    }

    public void executeChildTemplates(ElemTemplateElement elem, Node context, QName mode, ContentHandler handler) throws TransformerException {
        XPathContext xctxt = this.m_xcontext;
        if (mode != null) {
            try {
                pushMode(mode);
            } catch (Throwable th) {
                xctxt.popCurrentNode();
                if (mode != null) {
                    popMode();
                }
            }
        }
        xctxt.pushCurrentNode(xctxt.getDTMHandleFromNode(context));
        executeChildTemplates(elem, handler);
        xctxt.popCurrentNode();
        if (mode != null) {
            popMode();
        }
    }

    public void executeChildTemplates(ElemTemplateElement elem, boolean shouldAddAttrs) throws TransformerException {
        ElemTemplateElement t = elem.getFirstChildElem();
        if (t != null) {
            if (elem.hasTextLitOnly() && this.m_optimizer) {
                char[] chars = ((ElemTextLiteral) t).getChars();
                try {
                    pushElemTemplateElement(t);
                    this.m_serializationHandler.characters(chars, 0, chars.length);
                    popElemTemplateElement();
                } catch (SAXException se) {
                    throw new TransformerException(se);
                } catch (Throwable th) {
                    popElemTemplateElement();
                }
            } else {
                XPathContext xctxt = this.m_xcontext;
                xctxt.pushSAXLocatorNull();
                int currentTemplateElementsTop = this.m_currentTemplateElements.size();
                this.m_currentTemplateElements.push(null);
                while (t != null) {
                    if (!shouldAddAttrs) {
                        try {
                            if (t.getXSLToken() == 48) {
                                t = t.getNextSiblingElem();
                            }
                        } catch (RuntimeException re) {
                            TransformerException te = new TransformerException(re);
                            te.setLocator(t);
                            throw te;
                        } catch (Throwable th2) {
                            this.m_currentTemplateElements.pop();
                            xctxt.popSAXLocator();
                        }
                    }
                    xctxt.setSAXLocator(t);
                    this.m_currentTemplateElements.setElementAt(t, currentTemplateElementsTop);
                    t.execute(this);
                    t = t.getNextSiblingElem();
                }
                this.m_currentTemplateElements.pop();
                xctxt.popSAXLocator();
            }
        }
    }

    public void executeChildTemplates(ElemTemplateElement elem, ContentHandler handler) throws TransformerException {
        SerializationHandler xoh = getSerializationHandler();
        SerializationHandler savedHandler = xoh;
        try {
            xoh.flushPending();
            LexicalHandler lex = null;
            if (handler instanceof LexicalHandler) {
                lex = (LexicalHandler) handler;
            }
            this.m_serializationHandler = new ToXMLSAXHandler(handler, lex, xoh.getEncoding());
            this.m_serializationHandler.setTransformer(this);
            executeChildTemplates(elem, true);
            this.m_serializationHandler = xoh;
        } catch (TransformerException e) {
            throw e;
        } catch (SAXException se) {
            throw new TransformerException(se);
        } catch (Throwable th) {
            this.m_serializationHandler = xoh;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0131  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00aa  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Vector processSortKeys(ElemForEach foreach, int sourceNodeContext) throws TransformerException {
        Vector vector = null;
        XPathContext xctxt = this.m_xcontext;
        int nElems = foreach.getSortElemCount();
        if (nElems > 0) {
            vector = new Vector();
        }
        for (int i = 0; i < nElems; i++) {
            boolean descending;
            AVT caseOrder;
            boolean caseOrderUpper;
            ElemSort sort = foreach.getSortElem(i);
            String langString = sort.getLang() != null ? sort.getLang().evaluate(xctxt, sourceNodeContext, foreach) : null;
            String dataTypeString = sort.getDataType().evaluate(xctxt, sourceNodeContext, foreach);
            if (dataTypeString.indexOf(":") >= 0) {
                System.out.println("TODO: Need to write the hooks for QNAME sort data type");
            } else if (!(dataTypeString.equalsIgnoreCase("text") || (dataTypeString.equalsIgnoreCase("number") ^ 1) == 0)) {
                foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{Constants.ATTRNAME_DATATYPE, dataTypeString});
            }
            boolean treatAsNumbers = dataTypeString != null && dataTypeString.equals("number");
            String orderString = sort.getOrder().evaluate(xctxt, sourceNodeContext, foreach);
            if (!orderString.equalsIgnoreCase(Constants.ATTRVAL_ORDER_ASCENDING)) {
                if ((orderString.equalsIgnoreCase(Constants.ATTRVAL_ORDER_DESCENDING) ^ 1) != 0) {
                    foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{Constants.ATTRNAME_ORDER, orderString});
                }
            }
            if (orderString != null) {
                if (orderString.equals(Constants.ATTRVAL_ORDER_DESCENDING)) {
                    descending = true;
                    caseOrder = sort.getCaseOrder();
                    if (caseOrder == null) {
                        String caseOrderString = caseOrder.evaluate(xctxt, sourceNodeContext, foreach);
                        if (!(caseOrderString.equalsIgnoreCase(Constants.ATTRVAL_CASEORDER_UPPER) || (caseOrderString.equalsIgnoreCase(Constants.ATTRVAL_CASEORDER_LOWER) ^ 1) == 0)) {
                            foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{Constants.ATTRNAME_CASEORDER, caseOrderString});
                        }
                        caseOrderUpper = caseOrderString != null && caseOrderString.equals(Constants.ATTRVAL_CASEORDER_UPPER);
                    } else {
                        caseOrderUpper = false;
                    }
                    vector.addElement(new NodeSortKey(this, sort.getSelect(), treatAsNumbers, descending, langString, caseOrderUpper, foreach));
                }
            }
            descending = false;
            caseOrder = sort.getCaseOrder();
            if (caseOrder == null) {
            }
            vector.addElement(new NodeSortKey(this, sort.getSelect(), treatAsNumbers, descending, langString, caseOrderUpper, foreach));
        }
        return vector;
    }

    public int getCurrentTemplateElementsCount() {
        return this.m_currentTemplateElements.size();
    }

    public ObjectStack getCurrentTemplateElements() {
        return this.m_currentTemplateElements;
    }

    public void pushElemTemplateElement(ElemTemplateElement elem) {
        this.m_currentTemplateElements.push(elem);
    }

    public void popElemTemplateElement() {
        this.m_currentTemplateElements.pop();
    }

    public void setCurrentElement(ElemTemplateElement e) {
        this.m_currentTemplateElements.setTop(e);
    }

    public ElemTemplateElement getCurrentElement() {
        return this.m_currentTemplateElements.size() > 0 ? (ElemTemplateElement) this.m_currentTemplateElements.peek() : null;
    }

    public int getCurrentNode() {
        return this.m_xcontext.getCurrentNode();
    }

    public ElemTemplate getCurrentTemplate() {
        ElemTemplateElement elem = getCurrentElement();
        while (elem != null && elem.getXSLToken() != 19) {
            elem = elem.getParentElem();
        }
        return (ElemTemplate) elem;
    }

    public void pushPairCurrentMatched(ElemTemplateElement template, int child) {
        this.m_currentMatchTemplates.push(template);
        this.m_currentMatchedNodes.push(child);
    }

    public void popCurrentMatched() {
        this.m_currentMatchTemplates.pop();
        this.m_currentMatchedNodes.pop();
    }

    public ElemTemplate getMatchedTemplate() {
        return (ElemTemplate) this.m_currentMatchTemplates.peek();
    }

    public int getMatchedNode() {
        return this.m_currentMatchedNodes.peepTail();
    }

    public DTMIterator getContextNodeList() {
        DTMIterator dTMIterator = null;
        try {
            DTMIterator cnl = this.m_xcontext.getContextNodeList();
            if (cnl != null) {
                dTMIterator = cnl.cloneWithReset();
            }
            return dTMIterator;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Transformer getTransformer() {
        return this;
    }

    public void setStylesheet(StylesheetRoot stylesheetRoot) {
        this.m_stylesheetRoot = stylesheetRoot;
    }

    public final StylesheetRoot getStylesheet() {
        return this.m_stylesheetRoot;
    }

    public boolean getQuietConflictWarnings() {
        return this.m_quietConflictWarnings;
    }

    public void setXPathContext(XPathContext xcontext) {
        this.m_xcontext = xcontext;
    }

    public final XPathContext getXPathContext() {
        return this.m_xcontext;
    }

    public SerializationHandler getResultTreeHandler() {
        return this.m_serializationHandler;
    }

    public SerializationHandler getSerializationHandler() {
        return this.m_serializationHandler;
    }

    public KeyManager getKeyManager() {
        return this.m_keyManager;
    }

    public boolean isRecursiveAttrSet(ElemAttributeSet attrSet) {
        if (this.m_attrSetStack == null) {
            this.m_attrSetStack = new Stack();
        }
        if (this.m_attrSetStack.empty() || this.m_attrSetStack.search(attrSet) <= -1) {
            return false;
        }
        return true;
    }

    public void pushElemAttributeSet(ElemAttributeSet attrSet) {
        this.m_attrSetStack.push(attrSet);
    }

    public void popElemAttributeSet() {
        this.m_attrSetStack.pop();
    }

    public CountersTable getCountersTable() {
        if (this.m_countersTable == null) {
            this.m_countersTable = new CountersTable();
        }
        return this.m_countersTable;
    }

    public boolean currentTemplateRuleIsNull() {
        if (this.m_currentTemplateRuleIsNull.isEmpty() || !this.m_currentTemplateRuleIsNull.peek()) {
            return false;
        }
        return true;
    }

    public void pushCurrentTemplateRuleIsNull(boolean b) {
        this.m_currentTemplateRuleIsNull.push(b);
    }

    public void popCurrentTemplateRuleIsNull() {
        this.m_currentTemplateRuleIsNull.pop();
    }

    public void pushCurrentFuncResult(Object val) {
        this.m_currentFuncResult.push(val);
    }

    public Object popCurrentFuncResult() {
        return this.m_currentFuncResult.pop();
    }

    public boolean currentFuncResultSeen() {
        if (this.m_currentFuncResult.empty() || this.m_currentFuncResult.peek() == null) {
            return false;
        }
        return true;
    }

    public MsgMgr getMsgMgr() {
        if (this.m_msgMgr == null) {
            this.m_msgMgr = new MsgMgr(this);
        }
        return this.m_msgMgr;
    }

    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        synchronized (this.m_reentryGuard) {
            if (listener == null) {
                throw new IllegalArgumentException(XSLMessages.createMessage("ER_NULL_ERROR_HANDLER", null));
            }
            this.m_errorHandler = listener;
        }
    }

    public ErrorListener getErrorListener() {
        return this.m_errorHandler;
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/trax/features/sax/input".equals(name) || "http://xml.org/trax/features/dom/input".equals(name)) {
            return true;
        }
        throw new SAXNotRecognizedException(name);
    }

    public QName getMode() {
        return this.m_modes.isEmpty() ? null : (QName) this.m_modes.peek();
    }

    public void pushMode(QName mode) {
        this.m_modes.push(mode);
    }

    public void popMode() {
        this.m_modes.pop();
    }

    public void runTransformThread(int priority) {
        setTransformThread(ThreadControllerWrapper.runThread(this, priority));
    }

    public void runTransformThread() {
        ThreadControllerWrapper.runThread(this, -1);
    }

    public static void runTransformThread(Runnable runnable) {
        ThreadControllerWrapper.runThread(runnable, -1);
    }

    public void waitTransformThread() throws SAXException {
        Thread transformThread = getTransformThread();
        if (transformThread != null) {
            try {
                ThreadControllerWrapper.waitThread(transformThread, this);
                if (!hasTransformThreadErrorCatcher()) {
                    Exception e = getExceptionThrown();
                    if (e != null) {
                        e.printStackTrace();
                        throw new SAXException(e);
                    }
                }
                setTransformThread(null);
            } catch (InterruptedException e2) {
            }
        }
    }

    public Exception getExceptionThrown() {
        return this.m_exceptionThrown;
    }

    public void setExceptionThrown(Exception e) {
        this.m_exceptionThrown = e;
    }

    public void setSourceTreeDocForThread(int doc) {
        this.m_doc = doc;
    }

    void postExceptionFromThread(Exception e) {
        this.m_exceptionThrown = e;
        synchronized (this) {
            notifyAll();
        }
    }

    public void run() {
        this.m_hasBeenReset = false;
        try {
            transformNode(this.m_doc);
            try {
                if (this.m_inputContentHandler instanceof TransformerHandlerImpl) {
                    ((TransformerHandlerImpl) this.m_inputContentHandler).clearCoRoutine();
                }
            } catch (Exception e) {
                if (this.m_transformThread != null) {
                    postExceptionFromThread(e);
                    return;
                }
                throw new RuntimeException(e.getMessage());
            }
        } catch (Exception e2) {
            if (this.m_transformThread != null) {
                postExceptionFromThread(e2);
                if (this.m_inputContentHandler instanceof TransformerHandlerImpl) {
                    ((TransformerHandlerImpl) this.m_inputContentHandler).clearCoRoutine();
                    return;
                }
                return;
            }
            throw new RuntimeException(e2.getMessage());
        } catch (Throwable th) {
            Throwable th2 = th;
            if (this.m_inputContentHandler instanceof TransformerHandlerImpl) {
                ((TransformerHandlerImpl) this.m_inputContentHandler).clearCoRoutine();
            }
        }
    }

    public short getShouldStripSpace(int elementHandle, DTM dtm) {
        try {
            WhiteSpaceInfo info = this.m_stylesheetRoot.getWhiteSpaceInfo(this.m_xcontext, elementHandle, dtm);
            if (info == null) {
                return (short) 3;
            }
            return info.getShouldStripSpace() ? (short) 2 : (short) 1;
        } catch (TransformerException e) {
            return (short) 3;
        }
    }

    public void init(ToXMLSAXHandler h, Transformer transformer, ContentHandler realHandler) {
        h.setTransformer(transformer);
        h.setContentHandler(realHandler);
    }

    public void setSerializationHandler(SerializationHandler xoh) {
        this.m_serializationHandler = xoh;
    }

    public void fireGenerateEvent(int eventType, char[] ch, int start, int length) {
    }

    public void fireGenerateEvent(int eventType, String name, Attributes atts) {
    }

    public void fireGenerateEvent(int eventType, String name, String data) {
    }

    public void fireGenerateEvent(int eventType, String data) {
    }

    public void fireGenerateEvent(int eventType) {
    }

    public boolean hasTraceListeners() {
        return false;
    }

    public boolean getIncremental() {
        return this.m_incremental;
    }

    public boolean getOptimize() {
        return this.m_optimizer;
    }

    public boolean getSource_location() {
        return this.m_source_location;
    }
}
