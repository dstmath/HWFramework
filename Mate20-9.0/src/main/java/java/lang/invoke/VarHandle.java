package java.lang.invoke;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import sun.misc.Unsafe;

public abstract class VarHandle {
    private static final int ALL_MODES_BIT_MASK = ((((READ_ACCESS_MODES_BIT_MASK | WRITE_ACCESS_MODES_BIT_MASK) | ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK) | NUMERIC_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK) | BITWISE_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK);
    private static final int ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK = accessTypesToBitMask(EnumSet.of(AccessType.COMPARE_AND_EXCHANGE, AccessType.COMPARE_AND_SWAP, AccessType.GET_AND_UPDATE));
    private static final int BITWISE_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK = accessTypesToBitMask(EnumSet.of(AccessType.GET_AND_UPDATE_BITWISE));
    private static final int NUMERIC_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK = accessTypesToBitMask(EnumSet.of(AccessType.GET_AND_UPDATE_NUMERIC));
    private static final int READ_ACCESS_MODES_BIT_MASK = accessTypesToBitMask(EnumSet.of(AccessType.GET));
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private static final int WRITE_ACCESS_MODES_BIT_MASK = accessTypesToBitMask(EnumSet.of(AccessType.SET));
    private final int accessModesBitMask;
    private final Class<?> coordinateType0;
    private final Class<?> coordinateType1;
    private final Class<?> varType;

    public enum AccessMode {
        GET("get", AccessType.GET),
        SET("set", AccessType.SET),
        GET_VOLATILE("getVolatile", AccessType.GET),
        SET_VOLATILE("setVolatile", AccessType.SET),
        GET_ACQUIRE("getAcquire", AccessType.GET),
        SET_RELEASE("setRelease", AccessType.SET),
        GET_OPAQUE("getOpaque", AccessType.GET),
        SET_OPAQUE("setOpaque", AccessType.SET),
        COMPARE_AND_SET("compareAndSet", AccessType.COMPARE_AND_SWAP),
        COMPARE_AND_EXCHANGE("compareAndExchange", AccessType.COMPARE_AND_EXCHANGE),
        COMPARE_AND_EXCHANGE_ACQUIRE("compareAndExchangeAcquire", AccessType.COMPARE_AND_EXCHANGE),
        COMPARE_AND_EXCHANGE_RELEASE("compareAndExchangeRelease", AccessType.COMPARE_AND_EXCHANGE),
        WEAK_COMPARE_AND_SET_PLAIN("weakCompareAndSetPlain", AccessType.COMPARE_AND_SWAP),
        WEAK_COMPARE_AND_SET("weakCompareAndSet", AccessType.COMPARE_AND_SWAP),
        WEAK_COMPARE_AND_SET_ACQUIRE("weakCompareAndSetAcquire", AccessType.COMPARE_AND_SWAP),
        WEAK_COMPARE_AND_SET_RELEASE("weakCompareAndSetRelease", AccessType.COMPARE_AND_SWAP),
        GET_AND_SET("getAndSet", AccessType.GET_AND_UPDATE),
        GET_AND_SET_ACQUIRE("getAndSetAcquire", AccessType.GET_AND_UPDATE),
        GET_AND_SET_RELEASE("getAndSetRelease", AccessType.GET_AND_UPDATE),
        GET_AND_ADD("getAndAdd", AccessType.GET_AND_UPDATE_NUMERIC),
        GET_AND_ADD_ACQUIRE("getAndAddAcquire", AccessType.GET_AND_UPDATE_NUMERIC),
        GET_AND_ADD_RELEASE("getAndAddRelease", AccessType.GET_AND_UPDATE_NUMERIC),
        GET_AND_BITWISE_OR("getAndBitwiseOr", AccessType.GET_AND_UPDATE_BITWISE),
        GET_AND_BITWISE_OR_RELEASE("getAndBitwiseOrRelease", AccessType.GET_AND_UPDATE_BITWISE),
        GET_AND_BITWISE_OR_ACQUIRE("getAndBitwiseOrAcquire", AccessType.GET_AND_UPDATE_BITWISE),
        GET_AND_BITWISE_AND("getAndBitwiseAnd", AccessType.GET_AND_UPDATE_BITWISE),
        GET_AND_BITWISE_AND_RELEASE("getAndBitwiseAndRelease", AccessType.GET_AND_UPDATE_BITWISE),
        GET_AND_BITWISE_AND_ACQUIRE("getAndBitwiseAndAcquire", AccessType.GET_AND_UPDATE_BITWISE),
        GET_AND_BITWISE_XOR("getAndBitwiseXor", AccessType.GET_AND_UPDATE_BITWISE),
        GET_AND_BITWISE_XOR_RELEASE("getAndBitwiseXorRelease", AccessType.GET_AND_UPDATE_BITWISE),
        GET_AND_BITWISE_XOR_ACQUIRE("getAndBitwiseXorAcquire", AccessType.GET_AND_UPDATE_BITWISE);
        
