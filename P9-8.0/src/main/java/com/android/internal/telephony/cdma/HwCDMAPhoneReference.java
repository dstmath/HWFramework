package com.android.internal.telephony.cdma;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.HwTelephony.NumMatchs;
import android.provider.HwTelephony.VirtualNets;
import android.provider.Settings.System;
import android.provider.Telephony.GlobalMatchs;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.android.ims.ImsException;
import com.android.internal.telephony.AbstractGsmCdmaPhone.CDMAPhoneReference;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwPhoneReferenceBase;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccRecords;

public class HwCDMAPhoneReference extends HwPhoneReferenceBase implements CDMAPhoneReference {
    private static final int ECC_NOCARD_INDEX = 4;
    private static final int ECC_WITHCARD_INDEX = 3;
    private static final String LOG_TAG = "HwCDMAPhoneReference";
    private static final int MEID_LENGTH = 14;
    private static final int NAME_INDEX = 1;
    private static final int NUMERIC_INDEX = 2;
    private static final String PROPERTY_GLOBAL_FORCE_TO_SET_ECC = "ril.force_to_set_ecc";
    private static CDMAPhoneUtils cdmaPhoneUtils = new CDMAPhoneUtils();
    private int mLteReleaseVersion;
    private String mPESN;
    private GsmCdmaPhone mPhone;
    private int mPhoneId = 0;
    private final PhoneStateListener mPhoneStateListener;
    private int mSlotId = 0;
    private TelephonyManager mTelephonyManager;
    private String preOperatorNumeric = "";
    private String subTag;

