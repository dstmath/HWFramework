package ohos.distributedschedule.interwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.bundle.AbilityInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

/* access modifiers changed from: package-private */
public class DeviceManagerServiceProxy {
    private static final int DEALWITH_EMUI_DISTRIBUTED_ENVIRONMENT = 215;
    private static final String DEVICEMANAGER_SERVICE_INTERFACE_TOKEN = "ohos.distributedschedule.accessToken";
    private static final int DISTRIBUTED_SERVICE_ID = 1401;
    private static final int DMS_PERMISSION_DENIED = 29360157;
    private static final int GET_DEVICE_INFO = 213;
    private static final int GET_DEVICE_LIST = 212;
    private static final int INIT_EMUI_DISTRIBUTED_ENVIRONMENT = 15;
    private static final Object INSTANCE_LOCK = new Object();
    private static final int LOCAL_ERROR_CODE = 2;
    private static final int MAX_ABILITY_SIZE = 20;
    private static final int MAX_DEVICE_SIZE = 50;
    private static final int NO_ERROR = 0;
    private static final Object PROXY_LOCK = new Object();
    private static final int QUERY_REMOTE_ABILITY_INFO = 214;
    private static final int REGISTER_DEVICE_STATE_CALLBACK = 210;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218109952, "DeviceManagerServiceProxy");
    private static final int UNINIT_EMUI_DISTRIBUTED_ENVIRONMENT = 16;
    private static final int UNREGISTER_DEVICE_STATE_CALLBACK = 211;
    private static DeviceManagerServiceProxy sDeviceManagerServiceProxy = null;
    private IRemoteObject mDeviceManagerProxy = null;

    private DeviceManagerServiceProxy() {
    }

    public static DeviceManagerServiceProxy getInstance() {
        DeviceManagerServiceProxy deviceManagerServiceProxy;
        synchronized (INSTANCE_LOCK) {
            if (sDeviceManagerServiceProxy == null) {
                sDeviceManagerServiceProxy = new DeviceManagerServiceProxy();
            }
            deviceManagerServiceProxy = sDeviceManagerServiceProxy;
        }
        return deviceManagerServiceProxy;
    }

    public boolean registerDeviceStateCallback(IRemoteObject iRemoteObject) throws RemoteException {
        if (iRemoteObject != null) {
            return sendRequest(210, iRemoteObject);
        }
        HiLog.error(SHELL_LABEL, "register callback is null", new Object[0]);
        return false;
    }

    public boolean unregisterDeviceStateCallback(IRemoteObject iRemoteObject) throws RemoteException {
        if (iRemoteObject != null) {
            return sendRequest(211, iRemoteObject);
        }
        HiLog.error(SHELL_LABEL, "unregister callback is null", new Object[0]);
        return false;
    }

    public List<DeviceInfo> getDeviceList(int i) throws RemoteException {
        List<DeviceInfo> emptyList;
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(DEVICEMANAGER_SERVICE_INTERFACE_TOKEN)) {
                emptyList = Collections.emptyList();
            } else if (!obtain.writeInt(i)) {
                emptyList = Collections.emptyList();
            } else {
                synchronized (PROXY_LOCK) {
                    if (!initDMSProxyLocked()) {
                        HiLog.error(SHELL_LABEL, "sendRequest get proxy failed", new Object[0]);
                        emptyList = Collections.emptyList();
                    } else {
                        boolean sendRequest = this.mDeviceManagerProxy.sendRequest(212, obtain, obtain2, messageOption);
                        if (!sendRequest) {
                            obtain.reclaim();
                            obtain2.reclaim();
                            return Collections.emptyList();
                        } else if (obtain2.readInt() != DMS_PERMISSION_DENIED) {
                            int readInt = obtain2.readInt();
                            if (readInt > 50 || readInt <= 0) {
                                HiLog.error(SHELL_LABEL, "get dms device list size failed", new Object[0]);
                                emptyList = Collections.emptyList();
                            } else {
                                ArrayList arrayList = new ArrayList();
                                for (int i2 = 0; i2 < readInt; i2++) {
                                    DeviceInfo deviceInfo = new DeviceInfo();
                                    if (!obtain2.readSequenceable(deviceInfo)) {
                                        HiLog.error(SHELL_LABEL, "getDeviceList readSequenceable failed", new Object[0]);
                                    } else {
                                        arrayList.add(deviceInfo);
                                    }
                                }
                                obtain.reclaim();
                                obtain2.reclaim();
                                return arrayList;
                            }
                        } else {
                            throw new SecurityException("permission denied");
                        }
                    }
                }
            }
            return emptyList;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0044, code lost:
        if (r5 == false) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004d, code lost:
        if (r1.readInt() == ohos.distributedschedule.interwork.DeviceManagerServiceProxy.DMS_PERMISSION_DENIED) goto L_0x0061;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004f, code lost:
        r5 = new ohos.distributedschedule.interwork.DeviceInfo();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0058, code lost:
        if (r1.readSequenceable(r5) == false) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005a, code lost:
        r0.reclaim();
        r1.reclaim();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0060, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0069, code lost:
        throw new java.lang.SecurityException("permission denied");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006a, code lost:
        r0.reclaim();
        r1.reclaim();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0070, code lost:
        return null;
     */
    public DeviceInfo getDeviceInfo(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (obtain.writeInterfaceToken(DEVICEMANAGER_SERVICE_INTERFACE_TOKEN) && obtain.writeString(str)) {
                synchronized (PROXY_LOCK) {
                    if (!initDMSProxyLocked()) {
                        HiLog.error(SHELL_LABEL, "sendRequest get proxy failed", new Object[0]);
                    } else {
                        boolean sendRequest = this.mDeviceManagerProxy.sendRequest(213, obtain, obtain2, messageOption);
                    }
                }
            }
            return null;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    private boolean sendRequest(int i, IRemoteObject iRemoteObject) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            boolean z = false;
            if (obtain.writeInterfaceToken(DEVICEMANAGER_SERVICE_INTERFACE_TOKEN) && obtain.writeRemoteObject(iRemoteObject)) {
                synchronized (PROXY_LOCK) {
                    if (!initDMSProxyLocked()) {
                        HiLog.error(SHELL_LABEL, "sendRequest get proxy failed", new Object[0]);
                    } else if (this.mDeviceManagerProxy.sendRequest(i, obtain, obtain2, messageOption)) {
                        int readInt = obtain2.readInt();
                        if (readInt == DMS_PERMISSION_DENIED) {
                            throw new SecurityException("permission denied");
                        } else if (readInt == 0) {
                            z = true;
                        }
                    } else {
                        obtain.reclaim();
                        obtain2.reclaim();
                        return false;
                    }
                }
            }
            return z;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    private boolean initDMSProxyLocked() {
        if (this.mDeviceManagerProxy != null) {
            return true;
        }
        this.mDeviceManagerProxy = SysAbilityManager.getSysAbility(1401);
        IRemoteObject iRemoteObject = this.mDeviceManagerProxy;
        if (iRemoteObject == null) {
            HiLog.error(SHELL_LABEL, "initDMSProxyLocked DMS not started", new Object[0]);
            return false;
        }
        iRemoteObject.addDeathRecipient(new IRemoteObject.DeathRecipient() {
            /* class ohos.distributedschedule.interwork.$$Lambda$DeviceManagerServiceProxy$KkkWZRpZ0J4OBArbPK3v3nQnxc */

            public final void onRemoteDied() {
                DeviceManagerServiceProxy.this.lambda$initDMSProxyLocked$0$DeviceManagerServiceProxy();
            }
        }, 0);
        return true;
    }

    public /* synthetic */ void lambda$initDMSProxyLocked$0$DeviceManagerServiceProxy() {
        HiLog.error(SHELL_LABEL, "initDMSProxyLocked receive death notify", new Object[0]);
        synchronized (PROXY_LOCK) {
            this.mDeviceManagerProxy = null;
        }
    }

    public List<AbilityInfo> queryRemoteAbilityByIntent(Intent intent) throws RemoteException {
        List<AbilityInfo> emptyList;
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            if (!obtain.writeInterfaceToken(DEVICEMANAGER_SERVICE_INTERFACE_TOKEN)) {
                emptyList = Collections.emptyList();
            } else {
                obtain.writeSequenceable(intent);
                synchronized (PROXY_LOCK) {
                    if (!initDMSProxyLocked()) {
                        HiLog.error(SHELL_LABEL, "sendRequest get proxy failed", new Object[0]);
                        emptyList = Collections.emptyList();
                    } else {
                        boolean sendRequest = this.mDeviceManagerProxy.sendRequest(214, obtain, obtain2, new MessageOption());
                        if (!sendRequest) {
                            emptyList = Collections.emptyList();
                        } else if (obtain2.readInt() != DMS_PERMISSION_DENIED) {
                            int readInt = obtain2.readInt();
                            if (readInt > 20 || readInt <= 0) {
                                HiLog.error(SHELL_LABEL, "queryRemoteAbilityByIntent failed, wrong length", new Object[0]);
                                emptyList = Collections.emptyList();
                            } else {
                                ArrayList arrayList = new ArrayList(readInt);
                                for (int i = 0; i < readInt; i++) {
                                    AbilityInfo abilityInfo = new AbilityInfo();
                                    if (!obtain2.readSequenceable(abilityInfo)) {
                                        HiLog.error(SHELL_LABEL, "queryRemoteAbilityByIntent readSequenceable failed", new Object[0]);
                                    } else {
                                        arrayList.add(abilityInfo);
                                    }
                                }
                                obtain.reclaim();
                                obtain2.reclaim();
                                return arrayList;
                            }
                        } else {
                            throw new SecurityException("permission denied");
                        }
                    }
                }
            }
            return emptyList;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    public boolean initDistributedEnvironment(String str, DistributedInitCallbackDelegate distributedInitCallbackDelegate) throws RemoteException {
        boolean dealWithDistributedEnvironment = dealWithDistributedEnvironment(15, str, distributedInitCallbackDelegate);
        if (!dealWithDistributedEnvironment && distributedInitCallbackDelegate != null) {
            distributedInitCallbackDelegate.onInitFailure(str, 2);
        }
        return dealWithDistributedEnvironment;
    }

    public boolean unInitDistributedEnvironment(String str, DistributedInitCallbackDelegate distributedInitCallbackDelegate) throws RemoteException {
        return dealWithDistributedEnvironment(16, str, distributedInitCallbackDelegate);
    }

    private boolean dealWithDistributedEnvironment(int i, String str, IRemoteObject iRemoteObject) throws RemoteException {
        boolean z = false;
        if (str == null || iRemoteObject == null) {
            HiLog.error(SHELL_LABEL, "dealWithDistributedEnvironment parameter is illegal", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            if (!obtain.writeInterfaceToken(DEVICEMANAGER_SERVICE_INTERFACE_TOKEN)) {
                return false;
            }
            if (!obtain.writeInt(i) || !obtain.writeString(str) || !obtain.writeRemoteObject(iRemoteObject)) {
                obtain.reclaim();
                obtain2.reclaim();
                return false;
            } else if (!sendRequestToDms(215, obtain, obtain2)) {
                obtain.reclaim();
                obtain2.reclaim();
                return false;
            } else {
                if (obtain2.readInt() == 0) {
                    z = true;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    private boolean sendRequestToDms(int i, MessageParcel messageParcel, MessageParcel messageParcel2) throws RemoteException {
        synchronized (PROXY_LOCK) {
            if (!initDMSProxyLocked()) {
                HiLog.error(SHELL_LABEL, "sendRequest get proxy failed", new Object[0]);
                return false;
            }
            return this.mDeviceManagerProxy.sendRequest(i, messageParcel, messageParcel2, new MessageOption());
        }
    }
}
