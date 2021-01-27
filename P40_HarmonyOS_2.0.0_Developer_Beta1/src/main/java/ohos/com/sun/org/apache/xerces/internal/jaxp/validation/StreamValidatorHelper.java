package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.io.IOException;
import java.lang.ref.SoftReference;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.TransformerConfigurationException;
import ohos.javax.xml.transform.TransformerFactoryConfigurationError;
import ohos.javax.xml.transform.stream.StreamResult;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;

final class StreamValidatorHelper implements ValidatorHelper {
    private static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    private static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    private static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    private static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    private static final String SCHEMA_VALIDATOR = "http://apache.org/xml/properties/internal/validator/schema";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    private static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    private XMLSchemaValidatorComponentManager fComponentManager;
    private SoftReference fConfiguration = new SoftReference(null);
    private XMLSchemaValidator fSchemaValidator;
    private ValidatorHandlerImpl handler = null;

    public StreamValidatorHelper(XMLSchemaValidatorComponentManager xMLSchemaValidatorComponentManager) {
        this.fComponentManager = xMLSchemaValidatorComponentManager;
        this.fSchemaValidator = (XMLSchemaValidator) this.fComponentManager.getProperty(SCHEMA_VALIDATOR);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.ValidatorHelper
    public void validate(Source source, Result result) throws SAXException, IOException {
        if (result == null || (result instanceof StreamResult)) {
            StreamSource streamSource = (StreamSource) source;
            if (result != null) {
                try {
                    ContentHandler newTransformerHandler = JdkXmlUtils.getSAXTransformFactory(this.fComponentManager.getFeature("jdk.xml.overrideDefaultParser")).newTransformerHandler();
                    this.handler = new ValidatorHandlerImpl(this.fComponentManager);
                    this.handler.setContentHandler(newTransformerHandler);
                    newTransformerHandler.setResult(result);
                } catch (TransformerConfigurationException e) {
                    throw new TransformerFactoryConfigurationError(e);
                }
            }
            XMLInputSource xMLInputSource = new XMLInputSource(streamSource.getPublicId(), streamSource.getSystemId(), null);
            xMLInputSource.setByteStream(streamSource.getInputStream());
            xMLInputSource.setCharacterStream(streamSource.getReader());
            XMLParserConfiguration xMLParserConfiguration = (XMLParserConfiguration) this.fConfiguration.get();
            if (xMLParserConfiguration == null) {
                xMLParserConfiguration = initialize();
            } else if (this.fComponentManager.getFeature(PARSER_SETTINGS)) {
                xMLParserConfiguration.setProperty("http://apache.org/xml/properties/internal/entity-resolver", this.fComponentManager.getProperty("http://apache.org/xml/properties/internal/entity-resolver"));
                xMLParserConfiguration.setProperty(ERROR_HANDLER, this.fComponentManager.getProperty(ERROR_HANDLER));
            }
            this.fComponentManager.reset();
            this.fSchemaValidator.setDocumentHandler(this.handler);
            try {
                xMLParserConfiguration.parse(xMLInputSource);
            } catch (XMLParseException e2) {
                throw Util.toSAXParseException(e2);
            } catch (XNIException e3) {
                throw Util.toSAXException(e3);
            }
        } else {
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(this.fComponentManager.getLocale(), "SourceResultMismatch", new Object[]{source.getClass().getName(), result.getClass().getName()}));
        }
    }

    private XMLParserConfiguration initialize() {
        XML11Configuration xML11Configuration = new XML11Configuration();
        if (this.fComponentManager.getFeature(Constants.FEATURE_SECURE_PROCESSING)) {
            xML11Configuration.setProperty("http://apache.org/xml/properties/security-manager", new XMLSecurityManager());
        }
        xML11Configuration.setProperty("http://apache.org/xml/properties/internal/entity-resolver", this.fComponentManager.getProperty("http://apache.org/xml/properties/internal/entity-resolver"));
        xML11Configuration.setProperty(ERROR_HANDLER, this.fComponentManager.getProperty(ERROR_HANDLER));
        XMLErrorReporter xMLErrorReporter = (XMLErrorReporter) this.fComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        xML11Configuration.setProperty("http://apache.org/xml/properties/internal/error-reporter", xMLErrorReporter);
        if (xMLErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210") == null) {
            XMLMessageFormatter xMLMessageFormatter = new XMLMessageFormatter();
            xMLErrorReporter.putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", xMLMessageFormatter);
            xMLErrorReporter.putMessageFormatter("http://www.w3.org/TR/1999/REC-xml-names-19990114", xMLMessageFormatter);
        }
        xML11Configuration.setProperty("http://apache.org/xml/properties/internal/symbol-table", this.fComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table"));
        xML11Configuration.setProperty(VALIDATION_MANAGER, this.fComponentManager.getProperty(VALIDATION_MANAGER));
        xML11Configuration.setDocumentHandler(this.fSchemaValidator);
        xML11Configuration.setDTDHandler(null);
        xML11Configuration.setDTDContentModelHandler(null);
        xML11Configuration.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fComponentManager.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager"));
        xML11Configuration.setProperty("http://apache.org/xml/properties/security-manager", this.fComponentManager.getProperty("http://apache.org/xml/properties/security-manager"));
        this.fConfiguration = new SoftReference(xML11Configuration);
        return xML11Configuration;
    }
}
