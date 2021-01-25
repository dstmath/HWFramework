package ohos.com.sun.org.apache.xerces.internal.xinclude;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.util.XML11Char;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

public class XInclude11TextReader extends XIncludeTextReader {
    public XInclude11TextReader(XMLInputSource xMLInputSource, XIncludeHandler xIncludeHandler, int i) throws IOException {
        super(xMLInputSource, xIncludeHandler, i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeTextReader
    public boolean isValid(int i) {
        return XML11Char.isXML11Valid(i);
    }
}
