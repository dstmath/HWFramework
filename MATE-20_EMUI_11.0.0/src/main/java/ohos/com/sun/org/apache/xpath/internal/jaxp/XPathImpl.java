package ohos.com.sun.org.apache.xpath.internal.jaxp;

import java.io.IOException;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.parsers.DocumentBuilder;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.xpath.XPath;
import ohos.javax.xml.xpath.XPathConstants;
import ohos.javax.xml.xpath.XPathExpression;
import ohos.javax.xml.xpath.XPathExpressionException;
import ohos.javax.xml.xpath.XPathFunctionException;
import ohos.javax.xml.xpath.XPathFunctionResolver;
import ohos.javax.xml.xpath.XPathVariableResolver;
import ohos.jdk.xml.internal.JdkXmlFeatures;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;

public class XPathImpl implements XPath {
    private static Document d;
    private final JdkXmlFeatures featureManager;
    private boolean featureSecureProcessing;
    private XPathFunctionResolver functionResolver;
    private NamespaceContext namespaceContext;
    private XPathFunctionResolver origFunctionResolver;
    private XPathVariableResolver origVariableResolver;
    private boolean overrideDefaultParser;
    private JAXPPrefixResolver prefixResolver;
    private XPathVariableResolver variableResolver;

    XPathImpl(XPathVariableResolver xPathVariableResolver, XPathFunctionResolver xPathFunctionResolver) {
        this(xPathVariableResolver, xPathFunctionResolver, false, new JdkXmlFeatures(false));
    }

    XPathImpl(XPathVariableResolver xPathVariableResolver, XPathFunctionResolver xPathFunctionResolver, boolean z, JdkXmlFeatures jdkXmlFeatures) {
        this.namespaceContext = null;
        this.featureSecureProcessing = false;
        this.overrideDefaultParser = true;
        this.variableResolver = xPathVariableResolver;
        this.origVariableResolver = xPathVariableResolver;
        this.functionResolver = xPathFunctionResolver;
        this.origFunctionResolver = xPathFunctionResolver;
        this.featureSecureProcessing = z;
        this.featureManager = jdkXmlFeatures;
        this.overrideDefaultParser = jdkXmlFeatures.getFeature(JdkXmlFeatures.XmlFeature.JDK_OVERRIDE_PARSER);
    }

