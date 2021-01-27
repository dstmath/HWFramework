package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler;
import ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeNamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public class XIncludeParserConfiguration extends XML11Configuration {
    protected static final String ALLOW_UE_AND_NOTATION_EVENTS = "http://xml.org/sax/features/allow-dtd-events-after-endDTD";
    protected static final String NAMESPACE_CONTEXT = "http://apache.org/xml/properties/internal/namespace-context";
    protected static final String XINCLUDE_FIXUP_BASE_URIS = "http://apache.org/xml/features/xinclude/fixup-base-uris";
    protected static final String XINCLUDE_FIXUP_LANGUAGE = "http://apache.org/xml/features/xinclude/fixup-language";
    protected static final String XINCLUDE_HANDLER = "http://apache.org/xml/properties/internal/xinclude-handler";
    private XIncludeHandler fXIncludeHandler;

    public XIncludeParserConfiguration() {
        this(null, null, null);
    }

    public XIncludeParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public XIncludeParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public XIncludeParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(symbolTable, xMLGrammarPool, xMLComponentManager);
        this.fXIncludeHandler = new XIncludeHandler();
        addCommonComponent(this.fXIncludeHandler);
        addRecognizedFeatures(new String[]{ALLOW_UE_AND_NOTATION_EVENTS, XINCLUDE_FIXUP_BASE_URIS, XINCLUDE_FIXUP_LANGUAGE});
        addRecognizedProperties(new String[]{XINCLUDE_HANDLER, NAMESPACE_CONTEXT});
        setFeature(ALLOW_UE_AND_NOTATION_EVENTS, true);
        setFeature(XINCLUDE_FIXUP_BASE_URIS, true);
        setFeature(XINCLUDE_FIXUP_LANGUAGE, true);
        setProperty(XINCLUDE_HANDLER, this.fXIncludeHandler);
        setProperty(NAMESPACE_CONTEXT, new XIncludeNamespaceSupport());
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration
    public void configurePipeline() {
        XMLDocumentSource xMLDocumentSource;
        super.configurePipeline();
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
        if (xMLDocumentSource != null) {
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
        setDocumentHandler(this.fXIncludeHandler);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration
    public void configureXML11Pipeline() {
        XMLDocumentSource xMLDocumentSource;
        super.configureXML11Pipeline();
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
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        str.equals(XINCLUDE_HANDLER);
        super.setProperty(str, obj);
    }
}
