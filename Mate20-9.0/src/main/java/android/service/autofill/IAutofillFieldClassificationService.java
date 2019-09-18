package android.service.autofill;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.view.autofill.AutofillValue;
import java.util.List;

public interface IAutofillFieldClassificationService extends IInterface {

    public static abstract class Stub extends Binder implements IAutofillFieldClassificationService {
        private static final String DESCRIPTOR = "android.service.autofill.IAutofillFieldClassificationService";
        static final int TRANSACTION_getScores = 1;

        private static class Proxy implements IAutofillFieldClassificationService {
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

            public void getScores(RemoteCallback callback, String algorithmName, Bundle algorithmArgs, List<AutofillValue> actualValues, String[] userDataValues) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(algorithmName);
                    if (algorithmArgs != null) {
                        _data.writeInt(1);
                        algorithmArgs.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(actualValues);
                    _data.writeStringArray(userDataValues);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAutofillFieldClassificationService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAutofillFieldClassificationService)) {
                return new Proxy(obj);
            }
            return (IAutofillFieldClassificationService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RemoteCallback _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                Bundle _arg2 = null;
                if (data.readInt() != 0) {
                    _arg0 = RemoteCallback.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                String _arg1 = data.readString();
                if (data.readInt() != 0) {
                    _arg2 = Bundle.CREATOR.createFromParcel(data);
                }
                getScores(_arg0, _arg1, _arg2, data.createTypedArrayList(AutofillValue.CREATOR), data.createStringArray());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void getScores(RemoteCallback remoteCallback, String str, Bundle bundle, List<AutofillValue> list, String[] strArr) throws RemoteException;
}
