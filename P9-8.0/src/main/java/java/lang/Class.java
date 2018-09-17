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
        private static final BasicLruCache<Class, Type[]> genericInterfaces = new BasicLruCache(8);

        private Caches() {
        }
    }

    static native Class<?> classForName(String str, boolean z, ClassLoader classLoader) throws ClassNotFoundException;

    private native Constructor<T> getDeclaredConstructorInternal(Class<?>[] clsArr);

    private native Constructor<?>[] getDeclaredConstructorsInternal(boolean z);

    private native Method getDeclaredMethodInternal(String str, Class<?>[] clsArr);

    private native Constructor<?> getEnclosingConstructorNative();

    private native Method getEnclosingMethodNative();

    private native int getInnerClassFlags(int i);

    private native String getInnerClassName();

    private native Class<?>[] getInterfacesInternal();

    private native String getNameNative();

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
        StringBuilder stringBuilder = new StringBuilder();
        String str = isInterface() ? "interface " : isPrimitive() ? "" : "class ";
        return stringBuilder.append(str).append(getName()).toString();
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
    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        if (loader == null) {
            loader = BootClassLoader.getInstance();
        }
        try {
            return classForName(name, initialize, loader);
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
        boolean z = false;
        if (this == cls) {
            return true;
        }
        if (this == Object.class) {
            return cls.isPrimitive() ^ 1;
        }
        if (isArray()) {
            if (cls.isArray()) {
                z = this.componentType.isAssignableFrom(cls.componentType);
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
            if (!cls.isInterface()) {
                for (cls = cls.superClass; cls != null; cls = cls.superClass) {
                    if (cls == this) {
                        return true;
                    }
                }
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
        String name = this.name;
        if (name != null) {
            return name;
        }
        name = getNameNative();
        this.name = name;
        return name;
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
        Package packageR = null;
        ClassLoader loader = getClassLoader();
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
        String name = getName();
        int last = name.lastIndexOf(46);
        return last == -1 ? null : name.substring(0, last);
    }

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
        return result.length == 0 ? result : (Type[]) result.clone();
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
        return componentModifiers | 1040;
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
        return getName().contains("$") ^ 1;
    }

    public String getSimpleName() {
        if (isArray()) {
            return getComponentType().getSimpleName() + "[]";
        }
        if (isAnonymousClass()) {
            return "";
        }
        if (isMemberClass() || isLocalClass()) {
            return getInnerClassName();
        }
        String simpleName = getName();
        if (simpleName.lastIndexOf(".") > 0) {
            return simpleName.substring(simpleName.lastIndexOf(".") + 1);
        }
        return simpleName;
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
            if (canonicalName != null) {
                return canonicalName + "[]";
            }
            return null;
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
        if (getEnclosingMethod() == null && getEnclosingConstructor() == null) {
            return false;
        }
        return isAnonymousClass() ^ 1;
    }

    public boolean isMemberClass() {
        return getDeclaringClass() != null;
    }

    private boolean isLocalOrAnonymousClass() {
        return !isLocalClass() ? isAnonymousClass() : true;
    }

    @CallerSensitive
    public Class<?>[] getClasses() {
        List<Class<?>> result = new ArrayList();
        for (Class<?> c = this; c != null; c = c.superClass) {
            for (Class<?> member : c.getDeclaredClasses()) {
                if (Modifier.isPublic(member.getModifiers())) {
                    result.add(member);
                }
            }
        }
        return (Class[]) result.toArray(new Class[result.size()]);
    }

    @CallerSensitive
    public Field[] getFields() throws SecurityException {
        List<Field> fields = new ArrayList();
        getPublicFieldsRecursive(fields);
        return (Field[]) fields.toArray(new Field[fields.size()]);
    }

    private void getPublicFieldsRecursive(List<Field> result) {
        for (Class<?> c = this; c != null; c = c.superClass) {
            Collections.addAll(result, c.getPublicDeclaredFields());
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
        List<Method> methods = new ArrayList();
        getPublicMethodsInternal(methods);
        CollectionUtils.removeDuplicates(methods, Method.ORDER_BY_SIGNATURE);
        return (Method[]) methods.toArray(new Method[methods.size()]);
    }

    private void getPublicMethodsInternal(List<Method> result) {
        Collections.addAll(result, getDeclaredMethodsUnchecked(true));
        if (!isInterface()) {
            for (Class<?> c = this.superClass; c != null; c = c.superClass) {
                Collections.addAll(result, c.getDeclaredMethodsUnchecked(true));
            }
        }
        Object[] iftable = this.ifTable;
        if (iftable != null) {
            for (int i = 0; i < iftable.length; i += 2) {
                Collections.addAll(result, iftable[i].getDeclaredMethodsUnchecked(true));
            }
        }
    }

    @CallerSensitive
    public Constructor<?>[] getConstructors() throws SecurityException {
        return getDeclaredConstructorsInternal(true);
    }

    public Field getField(String name) throws NoSuchFieldException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        Field result = getPublicFieldRecursive(name);
        if (result != null) {
            return result;
        }
        throw new NoSuchFieldException(name);
    }

    @CallerSensitive
    public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return getMethod(name, parameterTypes, true);
    }

    public Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return getConstructor0(parameterTypes, 0);
    }

    public Method[] getDeclaredMethods() throws SecurityException {
        int i = 0;
        Method[] result = getDeclaredMethodsUnchecked(false);
        int length = result.length;
        while (i < length) {
            Method m = result[i];
            m.getReturnType();
            m.getParameterTypes();
            i++;
        }
        return result;
    }

    public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
        return getDeclaredConstructorsInternal(false);
    }

    @CallerSensitive
    public Method getDeclaredMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return getMethod(name, parameterTypes, false);
    }

    private Method getMethod(String name, Class<?>[] parameterTypes, boolean recursivePublicMethods) throws NoSuchMethodException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        Object[] parameterTypes2;
        Method result;
        if (parameterTypes2 == null) {
            parameterTypes2 = EmptyArray.CLASS;
        }
        for (Class<?> c : parameterTypes2) {
            if (c == null) {
                throw new NoSuchMethodException("parameter type is null");
            }
        }
        if (recursivePublicMethods) {
            result = getPublicMethodRecursive(name, parameterTypes2);
        } else {
            result = getDeclaredMethodInternal(name, parameterTypes2);
        }
        if (result != null && (!recursivePublicMethods || (Modifier.isPublic(result.getAccessFlags()) ^ 1) == 0)) {
            return result;
        }
        throw new NoSuchMethodException(name + " " + Arrays.toString(parameterTypes2));
    }

    private Method getPublicMethodRecursive(String name, Class<?>[] parameterTypes) {
        for (Class<?> c = this; c != null; c = c.getSuperclass()) {
            Method result = c.getDeclaredMethodInternal(name, parameterTypes);
            if (result != null && Modifier.isPublic(result.getAccessFlags())) {
                return result;
            }
        }
        return findInterfaceMethod(name, parameterTypes);
    }

    public Method getInstanceMethod(String name, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        for (Class<?> c = this; c != null; c = c.getSuperclass()) {
            Method result = c.getDeclaredMethodInternal(name, parameterTypes);
            if (result != null && (Modifier.isStatic(result.getModifiers()) ^ 1) != 0) {
                return result;
            }
        }
        return findInterfaceMethod(name, parameterTypes);
    }

    private Method findInterfaceMethod(String name, Class<?>[] parameterTypes) {
        Object[] iftable = this.ifTable;
        if (iftable != null) {
            for (int i = iftable.length - 2; i >= 0; i -= 2) {
                Method result = iftable[i].getPublicMethodRecursive(name, parameterTypes);
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

    public InputStream getResourceAsStream(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemResourceAsStream(name);
        }
        return cl.getResourceAsStream(name);
    }

    public URL getResource(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemResource(name);
        }
        return cl.getResource(name);
    }

    public ProtectionDomain getProtectionDomain() {
        return null;
    }

    private String resolveName(String name) {
        if (name == null) {
            return name;
        }
        if (name.startsWith("/")) {
            name = name.substring(1);
        } else {
            Class<?> c = this;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            String baseName = c.getName();
            int index = baseName.lastIndexOf(46);
            if (index != -1) {
                name = baseName.substring(0, index).replace('.', '/') + "/" + name;
            }
        }
        return name;
    }

    private Constructor<T> getConstructor0(Class<?>[] parameterTypes, int which) throws NoSuchMethodException {
        Object[] parameterTypes2;
        if (parameterTypes2 == null) {
            parameterTypes2 = EmptyArray.CLASS;
        }
        for (Class<?> c : parameterTypes2) {
            if (c == null) {
                throw new NoSuchMethodException("parameter type is null");
            }
        }
        Constructor<T> result = getDeclaredConstructorInternal(parameterTypes2);
        if (result != null && (which != 0 || (Modifier.isPublic(result.getAccessFlags()) ^ 1) == 0)) {
            return result;
        }
        throw new NoSuchMethodException("<init> " + Arrays.toString(parameterTypes2));
    }

    public boolean desiredAssertionStatus() {
        return false;
    }

    public boolean isEnum() {
        if ((getModifiers() & 16384) == 0 || getSuperclass() != Enum.class) {
            return false;
        }
        return true;
    }

    public T[] getEnumConstants() {
        T[] values = getEnumConstantsShared();
        if (values != null) {
            return (Object[]) values.clone();
        }
        return null;
    }

    T[] getEnumConstantsShared() {
        if (isEnum()) {
            return Enum.getSharedConstants(this);
        }
        return null;
    }

    public T cast(Object obj) {
        if (obj == null || (isInstance(obj) ^ 1) == 0) {
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
                annotation = sup.getDeclaredAnnotation(annotationClass);
                if (annotation != null) {
                    return annotation;
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
            Class<?> superClass = getSuperclass();
            if (superClass != null) {
                return superClass.getAnnotationsByType(annotationClass);
            }
        }
        return (Annotation[]) Array.newInstance((Class) annotationClass, 0);
    }

    public Annotation[] getAnnotations() {
        HashMap<Class<?>, Annotation> map = new HashMap();
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
