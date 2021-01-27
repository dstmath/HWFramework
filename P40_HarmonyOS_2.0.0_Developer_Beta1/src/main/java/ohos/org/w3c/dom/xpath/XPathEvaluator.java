package ohos.org.w3c.dom.xpath;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;

public interface XPathEvaluator {
    XPathExpression createExpression(String str, XPathNSResolver xPathNSResolver) throws XPathException, DOMException;

    XPathNSResolver createNSResolver(Node node);

    Object evaluate(String str, Node node, XPathNSResolver xPathNSResolver, short s, Object obj) throws XPathException, DOMException;
}
