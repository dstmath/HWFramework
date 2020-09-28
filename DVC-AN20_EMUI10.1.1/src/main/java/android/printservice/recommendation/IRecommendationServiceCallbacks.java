package android.printservice.recommendation;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IRecommendationServiceCallbacks extends IInterface {
    void onRecommendationsUpdated(List<RecommendationInfo> list) throws RemoteException;

    public static class Default implements IRecommendationServiceCallbacks {
        @Override // android.printservice.recommendation.IRecommendationServiceCallbacks
        public void onRecommendationsUpdated(List<RecommendationInfo> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRecommendationServiceCallbacks {
        private static final String DESCRIPTOR = "android.printservice.recommendation.IRecommendationServiceCallbacks";
        static final int TRANSACTION_onRecommendationsUpdated = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onRecommendationsUpdated";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onRecommendationsUpdated(data.createTypedArrayList(RecommendationInfo.CREATOR));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRecommendationServiceCallbacks {
            public static IRecommendationServiceCallbacks sDefaultImpl;
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

            @Override // android.printservice.recommendation.IRecommendationServiceCallbacks
            public void onRecommendationsUpdated(List<RecommendationInfo> recommendations) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(recommendations);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRecommendationsUpdated(recommendations);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRecommendationServiceCallbacks impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRecommendationServiceCallbacks getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
