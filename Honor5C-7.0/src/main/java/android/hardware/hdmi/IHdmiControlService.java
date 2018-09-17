package android.hardware.hdmi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHdmiControlService extends IInterface {

    public static abstract class Stub extends Binder implements IHdmiControlService {
        private static final String DESCRIPTOR = "android.hardware.hdmi.IHdmiControlService";
        static final int TRANSACTION_addDeviceEventListener = 7;
        static final int TRANSACTION_addHdmiMhlVendorCommandListener = 33;
        static final int TRANSACTION_addHotplugEventListener = 5;
        static final int TRANSACTION_addSystemAudioModeChangeListener = 15;
        static final int TRANSACTION_addVendorCommandListener = 25;
        static final int TRANSACTION_canChangeSystemAudioMode = 12;
        static final int TRANSACTION_clearTimerRecording = 31;
        static final int TRANSACTION_deviceSelect = 8;
        static final int TRANSACTION_getActiveSource = 2;
        static final int TRANSACTION_getDeviceList = 23;
        static final int TRANSACTION_getInputDevices = 22;
        static final int TRANSACTION_getPortInfo = 11;
        static final int TRANSACTION_getSupportedTypes = 1;
        static final int TRANSACTION_getSystemAudioMode = 13;
        static final int TRANSACTION_oneTouchPlay = 3;
        static final int TRANSACTION_portSelect = 9;
        static final int TRANSACTION_queryDisplayStatus = 4;
        static final int TRANSACTION_removeHotplugEventListener = 6;
        static final int TRANSACTION_removeSystemAudioModeChangeListener = 16;
        static final int TRANSACTION_sendKeyEvent = 10;
        static final int TRANSACTION_sendMhlVendorCommand = 32;
        static final int TRANSACTION_sendStandby = 26;
        static final int TRANSACTION_sendVendorCommand = 24;
        static final int TRANSACTION_setArcMode = 17;
        static final int TRANSACTION_setHdmiRecordListener = 27;
        static final int TRANSACTION_setInputChangeListener = 21;
        static final int TRANSACTION_setProhibitMode = 18;
        static final int TRANSACTION_setSystemAudioMode = 14;
        static final int TRANSACTION_setSystemAudioMute = 20;
        static final int TRANSACTION_setSystemAudioVolume = 19;
        static final int TRANSACTION_startOneTouchRecord = 28;
        static final int TRANSACTION_startTimerRecording = 30;
        static final int TRANSACTION_stopOneTouchRecord = 29;

        private static class Proxy implements IHdmiControlService {
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

            public int[] getSupportedTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSupportedTypes, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HdmiDeviceInfo getActiveSource() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    HdmiDeviceInfo hdmiDeviceInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSource, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        hdmiDeviceInfo = (HdmiDeviceInfo) HdmiDeviceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        hdmiDeviceInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return hdmiDeviceInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void oneTouchPlay(IHdmiControlCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_oneTouchPlay, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void queryDisplayStatus(IHdmiControlCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_queryDisplayStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addHotplugEventListener(IHdmiHotplugEventListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addHotplugEventListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeHotplugEventListener(IHdmiHotplugEventListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeHotplugEventListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addDeviceEventListener(IHdmiDeviceEventListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addDeviceEventListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deviceSelect(int deviceId, IHdmiControlCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_deviceSelect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void portSelect(int portId, IHdmiControlCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(portId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_portSelect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendKeyEvent(int deviceType, int keyCode, boolean isPressed) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(keyCode);
                    if (isPressed) {
                        i = Stub.TRANSACTION_getSupportedTypes;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_sendKeyEvent, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<HdmiPortInfo> getPortInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPortInfo, _data, _reply, 0);
                    _reply.readException();
                    List<HdmiPortInfo> _result = _reply.createTypedArrayList(HdmiPortInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canChangeSystemAudioMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_canChangeSystemAudioMode, _data, _reply, 0);
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

            public boolean getSystemAudioMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSystemAudioMode, _data, _reply, 0);
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

            public void setSystemAudioMode(boolean enabled, IHdmiControlCallback callback) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_getSupportedTypes;
                    }
                    _data.writeInt(i);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setSystemAudioMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addSystemAudioModeChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeSystemAudioModeChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setArcMode(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_getSupportedTypes;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setArcMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setProhibitMode(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_getSupportedTypes;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setProhibitMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSystemAudioVolume(int oldIndex, int newIndex, int maxIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(oldIndex);
                    _data.writeInt(newIndex);
                    _data.writeInt(maxIndex);
                    this.mRemote.transact(Stub.TRANSACTION_setSystemAudioVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSystemAudioMute(boolean mute) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mute) {
                        i = Stub.TRANSACTION_getSupportedTypes;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setSystemAudioMute, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInputChangeListener(IHdmiInputChangeListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setInputChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<HdmiDeviceInfo> getInputDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getInputDevices, _data, _reply, 0);
                    _reply.readException();
                    List<HdmiDeviceInfo> _result = _reply.createTypedArrayList(HdmiDeviceInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<HdmiDeviceInfo> getDeviceList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDeviceList, _data, _reply, 0);
                    _reply.readException();
                    List<HdmiDeviceInfo> _result = _reply.createTypedArrayList(HdmiDeviceInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendVendorCommand(int deviceType, int targetAddress, byte[] params, boolean hasVendorId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(targetAddress);
                    _data.writeByteArray(params);
                    if (hasVendorId) {
                        i = Stub.TRANSACTION_getSupportedTypes;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_sendVendorCommand, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addVendorCommandListener(IHdmiVendorCommandListener listener, int deviceType) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(deviceType);
                    this.mRemote.transact(Stub.TRANSACTION_addVendorCommandListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendStandby(int deviceType, int deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(deviceId);
                    this.mRemote.transact(Stub.TRANSACTION_sendStandby, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHdmiRecordListener(IHdmiRecordListener callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setHdmiRecordListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startOneTouchRecord(int recorderAddress, byte[] recordSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeByteArray(recordSource);
                    this.mRemote.transact(Stub.TRANSACTION_startOneTouchRecord, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopOneTouchRecord(int recorderAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    this.mRemote.transact(Stub.TRANSACTION_stopOneTouchRecord, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(sourceType);
                    _data.writeByteArray(recordSource);
                    this.mRemote.transact(Stub.TRANSACTION_startTimerRecording, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(recorderAddress);
                    _data.writeInt(sourceType);
                    _data.writeByteArray(recordSource);
                    this.mRemote.transact(Stub.TRANSACTION_clearTimerRecording, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendMhlVendorCommand(int portId, int offset, int length, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(portId);
                    _data.writeInt(offset);
                    _data.writeInt(length);
                    _data.writeByteArray(data);
                    this.mRemote.transact(Stub.TRANSACTION_sendMhlVendorCommand, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addHdmiMhlVendorCommandListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            List<HdmiDeviceInfo> _result2;
            switch (code) {
                case TRANSACTION_getSupportedTypes /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result3 = getSupportedTypes();
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case TRANSACTION_getActiveSource /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    HdmiDeviceInfo _result4 = getActiveSource();
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_getSupportedTypes);
                        _result4.writeToParcel(reply, TRANSACTION_getSupportedTypes);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_oneTouchPlay /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    oneTouchPlay(android.hardware.hdmi.IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_queryDisplayStatus /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    queryDisplayStatus(android.hardware.hdmi.IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addHotplugEventListener /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    addHotplugEventListener(android.hardware.hdmi.IHdmiHotplugEventListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeHotplugEventListener /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeHotplugEventListener(android.hardware.hdmi.IHdmiHotplugEventListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addDeviceEventListener /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    addDeviceEventListener(android.hardware.hdmi.IHdmiDeviceEventListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deviceSelect /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    deviceSelect(data.readInt(), android.hardware.hdmi.IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_portSelect /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    portSelect(data.readInt(), android.hardware.hdmi.IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendKeyEvent /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendKeyEvent(data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPortInfo /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<HdmiPortInfo> _result5 = getPortInfo();
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case TRANSACTION_canChangeSystemAudioMode /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = canChangeSystemAudioMode();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getSupportedTypes : 0);
                    return true;
                case TRANSACTION_getSystemAudioMode /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSystemAudioMode();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getSupportedTypes : 0);
                    return true;
                case TRANSACTION_setSystemAudioMode /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSystemAudioMode(data.readInt() != 0, android.hardware.hdmi.IHdmiControlCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addSystemAudioModeChangeListener /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    addSystemAudioModeChangeListener(android.hardware.hdmi.IHdmiSystemAudioModeChangeListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeSystemAudioModeChangeListener /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeSystemAudioModeChangeListener(android.hardware.hdmi.IHdmiSystemAudioModeChangeListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setArcMode /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    setArcMode(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setProhibitMode /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    setProhibitMode(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSystemAudioVolume /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSystemAudioVolume(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSystemAudioMute /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSystemAudioMute(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setInputChangeListener /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInputChangeListener(android.hardware.hdmi.IHdmiInputChangeListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getInputDevices /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInputDevices();
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getDeviceList /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDeviceList();
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_sendVendorCommand /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendVendorCommand(data.readInt(), data.readInt(), data.createByteArray(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addVendorCommandListener /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    addVendorCommandListener(android.hardware.hdmi.IHdmiVendorCommandListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendStandby /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendStandby(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setHdmiRecordListener /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    setHdmiRecordListener(android.hardware.hdmi.IHdmiRecordListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startOneTouchRecord /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    startOneTouchRecord(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopOneTouchRecord /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopOneTouchRecord(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startTimerRecording /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    startTimerRecording(data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearTimerRecording /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearTimerRecording(data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendMhlVendorCommand /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendMhlVendorCommand(data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addHdmiMhlVendorCommandListener /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    addHdmiMhlVendorCommandListener(android.hardware.hdmi.IHdmiMhlVendorCommandListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addDeviceEventListener(IHdmiDeviceEventListener iHdmiDeviceEventListener) throws RemoteException;

    void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener iHdmiMhlVendorCommandListener) throws RemoteException;

    void addHotplugEventListener(IHdmiHotplugEventListener iHdmiHotplugEventListener) throws RemoteException;

    void addSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener iHdmiSystemAudioModeChangeListener) throws RemoteException;

    void addVendorCommandListener(IHdmiVendorCommandListener iHdmiVendorCommandListener, int i) throws RemoteException;

    boolean canChangeSystemAudioMode() throws RemoteException;

    void clearTimerRecording(int i, int i2, byte[] bArr) throws RemoteException;

    void deviceSelect(int i, IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    HdmiDeviceInfo getActiveSource() throws RemoteException;

    List<HdmiDeviceInfo> getDeviceList() throws RemoteException;

    List<HdmiDeviceInfo> getInputDevices() throws RemoteException;

    List<HdmiPortInfo> getPortInfo() throws RemoteException;

    int[] getSupportedTypes() throws RemoteException;

    boolean getSystemAudioMode() throws RemoteException;

    void oneTouchPlay(IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    void portSelect(int i, IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    void queryDisplayStatus(IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    void removeHotplugEventListener(IHdmiHotplugEventListener iHdmiHotplugEventListener) throws RemoteException;

    void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener iHdmiSystemAudioModeChangeListener) throws RemoteException;

    void sendKeyEvent(int i, int i2, boolean z) throws RemoteException;

    void sendMhlVendorCommand(int i, int i2, int i3, byte[] bArr) throws RemoteException;

    void sendStandby(int i, int i2) throws RemoteException;

    void sendVendorCommand(int i, int i2, byte[] bArr, boolean z) throws RemoteException;

    void setArcMode(boolean z) throws RemoteException;

    void setHdmiRecordListener(IHdmiRecordListener iHdmiRecordListener) throws RemoteException;

    void setInputChangeListener(IHdmiInputChangeListener iHdmiInputChangeListener) throws RemoteException;

    void setProhibitMode(boolean z) throws RemoteException;

    void setSystemAudioMode(boolean z, IHdmiControlCallback iHdmiControlCallback) throws RemoteException;

    void setSystemAudioMute(boolean z) throws RemoteException;

    void setSystemAudioVolume(int i, int i2, int i3) throws RemoteException;

    void startOneTouchRecord(int i, byte[] bArr) throws RemoteException;

    void startTimerRecording(int i, int i2, byte[] bArr) throws RemoteException;

    void stopOneTouchRecord(int i) throws RemoteException;
}
