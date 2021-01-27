package org.ccil.cowan.tagsoup;

import java.io.IOException;
import java.io.Reader;
import org.xml.sax.SAXException;

public interface Scanner {
    void resetDocumentLocator(String str, String str2);

    void scan(Reader reader, ScanHandler scanHandler) throws IOException, SAXException;

    void startCDATA();
}
