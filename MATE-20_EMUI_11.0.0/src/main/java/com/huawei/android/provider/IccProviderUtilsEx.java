package com.huawei.android.provider;

import android.net.Uri;
import android.telephony.MSimTelephonyConstants;
import android.telephony.SubscriptionManager;
import java.util.HashMap;

public final class IccProviderUtilsEx {
    private static HashMap<String, Integer> indexColumn = new HashMap<>();
    private static HashMap<String, String> simAnr = new HashMap<>();
    private static HashMap<String, Uri> simProviderUri = new HashMap<>();
    private static HashMap<String, Uri> usimProviderUri = new HashMap<>();

    public static final HashMap getIndexColumn() {
        indexColumn.put("INDEX_NAME_COLUMN", 0);
        indexColumn.put("INDEX_NUMBER_COLUMN", 1);
        indexColumn.put("INDEX_EMAILS_COLUMN", 2);
        indexColumn.put("INDEX_EFID_COLUMN", 3);
        indexColumn.put("INDEX_SIM_INDEX_COLUMN", 4);
        indexColumn.put("INDEX_ANRS_COLUMN", 5);
        return indexColumn;
    }

    public static final HashMap getSimProviderUri() {
        int[] subId = SubscriptionManager.getSubId(0);
        if (subId != null && SubscriptionManager.isValidSubscriptionId(subId[0])) {
            HashMap<String, Uri> hashMap = simProviderUri;
            hashMap.put("sFirstSimProviderUri", Uri.parse("content://icc/adn/subId/" + subId[0]));
        }
        int[] subId2 = SubscriptionManager.getSubId(1);
        if (subId2 != null && SubscriptionManager.isValidSubscriptionId(subId2[0])) {
            HashMap<String, Uri> hashMap2 = simProviderUri;
            hashMap2.put("sSecondSimProviderUri", Uri.parse("content://icc/adn/subId/" + subId2[0]));
        }
        return simProviderUri;
    }

    public static final HashMap getUSimProviderUri() {
        usimProviderUri.put("sSingleUSimProviderUri", null);
        usimProviderUri.put("sFirstUSimProviderUri", null);
        usimProviderUri.put("sSecondUSimProviderUri", null);
        return usimProviderUri;
    }

    public static final HashMap getSimAnr() {
        simAnr.put("SIM_ANR", MSimTelephonyConstants.SIM_PHONEBOOK_COLIMN_NAME_ANR);
        simAnr.put("SIM_NEW_ANR", MSimTelephonyConstants.SIM_PHONEBOOK_COLIMN_NAME_NEW_ANR);
        return simAnr;
    }
}
