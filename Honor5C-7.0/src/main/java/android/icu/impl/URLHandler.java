package android.icu.impl;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class URLHandler {
    private static final boolean DEBUG = false;
    public static final String PROPNAME = "urlhandler.props";
    private static final Map<String, Method> handlers = null;

    public interface URLVisitor {
        void visit(String str);
    }

    private static class FileURLHandler extends URLHandler {
        File file;

        FileURLHandler(URL url) {
            try {
                this.file = new File(url.toURI());
            } catch (URISyntaxException e) {
            }
            if (this.file == null || !this.file.exists()) {
                if (URLHandler.DEBUG) {
                    System.err.println("file does not exist - " + url.toString());
                }
                throw new IllegalArgumentException();
            }
        }

        public void guide(URLVisitor v, boolean recurse, boolean strip) {
            if (this.file.isDirectory()) {
                URLVisitor uRLVisitor = v;
                boolean z = recurse;
                boolean z2 = strip;
                process(uRLVisitor, z, z2, "/", this.file.listFiles());
                return;
            }
            v.visit(this.file.getName());
        }

        private void process(URLVisitor v, boolean recurse, boolean strip, String path, File[] files) {
            for (File f : files) {
                if (!f.isDirectory()) {
                    v.visit(strip ? f.getName() : path + f.getName());
                } else if (recurse) {
                    process(v, recurse, strip, path + f.getName() + '/', f.listFiles());
                }
            }
        }
    }

    private static class JarURLHandler extends URLHandler {
        JarFile jarFile;
        String prefix;

        JarURLHandler(URL url) {
            try {
                this.prefix = url.getPath();
                int ix = this.prefix.lastIndexOf("!/");
                if (ix >= 0) {
                    this.prefix = this.prefix.substring(ix + 2);
                }
                if (!url.getProtocol().equals("jar")) {
                    String urlStr = url.toString();
                    int idx = urlStr.indexOf(":");
                    if (idx != -1) {
                        url = new URL("jar" + urlStr.substring(idx));
                    }
                }
                this.jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            } catch (Exception e) {
                if (URLHandler.DEBUG) {
                    System.err.println("icurb jar error: " + e);
                }
                throw new IllegalArgumentException("jar error: " + e.getMessage());
            }
        }

        public void guide(URLVisitor v, boolean recurse, boolean strip) {
            try {
                Enumeration<JarEntry> entries = this.jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    if (!entry.isDirectory()) {
                        String name = entry.getName();
                        if (name.startsWith(this.prefix)) {
                            name = name.substring(this.prefix.length());
                            int ix = name.lastIndexOf(47);
                            if (ix <= 0 || recurse) {
                                if (strip && ix != -1) {
                                    name = name.substring(ix + 1);
                                }
                                v.visit(name);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (URLHandler.DEBUG) {
                    System.err.println("icurb jar error: " + e);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.URLHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.URLHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.URLHandler.<clinit>():void");
    }

    public abstract void guide(URLVisitor uRLVisitor, boolean z, boolean z2);

    public static URLHandler get(URL url) {
        if (url == null) {
            return null;
        }
        String protocol = url.getProtocol();
        if (handlers != null) {
            Method m = (Method) handlers.get(protocol);
            if (m != null) {
                try {
                    URLHandler handler = (URLHandler) m.invoke(null, new Object[]{url});
                    if (handler != null) {
                        return handler;
                    }
                } catch (IllegalAccessException e) {
                    if (DEBUG) {
                        System.err.println(e);
                    }
                } catch (IllegalArgumentException e2) {
                    if (DEBUG) {
                        System.err.println(e2);
                    }
                } catch (InvocationTargetException e3) {
                    if (DEBUG) {
                        System.err.println(e3);
                    }
                }
            }
        }
        return getDefault(url);
    }

    protected static URLHandler getDefault(URL url) {
        String protocol = url.getProtocol();
        try {
            if (protocol.equals("file")) {
                return new FileURLHandler(url);
            }
            if (protocol.equals("jar") || protocol.equals("wsjar")) {
                return new JarURLHandler(url);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void guide(URLVisitor visitor, boolean recurse) {
        guide(visitor, recurse, true);
    }
}
