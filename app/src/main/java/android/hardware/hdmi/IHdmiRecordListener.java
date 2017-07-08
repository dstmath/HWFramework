package android.hardware.hdmi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHdmiRecordListener extends IInterface {

    public static abstract class Stub extends Binder implements IHdmiRecordListener {
        private static final String DESCRIPTOR = "android.hardware.hdmi.IHdmiRecordListener";
        static final int TRANSACTION_getOneTouchRecordSource = 1;
        static final int TRANSACTION_onClearTimerRecordingResult = 4;
        static final int TRANSACTION_onOneTouchRecordResult = 2;
        static final int TRANSACTION_onTimerRecordingResult = 3;

        private static class Proxy implements IHdmiRecordListener {
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

            public byte[] getOneTouchRecordSource(int recorderAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    this.mRemote.transact(Stub.TRANSACTION_getOneTouchRecordSource, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onOneTouchRecordResult(int recorderAddress, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(result);
                    this.mRemote.transact(Stub.TRANSACTION_onOneTouchRecordResult, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onTimerRecordingResult(int recorderAddress, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(result);
                    this.mRemote.transact(Stub.TRANSACTION_onTimerRecordingResult, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onClearTimerRecordingResult(int recorderAddress, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(result);
                    this.mRemote.transact(Stub.TRANSACTION_onClearTimerRecordingResult, _data, _reply, 0);
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

        public static IHdmiRecordListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHdmiRecordListener)) {
                return new Proxy(obj);
            }
            return (IHdmiRecordListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_getOneTouchRecordSource /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result = getOneTouchRecordSource(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    return true;
                case TRANSACTION_onOneTouchRecordResult /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onOneTouchRecordResult(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onTimerRecordingResult /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onTimerRecordingResult(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onClearTimerRecordingResult /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onClearTimerRecordingResult(data.readInt(), data.readInt());
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

    byte[] getOneTouchRecordSource(int i) throws RemoteException;

    void onClearTimerRecordingResult(int i, int i2) throws RemoteException;

    void onOneTouchRecordResult(int i, int i2) throws RemoteException;

    void onTimerRecordingResult(int i, int i2) throws RemoteException;
}
