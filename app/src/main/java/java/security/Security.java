package java.security;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import sun.security.jca.GetInstance;
import sun.security.jca.ProviderList;
import sun.security.jca.Providers;

public final class Security {
    private static final Properties props = null;
    private static final Map<String, Class> spiMap = null;
    private static final AtomicInteger version = null;

    /* renamed from: java.security.Security.1 */
    static class AnonymousClass1 implements PrivilegedAction<Void> {
        final /* synthetic */ boolean val$pa;

        AnonymousClass1(boolean val$pa) {
            this.val$pa = val$pa;
        }

        public Void run() {
            try {
                Field f;
                boolean accessible;
                Class cl = Class.forName("java.lang.SecurityManager", false, null);
                if (this.val$pa) {
                    f = cl.getDeclaredField("packageAccessValid");
                    accessible = f.isAccessible();
                    f.setAccessible(true);
                } else {
                    f = cl.getDeclaredField("packageDefinitionValid");
                    accessible = f.isAccessible();
                    f.setAccessible(true);
                }
                f.setBoolean(f, false);
                f.setAccessible(accessible);
            } catch (Exception e) {
            }
            return null;
        }
    }

    private static class ProviderProperty {
        String className;
        Provider provider;

        private ProviderProperty() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.Security.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.Security.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.security.Security.<clinit>():void");
    }

    private static void initializeStatic() {
        props.put("security.provider.1", "com.android.org.conscrypt.OpenSSLProvider");
        props.put("security.provider.2", "sun.security.provider.CertPathProvider");
        props.put("security.provider.3", "com.android.org.bouncycastle.jce.provider.BouncyCastleProvider");
        props.put("security.provider.4", "com.android.org.conscrypt.JSSEProvider");
    }

    private Security() {
    }

    private static ProviderProperty getProviderProperty(String key) {
        List<Provider> providers = Providers.getProviderList().providers();
        for (int i = 0; i < providers.size(); i++) {
            Provider prov = (Provider) providers.get(i);
            String prop = prov.getProperty(key);
            if (prop == null) {
                Enumeration<Object> e = prov.keys();
                while (e.hasMoreElements() && prop == null) {
                    String matchKey = (String) e.nextElement();
                    if (key.equalsIgnoreCase(matchKey)) {
                        prop = prov.getProperty(matchKey);
                        break;
                    }
                }
            }
            if (prop != null) {
                ProviderProperty newEntry = new ProviderProperty();
                newEntry.className = prop;
                newEntry.provider = prov;
                return newEntry;
            }
        }
        return null;
    }

    private static String getProviderProperty(String key, Provider provider) {
        String prop = provider.getProperty(key);
        if (prop != null) {
            return prop;
        }
        Enumeration<Object> e = provider.keys();
        while (e.hasMoreElements() && prop == null) {
            String matchKey = (String) e.nextElement();
            if (key.equalsIgnoreCase(matchKey)) {
                return provider.getProperty(matchKey);
            }
        }
        return prop;
    }

    @Deprecated
    public static String getAlgorithmProperty(String algName, String propName) {
        ProviderProperty entry = getProviderProperty("Alg." + propName + "." + algName);
        if (entry != null) {
            return entry.className;
        }
        return null;
    }

    public static synchronized int insertProviderAt(Provider provider, int position) {
        synchronized (Security.class) {
            String providerName = provider.getName();
            check("insertProvider." + providerName);
            ProviderList list = Providers.getFullProviderList();
            ProviderList newList = ProviderList.insertAt(list, provider, position - 1);
            if (list == newList) {
                return -1;
            }
            increaseVersion();
            Providers.setProviderList(newList);
            int index = newList.getIndex(providerName) + 1;
            return index;
        }
    }

    public static int addProvider(Provider provider) {
        return insertProviderAt(provider, 0);
    }

