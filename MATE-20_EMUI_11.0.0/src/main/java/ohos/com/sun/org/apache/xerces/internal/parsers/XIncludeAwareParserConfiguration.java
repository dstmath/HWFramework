package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.util.FeatureState;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler;
import ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeNamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public class XIncludeAwareParserConfiguration extends XML11Configuration {
    protected static final String ALLOW_UE_AND_NOTATION_EVENTS = "http://xml.org/sax/features/allow-dtd-events-after-endDTD";
    protected static final String NAMESPACE_CONTEXT = "http://apache.org/xml/properties/internal/namespace-context";
    protected static final String XINCLUDE_FEATURE = "http://apache.org/xml/features/xinclude";
    protected static final String XINCLUDE_FIXUP_BASE_URIS = "http://apache.org/xml/features/xinclude/fixup-base-uris";
    protected static final String XINCLUDE_FIXUP_LANGUAGE = "http://apache.org/xml/features/xinclude/fixup-language";
    protected static final String XINCLUDE_HANDLER = "http://apache.org/xml/properties/internal/xinclude-handler";
    protected NamespaceContext fCurrentNSContext;
    protected NamespaceSupport fNonXIncludeNSContext;
    protected boolean fXIncludeEnabled;
    protected XIncludeHandler fXIncludeHandler;
    protected XIncludeNamespaceSupport fXIncludeNSContext;

    public XIncludeAwareParserConfiguration() {
        this(null, null, null);
    }

    public XIncludeAwareParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public XIncludeAwareParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public XIncludeAwareParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(symbolTable, xMLGrammarPool, xMLComponentManager);
        this.fXIncludeEnabled = false;
        addRecognizedFeatures(new String[]{ALLOW_UE_AND_NOTATION_EVENTS, XINCLUDE_FIXUP_BASE_URIS, XINCLUDE_FIXUP_LANGUAGE});
        addRecognizedProperties(new String[]{XINCLUDE_HANDLER, NAMESPACE_CONTEXT});
        setFeature(ALLOW_UE_AND_NOTATION_EVENTS, true);
        setFeature(XINCLUDE_FIXUP_BASE_URIS, true);
        setFeature(XINCLUDE_FIXUP_LANGUAGE, true);
        this.fNonXIncludeNSContext = new NamespaceSupport();
        NamespaceSupport namespaceSupport = this.fNonXIncludeNSContext;
        this.fCurrentNSContext = namespaceSupport;
        setProperty(NAMESPACE_CONTEXT, namespaceSupport);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration
    public void configurePipeline() {
        XMLDocumentSource xMLDocumentSource;
        super.configurePipeline();
        if (this.fXIncludeEnabled) {
            if (this.fXIncludeHandler == null) {
                this.fXIncludeHandler = new XIncludeHandler();
                setProperty(XINCLUDE_HANDLER, this.fXIncludeHandler);
                addCommonComponent(this.fXIncludeHandler);
                this.fXIncludeHandler.reset(this);
            }
            NamespaceContext namespaceContext = this.fCurrentNSContext;
            XIncludeNamespaceSupport xIncludeNamespaceSupport = this.fXIncludeNSContext;
            if (namespaceContext != xIncludeNamespaceSupport) {
                if (xIncludeNamespaceSupport == null) {
                    this.fXIncludeNSContext = new XIncludeNamespaceSupport();
                }
                XIncludeNamespaceSupport xIncludeNamespaceSupport2 = this.fXIncludeNSContext;
                this.fCurrentNSContext = xIncludeNamespaceSupport2;
                setProperty(NAMESPACE_CONTEXT, xIncludeNamespaceSupport2);
            }
            this.fDTDScanner.setDTDHandler(this.fDTDProcessor);
            this.fDTDProcessor.setDTDSource(this.fDTDScanner);
            this.fDTDProcessor.setDTDHandler(this.fXIncludeHandler);
            this.fXIncludeHandler.setDTDSource(this.fDTDProcessor);
            this.fXIncludeHandler.setDTDHandler(this.fDTDHandler);
            if (this.fDTDHandler != null) {
                this.fDTDHandler.setDTDSource(this.fXIncludeHandler);
            }
            if (this.fFeatures.get("http://apache.org/xml/features/validation/schema") == Boolean.TRUE) {
                xMLDocumentSource = this.fSchemaValidator.getDocumentSource();
            } else {
                xMLDocumentSource = this.fLastComponent;
                this.fLastComponent = this.fXIncludeHandler;
            }
            XMLDocumentHandler documentHandler = xMLDocumentSource.getDocumentHandler();
            xMLDocumentSource.setDocumentHandler(this.fXIncludeHandler);
            this.fXIncludeHandler.setDocumentSource(xMLDocumentSource);
            if (documentHandler != null) {
                this.fXIncludeHandler.setDocumentHandler(documentHandler);
                documentHandler.setDocumentSource(this.fXIncludeHandler);
                return;
            }
            return;
        }
        NamespaceContext namespaceContext2 = this.fCurrentNSContext;
        NamespaceSupport namespaceSupport = this.fNonXIncludeNSContext;
        if (namespaceContext2 != namespaceSupport) {
            this.fCurrentNSContext = namespaceSupport;
            setProperty(NAMESPACE_CONTEXT, namespaceSupport);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration
    public void configureXML11Pipeline() {
        XMLDocumentSource xMLDocumentSource;
        super.configureXML11Pipeline();
        if (this.fXIncludeEnabled) {
            if (this.fXIncludeHandler == null) {
                this.fXIncludeHandler = new XIncludeHandler();
                setProperty(XINCLUDE_HANDLER, this.fXIncludeHandler);
                addCommonComponent(this.fXIncludeHandler);
                this.fXIncludeHandler.reset(this);
            }
            NamespaceContext namespaceContext = this.fCurrentNSContext;
            XIncludeNamespaceSupport xIncludeNamespaceSupport = this.fXIncludeNSContext;
            if (namespaceContext != xIncludeNamespaceSupport) {
                if (xIncludeNamespaceSupport == null) {
                    this.fXIncludeNSContext = new XIncludeNamespaceSupport();
                }
                XIncludeNamespaceSupport xIncludeNamespaceSupport2 = this.fXIncludeNSContext;
                this.fCurrentNSContext = xIncludeNamespaceSupport2;
                setProperty(NAMESPACE_CONTEXT, xIncludeNamespaceSupport2);
            }
            this.fXML11DTDScanner.setDTDHandler(this.fXML11DTDProcessor);
            this.fXML11DTDProcessor.setDTDSource(this.fXML11DTDScanner);
            this.fXML11DTDProcessor.setDTDHandler(this.fXIncludeHandler);
            this.fXIncludeHandler.setDTDSource(this.fXML11DTDProcessor);
            this.fXIncludeHandler.setDTDHandler(this.fDTDHandler);
            if (this.fDTDHandler != null) {
                this.fDTDHandler.setDTDSource(this.fXIncludeHandler);
            }
            if (this.fFeatures.get("http://apache.org/xml/features/validation/schema") == Boolean.TRUE) {
                xMLDocumentSource = this.fSchemaValidator.getDocumentSource();
            } else {
                xMLDocumentSource = this.fLastComponent;
                this.fLastComponent = this.fXIncludeHandler;
            }
            XMLDocumentHandler documentHandler = xMLDocumentSource.getDocumentHandler();
            xMLDocumentSource.setDocumentHandler(this.fXIncludeHandler);
            this.fXIncludeHandler.setDocumentSource(xMLDocumentSource);
            if (documentHandler != null) {
                this.fXIncludeHandler.setDocumentHandler(documentHandler);
                documentHandler.setDocumentSource(this.fXIncludeHandler);
                return;
            }
            return;
        }
        NamespaceContext namespaceContext2 = this.fCurrentNSContext;
        NamespaceSupport namespaceSupport = this.fNonXIncludeNSContext;
        if (namespaceContext2 != namespaceSupport) {
            this.fCurrentNSContext = namespaceSupport;
            setProperty(NAMESPACE_CONTEXT, namespaceSupport);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public FeatureState getFeatureState(String str) throws XMLConfigurationException {
        if (str.equals("http://apache.org/xml/features/internal/parser-settings")) {
            return FeatureState.is(this.fConfigUpdated);
        }
        if (str.equals(XINCLUDE_FEATURE)) {
            return FeatureState.is(this.fXIncludeEnabled);
        }
        return super.getFeatureState0(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        if (str.equals(XINCLUDE_FEATURE)) {
            this.fXIncludeEnabled = z;
            this.fConfigUpdated = true;
            return;
        }
        super.setFeature(str, z);
    }
}
