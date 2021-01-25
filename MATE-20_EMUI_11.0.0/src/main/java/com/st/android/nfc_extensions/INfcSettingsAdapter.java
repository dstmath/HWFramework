package com.st.android.nfc_extensions;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.st.android.nfc_extensions.INfcSettingsCallback;
import java.util.List;

public interface INfcSettingsAdapter extends IInterface {
    void DefaultRouteSet(int i) throws RemoteException;

    boolean EnableSE(String str, boolean z) throws RemoteException;

    void commitNonAidBasedServiceEntryList(List<ServiceEntry> list) throws RemoteException;

    void commitServiceEntryList(List<ServiceEntry> list) throws RemoteException;

    int getModeFlag(int i) throws RemoteException;

    List<ServiceEntry> getNonAidBasedServiceEntryList(int i) throws RemoteException;

    List<String> getSecureElementsStatus() throws RemoteException;

    List<ServiceEntry> getServiceEntryList(int i) throws RemoteException;

    boolean isRoutingTableOverflow() throws RemoteException;

    boolean isSEConnected(int i) throws RemoteException;

    boolean isShowOverflowMenu() throws RemoteException;

    boolean isUiccConnected() throws RemoteException;

    boolean iseSEConnected() throws RemoteException;

    void registerNfcSettingsCallback(INfcSettingsCallback iNfcSettingsCallback) throws RemoteException;

    void setModeFlag(int i, int i2) throws RemoteException;

    boolean testServiceEntryList(List<ServiceEntry> list) throws RemoteException;

    void unregisterNfcSettingsCallback(INfcSettingsCallback iNfcSettingsCallback) throws RemoteException;

