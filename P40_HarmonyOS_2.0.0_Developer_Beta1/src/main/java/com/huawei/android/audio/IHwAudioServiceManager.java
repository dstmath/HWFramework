package com.huawei.android.audio;

import android.media.AudioFocusInfo;
import android.media.IAudioFocusChangeDispatcher;
import android.media.IAudioModeDispatcher;
import android.media.IVolumeChangeDispatcher;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IHwAudioServiceManager extends IInterface {
    void checkMicMute() throws RemoteException;

    boolean checkMuteZenMode() throws RemoteException;

    boolean checkRecordActive() throws RemoteException;

    AudioFocusInfo getAudioFocusInfo(String str) throws RemoteException;

    IBinder getDeviceSelectCallback() throws RemoteException;

    int getRecordConcurrentType(String str) throws RemoteException;

    boolean isMultiAudioRecordEnable() throws RemoteException;

    boolean isVoiceRecordingEnable() throws RemoteException;

    boolean registerAudioDeviceSelectCallback(IBinder iBinder) throws RemoteException;

    boolean registerAudioFocusChangeCallback(IAudioFocusChangeDispatcher iAudioFocusChangeDispatcher, String str, String str2) throws RemoteException;

    void registerAudioModeCallback(IAudioModeDispatcher iAudioModeDispatcher) throws RemoteException;

    boolean registerVolumeChangeCallback(IVolumeChangeDispatcher iVolumeChangeDispatcher, String str, String str2) throws RemoteException;

    int removeVirtualAudio(String str, String str2, int i, Map map) throws RemoteException;

    void sendRecordStateChangedIntent(String str, int i, int i2, String str2) throws RemoteException;

    void setBluetoothScoState(int i, int i2) throws RemoteException;

    void setBtScoForRecord(boolean z) throws RemoteException;

    boolean setFmDeviceAvailable(int i) throws RemoteException;

    void setHistenNaturalMode(boolean z, IBinder iBinder) throws RemoteException;

    void setMultiAudioRecordEnable(boolean z) throws RemoteException;

    int setSoundEffectState(boolean z, String str, boolean z2, String str2) throws RemoteException;

    void setVoiceRecordingEnable(boolean z) throws RemoteException;

    boolean setVolumeByPidStream(int i, int i2, float f, IBinder iBinder) throws RemoteException;

    int startVirtualAudio(String str, String str2, int i, Map map) throws RemoteException;

    boolean unregisterAudioDeviceSelectCallback(IBinder iBinder) throws RemoteException;

    boolean unregisterAudioFocusChangeCallback(IAudioFocusChangeDispatcher iAudioFocusChangeDispatcher, String str, String str2) throws RemoteException;

    void unregisterAudioModeCallback(IAudioModeDispatcher iAudioModeDispatcher) throws RemoteException;

    boolean unregisterVolumeChangeCallback(IVolumeChangeDispatcher iVolumeChangeDispatcher, String str, String str2) throws RemoteException;

    public static class Default implements IHwAudioServiceManager {
        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public IBinder getDeviceSelectCallback() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public void setBluetoothScoState(int state, int sessionId) throws RemoteException {
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public int startVirtualAudio(String deviceId, String serviceId, int serviceType, Map dataMap) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public int removeVirtualAudio(String deviceId, String serviceId, int serviceType, Map dataMap) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean checkRecordActive() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public void checkMicMute() throws RemoteException {
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public void sendRecordStateChangedIntent(String sender, int state, int pid, String packageName) throws RemoteException {
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public int getRecordConcurrentType(String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean checkMuteZenMode() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public void registerAudioModeCallback(IAudioModeDispatcher pcdb) throws RemoteException {
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public void unregisterAudioModeCallback(IAudioModeDispatcher pcdb) throws RemoteException {
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean registerAudioDeviceSelectCallback(IBinder cb) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean unregisterAudioDeviceSelectCallback(IBinder cb) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean registerAudioFocusChangeCallback(IAudioFocusChangeDispatcher cb, String cbs, String pkgName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean unregisterAudioFocusChangeCallback(IAudioFocusChangeDispatcher cb, String cbs, String pkgName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public AudioFocusInfo getAudioFocusInfo(String pkgName) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean setFmDeviceAvailable(int state) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public void setBtScoForRecord(boolean on) throws RemoteException {
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean registerVolumeChangeCallback(IVolumeChangeDispatcher cb, String cbs, String pkgName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean unregisterVolumeChangeCallback(IVolumeChangeDispatcher cb, String cbs, String pkgName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public void setHistenNaturalMode(boolean on, IBinder cb) throws RemoteException {
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public void setMultiAudioRecordEnable(boolean enable) throws RemoteException {
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean isMultiAudioRecordEnable() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public void setVoiceRecordingEnable(boolean enable) throws RemoteException {
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean isVoiceRecordingEnable() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.audio.IHwAudioServiceManager
        public boolean setVolumeByPidStream(int pid, int streamType, float volume, IBinder cb) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAudioServiceManager {
        private static final String DESCRIPTOR = "com.huawei.android.audio.IHwAudioServiceManager";
        static final int TRANSACTION_checkMicMute = 7;
        static final int TRANSACTION_checkMuteZenMode = 10;
        static final int TRANSACTION_checkRecordActive = 6;
        static final int TRANSACTION_getAudioFocusInfo = 17;
        static final int TRANSACTION_getDeviceSelectCallback = 1;
        static final int TRANSACTION_getRecordConcurrentType = 9;
        static final int TRANSACTION_isMultiAudioRecordEnable = 24;
        static final int TRANSACTION_isVoiceRecordingEnable = 26;
        static final int TRANSACTION_registerAudioDeviceSelectCallback = 13;
        static final int TRANSACTION_registerAudioFocusChangeCallback = 15;
        static final int TRANSACTION_registerAudioModeCallback = 11;
        static final int TRANSACTION_registerVolumeChangeCallback = 20;
        static final int TRANSACTION_removeVirtualAudio = 5;
        static final int TRANSACTION_sendRecordStateChangedIntent = 8;
        static final int TRANSACTION_setBluetoothScoState = 2;
        static final int TRANSACTION_setBtScoForRecord = 19;
        static final int TRANSACTION_setFmDeviceAvailable = 18;
        static final int TRANSACTION_setHistenNaturalMode = 22;
        static final int TRANSACTION_setMultiAudioRecordEnable = 23;
        static final int TRANSACTION_setSoundEffectState = 3;
        static final int TRANSACTION_setVoiceRecordingEnable = 25;
        static final int TRANSACTION_setVolumeByPidStream = 27;
        static final int TRANSACTION_startVirtualAudio = 4;
        static final int TRANSACTION_unregisterAudioDeviceSelectCallback = 14;
        static final int TRANSACTION_unregisterAudioFocusChangeCallback = 16;
        static final int TRANSACTION_unregisterAudioModeCallback = 12;
        static final int TRANSACTION_unregisterVolumeChangeCallback = 21;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAudioServiceManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAudioServiceManager)) {
                return new Proxy(obj);
            }
            return (IHwAudioServiceManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getDeviceSelectCallback";
                case 2:
                    return "setBluetoothScoState";
                case 3:
                    return "setSoundEffectState";
                case 4:
                    return "startVirtualAudio";
                case 5:
                    return "removeVirtualAudio";
                case 6:
                    return "checkRecordActive";
                case 7:
                    return "checkMicMute";
                case 8:
                    return "sendRecordStateChangedIntent";
                case 9:
                    return "getRecordConcurrentType";
                case 10:
                    return "checkMuteZenMode";
                case 11:
                    return "registerAudioModeCallback";
                case 12:
                    return "unregisterAudioModeCallback";
                case 13:
                    return "registerAudioDeviceSelectCallback";
                case 14:
                    return "unregisterAudioDeviceSelectCallback";
                case 15:
                    return "registerAudioFocusChangeCallback";
                case 16:
                    return "unregisterAudioFocusChangeCallback";
                case 17:
                    return "getAudioFocusInfo";
                case 18:
                    return "setFmDeviceAvailable";
                case 19:
                    return "setBtScoForRecord";
                case 20:
                    return "registerVolumeChangeCallback";
                case 21:
                    return "unregisterVolumeChangeCallback";
                case 22:
                    return "setHistenNaturalMode";
                case 23:
                    return "setMultiAudioRecordEnable";
                case 24:
                    return "isMultiAudioRecordEnable";
                case 25:
                    return "setVoiceRecordingEnable";
                case 26:
                    return "isVoiceRecordingEnable";
                case 27:
                    return "setVolumeByPidStream";
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
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result = getDeviceSelectCallback();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setBluetoothScoState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg02 = data.readInt() != 0;
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        int _result2 = setSoundEffectState(_arg02, _arg1, _arg0, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = startVirtualAudio(data.readString(), data.readString(), data.readInt(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = removeVirtualAudio(data.readString(), data.readString(), data.readInt(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkRecordActive = checkRecordActive();
                        reply.writeNoException();
                        reply.writeInt(checkRecordActive ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        checkMicMute();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        sendRecordStateChangedIntent(data.readString(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getRecordConcurrentType(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkMuteZenMode = checkMuteZenMode();
                        reply.writeNoException();
                        reply.writeInt(checkMuteZenMode ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        registerAudioModeCallback(IAudioModeDispatcher.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterAudioModeCallback(IAudioModeDispatcher.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerAudioDeviceSelectCallback = registerAudioDeviceSelectCallback(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(registerAudioDeviceSelectCallback ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterAudioDeviceSelectCallback = unregisterAudioDeviceSelectCallback(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(unregisterAudioDeviceSelectCallback ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerAudioFocusChangeCallback = registerAudioFocusChangeCallback(IAudioFocusChangeDispatcher.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(registerAudioFocusChangeCallback ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterAudioFocusChangeCallback = unregisterAudioFocusChangeCallback(IAudioFocusChangeDispatcher.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(unregisterAudioFocusChangeCallback ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        AudioFocusInfo _result6 = getAudioFocusInfo(data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean fmDeviceAvailable = setFmDeviceAvailable(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(fmDeviceAvailable ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setBtScoForRecord(_arg0);
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerVolumeChangeCallback = registerVolumeChangeCallback(IVolumeChangeDispatcher.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(registerVolumeChangeCallback ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterVolumeChangeCallback = unregisterVolumeChangeCallback(IVolumeChangeDispatcher.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(unregisterVolumeChangeCallback ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setHistenNaturalMode(_arg0, data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setMultiAudioRecordEnable(_arg0);
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isMultiAudioRecordEnable = isMultiAudioRecordEnable();
                        reply.writeNoException();
                        reply.writeInt(isMultiAudioRecordEnable ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setVoiceRecordingEnable(_arg0);
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVoiceRecordingEnable = isVoiceRecordingEnable();
                        reply.writeNoException();
                        reply.writeInt(isVoiceRecordingEnable ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        boolean volumeByPidStream = setVolumeByPidStream(data.readInt(), data.readInt(), data.readFloat(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(volumeByPidStream ? 1 : 0);
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
        public static class Proxy implements IHwAudioServiceManager {
            public static IHwAudioServiceManager sDefaultImpl;
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public IBinder getDeviceSelectCallback() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceSelectCallback();
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public void setBluetoothScoState(int state, int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeInt(sessionId);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBluetoothScoState(state, sessionId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    _data.writeInt(restore ? 1 : 0);
                    _data.writeString(packageName);
                    if (!isOnTop) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(reserved);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSoundEffectState(restore, packageName, isOnTop, reserved);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public int startVirtualAudio(String deviceId, String serviceId, int serviceType, Map dataMap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(serviceId);
                    _data.writeInt(serviceType);
                    _data.writeMap(dataMap);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startVirtualAudio(deviceId, serviceId, serviceType, dataMap);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public int removeVirtualAudio(String deviceId, String serviceId, int serviceType, Map dataMap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(serviceId);
                    _data.writeInt(serviceType);
                    _data.writeMap(dataMap);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeVirtualAudio(deviceId, serviceId, serviceType, dataMap);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean checkRecordActive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkRecordActive();
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public void checkMicMute() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().checkMicMute();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public void sendRecordStateChangedIntent(String sender, int state, int pid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sender);
                    _data.writeInt(state);
                    _data.writeInt(pid);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendRecordStateChangedIntent(sender, state, pid, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public int getRecordConcurrentType(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRecordConcurrentType(packageName);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean checkMuteZenMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkMuteZenMode();
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public void registerAudioModeCallback(IAudioModeDispatcher pcdb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(pcdb != null ? pcdb.asBinder() : null);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerAudioModeCallback(pcdb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public void unregisterAudioModeCallback(IAudioModeDispatcher pcdb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(pcdb != null ? pcdb.asBinder() : null);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterAudioModeCallback(pcdb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean registerAudioDeviceSelectCallback(IBinder cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerAudioDeviceSelectCallback(cb);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean unregisterAudioDeviceSelectCallback(IBinder cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterAudioDeviceSelectCallback(cb);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean registerAudioFocusChangeCallback(IAudioFocusChangeDispatcher cb, String cbs, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeString(cbs);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerAudioFocusChangeCallback(cb, cbs, pkgName);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean unregisterAudioFocusChangeCallback(IAudioFocusChangeDispatcher cb, String cbs, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeString(cbs);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterAudioFocusChangeCallback(cb, cbs, pkgName);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public AudioFocusInfo getAudioFocusInfo(String pkgName) throws RemoteException {
                AudioFocusInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAudioFocusInfo(pkgName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AudioFocusInfo.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean setFmDeviceAvailable(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setFmDeviceAvailable(state);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public void setBtScoForRecord(boolean on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(on ? 1 : 0);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBtScoForRecord(on);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean registerVolumeChangeCallback(IVolumeChangeDispatcher cb, String cbs, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeString(cbs);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerVolumeChangeCallback(cb, cbs, pkgName);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean unregisterVolumeChangeCallback(IVolumeChangeDispatcher cb, String cbs, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeString(cbs);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterVolumeChangeCallback(cb, cbs, pkgName);
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public void setHistenNaturalMode(boolean on, IBinder cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(on ? 1 : 0);
                    _data.writeStrongBinder(cb);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHistenNaturalMode(on, cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public void setMultiAudioRecordEnable(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMultiAudioRecordEnable(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean isMultiAudioRecordEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isMultiAudioRecordEnable();
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public void setVoiceRecordingEnable(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoiceRecordingEnable(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean isVoiceRecordingEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVoiceRecordingEnable();
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

            @Override // com.huawei.android.audio.IHwAudioServiceManager
            public boolean setVolumeByPidStream(int pid, int streamType, float volume, IBinder cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(streamType);
                    _data.writeFloat(volume);
                    _data.writeStrongBinder(cb);
                    boolean _result = false;
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setVolumeByPidStream(pid, streamType, volume, cb);
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

        public static boolean setDefaultImpl(IHwAudioServiceManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAudioServiceManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
