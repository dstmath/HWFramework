package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.util.EntityResolver2Wrapper;
import ohos.com.sun.org.apache.xerces.internal.util.EntityResolverWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.Status;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.ext.EntityResolver2;
import ohos.org.xml.sax.helpers.LocatorImpl;

public class DOMParser extends AbstractDOMParser {
    private static final String[] RECOGNIZED_FEATURES = {REPORT_WHITESPACE};
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/grammar-pool"};
    protected static final String REPORT_WHITESPACE = "http://java.sun.com/xml/schema/features/report-ignored-element-content-whitespace";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String USE_ENTITY_RESOLVER2 = "http://xml.org/sax/features/use-entity-resolver2";
    protected static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    protected boolean fUseEntityResolver2;

    public DOMParser(XMLParserConfiguration xMLParserConfiguration) {
        super(xMLParserConfiguration);
        this.fUseEntityResolver2 = true;
    }

    public DOMParser() {
        this(null, null);
    }

    public DOMParser(SymbolTable symbolTable) {
        this(symbolTable, null);
    }

    public DOMParser(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        super(new XIncludeAwareParserConfiguration());
        this.fUseEntityResolver2 = true;
        this.fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        if (symbolTable != null) {
            this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
        }
        if (xMLGrammarPool != null) {
            this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/grammar-pool", xMLGrammarPool);
        }
        this.fConfiguration.addRecognizedFeatures(RECOGNIZED_FEATURES);
    }

    public void parse(String str) throws SAXException, IOException {
        try {
            parse(new XMLInputSource(null, str, null));
        } catch (XMLParseException e) {
            SAXException exception = e.getException();
            if (exception == null) {
                LocatorImpl locatorImpl = new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw new SAXParseException(e.getMessage(), locatorImpl);
            } else if (exception instanceof SAXException) {
                throw exception;
            } else if (exception instanceof IOException) {
                throw ((IOException) exception);
            } else {
                throw new SAXException(exception);
            }
        } catch (XNIException e2) {
            e2.printStackTrace();
            SAXException exception2 = e2.getException();
            if (exception2 == null) {
                throw new SAXException(e2.getMessage());
            } else if (exception2 instanceof SAXException) {
                throw exception2;
            } else if (exception2 instanceof IOException) {
                throw ((IOException) exception2);
            } else {
                throw new SAXException(exception2);
            }
        }
    }

    public void parse(InputSource inputSource) throws SAXException, IOException {
        try {
            XMLInputSource xMLInputSource = new XMLInputSource(inputSource.getPublicId(), inputSource.getSystemId(), null);
            xMLInputSource.setByteStream(inputSource.getByteStream());
            xMLInputSource.setCharacterStream(inputSource.getCharacterStream());
            xMLInputSource.setEncoding(inputSource.getEncoding());
            parse(xMLInputSource);
        } catch (XMLParseException e) {
            SAXException exception = e.getException();
            if (exception == null) {
                LocatorImpl locatorImpl = new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw new SAXParseException(e.getMessage(), locatorImpl);
            } else if (exception instanceof SAXException) {
                throw exception;
            } else if (exception instanceof IOException) {
                throw ((IOException) exception);
            } else {
                throw new SAXException(exception);
            }
        } catch (XNIException e2) {
            SAXException exception2 = e2.getException();
            if (exception2 == null) {
                throw new SAXException(e2.getMessage());
            } else if (exception2 instanceof SAXException) {
                throw exception2;
            } else if (exception2 instanceof IOException) {
                throw ((IOException) exception2);
            } else {
                throw new SAXException(exception2);
            }
        }
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        try {
            XMLEntityResolver xMLEntityResolver = (XMLEntityResolver) this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/entity-resolver");
            if (!this.fUseEntityResolver2 || !(entityResolver instanceof EntityResolver2)) {
                if (xMLEntityResolver instanceof EntityResolverWrapper) {
                    ((EntityResolverWrapper) xMLEntityResolver).setEntityResolver(entityResolver);
                } else {
                    this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/entity-resolver", new EntityResolverWrapper(entityResolver));
                }
            } else if (xMLEntityResolver instanceof EntityResolver2Wrapper) {
                ((EntityResolver2Wrapper) xMLEntityResolver).setEntityResolver((EntityResolver2) entityResolver);
            } else {
                this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/entity-resolver", new EntityResolver2Wrapper((EntityResolver2) entityResolver));
            }
        } catch (XMLConfigurationException unused) {
        }
    }

