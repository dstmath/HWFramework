package java.lang;

import dalvik.system.ClassExt;
import dalvik.system.VMStack;
import java.awt.font.NumericShaper;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import libcore.reflect.GenericSignatureParser;
import libcore.reflect.Types;
import libcore.util.BasicLruCache;
import libcore.util.CollectionUtils;
import libcore.util.EmptyArray;
import sun.reflect.CallerSensitive;

public final class Class<T> implements Serializable, GenericDeclaration, Type, AnnotatedElement {
    private static final int ANNOTATION = 8192;
    private static final int ENUM = 16384;
    private static final int FINALIZABLE = Integer.MIN_VALUE;
    private static final int SYNTHETIC = 4096;
    private static final long serialVersionUID = 3206093459760846163L;
    private transient int accessFlags;
    private transient int classFlags;
    private transient ClassLoader classLoader;
    private transient int classSize;
    private transient int clinitThreadId;
    private transient Class<?> componentType;
    private transient short copiedMethodsOffset;
    private transient Object dexCache;
    private transient int dexClassDefIndex;
    private volatile transient int dexTypeIndex;
    private transient ClassExt extData;
    private transient long iFields;
    private transient Object[] ifTable;
    private transient long methods;
    private transient String name;
    private transient int numReferenceInstanceFields;
    private transient int numReferenceStaticFields;
    private transient int objectSize;
    private transient int objectSizeAllocFastPath;
    private transient int primitiveType;
    private transient int referenceInstanceOffsets;
    private transient long sFields;
    private transient int status;
    private transient Class<? super T> superClass;
    private transient short virtualMethodsOffset;
    private transient Object vtable;

    private static class Caches {
        /* access modifiers changed from: private */
        public static final BasicLruCache<Class, Type[]> genericInterfaces = new BasicLruCache<>(8);

        private Caches() {
        }
    }

    static native Class<?> classForName(String str, boolean z, ClassLoader classLoader2) throws ClassNotFoundException;

    private native Constructor<T> getDeclaredConstructorInternal(Class<?>[] clsArr);

    private native Constructor<?>[] getDeclaredConstructorsInternal(boolean z);

    private native Method getDeclaredMethodInternal(String str, Class<?>[] clsArr);

    private native Constructor<?> getEnclosingConstructorNative();

    private native Method getEnclosingMethodNative();

    private native int getInnerClassFlags(int i);

    private native String getInnerClassName();

    private native Class<?>[] getInterfacesInternal();

    private native String getNameNative();

    static native Class<?> getPrimitiveClass(String str);

    private native Field[] getPublicDeclaredFields();

    private native Field getPublicFieldRecursive(String str);

    private native String[] getSignatureAnnotation();

    private native boolean isDeclaredAnnotationPresent(Class<? extends Annotation> cls);

    public native <A extends Annotation> A getDeclaredAnnotation(Class<A> cls);

    public native Annotation[] getDeclaredAnnotations();

    public native Class<?>[] getDeclaredClasses();

    public native Field getDeclaredField(String str) throws NoSuchFieldException;

    public native Field[] getDeclaredFields();

    public native Field[] getDeclaredFieldsUnchecked(boolean z);

    public native Method[] getDeclaredMethodsUnchecked(boolean z);

    public native Class<?> getDeclaringClass();

    public native Class<?> getEnclosingClass();

    public native boolean isAnonymousClass();

    public native T newInstance() throws InstantiationException, IllegalAccessException;

