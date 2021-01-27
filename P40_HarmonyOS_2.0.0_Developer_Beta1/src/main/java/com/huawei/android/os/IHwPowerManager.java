package com.huawei.android.os;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.os.IHwPowerDAMonitorCallback;
import com.huawei.android.os.IScreenStateCallback;
import java.util.List;

public interface IHwPowerManager extends IInterface {
    int getDisplayPanelType() throws RemoteException;

    int getHwBrightnessData(String str, Bundle bundle) throws RemoteException;

    String getSmartChargeState(String str) throws RemoteException;

    List<String> getWakeLockPackageName() throws RemoteException;

    boolean registerPowerMonitorCallback(IHwPowerDAMonitorCallback iHwPowerDAMonitorCallback) throws RemoteException;

    boolean registerScreenStateCallback(int i, IScreenStateCallback iScreenStateCallback) throws RemoteException;

    void requestNoUserActivityNotification(int i) throws RemoteException;

    int setAodBrightness(int i) throws RemoteException;

    void setAuthSucceeded() throws RemoteException;

    void setBiometricDetectState(int i) throws RemoteException;

    void setChargeLimit(String str) throws RemoteException;

    int setColorTemperature(int i) throws RemoteException;

    int setHwBrightnessData(String str, Bundle bundle) throws RemoteException;

    void setMirrorLinkPowerStatus(boolean z) throws RemoteException;

    void setPowerState(boolean z) throws RemoteException;

    void setSmartChargeState(String str, String str2) throws RemoteException;

    void startWakeUpReady(long j, String str) throws RemoteException;

    void stopWakeUpReady(long j, boolean z, String str) throws RemoteException;

    boolean unRegisterScreenStateCallback() throws RemoteException;

    int updateRgbGamma(float f, float f2, float f3) throws RemoteException;

