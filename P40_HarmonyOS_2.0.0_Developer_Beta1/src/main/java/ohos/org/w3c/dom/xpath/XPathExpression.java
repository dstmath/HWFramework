package ohos.org.w3c.dom.xpath;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;

public interface XPathExpression {
    Object evaluate(Node node, short s, Object obj) throws XPathException, DOMException;
}
