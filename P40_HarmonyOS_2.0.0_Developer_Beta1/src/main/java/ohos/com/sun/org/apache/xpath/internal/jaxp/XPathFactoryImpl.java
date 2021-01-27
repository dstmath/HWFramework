package ohos.com.sun.org.apache.xpath.internal.jaxp;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.javax.xml.xpath.XPath;
import ohos.javax.xml.xpath.XPathFactory;
import ohos.javax.xml.xpath.XPathFactoryConfigurationException;
import ohos.javax.xml.xpath.XPathFunctionResolver;
import ohos.javax.xml.xpath.XPathVariableResolver;
import ohos.jdk.xml.internal.JdkXmlFeatures;

public class XPathFactoryImpl extends XPathFactory {
    private static final String CLASS_NAME = "XPathFactoryImpl";
    private final JdkXmlFeatures _featureManager;
    private boolean _isNotSecureProcessing = true;
    private boolean _isSecureMode = false;
    private XPathFunctionResolver xPathFunctionResolver = null;
    private XPathVariableResolver xPathVariableResolver = null;

    public XPathFactoryImpl() {
        if (System.getSecurityManager() != null) {
            this._isSecureMode = true;
            this._isNotSecureProcessing = false;
        }
        this._featureManager = new JdkXmlFeatures(true ^ this._isNotSecureProcessing);
    }

    public boolean isObjectModelSupported(String str) {
        if (str == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_OBJECT_MODEL_NULL", new Object[]{getClass().getName()}));
        } else if (str.length() != 0) {
            return str.equals("http://java.sun.com/jaxp/xpath/dom");
        } else {
            throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_OBJECT_MODEL_EMPTY", new Object[]{getClass().getName()}));
        }
    }

    public XPath newXPath() {
        return new XPathImpl(this.xPathVariableResolver, this.xPathFunctionResolver, !this._isNotSecureProcessing, this._featureManager);
    }

    public void setFeature(String str, boolean z) throws XPathFactoryConfigurationException {
        JdkXmlFeatures jdkXmlFeatures;
        if (str == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_FEATURE_NAME_NULL", new Object[]{CLASS_NAME, new Boolean(z)}));
        } else if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
            if (!this._isSecureMode || z) {
                this._isNotSecureProcessing = !z;
                if (z && (jdkXmlFeatures = this._featureManager) != null) {
                    jdkXmlFeatures.setFeature(JdkXmlFeatures.XmlFeature.ENABLE_EXTENSION_FUNCTION, JdkXmlFeatures.State.FSP, false);
                    return;
                }
                return;
            }
            throw new XPathFactoryConfigurationException(XSLMessages.createXPATHMessage("ER_SECUREPROCESSING_FEATURE", new Object[]{str, CLASS_NAME, new Boolean(z)}));
        } else if (!str.equals("http://www.oracle.com/feature/use-service-mechanism") || !this._isSecureMode) {
            JdkXmlFeatures jdkXmlFeatures2 = this._featureManager;
            if (jdkXmlFeatures2 == null || !jdkXmlFeatures2.setFeature(str, JdkXmlFeatures.State.APIPROPERTY, Boolean.valueOf(z))) {
                throw new XPathFactoryConfigurationException(XSLMessages.createXPATHMessage("ER_FEATURE_UNKNOWN", new Object[]{str, CLASS_NAME, Boolean.valueOf(z)}));
            }
        }
    }

    public boolean getFeature(String str) throws XPathFactoryConfigurationException {
        if (str == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_GETTING_NULL_FEATURE", new Object[]{CLASS_NAME}));
        } else if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
            return !this._isNotSecureProcessing;
        } else {
            int index = this._featureManager.getIndex(str);
            if (index > -1) {
                return this._featureManager.getFeature(index);
            }
            throw new XPathFactoryConfigurationException(XSLMessages.createXPATHMessage("ER_GETTING_UNKNOWN_FEATURE", new Object[]{str, CLASS_NAME}));
        }
    }

    public void setXPathFunctionResolver(XPathFunctionResolver xPathFunctionResolver2) {
        if (xPathFunctionResolver2 != null) {
            this.xPathFunctionResolver = xPathFunctionResolver2;
            return;
        }
        throw new NullPointerException(XSLMessages.createXPATHMessage("ER_NULL_XPATH_FUNCTION_RESOLVER", new Object[]{CLASS_NAME}));
    }

    public void setXPathVariableResolver(XPathVariableResolver xPathVariableResolver2) {
        if (xPathVariableResolver2 != null) {
            this.xPathVariableResolver = xPathVariableResolver2;
            return;
        }
        throw new NullPointerException(XSLMessages.createXPATHMessage("ER_NULL_XPATH_VARIABLE_RESOLVER", new Object[]{CLASS_NAME}));
    }
}
