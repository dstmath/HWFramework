package com.android.internal.textservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SuggestionsInfo;

public interface ISpellCheckerSessionListener extends IInterface {

    public static abstract class Stub extends Binder implements ISpellCheckerSessionListener {
        private static final String DESCRIPTOR = "com.android.internal.textservice.ISpellCheckerSessionListener";
        static final int TRANSACTION_onGetSentenceSuggestions = 2;
        static final int TRANSACTION_onGetSuggestions = 1;

        private static class Proxy implements ISpellCheckerSessionListener {
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

            public void onGetSuggestions(SuggestionsInfo[] results) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(results, 0);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(result, 0);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISpellCheckerSessionListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISpellCheckerSessionListener)) {
                return new Proxy(obj);
            }
            return (ISpellCheckerSessionListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onGetSuggestions((SuggestionsInfo[]) data.createTypedArray(SuggestionsInfo.CREATOR));
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onGetSentenceSuggestions((SentenceSuggestionsInfo[]) data.createTypedArray(SentenceSuggestionsInfo.CREATOR));
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onGetSentenceSuggestions(SentenceSuggestionsInfo[] sentenceSuggestionsInfoArr) throws RemoteException;

    void onGetSuggestions(SuggestionsInfo[] suggestionsInfoArr) throws RemoteException;
}
