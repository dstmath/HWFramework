package ohos.rpc;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

public class RemoteProxy implements IRemoteObject {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "RemoteProxy");
    private static RemoteProxyHolder sProxyHolder = new RemoteProxyHolder();
    private final long mNativeData;

    private native boolean nativeAddDeathRecipient(IRemoteObject.DeathRecipient deathRecipient, int i);

    private static native void nativeFreeProxyHolder(long j);

    private native long nativeGetHandle();

    private native String nativeGetInterfaceDescriptor();

    private native boolean nativeIsObjectDead();

    private native boolean nativeRemoveDeathRecipient(IRemoteObject.DeathRecipient deathRecipient, int i);

    private native boolean nativeSendRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException;

    @Override // ohos.rpc.IRemoteObject
    public IRemoteBroker queryLocalInterface(String str) {
        return null;
    }

    private RemoteProxy(long j) {
        this.mNativeData = j;
    }

    private static final class RemoteProxyHolder {
        private static final Map<Long, WeakReference<RemoteProxy>> REMOTE_PROXY_MAP = new ConcurrentHashMap();

        private RemoteProxyHolder() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Optional<RemoteProxy> get(long j) {
            Optional<RemoteProxy> empty = Optional.empty();
            WeakReference<RemoteProxy> weakReference = REMOTE_PROXY_MAP.get(Long.valueOf(j));
            if (weakReference == null) {
                return empty;
            }
            RemoteProxy remoteProxy = weakReference.get();
            if (remoteProxy != null) {
                return Optional.of(remoteProxy);
            }
            REMOTE_PROXY_MAP.remove(Long.valueOf(j));
            return empty;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void set(long j, RemoteProxy remoteProxy) {
            if (j != 0) {
                REMOTE_PROXY_MAP.put(Long.valueOf(j), new WeakReference<>(remoteProxy));
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void remove(long j) {
            if (REMOTE_PROXY_MAP.containsKey(Long.valueOf(j))) {
                REMOTE_PROXY_MAP.remove(Long.valueOf(j));
            }
        }
    }

    private static RemoteProxy getInstance(long j) {
        Optional optional = sProxyHolder.get(j);
        if (optional.isPresent()) {
            return (RemoteProxy) optional.get();
        }
        RemoteProxy remoteProxy = new RemoteProxy(j);
        sProxyHolder.set(j, remoteProxy);
        return (RemoteProxy) Optional.of(remoteProxy).get();
    }

    @Override // ohos.rpc.IRemoteObject
    public boolean addDeathRecipient(IRemoteObject.DeathRecipient deathRecipient, int i) {
        return nativeAddDeathRecipient(deathRecipient, i);
    }

    @Override // ohos.rpc.IRemoteObject
    public boolean removeDeathRecipient(IRemoteObject.DeathRecipient deathRecipient, int i) {
        return nativeRemoveDeathRecipient(deathRecipient, i);
    }

    @Override // ohos.rpc.IRemoteObject
    public String getInterfaceDescriptor() {
        return nativeGetInterfaceDescriptor();
    }

    public static final void sendObituary(IRemoteObject.DeathRecipient deathRecipient) {
        HiLog.info(TAG, "Java sendObituary called", new Object[0]);
        deathRecipient.onRemoteDied();
    }

    @Override // ohos.rpc.IRemoteObject
    public boolean sendRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        return nativeSendRequest(i, messageParcel, messageParcel2, messageOption);
    }

    @Override // ohos.rpc.IRemoteObject
    public void dump(FileDescriptor fileDescriptor, String[] strArr) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeFileDescriptor(fileDescriptor);
        obtain.writeStringArray(strArr);
        try {
            sendRequest(IRemoteObject.DUMP_TRANSACTION, obtain, obtain2, messageOption);
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.rpc.IRemoteObject
    public boolean isObjectDead() {
        return nativeIsObjectDead();
    }

    public long getIdentity() {
        return nativeGetHandle();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            sProxyHolder.remove(this.mNativeData);
            nativeFreeProxyHolder(this.mNativeData);
        } finally {
            super.finalize();
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return getIdentity() == ((RemoteProxy) obj).getIdentity();
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(getIdentity()));
    }
}
