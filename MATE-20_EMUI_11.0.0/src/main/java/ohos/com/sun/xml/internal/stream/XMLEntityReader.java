package ohos.com.sun.xml.internal.stream;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;

public abstract class XMLEntityReader implements XMLLocator {
    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public abstract int getCharacterOffset();

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public abstract String getEncoding();

    public abstract String getVersion();

    public abstract boolean isExternal();

    public abstract int peekChar() throws IOException;

    public abstract void registerListener(XMLBufferListener xMLBufferListener);

    public abstract int scanChar() throws IOException;

    public abstract int scanContent(XMLString xMLString) throws IOException;

    public abstract boolean scanData(String str, XMLStringBuffer xMLStringBuffer) throws IOException;

    public abstract int scanLiteral(int i, XMLString xMLString) throws IOException;

    public abstract String scanName() throws IOException;

    public abstract String scanNmtoken() throws IOException;

    public abstract boolean scanQName(QName qName) throws IOException;

    public abstract void setEncoding(String str) throws IOException;

    public abstract void setVersion(String str);

    public abstract boolean skipChar(int i) throws IOException;

    public abstract boolean skipSpaces() throws IOException;

    public abstract boolean skipString(String str) throws IOException;
}
