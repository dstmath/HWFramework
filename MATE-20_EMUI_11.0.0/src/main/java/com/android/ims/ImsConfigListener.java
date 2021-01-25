package com.android.ims;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ImsConfigListener extends IInterface {
    void onGetFeatureResponse(int i, int i2, int i3, int i4) throws RemoteException;

    void onGetVideoQuality(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void onSetFeatureResponse(int i, int i2, int i3, int i4) throws RemoteException;

    void onSetVideoQuality(int i) throws RemoteException;

    public static class Default implements ImsConfigListener {
        @Override // com.android.ims.ImsConfigListener
        public void onGetFeatureResponse(int feature, int network, int value, int status) throws RemoteException {
        }

        @Override // com.android.ims.ImsConfigListener
        public void onSetFeatureResponse(int feature, int network, int value, int status) throws RemoteException {
        }

        @Override // com.android.ims.ImsConfigListener
        public void onGetVideoQuality(int status, int quality) throws RemoteException {
        }

        @Override // com.android.ims.ImsConfigListener
        public void onSetVideoQuality(int status) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ImsConfigListener {
        private static final String DESCRIPTOR = "com.android.ims.ImsConfigListener";
        static final int TRANSACTION_onGetFeatureResponse = 1;
        static final int TRANSACTION_onGetVideoQuality = 3;
        static final int TRANSACTION_onSetFeatureResponse = 2;
        static final int TRANSACTION_onSetVideoQuality = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ImsConfigListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ImsConfigListener)) {
                return new Proxy(obj);
            }
            return (ImsConfigListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onGetFeatureResponse";
            }
            if (transactionCode == 2) {
                return "onSetFeatureResponse";
            }
            if (transactionCode == 3) {
                return "onGetVideoQuality";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onSetVideoQuality";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onGetFeatureResponse(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onSetFeatureResponse(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onGetVideoQuality(data.readInt(), data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onSetVideoQuality(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ImsConfigListener {
            public static ImsConfigListener sDefaultImpl;
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

            @Override // com.android.ims.ImsConfigListener
            public void onGetFeatureResponse(int feature, int network, int value, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(network);
                    _data.writeInt(value);
                    _data.writeInt(status);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGetFeatureResponse(feature, network, value, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.ImsConfigListener
            public void onSetFeatureResponse(int feature, int network, int value, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(network);
                    _data.writeInt(value);
                    _data.writeInt(status);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSetFeatureResponse(feature, network, value, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.ImsConfigListener
            public void onGetVideoQuality(int status, int quality) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(quality);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGetVideoQuality(status, quality);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.ImsConfigListener
            public void onSetVideoQuality(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSetVideoQuality(status);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ImsConfigListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ImsConfigListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
