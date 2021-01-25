package ohos.com.sun.org.apache.xalan.internal.lib;

import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xpath.internal.NodeSet;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NodeList;

public class ExsltStrings extends ExsltBase {
    public static String align(String str, String str2, String str3) {
        if (str.length() >= str2.length()) {
            return str.substring(0, str2.length());
        }
        if (str3.equals("right")) {
            return str2.substring(0, str2.length() - str.length()) + str;
        } else if (str3.equals("center")) {
            int length = (str2.length() - str.length()) / 2;
            return str2.substring(0, length) + str + str2.substring(length + str.length());
        } else {
            return str + str2.substring(str.length());
        }
    }

    public static String align(String str, String str2) {
        return align(str, str2, "left");
    }

    public static String concat(NodeList nodeList) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < nodeList.getLength(); i++) {
            String exsltStrings = toString(nodeList.item(i));
            if (exsltStrings != null && exsltStrings.length() > 0) {
                stringBuffer.append(exsltStrings);
            }
        }
        return stringBuffer.toString();
    }

    public static String padding(double d, String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        int i = (int) d;
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            if (i2 == str.length()) {
                i2 = 0;
            }
            stringBuffer.append(str.charAt(i2));
            i2++;
        }
        return stringBuffer.toString();
    }

    public static String padding(double d) {
        return padding(d, " ");
    }

    public static NodeList split(String str, String str2) {
        boolean z;
        String str3;
        NodeSet nodeSet = new NodeSet();
        nodeSet.setShouldCacheNodes(true);
        boolean z2 = false;
        int i = 0;
        while (!z2 && i < str.length()) {
            int indexOf = str.indexOf(str2, i);
            if (indexOf >= 0) {
                String substring = str.substring(i, indexOf);
                int length = indexOf + str2.length();
                z = z2;
                str3 = substring;
                i = length;
            } else {
                str3 = str.substring(i);
                z = true;
            }
            Document dOMDocument = JdkXmlUtils.getDOMDocument();
            synchronized (dOMDocument) {
                Element createElement = dOMDocument.createElement(SchemaSymbols.ATTVAL_TOKEN);
                createElement.appendChild(dOMDocument.createTextNode(str3));
                nodeSet.addNode(createElement);
            }
            z2 = z;
        }
        return nodeSet;
    }

    public static NodeList split(String str) {
        return split(str, " ");
    }

    public static NodeList tokenize(String str, String str2) {
        NodeSet nodeSet = new NodeSet();
        if (str2 == null || str2.length() <= 0) {
            Document dOMDocument = JdkXmlUtils.getDOMDocument();
            synchronized (dOMDocument) {
                int i = 0;
                while (i < str.length()) {
                    Element createElement = dOMDocument.createElement(SchemaSymbols.ATTVAL_TOKEN);
                    int i2 = i + 1;
                    createElement.appendChild(dOMDocument.createTextNode(str.substring(i, i2)));
                    nodeSet.addNode(createElement);
                    i = i2;
                }
            }
        } else {
            StringTokenizer stringTokenizer = new StringTokenizer(str, str2);
            Document dOMDocument2 = JdkXmlUtils.getDOMDocument();
            synchronized (dOMDocument2) {
                while (stringTokenizer.hasMoreTokens()) {
                    Element createElement2 = dOMDocument2.createElement(SchemaSymbols.ATTVAL_TOKEN);
                    createElement2.appendChild(dOMDocument2.createTextNode(stringTokenizer.nextToken()));
                    nodeSet.addNode(createElement2);
                }
            }
        }
        return nodeSet;
    }

    public static NodeList tokenize(String str) {
        return tokenize(str, " \t\n\r");
    }
}
