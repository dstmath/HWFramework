package com.huawei.nearbysdk.DTCP;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nearbysdk.IPublishListener;
import com.huawei.nearbysdk.NearbyDevice;

public interface IDTCPService extends IInterface {

    public static abstract class Stub extends Binder implements IDTCPService {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.DTCP.IDTCPService";
        static final int TRANSACTION_publish = 1;
        static final int TRANSACTION_registerReceivelistener = 3;
        static final int TRANSACTION_sendFile = 5;
        static final int TRANSACTION_sendText = 6;
        static final int TRANSACTION_setHwIDInfo = 7;
        static final int TRANSACTION_unPublish = 2;
        static final int TRANSACTION_unRegisterReceivelistener = 4;

        private static class Proxy implements IDTCPService {
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

            public int publish(IPublishListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unPublish(IPublishListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int registerReceivelistener(IDTCPReceiveListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unRegisterReceivelistener(IDTCPReceiveListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IDTCPSender sendFile(NearbyDevice recvDevice, int timeout, Uri[] fileUriList, ITransmitCallback transCallback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (recvDevice != null) {
                        _data.writeInt(1);
                        recvDevice.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeout);
                    _data.writeTypedArray(fileUriList, 0);
                    if (transCallback != null) {
                        iBinder = transCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    IDTCPSender _result = com.huawei.nearbysdk.DTCP.IDTCPSender.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IDTCPSender sendText(NearbyDevice recvDevice, int timeout, String text, ITransmitCallback transCallback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (recvDevice != null) {
                        _data.writeInt(1);
                        recvDevice.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeout);
                    _data.writeString(text);
                    if (transCallback != null) {
                        iBinder = transCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    IDTCPSender _result = com.huawei.nearbysdk.DTCP.IDTCPSender.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setHwIDInfo(String nickName, byte[] headImage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nickName);
                    _data.writeByteArray(headImage);
                    this.mRemote.transact(7, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDTCPService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDTCPService)) {
                return new Proxy(obj);
            }
            return (IDTCPService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            NearbyDevice _arg0;
            IDTCPSender _result2;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = publish(com.huawei.nearbysdk.IPublishListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unPublish(com.huawei.nearbysdk.IPublishListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerReceivelistener(com.huawei.nearbysdk.DTCP.IDTCPReceiveListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unRegisterReceivelistener(com.huawei.nearbysdk.DTCP.IDTCPReceiveListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (NearbyDevice) NearbyDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result2 = sendFile(_arg0, data.readInt(), (Uri[]) data.createTypedArray(Uri.CREATOR), com.huawei.nearbysdk.DTCP.ITransmitCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (NearbyDevice) NearbyDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _result2 = sendText(_arg0, data.readInt(), data.readString(), com.huawei.nearbysdk.DTCP.ITransmitCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeStrongBinder(_result2 != null ? _result2.asBinder() : null);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = setHwIDInfo(data.readString(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int publish(IPublishListener iPublishListener) throws RemoteException;

    int registerReceivelistener(IDTCPReceiveListener iDTCPReceiveListener) throws RemoteException;

    IDTCPSender sendFile(NearbyDevice nearbyDevice, int i, Uri[] uriArr, ITransmitCallback iTransmitCallback) throws RemoteException;

    IDTCPSender sendText(NearbyDevice nearbyDevice, int i, String str, ITransmitCallback iTransmitCallback) throws RemoteException;

    boolean setHwIDInfo(String str, byte[] bArr) throws RemoteException;

    int unPublish(IPublishListener iPublishListener) throws RemoteException;

    int unRegisterReceivelistener(IDTCPReceiveListener iDTCPReceiveListener) throws RemoteException;
}
