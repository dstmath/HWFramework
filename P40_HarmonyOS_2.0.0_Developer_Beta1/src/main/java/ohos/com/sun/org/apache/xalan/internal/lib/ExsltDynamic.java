package ohos.com.sun.org.apache.xalan.internal.lib;

import ohos.com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.NodeSet;
import ohos.com.sun.org.apache.xpath.internal.NodeSetDTM;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.xml.sax.SAXNotSupportedException;

public class ExsltDynamic extends ExsltBase {
    public static final String EXSL_URI = "http://exslt.org/common";

    public static double max(ExpressionContext expressionContext, NodeList nodeList, String str) throws SAXNotSupportedException {
        if (expressionContext instanceof XPathContext.XPathExpressionContext) {
            XPathContext xPathContext = ((XPathContext.XPathExpressionContext) expressionContext).getXPathContext();
            if (str == null || str.length() == 0) {
                return Double.NaN;
            }
            NodeSetDTM nodeSetDTM = new NodeSetDTM(nodeList, xPathContext);
            xPathContext.pushContextNodeList(nodeSetDTM);
            double d = -1.7976931348623157E308d;
            for (int i = 0; i < nodeSetDTM.getLength(); i++) {
                int item = nodeSetDTM.item(i);
                xPathContext.pushCurrentNode(item);
                try {
                    double num = new XPath(str, xPathContext.getSAXLocator(), xPathContext.getNamespaceContext(), 0).execute(xPathContext, item, xPathContext.getNamespaceContext()).num();
                    xPathContext.popCurrentNode();
                    if (num > d) {
                        d = num;
                    }
                } catch (TransformerException unused) {
                    xPathContext.popCurrentNode();
                    xPathContext.popContextNodeList();
                    return Double.NaN;
                }
            }
            xPathContext.popContextNodeList();
            return d;
        }
        throw new SAXNotSupportedException(XSLMessages.createMessage("ER_INVALID_CONTEXT_PASSED", new Object[]{expressionContext}));
    }

    public static double min(ExpressionContext expressionContext, NodeList nodeList, String str) throws SAXNotSupportedException {
        if (expressionContext instanceof XPathContext.XPathExpressionContext) {
            XPathContext xPathContext = ((XPathContext.XPathExpressionContext) expressionContext).getXPathContext();
            if (str == null || str.length() == 0) {
                return Double.NaN;
            }
            NodeSetDTM nodeSetDTM = new NodeSetDTM(nodeList, xPathContext);
            xPathContext.pushContextNodeList(nodeSetDTM);
            double d = Double.MAX_VALUE;
            for (int i = 0; i < nodeList.getLength(); i++) {
                int item = nodeSetDTM.item(i);
                xPathContext.pushCurrentNode(item);
                try {
                    double num = new XPath(str, xPathContext.getSAXLocator(), xPathContext.getNamespaceContext(), 0).execute(xPathContext, item, xPathContext.getNamespaceContext()).num();
                    xPathContext.popCurrentNode();
                    if (num < d) {
                        d = num;
                    }
                } catch (TransformerException unused) {
                    xPathContext.popCurrentNode();
                    xPathContext.popContextNodeList();
                    return Double.NaN;
                }
            }
            xPathContext.popContextNodeList();
            return d;
        }
        throw new SAXNotSupportedException(XSLMessages.createMessage("ER_INVALID_CONTEXT_PASSED", new Object[]{expressionContext}));
    }

    public static double sum(ExpressionContext expressionContext, NodeList nodeList, String str) throws SAXNotSupportedException {
        if (expressionContext instanceof XPathContext.XPathExpressionContext) {
            XPathContext xPathContext = ((XPathContext.XPathExpressionContext) expressionContext).getXPathContext();
            if (str == null || str.length() == 0) {
                return Double.NaN;
            }
            NodeSetDTM nodeSetDTM = new NodeSetDTM(nodeList, xPathContext);
            xPathContext.pushContextNodeList(nodeSetDTM);
            double d = 0.0d;
            for (int i = 0; i < nodeList.getLength(); i++) {
                int item = nodeSetDTM.item(i);
                xPathContext.pushCurrentNode(item);
                try {
                    double num = new XPath(str, xPathContext.getSAXLocator(), xPathContext.getNamespaceContext(), 0).execute(xPathContext, item, xPathContext.getNamespaceContext()).num();
                    xPathContext.popCurrentNode();
                    d += num;
                } catch (TransformerException unused) {
                    xPathContext.popCurrentNode();
                    xPathContext.popContextNodeList();
                    return Double.NaN;
                }
            }
            xPathContext.popContextNodeList();
            return d;
        }
        throw new SAXNotSupportedException(XSLMessages.createMessage("ER_INVALID_CONTEXT_PASSED", new Object[]{expressionContext}));
    }