    public static synchronized void removeProvider(String name) {
        synchronized (Security.class) {
            check("removeProvider." + name);
            Providers.setProviderList(ProviderList.remove(Providers.getFullProviderList(), name));
            increaseVersion();
        }
    }

    public static Provider[] getProviders() {
        return Providers.getFullProviderList().toArray();
    }

    public static Provider getProvider(String name) {
        return Providers.getProviderList().getProvider(name);
    }

    public static Provider[] getProviders(String filter) {
        String key;
        String value;
        int index = filter.indexOf(58);
        if (index == -1) {
            key = filter;
            value = "";
        } else {
            key = filter.substring(0, index);
            value = filter.substring(index + 1);
        }
        Map hashtableFilter = new Hashtable(1);
        hashtableFilter.put(key, value);
        return getProviders(hashtableFilter);
    }

    public static Provider[] getProviders(Map<String, String> filter) {
        Provider[] allProviders = getProviders();
        Set<String> keySet = filter.keySet();
        LinkedHashSet<Provider> candidates = new LinkedHashSet(5);
        if (keySet == null || allProviders == null) {
            return allProviders;
        }
        boolean firstSearch = true;
        for (String key : keySet) {
            LinkedHashSet<Provider> newCandidates = getAllQualifyingCandidates(key, (String) filter.get(key), allProviders);
            if (firstSearch) {
                candidates = newCandidates;
                firstSearch = false;
            }
            if (newCandidates == null || newCandidates.isEmpty()) {
                candidates = null;
                break;
            }
            Iterator<Provider> cansIte = candidates.iterator();
            while (cansIte.hasNext()) {
                if (!newCandidates.contains((Provider) cansIte.next())) {
                    cansIte.remove();
                }
            }
        }
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Object[] candidatesArray = candidates.toArray();
        Provider[] result = new Provider[candidatesArray.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Provider) candidatesArray[i];
        }
        return result;
    }

    private static Class getSpiClass(String type) {
        Class clazz = (Class) spiMap.get(type);
        if (clazz != null) {
            return clazz;
        }
        try {
            clazz = Class.forName("java.security." + type + "Spi");
            spiMap.put(type, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Spi class not found", e);
        }
    }

    static Object[] getImpl(String algorithm, String type, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null) {
            return GetInstance.getInstance(type, getSpiClass(type), algorithm).toArray();
        }
        return GetInstance.getInstance(type, getSpiClass(type), algorithm, provider).toArray();
    }

    static Object[] getImpl(String algorithm, String type, String provider, Object params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if (provider == null) {
            return GetInstance.getInstance(type, getSpiClass(type), algorithm, params).toArray();
        }
        return GetInstance.getInstance(type, getSpiClass(type), algorithm, params, provider).toArray();
    }

    static Object[] getImpl(String algorithm, String type, Provider provider) throws NoSuchAlgorithmException {
        return GetInstance.getInstance(type, getSpiClass(type), algorithm, provider).toArray();
    }

    static Object[] getImpl(String algorithm, String type, Provider provider, Object params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        return GetInstance.getInstance(type, getSpiClass(type), algorithm, params, provider).toArray();
    }

    public static String getProperty(String key) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("getProperty." + key));
        }
        String name = props.getProperty(key);
        if (name != null) {
            return name.trim();
        }
        return name;
    }

    public static void setProperty(String key, String datum) {
        check("setProperty." + key);
        props.put(key, datum);
        increaseVersion();
        invalidateSMCache(key);
    }

    private static void invalidateSMCache(String key) {
        boolean pa = key.equals("package.access");
        boolean pd = key.equals("package.definition");
        if (pa || pd) {
            AccessController.doPrivileged(new AnonymousClass1(pa));
        }
    }

    private static void check(String directive) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }

    private static LinkedHashSet<Provider> getAllQualifyingCandidates(String filterKey, String filterValue, Provider[] allProviders) {
        String[] filterComponents = getFilterComponents(filterKey, filterValue);
        return getProvidersNotUsingCache(filterComponents[0], filterComponents[1], filterComponents[2], filterValue, allProviders);
    }

    private static LinkedHashSet<Provider> getProvidersNotUsingCache(String serviceName, String algName, String attrName, String filterValue, Provider[] allProviders) {
        LinkedHashSet<Provider> candidates = new LinkedHashSet(5);
        for (int i = 0; i < allProviders.length; i++) {
            if (isCriterionSatisfied(allProviders[i], serviceName, algName, attrName, filterValue)) {
                candidates.add(allProviders[i]);
            }
        }
        return candidates;
    }

    private static boolean isCriterionSatisfied(Provider prov, String serviceName, String algName, String attrName, String filterValue) {
        String key = serviceName + '.' + algName;
        if (attrName != null) {
            key = key + ' ' + attrName;
        }
        String propValue = getProviderProperty(key, prov);
        if (propValue == null) {
            String standardName = getProviderProperty("Alg.Alias." + serviceName + "." + algName, prov);
            if (standardName != null) {
                key = serviceName + "." + standardName;
                if (attrName != null) {
                    key = key + ' ' + attrName;
                }
                propValue = getProviderProperty(key, prov);
            }
            if (propValue == null) {
                return false;
            }
        }
        if (attrName == null) {
            return true;
        }
        if (isStandardAttr(attrName)) {
            return isConstraintSatisfied(attrName, filterValue, propValue);
        }
        return filterValue.equalsIgnoreCase(propValue);
    }

    private static boolean isStandardAttr(String attribute) {
        if (attribute.equalsIgnoreCase("KeySize") || attribute.equalsIgnoreCase("ImplementedIn")) {
            return true;
        }
        return false;
    }

    private static boolean isConstraintSatisfied(String attribute, String value, String prop) {
        if (attribute.equalsIgnoreCase("KeySize")) {
            if (Integer.parseInt(value) <= Integer.parseInt(prop)) {
                return true;
            }
            return false;
        } else if (attribute.equalsIgnoreCase("ImplementedIn")) {
            return value.equalsIgnoreCase(prop);
        } else {
            return false;
        }
    }

    static String[] getFilterComponents(String filterKey, String filterValue) {
        int algIndex = filterKey.indexOf(46);
        if (algIndex < 0) {
            throw new InvalidParameterException("Invalid filter");
        }
        String algName;
        String serviceName = filterKey.substring(0, algIndex);
        String str = null;
        if (filterValue.length() == 0) {
            algName = filterKey.substring(algIndex + 1).trim();
            if (algName.length() == 0) {
                throw new InvalidParameterException("Invalid filter");
            }
        }
        int attrIndex = filterKey.indexOf(32);
        if (attrIndex == -1) {
            throw new InvalidParameterException("Invalid filter");
        }
        str = filterKey.substring(attrIndex + 1).trim();
        if (str.length() == 0) {
            throw new InvalidParameterException("Invalid filter");
        } else if (attrIndex < algIndex || algIndex == attrIndex - 1) {
            throw new InvalidParameterException("Invalid filter");
        } else {
            algName = filterKey.substring(algIndex + 1, attrIndex);
        }
        return new String[]{serviceName, algName, str};
    }

    public static Set<String> getAlgorithms(String serviceName) {
        if (serviceName == null || serviceName.length() == 0 || serviceName.endsWith(".")) {
            return Collections.EMPTY_SET;
        }
        HashSet<String> result = new HashSet();
        Provider[] providers = getProviders();
        for (Provider keys : providers) {
            Enumeration<Object> e = keys.keys();
            while (e.hasMoreElements()) {
                String currentKey = ((String) e.nextElement()).toUpperCase();
                if (currentKey.startsWith(serviceName.toUpperCase()) && currentKey.indexOf(" ") < 0) {
                    result.add(currentKey.substring(serviceName.length() + 1));
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public static void increaseVersion() {
        version.incrementAndGet();
    }

    public static int getVersion() {
        return version.get();
    }
}
