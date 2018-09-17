package com.huawei.android.telephony;

import android.content.Context;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import com.huawei.android.util.NoExtAPIException;

public class MSimTelephonyManagerCustEx {
    private static final int preferredNetworkMode = 9;

    public static String getPesn(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getUserDefaultSubscription(Context context) {
        return System.getInt(context.getContentResolver(), "switch_dual_card_slots", 0);
    }

    public static int getNetworkmode(Context context, int mSubscription) {
        return Global.getInt(context.getContentResolver(), "preferred_network_mode", preferredNetworkMode);
    }

    public static void setNetworkmode(Context context, int mSubScription, int networkMode) {
        Global.putInt(context.getContentResolver(), "preferred_network_mode", networkMode);
    }
}
