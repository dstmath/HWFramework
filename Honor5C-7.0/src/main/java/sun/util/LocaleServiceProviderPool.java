package sun.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.ResourceBundle.Control;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.spi.LocaleServiceProvider;
import libcore.icu.ICU;
import sun.util.logging.PlatformLogger;
import sun.util.resources.OpenListResourceBundle;

public final class LocaleServiceProviderPool {
    private static volatile List<Locale> availableJRELocales;
    private static Locale locale_ja_JP_JP;
    private static Locale locale_th_TH_TH;
    private static ConcurrentMap<Class<? extends LocaleServiceProvider>, LocaleServiceProviderPool> poolOfPools;
    private Set<Locale> availableLocales;
    private Set<Locale> providerLocales;
    private Set<LocaleServiceProvider> providers;
    private Map<Locale, LocaleServiceProvider> providersCache;

    public interface LocalizedObjectGetter<P, S> {
        S getObject(P p, Locale locale, String str, Object... objArr);
    }

    /* renamed from: sun.util.LocaleServiceProviderPool.1 */
    class AnonymousClass1 implements PrivilegedExceptionAction<Object> {
        final /* synthetic */ Class val$c;

        AnonymousClass1(Class val$c) {
            this.val$c = val$c;
        }

        public Object run() {
            for (LocaleServiceProvider provider : ServiceLoader.loadInstalled(this.val$c)) {
                LocaleServiceProviderPool.this.providers.add(provider);
            }
            return null;
        }
    }

    private static class AllAvailableLocales {
        static final Locale[] allAvailableLocales = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.LocaleServiceProviderPool.AllAvailableLocales.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.LocaleServiceProviderPool.AllAvailableLocales.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.LocaleServiceProviderPool.AllAvailableLocales.<clinit>():void");
        }

