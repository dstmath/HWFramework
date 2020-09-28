package android.view.accessibility;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IAccessibilityInteractionConnectionCallback extends IInterface {
    @UnsupportedAppUsage
    void setFindAccessibilityNodeInfoResult(AccessibilityNodeInfo accessibilityNodeInfo, int i) throws RemoteException;

    @UnsupportedAppUsage
    void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> list, int i) throws RemoteException;

    @UnsupportedAppUsage
    void setPerformAccessibilityActionResult(boolean z, int i) throws RemoteException;

    public static class Default implements IAccessibilityInteractionConnectionCallback {
        @Override // android.view.accessibility.IAccessibilityInteractionConnectionCallback
        public void setFindAccessibilityNodeInfoResult(AccessibilityNodeInfo info, int interactionId) throws RemoteException {
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnectionCallback
        public void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> list, int interactionId) throws RemoteException {
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnectionCallback
        public void setPerformAccessibilityActionResult(boolean succeeded, int interactionId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAccessibilityInteractionConnectionCallback {
        private static final String DESCRIPTOR = "android.view.accessibility.IAccessibilityInteractionConnectionCallback";
        static final int TRANSACTION_setFindAccessibilityNodeInfoResult = 1;
        static final int TRANSACTION_setFindAccessibilityNodeInfosResult = 2;
        static final int TRANSACTION_setPerformAccessibilityActionResult = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAccessibilityInteractionConnectionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAccessibilityInteractionConnectionCallback)) {
                return new Proxy(obj);
            }
            return (IAccessibilityInteractionConnectionCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "setFindAccessibilityNodeInfoResult";
            }
            if (transactionCode == 2) {
                return "setFindAccessibilityNodeInfosResult";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "setPerformAccessibilityActionResult";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AccessibilityNodeInfo _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = AccessibilityNodeInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                setFindAccessibilityNodeInfoResult(_arg0, data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                setFindAccessibilityNodeInfosResult(data.createTypedArrayList(AccessibilityNodeInfo.CREATOR), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                setPerformAccessibilityActionResult(data.readInt() != 0, data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAccessibilityInteractionConnectionCallback {
            public static IAccessibilityInteractionConnectionCallback sDefaultImpl;
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

            @Override // android.view.accessibility.IAccessibilityInteractionConnectionCallback
            public void setFindAccessibilityNodeInfoResult(AccessibilityNodeInfo info, int interactionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(interactionId);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setFindAccessibilityNodeInfoResult(info, interactionId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.accessibility.IAccessibilityInteractionConnectionCallback
            public void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> infos, int interactionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(infos);
                    _data.writeInt(interactionId);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setFindAccessibilityNodeInfosResult(infos, interactionId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.accessibility.IAccessibilityInteractionConnectionCallback
            public void setPerformAccessibilityActionResult(boolean succeeded, int interactionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(succeeded ? 1 : 0);
                    _data.writeInt(interactionId);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPerformAccessibilityActionResult(succeeded, interactionId);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAccessibilityInteractionConnectionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAccessibilityInteractionConnectionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
