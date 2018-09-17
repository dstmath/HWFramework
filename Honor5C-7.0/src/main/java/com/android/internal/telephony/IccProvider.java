package com.android.internal.telephony;

import android.content.ContentProvider;
import android.content.ContentValues;
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
import com.android.internal.telephony.uicc.IccConstants;
import java.util.List;

public class IccProvider extends ContentProvider {
    private static final String[] ADDRESS_BOOK_COLUMN_NAMES = null;
    protected static final int ADN = 1;
    protected static final int ADN_ALL = 7;
    protected static final int ADN_SUB = 2;
    private static final boolean DBG = true;
    protected static final int FDN = 3;
    protected static final int FDN_SUB = 4;
    protected static final int SDN = 5;
    protected static final int SDN_SUB = 6;
    protected static final String STR_EMAILS = "emails";
    protected static final String STR_NUMBER = "number";
    protected static final String STR_PIN2 = "pin2";
    protected static final String STR_TAG = "tag";
    private static final String TAG = "IccProvider";
    private static final UriMatcher URL_MATCHER = null;
    private SubscriptionManager mSubscriptionManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.IccProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.IccProvider.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccProvider.<clinit>():void");
    }

    public boolean onCreate() {
        this.mSubscriptionManager = SubscriptionManager.from(getContext());
        return DBG;
    }

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        log("query");
        if (HwTelephonyFactory.getHwUiccManager().isHwSimPhonebookEnabled()) {
            return HwTelephonyFactory.getHwUiccManager().simContactsQuery(getContext(), url, projection, selection, selectionArgs, sort);
        }
        switch (URL_MATCHER.match(url)) {
            case ADN /*1*/:
                return loadFromEf(IccConstants.EF_CSIM_LI, SubscriptionManager.getDefaultSubscriptionId());
            case ADN_SUB /*2*/:
                return loadFromEf(IccConstants.EF_CSIM_LI, getRequestSubId(url));
            case FDN /*3*/:
                return loadFromEf(IccConstants.EF_FDN, SubscriptionManager.getDefaultSubscriptionId());
            case FDN_SUB /*4*/:
                return loadFromEf(IccConstants.EF_FDN, getRequestSubId(url));
            case SDN /*5*/:
                return loadFromEf(IccConstants.EF_SDN, SubscriptionManager.getDefaultSubscriptionId());
            case SDN_SUB /*6*/:
                return loadFromEf(IccConstants.EF_SDN, getRequestSubId(url));
            case ADN_ALL /*7*/:
                return loadAllSimContacts(IccConstants.EF_CSIM_LI);
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    private Cursor loadAllSimContacts(int efType) {
        Cursor[] result;
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
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
        if (HwTelephonyFactory.getHwUiccManager().isHwSimPhonebookEnabled()) {
            return HwTelephonyFactory.getHwUiccManager().simContactsGetType(getContext(), url);
        }
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
        if (HwTelephonyFactory.getHwUiccManager().isHwSimPhonebookEnabled()) {
            return HwTelephonyFactory.getHwUiccManager().simContactsInsert(getContext(), url, initialValues);
        }
        int efType;
        int subId;
        String pin2 = null;
        log("insert");
        int match = URL_MATCHER.match(url);
        switch (match) {
            case ADN /*1*/:
                efType = IccConstants.EF_CSIM_LI;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case ADN_SUB /*2*/:
                efType = IccConstants.EF_CSIM_LI;
                subId = getRequestSubId(url);
                break;
            case FDN /*3*/:
                efType = IccConstants.EF_FDN;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = initialValues.getAsString(STR_PIN2);
                break;
            case FDN_SUB /*4*/:
                efType = IccConstants.EF_FDN;
                subId = getRequestSubId(url);
                pin2 = initialValues.getAsString(STR_PIN2);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        if (!addIccRecordToEf(efType, initialValues.getAsString(STR_TAG), initialValues.getAsString(STR_NUMBER), null, pin2, subId)) {
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
        buf.append(0);
        Uri resultUri = Uri.parse(buf.toString());
        getContext().getContentResolver().notifyChange(url, null);
        return resultUri;
    }

    private String normalizeValue(String inVal) {
        int len = inVal.length();
        if (len == 0) {
            log("len of input String is 0");
            return inVal;
        }
        String retVal = inVal;
        if (inVal.charAt(0) == '\'' && inVal.charAt(len - 1) == '\'') {
            retVal = inVal.substring(ADN, len - 1);
        }
        return retVal;
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        if (HwTelephonyFactory.getHwUiccManager().isHwSimPhonebookEnabled()) {
            return HwTelephonyFactory.getHwUiccManager().simContactsDelete(getContext(), url, where, whereArgs);
        }
        int efType;
        int subId;
        switch (URL_MATCHER.match(url)) {
            case ADN /*1*/:
                efType = IccConstants.EF_CSIM_LI;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case ADN_SUB /*2*/:
                efType = IccConstants.EF_CSIM_LI;
                subId = getRequestSubId(url);
                break;
            case FDN /*3*/:
                efType = IccConstants.EF_FDN;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case FDN_SUB /*4*/:
                efType = IccConstants.EF_FDN;
                subId = getRequestSubId(url);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        log("delete");
        String tag = null;
        String number = null;
        String[] emails = null;
        Object pin2 = null;
        String[] tokens = where.split("AND");
        int n = tokens.length;
        while (true) {
            n--;
            if (n >= 0) {
                String param = tokens[n];
                String str = "'";
                log("parsing '" + param + r19);
                String[] pair = param.split("=", ADN_SUB);
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
                        emails = null;
                    } else if (STR_PIN2.equals(key)) {
                        pin2 = normalizeValue(val);
                    }
                }
            } else if (efType == FDN && TextUtils.isEmpty(pin2)) {
                return 0;
            } else {
                if (!deleteIccRecordFromEf(efType, tag, number, emails, pin2, subId)) {
                    return 0;
                }
                getContext().getContentResolver().notifyChange(url, null);
                return ADN;
            }
        }
    }

    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        if (HwTelephonyFactory.getHwUiccManager().isHwSimPhonebookEnabled()) {
            return HwTelephonyFactory.getHwUiccManager().simContactsUpdate(getContext(), url, values, where, whereArgs);
        }
        int efType;
        int subId;
        String pin2 = null;
        log("update");
        switch (URL_MATCHER.match(url)) {
            case ADN /*1*/:
                efType = IccConstants.EF_CSIM_LI;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case ADN_SUB /*2*/:
                efType = IccConstants.EF_CSIM_LI;
                subId = getRequestSubId(url);
                break;
            case FDN /*3*/:
                efType = IccConstants.EF_FDN;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = values.getAsString(STR_PIN2);
                break;
            case FDN_SUB /*4*/:
                efType = IccConstants.EF_FDN;
                subId = getRequestSubId(url);
                pin2 = values.getAsString(STR_PIN2);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        if (!updateIccRecordInEf(efType, values.getAsString(STR_TAG), values.getAsString(STR_NUMBER), values.getAsString("newTag"), values.getAsString("newNumber"), pin2, subId)) {
            return 0;
        }
        getContext().getContentResolver().notifyChange(url, null);
        return ADN;
    }

    private MatrixCursor loadFromEf(int efType, int subId) {
        log("loadFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        List adnRecords = null;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEfForSubscriber(subId, efType);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES, N);
            log("adnRecords.size=" + N);
            for (int i = 0; i < N; i += ADN) {
                loadRecord((AdnRecord) adnRecords.get(i), cursor, i);
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
    }

    private boolean addIccRecordToEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        log("addIccRecordToEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        boolean success = false;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, "", "", name, number, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("addIccRecordToEf: " + success);
        return success;
    }

    private boolean updateIccRecordInEf(int efType, String oldName, String oldNumber, String newName, String newNumber, String pin2, int subId) {
        log("updateIccRecordInEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        boolean success = false;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, oldName, oldNumber, newName, newNumber, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("updateIccRecordInEf: " + success);
        return success;
    }

    private boolean deleteIccRecordFromEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        log("deleteIccRecordFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        boolean success = false;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, name, number, "", "", pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("deleteIccRecordFromEf: " + success);
        return success;
    }

    private void loadRecord(AdnRecord record, MatrixCursor cursor, int id) {
        int i = 0;
        if (!record.isEmpty()) {
            Object[] contact = new Object[FDN_SUB];
            String alphaTag = record.getAlphaTag();
            String number = record.getNumber();
            contact[0] = alphaTag;
            contact[ADN] = number;
            String[] emails = record.getEmails();
            if (emails != null) {
                StringBuilder emailString = new StringBuilder();
                int length = emails.length;
                while (i < length) {
                    String email = emails[i];
                    log("Adding email:" + email);
                    emailString.append(email);
                    emailString.append(",");
                    i += ADN;
                }
                contact[ADN_SUB] = emailString.toString();
            }
            contact[FDN] = Integer.valueOf(id);
            cursor.addRow(contact);
        }
    }

    private void log(String msg) {
        Rlog.d(TAG, "[IccProvider] " + msg);
    }

    private int getRequestSubId(Uri url) {
        log("getRequestSubId url: " + url);
        try {
            return Integer.parseInt(url.getLastPathSegment());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }
}
