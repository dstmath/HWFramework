package com.android.internal.telephony.cdma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Handler;
import android.os.Message;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPhoneReferenceBase;
import com.android.internal.telephony.IHwGsmCdmaPhoneInner;
import com.android.internal.telephony.IServiceStateTrackerInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.TelephonyEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.CommandExceptionEx;
import com.huawei.internal.telephony.PhoneExt;
import huawei.cust.HwCfgFilePolicy;

public class HwCDMAPhoneReference extends HwPhoneReferenceBase {
    private static final int ECC_NOCARD_INDEX = 4;
    private static final int ECC_WITHCARD_INDEX = 3;
    private static final int EVENT_RADIO_ON = 5;
    private static final String LOG_TAG = "HwCDMAPhoneReference";
    private static final int MEID_LENGTH = 14;
    private static final int MLPL_INDEX = 0;
    private static final int MLPL_MSPL_ARRAY_LENGTH = 2;
    private static final int MSPL_INDEX = 1;
    private static final int NAME_INDEX = 1;
    private static final int NUMERIC_INDEX = 2;
    private static final String PROPERTY_GLOBAL_FORCE_TO_SET_ECC = "ril.force_to_set_ecc";
    private int mLteReleaseVersion;
    private String mPESN;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.cdma.HwCDMAPhoneReference.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && "com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(intent.getAction())) {
                int mainSlot = HwTelephonyManager.getDefault().getDefault4GSlotId();
                int radioState = HwCDMAPhoneReference.this.mPhoneExt.getCi().getRadioState();
                boolean switchDone = intent.getIntExtra("HW_SWITCH_SLOT_STEP", 1) == 1 && mainSlot == HwCDMAPhoneReference.this.mPhoneId;
                HwCDMAPhoneReference hwCDMAPhoneReference = HwCDMAPhoneReference.this;
                hwCDMAPhoneReference.logd("ACTION_HW_SWITCH_SLOT_DONE radio state:" + radioState + " switch done:" + switchDone);
                if (switchDone && HwCDMAPhoneReference.this.mPhoneExt.getPhoneType() != 1 && radioState == 1) {
                    HwCDMAPhoneReference.this.mPhoneExt.getCi().getLteReleaseVersion(HwCDMAPhoneReference.this.mPhoneExt.obtainMessage(108));
                }
            }
        }
    };
    private String preOperatorNumeric = BuildConfig.FLAVOR;
    private String subTag = ("HwCDMAPhoneReference[" + this.mPhoneId + "]");

    public HwCDMAPhoneReference(IHwGsmCdmaPhoneInner hwGsmCdmaPhoneInner, PhoneExt phoneExt) {
        super(hwGsmCdmaPhoneInner, phoneExt);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public String getMeid() {
        logd("[HwCDMAPhoneReference]getMeid() = xxxxxx");
        return this.mPhoneExt.getMeid();
    }

    public String getPesn() {
        return this.mPESN;
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        logd("some apk registerForLineControlInfo");
        this.mHwGsmCdmaPhoneInner.gsmCdmaPhoneRegisterForLineControlInfo(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        logd("some apk unregisterForLineControlInfo");
        this.mHwGsmCdmaPhoneInner.gsmCdmaPhoneUnRegisterForLineControlInfo(h);
    }

    public void afterHandleMessage(Message msg) {
        logd("handleMessage what = " + msg.what);
        int i = msg.what;
        if (i == 1 || i == 5) {
            logd("Radio available or on, get lte release version");
            this.mPhoneExt.getCi().getLteReleaseVersion(this.mPhoneExt.obtainMessage(108));
        } else if (i != 21) {
            logd("unhandle event");
        } else {
            logd("handleMessage EVENT_GET_DEVICE_IDENTITY_DONE");
            if (this.mPhoneExt.getMeid() != null && this.mPhoneExt.getMeid().length() > MEID_LENGTH) {
                this.mPhoneExt.setMeid(this.mPhoneExt.getMeid().substring(this.mPhoneExt.getMeid().length() - MEID_LENGTH));
            }
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar != null && ar.getException() == null) {
                String[] respId = (String[]) ar.getResult();
                if (respId != null) {
                    logd("handleMessage respId.length = " + respId.length);
                }
                if (respId != null && respId.length >= 4) {
                    logd("handleMessage mPESN = xxxxxx");
                    this.mPESN = respId[2];
                }
            }
        }
    }

    public void closeRrc() {
        this.mPhoneExt.getCi().closeRrc();
    }

    public void switchVoiceCallBackgroundState(int state) {
        this.mHwGsmCdmaPhoneInner.gsmCdmaPhoneSwitchVoiceCallBackgroundState(state);
    }

    public void riseCdmaCutoffFreq(boolean on) {
        this.mPhoneExt.getCi().riseCdmaCutoffFreq(on, (Message) null);
    }

    @Override // com.android.internal.telephony.HwPhoneReferenceBase
    public boolean beforeHandleMessage(Message msg) {
        logd("beforeHandleMessage what = " + msg.what);
        int i = msg.what;
        if (i == 108) {
            handleGetLteReleaseVersionDone(msg);
            return true;
        } else if (i == 111) {
            logd("beforeHandleMessage handled->EVENT_SET_MODE_TO_AUTO ");
            this.mPhoneExt.setNetworkSelectionModeAutomatic((Message) null);
            return true;
        } else if (i != 1000) {
            return super.beforeHandleMessage(msg);
        } else {
            handleRetryGetDeviceId(msg);
            return true;
        }
    }

    private void handleRetryGetDeviceId(Message msg) {
        logd("beforeHandleMessage handled->RETRY_GET_DEVICE_ID ");
        if (msg.arg2 == 2) {
            logd("start retry get DEVICE_ID_MASK_ALL");
            this.mPhoneExt.getCi().getDeviceIdentity(this.mPhoneExt.obtainMessage(21, msg.arg1, 0, (Object) null));
            return;
        }
        logd("EVENT_RETRY_GET_DEVICE_ID msg.arg2:" + msg.arg2 + ", error!!");
    }

    private void handleGetLteReleaseVersionDone(Message msg) {
        logd("onGetLteReleaseVersionDone:");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            logd("Error in get lte release version.");
            return;
        }
        int[] resultint = (int[]) ar.getResult();
        if (resultint == null) {
            logd("Error in get lte release version: null resultint");
        } else if (resultint.length != 0) {
            logd("onGetLteReleaseVersionDone: result=" + resultint[0]);
            this.mLteReleaseVersion = resultint[0];
        }
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public void setLTEReleaseVersion(int state, Message response) {
        this.mPhoneExt.getCi().setLTEReleaseVersion(state, response);
    }

    public int getLteReleaseVersion() {
        logd("getLteReleaseVersion: " + this.mLteReleaseVersion);
        return this.mLteReleaseVersion;
    }

    public boolean isChinaTelecom(int slotId) {
        return HwTelephonyManagerInner.getDefault().isChinaTelecom(slotId);
    }

    public void selectNetworkManually(Message response) {
        loge("selectNetworkManually: not possible in CDMA");
        if (response != null) {
            AsyncResultEx.forMessage(response).setException(new CommandExceptionEx(CommandExceptionEx.Error.REQUEST_NOT_SUPPORTED));
            response.sendToTarget();
        }
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        loge("method setNetworkSelectionModeAutomatic is NOT supported in CDMA!");
        if (response != null) {
            RlogEx.e(LOG_TAG, "setNetworkSelectionModeAutomatic: not possible in CDMA- Posting exception");
            AsyncResultEx.forMessage(response).setException(new CommandExceptionEx(CommandExceptionEx.Error.REQUEST_NOT_SUPPORTED));
            response.sendToTarget();
        }
    }

    public void registerForHWBuffer(Handler h, int what, Object obj) {
        this.mPhoneExt.getCi().registerForHWBuffer(h, what, obj);
    }

    public void unregisterForHWBuffer(Handler h) {
        this.mPhoneExt.getCi().unregisterForHWBuffer(h);
    }

    public void sendHWSolicited(Message reqMsg, int event, byte[] reqData) {
        this.mPhoneExt.getCi().sendHWBufferSolicited(reqMsg, event, reqData);
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        return this.mPhoneExt.getCi().cmdForECInfo(event, action, buf);
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        this.mPhoneExt.getCi().notifyCellularCommParaReady(paratype, pathtype, response);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String msg) {
        RlogEx.i(this.subTag, msg);
    }

    private void loge(String msg) {
        RlogEx.e(this.subTag, msg);
    }

    public void processEccNumber(IServiceStateTrackerInner cSST) {
        boolean isUseRplmn = false;
        if (SystemPropertiesEx.getBoolean("ro.config.hw_globalEcc", false) && SystemPropertiesEx.getBoolean("ro.config.hw_eccNumUseRplmn", false)) {
            logd("EVENT_RUIM_RECORDS_LOADED!!!!");
            SystemPropertiesEx.set(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "ruim_present");
            String hplmn = this.mHwGsmCdmaPhoneInner.getOperatorNumericHw();
            boolean isRegHomeState = isRegisteredHomeNetworkForTransportType(this.mPhoneId, 1);
            String rplmn = getRplmn();
            if (!TextUtils.isEmpty(rplmn) && !isRegHomeState) {
                isUseRplmn = true;
            }
            if (TextUtils.isEmpty(hplmn)) {
                logd("received EVENT_SIM_RECORDS_LOADED but not hplmn !!!!");
            } else if (isUseRplmn) {
                logd("processEccNumber: Use Rplmn, isRegHomeState= " + isRegHomeState + ", rplmn=" + rplmn);
                globalEccCustom(rplmn);
            } else {
                globalEccCustom(hplmn);
            }
        }
    }

    @Override // com.android.internal.telephony.HwPhoneReferenceBase
    public void globalEccCustom(String operatorNumeric) {
        String eccListWithCard = null;
        String eccListNoCard = null;
        String forceEccState = SystemPropertiesEx.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        boolean isRegStateChanged = this.mNetworkRegState != getCombinedNetworkRegState(this.mPhoneId);
        logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: operator numeric = " + operatorNumeric + "; preOperatorNumeric = " + this.preOperatorNumeric + ";forceEccState  = " + forceEccState + ", isRegStateChanged=" + isRegStateChanged);
        if (!TextUtils.isEmpty(operatorNumeric) && (!operatorNumeric.equals(this.preOperatorNumeric) || !"invalid".equals(forceEccState) || isRegStateChanged)) {
            this.preOperatorNumeric = operatorNumeric;
            SystemPropertiesEx.set(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
            if ((TextUtils.isEmpty(getRplmn()) || isRegisteredHomeNetworkForTransportType(this.mPhoneId, 1)) && virtualNetEccFormCarrier(this.mPhoneId)) {
                int slotId = SubscriptionManager.getSlotIndex(this.mPhoneId);
                try {
                    eccListWithCard = (String) HwCfgFilePolicy.getValue("virtual_ecclist_withcard", slotId, String.class);
                    eccListNoCard = (String) HwCfgFilePolicy.getValue("virtual_ecclist_nocard", slotId, String.class);
                    logd("globalEccCustom: Registered Home State, Use VirtualNet Ecc eccListWithCard=" + eccListWithCard + ",ecclistNocard=" + eccListNoCard);
                } catch (ClassCastException e) {
                    logd("Failed to get ecclist in carrier ClassCastException");
                } catch (Exception e2) {
                    logd("Failed to get ecclist in carrier");
                }
            }
            String custEcc = getCustEccList(operatorNumeric);
            if (!TextUtils.isEmpty(custEcc)) {
                String[] custEccArray = custEcc.split(":");
                if (custEccArray.length == 3 && custEccArray[0].equals(operatorNumeric) && !TextUtils.isEmpty(custEccArray[1]) && !TextUtils.isEmpty(custEccArray[2])) {
                    eccListWithCard = custEccArray[1];
                    eccListNoCard = custEccArray[2];
                }
            }
            if (eccListWithCard == null) {
                Cursor cursor = getCursor(operatorNumeric);
                if (cursor == null) {
                    logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: No matched emergency numbers in db.");
                    this.mPhoneExt.getCi().requestSetEmergencyNumbers(BuildConfig.FLAVOR, BuildConfig.FLAVOR);
                    return;
                }
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        eccListWithCard = cursor.getString(3);
                        eccListNoCard = cursor.getString(4);
                        cursor.moveToNext();
                    }
                } catch (IllegalStateException e3) {
                    logd("[SLOT" + this.mPhoneId + "]globalEccCustom: global version cause illegalstateexception!");
                } catch (RuntimeException e4) {
                    logd("[SLOT" + this.mPhoneId + "]globalEccCustom: global version cause exception!");
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
                cursor.close();
            }
            logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: ecc_withcard = " + eccListWithCard + ", ecc_nocard = " + eccListNoCard);
            boolean validEcclist = !TextUtils.isEmpty(eccListWithCard) || !TextUtils.isEmpty(eccListNoCard);
            String eccListWithCard2 = eccListWithCard != null ? eccListWithCard : BuildConfig.FLAVOR;
            String eccListNoCard2 = eccListNoCard != null ? eccListNoCard : BuildConfig.FLAVOR;
            if (validEcclist) {
                this.mPhoneExt.getCi().requestSetEmergencyNumbers(eccListWithCard2, eccListNoCard2);
            } else {
                this.mPhoneExt.getCi().requestSetEmergencyNumbers(BuildConfig.FLAVOR, BuildConfig.FLAVOR);
            }
        }
    }

    private Cursor getCursor(String operatorNumeric) {
        try {
            return this.mPhoneExt.getContext().getContentResolver().query(TelephonyEx.GlobalMatchs.CONTENT_URI, new String[]{"_id", HwTelephony.NumMatchs.NAME, "numeric", HwTelephony.VirtualNets.ECC_WITH_CARD, HwTelephony.VirtualNets.ECC_NO_CARD}, "numeric= ?", new String[]{operatorNumeric}, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        } catch (SQLException e) {
            loge("Query CONTENT_URI error.");
            return null;
        }
    }

    private String getCustEccList(String operatorNumeric) {
        String custEccList = null;
        try {
            custEccList = Settings.System.getString(this.mPhoneExt.getContext().getContentResolver(), "hw_cust_emergency_nums");
        } catch (IllegalArgumentException e) {
            RlogEx.e(LOG_TAG, "Failed to load vmNum from SettingsEx IllegalArgumentException");
        } catch (RuntimeException e2) {
            RlogEx.e(LOG_TAG, "Failed to load vmNum from SettingsEx");
        }
        if (TextUtils.isEmpty(custEccList) || TextUtils.isEmpty(operatorNumeric)) {
            return BuildConfig.FLAVOR;
        }
        String[] custEccListItems = custEccList.split(";");
        for (int i = 0; i < custEccListItems.length; i++) {
            String[] custItem = custEccListItems[i].split(":");
            if (custItem.length == 3 && custItem[0].equals(operatorNumeric)) {
                return custEccListItems[i];
            }
        }
        return BuildConfig.FLAVOR;
    }

    public boolean isDualImsAvailable() {
        return HwImsManagerInner.isDualImsAvailable();
    }

    public String getCdmaMlplVersion(String mlplVersion) {
        String realMlplVersion = null;
        int slotId = this.mPhoneExt.getPhoneId();
        if (HwModemCapability.isCapabilitySupport(9) && TelephonyManagerEx.getDefault().getSimState(slotId) == 5) {
            realMlplVersion = this.mPhoneExt.getCi().getHwCDMAMlplVersion();
        }
        if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            String strMlplMsplver = SystemPropertiesEx.get("ril.csim.mlpl_mspl_ver" + this.mPhoneExt.getPhoneId());
            if (strMlplMsplver != null) {
                String[] arrayMlplMspl = strMlplMsplver.split(",");
                if (arrayMlplMspl.length >= 2) {
                    realMlplVersion = arrayMlplMspl[0];
                } else {
                    realMlplVersion = null;
                }
            }
        }
        if (realMlplVersion == null) {
            realMlplVersion = mlplVersion;
        }
        logd("getMlplVersion: mlplVersion=" + realMlplVersion);
        return realMlplVersion;
    }

    public String getCdmaMsplVersion(String msplVersion) {
        String realMsplVersion = null;
        int slotId = this.mPhoneExt.getPhoneId();
        if (HwModemCapability.isCapabilitySupport(9) && TelephonyManagerEx.getDefault().getSimState(slotId) == 5) {
            realMsplVersion = this.mPhoneExt.getCi().getHwCDMAMsplVersion();
        }
        if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            String strMlplMsplver = SystemPropertiesEx.get("ril.csim.mlpl_mspl_ver" + this.mPhoneExt.getPhoneId());
            if (strMlplMsplver != null) {
                String[] arrayMlplMspl = strMlplMsplver.split(",");
                if (arrayMlplMspl.length >= 2) {
                    realMsplVersion = arrayMlplMspl[1];
                } else {
                    realMsplVersion = null;
                }
            }
        }
        if (realMsplVersion == null) {
            realMsplVersion = msplVersion;
        }
        logd("getMsplVersion: msplVersion=" + realMsplVersion);
        return realMsplVersion;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.HwPhoneReferenceBase
    public boolean isCurrentPhoneType() {
        return TelephonyManagerEx.getCurrentPhoneType(this.mSubId) == 2;
    }
}
