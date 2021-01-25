package ohos.global.icu.impl.locale;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ohos.com.sun.xml.internal.stream.writers.WriterUtility;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.ICUUncheckedIOException;

public class XCldrStub {

    public interface Predicate<T> {
        boolean test(T t);
    }

    public static class Multimap<K, V> {
        private final Map<K, Set<V>> map;
        private final Class<Set<V>> setClass;

        private Multimap(Map<K, Set<V>> map2, Class<?> cls) {
            this.map = map2;
            this.setClass = (Class<Set<V>>) (cls == null ? HashSet.class : cls);
        }

        @SafeVarargs
        public final Multimap<K, V> putAll(K k, V... vArr) {
            if (vArr.length != 0) {
                createSetIfMissing(k).addAll(Arrays.asList(vArr));
            }
            return this;
        }

        public void putAll(K k, Collection<V> collection) {
            if (!collection.isEmpty()) {
                createSetIfMissing(k).addAll(collection);
            }
        }

        public void putAll(Collection<K> collection, V v) {
            for (K k : collection) {
                put(k, v);
            }
        }

        public void putAll(Multimap<K, V> multimap) {
            for (Map.Entry<K, Set<V>> entry : multimap.map.entrySet()) {
                putAll((Multimap<K, V>) entry.getKey(), entry.getValue());
            }
        }

        public void put(K k, V v) {
            createSetIfMissing(k).add(v);
        }

        private Set<V> createSetIfMissing(K k) {
            Set<V> set = this.map.get(k);
            if (set != null) {
                return set;
            }
            Map<K, Set<V>> map2 = this.map;
            Set<V> instance = getInstance();
            map2.put(k, instance);
            return instance;
        }

        private Set<V> getInstance() {
            try {
                return this.setClass.newInstance();
            } catch (Exception e) {
                throw new ICUException(e);
            }
        }

        public Set<V> get(K k) {
            return this.map.get(k);
        }

        public Set<K> keySet() {
            return this.map.keySet();
        }

        public Map<K, Set<V>> asMap() {
            return this.map;
        }

        public Set<V> values() {
            Collection<Set<V>> values = this.map.values();
            if (values.size() == 0) {
                return Collections.emptySet();
            }
            Set<V> instance = getInstance();
            for (Set<V> set : values) {
                instance.addAll(set);
            }
            return instance;
        }

        public int size() {
            return this.map.size();
        }

        public Iterable<Map.Entry<K, V>> entries() {
            return new MultimapIterator(this.map);
        }

        public boolean equals(Object obj) {
            return this == obj || (obj != null && obj.getClass() == getClass() && this.map.equals(((Multimap) obj).map));
        }

        public int hashCode() {
            return this.map.hashCode();
        }
    }

    public static class Multimaps {
        public static <K, V> Map<K, V> forMap(Map<K, V> map) {
            return map;
        }

        public static <K, V, R extends Multimap<K, V>> R invertFrom(Multimap<V, K> multimap, R r) {
            for (Map.Entry<V, Set<K>> entry : multimap.asMap().entrySet()) {
                r.putAll(entry.getValue(), entry.getKey());
            }
            return r;
        }

        public static <K, V, R extends Multimap<K, V>> R invertFrom(Map<V, K> map, R r) {
            for (Map.Entry<V, K> entry : map.entrySet()) {
                r.put(entry.getValue(), entry.getKey());
            }
            return r;
        }
    }

    private static class MultimapIterator<K, V> implements Iterator<Map.Entry<K, V>>, Iterable<Map.Entry<K, V>> {
        private final ReusableEntry<K, V> entry;
        private final Iterator<Map.Entry<K, Set<V>>> it1;
        private Iterator<V> it2;

        @Override // java.lang.Iterable
        public Iterator<Map.Entry<K, V>> iterator() {
            return this;
        }

        private MultimapIterator(Map<K, Set<V>> map) {
            this.it2 = null;
            this.entry = new ReusableEntry<>();
            this.it1 = map.entrySet().iterator();
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            Iterator<V> it;
            return this.it1.hasNext() || ((it = this.it2) != null && it.hasNext());
        }

        @Override // java.util.Iterator
        public Map.Entry<K, V> next() {
            Iterator<V> it = this.it2;
            if (it == null || !it.hasNext()) {
                Map.Entry<K, Set<V>> next = this.it1.next();
                this.entry.key = next.getKey();
                this.it2 = next.getValue().iterator();
            } else {
                this.entry.value = this.it2.next();
            }
            return this.entry;
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: private */
    public static class ReusableEntry<K, V> implements Map.Entry<K, V> {
        K key;
        V value;

        private ReusableEntry() {
        }

        @Override // java.util.Map.Entry
        public K getKey() {
            return this.key;
        }

        @Override // java.util.Map.Entry
        public V getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public V setValue(V v) {
            throw new UnsupportedOperationException();
        }
    }

    public static class HashMultimap<K, V> extends Multimap<K, V> {
        private HashMultimap() {
            super(new HashMap(), HashSet.class);
        }

        public static <K, V> HashMultimap<K, V> create() {
            return new HashMultimap<>();
        }
    }

    public static class TreeMultimap<K, V> extends Multimap<K, V> {
        private TreeMultimap() {
            super(new TreeMap(), TreeSet.class);
        }

        public static <K, V> TreeMultimap<K, V> create() {
            return new TreeMultimap<>();
        }
    }

    public static class LinkedHashMultimap<K, V> extends Multimap<K, V> {
        private LinkedHashMultimap() {
            super(new LinkedHashMap(), LinkedHashSet.class);
        }

        public static <K, V> LinkedHashMultimap<K, V> create() {
            return new LinkedHashMultimap<>();
        }
    }

    public static <T> String join(T[] tArr, String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tArr.length; i++) {
            if (i != 0) {
                sb.append(str);
            }
            sb.append((Object) tArr[i]);
        }
        return sb.toString();
    }

