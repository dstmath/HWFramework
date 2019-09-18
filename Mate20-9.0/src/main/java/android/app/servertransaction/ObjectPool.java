package android.app.servertransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class ObjectPool {
    private static final int MAX_POOL_SIZE = 50;
    private static final Map<Class, ArrayList<? extends ObjectPoolItem>> sPoolMap = new HashMap();
    private static final Object sPoolSync = new Object();

    ObjectPool() {
    }

    public static <T extends ObjectPoolItem> T obtain(Class<T> itemClass) {
        synchronized (sPoolSync) {
            ArrayList<T> itemPool = sPoolMap.get(itemClass);
            if (itemPool == null || itemPool.isEmpty()) {
                return null;
            }
            T t = (ObjectPoolItem) itemPool.remove(itemPool.size() - 1);
            return t;
        }
    }

    public static <T extends ObjectPoolItem> void recycle(T item) {
        synchronized (sPoolSync) {
            ArrayList<T> itemPool = sPoolMap.get(item.getClass());
            if (itemPool == null) {
                itemPool = new ArrayList<>();
                sPoolMap.put(item.getClass(), itemPool);
            }
            int size = itemPool.size();
            int i = 0;
            while (i < size) {
                if (itemPool.get(i) != item) {
                    i++;
                } else {
                    throw new IllegalStateException("Trying to recycle already recycled item");
                }
            }
            if (size < 50) {
                itemPool.add(item);
            }
        }
    }
}
