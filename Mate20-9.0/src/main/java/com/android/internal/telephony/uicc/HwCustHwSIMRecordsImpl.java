package com.android.internal.telephony.uicc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsEx;
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
    private static final int ROAM_CUST_SET = 1;
    private static final int ROAM_CUST_UNSET = 0;
    private static final int SLOT0 = 0;
    private static final int SLOT1 = 1;
    private static boolean mIsSaveCardTypeLGU = SystemProperties.getBoolean("ro.config.save_cardtype_lgu", false);
    private static boolean mIsSupportCsgSearch = SystemProperties.getBoolean("ro.config.att.csg", false);
    private Handler custHandlerEx = new Handler() {
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ea, code lost:
            if (0 != 0) goto L_0x010e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x010c, code lost:
            if (0 != 0) goto L_0x010e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x010e, code lost:
            r10.this$0.mSIMRecords.onRecordLoaded();
         */
        public void handleMessage(Message msg) {
            if (HwCustHwSIMRecordsImpl.this.mSIMRecords == null || !HwCustHwSIMRecordsImpl.this.mSIMRecords.mDestroyed.get()) {
                try {
                    if (msg.what != 1) {
                        HwCustHwSIMRecordsImpl hwCustHwSIMRecordsImpl = HwCustHwSIMRecordsImpl.this;
                        hwCustHwSIMRecordsImpl.log("unknown Event: " + msg.what);
                    } else {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            if (ar.result != null) {
                                ArrayList<byte[]> dataLists = (ArrayList) ar.result;
                                int recordSize = 0;
                                byte[] unused = HwCustHwSIMRecordsImpl.this.mEfOcsgl = null;
                                int j = 0;
                                int list_size = dataLists.size();
                                while (true) {
                                    if (j >= list_size) {
                                        break;
                                    }
                                    byte[] item = dataLists.get(j);
                                    int i = 0;
                                    while (true) {
                                        if (i >= item.length) {
                                            break;
                                        } else if ((item[i] & 255) != 255) {
                                            byte[] unused2 = HwCustHwSIMRecordsImpl.this.mEfOcsgl = Arrays.copyOf(item, item.length);
                                            HwCustHwSIMRecordsImpl.this.log("=csg= SIMRecords:  OCSGL not empty.");
                                            break;
                                        } else {
                                            i++;
                                        }
                                    }
                                    if (i < item.length) {
                                        break;
                                    }
                                    recordSize++;
                                    j++;
                                }
                                if (recordSize >= dataLists.size()) {
                                    byte[] unused3 = HwCustHwSIMRecordsImpl.this.mEfOcsgl = new byte[0];
                                    HwCustHwSIMRecordsImpl.this.log("=csg= SIMRecords:  OCSGL is empty. ");
                                }
                                HwCustHwSIMRecordsImpl.this.mSIMRecords.mCsgRecordsLoadedRegistrants.notifyRegistrants();
                            }
                        }
                        HwCustHwSIMRecordsImpl hwCustHwSIMRecordsImpl2 = HwCustHwSIMRecordsImpl.this;
                        hwCustHwSIMRecordsImpl2.log("=csg= EVENT_GET_OCSGL_DONE exception = " + ar.exception);
                        byte[] unused4 = HwCustHwSIMRecordsImpl.this.mEfOcsgl = null;
                    }
                    if (HwCustHwSIMRecordsImpl.this.mSIMRecords != null) {
                    }
                } catch (RuntimeException exc) {
                    HwCustHwSIMRecordsImpl hwCustHwSIMRecordsImpl3 = HwCustHwSIMRecordsImpl.this;
                    hwCustHwSIMRecordsImpl3.log("Exception parsing SIM record:" + exc);
                    if (HwCustHwSIMRecordsImpl.this.mSIMRecords != null) {
                    }
                } catch (Throwable th) {
                    if (!(HwCustHwSIMRecordsImpl.this.mSIMRecords == null || 0 == 0)) {
                        HwCustHwSIMRecordsImpl.this.mSIMRecords.onRecordLoaded();
                    }
                    throw th;
                }
                return;
            }
            Rlog.e(HwCustHwSIMRecordsImpl.LOG_TAG, "Received message " + msg + "[" + msg.what + "]  while being destroyed. Ignoring.");
        }
    };
    boolean iccidChanged = false;
    /* access modifiers changed from: private */
    public byte[] mEfOcsgl = null;

    public HwCustHwSIMRecordsImpl(SIMRecords obj, Context mConText) {
        super(obj, mConText);
    }

    public void setVmPriorityModeInClaro(VoiceMailConstants mVmConfig) {
        if (this.mContext != null && this.mSIMRecords != null) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            int VoicemailPriorityMode = SettingsEx.Systemex.getInt(contentResolver, "voicemailPrioritySpecial_" + this.mSIMRecords.getOperatorNumeric(), 0);
            log("The SIM card MCCMNC = " + this.mSIMRecords.getOperatorNumeric());
            if (VoicemailPriorityMode != 0 && mVmConfig != null) {
                mVmConfig.setVoicemailInClaro(VoicemailPriorityMode);
                log("VoicemailPriorityMode from custom = " + VoicemailPriorityMode);
            }
        }
    }

    public static String getCustDataRoamingSettings(int slotId) {
        return "has_cust_data_roaming_set" + slotId;
    }

    public void refreshDataRoamingSettings() {
        String oldIccid;
        String area;
        String area2;
        String roamingAreaStr = SettingsEx.Systemex.getString(this.mContext.getContentResolver(), "list_roaming_open_area");
        log("refreshDataRoamingSettings(): roamingAreaStr = " + roamingAreaStr);
        if (TextUtils.isEmpty(roamingAreaStr) || this.mSIMRecords == null) {
            log("refreshDataRoamingSettings(): roamingAreaStr is empty");
            return;
        }
        SharedPreferences sp = this.mContext.getSharedPreferences("DataRoamingSettingIccid", 0);
        String mIccid = this.mSIMRecords.getIccId();
        if (!TextUtils.isEmpty(mIccid)) {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                oldIccid = sp.getString(LAST_ICCID + this.mSIMRecords.getSlotId(), null);
            } else {
                oldIccid = sp.getString(LAST_ICCID, null);
            }
            if (oldIccid != null) {
                try {
                    oldIccid = new String(Base64.decode(oldIccid, 0), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not UnsupportedEncodingException");
                }
            }
            if (!mIccid.equals(oldIccid)) {
                this.iccidChanged = HWDBG;
            } else {
                this.iccidChanged = false;
            }
            if (this.iccidChanged) {
                try {
                    SharedPreferences.Editor editor = sp.edit();
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        editor.putString(LAST_ICCID + this.mSIMRecords.getSlotId(), new String(Base64.encode(mIccid.getBytes("utf-8"), 0), "utf-8"));
                    } else {
                        editor.putString(LAST_ICCID, new String(Base64.encode(mIccid.getBytes("utf-8"), 0), "utf-8"));
                    }
                    editor.commit();
                } catch (UnsupportedEncodingException e2) {
                    Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not UnsupportedEncodingException");
                }
                Settings.Global.putInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(0), 0);
                Settings.Global.putInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(1), 0);
                String[] areaArray = roamingAreaStr.split(",");
                String operator = this.mSIMRecords.getOperatorNumeric();
                log("refreshDataRoamingSettings(): roamingAreaStr : " + roamingAreaStr + " operator : " + operator);
                if (TelephonyManager.getDefault() != null && !TelephonyManager.getDefault().isMultiSimEnabled()) {
                    int length = areaArray.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        log("refreshDataRoamingSettings(): area : " + area2);
                        if (!area2.equals(operator)) {
                            Settings.Global.putInt(this.mContext.getContentResolver(), "data_roaming", 0);
                            i++;
                        } else if (isSkipDataRoamingGid()) {
                            log("refreshDataRoamingSettings(): isSkipDataRoamingGid() returns true");
                        } else {
                            log("refreshDataRoamingSettings(): setting data roaming to true");
                            SettingsEx.Systemex.putInt(this.mContext.getContentResolver(), "roaming_saving_on", 1);
                            Settings.Global.putInt(this.mContext.getContentResolver(), "data_roaming", 1);
                            Settings.Global.putInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(0), 1);
                        }
                    }
                } else if (TelephonyManager.getDefault() != null && TelephonyManager.getDefault().isMultiSimEnabled()) {
                    log("######## MultiSimEnabled");
                    int length2 = areaArray.length;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= length2) {
                            break;
                        }
                        log("refreshDataRoamingSettings(): else loop area : " + area);
                        if (area.equals(operator)) {
                            if (this.mSIMRecords.getSlotId() == 0) {
                                if (isSkipDataRoamingGid()) {
                                    log("refreshDataRoamingSettings(): isSkipDataRoamingGid() returns true for SIM1");
                                } else {
                                    Settings.Global.putInt(this.mContext.getContentResolver(), "data_roaming", 1);
                                    Settings.Global.putInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(0), 1);
                                    log("refreshDataRoamingSettings(): setting data roaming to true else loop SIM1");
                                }
                            } else if (1 != this.mSIMRecords.getSlotId()) {
                                log("doesn't contains the carrier" + operator + "for slotId" + this.mSIMRecords.getSlotId());
                            } else if (isSkipDataRoamingGid()) {
                                log("refreshDataRoamingSettings(): isSkipDataRoamingGid() returns true for SIM2");
                            } else {
                                Settings.Global.putInt(this.mContext.getContentResolver(), DATA_ROAMING_SIM2, 1);
                                Settings.Global.putInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(1), 1);
                            }
                        } else if (this.mSIMRecords.getSlotId() == 0) {
                            Settings.Global.putInt(this.mContext.getContentResolver(), "data_roaming", 0);
                        } else if (1 == this.mSIMRecords.getSlotId()) {
                            Settings.Global.putInt(this.mContext.getContentResolver(), DATA_ROAMING_SIM2, 0);
                        }
                        i2++;
                    }
                }
                return;
            }
            Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not changed" + this.iccidChanged);
        }
    }

    private boolean isSkipDataRoamingGid() {
        String skipDataRoamingGid = SettingsEx.Systemex.getString(this.mContext.getContentResolver(), "hw_skip_data_roaming_gid");
        byte[] simGidbytes = this.mSIMRecords != null ? this.mSIMRecords.getGID1() : null;
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
        int length = gidArray.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String gid = gidArray[i];
            log("isSkipDataRoamingGid(): cust gid : " + gid);
            if (simGid.substring(0, 2).equals(gid)) {
                matched = HWDBG;
                break;
            }
            i++;
        }
        log("isSkipDataRoamingGid() returning : " + matched);
        return matched;
    }

    public void refreshMobileDataAlwaysOnSettings() {
        String dataAlwaysOnAreaStr = Settings.System.getString(this.mContext.getContentResolver(), "list_mobile_data_always_on");
        log("refreshMobileDataAlwaysOnSettings(): dataAlwaysOnAreaStr = " + dataAlwaysOnAreaStr);
        if (TextUtils.isEmpty(dataAlwaysOnAreaStr) || this.mSIMRecords == null) {
            log("refreshMobileDataAlwaysOnSettings(): dataAlwaysOnAreaStr is empty");
        } else if (Settings.System.getInt(this.mContext.getContentResolver(), "whether_data_alwayson_init", 0) == 1) {
            log("refreshMobileDataAlwaysOnSettings(): whether_data_alwayson_init is 1");
        } else {
            String[] areaArray = dataAlwaysOnAreaStr.split(",");
            String operator = this.mSIMRecords.getOperatorNumeric();
            int length = areaArray.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (areaArray[i].equals(operator)) {
                    Settings.System.putInt(this.mContext.getContentResolver(), "power_saving_on", 0);
                    Settings.System.putInt(this.mContext.getContentResolver(), "whether_data_alwayson_init", 1);
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void log(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public void custLoadCardSpecialFile(int fileid) {
        if (fileid != EF_OCSGL) {
            Rlog.d(LOG_TAG, "no fileid found for load");
        } else if (mIsSupportCsgSearch) {
            log("=csg= fetchSimRecords => CSG ... ");
            if (this.mSIMRecords != null) {
                this.mSIMRecords.mFh.loadEFLinearFixedAll(EF_OCSGL, this.custHandlerEx.obtainMessage(1));
            } else {
                log("IccRecords is null !!! ");
            }
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
                    SIMRecords sIMRecords = this.mSIMRecords;
                    log("LGU SIMRecords: Refresh_card_type card_type = " + 3);
                    SystemProperties.set("gsm.sim.card.type", String.valueOf(3));
                    return;
                }
                String countryCode = this.mSIMRecords.mImsi.substring(0, 5);
                if (countryCode.substring(0, 5).equals("45006")) {
                    SIMRecords sIMRecords2 = this.mSIMRecords;
                    card_type = 0;
                } else if (countryCode.substring(0, 3).equals("450")) {
                    SIMRecords sIMRecords3 = this.mSIMRecords;
                    card_type = 1;
                } else {
                    SIMRecords sIMRecords4 = this.mSIMRecords;
                    card_type = 2;
                }
            }
            SystemProperties.set("gsm.sim.card.type", String.valueOf(card_type));
            log("LGU SIMRecords: card_type = " + card_type);
            log("LGU SIMRecords: Refresh_card_type exit ");
        }
    }

    public boolean isHwCustDataRoamingOpenArea() {
        int dataRoaming = Settings.Global.getInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(0), 0);
        int dataRoamingSim2 = Settings.Global.getInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(1), 0);
        boolean isCustRoamingOpenArea = dataRoaming == 1;
        boolean isCustRoamingOpenAreaSIM2 = dataRoamingSim2 == 1;
        if (TelephonyManager.getDefault() != null && !TelephonyManager.getDefault().isMultiSimEnabled()) {
            log("single sim, isCustRoamingOpenArea:" + isCustRoamingOpenArea);
            return isCustRoamingOpenArea;
        } else if (TelephonyManager.getDefault() == null || !TelephonyManager.getDefault().isMultiSimEnabled()) {
            return false;
        } else {
            if (this.mSIMRecords != null && this.mSIMRecords.getSlotId() == 0) {
                log("multi sim, isCustRoamingOpenArea:" + isCustRoamingOpenArea);
                return isCustRoamingOpenArea;
            } else if (this.mSIMRecords == null || 1 != this.mSIMRecords.getSlotId()) {
                log("isHwCustDataRoamingOpenArea: invalid slotId");
                return false;
            } else {
                log("multi sim,isCustRoamingOpenAreaSIM2:" + isCustRoamingOpenAreaSIM2);
                return isCustRoamingOpenAreaSIM2;
            }
        }
    }
}
