package android.telephony.data;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IDataServiceCallback extends IInterface {

    public static abstract class Stub extends Binder implements IDataServiceCallback {
        private static final String DESCRIPTOR = "android.telephony.data.IDataServiceCallback";
        static final int TRANSACTION_onDataCallListChanged = 6;
        static final int TRANSACTION_onDeactivateDataCallComplete = 2;
        static final int TRANSACTION_onGetDataCallListComplete = 5;
        static final int TRANSACTION_onSetDataProfileComplete = 4;
        static final int TRANSACTION_onSetInitialAttachApnComplete = 3;
        static final int TRANSACTION_onSetupDataCallComplete = 1;

        private static class Proxy implements IDataServiceCallback {
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

            public void onSetupDataCallComplete(int result, DataCallResponse dataCallResponse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    if (dataCallResponse != null) {
                        _data.writeInt(1);
                        dataCallResponse.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeactivateDataCallComplete(int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSetInitialAttachApnComplete(int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSetDataProfileComplete(int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onGetDataCallListComplete(int result, List<DataCallResponse> dataCallList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    _data.writeTypedList(dataCallList);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDataCallListChanged(List<DataCallResponse> dataCallList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(dataCallList);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDataServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDataServiceCallback)) {
                return new Proxy(obj);
            }
            return (IDataServiceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DataCallResponse _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = DataCallResponse.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onSetupDataCallComplete(_arg0, _arg1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onDeactivateDataCallComplete(data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onSetInitialAttachApnComplete(data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onSetDataProfileComplete(data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onGetDataCallListComplete(data.readInt(), data.createTypedArrayList(DataCallResponse.CREATOR));
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onDataCallListChanged(data.createTypedArrayList(DataCallResponse.CREATOR));
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onDataCallListChanged(List<DataCallResponse> list) throws RemoteException;

    void onDeactivateDataCallComplete(int i) throws RemoteException;

    void onGetDataCallListComplete(int i, List<DataCallResponse> list) throws RemoteException;

    void onSetDataProfileComplete(int i) throws RemoteException;

    void onSetInitialAttachApnComplete(int i) throws RemoteException;

    void onSetupDataCallComplete(int i, DataCallResponse dataCallResponse) throws RemoteException;
}
