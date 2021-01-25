package android.nfc;

import android.content.ComponentName;
import android.nfc.cardemulation.NfcFServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface INfcFCardEmulation extends IInterface {
    boolean disableNfcFForegroundService() throws RemoteException;

    boolean enableNfcFForegroundService(ComponentName componentName) throws RemoteException;

    int getMaxNumOfRegisterableSystemCodes() throws RemoteException;

    List<NfcFServiceInfo> getNfcFServices(int i) throws RemoteException;

    String getNfcid2ForService(int i, ComponentName componentName) throws RemoteException;

    String getSystemCodeForService(int i, ComponentName componentName) throws RemoteException;

    boolean registerSystemCodeForService(int i, ComponentName componentName, String str) throws RemoteException;

    boolean removeSystemCodeForService(int i, ComponentName componentName) throws RemoteException;

    boolean setNfcid2ForService(int i, ComponentName componentName, String str) throws RemoteException;

    public static class Default implements INfcFCardEmulation {
        @Override // android.nfc.INfcFCardEmulation
        public String getSystemCodeForService(int userHandle, ComponentName service) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcFCardEmulation
        public boolean registerSystemCodeForService(int userHandle, ComponentName service, String systemCode) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcFCardEmulation
        public boolean removeSystemCodeForService(int userHandle, ComponentName service) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcFCardEmulation
        public String getNfcid2ForService(int userHandle, ComponentName service) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcFCardEmulation
        public boolean setNfcid2ForService(int userHandle, ComponentName service, String nfcid2) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcFCardEmulation
        public boolean enableNfcFForegroundService(ComponentName service) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcFCardEmulation
        public boolean disableNfcFForegroundService() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcFCardEmulation
        public List<NfcFServiceInfo> getNfcFServices(int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcFCardEmulation
        public int getMaxNumOfRegisterableSystemCodes() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcFCardEmulation {
        private static final String DESCRIPTOR = "android.nfc.INfcFCardEmulation";
        static final int TRANSACTION_disableNfcFForegroundService = 7;
        static final int TRANSACTION_enableNfcFForegroundService = 6;
        static final int TRANSACTION_getMaxNumOfRegisterableSystemCodes = 9;
        static final int TRANSACTION_getNfcFServices = 8;
        static final int TRANSACTION_getNfcid2ForService = 4;
        static final int TRANSACTION_getSystemCodeForService = 1;
        static final int TRANSACTION_registerSystemCodeForService = 2;
        static final int TRANSACTION_removeSystemCodeForService = 3;
        static final int TRANSACTION_setNfcid2ForService = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcFCardEmulation asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcFCardEmulation)) {
                return new Proxy(obj);
            }
            return (INfcFCardEmulation) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getSystemCodeForService";
                case 2:
                    return "registerSystemCodeForService";
                case 3:
                    return "removeSystemCodeForService";
                case 4:
                    return "getNfcid2ForService";
                case 5:
                    return "setNfcid2ForService";
                case 6:
                    return "enableNfcFForegroundService";
                case 7:
                    return "disableNfcFForegroundService";
                case 8:
                    return "getNfcFServices";
                case 9:
                    return "getMaxNumOfRegisterableSystemCodes";
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
            ComponentName _arg1;
            ComponentName _arg12;
            ComponentName _arg13;
            ComponentName _arg14;
            ComponentName _arg15;
            ComponentName _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        String _result = getSystemCodeForService(_arg02, _arg1);
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean registerSystemCodeForService = registerSystemCodeForService(_arg03, _arg12, data.readString());
                        reply.writeNoException();
                        reply.writeInt(registerSystemCodeForService ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean removeSystemCodeForService = removeSystemCodeForService(_arg04, _arg13);
                        reply.writeNoException();
                        reply.writeInt(removeSystemCodeForService ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        String _result2 = getNfcid2ForService(_arg05, _arg14);
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        boolean nfcid2ForService = setNfcid2ForService(_arg06, _arg15, data.readString());
                        reply.writeNoException();
                        reply.writeInt(nfcid2ForService ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean enableNfcFForegroundService = enableNfcFForegroundService(_arg0);
                        reply.writeNoException();
                        reply.writeInt(enableNfcFForegroundService ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableNfcFForegroundService = disableNfcFForegroundService();
                        reply.writeNoException();
                        reply.writeInt(disableNfcFForegroundService ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<NfcFServiceInfo> _result3 = getNfcFServices(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result3);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getMaxNumOfRegisterableSystemCodes();
                        reply.writeNoException();
                        reply.writeInt(_result4);
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
        public static class Proxy implements INfcFCardEmulation {
            public static INfcFCardEmulation sDefaultImpl;
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

            @Override // android.nfc.INfcFCardEmulation
            public String getSystemCodeForService(int userHandle, ComponentName service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemCodeForService(userHandle, service);
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

            @Override // android.nfc.INfcFCardEmulation
            public boolean registerSystemCodeForService(int userHandle, ComponentName service, String systemCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = true;
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(systemCode);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerSystemCodeForService(userHandle, service, systemCode);
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

            @Override // android.nfc.INfcFCardEmulation
            public boolean removeSystemCodeForService(int userHandle, ComponentName service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = true;
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeSystemCodeForService(userHandle, service);
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

            @Override // android.nfc.INfcFCardEmulation
            public String getNfcid2ForService(int userHandle, ComponentName service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcid2ForService(userHandle, service);
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

            @Override // android.nfc.INfcFCardEmulation
            public boolean setNfcid2ForService(int userHandle, ComponentName service, String nfcid2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = true;
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(nfcid2);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setNfcid2ForService(userHandle, service, nfcid2);
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

            @Override // android.nfc.INfcFCardEmulation
            public boolean enableNfcFForegroundService(ComponentName service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableNfcFForegroundService(service);
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

            @Override // android.nfc.INfcFCardEmulation
            public boolean disableNfcFForegroundService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableNfcFForegroundService();
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

            @Override // android.nfc.INfcFCardEmulation
            public List<NfcFServiceInfo> getNfcFServices(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcFServices(userHandle);
                    }
                    _reply.readException();
                    List<NfcFServiceInfo> _result = _reply.createTypedArrayList(NfcFServiceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcFCardEmulation
            public int getMaxNumOfRegisterableSystemCodes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxNumOfRegisterableSystemCodes();
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

        public static boolean setDefaultImpl(INfcFCardEmulation impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcFCardEmulation getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
