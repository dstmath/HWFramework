package ohos.distributedschedule.interwork;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

class DeviceStateCallbackImpl extends RemoteObject implements IRemoteBroker {
    private static final String DESCRIPTOR = "ohos.distributedschedule.interwork.DeviceStateCallbackImpl";
    private static final int DEVICE_OFFLINE = 2;
    private static final int DEVICE_ONLINE = 1;
    private static final Set<DeviceStateCallbackImpl> DEVICE_STATE_CALLBACKS = new HashSet();
    private static final Object LOCK = new Object();
    private static final HiLogLabel TAG = new HiLogLabel(3, 218109952, "DeviceStateCallbackImpl");
    private final IDeviceStateCallback mCallback;

    public IRemoteObject asObject() {
        return this;
    }

    private DeviceStateCallbackImpl(IDeviceStateCallback iDeviceStateCallback) {
        super("");
        this.mCallback = iDeviceStateCallback;
    }

    private static DeviceStateCallbackImpl getCallback(IDeviceStateCallback iDeviceStateCallback, boolean z) {
        synchronized (LOCK) {
            Iterator<DeviceStateCallbackImpl> it = DEVICE_STATE_CALLBACKS.iterator();
            while (it.hasNext()) {
                DeviceStateCallbackImpl next = it.next();
                if (next.mCallback.equals(iDeviceStateCallback)) {
                    if (z) {
                        it.remove();
                    }
                    return next;
                }
            }
            DeviceStateCallbackImpl deviceStateCallbackImpl = null;
            if (!z) {
                deviceStateCallbackImpl = new DeviceStateCallbackImpl(iDeviceStateCallback);
                DEVICE_STATE_CALLBACKS.add(deviceStateCallbackImpl);
            }
            return deviceStateCallbackImpl;
        }
    }

    public static DeviceStateCallbackImpl get(IDeviceStateCallback iDeviceStateCallback) throws NullPointerException {
        if (iDeviceStateCallback != null) {
            return getCallback(iDeviceStateCallback, false);
        }
        throw new NullPointerException("callback is null");
    }

    public static DeviceStateCallbackImpl remove(IDeviceStateCallback iDeviceStateCallback) throws NullPointerException {
        if (iDeviceStateCallback != null) {
            return getCallback(iDeviceStateCallback, true);
        }
        throw new NullPointerException("callback is null");
    }

    private void onDeviceOffline(String str, int i) throws RemoteException {
        IDeviceStateCallback iDeviceStateCallback = this.mCallback;
        if (iDeviceStateCallback != null && str != null) {
            iDeviceStateCallback.onDeviceOffline(str, i);
        }
    }

    private void onDeviceOnline(String str, int i) throws RemoteException {
        IDeviceStateCallback iDeviceStateCallback = this.mCallback;
        if (iDeviceStateCallback != null && str != null) {
            iDeviceStateCallback.onDeviceOnline(str, i);
        }
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            HiLog.error(TAG, "onRemoteRequest param invalid", new Object[0]);
            return false;
        }
        String readInterfaceToken = messageParcel.readInterfaceToken();
        if (readInterfaceToken == null) {
            HiLog.error(TAG, "onRemoteRequest error interfaceToken is null", new Object[0]);
            return false;
        } else if (!readInterfaceToken.equals(DESCRIPTOR)) {
            HiLog.error(TAG, "onRemoteRequest error interfaceToken:%{private}s", new Object[]{readInterfaceToken});
            return false;
        } else {
            if (i == 1) {
                HiLog.debug(TAG, "DEVICE_ONLINE receive", new Object[0]);
                onDeviceOnline(messageParcel.readString(), messageParcel.readInt());
            } else if (i != 2) {
                HiLog.warn(TAG, "OnRemoteRequest unknown code", new Object[0]);
                return DeviceStateCallbackImpl.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                HiLog.debug(TAG, "DEVICE_OFFLINE receive", new Object[0]);
                onDeviceOffline(messageParcel.readString(), messageParcel.readInt());
            }
            return true;
        }
    }
}
