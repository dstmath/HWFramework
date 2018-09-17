package com.android.internal.telephony;

import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;

public class HwPhone {
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = null;
    private static final String LOG_TAG = "HwPhone";
    private Phone mPhone;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues() {
        if (-com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues;
        }
        int[] iArr = new int[AppType.values().length];
        try {
            iArr[AppType.APPTYPE_CSIM.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppType.APPTYPE_ISIM.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppType.APPTYPE_RUIM.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppType.APPTYPE_SIM.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppType.APPTYPE_UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppType.APPTYPE_USIM.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = iArr;
        return iArr;
    }

    public HwPhone(Phone phoneproxy) {
        this.mPhone = phoneproxy;
        log("init HwPhone mPhone = " + this.mPhone);
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    public String getMeid() {
        return this.mPhone.getMeid();
    }

    public String getPesn() {
        return this.mPhone.getPesn();
    }

    public String getNVESN() {
        return this.mPhone.getNVESN();
    }

    public void closeRrc() {
        this.mPhone.closeRrc();
    }

    public boolean isCDMAPhone() {
        if (this.mPhone == null || (((GsmCdmaPhone) this.mPhone).isPhoneTypeGsm() ^ 1) == 0) {
            return false;
        }
        return true;
    }

    public void setDefaultMobileEnable(boolean enabled) {
        this.mPhone.mDcTracker.setEnabledPublic(0, enabled);
    }

    public void setDataEnabledWithoutPromp(boolean enabled) {
        this.mPhone.mDcTracker.setDataEnabled(enabled);
    }

    public void setDataRoamingEnabledWithoutPromp(boolean enabled) {
        this.mPhone.mDcTracker.setDataRoamingEnabled(enabled);
    }

    public DataState getDataConnectionState() {
        return this.mPhone.getDataConnectionState();
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        this.mPhone.setPreferredNetworkType(networkType, response);
    }

    public void getPreferredNetworkType(Message response) {
        this.mPhone.getPreferredNetworkType(response);
    }

    public String getImei() {
        return this.mPhone.getImei();
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    public int getHwPhoneType() {
        Rlog.d(LOG_TAG, "[enter]getHwPhoneType");
        return this.mPhone.getPhoneType();
    }

    public String getCdmaGsmImsi() {
        Rlog.d(LOG_TAG, "getCdmaGsmImsi: in HuaweiPhoneService");
        return this.mPhone.getCdmaGsmImsi();
    }

    public int getUiccCardType() {
        Rlog.d(LOG_TAG, "[enter]getUiccCardType");
        return this.mPhone.getUiccCardType();
    }

    public CellLocation getCellLocation() {
        Rlog.d(LOG_TAG, "[enter]getUiccCardType");
        return this.mPhone.getCellLocation();
    }

    public String getCdmaMlplVersion() {
        Rlog.d(LOG_TAG, "getCdmaMlplVersion: in HuaweiPhoneService");
        return this.mPhone.getCdmaMlplVersion();
    }

    public String getCdmaMsplVersion() {
        Rlog.d(LOG_TAG, "getCdmaMsplVersion: in HuaweiPhoneService");
        return this.mPhone.getCdmaMsplVersion();
    }

    public void testVoiceLoopBack(int mode) {
        this.mPhone.testVoiceLoopBack(mode);
    }

    public boolean setISMCOEX(String setISMCoex) {
        return this.mPhone.setISMCOEX(setISMCoex);
    }

    public int getUiccAppType() {
        switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues()[this.mPhone.getCurrentUiccAppType().ordinal()]) {
            case 1:
                return 4;
            case 2:
                return 5;
            case 3:
                return 3;
            case 4:
                return 1;
            case 5:
                return 2;
            default:
                return 0;
        }
    }

    public boolean isRadioAvailable() {
        return this.mPhone.isRadioAvailable();
    }

    public void setImsSwitch(boolean value) {
        this.mPhone.setImsSwitch(value);
    }

    public boolean getImsSwitch() {
        return this.mPhone.getImsSwitch();
    }

    public void setImsDomainConfig(int domainType) {
        this.mPhone.setImsDomainConfig(domainType);
    }

    public void handleMapconImsaReq(byte[] Msg) {
        this.mPhone.handleMapconImsaReq(Msg);
    }

    public void getImsDomain(Message Msg) {
        this.mPhone.getImsDomain(Msg);
    }

    public void handleUiccAuth(int auth_type, byte[] rand, byte[] auth, Message Msg) {
        this.mPhone.handleUiccAuth(auth_type, rand, auth, Msg);
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        try {
            return this.mPhone.cmdForECInfo(event, action, buf);
        } catch (Exception ex) {
            Rlog.e(LOG_TAG, "cmdForECInfo fail:" + ex);
            return false;
        }
    }

    public void requestForECInfo(Message msg, int event, byte[] buf) {
        this.mPhone.sendHWSolicited(msg, event, buf);
    }

    public boolean isCtSimCard() {
        String iccId = this.mPhone.getIccSerialNumber();
        if (iccId == null || iccId.length() < 7) {
            return false;
        }
        return HwAllInOneController.isCTCard(iccId.substring(0, 7));
    }
}
