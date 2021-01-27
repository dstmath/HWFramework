package ohos.global.icu.impl;

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
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public final class ResourceBundleWrapper extends UResourceBundle {
    private static CacheBase<String, ResourceBundleWrapper, Loader> BUNDLE_CACHE = new SoftCache<String, ResourceBundleWrapper, Loader>() {
        /* class ohos.global.icu.impl.ResourceBundleWrapper.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public ResourceBundleWrapper createInstance(String str, Loader loader) {
            return loader.load();
        }
    };
    private static final boolean DEBUG = ICUDebug.enabled("resourceBundleWrapper");
    private String baseName;
    private ResourceBundle bundle;
    private List<String> keys;
    private String localeID;

    /* access modifiers changed from: private */
    public static abstract class Loader {
        /* access modifiers changed from: package-private */
        public abstract ResourceBundleWrapper load();

        private Loader() {
        }
    }

    private ResourceBundleWrapper(ResourceBundle resourceBundle) {
        this.bundle = null;
        this.localeID = null;
        this.baseName = null;
        this.keys = null;
        this.bundle = resourceBundle;
    }

    /* access modifiers changed from: protected */
    public Object handleGetObject(String str) {
        Object obj;
        ResourceBundleWrapper resourceBundleWrapper = this;
        while (true) {
            if (resourceBundleWrapper == null) {
                obj = null;
                break;
            }
            try {
                obj = resourceBundleWrapper.bundle.getObject(str);
                break;
            } catch (MissingResourceException unused) {
                resourceBundleWrapper = (ResourceBundleWrapper) resourceBundleWrapper.getParent();
            }
        }
        if (obj != null) {
            return obj;
        }
        throw new MissingResourceException("Can't find resource for bundle " + this.baseName + ", key " + str, getClass().getName(), str);
    }

    public Enumeration<String> getKeys() {
        return Collections.enumeration(this.keys);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initKeysVector() {
        this.keys = new ArrayList();
        for (ResourceBundleWrapper resourceBundleWrapper = this; resourceBundleWrapper != null; resourceBundleWrapper = (ResourceBundleWrapper) resourceBundleWrapper.getParent()) {
            Enumeration<String> keys2 = resourceBundleWrapper.bundle.getKeys();
            while (keys2.hasMoreElements()) {
                String nextElement = keys2.nextElement();
                if (!this.keys.contains(nextElement)) {
                    this.keys.add(nextElement);
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
        return this.parent;
    }

    public static ResourceBundleWrapper getBundleInstance(String str, String str2, ClassLoader classLoader, boolean z) {
        ResourceBundleWrapper resourceBundleWrapper;
        if (classLoader == null) {
            classLoader = ClassLoaderUtil.getClassLoader();
        }
        if (z) {
            resourceBundleWrapper = instantiateBundle(str, str2, null, classLoader, z);
        } else {
            resourceBundleWrapper = instantiateBundle(str, str2, ULocale.getDefault().getBaseName(), classLoader, z);
        }
        if (resourceBundleWrapper != null) {
            return resourceBundleWrapper;
        }
        String str3 = str.indexOf(47) >= 0 ? PsuedoNames.PSEUDONAME_ROOT : "_";
        throw new MissingResourceException("Could not find the bundle " + str + str3 + str2, "", "");
    }

    /* access modifiers changed from: private */
    public static boolean localeIDStartsWithLangSubtag(String str, String str2) {
        return str.startsWith(str2) && (str.length() == str2.length() || str.charAt(str2.length()) == '_');
    }

    /* access modifiers changed from: private */
    public static ResourceBundleWrapper instantiateBundle(final String str, final String str2, final String str3, final ClassLoader classLoader, final boolean z) {
        final String str4;
        String str5;
        if (str2.isEmpty()) {
            str4 = str;
        } else {
            str4 = str + '_' + str2;
        }
        if (z) {
            str5 = str4;
        } else {
            str5 = str4 + '#' + str3;
        }
        return BUNDLE_CACHE.getInstance(str5, new Loader() {
            /* class ohos.global.icu.impl.ResourceBundleWrapper.AnonymousClass2 */

            /* JADX DEBUG: Multi-variable search result rejected for r8v5, resolved type: ohos.global.icu.impl.ResourceBundleWrapper */
            /* JADX DEBUG: Multi-variable search result rejected for r1v15, resolved type: ohos.global.icu.impl.ResourceBundleWrapper */
            /* JADX DEBUG: Multi-variable search result rejected for r1v17, resolved type: ohos.global.icu.impl.ResourceBundleWrapper */
            /* JADX WARN: Multi-variable type inference failed */
            /* JADX WARNING: Code restructure failed: missing block: B:72:0x0117, code lost:
                if (r14 == false) goto L_0x011d;
             */
            /* JADX WARNING: Removed duplicated region for block: B:18:0x0071  */
            /* JADX WARNING: Removed duplicated region for block: B:21:0x007c  */
            /* JADX WARNING: Removed duplicated region for block: B:27:0x0086 A[SYNTHETIC, Splitter:B:27:0x0086] */
            /* JADX WARNING: Removed duplicated region for block: B:58:0x00e3  */
            /* JADX WARNING: Removed duplicated region for block: B:69:0x0113  */
            /* JADX WARNING: Removed duplicated region for block: B:80:0x0128  */
            /* JADX WARNING: Removed duplicated region for block: B:83:0x0133  */
            /* JADX WARNING: Removed duplicated region for block: B:85:0x013a  */
            /* JADX WARNING: Removed duplicated region for block: B:86:0x013e  */
            @Override // ohos.global.icu.impl.ResourceBundleWrapper.Loader
            public ResourceBundleWrapper load() {
                boolean z;
                ResourceBundle resourceBundle;
                ResourceBundleWrapper resourceBundleWrapper;
                Exception e;
                ResourceBundleWrapper instantiateBundle;
                ResourceBundleWrapper resourceBundleWrapper2;
                Throwable th;
                Exception e2;
                int lastIndexOf = str2.lastIndexOf(95);
                boolean z2 = true;
                if (lastIndexOf != -1) {
                    z = false;
                    resourceBundle = ResourceBundleWrapper.instantiateBundle(str, str2.substring(0, lastIndexOf), str3, classLoader, z);
                } else if (!str2.isEmpty()) {
                    z = true;
                    resourceBundle = ResourceBundleWrapper.instantiateBundle(str, "", str3, classLoader, z);
                } else {
                    z = false;
                    resourceBundle = null;
                }
                try {
                    resourceBundleWrapper = new ResourceBundleWrapper((ResourceBundle) classLoader.loadClass(str4).asSubclass(ResourceBundle.class).newInstance());
                    if (resourceBundle != null) {
                        try {
                            resourceBundleWrapper.setParent(resourceBundle);
                        } catch (ClassNotFoundException | NoClassDefFoundError unused) {
                        } catch (Exception e3) {
                            e2 = e3;
                            if (ResourceBundleWrapper.DEBUG) {
                                System.out.println("failure");
                            }
                            if (ResourceBundleWrapper.DEBUG) {
                                System.out.println(e2);
                            }
                            z2 = false;
                            if (z2) {
                            }
                            if (resourceBundleWrapper == null) {
                            }
                            return resourceBundleWrapper;
                        }
                    }
                    resourceBundleWrapper.baseName = str;
                    resourceBundleWrapper.localeID = str2;
                } catch (ClassNotFoundException | NoClassDefFoundError unused2) {
                    resourceBundleWrapper = null;
                } catch (Exception e4) {
                    e2 = e4;
                    resourceBundleWrapper = null;
                    if (ResourceBundleWrapper.DEBUG) {
                    }
                    if (ResourceBundleWrapper.DEBUG) {
                    }
                    z2 = false;
                }
                z2 = false;
                if (z2) {
                    try {
                        final String str = str4.replace('.', '/') + ".properties";
                        InputStream inputStream = (InputStream) AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                            /* class ohos.global.icu.impl.ResourceBundleWrapper.AnonymousClass2.AnonymousClass1 */

                            @Override // java.security.PrivilegedAction
                            public InputStream run() {
                                return classLoader.getResourceAsStream(str);
                            }
                        });
                        if (inputStream != null) {
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                            try {
                                resourceBundleWrapper2 = new ResourceBundleWrapper(new PropertyResourceBundle(bufferedInputStream));
                                if (resourceBundle != null) {
                                    try {
                                        resourceBundleWrapper2.setParent(resourceBundle);
                                    } catch (Exception unused3) {
                                        resourceBundleWrapper = resourceBundleWrapper2;
                                        try {
                                            bufferedInputStream.close();
                                        } catch (Exception unused4) {
                                        }
                                        if (resourceBundleWrapper == null) {
                                        }
                                        if (instantiateBundle == null) {
                                        }
                                        resourceBundleWrapper = instantiateBundle;
                                        if (resourceBundleWrapper == null) {
                                        }
                                        return resourceBundleWrapper;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        try {
                                            bufferedInputStream.close();
                                        } catch (Exception unused5) {
                                        }
                                        try {
                                            throw th;
                                        } catch (Exception e5) {
                                            e = e5;
                                            resourceBundleWrapper = resourceBundleWrapper2;
                                        }
                                    }
                                }
                                resourceBundleWrapper2.baseName = str;
                                resourceBundleWrapper2.localeID = str2;
                                try {
                                    bufferedInputStream.close();
                                } catch (Exception unused6) {
                                }
                                resourceBundleWrapper = resourceBundleWrapper2;
                            } catch (Exception unused7) {
                                bufferedInputStream.close();
                                if (resourceBundleWrapper == null) {
                                }
                                if (instantiateBundle == null) {
                                }
                                resourceBundleWrapper = instantiateBundle;
                                if (resourceBundleWrapper == null) {
                                }
                                return resourceBundleWrapper;
                            } catch (Throwable th3) {
                                th = th3;
                                resourceBundleWrapper2 = resourceBundleWrapper;
                                bufferedInputStream.close();
                                throw th;
                            }
                        }
                        instantiateBundle = (resourceBundleWrapper == null || z || str2.isEmpty() || str2.indexOf(95) >= 0 || ResourceBundleWrapper.localeIDStartsWithLangSubtag(str3, str2)) ? resourceBundleWrapper : ResourceBundleWrapper.instantiateBundle(str, str3, str3, classLoader, z);
                        if (instantiateBundle == null) {
                            if (z) {
                                try {
                                } catch (Exception e6) {
                                    e = e6;
                                    resourceBundleWrapper = instantiateBundle;
                                    if (ResourceBundleWrapper.DEBUG) {
                                    }
                                    if (ResourceBundleWrapper.DEBUG) {
                                    }
                                    if (resourceBundleWrapper == null) {
                                    }
                                    return resourceBundleWrapper;
                                }
                            }
                            resourceBundleWrapper = resourceBundle;
                        }
                        resourceBundleWrapper = instantiateBundle;
                    } catch (Exception e7) {
                        e = e7;
                        if (ResourceBundleWrapper.DEBUG) {
                            System.out.println("failure");
                        }
                        if (ResourceBundleWrapper.DEBUG) {
                            System.out.println(e);
                        }
                        if (resourceBundleWrapper == null) {
                        }
                        return resourceBundleWrapper;
                    }
                }
                if (resourceBundleWrapper == null) {
                    resourceBundleWrapper.initKeysVector();
                } else if (ResourceBundleWrapper.DEBUG) {
                    System.out.println("Returning null for " + str + "_" + str2);
                }
                return resourceBundleWrapper;
            }
        });
    }
}
