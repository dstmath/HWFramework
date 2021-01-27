package com.huawei.nearbysdk;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IP2pDevFindListener extends IInterface {
    void updatePeers(List<WifiP2pDevice> list) throws RemoteException;

    public static abstract class Stub extends Binder implements IP2pDevFindListener {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.IP2pDevFindListener";
        static final int TRANSACTION_updatePeers = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IP2pDevFindListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IP2pDevFindListener)) {
                return new Proxy(obj);
            }
            return (IP2pDevFindListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    updatePeers(data.createTypedArrayList(WifiP2pDevice.CREATOR));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IP2pDevFindListener {
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

            @Override // com.huawei.nearbysdk.IP2pDevFindListener
            public void updatePeers(List<WifiP2pDevice> newPeers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(newPeers);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
