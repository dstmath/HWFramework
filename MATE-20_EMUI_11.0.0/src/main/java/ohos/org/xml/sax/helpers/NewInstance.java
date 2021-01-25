package ohos.org.xml.sax.helpers;

/* access modifiers changed from: package-private */
public class NewInstance {
    private static final String DEFAULT_PACKAGE = "com.sun.org.apache.xerces.internal";

    NewInstance() {
    }

    static Object newInstance(ClassLoader classLoader, String str) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> cls;
        boolean z = (System.getSecurityManager() == null || str == null || !str.startsWith(DEFAULT_PACKAGE)) ? false : true;
        if (classLoader == null || z) {
            cls = Class.forName(str);
        } else {
            cls = classLoader.loadClass(str);
        }
        return cls.newInstance();
    }
}
