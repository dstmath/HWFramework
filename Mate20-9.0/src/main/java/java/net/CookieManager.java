package java.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.util.logging.PlatformLogger;

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
            if (c2Path.startsWith(c1Path)) {
                return 1;
            }
            return 0;
        }
    }

    public CookieManager() {
        this(null, null);
    }

    public CookieManager(CookieStore store, CookiePolicy cookiePolicy) {
        CookiePolicy cookiePolicy2;
        this.cookieJar = null;
        if (cookiePolicy == null) {
            cookiePolicy2 = CookiePolicy.ACCEPT_ORIGINAL_SERVER;
        } else {
            cookiePolicy2 = cookiePolicy;
        }
        this.policyCallback = cookiePolicy2;
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
        Map<String, List<String>> cookieMap = new HashMap<>();
        if (this.cookieJar == null) {
            return Collections.unmodifiableMap(cookieMap);
        }
        boolean secureLink = "https".equalsIgnoreCase(uri.getScheme());
        List<HttpCookie> cookies = new ArrayList<>();
        for (HttpCookie cookie : this.cookieJar.get(uri)) {
            if (pathMatches(uri, cookie) && (secureLink || !cookie.getSecure())) {
                String ports = cookie.getPortlist();
                if (ports == null || ports.isEmpty()) {
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
        List<HttpCookie> cookies;
        if (uri == null || responseHeaders == null) {
            throw new IllegalArgumentException("Argument is null");
        } else if (this.cookieJar != null) {
            PlatformLogger logger = PlatformLogger.getLogger("java.net.CookieManager");
            for (String headerKey : responseHeaders.keySet()) {
                if (headerKey != null && (headerKey.equalsIgnoreCase("Set-Cookie2") || headerKey.equalsIgnoreCase("Set-Cookie"))) {
                    for (String headerValue : responseHeaders.get(headerKey)) {
                        try {
                            cookies = HttpCookie.parse(headerValue);
                        } catch (IllegalArgumentException e) {
                            try {
                                List<HttpCookie> cookies2 = Collections.emptyList();
                                if (logger.isLoggable(PlatformLogger.Level.SEVERE)) {
                                    logger.severe("Invalid cookie for " + uri + ": " + headerValue);
                                }
                                cookies = cookies2;
                            } catch (IllegalArgumentException e2) {
                            }
                        }
                        for (HttpCookie cookie : cookies) {
                            if (cookie.getPath() == null) {
                                String path = uri.getPath();
                                if (!path.endsWith("/")) {
                                    int i = path.lastIndexOf("/");
                                    if (i > 0) {
                                        path = path.substring(0, i + 1);
                                    } else {
                                        path = "/";
                                    }
                                }
                                cookie.setPath(path);
                            } else if (!pathMatches(uri, cookie)) {
                            }
                            if (cookie.getDomain() == null) {
                                String host = uri.getHost();
                                if (host != null && !host.contains(".")) {
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

    /* access modifiers changed from: private */
    public static String normalizePath(String path) {
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
            result.append(cookies.get(i).toString());
        }
        List<String> cookieHeader = new ArrayList<>();
        cookieHeader.add(result.toString());
        return cookieHeader;
    }
}
