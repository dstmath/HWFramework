package gov.nist.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NameValueList implements Serializable, Cloneable, Map<String, NameValue> {
    private static final long serialVersionUID = -6998271876574260243L;
    private Map<String, NameValue> hmap;
    private String separator;

    public NameValueList() {
        this.separator = Separators.SEMICOLON;
        this.hmap = new LinkedHashMap();
    }

    public NameValueList(boolean sync) {
        this.separator = Separators.SEMICOLON;
        if (sync) {
            this.hmap = new ConcurrentHashMap();
        } else {
            this.hmap = new LinkedHashMap();
        }
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        if (!this.hmap.isEmpty()) {
            Iterator<NameValue> iterator = this.hmap.values().iterator();
            if (iterator.hasNext()) {
                while (true) {
                    GenericObject obj = iterator.next();
                    if (obj instanceof GenericObject) {
                        obj.encode(buffer);
                    } else {
                        buffer.append(obj.toString());
                    }
                    if (!iterator.hasNext()) {
                        break;
                    }
                    buffer.append(this.separator);
                }
            }
        }
        return buffer;
    }

    public String toString() {
        return encode();
    }

    public void set(NameValue nv) {
        this.hmap.put(nv.getName().toLowerCase(), nv);
    }

    public void set(String name, Object value) {
        this.hmap.put(name.toLowerCase(), new NameValue(name, value));
    }

    public boolean equals(Object otherObject) {
        if (otherObject == null || !otherObject.getClass().equals(getClass())) {
            return false;
        }
        NameValueList other = (NameValueList) otherObject;
        if (this.hmap.size() != other.hmap.size()) {
            return false;
        }
        for (String key : this.hmap.keySet()) {
            NameValue nv1 = getNameValue(key);
            NameValue nv2 = (NameValue) other.hmap.get(key);
            if (nv2 == null) {
                return false;
            }
            if (!nv2.equals(nv1)) {
                return false;
            }
        }
        return true;
    }

    public Object getValue(String name) {
        NameValue nv = getNameValue(name.toLowerCase());
        if (nv != null) {
            return nv.getValueAsObject();
        }
        return null;
    }

    public NameValue getNameValue(String name) {
        return (NameValue) this.hmap.get(name.toLowerCase());
    }

    public boolean hasNameValue(String name) {
        return this.hmap.containsKey(name.toLowerCase());
    }

    public boolean delete(String name) {
        String lcName = name.toLowerCase();
        if (!this.hmap.containsKey(lcName)) {
            return false;
        }
        this.hmap.remove(lcName);
        return true;
    }

    public Object clone() {
        NameValueList retval = new NameValueList();
        retval.setSeparator(this.separator);
        for (NameValue clone : this.hmap.values()) {
            retval.set((NameValue) clone.clone());
        }
        return retval;
    }

    public int size() {
        return this.hmap.size();
    }

    public boolean isEmpty() {
        return this.hmap.isEmpty();
    }

    public Iterator<NameValue> iterator() {
        return this.hmap.values().iterator();
    }

    public Iterator<String> getNames() {
        return this.hmap.keySet().iterator();
    }

    public String getParameter(String name) {
        Object val = getValue(name);
        if (val == null) {
            return null;
        }
        if (val instanceof GenericObject) {
            return ((GenericObject) val).encode();
        }
        return val.toString();
    }

    public void clear() {
        this.hmap.clear();
    }

    public boolean containsKey(Object key) {
        return this.hmap.containsKey(key.toString().toLowerCase());
    }

    public boolean containsValue(Object value) {
        return this.hmap.containsValue(value);
    }

    public Set<Entry<String, NameValue>> entrySet() {
        return this.hmap.entrySet();
    }

    public NameValue get(Object key) {
        return (NameValue) this.hmap.get(key.toString().toLowerCase());
    }

    public Set<String> keySet() {
        return this.hmap.keySet();
    }

    public NameValue put(String name, NameValue nameValue) {
        return (NameValue) this.hmap.put(name, nameValue);
    }

    public void putAll(Map<? extends String, ? extends NameValue> map) {
        this.hmap.putAll(map);
    }

    public NameValue remove(Object key) {
        return (NameValue) this.hmap.remove(key.toString().toLowerCase());
    }

    public Collection<NameValue> values() {
        return this.hmap.values();
    }

    public int hashCode() {
        return this.hmap.keySet().hashCode();
    }
}
