package ohos.com.sun.org.apache.xpath.internal.jaxp;

import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

public class JAXPPrefixResolver implements PrefixResolver {
    public static final String S_XMLNAMESPACEURI = "http://www.w3.org/XML/1998/namespace";
    private NamespaceContext namespaceContext;

    @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
    public String getBaseIdentifier() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
    public boolean handlesNullPrefixes() {
        return false;
    }

    public JAXPPrefixResolver(NamespaceContext namespaceContext2) {
        this.namespaceContext = namespaceContext2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
    public String getNamespaceForPrefix(String str) {
        return this.namespaceContext.getNamespaceURI(str);
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
                NamedNodeMap attributes = node.getAttributes();
                int i = 0;
                while (true) {
                    if (i >= attributes.getLength()) {
                        break;
                    }
                    Node item = attributes.item(i);
                    String nodeName = item.getNodeName();
                    boolean startsWith = nodeName.startsWith("xmlns:");
                    if (startsWith || nodeName.equals("xmlns")) {
                        if ((startsWith ? nodeName.substring(nodeName.indexOf(58) + 1) : "").equals(str)) {
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
