package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import ohos.com.sun.org.apache.xerces.internal.util.DOMEntityResolverWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.DOMInputSource;
import ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.SAXInputSource;
import ohos.com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.StAXInputSource;
import ohos.com.sun.org.apache.xerces.internal.util.Status;
import ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.transform.stax.StAXSource;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.javax.xml.validation.Schema;
import ohos.javax.xml.validation.SchemaFactory;
import ohos.jdk.xml.internal.JdkXmlFeatures;
import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.SAXParseException;

public final class XMLSchemaFactory extends SchemaFactory {
    private static final String SCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    private static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    private final DOMEntityResolverWrapper fDOMEntityResolverWrapper = new DOMEntityResolverWrapper();
    private ErrorHandler fErrorHandler;
    private ErrorHandlerWrapper fErrorHandlerWrapper = new ErrorHandlerWrapper(DraconianErrorHandler.getInstance());
    private LSResourceResolver fLSResourceResolver;
    private final boolean fOverrideDefaultParser;
    private XMLSecurityManager fSecurityManager;
    private XMLSecurityPropertyManager fSecurityPropertyMgr;
    private XMLGrammarPoolWrapper fXMLGrammarPoolWrapper = new XMLGrammarPoolWrapper();
    private final XMLSchemaLoader fXMLSchemaLoader = new XMLSchemaLoader();
    private final JdkXmlFeatures fXmlFeatures;

