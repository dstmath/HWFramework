package java.util.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Permission;
import java.util.Iterator;
import java.util.ServiceLoader;

public abstract class Preferences {
    public static final int MAX_KEY_LENGTH = 80;
    public static final int MAX_NAME_LENGTH = 80;
    public static final int MAX_VALUE_LENGTH = 8192;
    private static PreferencesFactory factory = findPreferencesFactory();
    private static Permission prefsPerm = new RuntimePermission("preferences");

    public abstract String absolutePath();

    public abstract void addNodeChangeListener(NodeChangeListener nodeChangeListener);

    public abstract void addPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener);

    public abstract String[] childrenNames() throws BackingStoreException;

    public abstract void clear() throws BackingStoreException;

    public abstract void exportNode(OutputStream outputStream) throws IOException, BackingStoreException;

    public abstract void exportSubtree(OutputStream outputStream) throws IOException, BackingStoreException;

    public abstract void flush() throws BackingStoreException;

    public abstract String get(String str, String str2);

    public abstract boolean getBoolean(String str, boolean z);

    public abstract byte[] getByteArray(String str, byte[] bArr);

    public abstract double getDouble(String str, double d);

    public abstract float getFloat(String str, float f);

    public abstract int getInt(String str, int i);

    public abstract long getLong(String str, long j);

    public abstract boolean isUserNode();

    public abstract String[] keys() throws BackingStoreException;

    public abstract String name();

    public abstract Preferences node(String str);

    public abstract boolean nodeExists(String str) throws BackingStoreException;

    public abstract Preferences parent();

    public abstract void put(String str, String str2);

    public abstract void putBoolean(String str, boolean z);

    public abstract void putByteArray(String str, byte[] bArr);

    public abstract void putDouble(String str, double d);

    public abstract void putFloat(String str, float f);

    public abstract void putInt(String str, int i);

    public abstract void putLong(String str, long j);

    public abstract void remove(String str);

    public abstract void removeNode() throws BackingStoreException;

    public abstract void removeNodeChangeListener(NodeChangeListener nodeChangeListener);

    public abstract void removePreferenceChangeListener(PreferenceChangeListener preferenceChangeListener);

    public abstract void sync() throws BackingStoreException;

    public abstract String toString();

    private static PreferencesFactory findPreferencesFactory() {
        PreferencesFactory result = (PreferencesFactory) ServiceLoader.loadFromSystemProperty(PreferencesFactory.class);
        if (result != null) {
            return result;
        }
        Iterator impl$iterator = ServiceLoader.load(PreferencesFactory.class).iterator();
        if (impl$iterator.hasNext()) {
            return (PreferencesFactory) impl$iterator.next();
        }
        return new FileSystemPreferencesFactory();
    }

    public static PreferencesFactory setPreferencesFactory(PreferencesFactory pf) {
        PreferencesFactory previous = factory;
        factory = pf;
        return previous;
    }

    public static Preferences userNodeForPackage(Class<?> c) {
        return userRoot().node(nodeName(c));
    }

    public static Preferences systemNodeForPackage(Class<?> c) {
        return systemRoot().node(nodeName(c));
    }

    private static String nodeName(Class<?> c) {
        if (c.isArray()) {
            throw new IllegalArgumentException("Arrays have no associated preferences node.");
        }
        String className = c.getName();
        int pkgEndIndex = className.lastIndexOf(46);
        if (pkgEndIndex < 0) {
            return "/<unnamed>";
        }
        return "/" + className.substring(0, pkgEndIndex).replace('.', '/');
    }

    public static Preferences userRoot() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(prefsPerm);
        }
        return factory.userRoot();
    }

    public static Preferences systemRoot() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(prefsPerm);
        }
        return factory.systemRoot();
    }

    protected Preferences() {
    }

    public static void importPreferences(InputStream is) throws IOException, InvalidPreferencesFormatException {
        XmlSupport.importPreferences(is);
    }
}
