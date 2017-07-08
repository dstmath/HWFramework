package com.tencent.tmsecurelite.base;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

public interface ITmsProvider extends IInterface {

    public static abstract class Stub extends Binder implements ITmsProvider {

        private static class Proxy implements ITmsProvider {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int ipcCall(int i, Bundle bundle, Bundle bundle2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsProvider");
                    obtain.writeInt(i);
                    if (bundle == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    }
                    if (bundle2 == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        bundle2.writeToParcel(obtain, 0);
                    }
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    if (obtain2.readInt() != 0) {
                        bundle2.readFromParcel(obtain2);
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int getVersion() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsProvider");
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.tencent.tmsecurelite.base.ITmsProvider");
        }

        public static ITmsProvider asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.tencent.tmsecurelite.base.ITmsProvider");
            if (queryLocalInterface != null && (queryLocalInterface instanceof ITmsProvider)) {
                return (ITmsProvider) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            Bundle bundle = null;
            int ipcCall;
            switch (i) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    Bundle bundle2;
                    parcel.enforceInterface("com.tencent.tmsecurelite.base.ITmsProvider");
                    int readInt = parcel.readInt();
                    if (parcel.readInt() == 0) {
                        bundle2 = null;
                    } else {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    ipcCall = ipcCall(readInt, bundle2, bundle);
                    parcel2.writeNoException();
                    parcel2.writeInt(ipcCall);
                    if (bundle == null) {
                        parcel2.writeInt(0);
                    } else {
                        parcel2.writeInt(1);
                        bundle.writeToParcel(parcel2, 1);
                    }
                    return true;
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    parcel.enforceInterface("com.tencent.tmsecurelite.base.ITmsProvider");
                    ipcCall = getVersion();
                    parcel2.writeNoException();
                    parcel2.writeInt(ipcCall);
                    return true;
                case 1598968902:
                    parcel2.writeString("com.tencent.tmsecurelite.base.ITmsProvider");
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    int getVersion() throws RemoteException;

    int ipcCall(int i, Bundle bundle, Bundle bundle2) throws RemoteException;
}
