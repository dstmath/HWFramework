package android_maps_conflict_avoidance.com.google.common.util;

import java.util.Vector;

public class ArrayUtil {
    private ArrayUtil() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void copyIntoVector(Vector src, int srcIndex, Vector dest) {
        synchronized (dest) {
            int i = srcIndex;
            while (true) {
                if (i >= src.size()) {
                } else {
                    dest.insertElementAt(src.elementAt(i), i - srcIndex);
                    i++;
                }
            }
        }
    }
}
