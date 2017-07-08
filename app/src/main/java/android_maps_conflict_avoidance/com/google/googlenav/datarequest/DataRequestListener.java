package android_maps_conflict_avoidance.com.google.googlenav.datarequest;

public interface DataRequestListener {
    void onComplete(DataRequest dataRequest);

    void onNetworkError(int i, boolean z, String str);
}
