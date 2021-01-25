package huawei.android.view;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.RemoteViews;
import java.util.List;

public interface IHwExtDisplayUI extends IInterface {
    void addCustomViews(List<Rect> list, List<RemoteViews> list2) throws RemoteException;

    void executeSideAnimation(int i, boolean z) throws RemoteException;

    void removeCustomViews(List<Rect> list) throws RemoteException;

    void setTouchMapping(List<Rect> list, List<Rect> list2) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwExtDisplayUI {
        private static final String DESCRIPTOR = "huawei.android.view.IHwExtDisplayUI";
        static final int TRANSACTION_addCustomViews = 2;
        static final int TRANSACTION_executeSideAnimation = 1;
        static final int TRANSACTION_removeCustomViews = 3;
        static final int TRANSACTION_setTouchMapping = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwExtDisplayUI asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwExtDisplayUI)) {
                return new Proxy(obj);
            }
            return (IHwExtDisplayUI) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                executeSideAnimation(data.readInt(), data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                addCustomViews(data.createTypedArrayList(Rect.CREATOR), data.createTypedArrayList(RemoteViews.CREATOR));
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                removeCustomViews(data.createTypedArrayList(Rect.CREATOR));
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                setTouchMapping(data.createTypedArrayList(Rect.CREATOR), data.createTypedArrayList(Rect.CREATOR));
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwExtDisplayUI {
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

            @Override // huawei.android.view.IHwExtDisplayUI
            public void executeSideAnimation(int type, boolean isStart) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(isStart ? 1 : 0);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.view.IHwExtDisplayUI
            public void addCustomViews(List<Rect> rects, List<RemoteViews> customViews) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(rects);
                    _data.writeTypedList(customViews);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.view.IHwExtDisplayUI
            public void removeCustomViews(List<Rect> rects) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(rects);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.view.IHwExtDisplayUI
            public void setTouchMapping(List<Rect> fromRects, List<Rect> toRects) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(fromRects);
                    _data.writeTypedList(toRects);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
