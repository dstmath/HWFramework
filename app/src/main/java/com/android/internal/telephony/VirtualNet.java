package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.HwTelephony.VirtualNets;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import huawei.com.android.internal.telephony.RoamingBroker;
import java.util.Map;

public class VirtualNet {
    static final String APN_ID = "apn_id";
    private static final String CTRoamingNumeric = "20404";
    private static final boolean DBG = true;
    private static final String LOG_TAG = "GSM";
    private static final int MAX_PHONE_COUNT = 0;
    static final Uri PREFERAPN_NO_UPDATE_URI = null;
    private static final String SPN_EMPTY = "spn_null";
    private static final int SUB1 = 0;
    private static final int SUB2 = 1;
    private static final boolean isMultiSimEnabled = false;
    private static UiccCard[] mUiccCards;
    private static VirtualNet mVirtualNet;
    private static VirtualNet mVirtualNet1;
    private static Map<SpecialFile, byte[]> specialFilesMap;
    private static Map<SpecialFile, byte[]> specialFilesMap1;
    private String apnFilter;
    private String eccNoCard;
    private String eccWithCard;
    private boolean isRealNetwork;
    private int maxMessageSize;
    private int numMatch;
    private int numMatchShort;
    private String numeric;
    private String operatorName;
    private int plmnSameImsiStartCount;
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
                return VirtualNet.SUB1;
            }
            return (this.filePath.hashCode() * 31) + this.fileId.hashCode();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.VirtualNet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.VirtualNet.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.VirtualNet.<clinit>():void");
    }

    private static void clearCurVirtualNetsDb(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.VirtualNet.clearCurVirtualNetsDb(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.VirtualNet.clearCurVirtualNetsDb(int):void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.VirtualNet.clearCurVirtualNetsDb(int):void");
    }

    private static void saveLastVirtualNetsDb(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.VirtualNet.saveLastVirtualNetsDb(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.VirtualNet.saveLastVirtualNetsDb(int):void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.VirtualNet.saveLastVirtualNetsDb(int):void");
    }

    public static void addSpecialFile(String filePath, String fileId, byte[] bytes) {
        specialFilesMap.put(new SpecialFile(filePath, fileId), bytes);
    }

    public static void addSpecialFile(String filePath, String fileId, byte[] bytes, int slotId) {
        SpecialFile specialFile = new SpecialFile(filePath, fileId);
        if (slotId == SUB2) {
            specialFilesMap1.put(specialFile, bytes);
        } else if (slotId == 0) {
            specialFilesMap.put(specialFile, bytes);
        }
    }

    public static VirtualNet getCurrentVirtualNet() {
        if (!isMultiSimEnabled) {
            return mVirtualNet;
        }
        if (hasIccCard(SUB1)) {
            return mVirtualNet;
        }
        if (hasIccCard(SUB2)) {
            return mVirtualNet1;
        }
        return null;
    }

    public static VirtualNet getCurrentVirtualNet(int slotId) {
        logd("getCurrentVirtualNet, slotId=" + slotId);
        if (slotId == SUB2) {
            return mVirtualNet1;
        }
        if (slotId == 0) {
            return mVirtualNet;
        }
        return null;
    }

    public static boolean isVirtualNet() {
        boolean z = DBG;
        if (!isMultiSimEnabled) {
            if (mVirtualNet == null) {
                z = false;
            }
            return z;
        } else if (hasIccCard(SUB1)) {
            return isVirtualNet(SUB1);
        } else {
            if (hasIccCard(SUB2)) {
                return isVirtualNet(SUB2);
            }
            return false;
        }
    }

    public static boolean isVirtualNet(int slotId) {
        boolean z = DBG;
        logd("isVirtualNet, slotId= " + slotId);
        logd("mVirtualNet1, mVirtualNet1= " + mVirtualNet1);
        logd("mVirtualNet, mVirtualNet= " + mVirtualNet);
        if (slotId == SUB2) {
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
            if (slotId == SUB2) {
                specialFilesMap1.clear();
            } else {
                specialFilesMap.clear();
            }
        } else {
            specialFilesMap.clear();
        }
        String[] selectionArgs = new String[]{numeric, Integer.toString(4)};
        String[] projection = new String[]{VirtualNets.NUMERIC, VirtualNets.VIRTUAL_NET_RULE, VirtualNets.MATCH_PATH, VirtualNets.MATCH_FILE};
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
                if (slotId == SUB2) {
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
            if (slotId == SUB2) {
                mVirtualNet1 = null;
            } else {
                mVirtualNet = null;
            }
            if (PhoneFactory.getDefaultPhone() != null && PhoneFactory.getDefaultPhone().getContext() != null) {
                System.putString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), VirtualNets.VN_KEY + slotId, "");
                return;
            }
            return;
        }
        mVirtualNet = null;
        if (PhoneFactory.getDefaultPhone() != null && PhoneFactory.getDefaultPhone().getContext() != null) {
            System.putString(PhoneFactory.getDefaultPhone().getContext().getContentResolver(), "vn_key0", "");
        }
    }

    public static void loadVirtualNet(String numeric, SIMRecords simRecords) {
        logd("start loadVirtualNet: numeric= " + numeric);
        int slotId = simRecords.getSlotId();
        if (HwTelephonyManagerInner.getDefault().isCTSimCard(slotId) && CTRoamingNumeric.equals(numeric)) {
            logd("CT sim card 20404 is not virtualnet");
            removeVirtualNet(slotId);
            clearCurVirtualNetsDb(slotId);
        } else if (numeric == null) {
            loge("number is null,loadVirtualNet will return");
        } else {
            if (isMultiSimEnabled) {
                logd("start loadVirtualNet: slotId= " + slotId);
                if (slotId == SUB2) {
                    mVirtualNet1 = null;
                } else {
                    mVirtualNet = null;
                }
            } else {
                mVirtualNet = null;
            }
            logd("thread = " + Thread.currentThread().getName());
            String imsi = simRecords.getIMSI();
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
                    imsi = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerImsi(Integer.valueOf(slotId));
                    Log.d(LOG_TAG, "VirtualNet RoamingBrokerActivated, set homenetwork imsi");
                }
            } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
                imsi = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerImsi();
                Log.d(LOG_TAG, "VirtualNet RoamingBrokerActivated, set homenetwork imsi");
            }
            byte[] gid1 = simRecords.getGID1();
            String spn = simRecords.getServiceProviderName();
            logd("start loadVirtualNet: numeric=" + numeric + "; gid1=" + IccUtils.bytesToHexString(gid1) + "; spn=" + spn);
            String[] selectionArgs = new String[SUB2];
            selectionArgs[SUB1] = numeric;
            Phone phone = PhoneFactory.getDefaultPhone();
            String[] projection = new String[]{VirtualNets.NUMERIC, VirtualNets.VIRTUAL_NET_RULE, VirtualNets.IMSI_START, VirtualNets.GID1, VirtualNets.GID_MASK, VirtualNets.SPN, VirtualNets.MATCH_PATH, VirtualNets.MATCH_FILE, VirtualNets.MATCH_VALUE, VirtualNets.MATCH_MASK, VirtualNets.APN_FILTER, VirtualNets.VOICEMAIL_NUMBER, VirtualNets.VOICEMAIL_TAG, VirtualNets.NUM_MATCH, VirtualNets.NUM_MATCH_SHORT, VirtualNets.SMS_7BIT_ENABLED, VirtualNets.SMS_CODING_NATIONAL, VirtualNets.ONS_NAME, VirtualNets.SMS_MAX_MESSAGE_SIZE, VirtualNets.SMS_To_MMS_TEXTTHRESHOLD, VirtualNets.ECC_WITH_CARD, VirtualNets.ECC_NO_CARD, VirtualNets.VN_KEY};
            saveLastVirtualNetsDb(slotId);
            clearCurVirtualNetsDb(slotId);
            Cursor cursor = phone.getContext().getContentResolver().query(VirtualNets.CONTENT_URI, projection, "numeric = ?", selectionArgs, null);
            if (cursor == null) {
                loge("query virtual net db got a null cursor");
                return;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int tmpVirtualNetRule = cursor.getInt(cursor.getColumnIndex(VirtualNets.VIRTUAL_NET_RULE));
                switch (tmpVirtualNetRule) {
                    case SUB2 /*1*/:
                        String tmpImsiStart = cursor.getString(cursor.getColumnIndex(VirtualNets.IMSI_START));
                        if (isImsiVirtualNet(imsi, tmpImsiStart)) {
                            logd("find imsi virtual net imsiStart=" + tmpImsiStart);
                            createVirtualNet(cursor, slotId);
                            System.putInt(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId, SUB2);
                            System.putString(phone.getContext().getContentResolver(), VirtualNets.IMSI_START + slotId, tmpImsiStart);
                            break;
                        }
                        break;
                    case HwVSimUtilsInner.STATE_EB /*2*/:
                        try {
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
                        } catch (Exception e) {
                            e.printStackTrace();
                            loge("loadVirtualNet got Exception", e);
                            break;
                        } finally {
                            cursor.close();
                        }
                        break;
                    case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                        String tmpSpn = cursor.getString(cursor.getColumnIndex(VirtualNets.SPN));
                        if (isSpnVirtualNet(spn, tmpSpn)) {
                            logd("find spn virtual net spn=" + tmpSpn);
                            createVirtualNet(cursor, slotId);
                            System.putInt(phone.getContext().getContentResolver(), VirtualNets.VIRTUAL_NET_RULE + slotId, 3);
                            System.putString(phone.getContext().getContentResolver(), VirtualNets.SPN + slotId, tmpSpn);
                            break;
                        }
                        break;
                    case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
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
                    default:
                        logd("unhandled case: " + tmpVirtualNetRule);
                        break;
                }
                cursor.moveToNext();
            }
            if (isVirtualNet(slotId) && !isVirtualNetEqual(slotId)) {
                logd("find different virtual net,so setPreferredApn: delete");
                Phone subPhone = PhoneFactory.getPhone(slotId);
                ContentResolver resolver = subPhone.getContext().getContentResolver();
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    resolver.delete(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId), null, null);
                } else {
                    resolver.delete(PREFERAPN_NO_UPDATE_URI, null, null);
                }
                if (subPhone.mDcTracker != null) {
                    ApnContext apnContext = (ApnContext) subPhone.mDcTracker.mApnContexts.get("default");
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
    }

    private static String getStringFromSettingsEx(ContentResolver resolver, String key, String defaultValue) {
        String value = System.getString(resolver, key);
        return value == null ? defaultValue : value;
    }

    private static int getIntFromSettingsEx(ContentResolver resolver, String key) {
        return System.getInt(resolver, key, -99);
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
        String curNumeric = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.NUMERIC + slotId, "");
        String curApnFilter = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.APN_FILTER + slotId, "");
        String curVoicemalNumber = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_NUMBER + slotId, "");
        String curVoicemalTag = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_TAG + slotId, "");
        int curNumMatch = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.NUM_MATCH + slotId);
        int curNumMatchShort = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.NUM_MATCH_SHORT + slotId);
        int curSms7BitEnabled = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SMS_7BIT_ENABLED + slotId);
        int curSmsCodingNational = getIntFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.SMS_CODING_NATIONAL + slotId);
        String curOperatorName = getStringFromSettingsEx(phone.getContext().getContentResolver(), VirtualNets.ONS_NAME + slotId, "");
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
        if (curVirtualNetRule == 0) {
            logd("RULE_NONE different virtual net");
            return false;
        } else if (curVirtualNetRule != lastVirtualNetRule) {
            logd("curVirtualNetRule != lastVirtualNetRule different virtual net");
            return false;
        } else {
            switch (curVirtualNetRule) {
                case SUB2 /*1*/:
                    if (!curImsiStart.equals(lastImsiStart)) {
                        logd("RULE_IMSI different virtual net");
                        return false;
                    }
                    break;
                case HwVSimUtilsInner.STATE_EB /*2*/:
                    if (!(curGid1Value.equals(lastGid1Value) && curGidMask.equals(lastGidMask))) {
                        logd("RULE_GID1 different virtual net");
                        return false;
                    }
                case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                    if (!curSpn.equals(lastSpn)) {
                        logd("RULE_SPN different virtual net");
                        return false;
                    }
                    break;
                case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                    if (!(curMatchPath.equals(lastMatchPath) && curMatchFile.equals(lastMatchFile) && curMatchValue.equals(lastMatchValue) && curMatchMask.equals(lastMatchMask))) {
                        logd("RULE_MATCH_FILE different virtual net");
                        return false;
                    }
                default:
                    logd("RULE unkown different virtual net");
                    return false;
            }
            if (curNumeric.equals(lastNumeric) && curApnFilter.equals(lastApnFilter) && curVoicemalNumber.equals(lastVoicemalNumber) && curVoicemalTag.equals(lastVoicemalTag) && curNumMatch == lastNumMatch && curNumMatchShort == lastNumMatchShort && curSms7BitEnabled == lastSms7BitEnabled && curSmsCodingNational == lastSmsCodingNational && curOperatorName.equals(lastOperatorName)) {
                logd("the same virtual net");
                return DBG;
            }
            logd("params not equal ,different virtual net");
            return false;
        }
    }

    protected static boolean isSpecialFileVirtualNet(String matchPath, String matchFile, String matchValue, String matchMask, int slotId) {
        byte[] bytes;
        SpecialFile specialFile = new SpecialFile(matchPath, matchFile);
        if (!isMultiSimEnabled) {
            bytes = (byte[]) specialFilesMap.get(specialFile);
        } else if (slotId == SUB2) {
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
            return DBG;
        }
        boolean equals = (spn1 == null || spn2 == null) ? false : spn1.equals(spn2);
        return equals;
    }

    protected static boolean isGid1VirtualNet(byte[] gid1, String gid1Value, String gidMask) {
        logd("isGid1VirtualNet gid1 = " + IccUtils.bytesToHexString(gid1) + "; gid1Value = " + gid1Value + "; gidMask = " + gidMask);
        return matchByteWithMask(gid1, gid1Value, gidMask);
    }

    private static boolean matchByteWithMask(byte[] data, String value, String mask) {
        if (data == null || value == null || mask == null || data.length * 2 < value.length() - 2 || value.length() < 2 || !value.substring(SUB1, 2).equalsIgnoreCase("0x") || mask.length() < 2 || !mask.substring(SUB1, 2).equalsIgnoreCase("0x")) {
            return false;
        }
        if (!isEmptySimFile(data) || value.equalsIgnoreCase("0xFF")) {
            String valueString = value.substring(2);
            String maskString = mask.substring(2);
            String gid1String = IccUtils.bytesToHexString(data);
            int i;
            if (maskString == null || gid1String == null || valueString == null || maskString.length() % 2 != SUB2) {
                byte[] valueBytes = IccUtils.hexStringToBytes(valueString);
                byte[] maskBytes = IccUtils.hexStringToBytes(maskString);
                if (valueBytes.length != maskBytes.length) {
                    return false;
                }
                boolean match = DBG;
                for (i = SUB1; i < maskBytes.length; i += SUB2) {
                    if ((data[i] & maskBytes[i]) != valueBytes[i]) {
                        match = false;
                    }
                }
                return match;
            }
            int maskStringLength = maskString.length();
            int gid1StringLength = gid1String.length();
            int valueStringLength = valueString.length();
            logd("Gid1 length is odd ,Gid1 length:" + maskString.length());
            if (gid1StringLength < maskStringLength || maskStringLength != valueStringLength) {
                logd("Gid1 length is not match");
                return false;
            }
            i = SUB1;
            while (i < maskStringLength) {
                if (maskString.charAt(i) == 'F' && gid1String.charAt(i) == valueString.charAt(i)) {
                    i += SUB2;
                } else {
                    logd("Gid1 mask did not match");
                    return false;
                }
            }
            for (i = maskStringLength; i < gid1StringLength; i += SUB2) {
                if (gid1String.charAt(i) != 'f') {
                    logd("Gid1 string did not match");
                    return false;
                }
            }
            return DBG;
        }
        logd("matchByteWithMask data is null");
        return false;
    }

    private static boolean isEmptySimFile(byte[] gid1) {
        boolean isEmptyFile = DBG;
        int length = gid1.length;
        for (int i = SUB1; i < length; i += SUB2) {
            if (gid1[i] != -1) {
                isEmptyFile = false;
            }
        }
        return isEmptyFile;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static boolean isImsiVirtualNet(String imsi, String tmpImsiStart) {
        if (imsi == null || tmpImsiStart == null || !imsi.startsWith(tmpImsiStart)) {
            return false;
        }
        return DBG;
    }

    private static void createVirtualNet(Cursor cursor, int slotId) {
        if (!isMultiSimEnabled) {
            mVirtualNet = getVirtualNet(cursor, SUB1);
            logd("createVirtualNet sigelcard mVirtualNet =" + mVirtualNet);
        } else if (slotId == SUB2) {
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
        String tmpNumeric = cursor.getString(cursor.getColumnIndex(VirtualNets.NUMERIC));
        String tmpApnFilter = cursor.getString(cursor.getColumnIndex(VirtualNets.APN_FILTER));
        String tmpVoicemalNumber = cursor.getString(cursor.getColumnIndex(VirtualNets.VOICEMAIL_NUMBER));
        String tmpVoicemalTag = cursor.getString(cursor.getColumnIndex(VirtualNets.VOICEMAIL_TAG));
        int tmpNumMatch = cursor.getInt(cursor.getColumnIndex(VirtualNets.NUM_MATCH));
        int tmpNumMatchShort = cursor.getInt(cursor.getColumnIndex(VirtualNets.NUM_MATCH_SHORT));
        int tmpSms7BitEnabled = cursor.getInt(cursor.getColumnIndex(VirtualNets.SMS_7BIT_ENABLED));
        int tmpSmsCodingNational = cursor.getInt(cursor.getColumnIndex(VirtualNets.SMS_CODING_NATIONAL));
        String tmpOperatorName = cursor.getString(cursor.getColumnIndex(VirtualNets.ONS_NAME));
        int tmpmaxmessagesize = cursor.getInt(cursor.getColumnIndex(VirtualNets.SMS_MAX_MESSAGE_SIZE));
        int tmpsmstommstextthreshold = cursor.getInt(cursor.getColumnIndex(VirtualNets.SMS_To_MMS_TEXTTHRESHOLD));
        String tempEccWithCard = cursor.getString(cursor.getColumnIndex(VirtualNets.ECC_WITH_CARD));
        String tempEccNoCard = cursor.getString(cursor.getColumnIndex(VirtualNets.ECC_NO_CARD));
        String tmpVnKey = cursor.getString(cursor.getColumnIndex(VirtualNets.VN_KEY));
        VirtualNet virtualNet = null;
        if (tmpNumeric != null && tmpNumeric.trim().length() > 0) {
            virtualNet = new VirtualNet(tmpNumeric, tmpApnFilter, tmpVoicemalNumber, tmpVoicemalTag, tmpNumMatch, tmpNumMatchShort, tmpSms7BitEnabled, tmpSmsCodingNational, tmpOperatorName, tmpmaxmessagesize, tmpsmstommstextthreshold, tempEccWithCard, tempEccNoCard);
            Phone phone = PhoneFactory.getDefaultPhone();
            System.putString(phone.getContext().getContentResolver(), VirtualNets.NUMERIC + slotId, tmpNumeric);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.APN_FILTER + slotId, tmpApnFilter);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_NUMBER + slotId, tmpVoicemalNumber);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.VOICEMAIL_TAG + slotId, tmpVoicemalTag);
            System.putInt(phone.getContext().getContentResolver(), VirtualNets.NUM_MATCH + slotId, tmpNumMatch);
            System.putInt(phone.getContext().getContentResolver(), VirtualNets.NUM_MATCH_SHORT + slotId, tmpNumMatchShort);
            System.putInt(phone.getContext().getContentResolver(), VirtualNets.SMS_7BIT_ENABLED + slotId, tmpSms7BitEnabled);
            System.putInt(phone.getContext().getContentResolver(), VirtualNets.SMS_CODING_NATIONAL + slotId, tmpSmsCodingNational);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.ONS_NAME + slotId, tmpOperatorName);
            System.putString(phone.getContext().getContentResolver(), VirtualNets.VN_KEY + slotId, tmpVnKey);
        }
        int tmpVirtualNetRule = cursor.getInt(cursor.getColumnIndex(VirtualNets.VIRTUAL_NET_RULE));
        String tmpImsiStart = cursor.getString(cursor.getColumnIndex(VirtualNets.IMSI_START));
        if (tmpVirtualNetRule == SUB2 && tmpNumeric != null && tmpNumeric.equals(tmpImsiStart) && virtualNet != null) {
            logd("getVirtualNet find a realNetwork tmpNumeric = " + tmpNumeric);
            virtualNet.isRealNetwork = DBG;
            if (slotId == SUB2) {
                if (mVirtualNet1 == null) {
                    virtualNet.plmnSameImsiStartCount = SUB2;
                } else {
                    virtualNet.plmnSameImsiStartCount = mVirtualNet1.plmnSameImsiStartCount + SUB2;
                }
            } else if (mVirtualNet == null) {
                virtualNet.plmnSameImsiStartCount = SUB2;
            } else {
                virtualNet.plmnSameImsiStartCount = mVirtualNet.plmnSameImsiStartCount + SUB2;
            }
        }
        return virtualNet;
    }

    public VirtualNet(String tmpNumeric, String tmpApnFilter, String tmpVoicemalNumber, String tmpVoicemalTag, int tmpNumMatch, int tmpNumMatchShort, int tmpSms7BitEnabled, int tmpSmsCodingNational, String tmpOperatorName, int tmpmaxmessagesize, int tmpsmstommstextthreshold) {
        this.plmnSameImsiStartCount = SUB1;
        this.isRealNetwork = false;
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
        this.plmnSameImsiStartCount = SUB1;
        this.isRealNetwork = false;
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
        this.plmnSameImsiStartCount = SUB1;
        this.isRealNetwork = false;
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
        boolean z = false;
        boolean z2 = DBG;
        logd("validNetConfig isRealNetwork = " + this.isRealNetwork);
        if ("26207".equals(this.numeric)) {
            if (!this.isRealNetwork) {
                z = DBG;
            }
            return z;
        } else if (!this.isRealNetwork) {
            return DBG;
        } else {
            logd("validNetConfig plmnSameImsiStartCount = " + this.plmnSameImsiStartCount);
            if (this.plmnSameImsiStartCount > SUB2) {
                z2 = false;
            }
            return z2;
        }
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
                z = DBG;
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
                bRet = isCardPresent(slotId);
                logd("isMultiSimEnabled hasIccCard, bRet=" + bRet + " for slot" + slotId);
                return bRet;
            } catch (Exception e) {
                e.printStackTrace();
                loge("call isCardPresent got Exception", e);
                return bRet;
            }
        }
        try {
            bRet = isCardPresent(SUB1);
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
            return getOperatorKey(context, SUB1);
        }
        String op_key = getOperatorKey(context, SUB1);
        if (TextUtils.isEmpty(op_key)) {
            return getOperatorKey(context, SUB2);
        }
        return op_key;
    }

    public static String getOperatorKey(Context context, int slotId) {
        if ((slotId != 0 && slotId != SUB2) || context == null) {
            return null;
        }
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
}
