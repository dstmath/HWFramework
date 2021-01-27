package ohos.com.sun.org.apache.xalan.internal.lib;

import java.util.HashMap;
import ohos.com.sun.org.apache.xml.internal.utils.DOM2Helper;
import ohos.com.sun.org.apache.xpath.internal.NodeSet;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;

public class ExsltSets extends ExsltBase {
    public static NodeList leading(NodeList nodeList, NodeList nodeList2) {
        if (nodeList2.getLength() == 0) {
            return nodeList;
        }
        NodeSet nodeSet = new NodeSet(nodeList);
        NodeSet nodeSet2 = new NodeSet();
        Node item = nodeList2.item(0);
        if (!nodeSet.contains(item)) {
            return nodeSet2;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item2 = nodeList.item(i);
            if (DOM2Helper.isNodeAfter(item2, item) && !DOM2Helper.isNodeTheSame(item2, item)) {
                nodeSet2.addElement(item2);
            }
        }
        return nodeSet2;
    }

    public static NodeList trailing(NodeList nodeList, NodeList nodeList2) {
        if (nodeList2.getLength() == 0) {
            return nodeList;
        }
        NodeSet nodeSet = new NodeSet(nodeList);
        NodeSet nodeSet2 = new NodeSet();
        Node item = nodeList2.item(0);
        if (!nodeSet.contains(item)) {
            return nodeSet2;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item2 = nodeList.item(i);
            if (DOM2Helper.isNodeAfter(item, item2) && !DOM2Helper.isNodeTheSame(item, item2)) {
                nodeSet2.addElement(item2);
            }
        }
        return nodeSet2;
    }

    public static NodeList intersection(NodeList nodeList, NodeList nodeList2) {
        NodeSet nodeSet = new NodeSet(nodeList);
        NodeSet nodeSet2 = new NodeSet(nodeList2);
        NodeSet nodeSet3 = new NodeSet();
        nodeSet3.setShouldCacheNodes(true);
        for (int i = 0; i < nodeSet.getLength(); i++) {
            Node elementAt = nodeSet.elementAt(i);
            if (nodeSet2.contains(elementAt)) {
                nodeSet3.addElement(elementAt);
            }
        }
        return nodeSet3;
    }

    public static NodeList difference(NodeList nodeList, NodeList nodeList2) {
        NodeSet nodeSet = new NodeSet(nodeList);
        NodeSet nodeSet2 = new NodeSet(nodeList2);
        NodeSet nodeSet3 = new NodeSet();
        nodeSet3.setShouldCacheNodes(true);
        for (int i = 0; i < nodeSet.getLength(); i++) {
            Node elementAt = nodeSet.elementAt(i);
            if (!nodeSet2.contains(elementAt)) {
                nodeSet3.addElement(elementAt);
            }
        }
        return nodeSet3;
    }

    public static NodeList distinct(NodeList nodeList) {
        NodeSet nodeSet = new NodeSet();
        nodeSet.setShouldCacheNodes(true);
        HashMap hashMap = new HashMap();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String exsltSets = toString(item);
            if (exsltSets == null) {
                nodeSet.addElement(item);
            } else if (!hashMap.containsKey(exsltSets)) {
                hashMap.put(exsltSets, item);
                nodeSet.addElement(item);
            }
        }
        return nodeSet;
    }

    public static boolean hasSameNode(NodeList nodeList, NodeList nodeList2) {
        NodeSet nodeSet = new NodeSet(nodeList);
        NodeSet nodeSet2 = new NodeSet(nodeList2);
        for (int i = 0; i < nodeSet.getLength(); i++) {
            if (nodeSet2.contains(nodeSet.elementAt(i))) {
                return true;
            }
        }
        return false;
    }
}
