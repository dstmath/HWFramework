package org.apache.xml.utils;

import java.util.Locale;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public interface XMLString {
    char charAt(int i);

    int compareTo(XMLString xMLString);

    int compareToIgnoreCase(XMLString xMLString);

    XMLString concat(String str);

    void dispatchAsComment(LexicalHandler lexicalHandler) throws SAXException;

    void dispatchCharactersEvents(ContentHandler contentHandler) throws SAXException;

    boolean endsWith(String str);

    boolean equals(Object obj);

    boolean equals(String str);

    boolean equals(XMLString xMLString);

    boolean equalsIgnoreCase(String str);

    XMLString fixWhiteSpace(boolean z, boolean z2, boolean z3);

    void getChars(int i, int i2, char[] cArr, int i3);

    boolean hasString();

    int hashCode();

    int indexOf(int i);

    int indexOf(int i, int i2);

    int indexOf(String str);

    int indexOf(String str, int i);

    int indexOf(XMLString xMLString);

    int lastIndexOf(int i);

    int lastIndexOf(int i, int i2);

    int lastIndexOf(String str);

    int lastIndexOf(String str, int i);

    int length();

    boolean startsWith(String str);

    boolean startsWith(String str, int i);

    boolean startsWith(XMLString xMLString);

    boolean startsWith(XMLString xMLString, int i);

    XMLString substring(int i);

    XMLString substring(int i, int i2);

    double toDouble();

    XMLString toLowerCase();

    XMLString toLowerCase(Locale locale);

    String toString();

    XMLString toUpperCase();

    XMLString toUpperCase(Locale locale);

    XMLString trim();
}
