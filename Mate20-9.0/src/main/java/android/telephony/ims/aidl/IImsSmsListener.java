package android.telephony.ims.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IImsSmsListener extends IInterface {

    public static abstract class Stub extends Binder implements IImsSmsListener {
        private static final String DESCRIPTOR = "android.telephony.ims.aidl.IImsSmsListener";
        static final int TRANSACTION_onSendSmsResult = 1;
        static final int TRANSACTION_onSmsReceived = 3;
        static final int TRANSACTION_onSmsStatusReportReceived = 2;

        private static class Proxy implements IImsSmsListener {
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

            public void onSendSmsResult(int token, int messageRef, int status, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(messageRef);
                    _data.writeInt(status);
                    _data.writeInt(reason);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSmsStatusReportReceived(int token, int messageRef, String format, byte[] pdu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(messageRef);
                    _data.writeString(format);
                    _data.writeByteArray(pdu);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSmsReceived(int token, String format, byte[] pdu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeString(format);
                    _data.writeByteArray(pdu);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsSmsListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsSmsListener)) {
                return new Proxy(obj);
            }
            return (IImsSmsListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onSendSmsResult(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onSmsStatusReportReceived(data.readInt(), data.readInt(), data.readString(), data.createByteArray());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onSmsReceived(data.readInt(), data.readString(), data.createByteArray());
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

    void onSendSmsResult(int i, int i2, int i3, int i4) throws RemoteException;

    void onSmsReceived(int i, String str, byte[] bArr) throws RemoteException;

    void onSmsStatusReportReceived(int i, int i2, String str, byte[] bArr) throws RemoteException;
}
