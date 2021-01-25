package ohos.com.sun.org.apache.xpath.internal.jaxp;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.parsers.DocumentBuilder;
import ohos.javax.xml.parsers.DocumentBuilderFactory;
import ohos.javax.xml.transform.TransformerException;
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

public class XPathExpressionImpl implements XPathExpression {
    static Document d;
    static DocumentBuilder db;
    static DocumentBuilderFactory dbf;
    private final JdkXmlFeatures featureManager;
    private boolean featureSecureProcessing;
    private XPathFunctionResolver functionResolver;
    boolean overrideDefaultParser;
    private JAXPPrefixResolver prefixResolver;
    private XPathVariableResolver variableResolver;
    private XPath xpath;

    protected XPathExpressionImpl() {
        this(null, null, null, null, false, new JdkXmlFeatures(false));
    }

    protected XPathExpressionImpl(XPath xPath, JAXPPrefixResolver jAXPPrefixResolver, XPathFunctionResolver xPathFunctionResolver, XPathVariableResolver xPathVariableResolver) {
        this(xPath, jAXPPrefixResolver, xPathFunctionResolver, xPathVariableResolver, false, new JdkXmlFeatures(false));
    }

    protected XPathExpressionImpl(XPath xPath, JAXPPrefixResolver jAXPPrefixResolver, XPathFunctionResolver xPathFunctionResolver, XPathVariableResolver xPathVariableResolver, boolean z, JdkXmlFeatures jdkXmlFeatures) {
        this.featureSecureProcessing = false;
        this.xpath = xPath;
        this.prefixResolver = jAXPPrefixResolver;
        this.functionResolver = xPathFunctionResolver;
        this.variableResolver = xPathVariableResolver;
        this.featureSecureProcessing = z;
        this.featureManager = jdkXmlFeatures;
        this.overrideDefaultParser = jdkXmlFeatures.getFeature(JdkXmlFeatures.XmlFeature.JDK_OVERRIDE_PARSER);
    }

    public void setXPath(XPath xPath) {
        this.xpath = xPath;
    }

    public Object eval(Object obj, QName qName) throws TransformerException {
        return getResultAsType(eval(obj), qName);
    }

    private XObject eval(Object obj) throws TransformerException {
        XPathContext xPathContext;
        XPathFunctionResolver xPathFunctionResolver = this.functionResolver;
        if (xPathFunctionResolver != null) {
            xPathContext = new XPathContext(new JAXPExtensionsProvider(xPathFunctionResolver, this.featureSecureProcessing, this.featureManager));
        } else {
            xPathContext = new XPathContext();
        }
        xPathContext.setVarStack(new JAXPVariableStack(this.variableResolver));
        Node node = (Node) obj;
        if (node == null) {
            return this.xpath.execute(xPathContext, -1, this.prefixResolver);
        }
        return this.xpath.execute(xPathContext, node, this.prefixResolver);
    }

    public Object evaluate(Object obj, QName qName) throws XPathExpressionException {
        if (qName == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"returnType"}));
        } else if (isSupported(qName)) {
            try {
                return eval(obj, qName);
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

    public String evaluate(Object obj) throws XPathExpressionException {
        return (String) evaluate(obj, XPathConstants.STRING);
    }

    public Object evaluate(InputSource inputSource, QName qName) throws XPathExpressionException {
        if (inputSource == null || qName == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL", null));
        } else if (isSupported(qName)) {
            try {
                if (dbf == null) {
                    dbf = JdkXmlUtils.getDOMFactory(this.overrideDefaultParser);
                }
                db = dbf.newDocumentBuilder();
                return eval(db.parse(inputSource), qName);
            } catch (Exception e) {
                throw new XPathExpressionException(e);
            }
        } else {
            throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_UNSUPPORTED_RETURN_TYPE", new Object[]{qName.toString()}));
        }
    }

    public String evaluate(InputSource inputSource) throws XPathExpressionException {
        return (String) evaluate(inputSource, XPathConstants.STRING);
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
}
