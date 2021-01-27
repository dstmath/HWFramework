package android.content.pm;

public interface RegisteredServicesCacheListener<V> {
    void onServiceChanged(V v, int i, boolean z);
}
