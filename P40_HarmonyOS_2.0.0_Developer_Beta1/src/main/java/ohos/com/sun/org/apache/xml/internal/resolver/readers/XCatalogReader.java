package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogEntry;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogException;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.PublicId;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;

public class XCatalogReader extends SAXCatalogReader implements SAXCatalogParser {
    protected Catalog catalog = null;

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void characters(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void endDocument() throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void endElement(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void processingInstruction(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void setDocumentLocator(Locator locator) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void startDocument() throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogParser
    public void setCatalog(Catalog catalog2) {
        this.catalog = catalog2;
    }

    public Catalog getCatalog() {
        return this.catalog;
    }

    public XCatalogReader(SAXParserFactory sAXParserFactory) {
        super(sAXParserFactory);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        int i;
        Vector vector = new Vector();
        if (str2.equals("Base")) {
            Catalog catalog2 = this.catalog;
            i = Catalog.BASE;
            vector.add(attributes.getValue("HRef"));
            this.catalog.getCatalogManager().debug.message(4, "Base", attributes.getValue("HRef"));
        } else if (str2.equals("Delegate")) {
            Catalog catalog3 = this.catalog;
            i = Catalog.DELEGATE_PUBLIC;
            vector.add(attributes.getValue("PublicId"));
            vector.add(attributes.getValue("HRef"));
            this.catalog.getCatalogManager().debug.message(4, "Delegate", PublicId.normalize(attributes.getValue("PublicId")), attributes.getValue("HRef"));
        } else if (str2.equals("Extend")) {
            Catalog catalog4 = this.catalog;
            i = Catalog.CATALOG;
            vector.add(attributes.getValue("HRef"));
            this.catalog.getCatalogManager().debug.message(4, "Extend", attributes.getValue("HRef"));
        } else if (str2.equals("Map")) {
            Catalog catalog5 = this.catalog;
            i = Catalog.PUBLIC;
            vector.add(attributes.getValue("PublicId"));
            vector.add(attributes.getValue("HRef"));
            this.catalog.getCatalogManager().debug.message(4, "Map", PublicId.normalize(attributes.getValue("PublicId")), attributes.getValue("HRef"));
        } else if (str2.equals("Remap")) {
            Catalog catalog6 = this.catalog;
            i = Catalog.SYSTEM;
            vector.add(attributes.getValue("SystemId"));
            vector.add(attributes.getValue("HRef"));
            this.catalog.getCatalogManager().debug.message(4, "Remap", attributes.getValue("SystemId"), attributes.getValue("HRef"));
        } else {
            if (!str2.equals("XMLCatalog")) {
                this.catalog.getCatalogManager().debug.message(1, "Invalid catalog entry type", str2);
            }
            i = -1;
        }
        if (i >= 0) {
            try {
                this.catalog.addEntry(new CatalogEntry(i, vector));
            } catch (CatalogException e) {
                if (e.getExceptionType() == 3) {
                    this.catalog.getCatalogManager().debug.message(1, "Invalid catalog entry type", str2);
                } else if (e.getExceptionType() == 2) {
                    this.catalog.getCatalogManager().debug.message(1, "Invalid catalog entry", str2);
                }
            }
        }
    }
}
