package android.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAppCallback extends IInterface {

    public static abstract class Stub extends Binder implements IAppCallback {
        private static final String DESCRIPTOR = "android.nfc.IAppCallback";
        static final int TRANSACTION_createBeamShareData = 1;
        static final int TRANSACTION_onNdefPushComplete = 2;
        static final int TRANSACTION_onTagDiscovered = 3;

        private static class Proxy implements IAppCallback {
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

            public BeamShareData createBeamShareData(byte peerLlcpVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    BeamShareData beamShareData;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(peerLlcpVersion);
                    this.mRemote.transact(Stub.TRANSACTION_createBeamShareData, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        beamShareData = (BeamShareData) BeamShareData.CREATOR.createFromParcel(_reply);
                    } else {
                        beamShareData = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return beamShareData;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNdefPushComplete(byte peerLlcpVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(peerLlcpVersion);
                    this.mRemote.transact(Stub.TRANSACTION_onNdefPushComplete, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onTagDiscovered(Tag tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tag != null) {
                        _data.writeInt(Stub.TRANSACTION_createBeamShareData);
                        tag.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onTagDiscovered, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppCallback)) {
                return new Proxy(obj);
            }
            return (IAppCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_createBeamShareData /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    BeamShareData _result = createBeamShareData(data.readByte());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_createBeamShareData);
                        _result.writeToParcel(reply, TRANSACTION_createBeamShareData);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_onNdefPushComplete /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNdefPushComplete(data.readByte());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onTagDiscovered /*3*/:
                    Tag tag;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tag = (Tag) Tag.CREATOR.createFromParcel(data);
                    } else {
                        tag = null;
                    }
                    onTagDiscovered(tag);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    BeamShareData createBeamShareData(byte b) throws RemoteException;

    void onNdefPushComplete(byte b) throws RemoteException;

    void onTagDiscovered(Tag tag) throws RemoteException;
}
