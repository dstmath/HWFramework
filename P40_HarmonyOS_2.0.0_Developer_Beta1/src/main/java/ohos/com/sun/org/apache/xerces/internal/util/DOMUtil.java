package ohos.com.sun.org.apache.xerces.internal.util;

import java.lang.reflect.Method;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.ElementImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.NodeImpl;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.ls.LSException;

public class DOMUtil {
    protected DOMUtil() {
    }

    public static void copyInto(Node node, Node node2) throws DOMException {
        Node node3;
        Document ownerDocument = node2.getOwnerDocument();
        boolean z = ownerDocument instanceof DocumentImpl;
        Node node4 = node;
        Node node5 = node2;
        Node node6 = node4;
        while (node6 != null) {
            short nodeType = node6.getNodeType();
            if (nodeType == 1) {
                node3 = ownerDocument.createElement(node6.getNodeName());
                NamedNodeMap attributes = node6.getAttributes();
                int length = attributes.getLength();
                for (int i = 0; i < length; i++) {
                    Attr item = attributes.item(i);
                    String nodeName = item.getNodeName();
                    node3.setAttribute(nodeName, item.getNodeValue());
                    if (z && !item.getSpecified()) {
                        node3.getAttributeNode(nodeName).setSpecified(false);
                    }
                }
            } else if (nodeType == 3) {
                node3 = ownerDocument.createTextNode(node6.getNodeValue());
            } else if (nodeType == 4) {
                node3 = ownerDocument.createCDATASection(node6.getNodeValue());
            } else if (nodeType == 5) {
                node3 = ownerDocument.createEntityReference(node6.getNodeName());
            } else if (nodeType == 7) {
                node3 = ownerDocument.createProcessingInstruction(node6.getNodeName(), node6.getNodeValue());
            } else if (nodeType == 8) {
                node3 = ownerDocument.createComment(node6.getNodeValue());
            } else {
                throw new IllegalArgumentException("can't copy node type, " + ((int) nodeType) + " (" + node6.getNodeName() + ')');
            }
            node5.appendChild(node3);
            if (node6.hasChildNodes()) {
                node4 = node6;
                node6 = node6.getFirstChild();
                node5 = node3;
            } else {
                node6 = node6.getNextSibling();
                while (node6 == null && node4 != node) {
                    node6 = node4.getNextSibling();
                    node4 = node4.getParentNode();
                    node5 = node5.getParentNode();
                }
            }
        }
    }

