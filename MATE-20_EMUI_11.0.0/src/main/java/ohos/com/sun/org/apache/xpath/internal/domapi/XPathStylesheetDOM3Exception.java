package ohos.com.sun.org.apache.xpath.internal.domapi;

import ohos.javax.xml.transform.SourceLocator;
import ohos.javax.xml.transform.TransformerException;

public final class XPathStylesheetDOM3Exception extends TransformerException {
    public XPathStylesheetDOM3Exception(String str, SourceLocator sourceLocator) {
        super(str, sourceLocator);
    }
}
