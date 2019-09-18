package org.apache.xpath.jaxp;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.apache.xalan.res.XSLMessages;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class XPathExpressionImpl implements XPathExpression {
    static Document d = null;
    static DocumentBuilder db = null;
    static DocumentBuilderFactory dbf = null;
    private boolean featureSecureProcessing = false;
    private XPathFunctionResolver functionResolver;
    private JAXPPrefixResolver prefixResolver;
    private XPathVariableResolver variableResolver;
    private XPath xpath;

    protected XPathExpressionImpl() {
    }

    protected XPathExpressionImpl(XPath xpath2, JAXPPrefixResolver prefixResolver2, XPathFunctionResolver functionResolver2, XPathVariableResolver variableResolver2) {
        this.xpath = xpath2;
        this.prefixResolver = prefixResolver2;
        this.functionResolver = functionResolver2;
        this.variableResolver = variableResolver2;
        this.featureSecureProcessing = false;
    }

    protected XPathExpressionImpl(XPath xpath2, JAXPPrefixResolver prefixResolver2, XPathFunctionResolver functionResolver2, XPathVariableResolver variableResolver2, boolean featureSecureProcessing2) {
        this.xpath = xpath2;
        this.prefixResolver = prefixResolver2;
        this.functionResolver = functionResolver2;
        this.variableResolver = variableResolver2;
        this.featureSecureProcessing = featureSecureProcessing2;
    }

    public void setXPath(XPath xpath2) {
        this.xpath = xpath2;
    }

    public Object eval(Object item, QName returnType) throws TransformerException {
        return getResultAsType(eval(item), returnType);
    }

    private XObject eval(Object contextItem) throws TransformerException {
        XPathContext xpathSupport;
        if (this.functionResolver != null) {
            xpathSupport = new XPathContext(new JAXPExtensionsProvider(this.functionResolver, this.featureSecureProcessing), false);
        } else {
            xpathSupport = new XPathContext(false);
        }
        xpathSupport.setVarStack(new JAXPVariableStack(this.variableResolver));
        Node contextNode = (Node) contextItem;
        if (contextNode == null) {
            contextNode = getDummyDocument();
        }
        return this.xpath.execute(xpathSupport, contextNode, (PrefixResolver) this.prefixResolver);
    }

    public Object evaluate(Object item, QName returnType) throws XPathExpressionException {
        if (returnType == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"returnType"}));
        } else if (isSupported(returnType)) {
            try {
                return eval(item, returnType);
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
            throw new IllegalArgumentException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE, new Object[]{returnType.toString()}));
        }
    }

    public String evaluate(Object item) throws XPathExpressionException {
        return (String) evaluate(item, XPathConstants.STRING);
    }

    public Object evaluate(InputSource source, QName returnType) throws XPathExpressionException {
        if (source == null || returnType == null) {
            throw new NullPointerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL, null));
        } else if (isSupported(returnType)) {
            try {
                if (dbf == null) {
                    dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    dbf.setValidating(false);
                }
                db = dbf.newDocumentBuilder();
                return eval(db.parse(source), returnType);
            } catch (Exception e) {
                throw new XPathExpressionException(e);
            }
        } else {
            throw new IllegalArgumentException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE, new Object[]{returnType.toString()}));
        }
    }

    public String evaluate(InputSource source) throws XPathExpressionException {
        return (String) evaluate(source, XPathConstants.STRING);
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
        throw new IllegalArgumentException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE, new Object[]{returnType.toString()}));
    }

    private static Document getDummyDocument() {
        try {
            if (dbf == null) {
                dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                dbf.setValidating(false);
            }
            db = dbf.newDocumentBuilder();
            d = db.getDOMImplementation().createDocument("http://java.sun.com/jaxp/xpath", "dummyroot", null);
            return d;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
