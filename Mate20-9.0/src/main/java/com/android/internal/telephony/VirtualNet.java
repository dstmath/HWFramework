package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.util.XmlUtils;
import huawei.com.android.internal.telephony.RoamingBroker;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class VirtualNet {
    static final String APN_ID = "apn_id";
    private static final String CTRoamingNumeric = "20404";
    private static final boolean DBG = true;
    static final String FILE_FROM_CUST_DIR = "/data/cust/xml/specialImsiList-conf.xml";
    static final String FILE_FROM_SYSTEM_ETC_DIR = "/system/etc/specialImsiList-conf.xml";
    static final int IMSIEND = 1;
    static final int IMSISTART = 0;
    private static final String LOG_TAG = "GSM";
    private static final int MAX_PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    static final String PARAM_SPECIALIMSI_PATH = "etc/specialImsiList-conf.xml";
    static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    static final String SPECIAL_IMSI_CONFIG_FILE = "specialImsiList-conf.xml";
    static final int SPECIAL_IMSI_CONFIG_SIZE = 3;
    private static final String SPN_EMPTY = "spn_null";
    private static final String SPN_START = "voda";
    private static final int SUB1 = 0;
    private static final int SUB2 = 1;
    static final int VNKEY = 2;
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static ArrayList<String[]> mSpecialImsiList;
    private static UiccCard[] mUiccCards = new UiccCard[MAX_PHONE_COUNT];
    private static VirtualNet mVirtualNet = null;
    private static VirtualNet mVirtualNet1 = null;
    private static Map<SpecialFile, byte[]> specialFilesMap = new HashMap();
    private static Map<SpecialFile, byte[]> specialFilesMap1 = new HashMap();
    private static boolean specialImsiLoaded = false;
    private String apnFilter;
    private String eccNoCard;
    private String eccWithCard;
    private boolean isRealNetwork = false;
    private int maxMessageSize;
    private int numMatch;
    private int numMatchShort;
    private String numeric;
    private String operatorName;
    private int plmnSameImsiStartCount = 0;
    private int sms7BitEnabled;
    private int smsCodingNational;
    private int smsToMmsTextThreshold;
    private String voicemailNumber;
    private String voicemailTag;

    private static class SpecialFile {
        public String fileId;
        public String filePath;

        public SpecialFile(String filePath2, String fileId2) {
            this.filePath = filePath2;
            this.fileId = fileId2;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof SpecialFile)) {
                return false;
            }
            SpecialFile other = (SpecialFile) obj;
            if (this.filePath != null && this.fileId != null && this.filePath.equals(other.filePath) && this.fileId.equals(other.fileId)) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            if (this.filePath == null || this.fileId == null) {
                return 0;
            }
            return (this.filePath.hashCode() * 31) + this.fileId.hashCode();
        }
    }

    public static void addSpecialFile(String filePath, String fileId, byte[] bytes) {
        specialFilesMap.put(new SpecialFile(filePath, fileId), bytes);
    }

    public static void addSpecialFile(String filePath, String fileId, byte[] bytes, int slotId) {
        SpecialFile specialFile = new SpecialFile(filePath, fileId);
        if (slotId == 1) {
            specialFilesMap1.put(specialFile, bytes);
        } else if (slotId == 0) {
            specialFilesMap.put(specialFile, bytes);
        }
    }

    public static VirtualNet getCurrentVirtualNet() {
        if (!isMultiSimEnabled) {
            return mVirtualNet;
        }
        if (hasIccCard(0)) {
            return mVirtualNet;
        }
        if (hasIccCard(1)) {
            return mVirtualNet1;
        }
        return null;
    }

    public static VirtualNet getCurrentVirtualNet(int slotId) {
        logd("getCurrentVirtualNet, slotId=" + slotId);
        if (slotId == 1) {
            return mVirtualNet1;
        }
        if (slotId == 0) {
            return mVirtualNet;
        }
        return null;
    }

    public static boolean isVirtualNet() {
        boolean z = true;
        if (!isMultiSimEnabled) {
            if (mVirtualNet == null) {
                z = false;
            }
            return z;
        } else if (hasIccCard(0)) {
            return isVirtualNet(0);
        } else {
            if (hasIccCard(1)) {
                return isVirtualNet(1);
            }
            return false;
        }
    }

    public static boolean isVirtualNet(int slotId) {
        boolean z = false;
        if (slotId == 1) {
            if (mVirtualNet1 != null) {
                z = true;
            }
            return z;
        } else if (slotId != 0) {
            return false;
        } else {
            if (mVirtualNet != null) {
                z = true;
            }
            return z;
        }
    }

    private static void logd(String text) {
        Log.d(LOG_TAG, "[VirtualNet] " + text);
    }

    private static void loge(String text) {
        Log.e(LOG_TAG, "[VirtualNet] " + text);
    }

    public static void loadSpecialFiles(String numeric2, SIMRecords simRecords) {
        if (numeric2 == null) {
            loge("number is null,loadSpecialFiles will return");
            return;
        }
        int slotId = simRecords.getSlotId();
        if (isMultiSimEnabled) {
            logd("start loadSpecialFiles:slotId= " + slotId);
            if (slotId == 1) {
                specialFilesMap1.clear();
            } else {
                specialFilesMap.clear();
            }
        } else {
            specialFilesMap.clear();
        }
        String[] selectionArgs = {numeric2, Integer.toString(4)};
        Phone phone = PhoneFactory.getDefaultPhone();
        Cursor cursor = phone.getContext().getContentResolver().query(HwTelephony.VirtualNets.CONTENT_URI, new String[]{"numeric", HwTelephony.VirtualNets.VIRTUAL_NET_RULE, HwTelephony.VirtualNets.MATCH_PATH, HwTelephony.VirtualNets.MATCH_FILE}, "numeric = ? AND virtual_net_rule = ?", selectionArgs, null);
        if (cursor == null) {
            loge("query virtual net db got a null cursor");
            return;
        }
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String matchPath = cursor.getString(cursor.getColumnIndex(HwTelephony.VirtualNets.MATCH_PATH));
                String matchFile = cursor.getString(cursor.getColumnIndex(HwTelephony.VirtualNets.MATCH_FILE));
                SpecialFile specialFile = new SpecialFile(matchPath, matchFile);
                if (slotId == 1) {
                    if (!specialFilesMap1.containsKey(specialFile)) {
                        logd("slotId == SUB2, load specialFile from specialFilesMap1");
                        specialFilesMap1.put(specialFile, null);
                        simRecords.loadFile(matchPath, matchFile);
                    }
                } else if (!specialFilesMap.containsKey(specialFile)) {
                    logd("slotId != SUB2, load specialFile from specialFilesMap");
                    specialFilesMap.put(specialFile, null);
                    simRecords.loadFile(matchPath, matchFile);
                }
                cursor.moveToNext();
            }
        } catch (Exception e) {
            loge("loadVirtualNet got Exception");
        } catch (Throwable th) {
            cursor.close();
            throw th;
        }
        cursor.close();
    }

    public static void removeVirtualNet(int slotId) {
        logd("removeVirtualNet: slotId= " + slotId);
        if (isMultiSimEnabled) {
            if (slotId == 1) {
                mVirtualNet1 = null;
            } else {
                mVirtualNet = null;
            }
            if (PhoneFactory.getDefaultPhone() != null && PhoneFactory.getDefaultPhone().getContext() != null) {
                ContentResolver contentResolver = PhoneFactory.getDefaultPhone().getContext().getContentResolver();
                Settings.System.putString(contentResolver, HwTelephony.VirtualNets.VN_KEY + slotId, "");
                ContentResolver contentResolver2 = PhoneFactory.getDefaultPhone().getContext().getContentResolver();
                Settings.System.putString(contentResolver2, HwTelephony.VirtualNets.VN_KEY_FOR_SPECIALIMSI + slotId, "");
                return;
            }
            return;
        }
        mVirtualNet = null;
        if (PhoneFactory.getDefaultPhone() != null && PhoneFactory.getDefaultPhone().getContext() != null) {
            Settings.System.putString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "vn_key0", "");
            Settings.System.putString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "vn_key_for_specialimsi0", "");
        }
    }

    private static String getSimRecordsImsi(SIMRecords simRecords) {
        String imsi = simRecords.getIMSI();
        int slotId = simRecords.getSlotId();
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
                return imsi;
            }
            String imsi2 = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerImsi(Integer.valueOf(slotId));
            Log.d(LOG_TAG, "VirtualNet RoamingBrokerActivated, set homenetwork imsi");
            return imsi2;
        } else if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            return imsi;
        } else {
            String imsi3 = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerImsi();
            Log.d(LOG_TAG, "VirtualNet RoamingBrokerActivated, set homenetwork imsi");
            return imsi3;
        }
    }

    private static void clearVirtualNet(int slotId) {
        if (isMultiSimEnabled) {
            logd("start loadVirtualNet: slotId= " + slotId);
            if (slotId == 1) {
                mVirtualNet1 = null;
            } else {
                mVirtualNet = null;
            }
        } else {
            mVirtualNet = null;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0367, code lost:
        r9.moveToNext();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x036a, code lost:
        r4 = r10;
        r5 = r39;
        r11 = r40;
        r1 = r41;
     */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x039f  */
    public static void loadVirtualNet(String numeric2, SIMRecords simRecords) {
        String vn_key;
        String[] projection;
        boolean isCTRoaming;
        boolean z;
        String str = numeric2;
        logd("start loadVirtualNet: numeric= " + str);
        int slotId = simRecords.getSlotId();
        boolean z2 = true;
        boolean isCTRoaming2 = HwTelephonyManagerInner.getDefault().isCTSimCard(slotId) && CTRoamingNumeric.equals(str);
        if (isCTRoaming2) {
            logd("CT sim card 20404 is not virtualnet");
            removeVirtualNet(slotId);
            clearCurVirtualNetsDb(slotId);
        } else if (str == null) {
            loge("number is null,loadVirtualNet will return");
        } else {
            clearVirtualNet(slotId);
            logd("thread = " + Thread.currentThread().getName());
            String imsi = getSimRecordsImsi(simRecords);
            byte[] gid1 = simRecords.getGID1();
            String spn = simRecords.getServiceProviderName();
            logd("start loadVirtualNet: numeric=" + str + "; gid1=" + IccUtils.bytesToHexString(gid1) + "; spn=" + spn);
            Phone phone = PhoneFactory.getDefaultPhone();
            String[] projection2 = {"numeric", HwTelephony.VirtualNets.VIRTUAL_NET_RULE, HwTelephony.VirtualNets.IMSI_START, HwTelephony.VirtualNets.GID1, HwTelephony.VirtualNets.GID_MASK, "spn", HwTelephony.VirtualNets.MATCH_PATH, HwTelephony.VirtualNets.MATCH_FILE, HwTelephony.VirtualNets.MATCH_VALUE, HwTelephony.VirtualNets.MATCH_MASK, HwTelephony.VirtualNets.APN_FILTER, HwTelephony.VirtualNets.VOICEMAIL_NUMBER, HwTelephony.VirtualNets.VOICEMAIL_TAG, "num_match", "num_match_short", "sms_7bit_enabled", "sms_coding_national", HwTelephony.VirtualNets.ONS_NAME, "max_message_size", "sms_to_mms_textthreshold", HwTelephony.VirtualNets.ECC_WITH_CARD, HwTelephony.VirtualNets.ECC_NO_CARD, HwTelephony.VirtualNets.VN_KEY};
            saveLastVirtualNetsDb(slotId);
            clearCurVirtualNetsDb(slotId);
            Cursor cursor = phone.getContext().getContentResolver().query(HwTelephony.VirtualNets.CONTENT_URI, projection2, "numeric = ?", new String[]{str}, null);
            if (cursor == null) {
                loge("query virtual net db got a null cursor");
                return;
            }
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int tmpVirtualNetRule = cursor.getInt(cursor.getColumnIndex(HwTelephony.VirtualNets.VIRTUAL_NET_RULE));
                    switch (tmpVirtualNetRule) {
                        case 1:
                            isCTRoaming = isCTRoaming2;
                            projection = projection2;
                            String tmpImsiStart = cursor.getString(cursor.getColumnIndex(HwTelephony.VirtualNets.IMSI_START));
                            if (isImsiVirtualNet(imsi, tmpImsiStart)) {
                                logd("find imsi virtual net imsiStart=" + tmpImsiStart);
                                createVirtualNet(cursor, slotId);
                                ContentResolver contentResolver = phone.getContext().getContentResolver();
                                z = true;
                                Settings.System.putInt(contentResolver, HwTelephony.VirtualNets.VIRTUAL_NET_RULE + slotId, 1);
                                ContentResolver contentResolver2 = phone.getContext().getContentResolver();
                                Settings.System.putString(contentResolver2, HwTelephony.VirtualNets.IMSI_START + slotId, tmpImsiStart);
                                break;
                            }
                        case 2:
                            isCTRoaming = isCTRoaming2;
                            projection = projection2;
                            String tmpGid1Value = cursor.getString(cursor.getColumnIndex(HwTelephony.VirtualNets.GID1));
                            String tmpGidMask = cursor.getString(cursor.getColumnIndex(HwTelephony.VirtualNets.GID_MASK));
                            if (isGid1VirtualNet(gid1, tmpGid1Value, tmpGidMask)) {
                                logd("find gid1 virtual net spn=" + tmpGid1Value);
                                createVirtualNet(cursor, slotId);
                                ContentResolver contentResolver3 = phone.getContext().getContentResolver();
                                Settings.System.putInt(contentResolver3, HwTelephony.VirtualNets.VIRTUAL_NET_RULE + slotId, 2);
                                ContentResolver contentResolver4 = phone.getContext().getContentResolver();
                                Settings.System.putString(contentResolver4, HwTelephony.VirtualNets.GID1 + slotId, tmpGid1Value);
                                ContentResolver contentResolver5 = phone.getContext().getContentResolver();
                                Settings.System.putString(contentResolver5, HwTelephony.VirtualNets.GID_MASK + slotId, tmpGidMask);
                            }
                            z = true;
                            break;
                        case 3:
                            isCTRoaming = isCTRoaming2;
                            projection = projection2;
                            String tmpSpn = cursor.getString(cursor.getColumnIndex("spn"));
                            if (isSpnVirtualNet(spn, tmpSpn)) {
                                logd("find spn virtual net spn=" + tmpSpn);
                                createVirtualNet(cursor, slotId);
                                ContentResolver contentResolver6 = phone.getContext().getContentResolver();
                                Settings.System.putInt(contentResolver6, HwTelephony.VirtualNets.VIRTUAL_NET_RULE + slotId, 3);
                                ContentResolver contentResolver7 = phone.getContext().getContentResolver();
                                Settings.System.putString(contentResolver7, "spn" + slotId, tmpSpn);
                            }
                            z = true;
                            break;
                        case 4:
                            String tmpMatchPath = cursor.getString(cursor.getColumnIndex(HwTelephony.VirtualNets.MATCH_PATH));
                            String tmpMatchFile = cursor.getString(cursor.getColumnIndex(HwTelephony.VirtualNets.MATCH_FILE));
                            String tmpMatchValue = cursor.getString(cursor.getColumnIndex(HwTelephony.VirtualNets.MATCH_VALUE));
                            String tmpMatchMask = cursor.getString(cursor.getColumnIndex(HwTelephony.VirtualNets.MATCH_MASK));
                            if (!isSpecialFileVirtualNet(tmpMatchPath, tmpMatchFile, tmpMatchValue, tmpMatchMask, slotId)) {
                                isCTRoaming = isCTRoaming2;
                                projection = projection2;
                                z = true;
                                break;
                            } else {
                                StringBuilder sb = new StringBuilder();
                                isCTRoaming = isCTRoaming2;
                                try {
                                    sb.append("find special file virtual net matchValue =");
                                    sb.append(tmpMatchValue);
                                    logd(sb.toString());
                                    createVirtualNet(cursor, slotId);
                                    ContentResolver contentResolver8 = phone.getContext().getContentResolver();
                                    StringBuilder sb2 = new StringBuilder();
                                    projection = projection2;
                                    sb2.append(HwTelephony.VirtualNets.VIRTUAL_NET_RULE);
                                    sb2.append(slotId);
                                    Settings.System.putInt(contentResolver8, sb2.toString(), 4);
                                    ContentResolver contentResolver9 = phone.getContext().getContentResolver();
                                    Settings.System.putString(contentResolver9, HwTelephony.VirtualNets.MATCH_PATH + slotId, tmpMatchPath);
                                    ContentResolver contentResolver10 = phone.getContext().getContentResolver();
                                    Settings.System.putString(contentResolver10, HwTelephony.VirtualNets.MATCH_FILE + slotId, tmpMatchFile);
                                    ContentResolver contentResolver11 = phone.getContext().getContentResolver();
                                    Settings.System.putString(contentResolver11, HwTelephony.VirtualNets.MATCH_VALUE + slotId, tmpMatchValue);
                                    ContentResolver contentResolver12 = phone.getContext().getContentResolver();
                                    Settings.System.putString(contentResolver12, HwTelephony.VirtualNets.MATCH_MASK + slotId, tmpMatchMask);
                                    z = true;
                                    break;
                                } catch (Exception e) {
                                    String[] strArr = projection2;
                                    try {
                                        loge("loadVirtualNet got Exception");
                                        cursor.close();
                                        deletePreferApnIfNeed(slotId);
                                        loadSpecialImsiList();
                                        vn_key = getVnKeyFromSpecialIMSIList(imsi);
                                        if (!TextUtils.isEmpty(vn_key)) {
                                        }
                                    } catch (Throwable th) {
                                        th = th;
                                        cursor.close();
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    String[] strArr2 = projection2;
                                    cursor.close();
                                    throw th;
                                }
                            }
                            break;
                        default:
                            z = z2;
                            isCTRoaming = isCTRoaming2;
                            projection = projection2;
                            try {
                                logd("unhandled case: " + tmpVirtualNetRule);
                                break;
                            } catch (Exception e2) {
                                break;
                            }
                    }
                }
                String[] strArr3 = projection2;
            } catch (Exception e3) {
                boolean z3 = isCTRoaming2;
                String[] strArr4 = projection2;
                loge("loadVirtualNet got Exception");
                cursor.close();
                deletePreferApnIfNeed(slotId);
                loadSpecialImsiList();
                vn_key = getVnKeyFromSpecialIMSIList(imsi);
                if (!TextUtils.isEmpty(vn_key)) {
                }
            } catch (Throwable th3) {
                th = th3;
                boolean z4 = isCTRoaming2;
                String[] strArr5 = projection2;
                cursor.close();
                throw th;
            }
            cursor.close();
            deletePreferApnIfNeed(slotId);
            loadSpecialImsiList();
            vn_key = getVnKeyFromSpecialIMSIList(imsi);
            if (!TextUtils.isEmpty(vn_key)) {
                ContentResolver contentResolver13 = phone.getContext().getContentResolver();
                Settings.System.putString(contentResolver13, HwTelephony.VirtualNets.VN_KEY_FOR_SPECIALIMSI + slotId, vn_key);
            }
        }
    }

    private static void deletePreferApnIfNeed(int slotId) {
        if (isVirtualNet(slotId) && !isVirtualNetEqual(slotId)) {
            logd("find different virtual net,so setPreferredApn: delete");
            Phone phone = PhoneFactory.getPhone(slotId);
            ContentResolver resolver = phone.getContext().getContentResolver();
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                resolver.delete(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId), null, null);
            } else {
                resolver.delete(PREFERAPN_NO_UPDATE_URI, null, null);
            }
            if (phone.mDcTracker != null) {
                ApnContext apnContext = (ApnContext) phone.mDcTracker.mApnContexts.get("default");
                if (apnContext != null) {
                    ApnSetting apn = apnContext.getApnSetting();
                    logd("current apnSetting is " + apn);
                    if (apn != null && apnContext.getState() == DctConstants.State.CONNECTED) {
                        ContentValues values = new ContentValues();
                        values.put(APN_ID, Integer.valueOf(apn.id));
                        logd("insert prefer apn");
                        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                            resolver.insert(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId), values);
                        } else {
                            resolver.insert(PREFERAPN_NO_UPDATE_URI, values);
                        }
                    }
                }
            }
        }
    }

    private static String getStringFromSettingsEx(ContentResolver resolver, String key, String defaultValue) {
        String value = Settings.System.getString(resolver, key);
        return value == null ? defaultValue : value;
    }

    private static int getIntFromSettingsEx(ContentResolver resolver, String key) {
        return Settings.System.getInt(resolver, key, -99);
    }

    private static void saveLastVirtualNetsDb(int slotId) {
        int i = slotId;
        Phone phone = PhoneFactory.getDefaultPhone();
        ContentResolver contentResolver = phone.getContext().getContentResolver();
        int tmpVirtualNetRule = getIntFromSettingsEx(contentResolver, HwTelephony.VirtualNets.VIRTUAL_NET_RULE + i);
        ContentResolver contentResolver2 = phone.getContext().getContentResolver();
        String tmpImsiStart = getStringFromSettingsEx(contentResolver2, HwTelephony.VirtualNets.IMSI_START + i, "");
        ContentResolver contentResolver3 = phone.getContext().getContentResolver();
        String tmpGid1Value = getStringFromSettingsEx(contentResolver3, HwTelephony.VirtualNets.GID1 + i, "");
        ContentResolver contentResolver4 = phone.getContext().getContentResolver();
        String tmpGidMask = getStringFromSettingsEx(contentResolver4, HwTelephony.VirtualNets.GID_MASK + i, "");
        ContentResolver contentResolver5 = phone.getContext().getContentResolver();
        String tmpSpn = getStringFromSettingsEx(contentResolver5, "spn" + i, "");
        ContentResolver contentResolver6 = phone.getContext().getContentResolver();
        String tmpMatchPath = getStringFromSettingsEx(contentResolver6, HwTelephony.VirtualNets.MATCH_PATH + i, "");
        ContentResolver contentResolver7 = phone.getContext().getContentResolver();
        String tmpMatchFile = getStringFromSettingsEx(contentResolver7, HwTelephony.VirtualNets.MATCH_FILE + i, "");
        ContentResolver contentResolver8 = phone.getContext().getContentResolver();
        String tmpMatchValue = getStringFromSettingsEx(contentResolver8, HwTelephony.VirtualNets.MATCH_VALUE + i, "");
        ContentResolver contentResolver9 = phone.getContext().getContentResolver();
        String tmpMatchMask = getStringFromSettingsEx(contentResolver9, HwTelephony.VirtualNets.MATCH_MASK + i, "");
        ContentResolver contentResolver10 = phone.getContext().getContentResolver();
        String tmpNumeric = getStringFromSettingsEx(contentResolver10, "numeric" + i, "");
        ContentResolver contentResolver11 = phone.getContext().getContentResolver();
        String tmpApnFilter = getStringFromSettingsEx(contentResolver11, HwTelephony.VirtualNets.APN_FILTER + i, "");
        ContentResolver contentResolver12 = phone.getContext().getContentResolver();
        String tmpVoicemalNumber = getStringFromSettingsEx(contentResolver12, HwTelephony.VirtualNets.VOICEMAIL_NUMBER + i, "");
        ContentResolver contentResolver13 = phone.getContext().getContentResolver();
        StringBuilder sb = new StringBuilder();
        String tmpVoicemalNumber2 = tmpVoicemalNumber;
        sb.append(HwTelephony.VirtualNets.VOICEMAIL_TAG);
        sb.append(i);
        String tmpVoicemalTag = getStringFromSettingsEx(contentResolver13, sb.toString(), "");
        ContentResolver contentResolver14 = phone.getContext().getContentResolver();
        StringBuilder sb2 = new StringBuilder();
        String tmpVoicemalTag2 = tmpVoicemalTag;
        sb2.append("num_match");
        sb2.append(i);
        int tmpNumMatch = getIntFromSettingsEx(contentResolver14, sb2.toString());
        ContentResolver contentResolver15 = phone.getContext().getContentResolver();
        StringBuilder sb3 = new StringBuilder();
        int tmpNumMatch2 = tmpNumMatch;
        sb3.append("num_match_short");
        sb3.append(i);
        int tmpNumMatchShort = getIntFromSettingsEx(contentResolver15, sb3.toString());
        ContentResolver contentResolver16 = phone.getContext().getContentResolver();
        StringBuilder sb4 = new StringBuilder();
        int tmpNumMatchShort2 = tmpNumMatchShort;
        sb4.append("sms_7bit_enabled");
        sb4.append(i);
        int tmpSms7BitEnabled = getIntFromSettingsEx(contentResolver16, sb4.toString());
        ContentResolver contentResolver17 = phone.getContext().getContentResolver();
        StringBuilder sb5 = new StringBuilder();
        int tmpSms7BitEnabled2 = tmpSms7BitEnabled;
        sb5.append("sms_coding_national");
        sb5.append(i);
        int tmpSmsCodingNational = getIntFromSettingsEx(contentResolver17, sb5.toString());
        ContentResolver contentResolver18 = phone.getContext().getContentResolver();
        StringBuilder sb6 = new StringBuilder();
        int tmpSmsCodingNational2 = tmpSmsCodingNational;
        sb6.append(HwTelephony.VirtualNets.ONS_NAME);
        sb6.append(i);
        String tmpOperatorName = getStringFromSettingsEx(contentResolver18, sb6.toString(), "");
        ContentResolver contentResolver19 = phone.getContext().getContentResolver();
        StringBuilder sb7 = new StringBuilder();
        String tmpOperatorName2 = tmpOperatorName;
        sb7.append(HwTelephony.VirtualNets.SAVED_VIRTUAL_NET_RULE);
        sb7.append(i);
        Settings.System.putInt(contentResolver19, sb7.toString(), tmpVirtualNetRule);
        ContentResolver contentResolver20 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver20, HwTelephony.VirtualNets.SAVED_IMSI_START + i, tmpImsiStart);
        ContentResolver contentResolver21 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver21, HwTelephony.VirtualNets.SAVED_GID1 + i, tmpGid1Value);
        ContentResolver contentResolver22 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver22, HwTelephony.VirtualNets.SAVED_GID_MASK + i, tmpGidMask);
        ContentResolver contentResolver23 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver23, HwTelephony.VirtualNets.SAVED_SPN + i, tmpSpn);
        ContentResolver contentResolver24 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver24, HwTelephony.VirtualNets.SAVED_MATCH_PATH + i, tmpMatchPath);
        ContentResolver contentResolver25 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver25, HwTelephony.VirtualNets.SAVED_MATCH_FILE + i, tmpMatchFile);
        ContentResolver contentResolver26 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver26, HwTelephony.VirtualNets.SAVED_MATCH_VALUE + i, tmpMatchValue);
        ContentResolver contentResolver27 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver27, HwTelephony.VirtualNets.SAVED_MATCH_MASK + i, tmpMatchMask);
        ContentResolver contentResolver28 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver28, HwTelephony.VirtualNets.SAVED_NUMERIC + i, tmpNumeric);
        ContentResolver contentResolver29 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver29, HwTelephony.VirtualNets.SAVED_APN_FILTER + i, tmpApnFilter);
        ContentResolver contentResolver30 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver30, HwTelephony.VirtualNets.SAVED_VOICEMAIL_NUMBER + i, tmpVoicemalNumber2);
        ContentResolver contentResolver31 = phone.getContext().getContentResolver();
        StringBuilder sb8 = new StringBuilder();
        int i2 = tmpVirtualNetRule;
        sb8.append(HwTelephony.VirtualNets.SAVED_VOICEMAIL_TAG);
        sb8.append(i);
        Settings.System.putString(contentResolver31, sb8.toString(), tmpVoicemalTag2);
        ContentResolver contentResolver32 = phone.getContext().getContentResolver();
        StringBuilder sb9 = new StringBuilder();
        String str = tmpImsiStart;
        sb9.append(HwTelephony.VirtualNets.SAVED_NUM_MATCH);
        sb9.append(i);
        Settings.System.putInt(contentResolver32, sb9.toString(), tmpNumMatch2);
        ContentResolver contentResolver33 = phone.getContext().getContentResolver();
        StringBuilder sb10 = new StringBuilder();
        String str2 = tmpGid1Value;
        sb10.append(HwTelephony.VirtualNets.SAVED_NUM_MATCH_SHORT);
        sb10.append(i);
        int tmpNumMatchShort3 = tmpNumMatchShort2;
        Settings.System.putInt(contentResolver33, sb10.toString(), tmpNumMatchShort3);
        ContentResolver contentResolver34 = phone.getContext().getContentResolver();
        StringBuilder sb11 = new StringBuilder();
        int i3 = tmpNumMatchShort3;
        sb11.append(HwTelephony.VirtualNets.SAVED_SMS_7BIT_ENABLED);
        sb11.append(i);
        int tmpSms7BitEnabled3 = tmpSms7BitEnabled2;
        Settings.System.putInt(contentResolver34, sb11.toString(), tmpSms7BitEnabled3);
        ContentResolver contentResolver35 = phone.getContext().getContentResolver();
        StringBuilder sb12 = new StringBuilder();
        int i4 = tmpSms7BitEnabled3;
        sb12.append(HwTelephony.VirtualNets.SAVED_SMS_CODING_NATIONAL);
        sb12.append(i);
        Settings.System.putInt(contentResolver35, sb12.toString(), tmpSmsCodingNational2);
        ContentResolver contentResolver36 = phone.getContext().getContentResolver();
        StringBuilder sb13 = new StringBuilder();
        Phone phone2 = phone;
        sb13.append(HwTelephony.VirtualNets.SAVED_ONS_NAME);
        sb13.append(i);
        Settings.System.putString(contentResolver36, sb13.toString(), tmpOperatorName2);
    }

    private static void clearCurVirtualNetsDb(int slotId) {
        Phone phone = PhoneFactory.getDefaultPhone();
        ContentResolver contentResolver = phone.getContext().getContentResolver();
        Settings.System.putInt(contentResolver, HwTelephony.VirtualNets.VIRTUAL_NET_RULE + slotId, -99);
        ContentResolver contentResolver2 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver2, HwTelephony.VirtualNets.IMSI_START + slotId, "");
        ContentResolver contentResolver3 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver3, HwTelephony.VirtualNets.GID1 + slotId, "");
        ContentResolver contentResolver4 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver4, HwTelephony.VirtualNets.GID_MASK + slotId, "");
        ContentResolver contentResolver5 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver5, "spn" + slotId, "");
        ContentResolver contentResolver6 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver6, HwTelephony.VirtualNets.MATCH_PATH + slotId, "");
        ContentResolver contentResolver7 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver7, HwTelephony.VirtualNets.MATCH_FILE + slotId, "");
        ContentResolver contentResolver8 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver8, HwTelephony.VirtualNets.MATCH_VALUE + slotId, "");
        ContentResolver contentResolver9 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver9, HwTelephony.VirtualNets.MATCH_MASK + slotId, "");
        ContentResolver contentResolver10 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver10, "numeric" + slotId, "");
        ContentResolver contentResolver11 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver11, HwTelephony.VirtualNets.APN_FILTER + slotId, "");
        ContentResolver contentResolver12 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver12, HwTelephony.VirtualNets.VOICEMAIL_NUMBER + slotId, "");
        ContentResolver contentResolver13 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver13, HwTelephony.VirtualNets.VOICEMAIL_TAG + slotId, "");
        ContentResolver contentResolver14 = phone.getContext().getContentResolver();
        Settings.System.putInt(contentResolver14, "num_match" + slotId, -99);
        ContentResolver contentResolver15 = phone.getContext().getContentResolver();
        Settings.System.putInt(contentResolver15, "num_match_short" + slotId, -99);
        ContentResolver contentResolver16 = phone.getContext().getContentResolver();
        Settings.System.putInt(contentResolver16, "sms_7bit_enabled" + slotId, -99);
        ContentResolver contentResolver17 = phone.getContext().getContentResolver();
        Settings.System.putInt(contentResolver17, "sms_coding_national" + slotId, -99);
        ContentResolver contentResolver18 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver18, HwTelephony.VirtualNets.ONS_NAME + slotId, "");
        ContentResolver contentResolver19 = phone.getContext().getContentResolver();
        Settings.System.putString(contentResolver19, HwTelephony.VirtualNets.VN_KEY + slotId, "");
    }

    /* JADX WARNING: Removed duplicated region for block: B:75:0x06a8  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x06ab  */
    public static boolean isVirtualNetEqual(int slotId) {
        String lastGidMask;
        boolean anyValueNotMatch;
        String curVoicemalTag;
        String curGidMask;
        int i = slotId;
        Phone phone = PhoneFactory.getDefaultPhone();
        if (isVirtualNet(slotId)) {
            ContentResolver contentResolver = phone.getContext().getContentResolver();
            int curVirtualNetRule = getIntFromSettingsEx(contentResolver, HwTelephony.VirtualNets.VIRTUAL_NET_RULE + i);
            ContentResolver contentResolver2 = phone.getContext().getContentResolver();
            String curImsiStart = getStringFromSettingsEx(contentResolver2, HwTelephony.VirtualNets.IMSI_START + i, "");
            ContentResolver contentResolver3 = phone.getContext().getContentResolver();
            String curGid1Value = getStringFromSettingsEx(contentResolver3, HwTelephony.VirtualNets.GID1 + i, "");
            ContentResolver contentResolver4 = phone.getContext().getContentResolver();
            String curGidMask2 = getStringFromSettingsEx(contentResolver4, HwTelephony.VirtualNets.GID_MASK + i, "");
            ContentResolver contentResolver5 = phone.getContext().getContentResolver();
            String curSpn = getStringFromSettingsEx(contentResolver5, "spn" + i, "");
            ContentResolver contentResolver6 = phone.getContext().getContentResolver();
            String curMatchPath = getStringFromSettingsEx(contentResolver6, HwTelephony.VirtualNets.MATCH_PATH + i, "");
            ContentResolver contentResolver7 = phone.getContext().getContentResolver();
            String curMatchFile = getStringFromSettingsEx(contentResolver7, HwTelephony.VirtualNets.MATCH_FILE + i, "");
            ContentResolver contentResolver8 = phone.getContext().getContentResolver();
            String curMatchValue = getStringFromSettingsEx(contentResolver8, HwTelephony.VirtualNets.MATCH_VALUE + i, "");
            ContentResolver contentResolver9 = phone.getContext().getContentResolver();
            String curMatchMask = getStringFromSettingsEx(contentResolver9, HwTelephony.VirtualNets.MATCH_MASK + i, "");
            ContentResolver contentResolver10 = phone.getContext().getContentResolver();
            String curNumeric = getStringFromSettingsEx(contentResolver10, "numeric" + i, "");
            ContentResolver contentResolver11 = phone.getContext().getContentResolver();
            String curApnFilter = getStringFromSettingsEx(contentResolver11, HwTelephony.VirtualNets.APN_FILTER + i, "");
            ContentResolver contentResolver12 = phone.getContext().getContentResolver();
            String curVoicemalNumber = getStringFromSettingsEx(contentResolver12, HwTelephony.VirtualNets.VOICEMAIL_NUMBER + i, "");
            ContentResolver contentResolver13 = phone.getContext().getContentResolver();
            StringBuilder sb = new StringBuilder();
            String curVoicemalNumber2 = curVoicemalNumber;
            sb.append(HwTelephony.VirtualNets.VOICEMAIL_TAG);
            sb.append(i);
            String curVoicemalTag2 = getStringFromSettingsEx(contentResolver13, sb.toString(), "");
            ContentResolver contentResolver14 = phone.getContext().getContentResolver();
            StringBuilder sb2 = new StringBuilder();
            String curVoicemalTag3 = curVoicemalTag2;
            sb2.append("num_match");
            sb2.append(i);
            int curNumMatch = getIntFromSettingsEx(contentResolver14, sb2.toString());
            ContentResolver contentResolver15 = phone.getContext().getContentResolver();
            StringBuilder sb3 = new StringBuilder();
            int curNumMatch2 = curNumMatch;
            sb3.append("num_match_short");
            sb3.append(i);
            int curNumMatchShort = getIntFromSettingsEx(contentResolver15, sb3.toString());
            ContentResolver contentResolver16 = phone.getContext().getContentResolver();
            StringBuilder sb4 = new StringBuilder();
            int curNumMatchShort2 = curNumMatchShort;
            sb4.append("sms_7bit_enabled");
            sb4.append(i);
            int curSms7BitEnabled = getIntFromSettingsEx(contentResolver16, sb4.toString());
            ContentResolver contentResolver17 = phone.getContext().getContentResolver();
            StringBuilder sb5 = new StringBuilder();
            int curSms7BitEnabled2 = curSms7BitEnabled;
            sb5.append("sms_coding_national");
            sb5.append(i);
            int curSmsCodingNational = getIntFromSettingsEx(contentResolver17, sb5.toString());
            ContentResolver contentResolver18 = phone.getContext().getContentResolver();
            StringBuilder sb6 = new StringBuilder();
            int curSmsCodingNational2 = curSmsCodingNational;
            sb6.append(HwTelephony.VirtualNets.ONS_NAME);
            sb6.append(i);
            String curOperatorName = getStringFromSettingsEx(contentResolver18, sb6.toString(), "");
            ContentResolver contentResolver19 = phone.getContext().getContentResolver();
            StringBuilder sb7 = new StringBuilder();
            String curOperatorName2 = curOperatorName;
            sb7.append(HwTelephony.VirtualNets.SAVED_VIRTUAL_NET_RULE);
            sb7.append(i);
            int lastVirtualNetRule = getIntFromSettingsEx(contentResolver19, sb7.toString());
            ContentResolver contentResolver20 = phone.getContext().getContentResolver();
            StringBuilder sb8 = new StringBuilder();
            String curApnFilter2 = curApnFilter;
            sb8.append(HwTelephony.VirtualNets.SAVED_IMSI_START);
            sb8.append(i);
            String lastImsiStart = getStringFromSettingsEx(contentResolver20, sb8.toString(), "");
            ContentResolver contentResolver21 = phone.getContext().getContentResolver();
            StringBuilder sb9 = new StringBuilder();
            String curNumeric2 = curNumeric;
            sb9.append(HwTelephony.VirtualNets.SAVED_GID1);
            sb9.append(i);
            String lastGid1Value = getStringFromSettingsEx(contentResolver21, sb9.toString(), "");
            ContentResolver contentResolver22 = phone.getContext().getContentResolver();
            StringBuilder sb10 = new StringBuilder();
            String curImsiStart2 = curImsiStart;
            sb10.append(HwTelephony.VirtualNets.SAVED_GID_MASK);
            sb10.append(i);
            String lastGidMask2 = getStringFromSettingsEx(contentResolver22, sb10.toString(), "");
            ContentResolver contentResolver23 = phone.getContext().getContentResolver();
            StringBuilder sb11 = new StringBuilder();
            String lastImsiStart2 = lastImsiStart;
            sb11.append(HwTelephony.VirtualNets.SAVED_SPN);
            sb11.append(i);
            String lastSpn = getStringFromSettingsEx(contentResolver23, sb11.toString(), "");
            ContentResolver contentResolver24 = phone.getContext().getContentResolver();
            StringBuilder sb12 = new StringBuilder();
            String lastGidMask3 = lastGidMask2;
            sb12.append(HwTelephony.VirtualNets.SAVED_MATCH_PATH);
            sb12.append(i);
            String lastMatchPath = getStringFromSettingsEx(contentResolver24, sb12.toString(), "");
            ContentResolver contentResolver25 = phone.getContext().getContentResolver();
            StringBuilder sb13 = new StringBuilder();
            String curGidMask3 = curGidMask2;
            sb13.append(HwTelephony.VirtualNets.SAVED_MATCH_FILE);
            sb13.append(i);
            String lastMatchFile = getStringFromSettingsEx(contentResolver25, sb13.toString(), "");
            ContentResolver contentResolver26 = phone.getContext().getContentResolver();
            StringBuilder sb14 = new StringBuilder();
            String curGid1Value2 = curGid1Value;
            sb14.append(HwTelephony.VirtualNets.SAVED_MATCH_VALUE);
            sb14.append(i);
            String lastMatchValue = getStringFromSettingsEx(contentResolver26, sb14.toString(), "");
            ContentResolver contentResolver27 = phone.getContext().getContentResolver();
            StringBuilder sb15 = new StringBuilder();
            String lastGid1Value2 = lastGid1Value;
            sb15.append(HwTelephony.VirtualNets.SAVED_MATCH_MASK);
            sb15.append(i);
            String lastMatchMask = getStringFromSettingsEx(contentResolver27, sb15.toString(), "");
            ContentResolver contentResolver28 = phone.getContext().getContentResolver();
            StringBuilder sb16 = new StringBuilder();
            String curSpn2 = curSpn;
            sb16.append(HwTelephony.VirtualNets.SAVED_NUMERIC);
            sb16.append(i);
            String lastNumeric = getStringFromSettingsEx(contentResolver28, sb16.toString(), "");
            ContentResolver contentResolver29 = phone.getContext().getContentResolver();
            StringBuilder sb17 = new StringBuilder();
            String lastNumeric2 = lastNumeric;
            sb17.append(HwTelephony.VirtualNets.SAVED_APN_FILTER);
            sb17.append(i);
            String lastApnFilter = getStringFromSettingsEx(contentResolver29, sb17.toString(), "");
            ContentResolver contentResolver30 = phone.getContext().getContentResolver();
            StringBuilder sb18 = new StringBuilder();
            String lastApnFilter2 = lastApnFilter;
            sb18.append(HwTelephony.VirtualNets.SAVED_VOICEMAIL_NUMBER);
            sb18.append(i);
            String lastVoicemalNumber = getStringFromSettingsEx(contentResolver30, sb18.toString(), "");
            ContentResolver contentResolver31 = phone.getContext().getContentResolver();
            StringBuilder sb19 = new StringBuilder();
            String lastVoicemalNumber2 = lastVoicemalNumber;
            sb19.append(HwTelephony.VirtualNets.SAVED_VOICEMAIL_TAG);
            sb19.append(i);
            String lastVoicemalTag = getStringFromSettingsEx(contentResolver31, sb19.toString(), "");
            ContentResolver contentResolver32 = phone.getContext().getContentResolver();
            StringBuilder sb20 = new StringBuilder();
            String lastVoicemalTag2 = lastVoicemalTag;
            sb20.append(HwTelephony.VirtualNets.SAVED_NUM_MATCH);
            sb20.append(i);
            int lastNumMatch = getIntFromSettingsEx(contentResolver32, sb20.toString());
            ContentResolver contentResolver33 = phone.getContext().getContentResolver();
            StringBuilder sb21 = new StringBuilder();
            int lastNumMatch2 = lastNumMatch;
            sb21.append(HwTelephony.VirtualNets.SAVED_NUM_MATCH_SHORT);
            sb21.append(i);
            int lastNumMatchShort = getIntFromSettingsEx(contentResolver33, sb21.toString());
            ContentResolver contentResolver34 = phone.getContext().getContentResolver();
            StringBuilder sb22 = new StringBuilder();
            int lastNumMatchShort2 = lastNumMatchShort;
            sb22.append(HwTelephony.VirtualNets.SAVED_SMS_7BIT_ENABLED);
            sb22.append(i);
            int lastSms7BitEnabled = getIntFromSettingsEx(contentResolver34, sb22.toString());
            ContentResolver contentResolver35 = phone.getContext().getContentResolver();
            StringBuilder sb23 = new StringBuilder();
            int lastSms7BitEnabled2 = lastSms7BitEnabled;
            sb23.append(HwTelephony.VirtualNets.SAVED_SMS_CODING_NATIONAL);
            sb23.append(i);
            int lastSmsCodingNational = getIntFromSettingsEx(contentResolver35, sb23.toString());
            ContentResolver contentResolver36 = phone.getContext().getContentResolver();
            StringBuilder sb24 = new StringBuilder();
            Phone phone2 = phone;
            sb24.append(HwTelephony.VirtualNets.SAVED_ONS_NAME);
            sb24.append(i);
            String lastOperatorName = getStringFromSettingsEx(contentResolver36, sb24.toString(), "");
            if (curVirtualNetRule == 0) {
                logd("RULE_NONE different virtual net");
                return false;
            } else if (curVirtualNetRule != lastVirtualNetRule) {
                logd("curVirtualNetRule != lastVirtualNetRule different virtual net");
                return false;
            } else {
                switch (curVirtualNetRule) {
                    case 1:
                        String str = curGidMask3;
                        String str2 = curGid1Value2;
                        String str3 = lastGid1Value2;
                        String str4 = curSpn2;
                        String str5 = lastGidMask3;
                        String curImsiStart3 = curImsiStart2;
                        lastGidMask = lastImsiStart2;
                        if (curImsiStart3.equals(lastGidMask)) {
                            break;
                        } else {
                            String str6 = curImsiStart3;
                            logd("RULE_IMSI different virtual net");
                            return false;
                        }
                    case 2:
                        String lastGid1Value3 = lastGid1Value2;
                        if (curGid1Value2.equals(lastGid1Value3)) {
                            String str7 = lastGid1Value3;
                            int i2 = curVirtualNetRule;
                            String lastGidMask4 = lastGidMask3;
                            curGidMask = curGidMask3;
                            if (curGidMask.equals(lastGidMask4)) {
                                String str8 = curGidMask;
                                String str9 = lastGidMask4;
                                String str10 = curImsiStart2;
                                lastGidMask = lastImsiStart2;
                                break;
                            }
                        } else {
                            int i3 = curVirtualNetRule;
                            String str11 = lastGidMask3;
                            curGidMask = curGidMask3;
                        }
                        String str12 = curGidMask;
                        logd("RULE_GID1 different virtual net");
                        return false;
                    case 3:
                        if (curSpn2.equals(lastSpn)) {
                            int i4 = curVirtualNetRule;
                            String str13 = curImsiStart2;
                            lastGidMask = lastImsiStart2;
                            String str14 = lastGidMask3;
                            String str15 = curGidMask3;
                            String str16 = curGid1Value2;
                            String str17 = lastGid1Value2;
                            break;
                        } else {
                            logd("RULE_SPN different virtual net");
                            return false;
                        }
                    case 4:
                        if (!(!curMatchPath.equals(lastMatchPath) || !curMatchFile.equals(lastMatchFile) || !curMatchValue.equals(lastMatchValue) || !curMatchMask.equals(lastMatchMask))) {
                            int i5 = curVirtualNetRule;
                            String str18 = curImsiStart2;
                            lastGidMask = lastImsiStart2;
                            String str19 = lastGidMask3;
                            String str20 = curGidMask3;
                            String str21 = curGid1Value2;
                            String str22 = lastGid1Value2;
                            String str23 = curSpn2;
                            break;
                        } else {
                            logd("RULE_MATCH_FILE different virtual net");
                            return false;
                        }
                        break;
                    default:
                        String str24 = lastOperatorName;
                        int i6 = curVirtualNetRule;
                        String str25 = curVoicemalNumber2;
                        String str26 = curVoicemalTag3;
                        int i7 = curNumMatch2;
                        int i8 = curNumMatchShort2;
                        int i9 = curSms7BitEnabled2;
                        int i10 = curSmsCodingNational2;
                        String str27 = curOperatorName2;
                        String str28 = curApnFilter2;
                        String str29 = curNumeric2;
                        String str30 = curImsiStart2;
                        String str31 = lastImsiStart2;
                        String str32 = lastGidMask3;
                        String str33 = curGidMask3;
                        String str34 = curGid1Value2;
                        String str35 = lastGid1Value2;
                        String str36 = curSpn2;
                        String str37 = lastNumeric2;
                        String str38 = lastApnFilter2;
                        String str39 = lastVoicemalNumber2;
                        String str40 = lastVoicemalTag2;
                        int i11 = lastNumMatch2;
                        int i12 = lastNumMatchShort2;
                        int curVirtualNetRule2 = lastSms7BitEnabled2;
                        logd("RULE unkown different virtual net");
                        return false;
                }
                String str41 = lastGidMask;
                String curNumeric3 = curNumeric2;
                String lastNumeric3 = lastNumeric2;
                if (curNumeric3.equals(lastNumeric3)) {
                    String str42 = curNumeric3;
                    String str43 = lastNumeric3;
                    String curApnFilter3 = curApnFilter2;
                    String lastNumeric4 = lastApnFilter2;
                    if (curApnFilter3.equals(lastNumeric4)) {
                        String str44 = curApnFilter3;
                        String str45 = lastNumeric4;
                        String curApnFilter4 = curVoicemalNumber2;
                        String lastVoicemalNumber3 = lastVoicemalNumber2;
                        if (curApnFilter4.equals(lastVoicemalNumber3)) {
                            String str46 = curApnFilter4;
                            String str47 = lastVoicemalNumber3;
                            String curVoicemalNumber3 = curVoicemalTag3;
                            String lastVoicemalTag3 = lastVoicemalTag2;
                            if (curVoicemalNumber3.equals(lastVoicemalTag3)) {
                                String str48 = curVoicemalNumber3;
                                String str49 = lastVoicemalTag3;
                                int curNumMatch3 = curNumMatch2;
                                int lastNumMatch3 = lastNumMatch2;
                                if (curNumMatch3 == lastNumMatch3) {
                                    int i13 = curNumMatch3;
                                    int i14 = lastNumMatch3;
                                    int curNumMatchShort3 = curNumMatchShort2;
                                    int lastNumMatch4 = lastNumMatchShort2;
                                    if (curNumMatchShort3 == lastNumMatch4) {
                                        int i15 = curNumMatchShort3;
                                        int i16 = lastNumMatch4;
                                        int curNumMatchShort4 = curSms7BitEnabled2;
                                        if (curNumMatchShort4 == lastSms7BitEnabled2) {
                                            int i17 = curNumMatchShort4;
                                            int curSmsCodingNational3 = curSmsCodingNational2;
                                            if (curSmsCodingNational3 == lastSmsCodingNational) {
                                                int i18 = curSmsCodingNational3;
                                                curVoicemalTag = curOperatorName2;
                                                if (curVoicemalTag.equals(lastOperatorName)) {
                                                    anyValueNotMatch = false;
                                                    String str50 = curVoicemalTag;
                                                    StringBuilder sb25 = new StringBuilder();
                                                    String str51 = lastOperatorName;
                                                    sb25.append("anyValueNotMatch = ");
                                                    boolean anyValueNotMatch2 = anyValueNotMatch;
                                                    sb25.append(anyValueNotMatch2);
                                                    logd(sb25.toString());
                                                    return anyValueNotMatch2;
                                                }
                                            } else {
                                                curVoicemalTag = curOperatorName2;
                                            }
                                        } else {
                                            int curSms7BitEnabled3 = curNumMatchShort4;
                                            int i19 = curSmsCodingNational2;
                                            curVoicemalTag = curOperatorName2;
                                        }
                                    } else {
                                        int i20 = lastNumMatch4;
                                        int i21 = curSms7BitEnabled2;
                                        int i22 = curSmsCodingNational2;
                                        curVoicemalTag = curOperatorName2;
                                        int lastNumMatchShort3 = lastSms7BitEnabled2;
                                    }
                                } else {
                                    int i23 = lastNumMatch3;
                                    int i24 = curNumMatchShort2;
                                    int i25 = curSms7BitEnabled2;
                                    int i26 = curSmsCodingNational2;
                                    curVoicemalTag = curOperatorName2;
                                    int i27 = lastNumMatchShort2;
                                    int lastNumMatch5 = lastSms7BitEnabled2;
                                }
                            } else {
                                String curVoicemalTag4 = curVoicemalNumber3;
                                String str52 = lastVoicemalTag3;
                                int i28 = curNumMatch2;
                                int i29 = curNumMatchShort2;
                                int i30 = curSms7BitEnabled2;
                                int i31 = curSmsCodingNational2;
                                curVoicemalTag = curOperatorName2;
                                int i32 = lastNumMatch2;
                                int i33 = lastNumMatchShort2;
                                int i34 = lastSms7BitEnabled2;
                            }
                        } else {
                            String curVoicemalNumber4 = curApnFilter4;
                            String str53 = lastVoicemalNumber3;
                            String str54 = curVoicemalTag3;
                            int i35 = curNumMatch2;
                            int i36 = curNumMatchShort2;
                            int i37 = curSms7BitEnabled2;
                            int i38 = curSmsCodingNational2;
                            curVoicemalTag = curOperatorName2;
                            String str55 = lastVoicemalTag2;
                            int i39 = lastNumMatch2;
                            int i40 = lastNumMatchShort2;
                            int i41 = lastSms7BitEnabled2;
                        }
                    } else {
                        String str56 = lastNumeric4;
                        String str57 = curVoicemalNumber2;
                        String str58 = curVoicemalTag3;
                        int i42 = curNumMatch2;
                        int i43 = curNumMatchShort2;
                        int i44 = curSms7BitEnabled2;
                        int i45 = curSmsCodingNational2;
                        curVoicemalTag = curOperatorName2;
                        String str59 = lastVoicemalNumber2;
                        String str60 = lastVoicemalTag2;
                        int i46 = lastNumMatch2;
                        int i47 = lastNumMatchShort2;
                        int i48 = lastSms7BitEnabled2;
                    }
                } else {
                    String str61 = lastNumeric3;
                    String str62 = curVoicemalNumber2;
                    String str63 = curVoicemalTag3;
                    int i49 = curNumMatch2;
                    int i50 = curNumMatchShort2;
                    int i51 = curSms7BitEnabled2;
                    int i52 = curSmsCodingNational2;
                    curVoicemalTag = curOperatorName2;
                    String str64 = curApnFilter2;
                    String str65 = lastApnFilter2;
                    String str66 = lastVoicemalNumber2;
                    String str67 = lastVoicemalTag2;
                    int i53 = lastNumMatch2;
                    int i54 = lastNumMatchShort2;
                    int i55 = lastSms7BitEnabled2;
                }
                anyValueNotMatch = true;
                String str502 = curVoicemalTag;
                StringBuilder sb252 = new StringBuilder();
                String str512 = lastOperatorName;
                sb252.append("anyValueNotMatch = ");
                boolean anyValueNotMatch22 = anyValueNotMatch;
                sb252.append(anyValueNotMatch22);
                logd(sb252.toString());
                return anyValueNotMatch22;
            }
        } else {
            return false;
        }
    }

    protected static boolean isSpecialFileVirtualNet(String matchPath, String matchFile, String matchValue, String matchMask, int slotId) {
        byte[] bytes;
        SpecialFile specialFile = new SpecialFile(matchPath, matchFile);
        if (!isMultiSimEnabled) {
            bytes = specialFilesMap.get(specialFile);
        } else if (slotId == 1) {
            logd("isSpecialFileVirtualNet: slotId == SUB2");
            bytes = specialFilesMap1.get(specialFile);
        } else {
            bytes = specialFilesMap.get(specialFile);
        }
        if (bytes == null) {
            return false;
        }
        return matchByteWithMask(bytes, matchValue, matchMask);
    }

    protected static boolean isSpnVirtualNet(String spn1, String spn2) {
        logd("isSpnVirtualNet spn1 = " + spn1 + "; spn2 = " + spn2);
        boolean z = true;
        if (TextUtils.isEmpty(spn1) && spn2 != null && spn2.equals(SPN_EMPTY)) {
            return true;
        }
        if (!TextUtils.isEmpty(spn1) && spn1.toUpperCase().startsWith(SPN_START.toUpperCase()) && !TextUtils.isEmpty(spn2) && spn2.equals(SPN_EMPTY)) {
            return true;
        }
        if (spn1 == null || spn2 == null || !spn1.equals(spn2)) {
            z = false;
        }
        return z;
    }

    protected static boolean isGid1VirtualNet(byte[] gid1, String gid1Value, String gidMask) {
        logd("isGid1VirtualNet gid1 = " + IccUtils.bytesToHexString(gid1) + "; gid1Value = " + gid1Value + "; gidMask = " + gidMask);
        return matchByteWithMask(gid1, gid1Value, gidMask);
    }

    private static boolean matchByteWithMask(byte[] data, String value, String mask) {
        boolean isValidOddMask = true;
        if (data == null || value == null || mask == null || data.length * 2 < value.length() - 2 || value.length() < 2 || !value.substring(0, 2).equalsIgnoreCase("0x") || mask.length() < 2 || !mask.substring(0, 2).equalsIgnoreCase("0x")) {
            return false;
        }
        if (!isEmptySimFile(data) || value.equalsIgnoreCase("0xFF")) {
            String valueString = value.substring(2);
            String maskString = mask.substring(2);
            String gid1String = IccUtils.bytesToHexString(data);
            if (maskString == null || gid1String == null || valueString == null || maskString.length() % 2 != 1) {
                isValidOddMask = false;
            }
            if (isValidOddMask) {
                int maskStringLength = maskString.length();
                int gid1StringLength = gid1String.length();
                int valueStringLength = valueString.length();
                logd("Gid1 length is odd ,Gid1 length:" + maskString.length());
                if (gid1StringLength < maskStringLength || maskStringLength != valueStringLength) {
                    logd("Gid1 length is not match");
                    return false;
                }
                int i = 0;
                while (i < maskStringLength) {
                    if (maskString.charAt(i) == 'F' && gid1String.charAt(i) == valueString.charAt(i)) {
                        i++;
                    } else {
                        logd("Gid1 mask did not match");
                        return false;
                    }
                }
                for (int i2 = maskStringLength; i2 < gid1StringLength; i2++) {
                    if (gid1String.charAt(i2) != 'f') {
                        logd("Gid1 string did not match");
                        return false;
                    }
                }
                return true;
            }
            byte[] valueBytes = IccUtils.hexStringToBytes(valueString);
            byte[] maskBytes = IccUtils.hexStringToBytes(maskString);
            if (valueBytes.length != maskBytes.length) {
                return false;
            }
            boolean match = true;
            for (int i3 = 0; i3 < maskBytes.length; i3++) {
                if ((data[i3] & maskBytes[i3]) != valueBytes[i3]) {
                    match = false;
                }
            }
            return match;
        }
        logd("matchByteWithMask data is null");
        return false;
    }

    private static boolean isEmptySimFile(byte[] gid1) {
        boolean isEmptyFile = true;
        for (byte gid1Byte : gid1) {
            if (gid1Byte != -1) {
                isEmptyFile = false;
            }
        }
        return isEmptyFile;
    }

    protected static boolean isImsiVirtualNet(String imsi, String tmpImsiStart) {
        if (imsi == null || tmpImsiStart == null || !imsi.startsWith(tmpImsiStart)) {
            return false;
        }
        return true;
    }

    private static void createVirtualNet(Cursor cursor, int slotId) {
        if (!isMultiSimEnabled) {
            mVirtualNet = getVirtualNet(cursor, 0);
            logd("createVirtualNet sigelcard mVirtualNet =" + mVirtualNet);
        } else if (slotId == 1) {
            mVirtualNet1 = getVirtualNet(cursor, slotId);
            logd("createVirtualNet slotId" + slotId + " mVirtualNet1 =" + mVirtualNet1);
        } else if (slotId == 0) {
            mVirtualNet = getVirtualNet(cursor, slotId);
            logd("createVirtualNet slotId" + slotId + " mVirtualNet =" + mVirtualNet);
        }
    }

    private static VirtualNet getVirtualNet(Cursor cursor, int slotId) {
        String tmpVoicemalNumber;
        Cursor cursor2 = cursor;
        int i = slotId;
        if (cursor2 == null) {
            return null;
        }
        String tmpNumeric = cursor2.getString(cursor2.getColumnIndex("numeric"));
        String tmpApnFilter = cursor2.getString(cursor2.getColumnIndex(HwTelephony.VirtualNets.APN_FILTER));
        String tmpVoicemalNumber2 = cursor2.getString(cursor2.getColumnIndex(HwTelephony.VirtualNets.VOICEMAIL_NUMBER));
        String tmpVoicemalTag = cursor2.getString(cursor2.getColumnIndex(HwTelephony.VirtualNets.VOICEMAIL_TAG));
        int tmpNumMatch = cursor2.getInt(cursor2.getColumnIndex("num_match"));
        int tmpNumMatchShort = cursor2.getInt(cursor2.getColumnIndex("num_match_short"));
        int tmpSms7BitEnabled = cursor2.getInt(cursor2.getColumnIndex("sms_7bit_enabled"));
        int tmpSmsCodingNational = cursor2.getInt(cursor2.getColumnIndex("sms_coding_national"));
        String tmpOperatorName = cursor2.getString(cursor2.getColumnIndex(HwTelephony.VirtualNets.ONS_NAME));
        int tmpmaxmessagesize = cursor2.getInt(cursor2.getColumnIndex("max_message_size"));
        int tmpsmstommstextthreshold = cursor2.getInt(cursor2.getColumnIndex("sms_to_mms_textthreshold"));
        String tempEccWithCard = cursor2.getString(cursor2.getColumnIndex(HwTelephony.VirtualNets.ECC_WITH_CARD));
        String tempEccNoCard = cursor2.getString(cursor2.getColumnIndex(HwTelephony.VirtualNets.ECC_NO_CARD));
        String tmpVnKey = cursor2.getString(cursor2.getColumnIndex(HwTelephony.VirtualNets.VN_KEY));
        VirtualNet virtualNet = null;
        if (tmpNumeric == null || tmpNumeric.trim().length() <= 0) {
            tmpVoicemalNumber = tmpApnFilter;
            String str = tmpVoicemalTag;
            String tmpVoicemalTag2 = tmpVnKey;
            String tmpVnKey2 = str;
            int i2 = tmpNumMatch;
            String str2 = tmpOperatorName;
            int i3 = i2;
            int i4 = tmpNumMatchShort;
            int tmpNumMatchShort2 = tmpSmsCodingNational;
            int tmpSmsCodingNational2 = i4;
        } else {
            String tmpOperatorName2 = tmpOperatorName;
            int tmpSmsCodingNational3 = tmpSmsCodingNational;
            String tmpVnKey3 = tmpVnKey;
            int tmpNumMatchShort3 = tmpNumMatchShort;
            int tmpNumMatch2 = tmpNumMatch;
            String tmpVoicemalTag3 = tmpVoicemalTag;
            String tmpVoicemalNumber3 = tmpVoicemalNumber2;
            tmpVoicemalNumber = tmpApnFilter;
            VirtualNet virtualNet2 = new VirtualNet(tmpNumeric, tmpApnFilter, tmpVoicemalNumber2, tmpVoicemalTag, tmpNumMatch, tmpNumMatchShort, tmpSms7BitEnabled, tmpSmsCodingNational3, tmpOperatorName2, tmpmaxmessagesize, tmpsmstommstextthreshold, tempEccWithCard, tempEccNoCard);
            virtualNet = virtualNet2;
            Phone phone = PhoneFactory.getDefaultPhone();
            ContentResolver contentResolver = phone.getContext().getContentResolver();
            Settings.System.putString(contentResolver, "numeric" + i, tmpNumeric);
            ContentResolver contentResolver2 = phone.getContext().getContentResolver();
            Settings.System.putString(contentResolver2, HwTelephony.VirtualNets.APN_FILTER + i, tmpVoicemalNumber);
            ContentResolver contentResolver3 = phone.getContext().getContentResolver();
            Settings.System.putString(contentResolver3, HwTelephony.VirtualNets.VOICEMAIL_NUMBER + i, tmpVoicemalNumber3);
            ContentResolver contentResolver4 = phone.getContext().getContentResolver();
            Settings.System.putString(contentResolver4, HwTelephony.VirtualNets.VOICEMAIL_TAG + i, tmpVoicemalTag3);
            ContentResolver contentResolver5 = phone.getContext().getContentResolver();
            Settings.System.putInt(contentResolver5, "num_match" + i, tmpNumMatch2);
            ContentResolver contentResolver6 = phone.getContext().getContentResolver();
            Settings.System.putInt(contentResolver6, "num_match_short" + i, tmpNumMatchShort3);
            ContentResolver contentResolver7 = phone.getContext().getContentResolver();
            Settings.System.putInt(contentResolver7, "sms_7bit_enabled" + i, tmpSms7BitEnabled);
            ContentResolver contentResolver8 = phone.getContext().getContentResolver();
            Settings.System.putInt(contentResolver8, "sms_coding_national" + i, tmpSmsCodingNational3);
            ContentResolver contentResolver9 = phone.getContext().getContentResolver();
            Settings.System.putString(contentResolver9, HwTelephony.VirtualNets.ONS_NAME + i, tmpOperatorName2);
            ContentResolver contentResolver10 = phone.getContext().getContentResolver();
            Settings.System.putString(contentResolver10, HwTelephony.VirtualNets.VN_KEY + i, tmpVnKey3);
        }
        VirtualNet virtualNet3 = virtualNet;
        Cursor cursor3 = cursor;
        int tmpVirtualNetRule = cursor3.getInt(cursor3.getColumnIndex(HwTelephony.VirtualNets.VIRTUAL_NET_RULE));
        String tmpImsiStart = cursor3.getString(cursor3.getColumnIndex(HwTelephony.VirtualNets.IMSI_START));
        if (tmpVirtualNetRule != 1 || tmpNumeric == null || !tmpNumeric.equals(tmpImsiStart) || virtualNet3 == null) {
            String tmpApnFilter2 = tmpVoicemalNumber;
        } else {
            StringBuilder sb = new StringBuilder();
            String str3 = tmpVoicemalNumber;
            sb.append("getVirtualNet find a realNetwork tmpNumeric = ");
            sb.append(tmpNumeric);
            logd(sb.toString());
            virtualNet3.isRealNetwork = true;
            if (i == 1) {
                if (mVirtualNet1 == null) {
                    virtualNet3.plmnSameImsiStartCount = 1;
                } else {
                    virtualNet3.plmnSameImsiStartCount = mVirtualNet1.plmnSameImsiStartCount + 1;
                }
            } else if (mVirtualNet == null) {
                virtualNet3.plmnSameImsiStartCount = 1;
            } else {
                virtualNet3.plmnSameImsiStartCount = mVirtualNet.plmnSameImsiStartCount + 1;
            }
        }
        return virtualNet3;
    }

    public VirtualNet(String tmpNumeric, String tmpApnFilter, String tmpVoicemalNumber, String tmpVoicemalTag, int tmpNumMatch, int tmpNumMatchShort, int tmpSms7BitEnabled, int tmpSmsCodingNational, String tmpOperatorName, int tmpmaxmessagesize, int tmpsmstommstextthreshold) {
        this.numeric = tmpNumeric;
        this.apnFilter = tmpApnFilter;
        this.voicemailNumber = tmpVoicemalNumber;
        this.voicemailTag = tmpVoicemalTag;
        this.numMatch = tmpNumMatch;
        this.numMatchShort = tmpNumMatchShort;
        this.sms7BitEnabled = tmpSms7BitEnabled;
        this.smsCodingNational = tmpSmsCodingNational;
        this.operatorName = tmpOperatorName;
        this.maxMessageSize = tmpmaxmessagesize;
        this.smsToMmsTextThreshold = tmpsmstommstextthreshold;
    }

    public VirtualNet(String tmpNumeric, String tmpApnFilter, String tmpVoicemalNumber, String tmpVoicemalTag, int tmpNumMatch, int tmpNumMatchShort, int tmpSms7BitEnabled, int tmpSmsCodingNational, String tmpOperatorName, int tmpmaxmessagesize, int tmpsmstommstextthreshold, String tempEccWithCard, String tempEccNoCard) {
        this.numeric = tmpNumeric;
        this.apnFilter = tmpApnFilter;
        this.voicemailNumber = tmpVoicemalNumber;
        this.voicemailTag = tmpVoicemalTag;
        this.numMatch = tmpNumMatch;
        this.numMatchShort = tmpNumMatchShort;
        this.sms7BitEnabled = tmpSms7BitEnabled;
        this.smsCodingNational = tmpSmsCodingNational;
        this.operatorName = tmpOperatorName;
        this.maxMessageSize = tmpmaxmessagesize;
        this.smsToMmsTextThreshold = tmpsmstommstextthreshold;
        this.eccNoCard = tempEccNoCard;
        this.eccWithCard = tempEccWithCard;
    }

    public VirtualNet() {
    }

    public String getEccWithCard() {
        return this.eccWithCard;
    }

    public String getEccNoCard() {
        return this.eccNoCard;
    }

    public String getNumeric() {
        return this.numeric;
    }

    public static String getApnFilter() {
        VirtualNet virtualNet = getCurrentVirtualNet();
        if (virtualNet != null) {
            return virtualNet.apnFilter;
        }
        return null;
    }

    public static String getApnFilter(int slotId) {
        logd("getApnFilter, slotId=" + slotId);
        VirtualNet virtualNet = getCurrentVirtualNet(slotId);
        logd("getApnFilter, virtualNet=" + virtualNet);
        if (virtualNet == null) {
            return null;
        }
        logd("getApnFilter, apnFilter=" + virtualNet.apnFilter);
        return virtualNet.apnFilter;
    }

    public String getVoiceMailNumber() {
        return this.voicemailNumber;
    }

    public String getVoicemailTag() {
        return this.voicemailTag;
    }

    public int getNumMatch() {
        return this.numMatch;
    }

    public int getNumMatchShort() {
        return this.numMatchShort;
    }

    public int getSms7BitEnabled() {
        return this.sms7BitEnabled;
    }

    public int getSmsCodingNational() {
        return this.smsCodingNational;
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public int getMaxMessageSize() {
        return this.maxMessageSize;
    }

    public int getSmsToMmsTextThreshold() {
        return this.smsToMmsTextThreshold;
    }

    public boolean validNetConfig() {
        logd("validNetConfig isRealNetwork = " + this.isRealNetwork);
        boolean z = true;
        if (("26207".equals(this.numeric) || "23210".equals(this.numeric)) && this.isRealNetwork) {
            return !this.isRealNetwork;
        }
        if (!this.isRealNetwork) {
            return true;
        }
        logd("validNetConfig plmnSameImsiStartCount = " + this.plmnSameImsiStartCount);
        if (this.plmnSameImsiStartCount > 1) {
            z = false;
        }
        return z;
    }

    public static void saveUiccCardsToVirtualNet(UiccCard[] uiccCards) {
        mUiccCards = uiccCards;
    }

    private static boolean isCardPresent(int slotId) {
        boolean z = false;
        if (slotId < 0 || slotId >= MAX_PHONE_COUNT) {
            return false;
        }
        try {
            if (mUiccCards[slotId] != null && IccCardStatus.CardState.CARDSTATE_PRESENT == mUiccCards[slotId].getCardState()) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasIccCard(int slotId) {
        boolean bRet = false;
        if (isMultiSimEnabled) {
            try {
                return isCardPresent(slotId);
            } catch (Exception e) {
                loge("call isCardPresent got Exception");
                return false;
            }
        } else {
            try {
                bRet = isCardPresent(0);
                logd("hasIccCard, bRet=" + bRet + " for single card");
                return bRet;
            } catch (Exception e2) {
                loge("call isCardPresent got Exception");
                return bRet;
            }
        }
    }

    public static String getOperatorKey(Context context) {
        if (!isMultiSimEnabled) {
            return getOperatorKey(context, 0);
        }
        String op_key = getOperatorKey(context, 0);
        if (TextUtils.isEmpty(op_key)) {
            return getOperatorKey(context, 1);
        }
        return op_key;
    }

    public static String getOperatorKey(Context context, int slotId) {
        String op_key;
        if ((slotId != 0 && slotId != 1) || context == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        String specialVnkey = getStringFromSettingsEx(contentResolver, HwTelephony.VirtualNets.VN_KEY_FOR_SPECIALIMSI + slotId, "");
        if (!TextUtils.isEmpty(specialVnkey)) {
            logd("getOperatorKey, specialVnkey = " + specialVnkey + ", slotId: " + slotId);
            return specialVnkey;
        }
        ContentResolver contentResolver2 = context.getContentResolver();
        String op_key2 = getStringFromSettingsEx(contentResolver2, HwTelephony.VirtualNets.VN_KEY + slotId, null);
        if (!TextUtils.isEmpty(op_key2)) {
            logd("getOperatorKey, op_key= " + op_key2 + ", slotId: " + slotId);
            return op_key2;
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
            op_key = SystemProperties.get(RoamingBroker.PreviousOperator + slotId, "");
            logd("getOperatorKey, it is in roaming broker, op_key= " + op_key + ", slotId: " + slotId);
        } else {
            op_key = TelephonyManager.getDefault().getSimOperator(slotId);
        }
        return op_key;
    }

    public static FileReader getSpecialImsiFileReader() {
        File confFile = new File(FILE_FROM_SYSTEM_ETC_DIR);
        File sImsiFileCust = new File(FILE_FROM_CUST_DIR);
        File sImsiFile = new File(Environment.getRootDirectory(), PARAM_SPECIALIMSI_PATH);
        try {
            File cfg = HwCfgFilePolicy.getCfgFile(String.format("/xml/%s", new Object[]{SPECIAL_IMSI_CONFIG_FILE}), 0);
            if (cfg != null) {
                confFile = cfg;
                logd("load specialImsiList-conf.xml from HwCfgFilePolicy folder");
            } else if (sImsiFileCust.exists()) {
                confFile = sImsiFileCust;
                logd("load specialImsiList-conf.xml from cust folder");
            } else {
                confFile = sImsiFile;
                logd("load specialImsiList-conf.xml from etc folder");
            }
        } catch (NoClassDefFoundError e) {
            loge("NoClassDefFoundError : HwCfgFilePolicy ");
        }
        try {
            return new FileReader(confFile);
        } catch (FileNotFoundException e2) {
            loge("Invalid file");
            return null;
        }
    }

    private static void loadSpecialImsiList() {
        if (!specialImsiLoaded) {
            logd("loadSpecialImsiList begin!");
            mSpecialImsiList = new ArrayList<>();
            FileReader sImsiReader = getSpecialImsiFileReader();
            if (sImsiReader == null) {
                loge("loadSpecialImsiList failed!");
                return;
            }
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(sImsiReader);
                XmlUtils.beginDocument(parser, "specialImsiList");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"specialImsiList".equals(parser.getName())) {
                        break;
                    }
                    mSpecialImsiList.add(new String[]{parser.getAttributeValue(null, "imsiStart"), parser.getAttributeValue(null, "imsiEnd"), parser.getAttributeValue(null, "vnKey")});
                }
                if (sImsiReader != null) {
                    try {
                        sImsiReader.close();
                    } catch (IOException e) {
                        loge("IOException happen.close failed.");
                    }
                }
            } catch (XmlPullParserException e2) {
                logd("loadSpecialImsiList get XmlPullParserException in specialImsiList parser");
                if (sImsiReader != null) {
                    sImsiReader.close();
                }
            } catch (IOException e3) {
                logd("IOException get in specialImsiList parser");
                if (sImsiReader != null) {
                    sImsiReader.close();
                }
            } catch (Throwable th) {
                if (sImsiReader != null) {
                    try {
                        sImsiReader.close();
                    } catch (IOException e4) {
                        loge("IOException happen.close failed.");
                    }
                }
                throw th;
            }
            specialImsiLoaded = true;
        }
    }

    public static String getVnKeyFromSpecialIMSIList(String imsi) {
        if (TextUtils.isEmpty(imsi) || mSpecialImsiList.size() <= 0) {
            loge("imsi or mSpecialImsiList is empty, return null");
            return null;
        }
        Iterator<String[]> iter = mSpecialImsiList.iterator();
        while (iter.hasNext()) {
            String[] data = iter.next();
            if (3 == data.length) {
                int compareImsiStart = imsi.compareTo(data[0]);
                int compareImsiEnd = imsi.compareTo(data[1]);
                if (compareImsiStart >= 0 && compareImsiEnd <= 0) {
                    logd("return the special VnKey = " + data[2]);
                    return data[2];
                }
            }
        }
        return null;
    }
}
