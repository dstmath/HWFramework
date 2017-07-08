package android.service.carrier;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
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
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pdu != null) {
                        _data.writeInt(Stub.TRANSACTION_filterSms);
                        pdu.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(format);
                    _data.writeInt(destPort);
                    _data.writeInt(subId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_filterSms, _data, null, Stub.TRANSACTION_filterSms);
                } finally {
                    _data.recycle();
                }
            }

            public void sendTextSms(String text, int subId, String destAddress, int sendSmsFlag, ICarrierMessagingCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(text);
                    _data.writeInt(subId);
                    _data.writeString(destAddress);
                    _data.writeInt(sendSmsFlag);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_sendTextSms, _data, null, Stub.TRANSACTION_filterSms);
                } finally {
                    _data.recycle();
                }
            }

            public void sendDataSms(byte[] data, int subId, String destAddress, int destPort, int sendSmsFlag, ICarrierMessagingCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    _data.writeInt(subId);
                    _data.writeString(destAddress);
                    _data.writeInt(destPort);
                    _data.writeInt(sendSmsFlag);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_sendDataSms, _data, null, Stub.TRANSACTION_filterSms);
                } finally {
                    _data.recycle();
                }
            }

            public void sendMultipartTextSms(List<String> parts, int subId, String destAddress, int sendSmsFlag, ICarrierMessagingCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(parts);
                    _data.writeInt(subId);
                    _data.writeString(destAddress);
                    _data.writeInt(sendSmsFlag);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_sendMultipartTextSms, _data, null, Stub.TRANSACTION_filterSms);
                } finally {
                    _data.recycle();
                }
            }

            public void sendMms(Uri pduUri, int subId, Uri location, ICarrierMessagingCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pduUri != null) {
                        _data.writeInt(Stub.TRANSACTION_filterSms);
                        pduUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(subId);
                    if (location != null) {
                        _data.writeInt(Stub.TRANSACTION_filterSms);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_sendMms, _data, null, Stub.TRANSACTION_filterSms);
                } finally {
                    _data.recycle();
                }
            }

            public void downloadMms(Uri pduUri, int subId, Uri location, ICarrierMessagingCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pduUri != null) {
                        _data.writeInt(Stub.TRANSACTION_filterSms);
                        pduUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(subId);
                    if (location != null) {
                        _data.writeInt(Stub.TRANSACTION_filterSms);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_downloadMms, _data, null, Stub.TRANSACTION_filterSms);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg1;
            Uri uri;
            Uri uri2;
            switch (code) {
                case TRANSACTION_filterSms /*1*/:
                    MessagePdu messagePdu;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        messagePdu = (MessagePdu) MessagePdu.CREATOR.createFromParcel(data);
                    } else {
                        messagePdu = null;
                    }
                    filterSms(messagePdu, data.readString(), data.readInt(), data.readInt(), android.service.carrier.ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_sendTextSms /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendTextSms(data.readString(), data.readInt(), data.readString(), data.readInt(), android.service.carrier.ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_sendDataSms /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _arg0 = data.createByteArray();
                    _arg1 = data.readInt();
                    String _arg2 = data.readString();
                    int readInt = data.readInt();
                    sendDataSms(_arg0, _arg1, _arg2, _arg3, data.readInt(), android.service.carrier.ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_sendMultipartTextSms /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendMultipartTextSms(data.createStringArrayList(), data.readInt(), data.readString(), data.readInt(), android.service.carrier.ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_sendMms /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        uri2 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri2 = null;
                    }
                    sendMms(uri, _arg1, uri2, android.service.carrier.ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_downloadMms /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        uri2 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri2 = null;
                    }
                    downloadMms(uri, _arg1, uri2, android.service.carrier.ICarrierMessagingCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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
