package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogException;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.Namespaces;
import ohos.javax.xml.parsers.DocumentBuilderFactory;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.SAXException;
import sun.reflect.misc.ReflectUtil;

public class DOMCatalogReader implements CatalogReader {
    protected Map<String, String> namespaceMap = new HashMap();

    public void setCatalogParser(String str, String str2, String str3) {
        if (str == null) {
            this.namespaceMap.put(str2, str3);
            return;
        }
        Map<String, String> map = this.namespaceMap;
        map.put("{" + str + "}" + str2, str3);
    }

    public String getCatalogParser(String str, String str2) {
        if (str == null) {
            return this.namespaceMap.get(str2);
        }
        Map<String, String> map = this.namespaceMap;
        return map.get("{" + str + "}" + str2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.CatalogReader
    public void readCatalog(Catalog catalog, InputStream inputStream) throws IOException, CatalogException {
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        newInstance.setNamespaceAware(false);
        newInstance.setValidating(false);
        try {
            try {
                Element documentElement = newInstance.newDocumentBuilder().parse(inputStream).getDocumentElement();
                String namespaceURI = Namespaces.getNamespaceURI(documentElement);
                String localName = Namespaces.getLocalName(documentElement);
                String catalogParser = getCatalogParser(namespaceURI, localName);
                if (catalogParser != null) {
                    try {
                        DOMCatalogParser dOMCatalogParser = (DOMCatalogParser) ReflectUtil.forName(catalogParser).newInstance();
                        for (Node firstChild = documentElement.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                            dOMCatalogParser.parseCatalogEntry(catalog, firstChild);
                        }
                    } catch (ClassNotFoundException unused) {
                        catalog.getCatalogManager().debug.message(1, "Cannot load XML Catalog Parser class", catalogParser);
                        throw new CatalogException(6);
                    } catch (InstantiationException unused2) {
                        catalog.getCatalogManager().debug.message(1, "Cannot instantiate XML Catalog Parser class", catalogParser);
                        throw new CatalogException(6);
                    } catch (IllegalAccessException unused3) {
                        catalog.getCatalogManager().debug.message(1, "Cannot access XML Catalog Parser class", catalogParser);
                        throw new CatalogException(6);
                    } catch (ClassCastException unused4) {
                        catalog.getCatalogManager().debug.message(1, "Cannot cast XML Catalog Parser class", catalogParser);
                        throw new CatalogException(6);
                    }
                } else if (namespaceURI == null) {
                    Debug debug = catalog.getCatalogManager().debug;
                    debug.message(1, "No Catalog parser for " + localName);
                } else {
                    Debug debug2 = catalog.getCatalogManager().debug;
                    debug2.message(1, "No Catalog parser for {" + namespaceURI + "}" + localName);
                }
            } catch (SAXException unused5) {
                throw new CatalogException(5);
            }
        } catch (ParserConfigurationException unused6) {
            throw new CatalogException(6);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.CatalogReader
    public void readCatalog(Catalog catalog, String str) throws MalformedURLException, IOException, CatalogException {
        readCatalog(catalog, new URL(str).openConnection().getInputStream());
    }
}
