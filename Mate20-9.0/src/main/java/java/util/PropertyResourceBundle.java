package java.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import sun.util.ResourceBundleEnumeration;

public class PropertyResourceBundle extends ResourceBundle {
    private final Map<String, Object> lookup;

    public PropertyResourceBundle(InputStream stream) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);
        this.lookup = new HashMap(properties);
    }

    public PropertyResourceBundle(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        this.lookup = new HashMap(properties);
    }

    public Object handleGetObject(String key) {
        if (key != null) {
            return this.lookup.get(key);
        }
        throw new NullPointerException();
    }

    public Enumeration<String> getKeys() {
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(this.lookup.keySet(), parent != null ? parent.getKeys() : null);
    }

    /* access modifiers changed from: protected */
    public Set<String> handleKeySet() {
        return this.lookup.keySet();
    }
}
