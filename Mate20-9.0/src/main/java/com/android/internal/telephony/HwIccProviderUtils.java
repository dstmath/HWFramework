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
import com.android.internal.telephony.uicc.IccRecords;
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
        if (this.mCust != null) {
            this.mCust.addURI(URL_MATCHER);
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
        MatrixCursor matrixCursor;
        MatrixCursor matrixCursor2;
        Uri uri = url;
        String str = selection;
        boolean isQuerybyindex = false;
        AdnRecord searchAdn = new AdnRecord("", "");
        int efid = 0;
        int index = 0;
        if (str != null) {
            String[] parameters = initParameters(str, true);
            String tag = parameters[0];
            String number = parameters[1];
            String sEfid = parameters[5];
            String sIndex = parameters[6];
            if (!(sEfid == null || sIndex == null)) {
                efid = Integer.parseInt(sEfid);
                index = Integer.parseInt(sIndex);
                isQuerybyindex = true;
            }
            searchAdn = new AdnRecord(efid, index, tag, number);
        }
        if (this.mCust != null) {
            Cursor cursor = this.mCust.handleCustQuery(URL_MATCHER, uri, selectionArgs, ADDRESS_BOOK_COLUMN_NAMES);
            if (cursor != null) {
                return cursor;
            }
        } else {
            String[] strArr = selectionArgs;
        }
        switch (URL_MATCHER.match(uri)) {
            case 1:
                if (isQuerybyindex) {
                    matrixCursor = loadFromEf(28474, searchAdn, SubscriptionManager.getDefaultSubscriptionId());
                } else {
                    matrixCursor = loadFromEf(28474, SubscriptionManager.getDefaultSubscriptionId());
                }
                return matrixCursor;
            case 2:
                if (isQuerybyindex) {
                    matrixCursor2 = loadFromEf(28474, searchAdn, getRequestSubId(url));
                } else {
                    matrixCursor2 = loadFromEf(28474, getRequestSubId(url));
                }
                return matrixCursor2;
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
                throw new IllegalArgumentException("Unknown URL " + uri);
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
        boolean success;
        Uri uri = url;
        ContentValues contentValues = initialValues;
        String pin2 = null;
        int match = URL_MATCHER.match(uri);
        switch (match) {
            case 1:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 2:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case 3:
                efType = 28475;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = contentValues.getAsString(STR_PIN2);
                break;
            case 4:
                efType = 28475;
                subId = getRequestSubId(url);
                pin2 = contentValues.getAsString(STR_PIN2);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + uri);
        }
        String tag = contentValues.getAsString(STR_TAG);
        String tag2 = tag == null ? "" : tag;
        String number = contentValues.getAsString(STR_NUMBER);
        String number2 = number == null ? "" : number;
        String emails = contentValues.getAsString(STR_EMAILS);
        String emails2 = emails == null ? "" : emails;
        String anrs = contentValues.getAsString(STR_ANRS);
        String anrs2 = anrs == null ? "" : anrs;
        ContentValues mValues = new ContentValues();
        mValues.put(STR_TAG, "");
        mValues.put(STR_NUMBER, "");
        mValues.put(STR_EMAILS, "");
        mValues.put(STR_ANRS, "");
        mValues.put(STR_NEW_TAG, tag2);
        mValues.put(STR_NEW_NUMBER, number2);
        mValues.put(STR_NEW_EMAILS, emails2);
        mValues.put(STR_NEW_ANRS, anrs2);
        if (IccRecords.getEmailAnrSupport()) {
            success = updateIccRecordInEf(efType, mValues, pin2, subId);
            ContentValues contentValues2 = mValues;
            String str = anrs2;
        } else {
            ContentValues contentValues3 = mValues;
            String str2 = anrs2;
            success = addIccRecordToEf(efType, tag2, number2, null, pin2, subId);
        }
        if (!success) {
            return null;
        }
        StringBuilder buf = new StringBuilder("content://icc/");
        switch (match) {
            case 1:
                buf.append("adn/");
                break;
            case 2:
                buf.append("adn/subId/");
                break;
            case 3:
                buf.append("fdn/");
                break;
            case 4:
                buf.append("fdn/subId/");
                break;
        }
        buf.append(HwAdnRecordCache.s_index.get() + "/");
        buf.append(HwAdnRecordCache.s_efid.get());
        Uri resultUri = Uri.parse(buf.toString());
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    /* access modifiers changed from: protected */
    public String normalizeValue(String inVal) {
        int len = inVal.length();
        if (len == 0) {
            return inVal;
        }
        String retVal = inVal;
        if (inVal.charAt(0) == '\'' && inVal.charAt(len - 1) == '\'') {
            retVal = inVal.substring(1, len - 1);
        }
        return retVal;
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        int subId;
        int efType;
        boolean z;
        String sEfid;
        String pin2;
        boolean z2;
        Uri uri = url;
        int match = URL_MATCHER.match(uri);
        switch (match) {
            case 1:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 2:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case 3:
                efType = 28475;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 4:
                efType = 28475;
                subId = getRequestSubId(url);
                break;
            default:
                String str = where;
                int i = match;
                throw new UnsupportedOperationException("Cannot insert into URL: " + uri);
        }
        int efType2 = efType;
        int subId2 = subId;
        boolean success = false;
        String[] parameters = initParameters(where, false);
        String tag = parameters[0];
        String number = parameters[1];
        String emails = parameters[2];
        String anrs = parameters[3];
        String pin22 = parameters[4];
        String sEfid2 = parameters[5];
        String sIndex = parameters[6];
        ContentValues mValues = new ContentValues();
        int i2 = match;
        mValues.put(STR_TAG, tag);
        mValues.put(STR_NUMBER, number);
        mValues.put(STR_EMAILS, emails);
        mValues.put(STR_ANRS, anrs);
        String tag2 = tag;
        mValues.put(STR_NEW_TAG, "");
        mValues.put(STR_NEW_NUMBER, "");
        mValues.put(STR_NEW_EMAILS, "");
        mValues.put(STR_NEW_ANRS, "");
        if (efType2 == 28475 && TextUtils.isEmpty(pin22)) {
            return 0;
        }
        boolean efidAndIndexEmpty = (sEfid2 == null || sEfid2.equals("")) && (sIndex == null || sIndex.equals(""));
        if (efidAndIndexEmpty) {
            if (!IccRecords.getEmailAnrSupport()) {
                String str2 = emails;
                String str3 = anrs;
                String pin23 = pin22;
                sEfid = sEfid2;
                boolean z3 = efidAndIndexEmpty;
                String str4 = sIndex;
                z2 = deleteIccRecordFromEf(efType2, tag2, number, null, pin23, subId2);
                pin2 = pin23;
            } else {
                String str5 = emails;
                String str6 = anrs;
                sEfid = sEfid2;
                boolean z4 = efidAndIndexEmpty;
                String str7 = sIndex;
                pin2 = pin22;
                z2 = updateIccRecordInEf(efType2, mValues, pin2, subId2);
            }
            success = z2;
            String str8 = sEfid;
            String sEfid3 = pin2;
        } else {
            String str9 = emails;
            String str10 = anrs;
            String str11 = sEfid2;
            boolean z5 = efidAndIndexEmpty;
            String sIndex2 = sIndex;
            String sIndex3 = pin22;
            int sEf_id = Integer.parseInt(sEfid2);
            int index = Integer.parseInt(sIndex2);
            if (index > 0) {
                if (!IccRecords.getEmailAnrSupport()) {
                    String str12 = sEfid2;
                    z = deleteIccRecordFromEfByIndex(sEf_id, index, null, sIndex3, subId2);
                    String str13 = sIndex3;
                } else {
                    String str14 = sIndex3;
                    z = deleteUsimRecordFromEfByIndex(efType2, sEf_id, index, null, sIndex3, subId2);
                }
                success = z;
            } else {
                String str15 = sIndex3;
            }
        }
        if (!success) {
            return 0;
        }
        getContext().getContentResolver().notifyChange(uri, null);
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
                String[] pair = tokens[n].split("=");
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

    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int subId;
        int efType;
        int i;
        boolean z;
        boolean z2;
        Uri uri = url;
        ContentValues contentValues = values;
        String pin2 = null;
        int match = URL_MATCHER.match(uri);
        switch (match) {
            case 1:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 2:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case 3:
                efType = 28475;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = contentValues.getAsString(STR_PIN2);
                break;
            case 4:
                efType = 28475;
                subId = getRequestSubId(url);
                pin2 = contentValues.getAsString(STR_PIN2);
                break;
            default:
                int i2 = match;
                throw new UnsupportedOperationException("Cannot insert into URL: " + uri);
        }
        String tag = contentValues.getAsString(STR_TAG);
        String number = contentValues.getAsString(STR_NUMBER);
        String newTag = contentValues.getAsString(STR_NEW_TAG);
        String newNumber = contentValues.getAsString(STR_NEW_NUMBER);
        String newTag2 = newTag == null ? "" : newTag;
        String newNumber2 = newNumber == null ? "" : newNumber;
        String[] newEmails = contentValues.getAsString(STR_NEW_EMAILS) != null ? new String[]{contentValues.getAsString(STR_NEW_EMAILS)} : null;
        String[] newAnrs = contentValues.getAsString(STR_NEW_ANRS) != null ? new String[]{contentValues.getAsString(STR_NEW_ANRS)} : null;
        String Efid = contentValues.getAsString(STR_EFID);
        String sIndex = contentValues.getAsString(STR_INDEX);
        boolean success = false;
        if ((Efid == null || Efid.equals("")) && (sIndex == null || sIndex.equals(""))) {
            if (!IccRecords.getEmailAnrSupport()) {
                int i3 = match;
                String str = sIndex;
                z2 = updateIccRecordInEf(efType, tag, number, newTag2, newNumber2, pin2, subId);
            } else {
                String str2 = sIndex;
                z2 = updateIccRecordInEf(efType, contentValues, pin2, subId);
            }
            success = z2;
            String str3 = Efid;
            i = 1;
        } else {
            int i4 = match;
            int sEf_id = Integer.parseInt(Efid);
            int index = Integer.parseInt(sIndex);
            if (index > 0) {
                if (!IccRecords.getEmailAnrSupport()) {
                    String str4 = Efid;
                    i = 1;
                    z = updateIccRecordInEfByIndex(sEf_id, index, newTag2, newNumber2, pin2, subId);
                } else {
                    i = 1;
                    z = updateUsimRecordInEfByIndex(efType, sEf_id, index, newTag2, newNumber2, newEmails, newAnrs, pin2, subId);
                }
                success = z;
            } else {
                i = 1;
            }
        }
        if (!success) {
            return 0;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return i;
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
            MatrixCursor cursor = new MatrixCursor(IccRecords.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES, N);
            if (this.mCust != null) {
                this.mCust.fdnCacheProcess(adnRecords, efType, (long) subId);
            }
            for (int i = 0; i < N; i++) {
                loadRecord(adnRecords.get(i), cursor, i);
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(IccRecords.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES);
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
            MatrixCursor cursor = new MatrixCursor(IccRecords.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES, N);
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                } else if (HwIccUtils.equalAdn(searchAdn, adnRecords.get(i))) {
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
        return new MatrixCursor(IccRecords.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES);
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
                return iccIpb.updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efType, "", "", null, null, sEf_id, index, pin2);
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
            Object[] contact = new Object[(IccRecords.getEmailAnrSupport() ? 7 : 6)];
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
            if (!IccRecords.getEmailAnrSupport()) {
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
            return;
        }
        MatrixCursor matrixCursor = cursor;
    }

    /* access modifiers changed from: protected */
    public void log(String msg) {
        Rlog.d(TAG, "[IccProvider] " + msg);
    }

    private int getRequestSubId(Uri url) {
        try {
            return Integer.parseInt(url.getLastPathSegment());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }
}
