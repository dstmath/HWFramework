package android.hardware.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;
import android.hardware.hdmi.IHdmiDeviceEventListener;
import android.hardware.hdmi.IHdmiHotplugEventListener;
import android.hardware.hdmi.IHdmiInputChangeListener;
import android.hardware.hdmi.IHdmiMhlVendorCommandListener;
import android.hardware.hdmi.IHdmiRecordListener;
import android.hardware.hdmi.IHdmiSystemAudioModeChangeListener;
import android.hardware.hdmi.IHdmiVendorCommandListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHdmiControlService extends IInterface {
    void addDeviceEventListener(IHdmiDeviceEventListener iHdmiDeviceEventListener) throws RemoteException;

    void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener iHdmiMhlVendorCommandListener) throws RemoteException;

    void addHotplugEventListener(IHdmiHotplugEventListener iHdmiHotplugEventListener) throws RemoteException;

    void addSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener iHdmiSystemAudioModeChangeListener) throws RemoteException;

    void addVendorCommandListener(IHdmiVendorCommandListener iHdmiVendorCommandListener, int i) throws RemoteException;

    void askRemoteDeviceToBecomeActiveSource(int i) throws RemoteException;

    boolean canChangeSystemAudioMode() throws RemoteException;

    void clearTimerRecording(int i, int i2, byte[] bArr) throws RemoteException;

    void deviceSelect(int i, IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    HdmiDeviceInfo getActiveSource() throws RemoteException;

    List<HdmiDeviceInfo> getDeviceList() throws RemoteException;

    List<HdmiDeviceInfo> getInputDevices() throws RemoteException;

    int getPhysicalAddress() throws RemoteException;

    List<HdmiPortInfo> getPortInfo() throws RemoteException;

    int[] getSupportedTypes() throws RemoteException;

    boolean getSystemAudioMode() throws RemoteException;

    void oneTouchPlay(IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    void portSelect(int i, IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    void powerOffRemoteDevice(int i, int i2) throws RemoteException;

    void powerOnRemoteDevice(int i, int i2) throws RemoteException;

    void queryDisplayStatus(IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    void removeHotplugEventListener(IHdmiHotplugEventListener iHdmiHotplugEventListener) throws RemoteException;

    void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener iHdmiSystemAudioModeChangeListener) throws RemoteException;

    void reportAudioStatus(int i, int i2, int i3, boolean z) throws RemoteException;

    void sendKeyEvent(int i, int i2, boolean z) throws RemoteException;

    void sendMhlVendorCommand(int i, int i2, int i3, byte[] bArr) throws RemoteException;

    void sendStandby(int i, int i2) throws RemoteException;

    void sendVendorCommand(int i, int i2, byte[] bArr, boolean z) throws RemoteException;

    void sendVolumeKeyEvent(int i, int i2, boolean z) throws RemoteException;

    void setArcMode(boolean z) throws RemoteException;

    void setHdmiRecordListener(IHdmiRecordListener iHdmiRecordListener) throws RemoteException;

    void setInputChangeListener(IHdmiInputChangeListener iHdmiInputChangeListener) throws RemoteException;

    void setProhibitMode(boolean z) throws RemoteException;

    void setStandbyMode(boolean z) throws RemoteException;

    void setSystemAudioMode(boolean z, IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    void setSystemAudioModeOnForAudioOnlySource() throws RemoteException;

    void setSystemAudioMute(boolean z) throws RemoteException;

    void setSystemAudioVolume(int i, int i2, int i3) throws RemoteException;

    void startOneTouchRecord(int i, byte[] bArr) throws RemoteException;

    void startTimerRecording(int i, int i2, byte[] bArr) throws RemoteException;

    void stopOneTouchRecord(int i) throws RemoteException;

    public static class Default implements IHdmiControlService {
        @Override // android.hardware.hdmi.IHdmiControlService
        public int[] getSupportedTypes() throws RemoteException {
            return null;
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public HdmiDeviceInfo getActiveSource() throws RemoteException {
            return null;
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void oneTouchPlay(IHdmiControlCallback callback) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void queryDisplayStatus(IHdmiControlCallback callback) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void addHotplugEventListener(IHdmiHotplugEventListener listener) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void removeHotplugEventListener(IHdmiHotplugEventListener listener) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void addDeviceEventListener(IHdmiDeviceEventListener listener) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void deviceSelect(int deviceId, IHdmiControlCallback callback) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void portSelect(int portId, IHdmiControlCallback callback) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void sendKeyEvent(int deviceType, int keyCode, boolean isPressed) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void sendVolumeKeyEvent(int deviceType, int keyCode, boolean isPressed) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public List<HdmiPortInfo> getPortInfo() throws RemoteException {
            return null;
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public boolean canChangeSystemAudioMode() throws RemoteException {
            return false;
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public boolean getSystemAudioMode() throws RemoteException {
            return false;
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public int getPhysicalAddress() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void setSystemAudioMode(boolean enabled, IHdmiControlCallback callback) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void addSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void setArcMode(boolean enabled) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void setProhibitMode(boolean enabled) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void setSystemAudioVolume(int oldIndex, int newIndex, int maxIndex) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void setSystemAudioMute(boolean mute) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void setInputChangeListener(IHdmiInputChangeListener listener) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public List<HdmiDeviceInfo> getInputDevices() throws RemoteException {
            return null;
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public List<HdmiDeviceInfo> getDeviceList() throws RemoteException {
            return null;
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void powerOffRemoteDevice(int logicalAddress, int powerStatus) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void powerOnRemoteDevice(int logicalAddress, int powerStatus) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void askRemoteDeviceToBecomeActiveSource(int physicalAddress) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void sendVendorCommand(int deviceType, int targetAddress, byte[] params, boolean hasVendorId) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void addVendorCommandListener(IHdmiVendorCommandListener listener, int deviceType) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void sendStandby(int deviceType, int deviceId) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void setHdmiRecordListener(IHdmiRecordListener callback) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void startOneTouchRecord(int recorderAddress, byte[] recordSource) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void stopOneTouchRecord(int recorderAddress) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void startTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void clearTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void sendMhlVendorCommand(int portId, int offset, int length, byte[] data) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener listener) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void setStandbyMode(boolean isStandbyModeOn) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void reportAudioStatus(int deviceType, int volume, int maxVolume, boolean isMute) throws RemoteException {
        }

        @Override // android.hardware.hdmi.IHdmiControlService
        public void setSystemAudioModeOnForAudioOnlySource() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHdmiControlService {
        private static final String DESCRIPTOR = "android.hardware.hdmi.IHdmiControlService";
        static final int TRANSACTION_addDeviceEventListener = 7;
        static final int TRANSACTION_addHdmiMhlVendorCommandListener = 38;
        static final int TRANSACTION_addHotplugEventListener = 5;
        static final int TRANSACTION_addSystemAudioModeChangeListener = 17;
        static final int TRANSACTION_addVendorCommandListener = 30;
        static final int TRANSACTION_askRemoteDeviceToBecomeActiveSource = 28;
        static final int TRANSACTION_canChangeSystemAudioMode = 13;
        static final int TRANSACTION_clearTimerRecording = 36;
        static final int TRANSACTION_deviceSelect = 8;
        static final int TRANSACTION_getActiveSource = 2;
        static final int TRANSACTION_getDeviceList = 25;
        static final int TRANSACTION_getInputDevices = 24;
        static final int TRANSACTION_getPhysicalAddress = 15;
        static final int TRANSACTION_getPortInfo = 12;
        static final int TRANSACTION_getSupportedTypes = 1;
        static final int TRANSACTION_getSystemAudioMode = 14;
        static final int TRANSACTION_oneTouchPlay = 3;
        static final int TRANSACTION_portSelect = 9;
        static final int TRANSACTION_powerOffRemoteDevice = 26;
        static final int TRANSACTION_powerOnRemoteDevice = 27;
        static final int TRANSACTION_queryDisplayStatus = 4;
        static final int TRANSACTION_removeHotplugEventListener = 6;
        static final int TRANSACTION_removeSystemAudioModeChangeListener = 18;
        static final int TRANSACTION_reportAudioStatus = 40;
        static final int TRANSACTION_sendKeyEvent = 10;
        static final int TRANSACTION_sendMhlVendorCommand = 37;
        static final int TRANSACTION_sendStandby = 31;
        static final int TRANSACTION_sendVendorCommand = 29;
        static final int TRANSACTION_sendVolumeKeyEvent = 11;
        static final int TRANSACTION_setArcMode = 19;
        static final int TRANSACTION_setHdmiRecordListener = 32;
        static final int TRANSACTION_setInputChangeListener = 23;
        static final int TRANSACTION_setProhibitMode = 20;
        static final int TRANSACTION_setStandbyMode = 39;
        static final int TRANSACTION_setSystemAudioMode = 16;
        static final int TRANSACTION_setSystemAudioModeOnForAudioOnlySource = 41;
        static final int TRANSACTION_setSystemAudioMute = 22;
        static final int TRANSACTION_setSystemAudioVolume = 21;
        static final int TRANSACTION_startOneTouchRecord = 33;
        static final int TRANSACTION_startTimerRecording = 35;
        static final int TRANSACTION_stopOneTouchRecord = 34;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHdmiControlService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHdmiControlService)) {
                return new Proxy(obj);
            }
            return (IHdmiControlService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getSupportedTypes";
                case 2:
                    return "getActiveSource";
                case 3:
                    return "oneTouchPlay";
                case 4:
                    return "queryDisplayStatus";
                case 5:
                    return "addHotplugEventListener";
                case 6:
                    return "removeHotplugEventListener";
                case 7:
                    return "addDeviceEventListener";
                case 8:
                    return "deviceSelect";
                case 9:
                    return "portSelect";
                case 10:
                    return "sendKeyEvent";
                case 11:
                    return "sendVolumeKeyEvent";
                case 12:
                    return "getPortInfo";
                case 13:
                    return "canChangeSystemAudioMode";
                case 14:
                    return "getSystemAudioMode";
                case 15:
                    return "getPhysicalAddress";
                case 16:
                    return "setSystemAudioMode";
                case 17:
                    return "addSystemAudioModeChangeListener";
                case 18:
                    return "removeSystemAudioModeChangeListener";
                case 19:
                    return "setArcMode";
                case 20:
                    return "setProhibitMode";
                case 21:
                    return "setSystemAudioVolume";
                case 22:
                    return "setSystemAudioMute";
                case 23:
                    return "setInputChangeListener";
                case 24:
                    return "getInputDevices";
                case 25:
                    return "getDeviceList";
                case 26:
                    return "powerOffRemoteDevice";
                case 27:
                    return "powerOnRemoteDevice";
                case 28:
                    return "askRemoteDeviceToBecomeActiveSource";
                case 29:
                    return "sendVendorCommand";
                case 30:
                    return "addVendorCommandListener";
                case 31:
                    return "sendStandby";
                case 32:
                    return "setHdmiRecordListener";
                case 33:
                    return "startOneTouchRecord";
                case 34:
                    return "stopOneTouchRecord";
                case 35:
                    return "startTimerRecording";
                case 36:
                    return "clearTimerRecording";
                case 37:
                    return "sendMhlVendorCommand";
                case 38:
                    return "addHdmiMhlVendorCommandListener";
                case 39:
                    return "setStandbyMode";
                case 40:
                    return "reportAudioStatus";
                case 41:
                    return "setSystemAudioModeOnForAudioOnlySource";
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
                boolean _arg3 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result = getSupportedTypes();
                        reply.writeNoException();
                        reply.writeIntArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        HdmiDeviceInfo _result2 = getActiveSource();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        oneTouchPlay(IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        queryDisplayStatus(IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        addHotplugEventListener(IHdmiHotplugEventListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        removeHotplugEventListener(IHdmiHotplugEventListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        addDeviceEventListener(IHdmiDeviceEventListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        deviceSelect(data.readInt(), IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        portSelect(data.readInt(), IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        sendKeyEvent(_arg0, _arg1, _arg3);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        sendVolumeKeyEvent(_arg02, _arg12, _arg3);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        List<HdmiPortInfo> _result3 = getPortInfo();
                        reply.writeNoException();
                        reply.writeTypedList(_result3);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean canChangeSystemAudioMode = canChangeSystemAudioMode();
                        reply.writeNoException();
                        reply.writeInt(canChangeSystemAudioMode ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean systemAudioMode = getSystemAudioMode();
                        reply.writeNoException();
                        reply.writeInt(systemAudioMode ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getPhysicalAddress();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        setSystemAudioMode(_arg3, IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        addSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        setArcMode(_arg3);
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        setProhibitMode(_arg3);
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        setSystemAudioVolume(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        setSystemAudioMute(_arg3);
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        setInputChangeListener(IHdmiInputChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        List<HdmiDeviceInfo> _result5 = getInputDevices();
                        reply.writeNoException();
                        reply.writeTypedList(_result5);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        List<HdmiDeviceInfo> _result6 = getDeviceList();
                        reply.writeNoException();
                        reply.writeTypedList(_result6);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        powerOffRemoteDevice(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        powerOnRemoteDevice(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        askRemoteDeviceToBecomeActiveSource(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg13 = data.readInt();
                        byte[] _arg2 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        sendVendorCommand(_arg03, _arg13, _arg2, _arg3);
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        addVendorCommandListener(IHdmiVendorCommandListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        sendStandby(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        setHdmiRecordListener(IHdmiRecordListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        startOneTouchRecord(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        stopOneTouchRecord(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        startTimerRecording(data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        clearTimerRecording(data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        sendMhlVendorCommand(data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        setStandbyMode(_arg3);
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        int _arg14 = data.readInt();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        reportAudioStatus(_arg04, _arg14, _arg22, _arg3);
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        setSystemAudioModeOnForAudioOnlySource();
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
        public static class Proxy implements IHdmiControlService {
            public static IHdmiControlService sDefaultImpl;
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

            @Override // android.hardware.hdmi.IHdmiControlService
            public int[] getSupportedTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportedTypes();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public HdmiDeviceInfo getActiveSource() throws RemoteException {
                HdmiDeviceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveSource();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HdmiDeviceInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.hardware.hdmi.IHdmiControlService
            public void oneTouchPlay(IHdmiControlCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().oneTouchPlay(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void queryDisplayStatus(IHdmiControlCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().queryDisplayStatus(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void addHotplugEventListener(IHdmiHotplugEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addHotplugEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void removeHotplugEventListener(IHdmiHotplugEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeHotplugEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void addDeviceEventListener(IHdmiDeviceEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDeviceEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void deviceSelect(int deviceId, IHdmiControlCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deviceSelect(deviceId, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void portSelect(int portId, IHdmiControlCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(portId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().portSelect(portId, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void sendKeyEvent(int deviceType, int keyCode, boolean isPressed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(keyCode);
                    _data.writeInt(isPressed ? 1 : 0);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendKeyEvent(deviceType, keyCode, isPressed);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void sendVolumeKeyEvent(int deviceType, int keyCode, boolean isPressed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(keyCode);
                    _data.writeInt(isPressed ? 1 : 0);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendVolumeKeyEvent(deviceType, keyCode, isPressed);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public List<HdmiPortInfo> getPortInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPortInfo();
                    }
                    _reply.readException();
                    List<HdmiPortInfo> _result = _reply.createTypedArrayList(HdmiPortInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public boolean canChangeSystemAudioMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canChangeSystemAudioMode();
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

            @Override // android.hardware.hdmi.IHdmiControlService
            public boolean getSystemAudioMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemAudioMode();
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

            @Override // android.hardware.hdmi.IHdmiControlService
            public int getPhysicalAddress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPhysicalAddress();
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

            @Override // android.hardware.hdmi.IHdmiControlService
            public void setSystemAudioMode(boolean enabled, IHdmiControlCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSystemAudioMode(enabled, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void addSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addSystemAudioModeChangeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeSystemAudioModeChangeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void setArcMode(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setArcMode(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void setProhibitMode(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProhibitMode(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void setSystemAudioVolume(int oldIndex, int newIndex, int maxIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(oldIndex);
                    _data.writeInt(newIndex);
                    _data.writeInt(maxIndex);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSystemAudioVolume(oldIndex, newIndex, maxIndex);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void setSystemAudioMute(boolean mute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mute ? 1 : 0);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSystemAudioMute(mute);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void setInputChangeListener(IHdmiInputChangeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInputChangeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public List<HdmiDeviceInfo> getInputDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInputDevices();
                    }
                    _reply.readException();
                    List<HdmiDeviceInfo> _result = _reply.createTypedArrayList(HdmiDeviceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public List<HdmiDeviceInfo> getDeviceList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceList();
                    }
                    _reply.readException();
                    List<HdmiDeviceInfo> _result = _reply.createTypedArrayList(HdmiDeviceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void powerOffRemoteDevice(int logicalAddress, int powerStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(logicalAddress);
                    _data.writeInt(powerStatus);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().powerOffRemoteDevice(logicalAddress, powerStatus);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void powerOnRemoteDevice(int logicalAddress, int powerStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(logicalAddress);
                    _data.writeInt(powerStatus);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().powerOnRemoteDevice(logicalAddress, powerStatus);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void askRemoteDeviceToBecomeActiveSource(int physicalAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(physicalAddress);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().askRemoteDeviceToBecomeActiveSource(physicalAddress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void sendVendorCommand(int deviceType, int targetAddress, byte[] params, boolean hasVendorId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(targetAddress);
                    _data.writeByteArray(params);
                    _data.writeInt(hasVendorId ? 1 : 0);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendVendorCommand(deviceType, targetAddress, params, hasVendorId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void addVendorCommandListener(IHdmiVendorCommandListener listener, int deviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(deviceType);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addVendorCommandListener(listener, deviceType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void sendStandby(int deviceType, int deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(deviceId);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendStandby(deviceType, deviceId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void setHdmiRecordListener(IHdmiRecordListener callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHdmiRecordListener(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void startOneTouchRecord(int recorderAddress, byte[] recordSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeByteArray(recordSource);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startOneTouchRecord(recorderAddress, recordSource);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void stopOneTouchRecord(int recorderAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopOneTouchRecord(recorderAddress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void startTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(sourceType);
                    _data.writeByteArray(recordSource);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startTimerRecording(recorderAddress, sourceType, recordSource);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void clearTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(sourceType);
                    _data.writeByteArray(recordSource);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearTimerRecording(recorderAddress, sourceType, recordSource);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void sendMhlVendorCommand(int portId, int offset, int length, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(portId);
                    _data.writeInt(offset);
                    _data.writeInt(length);
                    _data.writeByteArray(data);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendMhlVendorCommand(portId, offset, length, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(38, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addHdmiMhlVendorCommandListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void setStandbyMode(boolean isStandbyModeOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isStandbyModeOn ? 1 : 0);
                    if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStandbyMode(isStandbyModeOn);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void reportAudioStatus(int deviceType, int volume, int maxVolume, boolean isMute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(volume);
                    _data.writeInt(maxVolume);
                    _data.writeInt(isMute ? 1 : 0);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportAudioStatus(deviceType, volume, maxVolume, isMute);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.hdmi.IHdmiControlService
            public void setSystemAudioModeOnForAudioOnlySource() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSystemAudioModeOnForAudioOnlySource();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHdmiControlService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHdmiControlService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
