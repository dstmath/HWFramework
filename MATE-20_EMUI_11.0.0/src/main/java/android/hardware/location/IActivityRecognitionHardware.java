package android.hardware.location;

import android.hardware.location.IActivityRecognitionHardwareSink;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IActivityRecognitionHardware extends IInterface {
    boolean disableActivityEvent(String str, int i) throws RemoteException;

    boolean enableActivityEvent(String str, int i, long j) throws RemoteException;

    boolean flush() throws RemoteException;

    String[] getSupportedActivities() throws RemoteException;

    boolean isActivitySupported(String str) throws RemoteException;

    boolean registerSink(IActivityRecognitionHardwareSink iActivityRecognitionHardwareSink) throws RemoteException;

    boolean unregisterSink(IActivityRecognitionHardwareSink iActivityRecognitionHardwareSink) throws RemoteException;

    public static class Default implements IActivityRecognitionHardware {
        @Override // android.hardware.location.IActivityRecognitionHardware
        public String[] getSupportedActivities() throws RemoteException {
            return null;
        }

        @Override // android.hardware.location.IActivityRecognitionHardware
        public boolean isActivitySupported(String activityType) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IActivityRecognitionHardware
        public boolean registerSink(IActivityRecognitionHardwareSink sink) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IActivityRecognitionHardware
        public boolean unregisterSink(IActivityRecognitionHardwareSink sink) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IActivityRecognitionHardware
        public boolean enableActivityEvent(String activityType, int eventType, long reportLatencyNs) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IActivityRecognitionHardware
        public boolean disableActivityEvent(String activityType, int eventType) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IActivityRecognitionHardware
        public boolean flush() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IActivityRecognitionHardware {
        private static final String DESCRIPTOR = "android.hardware.location.IActivityRecognitionHardware";
        static final int TRANSACTION_disableActivityEvent = 6;
        static final int TRANSACTION_enableActivityEvent = 5;
        static final int TRANSACTION_flush = 7;
        static final int TRANSACTION_getSupportedActivities = 1;
        static final int TRANSACTION_isActivitySupported = 2;
        static final int TRANSACTION_registerSink = 3;
        static final int TRANSACTION_unregisterSink = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IActivityRecognitionHardware asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IActivityRecognitionHardware)) {
                return new Proxy(obj);
            }
            return (IActivityRecognitionHardware) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getSupportedActivities";
                case 2:
                    return "isActivitySupported";
                case 3:
                    return "registerSink";
                case 4:
                    return "unregisterSink";
                case 5:
                    return "enableActivityEvent";
                case 6:
                    return "disableActivityEvent";
                case 7:
                    return "flush";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result = getSupportedActivities();
                        reply.writeNoException();
                        reply.writeStringArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isActivitySupported = isActivitySupported(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isActivitySupported ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerSink = registerSink(IActivityRecognitionHardwareSink.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerSink ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterSink = unregisterSink(IActivityRecognitionHardwareSink.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterSink ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableActivityEvent = enableActivityEvent(data.readString(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(enableActivityEvent ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableActivityEvent = disableActivityEvent(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disableActivityEvent ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean flush = flush();
                        reply.writeNoException();
                        reply.writeInt(flush ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IActivityRecognitionHardware {
            public static IActivityRecognitionHardware sDefaultImpl;
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

            @Override // android.hardware.location.IActivityRecognitionHardware
            public String[] getSupportedActivities() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportedActivities();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IActivityRecognitionHardware
            public boolean isActivitySupported(String activityType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activityType);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isActivitySupported(activityType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IActivityRecognitionHardware
            public boolean registerSink(IActivityRecognitionHardwareSink sink) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerSink(sink);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IActivityRecognitionHardware
            public boolean unregisterSink(IActivityRecognitionHardwareSink sink) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sink != null ? sink.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterSink(sink);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IActivityRecognitionHardware
            public boolean enableActivityEvent(String activityType, int eventType, long reportLatencyNs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activityType);
                    _data.writeInt(eventType);
                    _data.writeLong(reportLatencyNs);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableActivityEvent(activityType, eventType, reportLatencyNs);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IActivityRecognitionHardware
            public boolean disableActivityEvent(String activityType, int eventType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activityType);
                    _data.writeInt(eventType);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableActivityEvent(activityType, eventType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IActivityRecognitionHardware
            public boolean flush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().flush();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IActivityRecognitionHardware impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IActivityRecognitionHardware getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
