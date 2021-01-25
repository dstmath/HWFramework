package ohos.com.sun.org.apache.xerces.internal.util;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogManager;
import ohos.com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader;
import ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.ls.LSInput;
import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.EntityResolver2;

public class XMLCatalogResolver implements XMLEntityResolver, EntityResolver2, LSResourceResolver {
    private Catalog fCatalog;
    private boolean fCatalogsChanged;
    private String[] fCatalogsList;
    private boolean fPreferPublic;
    private CatalogManager fResolverCatalogManager;
    private boolean fUseLiteralSystemId;

    public InputSource getExternalSubset(String str, String str2) throws SAXException, IOException {
        return null;
    }

    public XMLCatalogResolver() {
        this(null, true);
    }

    public XMLCatalogResolver(String[] strArr) {
        this(strArr, true);
    }

    public XMLCatalogResolver(String[] strArr, boolean z) {
        this.fResolverCatalogManager = null;
        this.fCatalog = null;
        this.fCatalogsList = null;
        this.fCatalogsChanged = true;
        this.fPreferPublic = true;
        this.fUseLiteralSystemId = true;
        init(strArr, z);
    }

    public final synchronized String[] getCatalogList() {
        return this.fCatalogsList != null ? (String[]) this.fCatalogsList.clone() : null;
    }

    public final synchronized void setCatalogList(String[] strArr) {
        this.fCatalogsChanged = true;
        this.fCatalogsList = strArr != null ? (String[]) strArr.clone() : null;
    }

    public final synchronized void clear() {
        this.fCatalog = null;
    }

    public final boolean getPreferPublic() {
        return this.fPreferPublic;
    }

    public final void setPreferPublic(boolean z) {
        this.fPreferPublic = z;
        this.fResolverCatalogManager.setPreferPublic(z);
    }

    public final boolean getUseLiteralSystemId() {
        return this.fUseLiteralSystemId;
    }

    public final void setUseLiteralSystemId(boolean z) {
        this.fUseLiteralSystemId = z;
    }

    public InputSource resolveEntity(String str, String str2) throws SAXException, IOException {
        String str3;
        if (str == null || str2 == null) {
            str3 = str2 != null ? resolveSystem(str2) : null;
        } else {
            str3 = resolvePublic(str, str2);
        }
        if (str3 == null) {
            return null;
        }
        InputSource inputSource = new InputSource(str3);
        inputSource.setPublicId(str);
        return inputSource;
    }

    public InputSource resolveEntity(String str, String str2, String str3, String str4) throws SAXException, IOException {
        String str5;
        if (!getUseLiteralSystemId() && str3 != null) {
            try {
                str4 = new URI(new URI(str3), str4).toString();
            } catch (URI.MalformedURIException unused) {
            }
        }
        if (str2 == null || str4 == null) {
            str5 = str4 != null ? resolveSystem(str4) : null;
        } else {
            str5 = resolvePublic(str2, str4);
        }
        if (str5 == null) {
            return null;
        }
        InputSource inputSource = new InputSource(str5);
        inputSource.setPublicId(str2);
        return inputSource;
    }

