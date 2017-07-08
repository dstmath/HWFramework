package android.view.accessibility;

import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.MagnificationSpec;

public interface IAccessibilityInteractionConnection extends IInterface {

    public static abstract class Stub extends Binder implements IAccessibilityInteractionConnection {
        private static final String DESCRIPTOR = "android.view.accessibility.IAccessibilityInteractionConnection";
        static final int TRANSACTION_findAccessibilityNodeInfoByAccessibilityId = 1;
        static final int TRANSACTION_findAccessibilityNodeInfosByText = 3;
        static final int TRANSACTION_findAccessibilityNodeInfosByViewId = 2;
        static final int TRANSACTION_findFocus = 4;
        static final int TRANSACTION_focusSearch = 5;
        static final int TRANSACTION_performAccessibilityAction = 6;

        private static class Proxy implements IAccessibilityInteractionConnection {
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

            public void findAccessibilityNodeInfoByAccessibilityId(long accessibilityNodeId, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    if (bounds != null) {
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(interactionId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    _data.writeInt(interrogatingPid);
                    _data.writeLong(interrogatingTid);
                    if (spec != null) {
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                        spec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId, _data, null, Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                } finally {
                    _data.recycle();
                }
            }

            public void findAccessibilityNodeInfosByViewId(long accessibilityNodeId, String viewId, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeString(viewId);
                    if (bounds != null) {
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
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
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                        spec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_findAccessibilityNodeInfosByViewId, _data, null, Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                } finally {
                    _data.recycle();
                }
            }

            public void findAccessibilityNodeInfosByText(long accessibilityNodeId, String text, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeString(text);
                    if (bounds != null) {
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
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
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                        spec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_findAccessibilityNodeInfosByText, _data, null, Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                } finally {
                    _data.recycle();
                }
            }

            public void findFocus(long accessibilityNodeId, int focusType, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeInt(focusType);
                    if (bounds != null) {
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
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
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                        spec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_findFocus, _data, null, Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                } finally {
                    _data.recycle();
                }
            }

            public void focusSearch(long accessibilityNodeId, int direction, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeInt(direction);
                    if (bounds != null) {
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
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
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                        spec.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_focusSearch, _data, null, Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                } finally {
                    _data.recycle();
                }
            }

            public void performAccessibilityAction(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(accessibilityNodeId);
                    _data.writeInt(action);
                    if (arguments != null) {
                        _data.writeInt(Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                        arguments.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(interactionId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    _data.writeInt(interrogatingPid);
                    _data.writeLong(interrogatingTid);
                    this.mRemote.transact(Stub.TRANSACTION_performAccessibilityAction, _data, null, Stub.TRANSACTION_findAccessibilityNodeInfoByAccessibilityId);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            long _arg0;
            int _arg5;
            String _arg1;
            Region region;
            int _arg3;
            IAccessibilityInteractionConnectionCallback _arg4;
            int _arg6;
            long _arg7;
            MagnificationSpec magnificationSpec;
            int _arg12;
            switch (code) {
                case TRANSACTION_findAccessibilityNodeInfoByAccessibilityId /*1*/:
                    Region region2;
                    MagnificationSpec magnificationSpec2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readLong();
                    if (data.readInt() != 0) {
                        region2 = (Region) Region.CREATOR.createFromParcel(data);
                    } else {
                        region2 = null;
                    }
                    int _arg2 = data.readInt();
                    IAccessibilityInteractionConnectionCallback _arg32 = android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                    int _arg42 = data.readInt();
                    _arg5 = data.readInt();
                    long _arg62 = data.readLong();
                    if (data.readInt() != 0) {
                        magnificationSpec2 = (MagnificationSpec) MagnificationSpec.CREATOR.createFromParcel(data);
                    } else {
                        magnificationSpec2 = null;
                    }
                    findAccessibilityNodeInfoByAccessibilityId(_arg0, region2, _arg2, _arg32, _arg42, _arg5, _arg62, magnificationSpec2);
                    return true;
                case TRANSACTION_findAccessibilityNodeInfosByViewId /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readLong();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        region = (Region) Region.CREATOR.createFromParcel(data);
                    } else {
                        region = null;
                    }
                    _arg3 = data.readInt();
                    _arg4 = android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                    _arg5 = data.readInt();
                    _arg6 = data.readInt();
                    _arg7 = data.readLong();
                    if (data.readInt() != 0) {
                        magnificationSpec = (MagnificationSpec) MagnificationSpec.CREATOR.createFromParcel(data);
                    } else {
                        magnificationSpec = null;
                    }
                    findAccessibilityNodeInfosByViewId(_arg0, _arg1, region, _arg3, _arg4, _arg5, _arg6, _arg7, magnificationSpec);
                    return true;
                case TRANSACTION_findAccessibilityNodeInfosByText /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readLong();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        region = (Region) Region.CREATOR.createFromParcel(data);
                    } else {
                        region = null;
                    }
                    _arg3 = data.readInt();
                    _arg4 = android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                    _arg5 = data.readInt();
                    _arg6 = data.readInt();
                    _arg7 = data.readLong();
                    if (data.readInt() != 0) {
                        magnificationSpec = (MagnificationSpec) MagnificationSpec.CREATOR.createFromParcel(data);
                    } else {
                        magnificationSpec = null;
                    }
                    findAccessibilityNodeInfosByText(_arg0, _arg1, region, _arg3, _arg4, _arg5, _arg6, _arg7, magnificationSpec);
                    return true;
                case TRANSACTION_findFocus /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readLong();
                    _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        region = (Region) Region.CREATOR.createFromParcel(data);
                    } else {
                        region = null;
                    }
                    _arg3 = data.readInt();
                    _arg4 = android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                    _arg5 = data.readInt();
                    _arg6 = data.readInt();
                    _arg7 = data.readLong();
                    if (data.readInt() != 0) {
                        magnificationSpec = (MagnificationSpec) MagnificationSpec.CREATOR.createFromParcel(data);
                    } else {
                        magnificationSpec = null;
                    }
                    findFocus(_arg0, _arg12, region, _arg3, _arg4, _arg5, _arg6, _arg7, magnificationSpec);
                    return true;
                case TRANSACTION_focusSearch /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readLong();
                    _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        region = (Region) Region.CREATOR.createFromParcel(data);
                    } else {
                        region = null;
                    }
                    _arg3 = data.readInt();
                    _arg4 = android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                    _arg5 = data.readInt();
                    _arg6 = data.readInt();
                    _arg7 = data.readLong();
                    if (data.readInt() != 0) {
                        magnificationSpec = (MagnificationSpec) MagnificationSpec.CREATOR.createFromParcel(data);
                    } else {
                        magnificationSpec = null;
                    }
                    focusSearch(_arg0, _arg12, region, _arg3, _arg4, _arg5, _arg6, _arg7, magnificationSpec);
                    return true;
                case TRANSACTION_performAccessibilityAction /*6*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readLong();
                    _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    performAccessibilityAction(_arg0, _arg12, bundle, data.readInt(), android.view.accessibility.IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readLong());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void findAccessibilityNodeInfoByAccessibilityId(long j, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void findAccessibilityNodeInfosByText(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void findAccessibilityNodeInfosByViewId(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void findFocus(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void focusSearch(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void performAccessibilityAction(long j, int i, Bundle bundle, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2) throws RemoteException;
}
