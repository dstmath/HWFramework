package java.lang.invoke;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.invoke.util.BytecodeDescriptor;
import sun.invoke.util.Wrapper;

public final class MethodType implements Serializable {
    static final /* synthetic */ boolean -assertionsDisabled = (MethodType.class.desiredAssertionStatus() ^ 1);
    static final int MAX_JVM_ARITY = 255;
    static final int MAX_MH_ARITY = 254;
    static final int MAX_MH_INVOKER_ARITY = 253;
    static final Class<?>[] NO_PTYPES = new Class[0];
    static final ConcurrentWeakInternSet<MethodType> internTable = new ConcurrentWeakInternSet();
    private static final MethodType[] objectOnlyTypes = new MethodType[20];
    private static final long ptypesOffset;
    private static final long rtypeOffset;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];
    private static final long serialVersionUID = 292;
    @Stable
    private MethodTypeForm form;
    @Stable
    private String methodDescriptor;
    private final Class<?>[] ptypes;
    private final Class<?> rtype;
    @Stable
    private MethodType wrapAlt;

    private static class ConcurrentWeakInternSet<T> {
        private final ConcurrentMap<WeakEntry<T>, WeakEntry<T>> map = new ConcurrentHashMap();
        private final ReferenceQueue<T> stale = new ReferenceQueue();

        private static class WeakEntry<T> extends WeakReference<T> {
            public final int hashcode;

            public WeakEntry(T key, ReferenceQueue<T> queue) {
                super(key, queue);
                this.hashcode = key.hashCode();
            }

            public WeakEntry(T key) {
                super(key);
                this.hashcode = key.hashCode();
            }

            public boolean equals(Object obj) {
                if (!(obj instanceof WeakEntry)) {
                    return MethodType.-assertionsDisabled;
                }
                Object that = ((WeakEntry) obj).get();
                Object mine = get();
                boolean equals = (that == null || mine == null) ? this == obj ? true : MethodType.-assertionsDisabled : mine.lambda$-java_util_function_Predicate_4628(that);
                return equals;
            }

            public int hashCode() {
                return this.hashcode;
            }
        }

        public T get(T elem) {
            if (elem == null) {
                throw new NullPointerException();
            }
            expungeStaleElements();
            WeakEntry<T> value = (WeakEntry) this.map.get(new WeakEntry(elem));
            if (value != null) {
                T res = value.get();
                if (res != null) {
                    return res;
                }
            }
            return null;
        }

        public T add(T elem) {
            if (elem == null) {
                throw new NullPointerException();
            }
            T interned;
            WeakEntry<T> e = new WeakEntry(elem, this.stale);
            do {
                expungeStaleElements();
                WeakEntry<T> exist = (WeakEntry) this.map.putIfAbsent(e, e);
                if (exist == null) {
                    interned = elem;
                    continue;
                } else {
                    interned = exist.get();
                    continue;
                }
            } while (interned == null);
            return interned;
        }

        private void expungeStaleElements() {
            while (true) {
                Reference<? extends T> reference = this.stale.poll();
                if (reference != null) {
                    this.map.remove(reference);
                } else {
                    return;
                }
            }
        }
    }

    private MethodType(Class<?> rtype, Class<?>[] ptypes, boolean trusted) {
        checkRtype(rtype);
        checkPtypes(ptypes);
        this.rtype = rtype;
        if (!trusted) {
            ptypes = (Class[]) Arrays.copyOf((Object[]) ptypes, ptypes.length);
        }
        this.ptypes = ptypes;
    }

    private MethodType(Class<?>[] ptypes, Class<?> rtype) {
        this.rtype = rtype;
        this.ptypes = ptypes;
    }

    MethodTypeForm form() {
        return this.form;
    }

    public Class<?> rtype() {
        return this.rtype;
    }

    public Class<?>[] ptypes() {
        return this.ptypes;
    }

    private static void checkRtype(Class<?> rtype) {
        Objects.requireNonNull(rtype);
    }

    private static void checkPtype(Class<?> ptype) {
        Objects.requireNonNull(ptype);
        if (ptype == Void.TYPE) {
            throw MethodHandleStatics.newIllegalArgumentException("parameter type cannot be void");
        }
    }

    private static int checkPtypes(Class<?>[] ptypes) {
        int slots = 0;
        for (Class<?> ptype : ptypes) {
            checkPtype(ptype);
            if (ptype == Double.TYPE || ptype == Long.TYPE) {
                slots++;
            }
        }
        checkSlotCount(ptypes.length + slots);
        return slots;
    }

    static void checkSlotCount(int count) {
        boolean z = -assertionsDisabled;
        if ((count & MAX_JVM_ARITY) != count) {
            throw MethodHandleStatics.newIllegalArgumentException("bad parameter count " + count);
        }
    }

    private static IndexOutOfBoundsException newIndexOutOfBoundsException(Object num) {
        if (num instanceof Integer) {
            num = "bad index: " + num;
        }
        return new IndexOutOfBoundsException(num.toString());
    }

    static {
        try {
            rtypeOffset = MethodHandleStatics.UNSAFE.objectFieldOffset(MethodType.class.getDeclaredField("rtype"));
            ptypesOffset = MethodHandleStatics.UNSAFE.objectFieldOffset(MethodType.class.getDeclaredField("ptypes"));
        } catch (Throwable ex) {
            throw new Error(ex);
        }
    }

    public static MethodType methodType(Class<?> rtype, Class<?>[] ptypes) {
        return makeImpl(rtype, ptypes, -assertionsDisabled);
    }

    public static MethodType methodType(Class<?> rtype, List<Class<?>> ptypes) {
        return makeImpl(rtype, listToArray(ptypes), -assertionsDisabled);
    }

    private static Class<?>[] listToArray(List<Class<?>> ptypes) {
        checkSlotCount(ptypes.size());
        return (Class[]) ptypes.toArray(NO_PTYPES);
    }

    public static MethodType methodType(Class<?> rtype, Class<?> ptype0, Class<?>... ptypes) {
        Object ptypes1 = new Class[(ptypes.length + 1)];
        ptypes1[0] = ptype0;
        System.arraycopy((Object) ptypes, 0, ptypes1, 1, ptypes.length);
        return makeImpl(rtype, ptypes1, true);
    }

    public static MethodType methodType(Class<?> rtype) {
        return makeImpl(rtype, NO_PTYPES, true);
    }

    public static MethodType methodType(Class<?> rtype, Class<?> ptype0) {
        return makeImpl(rtype, new Class[]{ptype0}, true);
    }

    public static MethodType methodType(Class<?> rtype, MethodType ptypes) {
        return makeImpl(rtype, ptypes.ptypes, true);
    }

    static MethodType makeImpl(Class<?> rtype, Class<?>[] ptypes, boolean trusted) {
        MethodType mt = (MethodType) internTable.get(new MethodType(ptypes, rtype));
        if (mt != null) {
            return mt;
        }
        if (ptypes.length == 0) {
            ptypes = NO_PTYPES;
            trusted = true;
        }
        mt = new MethodType(rtype, ptypes, trusted);
        mt.form = MethodTypeForm.findForm(mt);
        return (MethodType) internTable.add(mt);
    }

    public static MethodType genericMethodType(int objectArgCount, boolean finalArray) {
        MethodType mt;
        checkSlotCount(objectArgCount);
        int ivarargs = !finalArray ? 0 : 1;
        int ootIndex = (objectArgCount * 2) + ivarargs;
        if (ootIndex < objectOnlyTypes.length) {
            mt = objectOnlyTypes[ootIndex];
            if (mt != null) {
                return mt;
            }
        }
        Object[] ptypes = new Class[(objectArgCount + ivarargs)];
        Arrays.fill(ptypes, (Object) Object.class);
        if (ivarargs != 0) {
            ptypes[objectArgCount] = Object[].class;
        }
        mt = makeImpl(Object.class, ptypes, true);
        if (ootIndex < objectOnlyTypes.length) {
            objectOnlyTypes[ootIndex] = mt;
        }
        return mt;
    }

    public static MethodType genericMethodType(int objectArgCount) {
        return genericMethodType(objectArgCount, -assertionsDisabled);
    }

    public MethodType changeParameterType(int num, Class<?> nptype) {
        if (parameterType(num) == nptype) {
            return this;
        }
        checkPtype(nptype);
        Class[] nptypes = (Class[]) this.ptypes.clone();
        nptypes[num] = nptype;
        return makeImpl(this.rtype, nptypes, true);
    }

    public MethodType insertParameterTypes(int num, Class<?>... ptypesToInsert) {
        int len = this.ptypes.length;
        if (num < 0 || num > len) {
            throw newIndexOutOfBoundsException(Integer.valueOf(num));
        }
        checkSlotCount((parameterSlotCount() + ptypesToInsert.length) + checkPtypes(ptypesToInsert));
        int ilen = ptypesToInsert.length;
        if (ilen == 0) {
            return this;
        }
        Object nptypes = (Class[]) Arrays.copyOfRange(this.ptypes, 0, len + ilen);
        System.arraycopy(nptypes, num, nptypes, num + ilen, len - num);
        System.arraycopy((Object) ptypesToInsert, 0, nptypes, num, ilen);
        return makeImpl(this.rtype, nptypes, true);
    }

    public MethodType appendParameterTypes(Class<?>... ptypesToInsert) {
        return insertParameterTypes(parameterCount(), (Class[]) ptypesToInsert);
    }

    public MethodType insertParameterTypes(int num, List<Class<?>> ptypesToInsert) {
        return insertParameterTypes(num, listToArray(ptypesToInsert));
    }

    public MethodType appendParameterTypes(List<Class<?>> ptypesToInsert) {
        return insertParameterTypes(parameterCount(), (List) ptypesToInsert);
    }

    MethodType replaceParameterTypes(int start, int end, Class<?>... ptypesToInsert) {
        if (start == end) {
            return insertParameterTypes(start, (Class[]) ptypesToInsert);
        }
        int len = this.ptypes.length;
        if (start < 0 || start > end || end > len) {
            throw newIndexOutOfBoundsException("start=" + start + " end=" + end);
        } else if (ptypesToInsert.length == 0) {
            return dropParameterTypes(start, end);
        } else {
            return dropParameterTypes(start, end).insertParameterTypes(start, (Class[]) ptypesToInsert);
        }
    }

    MethodType asSpreaderType(Class<?> arrayType, int arrayLength) {
        if (-assertionsDisabled || parameterCount() >= arrayLength) {
            int spreadPos = this.ptypes.length - arrayLength;
            if (arrayLength == 0) {
                return this;
            }
            if (arrayType == Object[].class) {
                if (isGeneric()) {
                    return this;
                }
                if (spreadPos == 0) {
                    MethodType res = genericMethodType(arrayLength);
                    if (this.rtype != Object.class) {
                        res = res.changeReturnType(this.rtype);
                    }
                    return res;
                }
            }
            Object elemType = arrayType.getComponentType();
            if (-assertionsDisabled || elemType != null) {
                for (int i = spreadPos; i < this.ptypes.length; i++) {
                    if (this.ptypes[i] != elemType) {
                        Class[] fixedPtypes = (Class[]) this.ptypes.clone();
                        Arrays.fill((Object[]) fixedPtypes, i, this.ptypes.length, elemType);
                        return methodType(this.rtype, fixedPtypes);
                    }
                }
                return this;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    Class<?> leadingReferenceParameter() {
        if (this.ptypes.length != 0) {
            Class<?> ptype = this.ptypes[0];
            if (!ptype.isPrimitive()) {
                return ptype;
            }
        }
        throw MethodHandleStatics.newIllegalArgumentException("no leading reference parameter");
    }

    MethodType asCollectorType(Class<?> arrayType, int arrayLength) {
        if (!-assertionsDisabled && parameterCount() < 1) {
            throw new AssertionError();
        } else if (-assertionsDisabled || lastParameterType().isAssignableFrom(arrayType)) {
            MethodType res;
            if (arrayType == Object[].class) {
                res = genericMethodType(arrayLength);
                if (this.rtype != Object.class) {
                    res = res.changeReturnType(this.rtype);
                }
            } else {
                Class<?> elemType = arrayType.getComponentType();
                if (-assertionsDisabled || elemType != null) {
                    res = methodType(this.rtype, Collections.nCopies(arrayLength, elemType));
                } else {
                    throw new AssertionError();
                }
            }
            if (this.ptypes.length == 1) {
                return res;
            }
            return res.insertParameterTypes(0, parameterList().subList(0, this.ptypes.length - 1));
        } else {
            throw new AssertionError();
        }
    }

    public MethodType dropParameterTypes(int start, int end) {
        int len = this.ptypes.length;
        if (start < 0 || start > end || end > len) {
            throw newIndexOutOfBoundsException("start=" + start + " end=" + end);
        } else if (start == end) {
            return this;
        } else {
            Class<?>[] nptypes;
            Class[] nptypes2;
            if (start == 0) {
                if (end == len) {
                    nptypes2 = NO_PTYPES;
                } else {
                    nptypes2 = (Class[]) Arrays.copyOfRange(this.ptypes, end, len);
                }
            } else if (end == len) {
                nptypes2 = (Class[]) Arrays.copyOfRange(this.ptypes, 0, start);
            } else {
                int tail = len - end;
                Object nptypes22 = (Class[]) Arrays.copyOfRange(this.ptypes, 0, start + tail);
                System.arraycopy(this.ptypes, end, nptypes22, start, tail);
            }
            return makeImpl(this.rtype, nptypes22, true);
        }
    }

    public MethodType changeReturnType(Class<?> nrtype) {
        if (returnType() == nrtype) {
            return this;
        }
        return makeImpl(nrtype, this.ptypes, true);
    }

    public boolean hasPrimitives() {
        return this.form.hasPrimitives();
    }

    public boolean hasWrappers() {
        return unwrap() != this ? true : -assertionsDisabled;
    }

    public MethodType erase() {
        return this.form.erasedType();
    }

    MethodType basicType() {
        return this.form.basicType();
    }

    MethodType invokerType() {
        return insertParameterTypes(0, MethodHandle.class);
    }

    public MethodType generic() {
        return genericMethodType(parameterCount());
    }

    boolean isGeneric() {
        return this == erase() ? hasPrimitives() ^ 1 : -assertionsDisabled;
    }

    public MethodType wrap() {
        return hasPrimitives() ? wrapWithPrims(this) : this;
    }

    public MethodType unwrap() {
        return unwrapWithNoPrims(!hasPrimitives() ? this : wrapWithPrims(this));
    }

    private static MethodType wrapWithPrims(MethodType pt) {
        if (-assertionsDisabled || pt.hasPrimitives()) {
            MethodType wt = pt.wrapAlt;
            if (wt == null) {
                wt = MethodTypeForm.canonicalize(pt, 2, 2);
                if (-assertionsDisabled || wt != null) {
                    pt.wrapAlt = wt;
                } else {
                    throw new AssertionError();
                }
            }
            return wt;
        }
        throw new AssertionError();
    }

    private static MethodType unwrapWithNoPrims(MethodType wt) {
        if (-assertionsDisabled || !wt.hasPrimitives()) {
            MethodType uwt = wt.wrapAlt;
            if (uwt == null) {
                uwt = MethodTypeForm.canonicalize(wt, 3, 3);
                if (uwt == null) {
                    uwt = wt;
                }
                wt.wrapAlt = uwt;
            }
            return uwt;
        }
        throw new AssertionError();
    }

    public Class<?> parameterType(int num) {
        return this.ptypes[num];
    }

    public int parameterCount() {
        return this.ptypes.length;
    }

    public Class<?> returnType() {
        return this.rtype;
    }

    public List<Class<?>> parameterList() {
        return Collections.unmodifiableList(Arrays.asList((Class[]) this.ptypes.clone()));
    }

    Class<?> lastParameterType() {
        int len = this.ptypes.length;
        return len == 0 ? Void.TYPE : this.ptypes[len - 1];
    }

    public Class<?>[] parameterArray() {
        return (Class[]) this.ptypes.clone();
    }

    public boolean equals(Object x) {
        if (this != x) {
            return x instanceof MethodType ? equals((MethodType) x) : -assertionsDisabled;
        } else {
            return true;
        }
    }

    private boolean equals(MethodType that) {
        if (this.rtype == that.rtype) {
            return Arrays.equals(this.ptypes, that.ptypes);
        }
        return -assertionsDisabled;
    }

    public int hashCode() {
        int hashCode = this.rtype.hashCode() + 31;
        for (Class<?> ptype : this.ptypes) {
            hashCode = (hashCode * 31) + ptype.hashCode();
        }
        return hashCode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < this.ptypes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(this.ptypes[i].getSimpleName());
        }
        sb.append(")");
        sb.append(this.rtype.getSimpleName());
        return sb.toString();
    }

    boolean isConvertibleTo(MethodType newType) {
        MethodTypeForm oldForm = form();
        MethodTypeForm newForm = newType.form();
        if (oldForm == newForm) {
            return true;
        }
        if (!canConvert(returnType(), newType.returnType())) {
            return -assertionsDisabled;
        }
        Class<?>[] srcTypes = newType.ptypes;
        Class<?>[] dstTypes = this.ptypes;
        if (srcTypes == dstTypes) {
            return true;
        }
        int argc = srcTypes.length;
        if (argc != dstTypes.length) {
            return -assertionsDisabled;
        }
        if (argc <= 1) {
            return (argc != 1 || (canConvert(srcTypes[0], dstTypes[0]) ^ 1) == 0) ? true : -assertionsDisabled;
        } else {
            if ((oldForm.primitiveParameterCount() != 0 || oldForm.erasedType != this) && (newForm.primitiveParameterCount() != 0 || newForm.erasedType != newType)) {
                return canConvertParameters(srcTypes, dstTypes);
            }
            if (-assertionsDisabled || canConvertParameters(srcTypes, dstTypes)) {
                return true;
            }
            throw new AssertionError();
        }
    }

    boolean explicitCastEquivalentToAsType(MethodType newType) {
        if (this == newType) {
            return true;
        }
        if (!explicitCastEquivalentToAsType(this.rtype, newType.rtype)) {
            return -assertionsDisabled;
        }
        Class<?>[] srcTypes = newType.ptypes;
        Class<?>[] dstTypes = this.ptypes;
        if (dstTypes == srcTypes) {
            return true;
        }
        if (-assertionsDisabled || dstTypes.length == srcTypes.length) {
            for (int i = 0; i < dstTypes.length; i++) {
                if (!explicitCastEquivalentToAsType(srcTypes[i], dstTypes[i])) {
                    return -assertionsDisabled;
                }
            }
            return true;
        }
        throw new AssertionError();
    }

    private static boolean explicitCastEquivalentToAsType(Class<?> src, Class<?> dst) {
        boolean z = true;
        if (src == dst || dst == Object.class || dst == Void.TYPE) {
            return true;
        }
        if (src.isPrimitive() && src != Void.TYPE) {
            return canConvert(src, dst);
        }
        if (dst.isPrimitive()) {
            return -assertionsDisabled;
        }
        if (dst.isInterface()) {
            z = dst.isAssignableFrom(src);
        }
        return z;
    }

    private boolean canConvertParameters(Class<?>[] srcTypes, Class<?>[] dstTypes) {
        for (int i = 0; i < srcTypes.length; i++) {
            if (!canConvert(srcTypes[i], dstTypes[i])) {
                return -assertionsDisabled;
            }
        }
        return true;
    }

    static boolean canConvert(Class<?> src, Class<?> dst) {
        if (src == dst || src == Object.class || dst == Object.class) {
            return true;
        }
        if (src.isPrimitive()) {
            if (src == Void.TYPE) {
                return true;
            }
            Wrapper sw = Wrapper.forPrimitiveType(src);
            if (dst.isPrimitive()) {
                return Wrapper.forPrimitiveType(dst).isConvertibleFrom(sw);
            }
            return dst.isAssignableFrom(sw.wrapperType());
        } else if (!dst.isPrimitive() || dst == Void.TYPE) {
            return true;
        } else {
            Wrapper dw = Wrapper.forPrimitiveType(dst);
            if (src.isAssignableFrom(dw.wrapperType())) {
                return true;
            }
            if (Wrapper.isWrapperType(src) && dw.isConvertibleFrom(Wrapper.forWrapperType(src))) {
                return true;
            }
            return -assertionsDisabled;
        }
    }

    int parameterSlotCount() {
        return this.form.parameterSlotCount();
    }

    public static MethodType fromMethodDescriptorString(String descriptor, ClassLoader loader) throws IllegalArgumentException, TypeNotPresentException {
        if (!descriptor.startsWith("(") || descriptor.indexOf(41) < 0 || descriptor.indexOf(46) >= 0) {
            throw MethodHandleStatics.newIllegalArgumentException("not a method descriptor: " + descriptor);
        }
        List<Class<?>> types = BytecodeDescriptor.parseMethod(descriptor, loader);
        Class<?> rtype = (Class) types.remove(types.size() - 1);
        checkSlotCount(types.size());
        return makeImpl(rtype, listToArray(types), true);
    }

    public String toMethodDescriptorString() {
        String desc = this.methodDescriptor;
        if (desc != null) {
            return desc;
        }
        desc = BytecodeDescriptor.unparse(this);
        this.methodDescriptor = desc;
        return desc;
    }

    static String toFieldDescriptorString(Class<?> cls) {
        return BytecodeDescriptor.unparse((Class) cls);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(returnType());
        s.writeObject(parameterArray());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        Class<?> returnType = (Class) s.readObject();
        Class[] parameterArray = (Class[]) s.readObject();
        checkRtype(returnType);
        checkPtypes(parameterArray);
        MethodType_init(returnType, (Class[]) parameterArray.clone());
    }

    private MethodType() {
        this.rtype = null;
        this.ptypes = null;
    }

    private void MethodType_init(Class<?> rtype, Class<?>[] ptypes) {
        checkRtype(rtype);
        checkPtypes(ptypes);
        MethodHandleStatics.UNSAFE.putObject(this, rtypeOffset, rtype);
        MethodHandleStatics.UNSAFE.putObject(this, ptypesOffset, ptypes);
    }

    private Object readResolve() {
        return methodType(this.rtype, this.ptypes);
    }
}
