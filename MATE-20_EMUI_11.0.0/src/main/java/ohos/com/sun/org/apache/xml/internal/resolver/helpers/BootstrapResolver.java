package ohos.com.sun.org.apache.xml.internal.resolver.helpers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.URIResolver;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.InputSource;

public class BootstrapResolver implements EntityResolver, URIResolver {
    public static final String xmlCatalogPubId = "-//OASIS//DTD XML Catalogs V1.0//EN";
    public static final String xmlCatalogRNG = "http://www.oasis-open.org/committees/entity/release/1.0/catalog.rng";
    public static final String xmlCatalogSysId = "http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd";
    public static final String xmlCatalogXSD = "http://www.oasis-open.org/committees/entity/release/1.0/catalog.xsd";
    private final Map<String, String> publicMap = new HashMap();
    private final Map<String, String> systemMap = new HashMap();
    private final Map<String, String> uriMap = new HashMap();

    public BootstrapResolver() {
        URL resource = getClass().getResource("/com/sun/org/apache/xml/internal/resolver/etc/catalog.dtd");
        if (resource != null) {
            this.publicMap.put(xmlCatalogPubId, resource.toString());
            this.systemMap.put(xmlCatalogSysId, resource.toString());
        }
        URL resource2 = getClass().getResource("/com/sun/org/apache/xml/internal/resolver/etc/catalog.rng");
        if (resource2 != null) {
            this.uriMap.put(xmlCatalogRNG, resource2.toString());
        }
        URL resource3 = getClass().getResource("/com/sun/org/apache/xml/internal/resolver/etc/catalog.xsd");
        if (resource3 != null) {
            this.uriMap.put(xmlCatalogXSD, resource3.toString());
        }
    }

    public InputSource resolveEntity(String str, String str2) {
        String str3;
        if (str2 == null || !this.systemMap.containsKey(str2)) {
            str3 = (str == null || !this.publicMap.containsKey(str)) ? null : this.publicMap.get(str);
        } else {
            str3 = this.systemMap.get(str2);
        }
        if (str3 != null) {
            try {
                InputSource inputSource = new InputSource(str3);
                inputSource.setPublicId(str);
                inputSource.setByteStream(new URL(str3).openStream());
                return inputSource;
            } catch (Exception unused) {
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
        if (this.uriMap.containsKey(str)) {
            str4 = this.uriMap.get(str);
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
        SAXSource sAXSource = new SAXSource();
        sAXSource.setInputSource(new InputSource(str4));
        return sAXSource;
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
