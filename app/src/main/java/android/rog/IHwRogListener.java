package android.rog;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwRogListener extends IInterface {

    public static abstract class Stub extends Binder implements IHwRogListener {
        private static final String DESCRIPTOR = "android.rog.IHwRogListener";
        static final int TRANSACTION_getPackageName = 1;
        static final int TRANSACTION_onRogInfoUpdated = 3;
        static final int TRANSACTION_onRogSwitchStateChanged = 2;

        private static class Proxy implements IHwRogListener {
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

            public String getPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPackageName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRogSwitchStateChanged(boolean state, AppRogInfo rogInfo) throws RemoteException {
                int i = Stub.TRANSACTION_getPackageName;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!state) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (rogInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getPackageName);
                        rogInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onRogSwitchStateChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRogInfoUpdated(AppRogInfo rogInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rogInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getPackageName);
                        rogInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onRogInfoUpdated, _data, _reply, 0);
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

        public static IHwRogListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwRogListener)) {
                return new Proxy(obj);
            }
            return (IHwRogListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_getPackageName /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result = getPackageName();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_onRogSwitchStateChanged /*2*/:
                    AppRogInfo appRogInfo;
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        appRogInfo = (AppRogInfo) AppRogInfo.CREATOR.createFromParcel(data);
                    } else {
                        appRogInfo = null;
                    }
                    onRogSwitchStateChanged(_arg0, appRogInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onRogInfoUpdated /*3*/:
                    AppRogInfo appRogInfo2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        appRogInfo2 = (AppRogInfo) AppRogInfo.CREATOR.createFromParcel(data);
                    } else {
                        appRogInfo2 = null;
                    }
                    onRogInfoUpdated(appRogInfo2);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String getPackageName() throws RemoteException;

    void onRogInfoUpdated(AppRogInfo appRogInfo) throws RemoteException;

    void onRogSwitchStateChanged(boolean z, AppRogInfo appRogInfo) throws RemoteException;
}
