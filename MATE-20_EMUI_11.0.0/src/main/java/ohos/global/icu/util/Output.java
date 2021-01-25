package ohos.global.icu.util;

public class Output<T> {
    public T value;

    public String toString() {
        T t = this.value;
        return t == null ? "null" : t.toString();
    }

    public Output() {
    }

    public Output(T t) {
        this.value = t;
    }
}
