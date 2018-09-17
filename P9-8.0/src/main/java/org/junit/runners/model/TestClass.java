package org.junit.runners.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.internal.MethodSorter;

public class TestClass implements Annotatable {
    private static final FieldComparator FIELD_COMPARATOR = new FieldComparator();
    private static final MethodComparator METHOD_COMPARATOR = new MethodComparator();
    private final Class<?> clazz;
    private final Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations;
    private final Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations;

    private static class FieldComparator implements Comparator<Field> {
        /* synthetic */ FieldComparator(FieldComparator -this0) {
            this();
        }

        private FieldComparator() {
        }

        public int compare(Field left, Field right) {
            return left.getName().compareTo(right.getName());
        }
    }

    private static class MethodComparator implements Comparator<FrameworkMethod> {
        /* synthetic */ MethodComparator(MethodComparator -this0) {
            this();
        }

        private MethodComparator() {
        }

        public int compare(FrameworkMethod left, FrameworkMethod right) {
            return MethodSorter.NAME_ASCENDING.compare(left.getMethod(), right.getMethod());
        }
    }

    public TestClass(Class<?> clazz) {
        this.clazz = clazz;
        if (clazz == null || clazz.getConstructors().length <= 1) {
            Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations = new LinkedHashMap();
            Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations = new LinkedHashMap();
            scanAnnotatedMembers(methodsForAnnotations, fieldsForAnnotations);
            this.methodsForAnnotations = makeDeeplyUnmodifiable(methodsForAnnotations);
            this.fieldsForAnnotations = makeDeeplyUnmodifiable(fieldsForAnnotations);
            return;
        }
        throw new IllegalArgumentException("Test class can only have one constructor");
    }

    protected void scanAnnotatedMembers(Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations, Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations) {
        for (Class<?> eachClass : getSuperClasses(this.clazz)) {
            for (Method eachMethod : MethodSorter.getDeclaredMethods(eachClass)) {
                addToAnnotationLists(new FrameworkMethod(eachMethod), methodsForAnnotations);
            }
            for (Field eachField : getSortedDeclaredFields(eachClass)) {
                addToAnnotationLists(new FrameworkField(eachField), fieldsForAnnotations);
            }
        }
    }

    private static Field[] getSortedDeclaredFields(Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        Arrays.sort(declaredFields, FIELD_COMPARATOR);
        return declaredFields;
    }

    protected static <T extends FrameworkMember<T>> void addToAnnotationLists(T member, Map<Class<? extends Annotation>, List<T>> map) {
        Annotation[] annotations = member.getAnnotations();
        int length = annotations.length;
        int i = 0;
        while (i < length) {
            Class<? extends Annotation> type = annotations[i].annotationType();
            List members = getAnnotatedMembers(map, type, true);
            if (!member.isShadowedBy(members)) {
                if (runsTopToBottom(type)) {
                    members.add(0, member);
                } else {
                    members.add(member);
                }
                i++;
            } else {
                return;
            }
        }
    }

    private static <T extends FrameworkMember<T>> Map<Class<? extends Annotation>, List<T>> makeDeeplyUnmodifiable(Map<Class<? extends Annotation>, List<T>> source) {
        LinkedHashMap<Class<? extends Annotation>, List<T>> copy = new LinkedHashMap();
        for (Entry<Class<? extends Annotation>, List<T>> entry : source.entrySet()) {
            copy.put((Class) entry.getKey(), Collections.unmodifiableList((List) entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public List<FrameworkMethod> getAnnotatedMethods() {
        List<FrameworkMethod> methods = collectValues(this.methodsForAnnotations);
        Collections.sort(methods, METHOD_COMPARATOR);
        return methods;
    }

    public List<FrameworkMethod> getAnnotatedMethods(Class<? extends Annotation> annotationClass) {
        return Collections.unmodifiableList(getAnnotatedMembers(this.methodsForAnnotations, annotationClass, false));
    }

    public List<FrameworkField> getAnnotatedFields() {
        return collectValues(this.fieldsForAnnotations);
    }

    public List<FrameworkField> getAnnotatedFields(Class<? extends Annotation> annotationClass) {
        return Collections.unmodifiableList(getAnnotatedMembers(this.fieldsForAnnotations, annotationClass, false));
    }

    private <T> List<T> collectValues(Map<?, List<T>> map) {
        Set<T> values = new LinkedHashSet();
        for (List<T> additionalValues : map.values()) {
            values.addAll(additionalValues);
        }
        return new ArrayList(values);
    }

    private static <T> List<T> getAnnotatedMembers(Map<Class<? extends Annotation>, List<T>> map, Class<? extends Annotation> type, boolean fillIfAbsent) {
        if (!map.containsKey(type) && fillIfAbsent) {
            map.put(type, new ArrayList());
        }
        List<T> members = (List) map.get(type);
        return members == null ? Collections.emptyList() : members;
    }

    private static boolean runsTopToBottom(Class<? extends Annotation> annotation) {
        if (annotation.equals(Before.class)) {
            return true;
        }
        return annotation.equals(BeforeClass.class);
    }

    private static List<Class<?>> getSuperClasses(Class<?> testClass) {
        ArrayList<Class<?>> results = new ArrayList();
        for (Class<?> current = testClass; current != null; current = current.getSuperclass()) {
            results.add(current);
        }
        return results;
    }

    public Class<?> getJavaClass() {
        return this.clazz;
    }

    public String getName() {
        if (this.clazz == null) {
            return "null";
        }
        return this.clazz.getName();
    }

    public Constructor<?> getOnlyConstructor() {
        Constructor<?>[] constructors = this.clazz.getConstructors();
        Assert.assertEquals(1, (long) constructors.length);
        return constructors[0];
    }

    public Annotation[] getAnnotations() {
        if (this.clazz == null) {
            return new Annotation[0];
        }
        return this.clazz.getAnnotations();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        if (this.clazz == null) {
            return null;
        }
        return this.clazz.getAnnotation(annotationType);
    }

    public <T> List<T> getAnnotatedFieldValues(Object test, Class<? extends Annotation> annotationClass, Class<T> valueClass) {
        List<T> results = new ArrayList();
        for (FrameworkField each : getAnnotatedFields(annotationClass)) {
            try {
                Object fieldValue = each.get(test);
                if (valueClass.isInstance(fieldValue)) {
                    results.add(valueClass.cast(fieldValue));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("How did getFields return a field we couldn't access?", e);
            }
        }
        return results;
    }

    public <T> List<T> getAnnotatedMethodValues(Object test, Class<? extends Annotation> annotationClass, Class<T> valueClass) {
        List<T> results = new ArrayList();
        for (FrameworkMethod each : getAnnotatedMethods(annotationClass)) {
            try {
                if (valueClass.isAssignableFrom(each.getReturnType())) {
                    results.add(valueClass.cast(each.invokeExplosively(test, new Object[0])));
                }
            } catch (Throwable e) {
                RuntimeException runtimeException = new RuntimeException("Exception in " + each.getName(), e);
            }
        }
        return results;
    }

    public boolean isPublic() {
        return Modifier.isPublic(this.clazz.getModifiers());
    }

    public boolean isANonStaticInnerClass() {
        return this.clazz.isMemberClass() ? Modifier.isStatic(this.clazz.getModifiers()) ^ 1 : false;
    }

    public int hashCode() {
        return this.clazz == null ? 0 : this.clazz.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this.clazz != ((TestClass) obj).clazz) {
            z = false;
        }
        return z;
    }
}
