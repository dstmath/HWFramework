package org.w3c.dom.ls;

import java.io.InputStream;
import java.io.Reader;

public interface LSInput {
    String getBaseURI();

    InputStream getByteStream();

    boolean getCertifiedText();

    Reader getCharacterStream();

    String getEncoding();

    String getPublicId();

    String getStringData();

    String getSystemId();

    void setBaseURI(String str);

    void setByteStream(InputStream inputStream);

    void setCertifiedText(boolean z);

    void setCharacterStream(Reader reader);

    void setEncoding(String str);

    void setPublicId(String str);

    void setStringData(String str);

    void setSystemId(String str);
}
