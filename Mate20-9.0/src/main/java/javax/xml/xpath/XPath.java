package javax.xml.xpath;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.xml.sax.InputSource;

public interface XPath {
    XPathExpression compile(String str) throws XPathExpressionException;

    Object evaluate(String str, Object obj, QName qName) throws XPathExpressionException;

    Object evaluate(String str, InputSource inputSource, QName qName) throws XPathExpressionException;

    String evaluate(String str, Object obj) throws XPathExpressionException;

    String evaluate(String str, InputSource inputSource) throws XPathExpressionException;

    NamespaceContext getNamespaceContext();

    XPathFunctionResolver getXPathFunctionResolver();

    XPathVariableResolver getXPathVariableResolver();

    void reset();

    void setNamespaceContext(NamespaceContext namespaceContext);

    void setXPathFunctionResolver(XPathFunctionResolver xPathFunctionResolver);

    void setXPathVariableResolver(XPathVariableResolver xPathVariableResolver);
}
