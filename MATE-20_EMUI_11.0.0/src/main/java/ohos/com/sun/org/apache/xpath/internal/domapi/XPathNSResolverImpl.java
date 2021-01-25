package ohos.com.sun.org.apache.xpath.internal.domapi;

import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.xpath.XPathNSResolver;

class XPathNSResolverImpl extends PrefixResolverDefault implements XPathNSResolver {
    public XPathNSResolverImpl(Node node) {
        super(node);
    }

    public String lookupNamespaceURI(String str) {
        return super.getNamespaceForPrefix(str);
    }
}