        static final Map<String, AccessMode> methodNameToAccessMode = null;
        final AccessType at;
        final String methodName;

        static {
            int i;
            methodNameToAccessMode = new HashMap(values().length);
            for (AccessMode am : values()) {
                methodNameToAccessMode.put(am.methodName, am);
            }
        }

        private AccessMode(String methodName2, AccessType at2) {
            this.methodName = methodName2;
            this.at = at2;
        }

        public String methodName() {
            return this.methodName;
        }

        public static AccessMode valueFromMethodName(String methodName2) {
            AccessMode am = methodNameToAccessMode.get(methodName2);
            if (am != null) {
                return am;
            }
            throw new IllegalArgumentException("No AccessMode value for method name " + methodName2);
        }
    }

    enum AccessType {
        GET,
        SET,
        COMPARE_AND_SWAP,
        COMPARE_AND_EXCHANGE,
        GET_AND_UPDATE,
        GET_AND_UPDATE_BITWISE,
        GET_AND_UPDATE_NUMERIC;

        /* access modifiers changed from: package-private */
        public MethodType accessModeType(Class<?> receiver, Class<?> value, Class<?>... intermediate) {
            switch (this) {
                case GET:
                    Class<?>[] ps = allocateParameters(0, receiver, intermediate);
                    fillParameters(ps, receiver, intermediate);
                    return MethodType.methodType(value, ps);
                case SET:
                    Class<?>[] ps2 = allocateParameters(1, receiver, intermediate);
                    ps2[fillParameters(ps2, receiver, intermediate)] = value;
                    return MethodType.methodType((Class<?>) Void.TYPE, ps2);
                case COMPARE_AND_SWAP:
                    Class<?>[] ps3 = allocateParameters(2, receiver, intermediate);
                    int i = fillParameters(ps3, receiver, intermediate);
                    ps3[i] = value;
                    ps3[i + 1] = value;
                    return MethodType.methodType((Class<?>) Boolean.TYPE, ps3);
                case COMPARE_AND_EXCHANGE:
                    Class<?>[] ps4 = allocateParameters(2, receiver, intermediate);
                    int i2 = fillParameters(ps4, receiver, intermediate);
                    ps4[i2] = value;
                    ps4[i2 + 1] = value;
                    return MethodType.methodType(value, ps4);
                case GET_AND_UPDATE:
                case GET_AND_UPDATE_BITWISE:
                case GET_AND_UPDATE_NUMERIC:
                    Class<?>[] ps5 = allocateParameters(1, receiver, intermediate);
                    ps5[fillParameters(ps5, receiver, intermediate)] = value;
                    return MethodType.methodType(value, ps5);
                default:
                    throw new InternalError("Unknown AccessType");
            }
        }

        private static Class<?>[] allocateParameters(int values, Class<?> receiver, Class<?>... intermediate) {
            return new Class[((receiver != null ? 1 : 0) + intermediate.length + values)];
        }

        private static int fillParameters(Class<?>[] ps, Class<?> receiver, Class<?>... intermediate) {
            int i = 0;
            if (receiver != null) {
                ps[0] = receiver;
                i = 0 + 1;
            }
            int j = 0;
            while (j < intermediate.length) {
                ps[i] = intermediate[j];
                j++;
                i++;
            }
            return i;
        }
    }

