package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IBluetoothHeadset extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothHeadset {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothHeadset";
        static final int TRANSACTION_clccResponse = 23;
        static final int TRANSACTION_connect = 8;
        static final int TRANSACTION_connectAudio = 14;
        static final int TRANSACTION_connectAudioWithAddress = 15;
        static final int TRANSACTION_disconnect = 9;
        static final int TRANSACTION_disconnectAudio = 16;
        static final int TRANSACTION_getActiveDevice = 25;
        static final int TRANSACTION_getAudioRouteAllowed = 18;
        static final int TRANSACTION_getAudioState = 12;
        static final int TRANSACTION_getConnectedDevices = 1;
        static final int TRANSACTION_getConnectionState = 3;
        static final int TRANSACTION_getDevicesMatchingConnectionStates = 2;
        static final int TRANSACTION_getPriority = 11;
        static final int TRANSACTION_isAudioConnected = 6;
        static final int TRANSACTION_isAudioOn = 13;
        static final int TRANSACTION_isInbandRingingEnabled = 26;
        static final int TRANSACTION_isScoAvailableOffCall = 27;
        static final int TRANSACTION_phoneStateChanged = 22;
        static final int TRANSACTION_sendVendorSpecificResultCode = 7;
        static final int TRANSACTION_setActiveDevice = 24;
        static final int TRANSACTION_setAudioRouteAllowed = 17;
        static final int TRANSACTION_setForceScoAudio = 19;
        static final int TRANSACTION_setPriority = 10;
        static final int TRANSACTION_startScoUsingVirtualVoiceCall = 20;
        static final int TRANSACTION_startVoiceRecognition = 4;
        static final int TRANSACTION_stopScoUsingVirtualVoiceCall = 21;
        static final int TRANSACTION_stopVoiceRecognition = 5;

        private static class Proxy implements IBluetoothHeadset {
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

            public List<BluetoothDevice> getConnectedDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
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
                    this.mRemote.transact(2, _data, _reply, 0);
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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startVoiceRecognition(BluetoothDevice device) throws RemoteException {
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
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public boolean stopVoiceRecognition(BluetoothDevice device) throws RemoteException {
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
                    this.mRemote.transact(5, _data, _reply, 0);
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

            public boolean isAudioConnected(BluetoothDevice device) throws RemoteException {
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

            public boolean sendVendorSpecificResultCode(BluetoothDevice device, String command, String arg) throws RemoteException {
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
                    _data.writeString(command);
                    _data.writeString(arg);
                    this.mRemote.transact(7, _data, _reply, 0);
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
                    this.mRemote.transact(9, _data, _reply, 0);
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
                    this.mRemote.transact(10, _data, _reply, 0);
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
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAudioState(BluetoothDevice device) throws RemoteException {
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
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAudioOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
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

            public boolean connectAudio() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public boolean connectAudioWithAddress(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    boolean _result = false;
                    this.mRemote.transact(15, _data, _reply, 0);
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

            public boolean disconnectAudio() throws RemoteException {
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

            public void setAudioRouteAllowed(boolean allowed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(allowed);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getAudioRouteAllowed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(18, _data, _reply, 0);
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

            public void setForceScoAudio(boolean forced) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(forced);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startScoUsingVirtualVoiceCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public boolean stopScoUsingVirtualVoiceCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
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

            public void phoneStateChanged(int numActive, int numHeld, int callState, String number, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(numActive);
                    _data.writeInt(numHeld);
                    _data.writeInt(callState);
                    _data.writeString(number);
                    _data.writeInt(type);
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void clccResponse(int index, int direction, int status, int mode, boolean mpty, String number, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    _data.writeInt(direction);
                    _data.writeInt(status);
                    _data.writeInt(mode);
                    _data.writeInt(mpty);
                    _data.writeString(number);
                    _data.writeInt(type);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
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

            public BluetoothDevice getActiveDevice() throws RemoteException {
                BluetoothDevice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
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

            public boolean isInbandRingingEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(26, _data, _reply, 0);
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

            public boolean isScoAvailableOffCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(27, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothHeadset asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothHeadset)) {
                return new Proxy(obj);
            }
            return (IBluetoothHeadset) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg0 = false;
                BluetoothDevice _arg02 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<BluetoothDevice> _result = getConnectedDevices();
                        reply.writeNoException();
                        parcel2.writeTypedList(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<BluetoothDevice> _result2 = getDevicesMatchingConnectionStates(data.createIntArray());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result3 = getConnectionState(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result4 = startVoiceRecognition(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result5 = stopVoiceRecognition(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result6 = isAudioConnected(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result7 = sendVendorSpecificResultCode(_arg02, data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result8 = connect(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result9 = disconnect(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result10 = setPriority(_arg02, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result11 = getPriority(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result12 = getAudioState(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result13 = isAudioOn();
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result14 = connectAudio();
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result15 = connectAudioWithAddress(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result16 = disconnectAudio();
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setAudioRouteAllowed(_arg0);
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result17 = getAudioRouteAllowed();
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setForceScoAudio(_arg0);
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result18 = startScoUsingVirtualVoiceCall();
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result19 = stopScoUsingVirtualVoiceCall();
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        phoneStateChanged(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        clccResponse(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result20 = setActiveDevice(_arg02);
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        BluetoothDevice _result21 = getActiveDevice();
                        reply.writeNoException();
                        if (_result21 != null) {
                            parcel2.writeInt(1);
                            _result21.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result22 = isInbandRingingEnabled();
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result23 = isScoAvailableOffCall();
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void clccResponse(int i, int i2, int i3, int i4, boolean z, String str, int i5) throws RemoteException;

    boolean connect(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean connectAudio() throws RemoteException;

    boolean connectAudioWithAddress(String str) throws RemoteException;

    boolean disconnect(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean disconnectAudio() throws RemoteException;

    BluetoothDevice getActiveDevice() throws RemoteException;

    boolean getAudioRouteAllowed() throws RemoteException;

    int getAudioState(BluetoothDevice bluetoothDevice) throws RemoteException;

    List<BluetoothDevice> getConnectedDevices() throws RemoteException;

    int getConnectionState(BluetoothDevice bluetoothDevice) throws RemoteException;

    List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] iArr) throws RemoteException;

    int getPriority(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean isAudioConnected(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean isAudioOn() throws RemoteException;

    boolean isInbandRingingEnabled() throws RemoteException;

    boolean isScoAvailableOffCall() throws RemoteException;

    void phoneStateChanged(int i, int i2, int i3, String str, int i4) throws RemoteException;

    boolean sendVendorSpecificResultCode(BluetoothDevice bluetoothDevice, String str, String str2) throws RemoteException;

    boolean setActiveDevice(BluetoothDevice bluetoothDevice) throws RemoteException;

    void setAudioRouteAllowed(boolean z) throws RemoteException;

    void setForceScoAudio(boolean z) throws RemoteException;

    boolean setPriority(BluetoothDevice bluetoothDevice, int i) throws RemoteException;

    boolean startScoUsingVirtualVoiceCall() throws RemoteException;

    boolean startVoiceRecognition(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean stopScoUsingVirtualVoiceCall() throws RemoteException;

    boolean stopVoiceRecognition(BluetoothDevice bluetoothDevice) throws RemoteException;
}
