package java.lang;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

final class ProcessEnvironment {
    static final int MIN_NAME_LENGTH = 0;
    private static final HashMap<Variable, Value> theEnvironment;
    private static final Map<String, String> theUnmodifiableEnvironment = Collections.unmodifiableMap(new StringEnvironment(theEnvironment));

    private static abstract class ExternalData {
        protected final byte[] bytes;
        protected final String str;

        protected ExternalData(String str, byte[] bytes) {
            this.str = str;
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return this.bytes;
        }

        public String toString() {
            return this.str;
        }

        public boolean equals(Object o) {
            if (o instanceof ExternalData) {
                return ProcessEnvironment.arrayEquals(getBytes(), ((ExternalData) o).getBytes());
            }
            return false;
        }

        public int hashCode() {
            return ProcessEnvironment.arrayHash(getBytes());
        }
    }

    private static class StringEntry implements Entry<String, String> {
        private final Entry<Variable, Value> e;

        public StringEntry(Entry<Variable, Value> e) {
            this.e = e;
        }

        public String getKey() {
            return ((Variable) this.e.getKey()).toString();
        }

        public String getValue() {
            return ((Value) this.e.getValue()).toString();
        }

        public String setValue(String newValue) {
            return ((Value) this.e.setValue(Value.valueOf(newValue))).toString();
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }

        public boolean equals(Object o) {
            if (o instanceof StringEntry) {
                return this.e.equals(((StringEntry) o).e);
            }
            return false;
        }

        public int hashCode() {
            return this.e.hashCode();
        }
    }

    private static class StringEntrySet extends AbstractSet<Entry<String, String>> {
        private final Set<Entry<Variable, Value>> s;

        public StringEntrySet(Set<Entry<Variable, Value>> s) {
            this.s = s;
        }

        public int size() {
            return this.s.size();
        }

        public boolean isEmpty() {
            return this.s.isEmpty();
        }

        public void clear() {
            this.s.clear();
        }

        public Iterator<Entry<String, String>> iterator() {
            return new Iterator<Entry<String, String>>() {
                Iterator<Entry<Variable, Value>> i = StringEntrySet.this.s.iterator();

                public boolean hasNext() {
                    return this.i.hasNext();
                }

                public Entry<String, String> next() {
                    return new StringEntry((Entry) this.i.next());
                }

                public void remove() {
                    this.i.remove();
                }
            };
        }

