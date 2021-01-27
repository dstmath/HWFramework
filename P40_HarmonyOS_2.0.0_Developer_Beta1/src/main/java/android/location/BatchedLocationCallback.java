package android.location;

import android.annotation.SystemApi;
import java.util.List;

@SystemApi
public abstract class BatchedLocationCallback {
    public void onLocationBatch(List<Location> list) {
    }
}
