package com.android.ims.internal;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsReasonInfo;

public interface IImsRegistrationListener extends IInterface {

    public static abstract class Stub extends Binder implements IImsRegistrationListener {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsRegistrationListener";
        static final int TRANSACTION_registrationAssociatedUriChanged = 11;
        static final int TRANSACTION_registrationChangeFailed = 12;
        static final int TRANSACTION_registrationConnected = 1;
        static final int TRANSACTION_registrationConnectedWithRadioTech = 3;
        static final int TRANSACTION_registrationDisconnected = 5;
        static final int TRANSACTION_registrationFeatureCapabilityChanged = 9;
        static final int TRANSACTION_registrationProgressing = 2;
        static final int TRANSACTION_registrationProgressingWithRadioTech = 4;
        static final int TRANSACTION_registrationResumed = 6;
        static final int TRANSACTION_registrationServiceCapabilityChanged = 8;
        static final int TRANSACTION_registrationSuspended = 7;
        static final int TRANSACTION_voiceMessageCountUpdate = 10;

        private static class Proxy implements IImsRegistrationListener {
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

            public void registrationConnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationProgressing() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationConnectedWithRadioTech(int imsRadioTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(imsRadioTech);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationProgressingWithRadioTech(int imsRadioTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(imsRadioTech);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationDisconnected(ImsReasonInfo imsReasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (imsReasonInfo != null) {
                        _data.writeInt(1);
                        imsReasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationResumed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationSuspended() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationServiceCapabilityChanged(int serviceClass, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceClass);
                    _data.writeInt(event);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceClass);
                    _data.writeIntArray(enabledFeatures);
                    _data.writeIntArray(disabledFeatures);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void voiceMessageCountUpdate(int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(count);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationAssociatedUriChanged(Uri[] uris) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(uris, 0);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registrationChangeFailed(int targetAccessTech, ImsReasonInfo imsReasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(targetAccessTech);
                    if (imsReasonInfo != null) {
                        _data.writeInt(1);
                        imsReasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsRegistrationListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsRegistrationListener)) {
                return new Proxy(obj);
            }
            return (IImsRegistrationListener) iin;
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
                        registrationConnected();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        registrationProgressing();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        registrationConnectedWithRadioTech(data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registrationProgressingWithRadioTech(data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        registrationDisconnected(_arg1);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        registrationResumed();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        registrationSuspended();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        registrationServiceCapabilityChanged(data.readInt(), data.readInt());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        registrationFeatureCapabilityChanged(data.readInt(), data.createIntArray(), data.createIntArray());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        voiceMessageCountUpdate(data.readInt());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        registrationAssociatedUriChanged((Uri[]) data.createTypedArray(Uri.CREATOR));
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ImsReasonInfo.CREATOR.createFromParcel(data);
                        }
                        registrationChangeFailed(_arg0, _arg1);
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

    void registrationAssociatedUriChanged(Uri[] uriArr) throws RemoteException;

    void registrationChangeFailed(int i, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void registrationConnected() throws RemoteException;

    void registrationConnectedWithRadioTech(int i) throws RemoteException;

    void registrationDisconnected(ImsReasonInfo imsReasonInfo) throws RemoteException;

    void registrationFeatureCapabilityChanged(int i, int[] iArr, int[] iArr2) throws RemoteException;

    void registrationProgressing() throws RemoteException;

    void registrationProgressingWithRadioTech(int i) throws RemoteException;

    void registrationResumed() throws RemoteException;

    void registrationServiceCapabilityChanged(int i, int i2) throws RemoteException;

    void registrationSuspended() throws RemoteException;

    void voiceMessageCountUpdate(int i) throws RemoteException;
}