    @MethodHandle.PolymorphicSignature
    public final native Object compareAndExchange(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object compareAndExchangeAcquire(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object compareAndExchangeRelease(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native boolean compareAndSet(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object get(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAcquire(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndAdd(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndAddAcquire(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndAddRelease(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndBitwiseAnd(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndBitwiseAndAcquire(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndBitwiseAndRelease(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndBitwiseOr(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndBitwiseOrAcquire(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndBitwiseOrRelease(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndBitwiseXor(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndBitwiseXorAcquire(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndBitwiseXorRelease(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndSet(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndSetAcquire(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getAndSetRelease(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getOpaque(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native Object getVolatile(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native void set(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native void setOpaque(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native void setRelease(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native void setVolatile(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native boolean weakCompareAndSet(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native boolean weakCompareAndSetAcquire(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native boolean weakCompareAndSetPlain(Object... objArr);

    @MethodHandle.PolymorphicSignature
    public final native boolean weakCompareAndSetRelease(Object... objArr);

    static {
        if (AccessMode.values().length <= 32) {
            return;
        }
        throw new InternalError("accessModes overflow");
    }

    public final Class<?> varType() {
        return this.varType;
    }

    public final List<Class<?>> coordinateTypes() {
        if (this.coordinateType0 == null) {
            return Collections.EMPTY_LIST;
        }
        if (this.coordinateType1 == null) {
            return Collections.singletonList(this.coordinateType0);
        }
        return Collections.unmodifiableList(Arrays.asList(this.coordinateType0, this.coordinateType1));
    }

    public final MethodType accessModeType(AccessMode accessMode) {
        if (this.coordinateType1 == null) {
            return accessMode.at.accessModeType(this.coordinateType0, this.varType, new Class[0]);
        }
        return accessMode.at.accessModeType(this.coordinateType0, this.varType, this.coordinateType1);
    }

    public final boolean isAccessModeSupported(AccessMode accessMode) {
        int testBit = 1 << accessMode.ordinal();
        if ((this.accessModesBitMask & testBit) == testBit) {
            return true;
        }
        return false;
    }

    public final MethodHandle toMethodHandle(AccessMode accessMode) {
        return MethodHandles.varHandleExactInvoker(accessMode, accessModeType(accessMode)).bindTo(this);
    }

    public static void fullFence() {
        UNSAFE.fullFence();
    }

    public static void acquireFence() {
        UNSAFE.loadFence();
    }

    public static void releaseFence() {
        UNSAFE.storeFence();
    }

    public static void loadLoadFence() {
        UNSAFE.loadFence();
    }

    public static void storeStoreFence() {
        UNSAFE.storeFence();
    }

    VarHandle(Class<?> varType2, boolean isFinal) {
        this.varType = (Class) Objects.requireNonNull(varType2);
        this.coordinateType0 = null;
        this.coordinateType1 = null;
        this.accessModesBitMask = alignedAccessModesBitMask(varType2, isFinal);
    }

    VarHandle(Class<?> varType2, boolean isFinal, Class<?> coordinateType) {
        this.varType = (Class) Objects.requireNonNull(varType2);
        this.coordinateType0 = (Class) Objects.requireNonNull(coordinateType);
        this.coordinateType1 = null;
        this.accessModesBitMask = alignedAccessModesBitMask(varType2, isFinal);
    }

    VarHandle(Class<?> varType2, Class<?> backingArrayType, boolean isFinal, Class<?> coordinateType02, Class<?> coordinateType12) {
        this.varType = (Class) Objects.requireNonNull(varType2);
        this.coordinateType0 = (Class) Objects.requireNonNull(coordinateType02);
        this.coordinateType1 = (Class) Objects.requireNonNull(coordinateType12);
        Objects.requireNonNull(backingArrayType);
        Class<?> backingArrayComponentType = backingArrayType.getComponentType();
        if (backingArrayComponentType != varType2 && backingArrayComponentType != Byte.TYPE) {
            throw new InternalError("Unsupported backingArrayType: " + backingArrayType);
        } else if (backingArrayType.getComponentType() == varType2) {
            this.accessModesBitMask = alignedAccessModesBitMask(varType2, isFinal);
        } else {
            this.accessModesBitMask = unalignedAccessModesBitMask(varType2);
        }
    }

    static int accessTypesToBitMask(EnumSet<AccessType> accessTypes) {
        int m = 0;
        for (AccessMode accessMode : AccessMode.values()) {
            if (accessTypes.contains(accessMode.at)) {
                m |= 1 << accessMode.ordinal();
            }
        }
        return m;
    }

    static int alignedAccessModesBitMask(Class<?> varType2, boolean isFinal) {
        int bitMask = ALL_MODES_BIT_MASK;
        if (isFinal) {
            bitMask &= READ_ACCESS_MODES_BIT_MASK;
        }
        if (!(varType2 == Byte.TYPE || varType2 == Short.TYPE || varType2 == Character.TYPE || varType2 == Integer.TYPE || varType2 == Long.TYPE || varType2 == Float.TYPE || varType2 == Double.TYPE)) {
            bitMask &= ~NUMERIC_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK;
        }
        if (varType2 == Boolean.TYPE || varType2 == Byte.TYPE || varType2 == Short.TYPE || varType2 == Character.TYPE || varType2 == Integer.TYPE || varType2 == Long.TYPE) {
            return bitMask;
        }
        return bitMask & (~BITWISE_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK);
    }

    static int unalignedAccessModesBitMask(Class<?> varType2) {
        int bitMask = READ_ACCESS_MODES_BIT_MASK | WRITE_ACCESS_MODES_BIT_MASK;
        if (varType2 == Integer.TYPE || varType2 == Long.TYPE || varType2 == Float.TYPE || varType2 == Double.TYPE) {
            bitMask |= ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK;
        }
        if (varType2 == Integer.TYPE || varType2 == Long.TYPE) {
            bitMask |= NUMERIC_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK;
        }
        if (varType2 == Integer.TYPE || varType2 == Long.TYPE) {
            return bitMask | BITWISE_ATOMIC_UPDATE_ACCESS_MODES_BIT_MASK;
        }
        return bitMask;
    }
}
