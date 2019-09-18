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

    public static abstract class Stub extends Binder implements INfcAdapter {
        private static final String DESCRIPTOR = "android.nfc.INfcAdapter";
        static final int TRANSACTION_addNfcUnlockHandler = 28;
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
        static final int TRANSACTION_isRwP2pOn = 16;
        static final int TRANSACTION_pausePolling = 13;
        static final int TRANSACTION_removeNfcUnlockHandler = 29;
        static final int TRANSACTION_resumePolling = 14;
        static final int TRANSACTION_selectCardEmulation = 18;
        static final int TRANSACTION_set2ndLevelMenu = 32;
        static final int TRANSACTION_setAppCallback = 21;
        static final int TRANSACTION_setForegroundDispatch = 20;
        static final int TRANSACTION_setP2pModes = 27;
        static final int TRANSACTION_setReaderMode = 26;
        static final int TRANSACTION_verifyNfcPermission = 15;

        private static class Proxy implements INfcAdapter {
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

            public INfcTag getNfcTagInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return INfcTag.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public INfcCardEmulation getNfcCardEmulationInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return INfcCardEmulation.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public INfcAdapterExtras getNfcAdapterExtrasInterface(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return INfcAdapterExtras.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enablePolling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disablePolling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getNfcAdapterVendorInterface(String vendor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(vendor);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disable(boolean saveState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(saveState);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableNdefPush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableNdefPush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNdefPushEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pausePolling(int timeoutInMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeoutInMs);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resumePolling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void verifyNfcPermission() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRwP2pOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSelectedCardEmulation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void selectCardEmulation(int sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sub);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSupportCardEmulation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppCallback(IAppCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void invokeBeam() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public boolean ignore(int nativeHandle, int debounceMs, ITagRemovedCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    _data.writeInt(debounceMs);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setP2pModes(int initatorModes, int targetModes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(initatorModes);
                    _data.writeInt(targetModes);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addNfcUnlockHandler(INfcUnlockHandler unlockHandler, int[] techList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(unlockHandler != null ? unlockHandler.asBinder() : null);
                    _data.writeIntArray(techList);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeNfcUnlockHandler(INfcUnlockHandler unlockHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(unlockHandler != null ? unlockHandler.asBinder() : null);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getFirmwareVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean is2ndLevelMenuOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void set2ndLevelMenu(boolean onOff) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(onOff);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public INfcFCardEmulation getNfcFCardEmulationInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return INfcFCardEmulation.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public INfcDta getNfcDtaInterface(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    return INfcDta.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v10, resolved type: android.nfc.TechListParcel} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v21, resolved type: android.os.Bundle} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v1, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r3v3, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r3v5, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r3v14 */
        /* JADX WARNING: type inference failed for: r3v18 */
        /* JADX WARNING: type inference failed for: r3v28, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r3v30, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r3v32 */
        /* JADX WARNING: type inference failed for: r3v33 */
        /* JADX WARNING: type inference failed for: r3v34 */
        /* JADX WARNING: type inference failed for: r3v35 */
        /* JADX WARNING: type inference failed for: r3v36 */
        /* JADX WARNING: type inference failed for: r3v37 */
        /* JADX WARNING: type inference failed for: r3v38 */
        /* JADX WARNING: type inference failed for: r3v39 */
        /* JADX WARNING: type inference failed for: r3v40 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PendingIntent _arg0;
            if (code != 1598968902) {
                boolean _arg02 = false;
                ? _arg3 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        INfcTag _result = getNfcTagInterface();
                        reply.writeNoException();
                        if (_result != null) {
                            _arg3 = _result.asBinder();
                        }
                        reply.writeStrongBinder(_arg3);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        INfcCardEmulation _result2 = getNfcCardEmulationInterface();
                        reply.writeNoException();
                        if (_result2 != null) {
                            _arg3 = _result2.asBinder();
                        }
                        reply.writeStrongBinder(_arg3);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        INfcAdapterExtras _result3 = getNfcAdapterExtrasInterface(data.readString());
                        reply.writeNoException();
                        if (_result3 != null) {
                            _arg3 = _result3.asBinder();
                        }
                        reply.writeStrongBinder(_arg3);
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
                            _arg02 = true;
                        }
                        boolean _result6 = disable(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result7 = enable();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result8 = enableNdefPush();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result9 = disableNdefPush();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result10 = isNdefPushEnabled();
                        reply.writeNoException();
                        reply.writeInt(_result10);
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
                        boolean _result11 = isRwP2pOn();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getSelectedCardEmulation();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        selectCardEmulation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getSupportCardEmulation();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        IntentFilter[] _arg1 = (IntentFilter[]) data.createTypedArray(IntentFilter.CREATOR);
                        if (data.readInt() != 0) {
                            _arg3 = TechListParcel.CREATOR.createFromParcel(data);
                        }
                        setForegroundDispatch(_arg0, _arg1, _arg3);
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
                            _arg3 = BeamShareData.CREATOR.createFromParcel(data);
                        }
                        invokeBeamInternal(_arg3);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result14 = ignore(data.readInt(), data.readInt(), ITagRemovedCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg3 = Tag.CREATOR.createFromParcel(data);
                        }
                        dispatch(_arg3);
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg03 = data.readStrongBinder();
                        IAppCallback _arg12 = IAppCallback.Stub.asInterface(data.readStrongBinder());
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = Bundle.CREATOR.createFromParcel(data);
                        }
                        setReaderMode(_arg03, _arg12, _arg2, _arg3);
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
                        String _result15 = getFirmwareVersion();
                        reply.writeNoException();
                        reply.writeString(_result15);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result16 = is2ndLevelMenuOn();
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        set2ndLevelMenu(_arg02);
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        INfcFCardEmulation _result17 = getNfcFCardEmulationInterface();
                        reply.writeNoException();
                        if (_result17 != null) {
                            _arg3 = _result17.asBinder();
                        }
                        reply.writeStrongBinder(_arg3);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        INfcDta _result18 = getNfcDtaInterface(data.readString());
                        reply.writeNoException();
                        if (_result18 != null) {
                            _arg3 = _result18.asBinder();
                        }
                        reply.writeStrongBinder(_arg3);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void addNfcUnlockHandler(INfcUnlockHandler iNfcUnlockHandler, int[] iArr) throws RemoteException;

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

    boolean isRwP2pOn() throws RemoteException;

    void pausePolling(int i) throws RemoteException;

    void removeNfcUnlockHandler(INfcUnlockHandler iNfcUnlockHandler) throws RemoteException;

    void resumePolling() throws RemoteException;

    void selectCardEmulation(int i) throws RemoteException;

    void set2ndLevelMenu(boolean z) throws RemoteException;

    void setAppCallback(IAppCallback iAppCallback) throws RemoteException;

    void setForegroundDispatch(PendingIntent pendingIntent, IntentFilter[] intentFilterArr, TechListParcel techListParcel) throws RemoteException;

    void setP2pModes(int i, int i2) throws RemoteException;

    void setReaderMode(IBinder iBinder, IAppCallback iAppCallback, int i, Bundle bundle) throws RemoteException;

    void verifyNfcPermission() throws RemoteException;
}
