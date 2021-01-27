package org.apache.commons.logging;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.NoOpLog;

@Deprecated
public class LogSource {
    protected static boolean jdk14IsAvailable;
    protected static boolean log4jIsAvailable;
    protected static Constructor logImplctor = null;
    protected static Hashtable logs = new Hashtable();

    static {
        log4jIsAvailable = false;
        jdk14IsAvailable = false;
        try {
            if (Class.forName("org.apache.log4j.Logger") != null) {
                log4jIsAvailable = true;
            } else {
                log4jIsAvailable = false;
            }
        } catch (Throwable th) {
            log4jIsAvailable = false;
        }
        try {
            if (Class.forName("java.util.logging.Logger") == null || Class.forName("org.apache.commons.logging.impl.Jdk14Logger") == null) {
                jdk14IsAvailable = false;
            } else {
                jdk14IsAvailable = true;
            }
        } catch (Throwable th2) {
            jdk14IsAvailable = false;
        }
        String name = null;
        try {
            name = System.getProperty("org.apache.commons.logging.log");
            if (name == null) {
                name = System.getProperty(LogFactoryImpl.LOG_PROPERTY);
            }
        } catch (Throwable th3) {
        }
        if (name != null) {
            try {
                setLogImplementation(name);
            } catch (Throwable th4) {
            }
        } else {
            try {
                if (log4jIsAvailable) {
                    setLogImplementation("org.apache.commons.logging.impl.Log4JLogger");
                } else if (jdk14IsAvailable) {
                    setLogImplementation("org.apache.commons.logging.impl.Jdk14Logger");
                } else {
                    setLogImplementation("org.apache.commons.logging.impl.NoOpLog");
                }
            } catch (Throwable th5) {
            }
        }
    }

    private LogSource() {
    }

    public static void setLogImplementation(String classname) throws LinkageError, ExceptionInInitializerError, NoSuchMethodException, SecurityException, ClassNotFoundException {
        try {
            logImplctor = Class.forName(classname).getConstructor("".getClass());
        } catch (Throwable th) {
            logImplctor = null;
        }
    }

    public static void setLogImplementation(Class logclass) throws LinkageError, ExceptionInInitializerError, NoSuchMethodException, SecurityException {
        logImplctor = logclass.getConstructor("".getClass());
    }

    public static Log getInstance(String name) {
        Log log = (Log) logs.get(name);
        if (log != null) {
            return log;
        }
        Log log2 = makeNewLogInstance(name);
        logs.put(name, log2);
        return log2;
    }

    public static Log getInstance(Class clazz) {
        return getInstance(clazz.getName());
    }

    public static Log makeNewLogInstance(String name) {
        Log log;
        try {
            log = (Log) logImplctor.newInstance(name);
        } catch (Throwable th) {
            log = null;
        }
        if (log == null) {
            return new NoOpLog(name);
        }
        return log;
    }

    public static String[] getLogNames() {
        return (String[]) logs.keySet().toArray(new String[logs.size()]);
    }
}