    public LSInput resolveResource(String str, String str2, String str3, String str4, String str5) {
        String str6;
        if (str2 != null) {
            try {
                str6 = resolveURI(str2);
            } catch (IOException unused) {
                str6 = null;
            }
        } else {
            str6 = null;
        }
        try {
            if (!getUseLiteralSystemId() && str5 != null) {
                try {
                    str4 = new URI(new URI(str5), str4).toString();
                } catch (URI.MalformedURIException unused2) {
                }
            }
            if (str6 == null) {
                if (str3 != null && str4 != null) {
                    str6 = resolvePublic(str3, str4);
                } else if (str4 != null) {
                    str6 = resolveSystem(str4);
                }
            }
        } catch (IOException unused3) {
        }
        if (str6 != null) {
            return new DOMInputImpl(str3, str6, str5);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver
    public XMLInputSource resolveEntity(XMLResourceIdentifier xMLResourceIdentifier) throws XNIException, IOException {
        String resolveIdentifier = resolveIdentifier(xMLResourceIdentifier);
        if (resolveIdentifier != null) {
            return new XMLInputSource(xMLResourceIdentifier.getPublicId(), resolveIdentifier, xMLResourceIdentifier.getBaseSystemId());
        }
        return null;
    }

    public String resolveIdentifier(XMLResourceIdentifier xMLResourceIdentifier) throws IOException, XNIException {
        String str;
        String namespace = xMLResourceIdentifier.getNamespace();
        String resolveURI = namespace != null ? resolveURI(namespace) : null;
        if (resolveURI != null) {
            return resolveURI;
        }
        String publicId = xMLResourceIdentifier.getPublicId();
        if (getUseLiteralSystemId()) {
            str = xMLResourceIdentifier.getLiteralSystemId();
        } else {
            str = xMLResourceIdentifier.getExpandedSystemId();
        }
        if (publicId == null || str == null) {
            return str != null ? resolveSystem(str) : resolveURI;
        }
        return resolvePublic(publicId, str);
    }

    public final synchronized String resolveSystem(String str) throws IOException {
        if (this.fCatalogsChanged) {
            parseCatalogs();
            this.fCatalogsChanged = false;
        }
        return this.fCatalog != null ? this.fCatalog.resolveSystem(str) : null;
    }

    public final synchronized String resolvePublic(String str, String str2) throws IOException {
        if (this.fCatalogsChanged) {
            parseCatalogs();
            this.fCatalogsChanged = false;
        }
        return this.fCatalog != null ? this.fCatalog.resolvePublic(str, str2) : null;
    }

    public final synchronized String resolveURI(String str) throws IOException {
        if (this.fCatalogsChanged) {
            parseCatalogs();
            this.fCatalogsChanged = false;
        }
        return this.fCatalog != null ? this.fCatalog.resolveURI(str) : null;
    }

    private void init(String[] strArr, boolean z) {
        this.fCatalogsList = strArr != null ? (String[]) strArr.clone() : null;
        this.fPreferPublic = z;
        this.fResolverCatalogManager = new CatalogManager();
        this.fResolverCatalogManager.setAllowOasisXMLCatalogPI(false);
        this.fResolverCatalogManager.setCatalogClassName("ohos.com.sun.org.apache.xml.internal.resolver.Catalog");
        this.fResolverCatalogManager.setCatalogFiles("");
        this.fResolverCatalogManager.setIgnoreMissingProperties(true);
        this.fResolverCatalogManager.setPreferPublic(this.fPreferPublic);
        this.fResolverCatalogManager.setRelativeCatalogs(false);
        this.fResolverCatalogManager.setUseStaticCatalog(false);
        this.fResolverCatalogManager.setVerbosity(0);
    }

    private void parseCatalogs() throws IOException {
        if (this.fCatalogsList != null) {
            this.fCatalog = new Catalog(this.fResolverCatalogManager);
            attachReaderToCatalog(this.fCatalog);
            int i = 0;
            while (true) {
                String[] strArr = this.fCatalogsList;
                if (i < strArr.length) {
                    String str = strArr[i];
                    if (str != null && str.length() > 0) {
                        this.fCatalog.parseCatalog(str);
                    }
                    i++;
                } else {
                    return;
                }
            }
        } else {
            this.fCatalog = null;
        }
    }

    private void attachReaderToCatalog(Catalog catalog) {
        SAXParserFactory sAXFactory = JdkXmlUtils.getSAXFactory(catalog.getCatalogManager().overrideDefaultParser());
        sAXFactory.setValidating(false);
        SAXCatalogReader sAXCatalogReader = new SAXCatalogReader(sAXFactory);
        sAXCatalogReader.setCatalogParser(OASISXMLCatalogReader.namespaceName, "catalog", "ohos.com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader");
        catalog.addReader("application/xml", sAXCatalogReader);
    }
}
