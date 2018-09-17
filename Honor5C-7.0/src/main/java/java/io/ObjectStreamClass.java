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
import java.lang.reflect.Modifier;
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
import java.util.concurrent.ConcurrentMap;
import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;
import sun.reflect.misc.ReflectUtil;

public class ObjectStreamClass implements Serializable {
    static final int MAX_SDK_TARGET_FOR_CLINIT_UIDGEN_WORKAROUND = 23;
    public static final ObjectStreamField[] NO_FIELDS = null;
    private static final ObjectStreamField[] serialPersistentFields = null;
    private static final long serialVersionUID = -6120832682080437368L;
    private Class<?> cl;
    private Constructor cons;
    private volatile ClassDataSlot[] dataLayout;
    private ExceptionInfo defaultSerializeEx;
    private ExceptionInfo deserializeEx;
    private boolean externalizable;
    private FieldReflector fieldRefl;
    private ObjectStreamField[] fields;
    private boolean hasBlockExternalData;
    private boolean hasWriteObjectData;
    private boolean isEnum;
    private boolean isProxy;
    private ObjectStreamClass localDesc;
    private String name;
    private int numObjFields;
    private int primDataSize;
    private Method readObjectMethod;
    private Method readObjectNoDataMethod;
    private Method readResolveMethod;
    private ClassNotFoundException resolveEx;
    private boolean serializable;
    private ExceptionInfo serializeEx;
    private volatile Long suid;
    private ObjectStreamClass superDesc;
    private Method writeObjectMethod;
    private Method writeReplaceMethod;

    /* renamed from: java.io.ObjectStreamClass.2 */
    class AnonymousClass2 implements PrivilegedAction<Void> {
        final /* synthetic */ Class val$cl;

        AnonymousClass2(Class val$cl) {
            this.val$cl = val$cl;
        }

        public Void run() {
            boolean z = true;
            if (ObjectStreamClass.this.isEnum) {
                ObjectStreamClass.this.suid = Long.valueOf(0);
                ObjectStreamClass.this.fields = ObjectStreamClass.NO_FIELDS;
                return null;
            } else if (this.val$cl.isArray()) {
                ObjectStreamClass.this.fields = ObjectStreamClass.NO_FIELDS;
                return null;
            } else {
                ObjectStreamClass.this.suid = ObjectStreamClass.getDeclaredSUID(this.val$cl);
                try {
                    ObjectStreamClass.this.fields = ObjectStreamClass.getSerialFields(this.val$cl);
                    ObjectStreamClass.this.computeFieldOffsets();
                } catch (InvalidClassException e) {
                    ObjectStreamClass.this.serializeEx = ObjectStreamClass.this.deserializeEx = new ExceptionInfo(e.classname, e.getMessage());
                    ObjectStreamClass.this.fields = ObjectStreamClass.NO_FIELDS;
                }
                if (ObjectStreamClass.this.externalizable) {
                    ObjectStreamClass.this.cons = ObjectStreamClass.getExternalizableConstructor(this.val$cl);
                } else {
                    ObjectStreamClass.this.cons = ObjectStreamClass.getSerializableConstructor(this.val$cl);
                    ObjectStreamClass.this.writeObjectMethod = ObjectStreamClass.getPrivateMethod(this.val$cl, "writeObject", new Class[]{ObjectOutputStream.class}, Void.TYPE);
                    ObjectStreamClass.this.readObjectMethod = ObjectStreamClass.getPrivateMethod(this.val$cl, "readObject", new Class[]{ObjectInputStream.class}, Void.TYPE);
                    ObjectStreamClass.this.readObjectNoDataMethod = ObjectStreamClass.getPrivateMethod(this.val$cl, "readObjectNoData", null, Void.TYPE);
                    ObjectStreamClass objectStreamClass = ObjectStreamClass.this;
                    if (ObjectStreamClass.this.writeObjectMethod == null) {
                        z = false;
                    }
                    objectStreamClass.hasWriteObjectData = z;
                }
                ObjectStreamClass.this.writeReplaceMethod = ObjectStreamClass.getInheritableMethod(this.val$cl, "writeReplace", null, Object.class);
                ObjectStreamClass.this.readResolveMethod = ObjectStreamClass.getInheritableMethod(this.val$cl, "readResolve", null, Object.class);
                return null;
            }
        }
    }

    private static class Caches {
        static final ConcurrentMap<WeakClassKey, Reference<?>> localDescs = null;
        private static final ReferenceQueue<Class<?>> localDescsQueue = null;
        static final ConcurrentMap<FieldReflectorKey, Reference<?>> reflectors = null;
        private static final ReferenceQueue<Class<?>> reflectorsQueue = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.ObjectStreamClass.Caches.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.ObjectStreamClass.Caches.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectStreamClass.Caches.<clinit>():void");
        }

