package android.service.wallpaper;

import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.MotionEvent;

public interface IWallpaperEngine extends IInterface {

    public static abstract class Stub extends Binder implements IWallpaperEngine {
        private static final String DESCRIPTOR = "android.service.wallpaper.IWallpaperEngine";
        static final int TRANSACTION_destroy = 6;
        static final int TRANSACTION_dispatchPointer = 4;
        static final int TRANSACTION_dispatchWallpaperCommand = 5;
        static final int TRANSACTION_setDesiredSize = 1;
        static final int TRANSACTION_setDisplayPadding = 2;
        static final int TRANSACTION_setVisibility = 3;

        private static class Proxy implements IWallpaperEngine {
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

            public void setDesiredSize(int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    this.mRemote.transact(Stub.TRANSACTION_setDesiredSize, _data, null, Stub.TRANSACTION_setDesiredSize);
                } finally {
                    _data.recycle();
                }
            }

            public void setDisplayPadding(Rect padding) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (padding != null) {
                        _data.writeInt(Stub.TRANSACTION_setDesiredSize);
                        padding.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setDisplayPadding, _data, null, Stub.TRANSACTION_setDesiredSize);
                } finally {
                    _data.recycle();
                }
            }

            public void setVisibility(boolean visible) throws RemoteException {
                int i = Stub.TRANSACTION_setDesiredSize;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!visible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setVisibility, _data, null, Stub.TRANSACTION_setDesiredSize);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchPointer(MotionEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(Stub.TRANSACTION_setDesiredSize);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_dispatchPointer, _data, null, Stub.TRANSACTION_setDesiredSize);
                } finally {
                    _data.recycle();
                }
            }

            public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    _data.writeInt(z);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_setDesiredSize);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_dispatchWallpaperCommand, _data, null, Stub.TRANSACTION_setDesiredSize);
                } finally {
                    _data.recycle();
                }
            }

            public void destroy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_destroy, _data, null, Stub.TRANSACTION_setDesiredSize);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWallpaperEngine asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWallpaperEngine)) {
                return new Proxy(obj);
            }
            return (IWallpaperEngine) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_setDesiredSize /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDesiredSize(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_setDisplayPadding /*2*/:
                    Rect rect;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    setDisplayPadding(rect);
                    return true;
                case TRANSACTION_setVisibility /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    setVisibility(_arg0);
                    return true;
                case TRANSACTION_dispatchPointer /*4*/:
                    MotionEvent motionEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        motionEvent = (MotionEvent) MotionEvent.CREATOR.createFromParcel(data);
                    } else {
                        motionEvent = null;
                    }
                    dispatchPointer(motionEvent);
                    return true;
                case TRANSACTION_dispatchWallpaperCommand /*5*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    int _arg1 = data.readInt();
                    int _arg2 = data.readInt();
                    int _arg3 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    dispatchWallpaperCommand(_arg02, _arg1, _arg2, _arg3, bundle);
                    return true;
                case TRANSACTION_destroy /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    destroy();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void destroy() throws RemoteException;

    void dispatchPointer(MotionEvent motionEvent) throws RemoteException;

    void dispatchWallpaperCommand(String str, int i, int i2, int i3, Bundle bundle) throws RemoteException;

    void setDesiredSize(int i, int i2) throws RemoteException;

    void setDisplayPadding(Rect rect) throws RemoteException;

    void setVisibility(boolean z) throws RemoteException;
}
