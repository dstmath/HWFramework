package android.companion;

import android.app.PendingIntent;
import android.companion.IFindDeviceCallback;
import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface ICompanionDeviceManager extends IInterface {
    void associate(AssociationRequest associationRequest, IFindDeviceCallback iFindDeviceCallback, String str) throws RemoteException;

    void disassociate(String str, String str2) throws RemoteException;

    List<String> getAssociations(String str, int i) throws RemoteException;

    boolean hasNotificationAccess(ComponentName componentName) throws RemoteException;

    PendingIntent requestNotificationAccess(ComponentName componentName) throws RemoteException;

    void stopScan(AssociationRequest associationRequest, IFindDeviceCallback iFindDeviceCallback, String str) throws RemoteException;

    public static class Default implements ICompanionDeviceManager {
        @Override // android.companion.ICompanionDeviceManager
        public void associate(AssociationRequest request, IFindDeviceCallback callback, String callingPackage) throws RemoteException {
        }

        @Override // android.companion.ICompanionDeviceManager
        public void stopScan(AssociationRequest request, IFindDeviceCallback callback, String callingPackage) throws RemoteException {
        }

        @Override // android.companion.ICompanionDeviceManager
        public List<String> getAssociations(String callingPackage, int userId) throws RemoteException {
            return null;
        }

        @Override // android.companion.ICompanionDeviceManager
        public void disassociate(String deviceMacAddress, String callingPackage) throws RemoteException {
        }

        @Override // android.companion.ICompanionDeviceManager
        public boolean hasNotificationAccess(ComponentName component) throws RemoteException {
            return false;
        }

        @Override // android.companion.ICompanionDeviceManager
        public PendingIntent requestNotificationAccess(ComponentName component) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICompanionDeviceManager {
        private static final String DESCRIPTOR = "android.companion.ICompanionDeviceManager";
        static final int TRANSACTION_associate = 1;
        static final int TRANSACTION_disassociate = 4;
        static final int TRANSACTION_getAssociations = 3;
        static final int TRANSACTION_hasNotificationAccess = 5;
        static final int TRANSACTION_requestNotificationAccess = 6;
        static final int TRANSACTION_stopScan = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICompanionDeviceManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICompanionDeviceManager)) {
                return new Proxy(obj);
            }
            return (ICompanionDeviceManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "associate";
                case 2:
                    return "stopScan";
                case 3:
                    return "getAssociations";
                case 4:
                    return "disassociate";
                case 5:
                    return "hasNotificationAccess";
                case 6:
                    return "requestNotificationAccess";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AssociationRequest _arg0;
            AssociationRequest _arg02;
            ComponentName _arg03;
            ComponentName _arg04;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AssociationRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        associate(_arg0, IFindDeviceCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = AssociationRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        stopScan(_arg02, IFindDeviceCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result = getAssociations(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        disassociate(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean hasNotificationAccess = hasNotificationAccess(_arg03);
                        reply.writeNoException();
                        reply.writeInt(hasNotificationAccess ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        PendingIntent _result2 = requestNotificationAccess(_arg04);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICompanionDeviceManager {
            public static ICompanionDeviceManager sDefaultImpl;
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

            @Override // android.companion.ICompanionDeviceManager
            public void associate(AssociationRequest request, IFindDeviceCallback callback, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().associate(request, callback, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.companion.ICompanionDeviceManager
            public void stopScan(AssociationRequest request, IFindDeviceCallback callback, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopScan(request, callback, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.companion.ICompanionDeviceManager
            public List<String> getAssociations(String callingPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAssociations(callingPackage, userId);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.companion.ICompanionDeviceManager
            public void disassociate(String deviceMacAddress, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceMacAddress);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disassociate(deviceMacAddress, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.companion.ICompanionDeviceManager
            public boolean hasNotificationAccess(ComponentName component) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasNotificationAccess(component);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.companion.ICompanionDeviceManager
            public PendingIntent requestNotificationAccess(ComponentName component) throws RemoteException {
                PendingIntent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestNotificationAccess(component);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PendingIntent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICompanionDeviceManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICompanionDeviceManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