        private Caches() {
        }
    }

    static class ClassDataSlot {
        final ObjectStreamClass desc;
        final boolean hasData;

        ClassDataSlot(ObjectStreamClass desc, boolean hasData) {
            this.desc = desc;
            this.hasData = hasData;
        }
    }

    private static class EntryFuture {
        private static final Object unset = null;
        private Object entry;
        private final Thread owner;

        /* renamed from: java.io.ObjectStreamClass.EntryFuture.1 */
        class AnonymousClass1 implements PrivilegedAction<Void> {
            final /* synthetic */ EntryFuture this$1;

            AnonymousClass1(EntryFuture this$1) {
                this.this$1 = this$1;
            }

            public /* bridge */ /* synthetic */ Object run() {
                return run();
            }

            public Void m6run() {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.ObjectStreamClass.EntryFuture.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.ObjectStreamClass.EntryFuture.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectStreamClass.EntryFuture.<clinit>():void");
        }

        /* synthetic */ EntryFuture(EntryFuture entryFuture) {
            this();
        }

        private EntryFuture() {
            this.owner = Thread.currentThread();
            this.entry = unset;
        }

        synchronized boolean set(Object entry) {
            if (this.entry != unset) {
                return false;
            }
            this.entry = entry;
            notifyAll();
            return true;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        synchronized Object get() {
            boolean interrupted = false;
            while (true) {
                if (this.entry != unset) {
                    break;
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                AccessController.doPrivileged(new AnonymousClass1(this));
            }
            return this.entry;
        }

        Thread getOwner() {
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

        InvalidClassException newInvalidClassException() {
            return new InvalidClassException(this.className, this.message);
        }
    }

    private static class FieldReflector {
        private static final Unsafe unsafe = null;
        private final ObjectStreamField[] fields;
        private final int numPrimFields;
        private final int[] offsets;
        private final long[] readKeys;
        private final char[] typeCodes;
        private final Class<?>[] types;
        private final long[] writeKeys;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.ObjectStreamClass.FieldReflector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.ObjectStreamClass.FieldReflector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectStreamClass.FieldReflector.<clinit>():void");
        }

        FieldReflector(ObjectStreamField[] fields) {
            this.fields = fields;
            int nfields = fields.length;
            this.readKeys = new long[nfields];
            this.writeKeys = new long[nfields];
            this.offsets = new int[nfields];
            this.typeCodes = new char[nfields];
            ArrayList<Class<?>> typeList = new ArrayList();
            Set<Long> usedKeys = new HashSet();
            for (int i = 0; i < nfields; i++) {
                ObjectStreamField f = fields[i];
                Field rf = f.getField();
                long key = rf != null ? unsafe.objectFieldOffset(rf) : -1;
                this.readKeys[i] = key;
                long[] jArr = this.writeKeys;
                if (!usedKeys.add(Long.valueOf(key))) {
                    key = -1;
                }
                jArr[i] = key;
                this.offsets[i] = f.getOffset();
                this.typeCodes[i] = f.getTypeCode();
                if (!f.isPrimitive()) {
                    Object type;
                    if (rf != null) {
                        type = rf.getType();
                    } else {
                        type = null;
                    }
                    typeList.add(type);
                }
            }
            this.types = (Class[]) typeList.toArray(new Class[typeList.size()]);
            this.numPrimFields = nfields - this.types.length;
        }

        ObjectStreamField[] getFields() {
            return this.fields;
        }

        void getPrimFieldValues(Object obj, byte[] buf) {
            if (obj == null) {
                throw new NullPointerException();
            }
            for (int i = 0; i < this.numPrimFields; i++) {
                long key = this.readKeys[i];
                int off = this.offsets[i];
                switch (this.typeCodes[i]) {
                    case 'B':
                        buf[off] = unsafe.getByte(obj, key);
                        break;
                    case 'C':
                        Bits.putChar(buf, off, unsafe.getChar(obj, key));
                        break;
                    case 'D':
                        Bits.putDouble(buf, off, unsafe.getDouble(obj, key));
                        break;
                    case Types.DATALINK /*70*/:
                        Bits.putFloat(buf, off, unsafe.getFloat(obj, key));
                        break;
                    case 'I':
                        Bits.putInt(buf, off, unsafe.getInt(obj, key));
                        break;
                    case 'J':
                        Bits.putLong(buf, off, unsafe.getLong(obj, key));
                        break;
                    case 'S':
                        Bits.putShort(buf, off, unsafe.getShort(obj, key));
                        break;
                    case 'Z':
                        Bits.putBoolean(buf, off, unsafe.getBoolean(obj, key));
                        break;
                    default:
                        throw new InternalError();
                }
            }
        }

        void setPrimFieldValues(Object obj, byte[] buf) {
            if (obj == null) {
                throw new NullPointerException();
            }
            for (int i = 0; i < this.numPrimFields; i++) {
                long key = this.writeKeys[i];
                if (key != -1) {
                    int off = this.offsets[i];
                    switch (this.typeCodes[i]) {
                        case 'B':
                            unsafe.putByte(obj, key, buf[off]);
                            break;
                        case 'C':
                            unsafe.putChar(obj, key, Bits.getChar(buf, off));
                            break;
                        case 'D':
                            unsafe.putDouble(obj, key, Bits.getDouble(buf, off));
                            break;
                        case Types.DATALINK /*70*/:
                            unsafe.putFloat(obj, key, Bits.getFloat(buf, off));
                            break;
                        case 'I':
                            unsafe.putInt(obj, key, Bits.getInt(buf, off));
                            break;
                        case 'J':
                            unsafe.putLong(obj, key, Bits.getLong(buf, off));
                            break;
                        case 'S':
                            unsafe.putShort(obj, key, Bits.getShort(buf, off));
                            break;
                        case 'Z':
                            unsafe.putBoolean(obj, key, Bits.getBoolean(buf, off));
                            break;
                        default:
                            throw new InternalError();
                    }
                }
            }
        }

        void getObjFieldValues(Object obj, Object[] vals) {
            if (obj == null) {
                throw new NullPointerException();
            }
            int i = this.numPrimFields;
            while (i < this.fields.length) {
                switch (this.typeCodes[i]) {
                    case 'L':
                    case Types.DATE /*91*/:
                        vals[this.offsets[i]] = unsafe.getObject(obj, this.readKeys[i]);
                        i++;
                    default:
                        throw new InternalError();
                }
            }
        }

        void setObjFieldValues(Object obj, Object[] vals) {
            if (obj == null) {
                throw new NullPointerException();
            }
            int i = this.numPrimFields;
            while (i < this.fields.length) {
                long key = this.writeKeys[i];
                if (key != -1) {
                    switch (this.typeCodes[i]) {
                        case 'L':
                        case Types.DATE /*91*/:
                            Object val = vals[this.offsets[i]];
                            if (val == null || this.types[i - this.numPrimFields].isInstance(val)) {
                                unsafe.putObject(obj, key, val);
                                break;
                            } else {
                                Field f = this.fields[i].getField();
                                throw new ClassCastException("cannot assign instance of " + val.getClass().getName() + " to field " + f.getDeclaringClass().getName() + "." + f.getName() + " of type " + f.getType().getName() + " in instance of " + obj.getClass().getName());
                            }
                        default:
                            throw new InternalError();
                    }
                }
                i++;
            }
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
                sbuf.append(f.getName()).append(f.getSignature());
            }
            this.sigs = sbuf.toString();
            this.hash = System.identityHashCode(cl) + this.sigs.hashCode();
        }

        public int hashCode() {
            return this.hash;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof FieldReflectorKey)) {
                return false;
            }
            FieldReflectorKey other = (FieldReflectorKey) obj;
            if (!this.nullClass) {
                Class<?> referent = (Class) get();
                if (referent != null && referent == other.get()) {
                }
                return z;
            }
            z = this.sigs.equals(other.sigs);
            return z;
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

        public MemberSignature(Constructor cons) {
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
            if (referent == null) {
                z = false;
            } else if (referent != ((WeakClassKey) obj).get()) {
                z = false;
            }
            return z;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.ObjectStreamClass.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.ObjectStreamClass.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectStreamClass.<clinit>():void");
    }

    private static native boolean hasStaticInitializer(Class<?> cls, boolean z);

    public static ObjectStreamClass lookup(Class<?> cl) {
        return lookup(cl, false);
    }

    public static ObjectStreamClass lookupAny(Class<?> cl) {
        return lookup(cl, true);
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
        if (System.getSecurityManager() != null && ReflectUtil.needsPackageAccessCheck(VMStack.getCallingClassLoader(), this.cl.getClassLoader())) {
            ReflectUtil.checkPackageAccess(this.cl);
        }
        return this.cl;
    }

    public ObjectStreamField[] getFields() {
        return getFields(true);
    }

    public ObjectStreamField getField(String name) {
        return getField(name, null);
    }

    public String toString() {
        return this.name + ": static final long serialVersionUID = " + getSerialVersionUID() + "L;";
    }

    static ObjectStreamClass lookup(Class<?> cl, boolean all) {
        if (!(!all ? Serializable.class.isAssignableFrom(cl) : true)) {
            return null;
        }
        processQueue(Caches.localDescsQueue, Caches.localDescs);
        WeakClassKey key = new WeakClassKey(cl, Caches.localDescsQueue);
        Reference<?> ref = (Reference) Caches.localDescs.get(key);
        Object entry = null;
        if (ref != null) {
            entry = ref.get();
        }
        EntryFuture future = null;
        if (entry == null) {
            EntryFuture newEntry = new EntryFuture();
            Reference<?> newRef = new SoftReference(newEntry);
            do {
                if (ref != null) {
                    Caches.localDescs.remove(key, ref);
                }
                ref = (Reference) Caches.localDescs.putIfAbsent(key, newRef);
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
                entry = new ObjectStreamClass(cl);
            } catch (Throwable th) {
                Throwable entry2 = th;
            }
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

    private ObjectStreamClass(Class<?> cl) {
        ObjectStreamClass objectStreamClass = null;
        this.hasBlockExternalData = true;
        this.cl = cl;
        this.name = cl.getName();
        this.isProxy = Proxy.isProxyClass(cl);
        this.isEnum = Enum.class.isAssignableFrom(cl);
        this.serializable = Serializable.class.isAssignableFrom(cl);
        this.externalizable = Externalizable.class.isAssignableFrom(cl);
        Class<?> superCl = cl.getSuperclass();
        if (superCl != null) {
            objectStreamClass = lookup(superCl, false);
        }
        this.superDesc = objectStreamClass;
        this.localDesc = this;
        if (this.serializable) {
            AccessController.doPrivileged(new AnonymousClass2(cl));
        } else {
            this.suid = Long.valueOf(0);
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
        } catch (InvalidClassException e) {
            throw new InternalError();
        }
    }

    ObjectStreamClass() {
        this.hasBlockExternalData = true;
    }

    void initProxy(Class<?> cl, ClassNotFoundException resolveEx, ObjectStreamClass superDesc) throws InvalidClassException {
        this.cl = cl;
        this.resolveEx = resolveEx;
        this.superDesc = superDesc;
        this.isProxy = true;
        this.serializable = true;
        this.suid = Long.valueOf(0);
        this.fields = NO_FIELDS;
        if (cl != null) {
            this.localDesc = lookup(cl, true);
            if (this.localDesc.isProxy) {
                this.name = this.localDesc.name;
                this.externalizable = this.localDesc.externalizable;
                this.cons = this.localDesc.cons;
                this.writeReplaceMethod = this.localDesc.writeReplaceMethod;
                this.readResolveMethod = this.localDesc.readResolveMethod;
                this.deserializeEx = this.localDesc.deserializeEx;
            } else {
                throw new InvalidClassException("cannot bind proxy descriptor to a non-proxy class");
            }
        }
        this.fieldRefl = getReflector(this.fields, this.localDesc);
    }

    void initNonProxy(ObjectStreamClass model, Class<?> cl, ClassNotFoundException resolveEx, ObjectStreamClass superDesc) throws InvalidClassException {
        this.cl = cl;
        this.resolveEx = resolveEx;
        this.superDesc = superDesc;
        this.name = model.name;
        this.suid = Long.valueOf(model.getSerialVersionUID());
        this.isProxy = false;
        this.isEnum = model.isEnum;
        this.serializable = model.serializable;
        this.externalizable = model.externalizable;
        this.hasBlockExternalData = model.hasBlockExternalData;
        this.hasWriteObjectData = model.hasWriteObjectData;
        this.fields = model.fields;
        this.primDataSize = model.primDataSize;
        this.numObjFields = model.numObjFields;
        if (cl != null) {
            this.localDesc = lookup(cl, true);
            if (this.localDesc.isProxy) {
                throw new InvalidClassException("cannot bind non-proxy descriptor to a proxy class");
            } else if (this.isEnum != this.localDesc.isEnum) {
                String str;
                if (this.isEnum) {
                    str = "cannot bind enum descriptor to a non-enum class";
                } else {
                    str = "cannot bind non-enum descriptor to an enum class";
                }
                throw new InvalidClassException(str);
            } else if (this.serializable == this.localDesc.serializable && !cl.isArray() && this.suid.longValue() != this.localDesc.getSerialVersionUID()) {
                throw new InvalidClassException(this.localDesc.name, "local class incompatible: stream classdesc serialVersionUID = " + this.suid + ", local class serialVersionUID = " + this.localDesc.getSerialVersionUID());
            } else if (classNamesEqual(this.name, this.localDesc.name)) {
                if (!this.isEnum) {
                    if (this.serializable != this.localDesc.serializable || this.externalizable == this.localDesc.externalizable) {
                        if (this.serializable == this.localDesc.serializable && this.externalizable == this.localDesc.externalizable) {
                            if (!this.serializable) {
                                if (this.externalizable) {
                                }
                            }
                        }
                        this.deserializeEx = new ExceptionInfo(this.localDesc.name, "class invalid for deserialization");
                    } else {
                        throw new InvalidClassException(this.localDesc.name, "Serializable incompatible with Externalizable");
                    }
                }
                this.cons = this.localDesc.cons;
                this.writeObjectMethod = this.localDesc.writeObjectMethod;
                this.readObjectMethod = this.localDesc.readObjectMethod;
                this.readObjectNoDataMethod = this.localDesc.readObjectNoDataMethod;
                this.writeReplaceMethod = this.localDesc.writeReplaceMethod;
                this.readResolveMethod = this.localDesc.readResolveMethod;
                if (this.deserializeEx == null) {
                    this.deserializeEx = this.localDesc.deserializeEx;
                }
            } else {
                throw new InvalidClassException(this.localDesc.name, "local class name incompatible with stream class name \"" + this.name + "\"");
            }
        }
        this.fieldRefl = getReflector(this.fields, this.localDesc);
        this.fields = this.fieldRefl.getFields();
    }

    void readNonProxy(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.name = in.readUTF();
        this.suid = Long.valueOf(in.readLong());
        this.isProxy = false;
        byte flags = in.readByte();
        this.hasWriteObjectData = (flags & 1) != 0;
        this.hasBlockExternalData = (flags & 8) != 0;
        this.externalizable = (flags & 4) != 0;
        boolean sflag = (flags & 2) != 0;
        if (this.externalizable && sflag) {
            throw new InvalidClassException(this.name, "serializable and externalizable flags conflict");
        }
        if (this.externalizable) {
            sflag = true;
        }
        this.serializable = sflag;
        this.isEnum = (flags & 16) != 0;
        if (!this.isEnum || this.suid.longValue() == 0) {
            int numFields = in.readShort();
            if (!this.isEnum || numFields == 0) {
                this.fields = numFields > 0 ? new ObjectStreamField[numFields] : NO_FIELDS;
                int i = 0;
                while (i < numFields) {
                    char tcode = (char) in.readByte();
                    String fname = in.readUTF();
                    String signature = (tcode == 'L' || tcode == '[') ? in.readTypeString() : new String(new char[]{tcode});
                    try {
                        this.fields[i] = new ObjectStreamField(fname, signature, false);
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

    void writeNonProxy(ObjectOutputStream out) throws IOException {
        out.writeUTF(this.name);
        out.writeLong(getSerialVersionUID());
        byte b = (byte) 0;
        if (this.externalizable) {
            b = (byte) 4;
            if (out.getProtocolVersion() != 1) {
                b = (byte) (b | 8);
            }
        } else if (this.serializable) {
            b = (byte) 2;
        }
        if (this.hasWriteObjectData) {
            b = (byte) (b | 1);
        }
        if (this.isEnum) {
            b = (byte) (b | 16);
        }
        out.writeByte(b);
        out.writeShort(this.fields.length);
        for (ObjectStreamField f : this.fields) {
            out.writeByte(f.getTypeCode());
            out.writeUTF(f.getName());
            if (!f.isPrimitive()) {
                out.writeTypeString(f.getTypeString());
            }
        }
    }

    ClassNotFoundException getResolveException() {
        return this.resolveEx;
    }

    void checkDeserialize() throws InvalidClassException {
        if (this.deserializeEx != null) {
            throw this.deserializeEx.newInvalidClassException();
        }
    }

    void checkSerialize() throws InvalidClassException {
        if (this.serializeEx != null) {
            throw this.serializeEx.newInvalidClassException();
        }
    }

    void checkDefaultSerialize() throws InvalidClassException {
        if (this.defaultSerializeEx != null) {
            throw this.defaultSerializeEx.newInvalidClassException();
        }
    }

    ObjectStreamClass getSuperDesc() {
        return this.superDesc;
    }

    ObjectStreamClass getLocalDesc() {
        return this.localDesc;
    }

    ObjectStreamField[] getFields(boolean copy) {
        return copy ? (ObjectStreamField[]) this.fields.clone() : this.fields;
    }

    ObjectStreamField getField(String name, Class<?> type) {
        for (ObjectStreamField f : this.fields) {
            if (f.getName().equals(name)) {
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

    boolean isProxy() {
        return this.isProxy;
    }

    boolean isEnum() {
        return this.isEnum;
    }

    boolean isExternalizable() {
        return this.externalizable;
    }

    boolean isSerializable() {
        return this.serializable;
    }

    boolean hasBlockExternalData() {
        return this.hasBlockExternalData;
    }

    boolean hasWriteObjectData() {
        return this.hasWriteObjectData;
    }

    boolean isInstantiable() {
        return this.cons != null;
    }

    boolean hasWriteObjectMethod() {
        return this.writeObjectMethod != null;
    }

    boolean hasReadObjectMethod() {
        return this.readObjectMethod != null;
    }

    boolean hasReadObjectNoDataMethod() {
        return this.readObjectNoDataMethod != null;
    }

    boolean hasWriteReplaceMethod() {
        return this.writeReplaceMethod != null;
    }

    boolean hasReadResolveMethod() {
        return this.readResolveMethod != null;
    }

    Object newInstance() throws InstantiationException, InvocationTargetException, UnsupportedOperationException {
        if (this.cons != null) {
            try {
                return this.cons.newInstance(new Object[0]);
            } catch (IllegalAccessException e) {
                throw new InternalError();
            }
        }
        throw new UnsupportedOperationException();
    }

    void invokeWriteObject(Object obj, ObjectOutputStream out) throws IOException, UnsupportedOperationException {
        if (this.writeObjectMethod != null) {
            try {
                this.writeObjectMethod.invoke(obj, out);
                return;
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof IOException) {
                    throw ((IOException) th);
                }
                throwMiscException(th);
                return;
            } catch (IllegalAccessException e) {
                throw new InternalError();
            }
        }
        throw new UnsupportedOperationException();
    }

    void invokeReadObject(Object obj, ObjectInputStream in) throws ClassNotFoundException, IOException, UnsupportedOperationException {
        if (this.readObjectMethod != null) {
            try {
                this.readObjectMethod.invoke(obj, in);
                return;
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ClassNotFoundException) {
                    throw ((ClassNotFoundException) th);
                } else if (th instanceof IOException) {
                    throw ((IOException) th);
                } else {
                    throwMiscException(th);
                    return;
                }
            } catch (IllegalAccessException e) {
                throw new InternalError();
            }
        }
        throw new UnsupportedOperationException();
    }

    void invokeReadObjectNoData(Object obj) throws IOException, UnsupportedOperationException {
        if (this.readObjectNoDataMethod != null) {
            try {
                this.readObjectNoDataMethod.invoke(obj, (Object[]) null);
                return;
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw ((ObjectStreamException) th);
                }
                throwMiscException(th);
                return;
            } catch (IllegalAccessException e) {
                throw new InternalError();
            }
        }
        throw new UnsupportedOperationException();
    }

    Object invokeWriteReplace(Object obj) throws IOException, UnsupportedOperationException {
        if (this.writeReplaceMethod != null) {
            try {
                return this.writeReplaceMethod.invoke(obj, (Object[]) null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw ((ObjectStreamException) th);
                }
                throwMiscException(th);
                throw new InternalError();
            } catch (IllegalAccessException e) {
                throw new InternalError();
            }
        }
        throw new UnsupportedOperationException();
    }

    Object invokeReadResolve(Object obj) throws IOException, UnsupportedOperationException {
        if (this.readResolveMethod != null) {
            try {
                return this.readResolveMethod.invoke(obj, (Object[]) null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw ((ObjectStreamException) th);
                }
                throwMiscException(th);
                throw new InternalError();
            } catch (IllegalAccessException e) {
                throw new InternalError();
            }
        }
        throw new UnsupportedOperationException();
    }

    ClassDataSlot[] getClassDataLayout() throws InvalidClassException {
        if (this.dataLayout == null) {
            this.dataLayout = getClassDataLayout0();
        }
        return this.dataLayout;
    }

    private ClassDataSlot[] getClassDataLayout0() throws InvalidClassException {
        ArrayList<ClassDataSlot> slots = new ArrayList();
        Class<?> start = this.cl;
        Class<?> end = this.cl;
        while (end != null && Serializable.class.isAssignableFrom(end)) {
            end = end.getSuperclass();
        }
        HashSet<String> oscNames = new HashSet(3);
        ObjectStreamClass d = this;
        while (d != null) {
            if (oscNames.contains(d.name)) {
                throw new InvalidClassException("Circular reference.");
            }
            Class<?> c;
            oscNames.add(d.name);
            String searchName = d.cl != null ? d.cl.getName() : d.name;
            Class<?> match = null;
            for (c = start; c != end; c = c.getSuperclass()) {
                if (searchName.equals(c.getName())) {
                    match = c;
                    break;
                }
            }
            if (match != null) {
                for (c = start; c != match; c = c.getSuperclass()) {
                    slots.add(new ClassDataSlot(lookup(c, true), false));
                }
                start = match.getSuperclass();
            }
            slots.add(new ClassDataSlot(d.getVariantFor(match), true));
            d = d.superDesc;
        }
        for (c = start; c != end; c = c.getSuperclass()) {
            slots.add(new ClassDataSlot(lookup(c, true), false));
        }
        Collections.reverse(slots);
        return (ClassDataSlot[]) slots.toArray(new ClassDataSlot[slots.size()]);
    }

    int getPrimDataSize() {
        return this.primDataSize;
    }

    int getNumObjFields() {
        return this.numObjFields;
    }

    void getPrimFieldValues(Object obj, byte[] buf) {
        this.fieldRefl.getPrimFieldValues(obj, buf);
    }

    void setPrimFieldValues(Object obj, byte[] buf) {
        this.fieldRefl.setPrimFieldValues(obj, buf);
    }

    void getObjFieldValues(Object obj, Object[] vals) {
        this.fieldRefl.getObjFieldValues(obj, vals);
    }

    void setObjFieldValues(Object obj, Object[] vals) {
        this.fieldRefl.setObjFieldValues(obj, vals);
    }

    private void computeFieldOffsets() throws InvalidClassException {
        this.primDataSize = 0;
        this.numObjFields = 0;
        int firstObjIndex = -1;
        for (int i = 0; i < this.fields.length; i++) {
            ObjectStreamField f = this.fields[i];
            int i2;
            switch (f.getTypeCode()) {
                case 'B':
                case 'Z':
                    i2 = this.primDataSize;
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
                    i2 = this.numObjFields;
                    this.numObjFields = i2 + 1;
                    f.setOffset(i2);
                    if (firstObjIndex != -1) {
                        break;
                    }
                    firstObjIndex = i;
                    break;
                default:
                    throw new InternalError();
            }
        }
        if (firstObjIndex != -1 && this.numObjFields + firstObjIndex != this.fields.length) {
            throw new InvalidClassException(this.name, "illegal field order");
        }
    }

    private ObjectStreamClass getVariantFor(Class<?> cl) throws InvalidClassException {
        if (this.cl == cl) {
            return this;
        }
        ObjectStreamClass desc = new ObjectStreamClass();
        if (this.isProxy) {
            desc.initProxy(cl, null, this.superDesc);
        } else {
            desc.initNonProxy(this, cl, null, this.superDesc);
        }
        return desc;
    }

    private static Constructor getExternalizableConstructor(Class<?> cl) {
        try {
            Constructor cons = cl.getDeclaredConstructor((Class[]) null);
            cons.setAccessible(true);
            if ((cons.getModifiers() & 1) == 0) {
                cons = null;
            }
            return cons;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Constructor getSerializableConstructor(Class<?> cl) {
        Class<?> initCl = cl;
        while (Serializable.class.isAssignableFrom(initCl)) {
            initCl = initCl.getSuperclass();
            if (initCl == null) {
                return null;
            }
        }
        try {
            Constructor cons = initCl.getDeclaredConstructor((Class[]) null);
            int mods = cons.getModifiers();
            if ((mods & 2) != 0 || ((mods & 5) == 0 && !packageEquals(cl, initCl))) {
                return null;
            }
            if (cons.getDeclaringClass() != cl) {
                cons = cons.serializationCopy(cons.getDeclaringClass(), cl);
            }
            cons.setAccessible(true);
            return cons;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Method getInheritableMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
        Method meth = null;
        Class<?> defCl = cl;
        while (defCl != null) {
            try {
                meth = defCl.getDeclaredMethod(name, argTypes);
                break;
            } catch (NoSuchMethodException e) {
                defCl = defCl.getSuperclass();
            }
        }
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
            if (cl != defCl) {
                meth = null;
            }
            return meth;
        }
        if (!packageEquals(cl, defCl)) {
            meth = null;
        }
        return meth;
    }

    private static Method getPrivateMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
        try {
            Method meth = cl.getDeclaredMethod(name, argTypes);
            meth.setAccessible(true);
            int mods = meth.getModifiers();
            if (!(meth.getReturnType() == returnType && (mods & 8) == 0 && (mods & 2) != 0)) {
                meth = null;
            }
            return meth;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static boolean packageEquals(Class<?> cl1, Class<?> cl2) {
        if (cl1.getClassLoader() == cl2.getClassLoader()) {
            return getPackageName(cl1).equals(getPackageName(cl2));
        }
        return false;
    }

    private static String getPackageName(Class<?> cl) {
        String s = cl.getName();
        int i = s.lastIndexOf(91);
        if (i >= 0) {
            s = s.substring(i + 2);
        }
        i = s.lastIndexOf(46);
        return i >= 0 ? s.substring(0, i) : "";
    }

    private static boolean classNamesEqual(String name1, String name2) {
        return name1.substring(name1.lastIndexOf(46) + 1).equals(name2.substring(name2.lastIndexOf(46) + 1));
    }

    private static String getClassSignature(Class<?> cl) {
        StringBuilder sbuf = new StringBuilder();
        while (cl.isArray()) {
            sbuf.append('[');
            cl = cl.getComponentType();
        }
        if (!cl.isPrimitive()) {
            sbuf.append('L').append(cl.getName().replace('.', '/')).append(';');
        } else if (cl == Integer.TYPE) {
            sbuf.append('I');
        } else if (cl == Byte.TYPE) {
            sbuf.append('B');
        } else if (cl == Long.TYPE) {
            sbuf.append('J');
        } else if (cl == Float.TYPE) {
            sbuf.append('F');
        } else if (cl == Double.TYPE) {
            sbuf.append('D');
        } else if (cl == Short.TYPE) {
            sbuf.append('S');
        } else if (cl == Character.TYPE) {
            sbuf.append('C');
        } else if (cl == Boolean.TYPE) {
            sbuf.append('Z');
        } else if (cl == Void.TYPE) {
            sbuf.append('V');
        } else {
            throw new InternalError();
        }
        return sbuf.toString();
    }

    private static String getMethodSignature(Class<?>[] paramTypes, Class<?> retType) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append('(');
        for (Class classSignature : paramTypes) {
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

    private static ObjectStreamField[] getSerialFields(Class<?> cl) throws InvalidClassException {
        if (!Serializable.class.isAssignableFrom(cl) || Externalizable.class.isAssignableFrom(cl) || Proxy.isProxyClass(cl) || cl.isInterface()) {
            return NO_FIELDS;
        }
        Object[] fields = getDeclaredSerialFields(cl);
        if (fields == null) {
            fields = getDefaultSerialFields(cl);
        }
        Arrays.sort(fields);
        return fields;
    }

    private static ObjectStreamField[] getDeclaredSerialFields(Class<?> cl) throws InvalidClassException {
        Field f;
        ObjectStreamField[] serialPersistentFields = null;
        try {
            f = cl.getDeclaredField("serialPersistentFields");
            if ((f.getModifiers() & 26) == 26) {
                f.setAccessible(true);
                serialPersistentFields = (ObjectStreamField[]) f.get(null);
            }
        } catch (Exception e) {
        }
        if (serialPersistentFields == null) {
            return null;
        }
        if (serialPersistentFields.length == 0) {
            return NO_FIELDS;
        }
        ObjectStreamField[] boundFields = new ObjectStreamField[serialPersistentFields.length];
        Set<String> fieldNames = new HashSet(serialPersistentFields.length);
        for (int i = 0; i < serialPersistentFields.length; i++) {
            ObjectStreamField spf = serialPersistentFields[i];
            String fname = spf.getName();
            if (fieldNames.contains(fname)) {
                throw new InvalidClassException("multiple serializable fields named " + fname);
            }
            fieldNames.add(fname);
            try {
                f = cl.getDeclaredField(fname);
                if (f.getType() == spf.getType() && (f.getModifiers() & 8) == 0) {
                    boundFields[i] = new ObjectStreamField(f, spf.isUnshared(), true);
                }
            } catch (NoSuchFieldException e2) {
            }
            if (boundFields[i] == null) {
                boundFields[i] = new ObjectStreamField(fname, spf.getType(), spf.isUnshared());
            }
        }
        return boundFields;
    }

    private static ObjectStreamField[] getDefaultSerialFields(Class<?> cl) {
        Field[] clFields = cl.getDeclaredFields();
        ArrayList<ObjectStreamField> list = new ArrayList();
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

    private static Long getDeclaredSUID(Class<?> cl) {
        try {
            Field f = cl.getDeclaredField("serialVersionUID");
            if ((f.getModifiers() & 24) == 24) {
                f.setAccessible(true);
                return Long.valueOf(f.getLong(null));
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static long computeDefaultSUID(Class<?> cl) {
        if (!Serializable.class.isAssignableFrom(cl) || Proxy.isProxyClass(cl)) {
            return 0;
        }
        try {
            int i;
            int length;
            MemberSignature sig;
            int mods;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            dout.writeUTF(cl.getName());
            int classMods = cl.getModifiers() & 1553;
            Method[] methods = cl.getDeclaredMethods();
            if ((classMods & Modifier.INTERFACE) != 0) {
                if (methods.length > 0) {
                    classMods |= Record.maxExpansion;
                } else {
                    classMods &= -1025;
                }
            }
            dout.writeInt(classMods);
            if (!cl.isArray()) {
                Class<?>[] interfaces = cl.getInterfaces();
                Object[] ifaceNames = new String[interfaces.length];
                i = 0;
                while (true) {
                    length = interfaces.length;
                    if (i >= r0) {
                        break;
                    }
                    ifaceNames[i] = interfaces[i].getName();
                    i++;
                }
                Arrays.sort(ifaceNames);
                i = 0;
                while (true) {
                    length = ifaceNames.length;
                    if (i >= r0) {
                        break;
                    }
                    dout.writeUTF(ifaceNames[i]);
                    i++;
                }
            }
            Field[] fields = cl.getDeclaredFields();
            MemberSignature[] fieldSigs = new MemberSignature[fields.length];
            i = 0;
            while (true) {
                length = fields.length;
                if (i >= r0) {
                    break;
                }
                fieldSigs[i] = new MemberSignature(fields[i]);
                i++;
            }
            Arrays.sort(fieldSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    return ms1.name.compareTo(ms2.name);
                }
            });
            i = 0;
            while (true) {
                length = fieldSigs.length;
                if (i >= r0) {
                    break;
                }
                sig = fieldSigs[i];
                mods = sig.member.getModifiers() & 223;
                if ((mods & 2) == 0 || (mods & 136) == 0) {
                    dout.writeUTF(sig.name);
                    dout.writeInt(mods);
                    dout.writeUTF(sig.signature);
                }
                i++;
            }
            if (hasStaticInitializer(cl, VMRuntime.getRuntime().getTargetSdkVersion() > MAX_SDK_TARGET_FOR_CLINIT_UIDGEN_WORKAROUND)) {
                dout.writeUTF("<clinit>");
                dout.writeInt(8);
                dout.writeUTF("()V");
            }
            Constructor[] cons = cl.getDeclaredConstructors();
            MemberSignature[] consSigs = new MemberSignature[cons.length];
            i = 0;
            while (true) {
                length = cons.length;
                if (i >= r0) {
                    break;
                }
                consSigs[i] = new MemberSignature(cons[i]);
                i++;
            }
            Arrays.sort(consSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    return ms1.signature.compareTo(ms2.signature);
                }
            });
            i = 0;
            while (true) {
                length = consSigs.length;
                if (i >= r0) {
                    break;
                }
                sig = consSigs[i];
                mods = sig.member.getModifiers() & 3391;
                if ((mods & 2) == 0) {
                    dout.writeUTF("<init>");
                    dout.writeInt(mods);
                    dout.writeUTF(sig.signature.replace('/', '.'));
                }
                i++;
            }
            MemberSignature[] methSigs = new MemberSignature[methods.length];
            i = 0;
            while (true) {
                length = methods.length;
                if (i >= r0) {
                    break;
                }
                methSigs[i] = new MemberSignature(methods[i]);
                i++;
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
            i = 0;
            while (true) {
                length = methSigs.length;
                if (i >= r0) {
                    break;
                }
                sig = methSigs[i];
                mods = sig.member.getModifiers() & 3391;
                if ((mods & 2) == 0) {
                    dout.writeUTF(sig.name);
                    dout.writeInt(mods);
                    dout.writeUTF(sig.signature.replace('/', '.'));
                }
                i++;
            }
            dout.flush();
            byte[] hashBytes = MessageDigest.getInstance("SHA").digest(bout.toByteArray());
            long hash = 0;
            for (i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
                hash = (hash << 8) | ((long) (hashBytes[i] & 255));
            }
            return hash;
        } catch (IOException e) {
            throw new InternalError();
        } catch (NoSuchAlgorithmException ex) {
            throw new SecurityException(ex.getMessage());
        }
    }

    private static FieldReflector getReflector(ObjectStreamField[] fields, ObjectStreamClass localDesc) throws InvalidClassException {
        Class cls = (localDesc == null || fields.length <= 0) ? null : localDesc.cl;
        processQueue(Caches.reflectorsQueue, Caches.reflectors);
        FieldReflectorKey key = new FieldReflectorKey(cls, fields, Caches.reflectorsQueue);
        Reference<?> ref = (Reference) Caches.reflectors.get(key);
        Object entry = null;
        if (ref != null) {
            entry = ref.get();
        }
        EntryFuture future = null;
        if (entry == null) {
            EntryFuture newEntry = new EntryFuture();
            Reference<?> newRef = new SoftReference(newEntry);
            do {
                if (ref != null) {
                    Caches.reflectors.remove(key, ref);
                }
                ref = (Reference) Caches.reflectors.putIfAbsent(key, newRef);
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
                entry = new FieldReflector(matchFields(fields, localDesc));
            } catch (Throwable th) {
                Throwable entry2 = th;
            }
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

    private static ObjectStreamField[] matchFields(ObjectStreamField[] fields, ObjectStreamClass localDesc) throws InvalidClassException {
        ObjectStreamField[] localFields = localDesc != null ? localDesc.fields : NO_FIELDS;
        ObjectStreamField[] matches = new ObjectStreamField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField f = fields[i];
            ObjectStreamField m = null;
            for (ObjectStreamField lf : localFields) {
                if (f.getName().equals(lf.getName())) {
                    if ((f.isPrimitive() || lf.isPrimitive()) && f.getTypeCode() != lf.getTypeCode()) {
                        throw new InvalidClassException(localDesc.name, "incompatible types for field " + f.getName());
                    } else if (lf.getField() != null) {
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
        System.logE("WARNING: ObjectStreamClass.getConstructorId(Class<?>) is private API andwill be removed in a future Android release.");
        return 1189998819991197253L;
    }

    private static Object newInstance(Class<?> clazz, long constructorId) {
        System.logE("WARNING: ObjectStreamClass.newInstance(Class<?>, long) is private API andwill be removed in a future Android release.");
        return Unsafe.getUnsafe().allocateInstance(clazz);
    }

    static void processQueue(ReferenceQueue<Class<?>> queue, ConcurrentMap<? extends WeakReference<Class<?>>, ?> map) {
        while (true) {
            Reference<? extends Class<?>> ref = queue.poll();
            if (ref != null) {
                map.remove(ref);
            } else {
                return;
            }
        }
    }
}
