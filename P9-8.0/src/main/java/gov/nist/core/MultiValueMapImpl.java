package gov.nist.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MultiValueMapImpl<V> implements MultiValueMap<String, V>, Cloneable {
    private static final long serialVersionUID = 4275505380960964605L;
    private HashMap<String, ArrayList<V>> map = new HashMap();

    public List<V> put(String key, V value) {
        ArrayList<V> keyList = (ArrayList) this.map.get(key);
        if (keyList == null) {
            keyList = new ArrayList(10);
            this.map.put(key, keyList);
        }
        keyList.add(value);
        return keyList;
    }

    public boolean containsValue(Object value) {
        Set<Entry> pairs = this.map.entrySet();
        if (pairs == null) {
            return false;
        }
        for (Entry keyValuePair : pairs) {
            if (((ArrayList) keyValuePair.getValue()).contains(value)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        for (Entry keyValuePair : this.map.entrySet()) {
            ((ArrayList) keyValuePair.getValue()).clear();
        }
        this.map.clear();
    }

    public Collection values() {
        ArrayList returnList = new ArrayList(this.map.size());
        for (Entry keyValuePair : this.map.entrySet()) {
            Object[] values = ((ArrayList) keyValuePair.getValue()).toArray();
            for (Object add : values) {
                returnList.add(add);
            }
        }
        return returnList;
    }

    public Object clone() {
        MultiValueMapImpl obj = new MultiValueMapImpl();
        obj.map = (HashMap) this.map.clone();
        return obj;
    }

    public int size() {
        return this.map.size();
    }

    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    public Set entrySet() {
        return this.map.entrySet();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    public List<V> get(Object key) {
        return (List) this.map.get(key);
    }

    public List<V> put(String key, List<V> value) {
        return (List) this.map.put(key, (ArrayList) value);
    }

    public List<V> remove(Object key) {
        return (List) this.map.remove(key);
    }

    public void putAll(Map<? extends String, ? extends List<V>> mapToPut) {
        for (String k : mapToPut.keySet()) {
            ArrayList<V> al = new ArrayList();
            al.addAll((Collection) mapToPut.get(k));
            this.map.put(k, al);
        }
    }
}
