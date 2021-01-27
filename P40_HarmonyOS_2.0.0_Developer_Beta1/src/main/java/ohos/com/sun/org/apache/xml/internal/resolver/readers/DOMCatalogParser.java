package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.org.w3c.dom.Node;

public interface DOMCatalogParser {
    void parseCatalogEntry(Catalog catalog, Node node);
}
