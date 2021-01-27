package ohos.telephony;

import ohos.app.Context;
import ohos.hiviewdfx.HiLogLabel;

public class SimInfoManager {
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "SimInfoManager");
    private static volatile SimInfoManager instance;
    private final Context context;
    private final TelephonyProxy telephonyProxy = TelephonyProxy.getInstance();

    private SimInfoManager(Context context2) {
        this.context = context2;
    }

    public static SimInfoManager getInstance(Context context2) {
        if (instance == null) {
            synchronized (SimInfoManager.class) {
                if (instance == null) {
                    instance = new SimInfoManager(context2);
                }
            }
        }
        return instance;
    }

    public boolean isSupportMultiSim() {
        return this.telephonyProxy.isSupportMultiSim();
    }

    public int getMaxSimCount() {
        return this.telephonyProxy.getMaxSimCount();
    }

    public String getIsoCountryCodeForSim(int i) {
        return this.telephonyProxy.getIsoCountryCodeForSim(i);
    }

    public String getSimOperatorNumeric(int i) {
        return this.telephonyProxy.getSimOperatorNumeric(i);
    }

    public String getSimSpn(int i) {
        return this.telephonyProxy.getSimSpnName(i);
    }

    public String getSimIccId(int i) {
        return this.telephonyProxy.getSimIccId(i);
    }

    public String getSimTelephoneNumber(int i) {
        return this.telephonyProxy.getSimTelephoneNumber(i);
    }

    public String getSimGid1(int i) {
        return this.telephonyProxy.getSimGid1(i);
    }

    public int getSimState(int i) {
        return this.telephonyProxy.getSimState(i);
    }

    public boolean hasSimCard(int i) {
        return this.telephonyProxy.hasSimCard(i);
    }

    public String getSimTeleNumberIdentifier(int i) {
        return this.telephonyProxy.getSimTeleNumberIdentifier(i);
    }

    public String getVoiceMailNumber(int i) {
        return this.telephonyProxy.getVoiceMailNumber(i);
    }

    public int getVoiceMailCount(int i) {
        return this.telephonyProxy.getVoiceMailCount(i);
    }

    public String getVoiceMailIdentifier(int i) {
        return this.telephonyProxy.getVoiceMailIdentifier(i);
    }

    public boolean isSimActive(int i) {
        return this.telephonyProxy.isSimActive(i);
    }

    public int getDefaultVoiceSlotId() {
        return this.telephonyProxy.getDefaultVoiceSlotId();
    }

    public boolean hasOperatorPrivileges(int i) {
        return this.telephonyProxy.hasOperatorPrivileges(i);
    }
}
