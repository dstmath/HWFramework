package android.app;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IInstrumentationWatcher extends IInterface {
    void instrumentationFinished(ComponentName componentName, int i, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    void instrumentationStatus(ComponentName componentName, int i, Bundle bundle) throws RemoteException;

    public static class Default implements IInstrumentationWatcher {
        @Override // android.app.IInstrumentationWatcher
        public void instrumentationStatus(ComponentName name, int resultCode, Bundle results) throws RemoteException {
        }

        @Override // android.app.IInstrumentationWatcher
        public void instrumentationFinished(ComponentName name, int resultCode, Bundle results) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IInstrumentationWatcher {
        private static final String DESCRIPTOR = "android.app.IInstrumentationWatcher";
        static final int TRANSACTION_instrumentationFinished = 2;
        static final int TRANSACTION_instrumentationStatus = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInstrumentationWatcher asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInstrumentationWatcher)) {
                return new Proxy(obj);
            }
            return (IInstrumentationWatcher) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "instrumentationStatus";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "instrumentationFinished";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            Bundle _arg2;
            ComponentName _arg02;
            Bundle _arg22;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                instrumentationStatus(_arg0, _arg1, _arg2);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                int _arg12 = data.readInt();
                if (data.readInt() != 0) {
                    _arg22 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg22 = null;
                }
                instrumentationFinished(_arg02, _arg12, _arg22);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IInstrumentationWatcher {
            public static IInstrumentationWatcher sDefaultImpl;
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

            @Override // android.app.IInstrumentationWatcher
            public void instrumentationStatus(ComponentName name, int resultCode, Bundle results) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (name != null) {
                        _data.writeInt(1);
                        name.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    if (results != null) {
                        _data.writeInt(1);
                        results.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().instrumentationStatus(name, resultCode, results);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IInstrumentationWatcher
            public void instrumentationFinished(ComponentName name, int resultCode, Bundle results) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (name != null) {
                        _data.writeInt(1);
                        name.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    if (results != null) {
                        _data.writeInt(1);
                        results.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().instrumentationFinished(name, resultCode, results);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IInstrumentationWatcher impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IInstrumentationWatcher getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
