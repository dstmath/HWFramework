package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IBluetoothA2dp extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothA2dp {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothA2dp";
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_disableOptionalCodecs = 16;
        static final int TRANSACTION_disconnect = 2;
        static final int TRANSACTION_enableOptionalCodecs = 15;
        static final int TRANSACTION_getActiveDevice = 7;
        static final int TRANSACTION_getCodecStatus = 13;
        static final int TRANSACTION_getConnectedDevices = 3;
        static final int TRANSACTION_getConnectionState = 5;
        static final int TRANSACTION_getDevicesMatchingConnectionStates = 4;
        static final int TRANSACTION_getHighQualityDefaultConfigValue = 23;
        static final int TRANSACTION_getLdacQualityValue = 21;
        static final int TRANSACTION_getOptionalCodecsEnabled = 18;
        static final int TRANSACTION_getPriority = 9;
        static final int TRANSACTION_isA2dpPlaying = 12;
        static final int TRANSACTION_isAvrcpAbsoluteVolumeSupported = 10;
        static final int TRANSACTION_isSupportHighQuality = 22;
        static final int TRANSACTION_isSupportPlayLDAC = 20;
        static final int TRANSACTION_setActiveDevice = 6;
        static final int TRANSACTION_setAvrcpAbsoluteVolume = 11;
        static final int TRANSACTION_setCodecConfigPreference = 14;
        static final int TRANSACTION_setHighQualityDefaultConfigValue = 24;
        static final int TRANSACTION_setOptionalCodecsEnabled = 19;
        static final int TRANSACTION_setPriority = 8;
        static final int TRANSACTION_supportsOptionalCodecs = 17;

        private static class Proxy implements IBluetoothA2dp {
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

            public boolean connect(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disconnect(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<BluetoothDevice> getConnectedDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(states);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getConnectionState(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setActiveDevice(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public BluetoothDevice getActiveDevice() throws RemoteException {
                BluetoothDevice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BluetoothDevice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPriority(BluetoothDevice device, int priority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(priority);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPriority(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAvrcpAbsoluteVolumeSupported() throws RemoteException {
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

            public void setAvrcpAbsoluteVolume(int volume) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(volume);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public boolean isA2dpPlaying(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public BluetoothCodecStatus getCodecStatus(BluetoothDevice device) throws RemoteException {
                BluetoothCodecStatus _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BluetoothCodecStatus.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCodecConfigPreference(BluetoothDevice device, BluetoothCodecConfig codecConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (codecConfig != null) {
                        _data.writeInt(1);
                        codecConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void enableOptionalCodecs(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void disableOptionalCodecs(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public int supportsOptionalCodecs(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getOptionalCodecsEnabled(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOptionalCodecsEnabled(BluetoothDevice device, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(value);
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public boolean isSupportPlayLDAC(String deviceMac) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceMac);
                    boolean _result = false;
                    this.mRemote.transact(20, _data, _reply, 0);
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

            public int getLdacQualityValue() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSupportHighQuality(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getHighQualityDefaultConfigValue(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setHighQualityDefaultConfigValue(BluetoothDevice device, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enable);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
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

        public static IBluetoothA2dp asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothA2dp)) {
                return new Proxy(obj);
            }
            return (IBluetoothA2dp) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v31, resolved type: android.bluetooth.BluetoothDevice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v35, resolved type: android.bluetooth.BluetoothCodecConfig} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v66, resolved type: android.bluetooth.BluetoothDevice} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v1 */
        /* JADX WARNING: type inference failed for: r3v5 */
        /* JADX WARNING: type inference failed for: r3v10 */
        /* JADX WARNING: type inference failed for: r3v14 */
        /* JADX WARNING: type inference failed for: r3v19 */
        /* JADX WARNING: type inference failed for: r3v23 */
        /* JADX WARNING: type inference failed for: r3v27 */
        /* JADX WARNING: type inference failed for: r3v39 */
        /* JADX WARNING: type inference failed for: r3v42 */
        /* JADX WARNING: type inference failed for: r3v45 */
        /* JADX WARNING: type inference failed for: r3v49 */
        /* JADX WARNING: type inference failed for: r3v53 */
        /* JADX WARNING: type inference failed for: r3v58 */
        /* JADX WARNING: type inference failed for: r3v62 */
        /* JADX WARNING: type inference failed for: r3v70 */
        /* JADX WARNING: type inference failed for: r3v71 */
        /* JADX WARNING: type inference failed for: r3v72 */
        /* JADX WARNING: type inference failed for: r3v73 */
        /* JADX WARNING: type inference failed for: r3v74 */
        /* JADX WARNING: type inference failed for: r3v75 */
        /* JADX WARNING: type inference failed for: r3v76 */
        /* JADX WARNING: type inference failed for: r3v77 */
        /* JADX WARNING: type inference failed for: r3v78 */
        /* JADX WARNING: type inference failed for: r3v79 */
        /* JADX WARNING: type inference failed for: r3v80 */
        /* JADX WARNING: type inference failed for: r3v81 */
        /* JADX WARNING: type inference failed for: r3v82 */
        /* JADX WARNING: type inference failed for: r3v83 */
        /* JADX WARNING: type inference failed for: r3v84 */
        /* JADX WARNING: type inference failed for: r3v85 */
        /* JADX WARNING: type inference failed for: r3v86 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BluetoothDevice _arg0;
            if (code != 1598968902) {
                boolean _arg1 = false;
                ? _arg02 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        boolean _result = connect(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        boolean _result2 = disconnect(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        List<BluetoothDevice> _result3 = getConnectedDevices();
                        reply.writeNoException();
                        reply.writeTypedList(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        List<BluetoothDevice> _result4 = getDevicesMatchingConnectionStates(data.createIntArray());
                        reply.writeNoException();
                        reply.writeTypedList(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        int _result5 = getConnectionState(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        boolean _result6 = setActiveDevice(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        BluetoothDevice _result7 = getActiveDevice();
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        boolean _result8 = setPriority(_arg02, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        int _result9 = getPriority(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result10 = isAvrcpAbsoluteVolumeSupported();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        setAvrcpAbsoluteVolume(data.readInt());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        boolean _result11 = isA2dpPlaying(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        BluetoothCodecStatus _result12 = getCodecStatus(_arg02);
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothCodecConfig.CREATOR.createFromParcel(data);
                        }
                        setCodecConfigPreference(_arg0, _arg02);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        enableOptionalCodecs(_arg02);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        disableOptionalCodecs(_arg02);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        int _result13 = supportsOptionalCodecs(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        int _result14 = getOptionalCodecsEnabled(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        setOptionalCodecsEnabled(_arg02, data.readInt());
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result15 = isSupportPlayLDAC(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = getLdacQualityValue();
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        boolean _result17 = isSupportHighQuality(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        boolean _result18 = getHighQualityDefaultConfigValue(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result19 = setHighQualityDefaultConfigValue(_arg02, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result19);
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

    boolean connect(BluetoothDevice bluetoothDevice) throws RemoteException;

    void disableOptionalCodecs(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean disconnect(BluetoothDevice bluetoothDevice) throws RemoteException;

    void enableOptionalCodecs(BluetoothDevice bluetoothDevice) throws RemoteException;

    BluetoothDevice getActiveDevice() throws RemoteException;

    BluetoothCodecStatus getCodecStatus(BluetoothDevice bluetoothDevice) throws RemoteException;

    List<BluetoothDevice> getConnectedDevices() throws RemoteException;

    int getConnectionState(BluetoothDevice bluetoothDevice) throws RemoteException;

    List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] iArr) throws RemoteException;

    boolean getHighQualityDefaultConfigValue(BluetoothDevice bluetoothDevice) throws RemoteException;

    int getLdacQualityValue() throws RemoteException;

    int getOptionalCodecsEnabled(BluetoothDevice bluetoothDevice) throws RemoteException;

    int getPriority(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean isA2dpPlaying(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean isAvrcpAbsoluteVolumeSupported() throws RemoteException;

    boolean isSupportHighQuality(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean isSupportPlayLDAC(String str) throws RemoteException;

    boolean setActiveDevice(BluetoothDevice bluetoothDevice) throws RemoteException;

    void setAvrcpAbsoluteVolume(int i) throws RemoteException;

    void setCodecConfigPreference(BluetoothDevice bluetoothDevice, BluetoothCodecConfig bluetoothCodecConfig) throws RemoteException;

    boolean setHighQualityDefaultConfigValue(BluetoothDevice bluetoothDevice, boolean z) throws RemoteException;

    void setOptionalCodecsEnabled(BluetoothDevice bluetoothDevice, int i) throws RemoteException;

    boolean setPriority(BluetoothDevice bluetoothDevice, int i) throws RemoteException;

    int supportsOptionalCodecs(BluetoothDevice bluetoothDevice) throws RemoteException;
}
