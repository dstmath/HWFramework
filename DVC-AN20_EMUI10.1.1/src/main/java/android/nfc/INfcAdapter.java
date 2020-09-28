package android.nfc;

import android.app.PendingIntent;
import android.content.IntentFilter;
import android.nfc.IAppCallback;
import android.nfc.INfcAdapterExtras;
import android.nfc.INfcCardEmulation;
import android.nfc.INfcDta;
import android.nfc.INfcFCardEmulation;
import android.nfc.INfcTag;
import android.nfc.INfcUnlockHandler;
import android.nfc.ITagRemovedCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INfcAdapter extends IInterface {
    void addNfcUnlockHandler(INfcUnlockHandler iNfcUnlockHandler, int[] iArr) throws RemoteException;

    boolean deviceSupportsNfcSecure() throws RemoteException;

    boolean disable(boolean z) throws RemoteException;

    boolean disableNdefPush() throws RemoteException;

    void disablePolling() throws RemoteException;

    void dispatch(Tag tag) throws RemoteException;

    boolean enable() throws RemoteException;

    boolean enableNdefPush() throws RemoteException;

    void enablePolling() throws RemoteException;

    String getFirmwareVersion() throws RemoteException;

    INfcAdapterExtras getNfcAdapterExtrasInterface(String str) throws RemoteException;

    IBinder getNfcAdapterVendorInterface(String str) throws RemoteException;

    INfcCardEmulation getNfcCardEmulationInterface() throws RemoteException;

    INfcDta getNfcDtaInterface(String str) throws RemoteException;

    INfcFCardEmulation getNfcFCardEmulationInterface() throws RemoteException;

    INfcTag getNfcTagInterface() throws RemoteException;

    int getSelectedCardEmulation() throws RemoteException;

    int getState() throws RemoteException;

    int getSupportCardEmulation() throws RemoteException;

    boolean ignore(int i, int i2, ITagRemovedCallback iTagRemovedCallback) throws RemoteException;

    void invokeBeam() throws RemoteException;

    void invokeBeamInternal(BeamShareData beamShareData) throws RemoteException;

    boolean is2ndLevelMenuOn() throws RemoteException;

    boolean isNdefPushEnabled() throws RemoteException;

    boolean isNfcSecureEnabled() throws RemoteException;

    boolean isRwP2pOn() throws RemoteException;

    void pausePolling(int i) throws RemoteException;

    void removeNfcUnlockHandler(INfcUnlockHandler iNfcUnlockHandler) throws RemoteException;

    void resumePolling() throws RemoteException;

    void selectCardEmulation(int i) throws RemoteException;

    void set2ndLevelMenu(boolean z) throws RemoteException;

    void setAppCallback(IAppCallback iAppCallback) throws RemoteException;

    void setForegroundDispatch(PendingIntent pendingIntent, IntentFilter[] intentFilterArr, TechListParcel techListParcel) throws RemoteException;

    boolean setNfcSecure(boolean z) throws RemoteException;

    void setP2pModes(int i, int i2) throws RemoteException;

    void setReaderMode(IBinder iBinder, IAppCallback iAppCallback, int i, Bundle bundle) throws RemoteException;

    void verifyNfcPermission() throws RemoteException;

    public static class Default implements INfcAdapter {
        @Override // android.nfc.INfcAdapter
        public INfcTag getNfcTagInterface() throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapter
        public INfcCardEmulation getNfcCardEmulationInterface() throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapter
        public INfcAdapterExtras getNfcAdapterExtrasInterface(String pkg) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapter
        public void enablePolling() throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void disablePolling() throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public IBinder getNfcAdapterVendorInterface(String vendor2) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapter
        public int getState() throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcAdapter
        public boolean disable(boolean saveState) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public boolean enable() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public boolean enableNdefPush() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public boolean disableNdefPush() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public boolean isNdefPushEnabled() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public void pausePolling(int timeoutInMs) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void resumePolling() throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void verifyNfcPermission() throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public boolean isRwP2pOn() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public int getSelectedCardEmulation() throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcAdapter
        public void selectCardEmulation(int sub) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public int getSupportCardEmulation() throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcAdapter
        public void setForegroundDispatch(PendingIntent intent, IntentFilter[] filters, TechListParcel techLists) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void setAppCallback(IAppCallback callback) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void invokeBeam() throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void invokeBeamInternal(BeamShareData shareData) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public boolean ignore(int nativeHandle, int debounceMs, ITagRemovedCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public void dispatch(Tag tag) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void setReaderMode(IBinder b, IAppCallback callback, int flags, Bundle extras) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void setP2pModes(int initatorModes, int targetModes) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void addNfcUnlockHandler(INfcUnlockHandler unlockHandler, int[] techList) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public void removeNfcUnlockHandler(INfcUnlockHandler unlockHandler) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public String getFirmwareVersion() throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapter
        public boolean is2ndLevelMenuOn() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public void set2ndLevelMenu(boolean onOff) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapter
        public INfcFCardEmulation getNfcFCardEmulationInterface() throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapter
        public INfcDta getNfcDtaInterface(String pkg) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapter
        public boolean isNfcSecureEnabled() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public boolean deviceSupportsNfcSecure() throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcAdapter
        public boolean setNfcSecure(boolean enable) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcAdapter {
        private static final String DESCRIPTOR = "android.nfc.INfcAdapter";
        static final int TRANSACTION_addNfcUnlockHandler = 28;
        static final int TRANSACTION_deviceSupportsNfcSecure = 36;
        static final int TRANSACTION_disable = 8;
        static final int TRANSACTION_disableNdefPush = 11;
        static final int TRANSACTION_disablePolling = 5;
        static final int TRANSACTION_dispatch = 25;
        static final int TRANSACTION_enable = 9;
        static final int TRANSACTION_enableNdefPush = 10;
        static final int TRANSACTION_enablePolling = 4;
        static final int TRANSACTION_getFirmwareVersion = 30;
        static final int TRANSACTION_getNfcAdapterExtrasInterface = 3;
        static final int TRANSACTION_getNfcAdapterVendorInterface = 6;
        static final int TRANSACTION_getNfcCardEmulationInterface = 2;
        static final int TRANSACTION_getNfcDtaInterface = 34;
        static final int TRANSACTION_getNfcFCardEmulationInterface = 33;
        static final int TRANSACTION_getNfcTagInterface = 1;
        static final int TRANSACTION_getSelectedCardEmulation = 17;
        static final int TRANSACTION_getState = 7;
        static final int TRANSACTION_getSupportCardEmulation = 19;
        static final int TRANSACTION_ignore = 24;
        static final int TRANSACTION_invokeBeam = 22;
        static final int TRANSACTION_invokeBeamInternal = 23;
        static final int TRANSACTION_is2ndLevelMenuOn = 31;
        static final int TRANSACTION_isNdefPushEnabled = 12;
        static final int TRANSACTION_isNfcSecureEnabled = 35;
        static final int TRANSACTION_isRwP2pOn = 16;
        static final int TRANSACTION_pausePolling = 13;
        static final int TRANSACTION_removeNfcUnlockHandler = 29;
        static final int TRANSACTION_resumePolling = 14;
        static final int TRANSACTION_selectCardEmulation = 18;
        static final int TRANSACTION_set2ndLevelMenu = 32;
        static final int TRANSACTION_setAppCallback = 21;
        static final int TRANSACTION_setForegroundDispatch = 20;
        static final int TRANSACTION_setNfcSecure = 37;
        static final int TRANSACTION_setP2pModes = 27;
        static final int TRANSACTION_setReaderMode = 26;
        static final int TRANSACTION_verifyNfcPermission = 15;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcAdapter)) {
                return new Proxy(obj);
            }
            return (INfcAdapter) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getNfcTagInterface";
                case 2:
                    return "getNfcCardEmulationInterface";
                case 3:
                    return "getNfcAdapterExtrasInterface";
                case 4:
                    return "enablePolling";
                case 5:
                    return "disablePolling";
                case 6:
                    return "getNfcAdapterVendorInterface";
                case 7:
                    return "getState";
                case 8:
                    return "disable";
                case 9:
                    return "enable";
                case 10:
                    return "enableNdefPush";
                case 11:
                    return "disableNdefPush";
                case 12:
                    return "isNdefPushEnabled";
                case 13:
                    return "pausePolling";
                case 14:
                    return "resumePolling";
                case 15:
                    return "verifyNfcPermission";
                case 16:
                    return "isRwP2pOn";
                case 17:
                    return "getSelectedCardEmulation";
                case 18:
                    return "selectCardEmulation";
                case 19:
                    return "getSupportCardEmulation";
                case 20:
                    return "setForegroundDispatch";
                case 21:
                    return "setAppCallback";
                case 22:
                    return "invokeBeam";
                case 23:
                    return "invokeBeamInternal";
                case 24:
                    return "ignore";
                case 25:
                    return "dispatch";
                case 26:
                    return "setReaderMode";
                case 27:
                    return "setP2pModes";
                case 28:
                    return "addNfcUnlockHandler";
                case 29:
                    return "removeNfcUnlockHandler";
                case 30:
                    return "getFirmwareVersion";
                case 31:
                    return "is2ndLevelMenuOn";
                case 32:
                    return "set2ndLevelMenu";
                case 33:
                    return "getNfcFCardEmulationInterface";
                case 34:
                    return "getNfcDtaInterface";
                case 35:
                    return "isNfcSecureEnabled";
                case 36:
                    return "deviceSupportsNfcSecure";
                case 37:
                    return "setNfcSecure";
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
            PendingIntent _arg0;
            TechListParcel _arg2;
            BeamShareData _arg02;
            Tag _arg03;
            Bundle _arg3;
            if (code != 1598968902) {
                boolean _arg04 = false;
                boolean _arg05 = false;
                boolean _arg06 = false;
                IBinder iBinder = null;
                IBinder iBinder2 = null;
                IBinder iBinder3 = null;
                IBinder iBinder4 = null;
                IBinder iBinder5 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        INfcTag _result = getNfcTagInterface();
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        INfcCardEmulation _result2 = getNfcCardEmulationInterface();
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder5 = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder5);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        INfcAdapterExtras _result3 = getNfcAdapterExtrasInterface(data.readString());
                        reply.writeNoException();
                        if (_result3 != null) {
                            iBinder4 = _result3.asBinder();
                        }
                        reply.writeStrongBinder(iBinder4);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        enablePolling();
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        disablePolling();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result4 = getNfcAdapterVendorInterface(data.readString());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getState();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        boolean disable = disable(_arg04);
                        reply.writeNoException();
                        reply.writeInt(disable ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enable = enable();
                        reply.writeNoException();
                        reply.writeInt(enable ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableNdefPush = enableNdefPush();
                        reply.writeNoException();
                        reply.writeInt(enableNdefPush ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableNdefPush = disableNdefPush();
                        reply.writeNoException();
                        reply.writeInt(disableNdefPush ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNdefPushEnabled = isNdefPushEnabled();
                        reply.writeNoException();
                        reply.writeInt(isNdefPushEnabled ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        pausePolling(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        resumePolling();
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        verifyNfcPermission();
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRwP2pOn = isRwP2pOn();
                        reply.writeNoException();
                        reply.writeInt(isRwP2pOn ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getSelectedCardEmulation();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        selectCardEmulation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getSupportCardEmulation();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        IntentFilter[] _arg1 = (IntentFilter[]) data.createTypedArray(IntentFilter.CREATOR);
                        if (data.readInt() != 0) {
                            _arg2 = TechListParcel.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        setForegroundDispatch(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        setAppCallback(IAppCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        invokeBeam();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BeamShareData.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        invokeBeamInternal(_arg02);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean ignore = ignore(data.readInt(), data.readInt(), ITagRemovedCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(ignore ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Tag.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        dispatch(_arg03);
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg07 = data.readStrongBinder();
                        IAppCallback _arg12 = IAppCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        setReaderMode(_arg07, _arg12, _arg22, _arg3);
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        setP2pModes(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        addNfcUnlockHandler(INfcUnlockHandler.Stub.asInterface(data.readStrongBinder()), data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        removeNfcUnlockHandler(INfcUnlockHandler.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        String _result8 = getFirmwareVersion();
                        reply.writeNoException();
                        reply.writeString(_result8);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        boolean is2ndLevelMenuOn = is2ndLevelMenuOn();
                        reply.writeNoException();
                        reply.writeInt(is2ndLevelMenuOn ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        set2ndLevelMenu(_arg06);
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        INfcFCardEmulation _result9 = getNfcFCardEmulationInterface();
                        reply.writeNoException();
                        if (_result9 != null) {
                            iBinder3 = _result9.asBinder();
                        }
                        reply.writeStrongBinder(iBinder3);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        INfcDta _result10 = getNfcDtaInterface(data.readString());
                        reply.writeNoException();
                        if (_result10 != null) {
                            iBinder2 = _result10.asBinder();
                        }
                        reply.writeStrongBinder(iBinder2);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNfcSecureEnabled = isNfcSecureEnabled();
                        reply.writeNoException();
                        reply.writeInt(isNfcSecureEnabled ? 1 : 0);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceSupportsNfcSecure = deviceSupportsNfcSecure();
                        reply.writeNoException();
                        reply.writeInt(deviceSupportsNfcSecure ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        boolean nfcSecure = setNfcSecure(_arg05);
                        reply.writeNoException();
                        reply.writeInt(nfcSecure ? 1 : 0);
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
        public static class Proxy implements INfcAdapter {
            public static INfcAdapter sDefaultImpl;
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

            @Override // android.nfc.INfcAdapter
            public INfcTag getNfcTagInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcTagInterface();
                    }
                    _reply.readException();
                    INfcTag _result = INfcTag.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public INfcCardEmulation getNfcCardEmulationInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcCardEmulationInterface();
                    }
                    _reply.readException();
                    INfcCardEmulation _result = INfcCardEmulation.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public INfcAdapterExtras getNfcAdapterExtrasInterface(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcAdapterExtrasInterface(pkg);
                    }
                    _reply.readException();
                    INfcAdapterExtras _result = INfcAdapterExtras.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void enablePolling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enablePolling();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void disablePolling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disablePolling();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public IBinder getNfcAdapterVendorInterface(String vendor2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(vendor2);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcAdapterVendorInterface(vendor2);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public int getState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getState();
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

            @Override // android.nfc.INfcAdapter
            public boolean disable(boolean saveState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(saveState ? 1 : 0);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disable(saveState);
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

            @Override // android.nfc.INfcAdapter
            public boolean enable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enable();
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

            @Override // android.nfc.INfcAdapter
            public boolean enableNdefPush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableNdefPush();
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

            @Override // android.nfc.INfcAdapter
            public boolean disableNdefPush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableNdefPush();
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

            @Override // android.nfc.INfcAdapter
            public boolean isNdefPushEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNdefPushEnabled();
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

            @Override // android.nfc.INfcAdapter
            public void pausePolling(int timeoutInMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeoutInMs);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pausePolling(timeoutInMs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void resumePolling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resumePolling();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void verifyNfcPermission() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().verifyNfcPermission();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public boolean isRwP2pOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRwP2pOn();
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

            @Override // android.nfc.INfcAdapter
            public int getSelectedCardEmulation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSelectedCardEmulation();
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

            @Override // android.nfc.INfcAdapter
            public void selectCardEmulation(int sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sub);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().selectCardEmulation(sub);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public int getSupportCardEmulation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportCardEmulation();
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

            @Override // android.nfc.INfcAdapter
            public void setForegroundDispatch(PendingIntent intent, IntentFilter[] filters, TechListParcel techLists) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(filters, 0);
                    if (techLists != null) {
                        _data.writeInt(1);
                        techLists.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setForegroundDispatch(intent, filters, techLists);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void setAppCallback(IAppCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void invokeBeam() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(22, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().invokeBeam();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void invokeBeamInternal(BeamShareData shareData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (shareData != null) {
                        _data.writeInt(1);
                        shareData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().invokeBeamInternal(shareData);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public boolean ignore(int nativeHandle, int debounceMs, ITagRemovedCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    _data.writeInt(debounceMs);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ignore(nativeHandle, debounceMs, callback);
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

            @Override // android.nfc.INfcAdapter
            public void dispatch(Tag tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tag != null) {
                        _data.writeInt(1);
                        tag.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dispatch(tag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void setReaderMode(IBinder b, IAppCallback callback, int flags, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(b);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(flags);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setReaderMode(b, callback, flags, extras);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void setP2pModes(int initatorModes, int targetModes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(initatorModes);
                    _data.writeInt(targetModes);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setP2pModes(initatorModes, targetModes);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void addNfcUnlockHandler(INfcUnlockHandler unlockHandler, int[] techList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(unlockHandler != null ? unlockHandler.asBinder() : null);
                    _data.writeIntArray(techList);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addNfcUnlockHandler(unlockHandler, techList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public void removeNfcUnlockHandler(INfcUnlockHandler unlockHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(unlockHandler != null ? unlockHandler.asBinder() : null);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeNfcUnlockHandler(unlockHandler);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public String getFirmwareVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFirmwareVersion();
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

            @Override // android.nfc.INfcAdapter
            public boolean is2ndLevelMenuOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().is2ndLevelMenuOn();
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

            @Override // android.nfc.INfcAdapter
            public void set2ndLevelMenu(boolean onOff) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(onOff ? 1 : 0);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().set2ndLevelMenu(onOff);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public INfcFCardEmulation getNfcFCardEmulationInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcFCardEmulationInterface();
                    }
                    _reply.readException();
                    INfcFCardEmulation _result = INfcFCardEmulation.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public INfcDta getNfcDtaInterface(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcDtaInterface(pkg);
                    }
                    _reply.readException();
                    INfcDta _result = INfcDta.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapter
            public boolean isNfcSecureEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNfcSecureEnabled();
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

            @Override // android.nfc.INfcAdapter
            public boolean deviceSupportsNfcSecure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deviceSupportsNfcSecure();
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

            @Override // android.nfc.INfcAdapter
            public boolean setNfcSecure(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setNfcSecure(enable);
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
        }

        public static boolean setDefaultImpl(INfcAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
