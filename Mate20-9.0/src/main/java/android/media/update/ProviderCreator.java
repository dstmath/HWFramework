package android.media.update;

@FunctionalInterface
public interface ProviderCreator<T, U> {
    U createProvider(T t);
}
