package android.service.notification;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IConditionProvider extends IInterface {
    void onConnected() throws RemoteException;

    void onSubscribe(Uri uri) throws RemoteException;

    void onUnsubscribe(Uri uri) throws RemoteException;

    public static class Default implements IConditionProvider {
        @Override // android.service.notification.IConditionProvider
        public void onConnected() throws RemoteException {
        }

        @Override // android.service.notification.IConditionProvider
        public void onSubscribe(Uri conditionId) throws RemoteException {
        }

        @Override // android.service.notification.IConditionProvider
        public void onUnsubscribe(Uri conditionId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IConditionProvider {
        private static final String DESCRIPTOR = "android.service.notification.IConditionProvider";
        static final int TRANSACTION_onConnected = 1;
        static final int TRANSACTION_onSubscribe = 2;
        static final int TRANSACTION_onUnsubscribe = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConditionProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConditionProvider)) {
                return new Proxy(obj);
            }
            return (IConditionProvider) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onConnected";
            }
            if (transactionCode == 2) {
                return "onSubscribe";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onUnsubscribe";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Uri _arg0;
            Uri _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onConnected();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = Uri.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onSubscribe(_arg0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = Uri.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onUnsubscribe(_arg02);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IConditionProvider {
            public static IConditionProvider sDefaultImpl;
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

            @Override // android.service.notification.IConditionProvider
            public void onConnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConnected();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.notification.IConditionProvider
            public void onSubscribe(Uri conditionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (conditionId != null) {
                        _data.writeInt(1);
                        conditionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSubscribe(conditionId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.notification.IConditionProvider
            public void onUnsubscribe(Uri conditionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (conditionId != null) {
                        _data.writeInt(1);
                        conditionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUnsubscribe(conditionId);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IConditionProvider impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IConditionProvider getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
