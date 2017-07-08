package org.apache.xpath.jaxp;

import javax.xml.namespace.NamespaceContext;
import org.apache.xalan.templates.Constants;
import org.apache.xml.utils.PrefixResolver;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class JAXPPrefixResolver implements PrefixResolver {
    public static final String S_XMLNAMESPACEURI = "http://www.w3.org/XML/1998/namespace";
    private NamespaceContext namespaceContext;

    public JAXPPrefixResolver(NamespaceContext nsContext) {
        this.namespaceContext = nsContext;
    }

    public String getNamespaceForPrefix(String prefix) {
        return this.namespaceContext.getNamespaceURI(prefix);
    }

    public String getBaseIdentifier() {
        return null;
    }

    public boolean handlesNullPrefixes() {
        return false;
    }

    public String getNamespaceForPrefix(String prefix, Node namespaceContext) {
        String namespace = null;
        if (prefix.equals(SerializerConstants.XML_PREFIX)) {
            return S_XMLNAMESPACEURI;
        }
        for (Node parent = namespaceContext; parent != null && namespace == null; parent = parent.getParentNode()) {
            int type = parent.getNodeType();
            if (type != 1 && type != 5) {
                return namespace;
            }
            if (type == 1) {
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
        return namespace;
    }
}
