package gov.nist.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public class DuplicateNameValueList implements Serializable, Cloneable {
    private static final long serialVersionUID = -5611332957903796952L;
    private MultiValueMapImpl<NameValue> nameValueMap = new MultiValueMapImpl<>();
    private String separator = Separators.SEMICOLON;

    public void setSeparator(String separator2) {
        this.separator = separator2;
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        if (!this.nameValueMap.isEmpty()) {
            Iterator<NameValue> iterator = this.nameValueMap.values().iterator();
            if (iterator.hasNext()) {
                while (true) {
                    NameValue gobj = iterator.next();
                    if (gobj instanceof GenericObject) {
                        gobj.encode(buffer);
                    } else {
                        buffer.append(gobj.toString());
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
        this.nameValueMap.put(nv.getName().toLowerCase(), nv);
    }

    public void set(String name, Object value) {
        this.nameValueMap.put(name.toLowerCase(), new NameValue(name, value));
    }

    public boolean equals(Object otherObject) {
        if (otherObject == null || !otherObject.getClass().equals(getClass())) {
            return false;
        }
        DuplicateNameValueList other = (DuplicateNameValueList) otherObject;
        if (this.nameValueMap.size() != other.nameValueMap.size()) {
            return false;
        }
        for (String key : this.nameValueMap.keySet()) {
            Collection nv1 = getNameValue(key);
            Collection nv2 = other.nameValueMap.get((Object) key);
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
        Collection nv = getNameValue(name.toLowerCase());
        if (nv != null) {
            return nv;
        }
        return null;
    }

    public Collection getNameValue(String name) {
        return this.nameValueMap.get((Object) name.toLowerCase());
    }

    public boolean hasNameValue(String name) {
        return this.nameValueMap.containsKey(name.toLowerCase());
    }

    public boolean delete(String name) {
        String lcName = name.toLowerCase();
        if (!this.nameValueMap.containsKey(lcName)) {
            return false;
        }
        this.nameValueMap.remove((Object) lcName);
        return true;
    }

    public Object clone() {
        DuplicateNameValueList retval = new DuplicateNameValueList();
        retval.setSeparator(this.separator);
        for (NameValue clone : this.nameValueMap.values()) {
            retval.set((NameValue) clone.clone());
        }
        return retval;
    }

    public Iterator<NameValue> iterator() {
        return this.nameValueMap.values().iterator();
    }

    public Iterator<String> getNames() {
        return this.nameValueMap.keySet().iterator();
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
        this.nameValueMap.clear();
    }

    public boolean isEmpty() {
        return this.nameValueMap.isEmpty();
    }

    public NameValue put(String key, NameValue value) {
        return (NameValue) this.nameValueMap.put(key, value);
    }

    public NameValue remove(Object key) {
        return (NameValue) this.nameValueMap.remove(key);
    }

    public int size() {
        return this.nameValueMap.size();
    }

    public Collection<NameValue> values() {
        return this.nameValueMap.values();
    }

    public int hashCode() {
        return this.nameValueMap.keySet().hashCode();
    }
}
