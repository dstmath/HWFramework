package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.Debug;

public abstract class Provider extends Properties {
    private static final int ALIAS_LENGTH = ALIAS_PREFIX.length();
    private static final String ALIAS_PREFIX = "Alg.Alias.";
    private static final String ALIAS_PREFIX_LOWER = "alg.alias.";
    private static final Debug debug = Debug.getInstance("provider", "Provider");
    private static final Map<String, EngineDescription> knownEngines = new HashMap();
    private static volatile ServiceKey previousKey = new ServiceKey("", "", false, null);
    static final long serialVersionUID = -4298000515446427739L;
    private transient Set<Entry<Object, Object>> entrySet = null;
    private transient int entrySetCallCount = 0;
    private String info;
    private transient boolean initialized;
    private transient boolean legacyChanged;
    private transient Map<ServiceKey, Service> legacyMap;
    private transient Map<String, String> legacyStrings;
    private String name;
    private volatile boolean registered = false;
    private transient Map<ServiceKey, Service> serviceMap;
    private transient Set<Service> serviceSet;
    private transient boolean servicesChanged;
    private double version;

    private static class EngineDescription {
        private volatile Class<?> constructorParameterClass;
        final String constructorParameterClassName;
        final String name;
        final boolean supportsParameter;

        EngineDescription(String name, boolean sp, String paramName) {
            this.name = name;
            this.supportsParameter = sp;
            this.constructorParameterClassName = paramName;
        }

        Class<?> getConstructorParameterClass() throws ClassNotFoundException {
            Class<?> clazz = this.constructorParameterClass;
            if (clazz != null) {
                return clazz;
            }
            clazz = Class.forName(this.constructorParameterClassName);
            this.constructorParameterClass = clazz;
            return clazz;
        }
    }

    public static class Service {
        private static final Class<?>[] CLASS0 = new Class[0];
        private String algorithm;
        private List<String> aliases;
        private Map<UString, String> attributes;
        private String className;
        private volatile Reference<Class<?>> classRef;
        private volatile Boolean hasKeyAttributes;
        private final Provider provider;
        private boolean registered;
        private Class[] supportedClasses;
        private String[] supportedFormats;
        private String type;

        /* synthetic */ Service(Provider provider, Service -this1) {
            this(provider);
        }

        private Service(Provider provider) {
            this.provider = provider;
            this.aliases = Collections.emptyList();
            this.attributes = Collections.emptyMap();
        }

        private boolean isValid() {
            return (this.type == null || this.algorithm == null || this.className == null) ? false : true;
        }

        private void addAlias(String alias) {
            if (this.aliases.isEmpty()) {
                this.aliases = new ArrayList(2);
            }
            this.aliases.add(alias);
        }

        void addAttribute(String type, String value) {
            if (this.attributes.isEmpty()) {
                this.attributes = new HashMap(8);
            }
            this.attributes.put(new UString(type), value);
        }

        public Service(Provider provider, String type, String algorithm, String className, List<String> aliases, Map<String, String> attributes) {
            if (provider == null || type == null || algorithm == null || className == null) {
                throw new NullPointerException();
            }
            this.provider = provider;
            this.type = Provider.getEngineName(type);
            this.algorithm = algorithm;
            this.className = className;
            if (aliases == null) {
                this.aliases = Collections.emptyList();
            } else {
                this.aliases = new ArrayList((Collection) aliases);
            }
            if (attributes == null) {
                this.attributes = Collections.emptyMap();
                return;
            }
            this.attributes = new HashMap();
            for (Entry<String, String> entry : attributes.entrySet()) {
                this.attributes.put(new UString((String) entry.getKey()), (String) entry.getValue());
            }
        }

        public final String getType() {
            return this.type;
        }

        public final String getAlgorithm() {
            return this.algorithm;
        }

        public final Provider getProvider() {
            return this.provider;
        }

        public final String getClassName() {
            return this.className;
        }

        private final List<String> getAliases() {
            return this.aliases;
        }

