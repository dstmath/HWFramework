package java.util.logging;

import dalvik.system.VMStack;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import sun.reflect.CallerSensitive;

public class Logger {
    static final /* synthetic */ boolean -assertionsDisabled = (Logger.class.desiredAssertionStatus() ^ 1);
    public static final String GLOBAL_LOGGER_NAME = "global";
    private static final LoggerBundle NO_RESOURCE_BUNDLE = new LoggerBundle(null, null, null);
    private static final LoggerBundle SYSTEM_BUNDLE = new LoggerBundle(SYSTEM_LOGGER_RB_NAME, null, null);
    static final String SYSTEM_LOGGER_RB_NAME = "sun.util.logging.resources.logging";
    private static final Handler[] emptyHandlers = new Handler[0];
    @Deprecated
    public static final Logger global = new Logger(GLOBAL_LOGGER_NAME);
    private static final int offValue = Level.OFF.intValue();
    private static final Object treeLock = new Object();
    private boolean anonymous;
    private WeakReference<ClassLoader> callersClassLoaderRef;
    private ResourceBundle catalog;
    private Locale catalogLocale;
    private String catalogName;
    private volatile Filter filter;
    private final CopyOnWriteArrayList<Handler> handlers;
    private final boolean isSystemLogger;
    private ArrayList<LoggerWeakRef> kids;
    private volatile Level levelObject;
    private volatile int levelValue;
    private volatile LoggerBundle loggerBundle;
    private volatile LogManager manager;
    private String name;
    private volatile Logger parent;
    private volatile boolean useParentHandlers;

    private static final class LoggerBundle {
        final String resourceBundleName;
        final ResourceBundle userBundle;

        /* synthetic */ LoggerBundle(String resourceBundleName, ResourceBundle bundle, LoggerBundle -this2) {
            this(resourceBundleName, bundle);
        }

        private LoggerBundle(String resourceBundleName, ResourceBundle bundle) {
            this.resourceBundleName = resourceBundleName;
            this.userBundle = bundle;
        }

        boolean isSystemBundle() {
            return Logger.SYSTEM_LOGGER_RB_NAME.equals(this.resourceBundleName);
        }

        static LoggerBundle get(String name, ResourceBundle bundle) {
            if (name == null && bundle == null) {
                return Logger.NO_RESOURCE_BUNDLE;
            }
            if (Logger.SYSTEM_LOGGER_RB_NAME.equals(name) && bundle == null) {
                return Logger.SYSTEM_BUNDLE;
            }
            return new LoggerBundle(name, bundle);
        }
    }

    private static class SystemLoggerHelper {
        static boolean disableCallerCheck = getBooleanProperty("sun.util.logging.disableCallerCheck");

        private SystemLoggerHelper() {
        }

