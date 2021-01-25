package android.media.soundtrigger;

import android.hardware.soundtrigger.SoundTrigger;
import android.media.soundtrigger.ISoundTriggerDetectionServiceClient;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.RemoteException;

public interface ISoundTriggerDetectionService extends IInterface {
    void onError(ParcelUuid parcelUuid, int i, int i2) throws RemoteException;

    void onGenericRecognitionEvent(ParcelUuid parcelUuid, int i, SoundTrigger.GenericRecognitionEvent genericRecognitionEvent) throws RemoteException;

    void onStopOperation(ParcelUuid parcelUuid, int i) throws RemoteException;

    void removeClient(ParcelUuid parcelUuid) throws RemoteException;

    void setClient(ParcelUuid parcelUuid, Bundle bundle, ISoundTriggerDetectionServiceClient iSoundTriggerDetectionServiceClient) throws RemoteException;

    public static class Default implements ISoundTriggerDetectionService {
        @Override // android.media.soundtrigger.ISoundTriggerDetectionService
        public void setClient(ParcelUuid uuid, Bundle params, ISoundTriggerDetectionServiceClient client) throws RemoteException {
        }

        @Override // android.media.soundtrigger.ISoundTriggerDetectionService
        public void removeClient(ParcelUuid uuid) throws RemoteException {
        }

        @Override // android.media.soundtrigger.ISoundTriggerDetectionService
        public void onGenericRecognitionEvent(ParcelUuid uuid, int opId, SoundTrigger.GenericRecognitionEvent event) throws RemoteException {
        }

        @Override // android.media.soundtrigger.ISoundTriggerDetectionService
        public void onError(ParcelUuid uuid, int opId, int status) throws RemoteException {
        }

        @Override // android.media.soundtrigger.ISoundTriggerDetectionService
        public void onStopOperation(ParcelUuid uuid, int opId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISoundTriggerDetectionService {
        private static final String DESCRIPTOR = "android.media.soundtrigger.ISoundTriggerDetectionService";
        static final int TRANSACTION_onError = 4;
        static final int TRANSACTION_onGenericRecognitionEvent = 3;
        static final int TRANSACTION_onStopOperation = 5;
        static final int TRANSACTION_removeClient = 2;
        static final int TRANSACTION_setClient = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISoundTriggerDetectionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISoundTriggerDetectionService)) {
                return new Proxy(obj);
            }
            return (ISoundTriggerDetectionService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "setClient";
            }
            if (transactionCode == 2) {
                return "removeClient";
            }
            if (transactionCode == 3) {
                return "onGenericRecognitionEvent";
            }
            if (transactionCode == 4) {
                return "onError";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onStopOperation";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelUuid _arg0;
            Bundle _arg1;
            ParcelUuid _arg02;
            ParcelUuid _arg03;
            SoundTrigger.GenericRecognitionEvent _arg2;
            ParcelUuid _arg04;
            ParcelUuid _arg05;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ParcelUuid.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                setClient(_arg0, _arg1, ISoundTriggerDetectionServiceClient.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = ParcelUuid.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                removeClient(_arg02);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = ParcelUuid.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                int _arg12 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = SoundTrigger.GenericRecognitionEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                onGenericRecognitionEvent(_arg03, _arg12, _arg2);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg04 = ParcelUuid.CREATOR.createFromParcel(data);
                } else {
                    _arg04 = null;
                }
                onError(_arg04, data.readInt(), data.readInt());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg05 = ParcelUuid.CREATOR.createFromParcel(data);
                } else {
                    _arg05 = null;
                }
                onStopOperation(_arg05, data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISoundTriggerDetectionService {
            public static ISoundTriggerDetectionService sDefaultImpl;
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

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void setClient(ParcelUuid uuid, Bundle params, ISoundTriggerDetectionServiceClient client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uuid != null) {
                        _data.writeInt(1);
                        uuid.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setClient(uuid, params, client);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void removeClient(ParcelUuid uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uuid != null) {
                        _data.writeInt(1);
                        uuid.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeClient(uuid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void onGenericRecognitionEvent(ParcelUuid uuid, int opId, SoundTrigger.GenericRecognitionEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uuid != null) {
                        _data.writeInt(1);
                        uuid.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(opId);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGenericRecognitionEvent(uuid, opId, event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void onError(ParcelUuid uuid, int opId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uuid != null) {
                        _data.writeInt(1);
                        uuid.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(opId);
                    _data.writeInt(status);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError(uuid, opId, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void onStopOperation(ParcelUuid uuid, int opId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uuid != null) {
                        _data.writeInt(1);
                        uuid.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(opId);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStopOperation(uuid, opId);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISoundTriggerDetectionService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISoundTriggerDetectionService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
