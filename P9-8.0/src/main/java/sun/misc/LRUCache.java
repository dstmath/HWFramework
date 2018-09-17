package sun.misc;

public abstract class LRUCache<N, V> {
    private V[] oa = null;
    private final int size;

    protected abstract V create(N n);

    protected abstract boolean hasName(V v, N n);

    public LRUCache(int size) {
        this.size = size;
    }

    public static void moveToFront(Object[] oa, int i) {
        Object ob = oa[i];
        for (int j = i; j > 0; j--) {
            oa[j] = oa[j - 1];
        }
        oa[0] = ob;
    }

    public V forName(N name) {
        V ob;
        if (this.oa == null) {
            this.oa = new Object[this.size];
        } else {
            for (int i = 0; i < this.oa.length; i++) {
                ob = this.oa[i];
                if (ob != null && hasName(ob, name)) {
                    if (i > 0) {
                        moveToFront(this.oa, i);
                    }
                    return ob;
                }
            }
        }
        ob = create(name);
        this.oa[this.oa.length - 1] = ob;
        moveToFront(this.oa, this.oa.length - 1);
        return ob;
    }
}
