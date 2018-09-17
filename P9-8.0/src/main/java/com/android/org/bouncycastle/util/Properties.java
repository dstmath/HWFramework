package com.android.org.bouncycastle.util;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Properties {
    public static boolean isOverrideSet(final String propertyName) {
        try {
            return "true".equals(AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    String value = System.getProperty(propertyName);
                    if (value == null) {
                        return null;
                    }
                    return Strings.toLowerCase(value);
                }
            }));
        } catch (AccessControlException e) {
            return false;
        }
    }
}
