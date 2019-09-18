package org.xml.sax;

public interface Attributes {
    int getIndex(String str);

    int getIndex(String str, String str2);

    int getLength();

    String getLocalName(int i);

    String getQName(int i);

    String getType(int i);

    String getType(String str);

    String getType(String str, String str2);

    String getURI(int i);

    String getValue(int i);

    String getValue(String str);

    String getValue(String str, String str2);
}
