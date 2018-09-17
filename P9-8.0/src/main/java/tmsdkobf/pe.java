package tmsdkobf;

import java.util.LinkedHashMap;
import java.util.Set;

public class pe<K, V> {
    private int AY = -1;
    private LinkedHashMap<K, V> Jr = new LinkedHashMap();

    public pe(int i) {
        this.AY = i;
    }

    public void f(K k) {
        this.Jr.remove(k);
    }

    public V get(K k) {
        return this.Jr.get(k);
    }

    public LinkedHashMap<K, V> hH() {
        return this.Jr;
    }

    public V put(K k, V v) {
        if (this.Jr.size() >= this.AY) {
            Set keySet = this.Jr.keySet();
            if (keySet != null) {
                this.Jr.remove(keySet.iterator().next());
            }
        }
        return this.Jr.put(k, v);
    }

    public int size() {
        return this.Jr.size();
    }
}
