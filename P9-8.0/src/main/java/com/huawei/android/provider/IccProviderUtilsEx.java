package com.huawei.android.provider;

import android.net.Uri;
import android.telephony.MSimTelephonyConstants;
import java.util.HashMap;

public final class IccProviderUtilsEx {
    private static HashMap<String, Integer> indexColumn = new HashMap();
    private static HashMap<String, String> simAnr = new HashMap();
    private static HashMap<String, Uri> simProviderUri = new HashMap();
    private static HashMap<String, Uri> usimProviderUri = new HashMap();

    public static final HashMap getIndexColumn() {
        indexColumn.put("INDEX_NAME_COLUMN", Integer.valueOf(0));
        indexColumn.put("INDEX_NUMBER_COLUMN", Integer.valueOf(1));
        indexColumn.put("INDEX_EMAILS_COLUMN", Integer.valueOf(2));
        indexColumn.put("INDEX_EFID_COLUMN", Integer.valueOf(3));
        indexColumn.put("INDEX_SIM_INDEX_COLUMN", Integer.valueOf(4));
        indexColumn.put("INDEX_ANRS_COLUMN", Integer.valueOf(5));
        return indexColumn;
    }

    public static final HashMap getSimProviderUri() {
        simProviderUri.put("sFirstSimProviderUri", Uri.parse("content://icc/adn/subId/0"));
        simProviderUri.put("sSecondSimProviderUri", Uri.parse("content://icc/adn/subId/1"));
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
