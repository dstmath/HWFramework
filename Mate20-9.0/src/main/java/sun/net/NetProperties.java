package sun.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

public class NetProperties {
    private static Properties props = new Properties();

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                NetProperties.loadDefaultProperties();
                return null;
            }
        });
    }

    private NetProperties() {
    }

    /* access modifiers changed from: private */
    public static void loadDefaultProperties() {
        String fname = System.getProperty("java.home");
        if (fname != null) {
            try {
                BufferedInputStream bin = new BufferedInputStream(new FileInputStream(new File(new File(fname, "lib"), "net.properties").getCanonicalPath()));
                props.load((InputStream) bin);
                bin.close();
            } catch (Exception e) {
            }
        } else {
            throw new Error("Can't find java.home ??");
        }
    }

    public static String get(String key) {
        try {
            return System.getProperty(key, props.getProperty(key));
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public static Integer getInteger(String key, int defval) {
        String val = null;
        try {
            val = System.getProperty(key, props.getProperty(key));
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        if (val != null) {
            try {
                return Integer.decode(val);
            } catch (NumberFormatException e2) {
            }
        }
        return new Integer(defval);
    }

    public static Boolean getBoolean(String key) {
        String val = null;
        try {
            val = System.getProperty(key, props.getProperty(key));
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        if (val != null) {
            try {
                return Boolean.valueOf(val);
            } catch (NumberFormatException e2) {
            }
        }
        return null;
    }
}
