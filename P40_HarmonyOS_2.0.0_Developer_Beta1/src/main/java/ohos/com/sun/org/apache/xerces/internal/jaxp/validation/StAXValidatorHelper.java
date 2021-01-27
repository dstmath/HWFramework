package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.Transformer;
import ohos.javax.xml.transform.TransformerConfigurationException;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.TransformerFactoryConfigurationError;
import ohos.javax.xml.transform.sax.SAXResult;
import ohos.javax.xml.transform.sax.SAXTransformerFactory;
import ohos.javax.xml.transform.sax.TransformerHandler;
import ohos.javax.xml.transform.stax.StAXResult;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.SAXException;

public final class StAXValidatorHelper implements ValidatorHelper {
    private XMLSchemaValidatorComponentManager fComponentManager;
    private ValidatorHandlerImpl handler = null;
    private Transformer identityTransformer1 = null;
    private TransformerHandler identityTransformer2 = null;

    public StAXValidatorHelper(XMLSchemaValidatorComponentManager xMLSchemaValidatorComponentManager) {
        this.fComponentManager = xMLSchemaValidatorComponentManager;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.ValidatorHelper
    public void validate(Source source, Result result) throws SAXException, IOException {
        if (result == null || (result instanceof StAXResult)) {
            if (this.identityTransformer1 == null) {
                try {
                    SAXTransformerFactory sAXTransformFactory = JdkXmlUtils.getSAXTransformFactory(this.fComponentManager.getFeature("jdk.xml.overrideDefaultParser"));
                    XMLSecurityManager xMLSecurityManager = (XMLSecurityManager) this.fComponentManager.getProperty("http://apache.org/xml/properties/security-manager");
                    if (xMLSecurityManager != null) {
                        XMLSecurityManager.Limit[] values = XMLSecurityManager.Limit.values();
                        for (XMLSecurityManager.Limit limit : values) {
                            if (xMLSecurityManager.isSet(limit.ordinal())) {
                                sAXTransformFactory.setAttribute(limit.apiProperty(), xMLSecurityManager.getLimitValueAsString(limit));
                            }
                        }
                        if (xMLSecurityManager.printEntityCountInfo()) {
                            sAXTransformFactory.setAttribute("http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo", "yes");
                        }
                    }
                    this.identityTransformer1 = sAXTransformFactory.newTransformer();
                    this.identityTransformer2 = sAXTransformFactory.newTransformerHandler();
                } catch (TransformerConfigurationException e) {
                    throw new TransformerFactoryConfigurationError(e);
                }
            }
            this.handler = new ValidatorHandlerImpl(this.fComponentManager);
            if (result != null) {
                this.handler.setContentHandler(this.identityTransformer2);
                this.identityTransformer2.setResult(result);
            }
            try {
                this.identityTransformer1.transform(source, new SAXResult(this.handler));
                this.handler.setContentHandler(null);
            } catch (TransformerException e2) {
                if (e2.getException() instanceof SAXException) {
                    throw e2.getException();
                }
                throw new SAXException(e2);
            } catch (Throwable th) {
                this.handler.setContentHandler(null);
                throw th;
            }
        } else {
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(this.fComponentManager.getLocale(), "SourceResultMismatch", new Object[]{source.getClass().getName(), result.getClass().getName()}));
        }
    }
}
