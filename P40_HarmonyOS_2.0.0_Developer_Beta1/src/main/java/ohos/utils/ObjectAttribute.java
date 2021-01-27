package ohos.utils;

public abstract class ObjectAttribute<O, T> {
    private Class<T> attrType;
    private String name;

    public abstract T get(O o);

    public boolean isReadOnly() {
        return true;
    }

    public ObjectAttribute(Class<T> cls, String str) {
        this.attrType = cls;
        this.name = str;
    }

    public void set(O o, T t) {
        throw new UnsupportedOperationException("Read-Only attribute:" + this.name);
    }

    public String getName() {
        return this.name;
    }

    public Class<T> getAttrType() {
        return this.attrType;
    }
}