        public final String getAttribute(String name) {
            if (name != null) {
                return (String) this.attributes.get(new UString(name));
            }
            throw new NullPointerException();
        }

        public Object newInstance(Object constructorParameter) throws NoSuchAlgorithmException {
            if (!this.registered) {
                if (this.provider.getService(this.type, this.algorithm) != this) {
                    throw new NoSuchAlgorithmException("Service not registered with Provider " + this.provider.getName() + ": " + this);
                }
                this.registered = true;
            }
            try {
                EngineDescription cap = (EngineDescription) Provider.knownEngines.get(this.type);
                if (cap == null) {
                    return newInstanceGeneric(constructorParameter);
                }
                if (cap.constructorParameterClassName != null) {
                    Class<?> paramClass = cap.getConstructorParameterClass();
                    if (constructorParameter == null || paramClass.isAssignableFrom(constructorParameter.getClass())) {
                        return getImplClass().getConstructor(paramClass).newInstance(constructorParameter);
                    }
                    throw new InvalidParameterException("constructorParameter must be instanceof " + cap.constructorParameterClassName.replace('$', '.') + " for engine type " + this.type);
                } else if (constructorParameter == null) {
                    return getImplClass().getConstructor(new Class[0]).newInstance(new Object[0]);
                } else {
                    throw new InvalidParameterException("constructorParameter not used with " + this.type + " engines");
                }
            } catch (NoSuchAlgorithmException e) {
                throw e;
            } catch (InvocationTargetException e2) {
                throw new NoSuchAlgorithmException("Error constructing implementation (algorithm: " + this.algorithm + ", provider: " + this.provider.getName() + ", class: " + this.className + ")", e2.getCause());
            } catch (Exception e3) {
                throw new NoSuchAlgorithmException("Error constructing implementation (algorithm: " + this.algorithm + ", provider: " + this.provider.getName() + ", class: " + this.className + ")", e3);
            }
        }

        private Class<?> getImplClass() throws NoSuchAlgorithmException {
            try {
                Reference<Class<?>> ref = this.classRef;
                Class<?> clazz = ref == null ? null : (Class) ref.get();
                if (clazz == null) {
                    ClassLoader cl = this.provider.getClass().getClassLoader();
                    if (cl == null) {
                        clazz = Class.forName(this.className);
                    } else {
                        clazz = cl.loadClass(this.className);
                    }
                    if (Modifier.isPublic(clazz.getModifiers())) {
                        this.classRef = new WeakReference(clazz);
                    } else {
                        throw new NoSuchAlgorithmException("class configured for " + this.type + " (provider: " + this.provider.getName() + ") is not public.");
                    }
                }
                return clazz;
            } catch (ClassNotFoundException e) {
                throw new NoSuchAlgorithmException("class configured for " + this.type + " (provider: " + this.provider.getName() + ") cannot be found.", e);
            }
        }

        private Object newInstanceGeneric(Object constructorParameter) throws Exception {
            Class<?> clazz = getImplClass();
            if (constructorParameter == null) {
                try {
                    return clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                } catch (NoSuchMethodException e) {
                    throw new NoSuchAlgorithmException("No public no-arg constructor found in class " + this.className);
                }
            }
            Class<?> argClass = constructorParameter.getClass();
            for (Constructor<?> con : clazz.getConstructors()) {
                Class<?>[] paramTypes = con.getParameterTypes();
                if (paramTypes.length == 1 && paramTypes[0].isAssignableFrom(argClass)) {
                    return con.newInstance(constructorParameter);
                }
            }
            throw new NoSuchAlgorithmException("No public constructor matching " + argClass.getName() + " found in class " + this.className);
        }

