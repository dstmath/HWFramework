package ohos.global.icu.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.MissingResourceException;
import java.util.Properties;

public class ICUConfig {
    private static final Properties CONFIG_PROPS = new Properties();
    public static final String CONFIG_PROPS_FILE = "/ohos/global/icu/ICUConfig.properties";

    static {
        InputStream stream = ICUData.getStream(CONFIG_PROPS_FILE);
        if (stream != null) {
            try {
                CONFIG_PROPS.load(stream);
                stream.close();
            } catch (IOException | MissingResourceException unused) {
            } catch (Throwable th) {
                stream.close();
                throw th;
            }
        }
    }

    public static String get(String str) {
        return get(str, null);
    }

    public static String get(final String str, String str2) {
        String str3;
        if (System.getSecurityManager() != null) {
            try {
                str3 = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                    /* class ohos.global.icu.impl.ICUConfig.AnonymousClass1 */

                    @Override // java.security.PrivilegedAction
                    public String run() {
                        return System.getProperty(str);
                    }
                });
            } catch (AccessControlException unused) {
                str3 = null;
            }
        } else {
            str3 = System.getProperty(str);
        }
        return str3 == null ? CONFIG_PROPS.getProperty(str, str2) : str3;
    }
}
