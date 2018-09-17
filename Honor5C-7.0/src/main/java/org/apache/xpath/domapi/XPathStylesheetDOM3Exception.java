package org.apache.xpath.domapi;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

public final class XPathStylesheetDOM3Exception extends TransformerException {
    public XPathStylesheetDOM3Exception(String msg, SourceLocator arg1) {
        super(msg, arg1);
    }
}
