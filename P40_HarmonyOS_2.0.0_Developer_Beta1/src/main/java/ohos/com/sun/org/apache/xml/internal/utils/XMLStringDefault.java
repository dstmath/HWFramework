package ohos.com.sun.org.apache.xml.internal.utils;

import java.util.Locale;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;

public class XMLStringDefault implements XMLString {
    private String m_str;

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public void dispatchAsComment(LexicalHandler lexicalHandler) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public void dispatchCharactersEvents(ContentHandler contentHandler) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean hasString() {
        return true;
    }

    public XMLStringDefault(String str) {
        this.m_str = str;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString fixWhiteSpace(boolean z, boolean z2, boolean z3) {
        return new XMLStringDefault(this.m_str.trim());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int length() {
        return this.m_str.length();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public char charAt(int i) {
        return this.m_str.charAt(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public void getChars(int i, int i2, char[] cArr, int i3) {
        while (i < i2) {
            cArr[i3] = this.m_str.charAt(i);
            i++;
            i3++;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equals(String str) {
        return this.m_str.equals(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equals(XMLString xMLString) {
        return this.m_str.equals(xMLString.toString());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equals(Object obj) {
        return this.m_str.equals(obj);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equalsIgnoreCase(String str) {
        return this.m_str.equalsIgnoreCase(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int compareTo(XMLString xMLString) {
        return this.m_str.compareTo(xMLString.toString());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int compareToIgnoreCase(XMLString xMLString) {
        return this.m_str.compareToIgnoreCase(xMLString.toString());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(String str, int i) {
        return this.m_str.startsWith(str, i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(XMLString xMLString, int i) {
        return this.m_str.startsWith(xMLString.toString(), i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(String str) {
        return this.m_str.startsWith(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(XMLString xMLString) {
        return this.m_str.startsWith(xMLString.toString());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean endsWith(String str) {
        return this.m_str.endsWith(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int hashCode() {
        return this.m_str.hashCode();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(int i) {
        return this.m_str.indexOf(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(int i, int i2) {
        return this.m_str.indexOf(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int lastIndexOf(int i) {
        return this.m_str.lastIndexOf(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int lastIndexOf(int i, int i2) {
        return this.m_str.lastIndexOf(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(String str) {
        return this.m_str.indexOf(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(XMLString xMLString) {
        return this.m_str.indexOf(xMLString.toString());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(String str, int i) {
        return this.m_str.indexOf(str, i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int lastIndexOf(String str) {
        return this.m_str.lastIndexOf(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int lastIndexOf(String str, int i) {
        return this.m_str.lastIndexOf(str, i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString substring(int i) {
        return new XMLStringDefault(this.m_str.substring(i));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString substring(int i, int i2) {
        return new XMLStringDefault(this.m_str.substring(i, i2));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString concat(String str) {
        return new XMLStringDefault(this.m_str.concat(str));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString toLowerCase(Locale locale) {
        return new XMLStringDefault(this.m_str.toLowerCase(locale));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString toLowerCase() {
        return new XMLStringDefault(this.m_str.toLowerCase());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString toUpperCase(Locale locale) {
        return new XMLStringDefault(this.m_str.toUpperCase(locale));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString toUpperCase() {
        return new XMLStringDefault(this.m_str.toUpperCase());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString trim() {
        return new XMLStringDefault(this.m_str.trim());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public String toString() {
        return this.m_str;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public double toDouble() {
        try {
            return Double.valueOf(this.m_str).doubleValue();
        } catch (NumberFormatException unused) {
            return Double.NaN;
        }
    }
}
