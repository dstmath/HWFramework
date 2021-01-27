package com.google.protobuf;

import java.util.Iterator;
import java.util.Map;

public class LazyField extends LazyFieldLite {
    private final MessageLite defaultInstance;

    public LazyField(MessageLite defaultInstance2, ExtensionRegistryLite extensionRegistry, ByteString bytes) {
        super(extensionRegistry, bytes);
        this.defaultInstance = defaultInstance2;
    }

    @Override // com.google.protobuf.LazyFieldLite
    public boolean containsDefaultInstance() {
        return super.containsDefaultInstance() || this.value == this.defaultInstance;
    }

    public MessageLite getValue() {
        return getValue(this.defaultInstance);
    }

    @Override // com.google.protobuf.LazyFieldLite
    public int hashCode() {
        return getValue().hashCode();
    }

    @Override // com.google.protobuf.LazyFieldLite
    public boolean equals(Object obj) {
        return getValue().equals(obj);
    }

    public String toString() {
        return getValue().toString();
    }

    /* access modifiers changed from: package-private */
    public static class LazyEntry<K> implements Map.Entry<K, Object> {
        private Map.Entry<K, LazyField> entry;

        private LazyEntry(Map.Entry<K, LazyField> entry2) {
            this.entry = entry2;
        }

        @Override // java.util.Map.Entry
        public K getKey() {
            return this.entry.getKey();
        }

        @Override // java.util.Map.Entry
        public Object getValue() {
            LazyField field = this.entry.getValue();
            if (field == null) {
                return null;
            }
            return field.getValue();
        }

        public LazyField getField() {
            return this.entry.getValue();
        }

        @Override // java.util.Map.Entry
        public Object setValue(Object value) {
            if (value instanceof MessageLite) {
                return this.entry.getValue().setValue((MessageLite) value);
            }
            throw new IllegalArgumentException("LazyField now only used for MessageSet, and the value of MessageSet must be an instance of MessageLite");
        }
    }

    /* access modifiers changed from: package-private */
    public static class LazyIterator<K> implements Iterator<Map.Entry<K, Object>> {
        private Iterator<Map.Entry<K, Object>> iterator;

        public LazyIterator(Iterator<Map.Entry<K, Object>> iterator2) {
            this.iterator = iterator2;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override // java.util.Iterator
        public Map.Entry<K, Object> next() {
            Map.Entry<K, ?> entry = this.iterator.next();
            if (entry.getValue() instanceof LazyField) {
                return new LazyEntry(entry);
            }
            return entry;
        }

        @Override // java.util.Iterator
        public void remove() {
            this.iterator.remove();
        }
    }
}
