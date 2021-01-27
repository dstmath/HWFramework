package com.android.internal.telephony;

import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import com.huawei.internal.telephony.uicc.UiccPhoneBookControllerExt;
import java.util.HashMap;
import java.util.List;

public class HwCustIccProviderUtilsImpl extends HwCustIccProviderUtils {
    private static final String DATA_FIXED_NUMBER = "*99#";
    private static final int FDN_EXISTS = 101;
    private static final int FDN_EXISTS_SUB = 102;
    private static final String FDN_NUM_VALUE = "exists";
    private static final boolean HWDBG = true;
    private static final int SLOT0 = 0;
    private static final int SLOT1 = 1;
    private static final String TAG = "HwCustIccProviderUtilsImpl";
    private boolean FDN_PRELOAD_CACHE = SystemProperties.getBoolean("ro.config.fdn.preload", (boolean) HWDBG);
    private HashMap<String, String> fdnMap1 = new HashMap<>();
    private HashMap<String, String> fdnMap2 = new HashMap<>();
    private boolean isPSAllowedByFdn1 = false;
    private boolean isPSAllowedByFdn2 = false;

    public void addURI(UriMatcher uriMatcher) {
        uriMatcher.addURI("icc", "fdn/exits_query", FDN_EXISTS);
        uriMatcher.addURI("icc", "fdn/exits_query/subId/#", FDN_EXISTS_SUB);
    }

    public Cursor handleCustQuery(UriMatcher uriMatcher, Uri url, String[] selectionArgs, String[] addressColumns) {
        int subId;
        int slotId;
        int urlMatchVal = uriMatcher.match(url);
        Cursor cursor = null;
        if (urlMatchVal == FDN_EXISTS) {
            subId = SubscriptionManager.getDefaultSubId();
        } else if (urlMatchVal != FDN_EXISTS_SUB) {
            return null;
        } else {
            subId = getRequestSubId(url);
        }
        SubscriptionController subscriptionController = SubscriptionController.getInstance();
        if (subscriptionController == null || (slotId = subscriptionController.getSlotIndex(subId)) == -1) {
            return null;
        }
        if ((slotId == 0 && this.fdnMap1.isEmpty()) || (slotId == 1 && this.fdnMap2.isEmpty())) {
            List<AdnRecordExt> adnRecords = UiccPhoneBookControllerExt.getAdnRecordsInEfForSubscriberHw(subId, 28475);
            if (adnRecords == null) {
                return null;
            }
            fdnCacheProcess(adnRecords, 28475, (long) subId);
        }
        if (urlMatchVal == FDN_EXISTS) {
            log("fddn FDN_EXISTS number: xxxx");
            if (this.fdnMap1.get(selectionArgs[0]) != null) {
                return new MatrixCursor(addressColumns, 1);
            }
            return null;
        } else if (urlMatchVal != FDN_EXISTS_SUB) {
            return null;
        } else {
            Cursor cursor2 = null;
            String exists = null;
            if (slotId == 0) {
                exists = this.fdnMap1.get(selectionArgs[0]);
                if (this.fdnMap1.get(selectionArgs[0]) != null) {
                    cursor = new MatrixCursor(addressColumns, 1);
                }
                cursor2 = cursor;
            } else if (slotId == 1) {
                exists = this.fdnMap2.get(selectionArgs[0]);
                if (this.fdnMap2.get(selectionArgs[0]) != null) {
                    cursor = new MatrixCursor(addressColumns, 1);
                }
                cursor2 = cursor;
            }
            log("fddn FDN_EXISTS_SUB slotId:" + slotId + " ,number: xxxx ,is exists:" + exists);
            return cursor2;
        }
    }

    public void fdnCacheProcess(List<AdnRecordExt> adnRecords, int efType, long subId) {
        int slotId;
        SubscriptionController subscriptionController = SubscriptionController.getInstance();
        if (!(subscriptionController == null || (slotId = subscriptionController.getSlotIndex((int) subId)) == -1 || adnRecords == null)) {
            int N = adnRecords.size();
            log("fddn loadFromEf FDN_PRELOAD_CACHE:" + this.FDN_PRELOAD_CACHE + " ,slotId" + slotId + " ,efType:" + efType);
            fdnCacheReset(efType, slotId);
            for (int i = 0; i < N; i++) {
                fdnCacheLoad(efType, adnRecords.get(i).getNumber(), slotId);
            }
            fdnCacheLoaded(efType, slotId);
        }
    }

    private void fdnCacheReset(int efType, int slotId) {
        if (this.FDN_PRELOAD_CACHE && 28475 == efType) {
            log("fddn fdnCacheReset slotId:" + slotId);
            if (slotId == 0) {
                this.isPSAllowedByFdn1 = false;
                this.fdnMap1.clear();
            } else if (slotId == 1) {
                this.isPSAllowedByFdn2 = false;
                this.fdnMap2.clear();
            }
        }
    }

    private void fdnCacheLoad(int efType, String number, int slotId) {
        if (this.FDN_PRELOAD_CACHE && 28475 == efType) {
            log("fddn fdnCacheLoad number: xxxx ,slotId:" + slotId);
            if (slotId == 0) {
                this.fdnMap1.put(number, FDN_NUM_VALUE);
                if (DATA_FIXED_NUMBER.equals(number)) {
                    this.isPSAllowedByFdn1 = HWDBG;
                    log("fddn fdnCacheLoad data fixed number found in card " + slotId);
                }
            } else if (slotId == 1) {
                this.fdnMap2.put(number, FDN_NUM_VALUE);
                if (DATA_FIXED_NUMBER.equals(number)) {
                    this.isPSAllowedByFdn2 = HWDBG;
                    log("fddn fdnCacheLoad data fixed number found in card " + slotId);
                }
            }
        }
    }

    private void fdnCacheLoaded(int efType, int slotId) {
        if (this.FDN_PRELOAD_CACHE && 28475 == efType) {
            log("fddn fdnCacheLoaded slotId:" + slotId + " ,isPSAllowedByFdn1:" + this.isPSAllowedByFdn1 + " ,isPSAllowedByFdn2:" + this.isPSAllowedByFdn2);
            if (slotId == 0 && this.isPSAllowedByFdn1) {
                SystemProperties.set(HwCustTelephonyProperties.PROPERTY_FDN_PS_FLAG_EXISTS_SUB1, "true");
            } else if (slotId == 0 && !this.isPSAllowedByFdn1) {
                SystemProperties.set(HwCustTelephonyProperties.PROPERTY_FDN_PS_FLAG_EXISTS_SUB1, "false");
            } else if (slotId == 1 && this.isPSAllowedByFdn2) {
                SystemProperties.set(HwCustTelephonyProperties.PROPERTY_FDN_PS_FLAG_EXISTS_SUB2, "true");
            } else if (slotId == 1 && !this.isPSAllowedByFdn2) {
                SystemProperties.set(HwCustTelephonyProperties.PROPERTY_FDN_PS_FLAG_EXISTS_SUB2, "false");
            }
        }
    }

    private int getRequestSubId(Uri url) {
        try {
            return Integer.parseInt(url.getLastPathSegment());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    private void log(String message) {
        Rlog.d(TAG, message);
    }
}
