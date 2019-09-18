package javax.xml.transform.sax;

import javax.xml.transform.Result;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

public class SAXResult implements Result {
    public static final String FEATURE = "http://javax.xml.transform.sax.SAXResult/feature";
    private ContentHandler handler;
    private LexicalHandler lexhandler;
    private String systemId;

    public SAXResult() {
    }

    public SAXResult(ContentHandler handler2) {
        setHandler(handler2);
    }

    public void setHandler(ContentHandler handler2) {
        this.handler = handler2;
    }

    public ContentHandler getHandler() {
        return this.handler;
    }

    public void setLexicalHandler(LexicalHandler handler2) {
        this.lexhandler = handler2;
    }

    public LexicalHandler getLexicalHandler() {
        return this.lexhandler;
    }

    public void setSystemId(String systemId2) {
        this.systemId = systemId2;
    }

    public String getSystemId() {
        return this.systemId;
    }
}
