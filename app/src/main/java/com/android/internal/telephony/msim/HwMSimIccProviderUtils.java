package com.android.internal.telephony.msim;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.HwIccProviderUtils;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.HwAdnRecordCache;
import com.android.internal.telephony.uicc.HwIccUtils;
import huawei.android.telephony.wrapper.IIccPhoneBookMSimWrapper;
import huawei.android.telephony.wrapper.OptWrapperFactory;
import huawei.android.telephony.wrapper.WrapperFactory;
import java.util.List;

public class HwMSimIccProviderUtils extends HwIccProviderUtils {
    static final String[] ADDRESS_BOOK_COLUMN_NAMES = null;
    private static final int ADN_ALL = 8;
    private static final int ADN_SUB1 = 1;
    private static final int ADN_SUB2 = 2;
    private static final int ADN_SUB3 = 3;
    private static final boolean DBG = true;
    private static final int FDN_SUB1 = 4;
    private static final int FDN_SUB2 = 5;
    private static final int FDN_SUB3 = 6;
    private static final int SDN = 7;
    private static final String TAG = "HwMSimIccProvider";
    private static final UriMatcher URL_MATCHER = null;
    private static volatile HwMSimIccProviderUtils instance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.msim.HwMSimIccProviderUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.msim.HwMSimIccProviderUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.msim.HwMSimIccProviderUtils.<clinit>():void");
    }

    private HwMSimIccProviderUtils(Context context) {
        super(context);
        this.mContext = context;
    }

    public static HwMSimIccProviderUtils getDefault(Context context) {
        if (instance == null) {
            instance = new HwMSimIccProviderUtils(context);
        }
        return instance;
    }

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        log("begin query");
        boolean isQuerybyindex = false;
        AdnRecord searchAdn = new AdnRecord("", "");
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
                log("parsing '" + param + "'");
                String[] pair = param.split("=");
                int length = pair.length;
                if (r0 != ADN_SUB2) {
                    Rlog.e(TAG, "resolve: bad whereClause parameter: " + param);
                } else {
                    String key = pair[0].trim();
                    String val = pair[ADN_SUB1].trim();
                    if ("tag".equals(key)) {
                        tag = normalizeValue(val);
                    } else {
                        if ("number".equals(key)) {
                            number = normalizeValue(val);
                        } else {
                            if (!"emails".equals(key)) {
                                if ("efid".equals(key)) {
                                    sEfid = normalizeValue(val);
                                } else {
                                    if ("index".equals(key)) {
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
                isQuerybyindex = DBG;
            }
            searchAdn = new AdnRecord(efid, index, tag, number);
            Rlog.w("SimProvider", "query tag=" + tag + ",number = xxxxxx  ,efid = " + efid + " ,index = " + index);
        }
        log("isQuerybyindex = " + isQuerybyindex + "; subscription = " + -1);
        int subscription;
        switch (URL_MATCHER.match(url)) {
            case ADN_SUB1 /*1*/:
                log("case ADN_SUB1");
                subscription = WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(0);
                if (subscription == -1) {
                    return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
                }
                if (isQuerybyindex) {
                    return loadFromEf(28474, searchAdn, subscription);
                }
                return loadFromEf(28474, subscription);
            case ADN_SUB2 /*2*/:
                log("case ADN_SUB2");
                subscription = WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(ADN_SUB1);
                if (subscription == -1) {
                    return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
                }
                if (isQuerybyindex) {
                    return loadFromEf(28474, searchAdn, subscription);
                }
                return loadFromEf(28474, subscription);
            case ADN_SUB3 /*3*/:
                log("case ADN_SUB3");
                return loadFromEf(28474, ADN_SUB2);
            case FDN_SUB1 /*4*/:
                log("case FDN_SUB1");
                subscription = WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(0);
                if (subscription == -1) {
                    return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
                }
                return loadFromEf(28475, subscription);
            case FDN_SUB2 /*5*/:
                log("case FDN_SUB2");
                subscription = WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(ADN_SUB1);
                if (subscription == -1) {
                    return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
                }
                return loadFromEf(28475, subscription);
            case FDN_SUB3 /*6*/:
                log("case FDN_SUB3");
                return loadFromEf(28475, ADN_SUB2);
            case SDN /*7*/:
                log("case SDN");
                return loadFromEf(28489, WrapperFactory.getMSimTelephonyManagerWrapper().getDefaultSubscription());
            default:
                log("case default");
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    public String getType(Uri url) {
        switch (URL_MATCHER.match(url)) {
            case ADN_SUB1 /*1*/:
            case ADN_SUB2 /*2*/:
            case ADN_SUB3 /*3*/:
            case FDN_SUB1 /*4*/:
            case FDN_SUB2 /*5*/:
            case FDN_SUB3 /*6*/:
            case SDN /*7*/:
            case ADN_ALL /*8*/:
                return "vnd.android.cursor.dir/sim-contact";
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    public Uri insert(Uri url, ContentValues initialValues) {
        int efType;
        String pin2 = null;
        int subscription = 0;
        log("insert");
        int match = URL_MATCHER.match(url);
        switch (match) {
            case ADN_SUB1 /*1*/:
                subscription = 0;
                efType = 28474;
                break;
            case ADN_SUB2 /*2*/:
                subscription = ADN_SUB1;
                efType = 28474;
                break;
            case ADN_SUB3 /*3*/:
                subscription = ADN_SUB2;
                efType = 28474;
                break;
            case FDN_SUB1 /*4*/:
            case FDN_SUB2 /*5*/:
            case FDN_SUB3 /*6*/:
                efType = 28475;
                pin2 = initialValues.getAsString("pin2");
                Integer temp = initialValues.getAsInteger("subscription");
                if (temp != null) {
                    subscription = temp.intValue();
                    break;
                }
                break;
            default:
                log("insert match unknow url");
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = initialValues.getAsString("tag");
        if (tag == null) {
            tag = "";
        }
        String number = initialValues.getAsString("number");
        if (number == null) {
            number = "";
        }
        log("insert before getSubidFromSlotId subscription = " + subscription);
        log("insert before getSubidFromSlotId match = " + match);
        if (ADN_SUB1 == match || FDN_SUB1 == match) {
            WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(0);
            if (subscription == -1) {
                return null;
            }
        } else if (ADN_SUB2 == match || FDN_SUB2 == match) {
            WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(ADN_SUB1);
            if (subscription == -1) {
                return null;
            }
        }
        log("insert after getSubidFromSlotId subscription = " + subscription);
        if (!addIccRecordToEf(efType, tag, number, null, pin2, subscription)) {
            return null;
        }
        StringBuilder buf = new StringBuilder("content://iccmsim/");
        switch (match) {
            case ADN_SUB1 /*1*/:
                buf.append("adn/");
                break;
            case ADN_SUB2 /*2*/:
                buf.append("adn_sub2/");
                break;
            case ADN_SUB3 /*3*/:
                buf.append("adn_sub3/");
                break;
            case FDN_SUB1 /*4*/:
                buf.append("fdn/");
                break;
            case FDN_SUB2 /*5*/:
                buf.append("fdn_sub2/");
                break;
            case FDN_SUB3 /*6*/:
                buf.append("fdn_sub3/");
                break;
        }
        buf.append(HwAdnRecordCache.s_index.get()).append("/");
        buf.append(HwAdnRecordCache.s_efid.get());
        log("returned string:" + buf.toString());
        Uri resultUri = Uri.parse(buf.toString());
        getContext().getContentResolver().notifyChange(url, null);
        return resultUri;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int delete(Uri url, String where, String[] whereArgs) {
        int subscription;
        int efType;
        log("delete");
        int match = URL_MATCHER.match(url);
        switch (match) {
            case ADN_SUB1 /*1*/:
                subscription = 0;
                efType = 28474;
                break;
            case ADN_SUB2 /*2*/:
                subscription = ADN_SUB1;
                efType = 28474;
                break;
            case ADN_SUB3 /*3*/:
                subscription = ADN_SUB2;
                efType = 28474;
                break;
            case FDN_SUB1 /*4*/:
                subscription = 0;
                efType = 28475;
                break;
            case FDN_SUB2 /*5*/:
                subscription = ADN_SUB1;
                efType = 28475;
                break;
            case FDN_SUB3 /*6*/:
                subscription = ADN_SUB2;
                efType = 28475;
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = null;
        String number = null;
        String[] emails = null;
        Object pin2 = null;
        String sEfid = null;
        String sIndex = null;
        int index = 0;
        boolean z = false;
        String[] tokens = where.split("AND");
        int n = tokens.length;
        while (true) {
            n--;
            if (n >= 0) {
                String param = tokens[n];
                log("parsing '" + param + "'");
                String[] pair = param.split("=");
                if (pair.length != ADN_SUB2) {
                    Rlog.e(TAG, "resolve: bad whereClause parameter: " + param);
                } else {
                    String key = pair[0].trim();
                    String val = pair[ADN_SUB1].trim();
                    if ("tag".equals(key)) {
                        tag = normalizeValue(val);
                    } else if ("number".equals(key)) {
                        number = normalizeValue(val);
                    } else if ("emails".equals(key)) {
                        emails = null;
                    } else if ("pin2".equals(key)) {
                        pin2 = normalizeValue(val);
                    } else if ("efid".equals(key)) {
                        sEfid = normalizeValue(val);
                    } else if ("index".equals(key)) {
                        sIndex = normalizeValue(val);
                    }
                }
            } else if ((efType == FDN_SUB1 || efType == FDN_SUB2 || efType == FDN_SUB3) && TextUtils.isEmpty(pin2)) {
                return 0;
            } else {
                if (ADN_SUB1 == match || FDN_SUB1 == match) {
                    WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(0);
                    if (subscription == -1) {
                        return 0;
                    }
                } else if (ADN_SUB2 == match || FDN_SUB2 == match) {
                    WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(ADN_SUB1);
                    if (subscription == -1) {
                        return 0;
                    }
                }
                if (sEfid != null) {
                }
                if (sIndex != null) {
                }
                z = deleteIccRecordFromEf(efType, tag, number, emails, pin2, subscription);
                log("sEfid=" + sEfid + ";sIndex=" + sIndex + ";efType=" + efType + ";index" + index);
                if (!z) {
                    return 0;
                }
                getContext().getContentResolver().notifyChange(url, null);
                return ADN_SUB1;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int subscription;
        int efType;
        String pin2 = null;
        log("update");
        int match = URL_MATCHER.match(url);
        switch (match) {
            case ADN_SUB1 /*1*/:
                subscription = 0;
                efType = 28474;
                break;
            case ADN_SUB2 /*2*/:
                subscription = ADN_SUB1;
                efType = 28474;
                break;
            case ADN_SUB3 /*3*/:
                subscription = ADN_SUB2;
                efType = 28474;
                break;
            case FDN_SUB1 /*4*/:
            case FDN_SUB2 /*5*/:
            case FDN_SUB3 /*6*/:
                efType = 28475;
                pin2 = values.getAsString("pin2");
                subscription = values.getAsInteger("subscription").intValue();
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = values.getAsString("tag");
        String number = values.getAsString("number");
        String newTag = values.getAsString("newTag");
        if (newTag == null) {
            newTag = "";
        }
        String newNumber = values.getAsString("newNumber");
        if (newNumber == null) {
            newNumber = "";
        }
        String Efid = values.getAsString("efid");
        String sIndex = values.getAsString("index");
        int index = 0;
        boolean z = false;
        if (ADN_SUB1 == match || FDN_SUB1 == match) {
            WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(0);
            if (subscription == -1) {
                return 0;
            }
        } else if (ADN_SUB2 == match || FDN_SUB2 == match) {
            WrapperFactory.getHuaweiTelephonyManagerWrapper().getSubidFromSlotId(ADN_SUB1);
            if (subscription == -1) {
                return 0;
            }
        }
        if (Efid != null) {
        }
        if (sIndex != null) {
        }
        z = updateIccRecordInEf(efType, tag, number, newTag, newNumber, pin2, subscription);
        log("update: Efid=" + Efid + ";sIndex=" + sIndex + ";efType=" + efType + ";index=" + index + ";subscription=" + subscription);
        if (!z) {
            return 0;
        }
        getContext().getContentResolver().notifyChange(url, null);
        return ADN_SUB1;
    }

    private MatrixCursor loadFromEf(int efType, int subscription) {
        List adnRecords = null;
        log("loadFromEf: efType=" + efType + "subscription = " + subscription);
        try {
            IIccPhoneBookMSimWrapper iccIpb = OptWrapperFactory.getIIccPhoneBookMSimWrapper();
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEf(efType, subscription);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES, N);
            log("adnRecords.size=" + N);
            for (int i = 0; i < N; i += ADN_SUB1) {
                loadRecord((AdnRecord) adnRecords.get(i), cursor, i);
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
    }

    protected MatrixCursor loadFromEf(int efType, AdnRecord searchAdn, int subscription) {
        List adnRecords = null;
        log("loadFromEf: efType=" + efType + "subscription = " + subscription);
        try {
            IIccPhoneBookMSimWrapper iccIpb = OptWrapperFactory.getIIccPhoneBookMSimWrapper();
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEf(efType, subscription);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES, N);
            log("adnRecords.size=" + N);
            for (int i = 0; i < N; i += ADN_SUB1) {
                if (HwIccUtils.equalAdn(searchAdn, (AdnRecord) adnRecords.get(i))) {
                    Rlog.w(TAG, "have one by efid and index");
                    loadRecord((AdnRecord) adnRecords.get(i), cursor, i);
                    break;
                }
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
    }

    private boolean addIccRecordToEf(int efType, String name, String number, String[] emails, String pin2, int subscription) {
        log("addIccRecordToEf: efType=" + efType + ", name=" + name + ", number=" + number + ", emails=" + "array" + ", subscription=" + subscription);
        boolean success = false;
        try {
            IIccPhoneBookMSimWrapper iccIpb = OptWrapperFactory.getIIccPhoneBookMSimWrapper();
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearch(efType, "", "", name, number, pin2, subscription);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("addIccRecordToEf: " + success);
        return success;
    }

    private boolean updateIccRecordInEf(int efType, String oldName, String oldNumber, String newName, String newNumber, String pin2, int subscription) {
        log("updateIccRecordInEf: efType=" + efType + ", oldname=" + oldName + ", oldnumber=" + oldNumber + ", newname=" + newName + ", newnumber=" + newNumber + ", subscription=" + subscription);
        boolean success = false;
        try {
            IIccPhoneBookMSimWrapper iccIpb = OptWrapperFactory.getIIccPhoneBookMSimWrapper();
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearch(efType, oldName, oldNumber, newName, newNumber, pin2, subscription);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("updateIccRecordInEf: " + success);
        return success;
    }

    private boolean deleteIccRecordFromEf(int efType, String name, String number, String[] emails, String pin2, int subscription) {
        log("deleteIccRecordFromEf: efType=" + efType + ", name=" + name + ", number=" + number + ", emails=" + "array" + ", pin2=" + pin2 + ", subscription=" + subscription);
        boolean success = false;
        try {
            IIccPhoneBookMSimWrapper iccIpb = OptWrapperFactory.getIIccPhoneBookMSimWrapper();
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearch(efType, name, number, "", "", pin2, subscription);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("deleteIccRecordFromEf: " + success);
        return success;
    }

    private boolean updateIccRecordInEfByIndex(int efType, int index, String newName, String newNumber, String pin2, int subscription) {
        log("updateIccRecordInEfByIndex: efType=" + efType + ", index=" + index + ", newname=" + newName + ", newnumber=" + newNumber + ", subscription=" + subscription);
        boolean success = false;
        try {
            IIccPhoneBookMSimWrapper iccIpb = OptWrapperFactory.getIIccPhoneBookMSimWrapper();
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfByIndex(efType, newName, newNumber, index, pin2, subscription);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("updateIccRecordInEfByIndex: " + success);
        return success;
    }

    private boolean deleteIccRecordFromEfByIndex(int efType, int index, String[] emails, String pin2, int subscription) {
        log("deleteIccRecordFromEfByIndex: efType=" + efType + ", index=" + index + ", emails=" + "array" + ", pin2=" + pin2 + ", subscription=" + subscription);
        boolean success = false;
        try {
            IIccPhoneBookMSimWrapper iccIpb = OptWrapperFactory.getIIccPhoneBookMSimWrapper();
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfByIndex(efType, "", "", index, pin2, subscription);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("deleteIccRecordFromEfByIndex: " + success);
        return success;
    }

    protected void log(String msg) {
        Rlog.d(TAG, "[MSimIccProvider] " + msg);
    }
}
