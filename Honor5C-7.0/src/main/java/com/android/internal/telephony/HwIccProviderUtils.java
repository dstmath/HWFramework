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
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.IIccPhoneBook.Stub;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordUtils;
import com.android.internal.telephony.uicc.HwAdnRecordCache;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.android.internal.telephony.uicc.IccRecords;
import huawei.cust.HwCustUtils;
import java.util.List;

public class HwIccProviderUtils {
    static final String[] ADDRESS_BOOK_COLUMN_NAMES = null;
    static final String[] ADDRESS_BOOK_COLUMN_NAMES_USIM = null;
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
    private static final UriMatcher URL_MATCHER = null;
    private static volatile HwIccProviderUtils instance;
    protected Context mContext;
    private HwCustIccProviderUtils mCust;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwIccProviderUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwIccProviderUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwIccProviderUtils.<clinit>():void");
    }

    protected HwIccProviderUtils(Context context) {
        this.mCust = null;
        this.mContext = null;
        this.mContext = context;
        this.mCust = (HwCustIccProviderUtils) HwCustUtils.createObj(HwCustIccProviderUtils.class, new Object[0]);
        if (this.mCust != null) {
            this.mCust.addURI(URL_MATCHER);
        }
    }

    protected Context getContext() {
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
        boolean isQuerybyindex = DBG;
        AdnRecord adnRecord = new AdnRecord("", "");
        int efid = 0;
        int index = 0;
        if (selection != null) {
            String tag = "";
            String number = "";
            String sEfid = null;
            String sIndex = null;
            String[] tokens = selection.split("AND");
            int n = tokens.length;
            while (true) {
                n--;
                if (n < 0) {
                    break;
                }
                String param = tokens[n];
                String[] pair = param.split("=");
                int length = pair.length;
                if (r0 != ADN_SUB) {
                    Rlog.e(TAG, "resolve: bad whereClause parameter: " + param);
                } else {
                    String key = pair[0].trim();
                    String val = pair[ADN].trim();
                    if (STR_TAG.equals(key)) {
                        tag = normalizeValue(val);
                    } else {
                        if (STR_NUMBER.equals(key)) {
                            number = normalizeValue(val);
                        } else {
                            if (!STR_EMAILS.equals(key)) {
                                if (STR_EFID.equals(key)) {
                                    sEfid = normalizeValue(val);
                                } else {
                                    if (STR_INDEX.equals(key)) {
                                        sIndex = normalizeValue(val);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!(sEfid == null || sIndex == null)) {
                efid = Integer.parseInt(sEfid);
                index = Integer.parseInt(sIndex);
                isQuerybyindex = true;
            }
            adnRecord = new AdnRecord(efid, index, tag, number);
        }
        if (this.mCust != null) {
            Cursor cursor = this.mCust.handleCustQuery(URL_MATCHER, url, selectionArgs, ADDRESS_BOOK_COLUMN_NAMES);
            if (cursor != null) {
                return cursor;
            }
        }
        switch (URL_MATCHER.match(url)) {
            case ADN /*1*/:
                if (isQuerybyindex) {
                    return loadFromEf(28474, searchAdn, SubscriptionManager.getDefaultSubscriptionId());
                }
                return loadFromEf(28474, SubscriptionManager.getDefaultSubscriptionId());
            case ADN_SUB /*2*/:
                if (isQuerybyindex) {
                    return loadFromEf(28474, searchAdn, getRequestSubId(url));
                }
                return loadFromEf(28474, getRequestSubId(url));
            case FDN /*3*/:
                return loadFromEf(28475, SubscriptionManager.getDefaultSubscriptionId());
            case FDN_SUB /*4*/:
                return loadFromEf(28475, getRequestSubId(url));
            case SDN /*5*/:
                return loadFromEf(28489, SubscriptionManager.getDefaultSubscriptionId());
            case SDN_SUB /*6*/:
                return loadFromEf(28489, getRequestSubId(url));
            case ADN_ALL /*7*/:
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
            for (int i = 0; i < subIdCount; i += ADN) {
                int subId = ((SubscriptionInfo) subInfoList.get(i)).getSubscriptionId();
                result[i] = loadFromEf(efType, subId);
                Rlog.i(TAG, "ADN Records loaded for Subscription ::" + subId);
            }
        }
        return new MergeCursor(result);
    }

    public String getType(Uri url) {
        switch (URL_MATCHER.match(url)) {
            case ADN /*1*/:
            case ADN_SUB /*2*/:
            case FDN /*3*/:
            case FDN_SUB /*4*/:
            case SDN /*5*/:
            case SDN_SUB /*6*/:
            case ADN_ALL /*7*/:
                return "vnd.android.cursor.dir/sim-contact";
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    public Uri insert(Uri url, ContentValues initialValues) {
        int efType;
        int subId;
        boolean success;
        String pin2 = null;
        int match = URL_MATCHER.match(url);
        switch (match) {
            case ADN /*1*/:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case ADN_SUB /*2*/:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case FDN /*3*/:
                efType = 28475;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = initialValues.getAsString(STR_PIN2);
                break;
            case FDN_SUB /*4*/:
                efType = 28475;
                subId = getRequestSubId(url);
                pin2 = initialValues.getAsString(STR_PIN2);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = initialValues.getAsString(STR_TAG);
        if (tag == null) {
            tag = "";
        }
        String number = initialValues.getAsString(STR_NUMBER);
        if (number == null) {
            number = "";
        }
        String emails = initialValues.getAsString(STR_EMAILS);
        if (emails == null) {
            emails = "";
        }
        String anrs = initialValues.getAsString(STR_ANRS);
        if (anrs == null) {
            anrs = "";
        }
        ContentValues mValues = new ContentValues();
        mValues.put(STR_TAG, "");
        mValues.put(STR_NUMBER, "");
        mValues.put(STR_EMAILS, "");
        mValues.put(STR_ANRS, "");
        mValues.put(STR_NEW_TAG, tag);
        mValues.put(STR_NEW_NUMBER, number);
        mValues.put(STR_NEW_EMAILS, emails);
        mValues.put(STR_NEW_ANRS, anrs);
        if (IccRecords.getEmailAnrSupport()) {
            success = updateIccRecordInEf(efType, mValues, pin2, subId);
        } else {
            success = addIccRecordToEf(efType, tag, number, null, pin2, subId);
        }
        if (!success) {
            return null;
        }
        StringBuilder buf = new StringBuilder("content://icc/");
        switch (match) {
            case ADN /*1*/:
                buf.append("adn/");
                break;
            case ADN_SUB /*2*/:
                buf.append("adn/subId/");
                break;
            case FDN /*3*/:
                buf.append("fdn/");
                break;
            case FDN_SUB /*4*/:
                buf.append("fdn/subId/");
                break;
        }
        buf.append(HwAdnRecordCache.s_index.get()).append("/");
        buf.append(HwAdnRecordCache.s_efid.get());
        Uri resultUri = Uri.parse(buf.toString());
        getContext().getContentResolver().notifyChange(url, null);
        return resultUri;
    }

    protected String normalizeValue(String inVal) {
        int len = inVal.length();
        if (len == 0) {
            return inVal;
        }
        String retVal = inVal;
        if (inVal.charAt(0) == '\'' && inVal.charAt(len - 1) == '\'') {
            retVal = inVal.substring(ADN, len - 1);
        }
        return retVal;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int delete(Uri url, String where, String[] whereArgs) {
        int efType;
        int subId;
        switch (URL_MATCHER.match(url)) {
            case ADN /*1*/:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case ADN_SUB /*2*/:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case FDN /*3*/:
                efType = 28475;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case FDN_SUB /*4*/:
                efType = 28475;
                subId = getRequestSubId(url);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = null;
        String number = null;
        String emails = null;
        String anrs = null;
        String pin2 = null;
        String sEfid = null;
        String sIndex = null;
        boolean success = DBG;
        String[] tokens = where.split("AND");
        int n = tokens.length;
        while (true) {
            n--;
            if (n >= 0) {
                String param = tokens[n];
                String[] pair = param.split("=");
                if (pair.length != ADN_SUB) {
                    Rlog.e(TAG, "resolve: bad whereClause parameter: " + param);
                } else {
                    String key = pair[0].trim();
                    String val = pair[ADN].trim();
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
                if (sEfid != null) {
                }
                if (sIndex != null) {
                }
                if (IccRecords.getEmailAnrSupport()) {
                    success = updateIccRecordInEf(efType, mValues, pin2, subId);
                } else {
                    success = deleteIccRecordFromEf(efType, tag, number, null, pin2, subId);
                }
                if (!success) {
                    return 0;
                }
                getContext().getContentResolver().notifyChange(url, null);
                return ADN;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int efType;
        int subId;
        String pin2 = null;
        switch (URL_MATCHER.match(url)) {
            case ADN /*1*/:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case ADN_SUB /*2*/:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case FDN /*3*/:
                efType = 28475;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = values.getAsString(STR_PIN2);
                break;
            case FDN_SUB /*4*/:
                efType = 28475;
                subId = getRequestSubId(url);
                pin2 = values.getAsString(STR_PIN2);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = values.getAsString(STR_TAG);
        String number = values.getAsString(STR_NUMBER);
        String newTag = values.getAsString(STR_NEW_TAG);
        if (newTag == null) {
            newTag = "";
        }
        String newNumber = values.getAsString(STR_NEW_NUMBER);
        if (newNumber == null) {
            newNumber = "";
        }
        String[] strArr = null;
        if (values.getAsString(STR_NEW_EMAILS) != null) {
            strArr = new String[ADN];
            strArr[0] = values.getAsString(STR_NEW_EMAILS);
        }
        String[] strArr2 = null;
        if (values.getAsString(STR_NEW_ANRS) != null) {
            strArr2 = new String[ADN];
            strArr2[0] = values.getAsString(STR_NEW_ANRS);
        }
        String Efid = values.getAsString(STR_EFID);
        String sIndex = values.getAsString(STR_INDEX);
        boolean success = DBG;
        if (Efid != null) {
        }
        if (sIndex != null) {
        }
        if (IccRecords.getEmailAnrSupport()) {
            success = updateIccRecordInEf(efType, values, pin2, subId);
        } else {
            success = updateIccRecordInEf(efType, tag, number, newTag, newNumber, pin2, subId);
        }
        if (!success) {
            return 0;
        }
        getContext().getContentResolver().notifyChange(url, null);
        return ADN;
    }

    private MatrixCursor loadFromEf(int efType, int subId) {
        List adnRecords = null;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEfForSubscriber(subId, efType);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(IccRecords.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES, N);
            if (this.mCust != null) {
                this.mCust.fdnCacheProcess(adnRecords, efType, (long) subId);
            }
            for (int i = 0; i < N; i += ADN) {
                loadRecord((AdnRecord) adnRecords.get(i), cursor, i);
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(IccRecords.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES);
    }

    private MatrixCursor loadFromEf(int efType, AdnRecord searchAdn, int subId) {
        List adnRecords = null;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEfForSubscriber(subId, efType);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(IccRecords.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES, N);
            for (int i = 0; i < N; i += ADN) {
                if (HwIccUtils.equalAdn(searchAdn, (AdnRecord) adnRecords.get(i))) {
                    Rlog.w(TAG, "have one by efid and index");
                    loadRecord((AdnRecord) adnRecords.get(i), cursor, i);
                    break;
                }
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(IccRecords.getEmailAnrSupport() ? ADDRESS_BOOK_COLUMN_NAMES_USIM : ADDRESS_BOOK_COLUMN_NAMES);
    }

    private boolean addIccRecordToEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        boolean success = DBG;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, "", "", name, number, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        return success;
    }

    private boolean updateIccRecordInEf(int efType, String oldName, String oldNumber, String newName, String newNumber, String pin2, int subId) {
        boolean success = DBG;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, oldName, oldNumber, newName, newNumber, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        return success;
    }

    private boolean updateIccRecordInEfByIndex(int efType, int index, String newName, String newNumber, String pin2, int subId) {
        boolean success = DBG;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfByIndexForSubscriber(subId, efType, newName, newNumber, index, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        return success;
    }

    private boolean updateUsimRecordInEfByIndex(int efType, int sEf_id, int index, String newName, String newNumber, String[] newEmails, String[] newAnrs, String pin2, int subId) {
        boolean success = DBG;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efType, newName, newNumber, newEmails, newAnrs, sEf_id, index, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        return success;
    }

    private boolean deleteUsimRecordFromEfByIndex(int efType, int sEf_id, int index, String[] emails, String pin2, int subId) {
        boolean success = DBG;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efType, "", "", null, null, sEf_id, index, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        return success;
    }

    private boolean deleteIccRecordFromEfByIndex(int efType, int index, String[] emails, String pin2, int subId) {
        boolean success = DBG;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfByIndexForSubscriber(subId, efType, "", "", index, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        return success;
    }

    private boolean deleteIccRecordFromEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        boolean success = DBG;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, name, number, "", "", pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        return success;
    }

    private boolean updateIccRecordInEf(int efType, ContentValues values, String pin2, int subId) {
        boolean success = DBG;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(subId, efType, values, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        return success;
    }

    protected void loadRecord(AdnRecord record, MatrixCursor cursor, int id) {
        if (!record.isEmpty()) {
            int count;
            int length;
            int i;
            Object[] contact = new Object[(IccRecords.getEmailAnrSupport() ? ADN_ALL : SDN_SUB)];
            String alphaTag = record.getAlphaTag();
            String number = record.getNumber();
            String[] anrs = record.getAdditionalNumbers();
            String efid = Integer.toString(AdnRecordUtils.getEfid(record));
            String index = Integer.toString(AdnRecordUtils.getRecordNumber(record));
            contact[0] = alphaTag;
            contact[ADN] = number;
            String[] emails = record.getEmails();
            if (emails != null) {
                StringBuilder emailString = new StringBuilder();
                count = 0;
                length = emails.length;
                for (i = 0; i < length; i += ADN) {
                    emailString.append(emails[i]);
                    count += ADN;
                    if (count < emails.length) {
                        emailString.append(",");
                    }
                }
                contact[ADN_SUB] = emailString.toString();
            } else {
                contact[ADN_SUB] = null;
            }
            contact[FDN] = efid;
            contact[FDN_SUB] = index;
            if (!IccRecords.getEmailAnrSupport()) {
                contact[SDN] = Integer.valueOf(id);
            } else if (anrs != null) {
                StringBuilder anrString = new StringBuilder();
                count = 0;
                length = anrs.length;
                for (i = 0; i < length; i += ADN) {
                    anrString.append(anrs[i]);
                    count += ADN;
                    if (count < anrs.length) {
                        anrString.append(",");
                    }
                }
                contact[SDN] = anrString.toString();
                contact[SDN_SUB] = Integer.valueOf(id);
            } else {
                contact[SDN] = null;
                contact[SDN_SUB] = Integer.valueOf(id);
            }
            cursor.addRow(contact);
        }
    }

    protected void log(String msg) {
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
