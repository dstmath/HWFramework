package java.lang.invoke;

import dalvik.system.VMStack;
import java.lang.invoke.Transformers.AlwaysThrow;
import java.lang.invoke.Transformers.CatchException;
import java.lang.invoke.Transformers.Constant;
import java.lang.invoke.Transformers.DropArguments;
import java.lang.invoke.Transformers.ExplicitCastArguments;
import java.lang.invoke.Transformers.FilterReturnValue;
import java.lang.invoke.Transformers.GuardWithTest;
import java.lang.invoke.Transformers.PermuteArguments;
import java.lang.invoke.Transformers.ReferenceArrayElementGetter;
import java.lang.invoke.Transformers.ReferenceArrayElementSetter;
import java.lang.invoke.Transformers.ReferenceIdentity;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import sun.invoke.util.VerifyAccess;
import sun.invoke.util.Wrapper;

public class MethodHandles {

    public static final class Lookup {
        static final /* synthetic */ boolean -assertionsDisabled = (Lookup.class.desiredAssertionStatus() ^ 1);
        private static final int ALL_MODES = 15;
        static final Lookup IMPL_LOOKUP = new Lookup(Object.class, 15);
        public static final int PACKAGE = 8;
        public static final int PRIVATE = 2;
        public static final int PROTECTED = 4;
        public static final int PUBLIC = 1;
        static final Lookup PUBLIC_LOOKUP = new Lookup(Object.class, 1);
        private final int allowedModes;
        private final Class<?> lookupClass;

        private static int fixmods(int mods) {
            mods &= 7;
            return mods != 0 ? mods : 8;
        }

        public Class<?> lookupClass() {
            return this.lookupClass;
        }

        public int lookupModes() {
            return this.allowedModes & 15;
        }

        Lookup(Class<?> lookupClass) {
            this(lookupClass, 15);
            checkUnprivilegedlookupClass(lookupClass, 15);
        }

        private Lookup(Class<?> lookupClass, int allowedModes) {
            this.lookupClass = lookupClass;
            this.allowedModes = allowedModes;
        }

        public Lookup in(Class<?> requestedLookupClass) {
            requestedLookupClass.getClass();
            if (requestedLookupClass == this.lookupClass) {
                return this;
            }
            int newModes = this.allowedModes & 11;
            if (!((newModes & 8) == 0 || (VerifyAccess.isSamePackage(this.lookupClass, requestedLookupClass) ^ 1) == 0)) {
                newModes &= -11;
            }
            if (!((newModes & 2) == 0 || (VerifyAccess.isSamePackageMember(this.lookupClass, requestedLookupClass) ^ 1) == 0)) {
                newModes &= -3;
            }
            if (!((newModes & 1) == 0 || (VerifyAccess.isClassAccessible(requestedLookupClass, this.lookupClass, this.allowedModes) ^ 1) == 0)) {
                newModes = 0;
            }
            checkUnprivilegedlookupClass(requestedLookupClass, newModes);
            return new Lookup(requestedLookupClass, newModes);
        }

        private static void checkUnprivilegedlookupClass(Class<?> lookupClass, int allowedModes) {
            String name = lookupClass.getName();
            if (name.startsWith("java.lang.invoke.")) {
                throw MethodHandleStatics.newIllegalArgumentException("illegal lookupClass: " + lookupClass);
            } else if (allowedModes != 15 || lookupClass.getClassLoader() != Object.class.getClassLoader()) {
            } else {
                if (name.startsWith("java.") || !(!name.startsWith("sun.") || (name.startsWith("sun.invoke.") ^ 1) == 0 || (name.equals("sun.reflect.ReflectionFactory") ^ 1) == 0)) {
                    throw MethodHandleStatics.newIllegalArgumentException("illegal lookupClass: " + lookupClass);
                }
            }
        }

        public String toString() {
            String cname = this.lookupClass.getName();
            switch (this.allowedModes) {
                case 0:
                    return cname + "/noaccess";
                case 1:
                    return cname + "/public";
                case 9:
                    return cname + "/package";
                case 11:
                    return cname + "/private";
                case 15:
                    return cname;
                default:
                    Object cname2 = cname + "/" + Integer.toHexString(this.allowedModes);
                    if (-assertionsDisabled) {
                        return cname2;
                    }
                    throw new AssertionError(cname2);
            }
        }

