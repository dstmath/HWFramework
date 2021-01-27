package ohos.com.sun.org.apache.xml.internal.utils;

import ohos.org.w3c.dom.Node;

public interface PrefixResolver {
    String getBaseIdentifier();

    String getNamespaceForPrefix(String str);

    String getNamespaceForPrefix(String str, Node node);

    boolean handlesNullPrefixes();
}
