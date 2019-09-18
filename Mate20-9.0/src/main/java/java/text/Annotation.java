package java.text;

public class Annotation {
    private Object value;

    public Annotation(Object value2) {
        this.value = value2;
    }

    public Object getValue() {
        return this.value;
    }

    public String toString() {
        return getClass().getName() + "[value=" + this.value + "]";
    }
}
