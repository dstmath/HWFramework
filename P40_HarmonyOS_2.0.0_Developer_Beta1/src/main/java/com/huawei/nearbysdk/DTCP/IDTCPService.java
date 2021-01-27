package com.huawei.nearbysdk.DTCP;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nearbysdk.BleScanLevel;
import com.huawei.nearbysdk.DTCP.IDTCPReceiveListener;
import com.huawei.nearbysdk.DTCP.IDTCPSender;
import com.huawei.nearbysdk.DTCP.ITransmitCallback;
import com.huawei.nearbysdk.IInitReadyListener;
import com.huawei.nearbysdk.IPublishListener;
import com.huawei.nearbysdk.NearbyDevice;
import com.huawei.nearbysdk.NearbySendBean;

public interface IDTCPService extends IInterface {
    int publish(IPublishListener iPublishListener) throws RemoteException;

    int registerInitListener(IInitReadyListener iInitReadyListener) throws RemoteException;

    int registerReceivelistener(IDTCPReceiveListener iDTCPReceiveListener) throws RemoteException;

    IDTCPSender sendFile(NearbyDevice nearbyDevice, int i, Uri[] uriArr, ITransmitCallback iTransmitCallback) throws RemoteException;

    IDTCPSender sendMassiveFile(NearbyDevice nearbyDevice, int i, NearbySendBean nearbySendBean, ITransmitCallback iTransmitCallback) throws RemoteException;

    IDTCPSender sendText(NearbyDevice nearbyDevice, int i, String str, ITransmitCallback iTransmitCallback) throws RemoteException;

    boolean setHwIDInfo(String str, byte[] bArr) throws RemoteException;

    int setScanLevel(BleScanLevel bleScanLevel, long j) throws RemoteException;

    int unPublish(IPublishListener iPublishListener) throws RemoteException;

    int unRegisterReceivelistener(IDTCPReceiveListener iDTCPReceiveListener) throws RemoteException;

    public static abstract class Stub extends Binder implements IDTCPService {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.DTCP.IDTCPService";
        static final int TRANSACTION_publish = 1;
        static final int TRANSACTION_registerInitListener = 10;
        static final int TRANSACTION_registerReceivelistener = 3;
        static final int TRANSACTION_sendFile = 5;
        static final int TRANSACTION_sendMassiveFile = 8;
        static final int TRANSACTION_sendText = 6;
        static final int TRANSACTION_setHwIDInfo = 7;
        static final int TRANSACTION_setScanLevel = 9;
        static final int TRANSACTION_unPublish = 2;
        static final int TRANSACTION_unRegisterReceivelistener = 4;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BleScanLevel _arg0;
            NearbyDevice _arg02;
            NearbySendBean _arg2;
            NearbyDevice _arg03;
            NearbyDevice _arg04;
            IBinder iBinder = null;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    int _result = publish(IPublishListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = unPublish(IPublishListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    int _result3 = registerReceivelistener(IDTCPReceiveListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    int _result4 = unRegisterReceivelistener(IDTCPReceiveListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = NearbyDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    IDTCPSender _result5 = sendFile(_arg04, data.readInt(), (Uri[]) data.createTypedArray(Uri.CREATOR), ITransmitCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result5 != null) {
                        iBinder = _result5.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = NearbyDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    IDTCPSender _result6 = sendText(_arg03, data.readInt(), data.readString(), ITransmitCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result6 != null) {
                        iBinder = _result6.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result7 = setHwIDInfo(data.readString(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result7 ? 1 : 0);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = NearbyDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = NearbySendBean.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    IDTCPSender _result8 = sendMassiveFile(_arg02, _arg1, _arg2, ITransmitCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result8 != null) {
                        iBinder = _result8.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = BleScanLevel.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    int _result9 = setScanLevel(_arg0, data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result9);
                    return true;
                case TRANSACTION_registerInitListener /* 10 */:
                    data.enforceInterface(DESCRIPTOR);
                    int _result10 = registerInitListener(IInitReadyListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result10);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IDTCPService {
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

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public int publish(IPublishListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public int unPublish(IPublishListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public int registerReceivelistener(IDTCPReceiveListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public int unRegisterReceivelistener(IDTCPReceiveListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public IDTCPSender sendFile(NearbyDevice recvDevice, int timeout, Uri[] fileUriList, ITransmitCallback transCallback) throws RemoteException {
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
                    _data.writeStrongBinder(transCallback != null ? transCallback.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return IDTCPSender.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public IDTCPSender sendText(NearbyDevice recvDevice, int timeout, String text, ITransmitCallback transCallback) throws RemoteException {
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
                    _data.writeStrongBinder(transCallback != null ? transCallback.asBinder() : null);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return IDTCPSender.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public boolean setHwIDInfo(String nickName, byte[] headImage) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nickName);
                    _data.writeByteArray(headImage);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public IDTCPSender sendMassiveFile(NearbyDevice recvDevice, int timeout, NearbySendBean sendBean, ITransmitCallback transCallback) throws RemoteException {
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
                    if (sendBean != null) {
                        _data.writeInt(1);
                        sendBean.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(transCallback != null ? transCallback.asBinder() : null);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return IDTCPSender.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public int setScanLevel(BleScanLevel level, long timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (level != null) {
                        _data.writeInt(1);
                        level.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(timeout);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.DTCP.IDTCPService
            public int registerInitListener(IInitReadyListener calback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(calback != null ? calback.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_registerInitListener, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
