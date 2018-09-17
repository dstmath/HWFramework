package java.lang.reflect;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import libcore.util.EmptyArray;
import sun.reflect.CallerSensitive;

public class Proxy implements Serializable {
    private static final Comparator<Method> ORDER_BY_SIGNATURE_AND_SUBTYPE = new Comparator<Method>() {
        public int compare(Method a, Method b) {
            int comparison = Method.ORDER_BY_SIGNATURE.compare(a, b);
            if (comparison != 0) {
                return comparison;
            }
            Class<?> aClass = a.getDeclaringClass();
            Class<?> bClass = b.getDeclaringClass();
            if (aClass == bClass) {
                return 0;
            }
            if (aClass.isAssignableFrom(bClass)) {
                return 1;
            }
            if (bClass.isAssignableFrom(aClass)) {
                return -1;
            }
            return 0;
        }
    };
    private static final Class<?>[] constructorParams = new Class[]{InvocationHandler.class};
    private static final Object key0 = new Object();
    private static final WeakCache<ClassLoader, Class<?>[], Class<?>> proxyClassCache = new WeakCache(new KeyFactory(), new ProxyClassFactory());
    private static final String proxyClassNamePrefix = "$Proxy";
    private static final long serialVersionUID = -2222568056686623797L;
    protected InvocationHandler h;

    private static final class Key1 extends WeakReference<Class<?>> {
        private final int hash;

