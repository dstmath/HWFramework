package android.net.lowpan;

import java.util.Map;

public abstract class LowpanProperty<T> {
    public abstract String getName();

    public abstract Class<T> getType();

    public void putInMap(Map map, T value) {
        map.put(getName(), value);
    }

    public T getFromMap(Map map) {
        return map.get(getName());
    }
}
