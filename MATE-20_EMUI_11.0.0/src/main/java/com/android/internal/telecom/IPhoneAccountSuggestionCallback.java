package com.android.internal.telecom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.PhoneAccountSuggestion;
import java.util.List;

public interface IPhoneAccountSuggestionCallback extends IInterface {
    void suggestPhoneAccounts(String str, List<PhoneAccountSuggestion> list) throws RemoteException;

    public static class Default implements IPhoneAccountSuggestionCallback {
        @Override // com.android.internal.telecom.IPhoneAccountSuggestionCallback
        public void suggestPhoneAccounts(String number, List<PhoneAccountSuggestion> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPhoneAccountSuggestionCallback {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IPhoneAccountSuggestionCallback";
        static final int TRANSACTION_suggestPhoneAccounts = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPhoneAccountSuggestionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPhoneAccountSuggestionCallback)) {
                return new Proxy(obj);
            }
            return (IPhoneAccountSuggestionCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "suggestPhoneAccounts";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                suggestPhoneAccounts(data.readString(), data.createTypedArrayList(PhoneAccountSuggestion.CREATOR));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPhoneAccountSuggestionCallback {
            public static IPhoneAccountSuggestionCallback sDefaultImpl;
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

            @Override // com.android.internal.telecom.IPhoneAccountSuggestionCallback
            public void suggestPhoneAccounts(String number, List<PhoneAccountSuggestion> suggestions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    _data.writeTypedList(suggestions);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().suggestPhoneAccounts(number, suggestions);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPhoneAccountSuggestionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPhoneAccountSuggestionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
