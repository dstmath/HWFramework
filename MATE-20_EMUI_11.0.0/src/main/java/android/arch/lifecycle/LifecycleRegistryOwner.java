package android.arch.lifecycle;

import android.support.annotation.NonNull;

@Deprecated
public interface LifecycleRegistryOwner extends LifecycleOwner {
    @Override // android.arch.lifecycle.LifecycleOwner
    @NonNull
    LifecycleRegistry getLifecycle();
}
