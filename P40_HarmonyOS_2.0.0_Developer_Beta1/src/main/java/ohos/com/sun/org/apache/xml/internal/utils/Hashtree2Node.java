package ohos.com.sun.org.apache.xml.internal.utils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;

public abstract class Hashtree2Node {
    public static void appendHashToNode(Hashtable hashtable, String str, Node node, Document document) {
        if (node != null && document != null && hashtable != null) {
            if (str == null || "".equals(str)) {
                str = "appendHashToNode";
            }
            try {
                Element createElement = document.createElement(str);
                node.appendChild(createElement);
                Enumeration keys = hashtable.keys();
                Vector vector = new Vector();
                while (keys.hasMoreElements()) {
                    Object nextElement = keys.nextElement();
                    String obj = nextElement.toString();
                    Object obj2 = hashtable.get(nextElement);
                    if (obj2 instanceof Hashtable) {
                        vector.addElement(obj);
                        vector.addElement((Hashtable) obj2);
                    } else {
                        try {
                            Element createElement2 = document.createElement("item");
                            createElement2.setAttribute("key", obj);
                            createElement2.appendChild(document.createTextNode((String) obj2));
                            createElement.appendChild(createElement2);
                        } catch (Exception e) {
                            Element createElement3 = document.createElement("item");
                            createElement3.setAttribute("key", obj);
                            createElement3.appendChild(document.createTextNode("ERROR: Reading " + nextElement + " threw: " + e.toString()));
                            createElement.appendChild(createElement3);
                        }
                    }
                }
                Enumeration elements = vector.elements();
                while (elements.hasMoreElements()) {
                    appendHashToNode((Hashtable) elements.nextElement(), (String) elements.nextElement(), createElement, document);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
