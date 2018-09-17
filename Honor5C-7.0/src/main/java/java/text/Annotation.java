package java.text;

public class Annotation {
    private Object value;

    public Annotation(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public String toString() {
        return getClass().getName() + "[value=" + this.value + "]";
    }
}