        Key1(Class<?> intf) {
            super(intf);
            this.hash = intf.hashCode();
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj != null && obj.getClass() == Key1.class) {
                Class<?> intf = (Class) get();
                if (intf != null) {
                    return intf == ((Key1) obj).get();
                }
            }
            return false;
        }
    }

    private static final class Key2 extends WeakReference<Class<?>> {
        private final int hash;
        private final WeakReference<Class<?>> ref2;

        Key2(Class<?> intf1, Class<?> intf2) {
            super(intf1);
            this.hash = (intf1.hashCode() * 31) + intf2.hashCode();
            this.ref2 = new WeakReference(intf2);
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj != null && obj.getClass() == Key2.class) {
                Class<?> intf1 = (Class) get();
                if (intf1 != null && intf1 == ((Key2) obj).get()) {
                    Class<?> intf2 = (Class) this.ref2.get();
                    if (intf2 != null) {
                        return intf2 == ((Key2) obj).ref2.get();
                    }
                }
            }
            return false;
        }
    }

    private static final class KeyFactory implements BiFunction<ClassLoader, Class<?>[], Object> {
        /* synthetic */ KeyFactory(KeyFactory -this0) {
            this();
        }

        private KeyFactory() {
        }

        public Object apply(ClassLoader classLoader, Class<?>[] interfaces) {
            switch (interfaces.length) {
                case 0:
                    return Proxy.key0;
                case 1:
                    return new Key1(interfaces[0]);
                case 2:
                    return new Key2(interfaces[0], interfaces[1]);
                default:
                    return new KeyX(interfaces);
            }
        }
    }

    private static final class KeyX {
        private final int hash;
        private final WeakReference<Class<?>>[] refs;

        KeyX(Class<?>[] interfaces) {
            this.hash = Arrays.hashCode((Object[]) interfaces);
            this.refs = new WeakReference[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                this.refs[i] = new WeakReference(interfaces[i]);
            }
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || obj.getClass() != KeyX.class) {
                return false;
            }
            return equals(this.refs, ((KeyX) obj).refs);
        }

        private static boolean equals(WeakReference<Class<?>>[] refs1, WeakReference<Class<?>>[] refs2) {
            if (refs1.length != refs2.length) {
                return false;
            }
            int i = 0;
            while (i < refs1.length) {
                Class<?> intf = (Class) refs1[i].get();
                if (intf == null || intf != refs2[i].get()) {
                    return false;
                }
                i++;
            }
            return true;
        }
    }

    private static final class ProxyClassFactory implements BiFunction<ClassLoader, Class<?>[], Class<?>> {
        private static final AtomicLong nextUniqueNumber = new AtomicLong();
        private static final String proxyClassNamePrefix = "$Proxy";

        /* synthetic */ ProxyClassFactory(ProxyClassFactory -this0) {
            this();
        }

        private ProxyClassFactory() {
        }

        public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {
            Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap(interfaces.length);
            int i = 0;
            int length = interfaces.length;
            while (i < length) {
                Object intf = interfaces[i];
                Class<?> interfaceClass = null;
                try {
                    interfaceClass = Class.forName(intf.getName(), false, loader);
                } catch (ClassNotFoundException e) {
                }
                if (interfaceClass != intf) {
                    throw new IllegalArgumentException(intf + " is not visible from class loader");
                } else if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
                } else if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                    throw new IllegalArgumentException("repeated interface: " + interfaceClass.getName());
                } else {
                    i++;
                }
            }
            String proxyPkg = null;
            for (Class<?> intf2 : interfaces) {
                if (!Modifier.isPublic(intf2.getModifiers())) {
                    String name = intf2.getName();
                    int n = name.lastIndexOf(46);
                    String pkg = n == -1 ? "" : name.substring(0, n + 1);
                    if (proxyPkg == null) {
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        throw new IllegalArgumentException("non-public interfaces from different packages");
                    }
                }
            }
            if (proxyPkg == null) {
                proxyPkg = "";
            }
            List<Method> methods = Proxy.getMethods(interfaces);
            Collections.sort(methods, Proxy.ORDER_BY_SIGNATURE_AND_SUBTYPE);
            Proxy.validateReturnTypes(methods);
            List<Class<?>[]> exceptions = Proxy.deduplicateAndGetExceptions(methods);
            return Proxy.generateProxy(proxyPkg + proxyClassNamePrefix + nextUniqueNumber.getAndIncrement(), interfaces, loader, (Method[]) methods.toArray(new Method[methods.size()]), (Class[][]) exceptions.toArray(new Class[exceptions.size()][]));
        }
    }

    private static native Class<?> generateProxy(String str, Class<?>[] clsArr, ClassLoader classLoader, Method[] methodArr, Class<?>[][] clsArr2);

    private Proxy() {
    }

    protected Proxy(InvocationHandler h) {
        Objects.requireNonNull(h);
        this.h = h;
    }

    @CallerSensitive
    public static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces) throws IllegalArgumentException {
        return getProxyClass0(loader, interfaces);
    }

    private static Class<?> getProxyClass0(ClassLoader loader, Class<?>... interfaces) {
        if (interfaces.length <= 65535) {
            return (Class) proxyClassCache.get(loader, interfaces);
        }
        throw new IllegalArgumentException("interface limit exceeded");
    }

    private static List<Class<?>[]> deduplicateAndGetExceptions(List<Method> methods) {
        List<Class<?>[]> exceptions = new ArrayList(methods.size());
        int i = 0;
        while (i < methods.size()) {
            Method method = (Method) methods.get(i);
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            if (i <= 0 || Method.ORDER_BY_SIGNATURE.compare(method, (Method) methods.get(i - 1)) != 0) {
                exceptions.add(exceptionTypes);
                i++;
            } else {
                exceptions.set(i - 1, intersectExceptions((Class[]) exceptions.get(i - 1), exceptionTypes));
                methods.remove(i);
            }
        }
        return exceptions;
    }

    private static Class<?>[] intersectExceptions(Class<?>[] aExceptions, Class<?>[] bExceptions) {
        if (aExceptions.length == 0 || bExceptions.length == 0) {
            return EmptyArray.CLASS;
        }
        if (Arrays.equals((Object[]) aExceptions, (Object[]) bExceptions)) {
            return aExceptions;
        }
        Set<Class<?>> intersection = new HashSet();
        for (Class<?> a : aExceptions) {
            for (Class<?> b : bExceptions) {
                if (a.isAssignableFrom(b)) {
                    intersection.add(b);
                } else if (b.isAssignableFrom(a)) {
                    intersection.add(a);
                }
            }
        }
        return (Class[]) intersection.toArray(new Class[intersection.size()]);
    }

    private static void validateReturnTypes(List<Method> methods) {
        Method vs = null;
        for (Object method : methods) {
            if (vs == null || (vs.equalNameAndParameters(method) ^ 1) != 0) {
                vs = method;
            } else {
                Class<?> returnType = method.getReturnType();
                Class<?> vsReturnType = vs.getReturnType();
                if (!returnType.isInterface() || !vsReturnType.isInterface()) {
                    if (vsReturnType.isAssignableFrom(returnType)) {
                        vs = method;
                    } else if (!returnType.isAssignableFrom(vsReturnType)) {
                        throw new IllegalArgumentException("proxied interface methods have incompatible return types:\n  " + vs + "\n  " + method);
                    }
                }
            }
        }
    }

    private static List<Method> getMethods(Class<?>[] interfaces) {
        List<Method> result = new ArrayList();
        try {
            result.add(Object.class.getMethod("equals", Object.class));
            result.add(Object.class.getMethod("hashCode", EmptyArray.CLASS));
            result.add(Object.class.getMethod("toString", EmptyArray.CLASS));
            getMethodsRecursive(interfaces, result);
            return result;
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        }
    }

    private static void getMethodsRecursive(Class<?>[] interfaces, List<Method> methods) {
        for (Class<?> i : interfaces) {
            getMethodsRecursive(i.getInterfaces(), methods);
            Collections.addAll(methods, i.getDeclaredMethods());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x004e A:{Splitter: B:1:0x000d, ExcHandler: java.lang.IllegalAccessException (r3_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:17:0x004e, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:19:0x0058, code:
            throw new java.lang.InternalError(r3.toString(), r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @CallerSensitive
    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException {
        Objects.requireNonNull(h);
        Class<?> cl = getProxyClass0(loader, (Class[]) interfaces.clone());
        try {
            Constructor<?> cons = cl.getConstructor(constructorParams);
            InvocationHandler ih = h;
            if (!Modifier.isPublic(cl.getModifiers())) {
                cons.setAccessible(true);
            }
            return cons.newInstance(h);
        } catch (ReflectiveOperationException e) {
        } catch (InvocationTargetException e2) {
            Throwable t = e2.getCause();
            if (t instanceof RuntimeException) {
                throw ((RuntimeException) t);
            }
            throw new InternalError(t.toString(), t);
        } catch (NoSuchMethodException e3) {
            throw new InternalError(e3.toString(), e3);
        }
    }

    public static boolean isProxyClass(Class<?> cl) {
        return Proxy.class.isAssignableFrom(cl) ? proxyClassCache.containsValue(cl) : false;
    }

    @CallerSensitive
    public static InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        if (isProxyClass(proxy.getClass())) {
            return ((Proxy) proxy).h;
        }
        throw new IllegalArgumentException("not a proxy instance");
    }

    private static Object invoke(Proxy proxy, Method method, Object[] args) throws Throwable {
        return proxy.h.invoke(proxy, method, args);
    }

    private static void reserved1() {
    }

    private static void reserved2() {
    }
}
