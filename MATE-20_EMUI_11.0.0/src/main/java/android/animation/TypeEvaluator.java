package android.animation;

public interface TypeEvaluator<T> {
    T evaluate(float f, T t, T t2);
}
