package android.arch.lifecycle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class Lifecycling {
    private static final int GENERATED_CALLBACK = 2;
    private static final int REFLECTIVE_CALLBACK = 1;
    private static Map<Class, Integer> sCallbackCache = new HashMap();
    private static Map<Class, List<Constructor<? extends GeneratedAdapter>>> sClassToAdapters = new HashMap();

    @NonNull
    static GenericLifecycleObserver getCallback(Object object) {
        if (object instanceof FullLifecycleObserver) {
            return new FullLifecycleObserverAdapter((FullLifecycleObserver) object);
        }
        if (object instanceof GenericLifecycleObserver) {
            return (GenericLifecycleObserver) object;
        }
        Class<?> klass = object.getClass();
        if (getObserverConstructorType(klass) != 2) {
            return new ReflectiveGenericLifecycleObserver(object);
        }
        List<Constructor<? extends GeneratedAdapter>> constructors = sClassToAdapters.get(klass);
        if (constructors.size() == 1) {
            return new SingleGeneratedAdapterObserver(createGeneratedAdapter(constructors.get(0), object));
        }
        GeneratedAdapter[] adapters = new GeneratedAdapter[constructors.size()];
        for (int i = 0; i < constructors.size(); i++) {
            adapters[i] = createGeneratedAdapter(constructors.get(i), object);
        }
        return new CompositeGeneratedAdaptersObserver(adapters);
    }

    private static GeneratedAdapter createGeneratedAdapter(Constructor<? extends GeneratedAdapter> constructor, Object object) {
        try {
            return (GeneratedAdapter) constructor.newInstance(new Object[]{object});
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e2) {
            throw new RuntimeException(e2);
        } catch (InvocationTargetException e3) {
            throw new RuntimeException(e3);
        }
    }

    @Nullable
    private static Constructor<? extends GeneratedAdapter> generatedConstructor(Class<?> klass) {
        String str;
        String str2;
        try {
            Package aPackage = klass.getPackage();
            String name = klass.getCanonicalName();
            String fullPackage = aPackage != null ? aPackage.getName() : "";
            if (fullPackage.isEmpty()) {
                str = name;
            } else {
                str = name.substring(fullPackage.length() + 1);
            }
            String adapterName = getAdapterName(str);
            if (fullPackage.isEmpty()) {
                str2 = adapterName;
            } else {
                str2 = fullPackage + "." + adapterName;
            }
            Constructor<?> declaredConstructor = Class.forName(str2).getDeclaredConstructor(new Class[]{klass});
            if (!declaredConstructor.isAccessible()) {
                declaredConstructor.setAccessible(true);
            }
            return declaredConstructor;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodException e2) {
            throw new RuntimeException(e2);
        }
    }

    private static int getObserverConstructorType(Class<?> klass) {
        if (sCallbackCache.containsKey(klass)) {
            return sCallbackCache.get(klass).intValue();
        }
        int type = resolveObserverCallbackType(klass);
        sCallbackCache.put(klass, Integer.valueOf(type));
        return type;
    }

    private static int resolveObserverCallbackType(Class<?> klass) {
        if (klass.getCanonicalName() == null) {
            return 1;
        }
        Constructor<? extends GeneratedAdapter> constructor = generatedConstructor(klass);
        if (constructor != null) {
            sClassToAdapters.put(klass, Collections.singletonList(constructor));
            return 2;
        } else if (ClassesInfoCache.sInstance.hasLifecycleMethods(klass)) {
            return 1;
        } else {
            Class<? super Object> superclass = klass.getSuperclass();
            List<Constructor<? extends GeneratedAdapter>> adapterConstructors = null;
            if (isLifecycleParent(superclass)) {
                if (getObserverConstructorType(superclass) == 1) {
                    return 1;
                }
                adapterConstructors = new ArrayList<>(sClassToAdapters.get(superclass));
            }
            for (Class<?> intrface : klass.getInterfaces()) {
                if (isLifecycleParent(intrface)) {
                    if (getObserverConstructorType(intrface) == 1) {
                        return 1;
                    }
                    if (adapterConstructors == null) {
                        adapterConstructors = new ArrayList<>();
                    }
                    adapterConstructors.addAll(sClassToAdapters.get(intrface));
                }
            }
            if (adapterConstructors == null) {
                return 1;
            }
            sClassToAdapters.put(klass, adapterConstructors);
            return 2;
        }
    }

    private static boolean isLifecycleParent(Class<?> klass) {
        return klass != null && LifecycleObserver.class.isAssignableFrom(klass);
    }

    public static String getAdapterName(String className) {
        return className.replace(".", "_") + "_LifecycleAdapter";
    }

    private Lifecycling() {
    }
}
