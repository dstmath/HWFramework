package android.hardware.hdmi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHdmiMhlVendorCommandListener extends IInterface {

    public static abstract class Stub extends Binder implements IHdmiMhlVendorCommandListener {
        private static final String DESCRIPTOR = "android.hardware.hdmi.IHdmiMhlVendorCommandListener";
        static final int TRANSACTION_onReceived = 1;

        private static class Proxy implements IHdmiMhlVendorCommandListener {
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

            public void onReceived(int portId, int offset, int length, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(portId);
                    _data.writeInt(offset);
                    _data.writeInt(length);
                    _data.writeByteArray(data);
                    this.mRemote.transact(Stub.TRANSACTION_onReceived, _data, null, Stub.TRANSACTION_onReceived);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHdmiMhlVendorCommandListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHdmiMhlVendorCommandListener)) {
                return new Proxy(obj);
            }
            return (IHdmiMhlVendorCommandListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onReceived /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onReceived(data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onReceived(int i, int i2, int i3, byte[] bArr) throws RemoteException;
}
