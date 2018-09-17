package android.hardware.hdmi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHdmiHotplugEventListener extends IInterface {

    public static abstract class Stub extends Binder implements IHdmiHotplugEventListener {
        private static final String DESCRIPTOR = "android.hardware.hdmi.IHdmiHotplugEventListener";
        static final int TRANSACTION_onReceived = 1;

        private static class Proxy implements IHdmiHotplugEventListener {
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

            public void onReceived(HdmiHotplugEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(Stub.TRANSACTION_onReceived);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onReceived, _data, null, Stub.TRANSACTION_onReceived);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHdmiHotplugEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHdmiHotplugEventListener)) {
                return new Proxy(obj);
            }
            return (IHdmiHotplugEventListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onReceived /*1*/:
                    HdmiHotplugEvent hdmiHotplugEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        hdmiHotplugEvent = (HdmiHotplugEvent) HdmiHotplugEvent.CREATOR.createFromParcel(data);
                    } else {
                        hdmiHotplugEvent = null;
                    }
                    onReceived(hdmiHotplugEvent);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onReceived(HdmiHotplugEvent hdmiHotplugEvent) throws RemoteException;
}
