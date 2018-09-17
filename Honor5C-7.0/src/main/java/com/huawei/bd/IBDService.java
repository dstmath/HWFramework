package com.huawei.bd;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import defpackage.bz;

public interface IBDService extends IInterface {

    public abstract class Stub extends Binder implements IBDService {
        private static final String DESCRIPTOR = "com.huawei.bd.IBDService";
        static final int TRANSACTION_sendAccumulativeData = 2;
        static final int TRANSACTION_sendAppActionData = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBDService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (queryLocalInterface == null || !(queryLocalInterface instanceof IBDService)) ? new bz(iBinder) : (IBDService) queryLocalInterface;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) {
            int sendAppActionData;
            switch (i) {
                case TRANSACTION_sendAppActionData /*1*/:
                    parcel.enforceInterface(DESCRIPTOR);
                    sendAppActionData = sendAppActionData(parcel.readString(), parcel.readInt(), parcel.readString(), parcel.readInt());
                    parcel2.writeNoException();
                    parcel2.writeInt(sendAppActionData);
                    return true;
                case TRANSACTION_sendAccumulativeData /*2*/:
                    parcel.enforceInterface(DESCRIPTOR);
                    sendAppActionData = sendAccumulativeData(parcel.readString(), parcel.readInt(), parcel.readInt());
                    parcel2.writeNoException();
                    parcel2.writeInt(sendAppActionData);
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    int sendAccumulativeData(String str, int i, int i2);

    int sendAppActionData(String str, int i, String str2, int i2);
}