    public XMLSchemaFactory() {
        this.fXMLSchemaLoader.setFeature(SCHEMA_FULL_CHECKING, true);
        this.fXMLSchemaLoader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", this.fXMLGrammarPoolWrapper);
        this.fXMLSchemaLoader.setEntityResolver(this.fDOMEntityResolverWrapper);
        this.fXMLSchemaLoader.setErrorHandler(this.fErrorHandlerWrapper);
        this.fSecurityManager = new XMLSecurityManager(true);
        this.fXMLSchemaLoader.setProperty("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
        this.fSecurityPropertyMgr = new XMLSecurityPropertyManager();
        this.fXMLSchemaLoader.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
        this.fXmlFeatures = new JdkXmlFeatures(this.fSecurityManager.isSecureProcessing());
        this.fOverrideDefaultParser = this.fXmlFeatures.getFeature(JdkXmlFeatures.XmlFeature.JDK_OVERRIDE_PARSER);
        this.fXMLSchemaLoader.setFeature("jdk.xml.overrideDefaultParser", this.fOverrideDefaultParser);
    }

    public boolean isSchemaLanguageSupported(String str) {
        if (str == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "SchemaLanguageNull", null));
        } else if (str.length() != 0) {
            return str.equals("http://www.w3.org/2001/XMLSchema");
        } else {
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "SchemaLanguageLengthZero", null));
        }
    }

    public LSResourceResolver getResourceResolver() {
        return this.fLSResourceResolver;
    }

    public void setResourceResolver(LSResourceResolver lSResourceResolver) {
        this.fLSResourceResolver = lSResourceResolver;
        this.fDOMEntityResolverWrapper.setEntityResolver(lSResourceResolver);
        this.fXMLSchemaLoader.setEntityResolver(this.fDOMEntityResolverWrapper);
    }

    public ErrorHandler getErrorHandler() {
        return this.fErrorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.fErrorHandler = errorHandler;
        ErrorHandlerWrapper errorHandlerWrapper = this.fErrorHandlerWrapper;
        if (errorHandler == null) {
            errorHandler = DraconianErrorHandler.getInstance();
        }
        errorHandlerWrapper.setErrorHandler(errorHandler);
        this.fXMLSchemaLoader.setErrorHandler(this.fErrorHandlerWrapper);
    }

    public Schema newSchema(Source[] sourceArr) throws SAXException {
        AbstractXMLSchema abstractXMLSchema;
        XMLGrammarPoolImplExtension xMLGrammarPoolImplExtension = new XMLGrammarPoolImplExtension();
        this.fXMLGrammarPoolWrapper.setGrammarPool(xMLGrammarPoolImplExtension);
        XMLInputSource[] xMLInputSourceArr = new XMLInputSource[sourceArr.length];
        for (int i = 0; i < sourceArr.length; i++) {
            Source source = sourceArr[i];
            if (source instanceof StreamSource) {
                StreamSource streamSource = (StreamSource) source;
                String publicId = streamSource.getPublicId();
                String systemId = streamSource.getSystemId();
                InputStream inputStream = streamSource.getInputStream();
                Reader reader = streamSource.getReader();
                xMLInputSourceArr[i] = new XMLInputSource(publicId, systemId, null);
                xMLInputSourceArr[i].setByteStream(inputStream);
                xMLInputSourceArr[i].setCharacterStream(reader);
            } else if (source instanceof SAXSource) {
                SAXSource sAXSource = (SAXSource) source;
                InputSource inputSource = sAXSource.getInputSource();
                if (inputSource != null) {
                    xMLInputSourceArr[i] = new SAXInputSource(sAXSource.getXMLReader(), inputSource);
                } else {
                    throw new SAXException(JAXPValidationMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "SAXSourceNullInputSource", null));
                }
            } else if (source instanceof DOMSource) {
                DOMSource dOMSource = (DOMSource) source;
                xMLInputSourceArr[i] = new DOMInputSource(dOMSource.getNode(), dOMSource.getSystemId());
            } else if (source instanceof StAXSource) {
                StAXSource stAXSource = (StAXSource) source;
                XMLEventReader xMLEventReader = stAXSource.getXMLEventReader();
                if (xMLEventReader != null) {
                    xMLInputSourceArr[i] = new StAXInputSource(xMLEventReader);
                } else {
                    xMLInputSourceArr[i] = new StAXInputSource(stAXSource.getXMLStreamReader());
                }
            } else if (source == null) {
                throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "SchemaSourceArrayMemberNull", null));
            } else {
                throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "SchemaFactorySourceUnrecognized", new Object[]{source.getClass().getName()}));
            }
        }
        try {
            this.fXMLSchemaLoader.loadGrammar(xMLInputSourceArr);
            this.fXMLGrammarPoolWrapper.setGrammarPool(null);
            int grammarCount = xMLGrammarPoolImplExtension.getGrammarCount();
            if (grammarCount > 1) {
                abstractXMLSchema = new XMLSchema(new ReadOnlyGrammarPool(xMLGrammarPoolImplExtension));
            } else if (grammarCount == 1) {
                abstractXMLSchema = new SimpleXMLSchema(xMLGrammarPoolImplExtension.retrieveInitialGrammarSet("http://www.w3.org/2001/XMLSchema")[0]);
            } else {
                abstractXMLSchema = new EmptyXMLSchema();
            }
            propagateFeatures(abstractXMLSchema);
            propagateProperties(abstractXMLSchema);
            return abstractXMLSchema;
        } catch (XNIException e) {
            throw Util.toSAXException(e);
        } catch (IOException e2) {
            SAXParseException sAXParseException = new SAXParseException(e2.getMessage(), (Locator) null, e2);
            this.fErrorHandler.error(sAXParseException);
            throw sAXParseException;
        }
    }

    public Schema newSchema() throws SAXException {
        WeakReferenceXMLSchema weakReferenceXMLSchema = new WeakReferenceXMLSchema();
        propagateFeatures(weakReferenceXMLSchema);
        propagateProperties(weakReferenceXMLSchema);
        return weakReferenceXMLSchema;
    }

    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "FeatureNameNull", null));
        } else if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
            XMLSecurityManager xMLSecurityManager = this.fSecurityManager;
            return xMLSecurityManager != null && xMLSecurityManager.isSecureProcessing();
        } else {
            try {
                return this.fXMLSchemaLoader.getFeature(str);
            } catch (XMLConfigurationException e) {
                String identifier = e.getIdentifier();
                if (e.getType() == Status.NOT_RECOGNIZED) {
                    throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "feature-not-recognized", new Object[]{identifier}));
                }
                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "feature-not-supported", new Object[]{identifier}));
            }
        }
    }

    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "ProperyNameNull", null));
        } else if (str.equals("http://apache.org/xml/properties/security-manager")) {
            return this.fSecurityManager;
        } else {
            if (!str.equals("http://apache.org/xml/properties/internal/grammar-pool")) {
                int index = this.fXmlFeatures.getIndex(str);
                if (index > -1) {
                    return Boolean.valueOf(this.fXmlFeatures.getFeature(index));
                }
                try {
                    return this.fXMLSchemaLoader.getProperty(str);
                } catch (XMLConfigurationException e) {
                    String identifier = e.getIdentifier();
                    if (e.getType() == Status.NOT_RECOGNIZED) {
                        throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "property-not-recognized", new Object[]{identifier}));
                    }
                    throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "property-not-supported", new Object[]{identifier}));
                }
            } else {
                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "property-not-supported", new Object[]{str}));
            }
        }
    }

    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "FeatureNameNull", null));
        } else if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
            if (System.getSecurityManager() == null || z) {
                this.fSecurityManager.setSecureProcessing(z);
                if (z) {
                    this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD, XMLSecurityPropertyManager.State.FSP, "");
                    this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA, XMLSecurityPropertyManager.State.FSP, "");
                }
                this.fXMLSchemaLoader.setProperty("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
                return;
            }
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(null, "jaxp-secureprocessing-feature", null));
        } else if (!str.equals("http://www.oracle.com/feature/use-service-mechanism") || System.getSecurityManager() == null) {
            JdkXmlFeatures jdkXmlFeatures = this.fXmlFeatures;
            if (jdkXmlFeatures == null || !jdkXmlFeatures.setFeature(str, JdkXmlFeatures.State.APIPROPERTY, Boolean.valueOf(z))) {
                try {
                    this.fXMLSchemaLoader.setFeature(str, z);
                } catch (XMLConfigurationException e) {
                    String identifier = e.getIdentifier();
                    if (e.getType() == Status.NOT_RECOGNIZED) {
                        throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "feature-not-recognized", new Object[]{identifier}));
                    }
                    throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "feature-not-supported", new Object[]{identifier}));
                }
            } else if (str.equals("jdk.xml.overrideDefaultParser") || str.equals("http://www.oracle.com/feature/use-service-mechanism")) {
                this.fXMLSchemaLoader.setFeature(str, z);
            }
        }
    }

    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "ProperyNameNull", null));
        } else if (str.equals("http://apache.org/xml/properties/security-manager")) {
            this.fSecurityManager = XMLSecurityManager.convert(obj, this.fSecurityManager);
            this.fXMLSchemaLoader.setProperty("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
        } else if (str.equals("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")) {
            if (obj == null) {
                this.fSecurityPropertyMgr = new XMLSecurityPropertyManager();
            } else {
                this.fSecurityPropertyMgr = (XMLSecurityPropertyManager) obj;
            }
            this.fXMLSchemaLoader.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
        } else if (!str.equals("http://apache.org/xml/properties/internal/grammar-pool")) {
            try {
                if (this.fSecurityManager != null && this.fSecurityManager.setLimit(str, XMLSecurityManager.State.APIPROPERTY, obj)) {
                    return;
                }
                if (this.fSecurityPropertyMgr == null || !this.fSecurityPropertyMgr.setValue(str, XMLSecurityPropertyManager.State.APIPROPERTY, obj)) {
                    this.fXMLSchemaLoader.setProperty(str, obj);
                }
            } catch (XMLConfigurationException e) {
                String identifier = e.getIdentifier();
                if (e.getType() == Status.NOT_RECOGNIZED) {
                    throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "property-not-recognized", new Object[]{identifier}));
                }
                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "property-not-supported", new Object[]{identifier}));
            }
        } else {
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fXMLSchemaLoader.getLocale(), "property-not-supported", new Object[]{str}));
        }
    }

    private void propagateFeatures(AbstractXMLSchema abstractXMLSchema) {
        XMLSecurityManager xMLSecurityManager = this.fSecurityManager;
        abstractXMLSchema.setFeature(Constants.FEATURE_SECURE_PROCESSING, xMLSecurityManager != null && xMLSecurityManager.isSecureProcessing());
        abstractXMLSchema.setFeature("jdk.xml.overrideDefaultParser", this.fOverrideDefaultParser);
        String[] recognizedFeatures = this.fXMLSchemaLoader.getRecognizedFeatures();
        for (int i = 0; i < recognizedFeatures.length; i++) {
            abstractXMLSchema.setFeature(recognizedFeatures[i], this.fXMLSchemaLoader.getFeature(recognizedFeatures[i]));
        }
    }

    private void propagateProperties(AbstractXMLSchema abstractXMLSchema) {
        String[] recognizedProperties = this.fXMLSchemaLoader.getRecognizedProperties();
        for (int i = 0; i < recognizedProperties.length; i++) {
            abstractXMLSchema.setProperty(recognizedProperties[i], this.fXMLSchemaLoader.getProperty(recognizedProperties[i]));
        }
    }

    static class XMLGrammarPoolImplExtension extends XMLGrammarPoolImpl {
        public XMLGrammarPoolImplExtension() {
        }

        public XMLGrammarPoolImplExtension(int i) {
            super(i);
        }

        /* access modifiers changed from: package-private */
        public int getGrammarCount() {
            return this.fGrammarCount;
        }
    }

    static class XMLGrammarPoolWrapper implements XMLGrammarPool {
        private XMLGrammarPool fGrammarPool;

        XMLGrammarPoolWrapper() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar[] retrieveInitialGrammarSet(String str) {
            return this.fGrammarPool.retrieveInitialGrammarSet(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void cacheGrammars(String str, Grammar[] grammarArr) {
            this.fGrammarPool.cacheGrammars(str, grammarArr);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription) {
            return this.fGrammarPool.retrieveGrammar(xMLGrammarDescription);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void lockPool() {
            this.fGrammarPool.lockPool();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void unlockPool() {
            this.fGrammarPool.unlockPool();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void clear() {
            this.fGrammarPool.clear();
        }

        /* access modifiers changed from: package-private */
        public void setGrammarPool(XMLGrammarPool xMLGrammarPool) {
            this.fGrammarPool = xMLGrammarPool;
        }

        /* access modifiers changed from: package-private */
        public XMLGrammarPool getGrammarPool() {
            return this.fGrammarPool;
        }
    }
}
