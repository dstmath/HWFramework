package com.google.android.mms.pdu;

import android.app.ActivityThread;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Settings.Secure;
import android.provider.Telephony.BaseMmsColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Addr;
import android.provider.Telephony.Mms.Part;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.Threads;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HbpcdLookup;
import com.android.internal.telephony.TelephonyEventLog;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.android.mms.pdu.HwCustPduPersister;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MmsException;
import com.google.android.mms.util.DownloadDrmHelper;
import com.google.android.mms.util.DrmConvertSession;
import com.google.android.mms.util.PduCache;
import com.google.android.mms.util.PduCacheEntry;
import com.google.android.mms.util.SqliteWrapper;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

public class PduPersister {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int[] ADDRESS_FIELDS = null;
    private static final HashMap<Integer, Integer> CHARSET_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> CHARSET_COLUMN_NAME_MAP = null;
    private static final boolean DEBUG = false;
    private static final int DEFAULT_SUBSCRIPTION = 0;
    private static final long DUMMY_THREAD_ID = Long.MAX_VALUE;
    private static final HashMap<Integer, Integer> ENCODED_STRING_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> ENCODED_STRING_COLUMN_NAME_MAP = null;
    public static final int LOAD_MODE_MMS_COMMON = 0;
    public static final int LOAD_MODE_MMS_FAVORITES = 1;
    private static final boolean LOCAL_LOGV = false;
    public static final String LOCAL_NUMBER_FROM_DB = "localNumberFromDb";
    private static final HashMap<Integer, Integer> LONG_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> LONG_COLUMN_NAME_MAP = null;
    private static final HashMap<Uri, Integer> MESSAGE_BOX_MAP = null;
    private static final HashMap<Integer, Integer> OCTET_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> OCTET_COLUMN_NAME_MAP = null;
    private static final int PART_COLUMN_CHARSET = 1;
    private static final int PART_COLUMN_CONTENT_DISPOSITION = 2;
    private static final int PART_COLUMN_CONTENT_ID = 3;
    private static final int PART_COLUMN_CONTENT_LOCATION = 4;
    private static final int PART_COLUMN_CONTENT_TYPE = 5;
    private static final int PART_COLUMN_FILENAME = 6;
    private static final int PART_COLUMN_ID = 0;
    private static final int PART_COLUMN_NAME = 7;
    private static final int PART_COLUMN_TEXT = 8;
    private static final String[] PART_PROJECTION = null;
    private static final PduCache PDU_CACHE_INSTANCE = null;
    private static final int PDU_COLUMN_CONTENT_CLASS = 11;
    private static final int PDU_COLUMN_CONTENT_LOCATION = 5;
    private static final int PDU_COLUMN_CONTENT_TYPE = 6;
    private static final int PDU_COLUMN_DATE = 21;
    private static final int PDU_COLUMN_DELIVERY_REPORT = 12;
    private static final int PDU_COLUMN_DELIVERY_TIME = 22;
    private static final int PDU_COLUMN_EXPIRY = 23;
    private static final int PDU_COLUMN_ID = 0;
    private static final int PDU_COLUMN_MESSAGE_BOX = 1;
    private static final int PDU_COLUMN_MESSAGE_CLASS = 7;
    private static final int PDU_COLUMN_MESSAGE_ID = 8;
    private static final int PDU_COLUMN_MESSAGE_SIZE = 24;
    private static final int PDU_COLUMN_MESSAGE_TYPE = 13;
    private static final int PDU_COLUMN_MMS_VERSION = 14;
    private static final int PDU_COLUMN_PRIORITY = 15;
    private static final int PDU_COLUMN_READ_REPORT = 16;
    private static final int PDU_COLUMN_READ_STATUS = 17;
    private static final int PDU_COLUMN_REPORT_ALLOWED = 18;
    private static final int PDU_COLUMN_RESPONSE_TEXT = 9;
    private static final int PDU_COLUMN_RETRIEVE_STATUS = 19;
    private static final int PDU_COLUMN_RETRIEVE_TEXT = 3;
    private static final int PDU_COLUMN_RETRIEVE_TEXT_CHARSET = 26;
    private static final int PDU_COLUMN_STATUS = 20;
    private static final int PDU_COLUMN_SUBJECT = 4;
    private static final int PDU_COLUMN_SUBJECT_CHARSET = 25;
    private static final int PDU_COLUMN_THREAD_ID = 2;
    private static final int PDU_COLUMN_TRANSACTION_ID = 10;
    private static final String[] PDU_PROJECTION = null;
    public static final int PROC_STATUS_COMPLETED = 3;
    public static final int PROC_STATUS_PERMANENTLY_FAILURE = 2;
    public static final int PROC_STATUS_TRANSIENT_FAILURE = 1;
    private static final String TAG = "PduPersister";
    public static final String TEMPORARY_DRM_OBJECT_URI = "content://mms/9223372036854775807/part";
    private static final HashMap<Integer, Integer> TEXT_STRING_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> TEXT_STRING_COLUMN_NAME_MAP = null;
    private static PduPersister sPersister;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final DrmManagerClient mDrmManagerClient;
    HwCustPduPersister mHwCustPduPersister;
    private final TelephonyManager mTelephonyManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.mms.pdu.PduPersister.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.mms.pdu.PduPersister.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.mms.pdu.PduPersister.<clinit>():void");
    }

    private PduPersister(Context context) {
        this.mHwCustPduPersister = (HwCustPduPersister) HwCustUtils.createObj(HwCustPduPersister.class, new Object[PDU_COLUMN_ID]);
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mDrmManagerClient = new DrmManagerClient(context);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    public static PduPersister getPduPersister(Context context) {
        Context priorContext = null;
        ActivityThread actThread = ActivityThread.currentActivityThread();
        if (actThread != null) {
            priorContext = actThread.getApplication();
        }
        if (priorContext == null) {
            priorContext = context.getApplicationContext();
        }
        if (sPersister == null) {
            sPersister = new PduPersister(priorContext);
        } else if (!priorContext.equals(sPersister.mContext)) {
            Log.w(TAG, "PduPersister create a new one. may cause memory leak");
            sPersister.release();
            sPersister = new PduPersister(priorContext);
        }
        return sPersister;
    }

    private void setEncodedStringValueToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if (s != null && s.length() > 0) {
            headers.setEncodedStringValue(new EncodedStringValue(c.getInt(((Integer) CHARSET_COLUMN_INDEX_MAP.get(Integer.valueOf(mapColumn))).intValue()), getBytes(s)), mapColumn);
        }
    }

    private void setTextStringToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if (s != null) {
            headers.setTextString(getBytes(s), mapColumn);
        }
    }

    private void setOctetToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) throws InvalidHeaderValueException {
        if (!c.isNull(columnIndex)) {
            headers.setOctet(c.getInt(columnIndex), mapColumn);
        }
    }

    private void setLongToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        if (!c.isNull(columnIndex)) {
            headers.setLongInteger(c.getLong(columnIndex), mapColumn);
        }
    }

    private Integer getIntegerFromPartColumn(Cursor c, int columnIndex) {
        if (c.isNull(columnIndex)) {
            return null;
        }
        return Integer.valueOf(c.getInt(columnIndex));
    }

    private byte[] getByteArrayFromPartColumn(Cursor c, int columnIndex) {
        if (c.isNull(columnIndex)) {
            return null;
        }
        return getBytes(c.getString(columnIndex));
    }

    private PduPart[] loadParts(long msgId, int loadType) throws MmsException {
        Cursor c;
        if (loadType == PROC_STATUS_TRANSIENT_FAILURE) {
            c = SqliteWrapper.query(this.mContext, this.mContentResolver, Uri.parse("content://fav-mms/" + msgId + "/part"), PART_PROJECTION, null, null, null);
        } else {
            c = SqliteWrapper.query(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + msgId + "/part"), PART_PROJECTION, null, null, null);
        }
        if (c != null) {
            if (c.getCount() != 0) {
                PduPart[] parts = new PduPart[c.getCount()];
                int partIdx = PDU_COLUMN_ID;
                while (c.moveToNext()) {
                    PduPart part = new PduPart();
                    Integer charset = getIntegerFromPartColumn(c, PROC_STATUS_TRANSIENT_FAILURE);
                    if (charset != null) {
                        part.setCharset(charset.intValue());
                    }
                    byte[] contentDisposition = getByteArrayFromPartColumn(c, PROC_STATUS_PERMANENTLY_FAILURE);
                    if (contentDisposition != null) {
                        part.setContentDisposition(contentDisposition);
                    }
                    byte[] contentId = getByteArrayFromPartColumn(c, PROC_STATUS_COMPLETED);
                    if (contentId != null) {
                        part.setContentId(contentId);
                    }
                    byte[] contentLocation = getByteArrayFromPartColumn(c, PDU_COLUMN_SUBJECT);
                    if (contentLocation != null) {
                        part.setContentLocation(contentLocation);
                    }
                    byte[] contentType = getByteArrayFromPartColumn(c, PDU_COLUMN_CONTENT_LOCATION);
                    if (contentType != null) {
                        Uri partURI;
                        part.setContentType(contentType);
                        byte[] fileName = getByteArrayFromPartColumn(c, PDU_COLUMN_CONTENT_TYPE);
                        if (fileName != null) {
                            part.setFilename(fileName);
                        }
                        byte[] name = getByteArrayFromPartColumn(c, PDU_COLUMN_MESSAGE_CLASS);
                        if (name != null) {
                            part.setName(name);
                        }
                        long partId = c.getLong(PDU_COLUMN_ID);
                        if (loadType == PROC_STATUS_TRANSIENT_FAILURE) {
                            partURI = Uri.parse("content://fav-mms/part/" + partId);
                        } else {
                            partURI = Uri.parse("content://mms/part/" + partId);
                        }
                        part.setDataUri(partURI);
                        String type = toIsoString(contentType);
                        if (!(ContentType.isImageType(type) || ContentType.isAudioType(type) || ContentType.isVideoType(type))) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            InputStream inputStream = null;
                            if (ContentType.TEXT_PLAIN.equals(type) || ContentType.APP_SMIL.equals(type) || ContentType.TEXT_HTML.equals(type)) {
                                String text = c.getString(PDU_COLUMN_MESSAGE_ID);
                                if (charset != null && charset.intValue() == PROC_STATUS_COMPLETED) {
                                    charset = Integer.valueOf(CharacterSets.UTF_8);
                                }
                                byte[] blob;
                                if (!"true".equals("true")) {
                                    if (text == null) {
                                        text = "";
                                    }
                                    blob = new EncodedStringValue(text).getTextString();
                                    baos.write(blob, PDU_COLUMN_ID, blob.length);
                                } else if (charset == null || charset.intValue() == 0) {
                                    if (text == null) {
                                        text = "";
                                    }
                                    blob = new EncodedStringValue(text).getTextString();
                                    baos.write(blob, PDU_COLUMN_ID, blob.length);
                                } else {
                                    try {
                                        int intValue = charset.intValue();
                                        if (text == null) {
                                            text = "";
                                        }
                                        blob = new EncodedStringValue(intValue, text).getTextString();
                                        baos.write(blob, PDU_COLUMN_ID, blob.length);
                                    } catch (Throwable e) {
                                        Log.e(TAG, "Failed to EncodedStringValue: ", e);
                                    } catch (Throwable e2) {
                                        Log.e(TAG, "Failed to EncodedStringValue: ", e2);
                                    } catch (Throwable th) {
                                        if (c != null) {
                                            c.close();
                                        }
                                    }
                                }
                            } else {
                                try {
                                    inputStream = this.mContentResolver.openInputStream(partURI);
                                    byte[] buffer = new byte[256];
                                    for (int len = inputStream.read(buffer); len >= 0; len = inputStream.read(buffer)) {
                                        baos.write(buffer, PDU_COLUMN_ID, len);
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable e3) {
                                            Log.e(TAG, "Failed to close stream", e3);
                                        }
                                    }
                                } catch (Throwable e32) {
                                    Log.e(TAG, "Failed to load part data", e32);
                                    c.close();
                                    throw new MmsException(e32);
                                } catch (Throwable th2) {
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable e322) {
                                            Log.e(TAG, "Failed to close stream", e322);
                                        }
                                    }
                                }
                            }
                            if (baos != null) {
                                part.setData(baos.toByteArray());
                            } else {
                                continue;
                            }
                        }
                        int partIdx2 = partIdx + PROC_STATUS_TRANSIENT_FAILURE;
                        parts[partIdx] = part;
                        partIdx = partIdx2;
                    } else {
                        throw new MmsException("Content-Type must be set.");
                    }
                }
                if (c != null) {
                    c.close();
                }
                return parts;
            }
        }
        if (c != null) {
            c.close();
        }
        return null;
    }

    private void loadAddress(long msgId, PduHeaders headers, int loadType) {
        Cursor c;
        Context context;
        ContentResolver contentResolver;
        Uri parse;
        String[] strArr;
        if (loadType == PROC_STATUS_TRANSIENT_FAILURE) {
            context = this.mContext;
            contentResolver = this.mContentResolver;
            parse = Uri.parse("content://fav-mms/" + msgId + "/addr");
            strArr = new String[PROC_STATUS_COMPLETED];
            strArr[PDU_COLUMN_ID] = TextBasedSmsColumns.ADDRESS;
            strArr[PROC_STATUS_TRANSIENT_FAILURE] = Addr.CHARSET;
            strArr[PROC_STATUS_PERMANENTLY_FAILURE] = TelephonyEventLog.DATA_KEY_DATA_CALL_TYPE;
            c = SqliteWrapper.query(context, contentResolver, parse, strArr, null, null, null);
        } else {
            context = this.mContext;
            contentResolver = this.mContentResolver;
            parse = Uri.parse("content://mms/" + msgId + "/addr");
            strArr = new String[PROC_STATUS_COMPLETED];
            strArr[PDU_COLUMN_ID] = TextBasedSmsColumns.ADDRESS;
            strArr[PROC_STATUS_TRANSIENT_FAILURE] = Addr.CHARSET;
            strArr[PROC_STATUS_PERMANENTLY_FAILURE] = TelephonyEventLog.DATA_KEY_DATA_CALL_TYPE;
            c = SqliteWrapper.query(context, contentResolver, parse, strArr, null, null, null);
        }
        if (c != null) {
            while (c.moveToNext()) {
                try {
                    String addr = c.getString(PDU_COLUMN_ID);
                    if (!TextUtils.isEmpty(addr)) {
                        int addrType = c.getInt(PROC_STATUS_PERMANENTLY_FAILURE);
                        switch (addrType) {
                            case PduPart.P_DISPOSITION_ATTACHMENT /*129*/:
                            case PduPart.P_LEVEL /*130*/:
                            case PduPart.P_NAME /*151*/:
                                headers.appendEncodedStringValue(new EncodedStringValue(c.getInt(PROC_STATUS_TRANSIENT_FAILURE), getBytes(addr)), addrType);
                                break;
                            case PduPart.P_CT_MR_TYPE /*137*/:
                                headers.setEncodedStringValue(new EncodedStringValue(c.getInt(PROC_STATUS_TRANSIENT_FAILURE), getBytes(addr)), addrType);
                                break;
                            default:
                                Log.e(TAG, "Unknown address type: " + addrType);
                                break;
                        }
                    }
                } finally {
                    c.close();
                }
            }
        }
    }

    public GenericPdu load(Uri uri) throws MmsException {
        Throwable th;
        PduCacheEntry cacheEntry = null;
        int loadType = PDU_COLUMN_ID;
        try {
            synchronized (PDU_CACHE_INSTANCE) {
                try {
                    if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                        PDU_CACHE_INSTANCE.wait();
                        cacheEntry = (PduCacheEntry) PDU_CACHE_INSTANCE.get(uri);
                        if (cacheEntry != null) {
                            GenericPdu pdu = cacheEntry.getPdu();
                            synchronized (PDU_CACHE_INSTANCE) {
                                PDU_CACHE_INSTANCE.setUpdating(uri, LOCAL_LOGV);
                                PDU_CACHE_INSTANCE.notifyAll();
                            }
                            return pdu;
                        }
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "load: ", e);
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
                PduCacheEntry cacheEntry2 = cacheEntry;
                try {
                    PDU_CACHE_INSTANCE.setUpdating(uri, true);
                } catch (Throwable th3) {
                    th = th3;
                    cacheEntry = cacheEntry2;
                    throw th;
                }
                Cursor c;
                try {
                    if (uri.toString() != null && uri.toString().contains("content://fav-mms")) {
                        loadType = PROC_STATUS_TRANSIENT_FAILURE;
                    }
                    c = SqliteWrapper.query(this.mContext, this.mContentResolver, uri, PDU_PROJECTION, null, null, null);
                    PduHeaders headers = new PduHeaders();
                    long msgId = ContentUris.parseId(uri);
                    if (c != null) {
                        if (c.getCount() == PROC_STATUS_TRANSIENT_FAILURE) {
                            if (c.moveToFirst()) {
                                int msgBox = c.getInt(PROC_STATUS_TRANSIENT_FAILURE);
                                long threadId = c.getLong(PROC_STATUS_PERMANENTLY_FAILURE);
                                for (Entry<Integer, Integer> e2 : ENCODED_STRING_COLUMN_INDEX_MAP.entrySet()) {
                                    setEncodedStringValueToHeaders(c, ((Integer) e2.getValue()).intValue(), headers, ((Integer) e2.getKey()).intValue());
                                }
                                for (Entry<Integer, Integer> e22 : TEXT_STRING_COLUMN_INDEX_MAP.entrySet()) {
                                    setTextStringToHeaders(c, ((Integer) e22.getValue()).intValue(), headers, ((Integer) e22.getKey()).intValue());
                                }
                                for (Entry<Integer, Integer> e222 : OCTET_COLUMN_INDEX_MAP.entrySet()) {
                                    setOctetToHeaders(c, ((Integer) e222.getValue()).intValue(), headers, ((Integer) e222.getKey()).intValue());
                                }
                                for (Entry<Integer, Integer> e2222 : LONG_COLUMN_INDEX_MAP.entrySet()) {
                                    setLongToHeaders(c, ((Integer) e2222.getValue()).intValue(), headers, ((Integer) e2222.getKey()).intValue());
                                }
                                if (c != null) {
                                    c.close();
                                }
                                if (msgId == -1) {
                                    throw new MmsException("Error! ID of the message: -1.");
                                }
                                loadAddress(msgId, headers, loadType);
                                int msgType = headers.getOctet(PduPart.P_DEP_COMMENT);
                                PduBody body = new PduBody();
                                if (msgType == 132 || msgType == 128) {
                                    PduPart[] parts = loadParts(msgId, loadType);
                                    if (parts != null) {
                                        int partsNum = parts.length;
                                        for (int i = PDU_COLUMN_ID; i < partsNum; i += PROC_STATUS_TRANSIENT_FAILURE) {
                                            body.addPart(parts[i]);
                                        }
                                    }
                                }
                                GenericPdu sendReq;
                                switch (msgType) {
                                    case PduPart.P_Q /*128*/:
                                        sendReq = new SendReq(headers, body);
                                        break;
                                    case PduPart.P_DISPOSITION_ATTACHMENT /*129*/:
                                    case PduPart.P_CT_MR_TYPE /*137*/:
                                    case PduPart.P_DEP_START /*138*/:
                                    case PduPart.P_DEP_START_INFO /*139*/:
                                    case PduPart.P_DEP_COMMENT /*140*/:
                                    case PduPart.P_DEP_DOMAIN /*141*/:
                                    case PduPart.P_MAX_AGE /*142*/:
                                    case PduPart.P_DEP_PATH /*143*/:
                                    case PduPart.P_SECURE /*144*/:
                                    case PduPart.P_SEC /*145*/:
                                    case PduPart.P_MAC /*146*/:
                                    case PduPart.P_CREATION_DATE /*147*/:
                                    case PduPart.P_MODIFICATION_DATE /*148*/:
                                    case PduPart.P_READ_DATE /*149*/:
                                    case PduPart.P_SIZE /*150*/:
                                    case PduPart.P_NAME /*151*/:
                                        throw new MmsException("Unsupported PDU type: " + Integer.toHexString(msgType));
                                    case PduPart.P_LEVEL /*130*/:
                                        sendReq = new NotificationInd(headers);
                                        break;
                                    case PduPart.P_TYPE /*131*/:
                                        sendReq = new NotifyRespInd(headers);
                                        break;
                                    case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
                                        sendReq = new RetrieveConf(headers, body);
                                        break;
                                    case PduPart.P_DEP_NAME /*133*/:
                                        sendReq = new AcknowledgeInd(headers);
                                        break;
                                    case PduPart.P_DEP_FILENAME /*134*/:
                                        sendReq = new DeliveryInd(headers);
                                        break;
                                    case PduPart.P_DIFFERENCES /*135*/:
                                        sendReq = new ReadRecInd(headers);
                                        break;
                                    case PduPart.P_PADDING /*136*/:
                                        sendReq = new ReadOrigInd(headers);
                                        break;
                                    default:
                                        throw new MmsException("Unrecognized PDU type: " + Integer.toHexString(msgType));
                                }
                                synchronized (PDU_CACHE_INSTANCE) {
                                    if (pdu != null) {
                                        try {
                                            if (!-assertionsDisabled) {
                                                if ((PDU_CACHE_INSTANCE.get(uri) == null ? PROC_STATUS_TRANSIENT_FAILURE : null) == null) {
                                                    throw new AssertionError();
                                                }
                                            }
                                            try {
                                                PDU_CACHE_INSTANCE.put(uri, new PduCacheEntry(pdu, msgBox, threadId));
                                            } catch (Throwable th4) {
                                                th = th4;
                                                throw th;
                                            }
                                        } catch (Throwable th5) {
                                            th = th5;
                                            cacheEntry = cacheEntry2;
                                            throw th;
                                        }
                                    }
                                    PDU_CACHE_INSTANCE.setUpdating(uri, LOCAL_LOGV);
                                    PDU_CACHE_INSTANCE.notifyAll();
                                    return pdu;
                                }
                            }
                        }
                    }
                    throw new MmsException("Bad uri: " + uri);
                } catch (Throwable th6) {
                    th = th6;
                    cacheEntry = cacheEntry2;
                    synchronized (PDU_CACHE_INSTANCE) {
                        PDU_CACHE_INSTANCE.setUpdating(uri, LOCAL_LOGV);
                        PDU_CACHE_INSTANCE.notifyAll();
                    }
                    throw th;
                }
            }
        } catch (Throwable th7) {
            th = th7;
            synchronized (PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri, LOCAL_LOGV);
                PDU_CACHE_INSTANCE.notifyAll();
            }
            throw th;
        }
    }

    private void persistAddress(long msgId, int type, EncodedStringValue[] array) {
        ContentValues[] allValues = new ContentValues[array.length];
        Uri uri = Uri.parse("content://mms/" + msgId + "/addr");
        int i = PDU_COLUMN_ID;
        int length = array.length;
        int idx = PDU_COLUMN_ID;
        while (i < length) {
            EncodedStringValue addr = array[i];
            ContentValues values = new ContentValues(PROC_STATUS_COMPLETED);
            values.put(TextBasedSmsColumns.ADDRESS, toIsoString(addr.getTextString()));
            values.put(Addr.CHARSET, Integer.valueOf(addr.getCharacterSet()));
            values.put(TelephonyEventLog.DATA_KEY_DATA_CALL_TYPE, Integer.valueOf(type));
            int idx2 = idx + PROC_STATUS_TRANSIENT_FAILURE;
            allValues[idx] = values;
            i += PROC_STATUS_TRANSIENT_FAILURE;
            idx = idx2;
        }
        this.mContext.getContentResolver().bulkInsert(uri, allValues);
    }

    private static String getPartContentType(PduPart part) {
        return part.getContentType() == null ? null : toIsoString(part.getContentType());
    }

    public Uri persistPart(PduPart part, long msgId, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        Uri uri = Uri.parse("content://mms/" + msgId + "/part");
        ContentValues values = new ContentValues(PDU_COLUMN_MESSAGE_ID);
        int charset = part.getCharset();
        if (charset != 0) {
            values.put(Part.CHARSET, Integer.valueOf(charset));
        } else {
            values.put(Part.CHARSET, Integer.valueOf(CharacterSets.UTF_8));
        }
        String contentType = getPartContentType(part);
        if (contentType != null) {
            if (ContentType.IMAGE_JPG.equals(contentType)) {
                contentType = ContentType.IMAGE_JPEG;
            }
            if (!ContentType.isSupportedType(contentType)) {
                String keyWord = null;
                if (part.getName() != null) {
                    keyWord = toIsoString(part.getName());
                } else if (part.getContentLocation() != null) {
                    keyWord = toIsoString(part.getContentLocation());
                }
                if (keyWord != null && keyWord.toLowerCase(Locale.US).endsWith(".vcs")) {
                    contentType = ContentType.TEXT_VCALENDAR;
                }
                if (keyWord != null && keyWord.toLowerCase(Locale.US).endsWith(".vcf")) {
                    contentType = ContentType.TEXT_VCARD;
                }
            }
            values.put(Part.CONTENT_TYPE, contentType);
            if (ContentType.APP_SMIL.equals(contentType)) {
                values.put(Part.SEQ, Integer.valueOf(-1));
            }
            if (part.getFilename() != null) {
                values.put(Part.FILENAME, toIsoString(part.getFilename()));
            }
            if (part.getName() != null) {
                values.put(Part.NAME, toIsoString(part.getName()));
            }
            if (part.getContentDisposition() != null) {
                values.put(Part.CONTENT_DISPOSITION, toIsoString(part.getContentDisposition()));
            }
            if (part.getContentId() != null) {
                values.put(TelephonyEventLog.DATA_KEY_DATA_CALL_CID, toIsoString(part.getContentId()));
            }
            if (part.getContentLocation() != null) {
                values.put(Part.CONTENT_LOCATION, toIsoString(part.getContentLocation()));
            }
            Uri res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
            if (res == null) {
                throw new MmsException("Failed to persist part, return null.");
            }
            persistData(part, res, contentType, preOpenedFiles);
            part.setDataUri(res);
            return res;
        }
        throw new MmsException("MIME type of the part must be set.");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void persistData(PduPart part, Uri uri, String contentType, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        Throwable e;
        Throwable e2;
        Throwable th;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        DrmConvertSession drmConvertSession = null;
        String str = null;
        try {
            byte[] data = part.getData();
            if (!ContentType.TEXT_PLAIN.equals(contentType) && !ContentType.APP_SMIL.equals(contentType) && !ContentType.TEXT_HTML.equals(contentType)) {
                boolean isDrm = DownloadDrmHelper.isDrmConvertNeeded(contentType);
                if (isDrm) {
                    if (uri != null) {
                        try {
                            str = convertUriToPath(this.mContext, uri);
                            if (new File(str).length() > 0) {
                                return;
                            }
                        } catch (Throwable e3) {
                            Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e3);
                        }
                    }
                    drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                    if (drmConvertSession == null) {
                        throw new MmsException("Mimetype " + contentType + " can not be converted.");
                    }
                }
                outputStream = this.mContentResolver.openOutputStream(uri);
                Uri dataUri;
                byte[] buffer;
                int len;
                byte[] convertedData;
                if (data == null) {
                    dataUri = part.getDataUri();
                    if (dataUri == null || dataUri == uri) {
                        Log.w(TAG, "Can't find data for this part.");
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (Throwable e4) {
                                Log.e(TAG, "IOException while closing: " + outputStream, e4);
                            }
                        }
                        if (drmConvertSession != null) {
                            drmConvertSession.close(str);
                            SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + new File(str).getName()), new ContentValues(PDU_COLUMN_ID), null, null);
                        }
                        return;
                    }
                    if (preOpenedFiles != null) {
                        if (preOpenedFiles.containsKey(dataUri)) {
                            inputStream = (InputStream) preOpenedFiles.get(dataUri);
                        }
                    }
                    if (inputStream == null) {
                        inputStream = this.mContentResolver.openInputStream(dataUri);
                    }
                    buffer = new byte[SmsCbConstants.SERIAL_NUMBER_ETWS_ACTIVATE_POPUP];
                    while (true) {
                        len = inputStream.read(buffer);
                        if (len != -1) {
                            if (isDrm) {
                                convertedData = drmConvertSession.convert(buffer, len);
                                if (convertedData == null) {
                                    break;
                                }
                                outputStream.write(convertedData, PDU_COLUMN_ID, convertedData.length);
                            } else {
                                outputStream.write(buffer, PDU_COLUMN_ID, len);
                            }
                        } else {
                            break;
                        }
                    }
                    throw new MmsException("Error converting drm data.");
                } else if (isDrm) {
                    dataUri = uri;
                    InputStream byteArrayInputStream = new ByteArrayInputStream(data);
                    try {
                        buffer = new byte[SmsCbConstants.SERIAL_NUMBER_ETWS_ACTIVATE_POPUP];
                        while (true) {
                            len = byteArrayInputStream.read(buffer);
                            if (len == -1) {
                                break;
                            }
                            convertedData = drmConvertSession.convert(buffer, len);
                            if (convertedData == null) {
                                break;
                            }
                            outputStream.write(convertedData, PDU_COLUMN_ID, convertedData.length);
                        }
                        throw new MmsException("Error converting drm data.");
                    } catch (FileNotFoundException e5) {
                        e2 = e5;
                        inputStream = byteArrayInputStream;
                        try {
                            Log.e(TAG, "Failed to open Input/Output stream.", e2);
                            throw new MmsException(e2);
                        } catch (Throwable th2) {
                            th = th2;
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (Throwable e42) {
                                    Log.e(TAG, "IOException while closing: " + outputStream, e42);
                                }
                            }
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (Throwable e422) {
                                    Log.e(TAG, "IOException while closing: " + inputStream, e422);
                                }
                            }
                            if (drmConvertSession != null) {
                                drmConvertSession.close(str);
                                SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + new File(str).getName()), new ContentValues(PDU_COLUMN_ID), null, null);
                            }
                            throw th;
                        }
                    } catch (IOException e6) {
                        e422 = e6;
                        inputStream = byteArrayInputStream;
                        Log.e(TAG, "Failed to read/write data.", e422);
                        throw new MmsException(e422);
                    } catch (Throwable th22) {
                        th = th22;
                        inputStream = byteArrayInputStream;
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (drmConvertSession != null) {
                            drmConvertSession.close(str);
                            SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + new File(str).getName()), new ContentValues(PDU_COLUMN_ID), null, null);
                        }
                        throw th;
                    }
                } else {
                    outputStream.write(data);
                }
            } else if ("true".equals("true")) {
                int charset = part.getCharset();
                cv = new ContentValues();
                if (charset != 0) {
                    cv.put(Part.TEXT, data != null ? new EncodedStringValue(charset, data).getString() : "");
                } else {
                    String string;
                    String str2 = Part.TEXT;
                    if (data != null) {
                        string = new EncodedStringValue(data).getString();
                    } else {
                        string = "";
                    }
                    cv.put(str2, string);
                }
                if (this.mContentResolver.update(uri, cv, null, null) != PROC_STATUS_TRANSIENT_FAILURE) {
                    throw new MmsException("unable to update " + uri.toString());
                }
            } else {
                cv = new ContentValues();
                cv.put(Part.TEXT, new EncodedStringValue(data).getString());
                if (this.mContentResolver.update(uri, cv, null, null) != PROC_STATUS_TRANSIENT_FAILURE) {
                    throw new MmsException("unable to update " + uri.toString());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable e4222) {
                    Log.e(TAG, "IOException while closing: " + outputStream, e4222);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable e42222) {
                    Log.e(TAG, "IOException while closing: " + inputStream, e42222);
                }
            }
            if (drmConvertSession != null) {
                drmConvertSession.close(str);
                SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + new File(str).getName()), new ContentValues(PDU_COLUMN_ID), null, null);
            }
        } catch (FileNotFoundException e7) {
            e2 = e7;
            Log.e(TAG, "Failed to open Input/Output stream.", e2);
            throw new MmsException(e2);
        } catch (IOException e8) {
            e42222 = e8;
            Log.e(TAG, "Failed to read/write data.", e42222);
            throw new MmsException(e42222);
        }
    }

    public static String convertUriToPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("") || scheme.equals("file")) {
            return uri.getPath();
        }
        if (scheme.equals("http")) {
            return uri.toString();
        }
        if (scheme.equals("content")) {
            String[] projection = new String[PROC_STATUS_TRANSIENT_FAILURE];
            projection[PDU_COLUMN_ID] = Part._DATA;
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (!(cursor == null || cursor.getCount() == 0)) {
                    if (cursor.moveToFirst()) {
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(Part._DATA));
                        if (cursor == null) {
                            return path;
                        }
                        cursor.close();
                        return path;
                    }
                }
                throw new IllegalArgumentException("Given Uri could not be found in media store");
            } catch (SQLiteException e) {
                throw new IllegalArgumentException("Given Uri is not formatted in a way so that it can be found in media store.");
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            throw new IllegalArgumentException("Given Uri scheme is not supported");
        }
    }

    private void updateAddress(long msgId, int type, EncodedStringValue[] array) {
        SqliteWrapper.delete(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + msgId + "/addr"), "type=" + type, null);
        persistAddress(msgId, type, array);
    }

    public void updateHeaders(Uri uri, SendReq sendReq) {
        synchronized (PDU_CACHE_INSTANCE) {
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                try {
                    PDU_CACHE_INSTANCE.wait();
                } catch (Throwable e) {
                    Log.e(TAG, "updateHeaders: ", e);
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri);
        ContentValues values = new ContentValues(PDU_COLUMN_TRANSACTION_ID);
        byte[] contentType = sendReq.getContentType();
        if (contentType != null) {
            values.put(BaseMmsColumns.CONTENT_TYPE, toIsoString(contentType));
        }
        long date = sendReq.getDate();
        if (date != -1) {
            values.put(ThreadsColumns.DATE, Long.valueOf(date));
        }
        int deliveryReport = sendReq.getDeliveryReport();
        if (deliveryReport != 0) {
            values.put(BaseMmsColumns.DELIVERY_REPORT, Integer.valueOf(deliveryReport));
        }
        long expiry = sendReq.getExpiry();
        if (expiry != -1) {
            values.put(BaseMmsColumns.EXPIRY, Long.valueOf(expiry));
        }
        byte[] msgClass = sendReq.getMessageClass();
        if (msgClass != null) {
            values.put(BaseMmsColumns.MESSAGE_CLASS, toIsoString(msgClass));
        }
        int priority = sendReq.getPriority();
        if (priority != 0) {
            values.put(BaseMmsColumns.PRIORITY, Integer.valueOf(priority));
        }
        int readReport = sendReq.getReadReport();
        if (readReport != 0) {
            values.put(BaseMmsColumns.READ_REPORT, Integer.valueOf(readReport));
        }
        byte[] transId = sendReq.getTransactionId();
        if (transId != null) {
            values.put(BaseMmsColumns.TRANSACTION_ID, toIsoString(transId));
        }
        EncodedStringValue subject = sendReq.getSubject();
        if (subject != null) {
            values.put(BaseMmsColumns.SUBJECT, toIsoString(subject.getTextString()));
            values.put(BaseMmsColumns.SUBJECT_CHARSET, Integer.valueOf(subject.getCharacterSet()));
        } else {
            values.put(BaseMmsColumns.SUBJECT, "");
        }
        long messageSize = sendReq.getMessageSize();
        if (messageSize > 0) {
            values.put(BaseMmsColumns.MESSAGE_SIZE, Long.valueOf(messageSize));
        }
        PduHeaders headers = sendReq.getPduHeaders();
        HashSet<String> recipients = new HashSet();
        int[] iArr = ADDRESS_FIELDS;
        int length = iArr.length;
        for (int i = PDU_COLUMN_ID; i < length; i += PROC_STATUS_TRANSIENT_FAILURE) {
            EncodedStringValue v;
            int addrType = iArr[i];
            EncodedStringValue[] array = null;
            if (addrType == PduPart.P_CT_MR_TYPE) {
                v = headers.getEncodedStringValue(addrType);
                if (v != null) {
                    array = new EncodedStringValue[PROC_STATUS_TRANSIENT_FAILURE];
                    array[PDU_COLUMN_ID] = v;
                }
            } else {
                array = headers.getEncodedStringValues(addrType);
            }
            if (array != null) {
                updateAddress(ContentUris.parseId(uri), addrType, array);
                if (addrType == PduPart.P_NAME) {
                    int length2 = array.length;
                    for (int i2 = PDU_COLUMN_ID; i2 < length2; i2 += PROC_STATUS_TRANSIENT_FAILURE) {
                        v = array[i2];
                        if (v != null) {
                            recipients.add(v.getString());
                        }
                    }
                }
            }
        }
        if (!recipients.isEmpty()) {
            values.put(TextBasedSmsColumns.THREAD_ID, Long.valueOf(Threads.getOrCreateThreadId(this.mContext, (Set) recipients)));
        }
        SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
    }

    private void updatePart(Uri uri, PduPart part, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        ContentValues values = new ContentValues(PDU_COLUMN_MESSAGE_CLASS);
        int charset = part.getCharset();
        if (charset != 0) {
            values.put(Part.CHARSET, Integer.valueOf(charset));
        }
        if (part.getContentType() != null) {
            String contentType = toIsoString(part.getContentType());
            values.put(Part.CONTENT_TYPE, contentType);
            if (part.getFilename() != null) {
                values.put(Part.FILENAME, toIsoString(part.getFilename()));
            }
            if (part.getName() != null) {
                values.put(Part.NAME, toIsoString(part.getName()));
            }
            if (part.getContentDisposition() != null) {
                values.put(Part.CONTENT_DISPOSITION, toIsoString(part.getContentDisposition()));
            }
            if (part.getContentId() != null) {
                values.put(TelephonyEventLog.DATA_KEY_DATA_CALL_CID, toIsoString(part.getContentId()));
            }
            if (part.getContentLocation() != null) {
                values.put(Part.CONTENT_LOCATION, toIsoString(part.getContentLocation()));
            }
            SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
            if (part.getData() != null || uri != part.getDataUri()) {
                persistData(part, uri, contentType, preOpenedFiles);
                return;
            }
            return;
        }
        throw new MmsException("MIME type of the part must be set.");
    }

    public void updateParts(Uri uri, PduBody body, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        PduPart pduPart;
        try {
            PduPart part;
            synchronized (PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    try {
                        PDU_CACHE_INSTANCE.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "updateParts: ", e);
                    }
                    PduCacheEntry cacheEntry = (PduCacheEntry) PDU_CACHE_INSTANCE.get(uri);
                    if (cacheEntry != null) {
                        ((MultimediaMessagePdu) cacheEntry.getPdu()).setBody(body);
                    }
                }
                PDU_CACHE_INSTANCE.setUpdating(uri, true);
            }
            ArrayList<PduPart> toBeCreated = new ArrayList();
            HashMap<Uri, PduPart> toBeUpdated = new HashMap();
            int partsNum = body.getPartsNum();
            StringBuilder filter = new StringBuilder().append('(');
            for (int i = PDU_COLUMN_ID; i < partsNum; i += PROC_STATUS_TRANSIENT_FAILURE) {
                part = body.getPart(i);
                Uri partUri = part.getDataUri();
                if (partUri == null || TextUtils.isEmpty(partUri.getAuthority()) || !partUri.getAuthority().startsWith("mms")) {
                    toBeCreated.add(part);
                } else {
                    toBeUpdated.put(partUri, part);
                    if (filter.length() > PROC_STATUS_TRANSIENT_FAILURE) {
                        filter.append(" AND ");
                    }
                    filter.append(HbpcdLookup.ID);
                    filter.append("!=");
                    DatabaseUtils.appendEscapedSQLString(filter, partUri.getLastPathSegment());
                }
            }
            filter.append(')');
            long msgId = ContentUris.parseId(uri);
            pduPart = this.mContext;
            SqliteWrapper.delete(pduPart, this.mContentResolver, Uri.parse(Mms.CONTENT_URI + "/" + msgId + "/part"), filter.length() > PROC_STATUS_PERMANENTLY_FAILURE ? filter.toString() : null, null);
            for (PduPart part2 : toBeCreated) {
                persistPart(part2, msgId, preOpenedFiles);
            }
            for (Entry<Uri, PduPart> e2 : toBeUpdated.entrySet()) {
                pduPart = (PduPart) e2.getValue();
                updatePart((Uri) e2.getKey(), pduPart, preOpenedFiles);
            }
            PDU_CACHE_INSTANCE.setUpdating(uri, LOCAL_LOGV);
            PDU_CACHE_INSTANCE.notifyAll();
        } finally {
            pduPart = PDU_CACHE_INSTANCE;
            synchronized (pduPart) {
            }
            PDU_CACHE_INSTANCE.setUpdating(uri, LOCAL_LOGV);
            PDU_CACHE_INSTANCE.notifyAll();
        }
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        return persist(pdu, uri, createThreadId, groupMmsEnabled, preOpenedFiles, PDU_COLUMN_ID);
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles, int subscription) throws MmsException {
        if (uri == null) {
            throw new MmsException("Uri may not be null.");
        }
        long msgId = -1;
        try {
            msgId = ContentUris.parseId(uri);
        } catch (NumberFormatException e) {
        }
        boolean existingUri = msgId != -1 ? true : LOCAL_LOGV;
        if (existingUri || MESSAGE_BOX_MAP.get(uri) != null) {
            int i;
            int addrType;
            EncodedStringValue[] array;
            long dummyId;
            boolean textOnly;
            int messageSize;
            PduBody body;
            int partsNum;
            int i2;
            PduPart part;
            String contentType;
            Uri res;
            synchronized (PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    try {
                        PDU_CACHE_INSTANCE.wait();
                    } catch (Throwable e2) {
                        Log.e(TAG, "persist1: ", e2);
                    }
                }
            }
            PDU_CACHE_INSTANCE.purge(uri);
            PduHeaders header = pdu.getPduHeaders();
            ContentValues values = new ContentValues();
            for (Entry<Integer, String> e3 : ENCODED_STRING_COLUMN_NAME_MAP.entrySet()) {
                int field = ((Integer) e3.getKey()).intValue();
                EncodedStringValue encodedString = header.getEncodedStringValue(field);
                if (encodedString != null) {
                    String charsetColumn = (String) CHARSET_COLUMN_NAME_MAP.get(Integer.valueOf(field));
                    values.put((String) e3.getValue(), toIsoString(encodedString.getTextString()));
                    values.put(charsetColumn, Integer.valueOf(encodedString.getCharacterSet()));
                }
            }
            for (Entry<Integer, String> e32 : TEXT_STRING_COLUMN_NAME_MAP.entrySet()) {
                byte[] text = header.getTextString(((Integer) e32.getKey()).intValue());
                if (text != null) {
                    values.put((String) e32.getValue(), toIsoString(text));
                }
            }
            for (Entry<Integer, String> e322 : OCTET_COLUMN_NAME_MAP.entrySet()) {
                int b = header.getOctet(((Integer) e322.getKey()).intValue());
                if (b != 0) {
                    values.put((String) e322.getValue(), Integer.valueOf(b));
                }
            }
            for (Entry<Integer, String> e3222 : LONG_COLUMN_NAME_MAP.entrySet()) {
                long l = header.getLongInteger(((Integer) e3222.getKey()).intValue());
                if (l != -1) {
                    values.put((String) e3222.getValue(), Long.valueOf(l));
                }
            }
            HashMap<Integer, EncodedStringValue[]> addressMap = new HashMap(ADDRESS_FIELDS.length);
            int[] iArr = ADDRESS_FIELDS;
            int length = iArr.length;
            for (i = PDU_COLUMN_ID; i < length; i += PROC_STATUS_TRANSIENT_FAILURE) {
                addrType = iArr[i];
                if (addrType == PduPart.P_CT_MR_TYPE) {
                    EncodedStringValue v = header.getEncodedStringValue(addrType);
                    String str = null;
                    if (v != null) {
                        str = v.getString();
                    }
                    if (str == null || str.length() == 0) {
                        array = new EncodedStringValue[PROC_STATUS_TRANSIENT_FAILURE];
                        array[PDU_COLUMN_ID] = new EncodedStringValue(this.mContext.getString(33685717));
                    } else {
                        array = new EncodedStringValue[PROC_STATUS_TRANSIENT_FAILURE];
                        array[PDU_COLUMN_ID] = v;
                    }
                } else {
                    array = header.getEncodedStringValues(addrType);
                }
                addressMap.put(Integer.valueOf(addrType), array);
            }
            HashSet<String> recipients = new HashSet();
            int msgType = pdu.getMessageType();
            if (!(msgType == 130 || msgType == 132)) {
                if (msgType == 128) {
                }
                dummyId = System.currentTimeMillis();
                textOnly = true;
                messageSize = PDU_COLUMN_ID;
                if (pdu instanceof MultimediaMessagePdu) {
                    body = ((MultimediaMessagePdu) pdu).getBody();
                    if (body != null) {
                        partsNum = body.getPartsNum();
                        if (partsNum > PROC_STATUS_PERMANENTLY_FAILURE) {
                            textOnly = LOCAL_LOGV;
                        }
                        for (i2 = PDU_COLUMN_ID; i2 < partsNum; i2 += PROC_STATUS_TRANSIENT_FAILURE) {
                            part = body.getPart(i2);
                            messageSize += part.getDataLength();
                            persistPart(part, dummyId, preOpenedFiles);
                            contentType = getPartContentType(part);
                            if (!(contentType == null || ContentType.APP_SMIL.equals(contentType) || ContentType.TEXT_PLAIN.equals(contentType))) {
                                textOnly = LOCAL_LOGV;
                            }
                        }
                    }
                }
                values.put(BaseMmsColumns.TEXT_ONLY, Integer.valueOf(textOnly ? PROC_STATUS_TRANSIENT_FAILURE : PDU_COLUMN_ID));
                if (values.getAsInteger(BaseMmsColumns.MESSAGE_SIZE) == null) {
                    values.put(BaseMmsColumns.MESSAGE_SIZE, Integer.valueOf(messageSize));
                }
                values.put(TextBasedSmsColumns.SUBSCRIPTION_ID, Integer.valueOf(subscription));
                if (existingUri) {
                    res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
                    if (res != null) {
                        throw new MmsException("persist() failed: return null.");
                    }
                    msgId = ContentUris.parseId(res);
                } else {
                    res = uri;
                    SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
                }
                values = new ContentValues(PROC_STATUS_TRANSIENT_FAILURE);
                values.put(Part.MSG_ID, Long.valueOf(msgId));
                SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + dummyId + "/part"), values, null, null);
                if (!existingUri) {
                    res = Uri.parse(uri + "/" + msgId);
                }
                iArr = ADDRESS_FIELDS;
                length = iArr.length;
                for (i = PDU_COLUMN_ID; i < length; i += PROC_STATUS_TRANSIENT_FAILURE) {
                    addrType = iArr[i];
                    array = (EncodedStringValue[]) addressMap.get(Integer.valueOf(addrType));
                    if (array != null) {
                        persistAddress(msgId, addrType, array);
                    }
                }
                return res;
            }
            switch (msgType) {
                case PduPart.P_Q /*128*/:
                    loadRecipients(PduPart.P_NAME, recipients, addressMap, LOCAL_LOGV);
                    break;
                case PduPart.P_LEVEL /*130*/:
                case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
                    loadRecipients(PduPart.P_CT_MR_TYPE, recipients, addressMap, LOCAL_LOGV);
                    if (groupMmsEnabled && !(this.mHwCustPduPersister != null && this.mHwCustPduPersister.isShortCodeFeatureEnabled() && this.mHwCustPduPersister.hasShortCode((EncodedStringValue[]) addressMap.get(Integer.valueOf(PduPart.P_NAME)), (EncodedStringValue[]) addressMap.get(Integer.valueOf(PduPart.P_LEVEL))))) {
                        loadRecipients(PduPart.P_NAME, recipients, addressMap, true);
                        filterMyNumber(groupMmsEnabled, recipients, addressMap, subscription);
                        loadRecipients(PduPart.P_LEVEL, recipients, addressMap, true);
                        break;
                    }
            }
            long threadId = 0;
            if (createThreadId && !recipients.isEmpty()) {
                threadId = Threads.getOrCreateThreadId(this.mContext, (Set) recipients);
            }
            values.put(TextBasedSmsColumns.THREAD_ID, Long.valueOf(threadId));
            dummyId = System.currentTimeMillis();
            textOnly = true;
            messageSize = PDU_COLUMN_ID;
            if (pdu instanceof MultimediaMessagePdu) {
                body = ((MultimediaMessagePdu) pdu).getBody();
                if (body != null) {
                    partsNum = body.getPartsNum();
                    if (partsNum > PROC_STATUS_PERMANENTLY_FAILURE) {
                        textOnly = LOCAL_LOGV;
                    }
                    for (i2 = PDU_COLUMN_ID; i2 < partsNum; i2 += PROC_STATUS_TRANSIENT_FAILURE) {
                        part = body.getPart(i2);
                        messageSize += part.getDataLength();
                        persistPart(part, dummyId, preOpenedFiles);
                        contentType = getPartContentType(part);
                        textOnly = LOCAL_LOGV;
                    }
                }
            }
            if (textOnly) {
            }
            values.put(BaseMmsColumns.TEXT_ONLY, Integer.valueOf(textOnly ? PROC_STATUS_TRANSIENT_FAILURE : PDU_COLUMN_ID));
            if (values.getAsInteger(BaseMmsColumns.MESSAGE_SIZE) == null) {
                values.put(BaseMmsColumns.MESSAGE_SIZE, Integer.valueOf(messageSize));
            }
            values.put(TextBasedSmsColumns.SUBSCRIPTION_ID, Integer.valueOf(subscription));
            if (existingUri) {
                res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
                if (res != null) {
                    msgId = ContentUris.parseId(res);
                } else {
                    throw new MmsException("persist() failed: return null.");
                }
            }
            res = uri;
            SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
            values = new ContentValues(PROC_STATUS_TRANSIENT_FAILURE);
            values.put(Part.MSG_ID, Long.valueOf(msgId));
            SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + dummyId + "/part"), values, null, null);
            if (existingUri) {
                res = Uri.parse(uri + "/" + msgId);
            }
            iArr = ADDRESS_FIELDS;
            length = iArr.length;
            for (i = PDU_COLUMN_ID; i < length; i += PROC_STATUS_TRANSIENT_FAILURE) {
                addrType = iArr[i];
                array = (EncodedStringValue[]) addressMap.get(Integer.valueOf(addrType));
                if (array != null) {
                    persistAddress(msgId, addrType, array);
                }
            }
            return res;
        }
        throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
    }

    private void loadRecipients(int addressType, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
        EncodedStringValue[] array = (EncodedStringValue[]) addressMap.get(Integer.valueOf(addressType));
        if (array != null) {
            int i;
            String myNumber;
            SubscriptionManager subscriptionManager = SubscriptionManager.from(this.mContext);
            Set<String> myPhoneNumbers = new HashSet();
            if (excludeMyNumber) {
                int[] activeSubscriptionIdList = subscriptionManager.getActiveSubscriptionIdList();
                int length = activeSubscriptionIdList.length;
                for (i = PDU_COLUMN_ID; i < length; i += PROC_STATUS_TRANSIENT_FAILURE) {
                    int subid = activeSubscriptionIdList[i];
                    myNumber = this.mTelephonyManager.getLine1Number(subid);
                    if (TextUtils.isEmpty(myNumber)) {
                        myNumber = Secure.getString(this.mContentResolver, "localNumberFromDb_" + subid);
                    }
                    if (myNumber != null) {
                        myPhoneNumbers.add(myNumber);
                    }
                }
            }
            int length2 = array.length;
            for (i = PDU_COLUMN_ID; i < length2; i += PROC_STATUS_TRANSIENT_FAILURE) {
                EncodedStringValue v = array[i];
                if (v != null) {
                    String number = v.getString();
                    boolean isAddNumber = true;
                    if (excludeMyNumber) {
                        for (String myNumber2 : myPhoneNumbers) {
                            if (PhoneNumberUtils.compare(number, myNumber2)) {
                                isAddNumber = LOCAL_LOGV;
                                break;
                            }
                        }
                        if (isAddNumber && !recipients.contains(number)) {
                            recipients.add(number);
                        }
                    } else if (!recipients.contains(number)) {
                        recipients.add(number);
                    }
                }
            }
        }
    }

    public Uri move(Uri from, Uri to) throws MmsException {
        long msgId = ContentUris.parseId(from);
        if (msgId == -1) {
            throw new MmsException("Error! ID of the message: -1.");
        }
        Integer msgBox = (Integer) MESSAGE_BOX_MAP.get(to);
        if (msgBox == null) {
            throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
        }
        ContentValues values = new ContentValues(PROC_STATUS_TRANSIENT_FAILURE);
        values.put(BaseMmsColumns.MESSAGE_BOX, msgBox);
        SqliteWrapper.update(this.mContext, this.mContentResolver, from, values, null, null);
        return ContentUris.withAppendedId(to, msgId);
    }

    public static String toIsoString(byte[] bytes) {
        try {
            return new String(bytes, CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return "";
        }
    }

    public static byte[] getBytes(String data) {
        try {
            return data.getBytes(CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return new byte[PDU_COLUMN_ID];
        }
    }

    public void release() {
        SqliteWrapper.delete(this.mContext, this.mContentResolver, Uri.parse(TEMPORARY_DRM_OBJECT_URI), null, null);
        this.mDrmManagerClient.release();
    }

    public Cursor getPendingMessages(long dueTime) {
        Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter(TelephonyEventLog.DATA_KEY_PROTOCOL, "mms");
        String[] selectionArgs = new String[PROC_STATUS_PERMANENTLY_FAILURE];
        selectionArgs[PDU_COLUMN_ID] = String.valueOf(PDU_COLUMN_TRANSACTION_ID);
        selectionArgs[PROC_STATUS_TRANSIENT_FAILURE] = String.valueOf(dueTime);
        return SqliteWrapper.query(this.mContext, this.mContentResolver, uriBuilder.build(), null, "err_type < ? AND due_time <= ?", selectionArgs, PendingMessages.DUE_TIME);
    }

    private void filterMyNumber(boolean groupMmsEnabled, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, int subId) {
        int i = PDU_COLUMN_ID;
        if (groupMmsEnabled && recipients.size() != PROC_STATUS_TRANSIENT_FAILURE && recipients.size() <= PROC_STATUS_PERMANENTLY_FAILURE) {
            String myNumber = this.mTelephonyManager.getLine1Number(subId);
            if (TextUtils.isEmpty(myNumber)) {
                myNumber = Secure.getString(this.mContentResolver, "localNumberFromDb_" + subId);
            }
            if (TextUtils.isEmpty(myNumber)) {
                EncodedStringValue[] array_to = (EncodedStringValue[]) addressMap.get(Integer.valueOf(PduPart.P_NAME));
                EncodedStringValue[] array_from = (EncodedStringValue[]) addressMap.get(Integer.valueOf(PduPart.P_CT_MR_TYPE));
                if (array_to != null && array_from != null) {
                    int i2;
                    EncodedStringValue v;
                    String number_from = "";
                    int length = array_from.length;
                    for (i2 = PDU_COLUMN_ID; i2 < length; i2 += PROC_STATUS_TRANSIENT_FAILURE) {
                        v = array_from[i2];
                        if (v != null && !TextUtils.isEmpty(v.getString())) {
                            number_from = v.getString();
                            break;
                        }
                    }
                    i2 = array_to.length;
                    while (i < i2) {
                        v = array_to[i];
                        if (v != null && !number_from.equals(v.getString()) && recipients.contains(v.getString())) {
                            recipients.remove(v.getString());
                            break;
                        }
                        i += PROC_STATUS_TRANSIENT_FAILURE;
                    }
                }
            }
        }
    }
}