    public static class Default implements IHwPowerManager {
        @Override // com.huawei.android.os.IHwPowerManager
        public boolean registerPowerMonitorCallback(IHwPowerDAMonitorCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public void setChargeLimit(String limitValue) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public void setPowerState(boolean state) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public void setMirrorLinkPowerStatus(boolean status) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public void startWakeUpReady(long eventTime, String opPackageName) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public void stopWakeUpReady(long eventTime, boolean enableBright, String opPackageName) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public void setAuthSucceeded() throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public int getDisplayPanelType() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public void requestNoUserActivityNotification(int timeout) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public int setColorTemperature(int colorTemper) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public int updateRgbGamma(float red, float green, float blue) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public List<String> getWakeLockPackageName() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public boolean registerScreenStateCallback(int remainTime, IScreenStateCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public boolean unRegisterScreenStateCallback() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public void setSmartChargeState(String scene, String value) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public String getSmartChargeState(String scene) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public int setHwBrightnessData(String name, Bundle data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public int getHwBrightnessData(String name, Bundle data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public void setBiometricDetectState(int state) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerManager
        public int setAodBrightness(int brightness) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwPowerManager {
        private static final String DESCRIPTOR = "com.huawei.android.os.IHwPowerManager";
        static final int TRANSACTION_getDisplayPanelType = 8;
        static final int TRANSACTION_getHwBrightnessData = 18;
        static final int TRANSACTION_getSmartChargeState = 16;
        static final int TRANSACTION_getWakeLockPackageName = 12;
        static final int TRANSACTION_registerPowerMonitorCallback = 1;
        static final int TRANSACTION_registerScreenStateCallback = 13;
        static final int TRANSACTION_requestNoUserActivityNotification = 9;
        static final int TRANSACTION_setAodBrightness = 20;
        static final int TRANSACTION_setAuthSucceeded = 7;
        static final int TRANSACTION_setBiometricDetectState = 19;
        static final int TRANSACTION_setChargeLimit = 2;
        static final int TRANSACTION_setColorTemperature = 10;
        static final int TRANSACTION_setHwBrightnessData = 17;
        static final int TRANSACTION_setMirrorLinkPowerStatus = 4;
        static final int TRANSACTION_setPowerState = 3;
        static final int TRANSACTION_setSmartChargeState = 15;
        static final int TRANSACTION_startWakeUpReady = 5;
        static final int TRANSACTION_stopWakeUpReady = 6;
        static final int TRANSACTION_unRegisterScreenStateCallback = 14;
        static final int TRANSACTION_updateRgbGamma = 11;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwPowerManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwPowerManager)) {
                return new Proxy(obj);
            }
            return (IHwPowerManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "registerPowerMonitorCallback";
                case 2:
                    return "setChargeLimit";
                case 3:
                    return "setPowerState";
                case 4:
                    return "setMirrorLinkPowerStatus";
                case 5:
                    return "startWakeUpReady";
                case 6:
                    return "stopWakeUpReady";
                case 7:
                    return "setAuthSucceeded";
                case 8:
                    return "getDisplayPanelType";
                case 9:
                    return "requestNoUserActivityNotification";
                case 10:
                    return "setColorTemperature";
                case 11:
                    return "updateRgbGamma";
                case 12:
                    return "getWakeLockPackageName";
                case 13:
                    return "registerScreenStateCallback";
                case 14:
                    return "unRegisterScreenStateCallback";
                case 15:
                    return "setSmartChargeState";
                case 16:
                    return "getSmartChargeState";
                case 17:
                    return "setHwBrightnessData";
                case 18:
                    return "getHwBrightnessData";
                case 19:
                    return "setBiometricDetectState";
                case 20:
                    return "setAodBrightness";
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
            Bundle _arg1;
            if (code != 1598968902) {
                boolean _arg12 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerPowerMonitorCallback = registerPowerMonitorCallback(IHwPowerDAMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerPowerMonitorCallback ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setChargeLimit(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setPowerState(_arg12);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setMirrorLinkPowerStatus(_arg12);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        startWakeUpReady(data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg0 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        stopWakeUpReady(_arg0, _arg12, data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setAuthSucceeded();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getDisplayPanelType();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        requestNoUserActivityNotification(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = setColorTemperature(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = updateRgbGamma(data.readFloat(), data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result4 = getWakeLockPackageName();
                        reply.writeNoException();
                        reply.writeStringList(_result4);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerScreenStateCallback = registerScreenStateCallback(data.readInt(), IScreenStateCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerScreenStateCallback ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unRegisterScreenStateCallback = unRegisterScreenStateCallback();
                        reply.writeNoException();
                        reply.writeInt(unRegisterScreenStateCallback ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        setSmartChargeState(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getSmartChargeState(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result6 = setHwBrightnessData(_arg02, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        Bundle _arg13 = new Bundle();
                        int _result7 = getHwBrightnessData(_arg03, _arg13);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        reply.writeInt(1);
                        _arg13.writeToParcel(reply, 1);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        setBiometricDetectState(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = setAodBrightness(data.readInt());
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
        public static class Proxy implements IHwPowerManager {
            public static IHwPowerManager sDefaultImpl;
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

            @Override // com.huawei.android.os.IHwPowerManager
            public boolean registerPowerMonitorCallback(IHwPowerDAMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerPowerMonitorCallback(callback);
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

            @Override // com.huawei.android.os.IHwPowerManager
            public void setChargeLimit(String limitValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(limitValue);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setChargeLimit(limitValue);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public void setPowerState(boolean state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state ? 1 : 0);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPowerState(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public void setMirrorLinkPowerStatus(boolean status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMirrorLinkPowerStatus(status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public void startWakeUpReady(long eventTime, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(eventTime);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startWakeUpReady(eventTime, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public void stopWakeUpReady(long eventTime, boolean enableBright, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(eventTime);
                    _data.writeInt(enableBright ? 1 : 0);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopWakeUpReady(eventTime, enableBright, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public void setAuthSucceeded() throws RemoteException {
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
                    Stub.getDefaultImpl().setAuthSucceeded();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public int getDisplayPanelType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisplayPanelType();
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

            @Override // com.huawei.android.os.IHwPowerManager
            public void requestNoUserActivityNotification(int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeout);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestNoUserActivityNotification(timeout);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public int setColorTemperature(int colorTemper) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(colorTemper);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setColorTemperature(colorTemper);
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

            @Override // com.huawei.android.os.IHwPowerManager
            public int updateRgbGamma(float red, float green, float blue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(red);
                    _data.writeFloat(green);
                    _data.writeFloat(blue);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateRgbGamma(red, green, blue);
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

            @Override // com.huawei.android.os.IHwPowerManager
            public List<String> getWakeLockPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWakeLockPackageName();
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

            @Override // com.huawei.android.os.IHwPowerManager
            public boolean registerScreenStateCallback(int remainTime, IScreenStateCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(remainTime);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerScreenStateCallback(remainTime, callback);
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

            @Override // com.huawei.android.os.IHwPowerManager
            public boolean unRegisterScreenStateCallback() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterScreenStateCallback();
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

            @Override // com.huawei.android.os.IHwPowerManager
            public void setSmartChargeState(String scene, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(scene);
                    _data.writeString(value);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSmartChargeState(scene, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public String getSmartChargeState(String scene) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(scene);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSmartChargeState(scene);
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

            @Override // com.huawei.android.os.IHwPowerManager
            public int setHwBrightnessData(String name, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setHwBrightnessData(name, data);
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

            @Override // com.huawei.android.os.IHwPowerManager
            public int getHwBrightnessData(String name, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwBrightnessData(name, data);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        data.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public void setBiometricDetectState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBiometricDetectState(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerManager
            public int setAodBrightness(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAodBrightness(brightness);
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

        public static boolean setDefaultImpl(IHwPowerManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwPowerManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
