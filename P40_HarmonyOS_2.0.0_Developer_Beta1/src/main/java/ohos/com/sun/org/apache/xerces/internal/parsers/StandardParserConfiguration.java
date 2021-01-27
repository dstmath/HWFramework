package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.FeatureState;
import ohos.com.sun.org.apache.xerces.internal.util.PropertyState;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;

public class StandardParserConfiguration extends DTDConfiguration {
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS = "http://apache.org/xml/features/generate-synthetic-annotations";
    protected static final String HONOUR_ALL_SCHEMALOCATIONS = "http://apache.org/xml/features/honour-all-schemaLocations";
    protected static final String NAMESPACE_GROWTH = "http://apache.org/xml/features/namespace-growth";
    protected static final String NORMALIZE_DATA = "http://apache.org/xml/features/validation/schema/normalized-value";
    protected static final String SCHEMA_AUGMENT_PSVI = "http://apache.org/xml/features/validation/schema/augment-psvi";
    protected static final String SCHEMA_DV_FACTORY = "http://apache.org/xml/properties/internal/validation/schema/dv-factory";
    protected static final String SCHEMA_ELEMENT_DEFAULT = "http://apache.org/xml/features/validation/schema/element-default";
    protected static final String SCHEMA_LOCATION = "http://apache.org/xml/properties/schema/external-schemaLocation";
    protected static final String SCHEMA_NONS_LOCATION = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";
    protected static final String SCHEMA_VALIDATOR = "http://apache.org/xml/properties/internal/validator/schema";
    protected static final String TOLERATE_DUPLICATES = "http://apache.org/xml/features/internal/tolerate-duplicates";
    protected static final String VALIDATE_ANNOTATIONS = "http://apache.org/xml/features/validate-annotations";
    protected static final String XMLSCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";
    protected static final String XMLSCHEMA_VALIDATION = "http://apache.org/xml/features/validation/schema";
    protected XMLSchemaValidator fSchemaValidator;

    public StandardParserConfiguration() {
        this(null, null, null);
    }

    public StandardParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public StandardParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public StandardParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(symbolTable, xMLGrammarPool, xMLComponentManager);
        addRecognizedFeatures(new String[]{NORMALIZE_DATA, SCHEMA_ELEMENT_DEFAULT, SCHEMA_AUGMENT_PSVI, "http://apache.org/xml/features/generate-synthetic-annotations", VALIDATE_ANNOTATIONS, HONOUR_ALL_SCHEMALOCATIONS, NAMESPACE_GROWTH, TOLERATE_DUPLICATES, XMLSCHEMA_VALIDATION, XMLSCHEMA_FULL_CHECKING});
        setFeature(SCHEMA_ELEMENT_DEFAULT, true);
        setFeature(NORMALIZE_DATA, true);
        setFeature(SCHEMA_AUGMENT_PSVI, true);
        setFeature("http://apache.org/xml/features/generate-synthetic-annotations", false);
        setFeature(VALIDATE_ANNOTATIONS, false);
        setFeature(HONOUR_ALL_SCHEMALOCATIONS, false);
        setFeature(NAMESPACE_GROWTH, false);
        setFeature(TOLERATE_DUPLICATES, false);
        addRecognizedProperties(new String[]{SCHEMA_LOCATION, SCHEMA_NONS_LOCATION, SCHEMA_DV_FACTORY});
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.DTDConfiguration
    public void configurePipeline() {
        super.configurePipeline();
        if (getFeature(XMLSCHEMA_VALIDATION)) {
            if (this.fSchemaValidator == null) {
                this.fSchemaValidator = new XMLSchemaValidator();
                this.fProperties.put(SCHEMA_VALIDATOR, this.fSchemaValidator);
                addComponent(this.fSchemaValidator);
                if (this.fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
                    this.fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
                }
            }
            this.fLastComponent = this.fSchemaValidator;
            this.fNamespaceBinder.setDocumentHandler(this.fSchemaValidator);
            this.fSchemaValidator.setDocumentHandler(this.fDocumentHandler);
            this.fSchemaValidator.setDocumentSource(this.fNamespaceBinder);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.DTDConfiguration, ohos.com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings
    public FeatureState checkFeature(String str) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            int length = str.length() - 31;
            if (length == 17 && str.endsWith(Constants.SCHEMA_VALIDATION_FEATURE)) {
                return FeatureState.RECOGNIZED;
            }
            if (length == 31 && str.endsWith(Constants.SCHEMA_FULL_CHECKING)) {
                return FeatureState.RECOGNIZED;
            }
            if (length == 34 && str.endsWith(Constants.SCHEMA_NORMALIZED_VALUE)) {
                return FeatureState.RECOGNIZED;
            }
            if (length == 33 && str.endsWith(Constants.SCHEMA_ELEMENT_DEFAULT)) {
                return FeatureState.RECOGNIZED;
            }
        }
        return super.checkFeature(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.DTDConfiguration, ohos.com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings
    public PropertyState checkProperty(String str) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            int length = str.length() - 33;
            if (length == 30 && str.endsWith(Constants.SCHEMA_LOCATION)) {
                return PropertyState.RECOGNIZED;
            }
            if (length == 41 && str.endsWith(Constants.SCHEMA_NONS_LOCATION)) {
                return PropertyState.RECOGNIZED;
            }
        }
        if (!str.startsWith(Constants.JAXP_PROPERTY_PREFIX) || str.length() - 40 != 12 || !str.endsWith(Constants.SCHEMA_SOURCE)) {
            return super.checkProperty(str);
        }
        return PropertyState.RECOGNIZED;
    }
}
