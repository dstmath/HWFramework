package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogEntry;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogException;

public class TR9401CatalogReader extends TextCatalogReader {
    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.TextCatalogReader, ohos.com.sun.org.apache.xml.internal.resolver.readers.CatalogReader
    public void readCatalog(Catalog catalog, InputStream inputStream) throws MalformedURLException, IOException {
        Vector vector;
        String str;
        this.catfile = inputStream;
        if (this.catfile != null) {
            loop0:
            while (true) {
                vector = null;
                while (true) {
                    try {
                        String nextToken = nextToken();
                        if (nextToken == null) {
                            break loop0;
                        }
                        if (this.caseSensitive) {
                            str = nextToken;
                        } else {
                            str = nextToken.toUpperCase();
                        }
                        if (str.equals("DELEGATE")) {
                            str = "DELEGATE_PUBLIC";
                        }
                        try {
                            int entryArgCount = CatalogEntry.getEntryArgCount(CatalogEntry.getEntryType(str));
                            Vector vector2 = new Vector();
                            if (vector != null) {
                                catalog.unknownEntry(vector);
                                vector = null;
                            }
                            for (int i = 0; i < entryArgCount; i++) {
                                vector2.addElement(nextToken());
                            }
                            catalog.addEntry(new CatalogEntry(str, vector2));
                        } catch (CatalogException e) {
                            if (e.getExceptionType() == 3) {
                                if (vector == null) {
                                    vector = new Vector();
                                }
                                vector.addElement(nextToken);
                            } else if (e.getExceptionType() == 2) {
                                catalog.getCatalogManager().debug.message(1, "Invalid catalog entry", nextToken);
                            } else if (e.getExceptionType() == 8) {
                                catalog.getCatalogManager().debug.message(1, e.getMessage());
                            }
                        }
                    } catch (CatalogException e2) {
                        if (e2.getExceptionType() == 8) {
                            catalog.getCatalogManager().debug.message(1, e2.getMessage());
                            return;
                        }
                        return;
                    }
                }
            }
            if (vector != null) {
                catalog.unknownEntry(vector);
            }
            this.catfile.close();
            this.catfile = null;
        }
    }
}
