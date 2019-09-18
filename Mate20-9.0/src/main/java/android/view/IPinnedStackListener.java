package android.view;

import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.IPinnedStackController;

public interface IPinnedStackListener extends IInterface {

    public static abstract class Stub extends Binder implements IPinnedStackListener {
        private static final String DESCRIPTOR = "android.view.IPinnedStackListener";
        static final int TRANSACTION_onActionsChanged = 6;
        static final int TRANSACTION_onImeVisibilityChanged = 3;
        static final int TRANSACTION_onListenerRegistered = 1;
        static final int TRANSACTION_onMinimizedStateChanged = 5;
        static final int TRANSACTION_onMovementBoundsChanged = 2;
        static final int TRANSACTION_onShelfVisibilityChanged = 4;

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
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(controller != null ? controller.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMovementBoundsChanged(Rect insetBounds, Rect normalBounds, Rect animatingBounds, boolean fromImeAdjustment, boolean fromShelfAdjustment, int displayRotation) throws RemoteException {
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
                    _data.writeInt(fromImeAdjustment);
                    _data.writeInt(fromShelfAdjustment);
                    _data.writeInt(displayRotation);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onImeVisibilityChanged(boolean imeVisible, int imeHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(imeVisible);
                    _data.writeInt(imeHeight);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onShelfVisibilityChanged(boolean shelfVisible, int shelfHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(shelfVisible);
                    _data.writeInt(shelfHeight);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMinimizedStateChanged(boolean isMinimized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isMinimized);
                    this.mRemote.transact(5, _data, null, 1);
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
                    this.mRemote.transact(6, _data, null, 1);
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
            Rect _arg0;
            Rect _arg1;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                Rect rect = null;
                boolean _arg4 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        onListenerRegistered(IPinnedStackController.Stub.asInterface(parcel.readStrongBinder()));
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            _arg0 = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (parcel.readInt() != 0) {
                            _arg1 = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (parcel.readInt() != 0) {
                            rect = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        }
                        Rect _arg2 = rect;
                        boolean _arg3 = parcel.readInt() != 0;
                        if (parcel.readInt() != 0) {
                            _arg4 = true;
                        }
                        onMovementBoundsChanged(_arg0, _arg1, _arg2, _arg3, _arg4, parcel.readInt());
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            _arg4 = true;
                        }
                        onImeVisibilityChanged(_arg4, parcel.readInt());
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            _arg4 = true;
                        }
                        onShelfVisibilityChanged(_arg4, parcel.readInt());
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            _arg4 = true;
                        }
                        onMinimizedStateChanged(_arg4);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            rect = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(parcel);
                        }
                        onActionsChanged(rect);
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

    void onActionsChanged(ParceledListSlice parceledListSlice) throws RemoteException;

    void onImeVisibilityChanged(boolean z, int i) throws RemoteException;

    void onListenerRegistered(IPinnedStackController iPinnedStackController) throws RemoteException;

    void onMinimizedStateChanged(boolean z) throws RemoteException;

    void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, boolean z2, int i) throws RemoteException;

    void onShelfVisibilityChanged(boolean z, int i) throws RemoteException;
}