        private static boolean getBooleanProperty(final String key) {
            return Boolean.parseBoolean((String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(key);
                }
            }));
        }
    }

    public static final Logger getGlobal() {
        LogManager.getLogManager();
        return global;
    }

    protected Logger(String name, String resourceBundleName) {
        this(name, resourceBundleName, null, LogManager.getLogManager(), -assertionsDisabled);
    }

    Logger(String name, String resourceBundleName, Class<?> caller, LogManager manager, boolean isSystemLogger) {
        this.handlers = new CopyOnWriteArrayList();
        this.loggerBundle = NO_RESOURCE_BUNDLE;
        this.useParentHandlers = true;
        this.manager = manager;
        this.isSystemLogger = isSystemLogger;
        setupResourceInfo(resourceBundleName, caller);
        this.name = name;
        this.levelValue = Level.INFO.intValue();
    }

    private void setCallersClassLoaderRef(Class<?> caller) {
        Object callersClassLoader;
        if (caller != null) {
            callersClassLoader = caller.getClassLoader();
        } else {
            callersClassLoader = null;
        }
        if (callersClassLoader != null) {
            this.callersClassLoaderRef = new WeakReference(callersClassLoader);
        }
    }

    private ClassLoader getCallersClassLoader() {
        if (this.callersClassLoaderRef != null) {
            return (ClassLoader) this.callersClassLoaderRef.get();
        }
        return null;
    }

    private Logger(String name) {
        this.handlers = new CopyOnWriteArrayList();
        this.loggerBundle = NO_RESOURCE_BUNDLE;
        this.useParentHandlers = true;
        this.name = name;
        this.isSystemLogger = true;
        this.levelValue = Level.INFO.intValue();
    }

    void setLogManager(LogManager manager) {
        this.manager = manager;
    }

    private void checkPermission() throws SecurityException {
        if (!this.anonymous) {
            if (this.manager == null) {
                this.manager = LogManager.getLogManager();
            }
            this.manager.checkPermission();
        }
    }

    private static Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        LogManager manager = LogManager.getLogManager();
        if (System.getSecurityManager() == null || (SystemLoggerHelper.disableCallerCheck ^ 1) == 0 || caller.getClassLoader() != null) {
            return manager.demandLogger(name, resourceBundleName, caller);
        }
        return manager.demandSystemLogger(name, resourceBundleName);
    }

    @CallerSensitive
    public static Logger getLogger(String name) {
        return demandLogger(name, null, VMStack.getStackClass1());
    }

    @CallerSensitive
    public static Logger getLogger(String name, String resourceBundleName) {
        Class<?> callerClass = VMStack.getStackClass1();
        Logger result = demandLogger(name, resourceBundleName, callerClass);
        result.setupResourceInfo(resourceBundleName, callerClass);
        return result;
    }

    static Logger getPlatformLogger(String name) {
        return LogManager.getLogManager().demandSystemLogger(name, SYSTEM_LOGGER_RB_NAME);
    }

    public static Logger getAnonymousLogger() {
        return getAnonymousLogger(null);
    }

    @CallerSensitive
    public static Logger getAnonymousLogger(String resourceBundleName) {
        LogManager manager = LogManager.getLogManager();
        manager.drainLoggerRefQueueBounded();
        Logger result = new Logger(null, resourceBundleName, VMStack.getStackClass1(), manager, -assertionsDisabled);
        result.anonymous = true;
        result.doSetParent(manager.getLogger(""));
        return result;
    }

    public ResourceBundle getResourceBundle() {
        return findResourceBundle(getResourceBundleName(), true);
    }

    public String getResourceBundleName() {
        return this.loggerBundle.resourceBundleName;
    }

    public void setFilter(Filter newFilter) throws SecurityException {
        checkPermission();
        this.filter = newFilter;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void log(LogRecord record) {
        if (isLoggable(record.getLevel())) {
            Filter theFilter = this.filter;
            if (theFilter == null || (theFilter.isLoggable(record) ^ 1) == 0) {
                Logger logger = this;
                while (logger != null) {
                    boolean useParentHdls;
                    Handler[] loggerHandlers;
                    if (this.isSystemLogger) {
                        loggerHandlers = logger.accessCheckedHandlers();
                    } else {
                        loggerHandlers = logger.getHandlers();
                    }
                    for (Handler handler : loggerHandlers) {
                        handler.publish(record);
                    }
                    if (this.isSystemLogger) {
                        useParentHdls = logger.useParentHandlers;
                    } else {
                        useParentHdls = logger.getUseParentHandlers();
                    }
                    if (!useParentHdls) {
                        break;
                    }
                    logger = this.isSystemLogger ? logger.parent : logger.getParent();
                }
            }
        }
    }

    private void doLog(LogRecord lr) {
        lr.setLoggerName(this.name);
        LoggerBundle lb = getEffectiveLoggerBundle();
        ResourceBundle bundle = lb.userBundle;
        String ebname = lb.resourceBundleName;
        if (!(ebname == null || bundle == null)) {
            lr.setResourceBundleName(ebname);
            lr.setResourceBundle(bundle);
        }
        log(lr);
    }

    public void log(Level level, String msg) {
        if (isLoggable(level)) {
            doLog(new LogRecord(level, msg));
        }
    }

    public void log(Level level, Supplier<String> msgSupplier) {
        if (isLoggable(level)) {
            doLog(new LogRecord(level, (String) msgSupplier.get()));
        }
    }

    public void log(Level level, String msg, Object param1) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setParameters(new Object[]{param1});
            doLog(lr);
        }
    }

    public void log(Level level, String msg, Object[] params) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setParameters(params);
            doLog(lr);
        }
    }

    public void log(Level level, String msg, Throwable thrown) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setThrown(thrown);
            doLog(lr);
        }
    }

    public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, (String) msgSupplier.get());
            lr.setThrown(thrown);
            doLog(lr);
        }
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            doLog(lr);
        }
    }

    public void logp(Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, (String) msgSupplier.get());
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            doLog(lr);
        }
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setParameters(new Object[]{param1});
            doLog(lr);
        }
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setParameters(params);
            doLog(lr);
        }
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            doLog(lr);
        }
    }

    public void logp(Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, (String) msgSupplier.get());
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            doLog(lr);
        }
    }

    private void doLog(LogRecord lr, String rbname) {
        lr.setLoggerName(this.name);
        if (rbname != null) {
            lr.setResourceBundleName(rbname);
            lr.setResourceBundle(findResourceBundle(rbname, -assertionsDisabled));
        }
        log(lr);
    }

    private void doLog(LogRecord lr, ResourceBundle rb) {
        lr.setLoggerName(this.name);
        if (rb != null) {
            lr.setResourceBundleName(rb.getBaseBundleName());
            lr.setResourceBundle(rb);
        }
        log(lr);
    }

    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            doLog(lr, bundleName);
        }
    }

    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object param1) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setParameters(new Object[]{param1});
            doLog(lr, bundleName);
        }
    }

    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object[] params) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setParameters(params);
            doLog(lr, bundleName);
        }
    }

    public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Object... params) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            if (!(params == null || params.length == 0)) {
                lr.setParameters(params);
            }
            doLog(lr, bundle);
        }
    }

    @Deprecated
    public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Throwable thrown) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            doLog(lr, bundleName);
        }
    }

    public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Throwable thrown) {
        if (isLoggable(level)) {
            LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            doLog(lr, bundle);
        }
    }

    public void entering(String sourceClass, String sourceMethod) {
        logp(Level.FINER, sourceClass, sourceMethod, "ENTRY");
    }

    public void entering(String sourceClass, String sourceMethod, Object param1) {
        logp(Level.FINER, sourceClass, sourceMethod, "ENTRY {0}", param1);
    }

    public void entering(String sourceClass, String sourceMethod, Object[] params) {
        String msg = "ENTRY";
        if (params == null) {
            logp(Level.FINER, sourceClass, sourceMethod, msg);
        } else if (isLoggable(Level.FINER)) {
            for (int i = 0; i < params.length; i++) {
                msg = msg + " {" + i + "}";
            }
            logp(Level.FINER, sourceClass, sourceMethod, msg, params);
        }
    }

    public void exiting(String sourceClass, String sourceMethod) {
        logp(Level.FINER, sourceClass, sourceMethod, "RETURN");
    }

    public void exiting(String sourceClass, String sourceMethod, Object result) {
        logp(Level.FINER, sourceClass, sourceMethod, "RETURN {0}", result);
    }

    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        if (isLoggable(Level.FINER)) {
            LogRecord lr = new LogRecord(Level.FINER, "THROW");
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            doLog(lr);
        }
    }

    public void severe(String msg) {
        log(Level.SEVERE, msg);
    }

    public void warning(String msg) {
        log(Level.WARNING, msg);
    }

    public void info(String msg) {
        log(Level.INFO, msg);
    }

    public void config(String msg) {
        log(Level.CONFIG, msg);
    }

    public void fine(String msg) {
        log(Level.FINE, msg);
    }

    public void finer(String msg) {
        log(Level.FINER, msg);
    }

    public void finest(String msg) {
        log(Level.FINEST, msg);
    }

    public void severe(Supplier<String> msgSupplier) {
        log(Level.SEVERE, (Supplier) msgSupplier);
    }

    public void warning(Supplier<String> msgSupplier) {
        log(Level.WARNING, (Supplier) msgSupplier);
    }

    public void info(Supplier<String> msgSupplier) {
        log(Level.INFO, (Supplier) msgSupplier);
    }

    public void config(Supplier<String> msgSupplier) {
        log(Level.CONFIG, (Supplier) msgSupplier);
    }

    public void fine(Supplier<String> msgSupplier) {
        log(Level.FINE, (Supplier) msgSupplier);
    }

    public void finer(Supplier<String> msgSupplier) {
        log(Level.FINER, (Supplier) msgSupplier);
    }

    public void finest(Supplier<String> msgSupplier) {
        log(Level.FINEST, (Supplier) msgSupplier);
    }

    public void setLevel(Level newLevel) throws SecurityException {
        checkPermission();
        synchronized (treeLock) {
            this.levelObject = newLevel;
            updateEffectiveLevel();
        }
    }

    final boolean isLevelInitialized() {
        return this.levelObject != null ? true : -assertionsDisabled;
    }

    public Level getLevel() {
        return this.levelObject;
    }

    public boolean isLoggable(Level level) {
        if (level.intValue() < this.levelValue || this.levelValue == offValue) {
            return -assertionsDisabled;
        }
        return true;
    }

    public String getName() {
        return this.name;
    }

    public void addHandler(Handler handler) throws SecurityException {
        handler.getClass();
        checkPermission();
        this.handlers.add(handler);
    }

    public void removeHandler(Handler handler) throws SecurityException {
        checkPermission();
        if (handler != null) {
            this.handlers.remove((Object) handler);
        }
    }

    public Handler[] getHandlers() {
        return accessCheckedHandlers();
    }

    Handler[] accessCheckedHandlers() {
        return (Handler[]) this.handlers.toArray(emptyHandlers);
    }

    public void setUseParentHandlers(boolean useParentHandlers) {
        checkPermission();
        this.useParentHandlers = useParentHandlers;
    }

    public boolean getUseParentHandlers() {
        return this.useParentHandlers;
    }

    private static ResourceBundle findSystemResourceBundle(final Locale locale) {
        return (ResourceBundle) AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>() {
            public ResourceBundle run() {
                try {
                    return ResourceBundle.getBundle(Logger.SYSTEM_LOGGER_RB_NAME, locale, ClassLoader.getSystemClassLoader());
                } catch (MissingResourceException e) {
                    throw new InternalError(e.toString());
                }
            }
        });
    }

    private synchronized ResourceBundle findResourceBundle(String name, boolean useCallersClassLoader) {
        if (name == null) {
            return null;
        }
        Locale currentLocale = Locale.getDefault();
        LoggerBundle lb = this.loggerBundle;
        if (lb.userBundle != null && name.equals(lb.resourceBundleName)) {
            return lb.userBundle;
        } else if (this.catalog != null && currentLocale.equals(this.catalogLocale) && name.equals(this.catalogName)) {
            return this.catalog;
        } else if (name.equals(SYSTEM_LOGGER_RB_NAME)) {
            this.catalog = findSystemResourceBundle(currentLocale);
            this.catalogName = name;
            this.catalogLocale = currentLocale;
            return this.catalog;
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            try {
                this.catalog = ResourceBundle.getBundle(name, currentLocale, cl);
                this.catalogName = name;
                this.catalogLocale = currentLocale;
                return this.catalog;
            } catch (MissingResourceException e) {
                if (!useCallersClassLoader) {
                    return null;
                }
                ClassLoader callersClassLoader = getCallersClassLoader();
                if (callersClassLoader == null || callersClassLoader == cl) {
                    return null;
                }
                try {
                    this.catalog = ResourceBundle.getBundle(name, currentLocale, callersClassLoader);
                    this.catalogName = name;
                    this.catalogLocale = currentLocale;
                    return this.catalog;
                } catch (MissingResourceException e2) {
                    return null;
                }
            }
        }
    }

    private synchronized void setupResourceInfo(String name, Class<?> callersClass) {
        LoggerBundle lb = this.loggerBundle;
        if (lb.resourceBundleName != null) {
            if (!lb.resourceBundleName.equals(name)) {
                throw new IllegalArgumentException(lb.resourceBundleName + " != " + name);
            }
        } else if (name != null) {
            setCallersClassLoaderRef(callersClass);
            if (this.isSystemLogger && getCallersClassLoader() != null) {
                checkPermission();
            }
            if (findResourceBundle(name, true) == null) {
                this.callersClassLoaderRef = null;
                throw new MissingResourceException("Can't find " + name + " bundle", name, "");
            } else if (-assertionsDisabled || lb.userBundle == null) {
                this.loggerBundle = LoggerBundle.get(name, null);
            } else {
                throw new AssertionError();
            }
        }
    }

    public void setResourceBundle(ResourceBundle bundle) {
        checkPermission();
        String baseName = bundle.getBaseBundleName();
        if (baseName == null || baseName.isEmpty()) {
            throw new IllegalArgumentException("resource bundle must have a name");
        }
        synchronized (this) {
            boolean canReplaceResourceBundle;
            LoggerBundle lb = this.loggerBundle;
            if (lb.resourceBundleName != null) {
                canReplaceResourceBundle = lb.resourceBundleName.equals(baseName);
            } else {
                canReplaceResourceBundle = true;
            }
            if (canReplaceResourceBundle) {
                this.loggerBundle = LoggerBundle.get(baseName, bundle);
            } else {
                throw new IllegalArgumentException("can't replace resource bundle");
            }
        }
    }

    public Logger getParent() {
        return this.parent;
    }

    public void setParent(Logger parent) {
        if (parent == null) {
            throw new NullPointerException();
        }
        if (this.manager == null) {
            this.manager = LogManager.getLogManager();
        }
        this.manager.checkPermission();
        doSetParent(parent);
    }

    private void doSetParent(Logger newParent) {
        Throwable th;
        synchronized (treeLock) {
            LoggerWeakRef ref = null;
            try {
                LoggerWeakRef ref2;
                if (this.parent != null) {
                    Iterator<LoggerWeakRef> iter = this.parent.kids.iterator();
                    while (iter.hasNext()) {
                        ref = (LoggerWeakRef) iter.next();
                        if (((Logger) ref.get()) == this) {
                            iter.remove();
                            ref2 = ref;
                            break;
                        }
                        ref = null;
                    }
                }
                ref2 = ref;
                try {
                    this.parent = newParent;
                    if (this.parent.kids == null) {
                        this.parent.kids = new ArrayList(2);
                    }
                    if (ref2 == null) {
                        LogManager logManager = this.manager;
                        logManager.getClass();
                        ref = new LoggerWeakRef(this);
                    } else {
                        ref = ref2;
                    }
                    ref.setParentRef(new WeakReference(this.parent));
                    this.parent.kids.add(ref);
                    updateEffectiveLevel();
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    final void removeChildLogger(LoggerWeakRef child) {
        synchronized (treeLock) {
            Iterator<LoggerWeakRef> iter = this.kids.iterator();
            while (iter.hasNext()) {
                if (((LoggerWeakRef) iter.next()) == child) {
                    iter.remove();
                    return;
                }
            }
        }
    }

    private void updateEffectiveLevel() {
        int newLevelValue;
        if (this.levelObject != null) {
            newLevelValue = this.levelObject.intValue();
        } else if (this.parent != null) {
            newLevelValue = this.parent.levelValue;
        } else {
            newLevelValue = Level.INFO.intValue();
        }
        if (this.levelValue != newLevelValue) {
            this.levelValue = newLevelValue;
            if (this.kids != null) {
                for (int i = 0; i < this.kids.size(); i++) {
                    Logger kid = (Logger) ((LoggerWeakRef) this.kids.get(i)).get();
                    if (kid != null) {
                        kid.updateEffectiveLevel();
                    }
                }
            }
        }
    }

    private LoggerBundle getEffectiveLoggerBundle() {
        LoggerBundle lb = this.loggerBundle;
        if (lb.isSystemBundle()) {
            return SYSTEM_BUNDLE;
        }
        ResourceBundle b = getResourceBundle();
        if (b != null && b == lb.userBundle) {
            return lb;
        }
        if (b != null) {
            return LoggerBundle.get(getResourceBundleName(), b);
        }
        Logger target = this.parent;
        while (target != null) {
            LoggerBundle trb = target.loggerBundle;
            if (trb.isSystemBundle()) {
                return SYSTEM_BUNDLE;
            }
            if (trb.userBundle != null) {
                return trb;
            }
            String rbName = this.isSystemLogger ? target.isSystemLogger ? trb.resourceBundleName : null : target.getResourceBundleName();
            if (rbName != null) {
                return LoggerBundle.get(rbName, findResourceBundle(rbName, true));
            }
            target = this.isSystemLogger ? target.parent : target.getParent();
        }
        return NO_RESOURCE_BUNDLE;
    }
}
