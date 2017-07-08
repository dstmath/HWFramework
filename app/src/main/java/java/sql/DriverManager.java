package java.sql;

import dalvik.system.VMStack;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import sun.reflect.CallerSensitive;

public class DriverManager {
    static final SQLPermission SET_LOG_PERMISSION = null;
    private static volatile PrintStream logStream;
    private static final Object logSync = null;
    private static volatile PrintWriter logWriter;
    private static volatile int loginTimeout;
    private static final CopyOnWriteArrayList<DriverInfo> registeredDrivers = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.sql.DriverManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.sql.DriverManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.sql.DriverManager.<clinit>():void");
    }

    private DriverManager() {
    }

    public static PrintWriter getLogWriter() {
        return logWriter;
    }

    public static void setLogWriter(PrintWriter out) {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(SET_LOG_PERMISSION);
        }
        logStream = null;
        logWriter = out;
    }

    @CallerSensitive
    public static Connection getConnection(String url, Properties info) throws SQLException {
        return getConnection(url, info, VMStack.getCallingClassLoader());
    }

    @CallerSensitive
    public static Connection getConnection(String url, String user, String password) throws SQLException {
        Properties info = new Properties();
        if (user != null) {
            info.put("user", user);
        }
        if (password != null) {
            info.put("password", password);
        }
        return getConnection(url, info, VMStack.getCallingClassLoader());
    }

    @CallerSensitive
    public static Connection getConnection(String url) throws SQLException {
        return getConnection(url, new Properties(), VMStack.getCallingClassLoader());
    }

    @CallerSensitive
    public static Driver getDriver(String url) throws SQLException {
        println("DriverManager.getDriver(\"" + url + "\")");
        ClassLoader callerClassLoader = VMStack.getCallingClassLoader();
        for (DriverInfo aDriver : registeredDrivers) {
            if (isDriverAllowed(aDriver.driver, callerClassLoader)) {
                try {
                    if (aDriver.driver.acceptsURL(url)) {
                        println("getDriver returning " + aDriver.driver.getClass().getName());
                        return aDriver.driver;
                    }
                } catch (SQLException e) {
                }
            } else {
                println("    skipping: " + aDriver.driver.getClass().getName());
            }
        }
        println("getDriver: no suitable driver");
        throw new SQLException("No suitable driver", "08001");
    }

    public static synchronized void registerDriver(Driver driver) throws SQLException {
        synchronized (DriverManager.class) {
            if (driver != null) {
                registeredDrivers.addIfAbsent(new DriverInfo(driver));
                println("registerDriver: " + driver);
            } else {
                throw new NullPointerException();
            }
        }
    }

    @CallerSensitive
    public static synchronized void deregisterDriver(Driver driver) throws SQLException {
        synchronized (DriverManager.class) {
            if (driver == null) {
                return;
            }
            println("DriverManager.deregisterDriver: " + driver);
            DriverInfo aDriver = new DriverInfo(driver);
            if (!registeredDrivers.contains(aDriver)) {
                println("    couldn't find driver to unload");
            } else if (isDriverAllowed(driver, VMStack.getCallingClassLoader())) {
                registeredDrivers.remove(aDriver);
            } else {
                throw new SecurityException();
            }
        }
    }

    @CallerSensitive
    public static Enumeration<Driver> getDrivers() {
        Vector<Driver> result = new Vector();
        ClassLoader callerClassLoader = VMStack.getCallingClassLoader();
        for (DriverInfo aDriver : registeredDrivers) {
            if (isDriverAllowed(aDriver.driver, callerClassLoader)) {
                result.addElement(aDriver.driver);
            } else {
                println("    skipping: " + aDriver.getClass().getName());
            }
        }
        return result.elements();
    }

    public static void setLoginTimeout(int seconds) {
        loginTimeout = seconds;
    }

    public static int getLoginTimeout() {
        return loginTimeout;
    }

    @Deprecated
    public static void setLogStream(PrintStream out) {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(SET_LOG_PERMISSION);
        }
        logStream = out;
        if (out != null) {
            logWriter = new PrintWriter((OutputStream) out);
        } else {
            logWriter = null;
        }
    }

    @Deprecated
    public static PrintStream getLogStream() {
        return logStream;
    }

    public static void println(String message) {
        synchronized (logSync) {
            if (logWriter != null) {
                logWriter.println(message);
                logWriter.flush();
            }
        }
    }

    private static boolean isDriverAllowed(Driver driver, ClassLoader classLoader) {
        if (driver == null) {
            return false;
        }
        Class<?> aClass = null;
        try {
            aClass = Class.forName(driver.getClass().getName(), true, classLoader);
        } catch (Exception e) {
        }
        return aClass == driver.getClass();
    }

    private static void loadInitialDrivers() {
        String str;
        try {
            str = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("jdbc.drivers");
                }
            });
        } catch (Exception e) {
            str = null;
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                Iterator driversIterator = ServiceLoader.load(Driver.class).iterator();
                while (driversIterator.hasNext()) {
                    try {
                        driversIterator.next();
                    } catch (Throwable th) {
                    }
                }
                return null;
            }
        });
        println("DriverManager.initialize: jdbc.drivers = " + str);
        if (str != null && !str.equals("")) {
            String[] driversList = str.split(":");
            println("number of Drivers:" + driversList.length);
            for (String aDriver : driversList) {
                try {
                    println("DriverManager.Initialize: loading " + aDriver);
                    Class.forName(aDriver, true, ClassLoader.getSystemClassLoader());
                } catch (Object ex) {
                    println("DriverManager.Initialize: load failed: " + ex);
                }
            }
        }
    }

    private static Connection getConnection(String url, Properties info, ClassLoader callerCL) throws SQLException {
        synchronized (DriverManager.class) {
            if (callerCL == null) {
                callerCL = Thread.currentThread().getContextClassLoader();
            }
        }
        if (url == null) {
            throw new SQLException("The url cannot be null", "08001");
        }
        println("DriverManager.getConnection(\"" + url + "\")");
        Object reason = null;
        for (DriverInfo aDriver : registeredDrivers) {
            if (isDriverAllowed(aDriver.driver, callerCL)) {
                try {
                    println("    trying " + aDriver.driver.getClass().getName());
                    Connection con = aDriver.driver.connect(url, info);
                    if (con != null) {
                        println("getConnection returning " + aDriver.driver.getClass().getName());
                        return con;
                    }
                } catch (SQLException ex) {
                    if (reason == null) {
                        reason = ex;
                    }
                }
            } else {
                println("    skipping: " + aDriver.getClass().getName());
            }
        }
        if (reason != null) {
            println("getConnection failed: " + reason);
            throw reason;
        }
        println("getConnection: no suitable driver found for " + url);
        throw new SQLException("No suitable driver found for " + url, "08001");
    }
}
