package com.android.internal.util;

import android.util.ArraySet;
import android.util.ExceptionUtils;
import com.android.internal.util.FunctionalUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> List<T> filter(List<T> list, Predicate<? super T> predicate) {
        ArrayList<T> result = null;
        for (int i = 0; i < size(list); i++) {
            T item = list.get(i);
            if (predicate.test(item)) {
                result = ArrayUtils.add(result, item);
            }
        }
        return emptyIfNull(result);
    }

    public static <T> Set<T> filter(Set<T> set, Predicate<? super T> predicate) {
        if (set == null || set.size() == 0) {
            return Collections.emptySet();
        }
        ArraySet<T> result = null;
        if (set instanceof ArraySet) {
            ArraySet<T> arraySet = (ArraySet) set;
            int size = arraySet.size();
            for (int i = 0; i < size; i++) {
                T item = arraySet.valueAt(i);
                if (predicate.test(item)) {
                    result = ArrayUtils.add(result, item);
                }
            }
        } else {
            for (T item2 : set) {
                if (predicate.test(item2)) {
                    result = ArrayUtils.add(result, item2);
                }
            }
        }
        return emptyIfNull(result);
    }

    public static <T> void addIf(List<T> source, Collection<? super T> dest, Predicate<? super T> predicate) {
        for (int i = 0; i < size(source); i++) {
            T item = source.get(i);
            if (predicate.test(item)) {
                dest.add(item);
            }
        }
    }

    public static <I, O> List<O> map(List<I> cur, Function<? super I, ? extends O> f) {
        if (isEmpty(cur)) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < cur.size(); i++) {
            arrayList.add(f.apply(cur.get(i)));
        }
        return arrayList;
    }

    public static <I, O> Set<O> map(Set<I> cur, Function<? super I, ? extends O> f) {
        if (isEmpty(cur)) {
            return Collections.emptySet();
        }
        ArraySet arraySet = new ArraySet();
        if (cur instanceof ArraySet) {
            ArraySet<I> arraySet2 = (ArraySet) cur;
            int size = arraySet2.size();
            for (int i = 0; i < size; i++) {
                arraySet.add(f.apply(arraySet2.valueAt(i)));
            }
        } else {
            for (I item : cur) {
                arraySet.add(f.apply(item));
            }
        }
        return arraySet;
    }

    public static <I, O> List<O> mapNotNull(List<I> cur, Function<? super I, ? extends O> f) {
        if (isEmpty(cur)) {
            return Collections.emptyList();
        }
        List<O> result = null;
        for (int i = 0; i < cur.size(); i++) {
            Object apply = f.apply(cur.get(i));
            if (apply != null) {
                result = add(result, apply);
            }
        }
        return emptyIfNull(result);
    }

    public static <T> List<T> emptyIfNull(List<T> cur) {
        return cur == null ? Collections.emptyList() : cur;
    }

    public static <T> Set<T> emptyIfNull(Set<T> cur) {
        return cur == null ? Collections.emptySet() : cur;
    }

    public static int size(Collection<?> cur) {
        if (cur != null) {
            return cur.size();
        }
        return 0;
    }

    public static int size(Map<?, ?> cur) {
        if (cur != null) {
            return cur.size();
        }
        return 0;
    }

    public static boolean isEmpty(Collection<?> cur) {
        return size(cur) == 0;
    }

    public static <T> List<T> filter(List<?> list, Class<T> c) {
        if (isEmpty(list)) {
            return Collections.emptyList();
        }
        ArrayList<T> result = null;
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (c.isInstance(item)) {
                result = ArrayUtils.add(result, item);
            }
        }
        return emptyIfNull(result);
    }

    public static <T> boolean any(List<T> items, Predicate<T> predicate) {
        return find(items, predicate) != null;
    }

    public static <T> T find(List<T> items, Predicate<T> predicate) {
        if (isEmpty(items)) {
            return null;
        }
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }

    public static <T> List<T> add(List<T> cur, T val) {
        if (cur == null || cur == Collections.emptyList()) {
            cur = new ArrayList();
        }
        cur.add(val);
        return cur;
    }

    public static <T> Set<T> add(Set<T> cur, T val) {
        if (cur == null || cur == Collections.emptySet()) {
            cur = new ArraySet();
        }
        cur.add(val);
        return cur;
    }

    public static <T> List<T> remove(List<T> cur, T val) {
        if (isEmpty(cur)) {
            return emptyIfNull(cur);
        }
        cur.remove(val);
        return cur;
    }

    public static <T> Set<T> remove(Set<T> cur, T val) {
        if (isEmpty(cur)) {
            return emptyIfNull(cur);
        }
        cur.remove(val);
        return cur;
    }

    public static <T> List<T> copyOf(List<T> cur) {
        return isEmpty(cur) ? Collections.emptyList() : new ArrayList(cur);
    }

    public static <T> Set<T> copyOf(Set<T> cur) {
        return isEmpty(cur) ? Collections.emptySet() : new ArraySet(cur);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: com.android.internal.util.FunctionalUtils$ThrowingConsumer<T> */
    /* JADX WARN: Multi-variable type inference failed */
    public static <T> void forEach(Set<T> cur, FunctionalUtils.ThrowingConsumer<T> action) {
        int size;
        if (cur != null && action != 0 && (size = cur.size()) != 0) {
            try {
                if (cur instanceof ArraySet) {
                    ArraySet<T> arraySet = (ArraySet) cur;
                    for (int i = 0; i < size; i++) {
                        action.acceptOrThrow(arraySet.valueAt(i));
                    }
                    return;
                }
                for (T t : cur) {
                    action.acceptOrThrow(t);
                }
            } catch (Exception e) {
                throw ExceptionUtils.propagate(e);
            }
        }
    }

    public static <T> T firstOrNull(List<T> cur) {
        if (isEmpty(cur)) {
            return null;
        }
        return cur.get(0);
    }

    public static <T> T firstOrNull(Collection<T> cur) {
        if (isEmpty(cur)) {
            return null;
        }
        return cur.iterator().next();
    }

    public static <T> List<T> singletonOrEmpty(T item) {
        return item == null ? Collections.emptyList() : Collections.singletonList(item);
    }
}
