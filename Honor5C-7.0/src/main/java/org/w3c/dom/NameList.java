package org.w3c.dom;

public interface NameList {
    boolean contains(String str);

    boolean containsNS(String str, String str2);

    int getLength();

    String getName(int i);

    String getNamespaceURI(int i);
}
