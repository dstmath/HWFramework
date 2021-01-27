package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogException;

public interface CatalogReader {
    void readCatalog(Catalog catalog, InputStream inputStream) throws IOException, CatalogException;

    void readCatalog(Catalog catalog, String str) throws MalformedURLException, IOException, CatalogException;
}
