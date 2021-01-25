package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MultiHashtable<K, V> {
    static final long serialVersionUID = -6151608290510033572L;
    private final Map<K, Set<V>> map = new HashMap();
    private boolean modifiable = true;

    public Set<V> put(K k, V v) {
        if (this.modifiable) {
            Set<V> set = this.map.get(k);
            if (set == null) {
                set = new HashSet<>();
                this.map.put(k, set);
            }
            set.add(v);
            return set;
        }
        throw new UnsupportedOperationException("The MultiHashtable instance is not modifiable.");
    }

    public V maps(K k, V v) {
        Set<V> set;
        if (!(k == null || (set = this.map.get(k)) == null)) {
            for (V v2 : set) {
                if (v2.equals(v)) {
                    return v2;
                }
            }
        }
        return null;
    }

    public void makeUnmodifiable() {
        this.modifiable = false;
    }
}