    public static <T> String join(Iterable<T> iterable, String str) {
        StringBuilder sb = new StringBuilder();
        boolean z = true;
        for (T t : iterable) {
            if (!z) {
                sb.append(str);
            } else {
                z = false;
            }
            sb.append(t.toString());
        }
        return sb.toString();
    }

    public static class CollectionUtilities {
        public static <T, U extends Iterable<T>> String join(U u, String str) {
            return XCldrStub.join(u, str);
        }
    }

    public static class Joiner {
        private final String separator;

        private Joiner(String str) {
            this.separator = str;
        }

        public static final Joiner on(String str) {
            return new Joiner(str);
        }

        public <T> String join(T[] tArr) {
            return XCldrStub.join(tArr, this.separator);
        }

        public <T> String join(Iterable<T> iterable) {
            return XCldrStub.join(iterable, this.separator);
        }
    }

    public static class Splitter {
        Pattern pattern;
        boolean trimResults;

        public Splitter(char c) {
            this(Pattern.compile("\\Q" + c + "\\E"));
        }

        public Splitter(Pattern pattern2) {
            this.trimResults = false;
            this.pattern = pattern2;
        }

        public static Splitter on(char c) {
            return new Splitter(c);
        }

        public static Splitter on(Pattern pattern2) {
            return new Splitter(pattern2);
        }

        public List<String> splitToList(String str) {
            String[] split = this.pattern.split(str);
            if (this.trimResults) {
                for (int i = 0; i < split.length; i++) {
                    split[i] = split[i].trim();
                }
            }
            return Arrays.asList(split);
        }

        public Splitter trimResults() {
            this.trimResults = true;
            return this;
        }

        public Iterable<String> split(String str) {
            return splitToList(str);
        }
    }

    public static class ImmutableSet {
        public static <T> Set<T> copyOf(Set<T> set) {
            return Collections.unmodifiableSet(new LinkedHashSet(set));
        }
    }

    public static class ImmutableMap {
        public static <K, V> Map<K, V> copyOf(Map<K, V> map) {
            return Collections.unmodifiableMap(new LinkedHashMap(map));
        }
    }

    public static class ImmutableMultimap {
        /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: java.util.LinkedHashMap */
        /* JADX WARN: Multi-variable type inference failed */
        public static <K, V> Multimap<K, V> copyOf(Multimap<K, V> multimap) {
            Set set;
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (Map.Entry<K, Set<V>> entry : multimap.asMap().entrySet()) {
                Set<V> value = entry.getValue();
                K key = entry.getKey();
                if (value.size() == 1) {
                    set = Collections.singleton(value.iterator().next());
                } else {
                    set = Collections.unmodifiableSet(new LinkedHashSet(value));
                }
                linkedHashMap.put(key, set);
            }
            return new Multimap<>(Collections.unmodifiableMap(linkedHashMap), null);
        }
    }

    public static class FileUtilities {
        public static final Charset UTF8 = Charset.forName(WriterUtility.UTF_8);

        public static BufferedReader openFile(Class<?> cls, String str) {
            return openFile(cls, str, UTF8);
        }

        public static BufferedReader openFile(Class<?> cls, String str, Charset charset) {
            String str2;
            try {
                InputStream resourceAsStream = cls.getResourceAsStream(str);
                if (charset == null) {
                    charset = UTF8;
                }
                return new BufferedReader(new InputStreamReader(resourceAsStream, charset), 65536);
            } catch (Exception e) {
                if (cls == null) {
                    str2 = null;
                } else {
                    str2 = cls.getCanonicalName();
                }
                try {
                    String canonicalPath = new File(getRelativeFileName(cls, "../util/")).getCanonicalPath();
                    throw new ICUUncheckedIOException("Couldn't open file " + str + "; in path " + canonicalPath + "; relative to class: " + str2, e);
                } catch (Exception unused) {
                    throw new ICUUncheckedIOException("Couldn't open file: " + str + "; relative to class: " + str2, e);
                }
            }
        }

        public static String getRelativeFileName(Class<?> cls, String str) {
            if (cls == null) {
                cls = FileUtilities.class;
            }
            String url = cls.getResource(str).toString();
            if (url.startsWith("file:")) {
                return url.substring(5);
            }
            if (url.startsWith("jar:file:")) {
                return url.substring(9);
            }
            throw new ICUUncheckedIOException("File not found: " + url);
        }
    }

    public static class RegexUtilities {
        public static int findMismatch(Matcher matcher, CharSequence charSequence) {
            int i = 1;
            while (i < charSequence.length() && (matcher.reset(charSequence.subSequence(0, i)).matches() || matcher.hitEnd())) {
                i++;
            }
            return i - 1;
        }

        public static String showMismatch(Matcher matcher, CharSequence charSequence) {
            int findMismatch = findMismatch(matcher, charSequence);
            return ((Object) charSequence.subSequence(0, findMismatch)) + "☹" + ((Object) charSequence.subSequence(findMismatch, charSequence.length()));
        }
    }
}
