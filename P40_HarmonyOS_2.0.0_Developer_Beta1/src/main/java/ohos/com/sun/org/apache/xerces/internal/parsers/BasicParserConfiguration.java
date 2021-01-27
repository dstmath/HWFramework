package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.util.FeatureState;
import ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import ohos.com.sun.org.apache.xerces.internal.util.PropertyState;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;

public abstract class BasicParserConfiguration extends ParserConfigurationSettings implements XMLParserConfiguration {
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    protected static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    protected static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    protected static final String XML_STRING = "http://xml.org/sax/properties/xml-string";
    protected ArrayList fComponents;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    protected XMLDTDHandler fDTDHandler;
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDocumentSource fLastComponent;
    protected Locale fLocale;
    protected SymbolTable fSymbolTable;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public abstract void parse(XMLInputSource xMLInputSource) throws XNIException, IOException;

    protected BasicParserConfiguration() {
        this(null, null);
    }

    protected BasicParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null);
    }

    protected BasicParserConfiguration(SymbolTable symbolTable, XMLComponentManager xMLComponentManager) {
        super(xMLComponentManager);
        this.fComponents = new ArrayList();
        this.fFeatures = new HashMap();
        this.fProperties = new HashMap();
        addRecognizedFeatures(new String[]{"http://apache.org/xml/features/internal/parser-settings", VALIDATION, "http://xml.org/sax/features/namespaces", EXTERNAL_GENERAL_ENTITIES, EXTERNAL_PARAMETER_ENTITIES});
        this.fFeatures.put("http://apache.org/xml/features/internal/parser-settings", Boolean.TRUE);
        this.fFeatures.put(VALIDATION, Boolean.FALSE);
        this.fFeatures.put("http://xml.org/sax/features/namespaces", Boolean.TRUE);
        this.fFeatures.put(EXTERNAL_GENERAL_ENTITIES, Boolean.TRUE);
        this.fFeatures.put(EXTERNAL_PARAMETER_ENTITIES, Boolean.TRUE);
        addRecognizedProperties(new String[]{XML_STRING, "http://apache.org/xml/properties/internal/symbol-table", ERROR_HANDLER, "http://apache.org/xml/properties/internal/entity-resolver"});
        this.fSymbolTable = symbolTable == null ? new SymbolTable() : symbolTable;
        this.fProperties.put("http://apache.org/xml/properties/internal/symbol-table", this.fSymbolTable);
    }

    /* access modifiers changed from: protected */
    public void addComponent(XMLComponent xMLComponent) {
        if (!this.fComponents.contains(xMLComponent)) {
            this.fComponents.add(xMLComponent);
            String[] recognizedFeatures = xMLComponent.getRecognizedFeatures();
            addRecognizedFeatures(recognizedFeatures);
            String[] recognizedProperties = xMLComponent.getRecognizedProperties();
            addRecognizedProperties(recognizedProperties);
            if (recognizedFeatures != null) {
                for (String str : recognizedFeatures) {
                    Boolean featureDefault = xMLComponent.getFeatureDefault(str);
                    if (featureDefault != null) {
                        super.setFeature(str, featureDefault.booleanValue());
                    }
                }
            }
            if (recognizedProperties != null) {
                for (String str2 : recognizedProperties) {
                    Object propertyDefault = xMLComponent.getPropertyDefault(str2);
                    if (propertyDefault != null) {
                        super.setProperty(str2, propertyDefault);
                    }
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler) {
        this.fDocumentHandler = xMLDocumentHandler;
        XMLDocumentSource xMLDocumentSource = this.fLastComponent;
        if (xMLDocumentSource != null) {
            xMLDocumentSource.setDocumentHandler(this.fDocumentHandler);
            XMLDocumentHandler xMLDocumentHandler2 = this.fDocumentHandler;
            if (xMLDocumentHandler2 != null) {
                xMLDocumentHandler2.setDocumentSource(this.fLastComponent);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLDocumentHandler getDocumentHandler() {
        return this.fDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setDTDHandler(XMLDTDHandler xMLDTDHandler) {
        this.fDTDHandler = xMLDTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLDTDHandler getDTDHandler() {
        return this.fDTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setDTDContentModelHandler(XMLDTDContentModelHandler xMLDTDContentModelHandler) {
        this.fDTDContentModelHandler = xMLDTDContentModelHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return this.fDTDContentModelHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setEntityResolver(XMLEntityResolver xMLEntityResolver) {
        this.fProperties.put("http://apache.org/xml/properties/internal/entity-resolver", xMLEntityResolver);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLEntityResolver getEntityResolver() {
        return (XMLEntityResolver) this.fProperties.get("http://apache.org/xml/properties/internal/entity-resolver");
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setErrorHandler(XMLErrorHandler xMLErrorHandler) {
        this.fProperties.put(ERROR_HANDLER, xMLErrorHandler);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLErrorHandler getErrorHandler() {
        return (XMLErrorHandler) this.fProperties.get(ERROR_HANDLER);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        int size = this.fComponents.size();
        for (int i = 0; i < size; i++) {
            ((XMLComponent) this.fComponents.get(i)).setFeature(str, z);
        }
        super.setFeature(str, z);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        int size = this.fComponents.size();
        for (int i = 0; i < size; i++) {
            ((XMLComponent) this.fComponents.get(i)).setProperty(str, obj);
        }
        super.setProperty(str, obj);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setLocale(Locale locale) throws XNIException {
        this.fLocale = locale;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public Locale getLocale() {
        return this.fLocale;
    }

    /* access modifiers changed from: protected */
    public void reset() throws XNIException {
        int size = this.fComponents.size();
        for (int i = 0; i < size; i++) {
            ((XMLComponent) this.fComponents.get(i)).reset(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings
    public PropertyState checkProperty(String str) throws XMLConfigurationException {
        if (!str.startsWith(Constants.SAX_PROPERTY_PREFIX) || str.length() - 30 != 10 || !str.endsWith(Constants.XML_STRING_PROPERTY)) {
            return super.checkProperty(str);
        }
        return PropertyState.NOT_SUPPORTED;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings
    public FeatureState checkFeature(String str) throws XMLConfigurationException {
        if (!str.startsWith(Constants.XERCES_FEATURE_PREFIX) || str.length() - 31 != 24 || !str.endsWith(Constants.PARSER_SETTINGS)) {
            return super.checkFeature(str);
        }
        return FeatureState.NOT_SUPPORTED;
    }
}
