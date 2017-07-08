package android.hardware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface ISerialManager extends IInterface {

    public static abstract class Stub extends Binder implements ISerialManager {
        private static final String DESCRIPTOR = "android.hardware.ISerialManager";
        static final int TRANSACTION_getSerialPorts = 1;
        static final int TRANSACTION_openSerialPort = 2;

        private static class Proxy implements ISerialManager {
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

            public String[] getSerialPorts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSerialPorts, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor openSerialPort(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_openSerialPort, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISerialManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISerialManager)) {
                return new Proxy(obj);
            }
            return (ISerialManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_getSerialPorts /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result = getSerialPorts();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_openSerialPort /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    ParcelFileDescriptor _result2 = openSerialPort(data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getSerialPorts);
                        _result2.writeToParcel(reply, TRANSACTION_getSerialPorts);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String[] getSerialPorts() throws RemoteException;

    ParcelFileDescriptor openSerialPort(String str) throws RemoteException;
}
