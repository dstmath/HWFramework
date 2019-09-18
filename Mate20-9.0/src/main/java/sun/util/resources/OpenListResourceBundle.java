package sun.util.resources;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import sun.util.ResourceBundleEnumeration;

public abstract class OpenListResourceBundle extends ResourceBundle {
    private volatile Set<String> keyset;
    private volatile Map<String, Object> lookup = null;

    /* access modifiers changed from: protected */
    public abstract Object[][] getContents();

    protected OpenListResourceBundle() {
    }

    /* access modifiers changed from: protected */
    public Object handleGetObject(String key) {
        if (key != null) {
            loadLookupTablesIfNecessary();
            return this.lookup.get(key);
        }
        throw new NullPointerException();
    }

    public Enumeration<String> getKeys() {
        ResourceBundle parentBundle = this.parent;
        return new ResourceBundleEnumeration(handleKeySet(), parentBundle != null ? parentBundle.getKeys() : null);
    }

    /* access modifiers changed from: protected */
    public Set<String> handleKeySet() {
        loadLookupTablesIfNecessary();
        return this.lookup.keySet();
    }

    public Set<String> keySet() {
        if (this.keyset != null) {
            return this.keyset;
        }
        Set<String> ks = createSet();
        ks.addAll(handleKeySet());
        if (this.parent != null) {
            ks.addAll(this.parent.keySet());
        }
        synchronized (this) {
            if (this.keyset == null) {
                this.keyset = ks;
            }
        }
        return this.keyset;
    }

    /* access modifiers changed from: package-private */
    public void loadLookupTablesIfNecessary() {
        if (this.lookup == null) {
            loadLookup();
        }
    }

    private void loadLookup() {
        Object[][] contents = getContents();
        Map<String, Object> temp = createMap(contents.length);
        for (int i = 0; i < contents.length; i++) {
            String key = (String) contents[i][0];
            Object value = contents[i][1];
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            temp.put(key, value);
        }
        synchronized (this) {
            if (this.lookup == null) {
                this.lookup = temp;
            }
        }
    }

    /* access modifiers changed from: protected */
    public <K, V> Map<K, V> createMap(int size) {
        return new HashMap(size);
    }

    /* access modifiers changed from: protected */
    public <E> Set<E> createSet() {
        return new HashSet();
    }
}
