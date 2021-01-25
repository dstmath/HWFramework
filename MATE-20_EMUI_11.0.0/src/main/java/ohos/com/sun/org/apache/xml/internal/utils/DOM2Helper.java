package ohos.com.sun.org.apache.xml.internal.utils;

import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

public final class DOM2Helper {
    private DOM2Helper() {
    }

    public static String getLocalNameOfNode(Node node) {
        String localName = node.getLocalName();
        return localName == null ? getLocalNameOfNodeFallback(node) : localName;
    }

    private static String getLocalNameOfNodeFallback(Node node) {
        String nodeName = node.getNodeName();
        int indexOf = nodeName.indexOf(58);
        return indexOf < 0 ? nodeName : nodeName.substring(indexOf + 1);
    }

    public static String getNamespaceOfNode(Node node) {
        return node.getNamespaceURI();
    }

    public static boolean isNodeAfter(Node node, Node node2) {
        if (node == node2 || isNodeTheSame(node, node2)) {
            return true;
        }
        Node parentOfNode = getParentOfNode(node);
        Node parentOfNode2 = getParentOfNode(node2);
        if (parentOfNode != parentOfNode2 && !isNodeTheSame(parentOfNode, parentOfNode2)) {
            int i = 2;
            int i2 = 2;
            while (parentOfNode != null) {
                i2++;
                parentOfNode = getParentOfNode(parentOfNode);
            }
            while (parentOfNode2 != null) {
                i++;
                parentOfNode2 = getParentOfNode(parentOfNode2);
            }
            if (i2 < i) {
                int i3 = i - i2;
                Node node3 = node2;
                for (int i4 = 0; i4 < i3; i4++) {
                    node3 = getParentOfNode(node3);
                }
                node2 = node3;
            } else if (i2 > i) {
                int i5 = i2 - i;
                Node node4 = node;
                for (int i6 = 0; i6 < i5; i6++) {
                    node4 = getParentOfNode(node4);
                }
                node = node4;
            }
            Node node5 = null;
            Node node6 = node;
            Node node7 = null;
            while (node6 != null) {
                if (node6 != node2 && !isNodeTheSame(node6, node2)) {
                    node6 = getParentOfNode(node6);
                    node7 = node6;
                    node5 = node2;
                    node2 = getParentOfNode(node2);
                } else if (node7 != null) {
                    return isNodeAfterSibling(node6, node7, node5);
                } else {
                    if (i2 < i) {
                        return true;
                    }
                    return false;
                }
            }
            return true;
        } else if (parentOfNode != null) {
            return isNodeAfterSibling(parentOfNode, node, node2);
        } else {
            return true;
        }
    }

    public static boolean isNodeTheSame(Node node, Node node2) {
        if (!(node instanceof DTMNodeProxy) || !(node2 instanceof DTMNodeProxy)) {
            return node == node2;
        }
        return ((DTMNodeProxy) node).equals((Node) ((DTMNodeProxy) node2));
    }

    public static Node getParentOfNode(Node node) {
        Node parentNode = node.getParentNode();
        return (parentNode == null && 2 == node.getNodeType()) ? ((Attr) node).getOwnerElement() : parentNode;
    }

    private static boolean isNodeAfterSibling(Node node, Node node2, Node node3) {
        short nodeType = node2.getNodeType();
        short nodeType2 = node3.getNodeType();
        if (2 == nodeType || 2 != nodeType2) {
            if (2 == nodeType && 2 != nodeType2) {
                return true;
            }
            if (2 == nodeType) {
                NamedNodeMap attributes = node.getAttributes();
                int length = attributes.getLength();
                boolean z = false;
                boolean z2 = false;
                for (int i = 0; i < length; i++) {
                    Node item = attributes.item(i);
                    if (node2 == item || isNodeTheSame(node2, item)) {
                        if (z) {
                            break;
                        }
                        z2 = true;
                    } else if (node3 == item || isNodeTheSame(node3, item)) {
                        if (z2) {
                            return true;
                        }
                        z = true;
                    }
                }
            } else {
                boolean z3 = false;
                boolean z4 = false;
                for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                    if (node2 == firstChild || isNodeTheSame(node2, firstChild)) {
                        if (z3) {
                            break;
                        }
                        z4 = true;
                    } else if (node3 == firstChild || isNodeTheSame(node3, firstChild)) {
                        if (z4) {
                            return true;
                        }
                        z3 = true;
                    }
                }
            }
        }
        return false;
    }
}
