package android.vrsystem;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import java.util.List;

public interface IVRListener extends IInterface {
    void onBatteryChanged(Intent intent) throws RemoteException;

    void onCallback(String str, List list) throws RemoteException;

    void onHeartBeatChanged(boolean z) throws RemoteException;

    void onHelmetChanged(boolean z) throws RemoteException;

    void onModeChanged(boolean z) throws RemoteException;

    void onNetworkStateChanged(Intent intent) throws RemoteException;

    void onNewNotification(StatusBarNotification statusBarNotification) throws RemoteException;

    void onNewSMS(Intent intent) throws RemoteException;

    void onPhoneStateChanged(int i, int i2, String str) throws RemoteException;

    public static class Default implements IVRListener {
        @Override // android.vrsystem.IVRListener
        public void onModeChanged(boolean isVR) throws RemoteException {
        }

        @Override // android.vrsystem.IVRListener
        public void onPhoneStateChanged(int subId, int state, String incomingNumber) throws RemoteException {
        }

        @Override // android.vrsystem.IVRListener
        public void onNewSMS(Intent intent) throws RemoteException {
        }

        @Override // android.vrsystem.IVRListener
        public void onNewNotification(StatusBarNotification sbn) throws RemoteException {
        }

        @Override // android.vrsystem.IVRListener
        public void onBatteryChanged(Intent intent) throws RemoteException {
        }

        @Override // android.vrsystem.IVRListener
        public void onNetworkStateChanged(Intent intent) throws RemoteException {
        }

        @Override // android.vrsystem.IVRListener
        public void onHelmetChanged(boolean up) throws RemoteException {
        }

        @Override // android.vrsystem.IVRListener
        public void onHeartBeatChanged(boolean connected) throws RemoteException {
        }

        @Override // android.vrsystem.IVRListener
        public void onCallback(String callbackType, List params) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVRListener {
        private static final String DESCRIPTOR = "android.vrsystem.IVRListener";
        static final int TRANSACTION_onBatteryChanged = 5;
        static final int TRANSACTION_onCallback = 9;
        static final int TRANSACTION_onHeartBeatChanged = 8;
        static final int TRANSACTION_onHelmetChanged = 7;
        static final int TRANSACTION_onModeChanged = 1;
        static final int TRANSACTION_onNetworkStateChanged = 6;
        static final int TRANSACTION_onNewNotification = 4;
        static final int TRANSACTION_onNewSMS = 3;
        static final int TRANSACTION_onPhoneStateChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVRListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVRListener)) {
                return new Proxy(obj);
            }
            return (IVRListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onModeChanged";
                case 2:
                    return "onPhoneStateChanged";
                case 3:
                    return "onNewSMS";
                case 4:
                    return "onNewNotification";
                case 5:
                    return "onBatteryChanged";
                case 6:
                    return "onNetworkStateChanged";
                case 7:
                    return "onHelmetChanged";
                case 8:
                    return "onHeartBeatChanged";
                case 9:
                    return "onCallback";
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
            Intent _arg0;
            StatusBarNotification _arg02;
            Intent _arg03;
            Intent _arg04;
            if (code != 1598968902) {
                boolean _arg05 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        onModeChanged(_arg05);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onPhoneStateChanged(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onNewSMS(_arg0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = StatusBarNotification.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onNewNotification(_arg02);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onBatteryChanged(_arg03);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        onNetworkStateChanged(_arg04);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        onHelmetChanged(_arg05);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        onHeartBeatChanged(_arg05);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onCallback(data.readString(), data.readArrayList(getClass().getClassLoader()));
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
        public static class Proxy implements IVRListener {
            public static IVRListener sDefaultImpl;
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

            @Override // android.vrsystem.IVRListener
            public void onModeChanged(boolean isVR) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isVR ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onModeChanged(isVR);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRListener
            public void onPhoneStateChanged(int subId, int state, String incomingNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(state);
                    _data.writeString(incomingNumber);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onPhoneStateChanged(subId, state, incomingNumber);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRListener
            public void onNewSMS(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onNewSMS(intent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRListener
            public void onNewNotification(StatusBarNotification sbn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sbn != null) {
                        _data.writeInt(1);
                        sbn.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onNewNotification(sbn);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRListener
            public void onBatteryChanged(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onBatteryChanged(intent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRListener
            public void onNetworkStateChanged(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onNetworkStateChanged(intent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRListener
            public void onHelmetChanged(boolean up) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(up ? 1 : 0);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onHelmetChanged(up);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRListener
            public void onHeartBeatChanged(boolean connected) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(connected ? 1 : 0);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onHeartBeatChanged(connected);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRListener
            public void onCallback(String callbackType, List params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callbackType);
                    _data.writeList(params);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCallback(callbackType, params);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVRListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVRListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
