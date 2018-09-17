package com.android.internal.telephony;

import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.HashMap;
import java.util.List;

public class HwCustIccProviderUtilsImpl extends HwCustIccProviderUtils {
    private static final String DATA_FIXED_NUMBER = "*99#";
    private static final int FDN_EXISTS = 101;
    private static final int FDN_EXISTS_SUB = 102;
    private static final String FDN_NUM_VALUE = "exists";
    private static final boolean HWDBG = true;
    private static final String TAG = "HwCustIccProviderUtilsImpl";
    private boolean FDN_PRELOAD_CACHE = SystemProperties.getBoolean("ro.config.fdn.preload", HWDBG);
    private HashMap<String, String> fdnMap1 = new HashMap();
    private HashMap<String, String> fdnMap2 = new HashMap();
    private boolean isPSAllowedByFdn1 = false;
    private boolean isPSAllowedByFdn2 = false;

    public void addURI(UriMatcher uriMatcher) {
        uriMatcher.addURI("icc", "fdn/exits_query", FDN_EXISTS);
        uriMatcher.addURI("icc", "fdn/exits_query/subId/#", FDN_EXISTS_SUB);
    }

    /* JADX WARNING: Missing block: B:9:0x0017, code:
            if (r11.fdnMap1.isEmpty() == false) goto L_0x0019;
     */
    /* JADX WARNING: Missing block: B:13:0x0022, code:
            if (r11.fdnMap2.isEmpty() != false) goto L_0x0024;
     */
    /* JADX WARNING: Missing block: B:14:0x0024, code:
            r5 = com.android.internal.telephony.IIccPhoneBook.Stub.asInterface(android.os.ServiceManager.getService("simphonebook"));
     */
    /* JADX WARNING: Missing block: B:15:0x002f, code:
            if (r5 == null) goto L_0x004b;
     */
    /* JADX WARNING: Missing block: B:16:0x0031, code:
            r0 = r5.getAdnRecordsInEfForSubscriber(r6, 28475);
     */
    /* JADX WARNING: Missing block: B:17:0x0037, code:
            if (r0 == null) goto L_0x0049;
     */
    /* JADX WARNING: Missing block: B:18:0x0039, code:
            fdnCacheProcess(r0, 28475, (long) r6);
     */
    /* JADX WARNING: Missing block: B:24:0x004a, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:26:0x004c, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Cursor handleCustQuery(UriMatcher uriMatcher, Uri url, String[] selectionArgs, String[] addressColumns) {
        int subId;
        int urlMatchVal = uriMatcher.match(url);
        switch (urlMatchVal) {
            case FDN_EXISTS /*101*/:
                subId = SubscriptionManager.getDefaultSubId();
                break;
            case FDN_EXISTS_SUB /*102*/:
                subId = getRequestSubId(url);
                break;
            default:
                return null;
        }
        if (subId == 0) {
            try {
            } catch (RemoteException ex) {
                log(ex.toString());
                return null;
            } catch (SecurityException ex2) {
                log(ex2.toString());
                return null;
            }
        }
        if (subId == 1) {
        }
        switch (urlMatchVal) {
            case FDN_EXISTS /*101*/:
                Cursor matrixCursor;
                log("fddn FDN_EXISTS number: xxxx");
                if (this.fdnMap1.get(selectionArgs[0]) != null) {
                    matrixCursor = new MatrixCursor(addressColumns, 1);
                } else {
                    matrixCursor = null;
                }
                return matrixCursor;
            case FDN_EXISTS_SUB /*102*/:
                Cursor cursor = null;
                String exists = null;
                if (subId == 0) {
                    exists = (String) this.fdnMap1.get(selectionArgs[0]);
                    if (this.fdnMap1.get(selectionArgs[0]) != null) {
                        cursor = new MatrixCursor(addressColumns, 1);
                    } else {
                        cursor = null;
                    }
                } else if (subId == 1) {
                    exists = (String) this.fdnMap2.get(selectionArgs[0]);
                    if (this.fdnMap2.get(selectionArgs[0]) != null) {
                        cursor = new MatrixCursor(addressColumns, 1);
                    } else {
                        cursor = null;
                    }
                }
                log("fddn FDN_EXISTS_SUB subId:" + subId + " ,number: xxxx ,is exists:" + exists);
                return cursor;
            default:
                return null;
        }
    }

    public void fdnCacheProcess(List<AdnRecord> adnRecords, int efType, long subId) {
        if (adnRecords != null) {
            int N = adnRecords.size();
            log("fddn loadFromEf FDN_PRELOAD_CACHE:" + this.FDN_PRELOAD_CACHE + " ,subId" + subId + " ,efType:" + efType);
            fdnCacheReset(efType, subId);
            for (int i = 0; i < N; i++) {
                fdnCacheLoad(efType, ((AdnRecord) adnRecords.get(i)).getNumber(), subId);
            }
            fdnCacheLoaded(efType, subId);
        }
    }

    private void fdnCacheReset(int efType, long subId) {
        if (this.FDN_PRELOAD_CACHE && 28475 == efType) {
            log("fddn fdnCacheReset subId:" + subId);
            if (subId == 0) {
                this.isPSAllowedByFdn1 = false;
                this.fdnMap1.clear();
            } else if (subId == 1) {
                this.isPSAllowedByFdn2 = false;
                this.fdnMap2.clear();
            }
        }
    }

    private void fdnCacheLoad(int efType, String number, long subId) {
        if (this.FDN_PRELOAD_CACHE && 28475 == efType) {
            log("fddn fdnCacheLoad number: xxxx ,subId:" + subId);
            if (subId == 0) {
                this.fdnMap1.put(number, FDN_NUM_VALUE);
                if (DATA_FIXED_NUMBER.equals(number)) {
                    this.isPSAllowedByFdn1 = HWDBG;
                    log("fddn fdnCacheLoad data fixed number found in card " + subId);
                }
            } else if (subId == 1) {
                this.fdnMap2.put(number, FDN_NUM_VALUE);
                if (DATA_FIXED_NUMBER.equals(number)) {
                    this.isPSAllowedByFdn2 = HWDBG;
                    log("fddn fdnCacheLoad data fixed number found in card " + subId);
                }
            }
        }
    }

    private void fdnCacheLoaded(int efType, long subId) {
        if (this.FDN_PRELOAD_CACHE && 28475 == efType) {
            log("fddn fdnCacheLoaded subId:" + subId + " ,isPSAllowedByFdn1:" + this.isPSAllowedByFdn1 + " ,isPSAllowedByFdn2:" + this.isPSAllowedByFdn2);
            if (subId == 0 && this.isPSAllowedByFdn1) {
                SystemProperties.set(HwCustTelephonyProperties.PROPERTY_FDN_PS_FLAG_EXISTS_SUB1, "true");
            } else if (subId == 0 && (this.isPSAllowedByFdn1 ^ 1) != 0) {
                SystemProperties.set(HwCustTelephonyProperties.PROPERTY_FDN_PS_FLAG_EXISTS_SUB1, "false");
            } else if (subId == 1 && this.isPSAllowedByFdn2) {
                SystemProperties.set(HwCustTelephonyProperties.PROPERTY_FDN_PS_FLAG_EXISTS_SUB2, "true");
            } else if (subId == 1 && (this.isPSAllowedByFdn2 ^ 1) != 0) {
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
