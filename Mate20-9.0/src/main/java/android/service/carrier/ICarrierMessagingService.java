package android.service.carrier;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.carrier.ICarrierMessagingCallback;
import java.util.List;

public interface ICarrierMessagingService extends IInterface {

    public static abstract class Stub extends Binder implements ICarrierMessagingService {
        private static final String DESCRIPTOR = "android.service.carrier.ICarrierMessagingService";
        static final int TRANSACTION_downloadMms = 6;
        static final int TRANSACTION_filterSms = 1;
        static final int TRANSACTION_sendDataSms = 3;
        static final int TRANSACTION_sendMms = 5;
        static final int TRANSACTION_sendMultipartTextSms = 4;
        static final int TRANSACTION_sendTextSms = 2;

        private static class Proxy implements ICarrierMessagingService {
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

            public void filterSms(MessagePdu pdu, String format, int destPort, int subId, ICarrierMessagingCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pdu != null) {
                        _data.writeInt(1);
                        pdu.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(format);
                    _data.writeInt(destPort);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendTextSms(String text, int subId, String destAddress, int sendSmsFlag, ICarrierMessagingCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(text);
                    _data.writeInt(subId);
                    _data.writeString(destAddress);
                    _data.writeInt(sendSmsFlag);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendDataSms(byte[] data, int subId, String destAddress, int destPort, int sendSmsFlag, ICarrierMessagingCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    _data.writeInt(subId);
                    _data.writeString(destAddress);
                    _data.writeInt(destPort);
                    _data.writeInt(sendSmsFlag);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendMultipartTextSms(List<String> parts, int subId, String destAddress, int sendSmsFlag, ICarrierMessagingCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(parts);
                    _data.writeInt(subId);
                    _data.writeString(destAddress);
                    _data.writeInt(sendSmsFlag);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendMms(Uri pduUri, int subId, Uri location, ICarrierMessagingCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pduUri != null) {
                        _data.writeInt(1);
                        pduUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(subId);
                    if (location != null) {
                        _data.writeInt(1);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void downloadMms(Uri pduUri, int subId, Uri location, ICarrierMessagingCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pduUri != null) {
                        _data.writeInt(1);
                        pduUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(subId);
                    if (location != null) {
                        _data.writeInt(1);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICarrierMessagingService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICarrierMessagingService)) {
                return new Proxy(obj);
            }
            return (ICarrierMessagingService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.net.Uri} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.net.Uri} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: android.service.carrier.MessagePdu} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v15, resolved type: android.net.Uri} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v19, resolved type: android.net.Uri} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v24, resolved type: android.net.Uri} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v25, resolved type: android.net.Uri} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v26, resolved type: android.net.Uri} */
        /* JADX WARNING: type inference failed for: r0v8, types: [android.service.carrier.MessagePdu] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Uri _arg0;
            Uri _arg02;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                Uri _arg2 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = MessagePdu.CREATOR.createFromParcel(parcel);
                        }
                        filterSms(_arg2, data.readString(), data.readInt(), data.readInt(), ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        sendTextSms(data.readString(), data.readInt(), data.readString(), data.readInt(), ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        sendDataSms(data.createByteArray(), data.readInt(), data.readString(), data.readInt(), data.readInt(), ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        sendMultipartTextSms(data.createStringArrayList(), data.readInt(), data.readString(), data.readInt(), ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        sendMms(_arg0, _arg1, _arg2, ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        downloadMms(_arg02, _arg12, _arg2, ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void downloadMms(Uri uri, int i, Uri uri2, ICarrierMessagingCallback iCarrierMessagingCallback) throws RemoteException;

    void filterSms(MessagePdu messagePdu, String str, int i, int i2, ICarrierMessagingCallback iCarrierMessagingCallback) throws RemoteException;

    void sendDataSms(byte[] bArr, int i, String str, int i2, int i3, ICarrierMessagingCallback iCarrierMessagingCallback) throws RemoteException;

    void sendMms(Uri uri, int i, Uri uri2, ICarrierMessagingCallback iCarrierMessagingCallback) throws RemoteException;

    void sendMultipartTextSms(List<String> list, int i, String str, int i2, ICarrierMessagingCallback iCarrierMessagingCallback) throws RemoteException;

    void sendTextSms(String str, int i, String str2, int i2, ICarrierMessagingCallback iCarrierMessagingCallback) throws RemoteException;
}
