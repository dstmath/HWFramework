package android.app;

import android.content.pm.ApplicationInfo;

public interface ZygotePreload {
    void doPreload(ApplicationInfo applicationInfo);
}
