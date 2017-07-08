package sun.security.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ResourceBundle;

public class ResourcesMgr {
    private static ResourceBundle altBundle;
    private static ResourceBundle bundle;

    /* renamed from: sun.security.util.ResourcesMgr.2 */
    static class AnonymousClass2 implements PrivilegedAction<ResourceBundle> {
        final /* synthetic */ String val$altBundleName;

        AnonymousClass2(String val$altBundleName) {
            this.val$altBundleName = val$altBundleName;
        }

        public ResourceBundle run() {
            return ResourceBundle.getBundle(this.val$altBundleName);
        }
    }

    public static String getString(String s) {
        if (bundle == null) {
            bundle = (ResourceBundle) AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>() {
                public ResourceBundle run() {
                    return ResourceBundle.getBundle(Resources.class.getName());
                }
            });
        }
        return bundle.getString(s);
    }

    public static String getString(String s, String altBundleName) {
        if (altBundle == null) {
            altBundle = (ResourceBundle) AccessController.doPrivileged(new AnonymousClass2(altBundleName));
        }
        return altBundle.getString(s);
    }
}
