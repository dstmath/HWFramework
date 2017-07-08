package android.icu.text;

public interface Transform<S, D> {
    D transform(S s);
}
