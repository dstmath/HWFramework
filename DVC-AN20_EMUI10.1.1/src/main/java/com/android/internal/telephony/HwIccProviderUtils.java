package com.android.internal.telephony;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.HwTelephony;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordUtils;
import com.android.internal.telephony.uicc.HwAdnRecordCache;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import huawei.cust.HwCustUtils;
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
    protected static final String STR_ANRS = "anrs";
    protected static final String STR_EFID = "efid";
    protected static final String STR_EMAILS = "emails";
    protected static final String STR_INDEX = "index";
    protected static final String STR_NEW_ANRS = "newAnrs";
    protected static final String STR_NEW_EMAILS = "newEmails";
    protected static final String STR_NEW_NUMBER = "newNumber";
    protected static final String STR_NEW_TAG = "newTag";
    protected static final String STR_NUMBER = "number";
    protected static final String STR_PIN2 = "pin2";
    protected static final String STR_TAG = "tag";
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

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mContext;
    }

    public static HwIccProviderUtils getDefault(Context context) {
        if (instance == null) {
            instance = new HwIccProviderUtils(context);
        }
        return instance;
    }

    public boolean isHwSimPhonebookEnabled() {
        return true;
    }

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        boolean isQuerybyindex = false;
        AdnRecord searchAdn = new AdnRecord("", "");
        int efid = 0;
        int index = 0;
        if (selection != null) {
            String[] parameters = initParameters(selection, true);
            String tag = parameters[0];
            String number = parameters[1];
            String sEfid = parameters[5];
            String sIndex = parameters[6];
            if (!(sEfid == null || sIndex == null)) {
                try {
                    efid = Integer.parseInt(sEfid);
                    index = Integer.parseInt(sIndex);
                } catch (NumberFormatException e) {
                    log("NumberFormat Exception");
                }
                isQuerybyindex = true;
            }
            searchAdn = new AdnRecord(efid, index, tag, number);
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
        List<SubscriptionInfo> subInfoList = SubscriptionController.getInstance().getActiveSubscriptionInfoList("com.android.phone");
        if (subInfoList == null || subInfoList.size() == 0) {
            result = new Cursor[0];
        } else {
            int subIdCount = subInfoList.size();
            result = new Cursor[subIdCount];
            for (int i = 0; i < subIdCount; i++) {
                int subId = subInfoList.get(i).getSubscriptionId();
                result[i] = loadFromEf(efType, subId);
                Rlog.i(TAG, "ADN Records loaded for Subscription ::" + subId);
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
        String number;
        String emails;
        String anrs;
        boolean success;
        String pin2 = null;
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
        String tag2 = tag == null ? "" : tag;
        String number2 = initialValues.getAsString(STR_NUMBER);
        if (number2 == null) {
            number = "";
        } else {
            number = number2;
        }
        String emails2 = initialValues.getAsString(STR_EMAILS);
        if (emails2 == null) {
            emails = "";
        } else {
            emails = emails2;
        }
        String anrs2 = initialValues.getAsString(STR_ANRS);
        if (anrs2 == null) {
            anrs = "";
        } else {
            anrs = anrs2;
        }
        ContentValues mValues = new ContentValues();
        mValues.put(STR_TAG, "");
        mValues.put(STR_NUMBER, "");
        mValues.put(STR_EMAILS, "");
        mValues.put(STR_ANRS, "");
        mValues.put(STR_NEW_TAG, tag2);
        mValues.put(STR_NEW_NUMBER, number);
        mValues.put(STR_NEW_EMAILS, emails);
        mValues.put(STR_NEW_ANRS, anrs);
        if (IccRecordsEx.getEmailAnrSupport()) {
            success = updateIccRecordInEf(efType, mValues, pin2, subId);
        } else {
            success = addIccRecordToEf(efType, tag2, number, null, pin2, subId);
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
        buf.append(HwAdnRecordCache.s_index.get() + "/");
        buf.append(HwAdnRecordCache.s_efid.get());
        Uri resultUri = Uri.parse(buf.toString());
        getContext().getContentResolver().notifyChange(url, null);
        return resultUri;
    }

    /* access modifiers changed from: protected */
    public String normalizeValue(String inVal) {
        int len = inVal.length();
        if (len != 0 && inVal.charAt(0) == '\'' && inVal.charAt(len - 1) == '\'') {
            return inVal.substring(1, len - 1);
        }
        return inVal;
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x015c A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x015d  */
    public int delete(Uri url, String where, String[] whereArgs) {
        int subId;
        int efType;
        int i;
        boolean z;
        boolean z2;
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
        boolean success = false;
        String[] parameters = initParameters(where, false);
        String tag = parameters[0];
        String number = parameters[1];
        String emails = parameters[2];
        String anrs = parameters[3];
        String pin2 = parameters[4];
        String sEfid = parameters[5];
        String sIndex = parameters[6];
        ContentValues mValues = new ContentValues();
        mValues.put(STR_TAG, tag);
        mValues.put(STR_NUMBER, number);
        mValues.put(STR_EMAILS, emails);
        mValues.put(STR_ANRS, anrs);
        mValues.put(STR_NEW_TAG, "");
        mValues.put(STR_NEW_NUMBER, "");
        mValues.put(STR_NEW_EMAILS, "");
        mValues.put(STR_NEW_ANRS, "");
        if (efType == 28475 && TextUtils.isEmpty(pin2)) {
            return 0;
        }
        if ((sEfid == null || sEfid.equals("")) && (sIndex == null || sIndex.equals(""))) {
            if (!IccRecordsEx.getEmailAnrSupport()) {
                i = 0;
                z2 = deleteIccRecordFromEf(efType, tag, number, null, pin2, subId);
            } else {
                i = 0;
                z2 = updateIccRecordInEf(efType, mValues, pin2, subId);
            }
            success = z2;
        } else {
            i = 0;
            try {
                int sEf_id = Integer.parseInt(sEfid);
                int index = Integer.parseInt(sIndex);
                if (index > 0) {
                    if (!IccRecordsEx.getEmailAnrSupport()) {
                        try {
                            z = deleteIccRecordFromEfByIndex(sEf_id, index, null, pin2, subId);
                        } catch (NumberFormatException e) {
                            log("NumberFormat Exception");
                            if (success) {
                            }
                        }
                    } else {
                        try {
                            z = deleteUsimRecordFromEfByIndex(efType, sEf_id, index, null, pin2, subId);
                        } catch (NumberFormatException e2) {
                            log("NumberFormat Exception");
                            if (success) {
                            }
                        }
                    }
                    success = z;
                }
            } catch (NumberFormatException e3) {
                log("NumberFormat Exception");
                if (success) {
                }
            }
        }
        if (success) {
            return i;
        }
        getContext().getContentResolver().notifyChange(url, null);
        return 1;
    }

    private String[] initParameters(String where, boolean isQuery) {
        String number;
        String tag;
        if (isQuery) {
            tag = "";
            number = "";
        } else {
            tag = null;
            number = null;
        }
        String emails = null;
        String anrs = null;
        String pin2 = null;
        String sEfid = null;
        String sIndex = null;
        String[] tokens = where.split("AND");
        int n = tokens.length;
        while (true) {
            n--;
            if (n >= 0) {
                String param = tokens[n];
                String[] pair = param.split("=");
                if (pair.length != 2) {
                    Rlog.e(TAG, "resolve: bad whereClause parameter: " + param);
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
                        sEfid = normalizeValue(val);
                    } else if (STR_INDEX.equals(key)) {
                        sIndex = normalizeValue(val);
                    }
                }
            } else {
                return new String[]{tag, number, emails, anrs, pin2, sEfid, sIndex};
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:0x0145 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0146  */
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int subId;
        String pin2;
        int efType;
        String newNumber;
        boolean z;
        boolean z2;
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
        String newTag = values.getAsString(STR_NEW_TAG);
        String newNumber2 = values.getAsString(STR_NEW_NUMBER);
        String newTag2 = newTag == null ? "" : newTag;
        if (newNumber2 == null) {
            newNumber = "";
        } else {
            newNumber = newNumber2;
        }
        String[] newEmails = values.getAsString(STR_NEW_EMAILS) != null ? new String[]{values.getAsString(STR_NEW_EMAILS)} : null;
        String[] newAnrs = values.getAsString(STR_NEW_ANRS) != null ? new String[]{values.getAsString(STR_NEW_ANRS)} : null;
        String Efid = values.getAsString(STR_EFID);
        String sIndex = values.getAsString(STR_INDEX);
        boolean success = false;
        if ((Efid == null || Efid.equals("")) && (sIndex == null || sIndex.equals(""))) {
            if (!IccRecordsEx.getEmailAnrSupport()) {
                z2 = updateIccRecordInEf(efType, tag, number, newTag2, newNumber, pin2, subId);
            } else {
                z2 = updateIccRecordInEf(efType, values, pin2, subId);
            }
            success = z2;
        } else {
            try {
                int sEf_id = Integer.parseInt(Efid);
                int index = Integer.parseInt(sIndex);
                if (index > 0) {
                    try {
                        if (!IccRecordsEx.getEmailAnrSupport()) {
                            z = updateIccRecordInEfByIndex(sEf_id, index, newTag2, newNumber, pin2, subId);
                        } else {
                            z = updateUsimRecordInEfByIndex(efType, sEf_id, index, newTag2, newNumber, newEmails, newAnrs, pin2, subId);
                        }
                        success = z;
                    } catch (NumberFormatException e) {
                        log("NumberFormat Exception");
                        if (!success) {
                        }
                    }
                }
            } catch (NumberFormatException e2) {
                log("NumberFormat Exception");
                if (!success) {
                }
            }
        }
        if (!success) {
            return 0;
        }
        getContext().getContentResolver().notifyChange(url, null);
        return 1;
    }

    private MatrixCursor loadFromEf(int efType, int subId) {
        List<AdnRecord> adnRecords = null;
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEfForSubscriber(subId, efType);
            }
        } catch (RemoteException | SecurityException e) {
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(IccRecordsEx.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES, N);
            HwCustIccProviderUtils hwCustIccProviderUtils = this.mCust;
            if (hwCustIccProviderUtils != null) {
                hwCustIccProviderUtils.fdnCacheProcess(adnRecords, efType, (long) subId);
            }
            for (int i = 0; i < N; i++) {
                loadRecord(adnRecords.get(i), cursor, i);
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(IccRecordsEx.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES);
    }

    private MatrixCursor loadFromEf(int efType, AdnRecord searchAdn, int subId) {
        List<AdnRecord> adnRecords = null;
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEfForSubscriber(subId, efType);
            }
        } catch (RemoteException | SecurityException e) {
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(IccRecordsEx.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES, N);
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                } else if (HwIccUtils.equalAdn(AdnRecordExt.from(searchAdn), AdnRecordExt.from(adnRecords.get(i)))) {
                    Rlog.w(TAG, "have one by efid and index");
                    loadRecord(adnRecords.get(i), cursor, i);
                    break;
                } else {
                    i++;
                }
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(IccRecordsEx.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES);
    }

    private boolean addIccRecordToEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, "", "", name, number, pin2);
            }
            return false;
        } catch (RemoteException | SecurityException e) {
            return false;
        }
    }

    private boolean updateIccRecordInEf(int efType, String oldName, String oldNumber, String newName, String newNumber, String pin2, int subId) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, oldName, oldNumber, newName, newNumber, pin2);
            }
            return false;
        } catch (RemoteException | SecurityException e) {
            return false;
        }
    }

    private boolean updateIccRecordInEfByIndex(int efType, int index, String newName, String newNumber, String pin2, int subId) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateAdnRecordsInEfByIndexForSubscriber(subId, efType, newName, newNumber, index, pin2);
            }
            return false;
        } catch (RemoteException | SecurityException e) {
            return false;
        }
    }

    private boolean updateUsimRecordInEfByIndex(int efType, int sEf_id, int index, String newName, String newNumber, String[] newEmails, String[] newAnrs, String pin2, int subId) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efType, newName, newNumber, newEmails, newAnrs, sEf_id, index, pin2);
            }
            return false;
        } catch (RemoteException | SecurityException e) {
            return false;
        }
    }

    private boolean deleteUsimRecordFromEfByIndex(int efType, int sEf_id, int index, String[] emails, String pin2, int subId) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efType, "", "", (String[]) null, (String[]) null, sEf_id, index, pin2);
            }
            return false;
        } catch (RemoteException | SecurityException e) {
            return false;
        }
    }

    private boolean deleteIccRecordFromEfByIndex(int efType, int index, String[] emails, String pin2, int subId) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateAdnRecordsInEfByIndexForSubscriber(subId, efType, "", "", index, pin2);
            }
            return false;
        } catch (RemoteException | SecurityException e) {
            return false;
        }
    }

    private boolean deleteIccRecordFromEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, name, number, "", "", pin2);
            }
            return false;
        } catch (RemoteException | SecurityException e) {
            return false;
        }
    }

    private boolean updateIccRecordInEf(int efType, ContentValues values, String pin2, int subId) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(subId, efType, values, pin2);
            }
            return false;
        } catch (RemoteException | SecurityException e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void loadRecord(AdnRecord record, MatrixCursor cursor, int id) {
        if (!record.isEmpty()) {
            Object[] contact = new Object[(IccRecordsEx.getEmailAnrSupport() ? 7 : 6)];
            String alphaTag = record.getAlphaTag();
            String number = record.getNumber();
            String[] anrs = record.getAdditionalNumbers();
            String efid = Integer.toString(AdnRecordUtils.getEfid(record));
            String index = Integer.toString(AdnRecordUtils.getRecordNumber(record));
            contact[0] = alphaTag;
            contact[1] = number;
            String[] emails = record.getEmails();
            if (emails != null) {
                StringBuilder emailString = new StringBuilder();
                int count = 0;
                for (String email : emails) {
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
            contact[3] = efid;
            contact[4] = index;
            if (!IccRecordsEx.getEmailAnrSupport()) {
                contact[5] = Integer.valueOf(id);
            } else if (anrs != null) {
                StringBuilder anrString = new StringBuilder();
                int count2 = 0;
                for (String anr : anrs) {
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
            cursor.addRow(contact);
        }
    }

    private static void log(String msg) {
        RlogEx.i(TAG, "[IccProvider] " + msg);
    }

    private int getRequestSubId(Uri url) {
        try {
            return Integer.parseInt(url.getLastPathSegment());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }
}
