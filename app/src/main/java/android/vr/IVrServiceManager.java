package android.vr;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVrServiceManager extends IInterface {

    public static abstract class Stub extends Binder implements IVrServiceManager {
        private static final String DESCRIPTOR = "android.vr.IVrServiceManager";
        static final int TRANSACTION_getVsync = 1;
        static final int TRANSACTION_setSchedFifo = 4;
        static final int TRANSACTION_startFrontBufferDisplay = 2;
        static final int TRANSACTION_stopFrontBufferDisplay = 3;

        private static class Proxy implements IVrServiceManager {
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

            public double getVsync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVsync, _data, _reply, 0);
                    _reply.readException();
                    double _result = _reply.readDouble();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startFrontBufferDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_startFrontBufferDisplay, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopFrontBufferDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopFrontBufferDisplay, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setSchedFifo(int tid, int rtPriority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tid);
                    _data.writeInt(rtPriority);
                    this.mRemote.transact(Stub.TRANSACTION_setSchedFifo, _data, _reply, 0);
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

        public static IVrServiceManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVrServiceManager)) {
                return new Proxy(obj);
            }
            return (IVrServiceManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            boolean _result;
            switch (code) {
                case TRANSACTION_getVsync /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    double _result2 = getVsync();
                    reply.writeNoException();
                    reply.writeDouble(_result2);
                    return true;
                case TRANSACTION_startFrontBufferDisplay /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = startFrontBufferDisplay();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_getVsync;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_stopFrontBufferDisplay /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = stopFrontBufferDisplay();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_getVsync;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_setSchedFifo /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result3 = setSchedFifo(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    double getVsync() throws RemoteException;

    int setSchedFifo(int i, int i2) throws RemoteException;

    boolean startFrontBufferDisplay() throws RemoteException;

    boolean stopFrontBufferDisplay() throws RemoteException;
}
