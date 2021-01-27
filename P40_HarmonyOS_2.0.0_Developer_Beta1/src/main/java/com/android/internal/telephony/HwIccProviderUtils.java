package com.android.internal.telephony;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.HwTelephony;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.uicc.HwAdnRecordCacheEx;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccPhoneBookControllerExt;
import huawei.cust.HwCustUtils;
import java.util.Arrays;
import java.util.List;

public class HwIccProviderUtils {
    static final String[] ADDRESS_BOOK_COLUMN_NAMES = {HwTelephony.NumMatchs.NAME, STR_NUMBER, STR_EMAILS, STR_EFID, STR_INDEX, "_id"};
    static final String[] ADDRESS_BOOK_COLUMN_NAMES_USIM = {HwTelephony.NumMatchs.NAME, STR_NUMBER, STR_EMAILS, STR_EFID, STR_INDEX, STR_ANRS, "_id"};
    private static final int ADN = 1;
    private static final int ADN_ALL = 7;
    private static final int ADN_SUB = 2;
    private static final boolean DBG = false;
    private static final int FDN = 3;
    private static final int FDN_SUB = 4;
    private static final int SDN = 5;
    private static final int SDN_SUB = 6;
    static final String STR_ANRS = "anrs";
    static final String STR_EFID = "efid";
    static final String STR_EMAILS = "emails";
    static final String STR_INDEX = "index";
    static final String STR_NEW_ANRS = "newAnrs";
    static final String STR_NEW_EMAILS = "newEmails";
    static final String STR_NEW_NUMBER = "newNumber";
    static final String STR_NEW_TAG = "newTag";
    static final String STR_NUMBER = "number";
    static final String STR_PIN2 = "pin2";
    static final String STR_TAG = "tag";
    private static final String TAG = "HwIccProviderUtils";
    private static final UriMatcher URL_MATCHER = new UriMatcher(-1);
    private static volatile HwIccProviderUtils instance;
    protected Context mContext = null;
    private HwCustIccProviderUtils mCust = null;

    static {
        URL_MATCHER.addURI("icc", "adn", 1);
        URL_MATCHER.addURI("icc", "adn/subId/#", 2);
        URL_MATCHER.addURI("icc", "fdn", 3);
        URL_MATCHER.addURI("icc", "fdn/subId/#", 4);
        URL_MATCHER.addURI("icc", "sdn", 5);
        URL_MATCHER.addURI("icc", "sdn/subId/#", 6);
        URL_MATCHER.addURI("icc", "adn/adn_all", 7);
    }

    protected HwIccProviderUtils(Context context) {
        this.mContext = context;
        this.mCust = (HwCustIccProviderUtils) HwCustUtils.createObj(HwCustIccProviderUtils.class, new Object[0]);
        HwCustIccProviderUtils hwCustIccProviderUtils = this.mCust;
        if (hwCustIccProviderUtils != null) {
            hwCustIccProviderUtils.addURI(URL_MATCHER);
        }
    }

    public static HwIccProviderUtils getDefault(Context context) {
        if (instance == null) {
            instance = new HwIccProviderUtils(context);
        }
        return instance;
    }

    private static void log(String msg) {
        RlogEx.i(TAG, "[IccProvider] " + msg);
    }

    private static void logd(String msg) {
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mContext;
    }

