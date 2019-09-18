package android.app;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Parcel;
import android.os.RemoteException;

public interface IInstantAppResolver extends IInterface {

    public static abstract class Stub extends Binder implements IInstantAppResolver {
        private static final String DESCRIPTOR = "android.app.IInstantAppResolver";
        static final int TRANSACTION_getInstantAppIntentFilterList = 2;
        static final int TRANSACTION_getInstantAppResolveInfoList = 1;

        private static class Proxy implements IInstantAppResolver {
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

            public void getInstantAppResolveInfoList(Intent sanitizedIntent, int[] hostDigestPrefix, String token, int sequence, IRemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sanitizedIntent != null) {
                        _data.writeInt(1);
                        sanitizedIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeIntArray(hostDigestPrefix);
                    _data.writeString(token);
                    _data.writeInt(sequence);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getInstantAppIntentFilterList(Intent sanitizedIntent, int[] hostDigestPrefix, String token, IRemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sanitizedIntent != null) {
                        _data.writeInt(1);
                        sanitizedIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeIntArray(hostDigestPrefix);
                    _data.writeString(token);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInstantAppResolver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInstantAppResolver)) {
                return new Proxy(obj);
            }
            return (IInstantAppResolver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                Intent _arg0 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        Intent _arg02 = _arg0;
                        getInstantAppResolveInfoList(_arg02, data.createIntArray(), data.readString(), data.readInt(), IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        getInstantAppIntentFilterList(_arg0, data.createIntArray(), data.readString(), IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void getInstantAppIntentFilterList(Intent intent, int[] iArr, String str, IRemoteCallback iRemoteCallback) throws RemoteException;

    void getInstantAppResolveInfoList(Intent intent, int[] iArr, String str, int i, IRemoteCallback iRemoteCallback) throws RemoteException;
}
