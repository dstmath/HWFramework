package ohos.javax.xml.xpath;

import ohos.javax.xml.namespace.QName;
import ohos.org.xml.sax.InputSource;

public interface XPathExpression {
    Object evaluate(Object obj, QName qName) throws XPathExpressionException;

    Object evaluate(InputSource inputSource, QName qName) throws XPathExpressionException;

    String evaluate(Object obj) throws XPathExpressionException;

    String evaluate(InputSource inputSource) throws XPathExpressionException;
}
