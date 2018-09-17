package sun.util.resources;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import sun.util.ResourceBundleEnumeration;

public abstract class OpenListResourceBundle extends ResourceBundle {
    private Map lookup = null;

    protected abstract Object[][] getContents();

    protected OpenListResourceBundle() {
    }

    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        loadLookupTablesIfNecessary();
        return this.lookup.get(key);
    }

    public Enumeration<String> getKeys() {
        Enumeration enumeration = null;
        ResourceBundle parent = this.parent;
        Set handleGetKeys = handleGetKeys();
        if (parent != null) {
            enumeration = parent.getKeys();
        }
        return new ResourceBundleEnumeration(handleGetKeys, enumeration);
    }

    public Set<String> handleGetKeys() {
        loadLookupTablesIfNecessary();
        return this.lookup.keySet();
    }

    public OpenListResourceBundle getParent() {
        return (OpenListResourceBundle) this.parent;
    }

    void loadLookupTablesIfNecessary() {
        if (this.lookup == null) {
            loadLookup();
        }
    }

    private synchronized void loadLookup() {
        if (this.lookup == null) {
            Object[][] contents = getContents();
            Map temp = createMap(contents.length);
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

    protected Map createMap(int size) {
        return new HashMap(size);
    }
}
