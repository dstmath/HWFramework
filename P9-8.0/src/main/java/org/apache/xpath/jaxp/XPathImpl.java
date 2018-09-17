package org.apache.xpath.jaxp;

import java.io.IOException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathImpl implements XPath {
    private static Document d = null;
    private boolean featureSecureProcessing = false;
    private XPathFunctionResolver functionResolver;
    private NamespaceContext namespaceContext = null;
    private XPathFunctionResolver origFunctionResolver;
    private XPathVariableResolver origVariableResolver;
    private JAXPPrefixResolver prefixResolver;
    private XPathVariableResolver variableResolver;

    XPathImpl(XPathVariableResolver vr, XPathFunctionResolver fr) {
        this.variableResolver = vr;
        this.origVariableResolver = vr;
        this.functionResolver = fr;
        this.origFunctionResolver = fr;
    }

    XPathImpl(XPathVariableResolver vr, XPathFunctionResolver fr, boolean featureSecureProcessing) {
        this.variableResolver = vr;
        this.origVariableResolver = vr;
        this.functionResolver = fr;
        this.origFunctionResolver = fr;
        this.featureSecureProcessing = featureSecureProcessing;
    }

    public void setXPathVariableResolver(XPathVariableResolver resolver) {
        if (resolver == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"XPathVariableResolver"}));
        }
        this.variableResolver = resolver;
    }

    public XPathVariableResolver getXPathVariableResolver() {
        return this.variableResolver;
    }

    public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
        if (resolver == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"XPathFunctionResolver"}));
        }
        this.functionResolver = resolver;
    }

    public XPathFunctionResolver getXPathFunctionResolver() {
        return this.functionResolver;
    }

    public void setNamespaceContext(NamespaceContext nsContext) {
        if (nsContext == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"NamespaceContext"}));
        }
        this.namespaceContext = nsContext;
        this.prefixResolver = new JAXPPrefixResolver(nsContext);
    }

    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    private static DocumentBuilder getParser() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new Error(e.toString());
        }
    }

    private static Document getDummyDocument() {
        if (d == null) {
            d = getParser().getDOMImplementation().createDocument("http://java.sun.com/jaxp/xpath", "dummyroot", null);
        }
        return d;
    }

    private XObject eval(String expression, Object contextItem) throws TransformerException {
        XPathContext xpathSupport;
        org.apache.xpath.XPath xpath = new org.apache.xpath.XPath(expression, null, this.prefixResolver, 0);
        if (this.functionResolver != null) {
            xpathSupport = new XPathContext(new JAXPExtensionsProvider(this.functionResolver, this.featureSecureProcessing), false);
        } else {
            xpathSupport = new XPathContext(false);
        }
        xpathSupport.setVarStack(new JAXPVariableStack(this.variableResolver));
        if (contextItem instanceof Node) {
            return xpath.execute(xpathSupport, (Node) contextItem, this.prefixResolver);
        }
        return xpath.execute(xpathSupport, -1, this.prefixResolver);
    }

    public Object evaluate(String expression, Object item, QName returnType) throws XPathExpressionException {
        if (expression == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"XPath expression"}));
        } else if (returnType == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"returnType"}));
        } else if (isSupported(returnType)) {
            try {
                return getResultAsType(eval(expression, item), returnType);
            } catch (NullPointerException npe) {
                throw new XPathExpressionException(npe);
            } catch (TransformerException te) {
                Throwable nestedException = te.getException();
                if (nestedException instanceof XPathFunctionException) {
                    throw ((XPathFunctionException) nestedException);
                }
                throw new XPathExpressionException(te);
            }
        } else {
            throw new IllegalArgumentException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE, new Object[]{returnType.toString()}));
        }
    }

    private boolean isSupported(QName returnType) {
        if (returnType.equals(XPathConstants.STRING) || returnType.equals(XPathConstants.NUMBER) || returnType.equals(XPathConstants.BOOLEAN) || returnType.equals(XPathConstants.NODE) || returnType.equals(XPathConstants.NODESET)) {
            return true;
        }
        return false;
    }

    private Object getResultAsType(XObject resultObject, QName returnType) throws TransformerException {
        if (returnType.equals(XPathConstants.STRING)) {
            return resultObject.str();
        }
        if (returnType.equals(XPathConstants.NUMBER)) {
            return new Double(resultObject.num());
        }
        if (returnType.equals(XPathConstants.BOOLEAN)) {
            return new Boolean(resultObject.bool());
        }
        if (returnType.equals(XPathConstants.NODESET)) {
            return resultObject.nodelist();
        }
        if (returnType.equals(XPathConstants.NODE)) {
            return resultObject.nodeset().nextNode();
        }
        throw new IllegalArgumentException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE, new Object[]{returnType.toString()}));
    }

    public String evaluate(String expression, Object item) throws XPathExpressionException {
        return (String) evaluate(expression, item, XPathConstants.STRING);
    }

    public XPathExpression compile(String expression) throws XPathExpressionException {
        if (expression == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"XPath expression"}));
        }
        try {
            return new XPathExpressionImpl(new org.apache.xpath.XPath(expression, null, this.prefixResolver, 0), this.prefixResolver, this.functionResolver, this.variableResolver, this.featureSecureProcessing);
        } catch (TransformerException te) {
            throw new XPathExpressionException(te);
        }
    }

    public Object evaluate(String expression, InputSource source, QName returnType) throws XPathExpressionException {
        if (source == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"source"}));
        } else if (expression == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"XPath expression"}));
        } else if (returnType == null) {
            throw new NullPointerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"returnType"}));
        } else if (isSupported(returnType)) {
            try {
                return getResultAsType(eval(expression, getParser().parse(source)), returnType);
            } catch (SAXException e) {
                throw new XPathExpressionException(e);
            } catch (IOException e2) {
                throw new XPathExpressionException(e2);
            } catch (TransformerException te) {
                Throwable nestedException = te.getException();
                if (nestedException instanceof XPathFunctionException) {
                    throw ((XPathFunctionException) nestedException);
                }
                throw new XPathExpressionException(te);
            }
        } else {
            throw new IllegalArgumentException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE, new Object[]{returnType.toString()}));
        }
    }

    public String evaluate(String expression, InputSource source) throws XPathExpressionException {
        return (String) evaluate(expression, source, XPathConstants.STRING);
    }

    public void reset() {
        this.variableResolver = this.origVariableResolver;
        this.functionResolver = this.origFunctionResolver;
        this.namespaceContext = null;
    }
}
