package org.apache.http.client.methods;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;

@Deprecated
public class HttpOptions extends HttpRequestBase {
    public static final String METHOD_NAME = "OPTIONS";

    public HttpOptions(URI uri) {
        setURI(uri);
    }

    public HttpOptions(String uri) {
        setURI(URI.create(uri));
    }

    public String getMethod() {
        return METHOD_NAME;
    }

    public Set<String> getAllowedMethods(HttpResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        HeaderIterator it = response.headerIterator("Allow");
        Set<String> methods = new HashSet();
        while (it.hasNext()) {
            for (HeaderElement element : it.nextHeader().getElements()) {
                methods.add(element.getName());
            }
        }
        return methods;
    }
}
