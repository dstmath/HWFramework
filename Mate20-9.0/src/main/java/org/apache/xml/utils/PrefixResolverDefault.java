package org.apache.xml.utils;

import org.apache.xalan.templates.Constants;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class PrefixResolverDefault implements PrefixResolver {
    Node m_context;

    public PrefixResolverDefault(Node xpathExpressionContext) {
        this.m_context = xpathExpressionContext;
    }

    public String getNamespaceForPrefix(String prefix) {
        return getNamespaceForPrefix(prefix, this.m_context);
    }

    public String getNamespaceForPrefix(String prefix, Node namespaceContext) {
        String namespace = null;
        if (!prefix.equals("xml")) {
            for (Node parent = namespaceContext; parent != null && namespace == null; parent = parent.getParentNode()) {
                int nodeType = parent.getNodeType();
                int type = nodeType;
                if (nodeType != 1 && type != 5) {
                    break;
                }
                if (type == 1) {
                    String nodeName = parent.getNodeName();
                    if (nodeName.indexOf(prefix + ":") != 0) {
                        NamedNodeMap nnm = parent.getAttributes();
                        int i = 0;
                        while (true) {
                            if (i >= nnm.getLength()) {
                                break;
                            }
                            Node attr = nnm.item(i);
                            String aname = attr.getNodeName();
                            boolean isPrefix = aname.startsWith(Constants.ATTRNAME_XMLNS);
                            if (isPrefix || aname.equals("xmlns")) {
                                if ((isPrefix ? aname.substring(aname.indexOf(58) + 1) : "").equals(prefix)) {
                                    namespace = attr.getNodeValue();
                                    break;
                                }
                            }
                            i++;
                        }
                    } else {
                        return parent.getNamespaceURI();
                    }
                }
            }
        } else {
            namespace = "http://www.w3.org/XML/1998/namespace";
        }
        return namespace;
    }

    public String getBaseIdentifier() {
        return null;
    }

    public boolean handlesNullPrefixes() {
        return false;
    }
}
