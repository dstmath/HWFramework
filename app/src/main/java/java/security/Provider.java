package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import sun.security.util.Debug;

public abstract class Provider extends Properties {
    private static final int ALIAS_LENGTH = 0;
    private static final String ALIAS_PREFIX = "Alg.Alias.";
    private static final String ALIAS_PREFIX_LOWER = "alg.alias.";
    private static final Debug debug = null;
    private static final Map<String, EngineDescription> knownEngines = null;
    private static volatile ServiceKey previousKey = null;
    static final long serialVersionUID = -4298000515446427739L;
    private transient Set<Entry<Object, Object>> entrySet;
    private transient int entrySetCallCount;
    private String info;
    private transient boolean initialized;
    private transient boolean legacyChanged;
    private transient Map<ServiceKey, Service> legacyMap;
    private transient Map<String, String> legacyStrings;
    private String name;
    private volatile boolean registered;
    private transient Map<ServiceKey, Service> serviceMap;
    private transient Set<Service> serviceSet;
    private transient boolean servicesChanged;
    private double version;

    private static class EngineDescription {
        private volatile Class constructorParameterClass;
        final String constructorParameterClassName;
        final String name;
        final boolean supportsParameter;

        EngineDescription(String name, boolean sp, String paramName) {
            this.name = name;
            this.supportsParameter = sp;
            this.constructorParameterClassName = paramName;
        }

        Class getConstructorParameterClass() throws ClassNotFoundException {
            Class clazz = this.constructorParameterClass;
            if (clazz != null) {
                return clazz;
            }
            clazz = Class.forName(this.constructorParameterClassName);
            this.constructorParameterClass = clazz;
            return clazz;
        }
    }

