package sun.net.www.protocol.netdoc;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.AccessController;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;

public class Handler extends URLStreamHandler {
    static URL base;

    public synchronized URLConnection openConnection(URL u) throws IOException {
        URLConnection uc;
        uc = null;
        boolean localonly = ((Boolean) AccessController.doPrivileged(new GetBooleanAction("newdoc.localonly"))).booleanValue();
        String docurl = (String) AccessController.doPrivileged(new GetPropertyAction("doc.url"));
        String file = u.getFile();
        if (!localonly) {
            URL url;
            try {
                if (base == null) {
                    base = new URL(docurl);
                }
                url = new URL(base, file);
            } catch (MalformedURLException e) {
                url = null;
            }
            if (url != null) {
                uc = url.openConnection();
            }
        }
        if (uc == null) {
            try {
                uc = new URL("file", "~", file).openConnection();
                InputStream inputStream = uc.getInputStream();
            } catch (MalformedURLException e2) {
                uc = null;
            } catch (IOException e3) {
                uc = null;
            }
        }
        if (uc == null) {
            throw new IOException("Can't find file for URL: " + u.toExternalForm());
        }
        return uc;
    }
}
