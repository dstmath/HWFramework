package org.apache.commons.logging.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

@Deprecated
public class LogFactoryImpl extends LogFactory {
    public static final String ALLOW_FLAWED_CONTEXT_PROPERTY = "org.apache.commons.logging.Log.allowFlawedContext";
    public static final String ALLOW_FLAWED_DISCOVERY_PROPERTY = "org.apache.commons.logging.Log.allowFlawedDiscovery";
    public static final String ALLOW_FLAWED_HIERARCHY_PROPERTY = "org.apache.commons.logging.Log.allowFlawedHierarchy";
    private static final String LOGGING_IMPL_JDK14_LOGGER = "org.apache.commons.logging.impl.Jdk14Logger";
    private static final String LOGGING_IMPL_LOG4J_LOGGER = "org.apache.commons.logging.impl.Log4JLogger";
    private static final String LOGGING_IMPL_LUMBERJACK_LOGGER = "org.apache.commons.logging.impl.Jdk13LumberjackLogger";
    private static final String LOGGING_IMPL_SIMPLE_LOGGER = "org.apache.commons.logging.impl.SimpleLog";
    public static final String LOG_PROPERTY = "org.apache.commons.logging.Log";
    protected static final String LOG_PROPERTY_OLD = "org.apache.commons.logging.log";
    private static final String PKG_IMPL = "org.apache.commons.logging.impl.";
    private static final int PKG_LEN = PKG_IMPL.length();
    private static final String[] classesToDiscover = {LOGGING_IMPL_LOG4J_LOGGER, LOGGING_IMPL_JDK14_LOGGER, LOGGING_IMPL_LUMBERJACK_LOGGER, LOGGING_IMPL_SIMPLE_LOGGER};
    private boolean allowFlawedContext;
    private boolean allowFlawedDiscovery;
    private boolean allowFlawedHierarchy;
    protected Hashtable attributes = new Hashtable();
    private String diagnosticPrefix;
    protected Hashtable instances = new Hashtable();
    private String logClassName;
    protected Constructor logConstructor = null;
    protected Class[] logConstructorSignature = {String.class};
    protected Method logMethod = null;
    protected Class[] logMethodSignature = {LogFactory.class};
    private boolean useTCCL = true;

    public LogFactoryImpl() {
        initDiagnostics();
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Instance created.");
        }
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public String[] getAttributeNames() {
        Vector names = new Vector();
        Enumeration keys = this.attributes.keys();
        while (keys.hasMoreElements()) {
            names.addElement((String) keys.nextElement());
        }
        String[] results = new String[names.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = (String) names.elementAt(i);
        }
        return results;
    }

    public Log getInstance(Class clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    public Log getInstance(String name) throws LogConfigurationException {
        Log instance = (Log) this.instances.get(name);
        if (instance != null) {
            return instance;
        }
        Log instance2 = newInstance(name);
        this.instances.put(name, instance2);
        return instance2;
    }

    public void release() {
        logDiagnostic("Releasing all known loggers");
        this.instances.clear();
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        if (this.logConstructor != null) {
            logDiagnostic("setAttribute: call too late; configuration already performed.");
        }
        if (value == null) {
            this.attributes.remove(name);
        } else {
            this.attributes.put(name, value);
        }
        if (name.equals(LogFactory.TCCL_KEY)) {
            this.useTCCL = Boolean.valueOf(value.toString()).booleanValue();
        }
    }

    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        return LogFactory.getContextClassLoader();
    }

    protected static boolean isDiagnosticsEnabled() {
        return LogFactory.isDiagnosticsEnabled();
    }

    protected static ClassLoader getClassLoader(Class clazz) {
        return LogFactory.getClassLoader(clazz);
    }

    private void initDiagnostics() {
        String classLoaderName;
        ClassLoader classLoader = getClassLoader(getClass());
        if (classLoader == null) {
            classLoaderName = "BOOTLOADER";
        } else {
            try {
                classLoaderName = objectId(classLoader);
            } catch (SecurityException e) {
                classLoaderName = "UNKNOWN";
            }
        }
        this.diagnosticPrefix = "[LogFactoryImpl@" + System.identityHashCode(this) + " from " + classLoaderName + "] ";
    }

    /* access modifiers changed from: protected */
    public void logDiagnostic(String msg) {
        if (isDiagnosticsEnabled()) {
            logRawDiagnostic(this.diagnosticPrefix + msg);
        }
    }

    /* access modifiers changed from: protected */
    public String getLogClassName() {
        if (this.logClassName == null) {
            discoverLogImplementation(getClass().getName());
        }
        return this.logClassName;
    }

    /* access modifiers changed from: protected */
    public Constructor getLogConstructor() throws LogConfigurationException {
        if (this.logConstructor == null) {
            discoverLogImplementation(getClass().getName());
        }
        return this.logConstructor;
    }

