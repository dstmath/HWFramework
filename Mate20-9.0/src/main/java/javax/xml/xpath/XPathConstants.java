package javax.xml.xpath;

import javax.xml.namespace.QName;

public class XPathConstants {
    public static final QName BOOLEAN = new QName("http://www.w3.org/1999/XSL/Transform", "BOOLEAN");
    public static final String DOM_OBJECT_MODEL = "http://java.sun.com/jaxp/xpath/dom";
    public static final QName NODE = new QName("http://www.w3.org/1999/XSL/Transform", "NODE");
    public static final QName NODESET = new QName("http://www.w3.org/1999/XSL/Transform", "NODESET");
    public static final QName NUMBER = new QName("http://www.w3.org/1999/XSL/Transform", "NUMBER");
    public static final QName STRING = new QName("http://www.w3.org/1999/XSL/Transform", "STRING");

    private XPathConstants() {
    }
}