        private static Entry<Variable, Value> vvEntry(final Object o) {
            if (o instanceof StringEntry) {
                return ((StringEntry) o).e;
            }
            return new Entry<Variable, Value>() {
                public Variable getKey() {
                    return Variable.valueOfQueryOnly(((Entry) o).getKey());
                }

                public Value getValue() {
                    return Value.valueOfQueryOnly(((Entry) o).getValue());
                }

                public Value setValue(Value value) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public boolean contains(Object o) {
            return this.s.contains(vvEntry(o));
        }

        public boolean remove(Object o) {
            return this.s.remove(vvEntry(o));
        }

        public boolean equals(Object o) {
            if (o instanceof StringEntrySet) {
                return this.s.equals(((StringEntrySet) o).s);
            }
            return false;
        }

        public int hashCode() {
            return this.s.hashCode();
        }
    }

    private static class StringEnvironment extends AbstractMap<String, String> {
        private Map<Variable, Value> m;

        private static String toString(Value v) {
            return v == null ? null : v.toString();
        }

        public StringEnvironment(Map<Variable, Value> m) {
            this.m = m;
        }

        public int size() {
            return this.m.size();
        }

        public boolean isEmpty() {
            return this.m.isEmpty();
        }

        public void clear() {
            this.m.clear();
        }

        public boolean containsKey(Object key) {
            return this.m.containsKey(Variable.valueOfQueryOnly(key));
        }

        public boolean containsValue(Object value) {
            return this.m.containsValue(Value.valueOfQueryOnly(value));
        }

        public String get(Object key) {
            return toString((Value) this.m.get(Variable.valueOfQueryOnly(key)));
        }

        public String put(String key, String value) {
            return toString((Value) this.m.put(Variable.valueOf(key), Value.valueOf(value)));
        }

        public String remove(Object key) {
            return toString((Value) this.m.remove(Variable.valueOfQueryOnly(key)));
        }

        public Set<String> keySet() {
            return new StringKeySet(this.m.keySet());
        }

        public Set<Entry<String, String>> entrySet() {
            return new StringEntrySet(this.m.entrySet());
        }

        public Collection<String> values() {
            return new StringValues(this.m.values());
        }

        public byte[] toEnvironmentBlock(int[] envc) {
            int count = this.m.size() * 2;
            for (Entry<Variable, Value> entry : this.m.entrySet()) {
                count = (count + ((Variable) entry.getKey()).getBytes().length) + ((Value) entry.getValue()).getBytes().length;
            }
            byte[] block = new byte[count];
            int i = 0;
            for (Entry<Variable, Value> entry2 : this.m.entrySet()) {
                byte[] key = ((Variable) entry2.getKey()).getBytes();
                byte[] value = ((Value) entry2.getValue()).getBytes();
                System.arraycopy(key, 0, block, i, key.length);
                i += key.length;
                int i2 = i + 1;
                block[i] = (byte) 61;
                System.arraycopy(value, 0, block, i2, value.length);
                i = i2 + (value.length + 1);
            }
            envc[0] = this.m.size();
            return block;
        }
    }

    private static class StringKeySet extends AbstractSet<String> {
        private final Set<Variable> s;

        public StringKeySet(Set<Variable> s) {
            this.s = s;
        }

        public int size() {
            return this.s.size();
        }

        public boolean isEmpty() {
            return this.s.isEmpty();
        }

        public void clear() {
            this.s.clear();
        }

        public Iterator<String> iterator() {
            return new Iterator<String>() {
                Iterator<Variable> i = StringKeySet.this.s.iterator();

                public boolean hasNext() {
                    return this.i.hasNext();
                }

                public String next() {
                    return ((Variable) this.i.next()).toString();
                }

                public void remove() {
                    this.i.remove();
                }
            };
        }

        public boolean contains(Object o) {
            return this.s.contains(Variable.valueOfQueryOnly(o));
        }

        public boolean remove(Object o) {
            return this.s.remove(Variable.valueOfQueryOnly(o));
        }
    }

    private static class StringValues extends AbstractCollection<String> {
        private final Collection<Value> c;

        public StringValues(Collection<Value> c) {
            this.c = c;
        }

        public int size() {
            return this.c.size();
        }

        public boolean isEmpty() {
            return this.c.isEmpty();
        }

        public void clear() {
            this.c.clear();
        }

        public Iterator<String> iterator() {
            return new Iterator<String>() {
                Iterator<Value> i = StringValues.this.c.iterator();

                public boolean hasNext() {
                    return this.i.hasNext();
                }

                public String next() {
                    return ((Value) this.i.next()).toString();
                }

                public void remove() {
                    this.i.remove();
                }
            };
        }

        public boolean contains(Object o) {
            return this.c.contains(Value.valueOfQueryOnly(o));
        }

        public boolean remove(Object o) {
            return this.c.remove(Value.valueOfQueryOnly(o));
        }

        public boolean equals(Object o) {
            if (o instanceof StringValues) {
                return this.c.equals(((StringValues) o).c);
            }
            return false;
        }

        public int hashCode() {
            return this.c.hashCode();
        }
    }

    private static class Value extends ExternalData implements Comparable<Value> {
        protected Value(String str, byte[] bytes) {
            super(str, bytes);
        }

        public static Value valueOfQueryOnly(Object str) {
            return valueOfQueryOnly((String) str);
        }

        public static Value valueOfQueryOnly(String str) {
            return new Value(str, str.getBytes());
        }

        public static Value valueOf(String str) {
            ProcessEnvironment.validateValue(str);
            return valueOfQueryOnly(str);
        }

        public static Value valueOf(byte[] bytes) {
            return new Value(new String(bytes), bytes);
        }

        public int compareTo(Value value) {
            return ProcessEnvironment.arrayCompare(getBytes(), value.getBytes());
        }

        public boolean equals(Object o) {
            return o instanceof Value ? super.equals(o) : false;
        }
    }

    private static class Variable extends ExternalData implements Comparable<Variable> {
        protected Variable(String str, byte[] bytes) {
            super(str, bytes);
        }

        public static Variable valueOfQueryOnly(Object str) {
            return valueOfQueryOnly((String) str);
        }

        public static Variable valueOfQueryOnly(String str) {
            return new Variable(str, str.getBytes());
        }

        public static Variable valueOf(String str) {
            ProcessEnvironment.validateVariable(str);
            return valueOfQueryOnly(str);
        }

        public static Variable valueOf(byte[] bytes) {
            return new Variable(new String(bytes), bytes);
        }

        public int compareTo(Variable variable) {
            return ProcessEnvironment.arrayCompare(getBytes(), variable.getBytes());
        }

        public boolean equals(Object o) {
            return o instanceof Variable ? super.equals(o) : false;
        }
    }

    private static native byte[][] environ();

    static {
        byte[][] environ = environ();
        theEnvironment = new HashMap((environ.length / 2) + 3);
        for (int i = environ.length - 1; i > 0; i -= 2) {
            theEnvironment.put(Variable.valueOf(environ[i - 1]), Value.valueOf(environ[i]));
        }
    }

    static String getenv(String name) {
        return (String) theUnmodifiableEnvironment.get(name);
    }

    static Map<String, String> getenv() {
        return theUnmodifiableEnvironment;
    }

    static Map<String, String> environment() {
        return new StringEnvironment((Map) theEnvironment.clone());
    }

    static Map<String, String> emptyEnvironment(int capacity) {
        return new StringEnvironment(new HashMap(capacity));
    }

    private ProcessEnvironment() {
    }

    private static void validateVariable(String name) {
        if (name.indexOf(61) != -1 || name.indexOf(0) != -1) {
            throw new IllegalArgumentException("Invalid environment variable name: \"" + name + "\"");
        }
    }

    private static void validateValue(String value) {
        if (value.indexOf(0) != -1) {
            throw new IllegalArgumentException("Invalid environment variable value: \"" + value + "\"");
        }
    }

    static byte[] toEnvironmentBlock(Map<String, String> map, int[] envc) {
        if (map == null) {
            return null;
        }
        return ((StringEnvironment) map).toEnvironmentBlock(envc);
    }

    private static int arrayCompare(byte[] x, byte[] y) {
        int min = x.length < y.length ? x.length : y.length;
        for (int i = 0; i < min; i++) {
            if (x[i] != y[i]) {
                return x[i] - y[i];
            }
        }
        return x.length - y.length;
    }

    private static boolean arrayEquals(byte[] x, byte[] y) {
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; i++) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    private static int arrayHash(byte[] x) {
        int hash = 0;
        for (byte b : x) {
            hash = (hash * 31) + b;
        }
        return hash;
    }
}
