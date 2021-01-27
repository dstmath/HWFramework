package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogException;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogManager;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.parsers.SAXParser;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.org.xml.sax.AttributeList;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DocumentHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.Parser;
import ohos.org.xml.sax.SAXException;
import sun.reflect.misc.ReflectUtil;

public class SAXCatalogReader implements CatalogReader, ContentHandler, DocumentHandler {
    private boolean abandonHope;
    private Catalog catalog;
    protected Debug debug;
    protected Map<String, String> namespaceMap;
    protected String parserClass;
    protected SAXParserFactory parserFactory;
    private SAXCatalogParser saxParser;

    public void setParserFactory(SAXParserFactory sAXParserFactory) {
        this.parserFactory = sAXParserFactory;
    }

    public void setParserClass(String str) {
        this.parserClass = str;
    }

    public SAXParserFactory getParserFactory() {
        return this.parserFactory;
    }

    public String getParserClass() {
        return this.parserClass;
    }

    public SAXCatalogReader() {
        this.parserFactory = null;
        this.parserClass = null;
        this.namespaceMap = new HashMap();
        this.saxParser = null;
        this.abandonHope = false;
        this.debug = CatalogManager.getStaticManager().debug;
        this.parserFactory = null;
        this.parserClass = null;
    }

    public SAXCatalogReader(SAXParserFactory sAXParserFactory) {
        this.parserFactory = null;
        this.parserClass = null;
        this.namespaceMap = new HashMap();
        this.saxParser = null;
        this.abandonHope = false;
        this.debug = CatalogManager.getStaticManager().debug;
        this.parserFactory = sAXParserFactory;
    }

    public SAXCatalogReader(String str) {
        this.parserFactory = null;
        this.parserClass = null;
        this.namespaceMap = new HashMap();
        this.saxParser = null;
        this.abandonHope = false;
        this.debug = CatalogManager.getStaticManager().debug;
        this.parserClass = str;
    }

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
    public void readCatalog(Catalog catalog2, String str) throws MalformedURLException, IOException, CatalogException {
        URL url;
        try {
            url = new URL(str);
        } catch (MalformedURLException unused) {
            url = new URL("file:///" + str);
        }
        this.debug = catalog2.getCatalogManager().debug;
        try {
            readCatalog(catalog2, url.openConnection().getInputStream());
        } catch (FileNotFoundException unused2) {
            catalog2.getCatalogManager().debug.message(1, "Failed to load catalog, file not found", url.toString());
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.CatalogReader
    public void readCatalog(Catalog catalog2, InputStream inputStream) throws IOException, CatalogException {
        if (this.parserFactory == null && this.parserClass == null) {
            this.debug.message(1, "Cannot read SAX catalog without a parser");
            throw new CatalogException(6);
        }
        this.debug = catalog2.getCatalogManager().debug;
        EntityResolver bootstrapResolver = catalog2.getCatalogManager().getBootstrapResolver();
        this.catalog = catalog2;
        try {
            if (this.parserFactory != null) {
                SAXParser newSAXParser = this.parserFactory.newSAXParser();
                SAXParserHandler sAXParserHandler = new SAXParserHandler();
                sAXParserHandler.setContentHandler(this);
                if (bootstrapResolver != null) {
                    sAXParserHandler.setEntityResolver(bootstrapResolver);
                }
                newSAXParser.parse(new InputSource(inputStream), sAXParserHandler);
                return;
            }
            Parser parser = (Parser) ReflectUtil.forName(this.parserClass).newInstance();
            parser.setDocumentHandler(this);
            if (bootstrapResolver != null) {
                parser.setEntityResolver(bootstrapResolver);
            }
            parser.parse(new InputSource(inputStream));
        } catch (ClassNotFoundException unused) {
            throw new CatalogException(6);
        } catch (IllegalAccessException unused2) {
            throw new CatalogException(6);
        } catch (InstantiationException unused3) {
            throw new CatalogException(6);
        } catch (ParserConfigurationException unused4) {
            throw new CatalogException(5);
        } catch (SAXException e) {
            Exception exception = e.getException();
            UnknownHostException unknownHostException = new UnknownHostException();
            FileNotFoundException fileNotFoundException = new FileNotFoundException();
            if (exception != null) {
                if (exception.getClass() == unknownHostException.getClass()) {
                    throw new CatalogException(7, exception.toString());
                } else if (exception.getClass() == fileNotFoundException.getClass()) {
                    throw new CatalogException(7, exception.toString());
                }
            }
            throw new CatalogException((Exception) e);
        }
    }

    public void setDocumentLocator(Locator locator) {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.setDocumentLocator(locator);
        }
    }

    public void startDocument() throws SAXException {
        this.saxParser = null;
        this.abandonHope = false;
    }

    public void endDocument() throws SAXException {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.endDocument();
        }
    }

