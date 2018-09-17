package android.view.accessibility;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IAccessibilityInteractionConnectionCallback extends IInterface {

    public static abstract class Stub extends Binder implements IAccessibilityInteractionConnectionCallback {
        private static final String DESCRIPTOR = "android.view.accessibility.IAccessibilityInteractionConnectionCallback";
        static final int TRANSACTION_setFindAccessibilityNodeInfoResult = 1;
        static final int TRANSACTION_setFindAccessibilityNodeInfosResult = 2;
        static final int TRANSACTION_setPerformAccessibilityActionResult = 3;

        private static class Proxy implements IAccessibilityInteractionConnectionCallback {
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> infos, int interactionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(infos);
                    _data.writeInt(interactionId);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setPerformAccessibilityActionResult(boolean succeeded, int interactionId) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!succeeded) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(interactionId);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    AccessibilityNodeInfo _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (AccessibilityNodeInfo) AccessibilityNodeInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    setFindAccessibilityNodeInfoResult(_arg0, data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    setFindAccessibilityNodeInfosResult(data.createTypedArrayList(AccessibilityNodeInfo.CREATOR), data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    setPerformAccessibilityActionResult(data.readInt() != 0, data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void setFindAccessibilityNodeInfoResult(AccessibilityNodeInfo accessibilityNodeInfo, int i) throws RemoteException;

    void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> list, int i) throws RemoteException;

    void setPerformAccessibilityActionResult(boolean z, int i) throws RemoteException;
}
