package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler;
import ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeNamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerHandler;

public class XPointerParserConfiguration extends XML11Configuration {
    protected static final String ALLOW_UE_AND_NOTATION_EVENTS = "http://xml.org/sax/features/allow-dtd-events-after-endDTD";
    protected static final String NAMESPACE_CONTEXT = "http://apache.org/xml/properties/internal/namespace-context";
    protected static final String XINCLUDE_FIXUP_BASE_URIS = "http://apache.org/xml/features/xinclude/fixup-base-uris";
    protected static final String XINCLUDE_FIXUP_LANGUAGE = "http://apache.org/xml/features/xinclude/fixup-language";
    protected static final String XINCLUDE_HANDLER = "http://apache.org/xml/properties/internal/xinclude-handler";
    protected static final String XPOINTER_HANDLER = "http://apache.org/xml/properties/internal/xpointer-handler";
    private XIncludeHandler fXIncludeHandler;
    private XPointerHandler fXPointerHandler;

    public XPointerParserConfiguration() {
        this(null, null, null);
    }

    public XPointerParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public XPointerParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public XPointerParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(symbolTable, xMLGrammarPool, xMLComponentManager);
        this.fXIncludeHandler = new XIncludeHandler();
        addCommonComponent(this.fXIncludeHandler);
        this.fXPointerHandler = new XPointerHandler();
        addCommonComponent(this.fXPointerHandler);
        addRecognizedFeatures(new String[]{ALLOW_UE_AND_NOTATION_EVENTS, XINCLUDE_FIXUP_BASE_URIS, XINCLUDE_FIXUP_LANGUAGE});
        addRecognizedProperties(new String[]{XINCLUDE_HANDLER, XPOINTER_HANDLER, NAMESPACE_CONTEXT});
        setFeature(ALLOW_UE_AND_NOTATION_EVENTS, true);
        setFeature(XINCLUDE_FIXUP_BASE_URIS, true);
        setFeature(XINCLUDE_FIXUP_LANGUAGE, true);
        setProperty(XINCLUDE_HANDLER, this.fXIncludeHandler);
        setProperty(XPOINTER_HANDLER, this.fXPointerHandler);
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
        this.fXIncludeHandler.setDTDHandler(this.fXPointerHandler);
        this.fXPointerHandler.setDTDSource(this.fXIncludeHandler);
        this.fXPointerHandler.setDTDHandler(this.fDTDHandler);
        if (this.fDTDHandler != null) {
            this.fDTDHandler.setDTDSource(this.fXPointerHandler);
        }
        if (this.fFeatures.get("http://apache.org/xml/features/validation/schema") == Boolean.TRUE) {
            xMLDocumentSource = this.fSchemaValidator.getDocumentSource();
        } else {
            xMLDocumentSource = this.fLastComponent;
            this.fLastComponent = this.fXPointerHandler;
        }
        XMLDocumentHandler documentHandler = xMLDocumentSource.getDocumentHandler();
        xMLDocumentSource.setDocumentHandler(this.fXIncludeHandler);
        this.fXIncludeHandler.setDocumentSource(xMLDocumentSource);
        if (documentHandler != null) {
            this.fXIncludeHandler.setDocumentHandler(documentHandler);
            documentHandler.setDocumentSource(this.fXIncludeHandler);
        }
        this.fXIncludeHandler.setDocumentHandler(this.fXPointerHandler);
        this.fXPointerHandler.setDocumentSource(this.fXIncludeHandler);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration
    public void configureXML11Pipeline() {
        XMLDocumentSource xMLDocumentSource;
        super.configureXML11Pipeline();
        this.fXML11DTDScanner.setDTDHandler(this.fXML11DTDProcessor);
        this.fXML11DTDProcessor.setDTDSource(this.fXML11DTDScanner);
        this.fDTDProcessor.setDTDHandler(this.fXIncludeHandler);
        this.fXIncludeHandler.setDTDSource(this.fXML11DTDProcessor);
        this.fXIncludeHandler.setDTDHandler(this.fXPointerHandler);
        this.fXPointerHandler.setDTDSource(this.fXIncludeHandler);
        this.fXPointerHandler.setDTDHandler(this.fDTDHandler);
        if (this.fDTDHandler != null) {
            this.fDTDHandler.setDTDSource(this.fXPointerHandler);
        }
        if (this.fFeatures.get("http://apache.org/xml/features/validation/schema") == Boolean.TRUE) {
            xMLDocumentSource = this.fSchemaValidator.getDocumentSource();
        } else {
            xMLDocumentSource = this.fLastComponent;
            this.fLastComponent = this.fXPointerHandler;
        }
        XMLDocumentHandler documentHandler = xMLDocumentSource.getDocumentHandler();
        xMLDocumentSource.setDocumentHandler(this.fXIncludeHandler);
        this.fXIncludeHandler.setDocumentSource(xMLDocumentSource);
        if (documentHandler != null) {
            this.fXIncludeHandler.setDocumentHandler(documentHandler);
            documentHandler.setDocumentSource(this.fXIncludeHandler);
        }
        this.fXIncludeHandler.setDocumentHandler(this.fXPointerHandler);
        this.fXPointerHandler.setDocumentSource(this.fXIncludeHandler);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        super.setProperty(str, obj);
    }
}
