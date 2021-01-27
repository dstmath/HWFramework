package org.bouncycastle.util;

import java.math.BigInteger;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class Properties {
    private static final ThreadLocal threadProperties = new ThreadLocal();

    private Properties() {
    }

    public static BigInteger asBigInteger(String str) {
        String propertyValue = getPropertyValue(str);
        if (propertyValue != null) {
            return new BigInteger(propertyValue);
        }
        return null;
    }

    public static Set<String> asKeySet(String str) {
        HashSet hashSet = new HashSet();
        String propertyValue = getPropertyValue(str);
        if (propertyValue != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(propertyValue, ",");
            while (stringTokenizer.hasMoreElements()) {
                hashSet.add(Strings.toLowerCase(stringTokenizer.nextToken()).trim());
            }
        }
        return Collections.unmodifiableSet(hashSet);
    }

    public static String getPropertyValue(final String str) {
        String str2;
        String str3 = (String) AccessController.doPrivileged(new PrivilegedAction() {
            /* class org.bouncycastle.util.Properties.AnonymousClass1 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return Security.getProperty(str);
            }
        });
        if (str3 != null) {
            return str3;
        }
        Map map = (Map) threadProperties.get();
        return (map == null || (str2 = (String) map.get(str)) == null) ? (String) AccessController.doPrivileged(new PrivilegedAction() {
            /* class org.bouncycastle.util.Properties.AnonymousClass2 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return System.getProperty(str);
            }
        }) : str2;
    }

    public static boolean isOverrideSet(String str) {
        try {
            return "true".equalsIgnoreCase(getPropertyValue(str));
        } catch (AccessControlException e) {
            return false;
        }
    }

    public static boolean removeThreadOverride(String str) {
        String str2;
        Map map = (Map) threadProperties.get();
        if (map == null || (str2 = (String) map.remove(str)) == null) {
            return false;
        }
        if (map.isEmpty()) {
            threadProperties.remove();
        }
        return "true".equalsIgnoreCase(str2);
    }

    public static boolean setThreadOverride(String str, boolean z) {
        boolean isOverrideSet = isOverrideSet(str);
        Map map = (Map) threadProperties.get();
        if (map == null) {
            map = new HashMap();
            threadProperties.set(map);
        }
        map.put(str, z ? "true" : "false");
        return isOverrideSet;
    }
}
