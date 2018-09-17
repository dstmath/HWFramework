package android.util;

public abstract class FloatProperty<T> extends Property<T, Float> {
    public abstract void setValue(T t, float f);

    public FloatProperty(String name) {
        super(Float.class, name);
    }

    public final void set(T object, Float value) {
        setValue(object, value.floatValue());
    }
}
