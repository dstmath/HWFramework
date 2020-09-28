package com.mediatek.gba;

import android.net.Network;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGbaService extends IInterface {
    NafSessionKey getCachedKey(String str, byte[] bArr, int i) throws RemoteException;

    int getGbaSupported() throws RemoteException;

    int getGbaSupportedForSubscriber(int i) throws RemoteException;

    boolean isGbaKeyExpired(String str, byte[] bArr) throws RemoteException;

    boolean isGbaKeyExpiredForSubscriber(String str, byte[] bArr, int i) throws RemoteException;

    NafSessionKey runGbaAuthentication(String str, byte[] bArr, boolean z) throws RemoteException;

    NafSessionKey runGbaAuthenticationForSubscriber(String str, byte[] bArr, boolean z, int i) throws RemoteException;

    void setNetwork(Network network) throws RemoteException;

    void updateCachedKey(String str, byte[] bArr, int i, NafSessionKey nafSessionKey) throws RemoteException;

    public static class Default implements IGbaService {
        @Override // com.mediatek.gba.IGbaService
        public int getGbaSupported() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.gba.IGbaService
        public int getGbaSupportedForSubscriber(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.gba.IGbaService
        public boolean isGbaKeyExpired(String nafFqdn, byte[] nafSecurProtocolId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.gba.IGbaService
        public boolean isGbaKeyExpiredForSubscriber(String nafFqdn, byte[] nafSecurProtocolId, int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.gba.IGbaService
        public NafSessionKey runGbaAuthentication(String nafFqdn, byte[] nafSecurProtocolId, boolean forceRun) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.gba.IGbaService
        public NafSessionKey runGbaAuthenticationForSubscriber(String nafFqdn, byte[] nafSecurProtocolId, boolean forceRun, int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.gba.IGbaService
        public void setNetwork(Network network) throws RemoteException {
        }

        @Override // com.mediatek.gba.IGbaService
        public NafSessionKey getCachedKey(String nafFqdn, byte[] nafSecurProtocolId, int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.gba.IGbaService
        public void updateCachedKey(String nafFqdn, byte[] nafSecurProtocolId, int subId, NafSessionKey nafSessionKey) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGbaService {
        private static final String DESCRIPTOR = "com.mediatek.gba.IGbaService";
        static final int TRANSACTION_getCachedKey = 8;
        static final int TRANSACTION_getGbaSupported = 1;
        static final int TRANSACTION_getGbaSupportedForSubscriber = 2;
        static final int TRANSACTION_isGbaKeyExpired = 3;
        static final int TRANSACTION_isGbaKeyExpiredForSubscriber = 4;
        static final int TRANSACTION_runGbaAuthentication = 5;
        static final int TRANSACTION_runGbaAuthenticationForSubscriber = 6;
        static final int TRANSACTION_setNetwork = 7;
        static final int TRANSACTION_updateCachedKey = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGbaService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGbaService)) {
                return new Proxy(obj);
            }
            return (IGbaService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Network _arg0;
            NafSessionKey _arg3;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getGbaSupported();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getGbaSupportedForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGbaKeyExpired = isGbaKeyExpired(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(isGbaKeyExpired ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGbaKeyExpiredForSubscriber = isGbaKeyExpiredForSubscriber(data.readString(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isGbaKeyExpiredForSubscriber ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        NafSessionKey _result3 = runGbaAuthentication(data.readString(), data.createByteArray(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        NafSessionKey _result4 = runGbaAuthenticationForSubscriber(data.readString(), data.createByteArray(), data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Network) Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setNetwork(_arg0);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getCachedKey /*{ENCODED_INT: 8}*/:
                        data.enforceInterface(DESCRIPTOR);
                        NafSessionKey _result5 = getCachedKey(data.readString(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_updateCachedKey /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        byte[] _arg1 = data.createByteArray();
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = NafSessionKey.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        updateCachedKey(_arg02, _arg1, _arg2, _arg3);
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
        public static class Proxy implements IGbaService {
            public static IGbaService sDefaultImpl;
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

            @Override // com.mediatek.gba.IGbaService
            public int getGbaSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGbaSupported();
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

            @Override // com.mediatek.gba.IGbaService
            public int getGbaSupportedForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGbaSupportedForSubscriber(subId);
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

            @Override // com.mediatek.gba.IGbaService
            public boolean isGbaKeyExpired(String nafFqdn, byte[] nafSecurProtocolId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nafFqdn);
                    _data.writeByteArray(nafSecurProtocolId);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGbaKeyExpired(nafFqdn, nafSecurProtocolId);
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

            @Override // com.mediatek.gba.IGbaService
            public boolean isGbaKeyExpiredForSubscriber(String nafFqdn, byte[] nafSecurProtocolId, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nafFqdn);
                    _data.writeByteArray(nafSecurProtocolId);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGbaKeyExpiredForSubscriber(nafFqdn, nafSecurProtocolId, subId);
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

            @Override // com.mediatek.gba.IGbaService
            public NafSessionKey runGbaAuthentication(String nafFqdn, byte[] nafSecurProtocolId, boolean forceRun) throws RemoteException {
                NafSessionKey _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nafFqdn);
                    _data.writeByteArray(nafSecurProtocolId);
                    _data.writeInt(forceRun ? 1 : 0);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().runGbaAuthentication(nafFqdn, nafSecurProtocolId, forceRun);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NafSessionKey.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gba.IGbaService
            public NafSessionKey runGbaAuthenticationForSubscriber(String nafFqdn, byte[] nafSecurProtocolId, boolean forceRun, int subId) throws RemoteException {
                NafSessionKey _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nafFqdn);
                    _data.writeByteArray(nafSecurProtocolId);
                    _data.writeInt(forceRun ? 1 : 0);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().runGbaAuthenticationForSubscriber(nafFqdn, nafSecurProtocolId, forceRun, subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NafSessionKey.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gba.IGbaService
            public void setNetwork(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetwork(network);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gba.IGbaService
            public NafSessionKey getCachedKey(String nafFqdn, byte[] nafSecurProtocolId, int subId) throws RemoteException {
                NafSessionKey _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nafFqdn);
                    _data.writeByteArray(nafSecurProtocolId);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCachedKey, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCachedKey(nafFqdn, nafSecurProtocolId, subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NafSessionKey.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gba.IGbaService
            public void updateCachedKey(String nafFqdn, byte[] nafSecurProtocolId, int subId, NafSessionKey nafSessionKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nafFqdn);
                    _data.writeByteArray(nafSecurProtocolId);
                    _data.writeInt(subId);
                    if (nafSessionKey != null) {
                        _data.writeInt(1);
                        nafSessionKey.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_updateCachedKey, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateCachedKey(nafFqdn, nafSecurProtocolId, subId, nafSessionKey);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGbaService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGbaService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
