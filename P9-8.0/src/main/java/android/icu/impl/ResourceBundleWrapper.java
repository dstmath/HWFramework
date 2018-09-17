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
        protected ResourceBundleWrapper createInstance(String unusedKey, Loader loader) {
            return loader.load();
        }
    };
    private static final boolean DEBUG = ICUDebug.enabled("resourceBundleWrapper");
    private String baseName;
    private ResourceBundle bundle;
    private List<String> keys;
    private String localeID;

    private static abstract class Loader {
        /* synthetic */ Loader(Loader -this0) {
            this();
        }

        abstract ResourceBundleWrapper load();

        private Loader() {
        }
    }

    /* synthetic */ ResourceBundleWrapper(ResourceBundle bundle, ResourceBundleWrapper -this1) {
        this(bundle);
    }

    private ResourceBundleWrapper(ResourceBundle bundle) {
        this.bundle = null;
        this.localeID = null;
        this.baseName = null;
        this.keys = null;
        this.bundle = bundle;
    }

    protected Object handleGetObject(String aKey) {
        ResourceBundleWrapper current = this;
        Object obj = null;
        while (current != null) {
            try {
                obj = current.bundle.getObject(aKey);
                break;
            } catch (MissingResourceException e) {
                current = (ResourceBundleWrapper) current.getParent();
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

    private void initKeysVector() {
        this.keys = new ArrayList();
        for (ResourceBundleWrapper current = this; current != null; current = (ResourceBundleWrapper) current.getParent()) {
            Enumeration<String> e = current.bundle.getKeys();
            while (e.hasMoreElements()) {
                String elem = (String) e.nextElement();
                if (!this.keys.contains(elem)) {
                    this.keys.add(elem);
                }
            }
        }
    }

    protected String getLocaleID() {
        return this.localeID;
    }

    protected String getBaseName() {
        return this.bundle.getClass().getName().replace('.', '/');
    }

    public ULocale getULocale() {
        return new ULocale(this.localeID);
    }

    public UResourceBundle getParent() {
        return (UResourceBundle) this.parent;
    }

    public static ResourceBundleWrapper getBundleInstance(String baseName, String localeID, ClassLoader root, boolean disableFallback) {
        ResourceBundleWrapper b;
        if (root == null) {
            root = ClassLoaderUtil.getClassLoader();
        }
        if (disableFallback) {
            b = instantiateBundle(baseName, localeID, null, root, disableFallback);
        } else {
            b = instantiateBundle(baseName, localeID, ULocale.getDefault().getBaseName(), root, disableFallback);
        }
        if (b != null) {
            return b;
        }
        String separator = BaseLocale.SEP;
        if (baseName.indexOf(47) >= 0) {
            separator = "/";
        }
        throw new MissingResourceException("Could not find the bundle " + baseName + separator + localeID, "", "");
    }

    private static boolean localeIDStartsWithLangSubtag(String localeID, String lang) {
        if (localeID.startsWith(lang)) {
            return localeID.length() == lang.length() || localeID.charAt(lang.length()) == '_';
        } else {
            return false;
        }
    }

    private static ResourceBundleWrapper instantiateBundle(String baseName, String localeID, String defaultID, ClassLoader root, boolean disableFallback) {
        final String name = localeID.isEmpty() ? baseName : baseName + '_' + localeID;
        final String str = localeID;
        final String str2 = baseName;
        final String str3 = defaultID;
        final ClassLoader classLoader = root;
        final boolean z = disableFallback;
        return (ResourceBundleWrapper) BUNDLE_CACHE.getInstance(disableFallback ? name : name + '#' + defaultID, new Loader() {
            /* JADX WARNING: Removed duplicated region for block: B:92:0x0251  */
            /* JADX WARNING: Removed duplicated region for block: B:11:0x008d A:{SYNTHETIC, Splitter: B:11:0x008d} */
            /* JADX WARNING: Removed duplicated region for block: B:78:0x0205  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0176  */
            /* JADX WARNING: Removed duplicated region for block: B:11:0x008d A:{SYNTHETIC, Splitter: B:11:0x008d} */
            /* JADX WARNING: Removed duplicated region for block: B:92:0x0251  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0176  */
            /* JADX WARNING: Removed duplicated region for block: B:78:0x0205  */
            /* JADX WARNING: Removed duplicated region for block: B:92:0x0251  */
            /* JADX WARNING: Removed duplicated region for block: B:11:0x008d A:{SYNTHETIC, Splitter: B:11:0x008d} */
            /* JADX WARNING: Removed duplicated region for block: B:78:0x0205  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0176  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0176  */
            /* JADX WARNING: Removed duplicated region for block: B:78:0x0205  */
            /* JADX WARNING: Removed duplicated region for block: B:11:0x008d A:{SYNTHETIC, Splitter: B:11:0x008d} */
            /* JADX WARNING: Removed duplicated region for block: B:92:0x0251  */
            /* JADX WARNING: Removed duplicated region for block: B:78:0x0205  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0176  */
            /* JADX WARNING: Removed duplicated region for block: B:92:0x0251  */
            /* JADX WARNING: Removed duplicated region for block: B:11:0x008d A:{SYNTHETIC, Splitter: B:11:0x008d} */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0176  */
            /* JADX WARNING: Removed duplicated region for block: B:78:0x0205  */
            /* JADX WARNING: Removed duplicated region for block: B:50:0x01b0  */
            /* JADX WARNING: Removed duplicated region for block: B:93:0x0254  */
            /* JADX WARNING: Removed duplicated region for block: B:53:0x01be  */
            /* JADX WARNING: Removed duplicated region for block: B:11:0x008d A:{SYNTHETIC, Splitter: B:11:0x008d} */
            /* JADX WARNING: Removed duplicated region for block: B:92:0x0251  */
            /* JADX WARNING: Removed duplicated region for block: B:78:0x0205  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0176  */
            /* JADX WARNING: Removed duplicated region for block: B:25:0x0109 A:{SYNTHETIC, Splitter: B:25:0x0109} */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0176  */
            /* JADX WARNING: Removed duplicated region for block: B:78:0x0205  */
            /* JADX WARNING: Removed duplicated region for block: B:25:0x0109 A:{SYNTHETIC, Splitter: B:25:0x0109} */
            /* JADX WARNING: Removed duplicated region for block: B:78:0x0205  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0176  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public ResourceBundleWrapper load() {
                ResourceBundleWrapper b;
                Exception e;
                Throwable th;
                ResourceBundle parent = null;
                int i = str.lastIndexOf(95);
                boolean loadFromProperties = false;
                boolean parentIsRoot = false;
                if (i != -1) {
                    String locName = str.substring(0, i);
                    parent = ResourceBundleWrapper.instantiateBundle(str2, locName, str3, classLoader, z);
                } else if (!str.isEmpty()) {
                    parent = ResourceBundleWrapper.instantiateBundle(str2, "", str3, classLoader, z);
                    parentIsRoot = true;
                }
                ResourceBundleWrapper b2 = null;
                try {
                    b = new ResourceBundleWrapper((ResourceBundle) classLoader.loadClass(name).asSubclass(ResourceBundle.class).newInstance(), null);
                    if (parent != null) {
                        try {
                            b.setParent(parent);
                        } catch (ClassNotFoundException e2) {
                            b2 = b;
                            loadFromProperties = true;
                            b = b2;
                            if (loadFromProperties) {
                            }
                            if (b2 == null) {
                            }
                            return b2;
                        } catch (NoClassDefFoundError e3) {
                            b2 = b;
                            loadFromProperties = true;
                            b = b2;
                            if (loadFromProperties) {
                            }
                            if (b2 == null) {
                            }
                            return b2;
                        } catch (Exception e4) {
                            e = e4;
                            b2 = b;
                            if (ResourceBundleWrapper.DEBUG) {
                            }
                            if (ResourceBundleWrapper.DEBUG) {
                            }
                            if (loadFromProperties) {
                            }
                            if (b2 == null) {
                            }
                            return b2;
                        }
                    }
                    b.baseName = str2;
                    b.localeID = str;
                } catch (ClassNotFoundException e5) {
                    loadFromProperties = true;
                    b = b2;
                    if (loadFromProperties) {
                    }
                    if (b2 == null) {
                    }
                    return b2;
                } catch (NoClassDefFoundError e6) {
                    loadFromProperties = true;
                    b = b2;
                    if (loadFromProperties) {
                    }
                    if (b2 == null) {
                    }
                    return b2;
                } catch (Exception e7) {
                    e = e7;
                    if (ResourceBundleWrapper.DEBUG) {
                        System.out.println("failure");
                    }
                    if (ResourceBundleWrapper.DEBUG) {
                        System.out.println(e);
                        b = b2;
                    } else {
                        b = b2;
                    }
                    if (loadFromProperties) {
                    }
                    if (b2 == null) {
                    }
                    return b2;
                }
                if (loadFromProperties) {
                    try {
                        String resName = name.replace('.', '/') + ".properties";
                        final ClassLoader classLoader = classLoader;
                        final String str = resName;
                        InputStream stream = (InputStream) AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                            public InputStream run() {
                                return classLoader.getResourceAsStream(str);
                            }
                        });
                        if (stream != null) {
                            InputStream bufferedInputStream = new BufferedInputStream(stream);
                            try {
                                b2 = new ResourceBundleWrapper(new PropertyResourceBundle(bufferedInputStream), null);
                                if (parent != null) {
                                    try {
                                        b2.setParent(parent);
                                    } catch (Exception e8) {
                                        try {
                                            bufferedInputStream.close();
                                        } catch (Exception e9) {
                                        }
                                        stream = bufferedInputStream;
                                        if (b2 == null) {
                                        }
                                        b2 = parent;
                                        if (b2 == null) {
                                        }
                                        return b2;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        try {
                                            bufferedInputStream.close();
                                        } catch (Exception e10) {
                                        }
                                        throw th;
                                    }
                                }
                                b2.baseName = str2;
                                b2.localeID = str;
                                try {
                                    bufferedInputStream.close();
                                } catch (Exception e11) {
                                }
                            } catch (Exception e12) {
                                b2 = b;
                                bufferedInputStream.close();
                                stream = bufferedInputStream;
                                if (b2 == null) {
                                }
                                b2 = parent;
                                if (b2 == null) {
                                }
                                return b2;
                            } catch (Throwable th3) {
                                th = th3;
                                b2 = b;
                                bufferedInputStream.close();
                                throw th;
                            }
                            stream = bufferedInputStream;
                        } else {
                            b2 = b;
                        }
                        if (b2 == null) {
                            try {
                                if (!((z ^ 1) == 0 || (str.isEmpty() ^ 1) == 0 || str.indexOf(95) >= 0 || (ResourceBundleWrapper.localeIDStartsWithLangSubtag(str3, str) ^ 1) == 0)) {
                                    b2 = ResourceBundleWrapper.instantiateBundle(str2, str3, str3, classLoader, z);
                                }
                            } catch (Exception e13) {
                                e = e13;
                            }
                        }
                        if (b2 == null && !(parentIsRoot && (z ^ 1) == 0)) {
                            b2 = parent;
                        }
                    } catch (Exception e14) {
                        e = e14;
                        b2 = b;
                        if (ResourceBundleWrapper.DEBUG) {
                            System.out.println("failure");
                        }
                        if (ResourceBundleWrapper.DEBUG) {
                            System.out.println(e);
                        }
                        if (b2 == null) {
                        }
                        return b2;
                    }
                }
                b2 = b;
                if (b2 == null) {
                    b2.initKeysVector();
                } else if (ResourceBundleWrapper.DEBUG) {
                    System.out.println("Returning null for " + str2 + BaseLocale.SEP + str);
                }
                return b2;
            }
        });
    }
}
