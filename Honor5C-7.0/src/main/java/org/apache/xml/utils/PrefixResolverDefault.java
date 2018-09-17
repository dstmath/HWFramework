package org.apache.xml.utils;

import org.apache.xalan.templates.Constants;
import org.apache.xpath.jaxp.JAXPPrefixResolver;
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
        if (!prefix.equals(SerializerConstants.XML_PREFIX)) {
            for (Node parent = namespaceContext; parent != null && namespace == null; parent = parent.getParentNode()) {
                int type = parent.getNodeType();
                if (type != 1 && type != 5) {
                    break;
                }
                if (type == 1) {
                    if (parent.getNodeName().indexOf(prefix + ":") == 0) {
                        return parent.getNamespaceURI();
                    }
                    NamedNodeMap nnm = parent.getAttributes();
                    for (int i = 0; i < nnm.getLength(); i++) {
                        Node attr = nnm.item(i);
                        String aname = attr.getNodeName();
                        boolean isPrefix = aname.startsWith(Constants.ATTRNAME_XMLNS);
                        if (isPrefix || aname.equals(SerializerConstants.XMLNS_PREFIX)) {
                            if ((isPrefix ? aname.substring(aname.indexOf(58) + 1) : SerializerConstants.EMPTYSTRING).equals(prefix)) {
                                namespace = attr.getNodeValue();
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            namespace = JAXPPrefixResolver.S_XMLNAMESPACEURI;
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
