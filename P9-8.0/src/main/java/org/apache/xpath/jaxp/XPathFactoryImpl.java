package org.apache.xpath.jaxp;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class XPathFactoryImpl extends XPathFactory {
    private static final String CLASS_NAME = "XPathFactoryImpl";
    private boolean featureSecureProcessing = false;
    private XPathFunctionResolver xPathFunctionResolver = null;
    private XPathVariableResolver xPathVariableResolver = null;

    public boolean isObjectModelSupported(String objectModel) {
        if (objectModel == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_OBJECT_MODEL_NULL, new Object[]{getClass().getName()}));
        } else if (objectModel.length() != 0) {
            return objectModel.equals("http://java.sun.com/jaxp/xpath/dom");
        } else {
            throw new IllegalArgumentException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_OBJECT_MODEL_EMPTY, new Object[]{getClass().getName()}));
        }
    }

    public XPath newXPath() {
        return new XPathImpl(this.xPathVariableResolver, this.xPathFunctionResolver, this.featureSecureProcessing);
    }

    public void setFeature(String name, boolean value) throws XPathFactoryConfigurationException {
        if (name == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_FEATURE_NAME_NULL, new Object[]{CLASS_NAME, new Boolean(value)}));
        } else if (name.equals("http://javax.xml.XMLConstants/feature/secure-processing")) {
            this.featureSecureProcessing = value;
        } else {
            throw new XPathFactoryConfigurationException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_FEATURE_UNKNOWN, new Object[]{name, CLASS_NAME, new Boolean(value)}));
        }
    }

    public boolean getFeature(String name) throws XPathFactoryConfigurationException {
        if (name == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_GETTING_NULL_FEATURE, new Object[]{CLASS_NAME}));
        } else if (name.equals("http://javax.xml.XMLConstants/feature/secure-processing")) {
            return this.featureSecureProcessing;
        } else {
            throw new XPathFactoryConfigurationException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_GETTING_UNKNOWN_FEATURE, new Object[]{name, CLASS_NAME}));
        }
    }

    public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
        if (resolver == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_XPATH_FUNCTION_RESOLVER, new Object[]{CLASS_NAME}));
        }
        this.xPathFunctionResolver = resolver;
    }

    public void setXPathVariableResolver(XPathVariableResolver resolver) {
        if (resolver == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_XPATH_VARIABLE_RESOLVER, new Object[]{CLASS_NAME}));
        }
        this.xPathVariableResolver = resolver;
    }
}
