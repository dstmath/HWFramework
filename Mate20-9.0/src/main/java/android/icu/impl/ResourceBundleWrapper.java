package android.icu.impl;

import android.icu.impl.locale.BaseLocale;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class ResourceBundleWrapper extends UResourceBundle {
    private static CacheBase<String, ResourceBundleWrapper, Loader> BUNDLE_CACHE = new SoftCache<String, ResourceBundleWrapper, Loader>() {
        /* access modifiers changed from: protected */
        public ResourceBundleWrapper createInstance(String unusedKey, Loader loader) {
            return loader.load();
        }
    };
    /* access modifiers changed from: private */
    public static final boolean DEBUG = ICUDebug.enabled("resourceBundleWrapper");
    /* access modifiers changed from: private */
    public String baseName;
    private ResourceBundle bundle;
    private List<String> keys;
    /* access modifiers changed from: private */
    public String localeID;

    private static abstract class Loader {
        /* access modifiers changed from: package-private */
        public abstract ResourceBundleWrapper load();

        private Loader() {
        }
    }

    private ResourceBundleWrapper(ResourceBundle bundle2) {
        this.bundle = null;
        this.localeID = null;
        this.baseName = null;
        this.keys = null;
        this.bundle = bundle2;
    }

    /* JADX WARNING: type inference failed for: r3v2, types: [android.icu.util.UResourceBundle] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public Object handleGetObject(String aKey) {
        ResourceBundleWrapper current = this;
        Object obj = null;
        while (true) {
            if (current == null) {
                break;
            }
            try {
                obj = current.bundle.getObject(aKey);
                break;
            } catch (MissingResourceException e) {
                current = current.getParent();
            }
        }
        if (obj != null) {
            return obj;
        }
        throw new MissingResourceException("Can't find resource for bundle " + this.baseName + ", key " + aKey, getClass().getName(), aKey);
    }

    public Enumeration<String> getKeys() {
        return Collections.enumeration(this.keys);
    }

    /* JADX WARNING: type inference failed for: r2v1, types: [android.icu.util.UResourceBundle] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    public void initKeysVector() {
        this.keys = new ArrayList();
        for (ResourceBundleWrapper current = this; current != null; current = current.getParent()) {
            Enumeration<String> e = current.bundle.getKeys();
            while (e.hasMoreElements()) {
                String elem = e.nextElement();
                if (!this.keys.contains(elem)) {
                    this.keys.add(elem);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getLocaleID() {
        return this.localeID;
    }

    /* access modifiers changed from: protected */
    public String getBaseName() {
        return this.bundle.getClass().getName().replace('.', '/');
    }

    public ULocale getULocale() {
        return new ULocale(this.localeID);
    }

    public UResourceBundle getParent() {
        return (UResourceBundle) this.parent;
    }

    public static ResourceBundleWrapper getBundleInstance(String baseName2, String localeID2, ClassLoader root, boolean disableFallback) {
        ResourceBundleWrapper b;
        if (root == null) {
            root = ClassLoaderUtil.getClassLoader();
        }
        if (disableFallback) {
            b = instantiateBundle(baseName2, localeID2, null, root, disableFallback);
        } else {
            b = instantiateBundle(baseName2, localeID2, ULocale.getDefault().getBaseName(), root, disableFallback);
        }
        if (b != null) {
            return b;
        }
        String separator = BaseLocale.SEP;
        if (baseName2.indexOf(47) >= 0) {
            separator = "/";
        }
        throw new MissingResourceException("Could not find the bundle " + baseName2 + separator + localeID2, "", "");
    }

    /* access modifiers changed from: private */
    public static boolean localeIDStartsWithLangSubtag(String localeID2, String lang) {
        return localeID2.startsWith(lang) && (localeID2.length() == lang.length() || localeID2.charAt(lang.length()) == '_');
    }

    /* access modifiers changed from: private */
    public static ResourceBundleWrapper instantiateBundle(String baseName2, String localeID2, String defaultID, ClassLoader root, boolean disableFallback) {
        String name;
        String str;
        if (localeID2.isEmpty()) {
            name = baseName2;
        } else {
            name = baseName2 + '_' + localeID2;
        }
        if (disableFallback) {
            str = name;
        } else {
            str = name + '#' + defaultID;
        }
        String cacheKey = str;
        CacheBase<String, ResourceBundleWrapper, Loader> cacheBase = BUNDLE_CACHE;
        final String str2 = localeID2;
        final String str3 = baseName2;
        final String str4 = defaultID;
        final ClassLoader classLoader = root;
        final boolean z = disableFallback;
        final String str5 = name;
        AnonymousClass2 r1 = new Loader() {
            public ResourceBundleWrapper load() {
                InputStream stream;
                ResourceBundleWrapper parent = null;
                int i = str2.lastIndexOf(95);
                boolean loadFromProperties = false;
                boolean parentIsRoot = false;
                if (i != -1) {
                    parent = ResourceBundleWrapper.instantiateBundle(str3, str2.substring(0, i), str4, classLoader, z);
                } else if (!str2.isEmpty()) {
                    parent = ResourceBundleWrapper.instantiateBundle(str3, "", str4, classLoader, z);
                    parentIsRoot = true;
                }
                ResourceBundleWrapper b = null;
                try {
                    b = new ResourceBundleWrapper((ResourceBundle) classLoader.loadClass(str5).asSubclass(ResourceBundle.class).newInstance());
                    if (parent != null) {
                        b.setParent(parent);
                    }
                    String unused = b.baseName = str3;
                    String unused2 = b.localeID = str2;
                } catch (ClassNotFoundException e) {
                    loadFromProperties = true;
                } catch (NoClassDefFoundError e2) {
                    loadFromProperties = true;
                } catch (Exception e3) {
                    if (ResourceBundleWrapper.DEBUG) {
                        System.out.println("failure");
                    }
                    if (ResourceBundleWrapper.DEBUG) {
                        System.out.println(e3);
                    }
                }
                if (loadFromProperties) {
                    try {
                        final String resName = str5.replace('.', '/') + ".properties";
                        InputStream stream2 = (InputStream) AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                            public InputStream run() {
                                return classLoader.getResourceAsStream(resName);
                            }
                        });
                        if (stream2 != null) {
                            stream = new BufferedInputStream(stream2);
                            b = new ResourceBundleWrapper(new PropertyResourceBundle(stream));
                            if (parent != null) {
                                b.setParent(parent);
                            }
                            String unused3 = b.baseName = str3;
                            String unused4 = b.localeID = str2;
                            try {
                                stream.close();
                            } catch (Exception e4) {
                            }
                        }
                    } catch (Exception e5) {
                        stream.close();
                    } catch (Exception e6) {
                        if (ResourceBundleWrapper.DEBUG) {
                            System.out.println("failure");
                        }
                        if (ResourceBundleWrapper.DEBUG) {
                            System.out.println(e6);
                        }
                    } catch (Throwable th) {
                        try {
                            stream.close();
                        } catch (Exception e7) {
                        }
                        throw th;
                    }
                    if (b == null) {
                        if (!z && !str2.isEmpty() && str2.indexOf(95) < 0 && !ResourceBundleWrapper.localeIDStartsWithLangSubtag(str4, str2)) {
                            b = ResourceBundleWrapper.instantiateBundle(str3, str4, str4, classLoader, z);
                        }
                    }
                    if (b == null && (!parentIsRoot || !z)) {
                        b = parent;
                    }
                }
                if (b != null) {
                    b.initKeysVector();
                } else if (ResourceBundleWrapper.DEBUG) {
                    System.out.println("Returning null for " + str3 + BaseLocale.SEP + str2);
                }
                return b;
            }
        };
        return cacheBase.getInstance(cacheKey, r1);
    }
}
