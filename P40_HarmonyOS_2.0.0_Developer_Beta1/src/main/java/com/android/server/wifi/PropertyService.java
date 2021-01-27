package com.android.server.wifi;

import com.android.internal.annotations.VisibleForTesting;

@VisibleForTesting
public interface PropertyService {
    String get(String str, String str2);

    boolean getBoolean(String str, boolean z);

    String getString(String str, String str2);

    void set(String str, String str2);
}
