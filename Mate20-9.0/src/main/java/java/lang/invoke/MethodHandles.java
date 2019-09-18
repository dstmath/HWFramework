package java.lang.invoke;

import dalvik.system.VMStack;
import java.lang.invoke.Transformers;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import sun.invoke.util.VerifyAccess;
import sun.invoke.util.Wrapper;

public class MethodHandles {

    public static final class Lookup {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final int ALL_MODES = 15;
        static final Lookup IMPL_LOOKUP = new Lookup(Object.class, 15);
        public static final int PACKAGE = 8;
        public static final int PRIVATE = 2;
        public static final int PROTECTED = 4;
        public static final int PUBLIC = 1;
        static final Lookup PUBLIC_LOOKUP = new Lookup(Object.class, 1);
        private final int allowedModes;
        private final Class<?> lookupClass;

        static {
            Class<MethodHandles> cls = MethodHandles.class;
        }

        private static int fixmods(int mods) {
            int mods2 = mods & 7;
            if (mods2 != 0) {
                return mods2;
            }
            return 8;
        }

        public Class<?> lookupClass() {
            return this.lookupClass;
        }

        public int lookupModes() {
            return this.allowedModes & 15;
        }

        Lookup(Class<?> lookupClass2) {
            this(lookupClass2, 15);
            checkUnprivilegedlookupClass(lookupClass2, 15);
        }

        private Lookup(Class<?> lookupClass2, int allowedModes2) {
            this.lookupClass = lookupClass2;
            this.allowedModes = allowedModes2;
        }

        public Lookup in(Class<?> requestedLookupClass) {
            requestedLookupClass.getClass();
            if (requestedLookupClass == this.lookupClass) {
                return this;
            }
            int newModes = this.allowedModes & 11;
            if ((newModes & 8) != 0 && !VerifyAccess.isSamePackage(this.lookupClass, requestedLookupClass)) {
                newModes &= -11;
            }
            if ((newModes & 2) != 0 && !VerifyAccess.isSamePackageMember(this.lookupClass, requestedLookupClass)) {
                newModes &= -3;
            }
            if ((newModes & 1) != 0 && !VerifyAccess.isClassAccessible(requestedLookupClass, this.lookupClass, this.allowedModes)) {
                newModes = 0;
            }
            checkUnprivilegedlookupClass(requestedLookupClass, newModes);
            return new Lookup(requestedLookupClass, newModes);
        }

        private static void checkUnprivilegedlookupClass(Class<?> lookupClass2, int allowedModes2) {
            String name = lookupClass2.getName();
            if (name.startsWith("java.lang.invoke.")) {
                throw MethodHandleStatics.newIllegalArgumentException("illegal lookupClass: " + lookupClass2);
            } else if (allowedModes2 != 15 || lookupClass2.getClassLoader() != Object.class.getClassLoader()) {
            } else {
                if (name.startsWith("java.") || (name.startsWith("sun.") && !name.startsWith("sun.invoke.") && !name.equals("sun.reflect.ReflectionFactory"))) {
                    throw MethodHandleStatics.newIllegalArgumentException("illegal lookupClass: " + lookupClass2);
                }
            }
        }

        public String toString() {
            String cname = this.lookupClass.getName();
            int i = this.allowedModes;
            if (i == 9) {
                return cname + "/package";
            } else if (i == 11) {
                return cname + "/private";
            } else if (i == 15) {
                return cname;
            } else {
                switch (i) {
                    case 0:
                        return cname + "/noaccess";
                    case 1:
                        return cname + "/public";
                    default:
                        return cname + "/" + Integer.toHexString(this.allowedModes);
                }
            }
        }

        public MethodHandle findStatic(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            Method method = refc.getDeclaredMethod(name, type.ptypes());
            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                checkReturnType(method, type);
                checkAccess(refc, method.getDeclaringClass(), modifiers, method.getName());
                return createMethodHandle(method, 3, type);
            }
            throw new IllegalAccessException("Method" + method + " is not static");
        }

        private MethodHandle findVirtualForMH(String name, MethodType type) {
            if ("invoke".equals(name)) {
                return MethodHandles.invoker(type);
            }
            if ("invokeExact".equals(name)) {
                return MethodHandles.exactInvoker(type);
            }
            return null;
        }