    public void setXPathVariableResolver(XPathVariableResolver xPathVariableResolver) {
        if (xPathVariableResolver != null) {
            this.variableResolver = xPathVariableResolver;
            return;
        }
        throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"XPathVariableResolver"}));
    }

    public XPathVariableResolver getXPathVariableResolver() {
        return this.variableResolver;
    }

    public void setXPathFunctionResolver(XPathFunctionResolver xPathFunctionResolver) {
        if (xPathFunctionResolver != null) {
            this.functionResolver = xPathFunctionResolver;
            return;
        }
        throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"XPathFunctionResolver"}));
    }

    public XPathFunctionResolver getXPathFunctionResolver() {
        return this.functionResolver;
    }

    public void setNamespaceContext(NamespaceContext namespaceContext2) {
        if (namespaceContext2 != null) {
            this.namespaceContext = namespaceContext2;
            this.prefixResolver = new JAXPPrefixResolver(namespaceContext2);
            return;
        }
        throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"NamespaceContext"}));
    }

    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    private DocumentBuilder getParser() {
        try {
            return JdkXmlUtils.getDOMFactory(this.overrideDefaultParser).newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new Error((Throwable) e);
        }
    }

    private XObject eval(String str, Object obj) throws TransformerException {
        XPathContext xPathContext;
        ohos.com.sun.org.apache.xpath.internal.XPath xPath = new ohos.com.sun.org.apache.xpath.internal.XPath(str, null, this.prefixResolver, 0);
        XPathFunctionResolver xPathFunctionResolver = this.functionResolver;
        if (xPathFunctionResolver != null) {
            xPathContext = new XPathContext(new JAXPExtensionsProvider(xPathFunctionResolver, this.featureSecureProcessing, this.featureManager));
        } else {
            xPathContext = new XPathContext();
        }
        xPathContext.setVarStack(new JAXPVariableStack(this.variableResolver));
        if (obj instanceof Node) {
            return xPath.execute(xPathContext, (Node) obj, this.prefixResolver);
        }
        return xPath.execute(xPathContext, -1, this.prefixResolver);
    }

    public Object evaluate(String str, Object obj, QName qName) throws XPathExpressionException {
        if (str == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"XPath expression"}));
        } else if (qName == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"returnType"}));
        } else if (isSupported(qName)) {
            try {
                return getResultAsType(eval(str, obj), qName);
            } catch (NullPointerException e) {
                throw new XPathExpressionException(e);
            } catch (TransformerException e2) {
                XPathFunctionException exception = e2.getException();
                if (exception instanceof XPathFunctionException) {
                    throw exception;
                }
                throw new XPathExpressionException(e2);
            }
        } else {
            throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_UNSUPPORTED_RETURN_TYPE", new Object[]{qName.toString()}));
        }
    }

    private boolean isSupported(QName qName) {
        return qName.equals(XPathConstants.STRING) || qName.equals(XPathConstants.NUMBER) || qName.equals(XPathConstants.BOOLEAN) || qName.equals(XPathConstants.NODE) || qName.equals(XPathConstants.NODESET);
    }

    private Object getResultAsType(XObject xObject, QName qName) throws TransformerException {
        if (qName.equals(XPathConstants.STRING)) {
            return xObject.str();
        }
        if (qName.equals(XPathConstants.NUMBER)) {
            return new Double(xObject.num());
        }
        if (qName.equals(XPathConstants.BOOLEAN)) {
            return new Boolean(xObject.bool());
        }
        if (qName.equals(XPathConstants.NODESET)) {
            return xObject.nodelist();
        }
        if (qName.equals(XPathConstants.NODE)) {
            return xObject.nodeset().nextNode();
        }
        throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_UNSUPPORTED_RETURN_TYPE", new Object[]{qName.toString()}));
    }

    public String evaluate(String str, Object obj) throws XPathExpressionException {
        return (String) evaluate(str, obj, XPathConstants.STRING);
    }

    public XPathExpression compile(String str) throws XPathExpressionException {
        if (str != null) {
            try {
                return new XPathExpressionImpl(new ohos.com.sun.org.apache.xpath.internal.XPath(str, null, this.prefixResolver, 0), this.prefixResolver, this.functionResolver, this.variableResolver, this.featureSecureProcessing, this.featureManager);
            } catch (TransformerException e) {
                throw new XPathExpressionException(e);
            }
        } else {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"XPath expression"}));
        }
    }

    public Object evaluate(String str, InputSource inputSource, QName qName) throws XPathExpressionException {
        if (inputSource == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"source"}));
        } else if (str == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"XPath expression"}));
        } else if (qName == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"returnType"}));
        } else if (isSupported(qName)) {
            try {
                return getResultAsType(eval(str, getParser().parse(inputSource)), qName);
            } catch (SAXException e) {
                throw new XPathExpressionException(e);
            } catch (IOException e2) {
                throw new XPathExpressionException(e2);
            } catch (TransformerException e3) {
                XPathFunctionException exception = e3.getException();
                if (exception instanceof XPathFunctionException) {
                    throw exception;
                }
                throw new XPathExpressionException(e3);
            }
        } else {
            throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_UNSUPPORTED_RETURN_TYPE", new Object[]{qName.toString()}));
        }
    }

    public String evaluate(String str, InputSource inputSource) throws XPathExpressionException {
        return (String) evaluate(str, inputSource, XPathConstants.STRING);
    }

    public void reset() {
        this.variableResolver = this.origVariableResolver;
        this.functionResolver = this.origFunctionResolver;
        this.namespaceContext = null;
    }
}
