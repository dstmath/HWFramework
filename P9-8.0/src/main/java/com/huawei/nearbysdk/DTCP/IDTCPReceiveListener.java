package com.huawei.nearbysdk.DTCP;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo;

public interface IDTCPReceiveListener extends IInterface {

    public static abstract class Stub extends Binder implements IDTCPReceiveListener {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.DTCP.IDTCPReceiveListener";
        static final int TRANSACTION_onErrorBeforeConfirm = 4;
        static final int TRANSACTION_onPreviewReceive = 2;
        static final int TRANSACTION_onSendCancelBeforeConfirm = 3;
        static final int TRANSACTION_onStatusChanged = 1;

        private static class Proxy implements IDTCPReceiveListener {
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

            public void onStatusChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onPreviewReceive(BaseShareInfo shareInfo, IDTCPReceiver dtcpRecv) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (shareInfo != null) {
                        _data.writeInt(1);
                        shareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (dtcpRecv != null) {
                        iBinder = dtcpRecv.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onSendCancelBeforeConfirm(IDTCPReceiver dtcpRecv) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dtcpRecv != null) {
                        iBinder = dtcpRecv.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onErrorBeforeConfirm(IDTCPReceiver dtcpRecv, int errcode) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dtcpRecv != null) {
                        iBinder = dtcpRecv.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(errcode);
                    this.mRemote.transact(4, _data, _reply, 0);
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

        public static IDTCPReceiveListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDTCPReceiveListener)) {
                return new Proxy(obj);
            }
            return (IDTCPReceiveListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onStatusChanged(data.readInt());
                    reply.writeNoException();
                    return true;
                case 2:
                    BaseShareInfo _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (BaseShareInfo) BaseShareInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onPreviewReceive(_arg0, com.huawei.nearbysdk.DTCP.IDTCPReceiver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onSendCancelBeforeConfirm(com.huawei.nearbysdk.DTCP.IDTCPReceiver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onErrorBeforeConfirm(com.huawei.nearbysdk.DTCP.IDTCPReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onErrorBeforeConfirm(IDTCPReceiver iDTCPReceiver, int i) throws RemoteException;

    void onPreviewReceive(BaseShareInfo baseShareInfo, IDTCPReceiver iDTCPReceiver) throws RemoteException;

    void onSendCancelBeforeConfirm(IDTCPReceiver iDTCPReceiver) throws RemoteException;

    void onStatusChanged(int i) throws RemoteException;
}
