package android.os;

public interface IHardwarePropertiesManager extends IInterface {

    public static abstract class Stub extends Binder implements IHardwarePropertiesManager {
        private static final String DESCRIPTOR = "android.os.IHardwarePropertiesManager";
        static final int TRANSACTION_getCpuUsages = 2;
        static final int TRANSACTION_getDeviceTemperatures = 1;
        static final int TRANSACTION_getFanSpeeds = 3;

        private static class Proxy implements IHardwarePropertiesManager {
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

            public float[] getDeviceTemperatures(String callingPackage, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    float[] _result = _reply.createFloatArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CpuUsageInfo[] getCpuUsages(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    CpuUsageInfo[] _result = (CpuUsageInfo[]) _reply.createTypedArray(CpuUsageInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float[] getFanSpeeds(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    float[] _result = _reply.createFloatArray();
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

        public static IHardwarePropertiesManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHardwarePropertiesManager)) {
                return new Proxy(obj);
            }
            return (IHardwarePropertiesManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            float[] _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDeviceTemperatures(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeFloatArray(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    CpuUsageInfo[] _result2 = getCpuUsages(data.readString());
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, 1);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getFanSpeeds(data.readString());
                    reply.writeNoException();
                    reply.writeFloatArray(_result);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    CpuUsageInfo[] getCpuUsages(String str) throws RemoteException;

    float[] getDeviceTemperatures(String str, int i, int i2) throws RemoteException;

    float[] getFanSpeeds(String str) throws RemoteException;
}
