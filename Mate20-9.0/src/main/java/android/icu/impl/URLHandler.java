package android.icu.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
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
    /* access modifiers changed from: private */
    public static final boolean DEBUG = ICUDebug.enabled("URLHandler");
    public static final String PROPNAME = "urlhandler.props";
    private static final Map<String, Method> handlers;

    private static class FileURLHandler extends URLHandler {
        File file;

        FileURLHandler(URL url) {
            try {
                this.file = new File(url.toURI());
            } catch (URISyntaxException e) {
            }
            if (this.file == null || !this.file.exists()) {
                if (URLHandler.DEBUG) {
                    PrintStream printStream = System.err;
                    printStream.println("file does not exist - " + url.toString());
                }
                throw new IllegalArgumentException();
            }
        }

        public void guide(URLVisitor v, boolean recurse, boolean strip) {
            if (this.file.isDirectory()) {
                process(v, recurse, strip, "/", this.file.listFiles());
                return;
            }
            v.visit(this.file.getName());
        }

        private void process(URLVisitor v, boolean recurse, boolean strip, String path, File[] files) {
            String str;
            if (files != null) {
                for (File f : files) {
                    if (!f.isDirectory()) {
                        if (strip) {
                            str = f.getName();
                        } else {
                            str = path + f.getName();
                        }
                        v.visit(str);
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
                    PrintStream printStream = System.err;
                    printStream.println("icurb jar error: " + e);
                }
                throw new IllegalArgumentException("jar error: " + e.getMessage());
            }
        }

        public void guide(URLVisitor v, boolean recurse, boolean strip) {
            try {
                Enumeration<JarEntry> entries = this.jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) {
                        String name = entry.getName();
                        if (name.startsWith(this.prefix)) {
                            String name2 = name.substring(this.prefix.length());
                            int ix = name2.lastIndexOf(47);
                            if (ix <= 0 || recurse) {
                                if (strip && ix != -1) {
                                    name2 = name2.substring(ix + 1);
                                }
                                v.visit(name2);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (URLHandler.DEBUG) {
                    PrintStream printStream = System.err;
                    printStream.println("icurb jar error: " + e);
                }
            }
        }
    }

    public interface URLVisitor {
        void visit(String str);
    }

    public abstract void guide(URLVisitor uRLVisitor, boolean z, boolean z2);

    static {
        Map<String, Method> h = null;
        BufferedReader br = null;
        try {
            InputStream is = ClassLoaderUtil.getClassLoader(URLHandler.class).getResourceAsStream(PROPNAME);
            if (is != null) {
                Class[] clsArr = {URL.class};
                br = new BufferedReader(new InputStreamReader(is));
                String line = br.readLine();
                while (true) {
                    if (line == null) {
                        break;
                    }
                    String line2 = line.trim();
                    if (line2.length() != 0) {
                        if (line2.charAt(0) != '#') {
                            int ix = line2.indexOf(61);
                            if (ix != -1) {
                                String key = line2.substring(0, ix).trim();
                                Method m = Class.forName(line2.substring(ix + 1).trim()).getDeclaredMethod("get", clsArr);
                                if (h == null) {
                                    h = new HashMap<>();
                                }
                                h.put(key, m);
                            } else if (DEBUG) {
                                PrintStream printStream = System.err;
                                printStream.println("bad urlhandler line: '" + line2 + "'");
                            }
                        }
                    }
                    line = br.readLine();
                }
                br.close();
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        } catch (ClassNotFoundException e2) {
            if (DEBUG) {
                System.err.println(e2);
            }
        } catch (NoSuchMethodException e3) {
            if (DEBUG) {
                System.err.println(e3);
            }
        } catch (SecurityException e4) {
            if (DEBUG) {
                System.err.println(e4);
            }
        } catch (Throwable t) {
            try {
                if (DEBUG) {
                    System.err.println(t);
                }
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e5) {
                    }
                }
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
            Method m = handlers.get(protocol);
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
