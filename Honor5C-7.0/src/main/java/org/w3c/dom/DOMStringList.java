package org.w3c.dom;

public interface DOMStringList {
    boolean contains(String str);

    int getLength();

    String item(int i);
}
