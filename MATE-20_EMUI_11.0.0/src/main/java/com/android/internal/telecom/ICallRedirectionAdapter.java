package com.android.internal.telecom;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.PhoneAccountHandle;

public interface ICallRedirectionAdapter extends IInterface {
    void cancelCall() throws RemoteException;

    void placeCallUnmodified() throws RemoteException;

    void redirectCall(Uri uri, PhoneAccountHandle phoneAccountHandle, boolean z) throws RemoteException;

    public static class Default implements ICallRedirectionAdapter {
        @Override // com.android.internal.telecom.ICallRedirectionAdapter
        public void cancelCall() throws RemoteException {
        }

        @Override // com.android.internal.telecom.ICallRedirectionAdapter
        public void placeCallUnmodified() throws RemoteException {
        }

        @Override // com.android.internal.telecom.ICallRedirectionAdapter
        public void redirectCall(Uri handle, PhoneAccountHandle targetPhoneAccount, boolean confirmFirst) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICallRedirectionAdapter {
        private static final String DESCRIPTOR = "com.android.internal.telecom.ICallRedirectionAdapter";
        static final int TRANSACTION_cancelCall = 1;
        static final int TRANSACTION_placeCallUnmodified = 2;
        static final int TRANSACTION_redirectCall = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICallRedirectionAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICallRedirectionAdapter)) {
                return new Proxy(obj);
            }
            return (ICallRedirectionAdapter) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "cancelCall";
            }
            if (transactionCode == 2) {
                return "placeCallUnmodified";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "redirectCall";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Uri _arg0;
            PhoneAccountHandle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                cancelCall();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                placeCallUnmodified();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = Uri.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                redirectCall(_arg0, _arg1, data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICallRedirectionAdapter {
            public static ICallRedirectionAdapter sDefaultImpl;
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

            @Override // com.android.internal.telecom.ICallRedirectionAdapter
            public void cancelCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancelCall();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.ICallRedirectionAdapter
            public void placeCallUnmodified() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().placeCallUnmodified();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.ICallRedirectionAdapter
            public void redirectCall(Uri handle, PhoneAccountHandle targetPhoneAccount, boolean confirmFirst) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    if (handle != null) {
                        _data.writeInt(1);
                        handle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (targetPhoneAccount != null) {
                        _data.writeInt(1);
                        targetPhoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (confirmFirst) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().redirectCall(handle, targetPhoneAccount, confirmFirst);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICallRedirectionAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICallRedirectionAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
