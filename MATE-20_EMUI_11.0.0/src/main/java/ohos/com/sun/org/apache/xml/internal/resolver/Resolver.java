package ohos.com.sun.org.apache.xml.internal.resolver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader;
import ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader;
import ohos.com.sun.org.apache.xml.internal.resolver.readers.TR9401CatalogReader;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.jdk.xml.internal.JdkXmlUtils;

public class Resolver extends Catalog {
    public static final int RESOLVER = CatalogEntry.addEntryType("RESOLVER", 1);
    public static final int SYSTEMREVERSE = CatalogEntry.addEntryType("SYSTEMREVERSE", 1);
    public static final int SYSTEMSUFFIX = CatalogEntry.addEntryType("SYSTEMSUFFIX", 2);
    public static final int URISUFFIX = CatalogEntry.addEntryType("URISUFFIX", 2);

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.Catalog
    public void setupReaders() {
        SAXParserFactory sAXFactory = JdkXmlUtils.getSAXFactory(this.catalogManager.overrideDefaultParser());
        sAXFactory.setValidating(false);
        SAXCatalogReader sAXCatalogReader = new SAXCatalogReader(sAXFactory);
        sAXCatalogReader.setCatalogParser(null, "XMLCatalog", "ohos.com.sun.org.apache.xml.internal.resolver.readers.XCatalogReader");
        sAXCatalogReader.setCatalogParser(OASISXMLCatalogReader.namespaceName, "catalog", "ohos.com.sun.org.apache.xml.internal.resolver.readers.ExtendedXMLCatalogReader");
        addReader("application/xml", sAXCatalogReader);
        addReader("text/plain", new TR9401CatalogReader());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.Catalog
    public void addEntry(CatalogEntry catalogEntry) {
        int entryType = catalogEntry.getEntryType();
        if (entryType == URISUFFIX) {
            String normalizeURI = normalizeURI(catalogEntry.getEntryArg(0));
            String makeAbsolute = makeAbsolute(normalizeURI(catalogEntry.getEntryArg(1)));
            catalogEntry.setEntryArg(1, makeAbsolute);
            this.catalogManager.debug.message(4, "URISUFFIX", normalizeURI, makeAbsolute);
        } else if (entryType == SYSTEMSUFFIX) {
            String normalizeURI2 = normalizeURI(catalogEntry.getEntryArg(0));
            String makeAbsolute2 = makeAbsolute(normalizeURI(catalogEntry.getEntryArg(1)));
            catalogEntry.setEntryArg(1, makeAbsolute2);
            this.catalogManager.debug.message(4, "SYSTEMSUFFIX", normalizeURI2, makeAbsolute2);
        }
        super.addEntry(catalogEntry);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.Catalog
    public String resolveURI(String str) throws MalformedURLException, IOException {
        String resolveURI = super.resolveURI(str);
        if (resolveURI != null) {
            return resolveURI;
        }
        Enumeration elements = this.catalogEntries.elements();
        while (elements.hasMoreElements()) {
            CatalogEntry catalogEntry = (CatalogEntry) elements.nextElement();
            if (catalogEntry.getEntryType() == RESOLVER) {
                String resolveExternalSystem = resolveExternalSystem(str, catalogEntry.getEntryArg(0));
                if (resolveExternalSystem != null) {
                    return resolveExternalSystem;
                }
            } else if (catalogEntry.getEntryType() == URISUFFIX) {
                String entryArg = catalogEntry.getEntryArg(0);
                String entryArg2 = catalogEntry.getEntryArg(1);
                if (entryArg.length() <= str.length() && str.substring(str.length() - entryArg.length()).equals(entryArg)) {
                    return entryArg2;
                }
            } else {
                continue;
            }
        }
        return resolveSubordinateCatalogs(Catalog.URI, null, null, str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.Catalog
    public String resolveSystem(String str) throws MalformedURLException, IOException {
        String resolveSystem = super.resolveSystem(str);
        if (resolveSystem != null) {
            return resolveSystem;
        }
        Enumeration elements = this.catalogEntries.elements();
        while (elements.hasMoreElements()) {
            CatalogEntry catalogEntry = (CatalogEntry) elements.nextElement();
            if (catalogEntry.getEntryType() == RESOLVER) {
                String resolveExternalSystem = resolveExternalSystem(str, catalogEntry.getEntryArg(0));
                if (resolveExternalSystem != null) {
                    return resolveExternalSystem;
                }
            } else if (catalogEntry.getEntryType() == SYSTEMSUFFIX) {
                String entryArg = catalogEntry.getEntryArg(0);
                String entryArg2 = catalogEntry.getEntryArg(1);
                if (entryArg.length() <= str.length() && str.substring(str.length() - entryArg.length()).equals(entryArg)) {
                    return entryArg2;
                }
            } else {
                continue;
            }
        }
        return resolveSubordinateCatalogs(Catalog.SYSTEM, null, null, str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.Catalog
    public String resolvePublic(String str, String str2) throws MalformedURLException, IOException {
        String resolveExternalSystem;
        String resolvePublic = super.resolvePublic(str, str2);
        if (resolvePublic != null) {
            return resolvePublic;
        }
        Enumeration elements = this.catalogEntries.elements();
        while (elements.hasMoreElements()) {
            CatalogEntry catalogEntry = (CatalogEntry) elements.nextElement();
            if (catalogEntry.getEntryType() == RESOLVER) {
                if (str2 != null && (resolveExternalSystem = resolveExternalSystem(str2, catalogEntry.getEntryArg(0))) != null) {
                    return resolveExternalSystem;
                }
                String resolveExternalPublic = resolveExternalPublic(str, catalogEntry.getEntryArg(0));
                if (resolveExternalPublic != null) {
                    return resolveExternalPublic;
                }
            }
        }
        return resolveSubordinateCatalogs(Catalog.PUBLIC, null, str, str2);
    }

    /* access modifiers changed from: protected */
    public String resolveExternalSystem(String str, String str2) throws MalformedURLException, IOException {
        Resolver queryResolver = queryResolver(str2, "i2l", str, null);
        if (queryResolver != null) {
            return queryResolver.resolveSystem(str);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String resolveExternalPublic(String str, String str2) throws MalformedURLException, IOException {
        Resolver queryResolver = queryResolver(str2, "fpi2l", str, null);
        if (queryResolver != null) {
            return queryResolver.resolvePublic(str, null);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Resolver queryResolver(String str, String str2, String str3, String str4) {
        String str5 = str + "?command=" + str2 + "&format=tr9401&uri=" + str3 + "&uri2=" + str4;
        try {
            URLConnection openConnection = new URL(str5).openConnection();
            openConnection.setUseCaches(false);
            Resolver resolver = (Resolver) newCatalog();
            String contentType = openConnection.getContentType();
            if (contentType.indexOf(";") > 0) {
                contentType = contentType.substring(0, contentType.indexOf(";"));
            }
            resolver.parseCatalog(contentType, openConnection.getInputStream());
            return resolver;
        } catch (CatalogException e) {
            if (e.getExceptionType() == 6) {
                this.catalogManager.debug.message(1, "Unparseable catalog: " + str5);
            } else if (e.getExceptionType() == 5) {
                this.catalogManager.debug.message(1, "Unknown catalog format: " + str5);
            }
            return null;
        } catch (MalformedURLException unused) {
            this.catalogManager.debug.message(1, "Malformed resolver URL: " + str5);
            return null;
        } catch (IOException unused2) {
            this.catalogManager.debug.message(1, "I/O Exception opening resolver: " + str5);
            return null;
        }
    }

    private Vector appendVector(Vector vector, Vector vector2) {
        if (vector2 != null) {
            for (int i = 0; i < vector2.size(); i++) {
                vector.addElement(vector2.elementAt(i));
            }
        }
        return vector;
    }

    public Vector resolveAllSystemReverse(String str) throws MalformedURLException, IOException {
        Vector vector = new Vector();
        if (str != null) {
            vector = appendVector(vector, resolveLocalSystemReverse(str));
        }
        return appendVector(vector, resolveAllSubordinateCatalogs(SYSTEMREVERSE, null, null, str));
    }

    public String resolveSystemReverse(String str) throws MalformedURLException, IOException {
        Vector resolveAllSystemReverse = resolveAllSystemReverse(str);
        if (resolveAllSystemReverse == null || resolveAllSystemReverse.size() <= 0) {
            return null;
        }
        return (String) resolveAllSystemReverse.elementAt(0);
    }

    public Vector resolveAllSystem(String str) throws MalformedURLException, IOException {
        Vector vector = new Vector();
        if (str != null) {
            vector = appendVector(vector, resolveAllLocalSystem(str));
        }
        Vector appendVector = appendVector(vector, resolveAllSubordinateCatalogs(SYSTEM, null, null, str));
        if (appendVector.size() > 0) {
            return appendVector;
        }
        return null;
    }

    private Vector resolveAllLocalSystem(String str) {
        Vector vector = new Vector();
        boolean z = SecuritySupport.getSystemProperty("os.name").indexOf("Windows") >= 0;
        Enumeration elements = this.catalogEntries.elements();
        while (elements.hasMoreElements()) {
            CatalogEntry catalogEntry = (CatalogEntry) elements.nextElement();
            if (catalogEntry.getEntryType() == SYSTEM && (catalogEntry.getEntryArg(0).equals(str) || (z && catalogEntry.getEntryArg(0).equalsIgnoreCase(str)))) {
                vector.addElement(catalogEntry.getEntryArg(1));
            }
        }
        if (vector.size() == 0) {
            return null;
        }
        return vector;
    }

    private Vector resolveLocalSystemReverse(String str) {
        Vector vector = new Vector();
        boolean z = SecuritySupport.getSystemProperty("os.name").indexOf("Windows") >= 0;
        Enumeration elements = this.catalogEntries.elements();
        while (elements.hasMoreElements()) {
            CatalogEntry catalogEntry = (CatalogEntry) elements.nextElement();
            if (catalogEntry.getEntryType() == SYSTEM && (catalogEntry.getEntryArg(1).equals(str) || (z && catalogEntry.getEntryArg(1).equalsIgnoreCase(str)))) {
                vector.addElement(catalogEntry.getEntryArg(0));
            }
        }
        if (vector.size() == 0) {
            return null;
        }
        return vector;
    }

    private synchronized Vector resolveAllSubordinateCatalogs(int i, String str, String str2, String str3) throws MalformedURLException, IOException {
        Resolver resolver;
        Vector vector = new Vector();
        int i2 = 0;
        while (true) {
            if (i2 >= this.catalogs.size()) {
                break;
            }
            try {
                resolver = (Resolver) this.catalogs.elementAt(i2);
            } catch (ClassCastException unused) {
                String str4 = (String) this.catalogs.elementAt(i2);
                Resolver resolver2 = (Resolver) newCatalog();
                try {
                    resolver2.parseCatalog(str4);
                } catch (MalformedURLException unused2) {
                    this.catalogManager.debug.message(1, "Malformed Catalog URL", str4);
                } catch (FileNotFoundException unused3) {
                    this.catalogManager.debug.message(1, "Failed to load catalog, file not found", str4);
                } catch (IOException unused4) {
                    this.catalogManager.debug.message(1, "Failed to load catalog, I/O error", str4);
                }
                this.catalogs.setElementAt(resolver2, i2);
                resolver = resolver2;
            }
            if (i == DOCTYPE) {
                String resolveDoctype = resolver.resolveDoctype(str, str2, str3);
                if (resolveDoctype != null) {
                    vector.addElement(resolveDoctype);
                    return vector;
                }
            } else if (i == DOCUMENT) {
                String resolveDocument = resolver.resolveDocument();
                if (resolveDocument != null) {
                    vector.addElement(resolveDocument);
                    return vector;
                }
            } else if (i == ENTITY) {
                String resolveEntity = resolver.resolveEntity(str, str2, str3);
                if (resolveEntity != null) {
                    vector.addElement(resolveEntity);
                    return vector;
                }
            } else if (i == NOTATION) {
                String resolveNotation = resolver.resolveNotation(str, str2, str3);
                if (resolveNotation != null) {
                    vector.addElement(resolveNotation);
                    return vector;
                }
            } else if (i == PUBLIC) {
                String resolvePublic = resolver.resolvePublic(str2, str3);
                if (resolvePublic != null) {
                    vector.addElement(resolvePublic);
                    return vector;
                }
            } else if (i == SYSTEM) {
                vector = appendVector(vector, resolver.resolveAllSystem(str3));
                break;
            } else if (i == SYSTEMREVERSE) {
                vector = appendVector(vector, resolver.resolveAllSystemReverse(str3));
            }
            i2++;
        }
        if (vector != null) {
            return vector;
        }
        return null;
    }
}