    public HwCDMAPhoneReference(GsmCdmaPhone cdmaPhone) {
        super(cdmaPhone);
        this.mPhone = cdmaPhone;
        this.subTag = "HwCDMAPhoneReference[" + this.mPhone.getPhoneId() + "]";
        this.mPhoneId = this.mPhone.getPhoneId();
        this.mSlotId = SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhoneId);
        this.mTelephonyManager = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        this.mPhoneStateListener = new PhoneStateListener(Integer.valueOf(this.mSlotId)) {
            public void onServiceStateChanged(ServiceState serviceState) {
                String hplmn = null;
                if (HwCDMAPhoneReference.this.mPhone.mIccRecords.get() != null) {
                    hplmn = ((IccRecords) HwCDMAPhoneReference.this.mPhone.mIccRecords.get()).getOperatorNumeric();
                }
                if (TelephonyManager.getDefault().getCurrentPhoneType(HwCDMAPhoneReference.this.mSlotId) != 2 || !SystemProperties.getBoolean("ro.config.hw_eccNumUseRplmn", false)) {
                    return;
                }
                if (TelephonyManager.getDefault().isNetworkRoaming(HwCDMAPhoneReference.this.mSlotId)) {
                    HwCDMAPhoneReference.this.globalEccCustom(serviceState.getOperatorNumeric());
                } else if (hplmn != null) {
                    HwCDMAPhoneReference.this.globalEccCustom(hplmn);
                }
            }
        };
        startListen();
    }

    public String getMeid() {
        logd("[HwCDMAPhoneReference]getMeid() = xxxxxx");
        return cdmaPhoneUtils.getMeid(this.mPhone);
    }

    public String getPesn() {
        return this.mPESN;
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        logd("some apk registerForLineControlInfo");
        this.mPhone.mCT.registerForLineControlInfo(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        logd("some apk unregisterForLineControlInfo");
        this.mPhone.mCT.unregisterForLineControlInfo(h);
    }

    public void startListen() {
        this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
    }

    public void afterHandleMessage(Message msg) {
        logd("handleMessage what = " + msg.what);
        switch (msg.what) {
            case 1:
                logd("[HwGSMPhoneReference]Phone.EVENT_RADIO_AVAILABLE");
                logd("Radio available, get lte release version");
                this.mPhone.mCi.getLteReleaseVersion(this.mPhone.obtainMessage(108));
                return;
            case 21:
                logd("handleMessage EVENT_GET_DEVICE_IDENTITY_DONE");
                if (cdmaPhoneUtils.getMeid(this.mPhone) != null && cdmaPhoneUtils.getMeid(this.mPhone).length() > 14) {
                    cdmaPhoneUtils.setMeid(this.mPhone, cdmaPhoneUtils.getMeid(this.mPhone).substring(cdmaPhoneUtils.getMeid(this.mPhone).length() - 14));
                }
                AsyncResult ar = msg.obj;
                if (ar.exception == null) {
                    String[] respId = ar.result;
                    if (respId != null) {
                        logd("handleMessage respId.length = " + respId.length);
                    }
                    if (respId != null && respId.length >= 4) {
                        logd("handleMessage mPESN = xxxxxx");
                        this.mPESN = respId[2];
                        return;
                    }
                    return;
                }
                return;
            default:
                logd("unhandle event");
                return;
        }
    }

    public void closeRrc() {
        try {
            this.mPhone.mCi.getClass().getMethod("closeRrc", new Class[0]).invoke(this.mPhone.mCi, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchVoiceCallBackgroundState(int state) {
        this.mPhone.mCT.switchVoiceCallBackgroundState(state);
    }

    public void riseCdmaCutoffFreq(boolean on) {
        this.mPhone.mCi.riseCdmaCutoffFreq(on, null);
    }

    public boolean beforeHandleMessage(Message msg) {
        boolean msgHandled;
        logd("beforeHandleMessage what = " + msg.what);
        switch (msg.what) {
            case 108:
                logd("onGetLteReleaseVersionDone:");
                AsyncResult ar = msg.obj;
                msgHandled = true;
                if (ar.exception == null) {
                    int[] resultint = ar.result;
                    if (resultint.length != 0) {
                        logd("onGetLteReleaseVersionDone: result=" + resultint[0]);
                        switch (resultint[0]) {
                            case 0:
                                this.mLteReleaseVersion = 0;
                                break;
                            case 1:
                                this.mLteReleaseVersion = 1;
                                break;
                            case 3:
                                this.mLteReleaseVersion = 3;
                                break;
                            default:
                                this.mLteReleaseVersion = 0;
                                break;
                        }
                    }
                }
                logd("Error in get lte release version:" + ar.exception);
                break;
                break;
            case 111:
                logd("beforeHandleMessage handled->EVENT_SET_MODE_TO_AUTO ");
                msgHandled = true;
                this.mPhone.setNetworkSelectionModeAutomatic(null);
                break;
            case HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE /*1000*/:
                logd("beforeHandleMessage handled->RETRY_GET_DEVICE_ID ");
                msgHandled = true;
                if (msg.arg2 != 2) {
                    logd("EVENT_RETRY_GET_DEVICE_ID msg.arg2:" + msg.arg2 + ", error!!");
                    break;
                }
                logd("start retry get DEVICE_ID_MASK_ALL");
                this.mPhone.mCi.getDeviceIdentity(this.mPhone.obtainMessage(21, msg.arg1, 0, null));
                break;
            default:
                return super.beforeHandleMessage(msg);
        }
        return msgHandled;
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public void setLTEReleaseVersion(int state, Message response) {
        this.mPhone.mCi.setLTEReleaseVersion(state, response);
    }

    public int getLteReleaseVersion() {
        logd("getLteReleaseVersion: " + this.mLteReleaseVersion);
        return this.mLteReleaseVersion;
    }

    public boolean isChinaTelecom(int slotId) {
        return HwTelephonyManagerInner.getDefault().isChinaTelecom(slotId);
    }

    public void selectNetworkManually(OperatorInfo network, Message response) {
        loge("selectNetworkManually: not possible in CDMA");
        if (response != null) {
            AsyncResult.forMessage(response).exception = new CommandException(Error.REQUEST_NOT_SUPPORTED);
            response.sendToTarget();
        }
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        loge("method setNetworkSelectionModeAutomatic is NOT supported in CDMA!");
        if (response != null) {
            Rlog.e(LOG_TAG, "setNetworkSelectionModeAutomatic: not possible in CDMA- Posting exception");
            AsyncResult.forMessage(response).exception = new CommandException(Error.REQUEST_NOT_SUPPORTED);
            response.sendToTarget();
        }
    }

    public void registerForHWBuffer(Handler h, int what, Object obj) {
        this.mPhone.mCi.registerForHWBuffer(h, what, obj);
    }

    public void unregisterForHWBuffer(Handler h) {
        this.mPhone.mCi.unregisterForHWBuffer(h);
    }

    public void sendHWSolicited(Message reqMsg, int event, byte[] reqData) {
        this.mPhone.mCi.sendHWBufferSolicited(reqMsg, event, reqData);
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        return this.mPhone.mCi.cmdForECInfo(event, action, buf);
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        this.mPhone.mCi.notifyCellularCommParaReady(paratype, pathtype, response);
    }

    private void logd(String msg) {
        Rlog.d(this.subTag, msg);
    }

    private void loge(String msg) {
        Rlog.e(this.subTag, msg);
    }

    public void processEccNumber(ServiceStateTracker cSST) {
        if (SystemProperties.getBoolean("ro.config.hw_globalEcc", false) && SystemProperties.getBoolean("ro.config.hw_eccNumUseRplmn", false)) {
            logd("EVENT_RUIM_RECORDS_LOADED!!!!");
            SystemProperties.set(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "ruim_present");
            String hplmn = this.mPhone.getOperatorNumeric();
            boolean isRoaming = cSST.mSS.getRoaming();
            String rplmn = cSST.mSS.getOperatorNumeric();
            if (TextUtils.isEmpty(hplmn)) {
                logd("received EVENT_SIM_RECORDS_LOADED but not hplmn !!!!");
            } else if (isRoaming) {
                globalEccCustom(rplmn);
            } else {
                globalEccCustom(hplmn);
            }
        }
    }

    public void globalEccCustom(String operatorNumeric) {
        int isValid;
        Object ecclist_withcard = null;
        Object ecclist_nocard = null;
        String forceEccState = SystemProperties.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: operator numeric = " + operatorNumeric + "; preOperatorNumeric = " + this.preOperatorNumeric + ";forceEccState  = " + forceEccState);
        if (TextUtils.isEmpty(operatorNumeric)) {
            isValid = 0;
        } else {
            isValid = operatorNumeric.equals(this.preOperatorNumeric) ? "invalid".equals(forceEccState) ^ 1 : 1;
        }
        if (isValid != 0) {
            this.preOperatorNumeric = operatorNumeric;
            SystemProperties.set(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
            if (HwTelephonyFactory.getHwPhoneManager().isSupportEccFormVirtualNet()) {
                ecclist_withcard = HwTelephonyFactory.getHwPhoneManager().getVirtualNetEccWihCard(this.mPhoneId);
                ecclist_nocard = HwTelephonyFactory.getHwPhoneManager().getVirtualNetEccNoCard(this.mPhoneId);
                logd("try to get Ecc form virtualNet ecclist_withcard=" + ecclist_withcard + " ecclist_nocard=" + ecclist_nocard);
            }
            String custEcc = getCustEccList(operatorNumeric);
            if (!TextUtils.isEmpty(custEcc)) {
                String[] custEccArray = custEcc.split(":");
                if (custEccArray.length == 3 && custEccArray[0].equals(operatorNumeric) && (TextUtils.isEmpty(custEccArray[1]) ^ 1) != 0 && (TextUtils.isEmpty(custEccArray[2]) ^ 1) != 0) {
                    ecclist_withcard = custEccArray[1];
                    ecclist_nocard = custEccArray[2];
                }
            }
            if (ecclist_withcard == null) {
                Cursor cursor = this.mPhone.getContext().getContentResolver().query(GlobalMatchs.CONTENT_URI, new String[]{"_id", NumMatchs.NAME, "numeric", VirtualNets.ECC_WITH_CARD, VirtualNets.ECC_NO_CARD}, "numeric=\"" + operatorNumeric + "\"", null, NumMatchs.DEFAULT_SORT_ORDER);
                if (cursor == null) {
                    logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: No matched emergency numbers in db.");
                    this.mPhone.mCi.requestSetEmergencyNumbers("", "");
                    return;
                }
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        ecclist_withcard = cursor.getString(3);
                        ecclist_nocard = cursor.getString(4);
                        cursor.moveToNext();
                    }
                } catch (RuntimeException ex) {
                    logd("[SLOT" + this.mPhoneId + "]globalEccCustom: global version cause exception!" + ex.toString());
                } finally {
                    cursor.close();
                }
            }
            logd("[SLOT" + this.mPhoneId + "]GECC-globalEccCustom: ecc_withcard = " + ecclist_withcard + ", ecc_nocard = " + ecclist_nocard);
            if ((TextUtils.isEmpty(ecclist_withcard) ? TextUtils.isEmpty(ecclist_nocard) ^ 1 : 1) != 0) {
                this.mPhone.mCi.requestSetEmergencyNumbers(ecclist_withcard, ecclist_nocard);
            } else {
                this.mPhone.mCi.requestSetEmergencyNumbers("", "");
            }
        }
    }

    private String getCustEccList(String operatorNumeric) {
        Object custEccList = null;
        String matchEccList = "";
        try {
            custEccList = System.getString(this.mPhone.getContext().getContentResolver(), "hw_cust_emergency_nums");
        } catch (RuntimeException e) {
            Rlog.e(LOG_TAG, "Failed to load vmNum from SettingsEx", e);
        }
        if (TextUtils.isEmpty(custEccList) || TextUtils.isEmpty(operatorNumeric)) {
            return matchEccList;
        }
        String[] custEccListItems = custEccList.split(";");
        for (int i = 0; i < custEccListItems.length; i++) {
            String[] custItem = custEccListItems[i].split(":");
            if (custItem.length == 3 && custItem[0].equals(operatorNumeric)) {
                matchEccList = custEccListItems[i];
                break;
            }
        }
        return matchEccList;
    }

    public void updateWfcMode(Context context, boolean roaming, int subId) throws ImsException {
        HwImsManagerInner.updateWfcMode(context, roaming, subId);
    }

    public boolean isDualImsAvailable() {
        return HwImsManagerInner.isDualImsAvailable();
    }
}
