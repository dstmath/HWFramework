package com.android.internal.util;

import android.util.ArraySet;
import dalvik.system.VMRuntime;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import libcore.util.EmptyArray;

public class ArrayUtils {
    private static final int CACHE_SIZE = 73;
    private static Object[] sCache = new Object[73];

    private ArrayUtils() {
    }

    public static byte[] newUnpaddedByteArray(int minLen) {
        return (byte[]) VMRuntime.getRuntime().newUnpaddedArray(Byte.TYPE, minLen);
    }

    public static char[] newUnpaddedCharArray(int minLen) {
        return (char[]) VMRuntime.getRuntime().newUnpaddedArray(Character.TYPE, minLen);
    }

    public static int[] newUnpaddedIntArray(int minLen) {
        return (int[]) VMRuntime.getRuntime().newUnpaddedArray(Integer.TYPE, minLen);
    }

    public static boolean[] newUnpaddedBooleanArray(int minLen) {
        return (boolean[]) VMRuntime.getRuntime().newUnpaddedArray(Boolean.TYPE, minLen);
    }

    public static long[] newUnpaddedLongArray(int minLen) {
        return (long[]) VMRuntime.getRuntime().newUnpaddedArray(Long.TYPE, minLen);
    }

    public static float[] newUnpaddedFloatArray(int minLen) {
        return (float[]) VMRuntime.getRuntime().newUnpaddedArray(Float.TYPE, minLen);
    }

    public static Object[] newUnpaddedObjectArray(int minLen) {
        return (Object[]) VMRuntime.getRuntime().newUnpaddedArray(Object.class, minLen);
    }

    public static <T> T[] newUnpaddedArray(Class<T> clazz, int minLen) {
        return (Object[]) VMRuntime.getRuntime().newUnpaddedArray(clazz, minLen);
    }

