package ohos.com.sun.org.apache.xerces.internal.util;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

public final class HTTPInputSource extends XMLInputSource {
    protected boolean fFollowRedirects = true;
    protected Map<String, String> fHTTPRequestProperties = new HashMap();

    public HTTPInputSource(String str, String str2, String str3) {
        super(str, str2, str3);
    }

    public HTTPInputSource(XMLResourceIdentifier xMLResourceIdentifier) {
        super(xMLResourceIdentifier);
    }

    public HTTPInputSource(String str, String str2, String str3, InputStream inputStream, String str4) {
        super(str, str2, str3, inputStream, str4);
    }

    public HTTPInputSource(String str, String str2, String str3, Reader reader, String str4) {
        super(str, str2, str3, reader, str4);
    }

    public boolean getFollowHTTPRedirects() {
        return this.fFollowRedirects;
    }

    public void setFollowHTTPRedirects(boolean z) {
        this.fFollowRedirects = z;
    }

    public String getHTTPRequestProperty(String str) {
        return this.fHTTPRequestProperties.get(str);
    }

    public Iterator<Map.Entry<String, String>> getHTTPRequestProperties() {
        return this.fHTTPRequestProperties.entrySet().iterator();
    }

    public void setHTTPRequestProperty(String str, String str2) {
        if (str2 != null) {
            this.fHTTPRequestProperties.put(str, str2);
        } else {
            this.fHTTPRequestProperties.remove(str);
        }
    }
}