        public boolean supportsParameter(Object parameter) {
            EngineDescription cap = (EngineDescription) Provider.knownEngines.get(this.type);
            if (cap == null) {
                return true;
            }
            if (!cap.supportsParameter) {
                throw new InvalidParameterException("supportsParameter() not used with " + this.type + " engines");
            } else if (parameter != null && !(parameter instanceof Key)) {
                throw new InvalidParameterException("Parameter must be instanceof Key for engine " + this.type);
            } else if (!hasKeyAttributes()) {
                return true;
            } else {
                if (parameter == null) {
                    return false;
                }
                Key key = (Key) parameter;
                return supportsKeyFormat(key) || supportsKeyClass(key);
            }
        }

        private boolean hasKeyAttributes() {
            Boolean b = this.hasKeyAttributes;
            if (b == null) {
                synchronized (this) {
                    String s = getAttribute("SupportedKeyFormats");
                    if (s != null) {
                        this.supportedFormats = s.split("\\|");
                    }
                    s = getAttribute("SupportedKeyClasses");
                    if (s != null) {
                        String[] classNames = s.split("\\|");
                        List<Class<?>> classList = new ArrayList(classNames.length);
                        for (String className : classNames) {
                            Class<?> clazz = getKeyClass(className);
                            if (clazz != null) {
                                classList.add(clazz);
                            }
                        }
                        this.supportedClasses = (Class[]) classList.toArray(CLASS0);
                    }
                    boolean bool = this.supportedFormats == null ? this.supportedClasses != null : true;
                    b = Boolean.valueOf(bool);
                    this.hasKeyAttributes = b;
                }
            }
            return b.booleanValue();
        }

