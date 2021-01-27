package ohos.com.sun.org.apache.xalan.internal.lib;

import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import ohos.com.sun.org.apache.xpath.internal.NodeSet;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.Text;
import ohos.org.w3c.dom.traversal.NodeIterator;
import ohos.org.xml.sax.SAXNotSupportedException;

public class Extensions {
    private Extensions() {
    }

    public static NodeSet nodeset(ExpressionContext expressionContext, Object obj) {
        String str;
        if (obj instanceof NodeIterator) {
            return new NodeSet((NodeIterator) obj);
        }
        if (obj instanceof String) {
            str = (String) obj;
        } else if (obj instanceof Boolean) {
            str = new XBoolean(((Boolean) obj).booleanValue()).str();
        } else if (obj instanceof Double) {
            str = new XNumber(((Double) obj).doubleValue()).str();
        } else {
            str = obj.toString();
        }
        Document dOMDocument = JdkXmlUtils.getDOMDocument();
        Text createTextNode = dOMDocument.createTextNode(str);
        DocumentFragment createDocumentFragment = dOMDocument.createDocumentFragment();
        createDocumentFragment.appendChild(createTextNode);
        return new NodeSet((Node) createDocumentFragment);
    }

    public static NodeList intersection(NodeList nodeList, NodeList nodeList2) {
        return ExsltSets.intersection(nodeList, nodeList2);
    }

    public static NodeList difference(NodeList nodeList, NodeList nodeList2) {
        return ExsltSets.difference(nodeList, nodeList2);
    }

    public static NodeList distinct(NodeList nodeList) {
        return ExsltSets.distinct(nodeList);
    }

    public static boolean hasSameNodes(NodeList nodeList, NodeList nodeList2) {
        NodeSet nodeSet = new NodeSet(nodeList);
        NodeSet nodeSet2 = new NodeSet(nodeList2);
        if (nodeSet.getLength() != nodeSet2.getLength()) {
            return false;
        }
        for (int i = 0; i < nodeSet.getLength(); i++) {
            if (!nodeSet2.contains(nodeSet.elementAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static XObject evaluate(ExpressionContext expressionContext, String str) throws SAXNotSupportedException {
        return ExsltDynamic.evaluate(expressionContext, str);
    }

    public static NodeList tokenize(String str, String str2) {
        Document dOMDocument = JdkXmlUtils.getDOMDocument();
        StringTokenizer stringTokenizer = new StringTokenizer(str, str2);
        NodeSet nodeSet = new NodeSet();
        synchronized (dOMDocument) {
            while (stringTokenizer.hasMoreTokens()) {
                nodeSet.addNode(dOMDocument.createTextNode(stringTokenizer.nextToken()));
            }
        }
        return nodeSet;
    }

    public static NodeList tokenize(String str) {
        return tokenize(str, " \t\n\r");
    }
}
