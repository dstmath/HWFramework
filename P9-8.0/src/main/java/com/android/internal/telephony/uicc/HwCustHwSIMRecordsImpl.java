package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class HwCustHwSIMRecordsImpl extends HwCustHwSIMRecords {
    public static final String DATA_ROAMING_SIM2 = "data_roaming_sim2";
    private static final int EF_OCSGL = 20356;
    private static final int EVENT_GET_OCSGL_DONE = 1;
    private static final boolean HWDBG = true;
    private static String LAST_ICCID = "data_roaming_setting_last_iccid";
    private static final String LOG_TAG = "HwCustHwSIMRecordsImpl";
    private static boolean mIsSaveCardTypeLGU = SystemProperties.getBoolean("ro.config.save_cardtype_lgu", false);
    private static boolean mIsSupportCsgSearch = SystemProperties.getBoolean("ro.config.att.csg", false);
    private Handler custHandlerEx = new Handler() {
        /* JADX WARNING: Removed duplicated region for block: B:41:0x011f A:{LOOP_END, LOOP:0: B:24:0x00cb->B:41:0x011f} */
        /* JADX WARNING: Removed duplicated region for block: B:42:0x00f5 A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x00fb A:{Catch:{ RuntimeException -> 0x009c, all -> 0x0116 }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            if (HwCustHwSIMRecordsImpl.this.mSIMRecords == null || !HwCustHwSIMRecordsImpl.this.mSIMRecords.mDestroyed.get()) {
                SIMRecords sIMRecords;
                try {
                    switch (msg.what) {
                        case 1:
                            AsyncResult ar = msg.obj;
                            if (ar.exception != null || ar.result == null) {
                                HwCustHwSIMRecordsImpl.this.log("=csg= EVENT_GET_OCSGL_DONE exception = " + ar.exception);
                                HwCustHwSIMRecordsImpl.this.mEfOcsgl = null;
                                break;
                            }
                            ArrayList<byte[]> dataLists = ar.result;
                            int recordSize = 0;
                            HwCustHwSIMRecordsImpl.this.mEfOcsgl = null;
                            int j = 0;
                            int list_size = dataLists.size();
                            while (j < list_size) {
                                byte[] item = (byte[]) dataLists.get(j);
                                int i = 0;
                                while (i < item.length) {
                                    if ((item[i] & 255) != 255) {
                                        HwCustHwSIMRecordsImpl.this.mEfOcsgl = Arrays.copyOf(item, item.length);
                                        HwCustHwSIMRecordsImpl.this.log("=csg= SIMRecords:  OCSGL not empty.");
                                        if (i >= item.length) {
                                            if (recordSize >= dataLists.size()) {
                                                HwCustHwSIMRecordsImpl.this.mEfOcsgl = new byte[0];
                                                HwCustHwSIMRecordsImpl.this.log("=csg= SIMRecords:  OCSGL is empty. ");
                                            }
                                            HwCustHwSIMRecordsImpl.this.mSIMRecords.mCsgRecordsLoadedRegistrants.notifyRegistrants();
                                            break;
                                        }
                                        recordSize++;
                                        j++;
                                    } else {
                                        i++;
                                    }
                                }
                                if (i >= item.length) {
                                }
                            }
                            if (recordSize >= dataLists.size()) {
                            }
                            HwCustHwSIMRecordsImpl.this.mSIMRecords.mCsgRecordsLoadedRegistrants.notifyRegistrants();
                            break;
                        default:
                            HwCustHwSIMRecordsImpl.this.log("unknown Event: " + msg.what);
                            break;
                    }
                    sIMRecords = HwCustHwSIMRecordsImpl.this.mSIMRecords;
                } catch (RuntimeException exc) {
                    HwCustHwSIMRecordsImpl.this.log("Exception parsing SIM record:" + exc);
                    sIMRecords = HwCustHwSIMRecordsImpl.this.mSIMRecords;
                } catch (Throwable th) {
                    SIMRecords sIMRecords2 = HwCustHwSIMRecordsImpl.this.mSIMRecords;
                    throw th;
                }
                return;
            }
            Rlog.e(HwCustHwSIMRecordsImpl.LOG_TAG, "Received message " + msg + "[" + msg.what + "] " + " while being destroyed. Ignoring.");
        }
    };
    boolean iccidChanged = false;
    private byte[] mEfOcsgl = null;

    public HwCustHwSIMRecordsImpl(SIMRecords obj, Context mConText) {
        super(obj, mConText);
    }

    public void setVmPriorityModeInClaro(VoiceMailConstants mVmConfig) {
        if (this.mContext != null && this.mSIMRecords != null) {
            int VoicemailPriorityMode = Systemex.getInt(this.mContext.getContentResolver(), "voicemailPrioritySpecial_" + this.mSIMRecords.getOperatorNumeric(), 0);
            log("The SIM card MCCMNC = " + this.mSIMRecords.getOperatorNumeric());
            if (VoicemailPriorityMode != 0 && mVmConfig != null) {
                mVmConfig.setVoicemailInClaro(VoicemailPriorityMode);
                log("VoicemailPriorityMode from custom = " + VoicemailPriorityMode);
            }
        }
    }

    public void refreshDataRoamingSettings() {
        String roamingAreaStr = Systemex.getString(this.mContext.getContentResolver(), "list_roaming_open_area");
        log("refreshDataRoamingSettings(): roamingAreaStr = " + roamingAreaStr);
        if (TextUtils.isEmpty(roamingAreaStr) || this.mSIMRecords == null) {
            log("refreshDataRoamingSettings(): roamingAreaStr is empty");
            return;
        }
        SharedPreferences sp = this.mContext.getSharedPreferences("DataRoamingSettingIccid", 0);
        String mIccid = this.mSIMRecords.getIccId();
        if (!TextUtils.isEmpty(mIccid)) {
            String oldIccid;
            String oldIccid2;
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                oldIccid = sp.getString(LAST_ICCID + this.mSIMRecords.getSlotId(), null);
            } else {
                oldIccid = sp.getString(LAST_ICCID, null);
            }
            if (oldIccid != null) {
                try {
                    oldIccid2 = new String(Base64.decode(oldIccid, 0), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not UnsupportedEncodingException");
                    oldIccid2 = oldIccid;
                }
            } else {
                oldIccid2 = oldIccid;
            }
            if (mIccid.equals(oldIccid2)) {
                this.iccidChanged = false;
            } else {
                this.iccidChanged = HWDBG;
            }
            if (this.iccidChanged) {
                try {
                    Editor editor = sp.edit();
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        editor.putString(LAST_ICCID + this.mSIMRecords.getSlotId(), new String(Base64.encode(mIccid.getBytes("utf-8"), 0), "utf-8"));
                    } else {
                        editor.putString(LAST_ICCID, new String(Base64.encode(mIccid.getBytes("utf-8"), 0), "utf-8"));
                    }
                    editor.commit();
                } catch (UnsupportedEncodingException e2) {
                    Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not UnsupportedEncodingException");
                }
                String[] areaArray = roamingAreaStr.split(",");
                String operator = this.mSIMRecords.getOperatorNumeric();
                log("refreshDataRoamingSettings(): roamingAreaStr : " + roamingAreaStr + " operator : " + operator);
                int i;
                int length;
                String area;
                if (TelephonyManager.getDefault() != null && (TelephonyManager.getDefault().isMultiSimEnabled() ^ 1) != 0) {
                    i = 0;
                    length = areaArray.length;
                    while (i < length) {
                        area = areaArray[i];
                        log("refreshDataRoamingSettings(): area : " + area);
                        if (!area.equals(operator)) {
                            Global.putInt(this.mContext.getContentResolver(), "data_roaming", 0);
                            i++;
                        } else if (isSkipDataRoamingGid()) {
                            log("refreshDataRoamingSettings(): isSkipDataRoamingGid() returns true");
                        } else {
                            log("refreshDataRoamingSettings(): setting data roaming to true");
                            Systemex.putInt(this.mContext.getContentResolver(), "roaming_saving_on", 1);
                            Global.putInt(this.mContext.getContentResolver(), "data_roaming", 1);
                        }
                    }
                } else if (TelephonyManager.getDefault() != null && TelephonyManager.getDefault().isMultiSimEnabled()) {
                    log("######## MultiSimEnabled");
                    for (String area2 : areaArray) {
                        log("refreshDataRoamingSettings(): else loop area : " + area2);
                        if (area2.equals(operator)) {
                            if (this.mSIMRecords.getSlotId() == 0) {
                                if (isSkipDataRoamingGid()) {
                                    log("refreshDataRoamingSettings(): isSkipDataRoamingGid() returns true for SIM1");
                                } else {
                                    Global.putInt(this.mContext.getContentResolver(), "data_roaming", 1);
                                    log("refreshDataRoamingSettings(): setting data roaming to true else loop SIM1");
                                }
                            } else if (1 != this.mSIMRecords.getSlotId()) {
                                log("doesn't contains the carrier" + operator + "for slotId" + this.mSIMRecords.getSlotId());
                            } else if (isSkipDataRoamingGid()) {
                                log("refreshDataRoamingSettings(): isSkipDataRoamingGid() returns true for SIM2");
                            } else {
                                Global.putInt(this.mContext.getContentResolver(), DATA_ROAMING_SIM2, 1);
                            }
                        } else if (this.mSIMRecords.getSlotId() == 0) {
                            Global.putInt(this.mContext.getContentResolver(), "data_roaming", 0);
                        } else if (1 == this.mSIMRecords.getSlotId()) {
                            Global.putInt(this.mContext.getContentResolver(), DATA_ROAMING_SIM2, 0);
                        }
                    }
                }
                return;
            }
            Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not changed" + this.iccidChanged);
        }
    }

    private boolean isSkipDataRoamingGid() {
        String skipDataRoamingGid = Systemex.getString(this.mContext.getContentResolver(), "hw_skip_data_roaming_gid");
        Object simGidbytes = this.mSIMRecords != null ? this.mSIMRecords.getGID1() : null;
        log("isSkipDataRoamingGid(): skipDataRoamingGid : " + skipDataRoamingGid + " simGidbytes : " + simGidbytes);
        boolean matched = false;
        if (TextUtils.isEmpty(skipDataRoamingGid) || simGidbytes == null || simGidbytes.length <= 0) {
            return false;
        }
        String[] gidArray = skipDataRoamingGid.split(",");
        String simGid = IccUtils.bytesToHexString(simGidbytes);
        if (simGid == null || simGid.length() < 2) {
            return false;
        }
        log("isSkipDataRoamingGid(): simGid : " + simGid);
        for (String gid : gidArray) {
            log("isSkipDataRoamingGid(): cust gid : " + gid);
            if (simGid.substring(0, 2).equals(gid)) {
                matched = HWDBG;
                break;
            }
        }
        log("isSkipDataRoamingGid() returning : " + matched);
        return matched;
    }

    public void refreshMobileDataAlwaysOnSettings() {
        String dataAlwaysOnAreaStr = System.getString(this.mContext.getContentResolver(), "list_mobile_data_always_on");
        log("refreshMobileDataAlwaysOnSettings(): dataAlwaysOnAreaStr = " + dataAlwaysOnAreaStr);
        if (TextUtils.isEmpty(dataAlwaysOnAreaStr) || this.mSIMRecords == null) {
            log("refreshMobileDataAlwaysOnSettings(): dataAlwaysOnAreaStr is empty");
        } else if (System.getInt(this.mContext.getContentResolver(), "whether_data_alwayson_init", 0) == 1) {
            log("refreshMobileDataAlwaysOnSettings(): whether_data_alwayson_init is 1");
        } else {
            String[] areaArray = dataAlwaysOnAreaStr.split(",");
            String operator = this.mSIMRecords.getOperatorNumeric();
            for (String area : areaArray) {
                if (area.equals(operator)) {
                    System.putInt(this.mContext.getContentResolver(), "power_saving_on", 0);
                    System.putInt(this.mContext.getContentResolver(), "whether_data_alwayson_init", 1);
                    break;
                }
            }
        }
    }

    private void log(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public void custLoadCardSpecialFile(int fileid) {
        switch (fileid) {
            case EF_OCSGL /*20356*/:
                if (mIsSupportCsgSearch) {
                    log("=csg= fetchSimRecords => CSG ... ");
                    if (this.mSIMRecords != null) {
                        this.mSIMRecords.mFh.loadEFLinearFixedAll(EF_OCSGL, this.custHandlerEx.obtainMessage(1));
                        return;
                    } else {
                        log("IccRecords is null !!! ");
                        return;
                    }
                }
                return;
            default:
                Rlog.d(LOG_TAG, "no fileid found for load");
                return;
        }
    }

    public byte[] getOcsgl() {
        if (this.mEfOcsgl == null || this.mSIMRecords == null) {
            if (this.mSIMRecords != null) {
                this.mSIMRecords.setCsglexist(false);
            }
            return new byte[0];
        }
        this.mSIMRecords.setCsglexist(HWDBG);
        if (this.mEfOcsgl.length > 0) {
            return Arrays.copyOf(this.mEfOcsgl, this.mEfOcsgl.length);
        }
        return new byte[0];
    }

    public void refreshCardType() {
        if (mIsSaveCardTypeLGU) {
            int card_type = -1;
            if (this.mSIMRecords.mImsi != null) {
                if (this.mSIMRecords.mImsi.substring(0, 6).equals("450069")) {
                    log("LGU SIMRecords: Refresh_card_type card_type = " + 3);
                    SystemProperties.set("gsm.sim.card.type", String.valueOf(3));
                    return;
                }
                String countryCode = this.mSIMRecords.mImsi.substring(0, 5);
                if (countryCode.substring(0, 5).equals("45006")) {
                    card_type = 0;
                } else if (countryCode.substring(0, 3).equals("450")) {
                    card_type = 1;
                } else {
                    card_type = 2;
                }
            }
            SystemProperties.set("gsm.sim.card.type", String.valueOf(card_type));
            log("LGU SIMRecords: card_type = " + card_type);
            log("LGU SIMRecords: Refresh_card_type exit ");
        }
    }
}
