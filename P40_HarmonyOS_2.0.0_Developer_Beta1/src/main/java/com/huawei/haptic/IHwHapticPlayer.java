package com.huawei.haptic;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwHapticPlayer extends IInterface {
    int getDuration(IBinder iBinder) throws RemoteException;

    boolean isPlaying(IBinder iBinder) throws RemoteException;

    int play(IBinder iBinder, int i, String str, HwHapticAttributes hwHapticAttributes, HwHapticWave hwHapticWave) throws RemoteException;

    boolean setDynamicCurve(IBinder iBinder, int i, int i2, HwHapticCurve hwHapticCurve) throws RemoteException;

    void setLooping(IBinder iBinder, boolean z) throws RemoteException;

    void setSwapHapticPos(IBinder iBinder, boolean z) throws RemoteException;

    void stop(IBinder iBinder) throws RemoteException;

    public static class Default implements IHwHapticPlayer {
        @Override // com.huawei.haptic.IHwHapticPlayer
        public void setLooping(IBinder token, boolean looping) throws RemoteException {
        }

        @Override // com.huawei.haptic.IHwHapticPlayer
        public void setSwapHapticPos(IBinder token, boolean swap) throws RemoteException {
        }

        @Override // com.huawei.haptic.IHwHapticPlayer
        public boolean isPlaying(IBinder token) throws RemoteException {
            return false;
        }

        @Override // com.huawei.haptic.IHwHapticPlayer
        public int play(IBinder token, int uid, String opPkg, HwHapticAttributes attr, HwHapticWave wave) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.haptic.IHwHapticPlayer
        public void stop(IBinder token) throws RemoteException {
        }

        @Override // com.huawei.haptic.IHwHapticPlayer
        public int getDuration(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.haptic.IHwHapticPlayer
        public boolean setDynamicCurve(IBinder token, int curveType, int flags, HwHapticCurve curve) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwHapticPlayer {
        private static final String DESCRIPTOR = "com.huawei.haptic.IHwHapticPlayer";
        static final int TRANSACTION_getDuration = 6;
        static final int TRANSACTION_isPlaying = 3;
        static final int TRANSACTION_play = 4;
        static final int TRANSACTION_setDynamicCurve = 7;
        static final int TRANSACTION_setLooping = 1;
        static final int TRANSACTION_setSwapHapticPos = 2;
        static final int TRANSACTION_stop = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwHapticPlayer asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwHapticPlayer)) {
                return new Proxy(obj);
            }
            return (IHwHapticPlayer) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setLooping";
                case 2:
                    return "setSwapHapticPos";
                case 3:
                    return "isPlaying";
                case 4:
                    return "play";
                case 5:
                    return "stop";
                case 6:
                    return "getDuration";
                case 7:
                    return "setDynamicCurve";
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
            HwHapticAttributes _arg3;
            HwHapticWave _arg4;
            HwHapticCurve _arg32;
            if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg0 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setLooping(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg02 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setSwapHapticPos(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPlaying = isPlaying(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(isPlaying ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg03 = data.readStrongBinder();
                        int _arg12 = data.readInt();
                        String _arg2 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = HwHapticAttributes.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = HwHapticWave.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        int _result = play(_arg03, _arg12, _arg2, _arg3, _arg4);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        stop(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getDuration(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        int _arg13 = data.readInt();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg32 = HwHapticCurve.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        boolean dynamicCurve = setDynamicCurve(_arg04, _arg13, _arg22, _arg32);
                        reply.writeNoException();
                        reply.writeInt(dynamicCurve ? 1 : 0);
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
        public static class Proxy implements IHwHapticPlayer {
            public static IHwHapticPlayer sDefaultImpl;
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

            @Override // com.huawei.haptic.IHwHapticPlayer
            public void setLooping(IBinder token, boolean looping) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(looping ? 1 : 0);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLooping(token, looping);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.haptic.IHwHapticPlayer
            public void setSwapHapticPos(IBinder token, boolean swap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(swap ? 1 : 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSwapHapticPos(token, swap);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.haptic.IHwHapticPlayer
            public boolean isPlaying(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPlaying(token);
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

            @Override // com.huawei.haptic.IHwHapticPlayer
            public int play(IBinder token, int uid, String opPkg, HwHapticAttributes attr, HwHapticWave wave) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    if (attr != null) {
                        _data.writeInt(1);
                        attr.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (wave != null) {
                        _data.writeInt(1);
                        wave.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().play(token, uid, opPkg, attr, wave);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.haptic.IHwHapticPlayer
            public void stop(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stop(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.haptic.IHwHapticPlayer
            public int getDuration(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDuration(token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.haptic.IHwHapticPlayer
            public boolean setDynamicCurve(IBinder token, int curveType, int flags, HwHapticCurve curve) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(curveType);
                    _data.writeInt(flags);
                    boolean _result = true;
                    if (curve != null) {
                        _data.writeInt(1);
                        curve.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDynamicCurve(token, curveType, flags, curve);
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
        }

        public static boolean setDefaultImpl(IHwHapticPlayer impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwHapticPlayer getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
