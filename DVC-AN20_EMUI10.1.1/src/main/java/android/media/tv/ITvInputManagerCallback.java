package android.media.tv;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITvInputManagerCallback extends IInterface {
    void onInputAdded(String str) throws RemoteException;

    void onInputRemoved(String str) throws RemoteException;

    void onInputStateChanged(String str, int i) throws RemoteException;

    void onInputUpdated(String str) throws RemoteException;

    void onTvInputInfoUpdated(TvInputInfo tvInputInfo) throws RemoteException;

    public static class Default implements ITvInputManagerCallback {
        @Override // android.media.tv.ITvInputManagerCallback
        public void onInputAdded(String inputId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManagerCallback
        public void onInputRemoved(String inputId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManagerCallback
        public void onInputUpdated(String inputId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManagerCallback
        public void onInputStateChanged(String inputId, int state) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManagerCallback
        public void onTvInputInfoUpdated(TvInputInfo TvInputInfo) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITvInputManagerCallback {
        private static final String DESCRIPTOR = "android.media.tv.ITvInputManagerCallback";
        static final int TRANSACTION_onInputAdded = 1;
        static final int TRANSACTION_onInputRemoved = 2;
        static final int TRANSACTION_onInputStateChanged = 4;
        static final int TRANSACTION_onInputUpdated = 3;
        static final int TRANSACTION_onTvInputInfoUpdated = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvInputManagerCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvInputManagerCallback)) {
                return new Proxy(obj);
            }
            return (ITvInputManagerCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onInputAdded";
            }
            if (transactionCode == 2) {
                return "onInputRemoved";
            }
            if (transactionCode == 3) {
                return "onInputUpdated";
            }
            if (transactionCode == 4) {
                return "onInputStateChanged";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onTvInputInfoUpdated";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            TvInputInfo _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onInputAdded(data.readString());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onInputRemoved(data.readString());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onInputUpdated(data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onInputStateChanged(data.readString(), data.readInt());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = TvInputInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onTvInputInfoUpdated(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITvInputManagerCallback {
            public static ITvInputManagerCallback sDefaultImpl;
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

            @Override // android.media.tv.ITvInputManagerCallback
            public void onInputAdded(String inputId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInputAdded(inputId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManagerCallback
            public void onInputRemoved(String inputId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInputRemoved(inputId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManagerCallback
            public void onInputUpdated(String inputId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInputUpdated(inputId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManagerCallback
            public void onInputStateChanged(String inputId, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    _data.writeInt(state);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInputStateChanged(inputId, state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManagerCallback
            public void onTvInputInfoUpdated(TvInputInfo TvInputInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (TvInputInfo != null) {
                        _data.writeInt(1);
                        TvInputInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTvInputInfoUpdated(TvInputInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITvInputManagerCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITvInputManagerCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
