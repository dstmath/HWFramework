package ohos.telephony;

import java.util.ArrayList;
import java.util.List;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;

public interface ITelephony extends IRemoteBroker {
    void addObserver(int i, IRemoteObject iRemoteObject, String str, int i2);

    void addObserver(int i, IRadioStateObserver iRadioStateObserver, String str, int i2);

    List<CellInformation> createCellInfoFromObserverParcel(MessageParcel messageParcel);

    NetworkState createNetworkStateFromObserverParcel(MessageParcel messageParcel);

    List<SignalInformation> createSignalInfoFromObserverParcel(MessageParcel messageParcel);

    void enableCellularData(int i, boolean z);

    void enableCellularDataRoaming(int i, boolean z);

    List<CellInformation> getCellInfoList();

    int getCellularDataFlowType();

    int getCellularDataState(int i);

    int getCsRadioTech(int i);

    int getDefaultCellularDataSlotId();

    int getDefaultSmsSlotId();

    int getDefaultVoiceSlotId();

    String getImei(int i);

    String getImeiSv(int i);

    String getImsShortMessageFormat();

    String getIsoCountryCodeForNetwork(int i);

    String getManufacturerCode(int i);

    int getMasterSlotId();

    String getMeid(int i);

    NetworkState getNetworkState(int i);

    int getNrOptionMode();

    int getPsRadioTech(int i);

    int getRadioTech(int i);

    String getRadioTechName(int i);

    int getRadioTechnologyType(int i);

    List<SignalInformation> getSignalInfoList(int i);

    String getSimGid1(int i);

    String getSimIccId(int i);

    int getSimState(int i);

    String getSimTeleNumberIdentifier(int i);

    String getSimTelephoneNumber(int i);

    String getTypeAllocationCode(int i);

    String getUniqueDeviceId(int i);

    int getVoiceMailCount(int i);

    String getVoiceMailIdentifier(int i);

    String getVoiceMailNumber(int i);

    boolean hasSimCard(int i);

    void inputDialerSpecialCode(String str);

    boolean isCellularDataEnabled(int i);

    boolean isCellularDataRoamingEnabled(int i);

    boolean isEmergencyPhoneNumber(String str);

    boolean isImsSmsSupported();

    boolean isNsaState();

    boolean isSimActive(int i);

    boolean isVideoCallingEnabled();

    void removeObserver(int i, IRemoteObject iRemoteObject, String str);

    void removeObserver(int i, IRadioStateObserver iRadioStateObserver, String str);

    void sendMultipartTextMessage(String str, String str2, ArrayList<String> arrayList);

    void sendSmsMessage(String str, String str2, String str3);

    void setDefaultCellularDataSlotId(int i);

    boolean setDefaultSmsSlotId(int i);
}
