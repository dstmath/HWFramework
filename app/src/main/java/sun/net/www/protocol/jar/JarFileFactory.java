package sun.net.www.protocol.jar;

import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.HashMap;
import java.util.jar.JarFile;
import sun.net.util.URLUtil;
import sun.net.www.protocol.jar.URLJarFile.URLJarFileCloseController;
import sun.security.util.SecurityConstants;

class JarFileFactory implements URLJarFileCloseController {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final HashMap<String, JarFile> fileCache = null;
    private static final JarFileFactory instance = null;
    private static final HashMap<JarFile, URL> urlCache = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.protocol.jar.JarFileFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.protocol.jar.JarFileFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.protocol.jar.JarFileFactory.<clinit>():void");
    }

    private JarFileFactory() {
    }

    public static JarFileFactory getInstance() {
        return instance;
    }

    URLConnection getConnection(JarFile jarFile) throws IOException {
        synchronized (instance) {
            URL u = (URL) urlCache.get(jarFile);
        }
        if (u != null) {
            return u.openConnection();
        }
        return null;
    }

    public JarFile get(URL url) throws IOException {
        return get(url, true);
    }

    JarFile get(URL url, boolean useCaches) throws IOException {
        JarFile result;
        if (useCaches) {
            synchronized (instance) {
                result = getCachedJarFile(url);
            }
            if (result == null) {
                JarFile local_result = URLJarFile.getJarFile(url, this);
                synchronized (instance) {
                    result = getCachedJarFile(url);
                    if (result == null) {
                        fileCache.put(URLUtil.urlNoFragString(url), local_result);
                        urlCache.put(local_result, url);
                        result = local_result;
                    } else if (local_result != null) {
                        local_result.close();
                    }
                }
            }
        } else {
            result = URLJarFile.getJarFile(url, this);
        }
        if (result != null) {
            return result;
        }
        throw new FileNotFoundException(url.toString());
    }

    public void close(JarFile jarFile) {
        synchronized (instance) {
            URL urlRemoved = (URL) urlCache.remove(jarFile);
            if (urlRemoved != null) {
                fileCache.remove(URLUtil.urlNoFragString(urlRemoved));
            }
        }
    }

    private JarFile getCachedJarFile(URL url) {
        if (-assertionsDisabled || Thread.holdsLock(instance)) {
            JarFile result = (JarFile) fileCache.get(URLUtil.urlNoFragString(url));
            if (result != null) {
                Permission perm = getPermission(result);
                if (perm != null) {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        try {
                            sm.checkPermission(perm);
                        } catch (SecurityException se) {
                            if ((perm instanceof FilePermission) && perm.getActions().indexOf(SecurityConstants.PROPERTY_READ_ACTION) != -1) {
                                sm.checkRead(perm.getName());
                            } else if (!(perm instanceof SocketPermission) || perm.getActions().indexOf(SecurityConstants.SOCKET_CONNECT_ACTION) == -1) {
                                throw se;
                            } else {
                                sm.checkConnect(url.getHost(), url.getPort());
                            }
                        }
                    }
                }
            }
            return result;
        }
        throw new AssertionError();
    }

    private Permission getPermission(JarFile jarFile) {
        try {
            URLConnection uc = getConnection(jarFile);
            if (uc != null) {
                return uc.getPermission();
            }
        } catch (IOException e) {
        }
        return null;
    }
}
