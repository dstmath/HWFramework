package android.bluetooth;

import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IBluetoothAvrcpController extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothAvrcpController {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothAvrcpController";
        static final int TRANSACTION_getConnectedDevices = 1;
        static final int TRANSACTION_getConnectionState = 3;
        static final int TRANSACTION_getDevicesMatchingConnectionStates = 2;
        static final int TRANSACTION_getMetadata = 6;
        static final int TRANSACTION_getPlaybackState = 7;
        static final int TRANSACTION_getPlayerSettings = 5;
        static final int TRANSACTION_sendGroupNavigationCmd = 9;
        static final int TRANSACTION_sendPassThroughCmd = 4;
        static final int TRANSACTION_setPlayerApplicationSetting = 8;

        private static class Proxy implements IBluetoothAvrcpController {
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
                    this.mRemote.transact(Stub.TRANSACTION_getConnectedDevices, _data, _reply, 0);
                    _reply.readException();
                    List<BluetoothDevice> _result = _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getDevicesMatchingConnectionStates, _data, _reply, 0);
                    _reply.readException();
                    List<BluetoothDevice> _result = _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_getConnectedDevices);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getConnectionState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendPassThroughCmd(BluetoothDevice device, int keyCode, int keyState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_getConnectedDevices);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(keyCode);
                    _data.writeInt(keyState);
                    this.mRemote.transact(Stub.TRANSACTION_sendPassThroughCmd, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public BluetoothAvrcpPlayerSettings getPlayerSettings(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    BluetoothAvrcpPlayerSettings bluetoothAvrcpPlayerSettings;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_getConnectedDevices);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getPlayerSettings, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bluetoothAvrcpPlayerSettings = (BluetoothAvrcpPlayerSettings) BluetoothAvrcpPlayerSettings.CREATOR.createFromParcel(_reply);
                    } else {
                        bluetoothAvrcpPlayerSettings = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bluetoothAvrcpPlayerSettings;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MediaMetadata getMetadata(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    MediaMetadata mediaMetadata;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_getConnectedDevices);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getMetadata, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        mediaMetadata = (MediaMetadata) MediaMetadata.CREATOR.createFromParcel(_reply);
                    } else {
                        mediaMetadata = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return mediaMetadata;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PlaybackState getPlaybackState(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PlaybackState playbackState;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_getConnectedDevices);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getPlaybackState, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        playbackState = (PlaybackState) PlaybackState.CREATOR.createFromParcel(_reply);
                    } else {
                        playbackState = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return playbackState;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPlayerApplicationSetting(BluetoothAvrcpPlayerSettings plAppSetting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (plAppSetting != null) {
                        _data.writeInt(Stub.TRANSACTION_getConnectedDevices);
                        plAppSetting.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setPlayerApplicationSetting, _data, _reply, 0);
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

            public void sendGroupNavigationCmd(BluetoothDevice device, int keyCode, int keyState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_getConnectedDevices);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(keyCode);
                    _data.writeInt(keyState);
                    this.mRemote.transact(Stub.TRANSACTION_sendGroupNavigationCmd, _data, _reply, 0);
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

        public static IBluetoothAvrcpController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothAvrcpController)) {
                return new Proxy(obj);
            }
            return (IBluetoothAvrcpController) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            List<BluetoothDevice> _result;
            BluetoothDevice bluetoothDevice;
            switch (code) {
                case TRANSACTION_getConnectedDevices /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getConnectedDevices();
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case TRANSACTION_getDevicesMatchingConnectionStates /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDevicesMatchingConnectionStates(data.createIntArray());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case TRANSACTION_getConnectionState /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    int _result2 = getConnectionState(bluetoothDevice);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_sendPassThroughCmd /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    sendPassThroughCmd(bluetoothDevice, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPlayerSettings /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    BluetoothAvrcpPlayerSettings _result3 = getPlayerSettings(bluetoothDevice);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getConnectedDevices);
                        _result3.writeToParcel(reply, TRANSACTION_getConnectedDevices);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getMetadata /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    MediaMetadata _result4 = getMetadata(bluetoothDevice);
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_getConnectedDevices);
                        _result4.writeToParcel(reply, TRANSACTION_getConnectedDevices);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getPlaybackState /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    PlaybackState _result5 = getPlaybackState(bluetoothDevice);
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_getConnectedDevices);
                        _result5.writeToParcel(reply, TRANSACTION_getConnectedDevices);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setPlayerApplicationSetting /*8*/:
                    BluetoothAvrcpPlayerSettings bluetoothAvrcpPlayerSettings;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothAvrcpPlayerSettings = (BluetoothAvrcpPlayerSettings) BluetoothAvrcpPlayerSettings.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothAvrcpPlayerSettings = null;
                    }
                    boolean _result6 = setPlayerApplicationSetting(bluetoothAvrcpPlayerSettings);
                    reply.writeNoException();
                    reply.writeInt(_result6 ? TRANSACTION_getConnectedDevices : 0);
                    return true;
                case TRANSACTION_sendGroupNavigationCmd /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    sendGroupNavigationCmd(bluetoothDevice, data.readInt(), data.readInt());
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

    List<BluetoothDevice> getConnectedDevices() throws RemoteException;

    int getConnectionState(BluetoothDevice bluetoothDevice) throws RemoteException;

    List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] iArr) throws RemoteException;

    MediaMetadata getMetadata(BluetoothDevice bluetoothDevice) throws RemoteException;

    PlaybackState getPlaybackState(BluetoothDevice bluetoothDevice) throws RemoteException;

    BluetoothAvrcpPlayerSettings getPlayerSettings(BluetoothDevice bluetoothDevice) throws RemoteException;

    void sendGroupNavigationCmd(BluetoothDevice bluetoothDevice, int i, int i2) throws RemoteException;

    void sendPassThroughCmd(BluetoothDevice bluetoothDevice, int i, int i2) throws RemoteException;

    boolean setPlayerApplicationSetting(BluetoothAvrcpPlayerSettings bluetoothAvrcpPlayerSettings) throws RemoteException;
}
