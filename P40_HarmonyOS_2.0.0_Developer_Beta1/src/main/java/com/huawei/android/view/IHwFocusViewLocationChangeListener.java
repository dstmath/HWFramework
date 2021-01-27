package com.huawei.android.view;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwFocusViewLocationChangeListener extends IInterface {
    void onLocationChange(List<Rect> list, ComponentName componentName) throws RemoteException;

    public static class Default implements IHwFocusViewLocationChangeListener {
        @Override // com.huawei.android.view.IHwFocusViewLocationChangeListener
        public void onLocationChange(List<Rect> list, ComponentName componentName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwFocusViewLocationChangeListener {
        private static final String DESCRIPTOR = "com.huawei.android.view.IHwFocusViewLocationChangeListener";
        static final int TRANSACTION_onLocationChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwFocusViewLocationChangeListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwFocusViewLocationChangeListener)) {
                return new Proxy(obj);
            }
            return (IHwFocusViewLocationChangeListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onLocationChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                List<Rect> _arg0 = data.createTypedArrayList(Rect.CREATOR);
                if (data.readInt() != 0) {
                    _arg1 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onLocationChange(_arg0, _arg1);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwFocusViewLocationChangeListener {
            public static IHwFocusViewLocationChangeListener sDefaultImpl;
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

            @Override // com.huawei.android.view.IHwFocusViewLocationChangeListener
            public void onLocationChange(List<Rect> focusAreas, ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(focusAreas);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLocationChange(focusAreas, componentName);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwFocusViewLocationChangeListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwFocusViewLocationChangeListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
