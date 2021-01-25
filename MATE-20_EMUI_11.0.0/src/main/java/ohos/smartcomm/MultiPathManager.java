package ohos.smartcomm;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class MultiPathManager {
    private static final String DESCRIPTOR = "android.emcom.IEmcomManager";
    private static final int FIRST_CALL_TRANSACTION = 1;
    private static final int MP_SERVICE_TYPE_FROM_A = 1;
    public static final String MULTIPATH_FLOW_SCALE = "multipath_flow_scale";
    public static final String MULTIPATH_FLOW_TYPE = "multipath_flow_type";
    public static final String MULTIPATH_IP_ADDRESS = "multipath_ip_address";
    public static final String MULTIPATH_IP_CARRIER = "multipath_ip_carrier";
    public static final String MULTIPATH_IP_DEPLOYTYPE = "multipath_ip_deploytype";
    public static final String MULTIPATH_IP_GROUP = "multipath_ip_group";
    public static final String MULTIPATH_IP_PORT = "multipath_ip_port";
    public static final String MULTIPATH_PARAM_BANDWIDTH = "multipath_param_bandwidth";
    public static final String MULTIPATH_PARAM_RTT = "multipath_param_rtt";
    public static final String MULTIPATH_POLICY_TYPE = "multipath_policy_type";
    public static final String MULTIPATH_SCENARIO_FLAG = "multipath_scenario_flag";
    public static final String MULTIPATH_SOURCELINK_RTT = "multipath_sourcelink_rtt";
    public static final String MULTIPATH_SWITCH_PATH = "multipath_switch_path";
    public static final String MULTIPATH_SWITCH_TIME = "multipath_switch_time";
    public static final String MULTIPATH_TARGETLINK_RTT = "multipath_targetlink_rtt";
    public static final String MULTIPATH_USER_ID = "multipath_user_id";
    private static final String SERVICE_NAME = "EmcomManager";
    private static final int STRICT_POLICY = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, SmartCommConstant.SMART_COMM_DOMAIN, "SmartCommMP");
    private static final int TRANSACTION_DISABLE_MULTIPATH = 18;
    private static final int TRANSACTION_ENABLE_MULTIPATHFLOW = 17;
    private static final int WORK_SOURCE = 1;
    private static MultiPathManager instance = new MultiPathManager();
    private final Object mLock = new Object();
    private volatile IRemoteObject remoteMpService = null;

    private MultiPathManager() {
    }

    public static MultiPathManager getInstance() {
        return instance;
    }

    private void ensureRemoteService() throws RemoteException {
        synchronized (this.mLock) {
            if (this.remoteMpService == null) {
                HiLog.warn(TAG, "Reestablishing connection to %{public}s", SERVICE_NAME);
                SmartComm smartComm = new SmartComm();
                int mPServiceType = smartComm.getMPServiceType();
                HiLog.warn(TAG, "Mp type =%{public}d", Integer.valueOf(mPServiceType));
                if (mPServiceType == 1) {
                    this.remoteMpService = smartComm.getMPService();
                }
                if (this.remoteMpService == null) {
                    HiLog.error(TAG, "getSysAbility(%{public}s) failed.", SERVICE_NAME);
                    throw new RemoteException();
                }
            }
        }
    }

    private boolean writeInterfaceToken(String str, MessageParcel messageParcel) {
        boolean writeInt = messageParcel.writeInt(1);
        if (!writeInt) {
            return writeInt;
        }
        boolean writeInt2 = messageParcel.writeInt(1);
        if (!writeInt2) {
            return writeInt2;
        }
        return messageParcel.writeString(str);
    }

    private boolean writeStrings(String str, String str2, String str3, MessageParcel messageParcel) {
        boolean writeString = messageParcel.writeString(str);
        if (!writeString) {
            return writeString;
        }
        boolean writeString2 = messageParcel.writeString(str2);
        if (!writeString2) {
            return writeString2;
        }
        return messageParcel.writeString(str3);
    }

    private boolean writeStrings(String str, String str2, MessageParcel messageParcel) {
        boolean writeString = messageParcel.writeString(str);
        if (!writeString) {
            return writeString;
        }
        return messageParcel.writeString(str2);
    }

    public int enableMultipathFlow(String str, String str2, String str3, IMultiPathCallback iMultiPathCallback) throws RemoteException {
        ensureRemoteService();
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            if (writeInterfaceToken(DESCRIPTOR, obtain) && writeStrings(str, str2, str3, obtain)) {
                if (!obtain.writeRemoteObject(new MultiPathCallbackStub(iMultiPathCallback).asObject())) {
                    HiLog.error(TAG, "registerListener write listener error.", new Object[0]);
                } else {
                    if (!this.remoteMpService.sendRequest(17, obtain, obtain2, new MessageOption())) {
                        HiLog.error(TAG, "enableMultipathFlow transact error.", new Object[0]);
                    } else {
                        HiLog.error(TAG, "enableMultipathFlow transact ok.", new Object[0]);
                        obtain.readInt();
                        int readInt = obtain2.readInt();
                        obtain2.reclaim();
                        obtain.reclaim();
                        return readInt;
                    }
                }
            }
            return -1;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    public int disableMultipath(String str, String str2) throws RemoteException {
        ensureRemoteService();
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (writeInterfaceToken(DESCRIPTOR, obtain) && writeStrings(str, str2, obtain)) {
                if (!this.remoteMpService.sendRequest(18, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "disableMultipath transact error.", new Object[0]);
                } else {
                    HiLog.error(TAG, "disableMultipath transact ok.", new Object[0]);
                    obtain.readInt();
                    int readInt = obtain2.readInt();
                    obtain2.reclaim();
                    obtain.reclaim();
                    return readInt;
                }
            }
            return -1;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
