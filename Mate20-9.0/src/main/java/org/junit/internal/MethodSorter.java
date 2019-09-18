package org.junit.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.FixMethodOrder;

public class MethodSorter {
    public static final Comparator<Method> DEFAULT = new Comparator<Method>() {
        public int compare(Method m1, Method m2) {
            int i1 = m1.getName().hashCode();
            int i2 = m2.getName().hashCode();
            if (i1 == i2) {
                return MethodSorter.NAME_ASCENDING.compare(m1, m2);
            }
            return i1 < i2 ? -1 : 1;
        }
    };
    public static final Comparator<Method> NAME_ASCENDING = new Comparator<Method>() {
        public int compare(Method m1, Method m2) {
            int comparison = m1.getName().compareTo(m2.getName());
            if (comparison != 0) {
                return comparison;
            }
            return m1.toString().compareTo(m2.toString());
        }
    };

    public static Method[] getDeclaredMethods(Class<?> clazz) {
        Comparator<Method> comparator = getSorter((FixMethodOrder) clazz.getAnnotation(FixMethodOrder.class));
        Method[] methods = clazz.getDeclaredMethods();
        if (comparator != null) {
            Arrays.sort(methods, comparator);
        }
        return methods;
    }

    private MethodSorter() {
    }

    private static Comparator<Method> getSorter(FixMethodOrder fixMethodOrder) {
        if (fixMethodOrder == null) {
            return DEFAULT;
        }
        return fixMethodOrder.value().getComparator();
    }
}
