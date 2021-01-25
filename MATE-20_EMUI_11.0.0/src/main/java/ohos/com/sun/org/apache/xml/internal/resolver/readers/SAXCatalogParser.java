package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DocumentHandler;

public interface SAXCatalogParser extends ContentHandler, DocumentHandler {
    void setCatalog(Catalog catalog);
}
