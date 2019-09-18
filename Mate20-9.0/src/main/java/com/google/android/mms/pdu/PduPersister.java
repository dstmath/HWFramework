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
import android.hardware.radio.V1_0.RadioAccessFamily;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HbpcdLookup;
import com.android.internal.telephony.HwTelephonyFactory;
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
    public static final String LOCAL_NUMBER_FROM_DB = "localNumberFromDb";
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
        OCTET_COLUMN_INDEX_MAP.put(Integer.valueOf(PduHeaders.CONTENT_CLASS), 11);
        OCTET_COLUMN_INDEX_MAP.put(134, 12);
        OCTET_COLUMN_INDEX_MAP.put(140, 13);
        OCTET_COLUMN_INDEX_MAP.put(141, 14);
        OCTET_COLUMN_INDEX_MAP.put(143, 15);
        OCTET_COLUMN_INDEX_MAP.put(144, 16);
        OCTET_COLUMN_INDEX_MAP.put(155, 17);
        OCTET_COLUMN_INDEX_MAP.put(145, 18);
        OCTET_COLUMN_INDEX_MAP.put(153, 19);
        OCTET_COLUMN_INDEX_MAP.put(149, 20);
        OCTET_COLUMN_NAME_MAP.put(Integer.valueOf(PduHeaders.CONTENT_CLASS), "ct_cls");
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

    /* JADX WARNING: Removed duplicated region for block: B:104:0x01f8 A[Catch:{ IOException -> 0x0194, all -> 0x02aa }] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01e6 A[SYNTHETIC, Splitter:B:97:0x01e6] */
    private PduPart[] loadParts(long msgId, int loadType) throws MmsException {
        Cursor c;
        long partId;
        Uri partURI;
        InputStream is;
        IOException iOException;
        PduPersister pduPersister = this;
        long j = msgId;
        int i = loadType;
        int i2 = 1;
        if (i == 1) {
            c = SqliteWrapper.query(pduPersister.mContext, pduPersister.mContentResolver, Uri.parse("content://fav-mms/" + j + "/part"), PART_PROJECTION, null, null, null);
        } else {
            c = SqliteWrapper.query(pduPersister.mContext, pduPersister.mContentResolver, Uri.parse("content://mms/" + j + "/part"), PART_PROJECTION, null, null, null);
        }
        Cursor c2 = c;
        if (c2 != null) {
            try {
                if (c2.getCount() != 0) {
                    PduPart[] parts = new PduPart[c2.getCount()];
                    int partIdx = 0;
                    while (c2.moveToNext() != 0) {
                        PduPart part = new PduPart();
                        Integer charset = pduPersister.getIntegerFromPartColumn(c2, i2);
                        if (charset != null) {
                            part.setCharset(charset.intValue());
                        }
                        byte[] contentDisposition = pduPersister.getByteArrayFromPartColumn(c2, 2);
                        if (contentDisposition != null) {
                            part.setContentDisposition(contentDisposition);
                        }
                        byte[] contentId = pduPersister.getByteArrayFromPartColumn(c2, 3);
                        if (contentId != null) {
                            part.setContentId(contentId);
                        }
                        byte[] contentLocation = pduPersister.getByteArrayFromPartColumn(c2, 4);
                        if (contentLocation != null) {
                            part.setContentLocation(contentLocation);
                        }
                        byte[] contentType = pduPersister.getByteArrayFromPartColumn(c2, 5);
                        if (contentType != null) {
                            part.setContentType(contentType);
                            byte[] fileName = pduPersister.getByteArrayFromPartColumn(c2, 6);
                            if (fileName != null) {
                                part.setFilename(fileName);
                            }
                            byte[] name = pduPersister.getByteArrayFromPartColumn(c2, 7);
                            if (name != null) {
                                part.setName(name);
                            }
                            long partId2 = c2.getLong(0);
                            byte[] bArr = name;
                            if (i == 1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("content://fav-mms/part/");
                                partId = partId2;
                                sb.append(partId);
                                partURI = Uri.parse(sb.toString());
                            } else {
                                partId = partId2;
                                partURI = Uri.parse("content://mms/part/" + partId);
                            }
                            Uri partURI2 = partURI;
                            part.setDataUri(partURI2);
                            long j2 = partId;
                            String type = toIsoString(contentType);
                            if (ContentType.isImageType(type) || ContentType.isAudioType(type) || ContentType.isVideoType(type)) {
                                String str = type;
                            } else {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                InputStream is2 = null;
                                if (ContentType.TEXT_PLAIN.equals(type) || ContentType.APP_SMIL.equals(type)) {
                                    String str2 = type;
                                } else if (ContentType.TEXT_HTML.equals(type)) {
                                    Uri uri = partURI2;
                                    String str3 = type;
                                } else {
                                    try {
                                        InputStream is3 = pduPersister.mContentResolver.openInputStream(partURI2);
                                        try {
                                            byte[] buffer = new byte[256];
                                            InputStream is4 = is3;
                                            try {
                                                int len = is4.read(buffer);
                                                while (true) {
                                                    Uri partURI3 = partURI2;
                                                    int len2 = len;
                                                    if (len2 < 0) {
                                                        break;
                                                    }
                                                    String type2 = type;
                                                    try {
                                                        baos.write(buffer, 0, len2);
                                                        len = is4.read(buffer);
                                                        partURI2 = partURI3;
                                                        type = type2;
                                                    } catch (IOException e) {
                                                        e = e;
                                                        is2 = is4;
                                                        try {
                                                            Log.e(TAG, "Failed to load part data", e);
                                                            c2.close();
                                                            throw new MmsException((Throwable) e);
                                                        } catch (Throwable e2) {
                                                            iOException = e2;
                                                            is = is2;
                                                            if (is != null) {
                                                            }
                                                            throw iOException;
                                                        }
                                                    } catch (Throwable th) {
                                                        is = is4;
                                                        iOException = th;
                                                        if (is != null) {
                                                        }
                                                        throw iOException;
                                                    }
                                                }
                                                if (is4 != null) {
                                                    is4.close();
                                                }
                                                part.setData(baos.toByteArray());
                                            } catch (IOException e3) {
                                                e = e3;
                                                Uri uri2 = partURI2;
                                                String str4 = type;
                                                is2 = is4;
                                                Log.e(TAG, "Failed to load part data", e);
                                                c2.close();
                                                throw new MmsException((Throwable) e);
                                            } catch (Throwable th2) {
                                                Uri uri3 = partURI2;
                                                String str5 = type;
                                                is = is4;
                                                iOException = th2;
                                                if (is != null) {
                                                }
                                                throw iOException;
                                            }
                                        } catch (IOException e4) {
                                            e = e4;
                                            Uri uri4 = partURI2;
                                            String str6 = type;
                                            is2 = is3;
                                            Log.e(TAG, "Failed to load part data", e);
                                            c2.close();
                                            throw new MmsException((Throwable) e);
                                        } catch (Throwable th3) {
                                            Uri uri5 = partURI2;
                                            String str7 = type;
                                            is = is3;
                                            iOException = th3;
                                            if (is != null) {
                                            }
                                            throw iOException;
                                        }
                                    } catch (IOException e5) {
                                        e = e5;
                                        Uri uri6 = partURI2;
                                        String str8 = type;
                                        Log.e(TAG, "Failed to load part data", e);
                                        c2.close();
                                        throw new MmsException((Throwable) e);
                                    } catch (Throwable th4) {
                                        Uri uri7 = partURI2;
                                        String str9 = type;
                                        iOException = th4;
                                        is = null;
                                        if (is != null) {
                                            try {
                                                is.close();
                                                InputStream inputStream = is;
                                            } catch (IOException e6) {
                                                IOException iOException2 = e6;
                                                InputStream inputStream2 = is;
                                                Log.e(TAG, "Failed to close stream", e6);
                                            }
                                        }
                                        throw iOException;
                                    }
                                }
                                String text = c2.getString(8);
                                if (charset != null && charset.intValue() == 3) {
                                    charset = 106;
                                }
                                if (!"true".equals("true")) {
                                    byte[] blob = new EncodedStringValue(text != null ? text : "").getTextString();
                                    baos.write(blob, 0, blob.length);
                                } else if (charset == null || charset.intValue() == 0) {
                                    byte[] blob2 = new EncodedStringValue(text != null ? text : "").getTextString();
                                    baos.write(blob2, 0, blob2.length);
                                } else {
                                    try {
                                        EncodedStringValue v = new EncodedStringValue(charset.intValue(), text != null ? text : "");
                                        byte[] blob3 = v.getTextString();
                                        EncodedStringValue encodedStringValue = v;
                                        baos.write(blob3, 0, blob3.length);
                                    } catch (NullPointerException e7) {
                                        Log.e(TAG, "Failed to EncodedStringValue: ", e7);
                                    } catch (Exception e8) {
                                        Log.e(TAG, "Failed to EncodedStringValue: ", e8);
                                    }
                                }
                                part.setData(baos.toByteArray());
                            }
                            parts[partIdx] = part;
                            partIdx++;
                            pduPersister = this;
                            long j3 = msgId;
                            i = loadType;
                            i2 = 1;
                        } else {
                            throw new MmsException("Content-Type must be set.");
                        }
                    }
                    if (c2 != null) {
                        c2.close();
                    }
                    return parts;
                }
            } catch (IOException e9) {
                IOException iOException3 = e9;
                Log.e(TAG, "Failed to close stream", e9);
            } catch (Throwable th5) {
                if (c2 != null) {
                    c2.close();
                }
                throw th5;
            }
        }
        if (c2 != null) {
            c2.close();
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
                        if (addrType != 137) {
                            if (addrType != 151) {
                                switch (addrType) {
                                    case 129:
                                    case 130:
                                        break;
                                    default:
                                        Log.e(TAG, "Unknown address type: " + addrType);
                                        continue;
                                }
                            }
                            headers.appendEncodedStringValue(new EncodedStringValue(c.getInt(1), getBytes(addr)), addrType);
                        } else {
                            headers.setEncodedStringValue(new EncodedStringValue(c.getInt(1), getBytes(addr)), addrType);
                        }
                    }
                } finally {
                    c.close();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:?, code lost:
        PDU_CACHE_INSTANCE.put(r9, new com.google.android.mms.util.PduCacheEntry(r1, r11, r12));
        PDU_CACHE_INSTANCE.setUpdating(r9, false);
        PDU_CACHE_INSTANCE.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0238, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x023a, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x0256, code lost:
        throw new com.google.android.mms.MmsException("Unrecognized PDU type: " + java.lang.Integer.toHexString(r6));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0257, code lost:
        r21 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0260, code lost:
        throw new com.google.android.mms.MmsException("Error! ID of the message: -1.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0261, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0279, code lost:
        throw new com.google.android.mms.MmsException("Bad uri: " + r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x027a, code lost:
        if (r2 != null) goto L_0x027c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x027f, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0280, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x0282, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x0283, code lost:
        r14 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0285, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x0292, code lost:
        monitor-enter(PDU_CACHE_INSTANCE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0293, code lost:
        if (0 != 0) goto L_0x0296;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:?, code lost:
        PDU_CACHE_INSTANCE.put(r9, new com.google.android.mms.util.PduCacheEntry(null, r11, r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x02a1, code lost:
        PDU_CACHE_INSTANCE.setUpdating(r9, false);
        PDU_CACHE_INSTANCE.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x02ad, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0034, code lost:
        r5 = PDU_CACHE_INSTANCE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0036, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0037, code lost:
        if (0 == 0) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        PDU_CACHE_INSTANCE.put(r9, new com.google.android.mms.util.PduCacheEntry(null, 0, -1));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0045, code lost:
        PDU_CACHE_INSTANCE.setUpdating(r9, false);
        PDU_CACHE_INSTANCE.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004f, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0050, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0060, code lost:
        if (r25.toString() == null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006c, code lost:
        if (r25.toString().contains("content://fav-mms") == false) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006e, code lost:
        r3 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006f, code lost:
        r7 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0076, code lost:
        r14 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r2 = com.google.android.mms.util.SqliteWrapper.query(r1.mContext, r1.mContentResolver, r9, PDU_PROJECTION, null, null, null);
        r3 = new com.google.android.mms.pdu.PduHeaders();
        r4 = android.content.ContentUris.parseId(r25);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x008d, code lost:
        if (r2 == null) goto L_0x0263;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0093, code lost:
        if (r2.getCount() != 1) goto L_0x0263;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0099, code lost:
        if (r2.moveToFirst() == false) goto L_0x0263;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x009b, code lost:
        r11 = r2.getInt(1);
        r12 = r2.getLong(2);
        r0 = ENCODED_STRING_COLUMN_INDEX_MAP.entrySet();
        r6 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00b4, code lost:
        if (r6.hasNext() == false) goto L_0x00dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00b6, code lost:
        r7 = r6.next();
        setEncodedStringValueToHeaders(r2, r7.getValue().intValue(), r3, r7.getKey().intValue());
        r0 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00dc, code lost:
        r17 = r0;
        r0 = TEXT_STRING_COLUMN_INDEX_MAP.entrySet();
        r6 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ec, code lost:
        if (r6.hasNext() == false) goto L_0x0114;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00ee, code lost:
        r7 = r6.next();
        setTextStringToHeaders(r2, r7.getValue().intValue(), r3, r7.getKey().intValue());
        r0 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0114, code lost:
        r18 = r0;
        r0 = OCTET_COLUMN_INDEX_MAP.entrySet();
        r6 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0124, code lost:
        if (r6.hasNext() == false) goto L_0x014c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0126, code lost:
        r7 = r6.next();
        setOctetToHeaders(r2, r7.getValue().intValue(), r3, r7.getKey().intValue());
        r0 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x014c, code lost:
        r19 = r0;
        r0 = LONG_COLUMN_INDEX_MAP.entrySet();
        r6 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x015c, code lost:
        if (r6.hasNext() == false) goto L_0x0184;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x015e, code lost:
        r7 = r6.next();
        r20 = r6;
        setLongToHeaders(r2, r7.getValue().intValue(), r3, r7.getKey().intValue());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0180, code lost:
        r6 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0184, code lost:
        if (r2 == null) goto L_0x0189;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x018d, code lost:
        if (r4 == -1) goto L_0x0257;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x018f, code lost:
        loadAddress(r4, r3, r14);
        r6 = r3.getOctet(140);
        r7 = new com.google.android.mms.pdu.PduBody();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x019f, code lost:
        if (r6 == 132) goto L_0x01a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x01a3, code lost:
        if (r6 != 128) goto L_0x01a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x01a6, code lost:
        r21 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01a9, code lost:
        r8 = loadParts(r4, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01ad, code lost:
        if (r8 == null) goto L_0x01c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01af, code lost:
        r21 = r0;
        r0 = r8.length;
        r16 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01b4, code lost:
        r1 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01b8, code lost:
        if (r1 >= r0) goto L_0x01ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01ba, code lost:
        r7.addPart(r8[r1]);
        r16 = r1 + 1;
        r0 = r0;
        r1 = r24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01c8, code lost:
        r21 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01ca, code lost:
        switch(r6) {
            case 128: goto L_0x0216;
            case 129: goto L_0x01fb;
            case 130: goto L_0x01f5;
            case 131: goto L_0x01ef;
            case 132: goto L_0x01e9;
            case 133: goto L_0x01e3;
            case 134: goto L_0x01dd;
            case 135: goto L_0x01d7;
            case 136: goto L_0x01d1;
            case 137: goto L_0x01fb;
            case 138: goto L_0x01fb;
            case 139: goto L_0x01fb;
            case 140: goto L_0x01fb;
            case 141: goto L_0x01fb;
            case 142: goto L_0x01fb;
            case 143: goto L_0x01fb;
            case 144: goto L_0x01fb;
            case 145: goto L_0x01fb;
            case 146: goto L_0x01fb;
            case 147: goto L_0x01fb;
            case 148: goto L_0x01fb;
            case 149: goto L_0x01fb;
            case 150: goto L_0x01fb;
            case 151: goto L_0x01fb;
            default: goto L_0x01cd;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01d1, code lost:
        r0 = new com.google.android.mms.pdu.ReadOrigInd(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01d7, code lost:
        r0 = new com.google.android.mms.pdu.ReadRecInd(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01dd, code lost:
        r0 = new com.google.android.mms.pdu.DeliveryInd(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x01e3, code lost:
        r0 = new com.google.android.mms.pdu.AcknowledgeInd(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01e9, code lost:
        r0 = new com.google.android.mms.pdu.RetrieveConf(r3, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01ef, code lost:
        r0 = new com.google.android.mms.pdu.NotifyRespInd(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01f5, code lost:
        r0 = new com.google.android.mms.pdu.NotificationInd(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0215, code lost:
        throw new com.google.android.mms.MmsException("Unsupported PDU type: " + java.lang.Integer.toHexString(r6));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0216, code lost:
        r0 = new com.google.android.mms.pdu.SendReq(r3, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x021c, code lost:
        r1 = r0;
        r2 = PDU_CACHE_INSTANCE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x021f, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x0293  */
    public GenericPdu load(Uri uri) throws MmsException {
        Uri uri2 = uri;
        PduCacheEntry cacheEntry = null;
        int msgBox = 0;
        long threadId = -1;
        int loadType = 0;
        try {
            synchronized (PDU_CACHE_INSTANCE) {
                try {
                    if (PDU_CACHE_INSTANCE.isUpdating(uri2)) {
                        PDU_CACHE_INSTANCE.wait();
                        cacheEntry = (PduCacheEntry) PDU_CACHE_INSTANCE.get(uri2);
                        if (cacheEntry != null) {
                            GenericPdu pdu = cacheEntry.getPdu();
                        }
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "load: ", e);
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
                try {
                    PDU_CACHE_INSTANCE.setUpdating(uri2, true);
                } catch (Throwable th2) {
                    th = th2;
                    cacheEntry = cacheEntry;
                    throw th;
                }
            }
        } catch (Throwable th3) {
            th = th3;
            PduCacheEntry pduCacheEntry = cacheEntry;
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
            values.put("chset", 106);
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
                values.put("seq", -1);
            }
            if (part.getFilename() != null) {
                values.put("fn", toIsoString(part.getFilename()));
            }
            if (part.getName() != null) {
                values.put("name", toIsoString(part.getName()));
            }
            if (part.getContentDisposition() != null) {
                values.put("cd", toIsoString(part.getContentDisposition()));
            }
            if (part.getContentId() != null) {
                values.put("cid", toIsoString(part.getContentId()));
            }
            if (part.getContentLocation() != null) {
                values.put("cl", toIsoString(part.getContentLocation()));
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

    /* JADX WARNING: Removed duplicated region for block: B:145:0x02e7 A[SYNTHETIC, Splitter:B:145:0x02e7] */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x0305 A[SYNTHETIC, Splitter:B:150:0x0305] */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x0323  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x03b2 A[SYNTHETIC, Splitter:B:181:0x03b2] */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x03d0 A[SYNTHETIC, Splitter:B:186:0x03d0] */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x03ee  */
    private void persistData(PduPart part, Uri uri, String contentType, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        FileNotFoundException fileNotFoundException;
        OutputStream os;
        Uri dataUri;
        Uri uri2 = uri;
        String str = contentType;
        HashMap<Uri, InputStream> hashMap = preOpenedFiles;
        OutputStream os2 = null;
        InputStream is = null;
        DrmConvertSession drmConvertSession = null;
        Uri dataUri2 = null;
        String path = null;
        try {
            byte[] data = part.getData();
            if (ContentType.TEXT_PLAIN.equals(str) || ContentType.APP_SMIL.equals(str)) {
                os = os2;
                dataUri = null;
            } else if (ContentType.TEXT_HTML.equals(str)) {
                os = os2;
                dataUri = null;
            } else {
                boolean isDrm = DownloadDrmHelper.isDrmConvertNeeded(contentType);
                if (isDrm) {
                    if (uri2 != null) {
                        try {
                            path = convertUriToPath(this.mContext, uri2);
                            File f = new File(path);
                            if (f.length() > 0) {
                                if (os2 != null) {
                                    try {
                                        os2.close();
                                        File file = f;
                                    } catch (IOException e) {
                                        IOException iOException = e;
                                        StringBuilder sb = new StringBuilder();
                                        File file2 = f;
                                        sb.append("IOException while closing: ");
                                        sb.append(os2);
                                        Log.e(TAG, sb.toString(), e);
                                    }
                                } else {
                                    File file3 = f;
                                }
                                if (is != null) {
                                    try {
                                        is.close();
                                    } catch (IOException e2) {
                                        IOException iOException2 = e2;
                                        Log.e(TAG, "IOException while closing: " + is, e2);
                                    }
                                }
                                if (drmConvertSession != null) {
                                    drmConvertSession.close(path);
                                    File f2 = new File(path);
                                    ContentValues values = new ContentValues(0);
                                    Context context = this.mContext;
                                    ContentResolver contentResolver = this.mContentResolver;
                                    StringBuilder sb2 = new StringBuilder();
                                    OutputStream outputStream = os2;
                                    sb2.append("content://mms/resetFilePerm/");
                                    sb2.append(f2.getName());
                                    SqliteWrapper.update(context, contentResolver, Uri.parse(sb2.toString()), values, null, null);
                                }
                                return;
                            }
                            dataUri = null;
                        } catch (Exception e3) {
                            os = os2;
                            dataUri = null;
                            try {
                                Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e3);
                            } catch (FileNotFoundException e4) {
                                e = e4;
                                Uri uri3 = dataUri;
                                OutputStream outputStream2 = os;
                                Log.e(TAG, "Failed to open Input/Output stream.");
                                throw new MmsException((Throwable) e);
                            } catch (IOException e5) {
                                e = e5;
                                dataUri2 = dataUri;
                                os2 = os;
                                Log.e(TAG, "Failed to read/write data.", e);
                                throw new MmsException((Throwable) e);
                            } catch (Throwable th) {
                                fileNotFoundException = th;
                                os2 = os;
                                if (os2 != null) {
                                    try {
                                        os2.close();
                                    } catch (IOException e6) {
                                        IOException iOException3 = e6;
                                        Log.e(TAG, "IOException while closing: " + os2, e6);
                                    }
                                }
                                if (is != null) {
                                    try {
                                        is.close();
                                    } catch (IOException e7) {
                                        IOException iOException4 = e7;
                                        Log.e(TAG, "IOException while closing: " + is, e7);
                                    }
                                }
                                if (drmConvertSession != null) {
                                    drmConvertSession.close(path);
                                    new File(path);
                                    ContentValues values2 = new ContentValues(0);
                                    SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + f.getName()), values2, null, null);
                                }
                                throw fileNotFoundException;
                            }
                        }
                    } else {
                        dataUri = null;
                    }
                    drmConvertSession = DrmConvertSession.open(this.mContext, str);
                    if (drmConvertSession == null) {
                        throw new MmsException("Mimetype " + str + " can not be converted.");
                    }
                } else {
                    dataUri = null;
                }
                os2 = this.mContentResolver.openOutputStream(uri2);
                if (data == null) {
                    try {
                        Uri dataUri3 = part.getDataUri();
                        if (dataUri3 != null) {
                            try {
                                if (!dataUri3.equals(uri2)) {
                                    if (hashMap != null && hashMap.containsKey(dataUri3)) {
                                        is = hashMap.get(dataUri3);
                                    }
                                    if (is == null) {
                                        is = this.mContentResolver.openInputStream(dataUri3);
                                    }
                                    byte[] buffer = new byte[RadioAccessFamily.EVDO_B];
                                    while (true) {
                                        int read = is.read(buffer);
                                        int len = read;
                                        if (read == -1) {
                                            Uri uri4 = dataUri3;
                                            break;
                                        }
                                        if (!isDrm) {
                                            os2.write(buffer, 0, len);
                                        } else {
                                            byte[] convertedData = drmConvertSession.convert(buffer, len);
                                            if (convertedData != null) {
                                                os2.write(convertedData, 0, convertedData.length);
                                            } else {
                                                throw new MmsException("Error converting drm data.");
                                            }
                                        }
                                        String str2 = contentType;
                                    }
                                    if (os2 != null) {
                                        try {
                                            os2.close();
                                        } catch (IOException e8) {
                                            IOException iOException5 = e8;
                                            Log.e(TAG, "IOException while closing: " + os2, e8);
                                        }
                                    }
                                    if (is != null) {
                                        try {
                                            is.close();
                                        } catch (IOException e9) {
                                            IOException iOException6 = e9;
                                            Log.e(TAG, "IOException while closing: " + is, e9);
                                        }
                                    }
                                    if (drmConvertSession != null) {
                                        drmConvertSession.close(path);
                                        new File(path);
                                        ContentValues values3 = new ContentValues(0);
                                        SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + f.getName()), values3, null, null);
                                    }
                                }
                            } catch (FileNotFoundException e10) {
                                e = e10;
                                Uri uri5 = dataUri3;
                                Log.e(TAG, "Failed to open Input/Output stream.");
                                throw new MmsException((Throwable) e);
                            } catch (IOException e11) {
                                e = e11;
                                dataUri2 = dataUri3;
                                Log.e(TAG, "Failed to read/write data.", e);
                                throw new MmsException((Throwable) e);
                            } catch (Throwable th2) {
                                fileNotFoundException = th2;
                                Uri uri6 = dataUri3;
                                if (os2 != null) {
                                }
                                if (is != null) {
                                }
                                if (drmConvertSession != null) {
                                }
                                throw fileNotFoundException;
                            }
                        }
                        Log.w(TAG, "Can't find data for this part.");
                        if (os2 != null) {
                            try {
                                os2.close();
                            } catch (IOException e12) {
                                IOException iOException7 = e12;
                                Log.e(TAG, "IOException while closing: " + os2, e12);
                            }
                        }
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e13) {
                                IOException iOException8 = e13;
                                Log.e(TAG, "IOException while closing: " + is, e13);
                            }
                        }
                        if (drmConvertSession != null) {
                            drmConvertSession.close(path);
                            new File(path);
                            ContentValues values4 = new ContentValues(0);
                            SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + f.getName()), values4, null, null);
                        }
                        return;
                    } catch (FileNotFoundException e14) {
                        e = e14;
                        Uri uri7 = dataUri;
                    } catch (IOException e15) {
                        e = e15;
                        dataUri2 = dataUri;
                        Log.e(TAG, "Failed to read/write data.", e);
                        throw new MmsException((Throwable) e);
                    } catch (Throwable th3) {
                        th = th3;
                        fileNotFoundException = th;
                        if (os2 != null) {
                        }
                        if (is != null) {
                        }
                        if (drmConvertSession != null) {
                        }
                        throw fileNotFoundException;
                    }
                } else {
                    if (!isDrm) {
                        os2.write(data);
                    } else {
                        Uri dataUri4 = uri2;
                        try {
                            is = new ByteArrayInputStream(data);
                            byte[] buffer2 = new byte[RadioAccessFamily.EVDO_B];
                            while (true) {
                                int read2 = is.read(buffer2);
                                int len2 = read2;
                                if (read2 == -1) {
                                    Uri uri8 = dataUri4;
                                    break;
                                }
                                byte[] convertedData2 = drmConvertSession.convert(buffer2, len2);
                                if (convertedData2 != null) {
                                    os2.write(convertedData2, 0, convertedData2.length);
                                } else {
                                    throw new MmsException("Error converting drm data.");
                                }
                            }
                        } catch (FileNotFoundException e16) {
                            e = e16;
                            Uri uri9 = dataUri4;
                            Log.e(TAG, "Failed to open Input/Output stream.");
                            throw new MmsException((Throwable) e);
                        } catch (IOException e17) {
                            e = e17;
                            dataUri2 = dataUri4;
                            Log.e(TAG, "Failed to read/write data.", e);
                            throw new MmsException((Throwable) e);
                        } catch (Throwable th4) {
                            th = th4;
                            Uri uri10 = dataUri4;
                            fileNotFoundException = th;
                            if (os2 != null) {
                            }
                            if (is != null) {
                            }
                            if (drmConvertSession != null) {
                            }
                            throw fileNotFoundException;
                        }
                    }
                    if (os2 != null) {
                    }
                    if (is != null) {
                    }
                    if (drmConvertSession != null) {
                    }
                }
            }
            if ("true".equals("true")) {
                int charset = part.getCharset();
                ContentValues cv = new ContentValues();
                if (charset != 0) {
                    cv.put("text", data != null ? new EncodedStringValue(charset, data).getString() : "");
                } else {
                    cv.put("text", data != null ? new EncodedStringValue(data).getString() : "");
                }
                if (this.mContentResolver.update(uri2, cv, null, null) != 1) {
                    throw new MmsException("unable to update " + uri.toString());
                }
            } else {
                ContentValues cv2 = new ContentValues();
                cv2.put("text", new EncodedStringValue(data).getString());
                if (this.mContentResolver.update(uri2, cv2, null, null) != 1) {
                    throw new MmsException("unable to update " + uri.toString());
                }
            }
            os2 = os;
            if (os2 != null) {
            }
            if (is != null) {
            }
            if (drmConvertSession != null) {
            }
        } catch (FileNotFoundException e18) {
            e = e18;
            OutputStream outputStream3 = os2;
            Log.e(TAG, "Failed to open Input/Output stream.");
            throw new MmsException((Throwable) e);
        } catch (IOException e19) {
            e = e19;
            OutputStream outputStream4 = os2;
            Log.e(TAG, "Failed to read/write data.", e);
            throw new MmsException((Throwable) e);
        } catch (Throwable e20) {
            fileNotFoundException = e20;
            Uri uri11 = dataUri2;
            if (os2 != null) {
            }
            if (is != null) {
            }
            if (drmConvertSession != null) {
            }
            throw fileNotFoundException;
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
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
                if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
                    throw new IllegalArgumentException("Given Uri could not be found in media store");
                }
                String path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                if (cursor == null) {
                    return path;
                }
                cursor.close();
                return path;
            } catch (SQLiteException e) {
                throw new IllegalArgumentException("Given Uri is not formatted in a way so that it can be found in media store.");
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Given Uri scheme is not supported");
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
        byte[] transId;
        EncodedStringValue subject;
        int priority;
        long date;
        byte[] contentType;
        EncodedStringValue[] array;
        Uri uri2 = uri;
        synchronized (PDU_CACHE_INSTANCE) {
            if (PDU_CACHE_INSTANCE.isUpdating(uri2)) {
                try {
                    PDU_CACHE_INSTANCE.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "updateHeaders: ", e);
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri2);
        ContentValues values = new ContentValues(10);
        byte[] contentType2 = sendReq.getContentType();
        if (contentType2 != null) {
            values.put("ct_t", toIsoString(contentType2));
        }
        long date2 = sendReq.getDate();
        if (date2 != -1) {
            values.put("date", Long.valueOf(date2));
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
        int priority2 = sendReq.getPriority();
        if (priority2 != 0) {
            values.put("pri", Integer.valueOf(priority2));
        }
        int readReport = sendReq.getReadReport();
        if (readReport != 0) {
            values.put("rr", Integer.valueOf(readReport));
        }
        byte[] transId2 = sendReq.getTransactionId();
        if (transId2 != null) {
            values.put("tr_id", toIsoString(transId2));
        }
        EncodedStringValue subject2 = sendReq.getSubject();
        if (subject2 != null) {
            values.put("sub", toIsoString(subject2.getTextString()));
            values.put("sub_cs", Integer.valueOf(subject2.getCharacterSet()));
        } else {
            values.put("sub", "");
        }
        long messageSize = sendReq.getMessageSize();
        if (messageSize > 0) {
            subject = subject2;
            transId = transId2;
            values.put("m_size", Long.valueOf(messageSize));
        } else {
            subject = subject2;
            transId = transId2;
        }
        PduHeaders headers = sendReq.getPduHeaders();
        HashSet<String> recipients = new HashSet<>();
        long messageSize2 = messageSize;
        int[] iArr = ADDRESS_FIELDS;
        int length = iArr.length;
        int readReport2 = readReport;
        int readReport3 = 0;
        while (readReport3 < length) {
            int i = length;
            int addrType = iArr[readReport3];
            EncodedStringValue[] array2 = null;
            int[] iArr2 = iArr;
            if (addrType == 137) {
                EncodedStringValue v = headers.getEncodedStringValue(addrType);
                if (v != null) {
                    priority = priority2;
                    array2 = new EncodedStringValue[]{v};
                } else {
                    priority = priority2;
                }
            } else {
                priority = priority2;
                array2 = headers.getEncodedStringValues(addrType);
            }
            EncodedStringValue[] array3 = array2;
            if (array3 != null) {
                contentType = contentType2;
                date = date2;
                updateAddress(ContentUris.parseId(uri), addrType, array3);
                if (addrType == 151) {
                    int length2 = array3.length;
                    int i2 = 0;
                    while (i2 < length2) {
                        int addrType2 = addrType;
                        EncodedStringValue v2 = array3[i2];
                        if (v2 != null) {
                            array = array3;
                            recipients.add(v2.getString());
                        } else {
                            array = array3;
                        }
                        i2++;
                        addrType = addrType2;
                        array3 = array;
                    }
                }
            } else {
                contentType = contentType2;
                date = date2;
            }
            readReport3++;
            length = i;
            iArr = iArr2;
            priority2 = priority;
            contentType2 = contentType;
            date2 = date;
        }
        int priority3 = priority2;
        byte[] bArr = contentType2;
        long j = date2;
        if (!recipients.isEmpty()) {
            values.put("thread_id", Long.valueOf(Telephony.Threads.getOrCreateThreadId(this.mContext, recipients)));
        }
        long j2 = messageSize2;
        HashSet<String> hashSet = recipients;
        EncodedStringValue encodedStringValue = subject;
        PduHeaders pduHeaders = headers;
        byte[] bArr2 = transId;
        int i3 = readReport2;
        int i4 = priority3;
        SqliteWrapper.update(this.mContext, this.mContentResolver, uri2, values, null, null);
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
            String str = null;
            if (part.getFilename() != null) {
                str = toIsoString(part.getFilename());
                values.put("fn", str);
            }
            if (part.getName() != null) {
                str = toIsoString(part.getName());
                values.put("name", str);
            }
            if (part.getContentDisposition() != null) {
                str = toIsoString(part.getContentDisposition());
                values.put("cd", str);
            }
            if (part.getContentId() != null) {
                str = toIsoString(part.getContentId());
                values.put("cid", str);
            }
            if (part.getContentLocation() != null) {
                str = toIsoString(part.getContentLocation());
                values.put("cl", str);
            }
            Object value = str;
            SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
            if (part.getData() != null || !uri.equals(part.getDataUri())) {
                persistData(part, uri, contentType, preOpenedFiles);
                return;
            }
            return;
        }
        throw new MmsException("MIME type of the part must be set.");
    }

    /* JADX INFO: finally extract failed */
    public void updateParts(Uri uri, PduBody body, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        Uri uri2 = uri;
        PduBody pduBody = body;
        HashMap<Uri, InputStream> hashMap = preOpenedFiles;
        try {
            synchronized (PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri2)) {
                    try {
                        PDU_CACHE_INSTANCE.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "updateParts: ", e);
                    }
                    PduCacheEntry cacheEntry = (PduCacheEntry) PDU_CACHE_INSTANCE.get(uri2);
                    if (cacheEntry != null) {
                        ((MultimediaMessagePdu) cacheEntry.getPdu()).setBody(pduBody);
                    }
                }
                PDU_CACHE_INSTANCE.setUpdating(uri2, true);
            }
            ArrayList<PduPart> toBeCreated = new ArrayList<>();
            HashMap<Uri, PduPart> toBeUpdated = new HashMap<>();
            int partsNum = body.getPartsNum();
            StringBuilder filter = new StringBuilder();
            filter.append('(');
            for (int i = 0; i < partsNum; i++) {
                PduPart part = pduBody.getPart(i);
                Uri partUri = part.getDataUri();
                if (partUri != null && !TextUtils.isEmpty(partUri.getAuthority())) {
                    if (partUri.getAuthority().startsWith("mms")) {
                        toBeUpdated.put(partUri, part);
                        if (filter.length() > 1) {
                            filter.append(" AND ");
                        }
                        filter.append(HbpcdLookup.ID);
                        filter.append("!=");
                        DatabaseUtils.appendEscapedSQLString(filter, partUri.getLastPathSegment());
                    }
                }
                toBeCreated.add(part);
            }
            filter.append(')');
            long msgId = ContentUris.parseId(uri);
            Context context = this.mContext;
            ContentResolver contentResolver = this.mContentResolver;
            SqliteWrapper.delete(context, contentResolver, Uri.parse(Telephony.Mms.CONTENT_URI + "/" + msgId + "/part"), filter.length() > 2 ? filter.toString() : null, null);
            Iterator<PduPart> it = toBeCreated.iterator();
            while (it.hasNext()) {
                persistPart(it.next(), msgId, hashMap);
            }
            for (Map.Entry<Uri, PduPart> e2 : toBeUpdated.entrySet()) {
                updatePart(e2.getKey(), e2.getValue(), hashMap);
            }
            synchronized (PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri2, false);
                PDU_CACHE_INSTANCE.notifyAll();
            }
        } catch (Throwable th) {
            synchronized (PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri2, false);
                PDU_CACHE_INSTANCE.notifyAll();
                throw th;
            }
        }
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        return persist(pdu, uri, createThreadId, groupMmsEnabled, preOpenedFiles, 0);
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles, int subscription) throws MmsException {
        HashSet<String> recipients;
        boolean textOnly;
        int messageSize;
        long dummyId;
        int i;
        Uri res;
        int i2;
        int[] iArr;
        EncodedStringValue[] array;
        PduBody body;
        GenericPdu genericPdu = pdu;
        Uri uri2 = uri;
        if (uri2 != null) {
            long msgId = -1;
            try {
                msgId = ContentUris.parseId(uri);
            } catch (NumberFormatException e) {
            }
            long msgId2 = msgId;
            boolean existingUri = msgId2 != -1;
            if (existingUri || MESSAGE_BOX_MAP.get(uri2) != null) {
                synchronized (PDU_CACHE_INSTANCE) {
                    if (PDU_CACHE_INSTANCE.isUpdating(uri2)) {
                        try {
                            PDU_CACHE_INSTANCE.wait();
                        } catch (InterruptedException e2) {
                            Log.e(TAG, "persist1: ", e2);
                        }
                    }
                }
                PDU_CACHE_INSTANCE.purge(uri2);
                PduHeaders header = pdu.getPduHeaders();
                PduBody body2 = null;
                ContentValues values = new ContentValues();
                for (Map.Entry<Integer, String> e3 : ENCODED_STRING_COLUMN_NAME_MAP.entrySet()) {
                    int field = e3.getKey().intValue();
                    EncodedStringValue encodedString = header.getEncodedStringValue(field);
                    if (encodedString != null) {
                        body = body2;
                        values.put(e3.getValue(), toIsoString(encodedString.getTextString()));
                        values.put(CHARSET_COLUMN_NAME_MAP.get(Integer.valueOf(field)), Integer.valueOf(encodedString.getCharacterSet()));
                    } else {
                        body = body2;
                    }
                    body2 = body;
                }
                PduBody body3 = body2;
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
                int[] iArr2 = ADDRESS_FIELDS;
                int length = iArr2.length;
                int i3 = 0;
                while (i3 < length) {
                    int addrType = iArr2[i3];
                    if (addrType == 137) {
                        EncodedStringValue v = header.getEncodedStringValue(addrType);
                        String str = null;
                        if (v != null) {
                            str = v.getString();
                        }
                        iArr = iArr2;
                        String str2 = str;
                        if (str2 == null || str2.length() == 0) {
                            i2 = length;
                            EncodedStringValue encodedStringValue = v;
                            array = new EncodedStringValue[]{new EncodedStringValue(this.mContext.getString(33685936))};
                        } else {
                            String str3 = str2;
                            array = new EncodedStringValue[]{v};
                            i2 = length;
                        }
                    } else {
                        iArr = iArr2;
                        i2 = length;
                        array = header.getEncodedStringValues(addrType);
                    }
                    addressMap.put(Integer.valueOf(addrType), array);
                    i3++;
                    iArr2 = iArr;
                    length = i2;
                }
                HashSet<String> recipients2 = new HashSet<>();
                int msgType = pdu.getMessageType();
                if (msgType == 130 || msgType == 132 || msgType == 128) {
                    if (msgType == 128) {
                        loadRecipients(151, recipients2, addressMap, false);
                    } else if (msgType == 130 || msgType == 132) {
                        loadRecipients(137, recipients2, addressMap, false);
                        if (groupMmsEnabled && (this.mHwCustPduPersister == null || !this.mHwCustPduPersister.isShortCodeFeatureEnabled() || !this.mHwCustPduPersister.hasShortCode(addressMap.get(151), addressMap.get(130)))) {
                            loadRecipients(151, recipients2, addressMap, true);
                            HwTelephonyFactory.getHwInnerSmsManager().filterMyNumber(this.mContext, groupMmsEnabled, recipients2, addressMap, subscription);
                            loadRecipients(130, recipients2, addressMap, true);
                        }
                    }
                    long threadId = 0;
                    if (createThreadId && !recipients2.isEmpty()) {
                        threadId = Telephony.Threads.getOrCreateThreadId(this.mContext, recipients2);
                    }
                    values.put("thread_id", Long.valueOf(threadId));
                }
                long dummyId2 = System.currentTimeMillis();
                int messageSize2 = 0;
                PduHeaders pduHeaders = header;
                if (genericPdu instanceof MultimediaMessagePdu) {
                    PduBody body4 = ((MultimediaMessagePdu) genericPdu).getBody();
                    if (body4 != null) {
                        int partsNum = body4.getPartsNum();
                        textOnly = true;
                        if (partsNum > 2) {
                            textOnly = false;
                        }
                        int i4 = 0;
                        while (i4 < partsNum) {
                            int partsNum2 = partsNum;
                            PduPart part = body4.getPart(i4);
                            messageSize2 += part.getDataLength();
                            HashSet<String> recipients3 = recipients2;
                            persistPart(part, dummyId2, preOpenedFiles);
                            PduBody body5 = body4;
                            String contentType = getPartContentType(part);
                            if (contentType != null) {
                                PduPart pduPart = part;
                                if (!ContentType.APP_SMIL.equals(contentType) && !ContentType.TEXT_PLAIN.equals(contentType)) {
                                    textOnly = false;
                                }
                            }
                            i4++;
                            partsNum = partsNum2;
                            recipients2 = recipients3;
                            body4 = body5;
                        }
                        recipients = recipients2;
                        HashMap<Uri, InputStream> hashMap = preOpenedFiles;
                        messageSize = messageSize2;
                    } else {
                        textOnly = true;
                        recipients = recipients2;
                        HashMap<Uri, InputStream> hashMap2 = preOpenedFiles;
                        messageSize = 0;
                    }
                } else {
                    textOnly = true;
                    recipients = recipients2;
                    HashMap<Uri, InputStream> hashMap3 = preOpenedFiles;
                    messageSize = 0;
                    PduBody pduBody = body3;
                }
                values.put("text_only", Integer.valueOf(textOnly ? 1 : 0));
                if (values.getAsInteger("m_size") == null) {
                    values.put("m_size", Integer.valueOf(messageSize));
                }
                values.put("sub_id", Integer.valueOf(subscription));
                if (existingUri) {
                    Uri res2 = uri2;
                    dummyId = dummyId2;
                    i = 0;
                    int i5 = msgType;
                    HashSet<String> hashSet = recipients;
                    SqliteWrapper.update(this.mContext, this.mContentResolver, res2, values, null, null);
                    res = res2;
                } else {
                    dummyId = dummyId2;
                    int i6 = msgType;
                    HashSet<String> hashSet2 = recipients;
                    i = 0;
                    res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri2, values);
                    if (res != null) {
                        msgId2 = ContentUris.parseId(res);
                    } else {
                        long j = dummyId;
                        throw new MmsException("persist() failed: return null.");
                    }
                }
                ContentValues values2 = new ContentValues(1);
                values2.put("mid", Long.valueOf(msgId2));
                SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + dummyId + "/part"), values2, null, null);
                if (!existingUri) {
                    res = Uri.parse(uri2 + "/" + msgId2);
                }
                int[] iArr3 = ADDRESS_FIELDS;
                int length2 = iArr3.length;
                int i7 = i;
                while (i7 < length2) {
                    int addrType2 = iArr3[i7];
                    int messageSize3 = messageSize;
                    EncodedStringValue[] array2 = addressMap.get(Integer.valueOf(addrType2));
                    if (array2 != null) {
                        persistAddress(msgId2, addrType2, array2);
                    }
                    i7++;
                    messageSize = messageSize3;
                }
                return res;
            }
            throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
        }
        throw new MmsException("Uri may not be null.");
    }

    private void loadRecipients(int addressType, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
        HashSet<String> hashSet = recipients;
        EncodedStringValue[] array = addressMap.get(Integer.valueOf(addressType));
        if (array != null) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(this.mContext);
            Set<String> myPhoneNumbers = new HashSet<>();
            if (excludeMyNumber) {
                for (int subid : subscriptionManager.getActiveSubscriptionIdList()) {
                    String myNumber = this.mTelephonyManager.getLine1Number(subid);
                    if (TextUtils.isEmpty(myNumber)) {
                        myNumber = Settings.Secure.getString(this.mContentResolver, "localNumberFromDb_" + subid);
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
                        if (isAddNumber && !hashSet.contains(number)) {
                            hashSet.add(number);
                        }
                    } else if (!hashSet.contains(number)) {
                        hashSet.add(number);
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
            return "";
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
