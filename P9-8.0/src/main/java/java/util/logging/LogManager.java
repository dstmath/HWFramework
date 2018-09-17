package java.util.logging;

import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.WeakHashMap;
import sun.util.logging.PlatformLogger;

public class LogManager {
    static final /* synthetic */ boolean -assertionsDisabled = (LogManager.class.desiredAssertionStatus() ^ 1);
    public static final String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";
    private static final int MAX_ITERATIONS = 400;
    private static final Level defaultLevel = Level.INFO;
    private static LoggingMXBean loggingMXBean = null;
    private static final LogManager manager = ((LogManager) AccessController.doPrivileged(new PrivilegedAction<LogManager>() {
        public LogManager run() {
            LogManager mgr = null;
            try {
                String cname = System.getProperty("java.util.logging.manager");
                if (cname != null) {
                    mgr = (LogManager) LogManager.getClassInstance(cname).newInstance();
                }
            } catch (Exception ex) {
                System.err.println("Could not load Logmanager \"" + null + "\"");
                ex.printStackTrace();
            }
            if (mgr == null) {
                return new LogManager();
            }
            return mgr;
        }
    }));
    private WeakHashMap<Object, LoggerContext> contextsMap;
    private final Permission controlPermission;
    private boolean deathImminent;
    private volatile boolean initializationDone;
    private boolean initializedCalled;
    private boolean initializedGlobalHandlers;
    private final Map<Object, Integer> listenerMap;
    private final ReferenceQueue<Logger> loggerRefQueue;
    private volatile Properties props;
    private volatile boolean readPrimordialConfiguration;
    private volatile Logger rootLogger;
    private final LoggerContext systemContext;
    private final LoggerContext userContext;

    private static class Beans {
        private static final Class<?> propertyChangeEventClass = getClass("java.beans.PropertyChangeEvent");
        private static final Class<?> propertyChangeListenerClass = getClass("java.beans.PropertyChangeListener");
        private static final Method propertyChangeMethod = getMethod(propertyChangeListenerClass, "propertyChange", propertyChangeEventClass);
        private static final Constructor<?> propertyEventCtor = getConstructor(propertyChangeEventClass, Object.class, String.class, Object.class, Object.class);

        private Beans() {
        }