    /* access modifiers changed from: protected */
    public boolean isJdk13LumberjackAvailable() {
        return isLogLibraryAvailable("Jdk13Lumberjack", LOGGING_IMPL_LUMBERJACK_LOGGER);
    }

    /* access modifiers changed from: protected */
    public boolean isJdk14Available() {
        return isLogLibraryAvailable("Jdk14", LOGGING_IMPL_JDK14_LOGGER);
    }

    /* access modifiers changed from: protected */
    public boolean isLog4JAvailable() {
        return isLogLibraryAvailable("Log4J", LOGGING_IMPL_LOG4J_LOGGER);
    }

    /* access modifiers changed from: protected */
    public Log newInstance(String name) throws LogConfigurationException {
        Log instance;
        try {
            if (this.logConstructor == null) {
                instance = discoverLogImplementation(name);
            } else {
                instance = (Log) this.logConstructor.newInstance(new Object[]{name});
            }
            if (this.logMethod != null) {
                this.logMethod.invoke(instance, new Object[]{this});
            }
            return instance;
        } catch (LogConfigurationException lce) {
            throw lce;
        } catch (InvocationTargetException e) {
            Throwable c = e.getTargetException();
            if (c != null) {
                throw new LogConfigurationException(c);
            }
            throw new LogConfigurationException((Throwable) e);
        } catch (Throwable t) {
            throw new LogConfigurationException(t);
        }
    }