        public MethodHandle findStatic(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            Object method = refc.getDeclaredMethod(name, type.ptypes());
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

        private static MethodHandle createMethodHandle(Method method, int handleKind, MethodType methodType) {
            MethodHandle mh = new MethodHandleImpl(method.getArtMethod(), handleKind, methodType);
            if (method.isVarArgs()) {
                return new VarargsCollector(mh);
            }
            return mh;
        }

        public MethodHandle findVirtual(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            if (refc == MethodHandle.class) {
                MethodHandle mh = findVirtualForMH(name, type);
                if (mh != null) {
                    return mh;
                }
            }
            Method method = refc.getInstanceMethod(name, type.ptypes());
            if (method == null) {
                try {
                    Object m = refc.getDeclaredMethod(name, type.ptypes());
                    if (Modifier.isStatic(m.getModifiers())) {
                        throw new IllegalAccessException("Method" + m + " is static");
                    }
                } catch (NoSuchMethodException e) {
                }
                throw new NoSuchMethodException(name + " " + Arrays.toString(type.ptypes()));
            }
            checkReturnType(method, type);
            checkAccess(refc, method.getDeclaringClass(), method.getModifiers(), method.getName());
            return createMethodHandle(method, 0, type.insertParameterTypes(0, refc));
        }

        public MethodHandle findConstructor(Class<?> refc, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            if (refc.isArray()) {
                throw new NoSuchMethodException("no constructor for array class: " + refc.getName());
            }
            Constructor constructor = refc.getDeclaredConstructor(type.ptypes());
            if (constructor == null) {
                throw new NoSuchMethodException("No constructor for " + constructor.getDeclaringClass() + " matching " + type);
            }
            checkAccess(refc, constructor.getDeclaringClass(), constructor.getModifiers(), constructor.getName());
            return createMethodHandleForConstructor(constructor);
        }

        private MethodHandle createMethodHandleForConstructor(Constructor constructor) {
            MethodHandle mh;
            Class refc = constructor.getDeclaringClass();
            MethodType constructorType = MethodType.methodType(refc, constructor.getParameterTypes());
            if (refc == String.class) {
                mh = new MethodHandleImpl(constructor.getArtMethod(), 2, constructorType);
            } else {
                mh = new Construct(new MethodHandleImpl(constructor.getArtMethod(), 2, initMethodType(constructorType)), constructorType);
            }
            if (constructor.isVarArgs()) {
                return new VarargsCollector(mh);
            }
            return mh;
        }

        private static MethodType initMethodType(MethodType constructorType) {
            if (-assertionsDisabled || constructorType.rtype() != Void.TYPE) {
                Class[] initPtypes = new Class[(constructorType.ptypes().length + 1)];
                initPtypes[0] = constructorType.rtype();
                System.arraycopy(constructorType.ptypes(), 0, (Object) initPtypes, 1, constructorType.ptypes().length);
                return MethodType.methodType(Void.TYPE, initPtypes);
            }
            throw new AssertionError();
        }

        public MethodHandle findSpecial(Class<?> refc, String name, MethodType type, Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException {
            if (specialCaller == null) {
                throw new NullPointerException("specialCaller == null");
            } else if (type == null) {
                throw new NullPointerException("type == null");
            } else if (name == null) {
                throw new NullPointerException("name == null");
            } else if (refc == null) {
                throw new NullPointerException("ref == null");
            } else {
                checkSpecialCaller(specialCaller);
                if (name.startsWith("<")) {
                    throw new NoSuchMethodException(name + " is not a valid method name.");
                }
                Method method = refc.getDeclaredMethod(name, type.ptypes());
                checkReturnType(method, type);
                return findSpecial(method, type, (Class) refc, (Class) specialCaller);
            }
        }

        private MethodHandle findSpecial(Method method, MethodType type, Class<?> refc, Class<?> specialCaller) throws IllegalAccessException {
            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalAccessException("expected a non-static method:" + method);
            } else if (Modifier.isPrivate(method.getModifiers())) {
                if (refc != lookupClass()) {
                    throw new IllegalAccessException("no private access for invokespecial : " + refc + ", from" + this);
                }
                return createMethodHandle(method, 2, type.insertParameterTypes(0, refc));
            } else if (method.getDeclaringClass().isAssignableFrom(specialCaller)) {
                return createMethodHandle(method, 1, type.insertParameterTypes(0, specialCaller));
            } else {
                throw new IllegalAccessException(refc + "is not assignable from " + specialCaller);
            }
        }

        public MethodHandle findGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            return findAccessor(refc, name, type, 7);
        }

