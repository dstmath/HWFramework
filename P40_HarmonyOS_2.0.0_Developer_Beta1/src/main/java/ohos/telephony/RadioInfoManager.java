package ohos.telephony;

import java.util.List;
import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.MessageParcel;

public class RadioInfoManager {
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "RadioInfoManager");
    private static volatile RadioInfoManager instance;
    private final Context context;
    private final TelephonyProxy telephonyProxy = TelephonyProxy.getInstance();

    private RadioInfoManager(Context context2) {
        this.context = context2;
    }

    public static RadioInfoManager getInstance(Context context2) {
        if (instance == null) {
            synchronized (RadioInfoManager.class) {
                if (instance == null) {
                    instance = new RadioInfoManager(context2);
                }
            }
        }
        return instance;
    }

    public int getRadioTech(int i) {
        return this.telephonyProxy.getRadioTech(i);
    }

    public int getPsRadioTech(int i) {
        return this.telephonyProxy.getPsRadioTech(i);
    }

    public int getCsRadioTech(int i) {
        return this.telephonyProxy.getCsRadioTech(i);
    }

    public String getIsoCountryCodeForNetwork(int i) {
        return this.telephonyProxy.getIsoCountryCodeForNetwork(i);
    }

    public NetworkState getNetworkState(int i) {
        return this.telephonyProxy.getNetworkState(i);
    }

    public String getPlmnNumeric(int i) {
        return this.telephonyProxy.getPlmnNumeric(i);
    }

    public String getOperatorName(int i) {
        return this.telephonyProxy.getOperatorName(i);
    }

    public boolean isRoaming(int i) {
        return this.telephonyProxy.isRoaming(i);
    }

    @SystemApi
    public List<CellInformation> getCellInfoList() {
        return this.telephonyProxy.getCellInfoList();
    }

    public List<SignalInformation> getSignalInfoList(int i) {
        return this.telephonyProxy.getSignalInfoList(i);
    }

    public String getUniqueDeviceId(int i) {
        return this.telephonyProxy.getUniqueDeviceId(i);
    }

    public String getImei(int i) {
        return this.telephonyProxy.getImei(i);
    }

    public String getImeiSv(int i) {
        return this.telephonyProxy.getImeiSv(i);
    }

    public String getTypeAllocationCode(int i) {
        return this.telephonyProxy.getTypeAllocationCode(i);
    }

    public String getMeid(int i) {
        return this.telephonyProxy.getMeid(i);
    }

    public String getManufacturerCode(int i) {
        return this.telephonyProxy.getManufacturerCode(i);
    }

    public int getPrimarySlotId() {
        return this.telephonyProxy.getPrimarySlotId();
    }

    public boolean isNrSupported() {
        return this.telephonyProxy.isNrSupported();
    }

    @SystemApi
    public int getNrOptionMode() {
        return this.telephonyProxy.getNrOptionMode();
    }

    public boolean isNsaState() {
        return this.telephonyProxy.isNsaState();
    }

    public void addObserver(RadioStateObserver radioStateObserver, int i) {
        if (radioStateObserver != null && i != 0) {
            this.telephonyProxy.addObserver(radioStateObserver.slotId, radioStateObserver.callback, getCallingPackageName(), i);
        }
    }

    public void removeObserver(RadioStateObserver radioStateObserver) {
        if (radioStateObserver != null) {
            this.telephonyProxy.removeObserver(radioStateObserver.slotId, radioStateObserver.callback, getCallingPackageName());
        }
    }

    /* access modifiers changed from: package-private */
    public NetworkState createNetworkStateFromObserverParcel(MessageParcel messageParcel) {
        return this.telephonyProxy.createNetworkStateFromObserverParcel(messageParcel);
    }

    /* access modifiers changed from: package-private */
    public List<SignalInformation> createSignalInfoFromObserverParcel(MessageParcel messageParcel) {
        return this.telephonyProxy.createSignalInfoFromObserverParcel(messageParcel);
    }

    /* access modifiers changed from: package-private */
    @SystemApi
    public List<CellInformation> createCellInfoFromObserverParcel(MessageParcel messageParcel) {
        return this.telephonyProxy.createCellInfoFromObserverParcel(messageParcel);
    }

    private String getCallingPackageName() {
        Context context2 = this.context;
        return context2 != null ? context2.getBundleName() : "";
    }

    public String getRadioTechName(int i) {
        return this.telephonyProxy.getRadioTechName(i);
    }

    public int getNetworkSelectionMode(int i) {
        return this.telephonyProxy.getNetworkSelectionMode(i);
    }

    @SystemApi
    public boolean setNetworkSelectionMode(int i, int i2, NetworkInformation networkInformation, boolean z) {
        return this.telephonyProxy.setNetworkSelectionMode(i, i2, networkInformation, z);
    }

    @SystemApi
    public NetworkSearchResult getNetworkSearchInformation(int i) {
        return this.telephonyProxy.getNetworkSearchInformation(i);
    }
}
