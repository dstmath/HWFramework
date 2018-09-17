package com.android.internal.telephony;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.os.WorkSource;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import com.android.internal.telephony.PhoneConstants.DataState;
import java.util.List;

public interface PhoneInternalInterface extends AbstractPhoneInternalInterface {
    public static final int BM_10_800M_2 = 15;
    public static final int BM_4_450M = 10;
    public static final int BM_7_700M2 = 12;
    public static final int BM_8_1800M = 13;
    public static final int BM_9_900M = 14;
    public static final int BM_AUS2_BAND = 5;
    public static final int BM_AUS_BAND = 4;
    public static final int BM_AWS = 17;
    public static final int BM_CELL_800 = 6;
    public static final int BM_EURO_BAND = 1;
    public static final int BM_EURO_PAMR = 16;
    public static final int BM_IMT2000 = 11;
    public static final int BM_JPN_BAND = 3;
    public static final int BM_JTACS = 8;
    public static final int BM_KOREA_PCS = 9;
    public static final int BM_NUM_BAND_MODES = 19;
    public static final int BM_PCS = 7;
    public static final int BM_UNSPECIFIED = 0;
    public static final int BM_US_2500M = 18;
    public static final int BM_US_BAND = 2;
    public static final int CDMA_OTA_PROVISION_STATUS_A_KEY_EXCHANGED = 2;
    public static final int CDMA_OTA_PROVISION_STATUS_COMMITTED = 8;
    public static final int CDMA_OTA_PROVISION_STATUS_IMSI_DOWNLOADED = 6;
    public static final int CDMA_OTA_PROVISION_STATUS_MDN_DOWNLOADED = 5;
    public static final int CDMA_OTA_PROVISION_STATUS_NAM_DOWNLOADED = 4;
    public static final int CDMA_OTA_PROVISION_STATUS_OTAPA_ABORTED = 11;
    public static final int CDMA_OTA_PROVISION_STATUS_OTAPA_STARTED = 9;
    public static final int CDMA_OTA_PROVISION_STATUS_OTAPA_STOPPED = 10;
    public static final int CDMA_OTA_PROVISION_STATUS_PRL_DOWNLOADED = 7;
    public static final int CDMA_OTA_PROVISION_STATUS_SPC_RETRIES_EXCEEDED = 1;
    public static final int CDMA_OTA_PROVISION_STATUS_SPL_UNLOCKED = 0;
    public static final int CDMA_OTA_PROVISION_STATUS_SSD_UPDATED = 3;
    public static final int CDMA_RM_AFFILIATED = 1;
    public static final int CDMA_RM_ANY = 2;
    public static final int CDMA_RM_HOME = 0;
    public static final int CDMA_SUBSCRIPTION_NV = 1;
    public static final int CDMA_SUBSCRIPTION_RUIM_SIM = 0;
    public static final int CDMA_SUBSCRIPTION_UNKNOWN = -1;
    public static final boolean DEBUG_PHONE = true;
    public static final String FEATURE_ENABLE_CBS = "enableCBS";
    public static final String FEATURE_ENABLE_DUN = "enableDUN";
    public static final String FEATURE_ENABLE_DUN_ALWAYS = "enableDUNAlways";
    public static final String FEATURE_ENABLE_EMERGENCY = "enableEmergency";
    public static final String FEATURE_ENABLE_FOTA = "enableFOTA";
    public static final String FEATURE_ENABLE_HIPRI = "enableHIPRI";
    public static final String FEATURE_ENABLE_IMS = "enableIMS";
    public static final String FEATURE_ENABLE_MMS = "enableMMS";
    public static final String FEATURE_ENABLE_SUPL = "enableSUPL";
    public static final int NT_MODE_CDMA = 4;
    public static final int NT_MODE_CDMA_NO_EVDO = 5;
    public static final int NT_MODE_EVDO_NO_CDMA = 6;
    public static final int NT_MODE_GLOBAL = 7;
    public static final int NT_MODE_GSM_ONLY = 1;
    public static final int NT_MODE_GSM_UMTS = 3;
    public static final int NT_MODE_LTE_CDMA_AND_EVDO = 8;
    public static final int NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA = 10;
    public static final int NT_MODE_LTE_GSM_WCDMA = 9;
    public static final int NT_MODE_LTE_ONLY = 11;
    public static final int NT_MODE_LTE_TDSCDMA = 15;
    public static final int NT_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA = 22;
    public static final int NT_MODE_LTE_TDSCDMA_GSM = 17;
    public static final int NT_MODE_LTE_TDSCDMA_GSM_WCDMA = 20;
    public static final int NT_MODE_LTE_TDSCDMA_WCDMA = 19;
    public static final int NT_MODE_LTE_WCDMA = 12;
    public static final int NT_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA = 21;
    public static final int NT_MODE_TDSCDMA_GSM = 16;
    public static final int NT_MODE_TDSCDMA_GSM_WCDMA = 18;
    public static final int NT_MODE_TDSCDMA_ONLY = 13;
    public static final int NT_MODE_TDSCDMA_WCDMA = 14;
    public static final int NT_MODE_WCDMA_ONLY = 2;
    public static final int NT_MODE_WCDMA_PREF = 0;
    public static final int PREFERRED_CDMA_SUBSCRIPTION = 1;
    public static final int PREFERRED_NT_MODE = RILConstants.PREFERRED_NETWORK_MODE;
    public static final String REASON_APN_CHANGED = "apnChanged";
    public static final String REASON_APN_FAILED = "apnFailed";
    public static final String REASON_APN_SWITCHED = "apnSwitched";
    public static final String REASON_CARRIER_ACTION_DISABLE_METERED_APN = "carrierActionDisableMeteredApn";
    public static final String REASON_CARRIER_CHANGE = "carrierChange";
    public static final String REASON_CDMA_DATA_ATTACHED = "cdmaDataAttached";
    public static final String REASON_CDMA_DATA_DETACHED = "cdmaDataDetached";
    public static final String REASON_CONNECTED = "connected";
    public static final String REASON_DATA_ATTACHED = "dataAttached";
    public static final String REASON_DATA_DEPENDENCY_MET = "dependencyMet";
    public static final String REASON_DATA_DEPENDENCY_UNMET = "dependencyUnmet";
    public static final String REASON_DATA_DETACHED = "dataDetached";
    public static final String REASON_DATA_DISABLED = "dataDisabled";
    public static final String REASON_DATA_ENABLED = "dataEnabled";
    public static final String REASON_DATA_SPECIFIC_DISABLED = "specificDisabled";
    public static final String REASON_IWLAN_AVAILABLE = "iwlanAvailable";
    public static final String REASON_LOST_DATA_CONNECTION = "lostDataConnection";
    public static final String REASON_NW_TYPE_CHANGED = "nwTypeChanged";
    public static final String REASON_PDP_RESET = "pdpReset";
    public static final String REASON_PS_RESTRICT_DISABLED = "psRestrictDisabled";
    public static final String REASON_PS_RESTRICT_ENABLED = "psRestrictEnabled";
    public static final String REASON_RADIO_TURNED_OFF = "radioTurnedOff";
    public static final String REASON_RESTORE_DEFAULT_APN = "restoreDefaultApn";
    public static final String REASON_ROAMING_OFF = "roamingOff";
    public static final String REASON_ROAMING_ON = "roamingOn";
    public static final String REASON_SIM_LOADED = "simLoaded";
    public static final String REASON_SIM_NOT_READY = "simNotReady";
    public static final String REASON_SINGLE_PDN_ARBITRATION = "SinglePdnArbitration";
    public static final String REASON_VOICE_CALL_ENDED = "2GVoiceCallEnded";
    public static final String REASON_VOICE_CALL_STARTED = "2GVoiceCallStarted";
    public static final String REASON_VP_ENDED = "vpEnded";
    public static final String REASON_VP_STARTED = "vpStarted";
    public static final int TTY_MODE_FULL = 1;
    public static final int TTY_MODE_HCO = 2;
    public static final int TTY_MODE_OFF = 0;
    public static final int TTY_MODE_VCO = 3;