        private static Class<?> getClass(String name) {
            try {
                return Class.forName(name, true, Beans.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private static Constructor<?> getConstructor(Class<?> c, Class<?>... types) {
            if (c == null) {
                return null;
            }
            try {
                return c.getDeclaredConstructor(types);
            } catch (Object x) {
                throw new AssertionError(x);
            }
        }

        private static Method getMethod(Class<?> c, String name, Class<?>... types) {
            if (c == null) {
                return null;
            }
            try {
                return c.getMethod(name, types);
            } catch (Object e) {
                throw new AssertionError(e);
            }
        }

        static boolean isBeansPresent() {
            if (propertyChangeListenerClass == null || propertyChangeEventClass == null) {
                return LogManager.-assertionsDisabled;
            }
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:14:0x002f A:{Splitter: B:0:0x0000, ExcHandler: java.lang.InstantiationException (r1_0 'x' java.lang.Object)} */
        /* JADX WARNING: Missing block: B:14:0x002f, code:
            r1 = move-exception;
     */
        /* JADX WARNING: Missing block: B:16:0x0035, code:
            throw new java.lang.AssertionError(r1);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static Object newPropertyChangeEvent(Object source, String prop, Object oldValue, Object newValue) {
            try {
                return propertyEventCtor.newInstance(source, prop, oldValue, newValue);
            } catch (Object x) {
            } catch (Object x2) {
                Throwable cause = x2.getCause();
                if (cause instanceof Error) {
                    throw ((Error) cause);
                } else if (cause instanceof RuntimeException) {
                    throw ((RuntimeException) cause);
                } else {
                    throw new AssertionError(x2);
                }
            }
        }

        static void invokePropertyChange(Object listener, Object ev) {
            try {
                propertyChangeMethod.invoke(listener, ev);
            } catch (Object x) {
                throw new AssertionError(x);
            } catch (Object x2) {
                Throwable cause = x2.getCause();
                if (cause instanceof Error) {
                    throw ((Error) cause);
                } else if (cause instanceof RuntimeException) {
                    throw ((RuntimeException) cause);
                } else {
                    throw new AssertionError(x2);
                }
            }
        }
    }

    private class Cleaner extends Thread {
        /* synthetic */ Cleaner(LogManager this$0, Cleaner -this1) {
            this();
        }

        private Cleaner() {
            setContextClassLoader(null);
        }

        public void run() {
            LogManager mgr = LogManager.manager;
            synchronized (LogManager.this) {
                LogManager.this.deathImminent = true;
                LogManager.this.initializedGlobalHandlers = true;
            }
            LogManager.this.reset();
        }
    }

    private static class LogNode {
        HashMap<String, LogNode> children;
        final LoggerContext context;
        LoggerWeakRef loggerRef;
        LogNode parent;

        LogNode(LogNode parent, LoggerContext context) {
            this.parent = parent;
            this.context = context;
        }

        void walkAndSetParent(Logger parent) {
            if (this.children != null) {
                for (LogNode node : this.children.values()) {
                    LoggerWeakRef ref = node.loggerRef;
                    Logger logger = ref == null ? null : (Logger) ref.get();
                    if (logger == null) {
                        node.walkAndSetParent(parent);
                    } else {
                        LogManager.doSetParent(logger, parent);
                    }
                }
            }
        }
    }

    class LoggerContext {
        static final /* synthetic */ boolean -assertionsDisabled = (LoggerContext.class.desiredAssertionStatus() ^ 1);
        final /* synthetic */ boolean $assertionsDisabled;
        private final Hashtable<String, LoggerWeakRef> namedLoggers;
        private final LogNode root;

        /* synthetic */ LoggerContext(LogManager this$0, LoggerContext -this1) {
            this();
        }

        private LoggerContext() {
            this.namedLoggers = new Hashtable();
            this.root = new LogNode(null, this);
        }

        final boolean requiresDefaultLoggers() {
            boolean requiresDefaultLoggers = getOwner() == LogManager.manager ? true : LogManager.-assertionsDisabled;
            if (requiresDefaultLoggers) {
                getOwner().ensureLogManagerInitialized();
            }
            return requiresDefaultLoggers;
        }

        final LogManager getOwner() {
            return LogManager.this;
        }

        final Logger getRootLogger() {
            return getOwner().rootLogger;
        }

        final Logger getGlobalLogger() {
            return Logger.global;
        }

        Logger demandLogger(String name, String resourceBundleName) {
            return getOwner().demandLogger(name, resourceBundleName, null);
        }

        private void ensureInitialized() {
            if (requiresDefaultLoggers()) {
                ensureDefaultLogger(getRootLogger());
                ensureDefaultLogger(getGlobalLogger());
            }
        }

        /* JADX WARNING: Missing block: B:12:0x001d, code:
            return r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        synchronized Logger findLogger(String name) {
            ensureInitialized();
            LoggerWeakRef ref = (LoggerWeakRef) this.namedLoggers.get(name);
            if (ref == null) {
                return null;
            }
            Logger logger = (Logger) ref.get();
            if (logger == null) {
                ref.dispose();
            }
        }

        private void ensureAllDefaultLoggers(Logger logger) {
            if (requiresDefaultLoggers()) {
                String name = logger.getName();
                if (!name.isEmpty()) {
                    ensureDefaultLogger(getRootLogger());
                    if (!Logger.GLOBAL_LOGGER_NAME.equals(name)) {
                        ensureDefaultLogger(getGlobalLogger());
                    }
                }
            }
        }

        private void ensureDefaultLogger(Logger logger) {
            if (requiresDefaultLoggers() && logger != null && (logger == Logger.global || logger == LogManager.this.rootLogger)) {
                if (!this.namedLoggers.containsKey(logger.getName())) {
                    addLocalLogger(logger, (boolean) LogManager.-assertionsDisabled);
                }
            } else if (!-assertionsDisabled && logger != null) {
                throw new AssertionError();
            }
        }

        boolean addLocalLogger(Logger logger) {
            return addLocalLogger(logger, requiresDefaultLoggers());
        }

        boolean addLocalLogger(Logger logger, LogManager manager) {
            return addLocalLogger(logger, requiresDefaultLoggers(), manager);
        }

        boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded) {
            return addLocalLogger(logger, addDefaultLoggersIfNeeded, LogManager.manager);
        }

        synchronized boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded, LogManager manager) {
            if (addDefaultLoggersIfNeeded) {
                ensureAllDefaultLoggers(logger);
            }
            String name = logger.getName();
            if (name == null) {
                throw new NullPointerException();
            }
            LoggerWeakRef ref = (LoggerWeakRef) this.namedLoggers.get(name);
            if (ref != null) {
                if (ref.get() != null) {
                    return LogManager.-assertionsDisabled;
                }
                ref.dispose();
            }
            LogManager owner = getOwner();
            logger.setLogManager(owner);
            owner.getClass();
            ref = new LoggerWeakRef(logger);
            this.namedLoggers.put(name, ref);
            Level level = owner.getLevelProperty(name + ".level", null);
            if (!(level == null || (logger.isLevelInitialized() ^ 1) == 0)) {
                LogManager.doSetLevel(logger, level);
            }
            processParentHandlers(logger, name);
            LogNode node = getNode(name);
            node.loggerRef = ref;
            Logger parent = null;
            for (LogNode nodep = node.parent; nodep != null; nodep = nodep.parent) {
                LoggerWeakRef nodeRef = nodep.loggerRef;
                if (nodeRef != null) {
                    parent = (Logger) nodeRef.get();
                    if (parent != null) {
                        break;
                    }
                }
            }
            if (parent != null) {
                LogManager.doSetParent(logger, parent);
            }
            node.walkAndSetParent(logger);
            ref.setNode(node);
            return true;
        }

        synchronized void removeLoggerRef(String name, LoggerWeakRef ref) {
            this.namedLoggers.remove(name, ref);
        }

        synchronized Enumeration<String> getLoggerNames() {
            ensureInitialized();
            return this.namedLoggers.keys();
        }

        private void processParentHandlers(final Logger logger, final String name) {
            final LogManager owner = getOwner();
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    if (!(logger == owner.rootLogger || owner.getBooleanProperty(name + ".useParentHandlers", true))) {
                        logger.setUseParentHandlers(LogManager.-assertionsDisabled);
                    }
                    return null;
                }
            });
            int ix = 1;
            while (true) {
                int ix2 = name.indexOf(".", ix);
                if (ix2 >= 0) {
                    String pname = name.substring(0, ix2);
                    if (owner.getProperty(pname + ".level") != null || owner.getProperty(pname + ".handlers") != null) {
                        demandLogger(pname, null);
                    }
                    ix = ix2 + 1;
                } else {
                    return;
                }
            }
        }

        LogNode getNode(String name) {
            if (name == null || name.equals("")) {
                return this.root;
            }
            LogNode node = this.root;
            while (name.length() > 0) {
                String head;
                int ix = name.indexOf(".");
                if (ix > 0) {
                    head = name.substring(0, ix);
                    name = name.substring(ix + 1);
                } else {
                    head = name;
                    name = "";
                }
                if (node.children == null) {
                    node.children = new HashMap();
                }
                LogNode child = (LogNode) node.children.get(head);
                if (child == null) {
                    child = new LogNode(node, this);
                    node.children.put(head, child);
                }
                node = child;
            }
            return node;
        }
    }

    final class LoggerWeakRef extends WeakReference<Logger> {
        private boolean disposed = LogManager.-assertionsDisabled;
        private String name;
        private LogNode node;
        private WeakReference<Logger> parentRef;

        LoggerWeakRef(Logger logger) {
            super(logger, LogManager.this.loggerRefQueue);
            this.name = logger.getName();
        }

        /* JADX WARNING: Missing block: B:11:0x000c, code:
            r0 = r6.node;
     */
        /* JADX WARNING: Missing block: B:12:0x000e, code:
            if (r0 == null) goto L_0x0028;
     */
        /* JADX WARNING: Missing block: B:13:0x0010, code:
            r3 = r0.context;
     */
        /* JADX WARNING: Missing block: B:14:0x0012, code:
            monitor-enter(r3);
     */
        /* JADX WARNING: Missing block: B:16:?, code:
            r0.context.removeLoggerRef(r6.name, r6);
            r6.name = null;
     */
        /* JADX WARNING: Missing block: B:17:0x001f, code:
            if (r0.loggerRef != r6) goto L_0x0024;
     */
        /* JADX WARNING: Missing block: B:18:0x0021, code:
            r0.loggerRef = null;
     */
        /* JADX WARNING: Missing block: B:19:0x0024, code:
            r6.node = null;
     */
        /* JADX WARNING: Missing block: B:20:0x0027, code:
            monitor-exit(r3);
     */
        /* JADX WARNING: Missing block: B:22:0x002a, code:
            if (r6.parentRef == null) goto L_0x003b;
     */
        /* JADX WARNING: Missing block: B:23:0x002c, code:
            r1 = (java.util.logging.Logger) r6.parentRef.get();
     */
        /* JADX WARNING: Missing block: B:24:0x0034, code:
            if (r1 == null) goto L_0x0039;
     */
        /* JADX WARNING: Missing block: B:25:0x0036, code:
            r1.removeChildLogger(r6);
     */
        /* JADX WARNING: Missing block: B:26:0x0039, code:
            r6.parentRef = null;
     */
        /* JADX WARNING: Missing block: B:27:0x003b, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void dispose() {
            synchronized (this) {
                if (this.disposed) {
                    return;
                }
                this.disposed = true;
            }
        }

        void setNode(LogNode node) {
            this.node = node;
        }

        void setParentRef(WeakReference<Logger> parentRef) {
            this.parentRef = parentRef;
        }
    }

    private final class RootLogger extends Logger {
        /* synthetic */ RootLogger(LogManager this$0, RootLogger -this1) {
            this();
        }

        private RootLogger() {
            super("", null, null, LogManager.this, true);
        }

        public void log(LogRecord record) {
            LogManager.this.initializeGlobalHandlers();
            super.log(record);
        }

        public void addHandler(Handler h) {
            LogManager.this.initializeGlobalHandlers();
            super.addHandler(h);
        }

        public void removeHandler(Handler h) {
            LogManager.this.initializeGlobalHandlers();
            super.removeHandler(h);
        }

        Handler[] accessCheckedHandlers() {
            LogManager.this.initializeGlobalHandlers();
            return super.accessCheckedHandlers();
        }
    }

    final class SystemLoggerContext extends LoggerContext {
        SystemLoggerContext() {
            super(LogManager.this, null);
        }

        Logger demandLogger(String name, String resourceBundleName) {
            Logger result = findLogger(name);
            if (result == null) {
                Logger newLogger = new Logger(name, resourceBundleName, null, getOwner(), true);
                do {
                    if (addLocalLogger(newLogger)) {
                        result = newLogger;
                        continue;
                    } else {
                        result = findLogger(name);
                        continue;
                    }
                } while (result == null);
            }
            return result;
        }
    }

    protected LogManager() {
        this(checkSubclassPermissions());
    }

    private LogManager(Void checked) {
        this.props = new Properties();
        this.listenerMap = new HashMap();
        this.systemContext = new SystemLoggerContext();
        this.userContext = new LoggerContext(this, null);
        this.initializedGlobalHandlers = true;
        this.initializedCalled = -assertionsDisabled;
        this.initializationDone = -assertionsDisabled;
        this.contextsMap = null;
        this.loggerRefQueue = new ReferenceQueue();
        this.controlPermission = new LoggingPermission("control", null);
        try {
            Runtime.getRuntime().addShutdownHook(new Cleaner(this, null));
        } catch (IllegalStateException e) {
        }
    }

    private static Void checkSubclassPermissions() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:27:0x0031, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void ensureLogManagerInitialized() {
        if (!this.initializationDone && this == manager) {
            synchronized (this) {
                boolean isRecursiveInitialization = this.initializedCalled ? true : -assertionsDisabled;
                if (!-assertionsDisabled && !this.initializedCalled && this.initializationDone) {
                    throw new AssertionError((Object) "Initialization can't be done if initialized has not been called!");
                } else if (!isRecursiveInitialization) {
                    if (!this.initializationDone) {
                        this.initializedCalled = true;
                        try {
                            PrivilegedAction anonymousClass2 = new PrivilegedAction<Object>() {
                                static final /* synthetic */ boolean -assertionsDisabled = (AnonymousClass2.class.desiredAssertionStatus() ^ 1);

                                public Object run() {
                                    if (!-assertionsDisabled && LogManager.this.rootLogger != null) {
                                        throw new AssertionError();
                                    } else if (-assertionsDisabled || (LogManager.this.initializedCalled && !LogManager.this.initializationDone)) {
                                        this.readPrimordialConfiguration();
                                        LogManager logManager = this;
                                        LogManager logManager2 = this;
                                        logManager2.getClass();
                                        logManager.rootLogger = new RootLogger(logManager2, null);
                                        this.addLogger(this.rootLogger);
                                        if (!this.rootLogger.isLevelInitialized()) {
                                            this.rootLogger.setLevel(LogManager.defaultLevel);
                                        }
                                        this.addLogger(Logger.global);
                                        return null;
                                    } else {
                                        throw new AssertionError();
                                    }
                                }
                            };
                            AccessController.doPrivileged(anonymousClass2);
                            this.initializationDone = anonymousClass2;
                        } finally {
                            this.initializationDone = true;
                        }
                    }
                }
            }
        }
    }

    public static LogManager getLogManager() {
        if (manager != null) {
            manager.ensureLogManagerInitialized();
        }
        return manager;
    }

    private void readPrimordialConfiguration() {
        if (!this.readPrimordialConfiguration) {
            synchronized (this) {
                if (!this.readPrimordialConfiguration) {
                    if (System.out == null) {
                        return;
                    }
                    this.readPrimordialConfiguration = true;
                    try {
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                            public Void run() throws Exception {
                                LogManager.this.readConfiguration();
                                PlatformLogger.redirectPlatformLoggers();
                                return null;
                            }
                        });
                    } catch (Object ex) {
                        if (!-assertionsDisabled) {
                            throw new AssertionError("Exception raised while reading logging configuration: " + ex);
                        }
                    }
                }
            }
        }
    }

    @Deprecated
    public void addPropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        PropertyChangeListener listener = (PropertyChangeListener) Objects.requireNonNull(l);
        checkPermission();
        synchronized (this.listenerMap) {
            Integer value = (Integer) this.listenerMap.get(listener);
            this.listenerMap.put(listener, Integer.valueOf(value == null ? 1 : value.intValue() + 1));
        }
    }

