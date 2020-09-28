package com.st.android.nfc_dta_extensions;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INfcAdapterStDtaExtensions extends IInterface {
    boolean deinitialize() throws RemoteException;

    boolean disableDiscovery() throws RemoteException;

    int enableDiscovery(byte b, byte b2, byte b3, boolean z, boolean z2, byte b4, byte b5) throws RemoteException;

    boolean initialize() throws RemoteException;

    void setConnectionDevicesLimit(byte b, byte b2, byte b3, byte b4) throws RemoteException;

    void setCrVersion(byte b) throws RemoteException;

    void setFsdFscExtension(boolean z) throws RemoteException;

    void setListenNfcaUidMode(byte b) throws RemoteException;

    void setLlcpMode(int i) throws RemoteException;

    void setPatternNb(int i) throws RemoteException;

    void setSnepMode(byte b, byte b2, byte b3, byte b4, boolean z) throws RemoteException;

    void setT4atNfcdepPrio(byte b) throws RemoteException;

    public static class Default implements INfcAdapterStDtaExtensions {
        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public boolean initialize() throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public boolean deinitialize() throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public void setPatternNb(int nb) throws RemoteException {
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public void setCrVersion(byte ver) throws RemoteException {
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public void setConnectionDevicesLimit(byte cdlA, byte cdlB, byte cdlF, byte cdlV) throws RemoteException {
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public void setListenNfcaUidMode(byte mode) throws RemoteException {
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public void setT4atNfcdepPrio(byte prio) throws RemoteException {
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public void setFsdFscExtension(boolean ext) throws RemoteException {
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public void setLlcpMode(int miux_mode) throws RemoteException {
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public void setSnepMode(byte role, byte server_type, byte request_type, byte data_type, boolean disc_incorrect_len) throws RemoteException {
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public int enableDiscovery(byte con_poll, byte con_listen_dep, byte con_listen_t4tp, boolean con_listen_t3tp, boolean con_listen_acm, byte con_bitr_f, byte con_bitr_acm) throws RemoteException {
            return 0;
        }

        @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
        public boolean disableDiscovery() throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcAdapterStDtaExtensions {
        private static final String DESCRIPTOR = "com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions";
        static final int TRANSACTION_deinitialize = 2;
        static final int TRANSACTION_disableDiscovery = 12;
        static final int TRANSACTION_enableDiscovery = 11;
        static final int TRANSACTION_initialize = 1;
        static final int TRANSACTION_setConnectionDevicesLimit = 5;
        static final int TRANSACTION_setCrVersion = 4;
        static final int TRANSACTION_setFsdFscExtension = 8;
        static final int TRANSACTION_setListenNfcaUidMode = 6;
        static final int TRANSACTION_setLlcpMode = 9;
        static final int TRANSACTION_setPatternNb = 3;
        static final int TRANSACTION_setSnepMode = 10;
        static final int TRANSACTION_setT4atNfcdepPrio = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcAdapterStDtaExtensions asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcAdapterStDtaExtensions)) {
                return new Proxy(obj);
            }
            return (INfcAdapterStDtaExtensions) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean initialize = initialize();
                        reply.writeNoException();
                        reply.writeInt(initialize ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deinitialize = deinitialize();
                        reply.writeNoException();
                        reply.writeInt(deinitialize ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setPatternNb(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setCrVersion(data.readByte());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setConnectionDevicesLimit(data.readByte(), data.readByte(), data.readByte(), data.readByte());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setListenNfcaUidMode /*{ENCODED_INT: 6}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setListenNfcaUidMode(data.readByte());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setT4atNfcdepPrio /*{ENCODED_INT: 7}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setT4atNfcdepPrio(data.readByte());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setFsdFscExtension /*{ENCODED_INT: 8}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setFsdFscExtension(_arg0);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setLlcpMode /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setLlcpMode(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setSnepMode /*{ENCODED_INT: 10}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setSnepMode(data.readByte(), data.readByte(), data.readByte(), data.readByte(), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_enableDiscovery /*{ENCODED_INT: 11}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = enableDiscovery(data.readByte(), data.readByte(), data.readByte(), data.readInt() != 0, data.readInt() != 0, data.readByte(), data.readByte());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case TRANSACTION_disableDiscovery /*{ENCODED_INT: 12}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableDiscovery = disableDiscovery();
                        reply.writeNoException();
                        reply.writeInt(disableDiscovery ? 1 : 0);
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
        public static class Proxy implements INfcAdapterStDtaExtensions {
            public static INfcAdapterStDtaExtensions sDefaultImpl;
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

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public boolean initialize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().initialize();
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

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public boolean deinitialize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deinitialize();
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

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public void setPatternNb(int nb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nb);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPatternNb(nb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public void setCrVersion(byte ver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(ver);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCrVersion(ver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public void setConnectionDevicesLimit(byte cdlA, byte cdlB, byte cdlF, byte cdlV) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(cdlA);
                    _data.writeByte(cdlB);
                    _data.writeByte(cdlF);
                    _data.writeByte(cdlV);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setConnectionDevicesLimit(cdlA, cdlB, cdlF, cdlV);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public void setListenNfcaUidMode(byte mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(mode);
                    if (this.mRemote.transact(Stub.TRANSACTION_setListenNfcaUidMode, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setListenNfcaUidMode(mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public void setT4atNfcdepPrio(byte prio) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(prio);
                    if (this.mRemote.transact(Stub.TRANSACTION_setT4atNfcdepPrio, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setT4atNfcdepPrio(prio);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public void setFsdFscExtension(boolean ext) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ext ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_setFsdFscExtension, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFsdFscExtension(ext);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public void setLlcpMode(int miux_mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(miux_mode);
                    if (this.mRemote.transact(Stub.TRANSACTION_setLlcpMode, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLlcpMode(miux_mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public void setSnepMode(byte role, byte server_type, byte request_type, byte data_type, boolean disc_incorrect_len) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(role);
                    _data.writeByte(server_type);
                    _data.writeByte(request_type);
                    _data.writeByte(data_type);
                    _data.writeInt(disc_incorrect_len ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_setSnepMode, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSnepMode(role, server_type, request_type, data_type, disc_incorrect_len);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public int enableDiscovery(byte con_poll, byte con_listen_dep, byte con_listen_t4tp, boolean con_listen_t3tp, boolean con_listen_acm, byte con_bitr_f, byte con_bitr_acm) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeByte(con_poll);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByte(con_listen_dep);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByte(con_listen_t4tp);
                        int i = 1;
                        _data.writeInt(con_listen_t3tp ? 1 : 0);
                        if (!con_listen_acm) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        try {
                            _data.writeByte(con_bitr_f);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeByte(con_bitr_acm);
                            if (this.mRemote.transact(Stub.TRANSACTION_enableDiscovery, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int enableDiscovery = Stub.getDefaultImpl().enableDiscovery(con_poll, con_listen_dep, con_listen_t4tp, con_listen_t3tp, con_listen_acm, con_bitr_f, con_bitr_acm);
                            _reply.recycle();
                            _data.recycle();
                            return enableDiscovery;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.st.android.nfc_dta_extensions.INfcAdapterStDtaExtensions
            public boolean disableDiscovery() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_disableDiscovery, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableDiscovery();
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

        public static boolean setDefaultImpl(INfcAdapterStDtaExtensions impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcAdapterStDtaExtensions getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