    public enum DataActivityState {
        NONE,
        DATAIN,
        DATAOUT,
        DATAINANDOUT,
        DORMANT
    }

    public enum SuppService {
        UNKNOWN,
        SWITCH,
        SEPARATE,
        TRANSFER,
        CONFERENCE,
        REJECT,
        HANGUP,
        RESUME,
        HOLD
    }

    void acceptCall(int i) throws CallStateException;

    void activateCellBroadcastSms(int i, Message message);

    boolean canConference();

    boolean canTransfer();

    void clearDisconnected();

    void conference() throws CallStateException;

    Connection dial(String str, int i) throws CallStateException;

    Connection dial(String str, UUSInfo uUSInfo, int i, Bundle bundle) throws CallStateException;

    void disableLocationUpdates();

    void enableLocationUpdates();

    void explicitCallTransfer() throws CallStateException;

    void getAvailableNetworks(Message message);

    Call getBackgroundCall();

    void getCallForwardingOption(int i, Message message);

    void getCallWaiting(Message message);

    String getCdmaGsmImsi();

    String getCdmaMlplVersion();

    String getCdmaMsplVersion();

    void getCellBroadcastSmsConfig(Message message);

    CellLocation getCellLocation(WorkSource workSource);

    DataActivityState getDataActivityState();

    void getDataCallList(Message message);

    DataState getDataConnectionState(String str);

    boolean getDataEnabled();

    boolean getDataRoamingEnabled();

    String getDeviceId();

    String getDeviceSvn();

    String getEsn();

    Call getForegroundCall();

    String getGroupIdLevel1();

    String getGroupIdLevel2();

    IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager();

    String getImei();

    String getLine1AlphaTag();

    String getLine1Number();

    String getMeid();

    boolean getMute();

    void getOutgoingCallerIdDisplay(Message message);

    List<? extends MmiCode> getPendingMmiCodes();

    Call getRingingCall();

    ServiceState getServiceState();

    String getSubscriberId();

    int getUiccCardType();

    String getVoiceMailAlphaTag();

    String getVoiceMailNumber();

    boolean handleInCallMmiCommands(String str) throws CallStateException;

    boolean handlePinMmi(String str);

    boolean handleUssdRequest(String str, ResultReceiver resultReceiver) throws CallStateException;

    void registerForSuppServiceNotification(Handler handler, int i, Object obj);

    void rejectCall() throws CallStateException;

    void sendDtmf(char c);

    void sendUssdResponse(String str);

    void setCallForwardingOption(int i, int i2, String str, int i3, Message message);

    void setCallWaiting(boolean z, Message message);

    void setCellBroadcastSmsConfig(int[] iArr, Message message);

    void setDataEnabled(boolean z);

    void setDataRoamingEnabled(boolean z);

    boolean setLine1Number(String str, String str2, Message message);

    void setMute(boolean z);

    void setOutgoingCallerIdDisplay(int i, Message message);

    void setRadioPower(boolean z);

    void setRadioPower(boolean z, Message message);

    void setVoiceMailNumber(String str, String str2, Message message);

    void startDtmf(char c);

    void stopDtmf();

    void switchHoldingAndActive() throws CallStateException;

    void testVoiceLoopBack(int i);

    void unregisterForSuppServiceNotification(Handler handler);

    void updateServiceLocation();

    void getNeighboringCids(Message response, WorkSource workSource) {
    }
}
