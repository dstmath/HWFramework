package huawei.android.security.facerecognition;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFaceRecognizeServiceReceiver extends IInterface {
    void onCallback(long j, int i, int i2, int i3) throws RemoteException;

    public static class Default implements IFaceRecognizeServiceReceiver {
        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onCallback(long reqId, int type, int code, int errorCode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFaceRecognizeServiceReceiver {
        private static final String DESCRIPTOR = "huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver";
        static final int TRANSACTION_onCallback = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFaceRecognizeServiceReceiver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFaceRecognizeServiceReceiver)) {
                return new Proxy(obj);
            }
            return (IFaceRecognizeServiceReceiver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onCallback(data.readLong(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IFaceRecognizeServiceReceiver {
            public static IFaceRecognizeServiceReceiver sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onCallback(long reqId, int type, int code, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(reqId);
                    _data.writeInt(type);
                    _data.writeInt(code);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCallback(reqId, type, code, errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IFaceRecognizeServiceReceiver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFaceRecognizeServiceReceiver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
