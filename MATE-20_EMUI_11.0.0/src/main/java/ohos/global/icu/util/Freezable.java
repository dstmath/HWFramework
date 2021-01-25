package ohos.global.icu.util;

public interface Freezable<T> extends Cloneable {
    T cloneAsThawed();

    T freeze();

    boolean isFrozen();
}
