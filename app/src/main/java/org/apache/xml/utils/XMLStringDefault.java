package org.apache.xml.utils;

import java.util.Locale;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class XMLStringDefault implements XMLString {
    private String m_str;

    public XMLStringDefault(String str) {
        this.m_str = str;
    }

    public void dispatchCharactersEvents(ContentHandler ch) throws SAXException {
    }

    public void dispatchAsComment(LexicalHandler lh) throws SAXException {
    }

    public XMLString fixWhiteSpace(boolean trimHead, boolean trimTail, boolean doublePunctuationSpaces) {
        return new XMLStringDefault(this.m_str.trim());
    }

    public int length() {
        return this.m_str.length();
    }

    public char charAt(int index) {
        return this.m_str.charAt(index);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        int i = srcBegin;
        int destIndex = dstBegin;
        while (i < srcEnd) {
            int destIndex2 = destIndex + 1;
            dst[destIndex] = this.m_str.charAt(i);
            i++;
            destIndex = destIndex2;
        }
    }

    public boolean equals(String obj2) {
        return this.m_str.equals(obj2);
    }

    public boolean equals(XMLString anObject) {
        return this.m_str.equals(anObject.toString());
    }

    public boolean equals(Object anObject) {
        return this.m_str.equals(anObject);
    }

    public boolean equalsIgnoreCase(String anotherString) {
        return this.m_str.equalsIgnoreCase(anotherString);
    }

    public int compareTo(XMLString anotherString) {
        return this.m_str.compareTo(anotherString.toString());
    }

    public int compareToIgnoreCase(XMLString str) {
        return this.m_str.compareToIgnoreCase(str.toString());
    }

    public boolean startsWith(String prefix, int toffset) {
        return this.m_str.startsWith(prefix, toffset);
    }

    public boolean startsWith(XMLString prefix, int toffset) {
        return this.m_str.startsWith(prefix.toString(), toffset);
    }

    public boolean startsWith(String prefix) {
        return this.m_str.startsWith(prefix);
    }

    public boolean startsWith(XMLString prefix) {
        return this.m_str.startsWith(prefix.toString());
    }

    public boolean endsWith(String suffix) {
        return this.m_str.endsWith(suffix);
    }

    public int hashCode() {
        return this.m_str.hashCode();
    }

    public int indexOf(int ch) {
        return this.m_str.indexOf(ch);
    }

    public int indexOf(int ch, int fromIndex) {
        return this.m_str.indexOf(ch, fromIndex);
    }

    public int lastIndexOf(int ch) {
        return this.m_str.lastIndexOf(ch);
    }

    public int lastIndexOf(int ch, int fromIndex) {
        return this.m_str.lastIndexOf(ch, fromIndex);
    }

    public int indexOf(String str) {
        return this.m_str.indexOf(str);
    }

    public int indexOf(XMLString str) {
        return this.m_str.indexOf(str.toString());
    }

    public int indexOf(String str, int fromIndex) {
        return this.m_str.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str) {
        return this.m_str.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return this.m_str.lastIndexOf(str, fromIndex);
    }

    public XMLString substring(int beginIndex) {
        return new XMLStringDefault(this.m_str.substring(beginIndex));
    }

    public XMLString substring(int beginIndex, int endIndex) {
        return new XMLStringDefault(this.m_str.substring(beginIndex, endIndex));
    }

    public XMLString concat(String str) {
        return new XMLStringDefault(this.m_str.concat(str));
    }

    public XMLString toLowerCase(Locale locale) {
        return new XMLStringDefault(this.m_str.toLowerCase(locale));
    }

    public XMLString toLowerCase() {
        return new XMLStringDefault(this.m_str.toLowerCase());
    }

    public XMLString toUpperCase(Locale locale) {
        return new XMLStringDefault(this.m_str.toUpperCase(locale));
    }

    public XMLString toUpperCase() {
        return new XMLStringDefault(this.m_str.toUpperCase());
    }

    public XMLString trim() {
        return new XMLStringDefault(this.m_str.trim());
    }

    public String toString() {
        return this.m_str;
    }

    public boolean hasString() {
        return true;
    }

    public double toDouble() {
        try {
            return Double.valueOf(this.m_str).doubleValue();
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
