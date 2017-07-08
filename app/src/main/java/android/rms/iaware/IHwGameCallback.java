package android.rms.iaware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwGameCallback extends IInterface {

    public static abstract class Stub extends Binder implements IHwGameCallback {
        private static final String DESCRIPTOR = "android.rms.iaware.IHwGameCallback";
        static final int TRANSACTION_changeContinuousFpsMissedRate = 4;
        static final int TRANSACTION_changeDxFpsRate = 5;
        static final int TRANSACTION_changeFpsRate = 1;
        static final int TRANSACTION_changeMuteEnabled = 3;
        static final int TRANSACTION_changeSpecialEffects = 2;
        static final int TRANSACTION_getPid = 7;
        static final int TRANSACTION_queryExpectedFps = 6;

        private static class Proxy implements IHwGameCallback {
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

            public void changeFpsRate(int fps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fps);
                    this.mRemote.transact(Stub.TRANSACTION_changeFpsRate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void changeSpecialEffects(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    this.mRemote.transact(Stub.TRANSACTION_changeSpecialEffects, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void changeMuteEnabled(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_changeFpsRate;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_changeMuteEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void changeContinuousFpsMissedRate(int cycle, int maxFrameMissed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cycle);
                    _data.writeInt(maxFrameMissed);
                    this.mRemote.transact(Stub.TRANSACTION_changeContinuousFpsMissedRate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void changeDxFpsRate(int cycle, float maxFrameDx) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cycle);
                    _data.writeFloat(maxFrameDx);
                    this.mRemote.transact(Stub.TRANSACTION_changeDxFpsRate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void queryExpectedFps(int[] outExpectedFps, int[] outRealFps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (outExpectedFps == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(outExpectedFps.length);
                    }
                    if (outRealFps == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(outRealFps.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_queryExpectedFps, _data, _reply, 0);
                    _reply.readException();
                    _reply.readIntArray(outExpectedFps);
                    _reply.readIntArray(outRealFps);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPid, _data, _reply, 0);
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

        public static IHwGameCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwGameCallback)) {
                return new Proxy(obj);
            }
            return (IHwGameCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_changeFpsRate /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    changeFpsRate(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_changeSpecialEffects /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    changeSpecialEffects(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_changeMuteEnabled /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    changeMuteEnabled(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_changeContinuousFpsMissedRate /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    changeContinuousFpsMissedRate(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_changeDxFpsRate /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    changeDxFpsRate(data.readInt(), data.readFloat());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_queryExpectedFps /*6*/:
                    int[] iArr;
                    int[] iArr2;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0_length = data.readInt();
                    if (_arg0_length < 0) {
                        iArr = null;
                    } else {
                        iArr = new int[_arg0_length];
                    }
                    int _arg1_length = data.readInt();
                    if (_arg1_length < 0) {
                        iArr2 = null;
                    } else {
                        iArr2 = new int[_arg1_length];
                    }
                    queryExpectedFps(iArr, iArr2);
                    reply.writeNoException();
                    reply.writeIntArray(iArr);
                    reply.writeIntArray(iArr2);
                    return true;
                case TRANSACTION_getPid /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result = getPid();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void changeContinuousFpsMissedRate(int i, int i2) throws RemoteException;

    void changeDxFpsRate(int i, float f) throws RemoteException;

    void changeFpsRate(int i) throws RemoteException;

    void changeMuteEnabled(boolean z) throws RemoteException;

    void changeSpecialEffects(int i) throws RemoteException;

    int getPid() throws RemoteException;

    void queryExpectedFps(int[] iArr, int[] iArr2) throws RemoteException;
}
