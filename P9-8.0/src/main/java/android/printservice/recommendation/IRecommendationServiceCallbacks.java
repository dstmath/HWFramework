package android.printservice.recommendation;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IRecommendationServiceCallbacks extends IInterface {

    public static abstract class Stub extends Binder implements IRecommendationServiceCallbacks {
        private static final String DESCRIPTOR = "android.printservice.recommendation.IRecommendationServiceCallbacks";
        static final int TRANSACTION_onRecommendationsUpdated = 1;

        private static class Proxy implements IRecommendationServiceCallbacks {
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

            public void onRecommendationsUpdated(List<RecommendationInfo> recommendations) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(recommendations);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRecommendationServiceCallbacks asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRecommendationServiceCallbacks)) {
                return new Proxy(obj);
            }
            return (IRecommendationServiceCallbacks) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onRecommendationsUpdated(data.createTypedArrayList(RecommendationInfo.CREATOR));
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onRecommendationsUpdated(List<RecommendationInfo> list) throws RemoteException;
}
