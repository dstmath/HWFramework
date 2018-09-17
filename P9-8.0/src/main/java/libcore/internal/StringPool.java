package libcore.internal;

public final class StringPool {
    private final String[] pool = new String[512];

    private static boolean contentEquals(String s, char[] chars, int start, int length) {
        if (s.length() != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (chars[start + i] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public String get(char[] array, int start, int length) {
        int hashCode = 0;
        for (int i = start; i < start + length; i++) {
            hashCode = (hashCode * 31) + array[i];
        }
        hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
        int index = (hashCode ^ ((hashCode >>> 7) ^ (hashCode >>> 4))) & (this.pool.length - 1);
        String pooled = this.pool[index];
        if (pooled != null && contentEquals(pooled, array, start, length)) {
            return pooled;
        }
        String result = new String(array, start, length);
        this.pool[index] = result;
        return result;
    }
}
