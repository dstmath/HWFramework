package java.lang;

import com.android.dex.Dex;
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
import libcore.reflect.GenericSignatureParser;
import libcore.reflect.InternalNames;
import libcore.reflect.Types;
import libcore.util.BasicLruCache;
import libcore.util.CollectionUtils;
import libcore.util.EmptyArray;
import sun.reflect.CallerSensitive;
import sun.reflect.annotation.AnnotationType;

public final class Class<T> implements Serializable, GenericDeclaration, Type, AnnotatedElement {
    private static final int ANNOTATION = 8192;
    private static final int ENUM = 16384;
    private static final int FINALIZABLE = Integer.MIN_VALUE;
    private static final int SYNTHETIC = 4096;
    private static final long serialVersionUID = 3206093459760846163L;
    private transient int accessFlags;
    private AnnotationType annotationType;
    private transient int classFlags;
    private transient ClassLoader classLoader;
    private transient int classSize;
    private transient int clinitThreadId;
    private transient Class<?> componentType;
    private transient short copiedMethodsOffset;
    private transient DexCache dexCache;
    private transient long dexCacheStrings;
    private transient int dexClassDefIndex;
    private volatile transient int dexTypeIndex;
    private transient long iFields;
    private transient Object[] ifTable;
    private transient long methods;
    private transient String name;
    private transient int numReferenceInstanceFields;
    private transient int numReferenceStaticFields;
    private transient int objectSize;
    private transient int primitiveType;
    private transient int referenceInstanceOffsets;
    private transient long sFields;
    private transient int status;
    private transient Class<? super T> superClass;
    private transient Object verifyError;
    private transient short virtualMethodsOffset;
    private transient Object vtable;

    private static class Caches {
        private static final BasicLruCache<Class, Type[]> genericInterfaces = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Class.Caches.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Class.Caches.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Class.Caches.<clinit>():void");
        }

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

    private native String getNameNative();

    private native Class<?>[] getProxyInterfaces();

    private native Field[] getPublicDeclaredFields();

    private native Field getPublicFieldRecursive(String str);

    private native String[] getSignatureAnnotation();

    private native boolean isDeclaredAnnotationPresent(Class<? extends Annotation> cls);

    public native <T extends Annotation> T getDeclaredAnnotation(Class<T> cls);

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

    public boolean isInstance(Object object) {
        if (object == null) {
            return false;
        }
        return isAssignableFrom(object.getClass());
    }

