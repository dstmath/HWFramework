package java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

public final class ServiceLoader<S> implements Iterable<S> {
    private static final String PREFIX = "META-INF/services/";
    private final ClassLoader loader;
    /* access modifiers changed from: private */
    public ServiceLoader<S>.LazyIterator lookupIterator;
    /* access modifiers changed from: private */
    public LinkedHashMap<String, S> providers = new LinkedHashMap<>();
    private final Class<S> service;

    private class LazyIterator implements Iterator<S> {
        Enumeration<URL> configs;
        ClassLoader loader;
        String nextName;
        Iterator<String> pending;
        Class<S> service;

        private LazyIterator(Class<S> service2, ClassLoader loader2) {
            this.configs = null;
            this.pending = null;
            this.nextName = null;
            this.service = service2;
            this.loader = loader2;
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
                if (this.pending != null && this.pending.hasNext()) {
                    this.nextName = this.pending.next();
                    return true;
                } else if (!this.configs.hasMoreElements()) {
                    return false;
                } else {
                    this.pending = ServiceLoader.this.parse(this.service, this.configs.nextElement());
                }
            }
        }

        private S nextService() {
            if (hasNextService()) {
                String cn = this.nextName;
                Class<?> c = null;
                this.nextName = null;
                try {
                    c = Class.forName(cn, false, this.loader);
                } catch (ClassNotFoundException x) {
                    Class<S> cls = this.service;
                    ServiceLoader.fail(cls, "Provider " + cn + " not found", x);
                }
                if (!this.service.isAssignableFrom(c)) {
                    ClassCastException cce = new ClassCastException(this.service.getCanonicalName() + " is not assignable from " + c.getCanonicalName());
                    Class<S> cls2 = this.service;
                    ServiceLoader.fail(cls2, "Provider " + cn + " not a subtype", cce);
                }
                try {
                    S p = this.service.cast(c.newInstance());
                    ServiceLoader.this.providers.put(cn, p);
                    return p;
                } catch (Throwable x2) {
                    Class<S> cls3 = this.service;
                    ServiceLoader.fail(cls3, "Provider " + cn + " could not be instantiated", x2);
                    throw new Error();
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
        this.lookupIterator = new LazyIterator(this.service, this.loader);
    }

    private ServiceLoader(Class<S> svc, ClassLoader cl) {
        this.service = (Class) Objects.requireNonNull(svc, "Service interface cannot be null");
        this.loader = cl == null ? ClassLoader.getSystemClassLoader() : cl;
        reload();
    }

    /* access modifiers changed from: private */
    public static void fail(Class<?> service2, String msg, Throwable cause) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service2.getName() + ": " + msg, cause);
    }

    private static void fail(Class<?> service2, String msg) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service2.getName() + ": " + msg);
    }

    private static void fail(Class<?> service2, URL u, int line, String msg) throws ServiceConfigurationError {
        fail(service2, u + ":" + line + ": " + msg);
    }

    private int parseLine(Class<?> service2, URL u, BufferedReader r, int lc, List<String> names) throws IOException, ServiceConfigurationError {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf(35);
        if (ci >= 0) {
            ln = ln.substring(0, ci);
        }
        String ln2 = ln.trim();
        int n = ln2.length();
        if (n != 0) {
            if (ln2.indexOf(32) >= 0 || ln2.indexOf(9) >= 0) {
                fail(service2, u, lc, "Illegal configuration-file syntax");
            }
            int cp = ln2.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                fail(service2, u, lc, "Illegal provider-class name: " + ln2);
            }
            int i = Character.charCount(cp);
            while (i < n) {
                int cp2 = ln2.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp2) && cp2 != 46) {
                    fail(service2, u, lc, "Illegal provider-class name: " + ln2);
                }
                i += Character.charCount(cp2);
            }
            if (!this.providers.containsKey(ln2) && !names.contains(ln2)) {
                names.add(ln2);
            }
        }
        return lc + 1;
    }

    /* access modifiers changed from: private */
    public Iterator<String> parse(Class<?> service2, URL u) throws ServiceConfigurationError {
        int parseLine;
        InputStream in = null;
        BufferedReader r = null;
        ArrayList<String> names = new ArrayList<>();
        try {
            InputStream in2 = u.openStream();
            BufferedReader r2 = new BufferedReader(new InputStreamReader(in2, "utf-8"));
            int lc = 1;
            do {
                parseLine = parseLine(service2, u, r2, lc, names);
                lc = parseLine;
            } while (parseLine >= 0);
            try {
                r2.close();
                if (in2 != null) {
                    in2.close();
                }
            } catch (IOException y) {
                fail(service2, "Error closing configuration file", y);
            }
        } catch (IOException x) {
            fail(service2, "Error reading configuration file", x);
            if (r != null) {
                r.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (Throwable th) {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException y2) {
                    fail(service2, "Error closing configuration file", y2);
                    throw th;
                }
            }
            if (in != null) {
                in.close();
            }
            throw th;
        }
        return names.iterator();
    }

    public Iterator<S> iterator() {
        return new Iterator<S>() {
            Iterator<Map.Entry<String, S>> knownProviders = ServiceLoader.this.providers.entrySet().iterator();

            public boolean hasNext() {
                if (this.knownProviders.hasNext()) {
                    return true;
                }
                return ServiceLoader.this.lookupIterator.hasNext();
            }

            public S next() {
                if (this.knownProviders.hasNext()) {
                    return this.knownProviders.next().getValue();
                }
                return ServiceLoader.this.lookupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <S> ServiceLoader<S> load(Class<S> service2, ClassLoader loader2) {
        return new ServiceLoader<>(service2, loader2);
    }

    public static <S> ServiceLoader<S> load(Class<S> service2) {
        return load(service2, Thread.currentThread().getContextClassLoader());
    }

    public static <S> ServiceLoader<S> loadInstalled(Class<S> service2) {
        ClassLoader prev = null;
        for (ClassLoader cl = ClassLoader.getSystemClassLoader(); cl != null; cl = cl.getParent()) {
            prev = cl;
        }
        return load(service2, prev);
    }

    public static <S> S loadFromSystemProperty(Class<S> service2) {
        try {
            String className = System.getProperty(service2.getName());
            if (className != null) {
                return ClassLoader.getSystemClassLoader().loadClass(className).newInstance();
            }
            return null;
        } catch (Exception e) {
            throw new Error((Throwable) e);
        }
    }

    public String toString() {
        return "java.util.ServiceLoader[" + this.service.getName() + "]";
    }
}
