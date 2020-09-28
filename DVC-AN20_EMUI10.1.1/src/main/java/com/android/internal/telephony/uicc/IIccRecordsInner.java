package com.android.internal.telephony.uicc;

import android.content.ContentResolver;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.OperatorInfo;
import java.util.ArrayList;

public interface IIccRecordsInner {
    public static final int INVAILID_EVENT_ID = -1;

    void addRecordsToLoadNum();

    void disableRequestIccRecords();

    String getActingHplmn();

    AdnRecordCache getAdnCache();

    boolean getCsglexist();

    String[] getEhplmnOfSim();

    String[] getEhplmns();

    String getEons();

    ArrayList<OperatorInfo> getEonsForAvailableNetworks(ArrayList<OperatorInfo> arrayList);

    byte[] getGID1();

    IccFileHandler getIccFileHandler();

    String getIccIdHw();

    String getImsiHw();

    boolean getImsiReady();

    int getMncLength();

    byte[] getOcsgl();

    String getOperatorNumeric();

    String getOperatorNumericEx(ContentResolver contentResolver, String str);

    int getSlotId();

    String getVoiceMailNumber();

    void handleFileUpdateHw(int i);

    boolean has3Gphonebook();

    boolean isCspPlmnEnabled();

    boolean isEonsDisabled();

    boolean isGetPBRDone();

    boolean isHwCustDataRoamingOpenArea();

    boolean judgeIfDestroyed();

    void notifyRegisterForCsgRecordsLoaded();

    void notifyRegisterForFdnRecordsLoaded();

    void notifyRegisterForIccRefresh();

    void notifyRegisterForRecordsEvents(int i);

    void notifyRegisterLoadIccID(Object obj, Object obj2, Throwable th);

    void onRecordLoadedHw();

    void recordsRequired();

    void registerForCsgRecordsLoaded(Handler handler, int i, Object obj);

    void registerForIccRefresh(Handler handler, int i, Object obj);

    void registerForLoadIccID(Handler handler, int i, Object obj);

    void setCsglexist(boolean z);

    void setImsiHw(String str);

    void setMdnNumber(String str, String str2, Message message);

    void setMncLength(int i);

    void setSystemPropertyHw(String str, String str2);

    void setVoiceFixedFlag(boolean z);

    void setVoiceMailNumber(String str);

    void setVoiceMailTag(String str);

    void unRegisterForIccRefresh(Handler handler);

    void unRegisterForLoadIccID(Handler handler);

    void unregisterForCsgRecordsLoaded(Handler handler);

    boolean updateEons(String str, int i);

    default void setMdn(String mdn) {
    }

    default void setVoiceMailByCountryHw(String spn) {
    }

    default int getEventIdFromMap(String event) {
        return -1;
    }

    default void initEventIdMap() {
    }

    default void fetchSimRecordsHw() {
    }

    default void handleMessageEx(Message msg) {
    }

    default VoiceMailConstants getVmConfig() {
        return null;
    }

    default String getCdmaGsmImsi() {
        return null;
    }

    default void setCdmaGsmImsi(String imsiCdma) {
    }

    default String getRUIMOperatorNumeric() {
        return null;
    }
}
