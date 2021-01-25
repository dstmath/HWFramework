package ohos.com.sun.org.apache.xalan.internal.lib;

import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;

public abstract class ExsltBase {
    protected static String toString(Node node) {
        if (node instanceof DTMNodeProxy) {
            return ((DTMNodeProxy) node).getStringValue();
        }
        String nodeValue = node.getNodeValue();
        if (nodeValue != null) {
            return nodeValue;
        }
        NodeList childNodes = node.getChildNodes();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < childNodes.getLength(); i++) {
            stringBuffer.append(toString(childNodes.item(i)));
        }
        return stringBuffer.toString();
    }

    protected static double toNumber(Node node) {
        try {
            return Double.valueOf(toString(node)).doubleValue();
        } catch (NumberFormatException unused) {
            return Double.NaN;
        }
    }
}