    public static Element getFirstChildElement(Node node) {
        for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() == 1) {
                return (Element) firstChild;
            }
        }
        return null;
    }

    public static Element getFirstVisibleChildElement(Node node) {
        for (Element firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() == 1 && !isHidden(firstChild)) {
                return firstChild;
            }
        }
        return null;
    }

    public static Element getFirstVisibleChildElement(Node node, Map<Node, String> map) {
        for (Element firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() == 1 && !isHidden(firstChild, map)) {
                return firstChild;
            }
        }
        return null;
    }

    public static Element getLastChildElement(Node node) {
        for (Node lastChild = node.getLastChild(); lastChild != null; lastChild = lastChild.getPreviousSibling()) {
            if (lastChild.getNodeType() == 1) {
                return (Element) lastChild;
            }
        }
        return null;
    }

    public static Element getLastVisibleChildElement(Node node) {
        for (Element lastChild = node.getLastChild(); lastChild != null; lastChild = lastChild.getPreviousSibling()) {
            if (lastChild.getNodeType() == 1 && !isHidden(lastChild)) {
                return lastChild;
            }
        }
        return null;
    }

    public static Element getLastVisibleChildElement(Node node, Map<Node, String> map) {
        for (Element lastChild = node.getLastChild(); lastChild != null; lastChild = lastChild.getPreviousSibling()) {
            if (lastChild.getNodeType() == 1 && !isHidden(lastChild, map)) {
                return lastChild;
            }
        }
        return null;
    }

    public static Element getNextSiblingElement(Node node) {
        for (Node nextSibling = node.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
            if (nextSibling.getNodeType() == 1) {
                return (Element) nextSibling;
            }
        }
        return null;
    }

    public static Element getNextVisibleSiblingElement(Node node) {
        for (Element nextSibling = node.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
            if (nextSibling.getNodeType() == 1 && !isHidden(nextSibling)) {
                return nextSibling;
            }
        }
        return null;
    }

    public static Element getNextVisibleSiblingElement(Node node, Map<Node, String> map) {
        for (Element nextSibling = node.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
            if (nextSibling.getNodeType() == 1 && !isHidden(nextSibling, map)) {
                return nextSibling;
            }
        }
        return null;
    }

    public static void setHidden(Node node) {
        if (node instanceof NodeImpl) {
            ((NodeImpl) node).setReadOnly(true, false);
        } else if (node instanceof ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl) {
            ((ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl) node).setReadOnly(true, false);
        }
    }

    public static void setHidden(Node node, Map<Node, String> map) {
        if (node instanceof NodeImpl) {
            ((NodeImpl) node).setReadOnly(true, false);
        } else {
            map.put(node, "");
        }
    }

    public static void setVisible(Node node) {
        if (node instanceof NodeImpl) {
            ((NodeImpl) node).setReadOnly(false, false);
        } else if (node instanceof ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl) {
            ((ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl) node).setReadOnly(false, false);
        }
    }

    public static void setVisible(Node node, Map<Node, String> map) {
        if (node instanceof NodeImpl) {
            ((NodeImpl) node).setReadOnly(false, false);
        } else {
            map.remove(node);
        }
    }

    public static boolean isHidden(Node node) {
        if (node instanceof NodeImpl) {
            return ((NodeImpl) node).getReadOnly();
        }
        if (node instanceof ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl) {
            return ((ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl) node).getReadOnly();
        }
        return false;
    }

    public static boolean isHidden(Node node, Map<Node, String> map) {
        if (node instanceof NodeImpl) {
            return ((NodeImpl) node).getReadOnly();
        }
        return map.containsKey(node);
    }

    public static Element getFirstChildElement(Node node, String str) {
        for (Element firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() == 1 && firstChild.getNodeName().equals(str)) {
                return firstChild;
            }
        }
        return null;
    }

    public static Element getLastChildElement(Node node, String str) {
        for (Element lastChild = node.getLastChild(); lastChild != null; lastChild = lastChild.getPreviousSibling()) {
            if (lastChild.getNodeType() == 1 && lastChild.getNodeName().equals(str)) {
                return lastChild;
            }
        }
        return null;
    }

    public static Element getNextSiblingElement(Node node, String str) {
        for (Element nextSibling = node.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
            if (nextSibling.getNodeType() == 1 && nextSibling.getNodeName().equals(str)) {
                return nextSibling;
            }
        }
        return null;
    }

    public static Element getFirstChildElementNS(Node node, String str, String str2) {
        String namespaceURI;
        for (Element firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() == 1 && (namespaceURI = firstChild.getNamespaceURI()) != null && namespaceURI.equals(str) && firstChild.getLocalName().equals(str2)) {
                return firstChild;
            }
        }
        return null;
    }

    public static Element getLastChildElementNS(Node node, String str, String str2) {
        String namespaceURI;
        for (Element lastChild = node.getLastChild(); lastChild != null; lastChild = lastChild.getPreviousSibling()) {
            if (lastChild.getNodeType() == 1 && (namespaceURI = lastChild.getNamespaceURI()) != null && namespaceURI.equals(str) && lastChild.getLocalName().equals(str2)) {
                return lastChild;
            }
        }
        return null;
    }

    public static Element getNextSiblingElementNS(Node node, String str, String str2) {
        String namespaceURI;
        for (Element nextSibling = node.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
            if (nextSibling.getNodeType() == 1 && (namespaceURI = nextSibling.getNamespaceURI()) != null && namespaceURI.equals(str) && nextSibling.getLocalName().equals(str2)) {
                return nextSibling;
            }
        }
        return null;
    }

    public static Element getFirstChildElement(Node node, String[] strArr) {
        for (Element firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() == 1) {
                for (String str : strArr) {
                    if (firstChild.getNodeName().equals(str)) {
                        return firstChild;
                    }
                }
                continue;
            }
        }
        return null;
    }

    public static Element getLastChildElement(Node node, String[] strArr) {
        for (Element lastChild = node.getLastChild(); lastChild != null; lastChild = lastChild.getPreviousSibling()) {
            if (lastChild.getNodeType() == 1) {
                for (String str : strArr) {
                    if (lastChild.getNodeName().equals(str)) {
                        return lastChild;
                    }
                }
                continue;
            }
        }
        return null;
    }

    public static Element getNextSiblingElement(Node node, String[] strArr) {
        for (Element nextSibling = node.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
            if (nextSibling.getNodeType() == 1) {
                for (String str : strArr) {
                    if (nextSibling.getNodeName().equals(str)) {
                        return nextSibling;
                    }
                }
                continue;
            }
        }
        return null;
    }

    public static Element getFirstChildElementNS(Node node, String[][] strArr) {
        for (Element firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() == 1) {
                for (int i = 0; i < strArr.length; i++) {
                    String namespaceURI = firstChild.getNamespaceURI();
                    if (namespaceURI != null && namespaceURI.equals(strArr[i][0]) && firstChild.getLocalName().equals(strArr[i][1])) {
                        return firstChild;
                    }
                }
                continue;
            }
        }
        return null;
    }

    public static Element getLastChildElementNS(Node node, String[][] strArr) {
        for (Element lastChild = node.getLastChild(); lastChild != null; lastChild = lastChild.getPreviousSibling()) {
            if (lastChild.getNodeType() == 1) {
                for (int i = 0; i < strArr.length; i++) {
                    String namespaceURI = lastChild.getNamespaceURI();
                    if (namespaceURI != null && namespaceURI.equals(strArr[i][0]) && lastChild.getLocalName().equals(strArr[i][1])) {
                        return lastChild;
                    }
                }
                continue;
            }
        }
        return null;
    }

    public static Element getNextSiblingElementNS(Node node, String[][] strArr) {
        for (Element nextSibling = node.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
            if (nextSibling.getNodeType() == 1) {
                for (int i = 0; i < strArr.length; i++) {
                    String namespaceURI = nextSibling.getNamespaceURI();
                    if (namespaceURI != null && namespaceURI.equals(strArr[i][0]) && nextSibling.getLocalName().equals(strArr[i][1])) {
                        return nextSibling;
                    }
                }
                continue;
            }
        }
        return null;
    }

    public static Element getFirstChildElement(Node node, String str, String str2, String str3) {
        for (Element firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() == 1) {
                Element element = firstChild;
                if (element.getNodeName().equals(str) && element.getAttribute(str2).equals(str3)) {
                    return element;
                }
            }
        }
        return null;
    }

    public static Element getLastChildElement(Node node, String str, String str2, String str3) {
        for (Element lastChild = node.getLastChild(); lastChild != null; lastChild = lastChild.getPreviousSibling()) {
            if (lastChild.getNodeType() == 1) {
                Element element = lastChild;
                if (element.getNodeName().equals(str) && element.getAttribute(str2).equals(str3)) {
                    return element;
                }
            }
        }
        return null;
    }

    public static Element getNextSiblingElement(Node node, String str, String str2, String str3) {
        for (Element nextSibling = node.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
            if (nextSibling.getNodeType() == 1) {
                Element element = nextSibling;
                if (element.getNodeName().equals(str) && element.getAttribute(str2).equals(str3)) {
                    return element;
                }
            }
        }
        return null;
    }

    public static String getChildText(Node node) {
        if (node == null) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            short nodeType = firstChild.getNodeType();
            if (nodeType == 3) {
                stringBuffer.append(firstChild.getNodeValue());
            } else if (nodeType == 4) {
                stringBuffer.append(getChildText(firstChild));
            }
        }
        return stringBuffer.toString();
    }

    public static String getName(Node node) {
        return node.getNodeName();
    }

    public static String getLocalName(Node node) {
        String localName = node.getLocalName();
        return localName != null ? localName : node.getNodeName();
    }

    public static Element getParent(Element element) {
        Element parentNode = element.getParentNode();
        if (parentNode instanceof Element) {
            return parentNode;
        }
        return null;
    }

    public static Document getDocument(Node node) {
        return node.getOwnerDocument();
    }

    public static Element getRoot(Document document) {
        return document.getDocumentElement();
    }

    public static Attr getAttr(Element element, String str) {
        return element.getAttributeNode(str);
    }

    public static Attr getAttrNS(Element element, String str, String str2) {
        return element.getAttributeNodeNS(str, str2);
    }

    public static Attr[] getAttrs(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        Attr[] attrArr = new Attr[attributes.getLength()];
        for (int i = 0; i < attributes.getLength(); i++) {
            attrArr[i] = (Attr) attributes.item(i);
        }
        return attrArr;
    }

    public static String getValue(Attr attr) {
        return attr.getValue();
    }

    public static String getAttrValue(Element element, String str) {
        return element.getAttribute(str);
    }

    public static String getAttrValueNS(Element element, String str, String str2) {
        return element.getAttributeNS(str, str2);
    }

    public static String getPrefix(Node node) {
        return node.getPrefix();
    }

    public static String getNamespaceURI(Node node) {
        return node.getNamespaceURI();
    }

    public static String getAnnotation(Node node) {
        if (node instanceof ElementImpl) {
            return ((ElementImpl) node).getAnnotation();
        }
        return null;
    }

    public static String getSyntheticAnnotation(Node node) {
        if (node instanceof ElementImpl) {
            return ((ElementImpl) node).getSyntheticAnnotation();
        }
        return null;
    }

    public static DOMException createDOMException(short s, Throwable th) {
        DOMException dOMException = new DOMException(s, th != null ? th.getMessage() : null);
        if (th != null && ThrowableMethods.fgThrowableMethodsAvailable) {
            try {
                ThrowableMethods.fgThrowableInitCauseMethod.invoke(dOMException, th);
            } catch (Exception unused) {
            }
        }
        return dOMException;
    }

    public static LSException createLSException(short s, Throwable th) {
        LSException lSException = new LSException(s, th != null ? th.getMessage() : null);
        if (th != null && ThrowableMethods.fgThrowableMethodsAvailable) {
            try {
                ThrowableMethods.fgThrowableInitCauseMethod.invoke(lSException, th);
            } catch (Exception unused) {
            }
        }
        return lSException;
    }

    /* access modifiers changed from: package-private */
    public static class ThrowableMethods {
        private static Method fgThrowableInitCauseMethod = null;
        private static boolean fgThrowableMethodsAvailable = false;

        private ThrowableMethods() {
        }

        static {
            try {
                fgThrowableInitCauseMethod = Throwable.class.getMethod("initCause", Throwable.class);
                fgThrowableMethodsAvailable = true;
            } catch (Exception unused) {
                fgThrowableInitCauseMethod = null;
                fgThrowableMethodsAvailable = false;
            }
        }
    }
}
