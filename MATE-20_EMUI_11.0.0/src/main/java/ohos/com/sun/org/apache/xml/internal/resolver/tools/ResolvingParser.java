package ohos.com.sun.org.apache.xml.internal.resolver.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogManager;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import ohos.javax.xml.parsers.SAXParser;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.AttributeList;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.DocumentHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.Parser;
import ohos.org.xml.sax.SAXException;

public class ResolvingParser implements Parser, DTDHandler, DocumentHandler, EntityResolver {
    public static boolean namespaceAware = true;
    public static boolean suppressExplanation = false;
    public static boolean validating = false;
    private boolean allowXMLCatalogPI = false;
    private URL baseURL = null;
    private CatalogManager catalogManager = CatalogManager.getStaticManager();
    private CatalogResolver catalogResolver = null;
    private DocumentHandler documentHandler = null;
    private DTDHandler dtdHandler = null;
    private boolean oasisXMLCatalogPI = false;
    private Parser parser = null;
    private CatalogResolver piCatalogResolver = null;
    private SAXParser saxParser = null;

    public void setEntityResolver(EntityResolver entityResolver) {
    }

    public ResolvingParser() {
        initParser();
    }

    public ResolvingParser(CatalogManager catalogManager2) {
        this.catalogManager = catalogManager2;
        initParser();
    }

    private void initParser() {
        this.catalogResolver = new CatalogResolver(this.catalogManager);
        SAXParserFactory sAXFactory = JdkXmlUtils.getSAXFactory(this.catalogManager.overrideDefaultParser());
        sAXFactory.setValidating(validating);
        try {
            this.saxParser = sAXFactory.newSAXParser();
            this.parser = this.saxParser.getParser();
            this.documentHandler = null;
            this.dtdHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Catalog getCatalog() {
        return this.catalogResolver.getCatalog();
    }

    public void parse(InputSource inputSource) throws IOException, SAXException {
        setupParse(inputSource.getSystemId());
        try {
            this.parser.parse(inputSource);
        } catch (InternalError e) {
            explain(inputSource.getSystemId());
            throw e;
        }
    }

    public void parse(String str) throws IOException, SAXException {
        setupParse(str);
        try {
            this.parser.parse(str);
        } catch (InternalError e) {
            explain(str);
            throw e;
        }
    }

    public void setDocumentHandler(DocumentHandler documentHandler2) {
        this.documentHandler = documentHandler2;
    }

    public void setDTDHandler(DTDHandler dTDHandler) {
        this.dtdHandler = dTDHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.parser.setErrorHandler(errorHandler);
    }

    public void setLocale(Locale locale) throws SAXException {
        this.parser.setLocale(locale);
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.characters(cArr, i, i2);
        }
    }

    public void endDocument() throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.endDocument();
        }
    }

    public void endElement(String str) throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.endElement(str);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.ignorableWhitespace(cArr, i, i2);
        }
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
            DocumentHandler documentHandler2 = this.documentHandler;
            if (documentHandler2 != null) {
                documentHandler2.processingInstruction(str, str2);
            }
        }
    }

    public void setDocumentLocator(Locator locator) {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.setDocumentLocator(locator);
        }
    }

    public void startDocument() throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.startDocument();
        }
    }

    public void startElement(String str, AttributeList attributeList) throws SAXException {
        this.allowXMLCatalogPI = false;
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.startElement(str, attributeList);
        }
    }

    public void notationDecl(String str, String str2, String str3) throws SAXException {
        this.allowXMLCatalogPI = false;
        DTDHandler dTDHandler = this.dtdHandler;
        if (dTDHandler != null) {
            dTDHandler.notationDecl(str, str2, str3);
        }
    }

    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
        this.allowXMLCatalogPI = false;
        DTDHandler dTDHandler = this.dtdHandler;
        if (dTDHandler != null) {
            dTDHandler.unparsedEntityDecl(str, str2, str3, str4);
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

    private void setupParse(String str) {
        URL url;
        this.allowXMLCatalogPI = true;
        this.parser.setEntityResolver(this);
        this.parser.setDocumentHandler(this);
        this.parser.setDTDHandler(this);
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
            printStream.println("Parser probably encountered bad URI in " + str);
            System.out.println("For example, replace '/some/uri' with 'file:/some/uri'.");
        }
    }
}
