package android.util;

public abstract class IntProperty<T> extends Property<T, Integer> {
    public abstract void setValue(T t, int i);

    public IntProperty(String name) {
        super(Integer.class, name);
    }

    public final void set(T object, Integer value) {
        setValue(object, value.intValue());
    }
}
