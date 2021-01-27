package android.util;

public abstract class FloatProperty<T> extends Property<T, Float> {
    public abstract void setValue(T t, float f);

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // android.util.Property
    public /* bridge */ /* synthetic */ void set(Object obj, Float f) {
        set((FloatProperty<T>) obj, f);
    }

    public FloatProperty(String name) {
        super(Float.class, name);
    }

    public final void set(T object, Float value) {
        setValue(object, value.floatValue());
    }
}