        private MethodHandle findVirtualForVH(String name, MethodType type) {
            try {
                return MethodHandles.varHandleInvoker(VarHandle.AccessMode.valueFromMethodName(name), type);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        private static MethodHandle createMethodHandle(Method method, int handleKind, MethodType methodType) {
            MethodHandle mh = new MethodHandleImpl(method.getArtMethod(), handleKind, methodType);
            if (method.isVarArgs()) {
                return new Transformers.VarargsCollector(mh);
            }
            return mh;
        }

        public MethodHandle findVirtual(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            if (refc == MethodHandle.class) {
                MethodHandle mh = findVirtualForMH(name, type);
                if (mh != null) {
                    return mh;
                }
            } else if (refc == VarHandle.class) {
                MethodHandle mh2 = findVirtualForVH(name, type);
                if (mh2 != null) {
                    return mh2;
                }
            }
            Method method = refc.getInstanceMethod(name, type.ptypes());
            if (method == null) {
                try {
                    Method m = refc.getDeclaredMethod(name, type.ptypes());
                    if (Modifier.isStatic(m.getModifiers())) {
                        throw new IllegalAccessException("Method" + m + " is static");
                    }
                } catch (NoSuchMethodException e) {
                }
                throw new NoSuchMethodException(name + " " + Arrays.toString((Object[]) type.ptypes()));
            }
            checkReturnType(method, type);
            checkAccess(refc, method.getDeclaringClass(), method.getModifiers(), method.getName());
            return createMethodHandle(method, 0, type.insertParameterTypes(0, (Class<?>[]) new Class[]{refc}));
        }

        public MethodHandle findConstructor(Class<?> refc, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            if (!refc.isArray()) {
                Constructor constructor = refc.getDeclaredConstructor(type.ptypes());
                if (constructor != null) {
                    checkAccess(refc, constructor.getDeclaringClass(), constructor.getModifiers(), constructor.getName());
                    return createMethodHandleForConstructor(constructor);
                }
                throw new NoSuchMethodException("No constructor for " + constructor.getDeclaringClass() + " matching " + type);
            }
            throw new NoSuchMethodException("no constructor for array class: " + refc.getName());
        }

        private MethodHandle createMethodHandleForConstructor(Constructor constructor) {
            MethodHandle mh;
            Class<?> refc = constructor.getDeclaringClass();
            MethodType constructorType = MethodType.methodType(refc, (Class<?>[]) constructor.getParameterTypes());
            if (refc == String.class) {
                mh = new MethodHandleImpl(constructor.getArtMethod(), 2, constructorType);
            } else {
                mh = new Transformers.Construct(new MethodHandleImpl(constructor.getArtMethod(), 2, initMethodType(constructorType)), constructorType);
            }
            if (constructor.isVarArgs()) {
                return new Transformers.VarargsCollector(mh);
            }
            return mh;
        }

        private static MethodType initMethodType(MethodType constructorType) {
            Class<?>[] initPtypes = new Class[(constructorType.ptypes().length + 1)];
            initPtypes[0] = constructorType.rtype();
            System.arraycopy((Object) constructorType.ptypes(), 0, (Object) initPtypes, 1, constructorType.ptypes().length);
            return MethodType.methodType((Class<?>) Void.TYPE, initPtypes);
        }

        public MethodHandle findSpecial(Class<?> refc, String name, MethodType type, Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException {
            if (specialCaller == null) {
                throw new NullPointerException("specialCaller == null");
            } else if (type == null) {
                throw new NullPointerException("type == null");
            } else if (name == null) {
                throw new NullPointerException("name == null");
            } else if (refc != null) {
                checkSpecialCaller(specialCaller);
                if (!name.startsWith("<")) {
                    Method method = refc.getDeclaredMethod(name, type.ptypes());
                    checkReturnType(method, type);
                    return findSpecial(method, type, refc, specialCaller);
                }
                throw new NoSuchMethodException(name + " is not a valid method name.");
            } else {
                throw new NullPointerException("ref == null");
            }
        }

        private MethodHandle findSpecial(Method method, MethodType type, Class<?> refc, Class<?> specialCaller) throws IllegalAccessException {
            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalAccessException("expected a non-static method:" + method);
            } else if (Modifier.isPrivate(method.getModifiers())) {
                if (refc == lookupClass()) {
                    return createMethodHandle(method, 2, type.insertParameterTypes(0, (Class<?>[]) new Class[]{refc}));
                }
                throw new IllegalAccessException("no private access for invokespecial : " + refc + ", from" + this);
            } else if (method.getDeclaringClass().isAssignableFrom(specialCaller)) {
                return createMethodHandle(method, 1, type.insertParameterTypes(0, (Class<?>[]) new Class[]{specialCaller}));
            } else {
                throw new IllegalAccessException(refc + "is not assignable from " + specialCaller);
            }
        }

        public MethodHandle findGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            return findAccessor(refc, name, type, 9);
        }

        private MethodHandle findAccessor(Class<?> refc, String name, Class<?> type, int kind) throws NoSuchFieldException, IllegalAccessException {
            return findAccessor(findFieldOfType(refc, name, type), refc, type, kind, true);
        }

        private MethodHandle findAccessor(Field field, Class<?> refc, Class<?> type, int kind, boolean performAccessChecks) throws IllegalAccessException {
            MethodType methodType;
            Class<?> cls = refc;
            Class<?> cls2 = type;
            int i = kind;
            boolean isSetterKind = i == 10 || i == 12;
            commonFieldChecks(field, cls, cls2, i == 11 || i == 12, performAccessChecks);
            if (performAccessChecks) {
                int modifiers = field.getModifiers();
                if (isSetterKind && Modifier.isFinal(modifiers)) {
                    throw new IllegalAccessException("Field " + field + " is final");
                }
            }
            Field field2 = field;
            switch (i) {
                case 9:
                    methodType = MethodType.methodType(cls2, cls);
                    break;
                case 10:
                    methodType = MethodType.methodType(Void.TYPE, cls, cls2);
                    break;
                case 11:
                    methodType = MethodType.methodType(cls2);
                    break;
                case 12:
                    methodType = MethodType.methodType((Class<?>) Void.TYPE, cls2);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid kind " + i);
            }
            return new MethodHandleImpl(field2.getArtField(), i, methodType);
        }

        public MethodHandle findSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            return findAccessor(refc, name, type, 10);
        }

