package sun.net.www.protocol.file;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import sun.net.www.ParseUtil;

public class Handler extends URLStreamHandler {
    private String getHost(URL url) {
        String host = url.getHost();
        if (host == null) {
            return "";
        }
        return host;
    }

    /* access modifiers changed from: protected */
    public void parseURL(URL u, String spec, int start, int limit) {
        super.parseURL(u, spec.replace(File.separatorChar, '/'), start, limit);
    }

    public synchronized URLConnection openConnection(URL u) throws IOException {
        return openConnection(u, null);
    }

    public synchronized URLConnection openConnection(URL u, Proxy p) throws IOException {
        URLConnection uc;
        String str;
        String host = u.getHost();
        if (host == null || host.equals("") || host.equals("~") || host.equalsIgnoreCase("localhost")) {
            return createFileURLConnection(u, new File(ParseUtil.decode(u.getPath())));
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(u.getFile());
            if (u.getRef() == null) {
                str = "";
            } else {
                str = "#" + u.getRef();
            }
            sb.append(str);
            URL ru = new URL("ftp", host, sb.toString());
            if (p != null) {
                uc = ru.openConnection(p);
            } else {
                uc = ru.openConnection();
            }
        } catch (IOException e) {
            uc = null;
        }
        URLConnection uc2 = uc;
        if (uc2 != null) {
            return uc2;
        }
        throw new IOException("Unable to connect to: " + u.toExternalForm());
    }

    /* access modifiers changed from: protected */
    public URLConnection createFileURLConnection(URL u, File file) {
        return new FileURLConnection(u, file);
    }

    /* access modifiers changed from: protected */
    public boolean hostsEqual(URL u1, URL u2) {
        String s1 = u1.getHost();
        String s2 = u2.getHost();
        if ("localhost".equalsIgnoreCase(s1) && (s2 == null || "".equals(s2))) {
            return true;
        }
        if (!"localhost".equalsIgnoreCase(s2) || (s1 != null && !"".equals(s1))) {
            return super.hostsEqual(u1, u2);
        }
        return true;
    }
}
