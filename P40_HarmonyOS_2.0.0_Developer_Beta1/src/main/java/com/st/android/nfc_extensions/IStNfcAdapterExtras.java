package com.st.android.nfc_extensions;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IStNfcAdapterExtras extends IInterface {
    void deliverSeIntent(List<String> list, Intent intent) throws RemoteException;

    void notifyCheckCertResult(String str, boolean z) throws RemoteException;

    public static class Default implements IStNfcAdapterExtras {
        @Override // com.st.android.nfc_extensions.IStNfcAdapterExtras
        public void deliverSeIntent(List<String> list, Intent seIntent) throws RemoteException {
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapterExtras
        public void notifyCheckCertResult(String pkg, boolean success) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IStNfcAdapterExtras {
        private static final String DESCRIPTOR = "com.st.android.nfc_extensions.IStNfcAdapterExtras";
        static final int TRANSACTION_deliverSeIntent = 1;
        static final int TRANSACTION_notifyCheckCertResult = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStNfcAdapterExtras asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStNfcAdapterExtras)) {
                return new Proxy(obj);
            }
            return (IStNfcAdapterExtras) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _arg0 = data.createStringArrayList();
                if (data.readInt() != 0) {
                    _arg1 = (Intent) Intent.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                deliverSeIntent(_arg0, _arg1);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                notifyCheckCertResult(data.readString(), data.readInt() != 0);
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
        public static class Proxy implements IStNfcAdapterExtras {
            public static IStNfcAdapterExtras sDefaultImpl;
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

            @Override // com.st.android.nfc_extensions.IStNfcAdapterExtras
            public void deliverSeIntent(List<String> pkg, Intent seIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkg);
                    if (seIntent != null) {
                        _data.writeInt(1);
                        seIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deliverSeIntent(pkg, seIntent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.IStNfcAdapterExtras
            public void notifyCheckCertResult(String pkg, boolean success) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(success ? 1 : 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyCheckCertResult(pkg, success);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IStNfcAdapterExtras impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IStNfcAdapterExtras getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
