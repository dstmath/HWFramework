package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import ohos.ai.asr.util.AsrConstants;
import ohos.com.sun.org.apache.xalan.internal.XalanConstants;
import ohos.com.sun.org.apache.xalan.internal.utils.FeaturePropertyBase;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xalan.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xml.internal.utils.StopParseException;
import ohos.com.sun.org.apache.xml.internal.utils.StylesheetPIHandler;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.Templates;
import ohos.javax.xml.transform.Transformer;
import ohos.javax.xml.transform.TransformerConfigurationException;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.URIResolver;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.transform.sax.SAXTransformerFactory;
import ohos.javax.xml.transform.sax.TemplatesHandler;
import ohos.javax.xml.transform.sax.TransformerHandler;
import ohos.jdk.xml.internal.JdkXmlFeatures;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLFilter;
import ohos.org.xml.sax.XMLReader;

public class TransformerFactoryImpl extends SAXTransformerFactory implements SourceLoader, ErrorListener {
    public static final String AUTO_TRANSLET = "auto-translet";
    public static final String DEBUG = "debug";
    protected static final String DEFAULT_TRANSLET_NAME = "GregorSamsa";
    public static final String DESTINATION_DIRECTORY = "destination-directory";
    public static final String ENABLE_INLINING = "enable-inlining";
    public static final String GENERATE_TRANSLET = "generate-translet";
    public static final String INDENT_NUMBER = "indent-number";
    public static final String JAR_NAME = "jar-name";
    public static final String PACKAGE_NAME = "package-name";
    public static final String TRANSLET_NAME = "translet-name";
    public static final String USE_CLASSPATH = "use-classpath";
    private String _accessExternalDTD = "all";
    private String _accessExternalStylesheet = "all";
    private boolean _autoTranslet = false;
    private boolean _debug = false;
    private String _destinationDirectory = null;
    private boolean _enableInlining = false;
    private ErrorListener _errorListener = this;
    private ClassLoader _extensionClassLoader = null;
    private boolean _generateTranslet = false;
    private int _indentNumber = -1;
    private boolean _isNotSecureProcessing = true;
    private boolean _isSecureMode = false;
    private String _jarFileName = null;
    private boolean _overrideDefaultParser;
    private String _packageName = null;
    private Map<Source, PIParamWrapper> _piParams = null;
    private String _transletName = DEFAULT_TRANSLET_NAME;
    private URIResolver _uriResolver = null;
    private boolean _useClasspath = false;
    private final JdkXmlFeatures _xmlFeatures;
    private XMLSecurityManager _xmlSecurityManager;
    private XMLSecurityPropertyManager _xmlSecurityPropertyMgr;
    private Map<String, Class> _xsltcExtensionFunctions;

    /* access modifiers changed from: private */
    public static class PIParamWrapper {
        public String _charset = null;
        public String _media = null;
        public String _title = null;

        public PIParamWrapper(String str, String str2, String str3) {
            this._media = str;
            this._title = str2;
            this._charset = str3;
        }
    }