    public void startElement(String str, AttributeList attributeList) throws SAXException {
        String str2;
        String str3;
        if (!this.abandonHope) {
            SAXCatalogParser sAXCatalogParser = this.saxParser;
            if (sAXCatalogParser == null) {
                if (str.indexOf(58) > 0) {
                    str2 = str.substring(0, str.indexOf(58));
                } else {
                    str2 = "";
                }
                String substring = str.indexOf(58) > 0 ? str.substring(str.indexOf(58) + 1) : str;
                if (str2.equals("")) {
                    str3 = attributeList.getValue("xmlns");
                } else {
                    str3 = attributeList.getValue("xmlns:" + str2);
                }
                String catalogParser = getCatalogParser(str3, substring);
                if (catalogParser == null) {
                    this.abandonHope = true;
                    if (str3 == null) {
                        Debug debug2 = this.debug;
                        debug2.message(2, "No Catalog parser for " + str);
                        return;
                    }
                    Debug debug3 = this.debug;
                    debug3.message(2, "No Catalog parser for {" + str3 + "}" + str);
                    return;
                }
                try {
                    this.saxParser = (SAXCatalogParser) ReflectUtil.forName(catalogParser).newInstance();
                    this.saxParser.setCatalog(this.catalog);
                    this.saxParser.startDocument();
                    this.saxParser.startElement(str, attributeList);
                } catch (ClassNotFoundException e) {
                    this.saxParser = null;
                    this.abandonHope = true;
                    this.debug.message(2, e.toString());
                } catch (InstantiationException e2) {
                    this.saxParser = null;
                    this.abandonHope = true;
                    this.debug.message(2, e2.toString());
                } catch (IllegalAccessException e3) {
                    this.saxParser = null;
                    this.abandonHope = true;
                    this.debug.message(2, e3.toString());
                } catch (ClassCastException e4) {
                    this.saxParser = null;
                    this.abandonHope = true;
                    this.debug.message(2, e4.toString());
                }
            } else {
                sAXCatalogParser.startElement(str, attributeList);
            }
        }
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        if (!this.abandonHope) {
            SAXCatalogParser sAXCatalogParser = this.saxParser;
            if (sAXCatalogParser == null) {
                String catalogParser = getCatalogParser(str, str2);
                if (catalogParser == null) {
                    this.abandonHope = true;
                    if (str == null) {
                        Debug debug2 = this.debug;
                        debug2.message(2, "No Catalog parser for " + str2);
                        return;
                    }
                    Debug debug3 = this.debug;
                    debug3.message(2, "No Catalog parser for {" + str + "}" + str2);
                    return;
                }
                try {
                    this.saxParser = (SAXCatalogParser) ReflectUtil.forName(catalogParser).newInstance();
                    this.saxParser.setCatalog(this.catalog);
                    this.saxParser.startDocument();
                    this.saxParser.startElement(str, str2, str3, attributes);
                } catch (ClassNotFoundException e) {
                    this.saxParser = null;
                    this.abandonHope = true;
                    this.debug.message(2, e.toString());
                } catch (InstantiationException e2) {
                    this.saxParser = null;
                    this.abandonHope = true;
                    this.debug.message(2, e2.toString());
                } catch (IllegalAccessException e3) {
                    this.saxParser = null;
                    this.abandonHope = true;
                    this.debug.message(2, e3.toString());
                } catch (ClassCastException e4) {
                    this.saxParser = null;
                    this.abandonHope = true;
                    this.debug.message(2, e4.toString());
                }
            } else {
                sAXCatalogParser.startElement(str, str2, str3, attributes);
            }
        }
    }

    public void endElement(String str) throws SAXException {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.endElement(str);
        }
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.endElement(str, str2, str3);
        }
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.characters(cArr, i, i2);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.ignorableWhitespace(cArr, i, i2);
        }
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.processingInstruction(str, str2);
        }
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.startPrefixMapping(str, str2);
        }
    }

    public void endPrefixMapping(String str) throws SAXException {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.endPrefixMapping(str);
        }
    }

    public void skippedEntity(String str) throws SAXException {
        SAXCatalogParser sAXCatalogParser = this.saxParser;
        if (sAXCatalogParser != null) {
            sAXCatalogParser.skippedEntity(str);
        }
    }
}
