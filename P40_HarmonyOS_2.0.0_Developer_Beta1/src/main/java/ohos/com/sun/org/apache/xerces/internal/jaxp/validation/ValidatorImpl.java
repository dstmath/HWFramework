package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.Status;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.transform.stax.StAXSource;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.javax.xml.validation.Validator;
import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;

final class ValidatorImpl extends Validator implements PSVIProvider {
    private static final String CURRENT_ELEMENT_NODE = "http://apache.org/xml/properties/dom/current-element-node";
    private XMLSchemaValidatorComponentManager fComponentManager;
    private boolean fConfigurationChanged = false;
    private DOMValidatorHelper fDOMValidatorHelper;
    private boolean fErrorHandlerChanged = false;
    private boolean fResourceResolverChanged = false;
    private ValidatorHandlerImpl fSAXValidatorHelper;
    private StAXValidatorHelper fStaxValidatorHelper;
    private StreamValidatorHelper fStreamValidatorHelper;

    public ValidatorImpl(XSGrammarPoolContainer xSGrammarPoolContainer) {
        this.fComponentManager = new XMLSchemaValidatorComponentManager(xSGrammarPoolContainer);
        setErrorHandler(null);
        setResourceResolver(null);
    }

    public void validate(Source source, Result result) throws SAXException, IOException {
        if (source instanceof SAXSource) {
            if (this.fSAXValidatorHelper == null) {
                this.fSAXValidatorHelper = new ValidatorHandlerImpl(this.fComponentManager);
            }
            this.fSAXValidatorHelper.validate(source, result);
        } else if (source instanceof DOMSource) {
            if (this.fDOMValidatorHelper == null) {
                this.fDOMValidatorHelper = new DOMValidatorHelper(this.fComponentManager);
            }
            this.fDOMValidatorHelper.validate(source, result);
        } else if (source instanceof StreamSource) {
            if (this.fStreamValidatorHelper == null) {
                this.fStreamValidatorHelper = new StreamValidatorHelper(this.fComponentManager);
            }
            this.fStreamValidatorHelper.validate(source, result);
        } else if (source instanceof StAXSource) {
            if (this.fStaxValidatorHelper == null) {
                this.fStaxValidatorHelper = new StAXValidatorHelper(this.fComponentManager);
            }
            this.fStaxValidatorHelper.validate(source, result);
        } else if (source == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(this.fComponentManager.getLocale(), "SourceParameterNull", null));
        } else {
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(this.fComponentManager.getLocale(), "SourceNotAccepted", new Object[]{source.getClass().getName()}));
        }
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.fErrorHandlerChanged = errorHandler != null;
        this.fComponentManager.setErrorHandler(errorHandler);
    }

    public ErrorHandler getErrorHandler() {
        return this.fComponentManager.getErrorHandler();
    }

    public void setResourceResolver(LSResourceResolver lSResourceResolver) {
        this.fResourceResolverChanged = lSResourceResolver != null;
        this.fComponentManager.setResourceResolver(lSResourceResolver);
    }

    public LSResourceResolver getResourceResolver() {
        return this.fComponentManager.getResourceResolver();
    }

    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str != null) {
            try {
                return this.fComponentManager.getFeature(str);
            } catch (XMLConfigurationException e) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), e.getType() == Status.NOT_RECOGNIZED ? "feature-not-recognized" : "feature-not-supported", new Object[]{e.getIdentifier()}));
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str != null) {
            try {
                this.fComponentManager.setFeature(str, z);
                this.fConfigurationChanged = true;
            } catch (XMLConfigurationException e) {
                String identifier = e.getIdentifier();
                if (e.getType() != Status.NOT_ALLOWED) {
                    throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), e.getType() == Status.NOT_RECOGNIZED ? "feature-not-recognized" : "feature-not-supported", new Object[]{identifier}));
                }
                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), "jaxp-secureprocessing-feature", null));
            }
        } else {
            throw new NullPointerException();
        }
    }

    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str == null) {
            throw new NullPointerException();
        } else if (CURRENT_ELEMENT_NODE.equals(str)) {
            DOMValidatorHelper dOMValidatorHelper = this.fDOMValidatorHelper;
            if (dOMValidatorHelper != null) {
                return dOMValidatorHelper.getCurrentElement();
            }
            return null;
        } else {
            try {
                return this.fComponentManager.getProperty(str);
            } catch (XMLConfigurationException e) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), e.getType() == Status.NOT_RECOGNIZED ? "property-not-recognized" : "property-not-supported", new Object[]{e.getIdentifier()}));
            }
        }
    }

    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str != null) {
            try {
                this.fComponentManager.setProperty(str, obj);
                this.fConfigurationChanged = true;
            } catch (XMLConfigurationException e) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), e.getType() == Status.NOT_RECOGNIZED ? "property-not-recognized" : "property-not-supported", new Object[]{e.getIdentifier()}));
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void reset() {
        if (this.fConfigurationChanged) {
            this.fComponentManager.restoreInitialState();
            setErrorHandler(null);
            setResourceResolver(null);
            this.fConfigurationChanged = false;
            this.fErrorHandlerChanged = false;
            this.fResourceResolverChanged = false;
            return;
        }
        if (this.fErrorHandlerChanged) {
            setErrorHandler(null);
            this.fErrorHandlerChanged = false;
        }
        if (this.fResourceResolverChanged) {
            setResourceResolver(null);
            this.fResourceResolverChanged = false;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider
    public ElementPSVI getElementPSVI() {
        ValidatorHandlerImpl validatorHandlerImpl = this.fSAXValidatorHelper;
        if (validatorHandlerImpl != null) {
            return validatorHandlerImpl.getElementPSVI();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider
    public AttributePSVI getAttributePSVI(int i) {
        ValidatorHandlerImpl validatorHandlerImpl = this.fSAXValidatorHelper;
        if (validatorHandlerImpl != null) {
            return validatorHandlerImpl.getAttributePSVI(i);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider
    public AttributePSVI getAttributePSVIByName(String str, String str2) {
        ValidatorHandlerImpl validatorHandlerImpl = this.fSAXValidatorHelper;
        if (validatorHandlerImpl != null) {
            return validatorHandlerImpl.getAttributePSVIByName(str, str2);
        }
        return null;
    }
}
