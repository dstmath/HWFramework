package java.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.util.logging.PlatformLogger;
import sun.util.logging.PlatformLogger.Level;

public class CookieManager extends CookieHandler {
    private CookieStore cookieJar;
    private CookiePolicy policyCallback;

    static class CookiePathComparator implements Comparator<HttpCookie> {
        CookiePathComparator() {
        }

        public int compare(HttpCookie c1, HttpCookie c2) {
            if (c1 == c2) {
                return 0;
            }
            if (c1 == null) {
                return -1;
            }
            if (c2 == null) {
                return 1;
            }
            if (!c1.getName().equals(c2.getName())) {
                return 0;
            }
            String c1Path = CookieManager.normalizePath(c1.getPath());
            String c2Path = CookieManager.normalizePath(c2.getPath());
            if (c1Path.startsWith(c2Path)) {
                return -1;
            }
            return c2Path.startsWith(c1Path) ? 1 : 0;
        }
    }

    public CookieManager() {
        this(null, null);
    }

    public CookieManager(CookieStore store, CookiePolicy cookiePolicy) {
        this.cookieJar = null;
        if (cookiePolicy == null) {
            cookiePolicy = CookiePolicy.ACCEPT_ORIGINAL_SERVER;
        }
        this.policyCallback = cookiePolicy;
        if (store == null) {
            this.cookieJar = new InMemoryCookieStore();
        } else {
            this.cookieJar = store;
        }
    }

    public void setCookiePolicy(CookiePolicy cookiePolicy) {
        if (cookiePolicy != null) {
            this.policyCallback = cookiePolicy;
        }
    }

    public CookieStore getCookieStore() {
        return this.cookieJar;
    }

    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
        if (uri == null || requestHeaders == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        Map<String, List<String>> cookieMap = new HashMap();
        if (this.cookieJar == null) {
            return Collections.unmodifiableMap(cookieMap);
        }
        boolean secureLink = "https".equalsIgnoreCase(uri.getScheme());
        List<HttpCookie> cookies = new ArrayList();
        for (HttpCookie cookie : this.cookieJar.get(uri)) {
            if (pathMatches(uri, cookie) && (secureLink || (cookie.getSecure() ^ 1) != 0)) {
                String ports = cookie.getPortlist();
                if (ports == null || (ports.isEmpty() ^ 1) == 0) {
                    cookies.add(cookie);
                } else {
                    int port = uri.getPort();
                    if (port == -1) {
                        port = "https".equals(uri.getScheme()) ? 443 : 80;
                    }
                    if (isInPortList(ports, port)) {
                        cookies.add(cookie);
                    }
                }
            }
        }
        if (cookies.isEmpty()) {
            return Collections.emptyMap();
        }
        cookieMap.put("Cookie", sortByPath(cookies));
        return Collections.unmodifiableMap(cookieMap);
    }

    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
        if (uri == null || responseHeaders == null) {
            throw new IllegalArgumentException("Argument is null");
        } else if (this.cookieJar != null) {
            PlatformLogger logger = PlatformLogger.getLogger("java.net.CookieManager");
            for (String headerKey : responseHeaders.keySet()) {
                if (headerKey != null) {
                    int i;
                    if (headerKey.equalsIgnoreCase("Set-Cookie2")) {
                        i = 1;
                    } else {
                        i = headerKey.equalsIgnoreCase("Set-Cookie");
                    }
                    if ((i ^ 1) == 0) {
                        for (String headerValue : (List) responseHeaders.get(headerKey)) {
                            List<HttpCookie> cookies;
                            try {
                                cookies = HttpCookie.parse(headerValue);
                            } catch (IllegalArgumentException e) {
                                cookies = Collections.emptyList();
                                if (logger.isLoggable(Level.SEVERE)) {
                                    logger.severe("Invalid cookie for " + uri + ": " + headerValue);
                                }
                            }
                            try {
                                for (HttpCookie cookie : cookies) {
                                    if (cookie.getPath() == null) {
                                        String path = uri.getPath();
                                        if (!path.endsWith("/")) {
                                            int i2 = path.lastIndexOf("/");
                                            if (i2 > 0) {
                                                path = path.substring(0, i2 + 1);
                                            } else {
                                                path = "/";
                                            }
                                        }
                                        cookie.setPath(path);
                                    } else if (!pathMatches(uri, cookie)) {
                                    }
                                    if (cookie.getDomain() == null) {
                                        String host = uri.getHost();
                                        if (!(host == null || (host.contains(".") ^ 1) == 0)) {
                                            host = host + ".local";
                                        }
                                        cookie.setDomain(host);
                                    }
                                    String ports = cookie.getPortlist();
                                    if (ports != null) {
                                        int port = uri.getPort();
                                        if (port == -1) {
                                            port = "https".equals(uri.getScheme()) ? 443 : 80;
                                        }
                                        if (ports.isEmpty()) {
                                            cookie.setPortlist("" + port);
                                            if (shouldAcceptInternal(uri, cookie)) {
                                                this.cookieJar.add(uri, cookie);
                                            }
                                        } else if (isInPortList(ports, port) && shouldAcceptInternal(uri, cookie)) {
                                            this.cookieJar.add(uri, cookie);
                                        }
                                    } else if (shouldAcceptInternal(uri, cookie)) {
                                        this.cookieJar.add(uri, cookie);
                                    }
                                }
                            } catch (IllegalArgumentException e2) {
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean shouldAcceptInternal(URI uri, HttpCookie cookie) {
        try {
            return this.policyCallback.shouldAccept(uri, cookie);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isInPortList(String lst, int port) {
        int i = lst.indexOf(",");
        while (i > 0) {
            try {
                if (Integer.parseInt(lst.substring(0, i)) == port) {
                    return true;
                }
                lst = lst.substring(i + 1);
                i = lst.indexOf(",");
            } catch (NumberFormatException e) {
            }
        }
        if (!lst.isEmpty()) {
            try {
                if (Integer.parseInt(lst) == port) {
                    return true;
                }
            } catch (NumberFormatException e2) {
            }
        }
        return false;
    }

    private static boolean pathMatches(URI uri, HttpCookie cookie) {
        return normalizePath(uri.getPath()).startsWith(normalizePath(cookie.getPath()));
    }

    private static String normalizePath(String path) {
        if (path == null) {
            path = "";
        }
        if (path.endsWith("/")) {
            return path;
        }
        return path + "/";
    }

    private List<String> sortByPath(List<HttpCookie> cookies) {
        Collections.sort(cookies, new CookiePathComparator());
        StringBuilder result = new StringBuilder();
        int minVersion = 1;
        for (HttpCookie cookie : cookies) {
            if (cookie.getVersion() < minVersion) {
                minVersion = cookie.getVersion();
            }
        }
        if (minVersion == 1) {
            result.append("$Version=\"1\"; ");
        }
        for (int i = 0; i < cookies.size(); i++) {
            if (i != 0) {
                result.append("; ");
            }
            result.append(((HttpCookie) cookies.get(i)).toString());
        }
        List<String> cookieHeader = new ArrayList();
        cookieHeader.add(result.toString());
        return cookieHeader;
    }
}
