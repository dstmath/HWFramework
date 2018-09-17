package android.print;

import android.os.Binder;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

public interface IWriteResultCallback extends IInterface {

    public static abstract class Stub extends Binder implements IWriteResultCallback {
        private static final String DESCRIPTOR = "android.print.IWriteResultCallback";
        static final int TRANSACTION_onWriteCanceled = 4;
        static final int TRANSACTION_onWriteFailed = 3;
        static final int TRANSACTION_onWriteFinished = 2;
        static final int TRANSACTION_onWriteStarted = 1;

        private static class Proxy implements IWriteResultCallback {
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

            public void onWriteStarted(ICancellationSignal cancellation, int sequence) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cancellation != null) {
                        iBinder = cancellation.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_onWriteStarted, _data, null, Stub.TRANSACTION_onWriteStarted);
                } finally {
                    _data.recycle();
                }
            }

            public void onWriteFinished(PageRange[] pages, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(pages, 0);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_onWriteFinished, _data, null, Stub.TRANSACTION_onWriteStarted);
                } finally {
                    _data.recycle();
                }
            }

            public void onWriteFailed(CharSequence error, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (error != null) {
                        _data.writeInt(Stub.TRANSACTION_onWriteStarted);
                        TextUtils.writeToParcel(error, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_onWriteFailed, _data, null, Stub.TRANSACTION_onWriteStarted);
                } finally {
                    _data.recycle();
                }
            }

            public void onWriteCanceled(int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_onWriteCanceled, _data, null, Stub.TRANSACTION_onWriteStarted);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWriteResultCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWriteResultCallback)) {
                return new Proxy(obj);
            }
            return (IWriteResultCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onWriteStarted /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onWriteStarted(android.os.ICancellationSignal.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    return true;
                case TRANSACTION_onWriteFinished /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onWriteFinished((PageRange[]) data.createTypedArray(PageRange.CREATOR), data.readInt());
                    return true;
                case TRANSACTION_onWriteFailed /*3*/:
                    CharSequence charSequence;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        charSequence = null;
                    }
                    onWriteFailed(charSequence, data.readInt());
                    return true;
                case TRANSACTION_onWriteCanceled /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onWriteCanceled(data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onWriteCanceled(int i) throws RemoteException;

    void onWriteFailed(CharSequence charSequence, int i) throws RemoteException;

    void onWriteFinished(PageRange[] pageRangeArr, int i) throws RemoteException;

    void onWriteStarted(ICancellationSignal iCancellationSignal, int i) throws RemoteException;
}
