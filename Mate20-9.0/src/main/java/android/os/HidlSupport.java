package android.os;

import android.annotation.SystemApi;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@SystemApi
public class HidlSupport {

    public static final class Mutable<E> {
        public E value;

        public Mutable() {
            this.value = null;
        }

        public Mutable(E value2) {
            this.value = value2;
        }
    }

    @SystemApi
    public static native int getPidIfSharable();

    @SystemApi
    public static boolean deepEquals(Object lft, Object rgt) {
        boolean z = true;
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
            if (lftArray.length != rgtArray.length || !IntStream.range(0, lftArray.length).allMatch(new IntPredicate(lftArray, rgtArray) {
                private final /* synthetic */ Object[] f$0;
                private final /* synthetic */ Object[] f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final boolean test(int i) {
                    return HidlSupport.deepEquals(this.f$0[i], this.f$1[i]);
                }
            })) {
                z = false;
            }
            return z;
        } else if (lft instanceof List) {
            List<Object> lftList = (List) lft;
            List<Object> rgtList = (List) rgt;
            if (lftList.size() != rgtList.size()) {
                return false;
            }
            return rgtList.stream().allMatch(new Predicate(lftList.iterator()) {
                private final /* synthetic */ Iterator f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return HidlSupport.deepEquals(this.f$0.next(), obj);
                }
            });
        } else {
            throwErrorIfUnsupportedType(lft);
            return lft.equals(rgt);
        }
    }

    @SystemApi
    public static int deepHashCode(Object o) {
        if (o == null) {
            return 0;
        }
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            Class<?> elementType = clazz.getComponentType();
            if (elementType == null || !elementType.isPrimitive()) {
                return Arrays.hashCode(Arrays.stream((Object[]) o).mapToInt($$Lambda$HidlSupport$GHxmwrIWiKN83tl6aMQt_nV5hiw.INSTANCE).toArray());
            }
            return primitiveArrayHashCode(o);
        } else if (o instanceof List) {
            return Arrays.hashCode(((List) o).stream().mapToInt($$Lambda$HidlSupport$CwwfmHPEvZaybUxpLzKdwrpQRfA.INSTANCE).toArray());
        } else {
            throwErrorIfUnsupportedType(o);
            return o.hashCode();
        }
    }

    private static void throwErrorIfUnsupportedType(Object o) {
        if ((o instanceof Collection) && !(o instanceof List)) {
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

    @SystemApi
    public static boolean interfacesEqual(IHwInterface lft, Object rgt) {
        if (lft == rgt) {
            return true;
        }
        if (lft == null || rgt == null || !(rgt instanceof IHwInterface)) {
            return false;
        }
        return Objects.equals(lft.asBinder(), ((IHwInterface) rgt).asBinder());
    }
}