    public TransformerFactoryImpl() {
        if (System.getSecurityManager() != null) {
            this._isSecureMode = true;
            this._isNotSecureProcessing = false;
        }
        this._xmlFeatures = new JdkXmlFeatures(!this._isNotSecureProcessing);
        this._overrideDefaultParser = this._xmlFeatures.getFeature(JdkXmlFeatures.XmlFeature.JDK_OVERRIDE_PARSER);
        this._xmlSecurityPropertyMgr = new XMLSecurityPropertyManager();
        this._accessExternalDTD = this._xmlSecurityPropertyMgr.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        this._accessExternalStylesheet = this._xmlSecurityPropertyMgr.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_STYLESHEET);
        this._xmlSecurityManager = new XMLSecurityManager(true);
        this._xsltcExtensionFunctions = null;
    }

    public Map<String, Class> getExternalExtensionsMap() {
        return this._xsltcExtensionFunctions;
    }

    public void setErrorListener(ErrorListener errorListener) throws IllegalArgumentException {
        if (errorListener != null) {
            this._errorListener = errorListener;
            return;
        }
        throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.ERROR_LISTENER_NULL_ERR, "TransformerFactory").toString());
    }

    public ErrorListener getErrorListener() {
        return this._errorListener;
    }

    public Object getAttribute(String str) throws IllegalArgumentException {
        if (str.equals(TRANSLET_NAME)) {
            return this._transletName;
        }
        if (str.equals(GENERATE_TRANSLET)) {
            return new Boolean(this._generateTranslet);
        }
        if (str.equals(AUTO_TRANSLET)) {
            return new Boolean(this._autoTranslet);
        }
        if (str.equals(ENABLE_INLINING)) {
            if (this._enableInlining) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (str.equals("http://apache.org/xml/properties/security-manager")) {
            return this._xmlSecurityManager;
        } else {
            if (str.equals(XalanConstants.JDK_EXTENSION_CLASSLOADER)) {
                return this._extensionClassLoader;
            }
            XMLSecurityManager xMLSecurityManager = this._xmlSecurityManager;
            String str2 = null;
            String limitAsString = xMLSecurityManager != null ? xMLSecurityManager.getLimitAsString(str) : null;
            if (limitAsString != null) {
                return limitAsString;
            }
            XMLSecurityPropertyManager xMLSecurityPropertyManager = this._xmlSecurityPropertyMgr;
            if (xMLSecurityPropertyManager != null) {
                str2 = xMLSecurityPropertyManager.getValue(str);
            }
            if (str2 != null) {
                return str2;
            }
            throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.JAXP_INVALID_ATTR_ERR, str).toString());
        }
    }

    public void setAttribute(String str, Object obj) throws IllegalArgumentException {
        if (str.equals(TRANSLET_NAME) && (obj instanceof String)) {
            this._transletName = (String) obj;
        } else if (str.equals(DESTINATION_DIRECTORY) && (obj instanceof String)) {
            this._destinationDirectory = (String) obj;
        } else if (str.equals(PACKAGE_NAME) && (obj instanceof String)) {
            this._packageName = (String) obj;
        } else if (!str.equals(JAR_NAME) || !(obj instanceof String)) {
            if (str.equals(GENERATE_TRANSLET)) {
                if (obj instanceof Boolean) {
                    this._generateTranslet = ((Boolean) obj).booleanValue();
                    return;
                } else if (obj instanceof String) {
                    this._generateTranslet = ((String) obj).equalsIgnoreCase("true");
                    return;
                }
            } else if (str.equals(AUTO_TRANSLET)) {
                if (obj instanceof Boolean) {
                    this._autoTranslet = ((Boolean) obj).booleanValue();
                    return;
                } else if (obj instanceof String) {
                    this._autoTranslet = ((String) obj).equalsIgnoreCase("true");
                    return;
                }
            } else if (str.equals(USE_CLASSPATH)) {
                if (obj instanceof Boolean) {
                    this._useClasspath = ((Boolean) obj).booleanValue();
                    return;
                } else if (obj instanceof String) {
                    this._useClasspath = ((String) obj).equalsIgnoreCase("true");
                    return;
                }
            } else if (str.equals("debug")) {
                if (obj instanceof Boolean) {
                    this._debug = ((Boolean) obj).booleanValue();
                    return;
                } else if (obj instanceof String) {
                    this._debug = ((String) obj).equalsIgnoreCase("true");
                    return;
                }
            } else if (str.equals(ENABLE_INLINING)) {
                if (obj instanceof Boolean) {
                    this._enableInlining = ((Boolean) obj).booleanValue();
                    return;
                } else if (obj instanceof String) {
                    this._enableInlining = ((String) obj).equalsIgnoreCase("true");
                    return;
                }
            } else if (str.equals(INDENT_NUMBER)) {
                if (obj instanceof String) {
                    try {
                        this._indentNumber = Integer.parseInt((String) obj);
                        return;
                    } catch (NumberFormatException unused) {
                    }
                } else if (obj instanceof Integer) {
                    this._indentNumber = ((Integer) obj).intValue();
                    return;
                }
            } else if (str.equals(XalanConstants.JDK_EXTENSION_CLASSLOADER)) {
                if (obj instanceof ClassLoader) {
                    this._extensionClassLoader = (ClassLoader) obj;
                    return;
                }
                throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.JAXP_INVALID_ATTR_VALUE_ERR, "Extension Functions ClassLoader").toString());
            }
            XMLSecurityManager xMLSecurityManager = this._xmlSecurityManager;
            if (xMLSecurityManager == null || !xMLSecurityManager.setLimit(str, XMLSecurityManager.State.APIPROPERTY, obj)) {
                XMLSecurityPropertyManager xMLSecurityPropertyManager = this._xmlSecurityPropertyMgr;
                if (xMLSecurityPropertyManager == null || !xMLSecurityPropertyManager.setValue(str, FeaturePropertyBase.State.APIPROPERTY, obj)) {
                    throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.JAXP_INVALID_ATTR_ERR, str).toString());
                }
                this._accessExternalDTD = this._xmlSecurityPropertyMgr.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
                this._accessExternalStylesheet = this._xmlSecurityPropertyMgr.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_STYLESHEET);
            }
        } else {
            this._jarFileName = (String) obj;
        }
    }

    public void setFeature(String str, boolean z) throws TransformerConfigurationException {
        JdkXmlFeatures jdkXmlFeatures;
        if (str == null) {
            throw new NullPointerException(new ErrorMsg(ErrorMsg.JAXP_SET_FEATURE_NULL_NAME).toString());
        } else if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
            if (!this._isSecureMode || z) {
                this._isNotSecureProcessing = !z;
                this._xmlSecurityManager.setSecureProcessing(z);
                if (z) {
                    this._xmlSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD, FeaturePropertyBase.State.FSP, "");
                    this._xmlSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_STYLESHEET, FeaturePropertyBase.State.FSP, "");
                    this._accessExternalDTD = this._xmlSecurityPropertyMgr.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
                    this._accessExternalStylesheet = this._xmlSecurityPropertyMgr.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_STYLESHEET);
                }
                if (z && (jdkXmlFeatures = this._xmlFeatures) != null) {
                    jdkXmlFeatures.setFeature(JdkXmlFeatures.XmlFeature.ENABLE_EXTENSION_FUNCTION, JdkXmlFeatures.State.FSP, false);
                    return;
                }
                return;
            }
            throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.JAXP_SECUREPROCESSING_FEATURE).toString());
        } else if (!str.equals("http://www.oracle.com/feature/use-service-mechanism") || !this._isSecureMode) {
            JdkXmlFeatures jdkXmlFeatures2 = this._xmlFeatures;
            if (jdkXmlFeatures2 == null || !jdkXmlFeatures2.setFeature(str, JdkXmlFeatures.State.APIPROPERTY, Boolean.valueOf(z))) {
                throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.JAXP_UNSUPPORTED_FEATURE, str).toString());
            } else if (str.equals("jdk.xml.overrideDefaultParser") || str.equals("http://www.oracle.com/feature/use-service-mechanism")) {
                this._overrideDefaultParser = this._xmlFeatures.getFeature(JdkXmlFeatures.XmlFeature.JDK_OVERRIDE_PARSER);
            }
        }
    }

    public boolean getFeature(String str) {
        String[] strArr = {"http://ohos.javax.xml.transform.dom.DOMSource/feature", "http://ohos.javax.xml.transform.dom.DOMResult/feature", "http://ohos.javax.xml.transform.sax.SAXSource/feature", "http://ohos.javax.xml.transform.sax.SAXResult/feature", "http://ohos.javax.xml.transform.stax.StAXSource/feature", "http://ohos.javax.xml.transform.stax.StAXResult/feature", "http://ohos.javax.xml.transform.stream.StreamSource/feature", "http://ohos.javax.xml.transform.stream.StreamResult/feature", "http://ohos.javax.xml.transform.sax.SAXTransformerFactory/feature", "http://ohos.javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter", "http://www.oracle.com/feature/use-service-mechanism"};
        if (str != null) {
            for (String str2 : strArr) {
                if (str.equals(str2)) {
                    return true;
                }
            }
            if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
                return !this._isNotSecureProcessing;
            }
            int index = this._xmlFeatures.getIndex(str);
            if (index > -1) {
                return this._xmlFeatures.getFeature(index);
            }
            return false;
        }
        throw new NullPointerException(new ErrorMsg(ErrorMsg.JAXP_GET_FEATURE_NULL_NAME).toString());
    }

    public boolean overrideDefaultParser() {
        return this._overrideDefaultParser;
    }

    public JdkXmlFeatures getJdkXmlFeatures() {
        return this._xmlFeatures;
    }

    public URIResolver getURIResolver() {
        return this._uriResolver;
    }

    public void setURIResolver(URIResolver uRIResolver) {
        this._uriResolver = uRIResolver;
    }

    public Source getAssociatedStylesheet(Source source, String str, String str2, String str3) throws TransformerConfigurationException {
        XMLReader xMLReader = null;
        ContentHandler stylesheetPIHandler = new StylesheetPIHandler(null, str, str2, str3);
        try {
            if (source instanceof DOMSource) {
                DOMSource dOMSource = (DOMSource) source;
                String systemId = dOMSource.getSystemId();
                DOM2SAX dom2sax = new DOM2SAX(dOMSource.getNode());
                stylesheetPIHandler.setBaseId(systemId);
                dom2sax.setContentHandler(stylesheetPIHandler);
                dom2sax.parse();
            } else {
                if (source instanceof SAXSource) {
                    xMLReader = ((SAXSource) source).getXMLReader();
                }
                InputSource sourceToInputSource = SAXSource.sourceToInputSource(source);
                String systemId2 = sourceToInputSource.getSystemId();
                if (xMLReader == null) {
                    xMLReader = JdkXmlUtils.getXMLReader(this._overrideDefaultParser, !this._isNotSecureProcessing);
                }
                stylesheetPIHandler.setBaseId(systemId2);
                xMLReader.setContentHandler(stylesheetPIHandler);
                xMLReader.parse(sourceToInputSource);
            }
            if (this._uriResolver != null) {
                stylesheetPIHandler.setURIResolver(this._uriResolver);
            }
        } catch (StopParseException unused) {
        } catch (SAXException e) {
            throw new TransformerConfigurationException("getAssociatedStylesheets failed", e);
        } catch (IOException e2) {
            throw new TransformerConfigurationException("getAssociatedStylesheets failed", e2);
        }
        return stylesheetPIHandler.getAssociatedStylesheet();
    }

    public Transformer newTransformer() throws TransformerConfigurationException {
        TransformerImpl transformerImpl = new TransformerImpl(new Properties(), this._indentNumber, this);
        URIResolver uRIResolver = this._uriResolver;
        if (uRIResolver != null) {
            transformerImpl.setURIResolver(uRIResolver);
        }
        if (!this._isNotSecureProcessing) {
            transformerImpl.setSecureProcessing(true);
        }
        return transformerImpl;
    }

    public Transformer newTransformer(Source source) throws TransformerConfigurationException {
        Transformer newTransformer = newTemplates(source).newTransformer();
        URIResolver uRIResolver = this._uriResolver;
        if (uRIResolver != null) {
            newTransformer.setURIResolver(uRIResolver);
        }
        return newTransformer;
    }

    private void passWarningsToListener(ArrayList<ErrorMsg> arrayList) throws TransformerException {
        if (!(this._errorListener == null || arrayList == null)) {
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                ErrorMsg errorMsg = arrayList.get(i);
                if (errorMsg.isWarningError()) {
                    this._errorListener.error(new TransformerConfigurationException(errorMsg.toString()));
                } else {
                    this._errorListener.warning(new TransformerConfigurationException(errorMsg.toString()));
                }
            }
        }
    }

    private void passErrorsToListener(ArrayList<ErrorMsg> arrayList) {
        try {
            if (this._errorListener == null) {
                return;
            }
            if (arrayList != null) {
                int size = arrayList.size();
                for (int i = 0; i < size; i++) {
                    this._errorListener.error(new TransformerException(arrayList.get(i).toString()));
                }
            }
        } catch (TransformerException unused) {
        }
    }

    public Templates newTemplates(Source source) throws TransformerConfigurationException {
        ErrorMsg errorMsg;
        TransformerConfigurationException transformerConfigurationException;
        String parent;
        PIParamWrapper pIParamWrapper;
        byte[][] bArr;
        if (this._useClasspath) {
            String transletBaseName = getTransletBaseName(source);
            if (this._packageName != null) {
                transletBaseName = this._packageName + "." + transletBaseName;
            }
            try {
                Class<?> findProviderClass = ObjectFactory.findProviderClass(transletBaseName, true);
                resetTransientAttributes();
                return new TemplatesImpl(new Class[]{findProviderClass}, transletBaseName, (Properties) null, this._indentNumber, this);
            } catch (ClassNotFoundException unused) {
                throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR, transletBaseName).toString());
            } catch (Exception e) {
                throw new TransformerConfigurationException(new ErrorMsg(new ErrorMsg(ErrorMsg.RUNTIME_ERROR_KEY) + e.getMessage()).toString());
            }
        } else {
            if (this._autoTranslet) {
                String transletBaseName2 = getTransletBaseName(source);
                if (this._packageName != null) {
                    transletBaseName2 = this._packageName + "." + transletBaseName2;
                }
                if (this._jarFileName != null) {
                    bArr = getBytecodesFromJar(source, transletBaseName2);
                } else {
                    bArr = getBytecodesFromClasses(source, transletBaseName2);
                }
                if (bArr != null) {
                    if (this._debug) {
                        if (this._jarFileName != null) {
                            System.err.println(new ErrorMsg(ErrorMsg.TRANSFORM_WITH_JAR_STR, transletBaseName2, this._jarFileName));
                        } else {
                            System.err.println(new ErrorMsg(ErrorMsg.TRANSFORM_WITH_TRANSLET_STR, transletBaseName2));
                        }
                    }
                    resetTransientAttributes();
                    return new TemplatesImpl(bArr, transletBaseName2, (Properties) null, this._indentNumber, this);
                }
            }
            XSLTC xsltc = new XSLTC(this._xmlFeatures);
            if (this._debug) {
                xsltc.setDebug(true);
            }
            if (this._enableInlining) {
                xsltc.setTemplateInlining(true);
            } else {
                xsltc.setTemplateInlining(false);
            }
            if (!this._isNotSecureProcessing) {
                xsltc.setSecureProcessing(true);
            }
            xsltc.setProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalStylesheet", this._accessExternalStylesheet);
            xsltc.setProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", this._accessExternalDTD);
            xsltc.setProperty("http://apache.org/xml/properties/security-manager", this._xmlSecurityManager);
            xsltc.setProperty(XalanConstants.JDK_EXTENSION_CLASSLOADER, this._extensionClassLoader);
            xsltc.init();
            if (!this._isNotSecureProcessing) {
                this._xsltcExtensionFunctions = xsltc.getExternalExtensionFunctions();
            }
            if (this._uriResolver != null) {
                xsltc.setSourceLoader(this);
            }
            Map<Source, PIParamWrapper> map = this._piParams;
            if (!(map == null || map.get(source) == null || (pIParamWrapper = this._piParams.get(source)) == null)) {
                xsltc.setPIParameters(pIParamWrapper._media, pIParamWrapper._title, pIParamWrapper._charset);
            }
            int i = 2;
            if (this._generateTranslet || this._autoTranslet) {
                xsltc.setClassName(getTransletBaseName(source));
                String str = this._destinationDirectory;
                if (str != null) {
                    xsltc.setDestDirectory(str);
                } else {
                    String stylesheetFileName = getStylesheetFileName(source);
                    if (!(stylesheetFileName == null || (parent = new File(stylesheetFileName).getParent()) == null)) {
                        xsltc.setDestDirectory(parent);
                    }
                }
                String str2 = this._packageName;
                if (str2 != null) {
                    xsltc.setPackageName(str2);
                }
                String str3 = this._jarFileName;
                if (str3 != null) {
                    xsltc.setJarFileName(str3);
                    i = 5;
                } else {
                    i = 4;
                }
            }
            byte[][] compile = xsltc.compile(null, Util.getInputSource(xsltc, source), i);
            String className = xsltc.getClassName();
            if (!((!this._generateTranslet && !this._autoTranslet) || compile == null || this._jarFileName == null)) {
                try {
                    xsltc.outputToJar();
                } catch (IOException unused2) {
                }
            }
            resetTransientAttributes();
            if (this._errorListener != this) {
                try {
                    passWarningsToListener(xsltc.getWarnings());
                } catch (TransformerException e2) {
                    throw new TransformerConfigurationException(e2);
                }
            } else {
                xsltc.printWarnings();
            }
            if (compile != null) {
                return new TemplatesImpl(compile, className, xsltc.getOutputProperties(), this._indentNumber, this);
            }
            ArrayList<ErrorMsg> errors = xsltc.getErrors();
            if (errors != null) {
                errorMsg = errors.get(errors.size() - 1);
            } else {
                errorMsg = new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR);
            }
            Throwable cause = errorMsg.getCause();
            if (cause != null) {
                transformerConfigurationException = new TransformerConfigurationException(cause.getMessage(), cause);
            } else {
                transformerConfigurationException = new TransformerConfigurationException(errorMsg.toString());
            }
            if (this._errorListener != null) {
                passErrorsToListener(xsltc.getErrors());
                try {
                    this._errorListener.fatalError(transformerConfigurationException);
                } catch (TransformerException unused3) {
                }
            } else {
                xsltc.printErrors();
            }
            throw transformerConfigurationException;
        }
    }

    public TemplatesHandler newTemplatesHandler() throws TransformerConfigurationException {
        TemplatesHandlerImpl templatesHandlerImpl = new TemplatesHandlerImpl(this._indentNumber, this);
        URIResolver uRIResolver = this._uriResolver;
        if (uRIResolver != null) {
            templatesHandlerImpl.setURIResolver(uRIResolver);
        }
        return templatesHandlerImpl;
    }

    public TransformerHandler newTransformerHandler() throws TransformerConfigurationException {
        TransformerImpl newTransformer = newTransformer();
        URIResolver uRIResolver = this._uriResolver;
        if (uRIResolver != null) {
            newTransformer.setURIResolver(uRIResolver);
        }
        return new TransformerHandlerImpl(newTransformer);
    }

    public TransformerHandler newTransformerHandler(Source source) throws TransformerConfigurationException {
        TransformerImpl newTransformer = newTransformer(source);
        URIResolver uRIResolver = this._uriResolver;
        if (uRIResolver != null) {
            newTransformer.setURIResolver(uRIResolver);
        }
        return new TransformerHandlerImpl(newTransformer);
    }

    public TransformerHandler newTransformerHandler(Templates templates) throws TransformerConfigurationException {
        return new TransformerHandlerImpl(templates.newTransformer());
    }

    public XMLFilter newXMLFilter(Source source) throws TransformerConfigurationException {
        Templates newTemplates = newTemplates(source);
        if (newTemplates == null) {
            return null;
        }
        return newXMLFilter(newTemplates);
    }

    public XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException {
        try {
            return new TrAXFilter(templates);
        } catch (TransformerConfigurationException e) {
            ErrorListener errorListener = this._errorListener;
            if (errorListener != null) {
                try {
                    errorListener.fatalError(e);
                    return null;
                } catch (TransformerException e2) {
                    new TransformerConfigurationException(e2);
                    throw e;
                }
            }
            throw e;
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

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader
    public InputSource loadSource(String str, String str2, XSLTC xsltc) {
        Source resolve;
        try {
            if (this._uriResolver == null || (resolve = this._uriResolver.resolve(str, str2)) == null) {
                return null;
            }
            return Util.getInputSource(xsltc, resolve);
        } catch (TransformerException e) {
            xsltc.getParser().reportError(2, new ErrorMsg(ErrorMsg.INVALID_URI_ERR, str + "\n" + e.getMessage(), this));
            return null;
        }
    }

    private void resetTransientAttributes() {
        this._transletName = DEFAULT_TRANSLET_NAME;
        this._destinationDirectory = null;
        this._packageName = null;
        this._jarFileName = null;
    }

    private byte[][] getBytecodesFromClasses(Source source, String str) {
        String str2;
        if (str == null) {
            return null;
        }
        String stylesheetFileName = getStylesheetFileName(source);
        File file = stylesheetFileName != null ? new File(stylesheetFileName) : null;
        int lastIndexOf = str.lastIndexOf(46);
        String substring = lastIndexOf > 0 ? str.substring(lastIndexOf + 1) : str;
        String replace = str.replace('.', '/');
        if (this._destinationDirectory != null) {
            str2 = this._destinationDirectory + PsuedoNames.PSEUDONAME_ROOT + replace + ".class";
        } else if (file == null || file.getParent() == null) {
            str2 = replace + ".class";
        } else {
            str2 = file.getParent() + PsuedoNames.PSEUDONAME_ROOT + replace + ".class";
        }
        File file2 = new File(str2);
        if (!file2.exists()) {
            return null;
        }
        if (file != null && file.exists()) {
            if (file2.lastModified() < file.lastModified()) {
                return null;
            }
        }
        Vector vector = new Vector();
        int length = (int) file2.length();
        if (length > 0) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file2);
                byte[] bArr = new byte[length];
                readFromInputStream(bArr, fileInputStream, length);
                fileInputStream.close();
                vector.addElement(bArr);
                String parent = file2.getParent();
                if (parent == null) {
                    parent = SecuritySupport.getSystemProperty("user.dir");
                }
                File file3 = new File(parent);
                final String str3 = substring + "$";
                File[] listFiles = file3.listFiles(new FilenameFilter() {
                    /* class ohos.com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl.AnonymousClass1 */

                    @Override // java.io.FilenameFilter
                    public boolean accept(File file, String str) {
                        return str.endsWith(".class") && str.startsWith(str3);
                    }
                });
                for (File file4 : listFiles) {
                    int length2 = (int) file4.length();
                    if (length2 > 0) {
                        try {
                            FileInputStream fileInputStream2 = new FileInputStream(file4);
                            byte[] bArr2 = new byte[length2];
                            readFromInputStream(bArr2, fileInputStream2, length2);
                            fileInputStream2.close();
                            vector.addElement(bArr2);
                        } catch (FileNotFoundException unused) {
                        }
                    }
                }
                int size = vector.size();
                if (size > 0) {
                    byte[][] bArr3 = (byte[][]) Array.newInstance(byte.class, size, 1);
                    for (int i = 0; i < size; i++) {
                        bArr3[i] = (byte[]) vector.elementAt(i);
                    }
                    return bArr3;
                }
            } catch (FileNotFoundException unused2) {
            }
        }
        return null;
    }

    private byte[][] getBytecodesFromJar(Source source, String str) {
        String str2;
        String stylesheetFileName = getStylesheetFileName(source);
        File file = stylesheetFileName != null ? new File(stylesheetFileName) : null;
        if (this._destinationDirectory != null) {
            str2 = this._destinationDirectory + PsuedoNames.PSEUDONAME_ROOT + this._jarFileName;
        } else if (file == null || file.getParent() == null) {
            str2 = this._jarFileName;
        } else {
            str2 = file.getParent() + PsuedoNames.PSEUDONAME_ROOT + this._jarFileName;
        }
        File file2 = new File(str2);
        if (!file2.exists()) {
            return null;
        }
        if (file != null && file.exists()) {
            if (file2.lastModified() < file.lastModified()) {
                return null;
            }
        }
        try {
            ZipFile zipFile = new ZipFile(file2);
            String replace = str.replace('.', '/');
            String str3 = replace + "$";
            String str4 = replace + ".class";
            Vector vector = new Vector();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                String name = zipEntry.getName();
                if (zipEntry.getSize() > 0 && (name.equals(str4) || (name.endsWith(".class") && name.startsWith(str3)))) {
                    try {
                        InputStream inputStream = zipFile.getInputStream(zipEntry);
                        int size = (int) zipEntry.getSize();
                        byte[] bArr = new byte[size];
                        readFromInputStream(bArr, inputStream, size);
                        inputStream.close();
                        vector.addElement(bArr);
                    } catch (IOException unused) {
                        return null;
                    }
                }
            }
            int size2 = vector.size();
            if (size2 > 0) {
                byte[][] bArr2 = (byte[][]) Array.newInstance(byte.class, size2, 1);
                for (int i = 0; i < size2; i++) {
                    bArr2[i] = (byte[]) vector.elementAt(i);
                }
                return bArr2;
            }
        } catch (IOException unused2) {
        }
        return null;
    }

    private void readFromInputStream(byte[] bArr, InputStream inputStream, int i) throws IOException {
        int i2 = 0;
        while (i > 0) {
            int read = inputStream.read(bArr, i2, i);
            if (read > 0) {
                i2 += read;
                i -= read;
            } else {
                return;
            }
        }
    }

    private String getTransletBaseName(Source source) {
        String baseName;
        if (!this._transletName.equals(DEFAULT_TRANSLET_NAME)) {
            return this._transletName;
        }
        String systemId = source.getSystemId();
        String javaName = (systemId == null || (baseName = Util.baseName(systemId)) == null) ? null : Util.toJavaName(Util.noExtName(baseName));
        if (javaName != null) {
            return javaName;
        }
        return DEFAULT_TRANSLET_NAME;
    }

    private String getStylesheetFileName(Source source) {
        String systemId = source.getSystemId();
        if (systemId != null) {
            if (new File(systemId).exists()) {
                return systemId;
            }
            try {
                URL url = new URL(systemId);
                if (AsrConstants.ASR_SRC_FILE.equals(url.getProtocol())) {
                    return url.getFile();
                }
            } catch (MalformedURLException unused) {
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public final XSLTCDTMManager createNewDTMManagerInstance() {
        return XSLTCDTMManager.createNewDTMManagerInstance();
    }
}
