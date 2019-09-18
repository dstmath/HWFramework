package com.android.internal.util.function.pooled;

import android.os.Message;
import android.text.TextUtils;
import android.util.Pools;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.BitUtils;
import com.android.internal.util.function.HexConsumer;
import com.android.internal.util.function.HexFunction;
import com.android.internal.util.function.HexPredicate;
import com.android.internal.util.function.QuadConsumer;
import com.android.internal.util.function.QuadFunction;
import com.android.internal.util.function.QuadPredicate;
import com.android.internal.util.function.QuintConsumer;
import com.android.internal.util.function.QuintFunction;
import com.android.internal.util.function.QuintPredicate;
import com.android.internal.util.function.TriConsumer;
import com.android.internal.util.function.TriFunction;
import com.android.internal.util.function.TriPredicate;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class PooledLambdaImpl<R> extends OmniFunction<Object, Object, Object, Object, Object, Object, R> {
    private static final boolean DEBUG = false;
    private static final int FLAG_ACQUIRED_FROM_MESSAGE_CALLBACKS_POOL = 128;
    private static final int FLAG_RECYCLED = 32;
    private static final int FLAG_RECYCLE_ON_USE = 64;
    private static final String LOG_TAG = "PooledLambdaImpl";
    static final int MASK_EXPOSED_AS = 16128;
    static final int MASK_FUNC_TYPE = 1032192;
    private static final int MAX_ARGS = 5;
    private static final int MAX_POOL_SIZE = 50;
    static final Pool sMessageCallbacksPool = new Pool(Message.sPoolSync);
    static final Pool sPool = new Pool(new Object());
    Object[] mArgs = null;
    long mConstValue;
    int mFlags = 0;
    Object mFunc;

    static class LambdaType {
        public static final int MASK = 63;
        public static final int MASK_ARG_COUNT = 7;
        public static final int MASK_BIT_COUNT = 6;
        public static final int MASK_RETURN_TYPE = 56;

        static class ReturnType {
            public static final int BOOLEAN = 2;
            public static final int DOUBLE = 6;
            public static final int INT = 4;
            public static final int LONG = 5;
            public static final int OBJECT = 3;
            public static final int VOID = 1;

            ReturnType() {
            }

            static String toString(int returnType) {
                switch (returnType) {
                    case 1:
                        return "VOID";
                    case 2:
                        return "BOOLEAN";
                    case 3:
                        return "OBJECT";
                    case 4:
                        return "INT";
                    case 5:
                        return "LONG";
                    case 6:
                        return "DOUBLE";
                    default:
                        return "" + returnType;
                }
            }

            static String lambdaSuffix(int type) {
                return prefix(type) + suffix(type);
            }

            private static String prefix(int type) {
                switch (type) {
                    case 4:
                        return "Int";
                    case 5:
                        return "Long";
                    case 6:
                        return "Double";
                    default:
                        return "";
                }
            }

            private static String suffix(int type) {
                switch (type) {
                    case 1:
                        return "Consumer";
                    case 2:
                        return "Predicate";
                    case 3:
                        return "Function";
                    default:
                        return "Supplier";
                }
            }
        }

        LambdaType() {
        }

        static int encode(int argCount, int returnType) {
            return PooledLambdaImpl.mask(7, argCount) | PooledLambdaImpl.mask(56, returnType);
        }

        static int decodeArgCount(int type) {
            return type & 7;
        }

        static int decodeReturnType(int type) {
            return PooledLambdaImpl.unmask(56, type);
        }

        static String toString(int type) {
            int argCount = decodeArgCount(type);
            int returnType = decodeReturnType(type);
            if (argCount == 0) {
                if (returnType == 1) {
                    return "Runnable";
                }
                if (returnType == 3 || returnType == 2) {
                    return "Supplier";
                }
            }
            return argCountPrefix(argCount) + ReturnType.lambdaSuffix(returnType);
        }

        private static String argCountPrefix(int argCount) {
            switch (argCount) {
                case 1:
                    return "";
                case 2:
                    return "Bi";
                case 3:
                    return "Tri";
                case 4:
                    return "Quad";
                case 5:
                    return "Quint";
                case 6:
                    return "Hex";
                case 7:
                    return "";
                default:
                    throw new IllegalArgumentException("" + argCount);
            }
        }
    }

    static class Pool extends Pools.SynchronizedPool<PooledLambdaImpl> {
        public Pool(Object lock) {
            super(50, lock);
        }
    }

    private PooledLambdaImpl() {
    }

    public void recycle() {
        if (!isRecycled()) {
            doRecycle();
        }
    }

    private void doRecycle() {
        Pool pool;
        if ((this.mFlags & 128) != 0) {
            pool = sMessageCallbacksPool;
        } else {
            pool = sPool;
        }
        this.mFunc = null;
        if (this.mArgs != null) {
            Arrays.fill(this.mArgs, null);
        }
        this.mFlags = 32;
        this.mConstValue = 0;
        pool.release(this);
    }

    /*  JADX ERROR: StackOverflow in pass: MarkFinallyVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    R invoke(java.lang.Object r7, java.lang.Object r8, java.lang.Object r9, java.lang.Object r10, java.lang.Object r11, java.lang.Object r12) {
        /*
            r6 = this;
            r6.checkNotRecycled()
            boolean r0 = r6.fillInArg(r7)
            r1 = 0
            if (r0 == 0) goto L_0x002a
            boolean r0 = r6.fillInArg(r8)
            if (r0 == 0) goto L_0x002a
            boolean r0 = r6.fillInArg(r9)
            if (r0 == 0) goto L_0x002a
            boolean r0 = r6.fillInArg(r10)
            if (r0 == 0) goto L_0x002a
            boolean r0 = r6.fillInArg(r11)
            if (r0 == 0) goto L_0x002a
            boolean r0 = r6.fillInArg(r12)
            if (r0 == 0) goto L_0x002a
            r0 = 1
            goto L_0x002b
        L_0x002a:
            r0 = r1
        L_0x002b:
            r2 = 1032192(0xfc000, float:1.446409E-39)
            int r2 = r6.getFlags(r2)
            int r2 = com.android.internal.util.function.pooled.PooledLambdaImpl.LambdaType.decodeArgCount(r2)
            r3 = 7
            if (r2 == r3) goto L_0x006c
            r3 = r1
        L_0x003a:
            if (r3 >= r2) goto L_0x006c
            java.lang.Object[] r4 = r6.mArgs
            r4 = r4[r3]
            com.android.internal.util.function.pooled.ArgumentPlaceholder<?> r5 = com.android.internal.util.function.pooled.ArgumentPlaceholder.INSTANCE
            if (r4 == r5) goto L_0x0047
            int r3 = r3 + 1
            goto L_0x003a
        L_0x0047:
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "Missing argument #"
            r4.append(r5)
            r4.append(r3)
            java.lang.String r5 = " among "
            r4.append(r5)
            java.lang.Object[] r5 = r6.mArgs
            java.lang.String r5 = java.util.Arrays.toString(r5)
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            r1.<init>(r4)
            throw r1
        L_0x006c:
            java.lang.Object r3 = r6.doInvoke()     // Catch:{ all -> 0x008f }
            boolean r4 = r6.isRecycleOnUse()
            if (r4 == 0) goto L_0x0079
            r6.doRecycle()
        L_0x0079:
            boolean r4 = r6.isRecycled()
            if (r4 != 0) goto L_0x008e
            java.lang.Object[] r4 = r6.mArgs
            int r4 = com.android.internal.util.ArrayUtils.size((java.lang.Object[]) r4)
        L_0x0086:
            if (r1 >= r4) goto L_0x008e
            r6.popArg(r1)
            int r1 = r1 + 1
            goto L_0x0086
        L_0x008e:
            return r3
        L_0x008f:
            r3 = move-exception
            boolean r4 = r6.isRecycleOnUse()
            if (r4 == 0) goto L_0x0099
            r6.doRecycle()
        L_0x0099:
            boolean r4 = r6.isRecycled()
            if (r4 != 0) goto L_0x00ae
            java.lang.Object[] r4 = r6.mArgs
            int r4 = com.android.internal.util.ArrayUtils.size((java.lang.Object[]) r4)
        L_0x00a6:
            if (r1 >= r4) goto L_0x00ae
            r6.popArg(r1)
            int r1 = r1 + 1
            goto L_0x00a6
        L_0x00ae:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.function.pooled.PooledLambdaImpl.invoke(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object):java.lang.Object");
    }

    private boolean fillInArg(Object invocationArg) {
        int argsSize = ArrayUtils.size(this.mArgs);
        for (int i = 0; i < argsSize; i++) {
            if (this.mArgs[i] == ArgumentPlaceholder.INSTANCE) {
                this.mArgs[i] = invocationArg;
                this.mFlags = (int) (((long) this.mFlags) | BitUtils.bitAt(i));
                return true;
            }
        }
        if (invocationArg == null || invocationArg == ArgumentPlaceholder.INSTANCE) {
            return false;
        }
        throw new IllegalStateException("No more arguments expected for provided arg " + invocationArg + " among " + Arrays.toString(this.mArgs));
    }

    private void checkNotRecycled() {
        if (isRecycled()) {
            throw new IllegalStateException("Instance is recycled: " + this);
        }
    }

    private R doInvoke() {
        int funcType = getFlags(MASK_FUNC_TYPE);
        int argCount = LambdaType.decodeArgCount(funcType);
        int returnType = LambdaType.decodeReturnType(funcType);
        switch (argCount) {
            case 0:
                switch (returnType) {
                    case 1:
                        ((Runnable) this.mFunc).run();
                        return null;
                    case 2:
                    case 3:
                        return ((Supplier) this.mFunc).get();
                }
            case 1:
                switch (returnType) {
                    case 1:
                        ((Consumer) this.mFunc).accept(popArg(0));
                        return null;
                    case 2:
                        return Boolean.valueOf(((Predicate) this.mFunc).test(popArg(0)));
                    case 3:
                        return ((Function) this.mFunc).apply(popArg(0));
                }
            case 2:
                switch (returnType) {
                    case 1:
                        ((BiConsumer) this.mFunc).accept(popArg(0), popArg(1));
                        return null;
                    case 2:
                        return Boolean.valueOf(((BiPredicate) this.mFunc).test(popArg(0), popArg(1)));
                    case 3:
                        return ((BiFunction) this.mFunc).apply(popArg(0), popArg(1));
                }
            case 3:
                switch (returnType) {
                    case 1:
                        ((TriConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2));
                        return null;
                    case 2:
                        return Boolean.valueOf(((TriPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2)));
                    case 3:
                        return ((TriFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2));
                }
            case 4:
                switch (returnType) {
                    case 1:
                        ((QuadConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2), popArg(3));
                        return null;
                    case 2:
                        return Boolean.valueOf(((QuadPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2), popArg(3)));
                    case 3:
                        return ((QuadFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2), popArg(3));
                }
            case 5:
                switch (returnType) {
                    case 1:
                        ((QuintConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4));
                        return null;
                    case 2:
                        return Boolean.valueOf(((QuintPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4)));
                    case 3:
                        return ((QuintFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4));
                }
            case 6:
                switch (returnType) {
                    case 1:
                        ((HexConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5));
                        return null;
                    case 2:
                        return Boolean.valueOf(((HexPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5)));
                    case 3:
                        return ((HexFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5));
                }
            case 7:
                switch (returnType) {
                    case 4:
                        return Integer.valueOf(getAsInt());
                    case 5:
                        return Long.valueOf(getAsLong());
                    case 6:
                        return Double.valueOf(getAsDouble());
                    default:
                        return this.mFunc;
                }
        }
        throw new IllegalStateException("Unknown function type: " + LambdaType.toString(funcType));
    }

    private boolean isConstSupplier() {
        return LambdaType.decodeArgCount(getFlags(MASK_FUNC_TYPE)) == 7;
    }

    private Object popArg(int index) {
        Object result = this.mArgs[index];
        if (isInvocationArgAtIndex(index)) {
            this.mArgs[index] = ArgumentPlaceholder.INSTANCE;
            this.mFlags = (int) (((long) this.mFlags) & (~BitUtils.bitAt(index)));
        }
        return result;
    }

    public String toString() {
        if (isRecycled()) {
            return "<recycled PooledLambda@" + hashCodeHex(this) + ">";
        }
        StringBuilder sb = new StringBuilder();
        if (isConstSupplier()) {
            sb.append(getFuncTypeAsString());
            sb.append("(");
            sb.append(doInvoke());
            sb.append(")");
        } else {
            if (this.mFunc instanceof PooledLambdaImpl) {
                sb.append(this.mFunc);
            } else {
                sb.append(getFuncTypeAsString());
                sb.append("@");
                sb.append(hashCodeHex(this.mFunc));
            }
            sb.append("(");
            sb.append(commaSeparateFirstN(this.mArgs, LambdaType.decodeArgCount(getFlags(MASK_FUNC_TYPE))));
            sb.append(")");
        }
        return sb.toString();
    }

    private String commaSeparateFirstN(Object[] arr, int n) {
        if (arr == null) {
            return "";
        }
        return TextUtils.join(",", Arrays.copyOf(arr, n));
    }

    private static String hashCodeHex(Object o) {
        return Integer.toHexString(o.hashCode());
    }

    private String getFuncTypeAsString() {
        if (isRecycled()) {
            throw new IllegalStateException();
        } else if (isConstSupplier()) {
            return "supplier";
        } else {
            String name = LambdaType.toString(getFlags(MASK_EXPOSED_AS));
            if (name.endsWith("Consumer")) {
                return "consumer";
            }
            if (name.endsWith("Function")) {
                return "function";
            }
            if (name.endsWith("Predicate")) {
                return "predicate";
            }
            if (name.endsWith("Supplier")) {
                return "supplier";
            }
            if (name.endsWith("Runnable")) {
                return "runnable";
            }
            throw new IllegalStateException("Don't know the string representation of " + name);
        }
    }

    static <E extends PooledLambda> E acquire(Pool pool, Object func, int fNumArgs, int numPlaceholders, int fReturnType, Object a, Object b, Object c, Object d, Object e, Object f) {
        PooledLambdaImpl r = acquire(pool);
        r.mFunc = func;
        r.setFlags(MASK_FUNC_TYPE, LambdaType.encode(fNumArgs, fReturnType));
        r.setFlags(MASK_EXPOSED_AS, LambdaType.encode(numPlaceholders, fReturnType));
        if (ArrayUtils.size(r.mArgs) < fNumArgs) {
            r.mArgs = new Object[fNumArgs];
        }
        setIfInBounds(r.mArgs, 0, a);
        setIfInBounds(r.mArgs, 1, b);
        setIfInBounds(r.mArgs, 2, c);
        setIfInBounds(r.mArgs, 3, d);
        setIfInBounds(r.mArgs, 4, e);
        setIfInBounds(r.mArgs, 5, f);
        return r;
    }

    static PooledLambdaImpl acquireConstSupplier(int type) {
        PooledLambdaImpl r = acquire(sPool);
        int lambdaType = LambdaType.encode(7, type);
        r.setFlags(MASK_FUNC_TYPE, lambdaType);
        r.setFlags(MASK_EXPOSED_AS, lambdaType);
        return r;
    }

    static PooledLambdaImpl acquire(Pool pool) {
        PooledLambdaImpl r = (PooledLambdaImpl) pool.acquire();
        if (r == null) {
            r = new PooledLambdaImpl();
        }
        r.mFlags &= -33;
        r.setFlags(128, pool == sMessageCallbacksPool ? 1 : 0);
        return r;
    }

    private static void setIfInBounds(Object[] array, int i, Object a) {
        if (i < ArrayUtils.size(array)) {
            array[i] = a;
        }
    }

    public OmniFunction<Object, Object, Object, Object, Object, Object, R> negate() {
        throw new UnsupportedOperationException();
    }

    public <V> OmniFunction<Object, Object, Object, Object, Object, Object, V> andThen(Function<? super R, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    public double getAsDouble() {
        return Double.longBitsToDouble(this.mConstValue);
    }

    public int getAsInt() {
        return (int) this.mConstValue;
    }

    public long getAsLong() {
        return this.mConstValue;
    }

    public OmniFunction<Object, Object, Object, Object, Object, Object, R> recycleOnUse() {
        this.mFlags |= 64;
        return this;
    }

    private boolean isRecycled() {
        return (this.mFlags & 32) != 0;
    }

    private boolean isRecycleOnUse() {
        return (this.mFlags & 64) != 0;
    }

    private boolean isInvocationArgAtIndex(int argIndex) {
        return (this.mFlags & (1 << argIndex)) != 0;
    }

    /* access modifiers changed from: package-private */
    public int getFlags(int mask) {
        return unmask(mask, this.mFlags);
    }

    /* access modifiers changed from: package-private */
    public void setFlags(int mask, int value) {
        this.mFlags &= ~mask;
        this.mFlags |= mask(mask, value);
    }

    /* access modifiers changed from: private */
    public static int mask(int mask, int value) {
        return (value << Integer.numberOfTrailingZeros(mask)) & mask;
    }

    /* access modifiers changed from: private */
    public static int unmask(int mask, int bits) {
        return (bits & mask) / (1 << Integer.numberOfTrailingZeros(mask));
    }
}
