package ohos.com.sun.org.apache.xml.internal.utils;

import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

public class PrefixResolverDefault implements PrefixResolver {
    Node m_context;

    @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
    public String getBaseIdentifier() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
    public boolean handlesNullPrefixes() {
        return false;
    }

    public PrefixResolverDefault(Node node) {
        this.m_context = node;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
    public String getNamespaceForPrefix(String str) {
        return getNamespaceForPrefix(str, this.m_context);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
    public String getNamespaceForPrefix(String str, Node node) {
        if (str.equals("xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        String str2 = null;
        while (node != null && str2 == null) {
            short nodeType = node.getNodeType();
            if (nodeType != 1 && nodeType != 5) {
                return str2;
            }
            if (nodeType == 1) {
                String nodeName = node.getNodeName();
                if (nodeName.indexOf(str + ":") == 0) {
                    return node.getNamespaceURI();
                }
                NamedNodeMap attributes = node.getAttributes();
                int i = 0;
                while (true) {
                    if (i >= attributes.getLength()) {
                        break;
                    }
                    Node item = attributes.item(i);
                    String nodeName2 = item.getNodeName();
                    boolean startsWith = nodeName2.startsWith("xmlns:");
                    if (startsWith || nodeName2.equals("xmlns")) {
                        if ((startsWith ? nodeName2.substring(nodeName2.indexOf(58) + 1) : "").equals(str)) {
                            str2 = item.getNodeValue();
                            break;
                        }
                    }
                    i++;
                }
            }
            node = node.getParentNode();
        }
        return str2;
    }
}
