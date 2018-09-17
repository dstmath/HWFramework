package java.net;

import java.io.IOException;
import java.util.Objects;
import sun.net.util.IPAddressUtil;

public abstract class URLStreamHandler {
    protected abstract URLConnection openConnection(URL url) throws IOException;

    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    protected void parseURL(URL u, String spec, int start, int limit) {
        int i;
        int ind;
        String protocol = u.getProtocol();
        String authority = u.getAuthority();
        String userInfo = u.getUserInfo();
        String host = u.getHost();
        int port = u.getPort();
        String path = u.getPath();
        String query = u.getQuery();
        String ref = u.getRef();
        boolean querySet = false;
        if (start < limit) {
            int queryStart = spec.indexOf(63);
            if (queryStart == start) {
            }
            if (queryStart != -1 && queryStart < limit) {
                query = spec.substring(queryStart + 1, limit);
                if (limit > queryStart) {
                    limit = queryStart;
                }
                spec = spec.substring(0, queryStart);
                querySet = true;
            }
        }
        if (!false && start <= limit - 2 && spec.charAt(start) == '/') {
            if (spec.charAt(start + 1) == '/') {
                start += 2;
                i = spec.indexOf(47, start);
                if (i < 0 || i > limit) {
                    i = spec.indexOf(63, start);
                    if (i < 0 || i > limit) {
                        i = limit;
                    }
                }
                authority = spec.substring(start, i);
                host = authority;
                ind = authority.indexOf(64);
                if (ind == -1) {
                    userInfo = null;
                } else if (ind != authority.lastIndexOf(64)) {
                    userInfo = null;
                    host = null;
                } else {
                    userInfo = authority.substring(0, ind);
                    host = authority.substring(ind + 1);
                }
                if (host == null) {
                    host = "";
                } else if (host.length() <= 0 || host.charAt(0) != '[') {
                    ind = host.indexOf(58);
                    port = -1;
                    if (ind >= 0) {
                        if (host.length() > ind + 1) {
                            char firstPortChar = host.charAt(ind + 1);
                            if (firstPortChar < '0' || firstPortChar > '9') {
                                throw new IllegalArgumentException("invalid port: " + host.substring(ind + 1));
                            }
                            port = Integer.parseInt(host.substring(ind + 1));
                        }
                        host = host.substring(0, ind);
                    }
                } else {
                    ind = host.indexOf(93);
                    if (ind > 2) {
                        String nhost = host;
                        host = host.substring(0, ind + 1);
                        if (IPAddressUtil.isIPv6LiteralAddress(host.substring(1, ind))) {
                            port = -1;
                            if (nhost.length() > ind + 1) {
                                if (nhost.charAt(ind + 1) == ':') {
                                    ind++;
                                    if (nhost.length() > ind + 1) {
                                        port = Integer.parseInt(nhost.substring(ind + 1));
                                    }
                                } else {
                                    throw new IllegalArgumentException("Invalid authority field: " + authority);
                                }
                            }
                        }
                        throw new IllegalArgumentException("Invalid host: " + host);
                    }
                    throw new IllegalArgumentException("Invalid authority field: " + authority);
                }
                if (port < -1) {
                    throw new IllegalArgumentException("Invalid port number :" + port);
                }
                start = i;
                path = null;
                if (!querySet) {
                    query = null;
                }
            }
        }
        if (host == null) {
            host = "";
        }
        if (start < limit) {
            if (spec.charAt(start) == '/') {
                path = spec.substring(start, limit);
            } else if (path == null || path.length() <= 0) {
                path = (authority != null ? "/" : "") + spec.substring(start, limit);
            } else {
                ind = path.lastIndexOf(47);
                String seperator = "";
                if (ind == -1 && authority != null) {
                    seperator = "/";
                }
                path = path.substring(0, ind + 1) + seperator + spec.substring(start, limit);
            }
        }
        if (path == null) {
            path = "";
        }
        while (true) {
            i = path.indexOf("/./");
            if (i < 0) {
                break;
            }
            path = path.substring(0, i) + path.substring(i + 2);
        }
        i = 0;
        while (true) {
            i = path.indexOf("/../", i);
            if (i < 0) {
                break;
            } else if (i == 0) {
                path = path.substring(i + 3);
                i = 0;
            } else {
                if (i > 0) {
                    limit = path.lastIndexOf(47, i - 1);
                    if (limit >= 0 && path.indexOf("/../", limit) != 0) {
                        path = path.substring(0, limit) + path.substring(i + 3);
                        i = 0;
                    }
                }
                i += 3;
            }
        }
        while (path.endsWith("/..")) {
            limit = path.lastIndexOf(47, path.indexOf("/..") - 1);
            if (limit < 0) {
                break;
            }
            path = path.substring(0, limit + 1);
        }
        if (path.startsWith("./") && path.length() > 2) {
            path = path.substring(2);
        }
        if (path.endsWith("/.")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.endsWith("?")) {
            path = path.substring(0, path.length() - 1);
        }
        setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
    }