        private AllAvailableLocales() {
        }
    }

    private static class NullProvider extends LocaleServiceProvider {
        private static final NullProvider INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.LocaleServiceProviderPool.NullProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.LocaleServiceProviderPool.NullProvider.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.LocaleServiceProviderPool.NullProvider.<clinit>():void");
        }

        private NullProvider() {
        }

        public Locale[] getAvailableLocales() {
            throw new RuntimeException("Should not get called.");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.LocaleServiceProviderPool.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.LocaleServiceProviderPool.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.LocaleServiceProviderPool.<clinit>():void");
    }

    public static LocaleServiceProviderPool getPool(Class<? extends LocaleServiceProvider> providerClass) {
        LocaleServiceProviderPool pool = (LocaleServiceProviderPool) poolOfPools.get(providerClass);
        if (pool != null) {
            return pool;
        }
        LocaleServiceProviderPool newPool = new LocaleServiceProviderPool(providerClass);
        pool = (LocaleServiceProviderPool) poolOfPools.putIfAbsent(providerClass, newPool);
        if (pool == null) {
            return newPool;
        }
        return pool;
    }

    private LocaleServiceProviderPool(Class<? extends LocaleServiceProvider> c) {
        this.providers = new LinkedHashSet();
        this.providersCache = new ConcurrentHashMap();
        this.availableLocales = null;
        this.providerLocales = null;
        try {
            AccessController.doPrivileged(new AnonymousClass1(c));
        } catch (PrivilegedActionException e) {
            config(e.toString());
        }
    }

    private static void config(String message) {
        PlatformLogger.getLogger("sun.util.LocaleServiceProviderPool").config(message);
    }

    public static Locale[] getAllAvailableLocales() {
        return (Locale[]) AllAvailableLocales.allAvailableLocales.clone();
    }

    public synchronized Locale[] getAvailableLocales() {
        Locale[] tmp;
        if (this.availableLocales == null) {
            this.availableLocales = new HashSet(getJRELocales());
            if (hasProviders()) {
                this.availableLocales.addAll(getProviderLocales());
            }
        }
        tmp = new Locale[this.availableLocales.size()];
        this.availableLocales.toArray(tmp);
        return tmp;
    }

    private synchronized Set<Locale> getProviderLocales() {
        if (this.providerLocales == null) {
            this.providerLocales = new HashSet();
            if (hasProviders()) {
                for (LocaleServiceProvider lsp : this.providers) {
                    for (Locale locale : lsp.getAvailableLocales()) {
                        this.providerLocales.add(getLookupLocale(locale));
                    }
                }
            }
        }
        return this.providerLocales;
    }

    public boolean hasProviders() {
        return !this.providers.isEmpty();
    }

    private List<Locale> getJRELocales() {
        if (availableJRELocales == null) {
            synchronized (LocaleServiceProviderPool.class) {
                if (availableJRELocales == null) {
                    Locale[] allLocales = ICU.getAvailableLocales();
                    List<Locale> tmpList = new ArrayList(allLocales.length);
                    for (Locale locale : allLocales) {
                        tmpList.add(getLookupLocale(locale));
                    }
                    availableJRELocales = tmpList;
                }
            }
        }
        return availableJRELocales;
    }

    private boolean isJRESupported(Locale locale) {
        return getJRELocales().contains(getLookupLocale(locale));
    }

    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, Object... params) {
        return getLocalizedObjectImpl(getter, locale, true, null, null, null, params);
    }

    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, OpenListResourceBundle bundle, String key, Object... params) {
        return getLocalizedObjectImpl(getter, locale, false, null, bundle, key, params);
    }

    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, String bundleKey, OpenListResourceBundle bundle, String key, Object... params) {
        return getLocalizedObjectImpl(getter, locale, false, bundleKey, bundle, key, params);
    }

    private <P, S> S getLocalizedObjectImpl(LocalizedObjectGetter<P, S> getter, Locale locale, boolean isObjectProvider, String bundleKey, OpenListResourceBundle bundle, String key, Object... params) {
        if (hasProviders()) {
            S providersObj;
            if (bundleKey == null) {
                bundleKey = key;
            }
            Object locale2 = bundle != null ? bundle.getLocale() : null;
            List<Locale> lookupLocales = getLookupLocales(locale);
            Set<Locale> provLoc = getProviderLocales();
            for (int i = 0; i < lookupLocales.size(); i++) {
                Locale current = (Locale) lookupLocales.get(i);
                if (locale2 == null) {
                    if (isJRESupported(current)) {
                        break;
                    }
                } else if (current.equals(locale2)) {
                    break;
                }
                if (provLoc.contains(current)) {
                    Object lsp = findProvider(current);
                    if (lsp != null) {
                        providersObj = getter.getObject(lsp, locale, key, params);
                        if (providersObj != null) {
                            return providersObj;
                        }
                        if (isObjectProvider) {
                            config("A locale sensitive service provider returned null for a localized objects,  which should not happen.  provider: " + lsp + " locale: " + locale);
                        }
                    } else {
                        continue;
                    }
                }
            }
            while (bundle != null) {
                Locale bundleLocale = bundle.getLocale();
                if (bundle.handleGetKeys().contains(bundleKey)) {
                    return null;
                }
                P lsp2 = findProvider(bundleLocale);
                if (lsp2 != null) {
                    providersObj = getter.getObject(lsp2, locale, key, params);
                    if (providersObj != null) {
                        return providersObj;
                    }
                }
                bundle = bundle.getParent();
            }
        }
        return null;
    }

    private LocaleServiceProvider findProvider(Locale locale) {
        if (!hasProviders()) {
            return null;
        }
        if (this.providersCache.containsKey(locale)) {
            LocaleServiceProvider provider = (LocaleServiceProvider) this.providersCache.get(locale);
            if (provider != NullProvider.INSTANCE) {
                return provider;
            }
        }
        for (LocaleServiceProvider lsp : this.providers) {
            for (Locale available : lsp.getAvailableLocales()) {
                if (locale.equals(getLookupLocale(available))) {
                    LocaleServiceProvider providerInCache = (LocaleServiceProvider) this.providersCache.put(locale, lsp);
                    if (providerInCache == null) {
                        providerInCache = lsp;
                    }
                    return providerInCache;
                }
            }
        }
        this.providersCache.put(locale, NullProvider.INSTANCE);
        return null;
    }

    private static List<Locale> getLookupLocales(Locale locale) {
        return new Control() {
        }.getCandidateLocales("", locale);
    }

    private static Locale getLookupLocale(Locale locale) {
        Locale lookupLocale = locale;
        if (locale.getExtensionKeys().isEmpty() || locale.equals(locale_ja_JP_JP) || locale.equals(locale_th_TH_TH)) {
            return lookupLocale;
        }
        Builder locbld = new Builder();
        try {
            locbld.setLocale(locale);
            locbld.clearExtensions();
            return locbld.build();
        } catch (IllformedLocaleException e) {
            config("A locale(" + locale + ") has non-empty extensions, but has illformed fields.");
            return new Locale(locale.getLanguage(), locale.getCountry(), locale.getVariant());
        }
    }
}