        private Class<?> getKeyClass(String name) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                try {
                    ClassLoader cl = this.provider.getClass().getClassLoader();
                    if (cl != null) {
                        return cl.loadClass(name);
                    }
                } catch (ClassNotFoundException e2) {
                }
                return null;
            }
        }

        private boolean supportsKeyFormat(Key key) {
            if (this.supportedFormats == null) {
                return false;
            }
            String format = key.getFormat();
            if (format == null) {
                return false;
            }
            for (String supportedFormat : this.supportedFormats) {
                if (supportedFormat.equals(format)) {
                    return true;
                }
            }
            return false;
        }

        private boolean supportsKeyClass(Key key) {
            if (this.supportedClasses == null) {
                return false;
            }
            Class<?> keyClass = key.getClass();
            for (Class<?> clazz : this.supportedClasses) {
                if (clazz.isAssignableFrom(keyClass)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return this.provider.getName() + ": " + this.type + "." + this.algorithm + " -> " + this.className + (this.aliases.isEmpty() ? "" : "\r\n  aliases: " + this.aliases.toString()) + (this.attributes.isEmpty() ? "" : "\r\n  attributes: " + this.attributes.toString()) + "\r\n";
        }
    }

    private static class ServiceKey {
        private final String algorithm;
        private final String originalAlgorithm;
        private final String type;

        /* synthetic */ ServiceKey(String type, String algorithm, boolean intern, ServiceKey -this3) {
            this(type, algorithm, intern);
        }

        private ServiceKey(String type, String algorithm, boolean intern) {
            this.type = type;
            this.originalAlgorithm = algorithm;
            algorithm = algorithm.toUpperCase(Locale.ENGLISH);
            if (intern) {
                algorithm = algorithm.intern();
            }
            this.algorithm = algorithm;
        }

        public int hashCode() {
            return this.type.hashCode() + this.algorithm.hashCode();
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ServiceKey)) {
                return false;
            }
            ServiceKey other = (ServiceKey) obj;
            if (this.type.equals(other.type)) {
                z = this.algorithm.equals(other.algorithm);
            }
            return z;
        }

        boolean matches(String type, String algorithm) {
            return this.type == type && this.originalAlgorithm == algorithm;
        }
    }

    private static class UString {
        final String lowerString;
        final String string;

        UString(String s) {
            this.string = s;
            this.lowerString = s.toLowerCase(Locale.ENGLISH);
        }

        public int hashCode() {
            return this.lowerString.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof UString)) {
                return false;
            }
            return this.lowerString.equals(((UString) obj).lowerString);
        }

        public String toString() {
            return this.string;
        }
    }

    static {
        addEngine("AlgorithmParameterGenerator", false, null);
        addEngine("AlgorithmParameters", false, null);
        addEngine("KeyFactory", false, null);
        addEngine("KeyPairGenerator", false, null);
        addEngine("KeyStore", false, null);
        addEngine(PKCS9Attribute.MESSAGE_DIGEST_STR, false, null);
        addEngine("SecureRandom", false, null);
        addEngine("Signature", true, null);
        addEngine("CertificateFactory", false, null);
        addEngine("CertPathBuilder", false, null);
        addEngine("CertPathValidator", false, null);
        addEngine("CertStore", false, "java.security.cert.CertStoreParameters");
        addEngine("Cipher", true, null);
        addEngine("ExemptionMechanism", false, null);
        addEngine("Mac", true, null);
        addEngine("KeyAgreement", true, null);
        addEngine("KeyGenerator", false, null);
        addEngine("SecretKeyFactory", false, null);
        addEngine("KeyManagerFactory", false, null);
        addEngine("SSLContext", false, null);
        addEngine("TrustManagerFactory", false, null);
        addEngine("GssApiMechanism", false, null);
        addEngine("SaslClientFactory", false, null);
        addEngine("SaslServerFactory", false, null);
        addEngine("Policy", false, "java.security.Policy$Parameters");
        addEngine("Configuration", false, "javax.security.auth.login.Configuration$Parameters");
        addEngine("XMLSignatureFactory", false, null);
        addEngine("KeyInfoFactory", false, null);
        addEngine("TransformService", false, null);
        addEngine("TerminalFactory", false, "java.lang.Object");
    }

    protected Provider(String name, double version, String info) {
        this.name = name;
        this.version = version;
        this.info = info;
        putId();
        this.initialized = true;
    }

    public String getName() {
        return this.name;
    }

    public double getVersion() {
        return this.version;
    }

    public String getInfo() {
        return this.info;
    }

    public String toString() {
        return this.name + " version " + this.version;
    }

    public synchronized void clear() {
        check("clearProviderProperties." + this.name);
        if (debug != null) {
            debug.println("Remove " + this.name + " provider properties");
        }
        implClear();
    }

    public synchronized void load(InputStream inStream) throws IOException {
        check("putProviderProperty." + this.name);
        if (debug != null) {
            debug.println("Load " + this.name + " provider properties");
        }
        Properties tempProperties = new Properties();
        tempProperties.load(inStream);
        implPutAll(tempProperties);
    }

    public synchronized void putAll(Map<?, ?> t) {
        check("putProviderProperty." + this.name);
        if (debug != null) {
            debug.println("Put all " + this.name + " provider properties");
        }
        implPutAll(t);
    }

    public synchronized Set<Entry<Object, Object>> entrySet() {
        checkInitialized();
        if (this.entrySet == null) {
            int i = this.entrySetCallCount;
            this.entrySetCallCount = i + 1;
            if (i == 0) {
                this.entrySet = Collections.unmodifiableMap(this).entrySet();
            } else {
                return super.entrySet();
            }
        }
        if (this.entrySetCallCount != 2) {
            throw new RuntimeException("Internal error.");
        }
        return this.entrySet;
    }

    public Set<Object> keySet() {
        checkInitialized();
        return Collections.unmodifiableSet(super.keySet());
    }

    public Collection<Object> values() {
        checkInitialized();
        return Collections.unmodifiableCollection(super.values());
    }

    public synchronized Object put(Object key, Object value) {
        check("putProviderProperty." + this.name);
        if (debug != null) {
            debug.println("Set " + this.name + " provider property [" + key + "/" + value + "]");
        }
        return implPut(key, value);
    }

    public synchronized Object putIfAbsent(Object key, Object value) {
        check("putProviderProperty." + this.name);
        if (debug != null) {
            debug.println("Set " + this.name + " provider property [" + key + "/" + value + "]");
        }
        return implPutIfAbsent(key, value);
    }

    public synchronized Object remove(Object key) {
        check("removeProviderProperty." + this.name);
        if (debug != null) {
            debug.println("Remove " + this.name + " provider property " + key);
        }
        return implRemove(key);
    }

    public synchronized boolean remove(Object key, Object value) {
        check("removeProviderProperty." + this.name);
        if (debug != null) {
            debug.println("Remove " + this.name + " provider property " + key);
        }
        return implRemove(key, value);
    }

    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        check("putProviderProperty." + this.name);
        if (debug != null) {
            debug.println("Replace " + this.name + " provider property " + key);
        }
        return implReplace(key, oldValue, newValue);
    }

    public synchronized Object replace(Object key, Object value) {
        check("putProviderProperty." + this.name);
        if (debug != null) {
            debug.println("Replace " + this.name + " provider property " + key);
        }
        return implReplace(key, value);
    }

    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ? extends Object> function) {
        check("putProviderProperty." + this.name);
        if (debug != null) {
            debug.println("ReplaceAll " + this.name + " provider property ");
        }
        implReplaceAll(function);
    }

    public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        check("putProviderProperty." + this.name);
        check("removeProviderProperty" + this.name);
        if (debug != null) {
            debug.println("Compute " + this.name + " provider property " + key);
        }
        return implCompute(key, remappingFunction);
    }

    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ? extends Object> mappingFunction) {
        check("putProviderProperty." + this.name);
        check("removeProviderProperty" + this.name);
        if (debug != null) {
            debug.println("ComputeIfAbsent " + this.name + " provider property " + key);
        }
        return implComputeIfAbsent(key, mappingFunction);
    }

    public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        check("putProviderProperty." + this.name);
        check("removeProviderProperty" + this.name);
        if (debug != null) {
            debug.println("ComputeIfPresent " + this.name + " provider property " + key);
        }
        return implComputeIfPresent(key, remappingFunction);
    }

    public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        check("putProviderProperty." + this.name);
        check("removeProviderProperty" + this.name);
        if (debug != null) {
            debug.println("Merge " + this.name + " provider property " + key);
        }
        return implMerge(key, value, remappingFunction);
    }

    public Object get(Object key) {
        checkInitialized();
        return super.get(key);
    }

    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        checkInitialized();
        return super.getOrDefault(key, defaultValue);
    }

    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        checkInitialized();
        super.forEach(action);
    }

    public Enumeration<Object> keys() {
        checkInitialized();
        return super.keys();
    }

    public Enumeration<Object> elements() {
        checkInitialized();
        return super.elements();
    }

    public String getProperty(String key) {
        checkInitialized();
        return super.getProperty(key);
    }

    private void checkInitialized() {
        if (!this.initialized) {
            throw new IllegalStateException();
        }
    }

    private void check(String directive) {
        checkInitialized();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }

    private void putId() {
        super.put("Provider.id name", String.valueOf(this.name));
        super.put("Provider.id version", String.valueOf(this.version));
        super.put("Provider.id info", String.valueOf(this.info));
        super.put("Provider.id className", getClass().getName());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.registered = false;
        Map<Object, Object> copy = new HashMap();
        for (Entry<Object, Object> entry : super.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        this.defaults = null;
        in.defaultReadObject();
        implClear();
        this.initialized = true;
        putAll(copy);
    }

    private boolean checkLegacy(Object key) {
        if (this.registered) {
            Security.increaseVersion();
        }
        if (((String) key).startsWith("Provider.")) {
            return false;
        }
        this.legacyChanged = true;
        if (this.legacyStrings == null) {
            this.legacyStrings = new LinkedHashMap();
        }
        return true;
    }

    private void implPutAll(Map<?, ?> t) {
        for (Entry<?, ?> e : t.entrySet()) {
            implPut(e.getKey(), e.getValue());
        }
        if (this.registered) {
            Security.increaseVersion();
        }
    }

    private Object implRemove(Object key) {
        if (key instanceof String) {
            if (!checkLegacy(key)) {
                return null;
            }
            this.legacyStrings.remove((String) key);
        }
        return super.remove(key);
    }

    private boolean implRemove(Object key, Object value) {
        if ((key instanceof String) && (value instanceof String)) {
            if (!checkLegacy(key)) {
                return false;
            }
            this.legacyStrings.remove((String) key, value);
        }
        return super.remove(key, value);
    }

    private boolean implReplace(Object key, Object oldValue, Object newValue) {
        if ((key instanceof String) && (oldValue instanceof String) && (newValue instanceof String)) {
            if (!checkLegacy(key)) {
                return false;
            }
            this.legacyStrings.replace((String) key, (String) oldValue, (String) newValue);
        }
        return super.replace(key, oldValue, newValue);
    }

    private Object implReplace(Object key, Object value) {
        if ((key instanceof String) && (value instanceof String)) {
            if (!checkLegacy(key)) {
                return null;
            }
            this.legacyStrings.replace((String) key, (String) value);
        }
        return super.replace(key, value);
    }

    private void implReplaceAll(BiFunction<? super Object, ? super Object, ? extends Object> function) {
        this.legacyChanged = true;
        if (this.legacyStrings == null) {
            this.legacyStrings = new LinkedHashMap();
        } else {
            this.legacyStrings.replaceAll(function);
        }
        super.replaceAll(function);
    }

    private Object implMerge(Object key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        if ((key instanceof String) && (value instanceof String)) {
            if (!checkLegacy(key)) {
                return null;
            }
            this.legacyStrings.merge((String) key, (String) value, remappingFunction);
        }
        return super.merge(key, value, remappingFunction);
    }

    private Object implCompute(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        if (key instanceof String) {
            if (!checkLegacy(key)) {
                return null;
            }
            this.legacyStrings.compute((String) key, remappingFunction);
        }
        return super.compute(key, remappingFunction);
    }

    private Object implComputeIfAbsent(Object key, Function<? super Object, ? extends Object> mappingFunction) {
        if (key instanceof String) {
            if (!checkLegacy(key)) {
                return null;
            }
            this.legacyStrings.computeIfAbsent((String) key, mappingFunction);
        }
        return super.computeIfAbsent(key, mappingFunction);
    }

    private Object implComputeIfPresent(Object key, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        if (key instanceof String) {
            if (!checkLegacy(key)) {
                return null;
            }
            this.legacyStrings.computeIfPresent((String) key, remappingFunction);
        }
        return super.computeIfPresent(key, remappingFunction);
    }

    private Object implPut(Object key, Object value) {
        if ((key instanceof String) && (value instanceof String)) {
            if (!checkLegacy(key)) {
                return null;
            }
            this.legacyStrings.put((String) key, (String) value);
        }
        return super.put(key, value);
    }

    private Object implPutIfAbsent(Object key, Object value) {
        if ((key instanceof String) && (value instanceof String)) {
            if (!checkLegacy(key)) {
                return null;
            }
            this.legacyStrings.putIfAbsent((String) key, (String) value);
        }
        return super.putIfAbsent(key, value);
    }

    private void implClear() {
        if (this.legacyStrings != null) {
            this.legacyStrings.clear();
        }
        if (this.legacyMap != null) {
            this.legacyMap.clear();
        }
        if (this.serviceMap != null) {
            this.serviceMap.clear();
        }
        this.legacyChanged = false;
        this.servicesChanged = false;
        this.serviceSet = null;
        super.clear();
        putId();
        if (this.registered) {
            Security.increaseVersion();
        }
    }

    private void ensureLegacyParsed() {
        if (this.legacyChanged && this.legacyStrings != null) {
            this.serviceSet = null;
            if (this.legacyMap == null) {
                this.legacyMap = new LinkedHashMap();
            } else {
                this.legacyMap.clear();
            }
            for (Entry<String, String> entry : this.legacyStrings.entrySet()) {
                parseLegacyPut((String) entry.getKey(), (String) entry.getValue());
            }
            removeInvalidServices(this.legacyMap);
            this.legacyChanged = false;
        }
    }

    private void removeInvalidServices(Map<ServiceKey, Service> map) {
        Iterator<Entry<ServiceKey, Service>> t = map.entrySet().iterator();
        while (t.hasNext()) {
            if (!((Service) ((Entry) t.next()).getValue()).isValid()) {
                t.remove();
            }
        }
    }

    private String[] getTypeAndAlgorithm(String key) {
        int i = key.indexOf(".");
        if (i < 1) {
            if (debug != null) {
                debug.println("Ignoring invalid entry in provider " + this.name + ":" + key);
            }
            return null;
        }
        String type = key.substring(0, i);
        String alg = key.substring(i + 1);
        return new String[]{type, alg};
    }

    private void parseLegacyPut(String name, String value) {
        String stdAlg;
        String[] typeAndAlg;
        String type;
        ServiceKey key;
        Service s;
        if (name.toLowerCase(Locale.ENGLISH).startsWith(ALIAS_PREFIX_LOWER)) {
            stdAlg = value;
            typeAndAlg = getTypeAndAlgorithm(name.substring(ALIAS_LENGTH));
            if (typeAndAlg != null) {
                type = getEngineName(typeAndAlg[0]);
                String aliasAlg = typeAndAlg[1].intern();
                key = new ServiceKey(type, value, true, null);
                s = (Service) this.legacyMap.get(key);
                if (s == null) {
                    s = new Service(this, null);
                    s.type = type;
                    s.algorithm = value;
                    this.legacyMap.put(key, s);
                }
                this.legacyMap.put(new ServiceKey(type, aliasAlg, true, null), s);
                s.addAlias(aliasAlg);
            } else {
                return;
            }
        }
        typeAndAlg = getTypeAndAlgorithm(name);
        if (typeAndAlg != null) {
            int i = typeAndAlg[1].indexOf(32);
            if (i == -1) {
                type = getEngineName(typeAndAlg[0]);
                stdAlg = typeAndAlg[1].intern();
                String className = value;
                key = new ServiceKey(type, stdAlg, true, null);
                s = (Service) this.legacyMap.get(key);
                if (s == null) {
                    s = new Service(this, null);
                    s.type = type;
                    s.algorithm = stdAlg;
                    this.legacyMap.put(key, s);
                }
                s.className = value;
            } else {
                String attributeValue = value;
                type = getEngineName(typeAndAlg[0]);
                String attributeString = typeAndAlg[1];
                stdAlg = attributeString.substring(0, i).intern();
                String attributeName = attributeString.substring(i + 1);
                while (attributeName.startsWith(" ")) {
                    attributeName = attributeName.substring(1);
                }
                attributeName = attributeName.intern();
                key = new ServiceKey(type, stdAlg, true, null);
                s = (Service) this.legacyMap.get(key);
                if (s == null) {
                    s = new Service(this, null);
                    s.type = type;
                    s.algorithm = stdAlg;
                    this.legacyMap.put(key, s);
                }
                s.addAttribute(attributeName, value);
            }
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0036, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Service getService(String type, String algorithm) {
        Service service = null;
        synchronized (this) {
            checkInitialized();
            ServiceKey key = previousKey;
            if (!key.matches(type, algorithm)) {
                key = new ServiceKey(type, algorithm, false, null);
                previousKey = key;
            }
            if (this.serviceMap != null) {
                Service service2 = (Service) this.serviceMap.get(key);
                if (service2 != null) {
                    return service2;
                }
            }
            ensureLegacyParsed();
            if (this.legacyMap != null) {
                service = (Service) this.legacyMap.get(key);
            }
        }
    }

    public synchronized Set<Service> getServices() {
        checkInitialized();
        if (this.legacyChanged || this.servicesChanged) {
            this.serviceSet = null;
        }
        if (this.serviceSet == null) {
            ensureLegacyParsed();
            Set<Service> set = new LinkedHashSet();
            if (this.serviceMap != null) {
                set.addAll(this.serviceMap.values());
            }
            if (this.legacyMap != null) {
                set.addAll(this.legacyMap.values());
            }
            this.serviceSet = Collections.unmodifiableSet(set);
            this.servicesChanged = false;
        }
        return this.serviceSet;
    }

    protected synchronized void putService(Service s) {
        check("putProviderProperty." + this.name);
        if (debug != null) {
            debug.println(this.name + ".putService(): " + s);
        }
        if (s == null) {
            throw new NullPointerException();
        } else if (s.getProvider() != this) {
            throw new IllegalArgumentException("service.getProvider() must match this Provider object");
        } else {
            if (this.serviceMap == null) {
                this.serviceMap = new LinkedHashMap();
            }
            this.servicesChanged = true;
            String type = s.getType();
            ServiceKey key = new ServiceKey(type, s.getAlgorithm(), true, null);
            implRemoveService((Service) this.serviceMap.get(key));
            this.serviceMap.put(key, s);
            for (String alias : s.getAliases()) {
                this.serviceMap.put(new ServiceKey(type, alias, true, null), s);
            }
            putPropertyStrings(s);
        }
    }

    private void putPropertyStrings(Service s) {
        String type = s.getType();
        String algorithm = s.getAlgorithm();
        super.put(type + "." + algorithm, s.getClassName());
        for (String alias : s.getAliases()) {
            super.put(ALIAS_PREFIX + type + "." + alias, algorithm);
        }
        for (Entry<UString, String> entry : s.attributes.entrySet()) {
            super.put(type + "." + algorithm + " " + entry.getKey(), entry.getValue());
        }
        if (this.registered) {
            Security.increaseVersion();
        }
    }

    private void removePropertyStrings(Service s) {
        String type = s.getType();
        String algorithm = s.getAlgorithm();
        super.remove(type + "." + algorithm);
        for (String alias : s.getAliases()) {
            super.remove(ALIAS_PREFIX + type + "." + alias);
        }
        for (Entry<UString, String> entry : s.attributes.entrySet()) {
            super.remove(type + "." + algorithm + " " + entry.getKey());
        }
        if (this.registered) {
            Security.increaseVersion();
        }
    }

    protected synchronized void removeService(Service s) {
        check("removeProviderProperty." + this.name);
        if (debug != null) {
            debug.println(this.name + ".removeService(): " + s);
        }
        if (s == null) {
            throw new NullPointerException();
        }
        implRemoveService(s);
    }

    private void implRemoveService(Service s) {
        if (s != null && this.serviceMap != null) {
            String type = s.getType();
            ServiceKey key = new ServiceKey(type, s.getAlgorithm(), false, null);
            if (s == ((Service) this.serviceMap.get(key))) {
                this.servicesChanged = true;
                this.serviceMap.remove(key);
                for (String alias : s.getAliases()) {
                    this.serviceMap.remove(new ServiceKey(type, alias, false, null));
                }
                removePropertyStrings(s);
            }
        }
    }

    private static void addEngine(String name, boolean sp, String paramName) {
        EngineDescription ed = new EngineDescription(name, sp, paramName);
        knownEngines.put(name.toLowerCase(Locale.ENGLISH), ed);
        knownEngines.put(name, ed);
    }

    private static String getEngineName(String s) {
        EngineDescription e = (EngineDescription) knownEngines.get(s);
        if (e == null) {
            e = (EngineDescription) knownEngines.get(s.toLowerCase(Locale.ENGLISH));
        }
        return e == null ? s : e.name;
    }

    public void setRegistered() {
        this.registered = true;
    }

    public void setUnregistered() {
        this.registered = false;
    }

    public boolean isRegistered() {
        return this.registered;
    }

    public synchronized void warmUpServiceProvision() {
        checkInitialized();
        ensureLegacyParsed();
        getServices();
    }
}
