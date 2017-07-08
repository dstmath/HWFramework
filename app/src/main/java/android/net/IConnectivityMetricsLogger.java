package android.net;

import android.app.PendingIntent;
import android.net.ConnectivityMetricsEvent.Reference;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IConnectivityMetricsLogger extends IInterface {

    public static abstract class Stub extends Binder implements IConnectivityMetricsLogger {
        private static final String DESCRIPTOR = "android.net.IConnectivityMetricsLogger";
        static final int TRANSACTION_getEvents = 3;
        static final int TRANSACTION_logEvent = 1;
        static final int TRANSACTION_logEvents = 2;
        static final int TRANSACTION_register = 4;
        static final int TRANSACTION_unregister = 5;

        private static class Proxy implements IConnectivityMetricsLogger {
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

            public long logEvent(ConnectivityMetricsEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(Stub.TRANSACTION_logEvent);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_logEvent, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long logEvents(ConnectivityMetricsEvent[] events) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(events, 0);
                    this.mRemote.transact(Stub.TRANSACTION_logEvents, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ConnectivityMetricsEvent[] getEvents(Reference reference) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reference != null) {
                        _data.writeInt(Stub.TRANSACTION_logEvent);
                        reference.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getEvents, _data, _reply, 0);
                    _reply.readException();
                    ConnectivityMetricsEvent[] _result = (ConnectivityMetricsEvent[]) _reply.createTypedArray(ConnectivityMetricsEvent.CREATOR);
                    if (_reply.readInt() != 0) {
                        reference.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean register(PendingIntent newEventsIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newEventsIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_logEvent);
                        newEventsIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_register, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregister(PendingIntent newEventsIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newEventsIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_logEvent);
                        newEventsIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_unregister, _data, _reply, 0);
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

        public static IConnectivityMetricsLogger asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectivityMetricsLogger)) {
                return new Proxy(obj);
            }
            return (IConnectivityMetricsLogger) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            long _result;
            PendingIntent pendingIntent;
            switch (code) {
                case TRANSACTION_logEvent /*1*/:
                    ConnectivityMetricsEvent connectivityMetricsEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        connectivityMetricsEvent = (ConnectivityMetricsEvent) ConnectivityMetricsEvent.CREATOR.createFromParcel(data);
                    } else {
                        connectivityMetricsEvent = null;
                    }
                    _result = logEvent(connectivityMetricsEvent);
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_logEvents /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = logEvents((ConnectivityMetricsEvent[]) data.createTypedArray(ConnectivityMetricsEvent.CREATOR));
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_getEvents /*3*/:
                    Reference reference;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        reference = (Reference) Reference.CREATOR.createFromParcel(data);
                    } else {
                        reference = null;
                    }
                    ConnectivityMetricsEvent[] _result2 = getEvents(reference);
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_logEvent);
                    if (reference != null) {
                        reply.writeInt(TRANSACTION_logEvent);
                        reference.writeToParcel(reply, TRANSACTION_logEvent);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_register /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    boolean _result3 = register(pendingIntent);
                    reply.writeNoException();
                    if (_result3) {
                        i = TRANSACTION_logEvent;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_unregister /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    unregister(pendingIntent);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    ConnectivityMetricsEvent[] getEvents(Reference reference) throws RemoteException;

    long logEvent(ConnectivityMetricsEvent connectivityMetricsEvent) throws RemoteException;

    long logEvents(ConnectivityMetricsEvent[] connectivityMetricsEventArr) throws RemoteException;

    boolean register(PendingIntent pendingIntent) throws RemoteException;

    void unregister(PendingIntent pendingIntent) throws RemoteException;
}
