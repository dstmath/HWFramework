package android.hardware.hivrar;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHivrarService extends IInterface {
    public static final int ENABLE_STATUS_DIS = 0;
    public static final int ENABLE_STATUS_EN = 1;
    public static final int ERROR_ALREADY_EXISTS = 2;
    public static final int ERROR_HAL_DISABLED = 6;
    public static final int ERROR_HAL_IN_USE = 7;
    public static final int ERROR_HAL_NOT_AVAILABLE = 4;
    public static final int ERROR_ILLEGAL_ARGUMENT = 3;
    public static final int ERROR_INVALID_OPERATION = 8;
    public static final int ERROR_PERMISSION_DENIED = 1;
    public static final int ERROR_TIMED_OUT = 5;
    public static final int VR_EXTERNAL_DISPLAY = 1;
    public static final int VR_FPS_SCENE_GAME = 2;
    public static final int VR_FPS_SCENE_NORMAL = 0;
    public static final int VR_FPS_SCENE_VIDEO = 1;
    public static final int VR_LCD_MODE_EXTERNAL_DUAL = 2;
    public static final int VR_LCD_MODE_PRIMARY_DUAL = 1;
    public static final int VR_LCD_MODE_PRIMARY_SINGLE = 0;
    public static final int VR_POWER_HINT_NONE = 0;
    public static final int VR_POWER_HINT_SUSTAINED_PERFORMANCE = 1;
    public static final int VR_POWER_HINT_VR_MODE = 2;
    public static final int VR_PRIMARY_DISPLAY = 0;

    public static abstract class Stub extends Binder implements IHivrarService {
        private static final String DESCRIPTOR = "android.hardware.hivrar.IHivrarService";
        static final int TRANSACTION_vrAutoRefresh = 9;
        static final int TRANSACTION_vrDeinit = 2;
        static final int TRANSACTION_vrGetVsyncTimeStamp = 8;
        static final int TRANSACTION_vrInit = 1;
        static final int TRANSACTION_vrLcdMode = 5;
        static final int TRANSACTION_vrMode = 3;
        static final int TRANSACTION_vrPowerMode = 4;
        static final int TRANSACTION_vrSceneType = 6;
        static final int TRANSACTION_vrSetSchedFifo = 7;

        private static class Proxy implements IHivrarService {
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

            public int vrInit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int vrDeinit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int vrMode(int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int vrPowerMode(int mode, int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeInt(enable);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int vrLcdMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int vrSceneType(int scene) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scene);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int vrSetSchedFifo(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public double vrGetVsyncTimeStamp(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    double _result = _reply.readDouble();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int vrAutoRefresh(int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHivrarService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHivrarService)) {
                return new Proxy(obj);
            }
            return (IHivrarService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vrInit();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vrDeinit();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vrMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vrPowerMode(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vrLcdMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vrSceneType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vrSetSchedFifo(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    double _result2 = vrGetVsyncTimeStamp(data.readInt());
                    reply.writeNoException();
                    reply.writeDouble(_result2);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = vrAutoRefresh(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int vrAutoRefresh(int i) throws RemoteException;

    int vrDeinit() throws RemoteException;

    double vrGetVsyncTimeStamp(int i) throws RemoteException;

    int vrInit() throws RemoteException;

    int vrLcdMode(int i) throws RemoteException;

    int vrMode(int i) throws RemoteException;

    int vrPowerMode(int i, int i2) throws RemoteException;

    int vrSceneType(int i) throws RemoteException;

    int vrSetSchedFifo(int i) throws RemoteException;
}
