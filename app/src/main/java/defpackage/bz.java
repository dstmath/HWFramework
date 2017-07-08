package defpackage;

import android.os.IBinder;
import android.os.Parcel;
import com.huawei.bd.IBDService;

/* renamed from: bz */
public class bz implements IBDService {
    private IBinder cq;

    public bz(IBinder iBinder) {
        this.cq = iBinder;
    }

    public IBinder asBinder() {
        return this.cq;
    }

    public int sendAccumulativeData(String str, int i, int i2) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.huawei.bd.IBDService");
            obtain.writeString(str);
            obtain.writeInt(i);
            obtain.writeInt(i2);
            this.cq.transact(2, obtain, obtain2, 0);
            obtain2.readException();
            int readInt = obtain2.readInt();
            return readInt;
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }

    public int sendAppActionData(String str, int i, String str2, int i2) {
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.huawei.bd.IBDService");
            obtain.writeString(str);
            obtain.writeInt(i);
            obtain.writeString(str2);
            obtain.writeInt(i2);
            this.cq.transact(1, obtain, obtain2, 0);
            obtain2.readException();
            int readInt = obtain2.readInt();
            return readInt;
        } finally {
            obtain2.recycle();
            obtain.recycle();
        }
    }
}
