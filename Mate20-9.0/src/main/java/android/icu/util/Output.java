package android.icu.util;

public class Output<T> {
    public T value;

    public String toString() {
        return this.value == null ? "null" : this.value.toString();
    }

    public Output() {
    }

    public Output(T value2) {
        this.value = value2;
    }
}
