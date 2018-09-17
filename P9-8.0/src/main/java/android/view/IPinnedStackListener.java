package android.view;

import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPinnedStackListener extends IInterface {

    public static abstract class Stub extends Binder implements IPinnedStackListener {
        private static final String DESCRIPTOR = "android.view.IPinnedStackListener";
        static final int TRANSACTION_onActionsChanged = 5;
        static final int TRANSACTION_onImeVisibilityChanged = 3;
        static final int TRANSACTION_onListenerRegistered = 1;
        static final int TRANSACTION_onMinimizedStateChanged = 4;
        static final int TRANSACTION_onMovementBoundsChanged = 2;

        private static class Proxy implements IPinnedStackListener {
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

            public void onListenerRegistered(IPinnedStackController controller) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (controller != null) {
                        iBinder = controller.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMovementBoundsChanged(Rect insetBounds, Rect normalBounds, Rect animatingBounds, boolean fromImeAdjustement, int displayRotation) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (insetBounds != null) {
                        _data.writeInt(1);
                        insetBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (normalBounds != null) {
                        _data.writeInt(1);
                        normalBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (animatingBounds != null) {
                        _data.writeInt(1);
                        animatingBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!fromImeAdjustement) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(displayRotation);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onImeVisibilityChanged(boolean imeVisible, int imeHeight) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!imeVisible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(imeHeight);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMinimizedStateChanged(boolean isMinimized) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!isMinimized) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onActionsChanged(ParceledListSlice actions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (actions != null) {
                        _data.writeInt(1);
                        actions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPinnedStackListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPinnedStackListener)) {
                return new Proxy(obj);
            }
            return (IPinnedStackListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onListenerRegistered(android.view.IPinnedStackController.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 2:
                    Rect _arg0;
                    Rect _arg1;
                    Rect _arg2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg1 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    onMovementBoundsChanged(_arg0, _arg1, _arg2, data.readInt() != 0, data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onImeVisibilityChanged(data.readInt() != 0, data.readInt());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onMinimizedStateChanged(data.readInt() != 0);
                    return true;
                case 5:
                    ParceledListSlice _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    onActionsChanged(_arg02);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onActionsChanged(ParceledListSlice parceledListSlice) throws RemoteException;

    void onImeVisibilityChanged(boolean z, int i) throws RemoteException;

    void onListenerRegistered(IPinnedStackController iPinnedStackController) throws RemoteException;

    void onMinimizedStateChanged(boolean z) throws RemoteException;

    void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, int i) throws RemoteException;
}
