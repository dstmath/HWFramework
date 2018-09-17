package org.apache.xpath.objects;

import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.XMLCharacterRecognizer;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class XStringForFSB extends XString {
    static final long serialVersionUID = -1533039186550674548L;
    protected int m_hash = 0;
    int m_length;
    int m_start;
    protected String m_strCache = null;

    public XStringForFSB(FastStringBuffer val, int start, int length) {
        super((Object) val);
        this.m_start = start;
        this.m_length = length;
        if (val == null) {
            throw new IllegalArgumentException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_FASTSTRINGBUFFER_CANNOT_BE_NULL, null));
        }
    }

    private XStringForFSB(String val) {
        super(val);
        throw new IllegalArgumentException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_FSB_CANNOT_TAKE_STRING, null));
    }

    public FastStringBuffer fsb() {
        return (FastStringBuffer) this.m_obj;
    }

    public void appendToFsb(FastStringBuffer fsb) {
        fsb.append(str());
    }

    public boolean hasString() {
        return this.m_strCache != null;
    }

    public Object object() {
        return str();
    }

    public String str() {
        if (this.m_strCache == null) {
            this.m_strCache = fsb().getString(this.m_start, this.m_length);
        }
        return this.m_strCache;
    }

    public void dispatchCharactersEvents(ContentHandler ch) throws SAXException {
        fsb().sendSAXcharacters(ch, this.m_start, this.m_length);
    }

    public void dispatchAsComment(LexicalHandler lh) throws SAXException {
        fsb().sendSAXComment(lh, this.m_start, this.m_length);
    }

    public int length() {
        return this.m_length;
    }

    public char charAt(int index) {
        return fsb().charAt(this.m_start + index);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        int n = srcEnd - srcBegin;
        if (n > this.m_length) {
            n = this.m_length;
        }
        if (n > dst.length - dstBegin) {
            n = dst.length - dstBegin;
        }
        int end = (this.m_start + srcBegin) + n;
        int d = dstBegin;
        FastStringBuffer fsb = fsb();
        int i = srcBegin + this.m_start;
        int d2 = d;
        while (i < end) {
            d = d2 + 1;
            dst[d2] = fsb.charAt(i);
            i++;
            d2 = d;
        }
    }

    public boolean equals(XMLString obj2) {
        if (this == obj2) {
            return true;
        }
        int n = this.m_length;
        if (n != obj2.length()) {
            return false;
        }
        FastStringBuffer fsb = fsb();
        int i = this.m_start;
        int j = 0;
        while (true) {
            int n2 = n;
            n = n2 - 1;
            if (n2 == 0) {
                return true;
            }
            if (fsb.charAt(i) != obj2.charAt(j)) {
                return false;
            }
            i++;
            j++;
        }
    }

    public boolean equals(XObject obj2) {
        if (this == obj2) {
            return true;
        }
        if (obj2.getType() == 2) {
            return obj2.equals(this);
        }
        String str = obj2.str();
        int n = this.m_length;
        if (n != str.length()) {
            return false;
        }
        FastStringBuffer fsb = fsb();
        int i = this.m_start;
        int j = 0;
        while (true) {
            int n2 = n;
            n = n2 - 1;
            if (n2 == 0) {
                return true;
            }
            if (fsb.charAt(i) != str.charAt(j)) {
                return false;
            }
            i++;
            j++;
        }
    }

    public boolean equals(String anotherString) {
        int n = this.m_length;
        if (n != anotherString.length()) {
            return false;
        }
        FastStringBuffer fsb = fsb();
        int i = this.m_start;
        int j = 0;
        while (true) {
            int n2 = n;
            n = n2 - 1;
            if (n2 == 0) {
                return true;
            }
            if (fsb.charAt(i) != anotherString.charAt(j)) {
                return false;
            }
            i++;
            j++;
        }
    }

    public boolean equals(Object obj2) {
        if (obj2 == null) {
            return false;
        }
        if (obj2 instanceof XNumber) {
            return obj2.equals(this);
        }
        if (obj2 instanceof XNodeSet) {
            return obj2.equals(this);
        }
        if (obj2 instanceof XStringForFSB) {
            return equals((XMLString) obj2);
        }
        return equals(obj2.toString());
    }

    public boolean equalsIgnoreCase(String anotherString) {
        return this.m_length == anotherString.length() ? str().equalsIgnoreCase(anotherString) : false;
    }

    public int compareTo(XMLString xstr) {
        int len1 = this.m_length;
        int len2 = xstr.length();
        int n = Math.min(len1, len2);
        FastStringBuffer fsb = fsb();
        int i = this.m_start;
        int j = 0;
        while (true) {
            int n2 = n;
            n = n2 - 1;
            if (n2 == 0) {
                return len1 - len2;
            }
            char c1 = fsb.charAt(i);
            char c2 = xstr.charAt(j);
            if (c1 != c2) {
                return c1 - c2;
            }
            i++;
            j++;
        }
    }

    public int compareToIgnoreCase(XMLString xstr) {
        int len1 = this.m_length;
        int len2 = xstr.length();
        int n = Math.min(len1, len2);
        FastStringBuffer fsb = fsb();
        int i = this.m_start;
        int j = 0;
        while (true) {
            int n2 = n;
            n = n2 - 1;
            if (n2 == 0) {
                return len1 - len2;
            }
            char c1 = Character.toLowerCase(fsb.charAt(i));
            char c2 = Character.toLowerCase(xstr.charAt(j));
            if (c1 != c2) {
                return c1 - c2;
            }
            i++;
            j++;
        }
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean startsWith(XMLString prefix, int toffset) {
        FastStringBuffer fsb = fsb();
        int to = this.m_start + toffset;
        int tlim = this.m_start + this.m_length;
        int po = 0;
        int pc = prefix.length();
        if (toffset < 0 || toffset > this.m_length - pc) {
            return false;
        }
        while (true) {
            pc--;
            if (pc < 0) {
                return true;
            }
            if (fsb.charAt(to) != prefix.charAt(po)) {
                return false;
            }
            to++;
            po++;
        }
    }

    public boolean startsWith(XMLString prefix) {
        return startsWith(prefix, 0);
    }

    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }

    public int indexOf(int ch, int fromIndex) {
        int max = this.m_start + this.m_length;
        FastStringBuffer fsb = fsb();
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= this.m_length) {
            return -1;
        }
        for (int i = this.m_start + fromIndex; i < max; i++) {
            if (fsb.charAt(i) == ch) {
                return i - this.m_start;
            }
        }
        return -1;
    }

    public XMLString substring(int beginIndex) {
        int len = this.m_length - beginIndex;
        if (len <= 0) {
            return XString.EMPTYSTRING;
        }
        return new XStringForFSB(fsb(), this.m_start + beginIndex, len);
    }

    public XMLString substring(int beginIndex, int endIndex) {
        int len = endIndex - beginIndex;
        if (len > this.m_length) {
            len = this.m_length;
        }
        if (len <= 0) {
            return XString.EMPTYSTRING;
        }
        return new XStringForFSB(fsb(), this.m_start + beginIndex, len);
    }

    public XMLString concat(String str) {
        return new XString(str().concat(str));
    }

    public XMLString trim() {
        return fixWhiteSpace(true, true, false);
    }

    private static boolean isSpace(char ch) {
        return XMLCharacterRecognizer.isWhiteSpace(ch);
    }

    public XMLString fixWhiteSpace(boolean trimHead, boolean trimTail, boolean doublePunctuationSpaces) {
        int d;
        int end = this.m_length + this.m_start;
        char[] buf = new char[this.m_length];
        FastStringBuffer fsb = fsb();
        boolean edit = false;
        boolean pres = false;
        int s = this.m_start;
        int d2 = 0;
        while (s < end) {
            char c = fsb.charAt(s);
            if (!isSpace(c)) {
                d = d2 + 1;
                buf[d2] = c;
                pres = false;
            } else if (pres) {
                edit = true;
                pres = true;
                d = d2;
            } else {
                if (' ' != c) {
                    edit = true;
                }
                d = d2 + 1;
                buf[d2] = ' ';
                if (!doublePunctuationSpaces || d == 0) {
                    pres = true;
                } else {
                    char prevChar = buf[d - 1];
                    if (!(prevChar == '.' || prevChar == '!' || prevChar == '?')) {
                        pres = true;
                    }
                }
            }
            s++;
            d2 = d;
        }
        if (trimTail && 1 <= d2 && ' ' == buf[d2 - 1]) {
            edit = true;
            d = d2 - 1;
        } else {
            d = d2;
        }
        int start = 0;
        if (trimHead && d > 0 && ' ' == buf[0]) {
            edit = true;
            start = 1;
        }
        return edit ? XMLStringFactoryImpl.getFactory().newstr(buf, start, d - start) : this;
    }

    public double toDouble() {
        if (this.m_length == 0) {
            return Double.NaN;
        }
        String valueString = fsb().getString(this.m_start, this.m_length);
        int i = 0;
        while (i < this.m_length && XMLCharacterRecognizer.isWhiteSpace(valueString.charAt(i))) {
            i++;
        }
        if (i == this.m_length) {
            return Double.NaN;
        }
        if (valueString.charAt(i) == '-') {
            i++;
        }
        while (i < this.m_length) {
            char c = valueString.charAt(i);
            if (c != '.' && (c < '0' || c > '9')) {
                break;
            }
            i++;
        }
        while (i < this.m_length && XMLCharacterRecognizer.isWhiteSpace(valueString.charAt(i))) {
            i++;
        }
        if (i != this.m_length) {
            return Double.NaN;
        }
        try {
            return new Double(valueString).doubleValue();
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
