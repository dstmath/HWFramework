package android.service.wallpaper;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.wallpaper.IWallpaperConnection;

public interface IWallpaperService extends IInterface {
    void attach(IWallpaperConnection iWallpaperConnection, IBinder iBinder, int i, boolean z, int i2, int i3, Rect rect, int i4) throws RemoteException;

    void detach() throws RemoteException;

    public static class Default implements IWallpaperService {
        @Override // android.service.wallpaper.IWallpaperService
        public void attach(IWallpaperConnection connection, IBinder windowToken, int windowType, boolean isPreview, int reqWidth, int reqHeight, Rect padding, int displayId) throws RemoteException {
        }

        @Override // android.service.wallpaper.IWallpaperService
        public void detach() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWallpaperService {
        private static final String DESCRIPTOR = "android.service.wallpaper.IWallpaperService";
        static final int TRANSACTION_attach = 1;
        static final int TRANSACTION_detach = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWallpaperService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWallpaperService)) {
                return new Proxy(obj);
            }
            return (IWallpaperService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "attach";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "detach";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Rect _arg6;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IWallpaperConnection _arg0 = IWallpaperConnection.Stub.asInterface(data.readStrongBinder());
                IBinder _arg1 = data.readStrongBinder();
                int _arg2 = data.readInt();
                boolean _arg3 = data.readInt() != 0;
                int _arg4 = data.readInt();
                int _arg5 = data.readInt();
                if (data.readInt() != 0) {
                    _arg6 = Rect.CREATOR.createFromParcel(data);
                } else {
                    _arg6 = null;
                }
                attach(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                detach();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IWallpaperService {
            public static IWallpaperService sDefaultImpl;
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

            @Override // android.service.wallpaper.IWallpaperService
            public void attach(IWallpaperConnection connection, IBinder windowToken, int windowType, boolean isPreview, int reqWidth, int reqHeight, Rect padding, int displayId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection != null ? connection.asBinder() : null);
                    try {
                        _data.writeStrongBinder(windowToken);
                        try {
                            _data.writeInt(windowType);
                            _data.writeInt(isPreview ? 1 : 0);
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(reqWidth);
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(reqHeight);
                            if (padding != null) {
                                _data.writeInt(1);
                                padding.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeInt(displayId);
                            if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().attach(connection, windowToken, windowType, isPreview, reqWidth, reqHeight, padding, displayId);
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.service.wallpaper.IWallpaperService
            public void detach() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().detach();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWallpaperService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWallpaperService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
