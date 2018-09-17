package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.HwTelephony.VirtualNets;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
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

        public SpecialFile(String filePath, String fileId) {
            this.filePath = filePath;
            this.fileId = fileId;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof SpecialFile)) {
                return false;
            }
            SpecialFile other = (SpecialFile) obj;
            if (!(this.filePath == null || this.fileId == null || !this.filePath.equals(other.filePath))) {
                z = this.fileId.equals(other.fileId);
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
        boolean z = true;
        if (slotId == 1) {
            if (mVirtualNet1 == null) {
                z = false;
            }
            return z;
        } else if (slotId != 0) {
            return false;
        } else {
            if (mVirtualNet == null) {
                z = false;
            }
            return z;
        }
    }

    private static void logd(String text) {
        Log.d(LOG_TAG, "[VirtualNet] " + text);
    }

    private static void loge(String text, Exception e) {
        Log.e(LOG_TAG, "[VirtualNet] " + text + e);
    }

    private static void loge(String text) {
        Log.e(LOG_TAG, "[VirtualNet] " + text);
    }

    public static void loadSpecialFiles(String numeric, SIMRecords simRecords) {
        if (numeric == null) {
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
        String[] selectionArgs = new String[]{numeric, Integer.toString(4)};
        String[] projection = new String[]{"numeric", VirtualNets.VIRTUAL_NET_RULE, VirtualNets.MATCH_PATH, VirtualNets.MATCH_FILE};
        Cursor cursor = PhoneFactory.getDefaultPhone().getContext().getContentResolver().query(VirtualNets.CONTENT_URI, projection, "numeric = ? AND virtual_net_rule = ?", selectionArgs, null);
        if (cursor == null) {
            loge("query virtual net db got a null cursor");
            return;
        }
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String matchPath = cursor.getString(cursor.getColumnIndex(VirtualNets.MATCH_PATH));
                String matchFile = cursor.getString(cursor.getColumnIndex(VirtualNets.MATCH_FILE));
                SpecialFile specialFile = new SpecialFile(matchPath, matchFile);
                if (slotId == 1) {
                    if (!specialFilesMap1.containsKey(specialFile)) {
                        logd("load specialFilesMap1 matchPath=" + matchPath + ";" + "matchFile=" + matchFile);
                        specialFilesMap1.put(specialFile, null);
                        simRecords.loadFile(matchPath, matchFile);
                    }
                } else if (!specialFilesMap.containsKey(specialFile)) {
                    logd("load specialFile matchPath=" + matchPath + ";" + "matchFile=" + matchFile);
                    specialFilesMap.put(specialFile, null);
                    simRecords.loadFile(matchPath, matchFile);
                }
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loge("loadVirtualNet got Exception", e);
        } finally {
            cursor.close();
        }
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
                System.putString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), VirtualNets.VN_KEY + slotId, "");
                System.putString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), VirtualNets.VN_KEY_FOR_SPECIALIMSI + slotId, "");
                return;
            }
            return;
        }
        mVirtualNet = null;
        if (PhoneFactory.getDefaultPhone() != null && PhoneFactory.getDefaultPhone().getContext() != null) {
            System.putString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "vn_key0", "");
            System.putString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "vn_key_for_specialimsi0", "");
        }
    }

    private static String getSimRecordsImsi(SIMRecords simRecords) {
        String imsi = simRecords.getIMSI();
        int slotId = simRecords.getSlotId();
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
                return imsi;
            }
            imsi = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerImsi(Integer.valueOf(slotId));
            Log.d(LOG_TAG, "VirtualNet RoamingBrokerActivated, set homenetwork imsi");
            return imsi;
        } else if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            return imsi;
        } else {
            imsi = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerImsi();
            Log.d(LOG_TAG, "VirtualNet RoamingBrokerActivated, set homenetwork imsi");
            return imsi;
        }
    }

    private static void clearVirtualNet(int slotId) {
        if (isMultiSimEnabled) {
            logd("start loadVirtualNet: slotId= " + slotId);
            if (slotId == 1) {
                mVirtualNet1 = null;
                return;
            } else {
                mVirtualNet = null;
                return;
            }
        }
        mVirtualNet = null;
    }

    public static void loadVirtualNet(String numeric, SIMRecords simRecords) {
        logd("start loadVirtualNet: numeric= " + numeric);
        int slotId = simRecords.getSlotId();
        if (HwTelephonyManagerInner.getDefault().isCTSimCard(slotId) ? CTRoamingNumeric.equals(numeric) : false) {
            logd("CT sim card 20404 is not virtualnet");
            removeVirtualNet(slotId);
            clearCurVirtualNetsDb(slotId);
        } else if (numeric == null) {
            loge("number is null,loadVirtualNet will return");
        } else {
            clearVirtualNet(slotId);
            logd("thread = " + Thread.currentThread().getName());
            String imsi = getSimRecordsImsi(simRecords);
            byte[] gid1 = simRecords.getGID1();
            String spn = simRecords.getServiceProviderName();
            logd("start loadVirtualNet: numeric=" + numeric + "; gid1=" + IccUtils.bytesToHexString(gid1) + "; spn=" + spn);
            String[] selectionArgs = new String[]{numeric};
            Phone phone = PhoneFactory.getDefaultPhone();
            String iccid = simRecords.getIccId();
            logd("start loadVirtualNet: iccid = " + SubscriptionInfo.givePrintableIccid(iccid));
            String[] projection = new String[]{"numeric", VirtualNets.VIRTUAL_NET_RULE, VirtualNets.IMSI_START, VirtualNets.GID1, VirtualNets.GID_MASK, VirtualNets.SPN, VirtualNets.MATCH_PATH, VirtualNets.MATCH_FILE, VirtualNets.MATCH_VALUE, VirtualNets.MATCH_MASK, VirtualNets.APN_FILTER, VirtualNets.VOICEMAIL_NUMBER, VirtualNets.VOICEMAIL_TAG, "num_match", "num_match_short", "sms_7bit_enabled", "sms_coding_national", VirtualNets.ONS_NAME, "max_message_size", "sms_to_mms_textthreshold", VirtualNets.ECC_WITH_CARD, VirtualNets.ECC_NO_CARD, VirtualNets.VN_KEY, VirtualNets.IMSI_SP_LIST, VirtualNets.IMSI_SP_START, VirtualNets.IMSI_SP_END, VirtualNets.ICCID_RANGE_VALUE, VirtualNets.ICCID_START_POSITION, VirtualNets.ICCID_END_POSITION};
            saveLastVirtualNetsDb(slotId);
            clearCurVirtualNetsDb(slotId);
            Cursor cursor = phone.getContext().getContentResolver().query(VirtualNets.CONTENT_URI, projection, "numeric = ?", selectionArgs, null);
            if (cursor == null) {
                loge("query virtual net db got a null cursor");
                return;
            }
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int tmpVirtualNetRule = cursor.getInt(cursor.getColumnIndex(VirtualNets.VIRTUAL_NET_RULE));
                    switch (tmpVirtualNetRule) {
                        case 1:
                            String tmpImsiStart = cursor.getString(cursor.getColumnIndex(VirtualNets.IMSI_START));
                            if (isImsiVirtualNet(imsi, tmpImsiStart)) {
                                logd("find imsi virtual net imsiStart=" + tmpImsiStart);
                                createVirtualNet(cursor, slotId);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId, 1);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.IMSI_START + slotId, tmpImsiStart);
                                break;
                            }
                            break;
                        case 2:
                            String tmpGid1Value = cursor.getString(cursor.getColumnIndex(VirtualNets.GID1));
                            String tmpGidMask = cursor.getString(cursor.getColumnIndex(VirtualNets.GID_MASK));
                            if (isGid1VirtualNet(gid1, tmpGid1Value, tmpGidMask)) {
                                logd("find gid1 virtual net spn=" + tmpGid1Value);
                                createVirtualNet(cursor, slotId);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId, 2);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.GID1 + slotId, tmpGid1Value);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.GID_MASK + slotId, tmpGidMask);
                                break;
                            }
                            break;
                        case 3:
                            String tmpSpn = cursor.getString(cursor.getColumnIndex(VirtualNets.SPN));
                            if (isSpnVirtualNet(spn, tmpSpn)) {
                                logd("find spn virtual net spn=" + tmpSpn);
                                createVirtualNet(cursor, slotId);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId, 3);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.SPN + slotId, tmpSpn);
                                break;
                            }
                            break;
                        case 4:
                            String tmpMatchPath = cursor.getString(cursor.getColumnIndex(VirtualNets.MATCH_PATH));
                            String tmpMatchFile = cursor.getString(cursor.getColumnIndex(VirtualNets.MATCH_FILE));
                            String tmpMatchValue = cursor.getString(cursor.getColumnIndex(VirtualNets.MATCH_VALUE));
                            String tmpMatchMask = cursor.getString(cursor.getColumnIndex(VirtualNets.MATCH_MASK));
                            if (isSpecialFileVirtualNet(tmpMatchPath, tmpMatchFile, tmpMatchValue, tmpMatchMask, slotId)) {
                                logd("find special file virtual net matchValue =" + tmpMatchValue);
                                createVirtualNet(cursor, slotId);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId, 4);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.MATCH_PATH + slotId, tmpMatchPath);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.MATCH_FILE + slotId, tmpMatchFile);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.MATCH_VALUE + slotId, tmpMatchValue);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.MATCH_MASK + slotId, tmpMatchMask);
                                break;
                            }
                            break;
                        case 5:
                            int tmpImsiSPStart = cursor.getInt(cursor.getColumnIndex(VirtualNets.IMSI_SP_START));
                            int tmpImsiSPEnd = cursor.getInt(cursor.getColumnIndex(VirtualNets.IMSI_SP_END));
                            String tmpImsiSPList = cursor.getString(cursor.getColumnIndex(VirtualNets.IMSI_SP_LIST));
                            if (isSerialNumberVirtualNet(imsi, tmpImsiSPList, tmpImsiSPStart, tmpImsiSPEnd)) {
                                logd("find imsi_sp Virtual Net, imsiSPList=" + tmpImsiSPList + ", imsiSPStart=" + tmpImsiSPStart + ", imsiSPEnd=" + tmpImsiSPEnd);
                                createVirtualNet(cursor, slotId);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId, 5);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_LIST + slotId, tmpImsiSPList);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_START + slotId, tmpImsiSPStart);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_END + slotId, tmpImsiSPEnd);
                                break;
                            }
                            break;
                        case 6:
                            int tmpIccidStartPos = cursor.getInt(cursor.getColumnIndex(VirtualNets.ICCID_START_POSITION));
                            int tmpIccidEndPos = cursor.getInt(cursor.getColumnIndex(VirtualNets.ICCID_END_POSITION));
                            String tmpIccidRangeVal = cursor.getString(cursor.getColumnIndex(VirtualNets.ICCID_RANGE_VALUE));
                            if (isSerialNumberVirtualNet(iccid, tmpIccidRangeVal, tmpIccidStartPos, tmpIccidEndPos)) {
                                logd("find iccid Vitrual Net, tmpIccidRangeVal=" + tmpIccidRangeVal + ", tmpIccidStartPos=" + tmpIccidStartPos + ", tmpIccidEndPos=" + tmpIccidEndPos);
                                createVirtualNet(cursor, slotId);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId, 6);
                                System.putString(phone.getContext().getContentResolver(), VirtualNets.ICCID_RANGE_VALUE + slotId, tmpIccidRangeVal);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.ICCID_START_POSITION + slotId, tmpIccidStartPos);
                                System.putInt(phone.getContext().getContentResolver(), VirtualNets.ICCID_END_POSITION + slotId, tmpIccidEndPos);
                                break;
                            }
                            break;
                        default:
                            logd("unhandled case: " + tmpVirtualNetRule);
                            break;
                    }
                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
                loge("loadVirtualNet got Exception", e);
            } finally {
                cursor.close();
            }
            deletePreferApnIfNeed(slotId);
            loadSpecialImsiList();
            String vn_key = getVnKeyFromSpecialIMSIList(imsi);
            if (!TextUtils.isEmpty(vn_key)) {
                System.putString(phone.getContext().getContentResolver(), VirtualNets.VN_KEY_FOR_SPECIALIMSI + slotId, vn_key);
            }
        }
    }

    private static void deletePreferApnIfNeed(int slotId) {
        if (isVirtualNet(slotId) && (isVirtualNetEqual(slotId) ^ 1) != 0) {
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
                    if (apn != null && apnContext.getState() == State.CONNECTED) {
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
        String value = System.getString(resolver, key);
        return value == null ? defaultValue : value;
    }

    private static int getIntFromSettingsEx(ContentResolver resolver, String key) {
        return System.getInt(resolver, key, -99);
    }

    private static void saveLastVirtualNetsDb(int slotId) {
        Phone phone = PhoneFactory.getDefaultPhone();
        int tmpVirtualNetRule = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId);
        String tmpImsiStart = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.IMSI_START + slotId, "");
        String tmpGid1Value = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.GID1 + slotId, "");
        String tmpGidMask = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.GID_MASK + slotId, "");
        String tmpSpn = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SPN + slotId, "");
        String tmpMatchPath = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.MATCH_PATH + slotId, "");
        String tmpMatchFile = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.MATCH_FILE + slotId, "");
        String tmpMatchValue = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.MATCH_VALUE + slotId, "");
        String tmpMatchMask = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.MATCH_MASK + slotId, "");
        String tmpImsiSPList = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_LIST + slotId, "");
        int tmpImsiSPStart = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_START + slotId);
        int tmpImsiSPEnd = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_END + slotId);
        String tmpIccidRangeVal = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.ICCID_RANGE_VALUE + slotId, "");
        int tmpIccidStartPos = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.ICCID_START_POSITION + slotId);
        int tmpIccidEndPos = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.ICCID_END_POSITION + slotId);
        String tmpNumeric = getStringFromSettingsEx(phone.getContext().getContentResolver(), "numeric" + slotId, "");
        String tmpApnFilter = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.APN_FILTER + slotId, "");
        String tmpVoicemalNumber = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_NUMBER + slotId, "");
        String tmpVoicemalTag = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_TAG + slotId, "");
        int tmpNumMatch = getIntFromSettingsEx(phone.getContext().getContentResolver(), "num_match" + slotId);
        int tmpNumMatchShort = getIntFromSettingsEx(phone.getContext().getContentResolver(), "num_match_short" + slotId);
        int tmpSms7BitEnabled = getIntFromSettingsEx(phone.getContext().getContentResolver(), "sms_7bit_enabled" + slotId);
        int tmpSmsCodingNational = getIntFromSettingsEx(phone.getContext().getContentResolver(), "sms_coding_national" + slotId);
        String tmpOperatorName = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.ONS_NAME + slotId, "");
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.SAVED_VIRTUAL_NET_RULE + slotId, tmpVirtualNetRule);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_IMSI_START + slotId, tmpImsiStart);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_GID1 + slotId, tmpGid1Value);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_GID_MASK + slotId, tmpGidMask);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_SPN + slotId, tmpSpn);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_MATCH_PATH + slotId, tmpMatchPath);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_MATCH_FILE + slotId, tmpMatchFile);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_MATCH_VALUE + slotId, tmpMatchValue);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_MATCH_MASK + slotId, tmpMatchMask);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_IMSI_SP_LIST + slotId, tmpImsiSPList);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.SAVED_IMSI_SP_START + slotId, tmpImsiSPStart);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.SAVED_IMSI_SP_END + slotId, tmpImsiSPEnd);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_ICCID_RANGE_VALUE + slotId, tmpIccidRangeVal);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.SAVED_ICCID_START_POSITION + slotId, tmpIccidStartPos);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.SAVED_ICCID_END_POSITION + slotId, tmpIccidEndPos);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_NUMERIC + slotId, tmpNumeric);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_APN_FILTER + slotId, tmpApnFilter);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_VOICEMAIL_NUMBER + slotId, tmpVoicemalNumber);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_VOICEMAIL_TAG + slotId, tmpVoicemalTag);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.SAVED_NUM_MATCH + slotId, tmpNumMatch);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.SAVED_NUM_MATCH_SHORT + slotId, tmpNumMatchShort);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.SAVED_SMS_7BIT_ENABLED + slotId, tmpSms7BitEnabled);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.SAVED_SMS_CODING_NATIONAL + slotId, tmpSmsCodingNational);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SAVED_ONS_NAME + slotId, tmpOperatorName);
    }

    private static void clearCurVirtualNetsDb(int slotId) {
        Phone phone = PhoneFactory.getDefaultPhone();
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId, -99);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.IMSI_START + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.GID1 + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.GID_MASK + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.SPN + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.MATCH_PATH + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.MATCH_FILE + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.MATCH_VALUE + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.MATCH_MASK + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_LIST + slotId, "");
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_START + slotId, -99);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_END + slotId, -99);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.ICCID_RANGE_VALUE + slotId, "");
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.ICCID_START_POSITION + slotId, -99);
        System.putInt(phone.getContext().getContentResolver(), VirtualNets.ICCID_END_POSITION + slotId, -99);
        System.putString(phone.getContext().getContentResolver(), "numeric" + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.APN_FILTER + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_NUMBER + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_TAG + slotId, "");
        System.putInt(phone.getContext().getContentResolver(), "num_match" + slotId, -99);
        System.putInt(phone.getContext().getContentResolver(), "num_match_short" + slotId, -99);
        System.putInt(phone.getContext().getContentResolver(), "sms_7bit_enabled" + slotId, -99);
        System.putInt(phone.getContext().getContentResolver(), "sms_coding_national" + slotId, -99);
        System.putString(phone.getContext().getContentResolver(), VirtualNets.ONS_NAME + slotId, "");
        System.putString(phone.getContext().getContentResolver(), VirtualNets.VN_KEY + slotId, "");
    }

    public static boolean isVirtualNetEqual(int slotId) {
        Phone phone = PhoneFactory.getDefaultPhone();
        if (!isVirtualNet(slotId)) {
            return false;
        }
        int curVirtualNetRule = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId);
        String curImsiStart = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.IMSI_START + slotId, "");
        String curGid1Value = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.GID1 + slotId, "");
        String curGidMask = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.GID_MASK + slotId, "");
        String curSpn = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SPN + slotId, "");
        String curMatchPath = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.MATCH_PATH + slotId, "");
        String curMatchFile = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.MATCH_FILE + slotId, "");
        String curMatchValue = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.MATCH_VALUE + slotId, "");
        String curMatchMask = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.MATCH_MASK + slotId, "");
        String curNumeric = getStringFromSettingsEx(phone.getContext().getContentResolver(), "numeric" + slotId, "");
        String curApnFilter = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.APN_FILTER + slotId, "");
        String curVoicemalNumber = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_NUMBER + slotId, "");
        String curVoicemalTag = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_TAG + slotId, "");
        int curNumMatch = getIntFromSettingsEx(phone.getContext().getContentResolver(), "num_match" + slotId);
        int curNumMatchShort = getIntFromSettingsEx(phone.getContext().getContentResolver(), "num_match_short" + slotId);
        int curSms7BitEnabled = getIntFromSettingsEx(phone.getContext().getContentResolver(), "sms_7bit_enabled" + slotId);
        int curSmsCodingNational = getIntFromSettingsEx(phone.getContext().getContentResolver(), "sms_coding_national" + slotId);
        String curOperatorName = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.ONS_NAME + slotId, "");
        String curImsiSPList = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_LIST + slotId, "");
        int curImsiSPStart = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_START + slotId);
        int curImsiSPEnd = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.IMSI_SP_END + slotId);
        String curIccidRangeVal = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.ICCID_RANGE_VALUE + slotId, "");
        int curIccidStartPos = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.ICCID_START_POSITION + slotId);
        int curIccidEndPos = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.ICCID_END_POSITION + slotId);
        int lastVirtualNetRule = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_VIRTUAL_NET_RULE + slotId);
        String lastImsiStart = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_IMSI_START + slotId, "");
        String lastGid1Value = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_GID1 + slotId, "");
        String lastGidMask = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_GID_MASK + slotId, "");
        String lastSpn = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_SPN + slotId, "");
        String lastMatchPath = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_MATCH_PATH + slotId, "");
        String lastMatchFile = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_MATCH_FILE + slotId, "");
        String lastMatchValue = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_MATCH_VALUE + slotId, "");
        String lastMatchMask = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_MATCH_MASK + slotId, "");
        String lastNumeric = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_NUMERIC + slotId, "");
        String lastApnFilter = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_APN_FILTER + slotId, "");
        String lastVoicemalNumber = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_VOICEMAIL_NUMBER + slotId, "");
        String lastVoicemalTag = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_VOICEMAIL_TAG + slotId, "");
        int lastNumMatch = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_NUM_MATCH + slotId);
        int lastNumMatchShort = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_NUM_MATCH_SHORT + slotId);
        int lastSms7BitEnabled = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_SMS_7BIT_ENABLED + slotId);
        int lastSmsCodingNational = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_SMS_CODING_NATIONAL + slotId);
        String lastOperatorName = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_ONS_NAME + slotId, "");
        String lastImsiSPList = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_IMSI_SP_LIST + slotId, "");
        int lastImsiSPStart = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_IMSI_SP_START + slotId);
        int lastImsiSPEnd = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_IMSI_SP_END + slotId);
        String lastIccidRangeVal = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_ICCID_RANGE_VALUE + slotId, "");
        int lastIccidStartPos = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_ICCID_START_POSITION + slotId);
        int lastIccidEndPos = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SAVED_ICCID_END_POSITION + slotId);
        if (curVirtualNetRule == 0) {
            logd("RULE_NONE different virtual net");
            return false;
        } else if (curVirtualNetRule != lastVirtualNetRule) {
            logd("curVirtualNetRule != lastVirtualNetRule different virtual net");
            return false;
        } else {
            boolean anyValueNotMatch;
            switch (curVirtualNetRule) {
                case 1:
                    if (!curImsiStart.equals(lastImsiStart)) {
                        logd("RULE_IMSI different virtual net");
                        return false;
                    }
                    break;
                case 2:
                    if (!(curGid1Value.equals(lastGid1Value) && (curGidMask.equals(lastGidMask) ^ 1) == 0)) {
                        logd("RULE_GID1 different virtual net");
                        return false;
                    }
                case 3:
                    if (!curSpn.equals(lastSpn)) {
                        logd("RULE_SPN different virtual net");
                        return false;
                    }
                    break;
                case 4:
                    int fileNotMatch = (curMatchPath.equals(lastMatchPath) && (curMatchFile.equals(lastMatchFile) ^ 1) == 0 && (curMatchValue.equals(lastMatchValue) ^ 1) == 0) ? curMatchMask.equals(lastMatchMask) ^ 1 : 1;
                    if (fileNotMatch != 0) {
                        logd("RULE_MATCH_FILE different virtual net");
                        return false;
                    }
                    break;
                case 5:
                    boolean imsiSPNotMatched = (curImsiSPList.equals(lastImsiSPList) && curImsiSPStart == lastImsiSPStart && curImsiSPEnd == lastImsiSPEnd) ? false : true;
                    if (imsiSPNotMatched) {
                        logd("RULE_IMSI_SP different virtual net.");
                        return false;
                    }
                    break;
                case 6:
                    boolean iccidRuleNotMatched = (curIccidRangeVal.equals(lastIccidRangeVal) && curIccidStartPos == lastIccidStartPos && curIccidEndPos == lastIccidEndPos) ? false : true;
                    if (iccidRuleNotMatched) {
                        logd("RULE_ICCID different virtual net.");
                        return false;
                    }
                    break;
                default:
                    logd("RULE unkown different virtual net");
                    return false;
            }
            if (curNumeric.equals(lastNumeric) && (curApnFilter.equals(lastApnFilter) ^ 1) == 0 && (curVoicemalNumber.equals(lastVoicemalNumber) ^ 1) == 0 && (curVoicemalTag.equals(lastVoicemalTag) ^ 1) == 0 && curNumMatch == lastNumMatch && curNumMatchShort == lastNumMatchShort && curSms7BitEnabled == lastSms7BitEnabled && curSmsCodingNational == lastSmsCodingNational) {
                anyValueNotMatch = curOperatorName.equals(lastOperatorName) ^ 1;
            } else {
                anyValueNotMatch = true;
            }
            logd("anyValueNotMatch = " + anyValueNotMatch);
            return anyValueNotMatch ^ 1;
        }
    }

    protected static boolean isSpecialFileVirtualNet(String matchPath, String matchFile, String matchValue, String matchMask, int slotId) {
        byte[] bytes;
        SpecialFile specialFile = new SpecialFile(matchPath, matchFile);
        if (!isMultiSimEnabled) {
            bytes = (byte[]) specialFilesMap.get(specialFile);
        } else if (slotId == 1) {
            logd("isSpecialFileVirtualNet: slotId == SUB2");
            bytes = (byte[]) specialFilesMap1.get(specialFile);
        } else {
            bytes = (byte[]) specialFilesMap.get(specialFile);
        }
        if (bytes == null) {
            return false;
        }
        return matchByteWithMask(bytes, matchValue, matchMask);
    }

    protected static boolean isSpnVirtualNet(String spn1, String spn2) {
        logd("isSpnVirtualNet spn1 = " + spn1 + "; spn2 = " + spn2);
        if (TextUtils.isEmpty(spn1) && spn2 != null && spn2.equals(SPN_EMPTY)) {
            return true;
        }
        if (!TextUtils.isEmpty(spn1) && spn1.toUpperCase().startsWith(SPN_START.toUpperCase()) && !TextUtils.isEmpty(spn2) && spn2.equals(SPN_EMPTY)) {
            return true;
        }
        boolean equals = (spn1 == null || spn2 == null) ? false : spn1.equals(spn2);
        return equals;
    }

    protected static boolean isGid1VirtualNet(byte[] gid1, String gid1Value, String gidMask) {
        logd("isGid1VirtualNet gid1 = " + IccUtils.bytesToHexString(gid1) + "; gid1Value = " + gid1Value + "; gidMask = " + gidMask);
        return matchByteWithMask(gid1, gid1Value, gidMask);
    }

    private static boolean matchByteWithMask(byte[] data, String value, String mask) {
        boolean inValidParams;
        if (data == null || value == null || mask == null || data.length * 2 < value.length() - 2 || value.length() < 2 || (value.substring(0, 2).equalsIgnoreCase("0x") ^ 1) != 0 || mask.length() < 2) {
            inValidParams = true;
        } else {
            inValidParams = mask.substring(0, 2).equalsIgnoreCase("0x") ^ 1;
        }
        if (inValidParams) {
            return false;
        }
        if (isEmptySimFile(data)) {
            if ((value.equalsIgnoreCase("0xFF") ^ 1) != 0) {
                logd("matchByteWithMask data is null");
                return false;
            }
        }
        String valueString = value.substring(2);
        String maskString = mask.substring(2);
        String gid1String = IccUtils.bytesToHexString(data);
        boolean isValidOddMask = (maskString == null || gid1String == null || valueString == null || maskString.length() % 2 != 1) ? false : true;
        int i;
        if (isValidOddMask) {
            int maskStringLength = maskString.length();
            int gid1StringLength = gid1String.length();
            int valueStringLength = valueString.length();
            logd("Gid1 length is odd ,Gid1 length:" + maskString.length());
            if (gid1StringLength < maskStringLength || maskStringLength != valueStringLength) {
                logd("Gid1 length is not match");
                return false;
            }
            i = 0;
            while (i < maskStringLength) {
                if (maskString.charAt(i) == 'F' && gid1String.charAt(i) == valueString.charAt(i)) {
                    i++;
                } else {
                    logd("Gid1 mask did not match");
                    return false;
                }
            }
            for (i = maskStringLength; i < gid1StringLength; i++) {
                if (gid1String.charAt(i) != 'f') {
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
        for (i = 0; i < maskBytes.length; i++) {
            if ((data[i] & maskBytes[i]) != valueBytes[i]) {
                match = false;
            }
        }
        return match;
    }

    private static boolean isEmptySimFile(byte[] gid1) {
        boolean isEmptyFile = true;
        for (byte gid1Byte : gid1) {
            if (gid1Byte != (byte) -1) {
                isEmptyFile = false;
            }
        }
        return isEmptyFile;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static boolean isImsiVirtualNet(String imsi, String tmpImsiStart) {
        if (imsi == null || tmpImsiStart == null || !imsi.startsWith(tmpImsiStart)) {
            return false;
        }
        return true;
    }

    protected static boolean isSerialNumberVirtualNet(String serialNumber, String rangeVal, int StartPos, int endPos) {
        if (TextUtils.isEmpty(serialNumber) || TextUtils.isEmpty(rangeVal) || StartPos <= 0 || endPos <= 0) {
            return false;
        }
        if (endPos < StartPos || endPos > serialNumber.length() || StartPos > serialNumber.length()) {
            loge("isImsiSPVirtualNet: serialNumber length=" + serialNumber.length() + ",less than startPos=" + StartPos + ", or  endPos=" + endPos + ",Maybe config Error.");
            return false;
        }
        String serialNumFromSim = serialNumber.substring(StartPos - 1, endPos);
        for (String tmpSerialNum : rangeVal.split(",")) {
            if (!TextUtils.isEmpty(tmpSerialNum) && tmpSerialNum.equals(serialNumFromSim)) {
                return true;
            }
        }
        return false;
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
        if (cursor == null) {
            return null;
        }
        String tmpNumeric = cursor.getString(cursor.getColumnIndex("numeric"));
        String tmpApnFilter = cursor.getString(cursor.getColumnIndex(VirtualNets.APN_FILTER));
        String tmpVoicemalNumber = cursor.getString(cursor.getColumnIndex(VirtualNets.VOICEMAIL_NUMBER));
        String tmpVoicemalTag = cursor.getString(cursor.getColumnIndex(VirtualNets.VOICEMAIL_TAG));
        int tmpNumMatch = cursor.getInt(cursor.getColumnIndex("num_match"));
        int tmpNumMatchShort = cursor.getInt(cursor.getColumnIndex("num_match_short"));
        int tmpSms7BitEnabled = cursor.getInt(cursor.getColumnIndex("sms_7bit_enabled"));
        int tmpSmsCodingNational = cursor.getInt(cursor.getColumnIndex("sms_coding_national"));
        String tmpOperatorName = cursor.getString(cursor.getColumnIndex(VirtualNets.ONS_NAME));
        int tmpmaxmessagesize = cursor.getInt(cursor.getColumnIndex("max_message_size"));
        int tmpsmstommstextthreshold = cursor.getInt(cursor.getColumnIndex("sms_to_mms_textthreshold"));
        String tempEccWithCard = cursor.getString(cursor.getColumnIndex(VirtualNets.ECC_WITH_CARD));
        String tempEccNoCard = cursor.getString(cursor.getColumnIndex(VirtualNets.ECC_NO_CARD));
        String tmpVnKey = cursor.getString(cursor.getColumnIndex(VirtualNets.VN_KEY));
        VirtualNet virtualNet = null;
        if (tmpNumeric != null && tmpNumeric.trim().length() > 0) {
            virtualNet = new VirtualNet(tmpNumeric, tmpApnFilter, tmpVoicemalNumber, tmpVoicemalTag, tmpNumMatch, tmpNumMatchShort, tmpSms7BitEnabled, tmpSmsCodingNational, tmpOperatorName, tmpmaxmessagesize, tmpsmstommstextthreshold, tempEccWithCard, tempEccNoCard);
            Phone phone = PhoneFactory.getDefaultPhone();
            System.putString(phone.getContext().getContentResolver(), "numeric" + slotId, tmpNumeric);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.APN_FILTER + slotId, tmpApnFilter);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_NUMBER + slotId, tmpVoicemalNumber);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_TAG + slotId, tmpVoicemalTag);
            System.putInt(phone.getContext().getContentResolver(), "num_match" + slotId, tmpNumMatch);
            System.putInt(phone.getContext().getContentResolver(), "num_match_short" + slotId, tmpNumMatchShort);
            System.putInt(phone.getContext().getContentResolver(), "sms_7bit_enabled" + slotId, tmpSms7BitEnabled);
            System.putInt(phone.getContext().getContentResolver(), "sms_coding_national" + slotId, tmpSmsCodingNational);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.ONS_NAME + slotId, tmpOperatorName);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.VN_KEY + slotId, tmpVnKey);
        }
        int tmpVirtualNetRule = cursor.getInt(cursor.getColumnIndex(VirtualNets.VIRTUAL_NET_RULE));
        String tmpImsiStart = cursor.getString(cursor.getColumnIndex(VirtualNets.IMSI_START));
        if (tmpVirtualNetRule == 1 && tmpNumeric != null && tmpNumeric.equals(tmpImsiStart) && virtualNet != null) {
            logd("getVirtualNet find a realNetwork tmpNumeric = " + tmpNumeric);
            virtualNet.isRealNetwork = true;
            if (slotId == 1) {
                if (mVirtualNet1 == null) {
                    virtualNet.plmnSameImsiStartCount = 1;
                } else {
                    virtualNet.plmnSameImsiStartCount = mVirtualNet1.plmnSameImsiStartCount + 1;
                }
            } else if (mVirtualNet == null) {
                virtualNet.plmnSameImsiStartCount = 1;
            } else {
                virtualNet.plmnSameImsiStartCount = mVirtualNet.plmnSameImsiStartCount + 1;
            }
        }
        return virtualNet;
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
        boolean z = true;
        logd("validNetConfig isRealNetwork = " + this.isRealNetwork);
        if (("26207".equals(this.numeric) || "23210".equals(this.numeric)) && this.isRealNetwork) {
            return this.isRealNetwork ^ 1;
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
            if (CardState.CARDSTATE_PRESENT == mUiccCards[slotId].getCardState()) {
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
                e.printStackTrace();
                loge("call isCardPresent got Exception", e);
                return bRet;
            }
        }
        try {
            bRet = isCardPresent(0);
            logd("hasIccCard, bRet=" + bRet + " for single card");
            return bRet;
        } catch (Exception e2) {
            e2.printStackTrace();
            loge("call isCardPresent got Exception", e2);
            return bRet;
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
        if ((slotId != 0 && slotId != 1) || context == null) {
            return null;
        }
        String specialVnkey = getStringFromSettingsEx(context.getContentResolver(), VirtualNets.VN_KEY_FOR_SPECIALIMSI + slotId, "");
        if (TextUtils.isEmpty(specialVnkey)) {
            String op_key = getStringFromSettingsEx(context.getContentResolver(), VirtualNets.VN_KEY + slotId, null);
            if (TextUtils.isEmpty(op_key)) {
                if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
                    op_key = SystemProperties.get(RoamingBroker.PreviousOperator + slotId, "");
                    logd("getOperatorKey, it is in roaming broker, op_key= " + op_key + ", slotId: " + slotId);
                } else {
                    op_key = TelephonyManager.getDefault().getSimOperator(slotId);
                }
                return op_key;
            }
            logd("getOperatorKey, op_key= " + op_key + ", slotId: " + slotId);
            return op_key;
        }
        logd("getOperatorKey, specialVnkey = " + specialVnkey + ", slotId: " + slotId);
        return specialVnkey;
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
            loge("Can't open " + Environment.getRootDirectory() + "/" + PARAM_SPECIALIMSI_PATH);
            return null;
        }
    }

    private static void loadSpecialImsiList() {
        if (!specialImsiLoaded) {
            logd("loadSpecialImsiList begin!");
            mSpecialImsiList = new ArrayList();
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
                    if ("specialImsiList".equals(parser.getName())) {
                        mSpecialImsiList.add(new String[]{parser.getAttributeValue(null, "imsiStart"), parser.getAttributeValue(null, "imsiEnd"), parser.getAttributeValue(null, "vnKey")});
                    } else if (sImsiReader != null) {
                        try {
                            sImsiReader.close();
                        } catch (IOException e) {
                            loge("IOException happen.close failed.");
                        }
                    }
                }
            } catch (XmlPullParserException e2) {
                logd("Exception in specialImsiList parser " + e2);
                if (sImsiReader != null) {
                    try {
                        sImsiReader.close();
                    } catch (IOException e3) {
                        loge("IOException happen.close failed.");
                    }
                }
            } catch (IOException e4) {
                logd("Exception in specialImsiList parser " + e4);
                if (sImsiReader != null) {
                    try {
                        sImsiReader.close();
                    } catch (IOException e5) {
                        loge("IOException happen.close failed.");
                    }
                }
            } catch (Throwable th) {
                if (sImsiReader != null) {
                    try {
                        sImsiReader.close();
                    } catch (IOException e6) {
                        loge("IOException happen.close failed.");
                    }
                }
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
            String[] data = (String[]) iter.next();
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
