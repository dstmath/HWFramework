package com.huawei.ncdft;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface INcDft extends IInterface {
    void connectivityDftReport(String str, int i, String str2) throws RemoteException;

    String getConnectivityDftParm(String str, int i) throws RemoteException;

    String getNcDftParam(int i, List<String> list) throws RemoteException;

    int notifyNcDftBundleEvent(int i, int i2, Bundle bundle) throws RemoteException;

    int notifyNcDftEvent(int i, int i2, List<String> list) throws RemoteException;

    public static class Default implements INcDft {
        @Override // com.huawei.ncdft.INcDft
        public int notifyNcDftEvent(int domain, int eventId, List<String> list) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.ncdft.INcDft
        public int notifyNcDftBundleEvent(int domain, int eventId, Bundle data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.ncdft.INcDft
        public String getNcDftParam(int domain, List<String> list) throws RemoteException {
            return null;
        }

        @Override // com.huawei.ncdft.INcDft
        public void connectivityDftReport(String scenario, int eventId, String eventMsg) throws RemoteException {
        }

        @Override // com.huawei.ncdft.INcDft
        public String getConnectivityDftParm(String scenario, int eventId) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INcDft {
        private static final String DESCRIPTOR = "com.huawei.ncdft.INcDft";
        static final int TRANSACTION_connectivityDftReport = 4;
        static final int TRANSACTION_getConnectivityDftParm = 5;
        static final int TRANSACTION_getNcDftParam = 3;
        static final int TRANSACTION_notifyNcDftBundleEvent = 2;
        static final int TRANSACTION_notifyNcDftEvent = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INcDft asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INcDft)) {
                return new Proxy(obj);
            }
            return (INcDft) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = notifyNcDftEvent(data.readInt(), data.readInt(), data.createStringArrayList());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                int _result2 = notifyNcDftBundleEvent(_arg0, _arg1, _arg2);
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _result3 = getNcDftParam(data.readInt(), data.createStringArrayList());
                reply.writeNoException();
                reply.writeString(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                connectivityDftReport(data.readString(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                String _result4 = getConnectivityDftParm(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeString(_result4);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INcDft {
            public static INcDft sDefaultImpl;
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

            @Override // com.huawei.ncdft.INcDft
            public int notifyNcDftEvent(int domain, int eventId, List<String> data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(eventId);
                    _data.writeStringList(data);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyNcDftEvent(domain, eventId, data);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
            public int notifyNcDftBundleEvent(int domain, int eventId, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(eventId);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyNcDftBundleEvent(domain, eventId, data);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
            public String getNcDftParam(int domain, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeStringList(list);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNcDftParam(domain, list);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
            public void connectivityDftReport(String scenario, int eventId, String eventMsg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(scenario);
                    _data.writeInt(eventId);
                    _data.writeString(eventMsg);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().connectivityDftReport(scenario, eventId, eventMsg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
            public String getConnectivityDftParm(String scenario, int eventId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(scenario);
                    _data.writeInt(eventId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConnectivityDftParm(scenario, eventId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INcDft impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INcDft getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
