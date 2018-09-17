package java.util;

import sun.util.ResourceBundleEnumeration;

public abstract class ListResourceBundle extends ResourceBundle {
    private volatile Map<String, Object> lookup = null;

    protected abstract Object[][] getContents();

    public final Object handleGetObject(String key) {
        if (this.lookup == null) {
            loadLookup();
        }
        if (key != null) {
            return this.lookup.get(key);
        }
        throw new NullPointerException();
    }

    public Enumeration<String> getKeys() {
        Enumeration enumeration = null;
        if (this.lookup == null) {
            loadLookup();
        }
        ResourceBundle parent = this.parent;
        Set keySet = this.lookup.keySet();
        if (parent != null) {
            enumeration = parent.getKeys();
        }
        return new ResourceBundleEnumeration(keySet, enumeration);
    }

    protected Set<String> handleKeySet() {
        if (this.lookup == null) {
            loadLookup();
        }
        return this.lookup.keySet();
    }

    private synchronized void loadLookup() {
        if (this.lookup == null) {
            Object[][] contents = getContents();
            HashMap<String, Object> temp = new HashMap(contents.length);
            for (int i = 0; i < contents.length; i++) {
                String key = contents[i][0];
                Object value = contents[i][1];
                if (key == null || value == null) {
                    throw new NullPointerException();
                }
                temp.put(key, value);
            }
            this.lookup = temp;
        }
    }
}
