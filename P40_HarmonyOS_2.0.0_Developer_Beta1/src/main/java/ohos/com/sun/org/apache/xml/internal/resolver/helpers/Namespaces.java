package ohos.com.sun.org.apache.xml.internal.resolver.helpers;

import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;

public class Namespaces {
    public static String getPrefix(Element element) {
        String tagName = element.getTagName();
        return tagName.indexOf(58) > 0 ? tagName.substring(0, tagName.indexOf(58)) : "";
    }

    public static String getLocalName(Element element) {
        String tagName = element.getTagName();
        return tagName.indexOf(58) > 0 ? tagName.substring(tagName.indexOf(58) + 1) : tagName;
    }

    public static String getNamespaceURI(Node node, String str) {
        if (node == null || node.getNodeType() != 1) {
            return null;
        }
        if (str.equals("")) {
            Element element = (Element) node;
            if (element.hasAttribute("xmlns")) {
                return element.getAttribute("xmlns");
            }
        } else {
            String str2 = "xmlns:" + str;
            Element element2 = (Element) node;
            if (element2.hasAttribute(str2)) {
                return element2.getAttribute(str2);
            }
        }
        return getNamespaceURI(node.getParentNode(), str);
    }

    public static String getNamespaceURI(Element element) {
        return getNamespaceURI(element, getPrefix(element));
    }
}
