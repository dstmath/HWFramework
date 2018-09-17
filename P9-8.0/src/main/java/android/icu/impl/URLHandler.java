package android.icu.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class URLHandler {
    private static final boolean DEBUG = ICUDebug.enabled("URLHandler");
    public static final String PROPNAME = "urlhandler.props";
    private static final Map<String, Method> handlers;

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
            if (this.file == null || (this.file.exists() ^ 1) != 0) {
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
            if (files != null) {
                for (File f : files) {
                    if (!f.isDirectory()) {
                        v.visit(strip ? f.getName() : path + f.getName());
                    } else if (recurse) {
                        process(v, recurse, strip, path + f.getName() + '/', f.listFiles());
                    }
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
                            if (ix <= 0 || (recurse ^ 1) == 0) {
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

    public abstract void guide(URLVisitor uRLVisitor, boolean z, boolean z2);

    /* JADX WARNING: Removed duplicated region for block: B:74:0x0117 A:{Catch:{ Throwable -> 0x00e3, all -> 0x0109 }} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0100 A:{Catch:{ Throwable -> 0x00e3, all -> 0x0109 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static {
        ClassNotFoundException e;
        NoSuchMethodException e2;
        SecurityException e3;
        Throwable t;
        Throwable th;
        Map h = null;
        BufferedReader br = null;
        try {
            InputStream is = ClassLoaderUtil.getClassLoader(URLHandler.class).getResourceAsStream(PROPNAME);
            if (is != null) {
                Class<?>[] params = new Class[]{URL.class};
                BufferedReader br2 = new BufferedReader(new InputStreamReader(is));
                try {
                    String line = br2.readLine();
                    Map<String, Method> h2 = null;
                    while (line != null) {
                        Map<String, Method> h3;
                        try {
                            line = line.trim();
                            if (line.length() == 0 || line.charAt(0) == '#') {
                                h = h2;
                            } else {
                                int ix = line.indexOf(61);
                                if (ix == -1) {
                                    if (DEBUG) {
                                        System.err.println("bad urlhandler line: '" + line + "'");
                                    }
                                    br2.close();
                                    br = br2;
                                    h = h2;
                                } else {
                                    String key = line.substring(0, ix).trim();
                                    try {
                                        Method m = Class.forName(line.substring(ix + 1).trim()).getDeclaredMethod("get", params);
                                        if (h2 == null) {
                                            h = new HashMap();
                                        } else {
                                            h3 = h2;
                                        }
                                        try {
                                            h.put(key, m);
                                        } catch (ClassNotFoundException e4) {
                                            e = e4;
                                        } catch (NoSuchMethodException e5) {
                                            e2 = e5;
                                            if (DEBUG) {
                                            }
                                            line = br2.readLine();
                                            h2 = h;
                                        } catch (SecurityException e6) {
                                            e3 = e6;
                                            if (DEBUG) {
                                            }
                                            line = br2.readLine();
                                            h2 = h;
                                        }
                                    } catch (ClassNotFoundException e7) {
                                        e = e7;
                                        h3 = h2;
                                        if (DEBUG) {
                                            System.err.println(e);
                                        }
                                        line = br2.readLine();
                                        h2 = h;
                                    } catch (NoSuchMethodException e8) {
                                        e2 = e8;
                                        h = h2;
                                        if (DEBUG) {
                                            System.err.println(e2);
                                        }
                                        line = br2.readLine();
                                        h2 = h;
                                    } catch (SecurityException e9) {
                                        e3 = e9;
                                        h = h2;
                                        if (DEBUG) {
                                            System.err.println(e3);
                                        }
                                        line = br2.readLine();
                                        h2 = h;
                                    }
                                }
                            }
                            line = br2.readLine();
                            h2 = h;
                        } catch (Throwable th2) {
                            th = th2;
                            br = br2;
                        }
                    }
                    br2.close();
                    br = br2;
                    h = h2;
                } catch (Throwable th3) {
                    th = th3;
                    br = br2;
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e10) {
                }
            }
        } catch (Throwable th4) {
            t = th4;
            try {
                if (DEBUG) {
                    System.err.println(t);
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e11) {
                    }
                }
                handlers = h;
            } catch (Throwable th5) {
                th = th5;
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e12) {
                    }
                }
                throw th;
            }
        }
        handlers = h;
    }

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
