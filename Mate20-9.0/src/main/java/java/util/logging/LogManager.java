package java.util.logging;

import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import java.util.Objects;
import java.util.Properties;
import java.util.WeakHashMap;
import sun.util.logging.PlatformLogger;

public class LogManager {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";
    private static final int MAX_ITERATIONS = 400;
    /* access modifiers changed from: private */
    public static final Level defaultLevel = Level.INFO;
    private static LoggingMXBean loggingMXBean = null;
    /* access modifiers changed from: private */
    public static final LogManager manager = ((LogManager) AccessController.doPrivileged(new PrivilegedAction<LogManager>() {
        public LogManager run() {
            LogManager mgr = null;
            try {
                String cname = System.getProperty("java.util.logging.manager");
                if (cname != null) {
                    mgr = (LogManager) LogManager.getClassInstance(cname).newInstance();
                }
            } catch (Exception ex) {
                PrintStream printStream = System.err;
                printStream.println("Could not load Logmanager \"" + null + "\"");
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
    /* access modifiers changed from: private */
    public boolean deathImminent;
    /* access modifiers changed from: private */
    public volatile boolean initializationDone;
    /* access modifiers changed from: private */
    public boolean initializedCalled;
    /* access modifiers changed from: private */
    public boolean initializedGlobalHandlers;
    private final Map<Object, Integer> listenerMap;
    /* access modifiers changed from: private */
    public final ReferenceQueue<Logger> loggerRefQueue;
    private volatile Properties props;
    private volatile boolean readPrimordialConfiguration;
    /* access modifiers changed from: private */
    public volatile Logger rootLogger;
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
            } catch (NoSuchMethodException x) {
                throw new AssertionError((Object) x);
            }
        }

        private static Method getMethod(Class<?> c, String name, Class<?>... types) {
            if (c == null) {
                return null;
            }
            try {
                return c.getMethod(name, types);
            } catch (NoSuchMethodException e) {
                throw new AssertionError((Object) e);
            }
        }

        static boolean isBeansPresent() {
            if (propertyChangeListenerClass == null || propertyChangeEventClass == null) {
                return LogManager.$assertionsDisabled;
            }
            return true;
        }

        static Object newPropertyChangeEvent(Object source, String prop, Object oldValue, Object newValue) {
            try {
                return propertyEventCtor.newInstance(source, prop, oldValue, newValue);
            } catch (IllegalAccessException | InstantiationException x) {
                throw new AssertionError((Object) x);
            } catch (InvocationTargetException x2) {
                Throwable cause = x2.getCause();
                if (cause instanceof Error) {
                    throw ((Error) cause);
                } else if (cause instanceof RuntimeException) {
                    throw ((RuntimeException) cause);
                } else {
                    throw new AssertionError((Object) x2);
                }
            }
        }

        static void invokePropertyChange(Object listener, Object ev) {
            try {
                propertyChangeMethod.invoke(listener, ev);
            } catch (IllegalAccessException x) {
                throw new AssertionError((Object) x);
            } catch (InvocationTargetException x2) {
                Throwable cause = x2.getCause();
                if (cause instanceof Error) {
                    throw ((Error) cause);
                } else if (cause instanceof RuntimeException) {
                    throw ((RuntimeException) cause);
                } else {
                    throw new AssertionError((Object) x2);
                }
            }
        }
    }

    private class Cleaner extends Thread {
        private Cleaner() {
            setContextClassLoader(null);
        }

        public void run() {
            LogManager access$200 = LogManager.manager;
            synchronized (LogManager.this) {
                boolean unused = LogManager.this.deathImminent = true;
                boolean unused2 = LogManager.this.initializedGlobalHandlers = true;
            }
            LogManager.this.reset();
        }
    }

    private static class LogNode {
        HashMap<String, LogNode> children;
        final LoggerContext context;
        LoggerWeakRef loggerRef;
        LogNode parent;

        LogNode(LogNode parent2, LoggerContext context2) {
            this.parent = parent2;
            this.context = context2;
        }

        /* access modifiers changed from: package-private */
        public void walkAndSetParent(Logger parent2) {
            if (this.children != null) {
                for (LogNode node : this.children.values()) {
                    LoggerWeakRef ref = node.loggerRef;
                    Logger logger = ref == null ? null : (Logger) ref.get();
                    if (logger == null) {
                        node.walkAndSetParent(parent2);
                    } else {
                        LogManager.doSetParent(logger, parent2);
                    }
                }
            }
        }
    }

    class LoggerContext {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final Hashtable<String, LoggerWeakRef> namedLoggers;
        private final LogNode root;

        static {
            Class<LogManager> cls = LogManager.class;
        }

        private LoggerContext() {
            this.namedLoggers = new Hashtable<>();
            this.root = new LogNode(null, this);
        }

        /* access modifiers changed from: package-private */
        public final boolean requiresDefaultLoggers() {
            boolean requiresDefaultLoggers = getOwner() == LogManager.manager ? true : LogManager.$assertionsDisabled;
            if (requiresDefaultLoggers) {
                getOwner().ensureLogManagerInitialized();
            }
            return requiresDefaultLoggers;
        }

        /* access modifiers changed from: package-private */
        public final LogManager getOwner() {
            return LogManager.this;
        }

        /* access modifiers changed from: package-private */
        public final Logger getRootLogger() {
            return getOwner().rootLogger;
        }

        /* access modifiers changed from: package-private */
        public final Logger getGlobalLogger() {
            return Logger.global;
        }

        /* access modifiers changed from: package-private */
        public Logger demandLogger(String name, String resourceBundleName) {
            return getOwner().demandLogger(name, resourceBundleName, null);
        }

        private void ensureInitialized() {
            if (requiresDefaultLoggers()) {
                ensureDefaultLogger(getRootLogger());
                ensureDefaultLogger(getGlobalLogger());
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
            return r1;
         */
        public synchronized Logger findLogger(String name) {
            ensureInitialized();
            LoggerWeakRef ref = this.namedLoggers.get(name);
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
            if (requiresDefaultLoggers() && logger != null && ((logger == Logger.global || logger == LogManager.this.rootLogger) && !this.namedLoggers.containsKey(logger.getName()))) {
                addLocalLogger(logger, (boolean) LogManager.$assertionsDisabled);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean addLocalLogger(Logger logger) {
            return addLocalLogger(logger, requiresDefaultLoggers());
        }

        /* access modifiers changed from: package-private */
        public boolean addLocalLogger(Logger logger, LogManager manager) {
            return addLocalLogger(logger, requiresDefaultLoggers(), manager);
        }

        /* access modifiers changed from: package-private */
        public boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded) {
            return addLocalLogger(logger, addDefaultLoggersIfNeeded, LogManager.manager);
        }

        /* access modifiers changed from: package-private */
        public synchronized boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded, LogManager manager) {
            if (addDefaultLoggersIfNeeded) {
                try {
                    ensureAllDefaultLoggers(logger);
                } catch (Throwable th) {
                    throw th;
                }
            }
            String name = logger.getName();
            if (name != null) {
                LoggerWeakRef ref = this.namedLoggers.get(name);
                if (ref != null) {
                    if (ref.get() != null) {
                        return LogManager.$assertionsDisabled;
                    }
                    ref.dispose();
                }
                LogManager owner = getOwner();
                logger.setLogManager(owner);
                Objects.requireNonNull(owner);
                LoggerWeakRef ref2 = new LoggerWeakRef(logger);
                this.namedLoggers.put(name, ref2);
                Level level = owner.getLevelProperty(name + ".level", null);
                if (level != null && !logger.isLevelInitialized()) {
                    LogManager.doSetLevel(logger, level);
                }
                processParentHandlers(logger, name);
                LogNode node = getNode(name);
                node.loggerRef = ref2;
                Logger parent = null;
                LogNode nodep = node.parent;
                while (true) {
                    if (nodep == null) {
                        break;
                    }
                    LoggerWeakRef nodeRef = nodep.loggerRef;
                    if (nodeRef != null) {
                        parent = (Logger) nodeRef.get();
                        if (parent != null) {
                            break;
                        }
                    }
                    nodep = nodep.parent;
                }
                if (parent != null) {
                    LogManager.doSetParent(logger, parent);
                }
                node.walkAndSetParent(logger);
                ref2.setNode(node);
                return true;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: package-private */
        public synchronized void removeLoggerRef(String name, LoggerWeakRef ref) {
            this.namedLoggers.remove(name, ref);
        }

        /* access modifiers changed from: package-private */
        public synchronized Enumeration<String> getLoggerNames() {
            ensureInitialized();
            return this.namedLoggers.keys();
        }

        private void processParentHandlers(final Logger logger, final String name) {
            final LogManager owner = getOwner();
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    if (logger != owner.rootLogger) {
                        LogManager logManager = owner;
                        if (!logManager.getBooleanProperty(name + ".useParentHandlers", true)) {
                            logger.setUseParentHandlers(LogManager.$assertionsDisabled);
                        }
                    }
                    return null;
                }
            });
            int ix = 1;
            while (true) {
                int ix2 = name.indexOf(".", ix);
                if (ix2 >= 0) {
                    String pname = name.substring(0, ix2);
                    if (owner.getProperty(pname + ".level") == null) {
                        if (owner.getProperty(pname + ".handlers") == null) {
                            ix = ix2 + 1;
                        }
                    }
                    demandLogger(pname, null);
                    ix = ix2 + 1;
                } else {
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public LogNode getNode(String name) {
            String head;
            if (name == null || name.equals("")) {
                return this.root;
            }
            LogNode node = this.root;
            while (name.length() > 0) {
                int ix = name.indexOf(".");
                if (ix > 0) {
                    head = name.substring(0, ix);
                    name = name.substring(ix + 1);
                } else {
                    head = name;
                    name = "";
                }
                if (node.children == null) {
                    node.children = new HashMap<>();
                }
                LogNode child = node.children.get(head);
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
        private boolean disposed = LogManager.$assertionsDisabled;
        private String name;
        private LogNode node;
        private WeakReference<Logger> parentRef;

        LoggerWeakRef(Logger logger) {
            super(logger, LogManager.this.loggerRefQueue);
            this.name = logger.getName();
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0010, code lost:
            r2 = r0.context;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
            monitor-enter(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            r0.context.removeLoggerRef(r5.name, r5);
            r5.name = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x001e, code lost:
            if (r0.loggerRef != r5) goto L_0x0022;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0020, code lost:
            r0.loggerRef = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0022, code lost:
            r5.node = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0024, code lost:
            monitor-exit(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x002b, code lost:
            if (r5.parentRef == null) goto L_0x003c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x002d, code lost:
            r2 = r5.parentRef.get();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0035, code lost:
            if (r2 == null) goto L_0x003a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0037, code lost:
            r2.removeChildLogger(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x003a, code lost:
            r5.parentRef = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x003c, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x000b, code lost:
            r0 = r5.node;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x000e, code lost:
            if (r0 == null) goto L_0x0029;
         */
        public void dispose() {
            synchronized (this) {
                if (!this.disposed) {
                    this.disposed = true;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setNode(LogNode node2) {
            this.node = node2;
        }

        /* access modifiers changed from: package-private */
        public void setParentRef(WeakReference<Logger> parentRef2) {
            this.parentRef = parentRef2;
        }
    }

    private final class RootLogger extends Logger {
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

        /* access modifiers changed from: package-private */
        public Handler[] accessCheckedHandlers() {
            LogManager.this.initializeGlobalHandlers();
            return super.accessCheckedHandlers();
        }
    }

    final class SystemLoggerContext extends LoggerContext {
        SystemLoggerContext() {
            super();
        }

        /* access modifiers changed from: package-private */
        public Logger demandLogger(String name, String resourceBundleName) {
            Logger result = findLogger(name);
            if (result == null) {
                Logger logger = new Logger(name, resourceBundleName, null, getOwner(), true);
                do {
                    if (addLocalLogger(logger)) {
                        result = logger;
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
        this.userContext = new LoggerContext();
        this.initializedGlobalHandlers = true;
        this.initializedCalled = $assertionsDisabled;
        this.initializationDone = $assertionsDisabled;
        this.contextsMap = null;
        this.loggerRefQueue = new ReferenceQueue<>();
        this.controlPermission = new LoggingPermission("control", null);
        try {
            Runtime.getRuntime().addShutdownHook(new Cleaner());
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x002f, code lost:
        return;
     */
    public final void ensureLogManagerInitialized() {
        if (!this.initializationDone && this == manager) {
            synchronized (this) {
                if (!(this.initializedCalled ? true : $assertionsDisabled)) {
                    if (!this.initializationDone) {
                        this.initializedCalled = true;
                        try {
                            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                                static final /* synthetic */ boolean $assertionsDisabled = false;

                                static {
                                    Class<LogManager> cls = LogManager.class;
                                }

                                public Object run() {
                                    this.readPrimordialConfiguration();
                                    LogManager logManager = this;
                                    LogManager logManager2 = this;
                                    Objects.requireNonNull(logManager2);
                                    Logger unused = logManager.rootLogger = new RootLogger();
                                    this.addLogger(this.rootLogger);
                                    if (!this.rootLogger.isLevelInitialized()) {
                                        this.rootLogger.setLevel(LogManager.defaultLevel);
                                    }
                                    this.addLogger(Logger.global);
                                    return null;
                                }
                            });
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

    /* access modifiers changed from: private */
    public void readPrimordialConfiguration() {
        if (!this.readPrimordialConfiguration) {
            synchronized (this) {
                if (!this.readPrimordialConfiguration) {
                    if (System.out != null) {
                        this.readPrimordialConfiguration = true;
                        try {
                            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                                public Void run() throws Exception {
                                    LogManager.this.readConfiguration();
                                    PlatformLogger.redirectPlatformLoggers();
                                    return null;
                                }
                            });
                        } catch (Exception e) {
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
            Integer value = this.listenerMap.get(listener);
            int i = 1;
            if (value != null) {
                i = 1 + value.intValue();
            }
            this.listenerMap.put(listener, Integer.valueOf(i));
        }
    }

    @Deprecated
    public void removePropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        checkPermission();
        if (l != null) {
            PropertyChangeListener listener = l;
            synchronized (this.listenerMap) {
                Integer value = this.listenerMap.get(listener);
                if (value != null) {
                    int i = value.intValue();
                    if (i == 1) {
                        this.listenerMap.remove(listener);
                    } else {
                        this.listenerMap.put(listener, Integer.valueOf(i - 1));
                    }
                }
            }
        }
    }

    private LoggerContext getUserContext() {
        return this.userContext;
    }

    /* access modifiers changed from: package-private */
    public final LoggerContext getSystemContext() {
        return this.systemContext;
    }

    private List<LoggerContext> contexts() {
        List<LoggerContext> cxs = new ArrayList<>();
        cxs.add(getSystemContext());
        cxs.add(getUserContext());
        return cxs;
    }

    /* access modifiers changed from: package-private */
    public Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        Logger result = getLogger(name);
        if (result == null) {
            Logger logger = new Logger(name, resourceBundleName, caller, this, $assertionsDisabled);
            while (!addLogger(logger)) {
                result = getLogger(name);
                if (result != null) {
                }
            }
            return logger;
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public Logger demandSystemLogger(String name, String resourceBundleName) {
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

    /* access modifiers changed from: private */
    public static Class getClassInstance(String cname) {
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
                    } catch (Exception ex) {
                        System.err.println("Can't load log handler \"" + word + "\"");
                        System.err.println("" + ex);
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        });
    }

    /* access modifiers changed from: package-private */
    public final void drainLoggerRefQueueBounded() {
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
        if (name != null) {
            drainLoggerRefQueueBounded();
            if (!getUserContext().addLocalLogger(logger, this)) {
                return $assertionsDisabled;
            }
            loadLoggerHandlers(logger, name, name + ".handlers");
            return true;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: private */
    public static void doSetLevel(final Logger logger, final Level level) {
        if (System.getSecurityManager() == null) {
            logger.setLevel(level);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    Logger.this.setLevel(level);
                    return null;
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public static void doSetParent(final Logger logger, final Logger parent) {
        if (System.getSecurityManager() == null) {
            logger.setParent(parent);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    Logger.this.setParent(parent);
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
            } catch (Exception ex) {
                PrintStream printStream = System.err;
                printStream.println("Logging configuration class \"" + cname + "\" failed");
                PrintStream printStream2 = System.err;
                printStream2.println("" + ex);
            }
        }
        String fname = System.getProperty("java.util.logging.config.file");
        if (fname == null) {
            String fname2 = System.getProperty("java.home");
            if (fname2 != null) {
                fname = new File(new File(fname2, "lib"), "logging.properties").getCanonicalPath();
            } else {
                throw new Error("Can't find java.home ??");
            }
        }
        try {
            in = new FileInputStream(fname);
        } catch (Exception e) {
            InputStream in2 = LogManager.class.getResourceAsStream("logging.properties");
            if (in2 != null) {
                in = in2;
            } else {
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
                Logger logger = cx.findLogger(enum_.nextElement());
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

    /* access modifiers changed from: private */
    public String[] parseClassNames(String propertyName) {
        String hands = getProperty(propertyName);
        if (hands == null) {
            return new String[0];
        }
        String hands2 = hands.trim();
        int ix = 0;
        List<String> result = new ArrayList<>();
        while (ix < hands2.length()) {
            int end = ix;
            while (end < hands2.length() && !Character.isWhitespace(hands2.charAt(end)) && hands2.charAt(end) != ',') {
                end++;
            }
            String word = hands2.substring(ix, end);
            ix = end + 1;
            String word2 = word.trim();
            if (word2.length() != 0) {
                result.add(word2);
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public void readConfiguration(InputStream ins) throws IOException, SecurityException {
        checkPermission();
        reset();
        this.props.load(ins);
        String[] names = parseClassNames("config");
        for (String word : names) {
            try {
                getClassInstance(word).newInstance();
            } catch (Exception ex) {
                System.err.println("Can't load config class \"" + word + "\"");
                System.err.println("" + ex);
            }
        }
        setLevelsOnExistingLoggers();
        Map<Object, Integer> listeners = null;
        synchronized (this.listenerMap) {
            if (!this.listenerMap.isEmpty()) {
                listeners = new HashMap<>((Map<? extends Object, ? extends Integer>) this.listenerMap);
            }
        }
        if (listeners != null) {
            Object ev = Beans.newPropertyChangeEvent(LogManager.class, null, null, null);
            for (Map.Entry<Object, Integer> entry : listeners.entrySet()) {
                Object listener = entry.getKey();
                int count = entry.getValue().intValue();
                for (int i = 0; i < count; i++) {
                    Beans.invokePropertyChange(listener, ev);
                }
            }
        }
        synchronized (this) {
            this.initializedGlobalHandlers = $assertionsDisabled;
        }
    }

    public String getProperty(String name) {
        return this.props.getProperty(name);
    }

    /* access modifiers changed from: package-private */
    public String getStringProperty(String name, String defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        return val.trim();
    }

    /* access modifiers changed from: package-private */
    public int getIntProperty(String name, int defaultValue) {
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

    /* access modifiers changed from: package-private */
    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        String val2 = val.toLowerCase();
        if (val2.equals("true") || val2.equals("1")) {
            return true;
        }
        if (val2.equals("false") || val2.equals("0")) {
            return $assertionsDisabled;
        }
        return defaultValue;
    }

    /* access modifiers changed from: package-private */
    public Level getLevelProperty(String name, Level defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.findLevel(val.trim());
        return l != null ? l : defaultValue;
    }

    /* access modifiers changed from: package-private */
    public Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getProperty(name);
        if (val != null) {
            try {
                return (Filter) getClassInstance(val).newInstance();
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    /* access modifiers changed from: package-private */
    public Formatter getFormatterProperty(String name, Formatter defaultValue) {
        String val = getProperty(name);
        if (val != null) {
            try {
                return (Formatter) getClassInstance(val).newInstance();
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    /* access modifiers changed from: private */
    public synchronized void initializeGlobalHandlers() {
        if (!this.initializedGlobalHandlers) {
            this.initializedGlobalHandlers = true;
            if (!this.deathImminent) {
                loadLoggerHandlers(this.rootLogger, null, "handlers");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void checkPermission() {
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
                    PrintStream printStream = System.err;
                    printStream.println("Bad level value for property: " + key);
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
        LoggingMXBean loggingMXBean2;
        synchronized (LogManager.class) {
            if (loggingMXBean == null) {
                loggingMXBean = new Logging();
            }
            loggingMXBean2 = loggingMXBean;
        }
        return loggingMXBean2;
    }
}
