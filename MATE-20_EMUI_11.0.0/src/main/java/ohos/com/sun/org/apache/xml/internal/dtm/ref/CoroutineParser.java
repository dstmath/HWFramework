package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.ext.LexicalHandler;

public interface CoroutineParser {
    Object doMore(boolean z, int i);

    Object doParse(InputSource inputSource, int i);

    void doTerminate(int i);

    CoroutineManager getCoroutineManager();

    int getParserCoroutineID();

    void init(CoroutineManager coroutineManager, int i, XMLReader xMLReader);

    void setContentHandler(ContentHandler contentHandler);

    void setLexHandler(LexicalHandler lexicalHandler);
}
