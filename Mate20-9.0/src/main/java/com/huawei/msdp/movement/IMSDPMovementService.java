package com.huawei.msdp.movement;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.msdp.movement.IMSDPMovementStatusChangeCallBack;

public interface IMSDPMovementService extends IInterface {

    public static abstract class Stub extends Binder implements IMSDPMovementService {
        private static final String DESCRIPTOR = "com.huawei.msdp.movement.IMSDPMovementService";
        static final int TRANSACTION_disableMovementEvent = 7;
        static final int TRANSACTION_enableMovementEvent = 6;
        static final int TRANSACTION_exitEnvironment = 11;
        static final int TRANSACTION_flush = 9;
        static final int TRANSACTION_getARVersion = 12;
        static final int TRANSACTION_getCurrentMovement = 8;
        static final int TRANSACTION_getServcieVersion = 1;
        static final int TRANSACTION_getSupportedModule = 2;
        static final int TRANSACTION_getSupportedMovements = 3;
        static final int TRANSACTION_initEnvironment = 10;
        static final int TRANSACTION_registerSink = 4;
        static final int TRANSACTION_unregisterSink = 5;

        private static class Proxy implements IMSDPMovementService {
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

            public String getServcieVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSupportedModule() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getSupportedMovements(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerSink(String packageName, IMSDPMovementStatusChangeCallBack sink) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unregisterSink(String packageName, IMSDPMovementStatusChangeCallBack sink) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableMovementEvent(int type, String packageName, String activity, int eventType, long reportLatencyNs, HwMSDPOtherParameters params) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(packageName);
                    _data.writeString(activity);
                    _data.writeInt(eventType);
                    _data.writeLong(reportLatencyNs);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableMovementEvent(int type, String packageName, String activity, int eventType) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(packageName);
                    _data.writeString(activity);
                    _data.writeInt(eventType);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwMSDPMovementChangeEvent getCurrentMovement(int type, String packageName) throws RemoteException {
                HwMSDPMovementChangeEvent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(packageName);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwMSDPMovementChangeEvent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean flush() throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean initEnvironment(String packageName, String environment, HwMSDPOtherParameters params) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(environment);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean exitEnvironment(String packageName, String environment, HwMSDPOtherParameters params) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(environment);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getARVersion(String packageName, int sdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(sdkVersion);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMSDPMovementService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMSDPMovementService)) {
                return new Proxy(obj);
            }
            return (IMSDPMovementService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwMSDPOtherParameters _arg2;
            HwMSDPOtherParameters _arg22;
            HwMSDPOtherParameters _arg5;
            int i = 0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    String _result = getServcieVersion();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = getSupportedModule();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result3 = getSupportedMovements(data.readInt());
                    reply.writeNoException();
                    reply.writeStringArray(_result3);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result4 = registerSink(data.readString(), IMSDPMovementStatusChangeCallBack.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result4 ? 1 : 0);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result5 = unregisterSink(data.readString(), IMSDPMovementStatusChangeCallBack.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result5) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    String _arg1 = data.readString();
                    String _arg23 = data.readString();
                    int _arg3 = data.readInt();
                    long _arg4 = data.readLong();
                    if (data.readInt() != 0) {
                        _arg5 = HwMSDPOtherParameters.CREATOR.createFromParcel(data);
                    } else {
                        _arg5 = null;
                    }
                    boolean _result6 = enableMovementEvent(_arg0, _arg1, _arg23, _arg3, _arg4, _arg5);
                    reply.writeNoException();
                    if (_result6) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result7 = disableMovementEvent(data.readInt(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result7) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    HwMSDPMovementChangeEvent _result8 = getCurrentMovement(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(1);
                        _result8.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result9 = flush();
                    reply.writeNoException();
                    if (_result9) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    String _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        _arg22 = HwMSDPOtherParameters.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    boolean _result10 = initEnvironment(_arg02, _arg12, _arg22);
                    reply.writeNoException();
                    if (_result10) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg03 = data.readString();
                    String _arg13 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = HwMSDPOtherParameters.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    boolean _result11 = exitEnvironment(_arg03, _arg13, _arg2);
                    reply.writeNoException();
                    if (_result11) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    int _result12 = getARVersion(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result12);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean disableMovementEvent(int i, String str, String str2, int i2) throws RemoteException;

    boolean enableMovementEvent(int i, String str, String str2, int i2, long j, HwMSDPOtherParameters hwMSDPOtherParameters) throws RemoteException;

    boolean exitEnvironment(String str, String str2, HwMSDPOtherParameters hwMSDPOtherParameters) throws RemoteException;

    boolean flush() throws RemoteException;

    int getARVersion(String str, int i) throws RemoteException;

    HwMSDPMovementChangeEvent getCurrentMovement(int i, String str) throws RemoteException;

    String getServcieVersion() throws RemoteException;

    int getSupportedModule() throws RemoteException;

    String[] getSupportedMovements(int i) throws RemoteException;

    boolean initEnvironment(String str, String str2, HwMSDPOtherParameters hwMSDPOtherParameters) throws RemoteException;

    boolean registerSink(String str, IMSDPMovementStatusChangeCallBack iMSDPMovementStatusChangeCallBack) throws RemoteException;

    boolean unregisterSink(String str, IMSDPMovementStatusChangeCallBack iMSDPMovementStatusChangeCallBack) throws RemoteException;
}