    public static class Service {
        private static final Class[] CLASS0 = null;
        private String algorithm;
        private List<String> aliases;
        private Map<UString, String> attributes;
        private String className;
        private volatile Reference<Class> classRef;
        private volatile Boolean hasKeyAttributes;
        private final Provider provider;
        private boolean registered;
        private Class[] supportedClasses;
        private String[] supportedFormats;
        private String type;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.Provider.Service.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.Provider.Service.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.security.Provider.Service.<clinit>():void");
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
            this.type = type;
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
                    Class paramClass = cap.getConstructorParameterClass();
                    if (constructorParameter == null || paramClass.isAssignableFrom(constructorParameter.getClass())) {
                        return getImplClass().getConstructor(paramClass).newInstance(constructorParameter);
                    }
                    throw new InvalidParameterException("constructorParameter must be instanceof " + cap.constructorParameterClassName.replace('$', '.') + " for engine type " + this.type);
                } else if (constructorParameter == null) {
                    return getImplClass().newInstance();
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

        private Class getImplClass() throws NoSuchAlgorithmException {
            try {
                Reference<Class> ref = this.classRef;
                Class clazz = ref == null ? null : (Class) ref.get();
                if (clazz == null) {
                    ClassLoader cl = this.provider.getClass().getClassLoader();
                    if (cl == null) {
                        clazz = Class.forName(this.className);
                    } else {
                        clazz = cl.loadClass(this.className);
                    }
                    this.classRef = new WeakReference(clazz);
                }
                return clazz;
            } catch (ClassNotFoundException e) {
                throw new NoSuchAlgorithmException("class configured for " + this.type + "(provider: " + this.provider.getName() + ")" + "cannot be found.", e);
            }
        }

        private Object newInstanceGeneric(Object constructorParameter) throws Exception {
            Class clazz = getImplClass();
            if (constructorParameter == null) {
                return clazz.newInstance();
            }
            Class argClass = constructorParameter.getClass();
            Constructor[] cons = clazz.getConstructors();
            for (int i = Provider.ALIAS_LENGTH; i < cons.length; i++) {
                Constructor con = cons[i];
                Class[] paramTypes = con.getParameterTypes();
                if (paramTypes.length == 1 && paramTypes[Provider.ALIAS_LENGTH].isAssignableFrom(argClass)) {
                    return con.newInstance(constructorParameter);
                }
            }
            throw new NoSuchAlgorithmException("No constructor matching " + argClass.getName() + " found in class " + this.className);
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
            boolean bool = true;
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
                        List<Class> classList = new ArrayList(classNames.length);
                        int length = classNames.length;
                        for (int i = Provider.ALIAS_LENGTH; i < length; i++) {
                            Class clazz = getKeyClass(classNames[i]);
                            if (clazz != null) {
                                classList.add(clazz);
                            }
                        }
                        this.supportedClasses = (Class[]) classList.toArray(CLASS0);
                    }
                    if (this.supportedFormats == null && this.supportedClasses == null) {
                        bool = false;
                    }
                    b = Boolean.valueOf(bool);
                    this.hasKeyAttributes = b;
                }
            }
            return b.booleanValue();
        }

        private Class getKeyClass(String name) {
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
            String[] strArr = this.supportedFormats;
            int length = strArr.length;
            for (int i = Provider.ALIAS_LENGTH; i < length; i++) {
                if (strArr[i].equals(format)) {
                    return true;
                }
            }
            return false;
        }

        private boolean supportsKeyClass(Key key) {
            if (this.supportedClasses == null) {
                return false;
            }
            Class keyClass = key.getClass();
            Class[] clsArr = this.supportedClasses;
            int length = clsArr.length;
            for (int i = Provider.ALIAS_LENGTH; i < length; i++) {
                if (clsArr[i].isAssignableFrom(keyClass)) {
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

        /* synthetic */ ServiceKey(String type, String algorithm, boolean intern, ServiceKey serviceKey) {
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.Provider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.Provider.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.security.Provider.<clinit>():void");
    }

    protected Provider(String name, double version, String info) {
        this.registered = false;
        this.entrySet = null;
        this.entrySetCallCount = ALIAS_LENGTH;
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

    public synchronized Object remove(Object key) {
        check("removeProviderProperty." + this.name);
        if (debug != null) {
            debug.println("Remove " + this.name + " provider property " + key);
        }
        return implRemove(key);
    }

    public Object get(Object key) {
        checkInitialized();
        return super.get(key);
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

    private void implPutAll(Map t) {
        for (Entry e : t.entrySet()) {
            implPut(e.getKey(), e.getValue());
        }
        if (this.registered) {
            Security.increaseVersion();
        }
    }

    private Object implRemove(Object key) {
        if (this.registered) {
            Security.increaseVersion();
        }
        if (key instanceof String) {
            String keyString = (String) key;
            if (keyString.startsWith("Provider.")) {
                return null;
            }
            this.legacyChanged = true;
            if (this.legacyStrings == null) {
                this.legacyStrings = new LinkedHashMap();
            }
            this.legacyStrings.remove(keyString);
        }
        return super.remove(key);
    }

    private Object implPut(Object key, Object value) {
        if ((key instanceof String) && (value instanceof String)) {
            String keyString = (String) key;
            if (keyString.startsWith("Provider.")) {
                return null;
            }
            if (this.registered) {
                Security.increaseVersion();
            }
            this.legacyChanged = true;
            if (this.legacyStrings == null) {
                this.legacyStrings = new LinkedHashMap();
            }
            this.legacyStrings.put(keyString, (String) value);
        }
        return super.put(key, value);
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
        Iterator t = map.entrySet().iterator();
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
        String type = key.substring(ALIAS_LENGTH, i);
        String alg = key.substring(i + 1);
        return new String[]{type, alg};
    }

    private void parseLegacyPut(String name, String value) {
        String[] typeAndAlg;
        String type;
        ServiceKey key;
        Service s;
        if (name.toLowerCase(Locale.ENGLISH).startsWith(ALIAS_PREFIX_LOWER)) {
            String stdAlg = value;
            typeAndAlg = getTypeAndAlgorithm(name.substring(ALIAS_LENGTH));
            if (typeAndAlg != null) {
                type = typeAndAlg[ALIAS_LENGTH];
                String aliasAlg = typeAndAlg[1].intern();
                key = new ServiceKey(type, value, true, null);
                s = (Service) this.legacyMap.get(key);
                if (s == null) {
                    Provider provider = this;
                    s = new Service();
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
                type = typeAndAlg[ALIAS_LENGTH];
                stdAlg = typeAndAlg[1].intern();
                String className = value;
                key = new ServiceKey(type, stdAlg, true, null);
                s = (Service) this.legacyMap.get(key);
                if (s == null) {
                    provider = this;
                    s = new Service();
                    s.type = type;
                    s.algorithm = stdAlg;
                    this.legacyMap.put(key, s);
                }
                s.className = value;
            } else {
                String attributeValue = value;
                type = typeAndAlg[ALIAS_LENGTH];
                String attributeString = typeAndAlg[1];
                stdAlg = attributeString.substring(ALIAS_LENGTH, i).intern();
                String attributeName = attributeString.substring(i + 1);
                while (attributeName.startsWith(" ")) {
                    attributeName = attributeName.substring(1);
                }
                attributeName = attributeName.intern();
                key = new ServiceKey(type, stdAlg, true, null);
                s = (Service) this.legacyMap.get(key);
                if (s == null) {
                    provider = this;
                    s = new Service();
                    s.type = type;
                    s.algorithm = stdAlg;
                    this.legacyMap.put(key, s);
                }
                s.addAttribute(attributeName, value);
            }
        }
    }

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
            return service;
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
        knownEngines.put(name, new EngineDescription(name, sp, paramName));
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
