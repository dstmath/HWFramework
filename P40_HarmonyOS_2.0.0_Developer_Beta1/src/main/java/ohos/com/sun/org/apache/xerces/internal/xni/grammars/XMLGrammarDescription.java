package ohos.com.sun.org.apache.xerces.internal.xni.grammars;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;

public interface XMLGrammarDescription extends XMLResourceIdentifier {
    public static final String XML_DTD = "http://www.w3.org/TR/REC-xml";
    public static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    String getGrammarType();
}