    public static class Default implements INfcSettingsAdapter {
        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public int getModeFlag(int mode) throws RemoteException {
            return 0;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public void setModeFlag(int mode, int flag) throws RemoteException {
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public boolean isUiccConnected() throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public boolean iseSEConnected() throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public boolean isSEConnected(int HostID) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public List<String> getSecureElementsStatus() throws RemoteException {
            return null;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public boolean EnableSE(String se_id, boolean enable) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public void registerNfcSettingsCallback(INfcSettingsCallback cb) throws RemoteException {
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public void unregisterNfcSettingsCallback(INfcSettingsCallback cb) throws RemoteException {
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public boolean isRoutingTableOverflow() throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public boolean isShowOverflowMenu() throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public List<ServiceEntry> getServiceEntryList(int userHandle) throws RemoteException {
            return null;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public boolean testServiceEntryList(List<ServiceEntry> list) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public void commitServiceEntryList(List<ServiceEntry> list) throws RemoteException {
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public List<ServiceEntry> getNonAidBasedServiceEntryList(int userHandle) throws RemoteException {
            return null;
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public void commitNonAidBasedServiceEntryList(List<ServiceEntry> list) throws RemoteException {
        }

        @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
        public void DefaultRouteSet(int routeLoc) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcSettingsAdapter {
        private static final String DESCRIPTOR = "com.st.android.nfc_extensions.INfcSettingsAdapter";
        static final int TRANSACTION_DefaultRouteSet = 17;
        static final int TRANSACTION_EnableSE = 7;
        static final int TRANSACTION_commitNonAidBasedServiceEntryList = 16;
        static final int TRANSACTION_commitServiceEntryList = 14;
        static final int TRANSACTION_getModeFlag = 1;
        static final int TRANSACTION_getNonAidBasedServiceEntryList = 15;
        static final int TRANSACTION_getSecureElementsStatus = 6;
        static final int TRANSACTION_getServiceEntryList = 12;
        static final int TRANSACTION_isRoutingTableOverflow = 10;
        static final int TRANSACTION_isSEConnected = 5;
        static final int TRANSACTION_isShowOverflowMenu = 11;
        static final int TRANSACTION_isUiccConnected = 3;
        static final int TRANSACTION_iseSEConnected = 4;
        static final int TRANSACTION_registerNfcSettingsCallback = 8;
        static final int TRANSACTION_setModeFlag = 2;
        static final int TRANSACTION_testServiceEntryList = 13;
        static final int TRANSACTION_unregisterNfcSettingsCallback = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcSettingsAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcSettingsAdapter)) {
                return new Proxy(obj);
            }
            return (INfcSettingsAdapter) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getModeFlag(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setModeFlag(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUiccConnected = isUiccConnected();
                        reply.writeNoException();
                        reply.writeInt(isUiccConnected ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean iseSEConnected = iseSEConnected();
                        reply.writeNoException();
                        reply.writeInt(iseSEConnected ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSEConnected = isSEConnected(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isSEConnected ? 1 : 0);
                        return true;
                    case TRANSACTION_getSecureElementsStatus /* 6 */:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getSecureElementsStatus();
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case TRANSACTION_EnableSE /* 7 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean EnableSE = EnableSE(data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(EnableSE ? 1 : 0);
                        return true;
                    case TRANSACTION_registerNfcSettingsCallback /* 8 */:
                        data.enforceInterface(DESCRIPTOR);
                        registerNfcSettingsCallback(INfcSettingsCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_unregisterNfcSettingsCallback /* 9 */:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterNfcSettingsCallback(INfcSettingsCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isRoutingTableOverflow /* 10 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRoutingTableOverflow = isRoutingTableOverflow();
                        reply.writeNoException();
                        reply.writeInt(isRoutingTableOverflow ? 1 : 0);
                        return true;
                    case TRANSACTION_isShowOverflowMenu /* 11 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isShowOverflowMenu = isShowOverflowMenu();
                        reply.writeNoException();
                        reply.writeInt(isShowOverflowMenu ? 1 : 0);
                        return true;
                    case TRANSACTION_getServiceEntryList /* 12 */:
                        data.enforceInterface(DESCRIPTOR);
                        List<ServiceEntry> _result3 = getServiceEntryList(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result3);
                        return true;
                    case TRANSACTION_testServiceEntryList /* 13 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean testServiceEntryList = testServiceEntryList(data.createTypedArrayList(ServiceEntry.CREATOR));
                        reply.writeNoException();
                        reply.writeInt(testServiceEntryList ? 1 : 0);
                        return true;
                    case TRANSACTION_commitServiceEntryList /* 14 */:
                        data.enforceInterface(DESCRIPTOR);
                        commitServiceEntryList(data.createTypedArrayList(ServiceEntry.CREATOR));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getNonAidBasedServiceEntryList /* 15 */:
                        data.enforceInterface(DESCRIPTOR);
                        List<ServiceEntry> _result4 = getNonAidBasedServiceEntryList(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result4);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        commitNonAidBasedServiceEntryList(data.createTypedArrayList(ServiceEntry.CREATOR));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_DefaultRouteSet /* 17 */:
                        data.enforceInterface(DESCRIPTOR);
                        DefaultRouteSet(data.readInt());
                        reply.writeNoException();
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
        public static class Proxy implements INfcSettingsAdapter {
            public static INfcSettingsAdapter sDefaultImpl;
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

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public int getModeFlag(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModeFlag(mode);
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

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public void setModeFlag(int mode, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeInt(flag);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setModeFlag(mode, flag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public boolean isUiccConnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUiccConnected();
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

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public boolean iseSEConnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().iseSEConnected();
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

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public boolean isSEConnected(int HostID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(HostID);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSEConnected(HostID);
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

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public List<String> getSecureElementsStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSecureElementsStatus, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecureElementsStatus();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public boolean EnableSE(String se_id, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(se_id);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_EnableSE, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().EnableSE(se_id, enable);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public void registerNfcSettingsCallback(INfcSettingsCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_registerNfcSettingsCallback, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerNfcSettingsCallback(cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public void unregisterNfcSettingsCallback(INfcSettingsCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_unregisterNfcSettingsCallback, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterNfcSettingsCallback(cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
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

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public boolean isShowOverflowMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isShowOverflowMenu, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isShowOverflowMenu();
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

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public List<ServiceEntry> getServiceEntryList(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getServiceEntryList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServiceEntryList(userHandle);
                    }
                    _reply.readException();
                    List<ServiceEntry> _result = _reply.createTypedArrayList(ServiceEntry.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public boolean testServiceEntryList(List<ServiceEntry> proposal) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(proposal);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_testServiceEntryList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().testServiceEntryList(proposal);
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

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public void commitServiceEntryList(List<ServiceEntry> proposal) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(proposal);
                    if (this.mRemote.transact(Stub.TRANSACTION_commitServiceEntryList, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().commitServiceEntryList(proposal);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public List<ServiceEntry> getNonAidBasedServiceEntryList(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getNonAidBasedServiceEntryList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNonAidBasedServiceEntryList(userHandle);
                    }
                    _reply.readException();
                    List<ServiceEntry> _result = _reply.createTypedArrayList(ServiceEntry.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public void commitNonAidBasedServiceEntryList(List<ServiceEntry> proposal) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(proposal);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().commitNonAidBasedServiceEntryList(proposal);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_extensions.INfcSettingsAdapter
            public void DefaultRouteSet(int routeLoc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    if (this.mRemote.transact(Stub.TRANSACTION_DefaultRouteSet, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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
        }

        public static boolean setDefaultImpl(INfcSettingsAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcSettingsAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
