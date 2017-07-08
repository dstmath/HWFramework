package android.nfc;

import android.app.PendingIntent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.nxp.nfc.INxpNfcAdapter;

public interface INfcAdapter extends IInterface {

    public static abstract class Stub extends Binder implements INfcAdapter {
        private static final String DESCRIPTOR = "android.nfc.INfcAdapter";
        static final int TRANSACTION_addNfcUnlockHandler = 27;
        static final int TRANSACTION_disable = 8;
        static final int TRANSACTION_disableNdefPush = 11;
        static final int TRANSACTION_disablePolling = 5;
        static final int TRANSACTION_dispatch = 24;
        static final int TRANSACTION_enable = 9;
        static final int TRANSACTION_enableNdefPush = 10;
        static final int TRANSACTION_enablePolling = 4;
        static final int TRANSACTION_getFirmwareVersion = 29;
        static final int TRANSACTION_getNfcAdapterExtrasInterface = 3;
        static final int TRANSACTION_getNfcCardEmulationInterface = 2;
        static final int TRANSACTION_getNfcFCardEmulationInterface = 32;
        static final int TRANSACTION_getNfcTagInterface = 1;
        static final int TRANSACTION_getNxpNfcAdapterInterface = 6;
        static final int TRANSACTION_getSelectedCardEmulation = 16;
        static final int TRANSACTION_getState = 7;
        static final int TRANSACTION_getSupportCardEmulation = 18;
        static final int TRANSACTION_ignore = 23;
        static final int TRANSACTION_invokeBeam = 21;
        static final int TRANSACTION_invokeBeamInternal = 22;
        static final int TRANSACTION_is2ndLevelMenuOn = 30;
        static final int TRANSACTION_isNdefPushEnabled = 12;
        static final int TRANSACTION_pausePolling = 13;
        static final int TRANSACTION_removeNfcUnlockHandler = 28;
        static final int TRANSACTION_resumePolling = 14;
        static final int TRANSACTION_selectCardEmulation = 17;
        static final int TRANSACTION_set2ndLevelMenu = 31;
        static final int TRANSACTION_setAppCallback = 20;
        static final int TRANSACTION_setForegroundDispatch = 19;
        static final int TRANSACTION_setP2pModes = 26;
        static final int TRANSACTION_setReaderMode = 25;
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
                    this.mRemote.transact(Stub.TRANSACTION_getNfcTagInterface, _data, _reply, 0);
                    _reply.readException();
                    INfcTag _result = android.nfc.INfcTag.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getNfcCardEmulationInterface, _data, _reply, 0);
                    _reply.readException();
                    INfcCardEmulation _result = android.nfc.INfcCardEmulation.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getNfcAdapterExtrasInterface, _data, _reply, 0);
                    _reply.readException();
                    INfcAdapterExtras _result = android.nfc.INfcAdapterExtras.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_enablePolling, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_disablePolling, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public INxpNfcAdapter getNxpNfcAdapterInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNxpNfcAdapterInterface, _data, _reply, 0);
                    _reply.readException();
                    INxpNfcAdapter _result = com.nxp.nfc.INxpNfcAdapter.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disable(boolean saveState) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (saveState) {
                        i = Stub.TRANSACTION_getNfcTagInterface;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_disable, _data, _reply, 0);
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

            public boolean enable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enable, _data, _reply, 0);
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

            public boolean enableNdefPush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enableNdefPush, _data, _reply, 0);
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

            public boolean disableNdefPush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disableNdefPush, _data, _reply, 0);
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

            public boolean isNdefPushEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isNdefPushEnabled, _data, _reply, 0);
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

            public void pausePolling(int timeoutInMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeoutInMs);
                    this.mRemote.transact(Stub.TRANSACTION_pausePolling, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_resumePolling, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_verifyNfcPermission, _data, _reply, 0);
                    _reply.readException();
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
                    this.mRemote.transact(Stub.TRANSACTION_getSelectedCardEmulation, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_selectCardEmulation, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getSupportCardEmulation, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_getNfcTagInterface);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(filters, 0);
                    if (techLists != null) {
                        _data.writeInt(Stub.TRANSACTION_getNfcTagInterface);
                        techLists.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setForegroundDispatch, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppCallback(IAppCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setAppCallback, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_invokeBeam, _data, null, Stub.TRANSACTION_getNfcTagInterface);
                } finally {
                    _data.recycle();
                }
            }

            public void invokeBeamInternal(BeamShareData shareData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (shareData != null) {
                        _data.writeInt(Stub.TRANSACTION_getNfcTagInterface);
                        shareData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_invokeBeamInternal, _data, null, Stub.TRANSACTION_getNfcTagInterface);
                } finally {
                    _data.recycle();
                }
            }

            public boolean ignore(int nativeHandle, int debounceMs, ITagRemovedCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    _data.writeInt(debounceMs);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_ignore, _data, _reply, 0);
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

            public void dispatch(Tag tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tag != null) {
                        _data.writeInt(Stub.TRANSACTION_getNfcTagInterface);
                        tag.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_dispatch, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setReaderMode(IBinder b, IAppCallback callback, int flags, Bundle extras) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(b);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_getNfcTagInterface);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setReaderMode, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setP2pModes, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addNfcUnlockHandler(INfcUnlockHandler unlockHandler, int[] techList) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (unlockHandler != null) {
                        iBinder = unlockHandler.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeIntArray(techList);
                    this.mRemote.transact(Stub.TRANSACTION_addNfcUnlockHandler, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeNfcUnlockHandler(INfcUnlockHandler unlockHandler) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (unlockHandler != null) {
                        iBinder = unlockHandler.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeNfcUnlockHandler, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getFirmwareVersion, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_is2ndLevelMenuOn, _data, _reply, 0);
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

            public void set2ndLevelMenu(boolean onOff) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (onOff) {
                        i = Stub.TRANSACTION_getNfcTagInterface;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_set2ndLevelMenu, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getNfcFCardEmulationInterface, _data, _reply, 0);
                    _reply.readException();
                    INfcFCardEmulation _result = android.nfc.INfcFCardEmulation.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            boolean _result2;
            switch (code) {
                case TRANSACTION_getNfcTagInterface /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    INfcTag _result3 = getNfcTagInterface();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result3 != null ? _result3.asBinder() : null);
                    return true;
                case TRANSACTION_getNfcCardEmulationInterface /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    INfcCardEmulation _result4 = getNfcCardEmulationInterface();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4 != null ? _result4.asBinder() : null);
                    return true;
                case TRANSACTION_getNfcAdapterExtrasInterface /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    INfcAdapterExtras _result5 = getNfcAdapterExtrasInterface(data.readString());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                    return true;
                case TRANSACTION_enablePolling /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    enablePolling();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disablePolling /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    disablePolling();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getNxpNfcAdapterInterface /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    INxpNfcAdapter _result6 = getNxpNfcAdapterInterface();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result6 != null ? _result6.asBinder() : null);
                    return true;
                case TRANSACTION_getState /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_disable /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = disable(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getNfcTagInterface : 0);
                    return true;
                case TRANSACTION_enable /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = enable();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getNfcTagInterface : 0);
                    return true;
                case TRANSACTION_enableNdefPush /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = enableNdefPush();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getNfcTagInterface : 0);
                    return true;
                case TRANSACTION_disableNdefPush /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = disableNdefPush();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getNfcTagInterface : 0);
                    return true;
                case TRANSACTION_isNdefPushEnabled /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isNdefPushEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getNfcTagInterface : 0);
                    return true;
                case TRANSACTION_pausePolling /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    pausePolling(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resumePolling /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    resumePolling();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_verifyNfcPermission /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    verifyNfcPermission();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSelectedCardEmulation /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSelectedCardEmulation();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_selectCardEmulation /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    selectCardEmulation(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSupportCardEmulation /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSupportCardEmulation();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setForegroundDispatch /*19*/:
                    PendingIntent pendingIntent;
                    TechListParcel techListParcel;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    IntentFilter[] _arg1 = (IntentFilter[]) data.createTypedArray(IntentFilter.CREATOR);
                    if (data.readInt() != 0) {
                        techListParcel = (TechListParcel) TechListParcel.CREATOR.createFromParcel(data);
                    } else {
                        techListParcel = null;
                    }
                    setForegroundDispatch(pendingIntent, _arg1, techListParcel);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAppCallback /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAppCallback(android.nfc.IAppCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_invokeBeam /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    invokeBeam();
                    return true;
                case TRANSACTION_invokeBeamInternal /*22*/:
                    BeamShareData beamShareData;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        beamShareData = (BeamShareData) BeamShareData.CREATOR.createFromParcel(data);
                    } else {
                        beamShareData = null;
                    }
                    invokeBeamInternal(beamShareData);
                    return true;
                case TRANSACTION_ignore /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = ignore(data.readInt(), data.readInt(), android.nfc.ITagRemovedCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getNfcTagInterface : 0);
                    return true;
                case TRANSACTION_dispatch /*24*/:
                    Tag tag;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tag = (Tag) Tag.CREATOR.createFromParcel(data);
                    } else {
                        tag = null;
                    }
                    dispatch(tag);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setReaderMode /*25*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _arg0 = data.readStrongBinder();
                    IAppCallback _arg12 = android.nfc.IAppCallback.Stub.asInterface(data.readStrongBinder());
                    int _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    setReaderMode(_arg0, _arg12, _arg2, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setP2pModes /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    setP2pModes(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addNfcUnlockHandler /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    addNfcUnlockHandler(android.nfc.INfcUnlockHandler.Stub.asInterface(data.readStrongBinder()), data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeNfcUnlockHandler /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeNfcUnlockHandler(android.nfc.INfcUnlockHandler.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getFirmwareVersion /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result7 = getFirmwareVersion();
                    reply.writeNoException();
                    reply.writeString(_result7);
                    return true;
                case TRANSACTION_is2ndLevelMenuOn /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = is2ndLevelMenuOn();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getNfcTagInterface : 0);
                    return true;
                case TRANSACTION_set2ndLevelMenu /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    set2ndLevelMenu(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getNfcFCardEmulationInterface /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    INfcFCardEmulation _result8 = getNfcFCardEmulationInterface();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result8 != null ? _result8.asBinder() : null);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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

    INfcCardEmulation getNfcCardEmulationInterface() throws RemoteException;

    INfcFCardEmulation getNfcFCardEmulationInterface() throws RemoteException;

    INfcTag getNfcTagInterface() throws RemoteException;

    INxpNfcAdapter getNxpNfcAdapterInterface() throws RemoteException;

    int getSelectedCardEmulation() throws RemoteException;

    int getState() throws RemoteException;

    int getSupportCardEmulation() throws RemoteException;

    boolean ignore(int i, int i2, ITagRemovedCallback iTagRemovedCallback) throws RemoteException;

    void invokeBeam() throws RemoteException;

    void invokeBeamInternal(BeamShareData beamShareData) throws RemoteException;

    boolean is2ndLevelMenuOn() throws RemoteException;

    boolean isNdefPushEnabled() throws RemoteException;

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
