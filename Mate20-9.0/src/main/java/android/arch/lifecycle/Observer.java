package android.arch.lifecycle;

import android.support.annotation.Nullable;

public interface Observer<T> {
    void onChanged(@Nullable T t);
}
