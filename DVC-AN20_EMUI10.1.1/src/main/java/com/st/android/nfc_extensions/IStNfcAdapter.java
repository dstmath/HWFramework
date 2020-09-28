package com.st.android.nfc_extensions;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface IStNfcAdapter extends IInterface {
    void DefaultRouteSet(int i) throws RemoteException;

    int getCommittedAidRoutingTableSize() throws RemoteException;

    int getMaxAidRoutingTableSize() throws RemoteException;

    int getRoutingTableSizeFull(int i) throws RemoteException;

    int getRoutingTableSizeNotFull() throws RemoteException;

    int getRoutingTableSizeNotFullAlt() throws RemoteException;

    Map getServicesAidCacheSize(int i, String str) throws RemoteException;

    List<StApduServiceInfo> getStServices(int i, String str) throws RemoteException;

    boolean isOnHostDefaultRoute() throws RemoteException;

    boolean isRoutingTableOverflow() throws RemoteException;

    int updateServiceState(int i, Map map) throws RemoteException;

    public static class Default implements IStNfcAdapter {
        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public List<StApduServiceInfo> getStServices(int userHandle, String category) throws RemoteException {
            return null;
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public void DefaultRouteSet(int routeLoc) throws RemoteException {
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public Map getServicesAidCacheSize(int userId, String category) throws RemoteException {
            return null;
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public int getMaxAidRoutingTableSize() throws RemoteException {
            return 0;
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public int getCommittedAidRoutingTableSize() throws RemoteException {
            return 0;
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public int updateServiceState(int userId, Map serviceState) throws RemoteException {
            return 0;
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public boolean isRoutingTableOverflow() throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public boolean isOnHostDefaultRoute() throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public int getRoutingTableSizeFull(int route) throws RemoteException {
            return 0;
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public int getRoutingTableSizeNotFull() throws RemoteException {
            return 0;
        }

        @Override // com.st.android.nfc_extensions.IStNfcAdapter
        public int getRoutingTableSizeNotFullAlt() throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IStNfcAdapter {
        private static final String DESCRIPTOR = "com.st.android.nfc_extensions.IStNfcAdapter";
        static final int TRANSACTION_DefaultRouteSet = 2;
        static final int TRANSACTION_getCommittedAidRoutingTableSize = 5;
        static final int TRANSACTION_getMaxAidRoutingTableSize = 4;
        static final int TRANSACTION_getRoutingTableSizeFull = 9;
        static final int TRANSACTION_getRoutingTableSizeNotFull = 10;
        static final int TRANSACTION_getRoutingTableSizeNotFullAlt = 11;
        static final int TRANSACTION_getServicesAidCacheSize = 3;
        static final int TRANSACTION_getStServices = 1;
        static final int TRANSACTION_isOnHostDefaultRoute = 8;
        static final int TRANSACTION_isRoutingTableOverflow = 7;
        static final int TRANSACTION_updateServiceState = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStNfcAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStNfcAdapter)) {
                return new Proxy(obj);
            }
            return (IStNfcAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        List<StApduServiceInfo> _result = getStServices(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        DefaultRouteSet(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result2 = getServicesAidCacheSize(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeMap(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getMaxAidRoutingTableSize();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getCommittedAidRoutingTableSize();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case TRANSACTION_updateServiceState /*{ENCODED_INT: 6}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = updateServiceState(data.readInt(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case TRANSACTION_isRoutingTableOverflow /*{ENCODED_INT: 7}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRoutingTableOverflow = isRoutingTableOverflow();
                        reply.writeNoException();
                        reply.writeInt(isRoutingTableOverflow ? 1 : 0);
                        return true;
                    case TRANSACTION_isOnHostDefaultRoute /*{ENCODED_INT: 8}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isOnHostDefaultRoute = isOnHostDefaultRoute();
                        reply.writeNoException();
                        reply.writeInt(isOnHostDefaultRoute ? 1 : 0);
                        return true;
                    case TRANSACTION_getRoutingTableSizeFull /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getRoutingTableSizeFull(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case TRANSACTION_getRoutingTableSizeNotFull /*{ENCODED_INT: 10}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getRoutingTableSizeNotFull();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case TRANSACTION_getRoutingTableSizeNotFullAlt /*{ENCODED_INT: 11}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getRoutingTableSizeNotFullAlt();
                        reply.writeNoException();
                        reply.writeInt(_result8);
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
        public static class Proxy implements IStNfcAdapter {
            public static IStNfcAdapter sDefaultImpl;
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

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public List<StApduServiceInfo> getStServices(int userHandle, String category) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeString(category);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStServices(userHandle, category);
                    }
                    _reply.readException();
                    List<StApduServiceInfo> _result = _reply.createTypedArrayList(StApduServiceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public void DefaultRouteSet(int routeLoc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().DefaultRouteSet(routeLoc);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public Map getServicesAidCacheSize(int userId, String category) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(category);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServicesAidCacheSize(userId, category);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public int getMaxAidRoutingTableSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxAidRoutingTableSize();
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

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public int getCommittedAidRoutingTableSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCommittedAidRoutingTableSize();
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

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public int updateServiceState(int userId, Map serviceState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeMap(serviceState);
                    if (!this.mRemote.transact(Stub.TRANSACTION_updateServiceState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateServiceState(userId, serviceState);
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

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public boolean isRoutingTableOverflow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isRoutingTableOverflow, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRoutingTableOverflow();
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

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public boolean isOnHostDefaultRoute() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isOnHostDefaultRoute, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isOnHostDefaultRoute();
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

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public int getRoutingTableSizeFull(int route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(route);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getRoutingTableSizeFull, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRoutingTableSizeFull(route);
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

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public int getRoutingTableSizeNotFull() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getRoutingTableSizeNotFull, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRoutingTableSizeNotFull();
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

            @Override // com.st.android.nfc_extensions.IStNfcAdapter
            public int getRoutingTableSizeNotFullAlt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getRoutingTableSizeNotFullAlt, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRoutingTableSizeNotFullAlt();
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
        }

        public static boolean setDefaultImpl(IStNfcAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IStNfcAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
