package com.android.ims.internal;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.ImsReasonInfo;

public interface IImsRegistrationListener extends IInterface {

    public static abstract class Stub extends Binder implements IImsRegistrationListener {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsRegistrationListener";
        static final int TRANSACTION_registrationAssociatedUriChanged = 11;
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
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_registrationConnected, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registrationProgressing() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_registrationProgressing, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registrationConnectedWithRadioTech(int imsRadioTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(imsRadioTech);
                    this.mRemote.transact(Stub.TRANSACTION_registrationConnectedWithRadioTech, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registrationProgressingWithRadioTech(int imsRadioTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(imsRadioTech);
                    this.mRemote.transact(Stub.TRANSACTION_registrationProgressingWithRadioTech, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registrationDisconnected(ImsReasonInfo imsReasonInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (imsReasonInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_registrationConnected);
                        imsReasonInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_registrationDisconnected, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registrationResumed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_registrationResumed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registrationSuspended() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_registrationSuspended, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registrationServiceCapabilityChanged(int serviceClass, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceClass);
                    _data.writeInt(event);
                    this.mRemote.transact(Stub.TRANSACTION_registrationServiceCapabilityChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registrationFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceClass);
                    _data.writeIntArray(enabledFeatures);
                    _data.writeIntArray(disabledFeatures);
                    this.mRemote.transact(Stub.TRANSACTION_registrationFeatureCapabilityChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void voiceMessageCountUpdate(int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(count);
                    this.mRemote.transact(Stub.TRANSACTION_voiceMessageCountUpdate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registrationAssociatedUriChanged(Uri[] uris) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(uris, 0);
                    this.mRemote.transact(Stub.TRANSACTION_registrationAssociatedUriChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
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
            switch (code) {
                case TRANSACTION_registrationConnected /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    registrationConnected();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registrationProgressing /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    registrationProgressing();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registrationConnectedWithRadioTech /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    registrationConnectedWithRadioTech(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registrationProgressingWithRadioTech /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    registrationProgressingWithRadioTech(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registrationDisconnected /*5*/:
                    ImsReasonInfo imsReasonInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    registrationDisconnected(imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registrationResumed /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    registrationResumed();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registrationSuspended /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    registrationSuspended();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registrationServiceCapabilityChanged /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    registrationServiceCapabilityChanged(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registrationFeatureCapabilityChanged /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    registrationFeatureCapabilityChanged(data.readInt(), data.createIntArray(), data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_voiceMessageCountUpdate /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    voiceMessageCountUpdate(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registrationAssociatedUriChanged /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    registrationAssociatedUriChanged((Uri[]) data.createTypedArray(Uri.CREATOR));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void registrationAssociatedUriChanged(Uri[] uriArr) throws RemoteException;

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
