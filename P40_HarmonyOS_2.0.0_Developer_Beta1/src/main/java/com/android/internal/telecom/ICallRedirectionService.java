package com.android.internal.telecom;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.PhoneAccountHandle;
import com.android.internal.telecom.ICallRedirectionAdapter;

public interface ICallRedirectionService extends IInterface {
    void placeCall(ICallRedirectionAdapter iCallRedirectionAdapter, Uri uri, PhoneAccountHandle phoneAccountHandle, boolean z) throws RemoteException;

    public static class Default implements ICallRedirectionService {
        @Override // com.android.internal.telecom.ICallRedirectionService
        public void placeCall(ICallRedirectionAdapter adapter, Uri handle, PhoneAccountHandle initialPhoneAccount, boolean allowInteractiveResponse) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICallRedirectionService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.ICallRedirectionService";
        static final int TRANSACTION_placeCall = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICallRedirectionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICallRedirectionService)) {
                return new Proxy(obj);
            }
            return (ICallRedirectionService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "placeCall";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Uri _arg1;
            PhoneAccountHandle _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                ICallRedirectionAdapter _arg0 = ICallRedirectionAdapter.Stub.asInterface(data.readStrongBinder());
                if (data.readInt() != 0) {
                    _arg1 = Uri.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                if (data.readInt() != 0) {
                    _arg2 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                placeCall(_arg0, _arg1, _arg2, data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICallRedirectionService {
            public static ICallRedirectionService sDefaultImpl;
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

            @Override // com.android.internal.telecom.ICallRedirectionService
            public void placeCall(ICallRedirectionAdapter adapter, Uri handle, PhoneAccountHandle initialPhoneAccount, boolean allowInteractiveResponse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(adapter != null ? adapter.asBinder() : null);
                    int i = 0;
                    if (handle != null) {
                        _data.writeInt(1);
                        handle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (initialPhoneAccount != null) {
                        _data.writeInt(1);
                        initialPhoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (allowInteractiveResponse) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().placeCall(adapter, handle, initialPhoneAccount, allowInteractiveResponse);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICallRedirectionService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICallRedirectionService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
