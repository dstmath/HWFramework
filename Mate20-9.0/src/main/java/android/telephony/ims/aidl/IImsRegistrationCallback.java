package android.telephony.ims.aidl;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsReasonInfo;

public interface IImsRegistrationCallback extends IInterface {

    public static abstract class Stub extends Binder implements IImsRegistrationCallback {
        private static final String DESCRIPTOR = "android.telephony.ims.aidl.IImsRegistrationCallback";
        static final int TRANSACTION_onDeregistered = 3;
        static final int TRANSACTION_onRegistered = 1;
        static final int TRANSACTION_onRegistering = 2;
        static final int TRANSACTION_onSubscriberAssociatedUriChanged = 5;
        static final int TRANSACTION_onTechnologyChangeFailed = 4;

        private static class Proxy implements IImsRegistrationCallback {
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

            public void onRegistered(int imsRadioTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(imsRadioTech);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRegistering(int imsRadioTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(imsRadioTech);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeregistered(ImsReasonInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTechnologyChangeFailed(int imsRadioTech, ImsReasonInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(imsRadioTech);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSubscriberAssociatedUriChanged(Uri[] uris) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(uris, 0);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsRegistrationCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsRegistrationCallback)) {
                return new Proxy(obj);
            }
            return (IImsRegistrationCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ImsReasonInfo _arg1 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onRegistered(data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onRegistering(data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        onDeregistered(_arg1);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        onTechnologyChangeFailed(_arg0, _arg1);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onSubscriberAssociatedUriChanged((Uri[]) data.createTypedArray(Uri.CREATOR));
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

    void onDeregistered(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void onRegistered(int i) throws RemoteException;

    void onRegistering(int i) throws RemoteException;

    void onSubscriberAssociatedUriChanged(Uri[] uriArr) throws RemoteException;

    void onTechnologyChangeFailed(int i, ImsReasonInfo imsReasonInfo) throws RemoteException;
}
