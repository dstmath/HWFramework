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
    private CryptoPermissions appPerms;
    private URL jarURL;
    private boolean savePerms;

    /* renamed from: javax.crypto.JarVerifier.1 */
    class AnonymousClass1 implements PrivilegedExceptionAction {
        final /* synthetic */ URL val$url;

        AnonymousClass1(URL val$url) {
            this.val$url = val$url;
        }

        public Object run() throws Exception {
            JarURLConnection conn = (JarURLConnection) this.val$url.openConnection();
            conn.setUseCaches(false);
            return conn.getJarFile();
        }
    }

    JarVerifier(URL jarURL, boolean savePerms) {
        this.appPerms = null;
        this.jarURL = jarURL;
        this.savePerms = savePerms;
    }

    void verify() throws JarException, IOException {
        if (this.savePerms) {
            URL url = this.jarURL.getProtocol().equalsIgnoreCase("jar") ? this.jarURL : new URL("jar:" + this.jarURL.toString() + "!/");
            JarFile jf = null;
            try {
                jf = (JarFile) AccessController.doPrivileged(new AnonymousClass1(url));
                if (jf != null) {
                    JarEntry je = jf.getJarEntry("cryptoPerms");
                    if (je == null) {
                        throw new JarException("Can not find cryptoPerms");
                    }
                    this.appPerms = new CryptoPermissions();
                    this.appPerms.load(jf.getInputStream(je));
                }
                if (jf != null) {
                    jf.close();
                }
            } catch (Exception ex) {
                JarException jex = new JarException("Cannot load/parse" + this.jarURL.toString());
                jex.initCause(ex);
                throw jex;
            } catch (PrivilegedActionException pae) {
                SecurityException se = new SecurityException("Cannot load " + url.toString());
                se.initCause(pae);
                throw se;
            } catch (Throwable th) {
                if (jf != null) {
                    jf.close();
                }
            }
        }
    }

    static void verifyPolicySigned(Certificate[] certs) throws Exception {
    }

    CryptoPermissions getPermissions() {
        return this.appPerms;
    }
}
