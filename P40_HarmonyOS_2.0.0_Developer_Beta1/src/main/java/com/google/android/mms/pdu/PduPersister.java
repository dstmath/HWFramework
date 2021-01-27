package com.google.android.mms.pdu;

import android.app.ActivityThread;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HbpcdLookup;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.mms.pdu.HwCustPduPersister;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MmsException;
import com.google.android.mms.util.DownloadDrmHelper;
import com.google.android.mms.util.DrmConvertSession;
import com.google.android.mms.util.PduCache;
import com.google.android.mms.util.PduCacheEntry;
import com.google.android.mms.util.SqliteWrapper;
import com.huawei.internal.telephony.EncodedStringValueEx;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PduPersister {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int[] ADDRESS_FIELDS = {129, 130, 137, 151};
    private static final HashMap<Integer, Integer> CHARSET_COLUMN_INDEX_MAP = new HashMap<>();
    private static final HashMap<Integer, String> CHARSET_COLUMN_NAME_MAP = new HashMap<>();
    private static final boolean DEBUG = false;
    private static final int DEFAULT_SUBSCRIPTION = 0;
    private static final long DUMMY_THREAD_ID = Long.MAX_VALUE;
    private static final HashMap<Integer, Integer> ENCODED_STRING_COLUMN_INDEX_MAP = new HashMap<>();
    private static final HashMap<Integer, String> ENCODED_STRING_COLUMN_NAME_MAP = new HashMap<>();
    public static final int LOAD_MODE_MMS_COMMON = 0;
    public static final int LOAD_MODE_MMS_FAVORITES = 1;
    private static final boolean LOCAL_LOGV = false;
    private static final HashMap<Integer, Integer> LONG_COLUMN_INDEX_MAP = new HashMap<>();
    private static final HashMap<Integer, String> LONG_COLUMN_NAME_MAP = new HashMap<>();
    private static final HashMap<Uri, Integer> MESSAGE_BOX_MAP = new HashMap<>();
    private static final HashMap<Integer, Integer> OCTET_COLUMN_INDEX_MAP = new HashMap<>();
    private static final HashMap<Integer, String> OCTET_COLUMN_NAME_MAP = new HashMap<>();
    private static final int PART_COLUMN_CHARSET = 1;
    private static final int PART_COLUMN_CONTENT_DISPOSITION = 2;
    private static final int PART_COLUMN_CONTENT_ID = 3;
    private static final int PART_COLUMN_CONTENT_LOCATION = 4;
    private static final int PART_COLUMN_CONTENT_TYPE = 5;
    private static final int PART_COLUMN_FILENAME = 6;
    private static final int PART_COLUMN_ID = 0;
    private static final int PART_COLUMN_NAME = 7;
    private static final int PART_COLUMN_TEXT = 8;
    private static final String[] PART_PROJECTION = {HbpcdLookup.ID, "chset", "cd", "cid", "cl", "ct", "fn", "name", "text"};
    private static final PduCache PDU_CACHE_INSTANCE = PduCache.getInstance();
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
    private static final String[] PDU_PROJECTION = {HbpcdLookup.ID, "msg_box", "thread_id", "retr_txt", "sub", "ct_l", "ct_t", "m_cls", "m_id", "resp_txt", "tr_id", "ct_cls", "d_rpt", "m_type", "v", "pri", "rr", "read_status", "rpt_a", "retr_st", "st", "date", "d_tm", "exp", "m_size", "sub_cs", "retr_txt_cs"};
    public static final int PROC_STATUS_COMPLETED = 3;
    public static final int PROC_STATUS_PERMANENTLY_FAILURE = 2;
    public static final int PROC_STATUS_TRANSIENT_FAILURE = 1;
    private static final String TAG = "PduPersister";
    public static final String TEMPORARY_DRM_OBJECT_URI = "content://mms/9223372036854775807/part";
    private static final HashMap<Integer, Integer> TEXT_STRING_COLUMN_INDEX_MAP = new HashMap<>();
    private static final HashMap<Integer, String> TEXT_STRING_COLUMN_NAME_MAP = new HashMap<>();
    private static PduPersister sPersister;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final DrmManagerClient mDrmManagerClient;
    HwCustPduPersister mHwCustPduPersister = ((HwCustPduPersister) HwCustUtils.createObj(HwCustPduPersister.class, new Object[0]));
    private final TelephonyManager mTelephonyManager;

    static {
        MESSAGE_BOX_MAP.put(Telephony.Mms.Inbox.CONTENT_URI, 1);
        MESSAGE_BOX_MAP.put(Telephony.Mms.Sent.CONTENT_URI, 2);
        MESSAGE_BOX_MAP.put(Telephony.Mms.Draft.CONTENT_URI, 3);
        MESSAGE_BOX_MAP.put(Telephony.Mms.Outbox.CONTENT_URI, 4);
        CHARSET_COLUMN_INDEX_MAP.put(150, 25);
        CHARSET_COLUMN_INDEX_MAP.put(154, 26);
        CHARSET_COLUMN_NAME_MAP.put(150, "sub_cs");
        CHARSET_COLUMN_NAME_MAP.put(154, "retr_txt_cs");
        ENCODED_STRING_COLUMN_INDEX_MAP.put(154, 3);
        ENCODED_STRING_COLUMN_INDEX_MAP.put(150, 4);
        ENCODED_STRING_COLUMN_NAME_MAP.put(154, "retr_txt");
        ENCODED_STRING_COLUMN_NAME_MAP.put(150, "sub");
        TEXT_STRING_COLUMN_INDEX_MAP.put(131, 5);
        TEXT_STRING_COLUMN_INDEX_MAP.put(132, 6);
        TEXT_STRING_COLUMN_INDEX_MAP.put(138, 7);
        TEXT_STRING_COLUMN_INDEX_MAP.put(139, 8);
        TEXT_STRING_COLUMN_INDEX_MAP.put(147, 9);
        TEXT_STRING_COLUMN_INDEX_MAP.put(152, 10);
        TEXT_STRING_COLUMN_NAME_MAP.put(131, "ct_l");
        TEXT_STRING_COLUMN_NAME_MAP.put(132, "ct_t");
        TEXT_STRING_COLUMN_NAME_MAP.put(138, "m_cls");
        TEXT_STRING_COLUMN_NAME_MAP.put(139, "m_id");
        TEXT_STRING_COLUMN_NAME_MAP.put(147, "resp_txt");
        TEXT_STRING_COLUMN_NAME_MAP.put(152, "tr_id");
        OCTET_COLUMN_INDEX_MAP.put(Integer.valueOf((int) PduHeaders.CONTENT_CLASS), 11);
        OCTET_COLUMN_INDEX_MAP.put(134, 12);
        OCTET_COLUMN_INDEX_MAP.put(140, 13);
        OCTET_COLUMN_INDEX_MAP.put(141, 14);
        OCTET_COLUMN_INDEX_MAP.put(143, 15);
        OCTET_COLUMN_INDEX_MAP.put(144, 16);
        OCTET_COLUMN_INDEX_MAP.put(155, 17);
        OCTET_COLUMN_INDEX_MAP.put(145, 18);
        OCTET_COLUMN_INDEX_MAP.put(153, 19);
        OCTET_COLUMN_INDEX_MAP.put(149, 20);
        OCTET_COLUMN_NAME_MAP.put(Integer.valueOf((int) PduHeaders.CONTENT_CLASS), "ct_cls");
        OCTET_COLUMN_NAME_MAP.put(134, "d_rpt");
        OCTET_COLUMN_NAME_MAP.put(140, "m_type");
        OCTET_COLUMN_NAME_MAP.put(141, "v");
        OCTET_COLUMN_NAME_MAP.put(143, "pri");
        OCTET_COLUMN_NAME_MAP.put(144, "rr");
        OCTET_COLUMN_NAME_MAP.put(155, "read_status");
        OCTET_COLUMN_NAME_MAP.put(145, "rpt_a");
        OCTET_COLUMN_NAME_MAP.put(153, "retr_st");
        OCTET_COLUMN_NAME_MAP.put(149, "st");
        LONG_COLUMN_INDEX_MAP.put(133, 21);
        LONG_COLUMN_INDEX_MAP.put(135, 22);
        LONG_COLUMN_INDEX_MAP.put(136, 23);
        LONG_COLUMN_INDEX_MAP.put(142, 24);
        LONG_COLUMN_NAME_MAP.put(133, "date");
        LONG_COLUMN_NAME_MAP.put(135, "d_tm");
        LONG_COLUMN_NAME_MAP.put(136, "exp");
        LONG_COLUMN_NAME_MAP.put(142, "m_size");
    }

    private PduPersister(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mDrmManagerClient = new DrmManagerClient(context);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    public static PduPersister getPduPersister(Context context) {
        ActivityThread actThread = ActivityThread.currentActivityThread();
        Context priorContext = actThread != null ? actThread.getApplication() : null;
        if (priorContext == null) {
            priorContext = context.getApplicationContext();
        }
        PduPersister pduPersister = sPersister;
        if (pduPersister == null) {
            sPersister = new PduPersister(priorContext);
        } else if (!priorContext.equals(pduPersister.mContext)) {
            Log.w(TAG, "PduPersister create a new one. may cause memory leak");
            sPersister.release();
            sPersister = new PduPersister(priorContext);
        }
        return sPersister;
    }

    private void setEncodedStringValueToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if (s != null && s.length() > 0) {
            headers.setEncodedStringValue(new EncodedStringValue(c.getInt(CHARSET_COLUMN_INDEX_MAP.get(Integer.valueOf(mapColumn)).intValue()), getBytes(s)), mapColumn);
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
        if (!c.isNull(columnIndex)) {
            return Integer.valueOf(c.getInt(columnIndex));
        }
        return null;
    }

    private byte[] getByteArrayFromPartColumn(Cursor c, int columnIndex) {
        if (!c.isNull(columnIndex)) {
            return getBytes(c.getString(columnIndex));
        }
        return null;
    }

    /* JADX INFO: Multiple debug info for r3v1 java.lang.String: [D('type' java.lang.String), D('contentType' byte[])] */
    /* JADX INFO: Multiple debug info for r9v11 'partId'  long: [D('contentId' byte[]), D('partId' long)] */
    /* JADX INFO: Multiple debug info for r9v12 'partId'  long: [D('contentId' byte[]), D('partId' long)] */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x021f A[SYNTHETIC, Splitter:B:98:0x021f] */
    private PduPart[] loadParts(long msgId, int loadType) throws MmsException {
        Cursor c;
        Throwable th;
        PduPart[] parts;
        long partId;
        Uri partURI;
        ByteArrayOutputStream baos;
        IOException iOException;
        IOException e;
        byte[] buffer;
        PduPersister pduPersister = this;
        int i = loadType;
        int i2 = 1;
        if (i == 1) {
            c = SqliteWrapper.query(pduPersister.mContext, pduPersister.mContentResolver, Uri.parse("content://fav-mms/" + msgId + "/part"), PART_PROJECTION, null, null, null);
        } else {
            c = SqliteWrapper.query(pduPersister.mContext, pduPersister.mContentResolver, Uri.parse("content://mms/" + msgId + "/part"), PART_PROJECTION, null, null, null);
        }
        if (c != null) {
            try {
                if (c.getCount() != 0) {
                    PduPart[] parts2 = new PduPart[c.getCount()];
                    int partIdx = 0;
                    while (c.moveToNext()) {
                        try {
                            PduPart part = new PduPart();
                            Integer charset = pduPersister.getIntegerFromPartColumn(c, i2);
                            if (charset != null) {
                                part.setCharset(charset.intValue());
                            }
                            byte[] contentDisposition = pduPersister.getByteArrayFromPartColumn(c, 2);
                            if (contentDisposition != null) {
                                part.setContentDisposition(contentDisposition);
                            }
                            byte[] contentId = pduPersister.getByteArrayFromPartColumn(c, 3);
                            if (contentId != null) {
                                part.setContentId(contentId);
                            }
                            byte[] contentLocation = pduPersister.getByteArrayFromPartColumn(c, 4);
                            if (contentLocation != null) {
                                part.setContentLocation(contentLocation);
                            }
                            byte[] contentType = pduPersister.getByteArrayFromPartColumn(c, 5);
                            if (contentType != null) {
                                part.setContentType(contentType);
                                byte[] fileName = pduPersister.getByteArrayFromPartColumn(c, 6);
                                if (fileName != null) {
                                    part.setFilename(fileName);
                                }
                                byte[] name = pduPersister.getByteArrayFromPartColumn(c, 7);
                                if (name != null) {
                                    part.setName(name);
                                }
                                long partId2 = c.getLong(0);
                                if (i == 1) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("content://fav-mms/part/");
                                    parts = parts2;
                                    partId = partId2;
                                    try {
                                        sb.append(partId);
                                        partURI = Uri.parse(sb.toString());
                                    } catch (Throwable th2) {
                                        th = th2;
                                        c.close();
                                        throw th;
                                    }
                                } else {
                                    parts = parts2;
                                    partId = partId2;
                                    partURI = Uri.parse("content://mms/part/" + partId);
                                }
                                part.setDataUri(partURI);
                                String type = toIsoString(contentType);
                                if (!ContentType.isImageType(type)) {
                                    if (!ContentType.isAudioType(type)) {
                                        if (!ContentType.isVideoType(type)) {
                                            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                                            InputStream is = null;
                                            String type2 = type;
                                            if (ContentType.TEXT_PLAIN.equals(type2) || ContentType.APP_SMIL.equals(type2)) {
                                                baos = baos2;
                                            } else if (ContentType.TEXT_HTML.equals(type2)) {
                                                baos = baos2;
                                            } else {
                                                try {
                                                    InputStream is2 = pduPersister.mContentResolver.openInputStream(partURI);
                                                    try {
                                                        buffer = new byte[ApnSettingHelper.TYPE_IA];
                                                    } catch (IOException e2) {
                                                        e = e2;
                                                        is = is2;
                                                        try {
                                                            Log.e(TAG, "Failed to load part data", e);
                                                            c.close();
                                                            throw new MmsException(e);
                                                        } catch (Throwable e3) {
                                                            iOException = e3;
                                                            if (is != null) {
                                                            }
                                                            throw iOException;
                                                        }
                                                    } catch (Throwable th3) {
                                                        is = is2;
                                                        iOException = th3;
                                                        if (is != null) {
                                                        }
                                                        throw iOException;
                                                    }
                                                    try {
                                                        int len = is2.read(buffer);
                                                        while (len >= 0) {
                                                            try {
                                                                baos2.write(buffer, 0, len);
                                                                len = is2.read(buffer);
                                                                baos2 = baos2;
                                                                type2 = type2;
                                                                partId = partId;
                                                            } catch (IOException e4) {
                                                                e = e4;
                                                                is = is2;
                                                                Log.e(TAG, "Failed to load part data", e);
                                                                c.close();
                                                                throw new MmsException(e);
                                                            } catch (Throwable th4) {
                                                                is = is2;
                                                                iOException = th4;
                                                                if (is != null) {
                                                                }
                                                                throw iOException;
                                                            }
                                                        }
                                                        baos = baos2;
                                                        try {
                                                            is2.close();
                                                        } catch (IOException e5) {
                                                            Log.e(TAG, "Failed to close stream", e5);
                                                        }
                                                        part.setData(baos.toByteArray());
                                                    } catch (IOException e6) {
                                                        e = e6;
                                                        is = is2;
                                                        Log.e(TAG, "Failed to load part data", e);
                                                        c.close();
                                                        throw new MmsException(e);
                                                    } catch (Throwable th5) {
                                                        is = is2;
                                                        iOException = th5;
                                                        if (is != null) {
                                                            try {
                                                                is.close();
                                                            } catch (IOException e7) {
                                                                Log.e(TAG, "Failed to close stream", e7);
                                                            }
                                                        }
                                                        throw iOException;
                                                    }
                                                } catch (IOException e8) {
                                                    e = e8;
                                                    Log.e(TAG, "Failed to load part data", e);
                                                    c.close();
                                                    throw new MmsException(e);
                                                } catch (Throwable th6) {
                                                    iOException = th6;
                                                    if (is != null) {
                                                    }
                                                    throw iOException;
                                                }
                                            }
                                            String text = c.getString(8);
                                            if (charset != null && charset.intValue() == 3) {
                                                charset = 106;
                                            }
                                            boolean equals = "true".equals("true");
                                            String str = PhoneConfigurationManager.SSSS;
                                            if (!equals) {
                                                if (text != null) {
                                                    str = text;
                                                }
                                                byte[] blob = new EncodedStringValue(str).getTextString();
                                                baos.write(blob, 0, blob.length);
                                            } else if (charset == null || charset.intValue() == 0) {
                                                if (text != null) {
                                                    str = text;
                                                }
                                                byte[] blob2 = new EncodedStringValue(str).getTextString();
                                                baos.write(blob2, 0, blob2.length);
                                            } else {
                                                try {
                                                    int intValue = charset.intValue();
                                                    if (text != null) {
                                                        str = text;
                                                    }
                                                    byte[] blob3 = new EncodedStringValue(intValue, str).getTextString();
                                                    baos.write(blob3, 0, blob3.length);
                                                } catch (NullPointerException e9) {
                                                    Log.e(TAG, "Failed to EncodedStringValue: ", e9);
                                                } catch (Exception e10) {
                                                    Log.e(TAG, "Failed to EncodedStringValue: ", e10);
                                                }
                                            }
                                            part.setData(baos.toByteArray());
                                        }
                                    }
                                }
                                parts[partIdx] = part;
                                pduPersister = this;
                                i = loadType;
                                partIdx++;
                                parts2 = parts;
                                i2 = 1;
                            } else {
                                throw new MmsException("Content-Type must be set.");
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            c.close();
                            throw th;
                        }
                    }
                    c.close();
                    return parts2;
                }
            } catch (Throwable th8) {
                th = th8;
                c.close();
                throw th;
            }
        }
        if (c != null) {
            c.close();
        }
        return null;
    }

    private void loadAddress(long msgId, PduHeaders headers, int loadType) {
        Cursor c;
        if (loadType == 1) {
            Context context = this.mContext;
            ContentResolver contentResolver = this.mContentResolver;
            c = SqliteWrapper.query(context, contentResolver, Uri.parse("content://fav-mms/" + msgId + "/addr"), new String[]{"address", "charset", "type"}, null, null, null);
        } else {
            Context context2 = this.mContext;
            ContentResolver contentResolver2 = this.mContentResolver;
            c = SqliteWrapper.query(context2, contentResolver2, Uri.parse("content://mms/" + msgId + "/addr"), new String[]{"address", "charset", "type"}, null, null, null);
        }
        if (c != null) {
            while (c.moveToNext()) {
                try {
                    String addr = c.getString(0);
                    if (!TextUtils.isEmpty(addr)) {
                        int addrType = c.getInt(2);
                        if (!(addrType == 129 || addrType == 130)) {
                            if (addrType == 137) {
                                headers.setEncodedStringValue(new EncodedStringValue(c.getInt(1), getBytes(addr)), addrType);
                            } else if (addrType != 151) {
                                Log.e(TAG, "Unknown address type: " + addrType);
                            }
                        }
                        headers.appendEncodedStringValue(new EncodedStringValue(c.getInt(1), getBytes(addr)), addrType);
                    }
                } finally {
                    c.close();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0034, code lost:
        r5 = com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0036, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0037, code lost:
        if (0 == 0) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE.put(r21, new com.google.android.mms.util.PduCacheEntry(null, 0, -1));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0045, code lost:
        com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE.setUpdating(r21, false);
        com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004f, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0050, code lost:
        return r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x027a  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0293  */
    public GenericPdu load(Uri uri) throws MmsException {
        Throwable th;
        Throwable th2;
        PduCacheEntry cacheEntry;
        int loadType;
        Throwable th3;
        GenericPdu pdu;
        try {
            synchronized (PDU_CACHE_INSTANCE) {
                try {
                    if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                        try {
                            PDU_CACHE_INSTANCE.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "load: ", e);
                        }
                        PduCacheEntry cacheEntry2 = (PduCacheEntry) PDU_CACHE_INSTANCE.get(uri);
                        if (cacheEntry2 != null) {
                            GenericPdu pdu2 = cacheEntry2.getPdu();
                        } else {
                            cacheEntry = cacheEntry2;
                        }
                    } else {
                        cacheEntry = null;
                    }
                    try {
                        PDU_CACHE_INSTANCE.setUpdating(uri, true);
                    } catch (Throwable th4) {
                        th2 = th4;
                        throw th2;
                    }
                } catch (Throwable th5) {
                    th2 = th5;
                    throw th2;
                }
            }
            try {
                loadType = (uri.toString() == null || !uri.toString().contains("content://fav-mms")) ? 0 : 1;
            } catch (Throwable th6) {
                th = th6;
                synchronized (PDU_CACHE_INSTANCE) {
                }
            }
            try {
                try {
                    Cursor c = SqliteWrapper.query(this.mContext, this.mContentResolver, uri, PDU_PROJECTION, null, null, null);
                    PduHeaders headers = new PduHeaders();
                    long msgId = ContentUris.parseId(uri);
                    if (c != null) {
                        try {
                            if (c.getCount() == 1 && c.moveToFirst()) {
                                int msgBox = c.getInt(1);
                                long threadId = c.getLong(2);
                                Set<Map.Entry<Integer, Integer>> set = ENCODED_STRING_COLUMN_INDEX_MAP.entrySet();
                                for (Map.Entry<Integer, Integer> e2 : set) {
                                    try {
                                        setEncodedStringValueToHeaders(c, e2.getValue().intValue(), headers, e2.getKey().intValue());
                                        set = set;
                                    } catch (Throwable th7) {
                                        th3 = th7;
                                        if (c != null) {
                                        }
                                        throw th3;
                                    }
                                }
                                Set<Map.Entry<Integer, Integer>> set2 = TEXT_STRING_COLUMN_INDEX_MAP.entrySet();
                                for (Map.Entry<Integer, Integer> e3 : set2) {
                                    setTextStringToHeaders(c, e3.getValue().intValue(), headers, e3.getKey().intValue());
                                    set2 = set2;
                                }
                                Set<Map.Entry<Integer, Integer>> set3 = OCTET_COLUMN_INDEX_MAP.entrySet();
                                for (Map.Entry<Integer, Integer> e4 : set3) {
                                    setOctetToHeaders(c, e4.getValue().intValue(), headers, e4.getKey().intValue());
                                    set3 = set3;
                                }
                                Set<Map.Entry<Integer, Integer>> set4 = LONG_COLUMN_INDEX_MAP.entrySet();
                                for (Map.Entry<Integer, Integer> e5 : set4) {
                                    setLongToHeaders(c, e5.getValue().intValue(), headers, e5.getKey().intValue());
                                    set4 = set4;
                                }
                                c.close();
                                if (msgId != -1) {
                                    loadAddress(msgId, headers, loadType);
                                    int msgType = headers.getOctet(140);
                                    PduBody body = new PduBody();
                                    if (msgType == 132 || msgType == 128) {
                                        PduPart[] parts = loadParts(msgId, loadType);
                                        if (parts != null) {
                                            int partsNum = parts.length;
                                            int i = 0;
                                            while (i < partsNum) {
                                                body.addPart(parts[i]);
                                                i++;
                                                msgId = msgId;
                                            }
                                        }
                                    }
                                    switch (msgType) {
                                        case 128:
                                            pdu = new SendReq(headers, body);
                                            break;
                                        case 129:
                                        case 137:
                                        case 138:
                                        case 139:
                                        case 140:
                                        case 141:
                                        case 142:
                                        case 143:
                                        case 144:
                                        case 145:
                                        case 146:
                                        case 147:
                                        case 148:
                                        case 149:
                                        case 150:
                                        case 151:
                                            throw new MmsException("Unsupported PDU type: " + Integer.toHexString(msgType));
                                        case 130:
                                            pdu = new NotificationInd(headers);
                                            break;
                                        case 131:
                                            pdu = new NotifyRespInd(headers);
                                            break;
                                        case 132:
                                            pdu = new RetrieveConf(headers, body);
                                            break;
                                        case 133:
                                            pdu = new AcknowledgeInd(headers);
                                            break;
                                        case 134:
                                            pdu = new DeliveryInd(headers);
                                            break;
                                        case 135:
                                            pdu = new ReadRecInd(headers);
                                            break;
                                        case 136:
                                            pdu = new ReadOrigInd(headers);
                                            break;
                                        default:
                                            throw new MmsException("Unrecognized PDU type: " + Integer.toHexString(msgType));
                                    }
                                    synchronized (PDU_CACHE_INSTANCE) {
                                        PDU_CACHE_INSTANCE.put(uri, new PduCacheEntry(pdu, msgBox, threadId));
                                        PDU_CACHE_INSTANCE.setUpdating(uri, false);
                                        PDU_CACHE_INSTANCE.notifyAll();
                                    }
                                    return pdu;
                                }
                                throw new MmsException("Error! ID of the message: -1.");
                            }
                        } catch (Throwable th8) {
                            th3 = th8;
                            if (c != null) {
                            }
                            throw th3;
                        }
                    }
                    try {
                        throw new MmsException("Bad uri: " + uri);
                    } catch (Throwable th9) {
                        th3 = th9;
                        if (c != null) {
                            c.close();
                        }
                        throw th3;
                    }
                } catch (Throwable th10) {
                    th = th10;
                    synchronized (PDU_CACHE_INSTANCE) {
                    }
                }
            } catch (Throwable th11) {
                th = th11;
                synchronized (PDU_CACHE_INSTANCE) {
                    if (0 != 0) {
                        PDU_CACHE_INSTANCE.put(uri, new PduCacheEntry(null, 0, -1));
                    }
                    PDU_CACHE_INSTANCE.setUpdating(uri, false);
                    PDU_CACHE_INSTANCE.notifyAll();
                }
                throw th;
            }
        } catch (Throwable th12) {
            th = th12;
            synchronized (PDU_CACHE_INSTANCE) {
            }
        }
    }

    private void persistAddress(long msgId, int type, EncodedStringValue[] array) {
        ContentValues[] allValues = new ContentValues[array.length];
        Uri uri = Uri.parse("content://mms/" + msgId + "/addr");
        int idx = 0;
        int length = array.length;
        int i = 0;
        while (i < length) {
            EncodedStringValue addr = array[i];
            ContentValues values = new ContentValues(3);
            values.put("address", toIsoString(addr.getTextString()));
            values.put("charset", Integer.valueOf(addr.getCharacterSet()));
            values.put("type", Integer.valueOf(type));
            allValues[idx] = values;
            i++;
            idx++;
        }
        this.mContext.getContentResolver().bulkInsert(uri, allValues);
    }

    private static String getPartContentType(PduPart part) {
        if (part.getContentType() == null) {
            return null;
        }
        return toIsoString(part.getContentType());
    }

    public Uri persistPart(PduPart part, long msgId, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        Uri uri = Uri.parse("content://mms/" + msgId + "/part");
        ContentValues values = new ContentValues(8);
        int charset = part.getCharset();
        if (charset != 0) {
            values.put("chset", Integer.valueOf(charset));
        } else {
            values.put("chset", (Integer) 106);
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
            values.put("ct", contentType);
            if (ContentType.APP_SMIL.equals(contentType)) {
                values.put("seq", (Integer) -1);
            }
            if (part.getFilename() != null) {
                values.put("fn", (String) toIsoString(part.getFilename()));
            }
            if (part.getName() != null) {
                values.put("name", (String) toIsoString(part.getName()));
            }
            if (part.getContentDisposition() != null) {
                values.put("cd", (String) toIsoString(part.getContentDisposition()));
            }
            if (part.getContentId() != null) {
                values.put("cid", (String) toIsoString(part.getContentId()));
            }
            if (part.getContentLocation() != null) {
                values.put("cl", (String) toIsoString(part.getContentLocation()));
            }
            Uri res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
            if (res != null) {
                persistData(part, res, contentType, preOpenedFiles);
                part.setDataUri(res);
                return res;
            }
            throw new MmsException("Failed to persist part, return null.");
        }
        throw new MmsException("MIME type of the part must be set.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0096, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0098, code lost:
        if (r0 != null) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00a0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00a1, code lost:
        r16 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00aa, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ab, code lost:
        r0 = e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x023f A[SYNTHETIC, Splitter:B:133:0x023f] */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x025a A[SYNTHETIC, Splitter:B:138:0x025a] */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x02c0 A[SYNTHETIC, Splitter:B:165:0x02c0] */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x02db A[SYNTHETIC, Splitter:B:170:0x02db] */
    /* JADX WARNING: Removed duplicated region for block: B:187:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00d4 A[Catch:{ FileNotFoundException -> 0x0297, IOException -> 0x0293, all -> 0x028e }] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00d5 A[Catch:{ FileNotFoundException -> 0x0297, IOException -> 0x0293, all -> 0x028e }] */
    private void persistData(PduPart part, Uri uri, String contentType, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        FileNotFoundException fileNotFoundException;
        FileNotFoundException e;
        IOException e2;
        OutputStream os;
        OutputStream os2;
        Exception e3;
        OutputStream os3 = null;
        InputStream is = null;
        DrmConvertSession drmConvertSession = null;
        try {
            byte[] data = part.getData();
            if (ContentType.TEXT_PLAIN.equals(contentType)) {
                os = null;
            } else if (ContentType.APP_SMIL.equals(contentType)) {
                os = null;
            } else if (ContentType.TEXT_HTML.equals(contentType)) {
                os = null;
            } else {
                boolean isDrm = DownloadDrmHelper.isDrmConvertNeeded(contentType);
                if (isDrm) {
                    if (uri != null) {
                        try {
                            ParcelFileDescriptor pfd = this.mContentResolver.openFileDescriptor(uri, "r");
                            if (pfd.getStatSize() > 0) {
                                try {
                                    pfd.close();
                                    if (0 != 0) {
                                        try {
                                            os3.close();
                                        } catch (IOException e4) {
                                            Log.e(TAG, "IOException while closing: " + ((Object) null), e4);
                                        }
                                    }
                                    if (0 != 0) {
                                        try {
                                            is.close();
                                            return;
                                        } catch (IOException e5) {
                                            Log.e(TAG, "IOException while closing: " + ((Object) null), e5);
                                            return;
                                        }
                                    } else {
                                        return;
                                    }
                                } catch (Exception e6) {
                                    e3 = e6;
                                    os2 = null;
                                    try {
                                        Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e3);
                                        drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                                        if (drmConvertSession != null) {
                                        }
                                    } catch (FileNotFoundException e7) {
                                        e = e7;
                                        Log.e(TAG, "Failed to open Input/Output stream.");
                                        throw new MmsException(e);
                                    } catch (IOException e8) {
                                        e2 = e8;
                                        os3 = os2;
                                        Log.e(TAG, "Failed to read/write data.", e2);
                                        throw new MmsException(e2);
                                    } catch (Throwable th) {
                                        fileNotFoundException = th;
                                        os3 = os2;
                                        if (os3 != null) {
                                            try {
                                                os3.close();
                                            } catch (IOException e9) {
                                                Log.e(TAG, "IOException while closing: " + os3, e9);
                                            }
                                        }
                                        if (0 != 0) {
                                            try {
                                                is.close();
                                            } catch (IOException e10) {
                                                Log.e(TAG, "IOException while closing: " + ((Object) null), e10);
                                            }
                                        }
                                        throw fileNotFoundException;
                                    }
                                }
                            } else {
                                pfd.close();
                            }
                        } catch (Exception e11) {
                            e3 = e11;
                            os2 = null;
                            Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e3);
                            drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                            if (drmConvertSession != null) {
                            }
                        }
                    }
                    drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                    if (drmConvertSession != null) {
                        throw new MmsException("Mimetype " + contentType + " can not be converted.");
                    }
                }
                os3 = this.mContentResolver.openOutputStream(uri);
                if (data != null) {
                    if (isDrm) {
                        is = new ByteArrayInputStream(data);
                        byte[] buffer = new byte[4096];
                        while (true) {
                            int len = is.read(buffer);
                            if (len == -1) {
                                break;
                            }
                            byte[] convertedData = drmConvertSession.convert(buffer, len);
                            if (convertedData != null) {
                                os3.write(convertedData, 0, convertedData.length);
                            } else {
                                throw new MmsException("Error converting drm data.");
                            }
                        }
                    } else {
                        os3.write(data);
                    }
                } else {
                    try {
                        Uri dataUri = part.getDataUri();
                        if (dataUri != null) {
                            if (!dataUri.equals(uri)) {
                                if (preOpenedFiles != null && preOpenedFiles.containsKey(dataUri)) {
                                    is = preOpenedFiles.get(dataUri);
                                }
                                if (is == null) {
                                    is = this.mContentResolver.openInputStream(dataUri);
                                }
                                byte[] buffer2 = new byte[4096];
                                while (true) {
                                    int len2 = is.read(buffer2);
                                    if (len2 == -1) {
                                        break;
                                    } else if (!isDrm) {
                                        os3.write(buffer2, 0, len2);
                                    } else {
                                        byte[] convertedData2 = drmConvertSession.convert(buffer2, len2);
                                        if (convertedData2 != null) {
                                            os3.write(convertedData2, 0, convertedData2.length);
                                        } else {
                                            throw new MmsException("Error converting drm data.");
                                        }
                                    }
                                }
                            }
                        }
                        Log.w(TAG, "Can't find data for this part.");
                        if (os3 != null) {
                            try {
                                os3.close();
                            } catch (IOException e12) {
                                Log.e(TAG, "IOException while closing: " + os3, e12);
                            }
                        }
                        if (0 != 0) {
                            try {
                                is.close();
                                return;
                            } catch (IOException e13) {
                                Log.e(TAG, "IOException while closing: " + ((Object) null), e13);
                                return;
                            }
                        } else {
                            return;
                        }
                    } catch (FileNotFoundException e14) {
                        e = e14;
                        Log.e(TAG, "Failed to open Input/Output stream.");
                        throw new MmsException(e);
                    } catch (IOException e15) {
                        e2 = e15;
                        Log.e(TAG, "Failed to read/write data.", e2);
                        throw new MmsException(e2);
                    }
                }
                if (os3 != null) {
                    try {
                        os3.close();
                    } catch (IOException e16) {
                        Log.e(TAG, "IOException while closing: " + os3, e16);
                    }
                }
                if (is == null) {
                    try {
                        is.close();
                        return;
                    } catch (IOException e17) {
                        Log.e(TAG, "IOException while closing: " + is, e17);
                        return;
                    }
                } else {
                    return;
                }
            }
            if ("true".equals("true")) {
                int charset = part.getCharset();
                ContentValues cv = new ContentValues();
                String str = PhoneConfigurationManager.SSSS;
                if (charset != 0) {
                    if (data != null) {
                        str = new EncodedStringValue(charset, data).getString();
                    }
                    cv.put("text", str);
                } else {
                    if (data != null) {
                        str = new EncodedStringValue(data).getString();
                    }
                    cv.put("text", str);
                }
                if (this.mContentResolver.update(uri, cv, null, null) != 1) {
                    throw new MmsException("unable to update " + uri.toString());
                }
            } else {
                ContentValues cv2 = new ContentValues();
                cv2.put("text", new EncodedStringValue(data).getString());
                if (this.mContentResolver.update(uri, cv2, null, null) != 1) {
                    throw new MmsException("unable to update " + uri.toString());
                }
            }
            os3 = os;
            if (os3 != null) {
            }
            if (is == null) {
            }
        } catch (FileNotFoundException e18) {
            e = e18;
            Log.e(TAG, "Failed to open Input/Output stream.");
            throw new MmsException(e);
        } catch (IOException e19) {
            e2 = e19;
            Log.e(TAG, "Failed to read/write data.", e2);
            throw new MmsException(e2);
        } catch (Throwable e20) {
            fileNotFoundException = e20;
            if (os3 != null) {
            }
            if (0 != 0) {
            }
            throw fileNotFoundException;
        }
    }

    private void updateAddress(long msgId, int type, EncodedStringValue[] array) {
        Context context = this.mContext;
        ContentResolver contentResolver = this.mContentResolver;
        Uri parse = Uri.parse("content://mms/" + msgId + "/addr");
        StringBuilder sb = new StringBuilder();
        sb.append("type=");
        sb.append(type);
        SqliteWrapper.delete(context, contentResolver, parse, sb.toString(), null);
        persistAddress(msgId, type, array);
    }

    public void updateHeaders(Uri uri, SendReq sendReq) {
        int i;
        int i2;
        EncodedStringValue[] array;
        PduHeaders headers;
        int i3;
        synchronized (PDU_CACHE_INSTANCE) {
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                try {
                    PDU_CACHE_INSTANCE.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "updateHeaders: ", e);
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri);
        ContentValues values = new ContentValues(10);
        byte[] contentType = sendReq.getContentType();
        if (contentType != null) {
            values.put("ct_t", toIsoString(contentType));
        }
        long date = sendReq.getDate();
        if (date != -1) {
            values.put("date", Long.valueOf(date));
        }
        int deliveryReport = sendReq.getDeliveryReport();
        if (deliveryReport != 0) {
            values.put("d_rpt", Integer.valueOf(deliveryReport));
        }
        long expiry = sendReq.getExpiry();
        if (expiry != -1) {
            values.put("exp", Long.valueOf(expiry));
        }
        byte[] msgClass = sendReq.getMessageClass();
        if (msgClass != null) {
            values.put("m_cls", toIsoString(msgClass));
        }
        int priority = sendReq.getPriority();
        if (priority != 0) {
            values.put("pri", Integer.valueOf(priority));
        }
        int readReport = sendReq.getReadReport();
        if (readReport != 0) {
            values.put("rr", Integer.valueOf(readReport));
        }
        byte[] transId = sendReq.getTransactionId();
        if (transId != null) {
            values.put("tr_id", toIsoString(transId));
        }
        EncodedStringValue subject = sendReq.getSubject();
        if (subject != null) {
            values.put("sub", toIsoString(subject.getTextString()));
            values.put("sub_cs", Integer.valueOf(subject.getCharacterSet()));
        } else {
            values.put("sub", PhoneConfigurationManager.SSSS);
        }
        long messageSize = sendReq.getMessageSize();
        if (messageSize > 0) {
            values.put("m_size", Long.valueOf(messageSize));
        }
        PduHeaders headers2 = sendReq.getPduHeaders();
        HashSet<String> recipients = new HashSet<>();
        int[] iArr = ADDRESS_FIELDS;
        int length = iArr.length;
        int i4 = 0;
        while (i4 < length) {
            int addrType = iArr[i4];
            if (addrType == 137) {
                EncodedStringValue v = headers2.getEncodedStringValue(addrType);
                if (v != null) {
                    i = length;
                    i2 = 0;
                    array = new EncodedStringValue[]{v};
                } else {
                    i = length;
                    i2 = 0;
                    array = null;
                }
            } else {
                i = length;
                i2 = 0;
                array = headers2.getEncodedStringValues(addrType);
            }
            if (array != null) {
                headers = headers2;
                updateAddress(ContentUris.parseId(uri), addrType, array);
                if (addrType == 151) {
                    int length2 = array.length;
                    int addrType2 = i2;
                    while (addrType2 < length2) {
                        EncodedStringValue v2 = array[addrType2];
                        if (v2 != null) {
                            i3 = length2;
                            recipients.add(v2.getString());
                        } else {
                            i3 = length2;
                        }
                        addrType2++;
                        length2 = i3;
                    }
                }
            } else {
                headers = headers2;
            }
            i4++;
            headers2 = headers;
            iArr = iArr;
            length = i;
        }
        if (!recipients.isEmpty()) {
            values.put("thread_id", Long.valueOf(Telephony.Threads.getOrCreateThreadId(this.mContext, recipients)));
        }
        SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
    }

    private void updatePart(Uri uri, PduPart part, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        ContentValues values = new ContentValues(7);
        int charset = part.getCharset();
        if (charset != 0) {
            values.put("chset", Integer.valueOf(charset));
        }
        if (part.getContentType() != null) {
            String contentType = toIsoString(part.getContentType());
            values.put("ct", contentType);
            Object value = null;
            if (part.getFilename() != null) {
                value = toIsoString(part.getFilename());
                values.put("fn", (String) value);
            }
            if (part.getName() != null) {
                value = toIsoString(part.getName());
                values.put("name", (String) value);
            }
            if (part.getContentDisposition() != null) {
                value = toIsoString(part.getContentDisposition());
                values.put("cd", (String) value);
            }
            if (part.getContentId() != null) {
                value = toIsoString(part.getContentId());
                values.put("cid", (String) value);
            }
            if (part.getContentLocation() != null) {
                values.put("cl", (String) toIsoString(part.getContentLocation()));
            }
            SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
            if (part.getData() != null || !uri.equals(part.getDataUri())) {
                persistData(part, uri, contentType, preOpenedFiles);
                return;
            }
            return;
        }
        throw new MmsException("MIME type of the part must be set.");
    }

    public void updateParts(Uri uri, PduBody body, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        try {
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
            ArrayList<PduPart> toBeCreated = new ArrayList<>();
            HashMap<Uri, PduPart> toBeUpdated = new HashMap<>();
            int partsNum = body.getPartsNum();
            StringBuilder filter = new StringBuilder();
            filter.append('(');
            for (int i = 0; i < partsNum; i++) {
                PduPart part = body.getPart(i);
                Uri partUri = part.getDataUri();
                if (partUri == null || TextUtils.isEmpty(partUri.getAuthority()) || !partUri.getAuthority().startsWith("mms")) {
                    toBeCreated.add(part);
                } else {
                    toBeUpdated.put(partUri, part);
                    if (filter.length() > 1) {
                        filter.append(" AND ");
                    }
                    filter.append(HbpcdLookup.ID);
                    filter.append("!=");
                    DatabaseUtils.appendEscapedSQLString(filter, partUri.getLastPathSegment());
                }
            }
            filter.append(')');
            long msgId = ContentUris.parseId(uri);
            Context context = this.mContext;
            ContentResolver contentResolver = this.mContentResolver;
            SqliteWrapper.delete(context, contentResolver, Uri.parse(Telephony.Mms.CONTENT_URI + "/" + msgId + "/part"), filter.length() > 2 ? filter.toString() : null, null);
            Iterator<PduPart> it = toBeCreated.iterator();
            while (it.hasNext()) {
                persistPart(it.next(), msgId, preOpenedFiles);
            }
            for (Map.Entry<Uri, PduPart> e2 : toBeUpdated.entrySet()) {
                updatePart(e2.getKey(), e2.getValue(), preOpenedFiles);
            }
            synchronized (PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll();
            }
        } catch (Throwable th) {
            synchronized (PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll();
                throw th;
            }
        }
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        return persist(pdu, uri, createThreadId, groupMmsEnabled, preOpenedFiles, 0);
    }

    /* JADX INFO: Multiple debug info for r0v32 com.google.android.mms.pdu.EncodedStringValue: [D('tempArray' com.google.android.mms.pdu.EncodedStringValue), D('header' com.google.android.mms.pdu.PduHeaders)] */
    /* JADX INFO: Multiple debug info for r5v16 'array'  com.google.android.mms.pdu.EncodedStringValue[]: [D('body' com.google.android.mms.pdu.PduBody), D('array' com.google.android.mms.pdu.EncodedStringValue[])] */
    /* JADX INFO: Multiple debug info for r5v17 'array'  com.google.android.mms.pdu.EncodedStringValue[]: [D('body' com.google.android.mms.pdu.PduBody), D('array' com.google.android.mms.pdu.EncodedStringValue[])] */
    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles, int subscription) throws MmsException {
        long msgId;
        Throwable th;
        boolean textOnly;
        int i;
        long dummyId;
        long msgId2;
        Uri res;
        long msgId3;
        PduBody body;
        int i2;
        EncodedStringValue[] array;
        if (uri != null) {
            try {
                msgId = ContentUris.parseId(uri);
            } catch (NumberFormatException e) {
                msgId = -1;
            }
            boolean existingUri = msgId != -1;
            if (existingUri || MESSAGE_BOX_MAP.get(uri) != null) {
                synchronized (PDU_CACHE_INSTANCE) {
                    try {
                        if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                            try {
                                PDU_CACHE_INSTANCE.wait();
                            } catch (InterruptedException e2) {
                                Log.e(TAG, "persist1: ", e2);
                            } catch (Throwable th2) {
                                th = th2;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                                throw th;
                            }
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                PDU_CACHE_INSTANCE.purge(uri);
                PduHeaders header = pdu.getPduHeaders();
                PduBody body2 = null;
                ContentValues values = new ContentValues();
                for (Map.Entry<Integer, String> e3 : ENCODED_STRING_COLUMN_NAME_MAP.entrySet()) {
                    int field = e3.getKey().intValue();
                    EncodedStringValue encodedString = header.getEncodedStringValue(field);
                    if (encodedString != null) {
                        values.put(e3.getValue(), toIsoString(encodedString.getTextString()));
                        values.put(CHARSET_COLUMN_NAME_MAP.get(Integer.valueOf(field)), Integer.valueOf(encodedString.getCharacterSet()));
                    }
                }
                for (Map.Entry<Integer, String> e4 : TEXT_STRING_COLUMN_NAME_MAP.entrySet()) {
                    byte[] text = header.getTextString(e4.getKey().intValue());
                    if (text != null) {
                        values.put(e4.getValue(), toIsoString(text));
                    }
                }
                for (Map.Entry<Integer, String> e5 : OCTET_COLUMN_NAME_MAP.entrySet()) {
                    int b = header.getOctet(e5.getKey().intValue());
                    if (b != 0) {
                        values.put(e5.getValue(), Integer.valueOf(b));
                    }
                }
                for (Map.Entry<Integer, String> e6 : LONG_COLUMN_NAME_MAP.entrySet()) {
                    long l = header.getLongInteger(e6.getKey().intValue());
                    if (l != -1) {
                        values.put(e6.getValue(), Long.valueOf(l));
                    }
                }
                HashMap<Integer, EncodedStringValue[]> addressMap = new HashMap<>(ADDRESS_FIELDS.length);
                HashMap<Integer, EncodedStringValueEx[]> addressMapEx = new HashMap<>(ADDRESS_FIELDS.length);
                int[] iArr = ADDRESS_FIELDS;
                int length = iArr.length;
                int i3 = 0;
                while (i3 < length) {
                    int addrType = iArr[i3];
                    if (addrType == 137) {
                        EncodedStringValue v = header.getEncodedStringValue(addrType);
                        String str = null;
                        if (v != null) {
                            str = v.getString();
                        }
                        if (str == null || str.length() == 0) {
                            i2 = length;
                            body = body2;
                            msgId3 = msgId;
                            array = new EncodedStringValue[]{new EncodedStringValue(this.mContext.getString(33685936))};
                        } else {
                            i2 = length;
                            body = body2;
                            array = new EncodedStringValue[]{v};
                            msgId3 = msgId;
                        }
                    } else {
                        i2 = length;
                        body = body2;
                        msgId3 = msgId;
                        array = header.getEncodedStringValues(addrType);
                    }
                    EncodedStringValueEx[] arrayEx = null;
                    if (array != null) {
                        arrayEx = new EncodedStringValueEx[array.length];
                        int length2 = array.length;
                        int tempLength = 0;
                        int tempLength2 = 0;
                        while (tempLength2 < length2) {
                            EncodedStringValue tempArray = array[tempLength2];
                            arrayEx[tempLength] = new EncodedStringValueEx();
                            arrayEx[tempLength].setEncodedStringValue(tempArray);
                            tempLength++;
                            tempLength2++;
                            length2 = length2;
                            header = header;
                        }
                    }
                    addressMap.put(Integer.valueOf(addrType), array);
                    addressMapEx.put(Integer.valueOf(addrType), arrayEx);
                    i3++;
                    iArr = iArr;
                    length = i2;
                    body2 = body;
                    msgId = msgId3;
                    header = header;
                }
                HashSet<String> recipients = new HashSet<>();
                int msgType = pdu.getMessageType();
                if (msgType == 130 || msgType == 132 || msgType == 128) {
                    if (msgType == 128) {
                        loadRecipients(151, recipients, addressMap, false);
                    } else if (msgType == 130 || msgType == 132) {
                        loadRecipients(137, recipients, addressMap, false);
                        if (groupMmsEnabled) {
                            HwCustPduPersister hwCustPduPersister = this.mHwCustPduPersister;
                            if (hwCustPduPersister == null || !hwCustPduPersister.isShortCodeFeatureEnabled() || !this.mHwCustPduPersister.hasShortCode(addressMap.get(151), addressMap.get(130))) {
                                loadRecipients(151, recipients, addressMap, true);
                                HwTelephonyFactory.getHwInnerSmsManager().filterMyNumber(this.mContext, groupMmsEnabled, recipients, addressMapEx, subscription);
                                loadRecipients(130, recipients, addressMap, true);
                            }
                        }
                    }
                    long threadId = 0;
                    if (createThreadId && !recipients.isEmpty()) {
                        threadId = Telephony.Threads.getOrCreateThreadId(this.mContext, recipients);
                    }
                    values.put("thread_id", Long.valueOf(threadId));
                }
                long dummyId2 = System.currentTimeMillis();
                boolean textOnly2 = true;
                int messageSize = 0;
                if (pdu instanceof MultimediaMessagePdu) {
                    PduBody body3 = ((MultimediaMessagePdu) pdu).getBody();
                    if (body3 != null) {
                        int partsNum = body3.getPartsNum();
                        if (partsNum > 2) {
                            textOnly2 = false;
                        }
                        int i4 = 0;
                        while (i4 < partsNum) {
                            PduPart part = body3.getPart(i4);
                            messageSize += part.getDataLength();
                            persistPart(part, dummyId2, preOpenedFiles);
                            String contentType = getPartContentType(part);
                            if (contentType != null) {
                                if (!ContentType.APP_SMIL.equals(contentType) && !ContentType.TEXT_PLAIN.equals(contentType)) {
                                    textOnly2 = false;
                                    i4++;
                                    addressMapEx = addressMapEx;
                                }
                            }
                            textOnly2 = textOnly2;
                            i4++;
                            addressMapEx = addressMapEx;
                        }
                        textOnly = textOnly2;
                        i = messageSize;
                    } else {
                        textOnly = true;
                        i = 0;
                    }
                } else {
                    textOnly = true;
                    i = 0;
                }
                values.put("text_only", Integer.valueOf(textOnly ? 1 : 0));
                if (values.getAsInteger("m_size") == null) {
                    values.put("m_size", Integer.valueOf(i));
                }
                values.put("sub_id", Integer.valueOf(subscription));
                if (existingUri) {
                    dummyId = dummyId2;
                    SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
                    res = uri;
                    msgId2 = msgId;
                } else {
                    dummyId = dummyId2;
                    res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
                    if (res != null) {
                        msgId2 = ContentUris.parseId(res);
                    } else {
                        throw new MmsException("persist() failed: return null.");
                    }
                }
                ContentValues values2 = new ContentValues(1);
                values2.put("mid", Long.valueOf(msgId2));
                SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + dummyId + "/part"), values2, null, null);
                if (!existingUri) {
                    res = Uri.parse(uri + "/" + msgId2);
                }
                int[] iArr2 = ADDRESS_FIELDS;
                int length3 = iArr2.length;
                int i5 = 0;
                while (i5 < length3) {
                    int addrType2 = iArr2[i5];
                    EncodedStringValue[] array2 = addressMap.get(Integer.valueOf(addrType2));
                    if (array2 != null) {
                        persistAddress(msgId2, addrType2, array2);
                    }
                    i5++;
                    i = i;
                }
                return res;
            }
            throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
        }
        throw new MmsException("Uri may not be null.");
    }

    private void loadRecipients(int addressType, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
        HwCustPduPersister hwCustPduPersister;
        EncodedStringValue[] array = addressMap.get(Integer.valueOf(addressType));
        if (array != null) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(this.mContext);
            Set<String> myPhoneNumbers = new HashSet<>();
            if (excludeMyNumber) {
                int[] activeSubscriptionIdList = subscriptionManager.getActiveSubscriptionIdList();
                for (int subid : activeSubscriptionIdList) {
                    String myNumber = this.mTelephonyManager.getLine1Number(subid);
                    if (TextUtils.isEmpty(myNumber) && (hwCustPduPersister = this.mHwCustPduPersister) != null) {
                        myNumber = hwCustPduPersister.getCustLocalNumberFromDB(subid, this.mContext, myNumber);
                    }
                    if (myNumber != null) {
                        myPhoneNumbers.add(myNumber);
                    }
                }
            }
            for (EncodedStringValue v : array) {
                if (v != null) {
                    String number = v.getString();
                    boolean isAddNumber = true;
                    if (excludeMyNumber) {
                        Iterator<String> it = myPhoneNumbers.iterator();
                        while (true) {
                            if (it.hasNext()) {
                                if (PhoneNumberUtils.compare(number, it.next())) {
                                    isAddNumber = false;
                                    break;
                                }
                            } else {
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
        if (msgId != -1) {
            Integer msgBox = MESSAGE_BOX_MAP.get(to);
            if (msgBox != null) {
                ContentValues values = new ContentValues(1);
                values.put("msg_box", msgBox);
                SqliteWrapper.update(this.mContext, this.mContentResolver, from, values, null, null);
                return ContentUris.withAppendedId(to, msgId);
            }
            throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
        }
        throw new MmsException("Error! ID of the message: -1.");
    }

    public static String toIsoString(byte[] bytes) {
        try {
            return new String(bytes, CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return PhoneConfigurationManager.SSSS;
        }
    }

    public static byte[] getBytes(String data) {
        try {
            return data.getBytes(CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return new byte[0];
        }
    }

    public void release() {
        SqliteWrapper.delete(this.mContext, this.mContentResolver, Uri.parse(TEMPORARY_DRM_OBJECT_URI), null, null);
        this.mDrmManagerClient.release();
    }

    public Cursor getPendingMessages(long dueTime) {
        Uri.Builder uriBuilder = Telephony.MmsSms.PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        return SqliteWrapper.query(this.mContext, this.mContentResolver, uriBuilder.build(), null, "err_type < ? AND due_time <= ?", new String[]{String.valueOf(10), String.valueOf(dueTime)}, "due_time");
    }
}
