package com.android.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import android.provider.BlockedNumberContract;
import android.telephony.Rlog;

public class BlockChecker {
    private static final String TAG = "BlockChecker";
    private static final boolean VDBG = false;

    @Deprecated
    public static boolean isBlocked(Context context, String phoneNumber) {
        return isBlocked(context, phoneNumber, null);
    }

    public static boolean isBlocked(Context context, String phoneNumber, Bundle extras) {
        boolean isBlocked = false;
        long startTimeNano = System.nanoTime();
        try {
            if (BlockedNumberContract.SystemContract.shouldSystemBlockNumber(context, phoneNumber, extras)) {
                Rlog.d(TAG, "phone number is blocked.");
                isBlocked = true;
            }
        } catch (Exception e) {
            Rlog.e(TAG, "Exception checking for blocked number: " + e);
        }
        int durationMillis = (int) ((System.nanoTime() - startTimeNano) / 1000000);
        if (durationMillis > 500) {
            Rlog.d(TAG, "Blocked number lookup took: " + durationMillis + " ms.");
        }
        return isBlocked;
    }
}
