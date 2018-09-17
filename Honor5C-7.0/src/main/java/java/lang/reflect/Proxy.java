package java.lang.reflect;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import libcore.util.EmptyArray;
import sun.reflect.CallerSensitive;

public class Proxy implements Serializable {
    private static final Comparator<Method> ORDER_BY_SIGNATURE_AND_SUBTYPE = null;
    private static final Class[] constructorParams = null;
    private static Map<ClassLoader, Map<List<String>, Object>> loaderToCache = null;
    private static long nextUniqueNumber = 0;
    private static Object nextUniqueNumberLock = null;
    private static Object pendingGenerationMarker = null;
    private static final String proxyClassNamePrefix = "$Proxy";
    private static Map<Class<?>, Void> proxyClasses = null;
    private static final long serialVersionUID = -2222568056686623797L;
    protected InvocationHandler h;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.reflect.Proxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.reflect.Proxy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.reflect.Proxy.<clinit>():void");
    }

    private static native Class<?> generateProxy(String str, Class<?>[] clsArr, ClassLoader classLoader, Method[] methodArr, Class<?>[][] clsArr2);

    private Proxy() {
    }

    protected Proxy(InvocationHandler h) {
        this.h = h;
    }

    @CallerSensitive
    public static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces) throws IllegalArgumentException {
        return getProxyClass0(loader, interfaces);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Class<?> getProxyClass0(ClassLoader loader, Class<?>... interfaces) {
        int length = interfaces.length;
        if (r0 > 65535) {
            throw new IllegalArgumentException("interface limit exceeded");
        }
        Class<?> proxyClass = null;
        String[] interfaceNames = new String[interfaces.length];
        Set<Class<?>> interfaceSet = new HashSet();
        int i = 0;
        while (true) {
            length = interfaces.length;
            if (i >= r0) {
                break;
            }
            String interfaceName = interfaces[i].getName();
            Class<?> interfaceClass = null;
            try {
                interfaceClass = Class.forName(interfaceName, false, loader);
            } catch (ClassNotFoundException e) {
            }
            if (interfaceClass != interfaces[i]) {
                break;
            } else if (!interfaceClass.isInterface()) {
                break;
            } else if (interfaceSet.contains(interfaceClass)) {
                break;
            } else {
                interfaceSet.add(interfaceClass);
                interfaceNames[i] = interfaceName;
                i++;
            }
        }
        List<String> key = Arrays.asList(interfaceNames);
        synchronized (loaderToCache) {
            Map<List<String>, Object> cache = (Map) loaderToCache.get(loader);
            if (cache == null) {
                cache = new HashMap();
                loaderToCache.put(loader, cache);
            }
        }
        synchronized (cache) {
            while (true) {
                Object value = cache.get(key);
                if (value instanceof Reference) {
                    proxyClass = (Class) ((Reference) value).get();
                }
                if (proxyClass != null) {
                    return proxyClass;
                } else if (value == pendingGenerationMarker) {
                    try {
                        cache.wait();
                    } catch (InterruptedException e2) {
                    }
                } else {
                    long num;
                    cache.put(key, pendingGenerationMarker);
                    String proxyPkg = null;
                    i = 0;
                    while (true) {
                        length = interfaces.length;
                        if (i >= r0) {
                            break;
                        }
                        if (!Modifier.isPublic(interfaces[i].getModifiers())) {
                            String pkg;
                            String name = interfaces[i].getName();
                            int n = name.lastIndexOf(46);
                            if (n == -1) {
                                pkg = "";
                            } else {
                                try {
                                    pkg = name.substring(0, n + 1);
                                } catch (Throwable th) {
                                    synchronized (cache) {
                                        if (proxyClass == null) {
                                            cache.remove(key);
                                        }
                                        cache.notifyAll();
                                    }
                                    cache.put(key, new WeakReference(proxyClass));
                                    cache.notifyAll();
                                }
                            }
                            if (proxyPkg == null) {
                                proxyPkg = pkg;
                            } else if (!pkg.equals(proxyPkg)) {
                                break;
                            }
                        }
                        i++;
                    }
                    if (proxyPkg == null) {
                        proxyPkg = "";
                    }
                    List<Method> methods = getMethods(interfaces);
                    Collections.sort(methods, ORDER_BY_SIGNATURE_AND_SUBTYPE);
                    validateReturnTypes(methods);
                    List<Class<?>[]> exceptions = deduplicateAndGetExceptions(methods);
                    Method[] methodsArray = (Method[]) methods.toArray(new Method[methods.size()]);
                    Class[][] exceptionsArray = (Class[][]) exceptions.toArray(new Class[exceptions.size()][]);
                    synchronized (nextUniqueNumberLock) {
                        num = nextUniqueNumber;
                        nextUniqueNumber = 1 + num;
                    }
                    proxyClass = generateProxy(proxyPkg + proxyClassNamePrefix + num, interfaces, loader, methodsArray, exceptionsArray);
                    proxyClasses.put(proxyClass, null);
                    synchronized (cache) {
                        if (proxyClass != null) {
                            cache.put(key, new WeakReference(proxyClass));
                        } else {
                            cache.remove(key);
                        }
                        cache.notifyAll();
                    }
                    return proxyClass;
                }
            }
        }
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
            if (vs == null || !vs.equalNameAndParameters(method)) {
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

    @CallerSensitive
    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException {
        if (h == null) {
            throw new NullPointerException();
        }
        try {
            return newInstance(getProxyClass0(loader, interfaces).getConstructor(constructorParams), h);
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        }
    }

    private static Object newInstance(Constructor<?> cons, InvocationHandler h) {
        try {
            return cons.newInstance(h);
        } catch (ReflectiveOperationException e) {
            throw new InternalError(e.toString());
        } catch (InvocationTargetException e2) {
            Throwable t = e2.getCause();
            if (t instanceof RuntimeException) {
                throw ((RuntimeException) t);
            }
            throw new InternalError(t.toString());
        }
    }

    public static boolean isProxyClass(Class<?> cl) {
        if (cl != null) {
            return proxyClasses.containsKey(cl);
        }
        throw new NullPointerException();
    }

    public static InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        if (proxy instanceof Proxy) {
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