    public EntityResolver getEntityResolver() {
        EntityResolver entityResolver;
        try {
            XMLEntityResolver xMLEntityResolver = (XMLEntityResolver) this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/entity-resolver");
            if (xMLEntityResolver == null) {
                return null;
            }
            if (xMLEntityResolver instanceof EntityResolverWrapper) {
                entityResolver = ((EntityResolverWrapper) xMLEntityResolver).getEntityResolver();
            } else if (!(xMLEntityResolver instanceof EntityResolver2Wrapper)) {
                return null;
            } else {
                entityResolver = ((EntityResolver2Wrapper) xMLEntityResolver).getEntityResolver();
            }
            return entityResolver;
        } catch (XMLConfigurationException unused) {
            return null;
        }
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        try {
            XMLErrorHandler xMLErrorHandler = (XMLErrorHandler) this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/error-handler");
            if (xMLErrorHandler instanceof ErrorHandlerWrapper) {
                ((ErrorHandlerWrapper) xMLErrorHandler).setErrorHandler(errorHandler);
            } else {
                this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/error-handler", new ErrorHandlerWrapper(errorHandler));
            }
        } catch (XMLConfigurationException unused) {
        }
    }

    public ErrorHandler getErrorHandler() {
        try {
            XMLErrorHandler xMLErrorHandler = (XMLErrorHandler) this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/error-handler");
            if (xMLErrorHandler == null || !(xMLErrorHandler instanceof ErrorHandlerWrapper)) {
                return null;
            }
            return ((ErrorHandlerWrapper) xMLErrorHandler).getErrorHandler();
        } catch (XMLConfigurationException unused) {
            return null;
        }
    }

    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
        try {
            if (!str.equals(USE_ENTITY_RESOLVER2)) {
                this.fConfiguration.setFeature(str, z);
            } else if (z != this.fUseEntityResolver2) {
                this.fUseEntityResolver2 = z;
                setEntityResolver(getEntityResolver());
            }
        } catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == Status.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-recognized", new Object[]{identifier}));
            }
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-supported", new Object[]{identifier}));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XMLParser
    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        try {
            if (str.equals(USE_ENTITY_RESOLVER2)) {
                return this.fUseEntityResolver2;
            }
            return this.fConfiguration.getFeature(str);
        } catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == Status.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-recognized", new Object[]{identifier}));
            }
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-supported", new Object[]{identifier}));
        }
    }

    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str.equals("http://apache.org/xml/properties/security-manager")) {
            this.securityManager = XMLSecurityManager.convert(obj, this.securityManager);
            setProperty0("http://apache.org/xml/properties/security-manager", this.securityManager);
        } else if (str.equals("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")) {
            if (obj == null) {
                this.securityPropertyManager = new XMLSecurityPropertyManager();
            } else {
                this.securityPropertyManager = (XMLSecurityPropertyManager) obj;
            }
            setProperty0("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.securityPropertyManager);
        } else {
            if (this.securityManager == null) {
                this.securityManager = new XMLSecurityManager(true);
                setProperty0("http://apache.org/xml/properties/security-manager", this.securityManager);
            }
            if (this.securityPropertyManager == null) {
                this.securityPropertyManager = new XMLSecurityPropertyManager();
                setProperty0("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.securityPropertyManager);
            }
            int index = this.securityPropertyManager.getIndex(str);
            if (index > -1) {
                this.securityPropertyManager.setValue(index, XMLSecurityPropertyManager.State.APIPROPERTY, (String) obj);
            } else if (!this.securityManager.setLimit(str, XMLSecurityManager.State.APIPROPERTY, obj)) {
                setProperty0(str, obj);
            }
        }
    }

    public void setProperty0(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        try {
            this.fConfiguration.setProperty(str, obj);
        } catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == Status.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-recognized", new Object[]{identifier}));
            }
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-supported", new Object[]{identifier}));
        }
    }

    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        boolean z = false;
        if (str.equals("http://apache.org/xml/properties/dom/current-element-node")) {
            try {
                z = getFeature("http://apache.org/xml/features/dom/defer-node-expansion");
            } catch (XMLConfigurationException unused) {
            }
            if (z) {
                throw new SAXNotSupportedException("Current element node cannot be queried when node expansion is deferred.");
            } else if (this.fCurrentNode == null || this.fCurrentNode.getNodeType() != 1) {
                return null;
            } else {
                return this.fCurrentNode;
            }
        } else {
            try {
                XMLSecurityPropertyManager xMLSecurityPropertyManager = (XMLSecurityPropertyManager) this.fConfiguration.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager");
                int index = xMLSecurityPropertyManager.getIndex(str);
                if (index > -1) {
                    return xMLSecurityPropertyManager.getValueByIndex(index);
                }
                return this.fConfiguration.getProperty(str);
            } catch (XMLConfigurationException e) {
                String identifier = e.getIdentifier();
                if (e.getType() == Status.NOT_RECOGNIZED) {
                    throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-recognized", new Object[]{identifier}));
                }
                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-supported", new Object[]{identifier}));
            }
        }
    }

    public XMLParserConfiguration getXMLParserConfiguration() {
        return this.fConfiguration;
    }
}
