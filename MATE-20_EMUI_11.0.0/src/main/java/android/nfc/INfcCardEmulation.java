package android.nfc;

import android.content.ComponentName;
import android.nfc.cardemulation.AidGroup;
import android.nfc.cardemulation.ApduServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface INfcCardEmulation extends IInterface {
    AidGroup getAidGroupForService(int i, ComponentName componentName, String str) throws RemoteException;

    ApduServiceInfo getPaymentDefaultServiceInfo(int i) throws RemoteException;

    ComponentName getPaymentPriority(int i) throws RemoteException;

    List<ApduServiceInfo> getServices(int i, String str) throws RemoteException;

    int getUsedAidTableSizeInPercent(int i, String str) throws RemoteException;

    void initializePaymentDefault(int i, int i2) throws RemoteException;

    boolean isDefaultServiceForAid(int i, ComponentName componentName, String str) throws RemoteException;

    boolean isDefaultServiceForCategory(int i, ComponentName componentName, String str) throws RemoteException;

    boolean isRegisteredService(int i, ComponentName componentName, String str) throws RemoteException;

    boolean registerAidGroupForService(int i, ComponentName componentName, AidGroup aidGroup) throws RemoteException;

    void registerService(int i, ComponentName componentName, String str, int i2) throws RemoteException;

    boolean removeAidGroupForService(int i, ComponentName componentName, String str) throws RemoteException;

    boolean setDefaultForNextTap(int i, ComponentName componentName) throws RemoteException;

    boolean setDefaultServiceForCategory(int i, ComponentName componentName, String str) throws RemoteException;

    boolean setOffHostForService(int i, ComponentName componentName, String str) throws RemoteException;

    boolean setPreferredService(ComponentName componentName) throws RemoteException;

    boolean supportsAidPrefixRegistration() throws RemoteException;

    void unregisterOtherService(int i, ComponentName componentName) throws RemoteException;

    boolean unsetOffHostForService(int i, ComponentName componentName) throws RemoteException;

    boolean unsetPreferredService() throws RemoteException;

    public static class Default implements INfcCardEmulation {
        @Override // android.nfc.INfcCardEmulation
        public boolean isDefaultServiceForCategory(int userHandle, ComponentName service, String category) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean isDefaultServiceForAid(int userHandle, ComponentName service, String aid) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean setDefaultServiceForCategory(int userHandle, ComponentName service, String category) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean setDefaultForNextTap(int userHandle, ComponentName service) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean registerAidGroupForService(int userHandle, ComponentName service, AidGroup aidGroup) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean setOffHostForService(int userHandle, ComponentName service, String offHostSecureElement) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean unsetOffHostForService(int userHandle, ComponentName service) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public AidGroup getAidGroupForService(int userHandle, ComponentName service, String category) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean removeAidGroupForService(int userHandle, ComponentName service, String category) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public List<ApduServiceInfo> getServices(int userHandle, String category) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean setPreferredService(ComponentName service) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean unsetPreferredService() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean supportsAidPrefixRegistration() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public void registerService(int userHandle, ComponentName app, String category, int requester) throws RemoteException {
        }

        @Override // android.nfc.INfcCardEmulation
        public void unregisterOtherService(int userHandle, ComponentName app) throws RemoteException {
        }

        @Override // android.nfc.INfcCardEmulation
        public boolean isRegisteredService(int userHandle, ComponentName app, String category) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcCardEmulation
        public int getUsedAidTableSizeInPercent(int userHandle, String category) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcCardEmulation
        public void initializePaymentDefault(int userHandle, int necessity) throws RemoteException {
        }

        @Override // android.nfc.INfcCardEmulation
        public ComponentName getPaymentPriority(int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcCardEmulation
        public ApduServiceInfo getPaymentDefaultServiceInfo(int userHandle) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcCardEmulation {
        private static final String DESCRIPTOR = "android.nfc.INfcCardEmulation";
        static final int TRANSACTION_getAidGroupForService = 8;
        static final int TRANSACTION_getPaymentDefaultServiceInfo = 20;
        static final int TRANSACTION_getPaymentPriority = 19;
        static final int TRANSACTION_getServices = 10;
        static final int TRANSACTION_getUsedAidTableSizeInPercent = 17;
        static final int TRANSACTION_initializePaymentDefault = 18;
        static final int TRANSACTION_isDefaultServiceForAid = 2;
        static final int TRANSACTION_isDefaultServiceForCategory = 1;
        static final int TRANSACTION_isRegisteredService = 16;
        static final int TRANSACTION_registerAidGroupForService = 5;
        static final int TRANSACTION_registerService = 14;
        static final int TRANSACTION_removeAidGroupForService = 9;
        static final int TRANSACTION_setDefaultForNextTap = 4;
        static final int TRANSACTION_setDefaultServiceForCategory = 3;
        static final int TRANSACTION_setOffHostForService = 6;
        static final int TRANSACTION_setPreferredService = 11;
        static final int TRANSACTION_supportsAidPrefixRegistration = 13;
        static final int TRANSACTION_unregisterOtherService = 15;
        static final int TRANSACTION_unsetOffHostForService = 7;
        static final int TRANSACTION_unsetPreferredService = 12;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcCardEmulation asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcCardEmulation)) {
                return new Proxy(obj);
            }
            return (INfcCardEmulation) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "isDefaultServiceForCategory";
                case 2:
                    return "isDefaultServiceForAid";
                case 3:
                    return "setDefaultServiceForCategory";
                case 4:
                    return "setDefaultForNextTap";
                case 5:
                    return "registerAidGroupForService";
                case 6:
                    return "setOffHostForService";
                case 7:
                    return "unsetOffHostForService";
                case 8:
                    return "getAidGroupForService";
                case 9:
                    return "removeAidGroupForService";
                case 10:
                    return "getServices";
                case 11:
                    return "setPreferredService";
                case 12:
                    return "unsetPreferredService";
                case 13:
                    return "supportsAidPrefixRegistration";
                case 14:
                    return "registerService";
                case 15:
                    return "unregisterOtherService";
                case 16:
                    return "isRegisteredService";
                case 17:
                    return "getUsedAidTableSizeInPercent";
                case 18:
                    return "initializePaymentDefault";
                case 19:
                    return "getPaymentPriority";
                case 20:
                    return "getPaymentDefaultServiceInfo";
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
            AidGroup _arg2;
            ComponentName _arg16;
            ComponentName _arg17;
            ComponentName _arg18;
            ComponentName _arg19;
            ComponentName _arg0;
            ComponentName _arg110;
            ComponentName _arg111;
            ComponentName _arg112;
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
                        boolean isDefaultServiceForCategory = isDefaultServiceForCategory(_arg02, _arg1, data.readString());
                        reply.writeNoException();
                        reply.writeInt(isDefaultServiceForCategory ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean isDefaultServiceForAid = isDefaultServiceForAid(_arg03, _arg12, data.readString());
                        reply.writeNoException();
                        reply.writeInt(isDefaultServiceForAid ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean defaultServiceForCategory = setDefaultServiceForCategory(_arg04, _arg13, data.readString());
                        reply.writeNoException();
                        reply.writeInt(defaultServiceForCategory ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        boolean defaultForNextTap = setDefaultForNextTap(_arg05, _arg14);
                        reply.writeNoException();
                        reply.writeInt(defaultForNextTap ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = AidGroup.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        boolean registerAidGroupForService = registerAidGroupForService(_arg06, _arg15, _arg2);
                        reply.writeNoException();
                        reply.writeInt(registerAidGroupForService ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        boolean offHostForService = setOffHostForService(_arg07, _arg16, data.readString());
                        reply.writeNoException();
                        reply.writeInt(offHostForService ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg17 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        boolean unsetOffHostForService = unsetOffHostForService(_arg08, _arg17);
                        reply.writeNoException();
                        reply.writeInt(unsetOffHostForService ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg18 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        AidGroup _result = getAidGroupForService(_arg09, _arg18, data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg19 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg19 = null;
                        }
                        boolean removeAidGroupForService = removeAidGroupForService(_arg010, _arg19, data.readString());
                        reply.writeNoException();
                        reply.writeInt(removeAidGroupForService ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        List<ApduServiceInfo> _result2 = getServices(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean preferredService = setPreferredService(_arg0);
                        reply.writeNoException();
                        reply.writeInt(preferredService ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unsetPreferredService = unsetPreferredService();
                        reply.writeNoException();
                        reply.writeInt(unsetPreferredService ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supportsAidPrefixRegistration = supportsAidPrefixRegistration();
                        reply.writeNoException();
                        reply.writeInt(supportsAidPrefixRegistration ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg110 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg110 = null;
                        }
                        registerService(_arg011, _arg110, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg111 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg111 = null;
                        }
                        unregisterOtherService(_arg012, _arg111);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg013 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg112 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg112 = null;
                        }
                        boolean isRegisteredService = isRegisteredService(_arg013, _arg112, data.readString());
                        reply.writeNoException();
                        reply.writeInt(isRegisteredService ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getUsedAidTableSizeInPercent(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        initializePaymentDefault(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result4 = getPaymentPriority(data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        ApduServiceInfo _result5 = getPaymentDefaultServiceInfo(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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
        public static class Proxy implements INfcCardEmulation {
            public static INfcCardEmulation sDefaultImpl;
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

            @Override // android.nfc.INfcCardEmulation
            public boolean isDefaultServiceForCategory(int userHandle, ComponentName service, String category) throws RemoteException {
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
                    _data.writeString(category);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDefaultServiceForCategory(userHandle, service, category);
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

            @Override // android.nfc.INfcCardEmulation
            public boolean isDefaultServiceForAid(int userHandle, ComponentName service, String aid) throws RemoteException {
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
                    _data.writeString(aid);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDefaultServiceForAid(userHandle, service, aid);
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

            @Override // android.nfc.INfcCardEmulation
            public boolean setDefaultServiceForCategory(int userHandle, ComponentName service, String category) throws RemoteException {
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
                    _data.writeString(category);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDefaultServiceForCategory(userHandle, service, category);
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

            @Override // android.nfc.INfcCardEmulation
            public boolean setDefaultForNextTap(int userHandle, ComponentName service) throws RemoteException {
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
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDefaultForNextTap(userHandle, service);
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

            @Override // android.nfc.INfcCardEmulation
            public boolean registerAidGroupForService(int userHandle, ComponentName service, AidGroup aidGroup) throws RemoteException {
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
                    if (aidGroup != null) {
                        _data.writeInt(1);
                        aidGroup.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerAidGroupForService(userHandle, service, aidGroup);
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

            @Override // android.nfc.INfcCardEmulation
            public boolean setOffHostForService(int userHandle, ComponentName service, String offHostSecureElement) throws RemoteException {
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
                    _data.writeString(offHostSecureElement);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setOffHostForService(userHandle, service, offHostSecureElement);
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

            @Override // android.nfc.INfcCardEmulation
            public boolean unsetOffHostForService(int userHandle, ComponentName service) throws RemoteException {
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
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unsetOffHostForService(userHandle, service);
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

            @Override // android.nfc.INfcCardEmulation
            public AidGroup getAidGroupForService(int userHandle, ComponentName service, String category) throws RemoteException {
                AidGroup _result;
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
                    _data.writeString(category);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAidGroupForService(userHandle, service, category);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AidGroup.CREATOR.createFromParcel(_reply);
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

            @Override // android.nfc.INfcCardEmulation
            public boolean removeAidGroupForService(int userHandle, ComponentName service, String category) throws RemoteException {
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
                    _data.writeString(category);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeAidGroupForService(userHandle, service, category);
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

            @Override // android.nfc.INfcCardEmulation
            public List<ApduServiceInfo> getServices(int userHandle, String category) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeString(category);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServices(userHandle, category);
                    }
                    _reply.readException();
                    List<ApduServiceInfo> _result = _reply.createTypedArrayList(ApduServiceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcCardEmulation
            public boolean setPreferredService(ComponentName service) throws RemoteException {
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
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPreferredService(service);
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

            @Override // android.nfc.INfcCardEmulation
            public boolean unsetPreferredService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unsetPreferredService();
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

            @Override // android.nfc.INfcCardEmulation
            public boolean supportsAidPrefixRegistration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportsAidPrefixRegistration();
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

            @Override // android.nfc.INfcCardEmulation
            public void registerService(int userHandle, ComponentName app, String category, int requester) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (app != null) {
                        _data.writeInt(1);
                        app.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(category);
                    _data.writeInt(requester);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerService(userHandle, app, category, requester);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcCardEmulation
            public void unregisterOtherService(int userHandle, ComponentName app) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (app != null) {
                        _data.writeInt(1);
                        app.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterOtherService(userHandle, app);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcCardEmulation
            public boolean isRegisteredService(int userHandle, ComponentName app, String category) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    boolean _result = true;
                    if (app != null) {
                        _data.writeInt(1);
                        app.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(category);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRegisteredService(userHandle, app, category);
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

            @Override // android.nfc.INfcCardEmulation
            public int getUsedAidTableSizeInPercent(int userHandle, String category) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeString(category);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsedAidTableSizeInPercent(userHandle, category);
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

            @Override // android.nfc.INfcCardEmulation
            public void initializePaymentDefault(int userHandle, int necessity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    _data.writeInt(necessity);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().initializePaymentDefault(userHandle, necessity);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcCardEmulation
            public ComponentName getPaymentPriority(int userHandle) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPaymentPriority(userHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
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

            @Override // android.nfc.INfcCardEmulation
            public ApduServiceInfo getPaymentDefaultServiceInfo(int userHandle) throws RemoteException {
                ApduServiceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPaymentDefaultServiceInfo(userHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ApduServiceInfo.CREATOR.createFromParcel(_reply);
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
        }

        public static boolean setDefaultImpl(INfcCardEmulation impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcCardEmulation getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
