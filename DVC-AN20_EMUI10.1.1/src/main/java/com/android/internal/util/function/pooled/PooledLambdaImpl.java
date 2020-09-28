package com.android.internal.util.function.pooled;

import android.os.Message;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Pools;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.BitUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.HeptConsumer;
import com.android.internal.util.function.HeptFunction;
import com.android.internal.util.function.HeptPredicate;
import com.android.internal.util.function.HexConsumer;
import com.android.internal.util.function.HexFunction;
import com.android.internal.util.function.HexPredicate;
import com.android.internal.util.function.NonaConsumer;
import com.android.internal.util.function.NonaFunction;
import com.android.internal.util.function.NonaPredicate;
import com.android.internal.util.function.OctConsumer;
import com.android.internal.util.function.OctFunction;
import com.android.internal.util.function.OctPredicate;
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
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/* access modifiers changed from: package-private */
public final class PooledLambdaImpl<R> extends OmniFunction<Object, Object, Object, Object, Object, Object, Object, Object, Object, R> {
    private static final boolean DEBUG = false;
    private static final int FLAG_ACQUIRED_FROM_MESSAGE_CALLBACKS_POOL = 2048;
    private static final int FLAG_RECYCLED = 512;
    private static final int FLAG_RECYCLE_ON_USE = 1024;
    private static final String LOG_TAG = "PooledLambdaImpl";
    static final int MASK_EXPOSED_AS = 520192;
    static final int MASK_FUNC_TYPE = 66584576;
    private static final int MAX_ARGS = 9;
    private static final int MAX_POOL_SIZE = 50;
    static final Pool sMessageCallbacksPool = new Pool(Message.sPoolSync);
    static final Pool sPool = new Pool(new Object());
    Object[] mArgs = null;
    long mConstValue;
    int mFlags = 0;
    Object mFunc;

    /* access modifiers changed from: package-private */
    public static class Pool extends Pools.SynchronizedPool<PooledLambdaImpl> {
        public Pool(Object lock) {
            super(50, lock);
        }
    }

    private PooledLambdaImpl() {
    }

    @Override // com.android.internal.util.function.pooled.PooledLambda
    public void recycle() {
        if (!isRecycled()) {
            doRecycle();
        }
    }

    private void doRecycle() {
        Pool pool;
        if ((this.mFlags & 2048) != 0) {
            pool = sMessageCallbacksPool;
        } else {
            pool = sPool;
        }
        this.mFunc = null;
        Object[] objArr = this.mArgs;
        if (objArr != null) {
            Arrays.fill(objArr, (Object) null);
        }
        this.mFlags = 512;
        this.mConstValue = 0;
        pool.release(this);
    }

