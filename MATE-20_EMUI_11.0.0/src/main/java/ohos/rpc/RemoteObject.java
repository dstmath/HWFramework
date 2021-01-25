package ohos.rpc;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

public class RemoteObject implements IRemoteObject {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "RemoteObject");
    private String mDescriptor;
    private IRemoteBroker mInterface;
    private final long mNativeHolder;

    private static native void nativeFreeObjectHolder(long j);

    private native int nativeGetCallingPid();

    private native int nativeGetCallingUid();

    private static native long nativeGetObjectHolder(String str);

    @Override // ohos.rpc.IRemoteObject
    public boolean addDeathRecipient(IRemoteObject.DeathRecipient deathRecipient, int i) {
        return false;
    }

    @Override // ohos.rpc.IRemoteObject
    public boolean isObjectDead() {
        return false;
    }

    @Override // ohos.rpc.IRemoteObject
    public boolean removeDeathRecipient(IRemoteObject.DeathRecipient deathRecipient, int i) {
        return false;
    }

    public RemoteObject(String str) {
        this.mDescriptor = str;
        this.mNativeHolder = nativeGetObjectHolder(str);
    }

    @Override // ohos.rpc.IRemoteObject
    public IRemoteBroker queryLocalInterface(String str) {
        String str2 = this.mDescriptor;
        if (str2 == null || !str2.equals(str)) {
            return null;
        }
        return this.mInterface;
    }

    @Override // ohos.rpc.IRemoteObject
    public String getInterfaceDescriptor() {
        return this.mDescriptor;
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        HiLog.info(TAG, "Java onRemoteRequest called", new Object[0]);
        if (i == 1598311760) {
            HiLog.info(TAG, "Java DUMP_TRANSACTION called", new Object[0]);
            FileDescriptor readFileDescriptor = messageParcel.readFileDescriptor();
            dump(readFileDescriptor, messageParcel.readStringArray());
            messageParcel.closeFileDescriptor(readFileDescriptor);
        } else if (i != 1598968902) {
            return false;
        } else {
            messageParcel2.writeInterfaceToken(getInterfaceDescriptor());
        }
        return true;
    }

    private boolean dispatchRequest(int i, long j, long j2, MessageOption messageOption) {
        MessageParcel obtain = MessageParcel.obtain(j);
        MessageParcel obtain2 = MessageParcel.obtain(j2);
        boolean z = false;
        try {
            z = onRemoteRequest(i, obtain, obtain2, messageOption);
        } catch (RuntimeException | RemoteException e) {
            if ((messageOption.getFlags() & 1) == 0) {
                obtain2.rewindRead(0);
                obtain2.writeException(e);
            }
        }
        obtain2.reclaim();
        obtain.reclaim();
        return z;
    }

    private boolean dispatchDump(int i, long j, long j2, MessageOption messageOption) {
        HiLog.info(TAG, "Java dispatchDump called", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain(j);
        try {
            FileDescriptor readFileDescriptor = obtain.readFileDescriptor();
            dump(readFileDescriptor, obtain.readStringArray());
            obtain.closeFileDescriptor(readFileDescriptor);
            obtain.reclaim();
            return true;
        } catch (RemoteException unused) {
            obtain.reclaim();
            return false;
        } catch (Throwable th) {
            obtain.reclaim();
            throw th;
        }
    }

    @Override // ohos.rpc.IRemoteObject
    public boolean sendRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel != null) {
            messageParcel.rewindRead(0);
        }
        boolean onRemoteRequest = onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        if (messageParcel2 != null) {
            messageParcel2.rewindRead(0);
        }
        return onRemoteRequest;
    }

    public int getCallingPid() {
        return nativeGetCallingPid();
    }

    public int getCallingUid() {
        return nativeGetCallingUid();
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Dump Exception:server not implement");
    }

    @Override // ohos.rpc.IRemoteObject
    public void dump(FileDescriptor fileDescriptor, String[] strArr) throws RemoteException {
        Optional empty = Optional.empty();
        if (fileDescriptor != null) {
            try {
                PrintWriter printWriter = new PrintWriter(new FileOutputStream(fileDescriptor));
                empty = Optional.of(printWriter);
                dump(fileDescriptor, printWriter, strArr);
            } catch (SecurityException e) {
                if (empty.isPresent()) {
                    ((PrintWriter) empty.get()).println("Dump exception: " + e.getMessage());
                }
                if (!empty.isPresent()) {
                    return;
                }
            } catch (Throwable th) {
                if (empty.isPresent()) {
                    ((PrintWriter) empty.get()).close();
                }
                throw th;
            }
        }
        if (!empty.isPresent()) {
            return;
        }
        ((PrintWriter) empty.get()).close();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeFreeObjectHolder(this.mNativeHolder);
        } finally {
            super.finalize();
        }
    }

    public void attachLocalInterface(IRemoteBroker iRemoteBroker, String str) {
        this.mInterface = iRemoteBroker;
        this.mDescriptor = str;
    }
}
