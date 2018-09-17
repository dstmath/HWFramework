package android.os;

import android.os.-$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA.AnonymousClass2;
import android.os.-$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA.AnonymousClass3;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public class HidlSupport {
    public static boolean deepEquals(Object lft, Object rgt) {
        boolean z = false;
        if (lft == rgt) {
            return true;
        }
        if (lft == null || rgt == null) {
            return false;
        }
        Class<?> lftClazz = lft.getClass();
        Class<?> rgtClazz = rgt.getClass();
        if (lftClazz != rgtClazz) {
            return false;
        }
        if (lftClazz.isArray()) {
            Class<?> lftElementType = lftClazz.getComponentType();
            if (lftElementType != rgtClazz.getComponentType()) {
                return false;
            }
            if (lftElementType != null && lftElementType.isPrimitive()) {
                return Objects.deepEquals(lft, rgt);
            }
            Object[] lftArray = (Object[]) lft;
            Object[] rgtArray = (Object[]) rgt;
            if (lftArray.length == rgtArray.length) {
                z = IntStream.range(0, lftArray.length).allMatch(new AnonymousClass3(lftArray, rgtArray));
            }
            return z;
        } else if (lft instanceof List) {
            List<Object> lftList = (List) lft;
            List<Object> rgtList = (List) rgt;
            if (lftList.size() != rgtList.size()) {
                return false;
            }
            return rgtList.stream().allMatch(new AnonymousClass2(lftList.iterator()));
        } else {
            throwErrorIfUnsupportedType(lft);
            return lft.equals(rgt);
        }
    }

    public static int deepHashCode(Object o) {
        if (o == null) {
            return 0;
        }
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            Class<?> elementType = clazz.getComponentType();
            if (elementType == null || !elementType.isPrimitive()) {
                return Arrays.hashCode(Arrays.stream((Object[]) o).mapToInt(new -$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA()).toArray());
            }
            return primitiveArrayHashCode(o);
        } else if (o instanceof List) {
            return Arrays.hashCode(((List) o).stream().mapToInt(new ToIntFunction() {
                public final int applyAsInt(Object obj) {
                    return $m$0(obj);
                }
            }).toArray());
        } else {
            throwErrorIfUnsupportedType(o);
            return o.hashCode();
        }
    }

    private static void throwErrorIfUnsupportedType(Object o) {
        if ((o instanceof Collection) && ((o instanceof List) ^ 1) != 0) {
            throw new UnsupportedOperationException("Cannot check equality on collections other than lists: " + o.getClass().getName());
        } else if (o instanceof Map) {
            throw new UnsupportedOperationException("Cannot check equality on maps");
        }
    }

    private static int primitiveArrayHashCode(Object o) {
        Class<?> elementType = o.getClass().getComponentType();
        if (elementType == Boolean.TYPE) {
            return Arrays.hashCode((boolean[]) o);
        }
        if (elementType == Byte.TYPE) {
            return Arrays.hashCode((byte[]) o);
        }
        if (elementType == Character.TYPE) {
            return Arrays.hashCode((char[]) o);
        }
        if (elementType == Double.TYPE) {
            return Arrays.hashCode((double[]) o);
        }
        if (elementType == Float.TYPE) {
            return Arrays.hashCode((float[]) o);
        }
        if (elementType == Integer.TYPE) {
            return Arrays.hashCode((int[]) o);
        }
        if (elementType == Long.TYPE) {
            return Arrays.hashCode((long[]) o);
        }
        if (elementType == Short.TYPE) {
            return Arrays.hashCode((short[]) o);
        }
        throw new UnsupportedOperationException();
    }
}
