package com.android.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.internal.IImsFeatureStatusCallback;
import com.android.ims.internal.IImsMMTelFeature;
import com.android.ims.internal.IImsRcsFeature;

public interface IImsServiceController extends IInterface {
    IImsMMTelFeature createEmergencyMMTelFeature(int i, IImsFeatureStatusCallback iImsFeatureStatusCallback) throws RemoteException;

    IImsMMTelFeature createMMTelFeature(int i, IImsFeatureStatusCallback iImsFeatureStatusCallback) throws RemoteException;

    IImsRcsFeature createRcsFeature(int i, IImsFeatureStatusCallback iImsFeatureStatusCallback) throws RemoteException;

    void removeImsFeature(int i, int i2, IImsFeatureStatusCallback iImsFeatureStatusCallback) throws RemoteException;

    public static class Default implements IImsServiceController {
        @Override // com.android.ims.internal.IImsServiceController
        public IImsMMTelFeature createEmergencyMMTelFeature(int slotId, IImsFeatureStatusCallback c) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.IImsServiceController
        public IImsMMTelFeature createMMTelFeature(int slotId, IImsFeatureStatusCallback c) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.IImsServiceController
        public IImsRcsFeature createRcsFeature(int slotId, IImsFeatureStatusCallback c) throws RemoteException {
            return null;
        }

        @Override // com.android.ims.internal.IImsServiceController
        public void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImsServiceController {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsServiceController";
        static final int TRANSACTION_createEmergencyMMTelFeature = 1;
        static final int TRANSACTION_createMMTelFeature = 2;
        static final int TRANSACTION_createRcsFeature = 3;
        static final int TRANSACTION_removeImsFeature = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsServiceController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsServiceController)) {
                return new Proxy(obj);
            }
            return (IImsServiceController) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "createEmergencyMMTelFeature";
            }
            if (transactionCode == 2) {
                return "createMMTelFeature";
            }
            if (transactionCode == 3) {
                return "createRcsFeature";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "removeImsFeature";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IImsMMTelFeature _result = createEmergencyMMTelFeature(data.readInt(), IImsFeatureStatusCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                if (_result != null) {
                    iBinder = _result.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                IImsMMTelFeature _result2 = createMMTelFeature(data.readInt(), IImsFeatureStatusCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                if (_result2 != null) {
                    iBinder = _result2.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                IImsRcsFeature _result3 = createRcsFeature(data.readInt(), IImsFeatureStatusCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                if (_result3 != null) {
                    iBinder = _result3.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                removeImsFeature(data.readInt(), data.readInt(), IImsFeatureStatusCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IImsServiceController {
            public static IImsServiceController sDefaultImpl;
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

            @Override // com.android.ims.internal.IImsServiceController
            public IImsMMTelFeature createEmergencyMMTelFeature(int slotId, IImsFeatureStatusCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createEmergencyMMTelFeature(slotId, c);
                    }
                    _reply.readException();
                    IImsMMTelFeature _result = IImsMMTelFeature.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsServiceController
            public IImsMMTelFeature createMMTelFeature(int slotId, IImsFeatureStatusCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createMMTelFeature(slotId, c);
                    }
                    _reply.readException();
                    IImsMMTelFeature _result = IImsMMTelFeature.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsServiceController
            public IImsRcsFeature createRcsFeature(int slotId, IImsFeatureStatusCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createRcsFeature(slotId, c);
                    }
                    _reply.readException();
                    IImsRcsFeature _result = IImsRcsFeature.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsServiceController
            public void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(featureType);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeImsFeature(slotId, featureType, c);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IImsServiceController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImsServiceController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
