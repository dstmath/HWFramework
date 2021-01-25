package android.media;

import android.annotation.UnsupportedAppUsage;
import android.media.IMediaScannerListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMediaScannerService extends IInterface {
    @UnsupportedAppUsage
    void requestScanFile(String str, String str2, IMediaScannerListener iMediaScannerListener) throws RemoteException;

    @UnsupportedAppUsage
    void scanFile(String str, String str2) throws RemoteException;

    public static class Default implements IMediaScannerService {
        @Override // android.media.IMediaScannerService
        public void requestScanFile(String path, String mimeType, IMediaScannerListener listener) throws RemoteException {
        }

        @Override // android.media.IMediaScannerService
        public void scanFile(String path, String mimeType) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMediaScannerService {
        private static final String DESCRIPTOR = "android.media.IMediaScannerService";
        static final int TRANSACTION_requestScanFile = 1;
        static final int TRANSACTION_scanFile = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMediaScannerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMediaScannerService)) {
                return new Proxy(obj);
            }
            return (IMediaScannerService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "requestScanFile";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "scanFile";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                requestScanFile(data.readString(), data.readString(), IMediaScannerListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                scanFile(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMediaScannerService {
            public static IMediaScannerService sDefaultImpl;
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

            @Override // android.media.IMediaScannerService
            public void requestScanFile(String path, String mimeType, IMediaScannerListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeString(mimeType);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestScanFile(path, mimeType, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.IMediaScannerService
            public void scanFile(String path, String mimeType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeString(mimeType);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().scanFile(path, mimeType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMediaScannerService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMediaScannerService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
