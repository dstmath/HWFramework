package ohos.com.sun.org.apache.xerces.internal.xni;

import java.util.Enumeration;

public interface NamespaceContext {
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/".intern();
    public static final String XML_URI = "http://www.w3.org/XML/1998/namespace".intern();

    boolean declarePrefix(String str, String str2);

    Enumeration getAllPrefixes();

    String getDeclaredPrefixAt(int i);

    int getDeclaredPrefixCount();

    String getPrefix(String str);

    String getURI(String str);

    void popContext();

    void pushContext();

    void reset();
}
