package org.w3c.dom;

public interface Text extends CharacterData {
    String getWholeText();

    boolean isElementContentWhitespace();

    Text replaceWholeText(String str) throws DOMException;

    Text splitText(int i) throws DOMException;
}