    public boolean isHwSimPhonebookEnabled() {
        return true;
    }

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        logd("query");
        boolean isQuerybyindex = false;
        AdnRecordExt searchAdn = new AdnRecordExt(BuildConfig.FLAVOR, BuildConfig.FLAVOR);
        if (selection != null) {
            String[] parameters = initParameters(selection, true);
            String tag = parameters[0];
            String number = parameters[1];
            String strEfid = parameters[5];
            String strIndex = parameters[6];
            int efid = 0;
            int index = 0;
            if (!(strEfid == null || strIndex == null)) {
                try {
                    efid = Integer.parseInt(strEfid);
                    index = Integer.parseInt(strIndex);
                } catch (NumberFormatException e) {
                    log("NumberFormat Exception");
                }
                isQuerybyindex = true;
            }
            searchAdn = new AdnRecordExt(efid, index, tag, number);
        }
        HwCustIccProviderUtils hwCustIccProviderUtils = this.mCust;
        if (hwCustIccProviderUtils != null) {
            Cursor cursor = hwCustIccProviderUtils.handleCustQuery(URL_MATCHER, url, selectionArgs, ADDRESS_BOOK_COLUMN_NAMES);
            if (cursor != null) {
                return cursor;
            }
        }
        switch (URL_MATCHER.match(url)) {
            case 1:
                if (isQuerybyindex) {
                    return loadFromEf(28474, searchAdn, SubscriptionManager.getDefaultSubscriptionId());
                }
                return loadFromEf(28474, SubscriptionManager.getDefaultSubscriptionId());
            case 2:
                if (isQuerybyindex) {
                    return loadFromEf(28474, searchAdn, getRequestSubId(url));
                }
                return loadFromEf(28474, getRequestSubId(url));
            case 3:
                return loadFromEf(28475, SubscriptionManager.getDefaultSubscriptionId());
            case 4:
                return loadFromEf(28475, getRequestSubId(url));
            case 5:
                return loadFromEf(28489, SubscriptionManager.getDefaultSubscriptionId());
            case 6:
                return loadFromEf(28489, getRequestSubId(url));
            case 7:
                return loadAllSimContacts(28474);
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    private Cursor loadAllSimContacts(int efType) {
        Cursor[] result;
        List<SubscriptionInfo> subInfoList = SubscriptionControllerEx.getInstance().getActiveSubscriptionInfoList("com.android.phone");
        if (subInfoList == null || subInfoList.size() == 0) {
            result = new Cursor[0];
        } else {
            int subIdCount = subInfoList.size();
            result = new Cursor[subIdCount];
            for (int i = 0; i < subIdCount; i++) {
                int subId = subInfoList.get(i).getSubscriptionId();
                result[i] = loadFromEf(efType, subId);
                log("ADN Records loaded for Subscription ::" + subId);
            }
        }
        return new MergeCursor(result);
    }

    public String getType(Uri url) {
        switch (URL_MATCHER.match(url)) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                return "vnd.android.cursor.dir/sim-contact";
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    public Uri insert(Uri url, ContentValues initialValues) {
        int subId;
        int efType;
        boolean success;
        String pin2 = null;
        logd("insert");
        if (url != null) {
            if (initialValues != null) {
                int match = URL_MATCHER.match(url);
                if (match == 1) {
                    efType = 28474;
                    subId = SubscriptionManager.getDefaultSubscriptionId();
                } else if (match == 2) {
                    efType = 28474;
                    subId = getRequestSubId(url);
                } else if (match == 3) {
                    efType = 28475;
                    subId = SubscriptionManager.getDefaultSubscriptionId();
                    pin2 = initialValues.getAsString(STR_PIN2);
                } else if (match == 4) {
                    efType = 28475;
                    subId = getRequestSubId(url);
                    pin2 = initialValues.getAsString(STR_PIN2);
                } else {
                    throw new UnsupportedOperationException("Cannot insert into URL: " + url);
                }
                String tag = initialValues.getAsString(STR_TAG);
                String tag2 = tag == null ? BuildConfig.FLAVOR : tag;
                String number = initialValues.getAsString(STR_NUMBER);
                String number2 = number == null ? BuildConfig.FLAVOR : number;
                String emails = initialValues.getAsString(STR_EMAILS);
                String emails2 = emails == null ? BuildConfig.FLAVOR : emails;
                String anrs = initialValues.getAsString(STR_ANRS);
                String anrs2 = anrs == null ? BuildConfig.FLAVOR : anrs;
                ContentValues mValues = new ContentValues();
                mValues.put(STR_TAG, BuildConfig.FLAVOR);
                mValues.put(STR_NUMBER, BuildConfig.FLAVOR);
                mValues.put(STR_EMAILS, BuildConfig.FLAVOR);
                mValues.put(STR_ANRS, BuildConfig.FLAVOR);
                mValues.put(STR_NEW_TAG, tag2);
                mValues.put(STR_NEW_NUMBER, number2);
                mValues.put(STR_NEW_EMAILS, emails2);
                mValues.put(STR_NEW_ANRS, anrs2);
                if (IccRecordsEx.getEmailAnrSupport()) {
                    success = updateIccRecordInEf(efType, mValues, pin2, subId);
                } else {
                    success = addIccRecordToEf(efType, tag2, number2, null, pin2, subId);
                }
                if (!success) {
                    return null;
                }
                StringBuilder buf = new StringBuilder("content://icc/");
                if (match == 1) {
                    buf.append("adn/");
                } else if (match == 2) {
                    buf.append("adn/subId/");
                } else if (match == 3) {
                    buf.append("fdn/");
                } else if (match == 4) {
                    buf.append("fdn/subId/");
                }
                buf.append(HwAdnRecordCacheEx.UPDATE_ADN_RECORD_INDEX.get());
                buf.append("/");
                buf.append(HwAdnRecordCacheEx.UPDATE_ADN_RECORD_EFID.get());
                logd("returned string:" + buf.toString());
                Uri resultUri = Uri.parse(buf.toString());
                getContext().getContentResolver().notifyChange(url, null);
                return resultUri;
            }
        }
        Log.e(TAG, "insert: para error!");
        return null;
    }

    /* access modifiers changed from: protected */
    public String normalizeValue(String inVal) {
        int len = inVal.length();
        if (len == 0) {
            logd("len of input String is 0");
            return inVal;
        } else if (inVal.charAt(0) == '\'' && inVal.charAt(len - 1) == '\'') {
            return inVal.substring(1, len - 1);
        } else {
            return inVal;
        }
    }

    /* JADX INFO: Multiple debug info for r12v1 java.lang.String: [D('match' int), D('strEfid' java.lang.String)] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0181 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0182  */
    public int delete(Uri url, String where, String[] whereArgs) {
        int subId;
        int efType;
        int i;
        boolean z;
        ContentValues mValues;
        boolean z2;
        logd("delete");
        int match = URL_MATCHER.match(url);
        if (match == 1) {
            efType = 28474;
            subId = SubscriptionManager.getDefaultSubscriptionId();
        } else if (match == 2) {
            efType = 28474;
            subId = getRequestSubId(url);
        } else if (match == 3) {
            efType = 28475;
            subId = SubscriptionManager.getDefaultSubscriptionId();
        } else if (match == 4) {
            efType = 28475;
            subId = getRequestSubId(url);
        } else {
            throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        int index = 0;
        boolean success = false;
        String[] parameters = initParameters(where, false);
        String tag = parameters[0];
        String number = parameters[1];
        String emails = parameters[2];
        String anrs = parameters[3];
        ContentValues mValues2 = new ContentValues();
        mValues2.put(STR_TAG, tag);
        mValues2.put(STR_NUMBER, number);
        mValues2.put(STR_EMAILS, emails);
        mValues2.put(STR_ANRS, anrs);
        mValues2.put(STR_NEW_TAG, BuildConfig.FLAVOR);
        mValues2.put(STR_NEW_NUMBER, BuildConfig.FLAVOR);
        mValues2.put(STR_NEW_EMAILS, BuildConfig.FLAVOR);
        mValues2.put(STR_NEW_ANRS, BuildConfig.FLAVOR);
        String pin2 = parameters[4];
        if (efType == 28475 && TextUtils.isEmpty(pin2)) {
            return 0;
        }
        String strEfid = parameters[5];
        String strIndex = parameters[6];
        if ((strEfid == null || strEfid.equals(BuildConfig.FLAVOR)) && (strIndex == null || strIndex.equals(BuildConfig.FLAVOR))) {
            if (!IccRecordsEx.getEmailAnrSupport()) {
                mValues = mValues2;
                z2 = deleteIccRecordFromEf(efType, tag, number, null, pin2, subId);
            } else {
                mValues = mValues2;
                z2 = updateIccRecordInEf(efType, mValues, pin2, subId);
            }
            success = z2;
            i = 0;
        } else {
            try {
                int orginEfType = Integer.parseInt(strEfid);
                int index2 = Integer.parseInt(strIndex);
                if (index2 > 0) {
                    try {
                        if (!IccRecordsEx.getEmailAnrSupport()) {
                            try {
                                z = deleteIccRecordFromEfByIndex(orginEfType, index2, null, pin2, subId);
                                i = 0;
                            } catch (NumberFormatException e) {
                                index = index2;
                                i = 0;
                                log("NumberFormat Exception");
                                logd("strEfid=" + strEfid + ";strIndex=" + strIndex + ";efType=" + efType + ";index" + index);
                                if (!success) {
                                }
                            }
                        } else {
                            i = 0;
                            try {
                                z = deleteUsimRecordFromEfByIndex(efType, orginEfType, index2, null, pin2, subId);
                            } catch (NumberFormatException e2) {
                                index = index2;
                                log("NumberFormat Exception");
                                logd("strEfid=" + strEfid + ";strIndex=" + strIndex + ";efType=" + efType + ";index" + index);
                                if (!success) {
                                }
                            }
                        }
                        success = z;
                    } catch (NumberFormatException e3) {
                        i = 0;
                        index = index2;
                        log("NumberFormat Exception");
                        logd("strEfid=" + strEfid + ";strIndex=" + strIndex + ";efType=" + efType + ";index" + index);
                        if (!success) {
                        }
                    }
                } else {
                    i = 0;
                }
                index = index2;
            } catch (NumberFormatException e4) {
                i = 0;
                log("NumberFormat Exception");
                logd("strEfid=" + strEfid + ";strIndex=" + strIndex + ";efType=" + efType + ";index" + index);
                if (!success) {
                }
            }
        }
        logd("strEfid=" + strEfid + ";strIndex=" + strIndex + ";efType=" + efType + ";index" + index);
        if (!success) {
            return i;
        }
        getContext().getContentResolver().notifyChange(url, null);
        return 1;
    }

    private String[] initParameters(String where, boolean isQuery) {
        String number;
        String tag;
        if (isQuery) {
            tag = BuildConfig.FLAVOR;
            number = BuildConfig.FLAVOR;
        } else {
            tag = null;
            number = null;
        }
        String emails = null;
        String anrs = null;
        String pin2 = null;
        String strEfid = null;
        String strIndex = null;
        String[] tokens = where.split("AND");
        int n = tokens.length;
        while (true) {
            n--;
            if (n < 0) {
                return new String[]{tag, number, emails, anrs, pin2, strEfid, strIndex};
            }
            String param = tokens[n];
            logd("parsing '" + param + "'");
            String[] pair = param.split("=");
            if (pair.length != 2) {
                RlogEx.e(TAG, "resolve: bad whereClause parameter: " + param);
            } else {
                String key = pair[0].trim();
                String val = pair[1].trim();
                if (STR_TAG.equals(key)) {
                    tag = normalizeValue(val);
                } else if (STR_NUMBER.equals(key)) {
                    number = normalizeValue(val);
                } else if (STR_EMAILS.equals(key)) {
                    emails = normalizeValue(val);
                } else if (STR_ANRS.equals(key)) {
                    anrs = normalizeValue(val);
                } else if (STR_PIN2.equals(key)) {
                    pin2 = normalizeValue(val);
                } else if (STR_EFID.equals(key)) {
                    strEfid = normalizeValue(val);
                } else if (STR_INDEX.equals(key)) {
                    strIndex = normalizeValue(val);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x018c A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x018d  */
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int subId;
        String pin2;
        int efType;
        String Efid;
        String strIndex;
        int index;
        boolean z;
        boolean z2;
        logd("update");
        if (url != null) {
            if (values != null) {
                int match = URL_MATCHER.match(url);
                if (match == 1) {
                    efType = 28474;
                    subId = SubscriptionManager.getDefaultSubscriptionId();
                    pin2 = null;
                } else if (match == 2) {
                    efType = 28474;
                    subId = getRequestSubId(url);
                    pin2 = null;
                } else if (match == 3) {
                    efType = 28475;
                    subId = SubscriptionManager.getDefaultSubscriptionId();
                    pin2 = values.getAsString(STR_PIN2);
                } else if (match == 4) {
                    efType = 28475;
                    subId = getRequestSubId(url);
                    pin2 = values.getAsString(STR_PIN2);
                } else {
                    throw new UnsupportedOperationException("Cannot insert into URL: " + url);
                }
                String tag = values.getAsString(STR_TAG);
                String number = values.getAsString(STR_NUMBER);
                String newTag = values.getAsString(STR_NEW_TAG) == null ? BuildConfig.FLAVOR : values.getAsString(STR_NEW_TAG);
                String newNumber = values.getAsString(STR_NEW_NUMBER) == null ? BuildConfig.FLAVOR : values.getAsString(STR_NEW_NUMBER);
                String[] newEmails = values.getAsString(STR_NEW_EMAILS) != null ? new String[]{values.getAsString(STR_NEW_EMAILS)} : null;
                String[] newAnrs = values.getAsString(STR_NEW_ANRS) != null ? new String[]{values.getAsString(STR_NEW_ANRS)} : null;
                String Efid2 = values.getAsString(STR_EFID);
                String strIndex2 = values.getAsString(STR_INDEX);
                int index2 = 0;
                boolean success = false;
                if ((Efid2 == null || Efid2.equals(BuildConfig.FLAVOR)) && (strIndex2 == null || strIndex2.equals(BuildConfig.FLAVOR))) {
                    if (!IccRecordsEx.getEmailAnrSupport()) {
                        strIndex = strIndex2;
                        Efid = Efid2;
                        z2 = updateIccRecordInEf(efType, tag, number, newTag, newNumber, pin2, subId);
                    } else {
                        strIndex = strIndex2;
                        Efid = Efid2;
                        z2 = updateIccRecordInEf(efType, values, pin2, subId);
                    }
                    success = z2;
                    index = 0;
                } else {
                    strIndex = strIndex2;
                    Efid = Efid2;
                    try {
                        int orginEfType = Integer.parseInt(Efid);
                        index = Integer.parseInt(strIndex);
                        if (index > 0) {
                            try {
                                if (!IccRecordsEx.getEmailAnrSupport()) {
                                    z = updateIccRecordInEfByIndex(orginEfType, index, newTag, newNumber, pin2, subId);
                                } else {
                                    z = updateUsimRecordInEfByIndex(efType, orginEfType, index, newTag, newNumber, newEmails, newAnrs, pin2, subId);
                                }
                                success = z;
                            } catch (NumberFormatException e) {
                                index2 = index;
                                log("NumberFormat Exception");
                                index = index2;
                                logd("update: Efid=" + Efid + ";strIndex=" + strIndex + ";efType=" + efType + ";index =" + index);
                                if (!success) {
                                }
                            }
                        }
                    } catch (NumberFormatException e2) {
                        log("NumberFormat Exception");
                        index = index2;
                        logd("update: Efid=" + Efid + ";strIndex=" + strIndex + ";efType=" + efType + ";index =" + index);
                        if (!success) {
                        }
                    }
                }
                logd("update: Efid=" + Efid + ";strIndex=" + strIndex + ";efType=" + efType + ";index =" + index);
                if (!success) {
                    return 0;
                }
                getContext().getContentResolver().notifyChange(url, null);
                return 1;
            }
        }
        Log.e(TAG, "insert: para error!");
        return 0;
    }

    private MatrixCursor loadFromEf(int efType, int subId) {
        logd("loadFromEf: efType=" + efType + ", subscription=" + subId);
        List<AdnRecordExt> adnRecords = UiccPhoneBookControllerExt.getAdnRecordsInEfForSubscriberHw(efType, subId);
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(IccRecordsEx.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES, N);
            HwCustIccProviderUtils hwCustIccProviderUtils = this.mCust;
            if (hwCustIccProviderUtils != null) {
                hwCustIccProviderUtils.fdnCacheProcess(adnRecords, efType, (long) subId);
            }
            logd("adnRecords.size=" + N);
            for (int i = 0; i < N; i++) {
                loadRecord(adnRecords.get(i), cursor, i);
            }
            return cursor;
        }
        RlogEx.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(IccRecordsEx.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES);
    }

    private MatrixCursor loadFromEf(int efType, AdnRecordExt searchAdn, int subId) {
        logd("loadFromEf: efType=" + efType + ", subscription=" + subId);
        List<AdnRecordExt> adnRecords = UiccPhoneBookControllerExt.getAdnRecordsInEfForSubscriberHw(efType, subId);
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(IccRecordsEx.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES, N);
            logd("adnRecords.size=" + N);
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                } else if (HwIccUtils.equalAdn(searchAdn, adnRecords.get(i))) {
                    log("have one by efid and index");
                    loadRecord(adnRecords.get(i), cursor, i);
                    break;
                } else {
                    i++;
                }
            }
            logd("loadFromEf: return results");
            return cursor;
        }
        RlogEx.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(IccRecordsEx.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES);
    }

    private boolean addIccRecordToEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        logd("addIccRecordToEf: efType=" + efType + ", name=" + name + ", number=" + number + ", emails=" + Arrays.toString(emails) + ", subscription=" + subId);
        boolean success = UiccPhoneBookControllerExt.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, BuildConfig.FLAVOR, BuildConfig.FLAVOR, name, number, pin2);
        StringBuilder sb = new StringBuilder();
        sb.append("addIccRecordToEf: ");
        sb.append(success);
        logd(sb.toString());
        return success;
    }

