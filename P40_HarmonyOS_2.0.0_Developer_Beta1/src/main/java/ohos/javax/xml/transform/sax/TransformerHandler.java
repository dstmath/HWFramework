package ohos.javax.xml.transform.sax;

import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Transformer;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.ext.LexicalHandler;

public interface TransformerHandler extends ContentHandler, LexicalHandler, DTDHandler {
    String getSystemId();

    Transformer getTransformer();

    void setResult(Result result) throws IllegalArgumentException;

    void setSystemId(String str);
}
