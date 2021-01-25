package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLNSDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLNSDTDValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner;

public class IntegratedParserConfiguration extends StandardParserConfiguration {
    protected XMLNSDocumentScannerImpl fNamespaceScanner;
    protected XMLDTDValidator fNonNSDTDValidator;
    protected XMLDocumentScannerImpl fNonNSScanner;

    public IntegratedParserConfiguration() {
        this(null, null, null);
    }

    public IntegratedParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public IntegratedParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public IntegratedParserConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(symbolTable, xMLGrammarPool, xMLComponentManager);
        this.fNonNSScanner = new XMLDocumentScannerImpl();
        this.fNonNSDTDValidator = new XMLDTDValidator();
        addComponent(this.fNonNSScanner);
        addComponent(this.fNonNSDTDValidator);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.StandardParserConfiguration, ohos.com.sun.org.apache.xerces.internal.parsers.DTDConfiguration
    public void configurePipeline() {
        setProperty("http://apache.org/xml/properties/internal/datatype-validator-factory", this.fDatatypeValidatorFactory);
        configureDTDPipeline();
        if (this.fFeatures.get("http://xml.org/sax/features/namespaces") == Boolean.TRUE) {
            this.fProperties.put("http://apache.org/xml/properties/internal/namespace-binder", this.fNamespaceBinder);
            this.fScanner = this.fNamespaceScanner;
            this.fProperties.put("http://apache.org/xml/properties/internal/document-scanner", this.fNamespaceScanner);
            if (this.fDTDValidator != null) {
                this.fProperties.put("http://apache.org/xml/properties/internal/validator/dtd", this.fDTDValidator);
                this.fNamespaceScanner.setDTDValidator(this.fDTDValidator);
                this.fNamespaceScanner.setDocumentHandler(this.fDTDValidator);
                this.fDTDValidator.setDocumentSource(this.fNamespaceScanner);
                this.fDTDValidator.setDocumentHandler(this.fDocumentHandler);
                if (this.fDocumentHandler != null) {
                    this.fDocumentHandler.setDocumentSource(this.fDTDValidator);
                }
                this.fLastComponent = this.fDTDValidator;
            } else {
                this.fNamespaceScanner.setDocumentHandler(this.fDocumentHandler);
                this.fNamespaceScanner.setDTDValidator(null);
                if (this.fDocumentHandler != null) {
                    this.fDocumentHandler.setDocumentSource(this.fNamespaceScanner);
                }
                this.fLastComponent = this.fNamespaceScanner;
            }
        } else {
            this.fScanner = this.fNonNSScanner;
            this.fProperties.put("http://apache.org/xml/properties/internal/document-scanner", this.fNonNSScanner);
            if (this.fNonNSDTDValidator != null) {
                this.fProperties.put("http://apache.org/xml/properties/internal/validator/dtd", this.fNonNSDTDValidator);
                this.fNonNSScanner.setDocumentHandler(this.fNonNSDTDValidator);
                this.fNonNSDTDValidator.setDocumentSource(this.fNonNSScanner);
                this.fNonNSDTDValidator.setDocumentHandler(this.fDocumentHandler);
                if (this.fDocumentHandler != null) {
                    this.fDocumentHandler.setDocumentSource(this.fNonNSDTDValidator);
                }
                this.fLastComponent = this.fNonNSDTDValidator;
            } else {
                this.fScanner.setDocumentHandler(this.fDocumentHandler);
                if (this.fDocumentHandler != null) {
                    this.fDocumentHandler.setDocumentSource(this.fScanner);
                }
                this.fLastComponent = this.fScanner;
            }
        }
        if (this.fFeatures.get("http://apache.org/xml/features/validation/schema") == Boolean.TRUE) {
            if (this.fSchemaValidator == null) {
                this.fSchemaValidator = new XMLSchemaValidator();
                this.fProperties.put("http://apache.org/xml/properties/internal/validator/schema", this.fSchemaValidator);
                addComponent(this.fSchemaValidator);
                if (this.fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
                    this.fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
                }
            }
            this.fLastComponent.setDocumentHandler(this.fSchemaValidator);
            this.fSchemaValidator.setDocumentSource(this.fLastComponent);
            this.fSchemaValidator.setDocumentHandler(this.fDocumentHandler);
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.setDocumentSource(this.fSchemaValidator);
            }
            this.fLastComponent = this.fSchemaValidator;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.DTDConfiguration
    public XMLDocumentScanner createDocumentScanner() {
        this.fNamespaceScanner = new XMLNSDocumentScannerImpl();
        return this.fNamespaceScanner;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.DTDConfiguration
    public XMLDTDValidator createDTDValidator() {
        return new XMLNSDTDValidator();
    }
}
