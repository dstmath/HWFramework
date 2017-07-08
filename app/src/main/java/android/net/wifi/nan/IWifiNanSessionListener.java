package android.net.wifi.nan;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiNanSessionListener extends IInterface {

    public static abstract class Stub extends Binder implements IWifiNanSessionListener {
        private static final String DESCRIPTOR = "android.net.wifi.nan.IWifiNanSessionListener";
        static final int TRANSACTION_onMatch = 5;
        static final int TRANSACTION_onMessageReceived = 8;
        static final int TRANSACTION_onMessageSendFail = 7;
        static final int TRANSACTION_onMessageSendSuccess = 6;
        static final int TRANSACTION_onPublishFail = 1;
        static final int TRANSACTION_onPublishTerminated = 2;
        static final int TRANSACTION_onSubscribeFail = 3;
        static final int TRANSACTION_onSubscribeTerminated = 4;

        private static class Proxy implements IWifiNanSessionListener {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onPublishFail(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_onPublishFail, _data, null, Stub.TRANSACTION_onPublishFail);
                } finally {
                    _data.recycle();
                }
            }

            public void onPublishTerminated(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_onPublishTerminated, _data, null, Stub.TRANSACTION_onPublishFail);
                } finally {
                    _data.recycle();
                }
            }

            public void onSubscribeFail(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_onSubscribeFail, _data, null, Stub.TRANSACTION_onPublishFail);
                } finally {
                    _data.recycle();
                }
            }

            public void onSubscribeTerminated(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_onSubscribeTerminated, _data, null, Stub.TRANSACTION_onPublishFail);
                } finally {
                    _data.recycle();
                }
            }

            public void onMatch(int peerId, byte[] serviceSpecificInfo, int serviceSpecificInfoLength, byte[] matchFilter, int matchFilterLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(peerId);
                    _data.writeByteArray(serviceSpecificInfo);
                    _data.writeInt(serviceSpecificInfoLength);
                    _data.writeByteArray(matchFilter);
                    _data.writeInt(matchFilterLength);
                    this.mRemote.transact(Stub.TRANSACTION_onMatch, _data, null, Stub.TRANSACTION_onPublishFail);
                } finally {
                    _data.recycle();
                }
            }

            public void onMessageSendSuccess(int messageId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    this.mRemote.transact(Stub.TRANSACTION_onMessageSendSuccess, _data, null, Stub.TRANSACTION_onPublishFail);
                } finally {
                    _data.recycle();
                }
            }

            public void onMessageSendFail(int messageId, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_onMessageSendFail, _data, null, Stub.TRANSACTION_onPublishFail);
                } finally {
                    _data.recycle();
                }
            }

            public void onMessageReceived(int peerId, byte[] message, int messageLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(peerId);
                    _data.writeByteArray(message);
                    _data.writeInt(messageLength);
                    this.mRemote.transact(Stub.TRANSACTION_onMessageReceived, _data, null, Stub.TRANSACTION_onPublishFail);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiNanSessionListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiNanSessionListener)) {
                return new Proxy(obj);
            }
            return (IWifiNanSessionListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onPublishFail /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPublishFail(data.readInt());
                    return true;
                case TRANSACTION_onPublishTerminated /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPublishTerminated(data.readInt());
                    return true;
                case TRANSACTION_onSubscribeFail /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSubscribeFail(data.readInt());
                    return true;
                case TRANSACTION_onSubscribeTerminated /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSubscribeTerminated(data.readInt());
                    return true;
                case TRANSACTION_onMatch /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onMatch(data.readInt(), data.createByteArray(), data.readInt(), data.createByteArray(), data.readInt());
                    return true;
                case TRANSACTION_onMessageSendSuccess /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onMessageSendSuccess(data.readInt());
                    return true;
                case TRANSACTION_onMessageSendFail /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    onMessageSendFail(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onMessageReceived /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    onMessageReceived(data.readInt(), data.createByteArray(), data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onMatch(int i, byte[] bArr, int i2, byte[] bArr2, int i3) throws RemoteException;

    void onMessageReceived(int i, byte[] bArr, int i2) throws RemoteException;

    void onMessageSendFail(int i, int i2) throws RemoteException;

    void onMessageSendSuccess(int i) throws RemoteException;

    void onPublishFail(int i) throws RemoteException;

    void onPublishTerminated(int i) throws RemoteException;

    void onSubscribeFail(int i) throws RemoteException;

    void onSubscribeTerminated(int i) throws RemoteException;
}
