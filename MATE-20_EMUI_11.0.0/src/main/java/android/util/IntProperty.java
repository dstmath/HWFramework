package android.util;

public abstract class IntProperty<T> extends Property<T, Integer> {
    public abstract void setValue(T t, int i);

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // android.util.Property
    public /* bridge */ /* synthetic */ void set(Object obj, Integer num) {
        set((IntProperty<T>) obj, num);
    }

    public IntProperty(String name) {
        super(Integer.class, name);
    }

    public final void set(T object, Integer value) {
        setValue(object, value.intValue());
    }
}