    private boolean isLogLibraryAvailable(String name, String classname) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Checking for '" + name + "'.");
        }
        try {
            if (createLogFromClass(classname, getClass().getName(), false) == null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("Did not find '" + name + "'.");
                }
                return false;
            }
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Found '" + name + "'.");
            }
            return true;
        } catch (LogConfigurationException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Logging system '" + name + "' is available but not useable.");
            }
            return false;
        }
    }

    private String getConfigurationValue(String property) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[ENV] Trying to get configuration for item " + property);
        }
        Object valueObj = getAttribute(property);
        if (valueObj != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] Found LogFactory attribute [" + valueObj + "] for " + property);
            }
            return valueObj.toString();
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[ENV] No LogFactory attribute found for " + property);
        }
        try {
            String value = System.getProperty(property);
            if (value != null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("[ENV] Found system property [" + value + "] for " + property);
                }
                return value;
            }
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] No system property found for property " + property);
            }
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] No configuration defined for item " + property);
            }
            return null;
        } catch (SecurityException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] Security prevented reading system property " + property);
            }
        }
    }

    private boolean getBooleanConfiguration(String key, boolean dflt) {
        String val = getConfigurationValue(key);
        if (val == null) {
            return dflt;
        }
        return Boolean.valueOf(val).booleanValue();
    }

    private void initConfiguration() {
        this.allowFlawedContext = getBooleanConfiguration(ALLOW_FLAWED_CONTEXT_PROPERTY, true);
        this.allowFlawedDiscovery = getBooleanConfiguration(ALLOW_FLAWED_DISCOVERY_PROPERTY, true);
        this.allowFlawedHierarchy = getBooleanConfiguration(ALLOW_FLAWED_HIERARCHY_PROPERTY, true);
    }

    private Log discoverLogImplementation(String logCategory) throws LogConfigurationException {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Discovering a Log implementation...");
        }
        initConfiguration();
        Log result = null;
        String specifiedLogClassName = findUserSpecifiedLogClassName();
        if (specifiedLogClassName != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Attempting to load user-specified log class '" + specifiedLogClassName + "'...");
            }
            Log result2 = createLogFromClass(specifiedLogClassName, logCategory, true);
            if (result2 != null) {
                return result2;
            }
            StringBuffer messageBuffer = new StringBuffer("User-specified log class '");
            messageBuffer.append(specifiedLogClassName);
            messageBuffer.append("' cannot be found or is not useable.");
            if (specifiedLogClassName != null) {
                informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_LOG4J_LOGGER);
                informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_JDK14_LOGGER);
                informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_LUMBERJACK_LOGGER);
                informUponSimilarName(messageBuffer, specifiedLogClassName, LOGGING_IMPL_SIMPLE_LOGGER);
            }
            throw new LogConfigurationException(messageBuffer.toString());
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("No user-specified Log implementation; performing discovery using the standard supported logging implementations...");
        }
        for (int i = 0; i < classesToDiscover.length && result == null; i++) {
            result = createLogFromClass(classesToDiscover[i], logCategory, true);
        }
        if (result != null) {
            return result;
        }
        throw new LogConfigurationException("No suitable Log implementation");
    }

    private void informUponSimilarName(StringBuffer messageBuffer, String name, String candidate) {
        if (!name.equals(candidate)) {
            if (name.regionMatches(true, 0, candidate, 0, PKG_LEN + 5)) {
                messageBuffer.append(" Did you mean '");
                messageBuffer.append(candidate);
                messageBuffer.append("'?");
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v10, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private String findUserSpecifiedLogClassName() {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Trying to get log class from attribute 'org.apache.commons.logging.Log'");
        }
        String specifiedClass = (String) getAttribute(LOG_PROPERTY);
        if (specifiedClass == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from attribute 'org.apache.commons.logging.log'");
            }
            specifiedClass = getAttribute(LOG_PROPERTY_OLD);
        }
        if (specifiedClass == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property 'org.apache.commons.logging.Log'");
            }
            try {
                specifiedClass = System.getProperty(LOG_PROPERTY);
            } catch (SecurityException e) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("No access allowed to system property 'org.apache.commons.logging.Log' - " + e.getMessage());
                }
            }
        }
        if (specifiedClass == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property 'org.apache.commons.logging.log'");
            }
            try {
                specifiedClass = System.getProperty(LOG_PROPERTY_OLD);
            } catch (SecurityException e2) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("No access allowed to system property 'org.apache.commons.logging.log' - " + e2.getMessage());
                }
            }
        }
        if (specifiedClass != null) {
            return specifiedClass.trim();
        }
        return specifiedClass;
    }

    private Log createLogFromClass(String logAdapterClassName, String logCategory, boolean affectState) throws LogConfigurationException {
        Class c;
        URL url;
        String str = logAdapterClassName;
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Attempting to instantiate '" + str + "'");
        }
        Object[] params = {logCategory};
        Log logAdapter = null;
        ClassLoader currentCL = getBaseClassLoader();
        Class logAdapterClass = null;
        Constructor constructor = null;
        while (true) {
            logDiagnostic("Trying to load '" + str + "' from classloader " + objectId(currentCL));
            try {
                if (isDiagnosticsEnabled()) {
                    String resourceName = str.replace('.', '/') + ".class";
                    if (currentCL != null) {
                        url = currentCL.getResource(resourceName);
                    } else {
                        url = ClassLoader.getSystemResource(resourceName + ".class");
                    }
                    if (url == null) {
                        logDiagnostic("Class '" + str + "' [" + resourceName + "] cannot be found.");
                    } else {
                        logDiagnostic("Class '" + str + "' was found at '" + url + "'");
                    }
                }
                try {
                    c = Class.forName(str, true, currentCL);
                } catch (ClassNotFoundException e) {
                    ClassNotFoundException originalClassNotFoundException = e;
                    String msg = "" + originalClassNotFoundException.getMessage();
                    logDiagnostic("The log adapter '" + str + "' is not available via classloader " + objectId(currentCL) + ": " + msg.trim());
                    try {
                        c = Class.forName(logAdapterClassName);
                    } catch (ClassNotFoundException secondaryClassNotFoundException) {
                        ClassNotFoundException classNotFoundException = secondaryClassNotFoundException;
                        String msg2 = "" + secondaryClassNotFoundException.getMessage();
                        logDiagnostic("The log adapter '" + str + "' is not available via the LogFactoryImpl class classloader: " + msg2.trim());
                    }
                }
                constructor = c.getConstructor(this.logConstructorSignature);
                Object newInstance = constructor.newInstance(params);
                if (newInstance instanceof Log) {
                    logAdapterClass = c;
                    logAdapter = newInstance;
                    break;
                }
                handleFlawedHierarchy(currentCL, c);
                if (currentCL == null) {
                    break;
                }
                currentCL = currentCL.getParent();
            } catch (NoClassDefFoundError e2) {
                String msg3 = "" + e2.getMessage();
                logDiagnostic("The log adapter '" + str + "' is missing dependencies when loaded via classloader " + objectId(currentCL) + ": " + msg3.trim());
            } catch (ExceptionInInitializerError e3) {
                String msg4 = "" + e3.getMessage();
                logDiagnostic("The log adapter '" + str + "' is unable to initialize itself when loaded via classloader " + objectId(currentCL) + ": " + msg4.trim());
            } catch (LogConfigurationException e4) {
                throw e4;
            } catch (Throwable t) {
                handleFlawedDiscovery(str, currentCL, t);
            }
        }
        if (logAdapter != null && affectState) {
            this.logClassName = str;
            this.logConstructor = constructor;
            try {
                this.logMethod = logAdapterClass.getMethod("setLogFactory", this.logMethodSignature);
                logDiagnostic("Found method setLogFactory(LogFactory) in '" + str + "'");
            } catch (Throwable th) {
                this.logMethod = null;
                logDiagnostic("[INFO] '" + str + "' from classloader " + objectId(currentCL) + " does not declare optional method setLogFactory(LogFactory)");
            }
            logDiagnostic("Log adapter '" + str + "' from classloader " + objectId(logAdapterClass.getClassLoader()) + " has been selected for use.");
        }
        return logAdapter;
    }

    private ClassLoader getBaseClassLoader() throws LogConfigurationException {
        ClassLoader thisClassLoader = getClassLoader(LogFactoryImpl.class);
        if (!this.useTCCL) {
            return thisClassLoader;
        }
        ClassLoader contextClassLoader = getContextClassLoader();
        ClassLoader baseClassLoader = getLowestClassLoader(contextClassLoader, thisClassLoader);
        if (baseClassLoader != null) {
            if (baseClassLoader != contextClassLoader) {
                if (!this.allowFlawedContext) {
                    throw new LogConfigurationException("Bad classloader hierarchy; LogFactoryImpl was loaded via a classloader that is not related to the current context classloader.");
                } else if (isDiagnosticsEnabled()) {
                    logDiagnostic("Warning: the context classloader is an ancestor of the classloader that loaded LogFactoryImpl; it should be the same or a descendant. The application using commons-logging should ensure the context classloader is used correctly.");
                }
            }
            return baseClassLoader;
        } else if (this.allowFlawedContext) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[WARNING] the context classloader is not part of a parent-child relationship with the classloader that loaded LogFactoryImpl.");
            }
            return contextClassLoader;
        } else {
            throw new LogConfigurationException("Bad classloader hierarchy; LogFactoryImpl was loaded via a classloader that is not related to the current context classloader.");
        }
    }

    private ClassLoader getLowestClassLoader(ClassLoader c1, ClassLoader c2) {
        if (c1 == null) {
            return c2;
        }
        if (c2 == null) {
            return c1;
        }
        for (ClassLoader current = c1; current != null; current = current.getParent()) {
            if (current == c2) {
                return c1;
            }
        }
        for (ClassLoader current2 = c2; current2 != null; current2 = current2.getParent()) {
            if (current2 == c1) {
                return c2;
            }
        }
        return null;
    }

    private void handleFlawedDiscovery(String logAdapterClassName, ClassLoader classLoader, Throwable discoveryFlaw) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Could not instantiate Log '" + logAdapterClassName + "' -- " + discoveryFlaw.getClass().getName() + ": " + discoveryFlaw.getLocalizedMessage());
        }
        if (!this.allowFlawedDiscovery) {
            throw new LogConfigurationException(discoveryFlaw);
        }
    }

    private void handleFlawedHierarchy(ClassLoader badClassLoader, Class badClass) throws LogConfigurationException {
        boolean implementsLog = false;
        String logInterfaceName = Log.class.getName();
        Class[] interfaces = badClass.getInterfaces();
        int i = 0;
        while (true) {
            if (i >= interfaces.length) {
                break;
            } else if (logInterfaceName.equals(interfaces[i].getName())) {
                implementsLog = true;
                break;
            } else {
                i++;
            }
        }
        if (implementsLog) {
            if (isDiagnosticsEnabled()) {
                try {
                    ClassLoader logInterfaceClassLoader = getClassLoader(Log.class);
                    logDiagnostic("Class '" + badClass.getName() + "' was found in classloader " + objectId(badClassLoader) + ". It is bound to a Log interface which is not the one loaded from classloader " + objectId(logInterfaceClassLoader));
                } catch (Throwable th) {
                    logDiagnostic("Error while trying to output diagnostics about bad class '" + badClass + "'");
                }
            }
            if (!this.allowFlawedHierarchy) {
                StringBuffer msg = new StringBuffer();
                msg.append("Terminating logging for this context ");
                msg.append("due to bad log hierarchy. ");
                msg.append("You have more than one version of '");
                msg.append(Log.class.getName());
                msg.append("' visible.");
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(msg.toString());
                }
                throw new LogConfigurationException(msg.toString());
            } else if (isDiagnosticsEnabled()) {
                StringBuffer msg2 = new StringBuffer();
                msg2.append("Warning: bad log hierarchy. ");
                msg2.append("You have more than one version of '");
                msg2.append(Log.class.getName());
                msg2.append("' visible.");
                logDiagnostic(msg2.toString());
            }
        } else if (!this.allowFlawedDiscovery) {
            StringBuffer msg3 = new StringBuffer();
            msg3.append("Terminating logging for this context. ");
            msg3.append("Log class '");
            msg3.append(badClass.getName());
            msg3.append("' does not implement the Log interface.");
            if (isDiagnosticsEnabled()) {
                logDiagnostic(msg3.toString());
            }
            throw new LogConfigurationException(msg3.toString());
        } else if (isDiagnosticsEnabled()) {
            StringBuffer msg4 = new StringBuffer();
            msg4.append("[WARNING] Log class '");
            msg4.append(badClass.getName());
            msg4.append("' does not implement the Log interface.");
            logDiagnostic(msg4.toString());
        }
    }
}
