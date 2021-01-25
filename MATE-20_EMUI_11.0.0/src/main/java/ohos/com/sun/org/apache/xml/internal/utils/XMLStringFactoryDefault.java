package ohos.com.sun.org.apache.xml.internal.utils;

public class XMLStringFactoryDefault extends XMLStringFactory {
    private static final XMLStringDefault EMPTY_STR = new XMLStringDefault("");

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory
    public XMLString newstr(String str) {
        return new XMLStringDefault(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory
    public XMLString newstr(FastStringBuffer fastStringBuffer, int i, int i2) {
        return new XMLStringDefault(fastStringBuffer.getString(i, i2));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory
    public XMLString newstr(char[] cArr, int i, int i2) {
        return new XMLStringDefault(new String(cArr, i, i2));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory
    public XMLString emptystr() {
        return EMPTY_STR;
    }
}
