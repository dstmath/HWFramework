package com.android.server.wifi;

public interface PropertyService {
    String get(String str, String str2);

    boolean getBoolean(String str, boolean z);

    void set(String str, String str2);
}
