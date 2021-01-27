package ohos.distributedschedule.interwork;

import java.util.HashMap;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

class DistributedInitCallbackDelegate extends RemoteObject implements IRemoteBroker, IInitCallback {
    private static final String DESCRIPTOR = "com.huawei.harmonyos.interwork.IInitCallBack";
    private static final Map<IInitCallback, DistributedInitCallbackDelegate> DISTRIBUTED_INIT_CALLBACK_DELEGATES = new HashMap();
    private static final int INIT_FAILURE = 2;
    private static final int INIT_SUCCESS = 1;
    private static final Object LOCK = new Object();
    private static final HiLogLabel TAG = new HiLogLabel(3, 218109952, "DistributedInitCallbackDelegate");
    private IInitCallback initCallback = null;

    public IRemoteObject asObject() {
        return this;
    }

    private DistributedInitCallbackDelegate(IInitCallback iInitCallback) {
        super("");
        this.initCallback = iInitCallback;
    }

    static DistributedInitCallbackDelegate getOrCreate(IInitCallback iInitCallback) {
        DistributedInitCallbackDelegate computeIfAbsent;
        if (iInitCallback != null) {
            synchronized (LOCK) {
                computeIfAbsent = DISTRIBUTED_INIT_CALLBACK_DELEGATES.computeIfAbsent(iInitCallback, $$Lambda$DistributedInitCallbackDelegate$TzSnjb4l2nJ9Up3rKvvqWYHpQjI.INSTANCE);
            }
            return computeIfAbsent;
        }
        throw new IllegalArgumentException("callback is null");
    }

    static /* synthetic */ DistributedInitCallbackDelegate lambda$getOrCreate$0(IInitCallback iInitCallback) {
        return new DistributedInitCallbackDelegate(iInitCallback);
    }

    static DistributedInitCallbackDelegate get(IInitCallback iInitCallback) {
        DistributedInitCallbackDelegate distributedInitCallbackDelegate;
        if (iInitCallback != null) {
            synchronized (LOCK) {
                distributedInitCallbackDelegate = DISTRIBUTED_INIT_CALLBACK_DELEGATES.get(iInitCallback);
            }
            return distributedInitCallbackDelegate;
        }
        throw new IllegalArgumentException("callback is null");
    }

    static DistributedInitCallbackDelegate remove(IInitCallback iInitCallback) {
        DistributedInitCallbackDelegate remove;
        if (iInitCallback != null) {
            synchronized (LOCK) {
                remove = DISTRIBUTED_INIT_CALLBACK_DELEGATES.remove(iInitCallback);
            }
            return remove;
        }
        throw new IllegalArgumentException("callback is null");
    }

    @Override // ohos.distributedschedule.interwork.IInitCallback
    public void onInitSuccess(String str) {
        IInitCallback iInitCallback = this.initCallback;
        if (iInitCallback != null) {
            iInitCallback.onInitSuccess(str);
        }
    }

    @Override // ohos.distributedschedule.interwork.IInitCallback
    public void onInitFailure(String str, int i) {
        IInitCallback iInitCallback = this.initCallback;
        if (iInitCallback != null) {
            iInitCallback.onInitFailure(str, i);
        }
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null || messageOption == null) {
            HiLog.error(TAG, "onRemoteRequest param invalid", new Object[0]);
            return false;
        }
        HiLog.debug(TAG, "DmsDistributedCallbackDelegate onRemoteRequest code is %{private}d", new Object[]{Integer.valueOf(i)});
        String readInterfaceToken = messageParcel.readInterfaceToken();
        if (!DESCRIPTOR.equals(readInterfaceToken)) {
            HiLog.error(TAG, "onRemoteRequest error interfaceToken:%{private}s", new Object[]{readInterfaceToken});
            return false;
        }
        if (i == 1) {
            onInitSuccess(messageParcel.readString());
        } else if (i != 2) {
            return DistributedInitCallbackDelegate.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            onInitFailure(messageParcel.readString(), messageParcel.readInt());
        }
        return true;
    }
}
