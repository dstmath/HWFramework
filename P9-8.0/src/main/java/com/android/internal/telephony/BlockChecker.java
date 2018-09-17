package com.android.internal.telephony;

import android.content.Context;
import android.provider.BlockedNumberContract.SystemContract;
import android.telephony.Rlog;

public class BlockChecker {
    private static final String TAG = "BlockChecker";
    private static final boolean VDBG = false;

    public static boolean isBlocked(Context context, String phoneNumber) {
        boolean isBlocked = false;
        long startTimeNano = System.nanoTime();
        try {
            if (SystemContract.shouldSystemBlockNumber(context, phoneNumber)) {
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
