package java.net;

import java.util.List;

public interface CookieStore {
    void add(URI uri, HttpCookie httpCookie);

    List<HttpCookie> get(URI uri);

    List<HttpCookie> getCookies();

    List<URI> getURIs();

    boolean remove(URI uri, HttpCookie httpCookie);

    boolean removeAll();
}
