package ohos.com.sun.org.apache.xpath.internal.objects;

import java.util.Locale;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xml.internal.utils.XMLCharacterRecognizer;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;

public class XString extends XObject implements XMLString {
    public static final XString EMPTYSTRING = new XString("");
    static final long serialVersionUID = 2020470518395094525L;

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int getType() {
        return 3;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String getTypeString() {
        return "#STRING";
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean hasString() {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public XMLString xstr() {
        return this;
    }

    protected XString(Object obj) {
        super(obj);
    }

    public XString(String str) {
        super(str);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public double num() {
        return toDouble();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public double toDouble() {
        XMLString trim = trim();
        for (int i = 0; i < trim.length(); i++) {
            char charAt = trim.charAt(i);
            if (!(charAt == '-' || charAt == '.' || (charAt >= '0' && charAt <= '9'))) {
                return Double.NaN;
            }
        }
        try {
            return Double.parseDouble(trim.toString());
        } catch (NumberFormatException unused) {
            return Double.NaN;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean bool() {
        return str().length() > 0;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String str() {
        return this.m_obj != null ? (String) this.m_obj : "";
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int rtf(XPathContext xPathContext) {
        DTM createDocumentFragment = xPathContext.createDocumentFragment();
        createDocumentFragment.appendTextChild(str());
        return createDocumentFragment.getDocument();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public void dispatchCharactersEvents(ContentHandler contentHandler) throws SAXException {
        String str = str();
        contentHandler.characters(str.toCharArray(), 0, str.length());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public void dispatchAsComment(LexicalHandler lexicalHandler) throws SAXException {
        String str = str();
        lexicalHandler.comment(str.toCharArray(), 0, str.length());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int length() {
        return str().length();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public char charAt(int i) {
        return str().charAt(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public void getChars(int i, int i2, char[] cArr, int i3) {
        str().getChars(i, i2, cArr, i3);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean equals(XObject xObject) {
        int type = xObject.getType();
        if (4 == type) {
            try {
                return xObject.equals((XObject) this);
            } catch (TransformerException e) {
                throw new WrappedRuntimeException(e);
            }
        } else if (1 == type) {
            return xObject.bool() == bool();
        } else {
            if (2 == type) {
                return xObject.num() == num();
            }
            return xstr().equals(xObject.xstr());
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equals(String str) {
        return str().equals(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equals(XMLString xMLString) {
        if (xMLString == null) {
            return false;
        }
        if (!xMLString.hasString()) {
            return xMLString.equals(str());
        }
        return str().equals(xMLString.toString());
    }

    @Override // java.lang.Object, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof XNodeSet) {
            return obj.equals(this);
        }
        if (obj instanceof XNumber) {
            return obj.equals(this);
        }
        return str().equals(obj.toString());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equalsIgnoreCase(String str) {
        return str().equalsIgnoreCase(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int compareTo(XMLString xMLString) {
        int length = length();
        int length2 = xMLString.length();
        int min = Math.min(length, length2);
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = min - 1;
            if (min == 0) {
                return length - length2;
            }
            char charAt = charAt(i);
            char charAt2 = xMLString.charAt(i2);
            if (charAt != charAt2) {
                return charAt - charAt2;
            }
            i++;
            i2++;
            min = i3;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int compareToIgnoreCase(XMLString xMLString) {
        throw new WrappedRuntimeException(new NoSuchMethodException("Java 1.2 method, not yet implemented"));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(String str, int i) {
        return str().startsWith(str, i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(String str) {
        return startsWith(str, 0);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(XMLString xMLString, int i) {
        int length = length();
        int length2 = xMLString.length();
        if (i < 0 || i > length - length2) {
            return false;
        }
        int i2 = 0;
        while (true) {
            length2--;
            if (length2 < 0) {
                return true;
            }
            if (charAt(i) != xMLString.charAt(i2)) {
                return false;
            }
            i++;
            i2++;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(XMLString xMLString) {
        return startsWith(xMLString, 0);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean endsWith(String str) {
        return str().endsWith(str);
    }

    @Override // java.lang.Object, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int hashCode() {
        return str().hashCode();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(int i) {
        return str().indexOf(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(int i, int i2) {
        return str().indexOf(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int lastIndexOf(int i) {
        return str().lastIndexOf(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int lastIndexOf(int i, int i2) {
        return str().lastIndexOf(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(String str) {
        return str().indexOf(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(XMLString xMLString) {
        return str().indexOf(xMLString.toString());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(String str, int i) {
        return str().indexOf(str, i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int lastIndexOf(String str) {
        return str().lastIndexOf(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int lastIndexOf(String str, int i) {
        return str().lastIndexOf(str, i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString substring(int i) {
        return new XString(str().substring(i));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString substring(int i, int i2) {
        return new XString(str().substring(i, i2));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString concat(String str) {
        return new XString(str().concat(str));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString toLowerCase(Locale locale) {
        return new XString(str().toLowerCase(locale));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString toLowerCase() {
        return new XString(str().toLowerCase());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString toUpperCase(Locale locale) {
        return new XString(str().toUpperCase(locale));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString toUpperCase() {
        return new XString(str().toUpperCase());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString trim() {
        return new XString(str().trim());
    }

    private static boolean isSpace(char c) {
        return XMLCharacterRecognizer.isWhiteSpace(c);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString fixWhiteSpace(boolean z, boolean z2, boolean z3) {
        char c;
        int length = length();
        char[] cArr = new char[length];
        int i = 0;
        getChars(0, length, cArr, 0);
        int i2 = 0;
        while (i2 < length && !isSpace(cArr[i2])) {
            i2++;
        }
        boolean z4 = false;
        boolean z5 = false;
        int i3 = i2;
        while (i2 < length) {
            char c2 = cArr[i2];
            if (!isSpace(c2)) {
                cArr[i3] = c2;
                i3++;
                z4 = false;
            } else if (!z4) {
                if (' ' != c2) {
                    z5 = true;
                }
                int i4 = i3 + 1;
                cArr[i3] = ' ';
                if (!z3 || i2 == 0 || !((c = cArr[i2 - 1]) == '.' || c == '!' || c == '?')) {
                    z4 = true;
                }
                i3 = i4;
            } else {
                z4 = true;
                z5 = true;
            }
            i2++;
        }
        if (z2 && 1 <= i3 && ' ' == cArr[i3 - 1]) {
            i3--;
            z5 = true;
        }
        if (z && i3 > 0 && ' ' == cArr[0]) {
            i = 1;
            z5 = true;
        }
        return z5 ? XMLStringFactoryImpl.getFactory().newstr(new String(cArr, i, i3 - i)) : this;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        xPathVisitor.visitStringLiteral(expressionOwner, this);
    }
}
