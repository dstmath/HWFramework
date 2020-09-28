package gov.nist.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
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

    public void setSeparator(String separator2) {
        this.separator = separator2;
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

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0035  */
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
            NameValue nv2 = other.hmap.get(key);
            if (nv2 == null || !nv2.equals(nv1)) {
                return false;
            }
            while (li.hasNext()) {
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
        return this.hmap.get(name.toLowerCase());
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

    @Override // java.lang.Object
    public Object clone() {
        NameValueList retval = new NameValueList();
        retval.setSeparator(this.separator);
        for (NameValue nameValue : this.hmap.values()) {
            retval.set((NameValue) nameValue.clone());
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

    @Override // java.util.Map
    public Set<Map.Entry<String, NameValue>> entrySet() {
        return this.hmap.entrySet();
    }

    @Override // java.util.Map
    public NameValue get(Object key) {
        return this.hmap.get(key.toString().toLowerCase());
    }

    @Override // java.util.Map
    public Set<String> keySet() {
        return this.hmap.keySet();
    }

    public NameValue put(String name, NameValue nameValue) {
        return this.hmap.put(name, nameValue);
    }

    @Override // java.util.Map
    public void putAll(Map<? extends String, ? extends NameValue> map) {
        this.hmap.putAll(map);
    }

    @Override // java.util.Map
    public NameValue remove(Object key) {
        return this.hmap.remove(key.toString().toLowerCase());
    }

    @Override // java.util.Map
    public Collection<NameValue> values() {
        return this.hmap.values();
    }

    public int hashCode() {
        return this.hmap.keySet().hashCode();
    }
}
