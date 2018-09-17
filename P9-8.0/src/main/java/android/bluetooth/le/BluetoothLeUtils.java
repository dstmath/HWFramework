package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.util.SparseArray;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class BluetoothLeUtils {
    static String toString(SparseArray<byte[]> array) {
        if (array == null) {
            return "null";
        }
        if (array.size() == 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        for (int i = 0; i < array.size(); i++) {
            buffer.append(array.keyAt(i)).append("=").append(Arrays.toString((byte[]) array.valueAt(i)));
        }
        buffer.append('}');
        return buffer.toString();
    }

    static <T> String toString(Map<T, byte[]> map) {
        if (map == null) {
            return "null";
        }
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        Iterator<Entry<T, byte[]>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Object key = ((Entry) it.next()).getKey();
            buffer.append(key).append("=").append(Arrays.toString((byte[]) map.get(key)));
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean equals(SparseArray<byte[]> array, SparseArray<byte[]> otherArray) {
        if (array == otherArray) {
            return true;
        }
        if (array == null || otherArray == null || array.size() != otherArray.size()) {
            return false;
        }
        int i = 0;
        while (i < array.size()) {
            if (array.keyAt(i) != otherArray.keyAt(i) || (Arrays.equals((byte[]) array.valueAt(i), (byte[]) otherArray.valueAt(i)) ^ 1) != 0) {
                return false;
            }
            i++;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static <T> boolean equals(Map<T, byte[]> map, Map<T, byte[]> otherMap) {
        if (map == otherMap) {
            return true;
        }
        if (map == null || otherMap == null || map.size() != otherMap.size()) {
            return false;
        }
        Set<T> keys = map.keySet();
        if (!keys.equals(otherMap.keySet())) {
            return false;
        }
        for (T key : keys) {
            if (!Objects.deepEquals(map.get(key), otherMap.get(key))) {
                return false;
            }
        }
        return true;
    }

    static void checkAdapterStateOn(BluetoothAdapter adapter) {
        if (adapter == null || (adapter.isLeEnabled() ^ 1) != 0) {
            throw new IllegalStateException("BT Adapter is not turned ON");
        }
    }
}
