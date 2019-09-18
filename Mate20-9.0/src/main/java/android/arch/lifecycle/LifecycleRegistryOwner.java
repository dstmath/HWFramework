package android.arch.lifecycle;

import android.support.annotation.NonNull;

@Deprecated
public interface LifecycleRegistryOwner extends LifecycleOwner {
    @NonNull
    LifecycleRegistry getLifecycle();
}
