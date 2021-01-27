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

    public TestClass(Class<?> clazz2) {
        this.clazz = clazz2;
        if (clazz2 == null || clazz2.getConstructors().length <= 1) {
            Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations2 = new LinkedHashMap<>();
            Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations2 = new LinkedHashMap<>();
            scanAnnotatedMembers(methodsForAnnotations2, fieldsForAnnotations2);
            this.methodsForAnnotations = makeDeeplyUnmodifiable(methodsForAnnotations2);
            this.fieldsForAnnotations = makeDeeplyUnmodifiable(fieldsForAnnotations2);
            return;
        }
        throw new IllegalArgumentException("Test class can only have one constructor");
    }

    /* access modifiers changed from: protected */
    public void scanAnnotatedMembers(Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations2, Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations2) {
        for (Class<?> eachClass : getSuperClasses(this.clazz)) {
            for (Method eachMethod : MethodSorter.getDeclaredMethods(eachClass)) {
                addToAnnotationLists(new FrameworkMethod(eachMethod), methodsForAnnotations2);
            }
            for (Field eachField : getSortedDeclaredFields(eachClass)) {
                addToAnnotationLists(new FrameworkField(eachField), fieldsForAnnotations2);
            }
        }
    }

    private static Field[] getSortedDeclaredFields(Class<?> clazz2) {
        Field[] declaredFields = clazz2.getDeclaredFields();
        Arrays.sort(declaredFields, FIELD_COMPARATOR);
        return declaredFields;
    }

    protected static <T extends FrameworkMember<T>> void addToAnnotationLists(T member, Map<Class<? extends Annotation>, List<T>> map) {
        for (Annotation each : member.getAnnotations()) {
            Class<? extends Annotation> type = each.annotationType();
            List<T> members = getAnnotatedMembers(map, type, true);
            if (!member.isShadowedBy(members)) {
                if (runsTopToBottom(type)) {
                    members.add(0, member);
                } else {
                    members.add(member);
                }
            } else {
                return;
            }
        }
    }

    private static <T extends FrameworkMember<T>> Map<Class<? extends Annotation>, List<T>> makeDeeplyUnmodifiable(Map<Class<? extends Annotation>, List<T>> source) {
        LinkedHashMap<Class<? extends Annotation>, List<T>> copy = new LinkedHashMap<>();
        for (Map.Entry<Class<? extends Annotation>, List<T>> entry : source.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
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
        Set<T> values = new LinkedHashSet<>();
        for (List<T> additionalValues : map.values()) {
            values.addAll(additionalValues);
        }
        return new ArrayList(values);
    }

    private static <T> List<T> getAnnotatedMembers(Map<Class<? extends Annotation>, List<T>> map, Class<? extends Annotation> type, boolean fillIfAbsent) {
        if (!map.containsKey(type) && fillIfAbsent) {
            map.put(type, new ArrayList());
        }
        List<T> members = map.get(type);
        return members == null ? Collections.emptyList() : members;
    }

    private static boolean runsTopToBottom(Class<? extends Annotation> annotation) {
        return annotation.equals(Before.class) || annotation.equals(BeforeClass.class);
    }

    private static List<Class<?>> getSuperClasses(Class<?> testClass) {
        ArrayList<Class<?>> results = new ArrayList<>();
        for (Class<?> current = testClass; current != null; current = current.getSuperclass()) {
            results.add(current);
        }
        return results;
    }

    public Class<?> getJavaClass() {
        return this.clazz;
    }

    public String getName() {
        Class<?> cls = this.clazz;
        if (cls == null) {
            return "null";
        }
        return cls.getName();
    }

    public Constructor<?> getOnlyConstructor() {
        Constructor<?>[] constructors = this.clazz.getConstructors();
        Assert.assertEquals(1L, (long) constructors.length);
        return constructors[0];
    }

    @Override // org.junit.runners.model.Annotatable
    public Annotation[] getAnnotations() {
        Class<?> cls = this.clazz;
        if (cls == null) {
            return new Annotation[0];
        }
        return cls.getAnnotations();
    }

    @Override // org.junit.runners.model.Annotatable
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        Class<?> cls = this.clazz;
        if (cls == null) {
            return null;
        }
        return (T) cls.getAnnotation(annotationType);
    }

    public <T> List<T> getAnnotatedFieldValues(Object test, Class<? extends Annotation> annotationClass, Class<T> valueClass) {
        List<T> results = new ArrayList<>();
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
        List<T> results = new ArrayList<>();
        for (FrameworkMethod each : getAnnotatedMethods(annotationClass)) {
            try {
                if (valueClass.isAssignableFrom(each.getReturnType())) {
                    results.add(valueClass.cast(each.invokeExplosively(test, new Object[0])));
                }
            } catch (Throwable e) {
                throw new RuntimeException("Exception in " + each.getName(), e);
            }
        }
        return results;
    }

    public boolean isPublic() {
        return Modifier.isPublic(this.clazz.getModifiers());
    }

    public boolean isANonStaticInnerClass() {
        return this.clazz.isMemberClass() && !Modifier.isStatic(this.clazz.getModifiers());
    }

    public int hashCode() {
        Class<?> cls = this.clazz;
        if (cls == null) {
            return 0;
        }
        return cls.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this.clazz == ((TestClass) obj).clazz) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static class FieldComparator implements Comparator<Field> {
        private FieldComparator() {
        }

        public int compare(Field left, Field right) {
            return left.getName().compareTo(right.getName());
        }
    }

    private static class MethodComparator implements Comparator<FrameworkMethod> {
        private MethodComparator() {
        }

        public int compare(FrameworkMethod left, FrameworkMethod right) {
            return MethodSorter.NAME_ASCENDING.compare(left.getMethod(), right.getMethod());
        }
    }
}
