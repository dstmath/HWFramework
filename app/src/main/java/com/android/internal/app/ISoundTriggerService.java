package com.android.internal.app;

import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger.GenericSoundModel;
import android.hardware.soundtrigger.SoundTrigger.RecognitionConfig;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.RemoteException;

public interface ISoundTriggerService extends IInterface {

    public static abstract class Stub extends Binder implements ISoundTriggerService {
        private static final String DESCRIPTOR = "com.android.internal.app.ISoundTriggerService";
        static final int TRANSACTION_deleteSoundModel = 3;
        static final int TRANSACTION_getSoundModel = 1;
        static final int TRANSACTION_startRecognition = 4;
        static final int TRANSACTION_stopRecognition = 5;
        static final int TRANSACTION_updateSoundModel = 2;

        private static class Proxy implements ISoundTriggerService {
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

            public GenericSoundModel getSoundModel(ParcelUuid soundModelId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    GenericSoundModel genericSoundModel;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (soundModelId != null) {
                        _data.writeInt(Stub.TRANSACTION_getSoundModel);
                        soundModelId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getSoundModel, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        genericSoundModel = (GenericSoundModel) GenericSoundModel.CREATOR.createFromParcel(_reply);
                    } else {
                        genericSoundModel = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return genericSoundModel;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateSoundModel(GenericSoundModel soundModel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (soundModel != null) {
                        _data.writeInt(Stub.TRANSACTION_getSoundModel);
                        soundModel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateSoundModel, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteSoundModel(ParcelUuid soundModelId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (soundModelId != null) {
                        _data.writeInt(Stub.TRANSACTION_getSoundModel);
                        soundModelId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_deleteSoundModel, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startRecognition(ParcelUuid soundModelId, IRecognitionStatusCallback callback, RecognitionConfig config) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (soundModelId != null) {
                        _data.writeInt(Stub.TRANSACTION_getSoundModel);
                        soundModelId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (config != null) {
                        _data.writeInt(Stub.TRANSACTION_getSoundModel);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startRecognition, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stopRecognition(ParcelUuid soundModelId, IRecognitionStatusCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (soundModelId != null) {
                        _data.writeInt(Stub.TRANSACTION_getSoundModel);
                        soundModelId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_stopRecognition, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISoundTriggerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISoundTriggerService)) {
                return new Proxy(obj);
            }
            return (ISoundTriggerService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelUuid parcelUuid;
            int _result;
            switch (code) {
                case TRANSACTION_getSoundModel /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelUuid = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid = null;
                    }
                    GenericSoundModel _result2 = getSoundModel(parcelUuid);
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getSoundModel);
                        _result2.writeToParcel(reply, TRANSACTION_getSoundModel);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_updateSoundModel /*2*/:
                    GenericSoundModel genericSoundModel;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        genericSoundModel = (GenericSoundModel) GenericSoundModel.CREATOR.createFromParcel(data);
                    } else {
                        genericSoundModel = null;
                    }
                    updateSoundModel(genericSoundModel);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deleteSoundModel /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelUuid = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid = null;
                    }
                    deleteSoundModel(parcelUuid);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startRecognition /*4*/:
                    RecognitionConfig recognitionConfig;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelUuid = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid = null;
                    }
                    IRecognitionStatusCallback _arg1 = android.hardware.soundtrigger.IRecognitionStatusCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        recognitionConfig = (RecognitionConfig) RecognitionConfig.CREATOR.createFromParcel(data);
                    } else {
                        recognitionConfig = null;
                    }
                    _result = startRecognition(parcelUuid, _arg1, recognitionConfig);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_stopRecognition /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelUuid = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid = null;
                    }
                    _result = stopRecognition(parcelUuid, android.hardware.soundtrigger.IRecognitionStatusCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void deleteSoundModel(ParcelUuid parcelUuid) throws RemoteException;

    GenericSoundModel getSoundModel(ParcelUuid parcelUuid) throws RemoteException;

    int startRecognition(ParcelUuid parcelUuid, IRecognitionStatusCallback iRecognitionStatusCallback, RecognitionConfig recognitionConfig) throws RemoteException;

    int stopRecognition(ParcelUuid parcelUuid, IRecognitionStatusCallback iRecognitionStatusCallback) throws RemoteException;

    void updateSoundModel(GenericSoundModel genericSoundModel) throws RemoteException;
}