    protected int getDefaultPort() {
        return -1;
    }

    protected boolean equals(URL u1, URL u2) {
        if (Objects.equals(u1.getRef(), u2.getRef()) && Objects.equals(u1.getQuery(), u2.getQuery())) {
            return sameFile(u1, u2);
        }
        return false;
    }

    protected int hashCode(URL u) {
        return Objects.hash(u.getRef(), u.getQuery(), u.getProtocol(), u.getFile(), u.getHost(), Integer.valueOf(u.getPort()));
    }

    protected boolean sameFile(URL u1, URL u2) {
        boolean z;
        if (u1.getProtocol() == u2.getProtocol()) {
            z = true;
        } else if (u1.getProtocol() != null) {
            z = u1.getProtocol().equalsIgnoreCase(u2.getProtocol());
        } else {
            z = false;
        }
        if (!z) {
            return false;
        }
        if (u1.getFile() == u2.getFile()) {
            z = true;
        } else if (u1.getFile() != null) {
            z = u1.getFile().equals(u2.getFile());
        } else {
            z = false;
        }
        if (!z) {
            return false;
        }
        return (u1.getPort() != -1 ? u1.getPort() : u1.handler.getDefaultPort()) == (u2.getPort() != -1 ? u2.getPort() : u2.handler.getDefaultPort()) && hostsEqual(u1, u2);
    }

    /* JADX WARNING: Missing block: B:14:0x001a, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized InetAddress getHostAddress(URL u) {
        if (u.hostAddress != null) {
            return u.hostAddress;
        }
        String host = u.getHost();
        if (host != null && !host.equals("")) {
            try {
                u.hostAddress = InetAddress.getByName(host);
                return u.hostAddress;
            } catch (UnknownHostException e) {
                return null;
            } catch (SecurityException e2) {
                return null;
            }
        }
    }

    protected boolean hostsEqual(URL u1, URL u2) {
        boolean z = false;
        if (u1.getHost() != null && u2.getHost() != null) {
            return u1.getHost().equalsIgnoreCase(u2.getHost());
        }
        if (u1.getHost() == null && u2.getHost() == null) {
            z = true;
        }
        return z;
    }

    protected String toExternalForm(URL u) {
        int len = u.getProtocol().length() + 1;
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            len += u.getAuthority().length() + 2;
        }
        if (u.getPath() != null) {
            len += u.getPath().length();
        }
        if (u.getQuery() != null) {
            len += u.getQuery().length() + 1;
        }
        if (u.getRef() != null) {
            len += u.getRef().length() + 1;
        }
        StringBuilder result = new StringBuilder(len);
        result.append(u.getProtocol());
        result.append(":");
        if (u.getAuthority() != null) {
            result.append("//");
            result.append(u.getAuthority());
        }
        String fileAndQuery = u.getFile();
        if (fileAndQuery != null) {
            result.append(fileAndQuery);
        }
        if (u.getRef() != null) {
            result.append("#");
            result.append(u.getRef());
        }
        return result.toString();
    }

    protected void setURL(URL u, String protocol, String host, int port, String authority, String userInfo, String path, String query, String ref) {
        if (this != u.handler) {
            throw new SecurityException("handler for url different from this handler");
        }
        u.set(u.getProtocol(), host, port, authority, userInfo, path, query, ref);
    }

    @Deprecated
    protected void setURL(URL u, String protocol, String host, int port, String file, String ref) {
        String authority = null;
        String userInfo = null;
        if (!(host == null || host.length() == 0)) {
            authority = port == -1 ? host : host + ":" + port;
            int at = host.lastIndexOf(64);
            if (at != -1) {
                userInfo = host.substring(0, at);
                host = host.substring(at + 1);
            }
        }
        String path = null;
        String query = null;
        if (file != null) {
            int q = file.lastIndexOf(63);
            if (q != -1) {
                query = file.substring(q + 1);
                path = file.substring(0, q);
            } else {
                path = file;
            }
        }
        setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
    }
}
