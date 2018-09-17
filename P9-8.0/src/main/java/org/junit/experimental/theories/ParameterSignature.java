package org.junit.experimental.theories;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterSignature {
    private static final Map<Class<?>, Class<?>> CONVERTABLE_TYPES_MAP = buildConvertableTypesMap();
    private final Annotation[] annotations;
    private final Class<?> type;

    private static Map<Class<?>, Class<?>> buildConvertableTypesMap() {
        Map<Class<?>, Class<?>> map = new HashMap();
        putSymmetrically(map, Boolean.TYPE, Boolean.class);
        putSymmetrically(map, Byte.TYPE, Byte.class);
        putSymmetrically(map, Short.TYPE, Short.class);
        putSymmetrically(map, Character.TYPE, Character.class);
        putSymmetrically(map, Integer.TYPE, Integer.class);
        putSymmetrically(map, Long.TYPE, Long.class);
        putSymmetrically(map, Float.TYPE, Float.class);
        putSymmetrically(map, Double.TYPE, Double.class);
        return Collections.unmodifiableMap(map);
    }

    private static <T> void putSymmetrically(Map<T, T> map, T a, T b) {
        map.put(a, b);
        map.put(b, a);
    }

    public static ArrayList<ParameterSignature> signatures(Method method) {
        return signatures(method.getParameterTypes(), method.getParameterAnnotations());
    }

    public static List<ParameterSignature> signatures(Constructor<?> constructor) {
        return signatures(constructor.getParameterTypes(), constructor.getParameterAnnotations());
    }

    private static ArrayList<ParameterSignature> signatures(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations) {
        ArrayList<ParameterSignature> sigs = new ArrayList();
        for (int i = 0; i < parameterTypes.length; i++) {
            sigs.add(new ParameterSignature(parameterTypes[i], parameterAnnotations[i]));
        }
        return sigs;
    }

    private ParameterSignature(Class<?> type, Annotation[] annotations) {
        this.type = type;
        this.annotations = annotations;
    }

    public boolean canAcceptValue(Object candidate) {
        return candidate == null ? this.type.isPrimitive() ^ 1 : canAcceptType(candidate.getClass());
    }

    public boolean canAcceptType(Class<?> candidate) {
        if (this.type.isAssignableFrom(candidate)) {
            return true;
        }
        return isAssignableViaTypeConversion(this.type, candidate);
    }

    public boolean canPotentiallyAcceptType(Class<?> candidate) {
        if (candidate.isAssignableFrom(this.type) || isAssignableViaTypeConversion(candidate, this.type)) {
            return true;
        }
        return canAcceptType(candidate);
    }

    private boolean isAssignableViaTypeConversion(Class<?> targetType, Class<?> candidate) {
        if (CONVERTABLE_TYPES_MAP.containsKey(candidate)) {
            return targetType.isAssignableFrom((Class) CONVERTABLE_TYPES_MAP.get(candidate));
        }
        return false;
    }

    public Class<?> getType() {
        return this.type;
    }

    public List<Annotation> getAnnotations() {
        return Arrays.asList(this.annotations);
    }

    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return getAnnotation(type) != null;
    }

    public <T extends Annotation> T findDeepAnnotation(Class<T> annotationType) {
        return findDeepAnnotation(this.annotations, annotationType, 3);
    }

    private <T extends Annotation> T findDeepAnnotation(Annotation[] annotations, Class<T> annotationType, int depth) {
        if (depth == 0) {
            return null;
        }
        for (Annotation each : annotations) {
            if (annotationType.isInstance(each)) {
                return (Annotation) annotationType.cast(each);
            }
            Annotation candidate = findDeepAnnotation(each.annotationType().getAnnotations(), annotationType, depth - 1);
            if (candidate != null) {
                return (Annotation) annotationType.cast(candidate);
            }
        }
        return null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation each : getAnnotations()) {
            if (annotationType.isInstance(each)) {
                return (Annotation) annotationType.cast(each);
            }
        }
        return null;
    }
}
