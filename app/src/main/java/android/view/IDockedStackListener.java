package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDockedStackListener extends IInterface {

    public static abstract class Stub extends Binder implements IDockedStackListener {
        private static final String DESCRIPTOR = "android.view.IDockedStackListener";
        static final int TRANSACTION_onAdjustedForImeChanged = 4;
        static final int TRANSACTION_onDividerVisibilityChanged = 1;
        static final int TRANSACTION_onDockSideChanged = 5;
        static final int TRANSACTION_onDockedStackExistsChanged = 2;
        static final int TRANSACTION_onDockedStackMinimizedChanged = 3;

        private static class Proxy implements IDockedStackListener {
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

            public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
                int i = Stub.TRANSACTION_onDividerVisibilityChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!visible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onDividerVisibilityChanged, _data, null, Stub.TRANSACTION_onDividerVisibilityChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
                int i = Stub.TRANSACTION_onDividerVisibilityChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!exists) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onDockedStackExistsChanged, _data, null, Stub.TRANSACTION_onDividerVisibilityChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onDockedStackMinimizedChanged(boolean minimized, long animDuration) throws RemoteException {
                int i = Stub.TRANSACTION_onDividerVisibilityChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!minimized) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeLong(animDuration);
                    this.mRemote.transact(Stub.TRANSACTION_onDockedStackMinimizedChanged, _data, null, Stub.TRANSACTION_onDividerVisibilityChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
                int i = Stub.TRANSACTION_onDividerVisibilityChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!adjustedForIme) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeLong(animDuration);
                    this.mRemote.transact(Stub.TRANSACTION_onAdjustedForImeChanged, _data, null, Stub.TRANSACTION_onDividerVisibilityChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onDockSideChanged(int newDockSide) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newDockSide);
                    this.mRemote.transact(Stub.TRANSACTION_onDockSideChanged, _data, null, Stub.TRANSACTION_onDividerVisibilityChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDockedStackListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDockedStackListener)) {
                return new Proxy(obj);
            }
            return (IDockedStackListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_onDividerVisibilityChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onDividerVisibilityChanged(_arg0);
                    return true;
                case TRANSACTION_onDockedStackExistsChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onDockedStackExistsChanged(_arg0);
                    return true;
                case TRANSACTION_onDockedStackMinimizedChanged /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDockedStackMinimizedChanged(data.readInt() != 0, data.readLong());
                    return true;
                case TRANSACTION_onAdjustedForImeChanged /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onAdjustedForImeChanged(data.readInt() != 0, data.readLong());
                    return true;
                case TRANSACTION_onDockSideChanged /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDockSideChanged(data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAdjustedForImeChanged(boolean z, long j) throws RemoteException;

    void onDividerVisibilityChanged(boolean z) throws RemoteException;

    void onDockSideChanged(int i) throws RemoteException;

    void onDockedStackExistsChanged(boolean z) throws RemoteException;

    void onDockedStackMinimizedChanged(boolean z, long j) throws RemoteException;
}
