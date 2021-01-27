package ohos.com.sun.org.apache.xpath.internal;

import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.traversal.NodeIterator;

public class CachedXPathAPI {
    protected XPathContext xpathSupport;

    public CachedXPathAPI() {
        this.xpathSupport = new XPathContext(JdkXmlUtils.OVERRIDE_PARSER_DEFAULT);
    }

    public CachedXPathAPI(CachedXPathAPI cachedXPathAPI) {
        this.xpathSupport = cachedXPathAPI.xpathSupport;
    }

    public XPathContext getXPathContext() {
        return this.xpathSupport;
    }

    public Node selectSingleNode(Node node, String str) throws TransformerException {
        return selectSingleNode(node, str, node);
    }

    public Node selectSingleNode(Node node, String str, Node node2) throws TransformerException {
        return selectNodeIterator(node, str, node2).nextNode();
    }

    public NodeIterator selectNodeIterator(Node node, String str) throws TransformerException {
        return selectNodeIterator(node, str, node);
    }

    public NodeIterator selectNodeIterator(Node node, String str, Node node2) throws TransformerException {
        return eval(node, str, node2).nodeset();
    }

    public NodeList selectNodeList(Node node, String str) throws TransformerException {
        return selectNodeList(node, str, node);
    }

    public NodeList selectNodeList(Node node, String str, Node node2) throws TransformerException {
        return eval(node, str, node2).nodelist();
    }

    public XObject eval(Node node, String str) throws TransformerException {
        return eval(node, str, node);
    }

    public XObject eval(Node node, String str, Node node2) throws TransformerException {
        if (node2.getNodeType() == 9) {
            node2 = ((Document) node2).getDocumentElement();
        }
        PrefixResolverDefault prefixResolverDefault = new PrefixResolverDefault(node2);
        return new XPath(str, null, prefixResolverDefault, 0, null).execute(this.xpathSupport, this.xpathSupport.getDTMHandleFromNode(node), prefixResolverDefault);
    }

    public XObject eval(Node node, String str, PrefixResolver prefixResolver) throws TransformerException {
        XPath xPath = new XPath(str, null, prefixResolver, 0, null);
        XPathContext xPathContext = new XPathContext(JdkXmlUtils.OVERRIDE_PARSER_DEFAULT);
        return xPath.execute(xPathContext, xPathContext.getDTMHandleFromNode(node), prefixResolver);
    }
}
