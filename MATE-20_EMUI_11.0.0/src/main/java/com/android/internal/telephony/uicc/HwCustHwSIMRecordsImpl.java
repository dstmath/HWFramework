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
import huawei.cust.HwCfgFilePolicy;
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
    private static boolean mIsSaveCardTypeLGU = SystemProperties.getBoolean("ro.config.save_cardtype_cust", false);
    private static boolean mIsSupportCsgSearch = SystemProperties.getBoolean("ro.config.att.csg", false);
    private Handler custHandlerEx = new Handler() {
        /* class com.android.internal.telephony.uicc.HwCustHwSIMRecordsImpl.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (HwCustHwSIMRecordsImpl.this.mIccRecordsInner == null || !HwCustHwSIMRecordsImpl.this.mIccRecordsInner.judgeIfDestroyed()) {
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
                                HwCustHwSIMRecordsImpl.this.mEfOcsgl = null;
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
                                            HwCustHwSIMRecordsImpl.this.mEfOcsgl = Arrays.copyOf(item, item.length);
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
                                    HwCustHwSIMRecordsImpl.this.mEfOcsgl = new byte[0];
                                    HwCustHwSIMRecordsImpl.this.log("=csg= SIMRecords:  OCSGL is empty. ");
                                }
                                HwCustHwSIMRecordsImpl.this.mSIMRecords.notifyRegisterForCsgRecordsLoaded();
                            }
                        }
                        HwCustHwSIMRecordsImpl hwCustHwSIMRecordsImpl2 = HwCustHwSIMRecordsImpl.this;
                        hwCustHwSIMRecordsImpl2.log("=csg= EVENT_GET_OCSGL_DONE exception = " + ar.exception);
                        HwCustHwSIMRecordsImpl.this.mEfOcsgl = null;
                    }
                    if (((HwCustHwSIMRecordsImpl) HwCustHwSIMRecordsImpl.this).mIccRecordsInner == null || 0 == 0) {
                        return;
                    }
                } catch (RuntimeException exc) {
                    HwCustHwSIMRecordsImpl hwCustHwSIMRecordsImpl3 = HwCustHwSIMRecordsImpl.this;
                    hwCustHwSIMRecordsImpl3.log("Exception parsing SIM record:" + exc);
                    if (HwCustHwSIMRecordsImpl.this.mIccRecordsInner == null || 0 == 0) {
                        return;
                    }
                } catch (Throwable th) {
                    if (!(HwCustHwSIMRecordsImpl.this.mIccRecordsInner == null || 0 == 0)) {
                        HwCustHwSIMRecordsImpl.this.mIccRecordsInner.onRecordLoadedHw();
                    }
                    throw th;
                }
                HwCustHwSIMRecordsImpl.this.mIccRecordsInner.onRecordLoadedHw();
                return;
            }
            Rlog.e(HwCustHwSIMRecordsImpl.LOG_TAG, "Received message " + msg + "[" + msg.what + "]  while being destroyed. Ignoring.");
        }
    };
    boolean iccidChanged = false;
    private byte[] mEfOcsgl = null;

    public HwCustHwSIMRecordsImpl(IIccRecordsInner iccRecordsInner, IHwIccRecordsEx obj, Context mConText) {
        super(iccRecordsInner, obj, mConText);
    }

    public void setVmPriorityModeInClaro(IVoiceMailConstantsInner vmConfig) {
        if (this.mContext != null && this.mIccRecordsInner != null) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            int VoicemailPriorityMode = SettingsEx.Systemex.getInt(contentResolver, "voicemailPrioritySpecial_" + this.mIccRecordsInner.getOperatorNumeric(), 0);
            log("The SIM card MCCMNC = " + this.mIccRecordsInner.getOperatorNumeric());
            if (VoicemailPriorityMode != 0 && vmConfig != null) {
                vmConfig.setVoicemailInClaro(VoicemailPriorityMode);
                log("VoicemailPriorityMode from custom = " + VoicemailPriorityMode);
            }
        }
    }

    public static String getCustDataRoamingSettings(int slotId) {
        return "has_cust_data_roaming_set" + slotId;
    }

    public void refreshDataRoamingSettings() {
        String oldIccid;
        if (this.mSIMRecords != null) {
            SharedPreferences sp = this.mContext.getSharedPreferences("DataRoamingSettingIccid", 0);
            String iccid = this.mIccRecordsInner.getIccIdHw();
            if (!TextUtils.isEmpty(iccid)) {
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    oldIccid = sp.getString(LAST_ICCID + this.mIccRecordsInner.getSlotId(), null);
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
                if (!iccid.equals(oldIccid)) {
                    this.iccidChanged = HWDBG;
                } else {
                    this.iccidChanged = false;
                }
                if (this.iccidChanged) {
                    try {
                        SharedPreferences.Editor editor = sp.edit();
                        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                            editor.putString(LAST_ICCID + this.mIccRecordsInner.getSlotId(), new String(Base64.encode(iccid.getBytes("utf-8"), 0), "utf-8"));
                        } else {
                            editor.putString(LAST_ICCID, new String(Base64.encode(iccid.getBytes("utf-8"), 0), "utf-8"));
                        }
                        editor.commit();
                    } catch (UnsupportedEncodingException e2) {
                        Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not UnsupportedEncodingException");
                    }
                    int slotId = this.mIccRecordsInner.getSlotId();
                    Boolean isDataRoaming = (Boolean) HwCfgFilePolicy.getValue("data_roaming_switch", slotId, Boolean.class);
                    if (isDataRoaming == null) {
                        Settings.Global.putInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(slotId), 0);
                        return;
                    }
                    boolean isDataRoamingValue = isDataRoaming.booleanValue();
                    if (TelephonyManager.getDefault() != null && !TelephonyManager.getDefault().isMultiSimEnabled()) {
                        if (isDataRoamingValue) {
                            log("refreshDataRoamingSettings(): setting data roaming to true");
                            Settings.System.putInt(this.mContext.getContentResolver(), "roaming_saving_on", 1);
                            Settings.Global.putInt(this.mContext.getContentResolver(), "data_roaming", 1);
                        } else {
                            log("refreshDataRoamingSettings(): setting data roaming to false");
                            Settings.Global.putInt(this.mContext.getContentResolver(), "data_roaming", 0);
                        }
                        Settings.Global.putInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(slotId), 1);
                    } else if (TelephonyManager.getDefault() != null && TelephonyManager.getDefault().isMultiSimEnabled()) {
                        if (isDataRoamingValue) {
                            if (slotId == 0) {
                                log("refreshDataRoamingSettings(): setting data roaming to true for SIM1");
                                Settings.Global.putInt(this.mContext.getContentResolver(), "data_roaming", 1);
                            } else if (slotId == 1) {
                                log("refreshDataRoamingSettings(): setting data roaming to true for SIM2");
                                Settings.Global.putInt(this.mContext.getContentResolver(), DATA_ROAMING_SIM2, 1);
                            }
                        } else if (slotId == 0) {
                            log("refreshDataRoamingSettings(): setting data roaming to false for SIM1");
                            Settings.Global.putInt(this.mContext.getContentResolver(), "data_roaming", 0);
                        } else if (slotId == 1) {
                            log("refreshDataRoamingSettings(): setting data roaming to false for SIM2");
                            Settings.Global.putInt(this.mContext.getContentResolver(), DATA_ROAMING_SIM2, 0);
                        }
                        Settings.Global.putInt(this.mContext.getContentResolver(), getCustDataRoamingSettings(slotId), 1);
                    }
                } else {
                    Rlog.d(LOG_TAG, "refreshDataRoamingSettings(): iccid not changed" + this.iccidChanged);
                }
            }
        }
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
            String operator = this.mIccRecordsInner.getOperatorNumeric();
            for (String area : areaArray) {
                if (area.equals(operator)) {
                    Settings.System.putInt(this.mContext.getContentResolver(), "power_saving_on", 0);
                    Settings.System.putInt(this.mContext.getContentResolver(), "whether_data_alwayson_init", 1);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public void custLoadCardSpecialFile(int fileid) {
        if (fileid != EF_OCSGL) {
            Rlog.d(LOG_TAG, "no fileid found for load");
        } else if (mIsSupportCsgSearch) {
            log("=csg= fetchSimRecords => CSG ... ");
            if (this.mIccRecordsInner == null || this.mIccRecordsInner.getIccFileHandler() == null) {
                log("IccRecords is null !!! ");
            } else {
                this.mIccRecordsInner.getIccFileHandler().loadEFLinearFixedAll((int) EF_OCSGL, this.custHandlerEx.obtainMessage(1));
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
        this.mSIMRecords.setCsglexist((boolean) HWDBG);
        byte[] bArr = this.mEfOcsgl;
        if (bArr.length > 0) {
            return Arrays.copyOf(bArr, bArr.length);
        }
        return new byte[0];
    }

    public void refreshCardType() {
        if (mIsSaveCardTypeLGU) {
            int card_type = -1;
            String imsi = this.mIccRecordsInner.getImsiHw();
            if (imsi != null) {
                if (imsi.substring(0, 6).equals("450069")) {
                    log("LGU SIMRecords: Refresh_card_type card_type = 3");
                    SystemProperties.set("gsm.sim.card.type", String.valueOf(3));
                    return;
                }
                String countryCode = imsi.substring(0, 5);
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
            if (this.mSIMRecords != null && this.mIccRecordsInner.getSlotId() == 0) {
                log("multi sim, isCustRoamingOpenArea:" + isCustRoamingOpenArea);
                return isCustRoamingOpenArea;
            } else if (this.mSIMRecords == null || 1 != this.mIccRecordsInner.getSlotId()) {
                log("isHwCustDataRoamingOpenArea: invalid slotId");
                return false;
            } else {
                log("multi sim,isCustRoamingOpenAreaSIM2:" + isCustRoamingOpenAreaSIM2);
                return isCustRoamingOpenAreaSIM2;
            }
        }
    }
}
