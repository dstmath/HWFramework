package com.huawei.security.hwassetmanager;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.security.keymaster.HwKeymasterBlob;

public interface IHwAssetObserver extends IInterface {
    void onEvent(int i, int i2, HwKeymasterBlob hwKeymasterBlob) throws RemoteException;

    public static class Default implements IHwAssetObserver {
        @Override // com.huawei.security.hwassetmanager.IHwAssetObserver
        public void onEvent(int event, int dataType, HwKeymasterBlob extra) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAssetObserver {
        private static final String DESCRIPTOR = "com.huawei.security.hwassetmanager.IHwAssetObserver";
        static final int TRANSACTION_onEvent = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAssetObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAssetObserver)) {
                return new Proxy(obj);
            }
            return (IHwAssetObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwKeymasterBlob _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = (HwKeymasterBlob) HwKeymasterBlob.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                onEvent(_arg0, _arg1, _arg2);
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
        public static class Proxy implements IHwAssetObserver {
            public static IHwAssetObserver sDefaultImpl;
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

            @Override // com.huawei.security.hwassetmanager.IHwAssetObserver
            public void onEvent(int event, int dataType, HwKeymasterBlob extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    _data.writeInt(dataType);
                    if (extra != null) {
                        _data.writeInt(1);
                        extra.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onEvent(event, dataType, extra);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwAssetObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAssetObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
