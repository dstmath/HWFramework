package java.net;

import java.io.IOException;
import java.util.Objects;
import sun.net.util.IPAddressUtil;

public abstract class URLStreamHandler {
    /* access modifiers changed from: protected */
    public abstract URLConnection openConnection(URL url) throws IOException;

    /* access modifiers changed from: protected */
    public URLConnection openConnection(URL u, Proxy p) throws IOException {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x029d  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x02a8 A[LOOP:1: B:116:0x029f->B:118:0x02a8, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x02cd  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0313  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x034b  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x0357  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0361  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x036d  */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x02c3 A[EDGE_INSN: B:156:0x02c3->B:119:0x02c3 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x030b A[EDGE_INSN: B:159:0x030b->B:131:0x030b ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x022a  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x022f  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0233  */
    public void parseURL(URL u, String spec, int start, int limit) {
        String authority;
        int port;
        String userInfo;
        String query;
        int start2;
        String path;
        String host;
        int indexOf;
        int i;
        int indexOf2;
        int i2;
        int i3;
        int i4;
        String path2;
        String path3;
        String userInfo2;
        String authority2;
        String userInfo3;
        String spec2 = spec;
        int i5 = start;
        int limit2 = limit;
        String protocol = u.getProtocol();
        String authority3 = u.getAuthority();
        String userInfo4 = u.getUserInfo();
        String host2 = u.getHost();
        int port2 = u.getPort();
        String path4 = u.getPath();
        String query2 = u.getQuery();
        String ref = u.getRef();
        boolean isRelPath = false;
        boolean queryOnly = false;
        boolean querySet = false;
        if (i5 < limit2) {
            int queryStart = spec2.indexOf(63);
            queryOnly = queryStart == i5;
            if (queryStart != -1 && queryStart < limit2) {
                query2 = spec2.substring(queryStart + 1, limit2);
                if (limit2 > queryStart) {
                    limit2 = queryStart;
                }
                spec2 = spec2.substring(0, queryStart);
                querySet = true;
            }
        }
        boolean querySet2 = querySet;
        String spec3 = spec2;
        if (0 != 0 || i5 > limit2 - 2) {
            authority2 = authority3;
            userInfo2 = userInfo4;
            port = port2;
            path3 = path4;
        } else {
            authority2 = authority3;
            if (spec3.charAt(i5) == '/' && spec3.charAt(i5 + 1) == '/') {
                int start3 = i5 + 2;
                int i6 = start3;
                while (true) {
                    if (i6 >= limit2) {
                        break;
                    }
                    char charAt = spec3.charAt(i6);
                    String userInfo5 = userInfo4;
                    if (charAt == '#' || charAt == '/' || charAt == '?' || charAt == '\\') {
                        break;
                    }
                    i6++;
                    userInfo4 = userInfo5;
                }
                String host3 = spec3.substring(start3, i6);
                String authority4 = host3;
                int i7 = start3;
                int start4 = authority4.indexOf(64);
                if (start4 == -1) {
                    userInfo3 = null;
                } else if (start4 != authority4.lastIndexOf(64)) {
                    userInfo3 = null;
                    host3 = null;
                } else {
                    String userInfo6 = authority4.substring(0, start4);
                    host3 = authority4.substring(start4 + 1);
                    userInfo3 = userInfo6;
                }
                if (host3 != null) {
                    int i8 = start4;
                    if (host3.length() > 0) {
                        userInfo = userInfo3;
                        if (host3.charAt(0) == '[') {
                            int indexOf3 = host3.indexOf(93);
                            int ind = indexOf3;
                            int i9 = port2;
                            if (indexOf3 > 2) {
                                String nhost = host3;
                                String str = path4;
                                String host4 = nhost.substring(0, ind + 1);
                                if (IPAddressUtil.isIPv6LiteralAddress(host4.substring(1, ind))) {
                                    if (nhost.length() > ind + 1) {
                                        if (nhost.charAt(ind + 1) == ':') {
                                            ind++;
                                            if (nhost.length() > ind + 1) {
                                                port2 = Integer.parseInt(nhost.substring(ind + 1));
                                                int i10 = ind;
                                                host2 = host4;
                                                authority = authority4;
                                            }
                                        } else {
                                            StringBuilder sb = new StringBuilder();
                                            String str2 = nhost;
                                            sb.append("Invalid authority field: ");
                                            sb.append(authority4);
                                            throw new IllegalArgumentException(sb.toString());
                                        }
                                    }
                                    int i11 = ind;
                                    port2 = -1;
                                    host2 = host4;
                                    authority = authority4;
                                } else {
                                    String str3 = nhost;
                                    throw new IllegalArgumentException("Invalid host: " + host4);
                                }
                            } else {
                                throw new IllegalArgumentException("Invalid authority field: " + authority4);
                            }
                        } else {
                            String str4 = path4;
                        }
                    } else {
                        userInfo = userInfo3;
                        int i12 = port2;
                        String str5 = path4;
                    }
                    int ind2 = host3.indexOf(58);
                    port2 = -1;
                    if (ind2 >= 0) {
                        if (host3.length() > ind2 + 1) {
                            char firstPortChar = host3.charAt(ind2 + 1);
                            if (firstPortChar < '0' || firstPortChar > '9') {
                                String str6 = authority4;
                                StringBuilder sb2 = new StringBuilder();
                                char c = firstPortChar;
                                sb2.append("invalid port: ");
                                sb2.append(host3.substring(ind2 + 1));
                                throw new IllegalArgumentException(sb2.toString());
                            }
                            port2 = Integer.parseInt(host3.substring(ind2 + 1));
                            authority = authority4;
                        } else {
                            authority = authority4;
                        }
                        host2 = host3.substring(0, ind2);
                    } else {
                        authority = authority4;
                        host2 = host3;
                    }
                } else {
                    authority = authority4;
                    userInfo = userInfo3;
                    int i13 = port2;
                    String str7 = path4;
                    host2 = "";
                    int i14 = start4;
                }
                if (port2 >= -1) {
                    int start5 = i6;
                    path = null;
                    if (!querySet2) {
                        query = null;
                        start2 = start5;
                        port = port2;
                    } else {
                        port = port2;
                        query = query2;
                        start2 = start5;
                    }
                    if (host2 != null) {
                        host = "";
                    } else {
                        host = host2;
                    }
                    if (start2 < limit2) {
                        if (spec3.charAt(start2) == '/' || spec3.charAt(start2) == '\\') {
                            path = spec3.substring(start2, limit2);
                        } else if (path == null || path.length() <= 0) {
                            String seperator = authority != null ? "/" : "";
                            path = seperator + spec3.substring(start2, limit2);
                        } else {
                            isRelPath = true;
                            String seperator2 = "";
                            if (path.lastIndexOf(47) == -1 && authority != null) {
                                seperator2 = "/";
                            }
                            path = path.substring(0, ind + 1) + seperator2 + spec3.substring(start2, limit2);
                        }
                    }
                    if (path == null) {
                        path = "";
                    }
                    while (true) {
                        indexOf = path.indexOf("/./");
                        int i15 = indexOf;
                        if (indexOf >= 0) {
                            break;
                        }
                        path = path.substring(0, i15) + path.substring(i15 + 2);
                    }
                    i = 0;
                    while (true) {
                        indexOf2 = path.indexOf("/../", i);
                        i2 = indexOf2;
                        if (indexOf2 >= 0) {
                            break;
                        } else if (i2 == 0) {
                            path = path.substring(i2 + 3);
                            i = 0;
                        } else {
                            if (i2 > 0) {
                                int lastIndexOf = path.lastIndexOf(47, i2 - 1);
                                limit2 = lastIndexOf;
                                if (lastIndexOf >= 0 && path.indexOf("/../", limit2) != 0) {
                                    path = path.substring(0, limit2) + path.substring(i2 + 3);
                                    i = 0;
                                }
                            }
                            i = i2 + 3;
                        }
                    }
                    while (path.endsWith("/..")) {
                        i2 = path.indexOf("/..");
                        int lastIndexOf2 = path.lastIndexOf(47, i2 - 1);
                        limit2 = lastIndexOf2;
                        if (lastIndexOf2 < 0) {
                            break;
                        }
                        path = path.substring(0, limit2 + 1);
                    }
                    int i16 = i2;
                    int i17 = limit2;
                    if (path.startsWith("./") && path.length() > 2) {
                        path = path.substring(2);
                    }
                    if (!path.endsWith("/.")) {
                        i4 = 1;
                        i3 = 0;
                        path = path.substring(0, path.length() - 1);
                    } else {
                        i4 = 1;
                        i3 = 0;
                    }
                    if (!path.endsWith("?")) {
                        path2 = path.substring(i3, path.length() - i4);
                    } else {
                        path2 = path;
                    }
                    int i18 = start2;
                    setURL(u, protocol, host, port, authority, userInfo, path2, query, ref);
                }
                throw new IllegalArgumentException("Invalid port number :" + port2);
            }
            userInfo2 = userInfo4;
            port = port2;
            path3 = path4;
        }
        query = query2;
        authority = authority2;
        userInfo = userInfo2;
        path = path3;
        start2 = i5;
        if (host2 != null) {
        }
        if (start2 < limit2) {
        }
        if (path == null) {
        }
        while (true) {
            indexOf = path.indexOf("/./");
            int i152 = indexOf;
            if (indexOf >= 0) {
            }
            path = path.substring(0, i152) + path.substring(i152 + 2);
        }
        i = 0;
        while (true) {
            indexOf2 = path.indexOf("/../", i);
            i2 = indexOf2;
            if (indexOf2 >= 0) {
            }
        }
        while (path.endsWith("/..")) {
        }
        int i162 = i2;
        int i172 = limit2;
        path = path.substring(2);
        if (!path.endsWith("/.")) {
        }
        if (!path.endsWith("?")) {
        }
        int i182 = start2;
        setURL(u, protocol, host, port, authority, userInfo, path2, query, ref);
    }

    /* access modifiers changed from: protected */
    public int getDefaultPort() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public boolean equals(URL u1, URL u2) {
        return Objects.equals(u1.getRef(), u2.getRef()) && Objects.equals(u1.getQuery(), u2.getQuery()) && sameFile(u1, u2);
    }

    /* access modifiers changed from: protected */
    public int hashCode(URL u) {
        return Objects.hash(u.getRef(), u.getQuery(), u.getProtocol(), u.getFile(), u.getHost(), Integer.valueOf(u.getPort()));
    }

    /* access modifiers changed from: protected */
    public boolean sameFile(URL u1, URL u2) {
        if (u1.getProtocol() != u2.getProtocol() && (u1.getProtocol() == null || !u1.getProtocol().equalsIgnoreCase(u2.getProtocol()))) {
            return false;
        }
        if (u1.getFile() != u2.getFile() && (u1.getFile() == null || !u1.getFile().equals(u2.getFile()))) {
            return false;
        }
        if ((u1.getPort() != -1 ? u1.getPort() : u1.handler.getDefaultPort()) == (u2.getPort() != -1 ? u2.getPort() : u2.handler.getDefaultPort()) && hostsEqual(u1, u2)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x002b, code lost:
        return null;
     */
    public synchronized InetAddress getHostAddress(URL u) {
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

    /* access modifiers changed from: protected */
    public boolean hostsEqual(URL u1, URL u2) {
        if (u1.getHost() != null && u2.getHost() != null) {
            return u1.getHost().equalsIgnoreCase(u2.getHost());
        }
        return u1.getHost() == null && u2.getHost() == null;
    }

    /* access modifiers changed from: protected */
    public String toExternalForm(URL u) {
        int len = u.getProtocol().length() + 1;
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            len += 2 + u.getAuthority().length();
        }
        if (u.getPath() != null) {
            len += u.getPath().length();
        }
        if (u.getQuery() != null) {
            len += u.getQuery().length() + 1;
        }
        if (u.getRef() != null) {
            len += 1 + u.getRef().length();
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

    /* access modifiers changed from: protected */
    public void setURL(URL u, String protocol, String host, int port, String authority, String userInfo, String path, String query, String ref) {
        URL url = u;
        if (this == url.handler) {
            url.set(url.getProtocol(), host, port, authority, userInfo, path, query, ref);
            return;
        }
        throw new SecurityException("handler for url different from this handler");
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void setURL(URL u, String protocol, String host, int port, String file, String ref) {
        String str;
        String str2 = host;
        int i = port;
        String str3 = file;
        String authority = null;
        String userInfo = null;
        if (!(str2 == null || host.length() == 0)) {
            if (i == -1) {
                str = str2;
            } else {
                str = str2 + ":" + i;
            }
            authority = str;
            int at = str2.lastIndexOf(64);
            if (at != -1) {
                userInfo = str2.substring(0, at);
                str2 = str2.substring(at + 1);
            }
        }
        String host2 = str2;
        String authority2 = authority;
        String userInfo2 = userInfo;
        String path = null;
        String query = null;
        if (str3 != null) {
            int q = str3.lastIndexOf(63);
            if (q != -1) {
                query = str3.substring(q + 1);
                path = str3.substring(0, q);
            } else {
                path = str3;
            }
        }
        setURL(u, protocol, host2, i, authority2, userInfo2, path, query, ref);
    }
}
