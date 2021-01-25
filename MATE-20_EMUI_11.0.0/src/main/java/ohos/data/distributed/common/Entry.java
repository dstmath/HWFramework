package ohos.data.distributed.common;

import java.util.Objects;

public class Entry {
    private String key;
    private Value value;

    public Entry() {
    }

    public Entry(String str, Value value2) {
        this.key = str;
        this.value = value2;
    }

    public void setKey(String str) {
        this.key = str;
    }

    public String getKey() {
        return this.key;
    }

    public Value getValue() {
        return this.value;
    }

    public void setValue(Value value2) {
        this.value = value2;
    }

    public int hashCode() {
        return Objects.hash(this.key, this.value);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof Entry)) {
            return false;
        }
        Entry entry = (Entry) obj;
        return Objects.equals(this.key, entry.key) && Objects.equals(this.value, entry.value);
    }
}
