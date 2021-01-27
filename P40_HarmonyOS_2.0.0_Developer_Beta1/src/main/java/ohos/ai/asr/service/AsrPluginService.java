package ohos.ai.asr.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Optional;
import ohos.ai.asr.service.AsrPluginListener;

public interface AsrPluginService extends IInterface {
    void cancel(AsrPluginListener asrPluginListener) throws RemoteException;

    boolean checkServerVersion(int i) throws RemoteException;

    void destroy(AsrPluginListener asrPluginListener) throws RemoteException;

    void init(Intent intent, AsrPluginListener asrPluginListener) throws RemoteException;

    void startListening(Intent intent, AsrPluginListener asrPluginListener) throws RemoteException;

    void stopListening(AsrPluginListener asrPluginListener) throws RemoteException;

    void updateLexicon(Intent intent, AsrPluginListener asrPluginListener) throws RemoteException;

    void writePcm(byte[] bArr, int i, AsrPluginListener asrPluginListener) throws RemoteException;

    public static abstract class Stub extends Binder implements AsrPluginService {
        private static final String DESCRIPTOR = "com.huawei.hiai.asr.IAsrService";
        static final int TRANSACTION_CANCEL = 6;
        static final int TRANSACTION_CHECK_SERVER_VERSION = 8;
        static final int TRANSACTION_DESTROY = 7;
        static final int TRANSACTION_INIT = 1;
        static final int TRANSACTION_START_LISTENING = 2;
        static final int TRANSACTION_STOP_LISTENING = 5;
        static final int TRANSACTION_UPDATE_LEXICON = 4;
        static final int TRANSACTION_WRITE_PCM = 3;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static Optional<AsrPluginService> asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return Optional.empty();
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface instanceof AsrPluginService) {
                return Optional.of((AsrPluginService) queryLocalInterface);
            }
            return Optional.of(new Proxy(iBinder));
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        init(parcel.readInt() != 0 ? (Intent) Intent.CREATOR.createFromParcel(parcel) : null, AsrPluginListener.Stub.asInterface(parcel.readStrongBinder()).orElse(null));
                        parcel2.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        startListening(parcel.readInt() != 0 ? (Intent) Intent.CREATOR.createFromParcel(parcel) : null, AsrPluginListener.Stub.asInterface(parcel.readStrongBinder()).orElse(null));
                        parcel2.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        writePcm(parcel.createByteArray(), parcel.readInt(), AsrPluginListener.Stub.asInterface(parcel.readStrongBinder()).orElse(null));
                        parcel2.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateLexicon(parcel.readInt() != 0 ? (Intent) Intent.CREATOR.createFromParcel(parcel) : null, AsrPluginListener.Stub.asInterface(parcel.readStrongBinder()).orElse(null));
                        parcel2.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopListening(AsrPluginListener.Stub.asInterface(parcel.readStrongBinder()).orElse(null));
                        parcel2.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancel(AsrPluginListener.Stub.asInterface(parcel.readStrongBinder()).orElse(null));
                        parcel2.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        destroy(AsrPluginListener.Stub.asInterface(parcel.readStrongBinder()).orElse(null));
                        parcel2.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean checkServerVersion = checkServerVersion(parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(checkServerVersion ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements AsrPluginService {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // ohos.ai.asr.service.AsrPluginService
            public void init(Intent intent, AsrPluginListener asrPluginListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        obtain.writeInt(1);
                        intent.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(asrPluginListener != null ? asrPluginListener.asBinder() : null);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.ai.asr.service.AsrPluginService
            public void startListening(Intent intent, AsrPluginListener asrPluginListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        obtain.writeInt(1);
                        intent.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(asrPluginListener != null ? asrPluginListener.asBinder() : null);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.ai.asr.service.AsrPluginService
            public void writePcm(byte[] bArr, int i, AsrPluginListener asrPluginListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeByteArray(bArr);
                    obtain.writeInt(i);
                    obtain.writeStrongBinder(asrPluginListener != null ? asrPluginListener.asBinder() : null);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.ai.asr.service.AsrPluginService
            public void updateLexicon(Intent intent, AsrPluginListener asrPluginListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        obtain.writeInt(1);
                        intent.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(asrPluginListener != null ? asrPluginListener.asBinder() : null);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.ai.asr.service.AsrPluginService
            public void stopListening(AsrPluginListener asrPluginListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(asrPluginListener != null ? asrPluginListener.asBinder() : null);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.ai.asr.service.AsrPluginService
            public void cancel(AsrPluginListener asrPluginListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(asrPluginListener != null ? asrPluginListener.asBinder() : null);
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.ai.asr.service.AsrPluginService
            public void destroy(AsrPluginListener asrPluginListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(asrPluginListener != null ? asrPluginListener.asBinder() : null);
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.ai.asr.service.AsrPluginService
            public boolean checkServerVersion(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    boolean z = false;
                    this.mRemote.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
