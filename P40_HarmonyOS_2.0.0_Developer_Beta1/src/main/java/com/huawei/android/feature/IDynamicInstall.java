package com.huawei.android.feature;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import com.huawei.android.feature.IDynamicInstallCallback;
import java.util.ArrayList;
import java.util.List;

public interface IDynamicInstall extends IInterface {

    public abstract class Stub extends Binder implements IDynamicInstall {
        private static final String DESCRIPTOR = "com.huawei.android.feature.IDynamicInstall";
        static final int TRANSACTION_cancelInstall = 1;
        static final int TRANSACTION_deferredInstall = 2;
        static final int TRANSACTION_deferredUninstall = 3;
        static final int TRANSACTION_getSessionState = 4;
        static final int TRANSACTION_getSessionStates = 5;
        static final int TRANSACTION_startInstall = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDynamicInstall asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (queryLocalInterface == null || !(queryLocalInterface instanceof IDynamicInstall)) ? new a(iBinder) : (IDynamicInstall) queryLocalInterface;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) {
            Bundle bundle = null;
            switch (i) {
                case 1:
                    parcel.enforceInterface(DESCRIPTOR);
                    String readString = parcel.readString();
                    int readInt = parcel.readInt();
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    cancelInstall(readString, readInt, bundle, IDynamicInstallCallback.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    return true;
                case 2:
                    parcel.enforceInterface(DESCRIPTOR);
                    String readString2 = parcel.readString();
                    ArrayList createTypedArrayList = parcel.createTypedArrayList(Bundle.CREATOR);
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    deferredInstall(readString2, createTypedArrayList, bundle, IDynamicInstallCallback.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    return true;
                case 3:
                    parcel.enforceInterface(DESCRIPTOR);
                    String readString3 = parcel.readString();
                    ArrayList createTypedArrayList2 = parcel.createTypedArrayList(Bundle.CREATOR);
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    deferredUninstall(readString3, createTypedArrayList2, bundle, IDynamicInstallCallback.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    return true;
                case 4:
                    parcel.enforceInterface(DESCRIPTOR);
                    getSessionState(parcel.readString(), parcel.readInt(), IDynamicInstallCallback.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    return true;
                case 5:
                    parcel.enforceInterface(DESCRIPTOR);
                    getSessionStates(parcel.readString(), IDynamicInstallCallback.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    return true;
                case 6:
                    parcel.enforceInterface(DESCRIPTOR);
                    String readString4 = parcel.readString();
                    ArrayList createTypedArrayList3 = parcel.createTypedArrayList(Bundle.CREATOR);
                    if (parcel.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                    }
                    startInstall(readString4, createTypedArrayList3, bundle, IDynamicInstallCallback.Stub.asInterface(parcel.readStrongBinder()));
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

    void cancelInstall(String str, int i, Bundle bundle, IDynamicInstallCallback iDynamicInstallCallback);

    void deferredInstall(String str, List<Bundle> list, Bundle bundle, IDynamicInstallCallback iDynamicInstallCallback);

    void deferredUninstall(String str, List<Bundle> list, Bundle bundle, IDynamicInstallCallback iDynamicInstallCallback);

    void getSessionState(String str, int i, IDynamicInstallCallback iDynamicInstallCallback);

    void getSessionStates(String str, IDynamicInstallCallback iDynamicInstallCallback);

    void startInstall(String str, List<Bundle> list, Bundle bundle, IDynamicInstallCallback iDynamicInstallCallback);
}
