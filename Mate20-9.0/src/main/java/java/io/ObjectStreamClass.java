package java.io;

import dalvik.system.VMRuntime;
import dalvik.system.VMStack;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;
import sun.reflect.misc.ReflectUtil;

public class ObjectStreamClass implements Serializable {
    static final int MAX_SDK_TARGET_FOR_CLINIT_UIDGEN_WORKAROUND = 23;
    public static final ObjectStreamField[] NO_FIELDS = new ObjectStreamField[0];
    private static final ObjectStreamField[] serialPersistentFields = NO_FIELDS;
    private static final long serialVersionUID = -6120832682080437368L;
    /* access modifiers changed from: private */
    public Class<?> cl;
    /* access modifiers changed from: private */
    public Constructor<?> cons;
    private volatile ClassDataSlot[] dataLayout;
    private ExceptionInfo defaultSerializeEx;
    /* access modifiers changed from: private */
    public ExceptionInfo deserializeEx;
    /* access modifiers changed from: private */
    public boolean externalizable;
    private FieldReflector fieldRefl;
    /* access modifiers changed from: private */
    public ObjectStreamField[] fields;
    private boolean hasBlockExternalData = true;
    /* access modifiers changed from: private */
    public boolean hasWriteObjectData;
    private boolean initialized;
    /* access modifiers changed from: private */
    public boolean isEnum;
    private boolean isProxy;
    private ObjectStreamClass localDesc;
    private String name;
    private int numObjFields;
    private int primDataSize;
    /* access modifiers changed from: private */
    public Method readObjectMethod;
    /* access modifiers changed from: private */
    public Method readObjectNoDataMethod;
    /* access modifiers changed from: private */
    public Method readResolveMethod;
    private ClassNotFoundException resolveEx;
    private boolean serializable;
    /* access modifiers changed from: private */
    public ExceptionInfo serializeEx;
    /* access modifiers changed from: private */
    public volatile Long suid;
    private ObjectStreamClass superDesc;
    /* access modifiers changed from: private */
    public Method writeObjectMethod;
    /* access modifiers changed from: private */
    public Method writeReplaceMethod;

    private static class Caches {
        static final ConcurrentMap<WeakClassKey, Reference<?>> localDescs = new ConcurrentHashMap();
        /* access modifiers changed from: private */
        public static final ReferenceQueue<Class<?>> localDescsQueue = new ReferenceQueue<>();
        static final ConcurrentMap<FieldReflectorKey, Reference<?>> reflectors = new ConcurrentHashMap();
        /* access modifiers changed from: private */
        public static final ReferenceQueue<Class<?>> reflectorsQueue = new ReferenceQueue<>();

        private Caches() {
        }
    }

    static class ClassDataSlot {
        final ObjectStreamClass desc;
        final boolean hasData;

        ClassDataSlot(ObjectStreamClass desc2, boolean hasData2) {
            this.desc = desc2;
            this.hasData = hasData2;
        }
    }

    private static class EntryFuture {
        private static final Object unset = new Object();
        private Object entry;
        private final Thread owner;

        private EntryFuture() {
            this.owner = Thread.currentThread();
            this.entry = unset;
        }

        /* access modifiers changed from: package-private */
        public synchronized boolean set(Object entry2) {
            if (this.entry != unset) {
                return false;
            }
            this.entry = entry2;
            notifyAll();
            return true;
        }

