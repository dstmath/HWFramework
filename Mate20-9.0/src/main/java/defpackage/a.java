package defpackage;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import com.huawei.android.feature.IDynamicInstall;
import com.huawei.android.feature.IDynamicInstallCallback;
import java.util.List;

/* renamed from: a  reason: default package */
public final class a implements IDynamicInstall {
    private IBinder a;

    public a(IBinder iBinder) {
        this.a = iBinder;
    }

    public final IBinder asBinder() {
        return this.a;
    }

    public final void cancelInstall(String str, int i, Bundle bundle, IDynamicInstallCallback iDynamicInstallCallback) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.huawei.android.feature.IDynamicInstall");
            obtain.writeString(str);
            obtain.writeInt(i);
            if (bundle != null) {
                obtain.writeInt(1);
                bundle.writeToParcel(obtain, 0);
            } else {
                obtain.writeInt(0);
            }
            obtain.writeStrongBinder(iDynamicInstallCallback != null ? iDynamicInstallCallback.asBinder() : null);
            this.a.transact(1, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    public final void deferredInstall(String str, List<Bundle> list, Bundle bundle, IDynamicInstallCallback iDynamicInstallCallback) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.huawei.android.feature.IDynamicInstall");
            obtain.writeString(str);
            obtain.writeTypedList(list);
            if (bundle != null) {
                obtain.writeInt(1);
                bundle.writeToParcel(obtain, 0);
            } else {
                obtain.writeInt(0);
            }
            obtain.writeStrongBinder(iDynamicInstallCallback != null ? iDynamicInstallCallback.asBinder() : null);
            this.a.transact(2, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    public final void deferredUninstall(String str, List<Bundle> list, Bundle bundle, IDynamicInstallCallback iDynamicInstallCallback) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.huawei.android.feature.IDynamicInstall");
            obtain.writeString(str);
            obtain.writeTypedList(list);
            if (bundle != null) {
                obtain.writeInt(1);
                bundle.writeToParcel(obtain, 0);
            } else {
                obtain.writeInt(0);
            }
            obtain.writeStrongBinder(iDynamicInstallCallback != null ? iDynamicInstallCallback.asBinder() : null);
            this.a.transact(3, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    public final void getSessionState(String str, int i, IDynamicInstallCallback iDynamicInstallCallback) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.huawei.android.feature.IDynamicInstall");
            obtain.writeString(str);
            obtain.writeInt(i);
            obtain.writeStrongBinder(iDynamicInstallCallback != null ? iDynamicInstallCallback.asBinder() : null);
            this.a.transact(4, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    public final void getSessionStates(String str, IDynamicInstallCallback iDynamicInstallCallback) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.huawei.android.feature.IDynamicInstall");
            obtain.writeString(str);
            obtain.writeStrongBinder(iDynamicInstallCallback != null ? iDynamicInstallCallback.asBinder() : null);
            this.a.transact(5, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    public final void startInstall(String str, List<Bundle> list, Bundle bundle, IDynamicInstallCallback iDynamicInstallCallback) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.huawei.android.feature.IDynamicInstall");
            obtain.writeString(str);
            obtain.writeTypedList(list);
            if (bundle != null) {
                obtain.writeInt(1);
                bundle.writeToParcel(obtain, 0);
            } else {
                obtain.writeInt(0);
            }
            obtain.writeStrongBinder(iDynamicInstallCallback != null ? iDynamicInstallCallback.asBinder() : null);
            this.a.transact(6, obtain, obtain2, 0);
            obtain2.readException();
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }
}
