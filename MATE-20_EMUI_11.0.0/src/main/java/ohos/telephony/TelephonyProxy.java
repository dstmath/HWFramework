package ohos.telephony;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.system.Parameters;

/* access modifiers changed from: package-private */
public class TelephonyProxy implements ITelephony {
    private static final int RESULT_PERMISSION_DENY = -2;
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "TelephonyProxy");
    private static final String TELEPHONY_SA_DESCRIPTOR = "OHOS.Telephony.ITelephony";
    private static volatile TelephonyProxy instance = null;
    private final Object lock = new Object();
    private IRemoteObject telephonyRemoteService = null;

    public enum MultiSimVariants {
        DSDS,
        DSDA,
        TSTS,
        UNKNOWN
    }

    @Override // ohos.telephony.ITelephony
    public String getRadioTechName(int i) {
        switch (i) {
            case 1:
                return "GSM";
            case 2:
                return "CDMA - 1xRTT";
            case 3:
                return "WCDMA";
            case 4:
                return "HSPA";
            case 5:
                return "HSPA+";
            case 6:
                return "TD-SCDMA";
            case 7:
                return "CDMA - EvDo";
            case 8:
                return "CDMA - eHRPD";
            case 9:
                return "LTE";
            case 10:
                return "LTE-CA";
            case 11:
                return "IWLAN";
            case 12:
                return "NR";
            default:
                return "UNKNOWN";
        }
    }

    static {
        try {
            System.loadLibrary("ipc_core.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(TAG, "Load library libipc_core.z.so failed.", new Object[0]);
        }
    }

    private TelephonyProxy() {
    }

    public static TelephonyProxy getInstance() {
        if (instance == null) {
            synchronized (TelephonyProxy.class) {
                if (instance == null) {
                    instance = new TelephonyProxy();
                }
            }
        }
        return instance;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        synchronized (this.lock) {
            if (this.telephonyRemoteService != null) {
                return this.telephonyRemoteService;
            }
            this.telephonyRemoteService = SysAbilityManager.checkSysAbility(4001);
            if (this.telephonyRemoteService != null) {
                this.telephonyRemoteService.addDeathRecipient(new TelephonyDeathRecipient(this, null), 0);
            } else {
                HiLog.error(TAG, "getSysAbility(TelephonyService) failed.", new Object[0]);
            }
            return this.telephonyRemoteService;
        }
    }

    private IRemoteObject getTelephonySrvAbility() throws RemoteException {
        return (IRemoteObject) Optional.ofNullable(asObject()).orElseThrow($$Lambda$t9E2a5kBSvCJG3OvOwSmRDhzvos.INSTANCE);
    }

    /* access modifiers changed from: private */
    public class TelephonyDeathRecipient implements IRemoteObject.DeathRecipient {
        private TelephonyDeathRecipient() {
        }

        /* synthetic */ TelephonyDeathRecipient(TelephonyProxy telephonyProxy, AnonymousClass1 r2) {
            this();
        }

        @Override // ohos.rpc.IRemoteObject.DeathRecipient
        public void onRemoteDied() {
            HiLog.warn(TelephonyProxy.TAG, "TelephonyDeathRecipient::onRemoteDied.", new Object[0]);
            synchronized (TelephonyProxy.this.lock) {
                TelephonyProxy.this.telephonyRemoteService = null;
            }
        }
    }

    @Override // ohos.telephony.ITelephony
    public int getRadioTech(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i2 = 0;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return 0;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1, obtain, obtain2, messageOption);
            i2 = obtain2.readInt();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getRadioTech", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }

    @Override // ohos.telephony.ITelephony
    public int getPsRadioTech(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i2 = 0;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return 0;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(2, obtain, obtain2, messageOption);
            i2 = obtain2.readInt();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getPsRadioTech", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }

    @Override // ohos.telephony.ITelephony
    public int getCsRadioTech(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i2 = 0;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return 0;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(3, obtain, obtain2, messageOption);
            i2 = obtain2.readInt();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getCsRadioTech", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }

    @Override // ohos.telephony.ITelephony
    public String getIsoCountryCodeForNetwork(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(4, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getIsoCountryCodeForNetwork", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public NetworkState getNetworkState(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        NetworkState networkState = new NetworkState();
        if (!TelephonyUtils.isValidSlotId(i)) {
            return networkState;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(5, obtain, obtain2, messageOption);
            obtain2.readSequenceable(networkState);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getServiceState", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return networkState;
    }

    @Override // ohos.telephony.ITelephony
    public List<CellInformation> getCellInfoList() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        List<CellInformation> arrayList = new ArrayList<>();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(6, obtain, obtain2, messageOption);
            arrayList = CellInformation.createCellInfoListFromParcel(obtain2);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getCellInfoList", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        HiLog.debug(TAG, "getCellInfoList is: %{private}s", arrayList.toString());
        return arrayList;
    }

    @Override // ohos.telephony.ITelephony
    public List<SignalInformation> getSignalInfoList(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
        obtain.writeInt(i);
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        List<SignalInformation> arrayList = new ArrayList<>();
        if (!TelephonyUtils.isValidSlotId(i)) {
            return arrayList;
        }
        try {
            getTelephonySrvAbility().sendRequest(7, obtain, obtain2, messageOption);
            arrayList = SignalInformation.createSignalInfoListFromParcel(obtain2);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getSignalInfoList", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        HiLog.debug(TAG, "getSignalInfoList is: %{private}s", arrayList.toString());
        return arrayList;
    }

    @Override // ohos.telephony.ITelephony
    public String getUniqueDeviceId(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(8, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getUniqueDeviceId", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public String getImei(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(9, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getImei", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public String getImeiSv(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(10, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getImeiSv", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public String getTypeAllocationCode(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(11, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getTypeAllocationCode", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public String getMeid(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(12, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getTypeAllocationCode", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public String getManufacturerCode(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(13, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getManufacturerCode", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public int getMasterSlotId() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i = 0;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(14, obtain, obtain2, messageOption);
            i = obtain2.readInt();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getMasterSlotId", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i;
    }

    public boolean isNrSupported() {
        String telephonyProperty = TelephonyUtils.getTelephonyProperty(0, TelephonyUtils.PROPERTY_MODEM_CAPABILITY, "false");
        if (telephonyProperty.length() < 8 || (Integer.parseInt(String.valueOf(telephonyProperty.charAt(7)), 16) & 4) == 0) {
            return false;
        }
        return true;
    }

    @Override // ohos.telephony.ITelephony
    public int getNrOptionMode() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i = 0;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(17, obtain, obtain2, messageOption);
            i = obtain2.readInt();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getNrOptionMode", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i;
    }

    @Override // ohos.telephony.ITelephony
    public boolean isNsaState() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(18, obtain, obtain2, messageOption);
            if (obtain2.readInt() != 0) {
                z = true;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to isNsaState", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    public String getPlmnNumeric(int i) {
        if (!TelephonyUtils.isValidSlotId(i)) {
            return "";
        }
        String telephonyProperty = TelephonyUtils.getTelephonyProperty(i, TelephonyUtils.PROPERTY_OPERATOR_PLMN, "");
        HiLog.debug(TAG, "getPlmnNumeric, plmn = %s, slotId = %d", telephonyProperty, Integer.valueOf(i));
        return telephonyProperty;
    }

    public String getOperatorName(int i) {
        if (!TelephonyUtils.isValidSlotId(i)) {
            return "";
        }
        String telephonyProperty = TelephonyUtils.getTelephonyProperty(i, TelephonyUtils.PROPERTY_OPERATOR_NAME, "");
        HiLog.debug(TAG, "getPlmnName, plmnName = %s, slotId = %d", telephonyProperty, Integer.valueOf(i));
        return telephonyProperty;
    }

    public boolean isRoaming(int i) {
        if (!TelephonyUtils.isValidSlotId(i)) {
            return false;
        }
        boolean parseBoolean = Boolean.parseBoolean(TelephonyUtils.getTelephonyProperty(i, TelephonyUtils.PROPERTY_OPERATOR_ISROAMING, "false"));
        HiLog.debug(TAG, "isRoaming, isRoaming = %{public}s, slotId = %d", String.valueOf(parseBoolean), Integer.valueOf(i));
        return parseBoolean;
    }

    public boolean isSupportMultiSim() {
        String str = Parameters.get(TelephonyUtils.PROPERTY_MULTI_SIM_CONFIG, "");
        HiLog.debug(TAG, "isSupportMultiSim, multiSimProp = %s", str);
        return "dsds".equals(str) || "dsda".equals(str) || "tsts".equals(str);
    }

    public MultiSimVariants getMultiSimConfiguration() {
        String str = Parameters.get(TelephonyUtils.PROPERTY_MULTI_SIM_CONFIG, "");
        if ("dsda".equals(str)) {
            return MultiSimVariants.DSDA;
        }
        if ("dsds".equals(str)) {
            return MultiSimVariants.DSDS;
        }
        if ("tsts".equals(str)) {
            return MultiSimVariants.TSTS;
        }
        return MultiSimVariants.UNKNOWN;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.telephony.TelephonyProxy$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$telephony$TelephonyProxy$MultiSimVariants = new int[MultiSimVariants.values().length];

        static {
            try {
                $SwitchMap$ohos$telephony$TelephonyProxy$MultiSimVariants[MultiSimVariants.DSDS.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$telephony$TelephonyProxy$MultiSimVariants[MultiSimVariants.DSDA.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$telephony$TelephonyProxy$MultiSimVariants[MultiSimVariants.TSTS.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public int getMaxSimCount() {
        int i = AnonymousClass1.$SwitchMap$ohos$telephony$TelephonyProxy$MultiSimVariants[getMultiSimConfiguration().ordinal()];
        if (i == 1 || i == 2) {
            return 2;
        }
        return i != 3 ? 1 : 3;
    }

    public String getIsoCountryCodeForSim(int i) {
        if (!TelephonyUtils.isValidSlotId(i)) {
            return "";
        }
        String telephonyProperty = TelephonyUtils.getTelephonyProperty(i, TelephonyUtils.PROPERTY_SIM_ISO_COUNTRY, "");
        HiLog.debug(TAG, "getIsoCountryCodeForSim, SimIsoCountryCode = %s, slotId = %d", telephonyProperty, Integer.valueOf(i));
        return telephonyProperty;
    }

    public String getSimOperatorNumeric(int i) {
        if (!TelephonyUtils.isValidSlotId(i)) {
            return "";
        }
        String telephonyProperty = TelephonyUtils.getTelephonyProperty(i, TelephonyUtils.PROPERTY_SIM_PLMN_NUMERIC, "");
        HiLog.debug(TAG, "getSimOperatorNumeric, SimOperatorNumeric = %s, slotId = %d", telephonyProperty, Integer.valueOf(i));
        return telephonyProperty;
    }

    public String getSimSpnName(int i) {
        if (!TelephonyUtils.isValidSlotId(i)) {
            return "";
        }
        String telephonyProperty = TelephonyUtils.getTelephonyProperty(i, TelephonyUtils.PROPERTY_SIM_SERVICE_PROVIDER_NAME, "");
        HiLog.debug(TAG, "getSimSpnName, spn = %s, slotId = %d", telephonyProperty, Integer.valueOf(i));
        return telephonyProperty;
    }

    @Override // ohos.telephony.ITelephony
    public String getSimIccId(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1002, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getSimIccId", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public String getSimTelephoneNumber(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1003, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getTelephoneNumber", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public String getSimGid1(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1004, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getGid1", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public int getSimState(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i2 = 0;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return 0;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1005, obtain, obtain2, messageOption);
            i2 = obtain2.readInt();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getSimState", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }

    @Override // ohos.telephony.ITelephony
    public boolean hasSimCard(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return false;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1006, obtain, obtain2, messageOption);
            z = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to hasSimCard", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    @Override // ohos.telephony.ITelephony
    public boolean isEmergencyPhoneNumber(String str) {
        boolean z = false;
        if (str == null) {
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeString(str);
            getTelephonySrvAbility().sendRequest(2008, obtain, obtain2, messageOption);
            z = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to check emergency phone number", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    @Override // ohos.telephony.ITelephony
    public String getVoiceMailNumber(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1007, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to get voice mail number", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public boolean setDefaultSmsSlotId(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return false;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(3002, obtain, obtain2, messageOption);
            z = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to setDefaultSmsSlotId", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    @Override // ohos.telephony.ITelephony
    public int getDefaultSmsSlotId() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i = 0;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_GET_DEFAULT_SMS_SLOT_ID, obtain, obtain2, messageOption);
            i = obtain2.readInt();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getDefaultSmsSlotId", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i;
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.telephony.ITelephony
    public String getImsShortMessageFormat() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_GET_IMS_SMS_FORMAT, obtain, obtain2, messageOption);
            String readString = obtain2.readString();
            obtain2.reclaim();
            obtain.reclaim();
            return readString;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getImsShortMessageFormat", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return "unknown";
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    @Override // ohos.telephony.ITelephony
    public boolean isImsSmsSupported() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_IS_IMS_SMS_SUPPORT, obtain, obtain2, messageOption);
            z = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to isImsSmsSupported", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    @Override // ohos.telephony.ITelephony
    public void sendSmsMessage(String str, String str2, String str3) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeString(str);
            obtain.writeString(str2);
            obtain.writeString(str3);
            getTelephonySrvAbility().sendRequest(3001, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to sendSmsMessage", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.telephony.ITelephony
    public void sendMultipartTextMessage(String str, String str2, ArrayList<String> arrayList) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeString(str);
            obtain.writeString(str2);
            if (arrayList == null) {
                obtain.writeInt(-1);
            } else {
                int size = arrayList.size();
                obtain.writeInt(size);
                for (int i = 0; i < size; i++) {
                    obtain.writeString(arrayList.get(i));
                }
            }
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_SEND_MULTI_PART_TEXT_MESSAGE, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to sendSmsMessage", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.telephony.ITelephony
    public void addObserver(int i, IRadioStateObserver iRadioStateObserver, String str, int i2) {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
        obtain.writeInt(i);
        obtain.writeRemoteObject(iRadioStateObserver.asObject());
        obtain.writeString(str);
        obtain.writeInt(i2);
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_ADD_OBSERVER, obtain, obtain2, new MessageOption());
            if (obtain2.readInt() == -2) {
                throw new SecurityException(str + " failed to add observer, permission denied!");
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to addObserver", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.telephony.ITelephony
    public void removeObserver(int i, IRadioStateObserver iRadioStateObserver, String str) {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
        obtain.writeInt(i);
        obtain.writeRemoteObject(iRadioStateObserver.asObject());
        obtain.writeString(str);
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_REMOVE_OBSERVER, obtain, obtain2, new MessageOption());
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to removeObserver", new Object[0]);
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.telephony.ITelephony
    public NetworkState createNetworkStateFromObserverParcel(MessageParcel messageParcel) {
        NetworkState networkState;
        MessageParcel obtain = MessageParcel.obtain();
        try {
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_PARSE_OBSERVER_PARCEL_SERVICE_STATE, messageParcel, obtain, new MessageOption());
            networkState = new NetworkState();
            try {
                if (!obtain.readSequenceable(networkState)) {
                    return null;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "Failed to create service state from observer parcel", new Object[0]);
                obtain.reclaim();
                return networkState;
            }
        } catch (RemoteException unused2) {
            networkState = null;
            HiLog.error(TAG, "Failed to create service state from observer parcel", new Object[0]);
            obtain.reclaim();
            return networkState;
        }
        obtain.reclaim();
        return networkState;
    }

    @Override // ohos.telephony.ITelephony
    public List<SignalInformation> createSignalInfoFromObserverParcel(MessageParcel messageParcel) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        List<SignalInformation> arrayList = new ArrayList<>();
        try {
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_PARSE_OBSERVER_PARCEL_SIGNAL, messageParcel, obtain, messageOption);
            arrayList = SignalInformation.createSignalInfoListFromParcel(obtain);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to createSignalInfoFromObserverParcel", new Object[0]);
        }
        obtain.reclaim();
        return arrayList;
    }

    @Override // ohos.telephony.ITelephony
    public List<CellInformation> createCellInfoFromObserverParcel(MessageParcel messageParcel) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        List<CellInformation> arrayList = new ArrayList<>();
        try {
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_PARSE_OBSERVER_PARCEL_CELL, messageParcel, obtain, messageOption);
            arrayList = CellInformation.createCellInfoListFromParcel(obtain);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to createCellInfoFromObserverParcel", new Object[0]);
        }
        obtain.reclaim();
        return arrayList;
    }

    @Override // ohos.telephony.ITelephony
    public int getCellularDataFlowType() {
        int i;
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(4001, obtain, obtain2, messageOption);
            i = obtain2.readInt();
            try {
                HiLog.debug(TAG, "getCellularDataFlowType, dataFlow = %d", Integer.valueOf(i));
            } catch (RemoteException unused) {
            }
        } catch (RemoteException unused2) {
            i = 0;
            try {
                HiLog.error(TAG, "Failed to getCellularDataFlowType", new Object[0]);
                obtain2.reclaim();
                obtain.reclaim();
                return i;
            } catch (Throwable th) {
                obtain2.reclaim();
                obtain.reclaim();
                throw th;
            }
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i;
    }

    @Override // ohos.telephony.ITelephony
    public boolean isCellularDataEnabled(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return false;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(4002, obtain, obtain2, messageOption);
            if (obtain2.readInt() != 0) {
                z = true;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to isCellularDataEnabled", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    @Override // ohos.telephony.ITelephony
    public void enableCellularData(int i, boolean z) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            obtain.writeBoolean(z);
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_SET_DATA_ENABLED, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to enableCellularData", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.telephony.ITelephony
    public boolean isCellularDataRoamingEnabled(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return false;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_IS_DATA_ROAMING_ENABLED, obtain, obtain2, messageOption);
            if (obtain2.readInt() != 0) {
                z = true;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to isCellularDataRoamingEnabled", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    @Override // ohos.telephony.ITelephony
    public void enableCellularDataRoaming(int i, boolean z) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            obtain.writeBoolean(z);
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_ENABLE_DATA_ROAMING, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to enableCellularDataRoaming", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.telephony.ITelephony
    public int getRadioTechnologyType(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i2 = 0;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_GET_RADIO_TECHNOLOGY_TYPE, obtain, obtain2, messageOption);
            i2 = obtain2.readInt();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getRadioTechnologyType", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }

    @Override // ohos.telephony.ITelephony
    public int getCellularDataState(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i2 = 0;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return 0;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_GET_DATA_STATE, obtain, obtain2, messageOption);
            i2 = obtain2.readInt();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getCellularDataState", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.telephony.ITelephony
    public int getDefaultCellularDataSlotId() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_GET_DEFAULT_DATA_SLOT_INDEX_ID, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getDefaultCellularDataSlotId", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    @Override // ohos.telephony.ITelephony
    public void setDefaultCellularDataSlotId(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (TelephonyUtils.isValidSlotId(i)) {
            try {
                obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
                obtain.writeInt(i);
                getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_SET_DEFAULT_DATA_SLOT_INDEX_ID, obtain, obtain2, messageOption);
            } catch (RemoteException unused) {
                HiLog.error(TAG, "Failed to setDefaultCellularDataSlotId", new Object[0]);
            } catch (Throwable th) {
                obtain2.reclaim();
                obtain.reclaim();
                throw th;
            }
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.telephony.ITelephony
    public void addObserver(int i, IRemoteObject iRemoteObject, String str, int i2) {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
        obtain.writeInt(i);
        obtain.writeRemoteObject(iRemoteObject);
        obtain.writeString(str);
        obtain.writeInt(i2);
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_ADD_DATA_OBSERVER_ID, obtain, obtain2, new MessageOption());
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to addObserver", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.telephony.ITelephony
    public void removeObserver(int i, IRemoteObject iRemoteObject, String str) {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
        obtain.writeInt(i);
        obtain.writeRemoteObject(iRemoteObject);
        obtain.writeString(str);
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            getTelephonySrvAbility().sendRequest(TelephonyUtils.MSG_DELETE_DATA_OBSERVER_ID, obtain, obtain2, new MessageOption());
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to removeObserver", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.telephony.ITelephony
    public String getSimTeleNumberIdentifier(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1008, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getSimTeleNumberIdentifier", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public int getVoiceMailCount(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        int i2 = 0;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1009, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt != -1) {
                i2 = readInt;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getVoiceMailCount", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }

    @Override // ohos.telephony.ITelephony
    public String getVoiceMailIdentifier(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        String str = "";
        if (!TelephonyUtils.isValidSlotId(i)) {
            return str;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1010, obtain, obtain2, messageOption);
            str = obtain2.readString();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getVoiceMailIdentifier", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return str;
    }

    @Override // ohos.telephony.ITelephony
    public boolean isVideoCallingEnabled() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(2009, obtain, obtain2, messageOption);
            z = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to check vedio calling enabled", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    @Override // ohos.telephony.ITelephony
    public boolean isSimActive(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        boolean z = false;
        if (!TelephonyUtils.isValidSlotId(i)) {
            return false;
        }
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeInt(i);
            getTelephonySrvAbility().sendRequest(1011, obtain, obtain2, messageOption);
            z = obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to isSimActive", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.telephony.ITelephony
    public int getDefaultVoiceSlotId() {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            getTelephonySrvAbility().sendRequest(1012, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            return readInt;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to getDefaultVoiceSlotId", new Object[0]);
            obtain2.reclaim();
            obtain.reclaim();
            return -1;
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    @Override // ohos.telephony.ITelephony
    public void inputDialerSpecialCode(String str) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(TELEPHONY_SA_DESCRIPTOR);
            obtain.writeString(str);
            getTelephonySrvAbility().sendRequest(2010, obtain, obtain2, messageOption);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "Failed to inputDialerSpecialCode", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }
}
