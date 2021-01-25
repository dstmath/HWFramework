package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.ext.DeclHandler;
import ohos.org.xml.sax.ext.LexicalHandler;

public interface ExtendedSAX extends ContentHandler, LexicalHandler, DTDHandler, DeclHandler {
}
