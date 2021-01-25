package android.media;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IRecordingConfigDispatcher extends IInterface {
    void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> list) throws RemoteException;

    public static class Default implements IRecordingConfigDispatcher {
        @Override // android.media.IRecordingConfigDispatcher
        public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRecordingConfigDispatcher {
        private static final String DESCRIPTOR = "android.media.IRecordingConfigDispatcher";
        static final int TRANSACTION_dispatchRecordingConfigChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRecordingConfigDispatcher asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRecordingConfigDispatcher)) {
                return new Proxy(obj);
            }
            return (IRecordingConfigDispatcher) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "dispatchRecordingConfigChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                dispatchRecordingConfigChange(data.createTypedArrayList(AudioRecordingConfiguration.CREATOR));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRecordingConfigDispatcher {
            public static IRecordingConfigDispatcher sDefaultImpl;
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

            @Override // android.media.IRecordingConfigDispatcher
            public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> configs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(configs);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchRecordingConfigChange(configs);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRecordingConfigDispatcher impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRecordingConfigDispatcher getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