    public boolean isAssignableFrom(Class<?> c) {
        boolean z = false;
        if (this == c) {
            return true;
        }
        if (this == Object.class) {
            if (!c.isPrimitive()) {
                z = true;
            }
            return z;
        } else if (isArray()) {
            if (c.isArray()) {
                z = this.componentType.isAssignableFrom(c.componentType);
            }
            return z;
        } else if (isInterface()) {
            Object[] iftable = c.ifTable;
            if (iftable != null) {
                for (int i = 0; i < iftable.length; i += 2) {
                    if (iftable[i] == this) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            if (!c.isInterface()) {
                for (c = c.superClass; c != null; c = c.superClass) {
                    if (c == this) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean isInterface() {
        return (this.accessFlags & Modifier.INTERFACE) != 0;
    }

    public boolean isArray() {
        return getComponentType() != null;
    }

    public boolean isPrimitive() {
        return (this.primitiveType & 65535) != 0;
    }

    public boolean isFinalizable() {
        return (getModifiers() & FINALIZABLE) != 0;
    }

    public boolean isAnnotation() {
        return (getModifiers() & ANNOTATION) != 0;
    }

    public boolean isSynthetic() {
        return (getModifiers() & SYNTHETIC) != 0;
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
        } else if (isProxy()) {
            return getProxyInterfaces();
        } else {
            Dex dex = getDex();
            if (dex == null) {
                return EmptyArray.CLASS;
            }
            short[] interfaces = dex.interfaceTypeIndicesFromClassDefIndex(this.dexClassDefIndex);
            Class<?>[] result = new Class[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                result[i] = getDexCacheType(dex, interfaces[i]);
            }
            return result;
        }
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
        if ((componentModifiers & Modifier.INTERFACE) != 0) {
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
        return !getName().contains("$");
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
        if ((getEnclosingMethod() == null && getEnclosingConstructor() == null) || isAnonymousClass()) {
            return false;
        }
        return true;
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
        Method result;
        if (parameterTypes == null) {
            Object[] parameterTypes2 = EmptyArray.CLASS;
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
        if (result != null && (!recursivePublicMethods || Modifier.isPublic(result.getAccessFlags()))) {
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
        Object[] iftable = this.ifTable;
        if (iftable != null) {
            for (int i = iftable.length - 2; i >= 0; i -= 2) {
                result = iftable[i].getPublicMethodRecursive(name, parameterTypes);
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
        if (parameterTypes == null) {
            parameterTypes2 = EmptyArray.CLASS;
        }
        for (Class<?> c : parameterTypes2) {
            if (c == null) {
                throw new NoSuchMethodException("parameter type is null");
            }
        }
        Constructor<T> result = getDeclaredConstructorInternal(parameterTypes2);
        if (result != null && (which != 0 || Modifier.isPublic(result.getAccessFlags()))) {
            return result;
        }
        throw new NoSuchMethodException("<init> " + Arrays.toString(parameterTypes2));
    }

    public boolean desiredAssertionStatus() {
        return false;
    }

    public boolean isEnum() {
        if ((getModifiers() & ENUM) == 0 || getSuperclass() != Enum.class) {
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
        if (annotationClass == null) {
            throw new NullPointerException();
        }
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

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            throw new NullPointerException("annotationType == null");
        } else if (isDeclaredAnnotationPresent(annotationType)) {
            return true;
        } else {
            if (annotationType.isDeclaredAnnotationPresent(Inherited.class)) {
                for (Class<?> sup = getSuperclass(); sup != null; sup = sup.getSuperclass()) {
                    if (sup.isDeclaredAnnotationPresent(annotationType)) {
                        return true;
                    }
                }
            }
            return false;
        }
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

    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        T[] annotations = super.getAnnotationsByType(annotationClass);
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

    public void setAnnotationType(AnnotationType type) {
        this.annotationType = type;
    }

    public AnnotationType getAnnotationType() {
        return this.annotationType;
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

    public Dex getDex() {
        if (this.dexCache == null) {
            return null;
        }
        return this.dexCache.getDex();
    }

    public String getDexCacheString(Dex dex, int dexStringIndex) {
        String s = this.dexCache.getResolvedString(dexStringIndex);
        if (s != null) {
            return s;
        }
        s = ((String) dex.strings().get(dexStringIndex)).intern();
        this.dexCache.setResolvedString(dexStringIndex, s);
        return s;
    }

    public Class<?> getDexCacheType(Dex dex, int dexTypeIndex) {
        Class<?> resolvedType = this.dexCache.getResolvedType(dexTypeIndex);
        if (resolvedType != null) {
            return resolvedType;
        }
        resolvedType = InternalNames.getClass(getClassLoader(), getDexCacheString(dex, ((Integer) dex.typeIds().get(dexTypeIndex)).intValue()));
        this.dexCache.setResolvedType(dexTypeIndex, resolvedType);
        return resolvedType;
    }

    public int getDexAnnotationDirectoryOffset() {
        Dex dex = getDex();
        if (dex == null) {
            return 0;
        }
        int classDefIndex = getDexClassDefIndex();
        if (classDefIndex < 0) {
            return 0;
        }
        return dex.annotationDirectoryOffsetFromClassDefIndex(classDefIndex);
    }

    public int getDexTypeIndex() {
        int typeIndex = this.dexTypeIndex;
        if (typeIndex != 65535) {
            return typeIndex;
        }
        synchronized (this) {
            typeIndex = this.dexTypeIndex;
            if (typeIndex == 65535) {
                if (this.dexClassDefIndex >= 0) {
                    typeIndex = getDex().typeIndexFromClassDefIndex(this.dexClassDefIndex);
                } else {
                    typeIndex = getDex().findTypeIndex(InternalNames.getInternalName(this));
                    if (typeIndex < 0) {
                        typeIndex = -1;
                    }
                }
                this.dexTypeIndex = typeIndex;
            }
        }
        return typeIndex;
    }

    private boolean canAccess(Class<?> c) {
        if (Modifier.isPublic(c.accessFlags)) {
            return true;
        }
        return inSamePackage(c);
    }

    private boolean canAccessMember(Class<?> memberClass, int memberModifiers) {
        if (memberClass == this || Modifier.isPublic(memberModifiers)) {
            return true;
        }
        if (Modifier.isPrivate(memberModifiers)) {
            return false;
        }
        if (Modifier.isProtected(memberModifiers)) {
            for (Class<?> parent = this.superClass; parent != null; parent = parent.superClass) {
                if (parent == memberClass) {
                    return true;
                }
            }
        }
        return inSamePackage(memberClass);
    }

    private boolean inSamePackage(Class<?> c) {
        boolean z = false;
        if (this.classLoader != c.classLoader) {
            return false;
        }
        String packageName1 = getPackageName$();
        String packageName2 = c.getPackageName$();
        if (packageName1 == null) {
            if (packageName2 == null) {
                z = true;
            }
            return z;
        } else if (packageName2 == null) {
            return false;
        } else {
            return packageName1.equals(packageName2);
        }
    }

    public int getAccessFlags() {
        return this.accessFlags;
    }

    public int getDexClassDefIndex() {
        return this.dexClassDefIndex == 65535 ? -1 : this.dexClassDefIndex;
    }
}
