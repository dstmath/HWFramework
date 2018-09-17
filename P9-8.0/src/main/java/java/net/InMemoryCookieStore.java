package java.net;

import dalvik.system.VMRuntime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryCookieStore implements CookieStore {
    private final boolean applyMCompatibility;
    private ReentrantLock lock;
    private Map<URI, List<HttpCookie>> uriIndex;

    public InMemoryCookieStore() {
        this(VMRuntime.getRuntime().getTargetSdkVersion());
    }

    public InMemoryCookieStore(int targetSdkVersion) {
        boolean z = false;
        this.uriIndex = null;
        this.lock = null;
        this.uriIndex = new HashMap();
        this.lock = new ReentrantLock(false);
        if (targetSdkVersion <= 23) {
            z = true;
        }
        this.applyMCompatibility = z;
    }

    public void add(URI uri, HttpCookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("cookie is null");
        }
        this.lock.lock();
        try {
            addIndex(this.uriIndex, getEffectiveURI(uri), cookie);
        } finally {
            this.lock.unlock();
        }
    }

    public List<HttpCookie> get(URI uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null");
        }
        List<HttpCookie> cookies = new ArrayList();
        this.lock.lock();
        try {
            getInternal1(cookies, this.uriIndex, uri.getHost());
            getInternal2(cookies, this.uriIndex, getEffectiveURI(uri));
            return cookies;
        } finally {
            this.lock.unlock();
        }
    }

    public List<HttpCookie> getCookies() {
        List<HttpCookie> rt = new ArrayList();
        this.lock.lock();
        try {
            for (List<HttpCookie> list : this.uriIndex.values()) {
                Iterator<HttpCookie> it = list.iterator();
                while (it.hasNext()) {
                    HttpCookie cookie = (HttpCookie) it.next();
                    if (cookie.hasExpired()) {
                        it.remove();
                    } else if (!rt.contains(cookie)) {
                        rt.add(cookie);
                    }
                }
            }
            return rt;
        } finally {
            rt = Collections.unmodifiableList(rt);
            this.lock.unlock();
        }
    }

    public List<URI> getURIs() {
        List<URI> uris = new ArrayList();
        this.lock.lock();
        try {
            List<URI> result = new ArrayList(this.uriIndex.keySet());
            result.remove(null);
            List<URI> unmodifiableList = Collections.unmodifiableList(result);
            return unmodifiableList;
        } finally {
            uris.addAll(this.uriIndex.keySet());
            this.lock.unlock();
        }
    }

    public boolean remove(URI uri, HttpCookie ck) {
        boolean z = false;
        if (ck == null) {
            throw new NullPointerException("cookie is null");
        }
        this.lock.lock();
        try {
            uri = getEffectiveURI(uri);
            if (this.uriIndex.get(uri) == null) {
                return z;
            }
            List<HttpCookie> cookies = (List) this.uriIndex.get(uri);
            if (cookies != null) {
                boolean remove = cookies.remove((Object) ck);
                this.lock.unlock();
                return remove;
            }
            this.lock.unlock();
            return false;
        } finally {
            z = this.lock;
            z.unlock();
        }
    }

    public boolean removeAll() {
        this.lock.lock();
        boolean result = false;
        try {
            result = this.uriIndex.isEmpty() ^ 1;
            this.uriIndex.clear();
            return result;
        } finally {
            this.lock.unlock();
        }
    }

    private boolean netscapeDomainMatches(String domain, String host) {
        boolean z = false;
        if (domain == null || host == null) {
            return false;
        }
        boolean isLocalDomain = ".local".equalsIgnoreCase(domain);
        int embeddedDotInDomain = domain.indexOf(46);
        if (embeddedDotInDomain == 0) {
            embeddedDotInDomain = domain.indexOf(46, 1);
        }
        if (!isLocalDomain && (embeddedDotInDomain == -1 || embeddedDotInDomain == domain.length() - 1)) {
            return false;
        }
        if (host.indexOf(46) == -1 && isLocalDomain) {
            return true;
        }
        int lengthDiff = host.length() - domain.length();
        if (lengthDiff == 0) {
            return host.equalsIgnoreCase(domain);
        }
        if (lengthDiff > 0) {
            String D = host.substring(lengthDiff);
            if (!this.applyMCompatibility || (domain.startsWith(".") ^ 1) == 0) {
                return D.equalsIgnoreCase(domain);
            }
            return false;
        } else if (lengthDiff != -1) {
            return false;
        } else {
            if (domain.charAt(0) == '.') {
                z = host.equalsIgnoreCase(domain.substring(1));
            }
            return z;
        }
    }

    private void getInternal1(List<HttpCookie> cookies, Map<URI, List<HttpCookie>> cookieIndex, String host) {
        ArrayList<HttpCookie> toRemove = new ArrayList();
        for (Entry<URI, List<HttpCookie>> entry : cookieIndex.entrySet()) {
            List<HttpCookie> lst = (List) entry.getValue();
            for (HttpCookie c : lst) {
                String domain = c.getDomain();
                if ((c.getVersion() == 0 && netscapeDomainMatches(domain, host)) || (c.getVersion() == 1 && HttpCookie.domainMatches(domain, host))) {
                    if (c.hasExpired()) {
                        toRemove.add(c);
                    } else if (!cookies.contains(c)) {
                        cookies.add(c);
                    }
                }
            }
            for (Object c2 : toRemove) {
                lst.remove(c2);
            }
            toRemove.clear();
        }
    }

    private <T extends Comparable<T>> void getInternal2(List<HttpCookie> cookies, Map<T, List<HttpCookie>> cookieIndex, T comparator) {
        for (T index : cookieIndex.keySet()) {
            if (index == comparator || (index != null && comparator.compareTo(index) == 0)) {
                List<HttpCookie> indexedCookies = (List) cookieIndex.get(index);
                if (indexedCookies != null) {
                    Iterator<HttpCookie> it = indexedCookies.iterator();
                    while (it.hasNext()) {
                        HttpCookie ck = (HttpCookie) it.next();
                        if (ck.hasExpired()) {
                            it.remove();
                        } else if (!cookies.contains(ck)) {
                            cookies.add(ck);
                        }
                    }
                }
            }
        }
    }

    private <T> void addIndex(Map<T, List<HttpCookie>> indexStore, T index, HttpCookie cookie) {
        List<HttpCookie> cookies = (List) indexStore.get(index);
        if (cookies != null) {
            cookies.remove((Object) cookie);
            cookies.add(cookie);
            return;
        }
        cookies = new ArrayList();
        cookies.add(cookie);
        indexStore.put(index, cookies);
    }

    private URI getEffectiveURI(URI uri) {
        if (uri == null) {
            return null;
        }
        URI effectiveURI;
        try {
            effectiveURI = new URI("http", uri.getHost(), null, null, null);
        } catch (URISyntaxException e) {
            effectiveURI = uri;
        }
        return effectiveURI;
    }
}
