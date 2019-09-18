package javax.crypto;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;

final class JarVerifier {
    private CryptoPermissions appPerms = null;
    private URL jarURL;
    private boolean savePerms;

    JarVerifier(URL jarURL2, boolean savePerms2) {
        this.jarURL = jarURL2;
        this.savePerms = savePerms2;
    }

    /* access modifiers changed from: package-private */
    public void verify() throws JarException, IOException {
        final URL url;
        if (this.savePerms) {
            if (this.jarURL.getProtocol().equalsIgnoreCase("jar")) {
                url = this.jarURL;
            } else {
                url = new URL("jar:" + this.jarURL.toString() + "!/");
            }
            JarFile jf = null;
            try {
                jf = (JarFile) AccessController.doPrivileged(new PrivilegedExceptionAction<JarFile>() {
                    public JarFile run() throws Exception {
                        JarURLConnection conn = (JarURLConnection) url.openConnection();
                        conn.setUseCaches(false);
                        return conn.getJarFile();
                    }
                });
                if (jf != null) {
                    JarEntry je = jf.getJarEntry("cryptoPerms");
                    if (je != null) {
                        this.appPerms = new CryptoPermissions();
                        this.appPerms.load(jf.getInputStream(je));
                    } else {
                        throw new JarException("Can not find cryptoPerms");
                    }
                }
                if (jf != null) {
                    jf.close();
                }
            } catch (PrivilegedActionException pae) {
                throw new SecurityException("Cannot load " + url.toString(), pae);
            } catch (Exception ex) {
                JarException jex = new JarException("Cannot load/parse" + this.jarURL.toString());
                jex.initCause(ex);
                throw jex;
            } catch (Throwable th) {
                if (jf != null) {
                    jf.close();
                }
                throw th;
            }
        }
    }

    static void verifyPolicySigned(Certificate[] certs) throws Exception {
    }

    /* access modifiers changed from: package-private */
    public CryptoPermissions getPermissions() {
        return this.appPerms;
    }
}
