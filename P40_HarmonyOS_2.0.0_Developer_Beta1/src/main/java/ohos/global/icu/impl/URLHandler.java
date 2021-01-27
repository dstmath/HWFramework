package ohos.global.icu.impl;

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
import ohos.ai.asr.util.AsrConstants;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public abstract class URLHandler {
    private static final boolean DEBUG = ICUDebug.enabled("URLHandler");
    public static final String PROPNAME = "urlhandler.props";
    private static final Map<String, Method> handlers;

    public interface URLVisitor {
        void visit(String str);
    }

    public abstract void guide(URLVisitor uRLVisitor, boolean z, boolean z2);

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00cc A[Catch:{ all -> 0x00d9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00d3 A[SYNTHETIC, Splitter:B:51:0x00d3] */
    static {
        BufferedReader bufferedReader;
        Object th;
        HashMap hashMap;
        HashMap hashMap2 = null;
        hashMap2 = null;
        BufferedReader bufferedReader2 = null;
        try {
            InputStream resourceAsStream = ClassLoaderUtil.getClassLoader(URLHandler.class).getResourceAsStream(PROPNAME);
            if (resourceAsStream != null) {
                Class<?>[] clsArr = {URL.class};
                bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
                while (true) {
                    try {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            break;
                        }
                        String trim = readLine.trim();
                        if (trim.length() != 0) {
                            if (trim.charAt(0) != '#') {
                                int indexOf = trim.indexOf(61);
                                if (indexOf != -1) {
                                    String trim2 = trim.substring(0, indexOf).trim();
                                    try {
                                        Method declaredMethod = Class.forName(trim.substring(indexOf + 1).trim()).getDeclaredMethod("get", clsArr);
                                        if (hashMap2 == null) {
                                            hashMap2 = new HashMap();
                                        }
                                        hashMap2.put(trim2, declaredMethod);
                                    } catch (ClassNotFoundException e) {
                                        if (DEBUG) {
                                            System.err.println(e);
                                        }
                                    } catch (NoSuchMethodException e2) {
                                        if (DEBUG) {
                                            System.err.println(e2);
                                        }
                                    } catch (SecurityException e3) {
                                        if (DEBUG) {
                                            System.err.println(e3);
                                        }
                                    }
                                } else if (DEBUG) {
                                    System.err.println("bad urlhandler line: '" + trim + "'");
                                }
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        try {
                            if (DEBUG) {
                            }
                            handlers = hashMap2;
                        } finally {
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException unused) {
                                }
                            }
                        }
                    }
                }
                bufferedReader.close();
                hashMap = hashMap2;
                bufferedReader2 = bufferedReader;
            } else {
                hashMap = null;
            }
            if (bufferedReader2 != null) {
                try {
                    bufferedReader2.close();
                } catch (IOException unused2) {
                }
            }
            hashMap2 = hashMap;
        } catch (Throwable th3) {
            th = th3;
            bufferedReader = null;
            if (DEBUG) {
                System.err.println(th);
            }
            handlers = hashMap2;
        }
        handlers = hashMap2;
    }

    public static URLHandler get(URL url) {
        Method method;
        if (url == null) {
            return null;
        }
        String protocol = url.getProtocol();
        Map<String, Method> map = handlers;
        if (!(map == null || (method = map.get(protocol)) == null)) {
            try {
                URLHandler uRLHandler = (URLHandler) method.invoke(null, url);
                if (uRLHandler != null) {
                    return uRLHandler;
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
        return getDefault(url);
    }

    protected static URLHandler getDefault(URL url) {
        URLHandler jarURLHandler;
        String protocol = url.getProtocol();
        try {
            if (protocol.equals(AsrConstants.ASR_SRC_FILE)) {
                jarURLHandler = new FileURLHandler(url);
            } else if (!protocol.equals("jar") && !protocol.equals("wsjar")) {
                return null;
            } else {
                jarURLHandler = new JarURLHandler(url);
            }
            return jarURLHandler;
        } catch (Exception unused) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static class FileURLHandler extends URLHandler {
        File file;

        FileURLHandler(URL url) {
            try {
                this.file = new File(url.toURI());
            } catch (URISyntaxException unused) {
            }
            File file2 = this.file;
            if (file2 == null || !file2.exists()) {
                if (URLHandler.DEBUG) {
                    PrintStream printStream = System.err;
                    printStream.println("file does not exist - " + url.toString());
                }
                throw new IllegalArgumentException();
            }
        }

        @Override // ohos.global.icu.impl.URLHandler
        public void guide(URLVisitor uRLVisitor, boolean z, boolean z2) {
            if (this.file.isDirectory()) {
                process(uRLVisitor, z, z2, PsuedoNames.PSEUDONAME_ROOT, this.file.listFiles());
            } else {
                uRLVisitor.visit(this.file.getName());
            }
        }

        private void process(URLVisitor uRLVisitor, boolean z, boolean z2, String str, File[] fileArr) {
            if (fileArr != null) {
                for (File file2 : fileArr) {
                    if (!file2.isDirectory()) {
                        uRLVisitor.visit(z2 ? file2.getName() : str + file2.getName());
                    } else if (z) {
                        process(uRLVisitor, z, z2, str + file2.getName() + '/', file2.listFiles());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class JarURLHandler extends URLHandler {
        JarFile jarFile;
        String prefix;

        JarURLHandler(URL url) {
            String url2;
            int indexOf;
            try {
                this.prefix = url.getPath();
                int lastIndexOf = this.prefix.lastIndexOf("!/");
                if (lastIndexOf >= 0) {
                    this.prefix = this.prefix.substring(lastIndexOf + 2);
                }
                if (!url.getProtocol().equals("jar") && (indexOf = (url2 = url.toString()).indexOf(":")) != -1) {
                    url = new URL("jar" + url2.substring(indexOf));
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

        @Override // ohos.global.icu.impl.URLHandler
        public void guide(URLVisitor uRLVisitor, boolean z, boolean z2) {
            try {
                Enumeration<JarEntry> entries = this.jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry nextElement = entries.nextElement();
                    if (!nextElement.isDirectory()) {
                        String name = nextElement.getName();
                        if (name.startsWith(this.prefix)) {
                            String substring = name.substring(this.prefix.length());
                            int lastIndexOf = substring.lastIndexOf(47);
                            if (lastIndexOf <= 0 || z) {
                                if (z2 && lastIndexOf != -1) {
                                    substring = substring.substring(lastIndexOf + 1);
                                }
                                uRLVisitor.visit(substring);
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

    public void guide(URLVisitor uRLVisitor, boolean z) {
        guide(uRLVisitor, z, true);
    }
}
