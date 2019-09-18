package android.arch.lifecycle;

import android.support.annotation.NonNull;

public interface ViewModelStoreOwner {
    @NonNull
    ViewModelStore getViewModelStore();
}
