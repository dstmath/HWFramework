package org.apache.xml.serializer;

interface SerializerConstants {
    public static final String CDATA_CONTINUE = "]]]]><![CDATA[>";
    public static final String CDATA_DELIMITER_CLOSE = "]]>";
    public static final String CDATA_DELIMITER_OPEN = "<![CDATA[";
    public static final String DEFAULT_SAX_SERIALIZER = (SerializerBase.PKG_NAME + ".ToXMLSAXHandler");
    public static final String EMPTYSTRING = "";
    public static final String ENTITY_AMP = "&amp;";
    public static final String ENTITY_CRLF = "&#xA;";
    public static final String ENTITY_GT = "&gt;";
    public static final String ENTITY_LT = "&lt;";
    public static final String ENTITY_QUOT = "&quot;";
    public static final String XMLNS_PREFIX = "xmlns";
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    public static final String XMLVERSION10 = "1.0";
    public static final String XMLVERSION11 = "1.1";
    public static final String XML_PREFIX = "xml";
}
