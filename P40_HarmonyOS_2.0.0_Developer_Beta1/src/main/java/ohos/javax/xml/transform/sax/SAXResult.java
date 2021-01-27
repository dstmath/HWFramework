package ohos.javax.xml.transform.sax;

import ohos.javax.xml.transform.Result;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.ext.LexicalHandler;

public class SAXResult implements Result {
    public static final String FEATURE = "http://ohos.javax.xml.transform.sax.SAXResult/feature";
    private ContentHandler handler;
    private LexicalHandler lexhandler;
    private String systemId;

    public SAXResult() {
    }

    public SAXResult(ContentHandler contentHandler) {
        setHandler(contentHandler);
    }

    public void setHandler(ContentHandler contentHandler) {
        this.handler = contentHandler;
    }

    public ContentHandler getHandler() {
        return this.handler;
    }

    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.lexhandler = lexicalHandler;
    }

    public LexicalHandler getLexicalHandler() {
        return this.lexhandler;
    }

    @Override // ohos.javax.xml.transform.Result
    public void setSystemId(String str) {
        this.systemId = str;
    }

    @Override // ohos.javax.xml.transform.Result
    public String getSystemId() {
        return this.systemId;
    }
}
