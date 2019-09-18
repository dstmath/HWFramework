package com.huawei.android.feature;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import java.util.List;

public interface IDynamicInstallCallback extends IInterface {

    public abstract class Stub extends Binder implements IDynamicInstallCallback {
        private static final String DESCRIPTOR = "com.huawei.android.feature.IDynamicInstallCallback";
        static final int TRANSACTION_onCancelInstall = 2;
        static final int TRANSACTION_onDeferredInstall = 6;
        static final int TRANSACTION_onDeferredUninstall = 4;
        static final int TRANSACTION_onError = 7;
        static final int TRANSACTION_onGetSession = 3;
        static final int TRANSACTION_onGetSessionStates = 5;
        static final int TRANSACTION_onStartInstall = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDynamicInstallCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (queryLocalInterface == null || !(queryLocalInterface instanceof IDynamicInstallCallback)) ? new b(iBinder) : (IDynamicInstallCallback) queryLocalInterface;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) {
            Bundle bundle = null;
            switch (i) {
                case 1:
                    parcel.enforceInterface(DESCRIPTOR);
                    int readInt = parcel.readInt();
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    onStartInstall(readInt, bundle);
                    parcel2.writeNoException();
                    return true;
                case 2:
                    parcel.enforceInterface(DESCRIPTOR);
                    int readInt2 = parcel.readInt();
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    onCancelInstall(readInt2, bundle);
                    parcel2.writeNoException();
                    return true;
                case 3:
                    parcel.enforceInterface(DESCRIPTOR);
                    int readInt3 = parcel.readInt();
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    onGetSession(readInt3, bundle);
                    parcel2.writeNoException();
                    return true;
                case 4:
                    parcel.enforceInterface(DESCRIPTOR);
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    onDeferredUninstall(bundle);
                    parcel2.writeNoException();
                    return true;
                case 5:
                    parcel.enforceInterface(DESCRIPTOR);
                    onGetSessionStates(parcel.createTypedArrayList(Bundle.CREATOR));
                    parcel2.writeNoException();
                    return true;
                case 6:
                    parcel.enforceInterface(DESCRIPTOR);
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    onDeferredInstall(bundle);
                    parcel2.writeNoException();
                    return true;
                case 7:
                    parcel.enforceInterface(DESCRIPTOR);
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    onError(bundle);
                    parcel2.writeNoException();
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    void onCancelInstall(int i, Bundle bundle);

    void onDeferredInstall(Bundle bundle);

    void onDeferredUninstall(Bundle bundle);

    void onError(Bundle bundle);

    void onGetSession(int i, Bundle bundle);

    void onGetSessionStates(List<Bundle> list);

    void onStartInstall(int i, Bundle bundle);
}
