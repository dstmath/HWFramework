package java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map.Entry;

public final class ServiceLoader<S> implements Iterable<S> {
    private static final String PREFIX = "META-INF/services/";
    private final ClassLoader loader;
    private LazyIterator lookupIterator;
    private LinkedHashMap<String, S> providers = new LinkedHashMap();
    private final Class<S> service;

    private class LazyIterator implements Iterator<S> {
        Enumeration<URL> configs;
        ClassLoader loader;
        String nextName;
        Iterator<String> pending;
        Class<S> service;

        /* synthetic */ LazyIterator(ServiceLoader this$0, Class service, ClassLoader loader, LazyIterator -this3) {
            this(service, loader);
        }

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.configs = null;
            this.pending = null;
            this.nextName = null;
            this.service = service;
            this.loader = loader;
        }

        private boolean hasNextService() {
            if (this.nextName != null) {
                return true;
            }
            if (this.configs == null) {
                try {
                    String fullName = ServiceLoader.PREFIX + this.service.getName();
                    if (this.loader == null) {
                        this.configs = ClassLoader.getSystemResources(fullName);
                    } else {
                        this.configs = this.loader.getResources(fullName);
                    }
                } catch (IOException x) {
                    ServiceLoader.fail(this.service, "Error locating configuration files", x);
                }
            }
            while (true) {
                if (this.pending != null && (this.pending.hasNext() ^ 1) == 0) {
                    this.nextName = (String) this.pending.next();
                    return true;
                } else if (!this.configs.hasMoreElements()) {
                    return false;
                } else {
                    this.pending = ServiceLoader.this.parse(this.service, (URL) this.configs.nextElement());
                }
            }
        }

        private S nextService() {
            if (hasNextService()) {
                String cn = this.nextName;
                this.nextName = null;
                Class c = null;
                try {
                    c = Class.forName(cn, false, this.loader);
                } catch (ClassNotFoundException x) {
                    ServiceLoader.fail(this.service, "Provider " + cn + " not found", x);
                }
                if (!this.service.isAssignableFrom(c)) {
                    ServiceLoader.fail(this.service, "Provider " + cn + " not a subtype", new ClassCastException(this.service.getCanonicalName() + " is not assignable from " + c.getCanonicalName()));
                }
                try {
                    S p = this.service.cast(c.newInstance());
                    ServiceLoader.this.providers.put(cn, p);
                    return p;
                } catch (Throwable x2) {
                    ServiceLoader.fail(this.service, "Provider " + cn + " could not be instantiated", x2);
                    Error error = new Error();
                }
            } else {
                throw new NoSuchElementException();
            }
        }

        public boolean hasNext() {
            return hasNextService();
        }

        public S next() {
            return nextService();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public void reload() {
        this.providers.clear();
        this.lookupIterator = new LazyIterator(this, this.service, this.loader, null);
    }

    private ServiceLoader(Class<S> svc, ClassLoader cl) {
        this.service = (Class) Objects.requireNonNull((Object) svc, "Service interface cannot be null");
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        this.loader = cl;
        reload();
    }

    private static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
    }

    private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, URL u, int line, String msg) throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + msg);
    }

    private int parseLine(Class<?> service, URL u, BufferedReader r, int lc, List<String> names) throws IOException, ServiceConfigurationError {
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
            if (!(this.providers.containsKey(ln) || (names.contains(ln) ^ 1) == 0)) {
                names.add(ln);
            }
        }
        return lc + 1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0058 A:{SYNTHETIC, Splitter: B:31:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x005d A:{Catch:{ IOException -> 0x0061 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Iterator<String> parse(Class<?> service, URL u) throws ServiceConfigurationError {
        IOException x;
        Throwable th;
        InputStream inputStream = null;
        ArrayList<String> names = new ArrayList();
        BufferedReader r;
        try {
            inputStream = u.openStream();
            r = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            int lc = 1;
            do {
                try {
                    lc = parseLine(service, u, r, lc, names);
                } catch (IOException e) {
                    x = e;
                }
            } while (lc >= 0);
            if (r != null) {
                try {
                    r.close();
                } catch (IOException y) {
                    fail(service, "Error closing configuration file", y);
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e2) {
            x = e2;
            r = null;
            try {
                fail(service, "Error reading configuration file", x);
                if (r != null) {
                    try {
                        r.close();
                    } catch (IOException y2) {
                        fail(service, "Error closing configuration file", y2);
                    }
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                return names.iterator();
            } catch (Throwable th2) {
                th = th2;
                if (r != null) {
                    try {
                        r.close();
                    } catch (IOException y22) {
                        fail(service, "Error closing configuration file", y22);
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
            r = null;
            if (r != null) {
            }
            if (inputStream != null) {
            }
            throw th;
        }
        return names.iterator();
    }

    public Iterator<S> iterator() {
        return new Iterator<S>() {
            Iterator<Entry<String, S>> knownProviders = ServiceLoader.this.providers.entrySet().iterator();

            public boolean hasNext() {
                if (this.knownProviders.hasNext()) {
                    return true;
                }
                return ServiceLoader.this.lookupIterator.hasNext();
            }

            public S next() {
                if (this.knownProviders.hasNext()) {
                    return ((Entry) this.knownProviders.next()).getValue();
                }
                return ServiceLoader.this.lookupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        return new ServiceLoader(service, loader);
    }

    public static <S> ServiceLoader<S> load(Class<S> service) {
        return load(service, Thread.currentThread().getContextClassLoader());
    }

    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader prev = null;
        for (ClassLoader cl = ClassLoader.getSystemClassLoader(); cl != null; cl = cl.getParent()) {
            prev = cl;
        }
        return load(service, prev);
    }

    public static <S> S loadFromSystemProperty(Class<S> service) {
        try {
            String className = System.getProperty(service.getName());
            if (className != null) {
                return ClassLoader.getSystemClassLoader().loadClass(className).newInstance();
            }
            return null;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public String toString() {
        return "java.util.ServiceLoader[" + this.service.getName() + "]";
    }
}