    private boolean updateIccRecordInEf(int efType, String oldName, String oldNumber, String newName, String newNumber, String pin2, int subId) {
        logd("updateIccRecordInEf: efType=" + efType + ", oldname=" + oldName + ", oldnumber=" + oldNumber + ", newname=" + newName + ", newnumber=" + newNumber + ", subscription=" + subId);
        boolean success = UiccPhoneBookControllerExt.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, oldName, oldNumber, newName, newNumber, pin2);
        StringBuilder sb = new StringBuilder();
        sb.append("updateIccRecordInEf: ");
        sb.append(success);
        logd(sb.toString());
        return success;
    }

    private boolean updateIccRecordInEfByIndex(int efType, int index, String newName, String newNumber, String pin2, int subId) {
        logd("updateIccRecordInEfByIndex: efType=" + efType + ", index=" + index + ", newname=" + newName + ", newnumber=" + newNumber + ", subscription=" + subId);
        boolean success = UiccPhoneBookControllerExt.updateAdnRecordsInEfByIndexForSubscriber(subId, efType, newName, newNumber, index, pin2);
        StringBuilder sb = new StringBuilder();
        sb.append("updateIccRecordInEfByIndex: ");
        sb.append(success);
        logd(sb.toString());
        return success;
    }

    private boolean updateUsimRecordInEfByIndex(int efType, int orginEfType, int index, String newName, String newNumber, String[] newEmails, String[] newAnrs, String pin2, int subId) {
        StringBuilder sb = new StringBuilder();
        sb.append("updateUsimRecordInEfByIndex: efType=");
        sb.append(efType);
        sb.append(", orginEfType=");
        sb.append(orginEfType);
        sb.append(", index=");
        sb.append(index);
        sb.append(", newname=");
        sb.append(newName);
        sb.append(", newnumber=");
        sb.append(newNumber);
        sb.append(", newEmails=");
        String str = null;
        sb.append(newEmails != null ? newEmails[0] : null);
        sb.append(", newAnrs=");
        if (newAnrs != null) {
            str = newAnrs[0];
        }
        sb.append(str);
        sb.append(", subscription=");
        sb.append(subId);
        logd(sb.toString());
        boolean success = UiccPhoneBookControllerExt.updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efType, newName, newNumber, newEmails, newAnrs, orginEfType, index, pin2);
        logd("updateUsimRecordInEfByIndex: " + success);
        return success;
    }

    private boolean deleteUsimRecordFromEfByIndex(int efType, int orginEfType, int index, String[] emails, String pin2, int subId) {
        logd("deleteUsimRecordFromEfByIndex: efType=" + efType + ", orginEfType=" + orginEfType + ", index=" + index + ", emails=" + Arrays.toString(emails) + ", pin2=" + pin2 + ", subscription=" + subId);
        boolean success = UiccPhoneBookControllerExt.updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efType, BuildConfig.FLAVOR, BuildConfig.FLAVOR, (String[]) null, (String[]) null, orginEfType, index, pin2);
        StringBuilder sb = new StringBuilder();
        sb.append("deleteUsimRecordFromEfByIndex: ");
        sb.append(success);
        logd(sb.toString());
        return success;
    }

    private boolean deleteIccRecordFromEfByIndex(int efType, int index, String[] emails, String pin2, int subId) {
        logd("deleteIccRecordFromEfByIndex: efType=" + efType + ", index=" + index + ", emails=" + Arrays.toString(emails) + ", pin2=" + pin2 + ", subscription=" + subId);
        boolean success = UiccPhoneBookControllerExt.updateAdnRecordsInEfByIndexForSubscriber(subId, efType, BuildConfig.FLAVOR, BuildConfig.FLAVOR, index, pin2);
        StringBuilder sb = new StringBuilder();
        sb.append("deleteIccRecordFromEfByIndex: ");
        sb.append(success);
        logd(sb.toString());
        return success;
    }

    private boolean deleteIccRecordFromEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        logd("deleteIccRecordFromEf: efType=" + efType + ", name=" + name + ", number=" + number + ", emails=" + Arrays.toString(emails) + ", pin2=" + pin2 + ", subscription=" + subId);
        boolean success = UiccPhoneBookControllerExt.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, name, number, BuildConfig.FLAVOR, BuildConfig.FLAVOR, pin2);
        StringBuilder sb = new StringBuilder();
        sb.append("deleteIccRecordFromEf: ");
        sb.append(success);
        logd(sb.toString());
        return success;
    }

    private boolean updateIccRecordInEf(int efType, ContentValues values, String pin2, int subId) {
        logd("updateIccRecordInEf: efType=" + efType + ", values: [ " + values + " ], subId:" + subId);
        boolean success = UiccPhoneBookControllerExt.updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(subId, efType, values, pin2);
        StringBuilder sb = new StringBuilder();
        sb.append("updateIccRecordInEf: ");
        sb.append(success);
        logd(sb.toString());
        return success;
    }

    /* access modifiers changed from: protected */
    public void loadRecord(AdnRecordExt record, MatrixCursor cursor, int id) {
        if (!(record == null || cursor == null || record.isEmpty())) {
            Object[] contact = new Object[(IccRecordsEx.getEmailAnrSupport() ? 7 : 6)];
            String alphaTag = record.getAlphaTag();
            String number = record.getNumber();
            contact[0] = alphaTag;
            contact[1] = number;
            String[] emails = record.getEmails();
            if (emails != null) {
                StringBuilder emailString = new StringBuilder();
                int count = 0;
                for (String email : emails) {
                    logd("Adding email:" + email);
                    emailString.append(email);
                    count++;
                    if (count < emails.length) {
                        emailString.append(",");
                    }
                }
                contact[2] = emailString.toString();
            } else {
                contact[2] = null;
            }
            String efid = Integer.toString(record.getEfid());
            String index = Integer.toString(record.getRecordNumber());
            contact[3] = efid;
            contact[4] = index;
            if (IccRecordsEx.getEmailAnrSupport()) {
                String[] anrs = record.getAdditionalNumbers();
                if (anrs != null) {
                    StringBuilder anrString = new StringBuilder();
                    int count2 = 0;
                    for (String anr : anrs) {
                        logd("Adding anr:" + anr);
                        anrString.append(anr);
                        count2++;
                        if (count2 < anrs.length) {
                            anrString.append(",");
                        }
                    }
                    contact[5] = anrString.toString();
                    contact[6] = Integer.valueOf(id);
                } else {
                    contact[5] = null;
                    contact[6] = Integer.valueOf(id);
                }
            } else {
                contact[5] = Integer.valueOf(id);
            }
            cursor.addRow(contact);
        }
    }

    private int getRequestSubId(Uri url) {
        logd("getRequestSubId url: " + url);
        try {
            return Integer.parseInt(url.getLastPathSegment());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }
}
