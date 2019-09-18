package com.android.internal.telephony;

public interface HwReportManager {
    void reportMultiTZNoNitz();

    void reportMultiTZRegistered();

    void reportNitzIgnore(int i, String str);

    void reportSetTimeZoneByIso(Phone phone, String str, boolean z, String str2);

    void reportSetTimeZoneByLocation(String str);

    void reportSetTimeZoneByNitz(Phone phone, String str, int i, String str2);
}