        /* access modifiers changed from: package-private */
        public synchronized Object get() {
            boolean interrupted = false;
            while (this.entry == unset) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                });
            }
            return this.entry;
        }

        /* access modifiers changed from: package-private */
        public Thread getOwner() {
            return this.owner;
        }
    }

    private static class ExceptionInfo {
        private final String className;
        private final String message;

        ExceptionInfo(String cn, String msg) {
            this.className = cn;
            this.message = msg;
        }

        /* access modifiers changed from: package-private */
        public InvalidClassException newInvalidClassException() {
            return new InvalidClassException(this.className, this.message);
        }
    }

    private static class FieldReflector {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private final ObjectStreamField[] fields;
        private final int numPrimFields;
        private final int[] offsets;
        private final long[] readKeys;
        private final char[] typeCodes;
        private final Class<?>[] types;
        private final long[] writeKeys;

        FieldReflector(ObjectStreamField[] fields2) {
            this.fields = fields2;
            int nfields = fields2.length;
            this.readKeys = new long[nfields];
            this.writeKeys = new long[nfields];
            this.offsets = new int[nfields];
            this.typeCodes = new char[nfields];
            ArrayList<Class<?>> typeList = new ArrayList<>();
            Set<Long> usedKeys = new HashSet<>();
            for (int i = 0; i < nfields; i++) {
                ObjectStreamField f = fields2[i];
                Field rf = f.getField();
                long j = -1;
                long key = rf != null ? unsafe.objectFieldOffset(rf) : -1;
                this.readKeys[i] = key;
                this.writeKeys[i] = usedKeys.add(Long.valueOf(key)) ? key : j;
                this.offsets[i] = f.getOffset();
                this.typeCodes[i] = f.getTypeCode();
                if (!f.isPrimitive()) {
                    typeList.add(rf != null ? rf.getType() : null);
                }
            }
            this.types = (Class[]) typeList.toArray(new Class[typeList.size()]);
            this.numPrimFields = nfields - this.types.length;
        }

        /* access modifiers changed from: package-private */
        public ObjectStreamField[] getFields() {
            return this.fields;
        }

        /* access modifiers changed from: package-private */
        public void getPrimFieldValues(Object obj, byte[] buf) {
            if (obj != null) {
                for (int i = 0; i < this.numPrimFields; i++) {
                    long key = this.readKeys[i];
                    int off = this.offsets[i];
                    char c = this.typeCodes[i];
                    if (c == 'F') {
                        Bits.putFloat(buf, off, unsafe.getFloat(obj, key));
                    } else if (c == 'S') {
                        Bits.putShort(buf, off, unsafe.getShort(obj, key));
                    } else if (c != 'Z') {
                        switch (c) {
                            case 'B':
                                buf[off] = unsafe.getByte(obj, key);
                                break;
                            case 'C':
                                Bits.putChar(buf, off, unsafe.getChar(obj, key));
                                break;
                            case 'D':
                                Bits.putDouble(buf, off, unsafe.getDouble(obj, key));
                                break;
                            default:
                                switch (c) {
                                    case 'I':
                                        Bits.putInt(buf, off, unsafe.getInt(obj, key));
                                        break;
                                    case 'J':
                                        Bits.putLong(buf, off, unsafe.getLong(obj, key));
                                        break;
                                    default:
                                        throw new InternalError();
                                }
                        }
                    } else {
                        Bits.putBoolean(buf, off, unsafe.getBoolean(obj, key));
                    }
                }
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: package-private */
        public void setPrimFieldValues(Object obj, byte[] buf) {
            if (obj != null) {
                for (int i = 0; i < this.numPrimFields; i++) {
                    long key = this.writeKeys[i];
                    if (key != -1) {
                        int off = this.offsets[i];
                        char c = this.typeCodes[i];
                        if (c == 'F') {
                            unsafe.putFloat(obj, key, Bits.getFloat(buf, off));
                        } else if (c == 'S') {
                            unsafe.putShort(obj, key, Bits.getShort(buf, off));
                        } else if (c != 'Z') {
                            switch (c) {
                                case 'B':
                                    unsafe.putByte(obj, key, buf[off]);
                                    break;
                                case 'C':
                                    unsafe.putChar(obj, key, Bits.getChar(buf, off));
                                    break;
                                case 'D':
                                    unsafe.putDouble(obj, key, Bits.getDouble(buf, off));
                                    break;
                                default:
                                    switch (c) {
                                        case 'I':
                                            unsafe.putInt(obj, key, Bits.getInt(buf, off));
                                            break;
                                        case 'J':
                                            unsafe.putLong(obj, key, Bits.getLong(buf, off));
                                            break;
                                        default:
                                            throw new InternalError();
                                    }
                            }
                        } else {
                            unsafe.putBoolean(obj, key, Bits.getBoolean(buf, off));
                        }
                    }
                }
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: package-private */
        public void getObjFieldValues(Object obj, Object[] vals) {
            if (obj != null) {
                int i = this.numPrimFields;
                while (i < this.fields.length) {
                    char c = this.typeCodes[i];
                    if (c == 'L' || c == '[') {
                        vals[this.offsets[i]] = unsafe.getObject(obj, this.readKeys[i]);
                        i++;
                    } else {
                        throw new InternalError();
                    }
                }
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: package-private */
        public void setObjFieldValues(Object obj, Object[] vals) {
            if (obj != null) {
                for (int i = this.numPrimFields; i < this.fields.length; i++) {
                    long key = this.writeKeys[i];
                    if (key != -1) {
                        char c = this.typeCodes[i];
                        if (c == 'L' || c == '[') {
                            Object val = vals[this.offsets[i]];
                            if (val == null || this.types[i - this.numPrimFields].isInstance(val)) {
                                unsafe.putObject(obj, key, val);
                            } else {
                                Field f = this.fields[i].getField();
                                throw new ClassCastException("cannot assign instance of " + val.getClass().getName() + " to field " + f.getDeclaringClass().getName() + "." + f.getName() + " of type " + f.getType().getName() + " in instance of " + obj.getClass().getName());
                            }
                        } else {
                            throw new InternalError();
                        }
                    }
                }
                return;
            }
            throw new NullPointerException();
        }
    }

    private static class FieldReflectorKey extends WeakReference<Class<?>> {
        private final int hash;
        private final boolean nullClass;
        private final String sigs;

        FieldReflectorKey(Class<?> cl, ObjectStreamField[] fields, ReferenceQueue<Class<?>> queue) {
            super(cl, queue);
            this.nullClass = cl == null;
            StringBuilder sbuf = new StringBuilder();
            for (ObjectStreamField f : fields) {
                sbuf.append(f.getName());
                sbuf.append(f.getSignature());
            }
            this.sigs = sbuf.toString();
            this.hash = System.identityHashCode(cl) + this.sigs.hashCode();
        }

        public int hashCode() {
            return this.hash;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
            if (r4 == r1.get()) goto L_0x0024;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
            if (r5.sigs.equals(r1.sigs) != false) goto L_0x0030;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0012, code lost:
            if (r1.nullClass != false) goto L_0x0024;
         */
        public boolean equals(Object obj) {
            boolean z = true;
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof FieldReflectorKey)) {
                return false;
            }
            FieldReflectorKey other = (FieldReflectorKey) obj;
            if (!this.nullClass) {
                Class<?> cls = (Class) get();
                Class<?> referent = cls;
                if (cls != null) {
                }
                z = false;
                return z;
            }
        }
    }

    private static class MemberSignature {
        public final Member member;
        public final String name;
        public final String signature;

        public MemberSignature(Field field) {
            this.member = field;
            this.name = field.getName();
            this.signature = ObjectStreamClass.getClassSignature(field.getType());
        }

        public MemberSignature(Constructor<?> cons) {
            this.member = cons;
            this.name = cons.getName();
            this.signature = ObjectStreamClass.getMethodSignature(cons.getParameterTypes(), Void.TYPE);
        }

        public MemberSignature(Method meth) {
            this.member = meth;
            this.name = meth.getName();
            this.signature = ObjectStreamClass.getMethodSignature(meth.getParameterTypes(), meth.getReturnType());
        }
    }

    static class WeakClassKey extends WeakReference<Class<?>> {
        private final int hash;

        WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
            super(cl, refQueue);
            this.hash = System.identityHashCode(cl);
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof WeakClassKey)) {
                return false;
            }
            Object referent = get();
            if (referent == null || referent != ((WeakClassKey) obj).get()) {
                z = false;
            }
            return z;
        }
    }

    private static native boolean hasStaticInitializer(Class<?> cls, boolean z);

    public static ObjectStreamClass lookup(Class<?> cl2) {
        return lookup(cl2, false);
    }

    public static ObjectStreamClass lookupAny(Class<?> cl2) {
        return lookup(cl2, true);
    }

    public String getName() {
        return this.name;
    }

    public long getSerialVersionUID() {
        if (this.suid == null) {
            this.suid = (Long) AccessController.doPrivileged(new PrivilegedAction<Long>() {
                public Long run() {
                    return Long.valueOf(ObjectStreamClass.computeDefaultSUID(ObjectStreamClass.this.cl));
                }
            });
        }
        return this.suid.longValue();
    }

    @CallerSensitive
    public Class<?> forClass() {
        if (this.cl == null) {
            return null;
        }
        requireInitialized();
        if (System.getSecurityManager() != null && ReflectUtil.needsPackageAccessCheck(VMStack.getCallingClassLoader(), this.cl.getClassLoader())) {
            ReflectUtil.checkPackageAccess(this.cl);
        }
        return this.cl;
    }

    public ObjectStreamField[] getFields() {
        return getFields(true);
    }

    public ObjectStreamField getField(String name2) {
        return getField(name2, null);
    }

    public String toString() {
        return this.name + ": static final long serialVersionUID = " + getSerialVersionUID() + "L;";
    }

    static ObjectStreamClass lookup(Class<?> cl2, boolean all) {
        if (!all && !Serializable.class.isAssignableFrom(cl2)) {
            return null;
        }
        processQueue(Caches.localDescsQueue, Caches.localDescs);
        WeakClassKey key = new WeakClassKey(cl2, Caches.localDescsQueue);
        Reference<?> ref = Caches.localDescs.get(key);
        Object entry = null;
        if (ref != null) {
            entry = ref.get();
        }
        EntryFuture future = null;
        if (entry == null) {
            EntryFuture newEntry = new EntryFuture();
            Reference<?> newRef = new SoftReference<>(newEntry);
            do {
                if (ref != null) {
                    Caches.localDescs.remove(key, ref);
                }
                ref = Caches.localDescs.putIfAbsent(key, newRef);
                if (ref != null) {
                    entry = ref.get();
                }
                if (ref == null) {
                    break;
                }
            } while (entry == null);
            if (entry == null) {
                future = newEntry;
            }
        }
        if (entry instanceof ObjectStreamClass) {
            return (ObjectStreamClass) entry;
        }
        if (entry instanceof EntryFuture) {
            future = (EntryFuture) entry;
            if (future.getOwner() == Thread.currentThread()) {
                entry = null;
            } else {
                entry = future.get();
            }
        }
        if (entry == null) {
            try {
                entry = new ObjectStreamClass(cl2);
            } catch (Throwable th) {
                entry = th;
            }
            entry = entry;
            if (future.set(entry)) {
                Caches.localDescs.put(key, new SoftReference(entry));
            } else {
                entry = future.get();
            }
        }
        if (entry instanceof ObjectStreamClass) {
            return (ObjectStreamClass) entry;
        }
        if (entry instanceof RuntimeException) {
            throw ((RuntimeException) entry);
        } else if (entry instanceof Error) {
            throw ((Error) entry);
        } else {
            throw new InternalError("unexpected entry: " + entry);
        }
    }

    private ObjectStreamClass(final Class<?> cl2) {
        this.cl = cl2;
        this.name = cl2.getName();
        this.isProxy = Proxy.isProxyClass(cl2);
        this.isEnum = Enum.class.isAssignableFrom(cl2);
        this.serializable = Serializable.class.isAssignableFrom(cl2);
        this.externalizable = Externalizable.class.isAssignableFrom(cl2);
        Class<? super Object> superclass = cl2.getSuperclass();
        this.superDesc = superclass != null ? lookup(superclass, false) : null;
        this.localDesc = this;
        if (this.serializable) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    if (ObjectStreamClass.this.isEnum) {
                        Long unused = ObjectStreamClass.this.suid = 0L;
                        ObjectStreamField[] unused2 = ObjectStreamClass.this.fields = ObjectStreamClass.NO_FIELDS;
                        return null;
                    } else if (cl2.isArray()) {
                        ObjectStreamField[] unused3 = ObjectStreamClass.this.fields = ObjectStreamClass.NO_FIELDS;
                        return null;
                    } else {
                        Long unused4 = ObjectStreamClass.this.suid = ObjectStreamClass.getDeclaredSUID(cl2);
                        try {
                            ObjectStreamField[] unused5 = ObjectStreamClass.this.fields = ObjectStreamClass.getSerialFields(cl2);
                            ObjectStreamClass.this.computeFieldOffsets();
                        } catch (InvalidClassException e) {
                            ExceptionInfo unused6 = ObjectStreamClass.this.serializeEx = ObjectStreamClass.this.deserializeEx = new ExceptionInfo(e.classname, e.getMessage());
                            ObjectStreamField[] unused7 = ObjectStreamClass.this.fields = ObjectStreamClass.NO_FIELDS;
                        }
                        if (ObjectStreamClass.this.externalizable) {
                            Constructor unused8 = ObjectStreamClass.this.cons = ObjectStreamClass.getExternalizableConstructor(cl2);
                        } else {
                            Constructor unused9 = ObjectStreamClass.this.cons = ObjectStreamClass.getSerializableConstructor(cl2);
                            boolean z = true;
                            Method unused10 = ObjectStreamClass.this.writeObjectMethod = ObjectStreamClass.getPrivateMethod(cl2, "writeObject", new Class[]{ObjectOutputStream.class}, Void.TYPE);
                            Method unused11 = ObjectStreamClass.this.readObjectMethod = ObjectStreamClass.getPrivateMethod(cl2, "readObject", new Class[]{ObjectInputStream.class}, Void.TYPE);
                            Method unused12 = ObjectStreamClass.this.readObjectNoDataMethod = ObjectStreamClass.getPrivateMethod(cl2, "readObjectNoData", null, Void.TYPE);
                            ObjectStreamClass objectStreamClass = ObjectStreamClass.this;
                            if (ObjectStreamClass.this.writeObjectMethod == null) {
                                z = false;
                            }
                            boolean unused13 = objectStreamClass.hasWriteObjectData = z;
                        }
                        Method unused14 = ObjectStreamClass.this.writeReplaceMethod = ObjectStreamClass.getInheritableMethod(cl2, "writeReplace", null, Object.class);
                        Method unused15 = ObjectStreamClass.this.readResolveMethod = ObjectStreamClass.getInheritableMethod(cl2, "readResolve", null, Object.class);
                        return null;
                    }
                }
            });
        } else {
            this.suid = 0L;
            this.fields = NO_FIELDS;
        }
        try {
            this.fieldRefl = getReflector(this.fields, this);
            if (this.deserializeEx == null) {
                if (this.isEnum) {
                    this.deserializeEx = new ExceptionInfo(this.name, "enum type");
                } else if (this.cons == null) {
                    this.deserializeEx = new ExceptionInfo(this.name, "no valid constructor");
                }
            }
            for (ObjectStreamField field : this.fields) {
                if (field.getField() == null) {
                    this.defaultSerializeEx = new ExceptionInfo(this.name, "unmatched serializable field(s) declared");
                }
            }
            this.initialized = true;
        } catch (InvalidClassException ex) {
            throw new InternalError((Throwable) ex);
        }
    }

    ObjectStreamClass() {
    }

    /* access modifiers changed from: package-private */
    public void initProxy(Class<?> cl2, ClassNotFoundException resolveEx2, ObjectStreamClass superDesc2) throws InvalidClassException {
        ObjectStreamClass osc = null;
        if (cl2 != null) {
            osc = lookup(cl2, true);
            if (!osc.isProxy) {
                throw new InvalidClassException("cannot bind proxy descriptor to a non-proxy class");
            }
        }
        this.cl = cl2;
        this.resolveEx = resolveEx2;
        this.superDesc = superDesc2;
        this.isProxy = true;
        this.serializable = true;
        this.suid = 0L;
        this.fields = NO_FIELDS;
        if (osc != null) {
            this.localDesc = osc;
            this.name = this.localDesc.name;
            this.externalizable = this.localDesc.externalizable;
            this.writeReplaceMethod = this.localDesc.writeReplaceMethod;
            this.readResolveMethod = this.localDesc.readResolveMethod;
            this.deserializeEx = this.localDesc.deserializeEx;
            this.cons = this.localDesc.cons;
        }
        this.fieldRefl = getReflector(this.fields, this.localDesc);
        this.initialized = true;
    }

    /* access modifiers changed from: package-private */
    public void initNonProxy(ObjectStreamClass model, Class<?> cl2, ClassNotFoundException resolveEx2, ObjectStreamClass superDesc2) throws InvalidClassException {
        long suid2 = Long.valueOf(model.getSerialVersionUID()).longValue();
        ObjectStreamClass osc = null;
        if (cl2 != null) {
            osc = lookup(cl2, true);
            if (osc.isProxy) {
                throw new InvalidClassException("cannot bind non-proxy descriptor to a proxy class");
            } else if (model.isEnum != osc.isEnum) {
                throw new InvalidClassException(model.isEnum ? "cannot bind enum descriptor to a non-enum class" : "cannot bind non-enum descriptor to an enum class");
            } else if (model.serializable == osc.serializable && !cl2.isArray() && suid2 != osc.getSerialVersionUID()) {
                String str = osc.name;
                throw new InvalidClassException(str, "local class incompatible: stream classdesc serialVersionUID = " + suid2 + ", local class serialVersionUID = " + osc.getSerialVersionUID());
            } else if (!classNamesEqual(model.name, osc.name)) {
                String str2 = osc.name;
                throw new InvalidClassException(str2, "local class name incompatible with stream class name \"" + model.name + "\"");
            } else if (!model.isEnum) {
                if (model.serializable == osc.serializable && model.externalizable != osc.externalizable) {
                    throw new InvalidClassException(osc.name, "Serializable incompatible with Externalizable");
                } else if (!(model.serializable == osc.serializable && model.externalizable == osc.externalizable && (model.serializable || model.externalizable))) {
                    this.deserializeEx = new ExceptionInfo(osc.name, "class invalid for deserialization");
                }
            }
        }
        this.cl = cl2;
        this.resolveEx = resolveEx2;
        this.superDesc = superDesc2;
        this.name = model.name;
        this.suid = Long.valueOf(suid2);
        this.isProxy = false;
        this.isEnum = model.isEnum;
        this.serializable = model.serializable;
        this.externalizable = model.externalizable;
        this.hasBlockExternalData = model.hasBlockExternalData;
        this.hasWriteObjectData = model.hasWriteObjectData;
        this.fields = model.fields;
        this.primDataSize = model.primDataSize;
        this.numObjFields = model.numObjFields;
        if (osc != null) {
            this.localDesc = osc;
            this.writeObjectMethod = this.localDesc.writeObjectMethod;
            this.readObjectMethod = this.localDesc.readObjectMethod;
            this.readObjectNoDataMethod = this.localDesc.readObjectNoDataMethod;
            this.writeReplaceMethod = this.localDesc.writeReplaceMethod;
            this.readResolveMethod = this.localDesc.readResolveMethod;
            if (this.deserializeEx == null) {
                this.deserializeEx = this.localDesc.deserializeEx;
            }
            this.cons = this.localDesc.cons;
        }
        this.fieldRefl = getReflector(this.fields, this.localDesc);
        this.fields = this.fieldRefl.getFields();
        this.initialized = true;
    }

    /* access modifiers changed from: package-private */
    public void readNonProxy(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.name = in.readUTF();
        this.suid = Long.valueOf(in.readLong());
        this.isProxy = false;
        byte flags = in.readByte();
        this.hasWriteObjectData = (flags & 1) != 0;
        this.hasBlockExternalData = (flags & 8) != 0;
        this.externalizable = (flags & 4) != 0;
        boolean sflag = (flags & 2) != 0;
        if (!this.externalizable || !sflag) {
            this.serializable = this.externalizable || sflag;
            this.isEnum = (flags & 16) != 0;
            if (!this.isEnum || this.suid.longValue() == 0) {
                int numFields = in.readShort();
                if (!this.isEnum || numFields == 0) {
                    this.fields = numFields > 0 ? new ObjectStreamField[numFields] : NO_FIELDS;
                    int i = 0;
                    while (i < numFields) {
                        char tcode = (char) in.readByte();
                        try {
                            this.fields[i] = new ObjectStreamField(in.readUTF(), (tcode == 'L' || tcode == '[') ? in.readTypeString() : new String(new char[]{tcode}), false);
                            i++;
                        } catch (RuntimeException e) {
                            throw ((IOException) new InvalidClassException(this.name, "invalid descriptor for field " + fname).initCause(e));
                        }
                    }
                    computeFieldOffsets();
                    return;
                }
                throw new InvalidClassException(this.name, "enum descriptor has non-zero field count: " + numFields);
            }
            throw new InvalidClassException(this.name, "enum descriptor has non-zero serialVersionUID: " + this.suid);
        }
        throw new InvalidClassException(this.name, "serializable and externalizable flags conflict");
    }

    /* access modifiers changed from: package-private */
    public void writeNonProxy(ObjectOutputStream out) throws IOException {
        out.writeUTF(this.name);
        out.writeLong(getSerialVersionUID());
        byte flags = 0;
        if (this.externalizable) {
            flags = (byte) (0 | 4);
            if (out.getProtocolVersion() != 1) {
                flags = (byte) (flags | 8);
            }
        } else if (this.serializable) {
            flags = (byte) (0 | 2);
        }
        if (this.hasWriteObjectData) {
            flags = (byte) (flags | 1);
        }
        if (this.isEnum) {
            flags = (byte) (flags | 16);
        }
        out.writeByte(flags);
        out.writeShort(this.fields.length);
        for (ObjectStreamField f : this.fields) {
            out.writeByte(f.getTypeCode());
            out.writeUTF(f.getName());
            if (!f.isPrimitive()) {
                out.writeTypeString(f.getTypeString());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ClassNotFoundException getResolveException() {
        return this.resolveEx;
    }

    private final void requireInitialized() {
        if (!this.initialized) {
            throw new InternalError("Unexpected call when not initialized");
        }
    }

    /* access modifiers changed from: package-private */
    public void checkDeserialize() throws InvalidClassException {
        requireInitialized();
        if (this.deserializeEx != null) {
            throw this.deserializeEx.newInvalidClassException();
        }
    }

    /* access modifiers changed from: package-private */
    public void checkSerialize() throws InvalidClassException {
        requireInitialized();
        if (this.serializeEx != null) {
            throw this.serializeEx.newInvalidClassException();
        }
    }

    /* access modifiers changed from: package-private */
    public void checkDefaultSerialize() throws InvalidClassException {
        requireInitialized();
        if (this.defaultSerializeEx != null) {
            throw this.defaultSerializeEx.newInvalidClassException();
        }
    }

    /* access modifiers changed from: package-private */
    public ObjectStreamClass getSuperDesc() {
        requireInitialized();
        return this.superDesc;
    }

    /* access modifiers changed from: package-private */
    public ObjectStreamClass getLocalDesc() {
        requireInitialized();
        return this.localDesc;
    }

    /* access modifiers changed from: package-private */
    public ObjectStreamField[] getFields(boolean copy) {
        return copy ? (ObjectStreamField[]) this.fields.clone() : this.fields;
    }

    /* access modifiers changed from: package-private */
    public ObjectStreamField getField(String name2, Class<?> type) {
        for (ObjectStreamField f : this.fields) {
            if (f.getName().equals(name2)) {
                if (type == null || (type == Object.class && !f.isPrimitive())) {
                    return f;
                }
                Class<?> ftype = f.getType();
                if (ftype != null && type.isAssignableFrom(ftype)) {
                    return f;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isProxy() {
        requireInitialized();
        return this.isProxy;
    }

    /* access modifiers changed from: package-private */
    public boolean isEnum() {
        requireInitialized();
        return this.isEnum;
    }

    /* access modifiers changed from: package-private */
    public boolean isExternalizable() {
        requireInitialized();
        return this.externalizable;
    }

    /* access modifiers changed from: package-private */
    public boolean isSerializable() {
        requireInitialized();
        return this.serializable;
    }

    /* access modifiers changed from: package-private */
    public boolean hasBlockExternalData() {
        requireInitialized();
        return this.hasBlockExternalData;
    }

    /* access modifiers changed from: package-private */
    public boolean hasWriteObjectData() {
        requireInitialized();
        return this.hasWriteObjectData;
    }

    /* access modifiers changed from: package-private */
    public boolean isInstantiable() {
        requireInitialized();
        return this.cons != null;
    }

    /* access modifiers changed from: package-private */
    public boolean hasWriteObjectMethod() {
        requireInitialized();
        return this.writeObjectMethod != null;
    }

    /* access modifiers changed from: package-private */
    public boolean hasReadObjectMethod() {
        requireInitialized();
        return this.readObjectMethod != null;
    }

    /* access modifiers changed from: package-private */
    public boolean hasReadObjectNoDataMethod() {
        requireInitialized();
        return this.readObjectNoDataMethod != null;
    }

    /* access modifiers changed from: package-private */
    public boolean hasWriteReplaceMethod() {
        requireInitialized();
        return this.writeReplaceMethod != null;
    }

    /* access modifiers changed from: package-private */
    public boolean hasReadResolveMethod() {
        requireInitialized();
        return this.readResolveMethod != null;
    }

    /* access modifiers changed from: package-private */
    public Object newInstance() throws InstantiationException, InvocationTargetException, UnsupportedOperationException {
        requireInitialized();
        if (this.cons != null) {
            try {
                return this.cons.newInstance(new Object[0]);
            } catch (IllegalAccessException ex) {
                throw new InternalError((Throwable) ex);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public void invokeWriteObject(Object obj, ObjectOutputStream out) throws IOException, UnsupportedOperationException {
        requireInitialized();
        if (this.writeObjectMethod != null) {
            try {
                this.writeObjectMethod.invoke(obj, out);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (!(th instanceof IOException)) {
                    throwMiscException(th);
                    return;
                }
                throw ((IOException) th);
            } catch (IllegalAccessException ex2) {
                throw new InternalError((Throwable) ex2);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public void invokeReadObject(Object obj, ObjectInputStream in) throws ClassNotFoundException, IOException, UnsupportedOperationException {
        requireInitialized();
        if (this.readObjectMethod != null) {
            try {
                this.readObjectMethod.invoke(obj, in);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ClassNotFoundException) {
                    throw ((ClassNotFoundException) th);
                } else if (!(th instanceof IOException)) {
                    throwMiscException(th);
                } else {
                    throw ((IOException) th);
                }
            } catch (IllegalAccessException ex2) {
                throw new InternalError((Throwable) ex2);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public void invokeReadObjectNoData(Object obj) throws IOException, UnsupportedOperationException {
        requireInitialized();
        if (this.readObjectNoDataMethod != null) {
            try {
                this.readObjectNoDataMethod.invoke(obj, null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (!(th instanceof ObjectStreamException)) {
                    throwMiscException(th);
                    return;
                }
                throw ((ObjectStreamException) th);
            } catch (IllegalAccessException ex2) {
                throw new InternalError((Throwable) ex2);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public Object invokeWriteReplace(Object obj) throws IOException, UnsupportedOperationException {
        requireInitialized();
        if (this.writeReplaceMethod != null) {
            try {
                return this.writeReplaceMethod.invoke(obj, null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw ((ObjectStreamException) th);
                }
                throwMiscException(th);
                throw new InternalError(th);
            } catch (IllegalAccessException ex2) {
                throw new InternalError((Throwable) ex2);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public Object invokeReadResolve(Object obj) throws IOException, UnsupportedOperationException {
        requireInitialized();
        if (this.readResolveMethod != null) {
            try {
                return this.readResolveMethod.invoke(obj, null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw ((ObjectStreamException) th);
                }
                throwMiscException(th);
                throw new InternalError(th);
            } catch (IllegalAccessException ex2) {
                throw new InternalError((Throwable) ex2);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public ClassDataSlot[] getClassDataLayout() throws InvalidClassException {
        if (this.dataLayout == null) {
            this.dataLayout = getClassDataLayout0();
        }
        return this.dataLayout;
    }

    private ClassDataSlot[] getClassDataLayout0() throws InvalidClassException {
        ArrayList<ClassDataSlot> slots = new ArrayList<>();
        Class<?> start = this.cl;
        Class<?> end = this.cl;
        while (end != null && Serializable.class.isAssignableFrom(end)) {
            end = end.getSuperclass();
        }
        HashSet<String> oscNames = new HashSet<>(3);
        Class<? super Object> start2 = start;
        ObjectStreamClass d = this;
        while (d != null) {
            if (!oscNames.contains(d.name)) {
                oscNames.add(d.name);
                String searchName = d.cl != null ? d.cl.getName() : d.name;
                Class<?> match = null;
                Class<? super Object> c = start2;
                while (true) {
                    if (c == end) {
                        break;
                    } else if (searchName.equals(c.getName())) {
                        match = c;
                        break;
                    } else {
                        c = c.getSuperclass();
                    }
                }
                if (match != null) {
                    for (Class<?> c2 = start2; c2 != match; c2 = c2.getSuperclass()) {
                        slots.add(new ClassDataSlot(lookup(c2, true), false));
                    }
                    start2 = match.getSuperclass();
                }
                slots.add(new ClassDataSlot(d.getVariantFor(match), true));
                d = d.superDesc;
            } else {
                throw new InvalidClassException("Circular reference.");
            }
        }
        for (Class<? super Object> c3 = start2; c3 != end; c3 = c3.getSuperclass()) {
            slots.add(new ClassDataSlot(lookup(c3, true), false));
        }
        Collections.reverse(slots);
        return (ClassDataSlot[]) slots.toArray(new ClassDataSlot[slots.size()]);
    }

    /* access modifiers changed from: package-private */
    public int getPrimDataSize() {
        return this.primDataSize;
    }

    /* access modifiers changed from: package-private */
    public int getNumObjFields() {
        return this.numObjFields;
    }

    /* access modifiers changed from: package-private */
    public void getPrimFieldValues(Object obj, byte[] buf) {
        this.fieldRefl.getPrimFieldValues(obj, buf);
    }

    /* access modifiers changed from: package-private */
    public void setPrimFieldValues(Object obj, byte[] buf) {
        this.fieldRefl.setPrimFieldValues(obj, buf);
    }

    /* access modifiers changed from: package-private */
    public void getObjFieldValues(Object obj, Object[] vals) {
        this.fieldRefl.getObjFieldValues(obj, vals);
    }

    /* access modifiers changed from: package-private */
    public void setObjFieldValues(Object obj, Object[] vals) {
        this.fieldRefl.setObjFieldValues(obj, vals);
    }

    /* access modifiers changed from: private */
    public void computeFieldOffsets() throws InvalidClassException {
        this.primDataSize = 0;
        this.numObjFields = 0;
        int firstObjIndex = -1;
        for (int i = 0; i < this.fields.length; i++) {
            ObjectStreamField f = this.fields[i];
            switch (f.getTypeCode()) {
                case 'B':
                case 'Z':
                    int i2 = this.primDataSize;
                    this.primDataSize = i2 + 1;
                    f.setOffset(i2);
                    break;
                case 'C':
                case 'S':
                    f.setOffset(this.primDataSize);
                    this.primDataSize += 2;
                    break;
                case 'D':
                case 'J':
                    f.setOffset(this.primDataSize);
                    this.primDataSize += 8;
                    break;
                case Types.DATALINK /*70*/:
                case 'I':
                    f.setOffset(this.primDataSize);
                    this.primDataSize += 4;
                    break;
                case 'L':
                case Types.DATE /*91*/:
                    int i3 = this.numObjFields;
                    this.numObjFields = i3 + 1;
                    f.setOffset(i3);
                    if (firstObjIndex != -1) {
                        break;
                    } else {
                        firstObjIndex = i;
                        break;
                    }
                default:
                    throw new InternalError();
            }
        }
        if (firstObjIndex != -1 && this.numObjFields + firstObjIndex != this.fields.length) {
            throw new InvalidClassException(this.name, "illegal field order");
        }
    }

    private ObjectStreamClass getVariantFor(Class<?> cl2) throws InvalidClassException {
        if (this.cl == cl2) {
            return this;
        }
        ObjectStreamClass desc = new ObjectStreamClass();
        if (this.isProxy) {
            desc.initProxy(cl2, null, this.superDesc);
        } else {
            desc.initNonProxy(this, cl2, null, this.superDesc);
        }
        return desc;
    }

    /* access modifiers changed from: private */
    public static Constructor<?> getExternalizableConstructor(Class<?> cl2) {
        Constructor<?> constructor = null;
        try {
            Constructor<?> cons2 = cl2.getDeclaredConstructor(null);
            cons2.setAccessible(true);
            if ((1 & cons2.getModifiers()) != 0) {
                constructor = cons2;
            }
            return constructor;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static Constructor<?> getSerializableConstructor(Class<?> cl2) {
        Class<?> initCl = cl2;
        while (Serializable.class.isAssignableFrom(initCl)) {
            Class<? super Object> superclass = initCl.getSuperclass();
            initCl = superclass;
            if (superclass == null) {
                return null;
            }
        }
        try {
            Constructor<?> cons2 = initCl.getDeclaredConstructor(null);
            int mods = cons2.getModifiers();
            if ((mods & 2) == 0) {
                if ((mods & 5) != 0 || packageEquals(cl2, initCl)) {
                    if (cons2.getDeclaringClass() != cl2) {
                        cons2 = cons2.serializationCopy(cons2.getDeclaringClass(), cl2);
                    }
                    cons2.setAccessible(true);
                    return cons2;
                }
            }
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static Method getInheritableMethod(Class<?> cl2, String name2, Class<?>[] argTypes, Class<?> returnType) {
        Method meth = null;
        Class<? super Object> defCl = cl2;
        while (true) {
            if (defCl == null) {
                break;
            }
            try {
                meth = defCl.getDeclaredMethod(name2, argTypes);
                break;
            } catch (NoSuchMethodException e) {
                defCl = defCl.getSuperclass();
            }
        }
        Method method = null;
        if (meth == null || meth.getReturnType() != returnType) {
            return null;
        }
        meth.setAccessible(true);
        int mods = meth.getModifiers();
        if ((mods & 1032) != 0) {
            return null;
        }
        if ((mods & 5) != 0) {
            return meth;
        }
        if ((mods & 2) != 0) {
            if (cl2 == defCl) {
                method = meth;
            }
            return method;
        }
        if (packageEquals(cl2, defCl)) {
            method = meth;
        }
        return method;
    }

    /* access modifiers changed from: private */
    public static Method getPrivateMethod(Class<?> cl2, String name2, Class<?>[] argTypes, Class<?> returnType) {
        Method method = null;
        try {
            Method meth = cl2.getDeclaredMethod(name2, argTypes);
            meth.setAccessible(true);
            int mods = meth.getModifiers();
            if (meth.getReturnType() == returnType && (mods & 8) == 0 && (mods & 2) != 0) {
                method = meth;
            }
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static boolean packageEquals(Class<?> cl1, Class<?> cl2) {
        return cl1.getClassLoader() == cl2.getClassLoader() && getPackageName(cl1).equals(getPackageName(cl2));
    }

    private static String getPackageName(Class<?> cl2) {
        String s = cl2.getName();
        int i = s.lastIndexOf(91);
        if (i >= 0) {
            s = s.substring(i + 2);
        }
        int i2 = s.lastIndexOf(46);
        return i2 >= 0 ? s.substring(0, i2) : "";
    }

    private static boolean classNamesEqual(String name1, String name2) {
        return name1.substring(name1.lastIndexOf(46) + 1).equals(name2.substring(name2.lastIndexOf(46) + 1));
    }

    /* access modifiers changed from: private */
    public static String getClassSignature(Class<?> cl2) {
        StringBuilder sbuf = new StringBuilder();
        while (cl2.isArray()) {
            sbuf.append('[');
            cl2 = cl2.getComponentType();
        }
        if (!cl2.isPrimitive()) {
            sbuf.append('L' + cl2.getName().replace('.', '/') + ';');
        } else if (cl2 == Integer.TYPE) {
            sbuf.append('I');
        } else if (cl2 == Byte.TYPE) {
            sbuf.append('B');
        } else if (cl2 == Long.TYPE) {
            sbuf.append('J');
        } else if (cl2 == Float.TYPE) {
            sbuf.append('F');
        } else if (cl2 == Double.TYPE) {
            sbuf.append('D');
        } else if (cl2 == Short.TYPE) {
            sbuf.append('S');
        } else if (cl2 == Character.TYPE) {
            sbuf.append('C');
        } else if (cl2 == Boolean.TYPE) {
            sbuf.append('Z');
        } else if (cl2 == Void.TYPE) {
            sbuf.append('V');
        } else {
            throw new InternalError();
        }
        return sbuf.toString();
    }

    /* access modifiers changed from: private */
    public static String getMethodSignature(Class<?>[] paramTypes, Class<?> retType) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append('(');
        for (Class<?> classSignature : paramTypes) {
            sbuf.append(getClassSignature(classSignature));
        }
        sbuf.append(')');
        sbuf.append(getClassSignature(retType));
        return sbuf.toString();
    }

    private static void throwMiscException(Throwable th) throws IOException {
        if (th instanceof RuntimeException) {
            throw ((RuntimeException) th);
        } else if (th instanceof Error) {
            throw ((Error) th);
        } else {
            IOException ex = new IOException("unexpected exception type");
            ex.initCause(th);
            throw ex;
        }
    }

    /* access modifiers changed from: private */
    public static ObjectStreamField[] getSerialFields(Class<?> cl2) throws InvalidClassException {
        ObjectStreamField[] fields2;
        if (!Serializable.class.isAssignableFrom(cl2) || Externalizable.class.isAssignableFrom(cl2) || Proxy.isProxyClass(cl2) || cl2.isInterface()) {
            fields2 = NO_FIELDS;
        } else {
            ObjectStreamField[] declaredSerialFields = getDeclaredSerialFields(cl2);
            fields2 = declaredSerialFields;
            if (declaredSerialFields == null) {
                fields2 = getDefaultSerialFields(cl2);
            }
            Arrays.sort((Object[]) fields2);
        }
        return fields2;
    }

    private static ObjectStreamField[] getDeclaredSerialFields(Class<?> cl2) throws InvalidClassException {
        ObjectStreamField[] serialPersistentFields2 = null;
        try {
            Field f = cl2.getDeclaredField("serialPersistentFields");
            if ((f.getModifiers() & 26) == 26) {
                f.setAccessible(true);
                serialPersistentFields2 = (ObjectStreamField[]) f.get(null);
            }
        } catch (Exception e) {
        }
        if (serialPersistentFields2 == null) {
            return null;
        }
        if (serialPersistentFields2.length == 0) {
            return NO_FIELDS;
        }
        ObjectStreamField[] boundFields = new ObjectStreamField[serialPersistentFields2.length];
        Set<String> fieldNames = new HashSet<>(serialPersistentFields2.length);
        int i = 0;
        while (i < serialPersistentFields2.length) {
            ObjectStreamField spf = serialPersistentFields2[i];
            String fname = spf.getName();
            if (!fieldNames.contains(fname)) {
                fieldNames.add(fname);
                try {
                    Field f2 = cl2.getDeclaredField(fname);
                    if (f2.getType() == spf.getType() && (f2.getModifiers() & 8) == 0) {
                        boundFields[i] = new ObjectStreamField(f2, spf.isUnshared(), true);
                    }
                } catch (NoSuchFieldException e2) {
                }
                if (boundFields[i] == null) {
                    boundFields[i] = new ObjectStreamField(fname, spf.getType(), spf.isUnshared());
                }
                i++;
            } else {
                throw new InvalidClassException("multiple serializable fields named " + fname);
            }
        }
        return boundFields;
    }

    private static ObjectStreamField[] getDefaultSerialFields(Class<?> cl2) {
        Field[] clFields = cl2.getDeclaredFields();
        ArrayList<ObjectStreamField> list = new ArrayList<>();
        for (int i = 0; i < clFields.length; i++) {
            if ((clFields[i].getModifiers() & 136) == 0) {
                list.add(new ObjectStreamField(clFields[i], false, true));
            }
        }
        int size = list.size();
        if (size == 0) {
            return NO_FIELDS;
        }
        return (ObjectStreamField[]) list.toArray(new ObjectStreamField[size]);
    }

    /* access modifiers changed from: private */
    public static Long getDeclaredSUID(Class<?> cl2) {
        try {
            Field f = cl2.getDeclaredField("serialVersionUID");
            if ((f.getModifiers() & 24) == 24) {
                f.setAccessible(true);
                return Long.valueOf(f.getLong(null));
            }
        } catch (Exception e) {
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static long computeDefaultSUID(Class<?> cl2) {
        char c;
        char c2;
        int i;
        Class<?> cls = cl2;
        if (!Serializable.class.isAssignableFrom(cls) || Proxy.isProxyClass(cl2)) {
            return 0;
        }
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            dout.writeUTF(cl2.getName());
            int classMods = cl2.getModifiers() & 1553;
            Method[] methods = cl2.getDeclaredMethods();
            if ((classMods & 512) != 0) {
                if (methods.length > 0) {
                    i = classMods | 1024;
                } else {
                    i = classMods & -1025;
                }
                classMods = i;
            }
            dout.writeInt(classMods);
            if (!cl2.isArray()) {
                Class<?>[] interfaces = cl2.getInterfaces();
                String[] ifaceNames = new String[interfaces.length];
                for (int i2 = 0; i2 < interfaces.length; i2++) {
                    ifaceNames[i2] = interfaces[i2].getName();
                }
                Arrays.sort((Object[]) ifaceNames);
                for (String writeUTF : ifaceNames) {
                    dout.writeUTF(writeUTF);
                }
            }
            Field[] fields2 = cl2.getDeclaredFields();
            MemberSignature[] fieldSigs = new MemberSignature[fields2.length];
            for (int i3 = 0; i3 < fields2.length; i3++) {
                fieldSigs[i3] = new MemberSignature(fields2[i3]);
            }
            Arrays.sort(fieldSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    return ms1.name.compareTo(ms2.name);
                }
            });
            for (MemberSignature sig : fieldSigs) {
                int mods = sig.member.getModifiers() & 223;
                if ((mods & 2) == 0 || (mods & 136) == 0) {
                    dout.writeUTF(sig.name);
                    dout.writeInt(mods);
                    dout.writeUTF(sig.signature);
                }
            }
            if (hasStaticInitializer(cls, VMRuntime.getRuntime().getTargetSdkVersion() > MAX_SDK_TARGET_FOR_CLINIT_UIDGEN_WORKAROUND)) {
                dout.writeUTF("<clinit>");
                dout.writeInt(8);
                dout.writeUTF("()V");
            }
            Constructor<?>[] cons2 = cl2.getDeclaredConstructors();
            MemberSignature[] consSigs = new MemberSignature[cons2.length];
            for (int i4 = 0; i4 < cons2.length; i4++) {
                consSigs[i4] = new MemberSignature(cons2[i4]);
            }
            Arrays.sort(consSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    return ms1.signature.compareTo(ms2.signature);
                }
            });
            int i5 = 0;
            while (true) {
                c = '/';
                if (i5 >= consSigs.length) {
                    break;
                }
                MemberSignature sig2 = consSigs[i5];
                int mods2 = sig2.member.getModifiers() & 3391;
                if ((mods2 & 2) == 0) {
                    dout.writeUTF("<init>");
                    dout.writeInt(mods2);
                    dout.writeUTF(sig2.signature.replace('/', '.'));
                }
                i5++;
            }
            MemberSignature[] methSigs = new MemberSignature[methods.length];
            for (int i6 = 0; i6 < methods.length; i6++) {
                methSigs[i6] = new MemberSignature(methods[i6]);
            }
            Arrays.sort(methSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    int comp = ms1.name.compareTo(ms2.name);
                    if (comp == 0) {
                        return ms1.signature.compareTo(ms2.signature);
                    }
                    return comp;
                }
            });
            int i7 = 0;
            while (true) {
                int i8 = i7;
                if (i8 >= methSigs.length) {
                    break;
                }
                MemberSignature sig3 = methSigs[i8];
                int mods3 = sig3.member.getModifiers() & 3391;
                if ((mods3 & 2) == 0) {
                    dout.writeUTF(sig3.name);
                    dout.writeInt(mods3);
                    c2 = '/';
                    dout.writeUTF(sig3.signature.replace('/', '.'));
                } else {
                    c2 = c;
                }
                i7 = i8 + 1;
                c = c2;
                Class<?> cls2 = cl2;
            }
            dout.flush();
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] hashBytes = md.digest(bout.toByteArray());
            long hash = 0;
            char c3 = 8;
            int i9 = Math.min(hashBytes.length, 8) - 1;
            while (i9 >= 0) {
                hash = (hash << c3) | ((long) (hashBytes[i9] & Character.DIRECTIONALITY_UNDEFINED));
                i9--;
                bout = bout;
                md = md;
                c3 = 8;
            }
            MessageDigest messageDigest = md;
            return hash;
        } catch (IOException ex) {
            throw new InternalError((Throwable) ex);
        } catch (NoSuchAlgorithmException ex2) {
            throw new SecurityException(ex2.getMessage());
        }
    }

    private static FieldReflector getReflector(ObjectStreamField[] fields2, ObjectStreamClass localDesc2) throws InvalidClassException {
        Class<?> cl2 = (localDesc2 == null || fields2.length <= 0) ? null : localDesc2.cl;
        processQueue(Caches.reflectorsQueue, Caches.reflectors);
        FieldReflectorKey key = new FieldReflectorKey(cl2, fields2, Caches.reflectorsQueue);
        Reference<?> ref = Caches.reflectors.get(key);
        Object entry = null;
        if (ref != null) {
            entry = ref.get();
        }
        EntryFuture future = null;
        if (entry == null) {
            EntryFuture newEntry = new EntryFuture();
            Reference<?> newRef = new SoftReference<>(newEntry);
            do {
                if (ref != null) {
                    Caches.reflectors.remove(key, ref);
                }
                ref = Caches.reflectors.putIfAbsent(key, newRef);
                if (ref != null) {
                    entry = ref.get();
                }
                if (ref == null) {
                    break;
                }
            } while (entry == null);
            if (entry == null) {
                future = newEntry;
            }
        }
        if (entry instanceof FieldReflector) {
            return (FieldReflector) entry;
        }
        if (entry instanceof EntryFuture) {
            entry = ((EntryFuture) entry).get();
        } else if (entry == null) {
            try {
                entry = new FieldReflector(matchFields(fields2, localDesc2));
            } catch (Throwable th) {
                entry = th;
            }
            entry = entry;
            future.set(entry);
            Caches.reflectors.put(key, new SoftReference(entry));
        }
        if (entry instanceof FieldReflector) {
            return (FieldReflector) entry;
        }
        if (entry instanceof InvalidClassException) {
            throw ((InvalidClassException) entry);
        } else if (entry instanceof RuntimeException) {
            throw ((RuntimeException) entry);
        } else if (entry instanceof Error) {
            throw ((Error) entry);
        } else {
            throw new InternalError("unexpected entry: " + entry);
        }
    }

    private static ObjectStreamField[] matchFields(ObjectStreamField[] fields2, ObjectStreamClass localDesc2) throws InvalidClassException {
        ObjectStreamField[] localFields;
        if (localDesc2 != null) {
            localFields = localDesc2.fields;
        } else {
            localFields = NO_FIELDS;
        }
        ObjectStreamField[] matches = new ObjectStreamField[fields2.length];
        for (int i = 0; i < fields2.length; i++) {
            ObjectStreamField f = fields2[i];
            ObjectStreamField m = null;
            for (ObjectStreamField lf : localFields) {
                if (f.getName().equals(lf.getName()) && f.getSignature().equals(lf.getSignature())) {
                    if (lf.getField() != null) {
                        m = new ObjectStreamField(lf.getField(), lf.isUnshared(), false);
                    } else {
                        m = new ObjectStreamField(lf.getName(), lf.getSignature(), lf.isUnshared());
                    }
                }
            }
            if (m == null) {
                m = new ObjectStreamField(f.getName(), f.getSignature(), false);
            }
            m.setOffset(f.getOffset());
            matches[i] = m;
        }
        return matches;
    }

    private static long getConstructorId(Class<?> cls) {
        int targetSdkVersion = VMRuntime.getRuntime().getTargetSdkVersion();
        if (targetSdkVersion <= 0 || targetSdkVersion > 24) {
            throw new UnsupportedOperationException("ObjectStreamClass.getConstructorId(Class<?>) is not supported on SDK " + targetSdkVersion);
        }
        System.logE("WARNING: ObjectStreamClass.getConstructorId(Class<?>) is private API andwill be removed in a future Android release.");
        return 1189998819991197253L;
    }

    private static Object newInstance(Class<?> clazz, long constructorId) {
        int targetSdkVersion = VMRuntime.getRuntime().getTargetSdkVersion();
        if (targetSdkVersion <= 0 || targetSdkVersion > 24) {
            throw new UnsupportedOperationException("ObjectStreamClass.newInstance(Class<?>, long) is not supported on SDK " + targetSdkVersion);
        }
        System.logE("WARNING: ObjectStreamClass.newInstance(Class<?>, long) is private API andwill be removed in a future Android release.");
        return Unsafe.getUnsafe().allocateInstance(clazz);
    }

    static void processQueue(ReferenceQueue<Class<?>> queue, ConcurrentMap<? extends WeakReference<Class<?>>, ?> map) {
        while (true) {
            Reference<? extends Class<?>> poll = queue.poll();
            Reference<? extends Class<?>> ref = poll;
            if (poll != null) {
                map.remove(ref);
            } else {
                return;
            }
        }
    }
}
