package ohos.com.sun.org.apache.xml.internal.resolver.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogManager;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.URIResolver;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLReader;

public class CatalogResolver implements EntityResolver, URIResolver {
    private Catalog catalog = null;
    private CatalogManager catalogManager = CatalogManager.getStaticManager();
    public boolean namespaceAware = true;
    public boolean validating = false;

    public CatalogResolver() {
        initializeCatalogs(false);
    }

    public CatalogResolver(boolean z) {
        initializeCatalogs(z);
    }

    public CatalogResolver(CatalogManager catalogManager2) {
        this.catalogManager = catalogManager2;
        initializeCatalogs(!this.catalogManager.getUseStaticCatalog());
    }

    private void initializeCatalogs(boolean z) {
        this.catalog = this.catalogManager.getCatalog();
    }

    public Catalog getCatalog() {
        return this.catalog;
    }

    public String getResolvedEntity(String str, String str2) {
        Catalog catalog2 = this.catalog;
        String str3 = null;
        if (catalog2 == null) {
            this.catalogManager.debug.message(1, "Catalog resolution attempted with null catalog; ignored");
            return null;
        }
        if (str2 != null) {
            try {
                str3 = catalog2.resolveSystem(str2);
            } catch (MalformedURLException unused) {
                this.catalogManager.debug.message(1, "Malformed URL exception trying to resolve", str);
            } catch (IOException unused2) {
                this.catalogManager.debug.message(1, "I/O exception trying to resolve", str);
            }
        }
        if (str3 == null) {
            if (str != null) {
                try {
                    str3 = this.catalog.resolvePublic(str, str2);
                } catch (MalformedURLException unused3) {
                    this.catalogManager.debug.message(1, "Malformed URL exception trying to resolve", str);
                } catch (IOException unused4) {
                    this.catalogManager.debug.message(1, "I/O exception trying to resolve", str);
                }
            }
            if (str3 != null) {
                this.catalogManager.debug.message(2, "Resolved public", str, str3);
            }
        } else {
            this.catalogManager.debug.message(2, "Resolved system", str2, str3);
        }
        return str3;
    }

    public InputSource resolveEntity(String str, String str2) {
        String resolvedEntity = getResolvedEntity(str, str2);
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

    public Source resolve(String str, String str2) throws TransformerException {
        String str3;
        String url;
        int indexOf = str.indexOf("#");
        if (indexOf >= 0) {
            str3 = str.substring(0, indexOf);
            str.substring(indexOf + 1);
        } else {
            str3 = str;
        }
        String str4 = null;
        try {
            str4 = this.catalog.resolveURI(str);
        } catch (Exception unused) {
        }
        if (str4 == null) {
            if (str2 == null) {
                try {
                    url = new URL(str3).toString();
                } catch (MalformedURLException e) {
                    String makeAbsolute = makeAbsolute(str2);
                    if (!makeAbsolute.equals(str2)) {
                        return resolve(str, makeAbsolute);
                    }
                    throw new TransformerException("Malformed URL " + str + "(base " + str2 + ")", e);
                }
            } else {
                URL url2 = new URL(str2);
                if (str.length() != 0) {
                    url2 = new URL(url2, str3);
                }
                url = url2.toString();
            }
            str4 = url;
        }
        this.catalogManager.debug.message(2, "Resolved URI", str, str4);
        SAXSource sAXSource = new SAXSource();
        sAXSource.setInputSource(new InputSource(str4));
        setEntityResolver(sAXSource);
        return sAXSource;
    }

    private void setEntityResolver(SAXSource sAXSource) throws TransformerException {
        XMLReader xMLReader = sAXSource.getXMLReader();
        if (xMLReader == null) {
            try {
                xMLReader = JdkXmlUtils.getSAXFactory(this.catalogManager.overrideDefaultParser()).newSAXParser().getXMLReader();
            } catch (ParserConfigurationException e) {
                throw new TransformerException(e);
            } catch (SAXException e2) {
                throw new TransformerException(e2);
            }
        }
        xMLReader.setEntityResolver(this);
        sAXSource.setXMLReader(xMLReader);
    }

    private String makeAbsolute(String str) {
        if (str == null) {
            str = "";
        }
        try {
            return new URL(str).toString();
        } catch (MalformedURLException unused) {
            try {
                return FileURL.makeURL(str).toString();
            } catch (MalformedURLException unused2) {
                return str;
            }
        }
    }
}
