package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.com.sun.org.apache.xml.internal.utils.XMLCharacterRecognizer;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;

public class XStringForFSB extends XString {
    static final long serialVersionUID = -1533039186550674548L;
    protected int m_hash = 0;
    int m_length;
    int m_start;
    protected String m_strCache = null;

    public XStringForFSB(FastStringBuffer fastStringBuffer, int i, int i2) {
        super(fastStringBuffer);
        this.m_start = i;
        this.m_length = i2;
        if (fastStringBuffer == null) {
            throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_FASTSTRINGBUFFER_CANNOT_BE_NULL", null));
        }
    }

    private XStringForFSB(String str) {
        super(str);
        throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_FSB_CANNOT_TAKE_STRING", null));
    }

    public FastStringBuffer fsb() {
        return (FastStringBuffer) this.m_obj;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public void appendToFsb(FastStringBuffer fastStringBuffer) {
        fastStringBuffer.append(str());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean hasString() {
        return this.m_strCache != null;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public Object object() {
        return str();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String str() {
        if (this.m_strCache == null) {
            this.m_strCache = fsb().getString(this.m_start, this.m_length);
        }
        return this.m_strCache;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public void dispatchCharactersEvents(ContentHandler contentHandler) throws SAXException {
        fsb().sendSAXcharacters(contentHandler, this.m_start, this.m_length);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public void dispatchAsComment(LexicalHandler lexicalHandler) throws SAXException {
        fsb().sendSAXComment(lexicalHandler, this.m_start, this.m_length);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int length() {
        return this.m_length;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public char charAt(int i) {
        return fsb().charAt(this.m_start + i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public void getChars(int i, int i2, char[] cArr, int i3) {
        int i4 = i2 - i;
        int i5 = this.m_length;
        if (i4 > i5) {
            i4 = i5;
        }
        if (i4 > cArr.length - i3) {
            i4 = cArr.length - i3;
        }
        int i6 = this.m_start + i + i4;
        FastStringBuffer fsb = fsb();
        int i7 = i + this.m_start;
        while (i7 < i6) {
            cArr[i3] = fsb.charAt(i7);
            i7++;
            i3++;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equals(XMLString xMLString) {
        if (this == xMLString) {
            return true;
        }
        int i = this.m_length;
        if (i != xMLString.length()) {
            return false;
        }
        FastStringBuffer fsb = fsb();
        int i2 = this.m_start;
        int i3 = 0;
        while (true) {
            int i4 = i - 1;
            if (i == 0) {
                return true;
            }
            if (fsb.charAt(i2) != xMLString.charAt(i3)) {
                return false;
            }
            i2++;
            i3++;
            i = i4;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean equals(XObject xObject) {
        if (this == xObject) {
            return true;
        }
        if (xObject.getType() == 2) {
            return xObject.equals((XObject) this);
        }
        String str = xObject.str();
        int i = this.m_length;
        if (i != str.length()) {
            return false;
        }
        FastStringBuffer fsb = fsb();
        int i2 = this.m_start;
        int i3 = 0;
        while (true) {
            int i4 = i - 1;
            if (i == 0) {
                return true;
            }
            if (fsb.charAt(i2) != str.charAt(i3)) {
                return false;
            }
            i2++;
            i3++;
            i = i4;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equals(String str) {
        int i = this.m_length;
        if (i != str.length()) {
            return false;
        }
        FastStringBuffer fsb = fsb();
        int i2 = this.m_start;
        int i3 = 0;
        while (true) {
            int i4 = i - 1;
            if (i == 0) {
                return true;
            }
            if (fsb.charAt(i2) != str.charAt(i3)) {
                return false;
            }
            i2++;
            i3++;
            i = i4;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, java.lang.Object, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof XNumber) {
            return obj.equals(this);
        }
        if (obj instanceof XNodeSet) {
            return obj.equals(this);
        }
        if (obj instanceof XStringForFSB) {
            return equals((XMLString) obj);
        }
        return equals(obj.toString());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean equalsIgnoreCase(String str) {
        if (this.m_length == str.length()) {
            return str().equalsIgnoreCase(str);
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int compareTo(XMLString xMLString) {
        int i = this.m_length;
        int length = xMLString.length();
        int min = Math.min(i, length);
        FastStringBuffer fsb = fsb();
        int i2 = this.m_start;
        int i3 = 0;
        while (true) {
            int i4 = min - 1;
            if (min == 0) {
                return i - length;
            }
            char charAt = fsb.charAt(i2);
            char charAt2 = xMLString.charAt(i3);
            if (charAt != charAt2) {
                return charAt - charAt2;
            }
            i2++;
            i3++;
            min = i4;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int compareToIgnoreCase(XMLString xMLString) {
        int i = this.m_length;
        int length = xMLString.length();
        int min = Math.min(i, length);
        FastStringBuffer fsb = fsb();
        int i2 = this.m_start;
        int i3 = 0;
        while (true) {
            int i4 = min - 1;
            if (min == 0) {
                return i - length;
            }
            char lowerCase = Character.toLowerCase(fsb.charAt(i2));
            char lowerCase2 = Character.toLowerCase(xMLString.charAt(i3));
            if (lowerCase != lowerCase2) {
                return lowerCase - lowerCase2;
            }
            i2++;
            i3++;
            min = i4;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, java.lang.Object, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int hashCode() {
        return super.hashCode();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(XMLString xMLString, int i) {
        FastStringBuffer fsb = fsb();
        int i2 = this.m_start + i;
        int i3 = this.m_length;
        int length = xMLString.length();
        if (i < 0 || i > this.m_length - length) {
            return false;
        }
        int i4 = 0;
        while (true) {
            length--;
            if (length < 0) {
                return true;
            }
            if (fsb.charAt(i2) != xMLString.charAt(i4)) {
                return false;
            }
            i2++;
            i4++;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean startsWith(XMLString xMLString) {
        return startsWith(xMLString, 0);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(int i) {
        return indexOf(i, 0);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int indexOf(int i, int i2) {
        int i3 = this.m_start + this.m_length;
        FastStringBuffer fsb = fsb();
        if (i2 < 0) {
            i2 = 0;
        } else if (i2 >= this.m_length) {
            return -1;
        }
        for (int i4 = this.m_start + i2; i4 < i3; i4++) {
            if (fsb.charAt(i4) == i) {
                return i4 - this.m_start;
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString substring(int i) {
        int i2 = this.m_length - i;
        if (i2 <= 0) {
            return XString.EMPTYSTRING;
        }
        return new XStringForFSB(fsb(), this.m_start + i, i2);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString substring(int i, int i2) {
        int i3 = i2 - i;
        int i4 = this.m_length;
        if (i3 > i4) {
            i3 = i4;
        }
        if (i3 <= 0) {
            return XString.EMPTYSTRING;
        }
        return new XStringForFSB(fsb(), this.m_start + i, i3);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString concat(String str) {
        return new XString(str().concat(str));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString trim() {
        return fixWhiteSpace(true, true, false);
    }

    private static boolean isSpace(char c) {
        return XMLCharacterRecognizer.isWhiteSpace(c);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public XMLString fixWhiteSpace(boolean z, boolean z2, boolean z3) {
        char c;
        int i = this.m_length;
        int i2 = this.m_start + i;
        char[] cArr = new char[i];
        FastStringBuffer fsb = fsb();
        int i3 = 0;
        int i4 = 0;
        boolean z4 = false;
        boolean z5 = false;
        for (int i5 = this.m_start; i5 < i2; i5++) {
            char charAt = fsb.charAt(i5);
            if (!isSpace(charAt)) {
                cArr[i4] = charAt;
                i4++;
                z4 = false;
            } else if (!z4) {
                if (' ' != charAt) {
                    z5 = true;
                }
                int i6 = i4 + 1;
                cArr[i4] = ' ';
                if (!z3 || i6 == 0 || !((c = cArr[i6 - 1]) == '.' || c == '!' || c == '?')) {
                    z4 = true;
                }
                i4 = i6;
            } else {
                z4 = true;
                z5 = true;
            }
        }
        if (z2 && 1 <= i4 && ' ' == cArr[i4 - 1]) {
            i4--;
            z5 = true;
        }
        if (z && i4 > 0 && ' ' == cArr[0]) {
            i3 = 1;
            z5 = true;
        }
        return z5 ? XMLStringFactoryImpl.getFactory().newstr(cArr, i3, i4 - i3) : this;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0031, code lost:
        if (r0.charAt(r3) == '-') goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0033, code lost:
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
        if (r3 >= r6.m_length) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
        r4 = r0.charAt(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003f, code lost:
        if (r4 == '.') goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        if (r4 < '0') goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0047, code lost:
        if (r4 <= '9') goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004b, code lost:
        if (r3 >= r6.m_length) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0055, code lost:
        if (ohos.com.sun.org.apache.xml.internal.utils.XMLCharacterRecognizer.isWhiteSpace(r0.charAt(r3)) != false) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0058, code lost:
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005d, code lost:
        if (r3 == r6.m_length) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005f, code lost:
        return Double.NaN;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0064, code lost:
        return java.lang.Double.parseDouble(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0065, code lost:
        return Double.NaN;
     */
    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public double toDouble() {
        if (this.m_length == 0) {
            return Double.NaN;
        }
        String string = fsb().getString(this.m_start, this.m_length);
        int i = 0;
        while (i < this.m_length && XMLCharacterRecognizer.isWhiteSpace(string.charAt(i))) {
            i++;
        }
        if (i == this.m_length) {
            return Double.NaN;
        }
    }
}