    public static boolean equals(byte[] array1, byte[] array2, int length) {
        if (length < 0) {
            throw new IllegalArgumentException();
        } else if (array1 == array2) {
            return true;
        } else {
            if (array1 == null || array2 == null || array1.length < length || array2.length < length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (array1[i] != array2[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    public static <T> T[] emptyArray(Class<T> kind) {
        if (kind == Object.class) {
            return EmptyArray.OBJECT;
        }
        int bucket = (kind.hashCode() & Integer.MAX_VALUE) % 73;
        Object cache = sCache[bucket];
        if (cache == null || cache.getClass().getComponentType() != kind) {
            cache = Array.newInstance(kind, 0);
            sCache[bucket] = cache;
        }
        return (Object[]) cache;
    }

    public static boolean isEmpty(Collection<?> array) {
        return array != null ? array.isEmpty() : true;
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(long[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(boolean[] array) {
        return array == null || array.length == 0;
    }

    public static int size(Object[] array) {
        return array == null ? 0 : array.length;
    }

    public static <T> boolean contains(T[] array, T value) {
        return indexOf(array, value) != -1;
    }

    public static <T> int indexOf(T[] array, T value) {
        if (array == null) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], value)) {
                return i;
            }
        }
        return -1;
    }

    public static <T> boolean containsAll(T[] array, T[] check) {
        if (check == null) {
            return true;
        }
        for (Object checkItem : check) {
            if (!contains((Object[]) array, checkItem)) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean containsAny(T[] array, T[] check) {
        if (check == null) {
            return false;
        }
        for (Object checkItem : check) {
            if (contains((Object[]) array, checkItem)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(int[] array, int value) {
        if (array == null) {
            return false;
        }
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(long[] array, long value) {
        if (array == null) {
            return false;
        }
        for (long element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(char[] array, char value) {
        if (array == null) {
            return false;
        }
        for (char element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean containsAll(char[] array, char[] check) {
        if (check == null) {
            return true;
        }
        for (char checkItem : check) {
            if (!contains(array, checkItem)) {
                return false;
            }
        }
        return true;
    }

    public static long total(long[] array) {
        long total = 0;
        if (array != null) {
            for (long value : array) {
                total += value;
            }
        }
        return total;
    }

    public static int[] convertToIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = ((Integer) list.get(i)).intValue();
        }
        return array;
    }

    public static <T> T[] appendElement(Class<T> kind, T[] array, T element) {
        return appendElement(kind, array, element, false);
    }

    public static <T> T[] appendElement(Class<T> kind, T[] array, T element, boolean allowDuplicates) {
        int end;
        T[] result;
        if (array == null) {
            end = 0;
            Object[] result2 = (Object[]) Array.newInstance(kind, 1);
        } else if (!allowDuplicates && contains((Object[]) array, (Object) element)) {
            return array;
        } else {
            end = array.length;
            result2 = (Object[]) Array.newInstance(kind, end + 1);
            System.arraycopy(array, 0, result2, 0, end);
        }
        result2[end] = element;
        return result2;
    }

    public static <T> T[] removeElement(Class<T> kind, T[] array, T element) {
        if (array == null || !contains((Object[]) array, (Object) element)) {
            return array;
        }
        int length = array.length;
        int i = 0;
        while (i < length) {
            if (!Objects.equals(array[i], element)) {
                i++;
            } else if (length == 1) {
                return null;
            } else {
                Object[] result = (Object[]) Array.newInstance(kind, length - 1);
                System.arraycopy(array, 0, result, 0, i);
                System.arraycopy(array, i + 1, result, i, (length - i) - 1);
                return result;
            }
        }
        return array;
    }

    public static int[] appendInt(int[] cur, int val, boolean allowDuplicates) {
        if (cur == null) {
            return new int[]{val};
        }
        if (!allowDuplicates) {
            for (int i : cur) {
                if (i == val) {
                    return cur;
                }
            }
        }
        int[] ret = new int[(N + 1)];
        System.arraycopy(cur, 0, ret, 0, N);
        ret[N] = val;
        return ret;
    }

    public static int[] appendInt(int[] cur, int val) {
        return appendInt(cur, val, false);
    }

    public static int[] removeInt(int[] cur, int val) {
        if (cur == null) {
            return null;
        }
        int N = cur.length;
        for (int i = 0; i < N; i++) {
            if (cur[i] == val) {
                int[] ret = new int[(N - 1)];
                if (i > 0) {
                    System.arraycopy(cur, 0, ret, 0, i);
                }
                if (i < N - 1) {
                    System.arraycopy(cur, i + 1, ret, i, (N - i) - 1);
                }
                return ret;
            }
        }
        return cur;
    }

    public static String[] removeString(String[] cur, String val) {
        if (cur == null) {
            return null;
        }
        int N = cur.length;
        for (int i = 0; i < N; i++) {
            if (Objects.equals(cur[i], val)) {
                String[] ret = new String[(N - 1)];
                if (i > 0) {
                    System.arraycopy(cur, 0, ret, 0, i);
                }
                if (i < N - 1) {
                    System.arraycopy(cur, i + 1, ret, i, (N - i) - 1);
                }
                return ret;
            }
        }
        return cur;
    }

    public static long[] appendLong(long[] cur, long val) {
        if (cur == null) {
            return new long[]{val};
        }
        for (long j : cur) {
            if (j == val) {
                return cur;
            }
        }
        long[] ret = new long[(N + 1)];
        System.arraycopy(cur, 0, ret, 0, N);
        ret[N] = val;
        return ret;
    }

    public static long[] removeLong(long[] cur, long val) {
        if (cur == null) {
            return null;
        }
        int N = cur.length;
        for (int i = 0; i < N; i++) {
            if (cur[i] == val) {
                long[] ret = new long[(N - 1)];
                if (i > 0) {
                    System.arraycopy(cur, 0, ret, 0, i);
                }
                if (i < N - 1) {
                    System.arraycopy(cur, i + 1, ret, i, (N - i) - 1);
                }
                return ret;
            }
        }
        return cur;
    }

    public static long[] cloneOrNull(long[] array) {
        return array != null ? (long[]) array.clone() : null;
    }

    public static <T> ArraySet<T> cloneOrNull(ArraySet<T> array) {
        return array != null ? new ArraySet((ArraySet) array) : null;
    }

    public static <T> ArraySet<T> add(ArraySet<T> cur, T val) {
        if (cur == null) {
            cur = new ArraySet();
        }
        cur.add(val);
        return cur;
    }

    public static <T> ArraySet<T> remove(ArraySet<T> cur, T val) {
        if (cur == null) {
            return null;
        }
        cur.remove(val);
        if (cur.isEmpty()) {
            return null;
        }
        return cur;
    }

    public static <T> ArrayList<T> add(ArrayList<T> cur, T val) {
        if (cur == null) {
            cur = new ArrayList();
        }
        cur.add(val);
        return cur;
    }

    public static <T> ArrayList<T> remove(ArrayList<T> cur, T val) {
        if (cur == null) {
            return null;
        }
        cur.remove(val);
        if (cur.isEmpty()) {
            return null;
        }
        return cur;
    }

    public static <T> boolean contains(Collection<T> cur, T val) {
        return cur != null ? cur.contains(val) : false;
    }

    public static <T> T[] trimToSize(T[] array, int size) {
        if (array == null || size == 0) {
            return null;
        }
        if (array.length == size) {
            return array;
        }
        return Arrays.copyOf(array, size);
    }

    public static <T> boolean referenceEquals(ArrayList<T> a, ArrayList<T> b) {
        if (a == b) {
            return true;
        }
        int sizeA = a.size();
        int sizeB = b.size();
        if (a == null || b == null || sizeA != sizeB) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < sizeA && (diff ^ 1) != 0; i++) {
            int i2;
            if (a.get(i) != b.get(i)) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            diff |= i2;
        }
        return diff ^ 1;
    }

    public static <T> int unstableRemoveIf(ArrayList<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return 0;
        }
        int size = collection.size();
        int leftIdx = 0;
        int rightIdx = size - 1;
        while (leftIdx <= rightIdx) {
            while (leftIdx < size && (predicate.test(collection.get(leftIdx)) ^ 1) != 0) {
                leftIdx++;
            }
            while (rightIdx > leftIdx && predicate.test(collection.get(rightIdx))) {
                rightIdx--;
            }
            if (leftIdx >= rightIdx) {
                break;
            }
            Collections.swap(collection, leftIdx, rightIdx);
            leftIdx++;
            rightIdx--;
        }
        for (int i = size - 1; i >= leftIdx; i--) {
            collection.remove(i);
        }
        return size - leftIdx;
    }

    public static String[] defeatNullable(String[] val) {
        return val != null ? val : EmptyArray.STRING;
    }
}
