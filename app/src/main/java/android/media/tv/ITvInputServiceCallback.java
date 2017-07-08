package android.media.tv;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITvInputServiceCallback extends IInterface {

    public static abstract class Stub extends Binder implements ITvInputServiceCallback {
        private static final String DESCRIPTOR = "android.media.tv.ITvInputServiceCallback";
        static final int TRANSACTION_addHardwareInput = 1;
        static final int TRANSACTION_addHdmiInput = 2;
        static final int TRANSACTION_removeHardwareInput = 3;

        private static class Proxy implements ITvInputServiceCallback {
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

            public void addHardwareInput(int deviceId, TvInputInfo inputInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    if (inputInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_addHardwareInput);
                        inputInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addHardwareInput, _data, null, Stub.TRANSACTION_addHardwareInput);
                } finally {
                    _data.recycle();
                }
            }

            public void addHdmiInput(int id, TvInputInfo inputInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    if (inputInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_addHardwareInput);
                        inputInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addHdmiInput, _data, null, Stub.TRANSACTION_addHardwareInput);
                } finally {
                    _data.recycle();
                }
            }

            public void removeHardwareInput(String inputId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    this.mRemote.transact(Stub.TRANSACTION_removeHardwareInput, _data, null, Stub.TRANSACTION_addHardwareInput);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvInputServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvInputServiceCallback)) {
                return new Proxy(obj);
            }
            return (ITvInputServiceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            TvInputInfo tvInputInfo;
            switch (code) {
                case TRANSACTION_addHardwareInput /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        tvInputInfo = (TvInputInfo) TvInputInfo.CREATOR.createFromParcel(data);
                    } else {
                        tvInputInfo = null;
                    }
                    addHardwareInput(_arg0, tvInputInfo);
                    return true;
                case TRANSACTION_addHdmiInput /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        tvInputInfo = (TvInputInfo) TvInputInfo.CREATOR.createFromParcel(data);
                    } else {
                        tvInputInfo = null;
                    }
                    addHdmiInput(_arg0, tvInputInfo);
                    return true;
                case TRANSACTION_removeHardwareInput /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeHardwareInput(data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addHardwareInput(int i, TvInputInfo tvInputInfo) throws RemoteException;

    void addHdmiInput(int i, TvInputInfo tvInputInfo) throws RemoteException;

    void removeHardwareInput(String str) throws RemoteException;
}
