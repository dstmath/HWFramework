package com.huawei.android.os;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.haptic.IHwHapticPlayer;

public interface IHwVibrator extends IInterface {
    IHwHapticPlayer getHwHapticPlayer() throws RemoteException;

    String getHwParameter(String str) throws RemoteException;

    boolean isSupportHwVibrator(String str) throws RemoteException;

    void notifyVibrateOptions(IBinder iBinder, Bundle bundle) throws RemoteException;

    void setHwAmplitude(int i, String str, IBinder iBinder, String str2, int i2) throws RemoteException;

    void setHwParameter(String str) throws RemoteException;

    void setHwVibrator(int i, String str, IBinder iBinder, String str2) throws RemoteException;

    void setHwVibratorDelay(int i, String str, IBinder iBinder, String str2, int i2) throws RemoteException;

    void setHwVibratorRepeat(int i, String str, IBinder iBinder, String str2, int i2) throws RemoteException;

    void stopHwVibrator(int i, String str, IBinder iBinder, String str2) throws RemoteException;

    public static class Default implements IHwVibrator {
        @Override // com.huawei.android.os.IHwVibrator
        public boolean isSupportHwVibrator(String type) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.os.IHwVibrator
        public void setHwVibrator(int uid, String opPkg, IBinder token, String type) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwVibrator
        public void stopHwVibrator(int uid, String opPkg, IBinder token, String type) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwVibrator
        public void setHwParameter(String command) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwVibrator
        public String getHwParameter(String command) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.os.IHwVibrator
        public void notifyVibrateOptions(IBinder token, Bundle options) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwVibrator
        public void setHwVibratorDelay(int uid, String opPkg, IBinder token, String type, int delay) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwVibrator
        public void setHwVibratorRepeat(int uid, String opPkg, IBinder token, String type, int repeat) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwVibrator
        public void setHwAmplitude(int uid, String opPkg, IBinder token, String type, int amplitude) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwVibrator
        public IHwHapticPlayer getHwHapticPlayer() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwVibrator {
        private static final String DESCRIPTOR = "com.huawei.android.os.IHwVibrator";
        static final int TRANSACTION_getHwHapticPlayer = 10;
        static final int TRANSACTION_getHwParameter = 5;
        static final int TRANSACTION_isSupportHwVibrator = 1;
        static final int TRANSACTION_notifyVibrateOptions = 6;
        static final int TRANSACTION_setHwAmplitude = 9;
        static final int TRANSACTION_setHwParameter = 4;
        static final int TRANSACTION_setHwVibrator = 2;
        static final int TRANSACTION_setHwVibratorDelay = 7;
        static final int TRANSACTION_setHwVibratorRepeat = 8;
        static final int TRANSACTION_stopHwVibrator = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwVibrator asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwVibrator)) {
                return new Proxy(obj);
            }
            return (IHwVibrator) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "isSupportHwVibrator";
                case 2:
                    return "setHwVibrator";
                case 3:
                    return "stopHwVibrator";
                case 4:
                    return "setHwParameter";
                case 5:
                    return "getHwParameter";
                case 6:
                    return "notifyVibrateOptions";
                case 7:
                    return "setHwVibratorDelay";
                case 8:
                    return "setHwVibratorRepeat";
                case 9:
                    return "setHwAmplitude";
                case 10:
                    return "getHwHapticPlayer";
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
            Bundle _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSupportHwVibrator = isSupportHwVibrator(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isSupportHwVibrator ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setHwVibrator(data.readInt(), data.readString(), data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        stopHwVibrator(data.readInt(), data.readString(), data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setHwParameter(data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getHwParameter(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg0 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        notifyVibrateOptions(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setHwVibratorDelay(data.readInt(), data.readString(), data.readStrongBinder(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setHwVibratorRepeat(data.readInt(), data.readString(), data.readStrongBinder(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        setHwAmplitude(data.readInt(), data.readString(), data.readStrongBinder(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IHwHapticPlayer _result2 = getHwHapticPlayer();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
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
        public static class Proxy implements IHwVibrator {
            public static IHwVibrator sDefaultImpl;
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

            @Override // com.huawei.android.os.IHwVibrator
            public boolean isSupportHwVibrator(String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(type);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportHwVibrator(type);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwVibrator
            public void setHwVibrator(int uid, String opPkg, IBinder token, String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    _data.writeStrongBinder(token);
                    _data.writeString(type);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHwVibrator(uid, opPkg, token, type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwVibrator
            public void stopHwVibrator(int uid, String opPkg, IBinder token, String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    _data.writeStrongBinder(token);
                    _data.writeString(type);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopHwVibrator(uid, opPkg, token, type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwVibrator
            public void setHwParameter(String command) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHwParameter(command);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwVibrator
            public String getHwParameter(String command) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwParameter(command);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwVibrator
            public void notifyVibrateOptions(IBinder token, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyVibrateOptions(token, options);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwVibrator
            public void setHwVibratorDelay(int uid, String opPkg, IBinder token, String type, int delay) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    _data.writeStrongBinder(token);
                    _data.writeString(type);
                    _data.writeInt(delay);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHwVibratorDelay(uid, opPkg, token, type, delay);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwVibrator
            public void setHwVibratorRepeat(int uid, String opPkg, IBinder token, String type, int repeat) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    _data.writeStrongBinder(token);
                    _data.writeString(type);
                    _data.writeInt(repeat);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHwVibratorRepeat(uid, opPkg, token, type, repeat);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwVibrator
            public void setHwAmplitude(int uid, String opPkg, IBinder token, String type, int amplitude) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    _data.writeStrongBinder(token);
                    _data.writeString(type);
                    _data.writeInt(amplitude);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHwAmplitude(uid, opPkg, token, type, amplitude);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwVibrator
            public IHwHapticPlayer getHwHapticPlayer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwHapticPlayer();
                    }
                    _reply.readException();
                    IHwHapticPlayer _result = IHwHapticPlayer.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwVibrator impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwVibrator getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