    /* access modifiers changed from: package-private */
    /*  JADX ERROR: StackOverflowError in pass: MarkFinallyVisitor
        java.lang.StackOverflowError
        	at jadx.core.dex.nodes.InsnNode.isSame(InsnNode.java:311)
        	at jadx.core.dex.instructions.IfNode.isSame(IfNode.java:122)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.sameInsns(MarkFinallyVisitor.java:451)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.compareBlocks(MarkFinallyVisitor.java:436)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:408)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:411)
        */
    @Override // com.android.internal.util.function.pooled.OmniFunction
    public R invoke(java.lang.Object r7, java.lang.Object r8, java.lang.Object r9, java.lang.Object r10, java.lang.Object r11, java.lang.Object r12, java.lang.Object r13, java.lang.Object r14, java.lang.Object r15) {
        /*
            r6 = this;
            r6.checkNotRecycled()
            boolean r0 = r6.fillInArg(r7)
            if (r0 == 0) goto L_0x003b
            boolean r0 = r6.fillInArg(r8)
            if (r0 == 0) goto L_0x003b
            boolean r0 = r6.fillInArg(r9)
            if (r0 == 0) goto L_0x003b
            boolean r0 = r6.fillInArg(r10)
            if (r0 == 0) goto L_0x003b
            boolean r0 = r6.fillInArg(r11)
            if (r0 == 0) goto L_0x003b
            boolean r0 = r6.fillInArg(r12)
            if (r0 == 0) goto L_0x003b
            boolean r0 = r6.fillInArg(r13)
            if (r0 == 0) goto L_0x003b
            boolean r0 = r6.fillInArg(r14)
            if (r0 == 0) goto L_0x003b
            boolean r0 = r6.fillInArg(r15)
            if (r0 == 0) goto L_0x003b
            r0 = 1
            goto L_0x003c
        L_0x003b:
            r0 = 0
        L_0x003c:
            r1 = 66584576(0x3f80000, float:1.457613E-36)
            int r1 = r6.getFlags(r1)
            int r1 = com.android.internal.util.function.pooled.PooledLambdaImpl.LambdaType.decodeArgCount(r1)
            r2 = 15
            if (r1 == r2) goto L_0x007d
            r2 = 0
        L_0x004b:
            if (r2 >= r1) goto L_0x007d
            java.lang.Object[] r3 = r6.mArgs
            r3 = r3[r2]
            com.android.internal.util.function.pooled.ArgumentPlaceholder<?> r4 = com.android.internal.util.function.pooled.ArgumentPlaceholder.INSTANCE
            if (r3 == r4) goto L_0x0058
            int r2 = r2 + 1
            goto L_0x004b
        L_0x0058:
            java.lang.IllegalStateException r3 = new java.lang.IllegalStateException
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "Missing argument #"
            r4.append(r5)
            r4.append(r2)
            java.lang.String r5 = " among "
            r4.append(r5)
            java.lang.Object[] r5 = r6.mArgs
            java.lang.String r5 = java.util.Arrays.toString(r5)
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.<init>(r4)
            throw r3
        L_0x007d:
            java.lang.Object r2 = r6.doInvoke()     // Catch:{ all -> 0x00a0 }
            boolean r3 = r6.isRecycleOnUse()
            if (r3 == 0) goto L_0x008a
            r6.doRecycle()
        L_0x008a:
            boolean r3 = r6.isRecycled()
            if (r3 != 0) goto L_0x009f
            java.lang.Object[] r3 = r6.mArgs
            int r3 = com.android.internal.util.ArrayUtils.size(r3)
            r4 = 0
        L_0x0097:
            if (r4 >= r3) goto L_0x009f
            r6.popArg(r4)
            int r4 = r4 + 1
            goto L_0x0097
        L_0x009f:
            return r2
        L_0x00a0:
            r2 = move-exception
            boolean r3 = r6.isRecycleOnUse()
            if (r3 == 0) goto L_0x00aa
            r6.doRecycle()
        L_0x00aa:
            boolean r3 = r6.isRecycled()
            if (r3 != 0) goto L_0x00bf
            java.lang.Object[] r3 = r6.mArgs
            int r3 = com.android.internal.util.ArrayUtils.size(r3)
            r4 = 0
        L_0x00b7:
            if (r4 >= r3) goto L_0x00bf
            r6.popArg(r4)
            int r4 = r4 + 1
            goto L_0x00b7
        L_0x00bf:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.function.pooled.PooledLambdaImpl.invoke(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object):java.lang.Object");
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
        if (argCount == 15) {
            return returnType != 4 ? returnType != 5 ? returnType != 6 ? (R) this.mFunc : (R) Double.valueOf(getAsDouble()) : (R) Long.valueOf(getAsLong()) : (R) Integer.valueOf(getAsInt());
        }
        switch (argCount) {
            case 0:
                if (returnType == 1) {
                    ((Runnable) this.mFunc).run();
                    return null;
                } else if (returnType == 2 || returnType == 3) {
                    return (R) ((Supplier) this.mFunc).get();
                }
            case 1:
                if (returnType == 1) {
                    ((Consumer) this.mFunc).accept(popArg(0));
                    return null;
                } else if (returnType == 2) {
                    return (R) Boolean.valueOf(((Predicate) this.mFunc).test(popArg(0)));
                } else {
                    if (returnType == 3) {
                        return (R) ((Function) this.mFunc).apply(popArg(0));
                    }
                }
                break;
            case 2:
                if (returnType == 1) {
                    ((BiConsumer) this.mFunc).accept(popArg(0), popArg(1));
                    return null;
                } else if (returnType == 2) {
                    return (R) Boolean.valueOf(((BiPredicate) this.mFunc).test(popArg(0), popArg(1)));
                } else {
                    if (returnType == 3) {
                        return (R) ((BiFunction) this.mFunc).apply(popArg(0), popArg(1));
                    }
                }
                break;
            case 3:
                if (returnType == 1) {
                    ((TriConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2));
                    return null;
                } else if (returnType == 2) {
                    return (R) Boolean.valueOf(((TriPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2)));
                } else {
                    if (returnType == 3) {
                        return (R) ((TriFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2));
                    }
                }
                break;
            case 4:
                if (returnType == 1) {
                    ((QuadConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2), popArg(3));
                    return null;
                } else if (returnType == 2) {
                    return (R) Boolean.valueOf(((QuadPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2), popArg(3)));
                } else {
                    if (returnType == 3) {
                        return (R) ((QuadFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2), popArg(3));
                    }
                }
                break;
            case 5:
                if (returnType == 1) {
                    ((QuintConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4));
                    return null;
                } else if (returnType == 2) {
                    return (R) Boolean.valueOf(((QuintPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4)));
                } else {
                    if (returnType == 3) {
                        return (R) ((QuintFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4));
                    }
                }
                break;
            case 6:
                if (returnType == 1) {
                    ((HexConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5));
                    return null;
                } else if (returnType == 2) {
                    return (R) Boolean.valueOf(((HexPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5)));
                } else {
                    if (returnType == 3) {
                        return (R) ((HexFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5));
                    }
                }
                break;
            case 7:
                if (returnType == 1) {
                    ((HeptConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5), popArg(6));
                    return null;
                } else if (returnType == 2) {
                    return (R) Boolean.valueOf(((HeptPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5), popArg(6)));
                } else {
                    if (returnType == 3) {
                        return (R) ((HeptFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5), popArg(6));
                    }
                }
                break;
            case 8:
                if (returnType == 1) {
                    ((OctConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5), popArg(6), popArg(7));
                    return null;
                } else if (returnType == 2) {
                    return (R) Boolean.valueOf(((OctPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5), popArg(6), popArg(7)));
                } else {
                    if (returnType == 3) {
                        return (R) ((OctFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5), popArg(6), popArg(7));
                    }
                }
                break;
            case 9:
                if (returnType == 1) {
                    ((NonaConsumer) this.mFunc).accept(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5), popArg(6), popArg(7), popArg(8));
                    return null;
                } else if (returnType == 2) {
                    return (R) Boolean.valueOf(((NonaPredicate) this.mFunc).test(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5), popArg(6), popArg(7), popArg(8)));
                } else {
                    if (returnType == 3) {
                        return (R) ((NonaFunction) this.mFunc).apply(popArg(0), popArg(1), popArg(2), popArg(3), popArg(4), popArg(5), popArg(6), popArg(7), popArg(8));
                    }
                }
                break;
        }
        throw new IllegalStateException("Unknown function type: " + LambdaType.toString(funcType));
    }

    private boolean isConstSupplier() {
        return LambdaType.decodeArgCount(getFlags(MASK_FUNC_TYPE)) == 15;
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
            sb.append((Object) doInvoke());
            sb.append(")");
        } else {
            Object func = this.mFunc;
            if (func instanceof PooledLambdaImpl) {
                sb.append(func);
            } else {
                sb.append(getFuncTypeAsString());
                sb.append("@");
                sb.append(hashCodeHex(func));
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
        return TextUtils.join(SmsManager.REGEX_PREFIX_DELIMITER, Arrays.copyOf(arr, n));
    }

    private static String hashCodeHex(Object o) {
        return Integer.toHexString(Objects.hashCode(o));
    }

    private String getFuncTypeAsString() {
        if (isRecycled()) {
            return "<recycled>";
        }
        if (isConstSupplier()) {
            return "supplier";
        }
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
        return name;
    }

    static <E extends PooledLambda> E acquire(Pool pool, Object func, int fNumArgs, int numPlaceholders, int fReturnType, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i) {
        PooledLambdaImpl r = acquire(pool);
        r.mFunc = Preconditions.checkNotNull(func);
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
        setIfInBounds(r.mArgs, 6, g);
        setIfInBounds(r.mArgs, 7, h);
        setIfInBounds(r.mArgs, 8, i);
        return r;
    }

    static PooledLambdaImpl acquireConstSupplier(int type) {
        PooledLambdaImpl r = acquire(sPool);
        int lambdaType = LambdaType.encode(15, type);
        r.setFlags(MASK_FUNC_TYPE, lambdaType);
        r.setFlags(MASK_EXPOSED_AS, lambdaType);
        return r;
    }

    static PooledLambdaImpl acquire(Pool pool) {
        PooledLambdaImpl r = (PooledLambdaImpl) pool.acquire();
        if (r == null) {
            r = new PooledLambdaImpl();
        }
        r.mFlags &= -513;
        r.setFlags(2048, pool == sMessageCallbacksPool ? 1 : 0);
        return r;
    }

    private static void setIfInBounds(Object[] array, int i, Object a) {
        if (i < ArrayUtils.size(array)) {
            array[i] = a;
        }
    }

    @Override // java.util.function.BiPredicate, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, java.util.function.Predicate
    public OmniFunction<Object, Object, Object, Object, Object, Object, Object, Object, Object, R> negate() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.function.BiFunction, java.util.function.Function, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction
    public <V> OmniFunction<Object, Object, Object, Object, Object, Object, Object, Object, Object, V> andThen(Function<? super R, ? extends V> function) {
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

    @Override // com.android.internal.util.function.pooled.PooledFunction, com.android.internal.util.function.pooled.PooledFunction, com.android.internal.util.function.pooled.PooledLambda, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.OmniFunction, com.android.internal.util.function.pooled.PooledSupplier.OfInt, com.android.internal.util.function.pooled.PooledSupplier.OfInt, com.android.internal.util.function.pooled.PooledSupplier.OfDouble, com.android.internal.util.function.pooled.PooledSupplier.OfDouble, com.android.internal.util.function.pooled.PooledConsumer, com.android.internal.util.function.pooled.PooledConsumer, com.android.internal.util.function.pooled.PooledSupplier, com.android.internal.util.function.pooled.PooledSupplier, com.android.internal.util.function.pooled.PooledSupplier.OfLong, com.android.internal.util.function.pooled.PooledSupplier.OfLong, com.android.internal.util.function.pooled.PooledRunnable, com.android.internal.util.function.pooled.PooledRunnable, com.android.internal.util.function.pooled.PooledPredicate, com.android.internal.util.function.pooled.PooledPredicate
    public OmniFunction<Object, Object, Object, Object, Object, Object, Object, Object, Object, R> recycleOnUse() {
        this.mFlags |= 1024;
        return this;
    }

    private boolean isRecycled() {
        return (this.mFlags & 512) != 0;
    }

    private boolean isRecycleOnUse() {
        return (this.mFlags & 1024) != 0;
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

    /* access modifiers changed from: package-private */
    public static class LambdaType {
        public static final int MASK = 127;
        public static final int MASK_ARG_COUNT = 15;
        public static final int MASK_BIT_COUNT = 7;
        public static final int MASK_RETURN_TYPE = 112;

        LambdaType() {
        }

        static int encode(int argCount, int returnType) {
            return PooledLambdaImpl.mask(15, argCount) | PooledLambdaImpl.mask(112, returnType);
        }

        static int decodeArgCount(int type) {
            return type & 15;
        }

        static int decodeReturnType(int type) {
            return PooledLambdaImpl.unmask(112, type);
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
            if (argCount == 15) {
                return "";
            }
            switch (argCount) {
                case 0:
                    return "";
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
                    return "Hept";
                case 8:
                    return "Oct";
                case 9:
                    return "Nona";
                default:
                    return "" + argCount + "arg";
            }
        }

        /* access modifiers changed from: package-private */
        public static class ReturnType {
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
                if (type == 4) {
                    return "Int";
                }
                if (type == 5) {
                    return "Long";
                }
                if (type != 6) {
                    return "";
                }
                return "Double";
            }

            private static String suffix(int type) {
                if (type == 1) {
                    return "Consumer";
                }
                if (type == 2) {
                    return "Predicate";
                }
                if (type != 3) {
                    return "Supplier";
                }
                return "Function";
            }
        }
    }
}
