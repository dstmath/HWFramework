package ohos.bluetooth;

import java.io.FileDescriptor;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

/* access modifiers changed from: package-private */
public class SppSocketProxy implements ISppSocket {
    private static final int COMMAND_SPP_CONNECT_SOCKET = 1;
    private static final int COMMAND_SPP_CREATE_SOCKET_SERVER = 2;
    private static final int ERR_OK = 0;
    private static final int MIN_TRANSACTION_ID = 1;
    private IRemoteObject remote = null;

    public SppSocketProxy() {
        BluetoothHostProxy.getInstace().getSaProfileProxy(20).ifPresent(new Consumer() {
            /* class ohos.bluetooth.$$Lambda$SppSocketProxy$e_30vefinko58Z9Z6DDNbkqXOZU */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SppSocketProxy.this.lambda$new$0$SppSocketProxy((IRemoteObject) obj);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$SppSocketProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        this.remote = null;
        BluetoothHostProxy.getInstace().getSaProfileProxy(20).ifPresent(new Consumer() {
            /* class ohos.bluetooth.$$Lambda$SppSocketProxy$zDLOLye87aaOsTkMo9AQ29f5BA */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SppSocketProxy.this.lambda$asObject$1$SppSocketProxy((IRemoteObject) obj);
            }
        });
        return this.remote;
    }

    public /* synthetic */ void lambda$asObject$1$SppSocketProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.bluetooth.ISppSocket
    public Optional<FileDescriptor> sppConnectSocket(BluetoothRemoteDevice bluetoothRemoteDevice, int i, UUID uuid, int i2, int i3) {
        Optional<FileDescriptor> ofNullable;
        if (asObject() == null) {
            return Optional.ofNullable(null);
        }
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeSequenceable(bluetoothRemoteDevice);
        obtain.writeInt(i);
        obtain.writeLong(uuid.getMostSignificantBits());
        obtain.writeLong(uuid.getLeastSignificantBits());
        obtain.writeInt(i2);
        obtain.writeInt(i3);
        MessageOption messageOption = new MessageOption(0);
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
            if (obtain2.readInt() != 3756) {
                ofNullable = createFileDescriptor(obtain2);
                obtain.reclaim();
                obtain2.reclaim();
                return ofNullable;
            }
            throw new SecurityException("Permission denied");
        } catch (RemoteException unused) {
            ofNullable = Optional.ofNullable(null);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.bluetooth.ISppSocket
    public Optional<FileDescriptor> sppCreateSocketServer(String str, int i, UUID uuid, int i2, int i3) {
        Optional<FileDescriptor> ofNullable;
        if (asObject() == null) {
            return Optional.ofNullable(null);
        }
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeString(str);
        obtain.writeInt(i);
        obtain.writeLong(uuid.getMostSignificantBits());
        obtain.writeLong(uuid.getLeastSignificantBits());
        obtain.writeInt(i2);
        obtain.writeInt(i3);
        MessageOption messageOption = new MessageOption(0);
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
            if (obtain2.readInt() != 3756) {
                ofNullable = createFileDescriptor(obtain2);
                obtain.reclaim();
                obtain2.reclaim();
                return ofNullable;
            }
            throw new SecurityException("Permission denied");
        } catch (RemoteException unused) {
            ofNullable = Optional.ofNullable(null);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    private Optional<FileDescriptor> createFileDescriptor(MessageParcel messageParcel) {
        if (messageParcel.readInt() != 0) {
            return Optional.ofNullable(null);
        }
        return Optional.ofNullable(messageParcel.readFileDescriptor());
    }
}
