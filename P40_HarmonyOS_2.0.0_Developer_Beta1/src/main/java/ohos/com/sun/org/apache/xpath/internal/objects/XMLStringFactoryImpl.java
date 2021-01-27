package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;

public class XMLStringFactoryImpl extends XMLStringFactory {
    private static XMLStringFactory m_xstringfactory = new XMLStringFactoryImpl();

    public static XMLStringFactory getFactory() {
        return m_xstringfactory;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory
    public XMLString newstr(String str) {
        return new XString(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory
    public XMLString newstr(FastStringBuffer fastStringBuffer, int i, int i2) {
        return new XStringForFSB(fastStringBuffer, i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory
    public XMLString newstr(char[] cArr, int i, int i2) {
        return new XStringForChars(cArr, i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory
    public XMLString emptystr() {
        return XString.EMPTYSTRING;
    }
}
