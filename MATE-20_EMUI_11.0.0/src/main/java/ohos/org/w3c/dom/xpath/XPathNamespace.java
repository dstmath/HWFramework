package ohos.org.w3c.dom.xpath;

import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;

public interface XPathNamespace extends Node {
    public static final short XPATH_NAMESPACE_NODE = 13;

    Element getOwnerElement();
}