    public static NodeList map(ExpressionContext expressionContext, NodeList nodeList, String str) throws SAXNotSupportedException {
        Element element;
        if (expressionContext instanceof XPathContext.XPathExpressionContext) {
            XPathContext xPathContext = ((XPathContext.XPathExpressionContext) expressionContext).getXPathContext();
            if (str == null || str.length() == 0) {
                return new NodeSet();
            }
            NodeSetDTM nodeSetDTM = new NodeSetDTM(nodeList, xPathContext);
            xPathContext.pushContextNodeList(nodeSetDTM);
            NodeSet nodeSet = new NodeSet();
            nodeSet.setShouldCacheNodes(true);
            Document document = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                int item = nodeSetDTM.item(i);
                xPathContext.pushCurrentNode(item);
                try {
                    XObject execute = new XPath(str, xPathContext.getSAXLocator(), xPathContext.getNamespaceContext(), 0).execute(xPathContext, item, xPathContext.getNamespaceContext());
                    if (execute instanceof XNodeSet) {
                        NodeList nodelist = ((XNodeSet) execute).nodelist();
                        for (int i2 = 0; i2 < nodelist.getLength(); i2++) {
                            Node item2 = nodelist.item(i2);
                            if (!nodeSet.contains(item2)) {
                                nodeSet.addNode(item2);
                            }
                        }
                    } else {
                        if (document == null) {
                            document = JdkXmlUtils.getDOMDocument();
                        }
                        if (execute instanceof XNumber) {
                            element = document.createElementNS("http://exslt.org/common", "exsl:number");
                        } else if (execute instanceof XBoolean) {
                            element = document.createElementNS("http://exslt.org/common", "exsl:boolean");
                        } else {
                            element = document.createElementNS("http://exslt.org/common", "exsl:string");
                        }
                        element.appendChild(document.createTextNode(execute.str()));
                        nodeSet.addNode(element);
                    }
                    xPathContext.popCurrentNode();
                } catch (Exception unused) {
                    xPathContext.popCurrentNode();
                    xPathContext.popContextNodeList();
                    return new NodeSet();
                }
            }
            xPathContext.popContextNodeList();
            return nodeSet;
        }
        throw new SAXNotSupportedException(XSLMessages.createMessage("ER_INVALID_CONTEXT_PASSED", new Object[]{expressionContext}));
    }

    public static XObject evaluate(ExpressionContext expressionContext, String str) throws SAXNotSupportedException {
        if (expressionContext instanceof XPathContext.XPathExpressionContext) {
            XPathContext xPathContext = null;
            try {
                xPathContext = ((XPathContext.XPathExpressionContext) expressionContext).getXPathContext();
                return new XPath(str, xPathContext.getSAXLocator(), xPathContext.getNamespaceContext(), 0).execute(xPathContext, expressionContext.getContextNode(), xPathContext.getNamespaceContext());
            } catch (TransformerException unused) {
                return new XNodeSet(xPathContext.getDTMManager());
            }
        } else {
            throw new SAXNotSupportedException(XSLMessages.createMessage("ER_INVALID_CONTEXT_PASSED", new Object[]{expressionContext}));
        }
    }

    public static NodeList closure(ExpressionContext expressionContext, NodeList nodeList, String str) throws SAXNotSupportedException {
        if (expressionContext instanceof XPathContext.XPathExpressionContext) {
            XPathContext xPathContext = ((XPathContext.XPathExpressionContext) expressionContext).getXPathContext();
            if (str == null || str.length() == 0) {
                return new NodeSet();
            }
            NodeSet nodeSet = new NodeSet();
            nodeSet.setShouldCacheNodes(true);
            while (true) {
                NodeList nodeSet2 = new NodeSet();
                NodeSetDTM nodeSetDTM = new NodeSetDTM(nodeList, xPathContext);
                xPathContext.pushContextNodeList(nodeSetDTM);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    int item = nodeSetDTM.item(i);
                    xPathContext.pushCurrentNode(item);
                    try {
                        XObject execute = new XPath(str, xPathContext.getSAXLocator(), xPathContext.getNamespaceContext(), 0).execute(xPathContext, item, xPathContext.getNamespaceContext());
                        if (execute instanceof XNodeSet) {
                            NodeList nodelist = ((XNodeSet) execute).nodelist();
                            for (int i2 = 0; i2 < nodelist.getLength(); i2++) {
                                Node item2 = nodelist.item(i2);
                                if (!nodeSet2.contains(item2)) {
                                    nodeSet2.addNode(item2);
                                }
                            }
                            xPathContext.popCurrentNode();
                        } else {
                            xPathContext.popCurrentNode();
                            xPathContext.popContextNodeList();
                            return new NodeSet();
                        }
                    } catch (TransformerException unused) {
                        xPathContext.popCurrentNode();
                        xPathContext.popContextNodeList();
                        return new NodeSet();
                    }
                }
                xPathContext.popContextNodeList();
                for (int i3 = 0; i3 < nodeSet2.getLength(); i3++) {
                    Node item3 = nodeSet2.item(i3);
                    if (!nodeSet.contains(item3)) {
                        nodeSet.addNode(item3);
                    }
                }
                if (nodeSet2.getLength() <= 0) {
                    return nodeSet;
                }
                nodeList = nodeSet2;
            }
        } else {
            throw new SAXNotSupportedException(XSLMessages.createMessage("ER_INVALID_CONTEXT_PASSED", new Object[]{expressionContext}));
        }
    }
}
