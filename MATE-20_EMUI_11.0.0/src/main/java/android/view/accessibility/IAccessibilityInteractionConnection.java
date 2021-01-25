package android.view.accessibility;

import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.MagnificationSpec;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;

public interface IAccessibilityInteractionConnection extends IInterface {
    void clearAccessibilityFocus() throws RemoteException;

    void findAccessibilityNodeInfoByAccessibilityId(long j, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec, Bundle bundle) throws RemoteException;

    void findAccessibilityNodeInfosByText(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void findAccessibilityNodeInfosByViewId(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void findFocus(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void focusSearch(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void notifyOutsideTouch() throws RemoteException;

    void performAccessibilityAction(long j, int i, Bundle bundle, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2) throws RemoteException;

    public static class Default implements IAccessibilityInteractionConnection {
        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findAccessibilityNodeInfoByAccessibilityId(long accessibilityNodeId, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec, Bundle arguments) throws RemoteException {
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findAccessibilityNodeInfosByViewId(long accessibilityNodeId, String viewId, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findAccessibilityNodeInfosByText(long accessibilityNodeId, String text, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findFocus(long accessibilityNodeId, int focusType, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void focusSearch(long accessibilityNodeId, int direction, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void performAccessibilityAction(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) throws RemoteException {
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void clearAccessibilityFocus() throws RemoteException {
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void notifyOutsideTouch() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAccessibilityInteractionConnection {
        private static final String DESCRIPTOR = "android.view.accessibility.IAccessibilityInteractionConnection";
        static final int TRANSACTION_clearAccessibilityFocus = 7;
        static final int TRANSACTION_findAccessibilityNodeInfoByAccessibilityId = 1;
        static final int TRANSACTION_findAccessibilityNodeInfosByText = 3;
        static final int TRANSACTION_findAccessibilityNodeInfosByViewId = 2;
        static final int TRANSACTION_findFocus = 4;
        static final int TRANSACTION_focusSearch = 5;
        static final int TRANSACTION_notifyOutsideTouch = 8;
        static final int TRANSACTION_performAccessibilityAction = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAccessibilityInteractionConnection asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAccessibilityInteractionConnection)) {
                return new Proxy(obj);
            }
            return (IAccessibilityInteractionConnection) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "findAccessibilityNodeInfoByAccessibilityId";
                case 2:
                    return "findAccessibilityNodeInfosByViewId";
                case 3:
                    return "findAccessibilityNodeInfosByText";
                case 4:
                    return "findFocus";
                case 5:
                    return "focusSearch";
                case 6:
                    return "performAccessibilityAction";
                case 7:
                    return "clearAccessibilityFocus";
                case 8:
                    return "notifyOutsideTouch";
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
            Region _arg1;
            MagnificationSpec _arg7;
            Bundle _arg8;
            Region _arg2;
            MagnificationSpec _arg82;
            Region _arg22;
            MagnificationSpec _arg83;
            Region _arg23;
            MagnificationSpec _arg84;
            Region _arg24;
            MagnificationSpec _arg85;
            Bundle _arg25;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg0 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg1 = Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _arg26 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg3 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg4 = data.readInt();
                        int _arg5 = data.readInt();
                        long _arg6 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg7 = MagnificationSpec.CREATOR.createFromParcel(data);
                        } else {
                            _arg7 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg8 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg8 = null;
                        }
                        findAccessibilityNodeInfoByAccessibilityId(_arg0, _arg1, _arg26, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg02 = data.readLong();
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _arg32 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg42 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg52 = data.readInt();
                        int _arg62 = data.readInt();
                        long _arg72 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg82 = MagnificationSpec.CREATOR.createFromParcel(data);
                        } else {
                            _arg82 = null;
                        }
                        findAccessibilityNodeInfosByViewId(_arg02, _arg12, _arg2, _arg32, _arg42, _arg52, _arg62, _arg72, _arg82);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg03 = data.readLong();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        int _arg33 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg43 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg53 = data.readInt();
                        int _arg63 = data.readInt();
                        long _arg73 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg83 = MagnificationSpec.CREATOR.createFromParcel(data);
                        } else {
                            _arg83 = null;
                        }
                        findAccessibilityNodeInfosByText(_arg03, _arg13, _arg22, _arg33, _arg43, _arg53, _arg63, _arg73, _arg83);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg04 = data.readLong();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        int _arg34 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg44 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg54 = data.readInt();
                        int _arg64 = data.readInt();
                        long _arg74 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg84 = MagnificationSpec.CREATOR.createFromParcel(data);
                        } else {
                            _arg84 = null;
                        }
                        findFocus(_arg04, _arg14, _arg23, _arg34, _arg44, _arg54, _arg64, _arg74, _arg84);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg05 = data.readLong();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg24 = Region.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        int _arg35 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg45 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg55 = data.readInt();
                        int _arg65 = data.readInt();
                        long _arg75 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg85 = MagnificationSpec.CREATOR.createFromParcel(data);
                        } else {
                            _arg85 = null;
                        }
                        focusSearch(_arg05, _arg15, _arg24, _arg35, _arg45, _arg55, _arg65, _arg75, _arg85);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg06 = data.readLong();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg25 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        performAccessibilityAction(_arg06, _arg16, _arg25, data.readInt(), IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readLong());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        clearAccessibilityFocus();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        notifyOutsideTouch();
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
        public static class Proxy implements IAccessibilityInteractionConnection {
            public static IAccessibilityInteractionConnection sDefaultImpl;
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

            @Override // android.view.accessibility.IAccessibilityInteractionConnection
            public void findAccessibilityNodeInfoByAccessibilityId(long accessibilityNodeId, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec, Bundle arguments) throws RemoteException {
                Parcel _data;
                Throwable th;
                Parcel _data2 = Parcel.obtain();
                try {
                    _data2.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data2.writeLong(accessibilityNodeId);
                    if (bounds != null) {
                        try {
                            _data2.writeInt(1);
                            bounds.writeToParcel(_data2, 0);
                        } catch (Throwable th2) {
                            th = th2;
                            _data = _data2;
                        }
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeInt(interactionId);
                    _data2.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data2.writeInt(flags);
                    _data2.writeInt(interrogatingPid);
                    _data2.writeLong(interrogatingTid);
                    if (spec != null) {
                        _data2.writeInt(1);
                        spec.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (arguments != null) {
                        _data2.writeInt(1);
                        arguments.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data2, null, 1) || Stub.getDefaultImpl() == null) {
                        _data2.recycle();
                        return;
                    }
                    _data = _data2;
                    try {
                        Stub.getDefaultImpl().findAccessibilityNodeInfoByAccessibilityId(accessibilityNodeId, bounds, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec, arguments);
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _data = _data2;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.accessibility.IAccessibilityInteractionConnection
            public void findAccessibilityNodeInfosByViewId(long accessibilityNodeId, String viewId, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeString(viewId);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(interactionId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(flags);
                    _data.writeInt(interrogatingPid);
                    _data.writeLong(interrogatingTid);
                    if (spec != null) {
                        _data.writeInt(1);
                        spec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().findAccessibilityNodeInfosByViewId(accessibilityNodeId, viewId, bounds, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.accessibility.IAccessibilityInteractionConnection
            public void findAccessibilityNodeInfosByText(long accessibilityNodeId, String text, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeString(text);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(interactionId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(flags);
                    _data.writeInt(interrogatingPid);
                    _data.writeLong(interrogatingTid);
                    if (spec != null) {
                        _data.writeInt(1);
                        spec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().findAccessibilityNodeInfosByText(accessibilityNodeId, text, bounds, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.accessibility.IAccessibilityInteractionConnection
            public void findFocus(long accessibilityNodeId, int focusType, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeInt(focusType);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(interactionId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(flags);
                    _data.writeInt(interrogatingPid);
                    _data.writeLong(interrogatingTid);
                    if (spec != null) {
                        _data.writeInt(1);
                        spec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().findFocus(accessibilityNodeId, focusType, bounds, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.accessibility.IAccessibilityInteractionConnection
            public void focusSearch(long accessibilityNodeId, int direction, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeInt(direction);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(interactionId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(flags);
                    _data.writeInt(interrogatingPid);
                    _data.writeLong(interrogatingTid);
                    if (spec != null) {
                        _data.writeInt(1);
                        spec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().focusSearch(accessibilityNodeId, direction, bounds, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.accessibility.IAccessibilityInteractionConnection
            public void performAccessibilityAction(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(accessibilityNodeId);
                        _data.writeInt(action);
                        if (arguments != null) {
                            _data.writeInt(1);
                            arguments.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeInt(interactionId);
                        _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                        _data.writeInt(flags);
                        _data.writeInt(interrogatingPid);
                        _data.writeLong(interrogatingTid);
                        if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().performAccessibilityAction(accessibilityNodeId, action, arguments, interactionId, callback, flags, interrogatingPid, interrogatingTid);
                        _data.recycle();
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.accessibility.IAccessibilityInteractionConnection
            public void clearAccessibilityFocus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().clearAccessibilityFocus();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.accessibility.IAccessibilityInteractionConnection
            public void notifyOutsideTouch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyOutsideTouch();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAccessibilityInteractionConnection impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAccessibilityInteractionConnection getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
