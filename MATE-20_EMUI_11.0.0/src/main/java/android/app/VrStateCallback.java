package android.app;

import android.annotation.SystemApi;

@SystemApi
public abstract class VrStateCallback {
    public void onPersistentVrStateChanged(boolean enabled) {
    }

    public void onVrStateChanged(boolean enabled) {
    }
}
