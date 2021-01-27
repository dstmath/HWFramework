package ohos.global.resource;

public abstract class Entry {

    public enum Type {
        FOLDER,
        FILE,
        UNKNOWN
    }

    public abstract String getPath();

    public abstract Type getType();
}
