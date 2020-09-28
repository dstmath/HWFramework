package com.android.internal.telephony;

import com.huawei.internal.telephony.PhoneExt;

public interface HwReportManager {
    default void reportSetTimeZoneByNitz(PhoneExt phone, String zoneId, int tzOffset, String source) {
    }

    default void reportSetTimeZoneByIso(PhoneExt phone, String zoneId, boolean isNitzUpdatedTime, String source) {
    }

    default void reportNitzIgnore(int phoneId, String forbidden) {
    }

    default void reportMultiTZRegistered() {
    }

    default void reportMultiTZNoNitz() {
    }

    default void reportSetTimeZoneByLocation(String zoneId) {
    }
}
