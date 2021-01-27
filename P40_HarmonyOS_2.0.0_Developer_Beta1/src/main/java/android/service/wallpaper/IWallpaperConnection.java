package android.service.wallpaper;

import android.app.WallpaperColors;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.service.wallpaper.IWallpaperEngine;

public interface IWallpaperConnection extends IInterface {
    void attachEngine(IWallpaperEngine iWallpaperEngine, int i) throws RemoteException;

    void engineShown(IWallpaperEngine iWallpaperEngine) throws RemoteException;

    void onWallpaperColorsChanged(WallpaperColors wallpaperColors, int i) throws RemoteException;

    ParcelFileDescriptor setWallpaper(String str) throws RemoteException;

    public static class Default implements IWallpaperConnection {
        @Override // android.service.wallpaper.IWallpaperConnection
        public void attachEngine(IWallpaperEngine engine, int displayId) throws RemoteException {
        }

        @Override // android.service.wallpaper.IWallpaperConnection
        public void engineShown(IWallpaperEngine engine) throws RemoteException {
        }

        @Override // android.service.wallpaper.IWallpaperConnection
        public ParcelFileDescriptor setWallpaper(String name) throws RemoteException {
            return null;
        }

        @Override // android.service.wallpaper.IWallpaperConnection
        public void onWallpaperColorsChanged(WallpaperColors colors, int displayId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWallpaperConnection {
        private static final String DESCRIPTOR = "android.service.wallpaper.IWallpaperConnection";
        static final int TRANSACTION_attachEngine = 1;
        static final int TRANSACTION_engineShown = 2;
        static final int TRANSACTION_onWallpaperColorsChanged = 4;
        static final int TRANSACTION_setWallpaper = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWallpaperConnection asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWallpaperConnection)) {
                return new Proxy(obj);
            }
            return (IWallpaperConnection) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "attachEngine";
            }
            if (transactionCode == 2) {
                return "engineShown";
            }
            if (transactionCode == 3) {
                return "setWallpaper";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onWallpaperColorsChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            WallpaperColors _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                attachEngine(IWallpaperEngine.Stub.asInterface(data.readStrongBinder()), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                engineShown(IWallpaperEngine.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                ParcelFileDescriptor _result = setWallpaper(data.readString());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = WallpaperColors.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onWallpaperColorsChanged(_arg0, data.readInt());
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
        public static class Proxy implements IWallpaperConnection {
            public static IWallpaperConnection sDefaultImpl;
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

            @Override // android.service.wallpaper.IWallpaperConnection
            public void attachEngine(IWallpaperEngine engine, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(engine != null ? engine.asBinder() : null);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().attachEngine(engine, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.wallpaper.IWallpaperConnection
            public void engineShown(IWallpaperEngine engine) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(engine != null ? engine.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().engineShown(engine);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.wallpaper.IWallpaperConnection
            public ParcelFileDescriptor setWallpaper(String name) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setWallpaper(name);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.wallpaper.IWallpaperConnection
            public void onWallpaperColorsChanged(WallpaperColors colors, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (colors != null) {
                        _data.writeInt(1);
                        colors.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onWallpaperColorsChanged(colors, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWallpaperConnection impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWallpaperConnection getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