        private MethodHandle findAccessor(Class<?> refc, String name, Class<?> type, int kind) throws NoSuchFieldException, IllegalAccessException {
            Field field = refc.getDeclaredField(name);
            Object fieldType = field.getType();
            if (fieldType == type) {
                return findAccessor(field, refc, type, kind, true);
            }
            throw new NoSuchFieldException("Field has wrong type: " + fieldType + " != " + type);
        }

        private MethodHandle findAccessor(Field field, Class<?> refc, Class<?> fieldType, int kind, boolean performAccessChecks) throws IllegalAccessException {
            if (!performAccessChecks) {
                checkAccess(refc, field.getDeclaringClass(), field.getModifiers(), field.getName());
            }
            boolean isStaticKind = (kind == 9 || kind == 10) ? true : -assertionsDisabled;
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) != isStaticKind) {
                throw new IllegalAccessException("Field " + field + " is " + (isStaticKind ? "not " : "") + "static");
            }
            boolean isSetterKind = (kind == 8 || kind == 10) ? true : -assertionsDisabled;
            if (Modifier.isFinal(modifiers) && isSetterKind) {
                throw new IllegalAccessException("Field " + field + " is final");
            }
            MethodType methodType;
            switch (kind) {
                case 7:
                    methodType = MethodType.methodType((Class) fieldType, (Class) refc);
                    break;
                case 8:
                    methodType = MethodType.methodType(Void.TYPE, refc, fieldType);
                    break;
                case 9:
                    methodType = MethodType.methodType(fieldType);
                    break;
                case 10:
                    methodType = MethodType.methodType(Void.TYPE, (Class) fieldType);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid kind " + kind);
            }
            return new MethodHandleImpl(field.getArtField(), kind, methodType);
        }

        public MethodHandle findSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            return findAccessor(refc, name, type, 8);
        }

        public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            return findAccessor(refc, name, type, 9);
        }

        public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            return findAccessor(refc, name, type, 10);
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
            if (m == null) {
                throw new NullPointerException("m == null");
            }
            MethodType methodType = MethodType.methodType(m.getReturnType(), m.getParameterTypes());
            if (!m.isAccessible()) {
                checkAccess(m.getDeclaringClass(), m.getDeclaringClass(), m.getModifiers(), m.getName());
            }
            if (Modifier.isStatic(m.getModifiers())) {
                return createMethodHandle(m, 3, methodType);
            }
            return createMethodHandle(m, 0, methodType.insertParameterTypes(0, m.getDeclaringClass()));
        }

        public MethodHandle unreflectSpecial(Method m, Class<?> specialCaller) throws IllegalAccessException {
            if (m == null) {
                throw new NullPointerException("m == null");
            } else if (specialCaller == null) {
                throw new NullPointerException("specialCaller == null");
            } else {
                if (!m.isAccessible()) {
                    checkSpecialCaller(specialCaller);
                }
                return findSpecial(m, MethodType.methodType(m.getReturnType(), m.getParameterTypes()), m.getDeclaringClass(), (Class) specialCaller);
            }
        }

        public MethodHandle unreflectConstructor(Constructor<?> c) throws IllegalAccessException {
            if (c == null) {
                throw new NullPointerException("c == null");
            }
            if (!c.isAccessible()) {
                checkAccess(c.getDeclaringClass(), c.getDeclaringClass(), c.getModifiers(), c.getName());
            }
            return createMethodHandleForConstructor(c);
        }

        public MethodHandle unreflectGetter(Field f) throws IllegalAccessException {
            return findAccessor(f, f.getDeclaringClass(), f.getType(), Modifier.isStatic(f.getModifiers()) ? 9 : 7, f.isAccessible());
        }

        public MethodHandle unreflectSetter(Field f) throws IllegalAccessException {
            return findAccessor(f, f.getDeclaringClass(), f.getType(), Modifier.isStatic(f.getModifiers()) ? 10 : 8, f.isAccessible());
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
            return (this.allowedModes & 2) != 0 ? true : -assertionsDisabled;
        }

        void checkAccess(Class<?> refc, Class<?> defc, int mods, String methName) throws IllegalAccessException {
            int allowedModes = this.allowedModes;
            if (Modifier.isProtected(mods) && defc == Object.class && "clone".equals(methName) && refc.isArray()) {
                mods ^= 5;
            }
            if (Modifier.isProtected(mods) && Modifier.isConstructor(mods)) {
                mods ^= 4;
            }
            if (!Modifier.isPublic(mods) || !Modifier.isPublic(refc.getModifiers()) || allowedModes == 0) {
                int requestedModes = fixmods(mods);
                if ((requestedModes & allowedModes) != 0) {
                    if (VerifyAccess.isMemberAccessible(refc, defc, mods, lookupClass(), allowedModes)) {
                        return;
                    }
                } else if (!((requestedModes & 4) == 0 || (allowedModes & 8) == 0 || !VerifyAccess.isSamePackage(defc, lookupClass()))) {
                    return;
                }
                throwMakeAccessException(accessFailedMessage(refc, defc, mods), this);
            }
        }

        String accessFailedMessage(Class<?> refc, Class<?> defc, int mods) {
            boolean classOK;
            if (!Modifier.isPublic(defc.getModifiers())) {
                classOK = -assertionsDisabled;
            } else if (defc != refc) {
                classOK = Modifier.isPublic(refc.getModifiers());
            } else {
                classOK = true;
            }
            if (!(classOK || (this.allowedModes & 8) == 0)) {
                if (!VerifyAccess.isClassAccessible(defc, lookupClass(), 15)) {
                    classOK = -assertionsDisabled;
                } else if (defc != refc) {
                    classOK = VerifyAccess.isClassAccessible(refc, lookupClass(), 15);
                } else {
                    classOK = true;
                }
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
            message = message + ": " + toString();
            if (from != null) {
                message = message + ", from " + from;
            }
            throw new IllegalAccessException(message);
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

    private static MethodHandleImpl getMethodHandleImpl(MethodHandle target) {
        Object target2;
        if (target2 instanceof Construct) {
            target2 = ((Construct) target2).getConstructorHandle();
        }
        if (target2 instanceof VarargsCollector) {
            target2 = target2.asFixedArity();
        }
        if (target2 instanceof MethodHandleImpl) {
            return (MethodHandleImpl) target2;
        }
        throw new IllegalArgumentException(target2 + " is not a direct handle");
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x003e A:{Splitter: B:6:0x0026, ExcHandler: java.lang.NoSuchMethodException (r1_0 'exception' java.lang.Object)} */
    /* JADX WARNING: Missing block: B:9:0x003e, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0044, code:
            throw new java.lang.AssertionError(r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static MethodHandle arrayElementGetter(Class<?> arrayClass) throws IllegalArgumentException {
        Class<?> componentType = arrayClass.getComponentType();
        if (componentType == null) {
            throw new IllegalArgumentException("Not an array type: " + arrayClass);
        } else if (!componentType.isPrimitive()) {
            return new ReferenceArrayElementGetter(arrayClass);
        } else {
            try {
                return Lookup.PUBLIC_LOOKUP.findStatic(MethodHandles.class, "arrayElementGetter", MethodType.methodType(componentType, arrayClass, Integer.TYPE));
            } catch (Object exception) {
            }
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

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0043 A:{Splitter: B:6:0x0026, ExcHandler: java.lang.NoSuchMethodException (r1_0 'exception' java.lang.Object)} */
    /* JADX WARNING: Missing block: B:9:0x0043, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0049, code:
            throw new java.lang.AssertionError(r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static MethodHandle arrayElementSetter(Class<?> arrayClass) throws IllegalArgumentException {
        Class<?> componentType = arrayClass.getComponentType();
        if (componentType == null) {
            throw new IllegalArgumentException("Not an array type: " + arrayClass);
        } else if (!componentType.isPrimitive()) {
            return new ReferenceArrayElementSetter(arrayClass);
        } else {
            try {
                return Lookup.PUBLIC_LOOKUP.findStatic(MethodHandles.class, "arrayElementSetter", MethodType.methodType(Void.TYPE, arrayClass, Integer.TYPE, componentType));
            } catch (Object exception) {
            }
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

    public static MethodHandle spreadInvoker(MethodType type, int leadingArgCount) {
        if (leadingArgCount < 0 || leadingArgCount > type.parameterCount()) {
            throw MethodHandleStatics.newIllegalArgumentException("bad argument count", Integer.valueOf(leadingArgCount));
        }
        return invoker(type).asSpreader(Object[].class, type.parameterCount() - leadingArgCount);
    }

    public static MethodHandle exactInvoker(MethodType type) {
        return new Invoker(type, true);
    }

    public static MethodHandle invoker(MethodType type) {
        return new Invoker(type, false);
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
        return new ExplicitCastArguments(target, newType);
    }

    private static void explicitCastArgumentsChecks(MethodHandle target, MethodType newType) {
        if (target.type().parameterCount() != newType.parameterCount()) {
            throw new WrongMethodTypeException("cannot explicitly cast " + target + " to " + newType);
        }
    }

    public static MethodHandle permuteArguments(MethodHandle target, MethodType newType, int... reorder) {
        reorder = (int[]) reorder.clone();
        permuteArgumentChecks(reorder, newType, target.type());
        return new PermuteArguments(newType, target, reorder);
    }

    private static boolean permuteArgumentChecks(int[] reorder, MethodType newType, MethodType oldType) {
        if (newType.returnType() != oldType.returnType()) {
            throw MethodHandleStatics.newIllegalArgumentException("return types do not match", oldType, newType);
        }
        if (reorder.length == oldType.parameterCount()) {
            int limit = newType.parameterCount();
            boolean bad = false;
            int j = 0;
            while (j < reorder.length) {
                int i = reorder[j];
                if (i < 0 || i >= limit) {
                    bad = true;
                    break;
                } else if (newType.parameterType(i) != oldType.parameterType(j)) {
                    throw MethodHandleStatics.newIllegalArgumentException("parameter types do not match after reorder", oldType, newType);
                } else {
                    j++;
                }
            }
            if (!bad) {
                return true;
            }
        }
        throw MethodHandleStatics.newIllegalArgumentException("bad reorder array: " + Arrays.toString(reorder));
    }

    public static MethodHandle constant(Class<?> type, Object value) {
        if (type.isPrimitive()) {
            if (type == Void.TYPE) {
                throw MethodHandleStatics.newIllegalArgumentException("void type");
            }
            value = Wrapper.forPrimitiveType(type).convert(value, type);
        }
        return new Constant(type, value);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0021 A:{Splitter: B:5:0x0011, ExcHandler: java.lang.NoSuchMethodException (r0_0 'e' java.lang.Object)} */
    /* JADX WARNING: Missing block: B:8:0x0021, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0027, code:
            throw new java.lang.AssertionError(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static MethodHandle identity(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        } else if (!type.isPrimitive()) {
            return new ReferenceIdentity(type);
        } else {
            try {
                return Lookup.PUBLIC_LOOKUP.findStatic(MethodHandles.class, "identity", MethodType.methodType((Class) type, (Class) type));
            } catch (Object e) {
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
            if (ptype.isPrimitive()) {
                values[i] = Wrapper.forPrimitiveType(ptype).convert(values[i], ptype);
            } else {
                ptypes[pos + i].cast(values[i]);
            }
        }
        return new InsertArguments(target, pos, values);
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
        List valueTypes2 = copyTypes(valueTypes);
        MethodType oldType = target.type();
        int dropped = dropArgumentChecks(oldType, pos, valueTypes2);
        MethodType newType = oldType.insertParameterTypes(pos, valueTypes2);
        if (dropped == 0) {
            return target;
        }
        return new DropArguments(newType, target, pos, valueTypes2.size());
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
        return dropArguments(target, pos, Arrays.asList(valueTypes));
    }

    public static MethodHandle filterArguments(MethodHandle target, int pos, MethodHandle... filters) {
        filterArgumentsCheckArity(target, pos, filters);
        for (int i = 0; i < filters.length; i++) {
            filterArgumentChecks(target, i + pos, filters[i]);
        }
        return new FilterArguments(target, pos, filters);
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
        return new CollectArguments(target, filter, pos, collectArgumentsChecks(target, pos, filter));
    }

    private static MethodType collectArgumentsChecks(MethodHandle target, int pos, MethodHandle filter) throws RuntimeException {
        MethodType targetType = target.type();
        MethodType filterType = filter.type();
        Class<?> rtype = filterType.returnType();
        List filterArgs = filterType.parameterList();
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
        return new FilterReturnValue(target, filter);
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
        Class<?> rtype = foldArgumentChecks(0, target.type(), combiner.type());
        return new FoldArguments(target, combiner);
    }

    private static Class<?> foldArgumentChecks(int foldPos, MethodType targetType, MethodType combinerType) {
        int foldArgs = combinerType.parameterCount();
        Class<?> rtype = combinerType.returnType();
        int foldVals = rtype == Void.TYPE ? 0 : 1;
        int afterInsertPos = foldPos + foldVals;
        boolean ok = targetType.parameterCount() >= afterInsertPos + foldArgs;
        if (ok && (combinerType.parameterList().equals(targetType.parameterList().subList(afterInsertPos, afterInsertPos + foldArgs)) ^ 1) != 0) {
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
        Object gtype = test.type();
        MethodType ttype = target.type();
        Object ftype = fallback.type();
        if (!ttype.equals(ftype)) {
            throw misMatchedTypes("target and fallback types", ttype, ftype);
        } else if (gtype.returnType() != Boolean.TYPE) {
            throw MethodHandleStatics.newIllegalArgumentException("guard type is not a predicate " + gtype);
        } else {
            List<Class<?>> targs = ttype.parameterList();
            List<Class<?>> gargs = gtype.parameterList();
            if (!targs.equals(gargs)) {
                int gpc = gargs.size();
                int tpc = targs.size();
                if (gpc >= tpc || (targs.subList(0, gpc).equals(gargs) ^ 1) != 0) {
                    throw misMatchedTypes("target and test types", ttype, gtype);
                }
                test = dropArguments(test, gpc, targs.subList(gpc, tpc));
                MethodType gtype2 = test.type();
            }
            return new GuardWithTest(test, target, fallback);
        }
    }

    static RuntimeException misMatchedTypes(String what, MethodType t1, MethodType t2) {
        return MethodHandleStatics.newIllegalArgumentException(what + " must match: " + t1 + " != " + t2);
    }

    public static MethodHandle catchException(MethodHandle target, Class<? extends Throwable> exType, MethodHandle handler) {
        MethodType ttype = target.type();
        MethodType htype = handler.type();
        if (htype.parameterCount() < 1 || (htype.parameterType(0).isAssignableFrom(exType) ^ 1) != 0) {
            throw MethodHandleStatics.newIllegalArgumentException("handler does not accept exception type " + exType);
        } else if (htype.returnType() != ttype.returnType()) {
            throw misMatchedTypes("target and handler return types", ttype, htype);
        } else {
            List<Class<?>> targs = ttype.parameterList();
            List<Class<?>> hargs = htype.parameterList();
            hargs = hargs.subList(1, hargs.size());
            if (!targs.equals(hargs)) {
                int hpc = hargs.size();
                if (hpc >= targs.size() || (targs.subList(0, hpc).equals(hargs) ^ 1) != 0) {
                    throw misMatchedTypes("target and handler types", ttype, htype);
                }
            }
            return new CatchException(target, handler, exType);
        }
    }

    public static MethodHandle throwException(Class<?> returnType, Class<? extends Throwable> exType) {
        if (Throwable.class.isAssignableFrom(exType)) {
            return new AlwaysThrow(returnType, exType);
        }
        throw new ClassCastException(exType.getName());
    }
}
