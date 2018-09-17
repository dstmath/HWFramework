package sun.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

public final class Service {
    private static final String prefix = "META-INF/services/";

    private static class LazyIterator implements Iterator {
        Enumeration configs;
        ClassLoader loader;
        String nextName;
        Iterator pending;
        Set returned;
        Class service;

        private LazyIterator(Class service, ClassLoader loader) {
            this.configs = null;
            this.pending = null;
            this.returned = new TreeSet();
            this.nextName = null;
            this.service = service;
            this.loader = loader;
        }

        public boolean hasNext() throws ServiceConfigurationError {
            if (this.nextName != null) {
                return true;
            }
            if (this.configs == null) {
                try {
                    String fullName = Service.prefix + this.service.getName();
                    if (this.loader == null) {
                        this.configs = ClassLoader.getSystemResources(fullName);
                    } else {
                        this.configs = this.loader.getResources(fullName);
                    }
                } catch (Object x) {
                    Service.fail(this.service, ": " + x);
                }
            }
            while (true) {
                if (this.pending != null && this.pending.hasNext()) {
                    this.nextName = (String) this.pending.next();
                    return true;
                } else if (!this.configs.hasMoreElements()) {
                    return false;
                } else {
                    this.pending = Service.parse(this.service, (URL) this.configs.nextElement(), this.returned);
                }
            }
        }

        public Object next() throws ServiceConfigurationError {
            if (hasNext()) {
                String cn = this.nextName;
                this.nextName = null;
                Class c = null;
                try {
                    c = Class.forName(cn, false, this.loader);
                } catch (ClassNotFoundException e) {
                    Service.fail(this.service, "Provider " + cn + " not found");
                }
                if (!this.service.isAssignableFrom(c)) {
                    Service.fail(this.service, "Provider " + cn + " not a subtype");
                }
                try {
                    return this.service.cast(c.newInstance());
                } catch (Object x) {
                    Service.fail(this.service, "Provider " + cn + " could not be instantiated: " + x, x);
                    return null;
                }
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private Service() {
    }

    private static void fail(Class service, String msg, Throwable cause) throws ServiceConfigurationError {
        ServiceConfigurationError sce = new ServiceConfigurationError(service.getName() + ": " + msg);
        sce.initCause(cause);
        throw sce;
    }

    private static void fail(Class service, String msg) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class service, URL u, int line, String msg) throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + msg);
    }

    private static int parseLine(Class service, URL u, BufferedReader r, int lc, List names, Set returned) throws IOException, ServiceConfigurationError {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf(35);
        if (ci >= 0) {
            ln = ln.substring(0, ci);
        }
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if (ln.indexOf(32) >= 0 || ln.indexOf(9) >= 0) {
                fail(service, u, lc, "Illegal configuration-file syntax");
            }
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                fail(service, u, lc, "Illegal provider-class name: " + ln);
            }
            int i = Character.charCount(cp);
            while (i < n) {
                cp = ln.codePointAt(i);
                if (!(Character.isJavaIdentifierPart(cp) || cp == 46)) {
                    fail(service, u, lc, "Illegal provider-class name: " + ln);
                }
                i += Character.charCount(cp);
            }
            if (!returned.contains(ln)) {
                names.add(ln);
                returned.add(ln);
            }
        }
        return lc + 1;
    }

    private static Iterator parse(Class service, URL u, Set returned) throws ServiceConfigurationError {
        BufferedReader bufferedReader;
        Object x;
        Throwable th;
        InputStream inputStream = null;
        ArrayList names = new ArrayList();
        try {
            inputStream = u.openStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            int lc = 1;
            do {
                try {
                    lc = parseLine(service, u, bufferedReader, lc, names, returned);
                } catch (IOException e) {
                    x = e;
                }
            } while (lc >= 0);
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Object y) {
                    fail(service, ": " + y);
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e2) {
            x = e2;
            bufferedReader = null;
            try {
                fail(service, ": " + x);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Object y2) {
                        fail(service, ": " + y2);
                    }
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                return names.iterator();
            } catch (Throwable th2) {
                th = th2;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Object y22) {
                        fail(service, ": " + y22);
                        throw th;
                    }
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            bufferedReader = null;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            throw th;
        }
        return names.iterator();
    }

    public static Iterator providers(Class service, ClassLoader loader) throws ServiceConfigurationError {
        return new LazyIterator(loader, null);
    }

    public static Iterator providers(Class service) throws ServiceConfigurationError {
        return providers(service, Thread.currentThread().getContextClassLoader());
    }

    public static Iterator installedProviders(Class service) throws ServiceConfigurationError {
        ClassLoader prev = null;
        for (ClassLoader cl = ClassLoader.getSystemClassLoader(); cl != null; cl = cl.getParent()) {
            prev = cl;
        }
        return providers(service, prev);
    }
}
