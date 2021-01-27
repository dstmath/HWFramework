package com.android.internal.telephony;

import android.content.ContentValues;

public interface IHwSubscriptionInfoUpdaterEx {
    default void updateSubIdForNV(int slotId) {
    }

    default void updateSubActivation(int slotId) {
    }

    default void broadcastSubinfoRecordUpdated(String[] iccId) {
    }

    default void putExtraValueForEuicc(ContentValues values, String providerName, int state) {
    }

    default void recordSimStateBySlotId(int slotId) {
    }

    default String padTrailingFs(String iccId) {
        return iccId;
    }
}
