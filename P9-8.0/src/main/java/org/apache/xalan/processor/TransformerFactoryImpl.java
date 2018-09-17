package org.apache.xalan.processor;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TrAXFilter;
import org.apache.xalan.transformer.TransformerIdentityImpl;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.DOM2Helper;
import org.apache.xml.utils.DefaultErrorHandler;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.utils.TreeWalker;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class TransformerFactoryImpl extends SAXTransformerFactory {
    public static final String FEATURE_INCREMENTAL = "http://xml.apache.org/xalan/features/incremental";
    public static final String FEATURE_OPTIMIZE = "http://xml.apache.org/xalan/features/optimize";
    public static final String FEATURE_SOURCE_LOCATION = "http://xml.apache.org/xalan/properties/source-location";
    public static final String XSLT_PROPERTIES = "org/apache/xalan/res/XSLTInfo.properties";
    private String m_DOMsystemID = null;
    private ErrorListener m_errorListener = new DefaultErrorHandler(false);
    private boolean m_incremental = false;
    private boolean m_isSecureProcessing = false;
    private boolean m_optimize = true;
    private boolean m_source_location = false;
    URIResolver m_uriResolver;

    public Templates processFromNode(Node node) throws TransformerConfigurationException {
        try {
            TemplatesHandler builder = newTemplatesHandler();
            new TreeWalker(builder, new DOM2Helper(), builder.getSystemId()).traverse(node);
            return builder.getTemplates();
        } catch (SAXException se) {
            if (this.m_errorListener != null) {
                try {
                    this.m_errorListener.fatalError(new TransformerException(se));
                    return null;
                } catch (TransformerConfigurationException ex) {
                    throw ex;
                } catch (TransformerException ex2) {
                    throw new TransformerConfigurationException(ex2);
                }
            }
            throw new TransformerConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_PROCESSFROMNODE_FAILED, null), se);
        } catch (TransformerConfigurationException tce) {
            throw tce;
        } catch (Exception e) {
            if (this.m_errorListener != null) {
                try {
                    this.m_errorListener.fatalError(new TransformerException(e));
                    return null;
                } catch (TransformerConfigurationException ex3) {
                    throw ex3;
                } catch (TransformerException ex22) {
                    throw new TransformerConfigurationException(ex22);
                }
            }
            throw new TransformerConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_PROCESSFROMNODE_FAILED, null), e);
        }
    }

    String getDOMsystemID() {
        return this.m_DOMsystemID;
    }

    Templates processFromNode(Node node, String systemID) throws TransformerConfigurationException {
        this.m_DOMsystemID = systemID;
        return processFromNode(node);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:org.apache.xalan.processor.TransformerFactoryImpl.getAssociatedStylesheet(javax.xml.transform.Source, java.lang.String, java.lang.String, java.lang.String):javax.xml.transform.Source, dom blocks: [B:7:0x0038, B:15:0x006c]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00a7 A:{Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd, StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }, Splitter: B:7:0x0038, ExcHandler: org.apache.xml.utils.StopParseException (e org.apache.xml.utils.StopParseException)} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00cd A:{Splitter: B:7:0x0038, ExcHandler: java.io.IOException (r12_0 'ioe' java.io.IOException)} */
    public javax.xml.transform.Source getAssociatedStylesheet(javax.xml.transform.Source r23, java.lang.String r24, java.lang.String r25, java.lang.String r26) throws javax.xml.transform.TransformerConfigurationException {
        /*
        r22 = this;
        r13 = 0;
        r15 = 0;
        r16 = 0;
        r0 = r23;
        r0 = r0 instanceof javax.xml.transform.dom.DOMSource;
        r20 = r0;
        if (r20 == 0) goto L_0x0050;
    L_0x000c:
        r5 = r23;
        r5 = (javax.xml.transform.dom.DOMSource) r5;
        r15 = r5.getNode();
        r4 = r5.getSystemId();
    L_0x0018:
        r11 = new org.apache.xml.utils.StylesheetPIHandler;
        r0 = r24;
        r1 = r25;
        r2 = r26;
        r11.<init>(r4, r0, r1, r2);
        r0 = r22;
        r0 = r0.m_uriResolver;
        r20 = r0;
        if (r20 == 0) goto L_0x0036;
    L_0x002b:
        r0 = r22;
        r0 = r0.m_uriResolver;
        r20 = r0;
        r0 = r20;
        r11.setURIResolver(r0);
    L_0x0036:
        if (r15 == 0) goto L_0x0059;
    L_0x0038:
        r19 = new org.apache.xml.utils.TreeWalker;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r20 = new org.apache.xml.utils.DOM2Helper;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r20.<init>();	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0 = r19;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r1 = r20;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0.<init>(r11, r1, r4);	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0 = r19;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0.traverse(r15);	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
    L_0x004b:
        r20 = r11.getAssociatedStylesheet();
        return r20;
    L_0x0050:
        r13 = javax.xml.transform.sax.SAXSource.sourceToInputSource(r23);
        r4 = r13.getSystemId();
        goto L_0x0018;
    L_0x0059:
        r10 = javax.xml.parsers.SAXParserFactory.newInstance();	 Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd }
        r20 = 1;	 Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd }
        r0 = r20;	 Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd }
        r10.setNamespaceAware(r0);	 Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd }
        r0 = r22;	 Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd }
        r0 = r0.m_isSecureProcessing;	 Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd }
        r20 = r0;	 Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd }
        if (r20 == 0) goto L_0x0078;
    L_0x006c:
        r20 = "http://javax.xml.XMLConstants/feature/secure-processing";	 Catch:{ SAXException -> 0x00df, StopParseException -> 0x00a7, IOException -> 0x00cd }
        r21 = 1;	 Catch:{ SAXException -> 0x00df, StopParseException -> 0x00a7, IOException -> 0x00cd }
        r0 = r20;	 Catch:{ SAXException -> 0x00df, StopParseException -> 0x00a7, IOException -> 0x00cd }
        r1 = r21;	 Catch:{ SAXException -> 0x00df, StopParseException -> 0x00a7, IOException -> 0x00cd }
        r10.setFeature(r0, r1);	 Catch:{ SAXException -> 0x00df, StopParseException -> 0x00a7, IOException -> 0x00cd }
    L_0x0078:
        r14 = r10.newSAXParser();	 Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd }
        r16 = r14.getXMLReader();	 Catch:{ ParserConfigurationException -> 0x00c4, FactoryConfigurationError -> 0x00a9, NoSuchMethodError -> 0x00db, AbstractMethodError -> 0x00dd }
    L_0x0080:
        if (r16 != 0) goto L_0x0086;
    L_0x0082:
        r16 = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
    L_0x0086:
        r0 = r22;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0 = r0.m_isSecureProcessing;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r20 = r0;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        if (r20 == 0) goto L_0x009c;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
    L_0x008e:
        r20 = "http://xml.org/sax/features/external-general-entities";	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r21 = 0;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0 = r16;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r1 = r20;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r2 = r21;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0.setFeature(r1, r2);	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
    L_0x009c:
        r0 = r16;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0.setContentHandler(r11);	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0 = r16;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0.parse(r13);	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        goto L_0x004b;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
    L_0x00a7:
        r18 = move-exception;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        goto L_0x004b;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
    L_0x00a9:
        r8 = move-exception;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r20 = new org.xml.sax.SAXException;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r21 = r8.toString();	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r20.<init>(r21);	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        throw r20;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
    L_0x00b4:
        r17 = move-exception;
        r20 = new javax.xml.transform.TransformerConfigurationException;
        r21 = "getAssociatedStylesheets failed";
        r0 = r20;
        r1 = r21;
        r2 = r17;
        r0.<init>(r1, r2);
        throw r20;
    L_0x00c4:
        r7 = move-exception;
        r20 = new org.xml.sax.SAXException;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0 = r20;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        r0.<init>(r7);	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
        throw r20;	 Catch:{ StopParseException -> 0x00a7, SAXException -> 0x00b4, IOException -> 0x00cd }
    L_0x00cd:
        r12 = move-exception;
        r20 = new javax.xml.transform.TransformerConfigurationException;
        r21 = "getAssociatedStylesheets failed";
        r0 = r20;
        r1 = r21;
        r0.<init>(r1, r12);
        throw r20;
    L_0x00db:
        r9 = move-exception;
        goto L_0x0080;
    L_0x00dd:
        r3 = move-exception;
        goto L_0x0080;
    L_0x00df:
        r6 = move-exception;
        goto L_0x0078;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xalan.processor.TransformerFactoryImpl.getAssociatedStylesheet(javax.xml.transform.Source, java.lang.String, java.lang.String, java.lang.String):javax.xml.transform.Source");
    }

    public TemplatesHandler newTemplatesHandler() throws TransformerConfigurationException {
        return new StylesheetHandler(this);
    }

    public void setFeature(String name, boolean value) throws TransformerConfigurationException {
        if (name == null) {
            throw new NullPointerException(XSLMessages.createMessage(XSLTErrorResources.ER_SET_FEATURE_NULL_NAME, null));
        } else if (name.equals("http://javax.xml.XMLConstants/feature/secure-processing")) {
            this.m_isSecureProcessing = value;
        } else {
            throw new TransformerConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_UNSUPPORTED_FEATURE, new Object[]{name}));
        }
    }

    /* JADX WARNING: Missing block: B:8:0x001b, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getFeature(String name) {
        if (name == null) {
            throw new NullPointerException(XSLMessages.createMessage(XSLTErrorResources.ER_GET_FEATURE_NULL_NAME, null));
        } else if ("http://javax.xml.transform.dom.DOMResult/feature" == name || "http://javax.xml.transform.dom.DOMSource/feature" == name || "http://javax.xml.transform.sax.SAXResult/feature" == name || "http://javax.xml.transform.sax.SAXSource/feature" == name || "http://javax.xml.transform.stream.StreamResult/feature" == name || "http://javax.xml.transform.stream.StreamSource/feature" == name || "http://javax.xml.transform.sax.SAXTransformerFactory/feature" == name || "http://javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter" == name || "http://javax.xml.transform.dom.DOMResult/feature".equals(name) || "http://javax.xml.transform.dom.DOMSource/feature".equals(name) || "http://javax.xml.transform.sax.SAXResult/feature".equals(name) || "http://javax.xml.transform.sax.SAXSource/feature".equals(name) || "http://javax.xml.transform.stream.StreamResult/feature".equals(name) || "http://javax.xml.transform.stream.StreamSource/feature".equals(name) || "http://javax.xml.transform.sax.SAXTransformerFactory/feature".equals(name) || "http://javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter".equals(name)) {
            return true;
        } else {
            if (name.equals("http://javax.xml.XMLConstants/feature/secure-processing")) {
                return this.m_isSecureProcessing;
            }
            return false;
        }
    }

    public void setAttribute(String name, Object value) throws IllegalArgumentException {
        if (name.equals(FEATURE_INCREMENTAL)) {
            if (value instanceof Boolean) {
                this.m_incremental = ((Boolean) value).booleanValue();
            } else if (value instanceof String) {
                this.m_incremental = new Boolean((String) value).booleanValue();
            } else {
                throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_BAD_VALUE, new Object[]{name, value}));
            }
        } else if (name.equals(FEATURE_OPTIMIZE)) {
            if (value instanceof Boolean) {
                this.m_optimize = ((Boolean) value).booleanValue();
            } else if (value instanceof String) {
                this.m_optimize = new Boolean((String) value).booleanValue();
            } else {
                throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_BAD_VALUE, new Object[]{name, value}));
            }
        } else if (!name.equals("http://xml.apache.org/xalan/properties/source-location")) {
            throw new IllegalArgumentException(XSLMessages.createMessage("ER_NOT_SUPPORTED", new Object[]{name}));
        } else if (value instanceof Boolean) {
            this.m_source_location = ((Boolean) value).booleanValue();
        } else if (value instanceof String) {
            this.m_source_location = new Boolean((String) value).booleanValue();
        } else {
            throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_BAD_VALUE, new Object[]{name, value}));
        }
    }

    public Object getAttribute(String name) throws IllegalArgumentException {
        if (name.equals(FEATURE_INCREMENTAL)) {
            return new Boolean(this.m_incremental);
        }
        if (name.equals(FEATURE_OPTIMIZE)) {
            return new Boolean(this.m_optimize);
        }
        if (name.equals("http://xml.apache.org/xalan/properties/source-location")) {
            return new Boolean(this.m_source_location);
        }
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_ATTRIB_VALUE_NOT_RECOGNIZED, new Object[]{name}));
    }

    public XMLFilter newXMLFilter(Source src) throws TransformerConfigurationException {
        Templates templates = newTemplates(src);
        if (templates == null) {
            return null;
        }
        return newXMLFilter(templates);
    }

    public XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException {
        try {
            return new TrAXFilter(templates);
        } catch (TransformerConfigurationException ex) {
            if (this.m_errorListener != null) {
                try {
                    this.m_errorListener.fatalError(ex);
                    return null;
                } catch (TransformerConfigurationException ex1) {
                    throw ex1;
                } catch (TransformerException ex12) {
                    throw new TransformerConfigurationException(ex12);
                }
            }
            throw ex;
        }
    }

    public TransformerHandler newTransformerHandler(Source src) throws TransformerConfigurationException {
        Templates templates = newTemplates(src);
        if (templates == null) {
            return null;
        }
        return newTransformerHandler(templates);
    }

    public TransformerHandler newTransformerHandler(Templates templates) throws TransformerConfigurationException {
        try {
            TransformerImpl transformer = (TransformerImpl) templates.newTransformer();
            transformer.setURIResolver(this.m_uriResolver);
            return (TransformerHandler) transformer.getInputContentHandler(true);
        } catch (TransformerConfigurationException ex) {
            if (this.m_errorListener != null) {
                try {
                    this.m_errorListener.fatalError(ex);
                    return null;
                } catch (TransformerConfigurationException ex1) {
                    throw ex1;
                } catch (TransformerException ex12) {
                    throw new TransformerConfigurationException(ex12);
                }
            }
            throw ex;
        }
    }

    public TransformerHandler newTransformerHandler() throws TransformerConfigurationException {
        return new TransformerIdentityImpl(this.m_isSecureProcessing);
    }

    public Transformer newTransformer(Source source) throws TransformerConfigurationException {
        try {
            Templates tmpl = newTemplates(source);
            if (tmpl == null) {
                return null;
            }
            Transformer transformer = tmpl.newTransformer();
            transformer.setURIResolver(this.m_uriResolver);
            return transformer;
        } catch (TransformerConfigurationException ex) {
            if (this.m_errorListener != null) {
                try {
                    this.m_errorListener.fatalError(ex);
                    return null;
                } catch (TransformerConfigurationException ex1) {
                    throw ex1;
                } catch (TransformerException ex12) {
                    throw new TransformerConfigurationException(ex12);
                }
            }
            throw ex;
        }
    }

    public Transformer newTransformer() throws TransformerConfigurationException {
        return new TransformerIdentityImpl(this.m_isSecureProcessing);
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00c6 A:{Splitter: B:12:0x003f, ExcHandler: java.lang.Exception (r7_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:30:0x0094, code:
            r9 = move-exception;
     */
    /* JADX WARNING: Missing block: B:33:0x009e, code:
            throw new org.xml.sax.SAXException(r9.toString());
     */
    /* JADX WARNING: Missing block: B:34:0x009f, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:36:0x00a6, code:
            if (r22.m_errorListener != null) goto L_0x00a8;
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            r22.m_errorListener.fatalError(new javax.xml.transform.TransformerException(r19));
     */
    /* JADX WARNING: Missing block: B:39:0x00bb, code:
            r10 = move-exception;
     */
    /* JADX WARNING: Missing block: B:40:0x00bc, code:
            throw r10;
     */
    /* JADX WARNING: Missing block: B:41:0x00bd, code:
            r8 = move-exception;
     */
    /* JADX WARNING: Missing block: B:44:0x00c5, code:
            throw new org.xml.sax.SAXException(r8);
     */
    /* JADX WARNING: Missing block: B:45:0x00c6, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:47:0x00cd, code:
            if (r22.m_errorListener != null) goto L_0x00cf;
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r22.m_errorListener.fatalError(new javax.xml.transform.TransformerException(r7));
     */
    /* JADX WARNING: Missing block: B:51:0x00e1, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:52:0x00e2, code:
            r11 = move-exception;
     */
    /* JADX WARNING: Missing block: B:54:0x00ea, code:
            throw new javax.xml.transform.TransformerConfigurationException(r11);
     */
    /* JADX WARNING: Missing block: B:55:0x00eb, code:
            r10 = move-exception;
     */
    /* JADX WARNING: Missing block: B:56:0x00ec, code:
            throw r10;
     */
    /* JADX WARNING: Missing block: B:58:0x00fa, code:
            throw new javax.xml.transform.TransformerConfigurationException(r7.getMessage(), r7);
     */
    /* JADX WARNING: Missing block: B:59:0x00fb, code:
            r11 = move-exception;
     */
    /* JADX WARNING: Missing block: B:61:0x0103, code:
            throw new javax.xml.transform.TransformerConfigurationException(r11);
     */
    /* JADX WARNING: Missing block: B:63:0x0113, code:
            throw new javax.xml.transform.TransformerConfigurationException(r19.getMessage(), r19);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Templates newTemplates(Source source) throws TransformerConfigurationException {
        String baseID = source.getSystemId();
        if (baseID != null) {
            baseID = SystemIDResolver.getAbsoluteURI(baseID);
        }
        if (source instanceof DOMSource) {
            Node node = ((DOMSource) source).getNode();
            if (node != null) {
                return processFromNode(node, baseID);
            }
            throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_DOMSOURCE_INPUT, null));
        }
        InputSource isource;
        XMLReader reader;
        TemplatesHandler builder = newTemplatesHandler();
        builder.setSystemId(baseID);
        try {
            isource = SAXSource.sourceToInputSource(source);
            isource.setSystemId(baseID);
            reader = null;
            if (source instanceof SAXSource) {
                reader = ((SAXSource) source).getXMLReader();
            }
            if (reader == null) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                if (this.m_isSecureProcessing) {
                    factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
                }
                reader = factory.newSAXParser().getXMLReader();
            }
        } catch (SAXException e) {
        } catch (Exception e2) {
        }
        if (reader == null) {
            reader = XMLReaderFactory.createXMLReader();
        }
        reader.setContentHandler(builder);
        reader.parse(isource);
        return builder.getTemplates();
    }

    public void setURIResolver(URIResolver resolver) {
        this.m_uriResolver = resolver;
    }

    public URIResolver getURIResolver() {
        return this.m_uriResolver;
    }

    public ErrorListener getErrorListener() {
        return this.m_errorListener;
    }

    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_ERRORLISTENER, null));
        }
        this.m_errorListener = listener;
    }

    public boolean isSecureProcessing() {
        return this.m_isSecureProcessing;
    }
}
