package android.arch.lifecycle;

import android.support.annotation.NonNull;

public interface LifecycleOwner {
    @NonNull
    Lifecycle getLifecycle();
}
