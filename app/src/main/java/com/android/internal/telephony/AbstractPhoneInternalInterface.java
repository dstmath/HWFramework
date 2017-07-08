package com.android.internal.telephony;

import android.os.Handler;
import android.os.Message;

public interface AbstractPhoneInternalInterface {
    public static final String FEATURE_ENABLE_BIP0 = "enableBIP0";
    public static final String FEATURE_ENABLE_BIP1 = "enableBIP1";
    public static final String FEATURE_ENABLE_BIP2 = "enableBIP2";
    public static final String FEATURE_ENABLE_BIP3 = "enableBIP3";
    public static final String FEATURE_ENABLE_BIP4 = "enableBIP4";
    public static final String FEATURE_ENABLE_BIP5 = "enableBIP5";
    public static final String FEATURE_ENABLE_BIP6 = "enableBIP6";
    public static final String FEATURE_ENABLE_XCAP = "enableXCAP";
    public static final String REASON_CELL_LOCATION_CHANGE = "cellLocationChanged";
    public static final String REASON_NO_RETRY_AFTER_DISCONNECT = "noRetryAfterDisconnect";
    public static final String REASON_USER_DATA_ENABLED = "userDataEnabled";

    void changeBarringPassword(String str, String str2, Message message);

    void closeRrc();

    void getCallbarringOption(String str, String str2, Message message);

    int getDataRoamingScope();

    boolean getImsSwitch();

    int getLteReleaseVersion();

    String getMeid();

    String getNVESN();

    void getPOLCapabilty(Message message);

    String getPesn();

    void getPreferedOperatorList(Message message);

    String getSpeechInfoCodec();

    boolean isMmiCode(String str);

    boolean isSupportCFT();

    void registerForUnsolSpeechInfo(Handler handler, int i, Object obj);

    void riseCdmaCutoffFreq(boolean z);

    void selectCsgNetworkManually(Message message);

    void setCallForwardingUncondTimerOption(int i, int i2, int i3, int i4, int i5, int i6, String str, Message message);

    void setCallbarringOption(String str, String str2, boolean z, String str3, Message message);

    boolean setDataRoamingScope(int i);

    boolean setISMCOEX(String str);

    void setImsSwitch(boolean z);

    void setLTEReleaseVersion(boolean z, Message message);

    void setPOLEntry(int i, String str, int i2, Message message);

    void setSpeechInfoCodec(int i);

    void switchVoiceCallBackgroundState(int i);

    void unregisterForUnsolSpeechInfo(Handler handler);
}
