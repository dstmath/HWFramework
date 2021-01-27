package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public interface XMLErrorHandler {
    void error(String str, String str2, XMLParseException xMLParseException) throws XNIException;

    void fatalError(String str, String str2, XMLParseException xMLParseException) throws XNIException;

    void warning(String str, String str2, XMLParseException xMLParseException) throws XNIException;
}
