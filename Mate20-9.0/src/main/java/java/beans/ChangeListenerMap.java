package java.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class ChangeListenerMap<L extends EventListener> {
    private Map<String, L[]> map;

    public abstract L extract(L l);

    /* access modifiers changed from: protected */
    public abstract L[] newArray(int i);

    /* access modifiers changed from: protected */
    public abstract L newProxy(String str, L l);

    ChangeListenerMap() {
    }

    public final synchronized void add(String name, L listener) {
        int size;
        if (this.map == null) {
            this.map = new HashMap();
        }
        L[] array = (EventListener[]) this.map.get(name);
        if (array != null) {
            size = array.length;
        } else {
            size = 0;
        }
        L[] clone = newArray(size + 1);
        clone[size] = listener;
        if (array != null) {
            System.arraycopy((Object) array, 0, (Object) clone, 0, size);
        }
        this.map.put(name, clone);
    }

    public final synchronized void remove(String name, L listener) {
        if (this.map != null) {
            L[] array = (EventListener[]) this.map.get(name);
            if (array != null) {
                int i = 0;
                while (true) {
                    if (i >= array.length) {
                        break;
                    } else if (listener.equals(array[i])) {
                        int size = array.length - 1;
                        if (size > 0) {
                            L[] clone = newArray(size);
                            System.arraycopy((Object) array, 0, (Object) clone, 0, i);
                            System.arraycopy((Object) array, i + 1, (Object) clone, i, size - i);
                            this.map.put(name, clone);
                        } else {
                            this.map.remove(name);
                            if (this.map.isEmpty()) {
                                this.map = null;
                            }
                        }
                    } else {
                        i++;
                    }
                }
            }
        }
    }

    public final synchronized L[] get(String name) {
        L[] lArr;
        if (this.map != null) {
            lArr = (EventListener[]) this.map.get(name);
        } else {
            lArr = null;
        }
        return lArr;
    }

    public final void set(String name, L[] listeners) {
        if (listeners != null) {
            if (this.map == null) {
                this.map = new HashMap();
            }
            this.map.put(name, listeners);
        } else if (this.map != null) {
            this.map.remove(name);
            if (this.map.isEmpty()) {
                this.map = null;
            }
        }
    }

    public final synchronized L[] getListeners() {
        if (this.map == null) {
            return newArray(0);
        }
        List<L> list = new ArrayList<>();
        L[] listeners = (EventListener[]) this.map.get(null);
        if (listeners != null) {
            for (L listener : listeners) {
                list.add(listener);
            }
        }
        for (Map.Entry<String, L[]> entry : this.map.entrySet()) {
            String name = entry.getKey();
            if (name != null) {
                for (L listener2 : (EventListener[]) entry.getValue()) {
                    list.add(newProxy(name, listener2));
                }
            }
        }
        return (EventListener[]) list.toArray(newArray(list.size()));
    }

    public final L[] getListeners(String name) {
        if (name != null) {
            L[] listeners = get(name);
            if (listeners != null) {
                return (EventListener[]) listeners.clone();
            }
        }
        return newArray(0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        return r1;
     */
    public final synchronized boolean hasListeners(String name) {
        boolean z = false;
        if (this.map == null) {
            return false;
        }
        if (!(((EventListener[]) this.map.get(null)) == null && (name == null || this.map.get(name) == null))) {
            z = true;
        }
    }

    public final Set<Map.Entry<String, L[]>> getEntries() {
        if (this.map != null) {
            return this.map.entrySet();
        }
        return Collections.emptySet();
    }
}
