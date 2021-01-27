package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;

public class XStringForChars extends XString {
    static final long serialVersionUID = -2235248887220850467L;
    int m_length;
    int m_start;
    protected String m_strCache = null;

    public XStringForChars(char[] cArr, int i, int i2) {
        super(cArr);
        this.m_start = i;
        this.m_length = i2;
        if (cArr == null) {
            throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_FASTSTRINGBUFFER_CANNOT_BE_NULL", null));
        }
    }

    private XStringForChars(String str) {
        super(str);
        throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING", null));
    }

    public FastStringBuffer fsb() {
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public void appendToFsb(FastStringBuffer fastStringBuffer) {
        fastStringBuffer.append((char[]) this.m_obj, this.m_start, this.m_length);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public boolean hasString() {
        return this.m_strCache != null;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String str() {
        if (this.m_strCache == null) {
            this.m_strCache = new String((char[]) this.m_obj, this.m_start, this.m_length);
        }
        return this.m_strCache;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public Object object() {
        return str();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public void dispatchCharactersEvents(ContentHandler contentHandler) throws SAXException {
        contentHandler.characters((char[]) this.m_obj, this.m_start, this.m_length);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public void dispatchAsComment(LexicalHandler lexicalHandler) throws SAXException {
        lexicalHandler.comment((char[]) this.m_obj, this.m_start, this.m_length);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public int length() {
        return this.m_length;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public char charAt(int i) {
        return ((char[]) this.m_obj)[i + this.m_start];
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XString, ohos.com.sun.org.apache.xml.internal.utils.XMLString
    public void getChars(int i, int i2, char[] cArr, int i3) {
        System.arraycopy((char[]) this.m_obj, this.m_start + i, cArr, i3, i2);
    }
}
