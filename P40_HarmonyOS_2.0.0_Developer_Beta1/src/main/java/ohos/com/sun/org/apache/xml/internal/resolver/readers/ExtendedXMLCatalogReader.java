package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogEntry;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogException;
import ohos.com.sun.org.apache.xml.internal.resolver.Resolver;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.SAXException;

public class ExtendedXMLCatalogReader extends OASISXMLCatalogReader {
    public static final String extendedNamespaceName = "http://nwalsh.com/xcatalog/1.0";

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader, ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        boolean inExtensionNamespace = inExtensionNamespace();
        super.startElement(str, str2, str3, attributes);
        Vector vector = new Vector();
        if (str != null && extendedNamespaceName.equals(str) && !inExtensionNamespace) {
            if (attributes.getValue("xml:base") != null) {
                String value = attributes.getValue("xml:base");
                int i = Catalog.BASE;
                vector.add(value);
                this.baseURIStack.push(value);
                this.debug.message(4, "xml:base", value);
                try {
                    this.catalog.addEntry(new CatalogEntry(i, vector));
                } catch (CatalogException e) {
                    if (e.getExceptionType() == 3) {
                        this.debug.message(1, "Invalid catalog entry type", str2);
                    } else if (e.getExceptionType() == 2) {
                        this.debug.message(1, "Invalid catalog entry (base)", str2);
                    }
                }
                vector = new Vector();
            } else {
                this.baseURIStack.push(this.baseURIStack.peek());
            }
            int i2 = -1;
            if (str2.equals("uriSuffix")) {
                if (checkAttributes(attributes, "suffix", Constants.ELEMNAME_URL_STRING)) {
                    i2 = Resolver.URISUFFIX;
                    vector.add(attributes.getValue("suffix"));
                    vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
                    this.debug.message(4, "uriSuffix", attributes.getValue("suffix"), attributes.getValue(Constants.ELEMNAME_URL_STRING));
                }
            } else if (!str2.equals("systemSuffix")) {
                this.debug.message(1, "Invalid catalog entry type", str2);
            } else if (checkAttributes(attributes, "suffix", Constants.ELEMNAME_URL_STRING)) {
                i2 = Resolver.SYSTEMSUFFIX;
                vector.add(attributes.getValue("suffix"));
                vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
                this.debug.message(4, "systemSuffix", attributes.getValue("suffix"), attributes.getValue(Constants.ELEMNAME_URL_STRING));
            }
            if (i2 >= 0) {
                try {
                    this.catalog.addEntry(new CatalogEntry(i2, vector));
                } catch (CatalogException e2) {
                    if (e2.getExceptionType() == 3) {
                        this.debug.message(1, "Invalid catalog entry type", str2);
                    } else if (e2.getExceptionType() == 2) {
                        this.debug.message(1, "Invalid catalog entry", str2);
                    }
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader, ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void endElement(String str, String str2, String str3) throws SAXException {
        super.endElement(str, str2, str3);
        boolean inExtensionNamespace = inExtensionNamespace();
        Vector vector = new Vector();
        if (str != null && extendedNamespaceName.equals(str) && !inExtensionNamespace) {
            String str4 = (String) this.baseURIStack.peek();
            if (!str4.equals((String) this.baseURIStack.pop())) {
                Catalog catalog = this.catalog;
                int i = Catalog.BASE;
                vector.add(str4);
                this.debug.message(4, "(reset) xml:base", str4);
                try {
                    this.catalog.addEntry(new CatalogEntry(i, vector));
                } catch (CatalogException e) {
                    if (e.getExceptionType() == 3) {
                        this.debug.message(1, "Invalid catalog entry type", str2);
                    } else if (e.getExceptionType() == 2) {
                        this.debug.message(1, "Invalid catalog entry (rbase)", str2);
                    }
                }
            }
        }
    }
}
