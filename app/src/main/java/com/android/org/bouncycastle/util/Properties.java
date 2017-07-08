package com.android.org.bouncycastle.util;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Properties {

    /* renamed from: com.android.org.bouncycastle.util.Properties.1 */
    static class AnonymousClass1 implements PrivilegedAction {
        final /* synthetic */ String val$propertyName;

        AnonymousClass1(String val$propertyName) {
            this.val$propertyName = val$propertyName;
        }

        public Object run() {
            String value = System.getProperty(this.val$propertyName);
            if (value == null) {
                return null;
            }
            return Strings.toLowerCase(value);
        }
    }

    public static boolean isOverrideSet(String propertyName) {
        try {
            return "true".equals(AccessController.doPrivileged(new AnonymousClass1(propertyName)));
        } catch (AccessControlException e) {
            return false;
        }
    }
}
