package org.apache.xpath;

import javax.xml.transform.TransformerException;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.PrefixResolverDefault;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

public class XPathAPI {
    public static Node selectSingleNode(Node contextNode, String str) throws TransformerException {
        return selectSingleNode(contextNode, str, contextNode);
    }

    public static Node selectSingleNode(Node contextNode, String str, Node namespaceNode) throws TransformerException {
        return selectNodeIterator(contextNode, str, namespaceNode).nextNode();
    }

    public static NodeIterator selectNodeIterator(Node contextNode, String str) throws TransformerException {
        return selectNodeIterator(contextNode, str, contextNode);
    }

    public static NodeIterator selectNodeIterator(Node contextNode, String str, Node namespaceNode) throws TransformerException {
        return eval(contextNode, str, namespaceNode).nodeset();
    }

    public static NodeList selectNodeList(Node contextNode, String str) throws TransformerException {
        return selectNodeList(contextNode, str, contextNode);
    }

    public static NodeList selectNodeList(Node contextNode, String str, Node namespaceNode) throws TransformerException {
        return eval(contextNode, str, namespaceNode).nodelist();
    }

    public static XObject eval(Node contextNode, String str) throws TransformerException {
        return eval(contextNode, str, contextNode);
    }

    public static XObject eval(Node contextNode, String str, Node namespaceNode) throws TransformerException {
        XPathContext xpathSupport = new XPathContext(false);
        if (namespaceNode.getNodeType() == (short) 9) {
            namespaceNode = ((Document) namespaceNode).getDocumentElement();
        }
        PrefixResolver prefixResolver = new PrefixResolverDefault(namespaceNode);
        return new XPath(str, null, prefixResolver, 0, null).execute(xpathSupport, xpathSupport.getDTMHandleFromNode(contextNode), prefixResolver);
    }

    public static XObject eval(Node contextNode, String str, PrefixResolver prefixResolver) throws TransformerException {
        XPath xpath = new XPath(str, null, prefixResolver, 0, null);
        XPathContext xpathSupport = new XPathContext(false);
        return xpath.execute(xpathSupport, xpathSupport.getDTMHandleFromNode(contextNode), prefixResolver);
    }
}
