package ohos.com.sun.org.apache.xml.internal.resolver.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogManager;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.helpers.XMLFilterImpl;

public class ResolvingXMLFilter extends XMLFilterImpl {
    public static boolean suppressExplanation = false;
    private boolean allowXMLCatalogPI;
    private URL baseURL;
    CatalogManager catalogManager;
    private CatalogResolver catalogResolver;
    private boolean oasisXMLCatalogPI;
    private CatalogResolver piCatalogResolver;

    public ResolvingXMLFilter() {
        this.catalogManager = CatalogManager.getStaticManager();
        this.catalogResolver = null;
        this.piCatalogResolver = null;
        this.allowXMLCatalogPI = false;
        this.oasisXMLCatalogPI = false;
        this.baseURL = null;
        this.catalogResolver = new CatalogResolver(this.catalogManager);
    }

    public ResolvingXMLFilter(XMLReader xMLReader) {
        super(xMLReader);
        this.catalogManager = CatalogManager.getStaticManager();
        this.catalogResolver = null;
        this.piCatalogResolver = null;
        this.allowXMLCatalogPI = false;
        this.oasisXMLCatalogPI = false;
        this.baseURL = null;
        this.catalogResolver = new CatalogResolver(this.catalogManager);
    }

    public ResolvingXMLFilter(CatalogManager catalogManager2) {
        this.catalogManager = CatalogManager.getStaticManager();
        this.catalogResolver = null;
        this.piCatalogResolver = null;
        this.allowXMLCatalogPI = false;
        this.oasisXMLCatalogPI = false;
        this.baseURL = null;
        this.catalogManager = catalogManager2;
        this.catalogResolver = new CatalogResolver(this.catalogManager);
    }

    public ResolvingXMLFilter(XMLReader xMLReader, CatalogManager catalogManager2) {
        super(xMLReader);
        this.catalogManager = CatalogManager.getStaticManager();
        this.catalogResolver = null;
        this.piCatalogResolver = null;
        this.allowXMLCatalogPI = false;
        this.oasisXMLCatalogPI = false;
        this.baseURL = null;
        this.catalogManager = catalogManager2;
        this.catalogResolver = new CatalogResolver(this.catalogManager);
    }

    public Catalog getCatalog() {
        return this.catalogResolver.getCatalog();
    }

    public void parse(InputSource inputSource) throws IOException, SAXException {
        this.allowXMLCatalogPI = true;
        setupBaseURI(inputSource.getSystemId());
        try {
            ResolvingXMLFilter.super.parse(inputSource);
        } catch (InternalError e) {
            explain(inputSource.getSystemId());
            throw e;
        }
    }

    public void parse(String str) throws IOException, SAXException {
        this.allowXMLCatalogPI = true;
        setupBaseURI(str);
        try {
            ResolvingXMLFilter.super.parse(str);
        } catch (InternalError e) {
            explain(str);
            throw e;
        }
    }

    public InputSource resolveEntity(String str, String str2) {
        CatalogResolver catalogResolver2;
        this.allowXMLCatalogPI = false;
        String resolvedEntity = this.catalogResolver.getResolvedEntity(str, str2);
        if (resolvedEntity == null && (catalogResolver2 = this.piCatalogResolver) != null) {
            resolvedEntity = catalogResolver2.getResolvedEntity(str, str2);
        }
        if (resolvedEntity != null) {
            try {
                InputSource inputSource = new InputSource(resolvedEntity);
                inputSource.setPublicId(str);
                inputSource.setByteStream(new URL(resolvedEntity).openStream());
                return inputSource;
            } catch (Exception unused) {
                this.catalogManager.debug.message(1, "Failed to create InputSource", resolvedEntity);
            }
        }
        return null;
    }

    public void notationDecl(String str, String str2, String str3) throws SAXException {
        this.allowXMLCatalogPI = false;
        ResolvingXMLFilter.super.notationDecl(str, str2, str3);
    }

    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
        this.allowXMLCatalogPI = false;
        ResolvingXMLFilter.super.unparsedEntityDecl(str, str2, str3, str4);
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        this.allowXMLCatalogPI = false;
        ResolvingXMLFilter.super.startElement(str, str2, str3, attributes);
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        String substring;
        int indexOf;
        URL url;
        if (str.equals("oasis-xml-catalog")) {
            URL url2 = null;
            int indexOf2 = str2.indexOf("catalog=");
            if (indexOf2 >= 0) {
                String substring2 = str2.substring(indexOf2 + 8);
                if (substring2.length() > 1 && (indexOf = (substring = substring2.substring(1)).indexOf(substring2.substring(0, 1))) >= 0) {
                    String substring3 = substring.substring(0, indexOf);
                    try {
                        if (this.baseURL != null) {
                            url = new URL(this.baseURL, substring3);
                        } else {
                            url = new URL(substring3);
                        }
                        url2 = url;
                    } catch (MalformedURLException unused) {
                    }
                }
            }
            if (!this.allowXMLCatalogPI) {
                Debug debug = this.catalogManager.debug;
                debug.message(3, "PI oasis-xml-catalog occurred in an invalid place: " + str2);
            } else if (this.catalogManager.getAllowOasisXMLCatalogPI()) {
                this.catalogManager.debug.message(4, "oasis-xml-catalog PI", str2);
                if (url2 != null) {
                    try {
                        this.catalogManager.debug.message(4, "oasis-xml-catalog", url2.toString());
                        this.oasisXMLCatalogPI = true;
                        if (this.piCatalogResolver == null) {
                            this.piCatalogResolver = new CatalogResolver(true);
                        }
                        this.piCatalogResolver.getCatalog().parseCatalog(url2.toString());
                    } catch (Exception unused2) {
                        Debug debug2 = this.catalogManager.debug;
                        debug2.message(3, "Exception parsing oasis-xml-catalog: " + url2.toString());
                    }
                } else {
                    Debug debug3 = this.catalogManager.debug;
                    debug3.message(3, "PI oasis-xml-catalog unparseable: " + str2);
                }
            } else {
                Debug debug4 = this.catalogManager.debug;
                debug4.message(4, "PI oasis-xml-catalog ignored: " + str2);
            }
        } else {
            ResolvingXMLFilter.super.processingInstruction(str, str2);
        }
    }

    private void setupBaseURI(String str) {
        URL url;
        try {
            url = FileURL.makeURL("basename");
        } catch (MalformedURLException unused) {
            url = null;
        }
        try {
            this.baseURL = new URL(str);
        } catch (MalformedURLException unused2) {
            if (url != null) {
                try {
                    this.baseURL = new URL(url, str);
                } catch (MalformedURLException unused3) {
                    this.baseURL = null;
                }
            } else {
                this.baseURL = null;
            }
        }
    }

    private void explain(String str) {
        if (!suppressExplanation) {
            PrintStream printStream = System.out;
            printStream.println("XMLReader probably encountered bad URI in " + str);
            System.out.println("For example, replace '/some/uri' with 'file:/some/uri'.");
        }
        suppressExplanation = true;
    }
}