        public VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            Field field = findFieldOfType(recv, name, type);
            commonFieldChecks(field, recv, type, $assertionsDisabled, true);
            return FieldVarHandle.create(field);
        }

        private Field findFieldOfType(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException {
            Field field = null;
            Class<?> cls = refc;
            while (true) {
                if (cls == null) {
                    break;
                }
                try {
                    field = cls.getDeclaredField(name);
                    break;
                } catch (NoSuchFieldException e) {
                    cls = cls.getSuperclass();
                }
            }
            if (field == null) {
                field = refc.getDeclaredField(name);
            }
            if (field.getType() == type) {
                return field;
            }
            throw new NoSuchFieldException(name);
        }

        private void commonFieldChecks(Field field, Class<?> refc, Class<?> cls, boolean isStatic, boolean performAccessChecks) throws IllegalAccessException {
            int modifiers = field.getModifiers();
            if (performAccessChecks) {
                checkAccess(refc, field.getDeclaringClass(), modifiers, field.getName());
            }
            if (Modifier.isStatic(modifiers) != isStatic) {
                StringBuilder sb = new StringBuilder();
                sb.append("Field ");
                sb.append((Object) field);
                sb.append(" is ");
                sb.append(isStatic ? "not " : "");
                sb.append("static");
                throw new IllegalAccessException(sb.toString());
            }
        }

        public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            return findAccessor(refc, name, type, 11);
        }

        public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            return findAccessor(refc, name, type, 12);
        }

        public VarHandle findStaticVarHandle(Class<?> decl, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            Field field = findFieldOfType(decl, name, type);
            commonFieldChecks(field, decl, type, true, true);
            return FieldVarHandle.create(field);
        }

        public MethodHandle bind(Object receiver, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            MethodHandle handle = findVirtual(receiver.getClass(), name, type);
            MethodHandle adapter = handle.bindTo(receiver);
            MethodType adapterType = adapter.type();
            if (handle.isVarargsCollector()) {
                return adapter.asVarargsCollector(adapterType.parameterType(adapterType.parameterCount() - 1));
            }
            return adapter;
        }

        public MethodHandle unreflect(Method m) throws IllegalAccessException {
            if (m != null) {
                MethodType methodType = MethodType.methodType(m.getReturnType(), (Class<?>[]) m.getParameterTypes());
                if (!m.isAccessible()) {
                    checkAccess(m.getDeclaringClass(), m.getDeclaringClass(), m.getModifiers(), m.getName());
                }
                if (Modifier.isStatic(m.getModifiers())) {
                    return createMethodHandle(m, 3, methodType);
                }
                return createMethodHandle(m, 0, methodType.insertParameterTypes(0, (Class<?>[]) new Class[]{m.getDeclaringClass()}));
            }
            throw new NullPointerException("m == null");
        }

        public MethodHandle unreflectSpecial(Method m, Class<?> specialCaller) throws IllegalAccessException {
            if (m == null) {
                throw new NullPointerException("m == null");
            } else if (specialCaller != null) {
                if (!m.isAccessible()) {
                    checkSpecialCaller(specialCaller);
                }
                return findSpecial(m, MethodType.methodType(m.getReturnType(), (Class<?>[]) m.getParameterTypes()), m.getDeclaringClass(), specialCaller);
            } else {
                throw new NullPointerException("specialCaller == null");
            }
        }

        public MethodHandle unreflectConstructor(Constructor<?> c) throws IllegalAccessException {
            if (c != null) {
                if (!c.isAccessible()) {
                    checkAccess(c.getDeclaringClass(), c.getDeclaringClass(), c.getModifiers(), c.getName());
                }
                return createMethodHandleForConstructor(c);
            }
            throw new NullPointerException("c == null");
        }

        public MethodHandle unreflectGetter(Field f) throws IllegalAccessException {
            return findAccessor(f, f.getDeclaringClass(), f.getType(), Modifier.isStatic(f.getModifiers()) ? 11 : 9, !f.isAccessible());
        }

        public MethodHandle unreflectSetter(Field f) throws IllegalAccessException {
            return findAccessor(f, f.getDeclaringClass(), f.getType(), Modifier.isStatic(f.getModifiers()) ? 12 : 10, !f.isAccessible());
        }

        public VarHandle unreflectVarHandle(Field f) throws IllegalAccessException {
            Field field = f;
            commonFieldChecks(field, f.getDeclaringClass(), f.getType(), Modifier.isStatic(f.getModifiers()), true);
            return FieldVarHandle.create(f);
        }

        public MethodHandleInfo revealDirect(MethodHandle target) {
            MethodHandleInfo info = MethodHandles.getMethodHandleImpl(target).reveal();
            try {
                checkAccess(lookupClass(), info.getDeclaringClass(), info.getModifiers(), info.getName());
                return info;
            } catch (IllegalAccessException exception) {
                throw new IllegalArgumentException("Unable to access memeber.", exception);
            }
        }

        private boolean hasPrivateAccess() {
            if ((this.allowedModes & 2) != 0) {
                return true;
            }
            return $assertionsDisabled;
        }

        /* access modifiers changed from: package-private */
        public void checkAccess(Class<?> refc, Class<?> defc, int mods, String methName) throws IllegalAccessException {
            int allowedModes2 = this.allowedModes;
            if (Modifier.isProtected(mods) && defc == Object.class && "clone".equals(methName) && refc.isArray()) {
                mods ^= 5;
            }
            if (Modifier.isProtected(mods) && Modifier.isConstructor(mods)) {
                mods ^= 4;
            }
            if (!Modifier.isPublic(mods) || !Modifier.isPublic(refc.getModifiers()) || allowedModes2 == 0) {
                int requestedModes = fixmods(mods);
                if ((requestedModes & allowedModes2) != 0) {
                    if (VerifyAccess.isMemberAccessible(refc, defc, mods, lookupClass(), allowedModes2)) {
                        return;
                    }
                } else if (!((requestedModes & 4) == 0 || (allowedModes2 & 8) == 0 || !VerifyAccess.isSamePackage(defc, lookupClass()))) {
                    return;
                }
                throwMakeAccessException(accessFailedMessage(refc, defc, mods), this);
            }
        }

        /* access modifiers changed from: package-private */
        public String accessFailedMessage(Class<?> refc, Class<?> defc, int mods) {
            boolean isPublic = Modifier.isPublic(defc.getModifiers());
            boolean z = $assertionsDisabled;
            boolean classOK = isPublic && (defc == refc || Modifier.isPublic(refc.getModifiers()));
            if (!classOK && (this.allowedModes & 8) != 0) {
                if (VerifyAccess.isClassAccessible(defc, lookupClass(), 15) && (defc == refc || VerifyAccess.isClassAccessible(refc, lookupClass(), 15))) {
                    z = true;
                }
                classOK = z;
            }
            if (!classOK) {
                return "class is not public";
            }
            if (Modifier.isPublic(mods)) {
                return "access to public member failed";
            }
            if (Modifier.isPrivate(mods)) {
                return "member is private";
            }
            if (Modifier.isProtected(mods)) {
                return "member is protected";
            }
            return "member is private to package";
        }

        private void checkSpecialCaller(Class<?> specialCaller) throws IllegalAccessException {
            if (!hasPrivateAccess() || specialCaller != lookupClass()) {
                throw new IllegalAccessException("no private access for invokespecial : " + specialCaller + ", from" + this);
            }
        }

        private void throwMakeAccessException(String message, Object from) throws IllegalAccessException {
            String message2 = message + ": " + toString();
            if (from != null) {
                message2 = message2 + ", from " + from;
            }
            throw new IllegalAccessException(message2);
        }

        private void checkReturnType(Method method, MethodType methodType) throws NoSuchMethodException {
            if (method.getReturnType() != methodType.rtype()) {
                throw new NoSuchMethodException(method.getName() + methodType);
            }
        }
    }

    private MethodHandles() {
    }

    public static Lookup lookup() {
        return new Lookup(VMStack.getStackClass1());
    }

    public static Lookup publicLookup() {
        return Lookup.PUBLIC_LOOKUP;
    }

    public static <T extends Member> T reflectAs(Class<T> expected, MethodHandle target) {
        return (Member) expected.cast(getMethodHandleImpl(target).getMemberInternal());
    }

    /* access modifiers changed from: private */
    public static MethodHandleImpl getMethodHandleImpl(MethodHandle target) {
        if (target instanceof Transformers.Construct) {
            target = ((Transformers.Construct) target).getConstructorHandle();
        }
        if (target instanceof Transformers.VarargsCollector) {
            target = target.asFixedArity();
        }
        if (target instanceof MethodHandleImpl) {
            return (MethodHandleImpl) target;
        }
        throw new IllegalArgumentException(target + " is not a direct handle");
    }

    private static void checkClassIsArray(Class<?> c) {
        if (!c.isArray()) {
            throw new IllegalArgumentException("Not an array type: " + c);
        }
    }

    private static void checkTypeIsViewable(Class<?> componentType) {
        if (componentType != Short.TYPE && componentType != Character.TYPE && componentType != Integer.TYPE && componentType != Long.TYPE && componentType != Float.TYPE && componentType != Double.TYPE) {
            throw new UnsupportedOperationException("Component type not supported: " + componentType);
        }
    }

    public static MethodHandle arrayElementGetter(Class<?> arrayClass) throws IllegalArgumentException {
        checkClassIsArray(arrayClass);
        Class<?> componentType = arrayClass.getComponentType();
        if (!componentType.isPrimitive()) {
            return new Transformers.ReferenceArrayElementGetter(arrayClass);
        }
        try {
            return Lookup.PUBLIC_LOOKUP.findStatic(MethodHandles.class, "arrayElementGetter", MethodType.methodType(componentType, arrayClass, Integer.TYPE));
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw new AssertionError((Object) exception);
        }
    }

    public static byte arrayElementGetter(byte[] array, int i) {
        return array[i];
    }

    public static boolean arrayElementGetter(boolean[] array, int i) {
        return array[i];
    }

    public static char arrayElementGetter(char[] array, int i) {
        return array[i];
    }

    public static short arrayElementGetter(short[] array, int i) {
        return array[i];
    }

    public static int arrayElementGetter(int[] array, int i) {
        return array[i];
    }

    public static long arrayElementGetter(long[] array, int i) {
        return array[i];
    }

    public static float arrayElementGetter(float[] array, int i) {
        return array[i];
    }

    public static double arrayElementGetter(double[] array, int i) {
        return array[i];
    }

    public static MethodHandle arrayElementSetter(Class<?> arrayClass) throws IllegalArgumentException {
        checkClassIsArray(arrayClass);
        Class<?> componentType = arrayClass.getComponentType();
        if (!componentType.isPrimitive()) {
            return new Transformers.ReferenceArrayElementSetter(arrayClass);
        }
        try {
            return Lookup.PUBLIC_LOOKUP.findStatic(MethodHandles.class, "arrayElementSetter", MethodType.methodType(Void.TYPE, arrayClass, Integer.TYPE, componentType));
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw new AssertionError((Object) exception);
        }
    }

    public static void arrayElementSetter(byte[] array, int i, byte val) {
        array[i] = val;
    }

    public static void arrayElementSetter(boolean[] array, int i, boolean val) {
        array[i] = val;
    }

    public static void arrayElementSetter(char[] array, int i, char val) {
        array[i] = val;
    }

    public static void arrayElementSetter(short[] array, int i, short val) {
        array[i] = val;
    }

    public static void arrayElementSetter(int[] array, int i, int val) {
        array[i] = val;
    }

    public static void arrayElementSetter(long[] array, int i, long val) {
        array[i] = val;
    }

    public static void arrayElementSetter(float[] array, int i, float val) {
        array[i] = val;
    }

    public static void arrayElementSetter(double[] array, int i, double val) {
        array[i] = val;
    }

    public static VarHandle arrayElementVarHandle(Class<?> arrayClass) throws IllegalArgumentException {
        checkClassIsArray(arrayClass);
        return ArrayElementVarHandle.create(arrayClass);
    }

    public static VarHandle byteArrayViewVarHandle(Class<?> viewArrayClass, ByteOrder byteOrder) throws IllegalArgumentException {
        checkClassIsArray(viewArrayClass);
        checkTypeIsViewable(viewArrayClass.getComponentType());
        return ByteArrayViewVarHandle.create(viewArrayClass, byteOrder);
    }

    public static VarHandle byteBufferViewVarHandle(Class<?> viewArrayClass, ByteOrder byteOrder) throws IllegalArgumentException {
        checkClassIsArray(viewArrayClass);
        checkTypeIsViewable(viewArrayClass.getComponentType());
        return ByteBufferViewVarHandle.create(viewArrayClass, byteOrder);
    }

    public static MethodHandle spreadInvoker(MethodType type, int leadingArgCount) {
        if (leadingArgCount < 0 || leadingArgCount > type.parameterCount()) {
            throw MethodHandleStatics.newIllegalArgumentException("bad argument count", Integer.valueOf(leadingArgCount));
        }
        return invoker(type).asSpreader(Object[].class, type.parameterCount() - leadingArgCount);
    }

    public static MethodHandle exactInvoker(MethodType type) {
        return new Transformers.Invoker(type, true);
    }

    public static MethodHandle invoker(MethodType type) {
        return new Transformers.Invoker(type, false);
    }

    private static MethodHandle methodHandleForVarHandleAccessor(VarHandle.AccessMode accessMode, MethodType type, boolean isExactInvoker) {
        int kind;
        Class<VarHandle> cls = VarHandle.class;
        try {
            Method method = cls.getDeclaredMethod(accessMode.methodName(), Object[].class);
            MethodType methodType = type.insertParameterTypes(0, (Class<?>[]) new Class[]{VarHandle.class});
            if (isExactInvoker) {
                kind = 8;
            } else {
                kind = 7;
            }
            return new MethodHandleImpl(method.getArtMethod(), kind, methodType);
        } catch (NoSuchMethodException e) {
            throw new InternalError("No method for AccessMode " + accessMode, e);
        }
    }

    public static MethodHandle varHandleExactInvoker(VarHandle.AccessMode accessMode, MethodType type) {
        return methodHandleForVarHandleAccessor(accessMode, type, true);
    }

    public static MethodHandle varHandleInvoker(VarHandle.AccessMode accessMode, MethodType type) {
        return methodHandleForVarHandleAccessor(accessMode, type, false);
    }

    public static MethodHandle explicitCastArguments(MethodHandle target, MethodType newType) {
        explicitCastArgumentsChecks(target, newType);
        MethodType oldType = target.type();
        if (oldType == newType) {
            return target;
        }
        if (oldType.explicitCastEquivalentToAsType(newType)) {
            return target.asFixedArity().asType(newType);
        }
        return new Transformers.ExplicitCastArguments(target, newType);
    }

    private static void explicitCastArgumentsChecks(MethodHandle target, MethodType newType) {
        if (target.type().parameterCount() != newType.parameterCount()) {
            throw new WrongMethodTypeException("cannot explicitly cast " + target + " to " + newType);
        }
    }

    public static MethodHandle permuteArguments(MethodHandle target, MethodType newType, int... reorder) {
        int[] reorder2 = (int[]) reorder.clone();
        permuteArgumentChecks(reorder2, newType, target.type());
        return new Transformers.PermuteArguments(newType, target, reorder2);
    }

    private static boolean permuteArgumentChecks(int[] reorder, MethodType newType, MethodType oldType) {
        if (newType.returnType() == oldType.returnType()) {
            if (reorder.length == oldType.parameterCount()) {
                int limit = newType.parameterCount();
                boolean bad = false;
                int j = 0;
                while (true) {
                    if (j >= reorder.length) {
                        break;
                    }
                    int i = reorder[j];
                    if (i < 0 || i >= limit) {
                        bad = true;
                    } else if (newType.parameterType(i) == oldType.parameterType(j)) {
                        j++;
                    } else {
                        throw MethodHandleStatics.newIllegalArgumentException("parameter types do not match after reorder", oldType, newType);
                    }
                }
                bad = true;
                if (!bad) {
                    return true;
                }
            }
            throw MethodHandleStatics.newIllegalArgumentException("bad reorder array: " + Arrays.toString(reorder));
        }
        throw MethodHandleStatics.newIllegalArgumentException("return types do not match", oldType, newType);
    }

    public static MethodHandle constant(Class<?> type, Object value) {
        if (type.isPrimitive()) {
            if (type != Void.TYPE) {
                value = Wrapper.forPrimitiveType(type).convert(value, type);
            } else {
                throw MethodHandleStatics.newIllegalArgumentException("void type");
            }
        }
        return new Transformers.Constant(type, value);
    }

    public static MethodHandle identity(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        } else if (!type.isPrimitive()) {
            return new Transformers.ReferenceIdentity(type);
        } else {
            try {
                return Lookup.PUBLIC_LOOKUP.findStatic(MethodHandles.class, "identity", MethodType.methodType(type, type));
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new AssertionError((Object) e);
            }
        }
    }

    public static byte identity(byte val) {
        return val;
    }

    public static boolean identity(boolean val) {
        return val;
    }

    public static char identity(char val) {
        return val;
    }

    public static short identity(short val) {
        return val;
    }

    public static int identity(int val) {
        return val;
    }

    public static long identity(long val) {
        return val;
    }

    public static float identity(float val) {
        return val;
    }

    public static double identity(double val) {
        return val;
    }

    public static MethodHandle insertArguments(MethodHandle target, int pos, Object... values) {
        int insCount = values.length;
        Class<?>[] ptypes = insertArgumentsChecks(target, insCount, pos);
        if (insCount == 0) {
            return target;
        }
        for (int i = 0; i < insCount; i++) {
            Class<?> ptype = ptypes[pos + i];
            if (!ptype.isPrimitive()) {
                ptypes[pos + i].cast(values[i]);
            } else {
                values[i] = Wrapper.forPrimitiveType(ptype).convert(values[i], ptype);
            }
        }
        return new Transformers.InsertArguments(target, pos, values);
    }

    private static Class<?>[] insertArgumentsChecks(MethodHandle target, int insCount, int pos) throws RuntimeException {
        MethodType oldType = target.type();
        int inargs = oldType.parameterCount() - insCount;
        if (inargs < 0) {
            throw MethodHandleStatics.newIllegalArgumentException("too many values to insert");
        } else if (pos >= 0 && pos <= inargs) {
            return oldType.ptypes();
        } else {
            throw MethodHandleStatics.newIllegalArgumentException("no argument type to append");
        }
    }

    public static MethodHandle dropArguments(MethodHandle target, int pos, List<Class<?>> valueTypes) {
        List<Class<?>> valueTypes2 = copyTypes(valueTypes);
        MethodType oldType = target.type();
        int dropped = dropArgumentChecks(oldType, pos, valueTypes2);
        MethodType newType = oldType.insertParameterTypes(pos, valueTypes2);
        if (dropped == 0) {
            return target;
        }
        return new Transformers.DropArguments(newType, target, pos, valueTypes2.size());
    }

    private static List<Class<?>> copyTypes(List<Class<?>> types) {
        Object[] a = types.toArray();
        return Arrays.asList((Class[]) Arrays.copyOf(a, a.length, Class[].class));
    }

    private static int dropArgumentChecks(MethodType oldType, int pos, List<Class<?>> valueTypes) {
        int dropped = valueTypes.size();
        MethodType.checkSlotCount(dropped);
        int outargs = oldType.parameterCount();
        int inargs = outargs + dropped;
        if (pos >= 0 && pos <= outargs) {
            return dropped;
        }
        throw MethodHandleStatics.newIllegalArgumentException("no argument type to remove" + Arrays.asList(oldType, Integer.valueOf(pos), valueTypes, Integer.valueOf(inargs), Integer.valueOf(outargs)));
    }

    public static MethodHandle dropArguments(MethodHandle target, int pos, Class<?>... valueTypes) {
        return dropArguments(target, pos, (List<Class<?>>) Arrays.asList(valueTypes));
    }

    public static MethodHandle filterArguments(MethodHandle target, int pos, MethodHandle... filters) {
        filterArgumentsCheckArity(target, pos, filters);
        for (int i = 0; i < filters.length; i++) {
            filterArgumentChecks(target, i + pos, filters[i]);
        }
        return new Transformers.FilterArguments(target, pos, filters);
    }

    private static void filterArgumentsCheckArity(MethodHandle target, int pos, MethodHandle[] filters) {
        if (filters.length + pos > target.type().parameterCount()) {
            throw MethodHandleStatics.newIllegalArgumentException("too many filters");
        }
    }

    private static void filterArgumentChecks(MethodHandle target, int pos, MethodHandle filter) throws RuntimeException {
        MethodType targetType = target.type();
        MethodType filterType = filter.type();
        if (filterType.parameterCount() != 1 || filterType.returnType() != targetType.parameterType(pos)) {
            throw MethodHandleStatics.newIllegalArgumentException("target and filter types do not match", targetType, filterType);
        }
    }

    public static MethodHandle collectArguments(MethodHandle target, int pos, MethodHandle filter) {
        return new Transformers.CollectArguments(target, filter, pos, collectArgumentsChecks(target, pos, filter));
    }

    private static MethodType collectArgumentsChecks(MethodHandle target, int pos, MethodHandle filter) throws RuntimeException {
        MethodType targetType = target.type();
        MethodType filterType = filter.type();
        Class<?> rtype = filterType.returnType();
        List<Class<?>> filterArgs = filterType.parameterList();
        if (rtype == Void.TYPE) {
            return targetType.insertParameterTypes(pos, filterArgs);
        }
        if (rtype == targetType.parameterType(pos)) {
            return targetType.dropParameterTypes(pos, pos + 1).insertParameterTypes(pos, filterArgs);
        }
        throw MethodHandleStatics.newIllegalArgumentException("target and filter types do not match", targetType, filterType);
    }

    public static MethodHandle filterReturnValue(MethodHandle target, MethodHandle filter) {
        filterReturnValueChecks(target.type(), filter.type());
        return new Transformers.FilterReturnValue(target, filter);
    }

    private static void filterReturnValueChecks(MethodType targetType, MethodType filterType) throws RuntimeException {
        Class<?> rtype = targetType.returnType();
        int filterValues = filterType.parameterCount();
        if (filterValues == 0) {
            if (rtype == Void.TYPE) {
                return;
            }
        } else if (rtype == filterType.parameterType(0) && filterValues == 1) {
            return;
        }
        throw MethodHandleStatics.newIllegalArgumentException("target and filter types do not match", targetType, filterType);
    }

    public static MethodHandle foldArguments(MethodHandle target, MethodHandle combiner) {
        Class<?> foldArgumentChecks = foldArgumentChecks(0, target.type(), combiner.type());
        return new Transformers.FoldArguments(target, combiner);
    }

    private static Class<?> foldArgumentChecks(int foldPos, MethodType targetType, MethodType combinerType) {
        int foldArgs = combinerType.parameterCount();
        Class<?> rtype = combinerType.returnType();
        boolean ok = true;
        int foldVals = rtype == Void.TYPE ? 0 : 1;
        int afterInsertPos = foldPos + foldVals;
        if (targetType.parameterCount() < afterInsertPos + foldArgs) {
            ok = false;
        }
        if (ok && !combinerType.parameterList().equals(targetType.parameterList().subList(afterInsertPos, afterInsertPos + foldArgs))) {
            ok = false;
        }
        if (!(!ok || foldVals == 0 || combinerType.returnType() == targetType.parameterType(0))) {
            ok = false;
        }
        if (ok) {
            return rtype;
        }
        throw misMatchedTypes("target and combiner types", targetType, combinerType);
    }

    public static MethodHandle guardWithTest(MethodHandle test, MethodHandle target, MethodHandle fallback) {
        MethodType gtype = test.type();
        MethodType ttype = target.type();
        MethodType ftype = fallback.type();
        if (!ttype.equals((Object) ftype)) {
            throw misMatchedTypes("target and fallback types", ttype, ftype);
        } else if (gtype.returnType() == Boolean.TYPE) {
            List<Class<?>> targs = ttype.parameterList();
            List<Class<?>> gargs = gtype.parameterList();
            if (!targs.equals(gargs)) {
                int gpc = gargs.size();
                int tpc = targs.size();
                if (gpc >= tpc || !targs.subList(0, gpc).equals(gargs)) {
                    throw misMatchedTypes("target and test types", ttype, gtype);
                }
                test = dropArguments(test, gpc, targs.subList(gpc, tpc));
                MethodType gtype2 = test.type();
            }
            return new Transformers.GuardWithTest(test, target, fallback);
        } else {
            throw MethodHandleStatics.newIllegalArgumentException("guard type is not a predicate " + gtype);
        }
    }

    static RuntimeException misMatchedTypes(String what, MethodType t1, MethodType t2) {
        return MethodHandleStatics.newIllegalArgumentException(what + " must match: " + t1 + " != " + t2);
    }

    public static MethodHandle catchException(MethodHandle target, Class<? extends Throwable> exType, MethodHandle handler) {
        MethodType ttype = target.type();
        MethodType htype = handler.type();
        if (htype.parameterCount() < 1 || !htype.parameterType(0).isAssignableFrom(exType)) {
            throw MethodHandleStatics.newIllegalArgumentException("handler does not accept exception type " + exType);
        } else if (htype.returnType() == ttype.returnType()) {
            List<Class<?>> targs = ttype.parameterList();
            List<Class<?>> hargs = htype.parameterList();
            List<Class<?>> hargs2 = hargs.subList(1, hargs.size());
            if (!targs.equals(hargs2)) {
                int hpc = hargs2.size();
                if (hpc >= targs.size() || !targs.subList(0, hpc).equals(hargs2)) {
                    throw misMatchedTypes("target and handler types", ttype, htype);
                }
            }
            return new Transformers.CatchException(target, handler, exType);
        } else {
            throw misMatchedTypes("target and handler return types", ttype, htype);
        }
    }

    public static MethodHandle throwException(Class<?> returnType, Class<? extends Throwable> exType) {
        if (Throwable.class.isAssignableFrom(exType)) {
            return new Transformers.AlwaysThrow(returnType, exType);
        }
        throw new ClassCastException(exType.getName());
    }
}
