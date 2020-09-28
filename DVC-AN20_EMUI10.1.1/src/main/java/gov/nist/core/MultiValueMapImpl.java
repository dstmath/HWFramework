package gov.nist.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiValueMapImpl<V> implements MultiValueMap<String, V>, Cloneable {
    private static final long serialVersionUID = 4275505380960964605L;
    private HashMap<String, ArrayList<V>> map = new HashMap<>();

    @Override // java.util.Map
    public /* bridge */ /* synthetic */ Object put(Object obj, Object obj2) {
        return put((String) obj, (List) ((List) obj2));
    }

    public List<V> put(String key, V value) {
        ArrayList<V> keyList = this.map.get(key);
        if (keyList == null) {
            keyList = new ArrayList<>(10);
            this.map.put(key, keyList);
        }
        keyList.add(value);
        return keyList;
    }

    public boolean containsValue(Object value) {
        Set<Map.Entry<String, ArrayList<V>>> pairs = this.map.entrySet();
        if (pairs == null) {
            return false;
        }
        for (Map.Entry<String, ArrayList<V>> keyValuePair : pairs) {
            if (keyValuePair.getValue().contains(value)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        for (Map.Entry<String, ArrayList<V>> keyValuePair : this.map.entrySet()) {
            keyValuePair.getValue().clear();
        }
        this.map.clear();
    }

    @Override // java.util.Map
    public Collection values() {
        Object[] values;
        ArrayList returnList = new ArrayList(this.map.size());
        for (Map.Entry<String, ArrayList<V>> keyValuePair : this.map.entrySet()) {
            for (Object obj : keyValuePair.getValue().toArray()) {
                returnList.add(obj);
            }
        }
        return returnList;
    }

    @Override // java.lang.Object
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

    @Override // java.util.Map
    public Set entrySet() {
        return this.map.entrySet();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override // java.util.Map
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @Override // java.util.Map
    public List<V> get(Object key) {
        return this.map.get(key);
    }

    public List<V> put(String key, List<V> value) {
        return this.map.put(key, (ArrayList) value);
    }

    @Override // java.util.Map
    public List<V> remove(Object key) {
        return this.map.remove(key);
    }

    @Override // java.util.Map
    public void putAll(Map<? extends String, ? extends List<V>> mapToPut) {
        for (String k : mapToPut.keySet()) {
            ArrayList<V> al = new ArrayList<>();
            al.addAll((Collection) mapToPut.get(k));
            this.map.put(k, al);
        }
    }
}