    private Class() {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isInterface() ? "interface " : isPrimitive() ? "" : "class ");
        sb.append(getName());
        return sb.toString();
    }

    public String toGenericString() {
        if (isPrimitive()) {
            return toString();
        }
        StringBuilder sb = new StringBuilder();
        int modifiers = getModifiers() & Modifier.classModifiers();
        if (modifiers != 0) {
            sb.append(Modifier.toString(modifiers));
            sb.append(' ');
        }
        if (isAnnotation()) {
            sb.append('@');
        }
        if (isInterface()) {
            sb.append("interface");
        } else if (isEnum()) {
            sb.append("enum");
        } else {
            sb.append("class");
        }
        sb.append(' ');
        sb.append(getName());
        TypeVariable<?>[] typeparms = getTypeParameters();
        if (typeparms.length > 0) {
            boolean first = true;
            sb.append('<');
            for (TypeVariable<?> typeparm : typeparms) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(typeparm.getTypeName());
                first = false;
            }
            sb.append('>');
        }
        return sb.toString();
    }

    @CallerSensitive
    public static Class<?> forName(String className) throws ClassNotFoundException {
        return forName(className, true, VMStack.getCallingClassLoader());
    }

    @CallerSensitive
    public static Class<?> forName(String name2, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        if (loader == null) {
            loader = BootClassLoader.getInstance();
        }
        try {
            return classForName(name2, initialize, loader);
        } catch (ClassNotFoundException e) {
            Throwable cause = e.getCause();
            if (cause instanceof LinkageError) {
                throw ((LinkageError) cause);
            }
            throw e;
        }
    }

    public boolean isInstance(Object obj) {
        if (obj == null) {
            return false;
        }
        return isAssignableFrom(obj.getClass());
    }

    public boolean isAssignableFrom(Class<?> cls) {
        boolean z = true;
        if (this == cls) {
            return true;
        }
        if (this == Object.class) {
            return true ^ cls.isPrimitive();
        }
        if (isArray()) {
            if (!cls.isArray() || !this.componentType.isAssignableFrom(cls.componentType)) {
                z = false;
            }
            return z;
        } else if (isInterface()) {
            Object[] iftable = cls.ifTable;
            if (iftable != null) {
                for (int i = 0; i < iftable.length; i += 2) {
                    if (iftable[i] == this) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            boolean isInterface = cls.isInterface();
            Class cls2 = cls;
            if (!isInterface) {
                do {
                    cls2 = cls2.superClass;
                    if (cls2 != null) {
                    }
                } while (cls2 != this);
                return true;
            }
            return false;
        }
    }

    public boolean isInterface() {
        return (this.accessFlags & 512) != 0;
    }

    public boolean isArray() {
        return getComponentType() != null;
    }

    public boolean isPrimitive() {
        return (this.primitiveType & 65535) != 0;
    }

    public boolean isFinalizable() {
        return (getModifiers() & Integer.MIN_VALUE) != 0;
    }

    public boolean isAnnotation() {
        return (getModifiers() & 8192) != 0;
    }

    public boolean isSynthetic() {
        return (getModifiers() & 4096) != 0;
    }

    public String getName() {
        String name2 = this.name;
        if (name2 != null) {
            return name2;
        }
        String nameNative = getNameNative();
        String name3 = nameNative;
        this.name = nameNative;
        return name3;
    }

    public ClassLoader getClassLoader() {
        if (isPrimitive()) {
            return null;
        }
        return this.classLoader == null ? BootClassLoader.getInstance() : this.classLoader;
    }

    public synchronized TypeVariable<Class<T>>[] getTypeParameters() {
        String annotationSignature = getSignatureAttribute();
        if (annotationSignature == null) {
            return EmptyArray.TYPE_VARIABLE;
        }
        GenericSignatureParser parser = new GenericSignatureParser(getClassLoader());
        parser.parseForClass(this, annotationSignature);
        return parser.formalTypeParameters;
    }

    public Class<? super T> getSuperclass() {
        if (isInterface()) {
            return null;
        }
        return this.superClass;
    }

    public Type getGenericSuperclass() {
        Type genericSuperclass = getSuperclass();
        if (genericSuperclass == null) {
            return null;
        }
        String annotationSignature = getSignatureAttribute();
        if (annotationSignature != null) {
            GenericSignatureParser parser = new GenericSignatureParser(getClassLoader());
            parser.parseForClass(this, annotationSignature);
            genericSuperclass = parser.superclassType;
        }
        return Types.getType(genericSuperclass);
    }

    public Package getPackage() {
        ClassLoader loader = getClassLoader();
        Package packageR = null;
        if (loader == null) {
            return null;
        }
        String packageName = getPackageName$();
        if (packageName != null) {
            packageR = loader.getPackage(packageName);
        }
        return packageR;
    }

    public String getPackageName$() {
        String name2 = getName();
        int last = name2.lastIndexOf(46);
        if (last == -1) {
            return null;
        }
        return name2.substring(0, last);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: java.lang.Class<?>[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    public Class<?>[] getInterfaces() {
        if (isArray()) {
            return new Class[]{Cloneable.class, Serializable.class};
        }
        Class<?>[] ifaces = getInterfacesInternal();
        if (ifaces == null) {
            return EmptyArray.CLASS;
        }
        return ifaces;
    }

    public Type[] getGenericInterfaces() {
        Type[] result;
        synchronized (Caches.genericInterfaces) {
            result = (Type[]) Caches.genericInterfaces.get(this);
            if (result == null) {
                String annotationSignature = getSignatureAttribute();
                if (annotationSignature == null) {
                    result = getInterfaces();
                } else {
                    GenericSignatureParser parser = new GenericSignatureParser(getClassLoader());
                    parser.parseForClass(this, annotationSignature);
                    result = Types.getTypeArray(parser.interfaceTypes, false);
                }
                Caches.genericInterfaces.put(this, result);
            }
        }
        Type[] result2 = result;
        return result2.length == 0 ? result2 : (Type[]) result2.clone();
    }

    public Class<?> getComponentType() {
        return this.componentType;
    }

    public int getModifiers() {
        if (!isArray()) {
            return getInnerClassFlags(this.accessFlags & 65535) & 65535;
        }
        int componentModifiers = getComponentType().getModifiers();
        if ((componentModifiers & 512) != 0) {
            componentModifiers &= -521;
        }
        return 1040 | componentModifiers;
    }

    public Object[] getSigners() {
        return null;
    }

    public Method getEnclosingMethod() {
        if (classNameImpliesTopLevel()) {
            return null;
        }
        return getEnclosingMethodNative();
    }

    public Constructor<?> getEnclosingConstructor() {
        if (classNameImpliesTopLevel()) {
            return null;
        }
        return getEnclosingConstructorNative();
    }

    private boolean classNameImpliesTopLevel() {
        return !getName().contains("$");
    }

    public String getSimpleName() {
        if (isArray()) {
            return getComponentType().getSimpleName() + "[]";
        } else if (isAnonymousClass()) {
            return "";
        } else {
            if (isMemberClass() || isLocalClass()) {
                return getInnerClassName();
            }
            String simpleName = getName();
            if (simpleName.lastIndexOf(".") > 0) {
                return simpleName.substring(simpleName.lastIndexOf(".") + 1);
            }
            return simpleName;
        }
    }

    public String getTypeName() {
        if (isArray()) {
            Class<?> cl = this;
            int dimensions = 0;
            while (cl.isArray()) {
                try {
                    dimensions++;
                    cl = cl.getComponentType();
                } catch (Throwable th) {
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append(cl.getName());
            for (int i = 0; i < dimensions; i++) {
                sb.append("[]");
            }
            return sb.toString();
        }
        return getName();
    }

    public String getCanonicalName() {
        if (isArray()) {
            String canonicalName = getComponentType().getCanonicalName();
            if (canonicalName == null) {
                return null;
            }
            return canonicalName + "[]";
        } else if (isLocalOrAnonymousClass()) {
            return null;
        } else {
            Class<?> enclosingClass = getEnclosingClass();
            if (enclosingClass == null) {
                return getName();
            }
            String enclosingName = enclosingClass.getCanonicalName();
            if (enclosingName == null) {
                return null;
            }
            return enclosingName + "." + getSimpleName();
        }
    }

    public boolean isLocalClass() {
        return !(getEnclosingMethod() == null && getEnclosingConstructor() == null) && !isAnonymousClass();
    }

    public boolean isMemberClass() {
        return getDeclaringClass() != null;
    }

    private boolean isLocalOrAnonymousClass() {
        return isLocalClass() || isAnonymousClass();
    }

    @CallerSensitive
    public Class<?>[] getClasses() {
        List<Class<?>> result = new ArrayList<>();
        for (Class cls = this; cls != null; cls = cls.superClass) {
            for (Class<?> member : cls.getDeclaredClasses()) {
                if (Modifier.isPublic(member.getModifiers())) {
                    result.add(member);
                }
            }
        }
        return (Class[]) result.toArray(new Class[result.size()]);
    }

    @CallerSensitive
    public Field[] getFields() throws SecurityException {
        List<Field> fields = new ArrayList<>();
        getPublicFieldsRecursive(fields);
        return (Field[]) fields.toArray(new Field[fields.size()]);
    }

    private void getPublicFieldsRecursive(List<Field> result) {
        for (Class cls = this; cls != null; cls = cls.superClass) {
            Collections.addAll(result, cls.getPublicDeclaredFields());
        }
        Object[] iftable = this.ifTable;
        if (iftable != null) {
            for (int i = 0; i < iftable.length; i += 2) {
                Collections.addAll(result, ((Class) iftable[i]).getPublicDeclaredFields());
            }
        }
    }

    @CallerSensitive
    public Method[] getMethods() throws SecurityException {
        List<Method> methods2 = new ArrayList<>();
        getPublicMethodsInternal(methods2);
        CollectionUtils.removeDuplicates(methods2, Method.ORDER_BY_SIGNATURE);
        return (Method[]) methods2.toArray(new Method[methods2.size()]);
    }

    private void getPublicMethodsInternal(List<Method> result) {
        Collections.addAll(result, getDeclaredMethodsUnchecked(true));
        if (!isInterface()) {
            for (Class<? super T> cls = this.superClass; cls != null; cls = cls.superClass) {
                Collections.addAll(result, cls.getDeclaredMethodsUnchecked(true));
            }
        }
        Object[] iftable = this.ifTable;
        if (iftable != null) {
            for (int i = 0; i < iftable.length; i += 2) {
                Collections.addAll(result, ((Class) iftable[i]).getDeclaredMethodsUnchecked(true));
            }
        }
    }

    @CallerSensitive
    public Constructor<?>[] getConstructors() throws SecurityException {
        return getDeclaredConstructorsInternal(true);
    }

    public Field getField(String name2) throws NoSuchFieldException {
        if (name2 != null) {
            Field result = getPublicFieldRecursive(name2);
            if (result != null) {
                return result;
            }
            throw new NoSuchFieldException(name2);
        }
        throw new NullPointerException("name == null");
    }

    @CallerSensitive
    public Method getMethod(String name2, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return getMethod(name2, parameterTypes, true);
    }

    public Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return getConstructor0(parameterTypes, 0);
    }

    public Method[] getDeclaredMethods() throws SecurityException {
        Method[] result = getDeclaredMethodsUnchecked(false);
        for (Method m : result) {
            m.getReturnType();
            m.getParameterTypes();
        }
        return result;
    }

    public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
        return getDeclaredConstructorsInternal(false);
    }

    @CallerSensitive
    public Method getDeclaredMethod(String name2, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return getMethod(name2, parameterTypes, false);
    }

    private Method getMethod(String name2, Class<?>[] parameterTypes, boolean recursivePublicMethods) throws NoSuchMethodException {
        Method result;
        if (name2 != null) {
            if (parameterTypes == null) {
                parameterTypes = EmptyArray.CLASS;
            }
            int length = parameterTypes.length;
            int i = 0;
            while (i < length) {
                if (parameterTypes[i] != null) {
                    i++;
                } else {
                    throw new NoSuchMethodException("parameter type is null");
                }
            }
            if (recursivePublicMethods) {
                result = getPublicMethodRecursive(name2, parameterTypes);
            } else {
                result = getDeclaredMethodInternal(name2, parameterTypes);
            }
            if (result != null && (!recursivePublicMethods || Modifier.isPublic(result.getAccessFlags()))) {
                return result;
            }
            throw new NoSuchMethodException(name2 + " " + Arrays.toString((Object[]) parameterTypes));
        }
        throw new NullPointerException("name == null");
    }

    private Method getPublicMethodRecursive(String name2, Class<?>[] parameterTypes) {
        for (Class cls = this; cls != null; cls = cls.getSuperclass()) {
            Method result = cls.getDeclaredMethodInternal(name2, parameterTypes);
            if (result != null && Modifier.isPublic(result.getAccessFlags())) {
                return result;
            }
        }
        return findInterfaceMethod(name2, parameterTypes);
    }

    public Method getInstanceMethod(String name2, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        for (Class cls = this; cls != null; cls = cls.getSuperclass()) {
            Method result = cls.getDeclaredMethodInternal(name2, parameterTypes);
            if (result != null && !Modifier.isStatic(result.getModifiers())) {
                return result;
            }
        }
        return findInterfaceMethod(name2, parameterTypes);
    }

    private Method findInterfaceMethod(String name2, Class<?>[] parameterTypes) {
        Object[] iftable = this.ifTable;
        if (iftable != null) {
            for (int i = iftable.length - 2; i >= 0; i -= 2) {
                Method result = ((Class) iftable[i]).getPublicMethodRecursive(name2, parameterTypes);
                if (result != null && Modifier.isPublic(result.getAccessFlags())) {
                    return result;
                }
            }
        }
        return null;
    }

    @CallerSensitive
    public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return getConstructor0(parameterTypes, 1);
    }

    public InputStream getResourceAsStream(String name2) {
        String name3 = resolveName(name2);
        ClassLoader cl = getClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemResourceAsStream(name3);
        }
        return cl.getResourceAsStream(name3);
    }

    public URL getResource(String name2) {
        String name3 = resolveName(name2);
        ClassLoader cl = getClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemResource(name3);
        }
        return cl.getResource(name3);
    }

    public ProtectionDomain getProtectionDomain() {
        return null;
    }

    private String resolveName(String name2) {
        if (name2 == null) {
            return name2;
        }
        if (!name2.startsWith("/")) {
            Class cls = this;
            while (cls.isArray()) {
                cls = cls.getComponentType();
            }
            if (cls.getName().lastIndexOf(46) != -1) {
                name2 = baseName.substring(0, index).replace('.', '/') + "/" + name2;
            }
        } else {
            name2 = name2.substring(1);
        }
        return name2;
    }

    private Constructor<T> getConstructor0(Class<?>[] parameterTypes, int which) throws NoSuchMethodException {
        if (parameterTypes == null) {
            parameterTypes = EmptyArray.CLASS;
        }
        int length = parameterTypes.length;
        int i = 0;
        while (i < length) {
            if (parameterTypes[i] != null) {
                i++;
            } else {
                throw new NoSuchMethodException("parameter type is null");
            }
        }
        Constructor<T> result = getDeclaredConstructorInternal(parameterTypes);
        if (result != null && (which != 0 || Modifier.isPublic(result.getAccessFlags()))) {
            return result;
        }
        throw new NoSuchMethodException("<init> " + Arrays.toString((Object[]) parameterTypes));
    }

    public boolean desiredAssertionStatus() {
        return false;
    }

    public boolean isEnum() {
        return (getModifiers() & 16384) != 0 && getSuperclass() == Enum.class;
    }

    public T[] getEnumConstants() {
        T[] values = getEnumConstantsShared();
        if (values != null) {
            return (Object[]) values.clone();
        }
        return null;
    }

    public T[] getEnumConstantsShared() {
        if (!isEnum()) {
            return null;
        }
        return (Object[]) Enum.getSharedConstants(this);
    }

    public T cast(Object obj) {
        if (obj == null || isInstance(obj)) {
            return obj;
        }
        throw new ClassCastException(cannotCastMsg(obj));
    }

    private String cannotCastMsg(Object obj) {
        return "Cannot cast " + obj.getClass().getName() + " to " + getName();
    }

    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        if (clazz.isAssignableFrom(this)) {
            return this;
        }
        throw new ClassCastException(toString() + " cannot be cast to " + clazz.getName());
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);
        A annotation = getDeclaredAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        if (annotationClass.isDeclaredAnnotationPresent(Inherited.class)) {
            for (Class<?> sup = getSuperclass(); sup != null; sup = sup.getSuperclass()) {
                A annotation2 = sup.getDeclaredAnnotation(annotationClass);
                if (annotation2 != null) {
                    return annotation2;
                }
            }
        }
        return null;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        if (annotationClass == null) {
            throw new NullPointerException("annotationClass == null");
        } else if (isDeclaredAnnotationPresent(annotationClass)) {
            return true;
        } else {
            if (annotationClass.isDeclaredAnnotationPresent(Inherited.class)) {
                for (Class<?> sup = getSuperclass(); sup != null; sup = sup.getSuperclass()) {
                    if (sup.isDeclaredAnnotationPresent(annotationClass)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        A[] annotations = super.getAnnotationsByType(annotationClass);
        if (annotations.length != 0) {
            return annotations;
        }
        if (annotationClass.isDeclaredAnnotationPresent(Inherited.class)) {
            Class<?> superClass2 = getSuperclass();
            if (superClass2 != null) {
                return superClass2.getAnnotationsByType(annotationClass);
            }
        }
        return (Annotation[]) Array.newInstance((Class<?>) annotationClass, 0);
    }

    public Annotation[] getAnnotations() {
        HashMap<Class<?>, Annotation> map = new HashMap<>();
        for (Annotation declaredAnnotation : getDeclaredAnnotations()) {
            map.put(declaredAnnotation.annotationType(), declaredAnnotation);
        }
        for (Class<?> sup = getSuperclass(); sup != null; sup = sup.getSuperclass()) {
            for (Annotation declaredAnnotation2 : sup.getDeclaredAnnotations()) {
                Class<? extends Annotation> clazz = declaredAnnotation2.annotationType();
                if (!map.containsKey(clazz) && clazz.isDeclaredAnnotationPresent(Inherited.class)) {
                    map.put(clazz, declaredAnnotation2);
                }
            }
        }
        Collection<Annotation> coll = map.values();
        return (Annotation[]) coll.toArray(new Annotation[coll.size()]);
    }

    private String getSignatureAttribute() {
        String[] annotation = getSignatureAnnotation();
        if (annotation == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String s : annotation) {
            result.append(s);
        }
        return result.toString();
    }

    public boolean isProxy() {
        return (this.accessFlags & NumericShaper.MONGOLIAN) != 0;
    }

    public int getAccessFlags() {
        return this.accessFlags;
    }
}
