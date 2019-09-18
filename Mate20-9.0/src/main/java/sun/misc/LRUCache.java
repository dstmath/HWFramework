package sun.misc;

public abstract class LRUCache<N, V> {
    private V[] oa = null;
    private final int size;

    /* access modifiers changed from: protected */
    public abstract V create(N n);

    /* access modifiers changed from: protected */
    public abstract boolean hasName(V v, N n);

    public LRUCache(int size2) {
        this.size = size2;
    }

    public static void moveToFront(Object[] oa2, int i) {
        Object ob = oa2[i];
        for (int j = i; j > 0; j--) {
            oa2[j] = oa2[j - 1];
        }
        oa2[0] = ob;
    }

    public V forName(N name) {
        if (this.oa == null) {
            this.oa = new Object[this.size];
        } else {
            for (int i = 0; i < this.oa.length; i++) {
                V ob = this.oa[i];
                if (ob != null && hasName(ob, name)) {
                    if (i > 0) {
                        moveToFront(this.oa, i);
                    }
                    return ob;
                }
            }
        }
        V ob2 = create(name);
        this.oa[this.oa.length - 1] = ob2;
        moveToFront(this.oa, this.oa.length - 1);
        return ob2;
    }
}
