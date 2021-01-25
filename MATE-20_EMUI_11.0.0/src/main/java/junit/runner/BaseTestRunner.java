package junit.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.Properties;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestSuite;

public abstract class BaseTestRunner implements TestListener {
    public static final String SUITE_METHODNAME = "suite";
    private static Properties fPreferences;
    static boolean fgFilterStack = true;
    static int fgMaxMessageLength;
    boolean fLoading = true;

    /* access modifiers changed from: protected */
    public abstract void runFailed(String str);

    public abstract void testEnded(String str);

    public abstract void testFailed(int i, Test test, Throwable th);

    public abstract void testStarted(String str);

    static {
        fgMaxMessageLength = 500;
        fgMaxMessageLength = getPreference("maxmessage", fgMaxMessageLength);
    }

    public synchronized void startTest(Test test) {
        testStarted(test.toString());
    }

    protected static void setPreferences(Properties preferences) {
        fPreferences = preferences;
    }

    protected static Properties getPreferences() {
        if (fPreferences == null) {
            fPreferences = new Properties();
            fPreferences.put("loading", "true");
            fPreferences.put("filterstack", "true");
            readPreferences();
        }
        return fPreferences;
    }

    public static void savePreferences() throws IOException {
        FileOutputStream fos = new FileOutputStream(getPreferencesFile());
        try {
            getPreferences().store(fos, "");
        } finally {
            fos.close();
        }
    }

    public void setPreference(String key, String value) {
        getPreferences().put(key, value);
    }

    public synchronized void endTest(Test test) {
        testEnded(test.toString());
    }

    public synchronized void addError(Test test, Throwable t) {
        testFailed(1, test, t);
    }

    public synchronized void addFailure(Test test, AssertionFailedError t) {
        testFailed(2, test, t);
    }

    public Test getTest(String suiteClassName) {
        if (suiteClassName.length() <= 0) {
            clearStatus();
            return null;
        }
        try {
            Class<?> testClass = loadSuiteClass(suiteClassName);
            try {
                Method suiteMethod = testClass.getMethod(SUITE_METHODNAME, new Class[0]);
                if (!Modifier.isStatic(suiteMethod.getModifiers())) {
                    runFailed("Suite() method must be static");
                    return null;
                }
                try {
                    Test test = (Test) suiteMethod.invoke(null, new Class[0]);
                    if (test == null) {
                        return test;
                    }
                    clearStatus();
                    return test;
                } catch (InvocationTargetException e) {
                    runFailed("Failed to invoke suite():" + e.getTargetException().toString());
                    return null;
                } catch (IllegalAccessException e2) {
                    runFailed("Failed to invoke suite():" + e2.toString());
                    return null;
                }
            } catch (Exception e3) {
                clearStatus();
                return new TestSuite(testClass);
            }
        } catch (ClassNotFoundException e4) {
            String clazz = e4.getMessage();
            if (clazz == null) {
                clazz = suiteClassName;
            }
            runFailed("Class not found \"" + clazz + "\"");
            return null;
        } catch (Exception e5) {
            runFailed("Error: " + e5.toString());
            return null;
        }
    }

    public String elapsedTimeAsString(long runTime) {
        return NumberFormat.getInstance().format(((double) runTime) / 1000.0d);
    }

    /* access modifiers changed from: protected */
    public String processArguments(String[] args) {
        String suiteName = null;
        int i = 0;
        while (i < args.length) {
            if (args[i].equals("-noloading")) {
                setLoading(false);
            } else if (args[i].equals("-nofilterstack")) {
                fgFilterStack = false;
            } else if (args[i].equals("-c")) {
                if (args.length > i + 1) {
                    suiteName = extractClassName(args[i + 1]);
                } else {
                    System.out.println("Missing Test class name");
                }
                i++;
            } else {
                suiteName = args[i];
            }
            i++;
        }
        return suiteName;
    }

    public void setLoading(boolean enable) {
        this.fLoading = enable;
    }

    public String extractClassName(String className) {
        if (className.startsWith("Default package for")) {
            return className.substring(className.lastIndexOf(".") + 1);
        }
        return className;
    }

    public static String truncate(String s) {
        if (fgMaxMessageLength == -1 || s.length() <= fgMaxMessageLength) {
            return s;
        }
        return s.substring(0, fgMaxMessageLength) + "...";
    }

    @Deprecated
    public TestSuiteLoader getLoader() {
        return new StandardTestSuiteLoader();
    }

    /* access modifiers changed from: protected */
    public Class<?> loadSuiteClass(String suiteClassName) throws ClassNotFoundException {
        return Class.forName(suiteClassName);
    }

    /* access modifiers changed from: protected */
    public void clearStatus() {
    }

    /* access modifiers changed from: protected */
    public boolean useReloadingTestSuiteLoader() {
        return getPreference("loading").equals("true") && this.fLoading;
    }

    private static File getPreferencesFile() {
        return new File(System.getProperty("user.home"), "junit.properties");
    }

    private static void readPreferences() {
        InputStream is = null;
        try {
            is = new FileInputStream(getPreferencesFile());
            setPreferences(new Properties(getPreferences()));
            getPreferences().load(is);
        } catch (IOException e) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e2) {
                }
            }
        }
    }

    public static String getPreference(String key) {
        return getPreferences().getProperty(key);
    }

    public static int getPreference(String key, int dflt) {
        String value = getPreference(key);
        if (value == null) {
            return dflt;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return dflt;
        }
    }

    public static String getFilteredTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter));
        return getFilteredTrace(stringWriter.getBuffer().toString());
    }

    @Deprecated
    public static boolean inVAJava() {
        return false;
    }

    public static String getFilteredTrace(String stack) {
        if (showStackRaw()) {
            return stack;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        BufferedReader br = new BufferedReader(new StringReader(stack), 1000);
        while (true) {
            try {
                String line = br.readLine();
                if (line == null) {
                    return sw.toString();
                }
                if (!filterLine(line)) {
                    pw.println(line);
                }
            } catch (Exception e) {
                return stack;
            }
        }
    }

    protected static boolean showStackRaw() {
        return !getPreference("filterstack").equals("true") || !fgFilterStack;
    }

    static boolean filterLine(String line) {
        String[] patterns;
        for (String str : new String[]{"junit.framework.TestCase", "junit.framework.TestResult", "junit.framework.TestSuite", "junit.framework.Assert.", "junit.swingui.TestRunner", "junit.awtui.TestRunner", "junit.textui.TestRunner", "java.lang.reflect.Method.invoke("}) {
            if (line.indexOf(str) > 0) {
                return true;
            }
        }
        return false;
    }
}
