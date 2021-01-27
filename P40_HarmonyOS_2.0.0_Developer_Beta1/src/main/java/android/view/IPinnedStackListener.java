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
    void onActionsChanged(ParceledListSlice parceledListSlice) throws RemoteException;

    void onImeVisibilityChanged(boolean z, int i) throws RemoteException;

    void onListenerRegistered(IPinnedStackController iPinnedStackController) throws RemoteException;

    void onMinimizedStateChanged(boolean z) throws RemoteException;

    void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, boolean z2, int i) throws RemoteException;

    void onShelfVisibilityChanged(boolean z, int i) throws RemoteException;

    public static class Default implements IPinnedStackListener {
        @Override // android.view.IPinnedStackListener
        public void onListenerRegistered(IPinnedStackController controller) throws RemoteException {
        }

        @Override // android.view.IPinnedStackListener
        public void onMovementBoundsChanged(Rect insetBounds, Rect normalBounds, Rect animatingBounds, boolean fromImeAdjustment, boolean fromShelfAdjustment, int displayRotation) throws RemoteException {
        }

        @Override // android.view.IPinnedStackListener
        public void onImeVisibilityChanged(boolean imeVisible, int imeHeight) throws RemoteException {
        }

        @Override // android.view.IPinnedStackListener
        public void onShelfVisibilityChanged(boolean shelfVisible, int shelfHeight) throws RemoteException {
        }

        @Override // android.view.IPinnedStackListener
        public void onMinimizedStateChanged(boolean isMinimized) throws RemoteException {
        }

        @Override // android.view.IPinnedStackListener
        public void onActionsChanged(ParceledListSlice actions) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPinnedStackListener {
        private static final String DESCRIPTOR = "android.view.IPinnedStackListener";
        static final int TRANSACTION_onActionsChanged = 6;
        static final int TRANSACTION_onImeVisibilityChanged = 3;
        static final int TRANSACTION_onListenerRegistered = 1;
        static final int TRANSACTION_onMinimizedStateChanged = 5;
        static final int TRANSACTION_onMovementBoundsChanged = 2;
        static final int TRANSACTION_onShelfVisibilityChanged = 4;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onListenerRegistered";
                case 2:
                    return "onMovementBoundsChanged";
                case 3:
                    return "onImeVisibilityChanged";
                case 4:
                    return "onShelfVisibilityChanged";
                case 5:
                    return "onMinimizedStateChanged";
                case 6:
                    return "onActionsChanged";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Rect _arg0;
            Rect _arg1;
            Rect _arg2;
            ParceledListSlice _arg02;
            if (code != 1598968902) {
                boolean _arg03 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onListenerRegistered(IPinnedStackController.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        onMovementBoundsChanged(_arg0, _arg1, _arg2, data.readInt() != 0, data.readInt() != 0, data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        onImeVisibilityChanged(_arg03, data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        onShelfVisibilityChanged(_arg03, data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        onMinimizedStateChanged(_arg03);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onActionsChanged(_arg02);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPinnedStackListener {
            public static IPinnedStackListener sDefaultImpl;
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

            @Override // android.view.IPinnedStackListener
            public void onListenerRegistered(IPinnedStackController controller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(controller != null ? controller.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onListenerRegistered(controller);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IPinnedStackListener
            public void onMovementBoundsChanged(Rect insetBounds, Rect normalBounds, Rect animatingBounds, boolean fromImeAdjustment, boolean fromShelfAdjustment, int displayRotation) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
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
                    _data.writeInt(fromImeAdjustment ? 1 : 0);
                    if (fromShelfAdjustment) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    try {
                        _data.writeInt(displayRotation);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().onMovementBoundsChanged(insetBounds, normalBounds, animatingBounds, fromImeAdjustment, fromShelfAdjustment, displayRotation);
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.IPinnedStackListener
            public void onImeVisibilityChanged(boolean imeVisible, int imeHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(imeVisible ? 1 : 0);
                    _data.writeInt(imeHeight);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onImeVisibilityChanged(imeVisible, imeHeight);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IPinnedStackListener
            public void onShelfVisibilityChanged(boolean shelfVisible, int shelfHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(shelfVisible ? 1 : 0);
                    _data.writeInt(shelfHeight);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onShelfVisibilityChanged(shelfVisible, shelfHeight);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IPinnedStackListener
            public void onMinimizedStateChanged(boolean isMinimized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isMinimized ? 1 : 0);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMinimizedStateChanged(isMinimized);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IPinnedStackListener
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
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onActionsChanged(actions);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPinnedStackListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPinnedStackListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