    @Deprecated
    public void removePropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        checkPermission();
        if (l != null) {
            PropertyChangeListener listener = l;
            synchronized (this.listenerMap) {
                Integer value = (Integer) this.listenerMap.get(l);
                if (value != null) {
                    int i = value.intValue();
                    if (i == 1) {
                        this.listenerMap.remove(l);
                    } else if (-assertionsDisabled || i > 1) {
                        this.listenerMap.put(l, Integer.valueOf(i - 1));
                    } else {
                        throw new AssertionError();
                    }
                }
            }
        }
    }

    private LoggerContext getUserContext() {
        return this.userContext;
    }

    final LoggerContext getSystemContext() {
        return this.systemContext;
    }

    private List<LoggerContext> contexts() {
        List<LoggerContext> cxs = new ArrayList();
        cxs.add(getSystemContext());
        cxs.add(getUserContext());
        return cxs;
    }

    Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        Logger result = getLogger(name);
        if (result == null) {
            Logger newLogger = new Logger(name, resourceBundleName, caller, this, -assertionsDisabled);
            while (!addLogger(newLogger)) {
                result = getLogger(name);
                if (result != null) {
                }
            }
            return newLogger;
        }
        return result;
    }

    Logger demandSystemLogger(String name, String resourceBundleName) {
        Logger logger;
        final Logger sysLogger = getSystemContext().demandLogger(name, resourceBundleName);
        do {
            if (addLogger(sysLogger)) {
                logger = sysLogger;
                continue;
            } else {
                logger = getLogger(name);
                continue;
            }
        } while (logger == null);
        if (logger != sysLogger && sysLogger.accessCheckedHandlers().length == 0) {
            final Logger l = logger;
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    for (Handler hdl : l.accessCheckedHandlers()) {
                        sysLogger.addHandler(hdl);
                    }
                    return null;
                }
            });
        }
        return sysLogger;
    }

    private static Class getClassInstance(String cname) {
        if (cname == null) {
            return null;
        }
        try {
            return ClassLoader.getSystemClassLoader().loadClass(cname);
        } catch (ClassNotFoundException e) {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(cname);
            } catch (ClassNotFoundException e2) {
                return null;
            }
        }
    }

    private void loadLoggerHandlers(final Logger logger, String name, final String handlersPropertyName) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                String[] names = LogManager.this.parseClassNames(handlersPropertyName);
                for (String word : names) {
                    try {
                        Handler hdl = (Handler) LogManager.getClassInstance(word).newInstance();
                        String levs = LogManager.this.getProperty(word + ".level");
                        if (levs != null) {
                            Level l = Level.findLevel(levs);
                            if (l != null) {
                                hdl.setLevel(l);
                            } else {
                                System.err.println("Can't set level for " + word);
                            }
                        }
                        logger.addHandler(hdl);
                    } catch (Object ex) {
                        System.err.println("Can't load log handler \"" + word + "\"");
                        System.err.println("" + ex);
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        });
    }

    final void drainLoggerRefQueueBounded() {
        int i = 0;
        while (i < 400 && this.loggerRefQueue != null) {
            LoggerWeakRef ref = (LoggerWeakRef) this.loggerRefQueue.poll();
            if (ref != null) {
                ref.dispose();
                i++;
            } else {
                return;
            }
        }
    }

    public boolean addLogger(Logger logger) {
        String name = logger.getName();
        if (name == null) {
            throw new NullPointerException();
        }
        drainLoggerRefQueueBounded();
        if (!getUserContext().addLocalLogger(logger, this)) {
            return -assertionsDisabled;
        }
        loadLoggerHandlers(logger, name, name + ".handlers");
        return true;
    }

    private static void doSetLevel(final Logger logger, final Level level) {
        if (System.getSecurityManager() == null) {
            logger.setLevel(level);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    logger.setLevel(level);
                    return null;
                }
            });
        }
    }

    private static void doSetParent(final Logger logger, final Logger parent) {
        if (System.getSecurityManager() == null) {
            logger.setParent(parent);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    logger.setParent(parent);
                    return null;
                }
            });
        }
    }

    public Logger getLogger(String name) {
        return getUserContext().findLogger(name);
    }

    public Enumeration<String> getLoggerNames() {
        return getUserContext().getLoggerNames();
    }

    public void readConfiguration() throws IOException, SecurityException {
        InputStream in;
        checkPermission();
        String cname = System.getProperty("java.util.logging.config.class");
        if (cname != null) {
            try {
                getClassInstance(cname).newInstance();
                return;
            } catch (Object ex) {
                System.err.println("Logging configuration class \"" + cname + "\" failed");
                System.err.println("" + ex);
            }
        }
        String fname = System.getProperty("java.util.logging.config.file");
        if (fname == null) {
            fname = System.getProperty("java.home");
            if (fname == null) {
                throw new Error("Can't find java.home ??");
            }
            fname = new File(new File(fname, "lib"), "logging.properties").getCanonicalPath();
        }
        try {
            in = new FileInputStream(fname);
        } catch (Exception e) {
            in = LogManager.class.getResourceAsStream("logging.properties");
            if (in == null) {
                throw e;
            }
        }
        try {
            readConfiguration(new BufferedInputStream(in));
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public void reset() throws SecurityException {
        checkPermission();
        synchronized (this) {
            this.props = new Properties();
            this.initializedGlobalHandlers = true;
        }
        for (LoggerContext cx : contexts()) {
            Enumeration<String> enum_ = cx.getLoggerNames();
            while (enum_.hasMoreElements()) {
                Logger logger = cx.findLogger((String) enum_.nextElement());
                if (logger != null) {
                    resetLogger(logger);
                }
            }
        }
    }

    private void resetLogger(Logger logger) {
        Handler[] targets = logger.getHandlers();
        for (Handler h : targets) {
            logger.removeHandler(h);
            try {
                h.close();
            } catch (Exception e) {
            }
        }
        String name = logger.getName();
        if (name == null || !name.equals("")) {
            logger.setLevel(null);
        } else {
            logger.setLevel(defaultLevel);
        }
    }

    private String[] parseClassNames(String propertyName) {
        String hands = getProperty(propertyName);
        if (hands == null) {
            return new String[0];
        }
        hands = hands.trim();
        int ix = 0;
        List<String> result = new ArrayList();
        while (ix < hands.length()) {
            int end = ix;
            while (end < hands.length() && !Character.isWhitespace(hands.charAt(end)) && hands.charAt(end) != ',') {
                end++;
            }
            String word = hands.substring(ix, end);
            ix = end + 1;
            word = word.trim();
            if (word.length() != 0) {
                result.add(word);
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public void readConfiguration(InputStream ins) throws IOException, SecurityException {
        int i;
        checkPermission();
        reset();
        this.props.load(ins);
        String[] names = parseClassNames("config");
        for (String word : names) {
            try {
                getClassInstance(word).newInstance();
            } catch (Object ex) {
                System.err.println("Can't load config class \"" + word + "\"");
                System.err.println("" + ex);
            }
        }
        setLevelsOnExistingLoggers();
        Map map = null;
        synchronized (this.listenerMap) {
            if (!this.listenerMap.isEmpty()) {
                map = new HashMap(this.listenerMap);
            }
        }
        if (map != null) {
            if (-assertionsDisabled || Beans.isBeansPresent()) {
                Object ev = Beans.newPropertyChangeEvent(LogManager.class, null, null, null);
                for (Entry<Object, Integer> entry : map.entrySet()) {
                    Object listener = entry.getKey();
                    int count = ((Integer) entry.getValue()).intValue();
                    for (i = 0; i < count; i++) {
                        Beans.invokePropertyChange(listener, ev);
                    }
                }
            } else {
                throw new AssertionError();
            }
        }
        synchronized (this) {
            this.initializedGlobalHandlers = -assertionsDisabled;
        }
    }

    public String getProperty(String name) {
        return this.props.getProperty(name);
    }

    String getStringProperty(String name, String defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        return val.trim();
    }

    int getIntProperty(String name, int defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        val = val.toLowerCase();
        if (val.equals("true") || val.equals("1")) {
            return true;
        }
        if (val.equals("false") || val.equals("0")) {
            return -assertionsDisabled;
        }
        return defaultValue;
    }

    Level getLevelProperty(String name, Level defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.findLevel(val.trim());
        if (l == null) {
            l = defaultValue;
        }
        return l;
    }

    Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getProperty(name);
        if (val != null) {
            try {
                return (Filter) getClassInstance(val).newInstance();
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    Formatter getFormatterProperty(String name, Formatter defaultValue) {
        String val = getProperty(name);
        if (val != null) {
            try {
                return (Formatter) getClassInstance(val).newInstance();
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    private synchronized void initializeGlobalHandlers() {
        if (!this.initializedGlobalHandlers) {
            this.initializedGlobalHandlers = true;
            if (!this.deathImminent) {
                loadLoggerHandlers(this.rootLogger, null, "handlers");
            }
        }
    }

    void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(this.controlPermission);
        }
    }

    public void checkAccess() throws SecurityException {
        checkPermission();
    }

    private synchronized void setLevelsOnExistingLoggers() {
        Enumeration<?> enum_ = this.props.propertyNames();
        while (enum_.hasMoreElements()) {
            String key = (String) enum_.nextElement();
            if (key.endsWith(".level")) {
                String name = key.substring(0, key.length() - 6);
                Level level = getLevelProperty(key, null);
                if (level == null) {
                    System.err.println("Bad level value for property: " + key);
                } else {
                    for (LoggerContext cx : contexts()) {
                        Logger l = cx.findLogger(name);
                        if (l != null) {
                            l.setLevel(level);
                        }
                    }
                }
            }
        }
    }

    public static synchronized LoggingMXBean getLoggingMXBean() {
        LoggingMXBean loggingMXBean;
        synchronized (LogManager.class) {
            if (loggingMXBean == null) {
                loggingMXBean = new Logging();
            }
            loggingMXBean = loggingMXBean;
        }
        return loggingMXBean;
    }
}
