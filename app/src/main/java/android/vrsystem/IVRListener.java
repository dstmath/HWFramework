package android.vrsystem;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;

public interface IVRListener extends IInterface {

    public static abstract class Stub extends Binder implements IVRListener {
        private static final String DESCRIPTOR = "android.vrsystem.IVRListener";
        static final int TRANSACTION_onBatteryChanged = 5;
        static final int TRANSACTION_onHeartBeatChanged = 8;
        static final int TRANSACTION_onHelmetChanged = 7;
        static final int TRANSACTION_onModeChanged = 1;
        static final int TRANSACTION_onNetworkStateChanged = 6;
        static final int TRANSACTION_onNewNotification = 4;
        static final int TRANSACTION_onNewSMS = 3;
        static final int TRANSACTION_onPhoneStateChanged = 2;

        private static class Proxy implements IVRListener {
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

            public void onModeChanged(boolean isVR) throws RemoteException {
                int i = Stub.TRANSACTION_onModeChanged;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!isVR) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onModeChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onPhoneStateChanged(int subId, int state, String incomingNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(state);
                    _data.writeString(incomingNumber);
                    this.mRemote.transact(Stub.TRANSACTION_onPhoneStateChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNewSMS(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_onModeChanged);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onNewSMS, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNewNotification(StatusBarNotification sbn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sbn != null) {
                        _data.writeInt(Stub.TRANSACTION_onModeChanged);
                        sbn.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onNewNotification, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onBatteryChanged(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_onModeChanged);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onBatteryChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNetworkStateChanged(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_onModeChanged);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onNetworkStateChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onHelmetChanged(boolean up) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (up) {
                        i = Stub.TRANSACTION_onModeChanged;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onHelmetChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onHeartBeatChanged(boolean connected) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connected) {
                        i = Stub.TRANSACTION_onModeChanged;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onHeartBeatChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            Intent intent;
            switch (code) {
                case TRANSACTION_onModeChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onModeChanged(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onPhoneStateChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPhoneStateChanged(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onNewSMS /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    onNewSMS(intent);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onNewNotification /*4*/:
                    StatusBarNotification statusBarNotification;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        statusBarNotification = (StatusBarNotification) StatusBarNotification.CREATOR.createFromParcel(data);
                    } else {
                        statusBarNotification = null;
                    }
                    onNewNotification(statusBarNotification);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onBatteryChanged /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    onBatteryChanged(intent);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onNetworkStateChanged /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    onNetworkStateChanged(intent);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onHelmetChanged /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onHelmetChanged(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onHeartBeatChanged /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onHeartBeatChanged(_arg0);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onBatteryChanged(Intent intent) throws RemoteException;

    void onHeartBeatChanged(boolean z) throws RemoteException;

    void onHelmetChanged(boolean z) throws RemoteException;

    void onModeChanged(boolean z) throws RemoteException;

    void onNetworkStateChanged(Intent intent) throws RemoteException;

    void onNewNotification(StatusBarNotification statusBarNotification) throws RemoteException;

    void onNewSMS(Intent intent) throws RemoteException;

    void onPhoneStateChanged(int i, int i2, String str) throws RemoteException;
}
