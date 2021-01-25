package ohos.com.sun.org.apache.xalan.internal.lib;

import ohos.com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeIterator;
import ohos.com.sun.org.apache.xpath.internal.NodeSet;
import ohos.com.sun.org.apache.xpath.internal.axes.RTFIterator;

public class ExsltCommon {
    public static String objectType(Object obj) {
        if (obj instanceof String) {
            return "string";
        }
        if (obj instanceof Boolean) {
            return "boolean";
        }
        if (obj instanceof Number) {
            return "number";
        }
        if (obj instanceof DTMNodeIterator) {
            return ((DTMNodeIterator) obj).getDTMIterator() instanceof RTFIterator ? "RTF" : "node-set";
        }
        return "unknown";
    }

    public static NodeSet nodeSet(ExpressionContext expressionContext, Object obj) {
        return Extensions.nodeset(expressionContext, obj);
    }
}
