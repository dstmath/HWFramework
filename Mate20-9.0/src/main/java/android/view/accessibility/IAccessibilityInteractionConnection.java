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

            public void findAccessibilityNodeInfoByAccessibilityId(long accessibilityNodeId, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec, Bundle arguments) throws RemoteException {
                Region region = bounds;
                MagnificationSpec magnificationSpec = spec;
                Bundle bundle = arguments;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(accessibilityNodeId);
                        if (region != null) {
                            _data.writeInt(1);
                            region.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(interactionId);
                            _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                            try {
                                _data.writeInt(flags);
                                try {
                                    _data.writeInt(interrogatingPid);
                                } catch (Throwable th) {
                                    th = th;
                                    long j = interrogatingTid;
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                int i = interrogatingPid;
                                long j2 = interrogatingTid;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i2 = flags;
                            int i3 = interrogatingPid;
                            long j22 = interrogatingTid;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i4 = interactionId;
                        int i22 = flags;
                        int i32 = interrogatingPid;
                        long j222 = interrogatingTid;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(interrogatingTid);
                        if (magnificationSpec != null) {
                            _data.writeInt(1);
                            magnificationSpec.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (bundle != null) {
                            _data.writeInt(1);
                            bundle.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            this.mRemote.transact(1, _data, null, 1);
                            _data.recycle();
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
                } catch (Throwable th7) {
                    th = th7;
                    long j3 = accessibilityNodeId;
                    int i42 = interactionId;
                    int i222 = flags;
                    int i322 = interrogatingPid;
                    long j2222 = interrogatingTid;
                    _data.recycle();
                    throw th;
                }
            }

            public void findAccessibilityNodeInfosByViewId(long accessibilityNodeId, String viewId, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Region region = bounds;
                MagnificationSpec magnificationSpec = spec;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(accessibilityNodeId);
                        try {
                            _data.writeString(viewId);
                            if (region != null) {
                                _data.writeInt(1);
                                region.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeInt(interactionId);
                                _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                                try {
                                    _data.writeInt(flags);
                                    try {
                                        _data.writeInt(interrogatingPid);
                                    } catch (Throwable th) {
                                        th = th;
                                        long j = interrogatingTid;
                                        _data.recycle();
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    int i = interrogatingPid;
                                    long j2 = interrogatingTid;
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                int i2 = flags;
                                int i3 = interrogatingPid;
                                long j22 = interrogatingTid;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            int i4 = interactionId;
                            int i22 = flags;
                            int i32 = interrogatingPid;
                            long j222 = interrogatingTid;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(interrogatingTid);
                            if (magnificationSpec != null) {
                                _data.writeInt(1);
                                magnificationSpec.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                this.mRemote.transact(2, _data, null, 1);
                                _data.recycle();
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
                    } catch (Throwable th7) {
                        th = th7;
                        String str = viewId;
                        int i42 = interactionId;
                        int i222 = flags;
                        int i322 = interrogatingPid;
                        long j2222 = interrogatingTid;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    long j3 = accessibilityNodeId;
                    String str2 = viewId;
                    int i422 = interactionId;
                    int i2222 = flags;
                    int i3222 = interrogatingPid;
                    long j22222 = interrogatingTid;
                    _data.recycle();
                    throw th;
                }
            }

            public void findAccessibilityNodeInfosByText(long accessibilityNodeId, String text, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Region region = bounds;
                MagnificationSpec magnificationSpec = spec;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(accessibilityNodeId);
                        try {
                            _data.writeString(text);
                            if (region != null) {
                                _data.writeInt(1);
                                region.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeInt(interactionId);
                                _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                                try {
                                    _data.writeInt(flags);
                                    try {
                                        _data.writeInt(interrogatingPid);
                                    } catch (Throwable th) {
                                        th = th;
                                        long j = interrogatingTid;
                                        _data.recycle();
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    int i = interrogatingPid;
                                    long j2 = interrogatingTid;
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                int i2 = flags;
                                int i3 = interrogatingPid;
                                long j22 = interrogatingTid;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            int i4 = interactionId;
                            int i22 = flags;
                            int i32 = interrogatingPid;
                            long j222 = interrogatingTid;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(interrogatingTid);
                            if (magnificationSpec != null) {
                                _data.writeInt(1);
                                magnificationSpec.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                this.mRemote.transact(3, _data, null, 1);
                                _data.recycle();
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
                    } catch (Throwable th7) {
                        th = th7;
                        String str = text;
                        int i42 = interactionId;
                        int i222 = flags;
                        int i322 = interrogatingPid;
                        long j2222 = interrogatingTid;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    long j3 = accessibilityNodeId;
                    String str2 = text;
                    int i422 = interactionId;
                    int i2222 = flags;
                    int i3222 = interrogatingPid;
                    long j22222 = interrogatingTid;
                    _data.recycle();
                    throw th;
                }
            }

            public void findFocus(long accessibilityNodeId, int focusType, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Region region = bounds;
                MagnificationSpec magnificationSpec = spec;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(accessibilityNodeId);
                        try {
                            _data.writeInt(focusType);
                            if (region != null) {
                                _data.writeInt(1);
                                region.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeInt(interactionId);
                                _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                                try {
                                    _data.writeInt(flags);
                                    try {
                                        _data.writeInt(interrogatingPid);
                                    } catch (Throwable th) {
                                        th = th;
                                        long j = interrogatingTid;
                                        _data.recycle();
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    int i = interrogatingPid;
                                    long j2 = interrogatingTid;
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                int i2 = flags;
                                int i3 = interrogatingPid;
                                long j22 = interrogatingTid;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            int i4 = interactionId;
                            int i22 = flags;
                            int i32 = interrogatingPid;
                            long j222 = interrogatingTid;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(interrogatingTid);
                            if (magnificationSpec != null) {
                                _data.writeInt(1);
                                magnificationSpec.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                this.mRemote.transact(4, _data, null, 1);
                                _data.recycle();
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
                    } catch (Throwable th7) {
                        th = th7;
                        int i5 = focusType;
                        int i42 = interactionId;
                        int i222 = flags;
                        int i322 = interrogatingPid;
                        long j2222 = interrogatingTid;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    long j3 = accessibilityNodeId;
                    int i52 = focusType;
                    int i422 = interactionId;
                    int i2222 = flags;
                    int i3222 = interrogatingPid;
                    long j22222 = interrogatingTid;
                    _data.recycle();
                    throw th;
                }
            }

            public void focusSearch(long accessibilityNodeId, int direction, Region bounds, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) throws RemoteException {
                Region region = bounds;
                MagnificationSpec magnificationSpec = spec;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(accessibilityNodeId);
                        try {
                            _data.writeInt(direction);
                            if (region != null) {
                                _data.writeInt(1);
                                region.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeInt(interactionId);
                                _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                                try {
                                    _data.writeInt(flags);
                                    try {
                                        _data.writeInt(interrogatingPid);
                                    } catch (Throwable th) {
                                        th = th;
                                        long j = interrogatingTid;
                                        _data.recycle();
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    int i = interrogatingPid;
                                    long j2 = interrogatingTid;
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                int i2 = flags;
                                int i3 = interrogatingPid;
                                long j22 = interrogatingTid;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            int i4 = interactionId;
                            int i22 = flags;
                            int i32 = interrogatingPid;
                            long j222 = interrogatingTid;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(interrogatingTid);
                            if (magnificationSpec != null) {
                                _data.writeInt(1);
                                magnificationSpec.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                this.mRemote.transact(5, _data, null, 1);
                                _data.recycle();
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
                    } catch (Throwable th7) {
                        th = th7;
                        int i5 = direction;
                        int i42 = interactionId;
                        int i222 = flags;
                        int i322 = interrogatingPid;
                        long j2222 = interrogatingTid;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    long j3 = accessibilityNodeId;
                    int i52 = direction;
                    int i422 = interactionId;
                    int i2222 = flags;
                    int i3222 = interrogatingPid;
                    long j22222 = interrogatingTid;
                    _data.recycle();
                    throw th;
                }
            }

            public void performAccessibilityAction(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
                    this.mRemote.transact(6, _data, null, 1);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v0, resolved type: android.view.MagnificationSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v11, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r37v1, resolved type: android.view.MagnificationSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v15, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r25v1, resolved type: android.view.MagnificationSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v19, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r35v1, resolved type: android.view.MagnificationSpec} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v23, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v29, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v30, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v31, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v32, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v33, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v34, resolved type: android.os.Bundle} */
        /* JADX WARNING: type inference failed for: r3v10, types: [android.view.MagnificationSpec] */
        /* JADX WARNING: type inference failed for: r3v14, types: [android.view.MagnificationSpec] */
        /* JADX WARNING: type inference failed for: r3v18, types: [android.view.MagnificationSpec] */
        /* JADX WARNING: type inference failed for: r3v22, types: [android.view.MagnificationSpec] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Region _arg1;
            MagnificationSpec _arg7;
            Region _arg2;
            Region _arg22;
            Region _arg23;
            Region _arg24;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                Bundle _arg8 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _arg0 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg1 = (Region) Region.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        int _arg25 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg3 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg4 = data.readInt();
                        int _arg5 = data.readInt();
                        long _arg6 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg7 = MagnificationSpec.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg7 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg8 = Bundle.CREATOR.createFromParcel(parcel);
                        }
                        findAccessibilityNodeInfoByAccessibilityId(_arg0, _arg1, _arg25, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _arg02 = data.readLong();
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (Region) Region.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        int _arg32 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg42 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg52 = data.readInt();
                        int _arg62 = data.readInt();
                        long _arg72 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg8 = MagnificationSpec.CREATOR.createFromParcel(parcel);
                        }
                        findAccessibilityNodeInfosByViewId(_arg02, _arg12, _arg2, _arg32, _arg42, _arg52, _arg62, _arg72, _arg8);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _arg03 = data.readLong();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Region) Region.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        int _arg33 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg43 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg53 = data.readInt();
                        int _arg63 = data.readInt();
                        long _arg73 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg8 = MagnificationSpec.CREATOR.createFromParcel(parcel);
                        }
                        findAccessibilityNodeInfosByText(_arg03, _arg13, _arg22, _arg33, _arg43, _arg53, _arg63, _arg73, _arg8);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _arg04 = data.readLong();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (Region) Region.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg23 = null;
                        }
                        int _arg34 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg44 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg54 = data.readInt();
                        int _arg64 = data.readInt();
                        long _arg74 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg8 = MagnificationSpec.CREATOR.createFromParcel(parcel);
                        }
                        findFocus(_arg04, _arg14, _arg23, _arg34, _arg44, _arg54, _arg64, _arg74, _arg8);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _arg05 = data.readLong();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg24 = (Region) Region.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg24 = null;
                        }
                        int _arg35 = data.readInt();
                        IAccessibilityInteractionConnectionCallback _arg45 = IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg55 = data.readInt();
                        int _arg65 = data.readInt();
                        long _arg75 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg8 = MagnificationSpec.CREATOR.createFromParcel(parcel);
                        }
                        focusSearch(_arg05, _arg15, _arg24, _arg35, _arg45, _arg55, _arg65, _arg75, _arg8);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _arg06 = data.readLong();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg8 = Bundle.CREATOR.createFromParcel(parcel);
                        }
                        performAccessibilityAction(_arg06, _arg16, _arg8, data.readInt(), IAccessibilityInteractionConnectionCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readLong());
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

    void findAccessibilityNodeInfoByAccessibilityId(long j, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec, Bundle bundle) throws RemoteException;

    void findAccessibilityNodeInfosByText(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void findAccessibilityNodeInfosByViewId(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void findFocus(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void focusSearch(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) throws RemoteException;

    void performAccessibilityAction(long j, int i, Bundle bundle, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2) throws RemoteException;
}
