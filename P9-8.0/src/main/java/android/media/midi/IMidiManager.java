package android.media.midi;

import android.bluetooth.BluetoothDevice;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMidiManager extends IInterface {

    public static abstract class Stub extends Binder implements IMidiManager {
        private static final String DESCRIPTOR = "android.media.midi.IMidiManager";
        static final int TRANSACTION_closeDevice = 6;
        static final int TRANSACTION_getDeviceStatus = 10;
        static final int TRANSACTION_getDevices = 1;
        static final int TRANSACTION_getServiceDeviceInfo = 9;
        static final int TRANSACTION_openBluetoothDevice = 5;
        static final int TRANSACTION_openDevice = 4;
        static final int TRANSACTION_registerDeviceServer = 7;
        static final int TRANSACTION_registerListener = 2;
        static final int TRANSACTION_setDeviceStatus = 11;
        static final int TRANSACTION_unregisterDeviceServer = 8;
        static final int TRANSACTION_unregisterListener = 3;

        private static class Proxy implements IMidiManager {
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

            public MidiDeviceInfo[] getDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    MidiDeviceInfo[] _result = (MidiDeviceInfo[]) _reply.createTypedArray(MidiDeviceInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerListener(IBinder clientToken, IMidiDeviceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(clientToken);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterListener(IBinder clientToken, IMidiDeviceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(clientToken);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void openDevice(IBinder clientToken, MidiDeviceInfo device, IMidiDeviceOpenCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(clientToken);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void openBluetoothDevice(IBinder clientToken, BluetoothDevice bluetoothDevice, IMidiDeviceOpenCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(clientToken);
                    if (bluetoothDevice != null) {
                        _data.writeInt(1);
                        bluetoothDevice.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closeDevice(IBinder clientToken, IBinder deviceToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(clientToken);
                    _data.writeStrongBinder(deviceToken);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MidiDeviceInfo registerDeviceServer(IMidiDeviceServer server, int numInputPorts, int numOutputPorts, String[] inputPortNames, String[] outputPortNames, Bundle properties, int type) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    MidiDeviceInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (server != null) {
                        iBinder = server.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(numInputPorts);
                    _data.writeInt(numOutputPorts);
                    _data.writeStringArray(inputPortNames);
                    _data.writeStringArray(outputPortNames);
                    if (properties != null) {
                        _data.writeInt(1);
                        properties.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (MidiDeviceInfo) MidiDeviceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterDeviceServer(IMidiDeviceServer server) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (server != null) {
                        iBinder = server.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MidiDeviceInfo getServiceDeviceInfo(String packageName, String className) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    MidiDeviceInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(className);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (MidiDeviceInfo) MidiDeviceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MidiDeviceStatus getDeviceStatus(MidiDeviceInfo deviceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    MidiDeviceStatus _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        _data.writeInt(1);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (MidiDeviceStatus) MidiDeviceStatus.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDeviceStatus(IMidiDeviceServer server, MidiDeviceStatus status) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (server != null) {
                        iBinder = server.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (status != null) {
                        _data.writeInt(1);
                        status.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
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

        public static IMidiManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMidiManager)) {
                return new Proxy(obj);
            }
            return (IMidiManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder _arg0;
            IMidiDeviceServer _arg02;
            MidiDeviceInfo _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    MidiDeviceInfo[] _result2 = getDevices();
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, 1);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    registerListener(data.readStrongBinder(), android.media.midi.IMidiDeviceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterListener(data.readStrongBinder(), android.media.midi.IMidiDeviceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    MidiDeviceInfo _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg1 = (MidiDeviceInfo) MidiDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    openDevice(_arg0, _arg1, android.media.midi.IMidiDeviceOpenCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 5:
                    BluetoothDevice _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg12 = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    openBluetoothDevice(_arg0, _arg12, android.media.midi.IMidiDeviceOpenCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    closeDevice(data.readStrongBinder(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 7:
                    Bundle _arg5;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = android.media.midi.IMidiDeviceServer.Stub.asInterface(data.readStrongBinder());
                    int _arg13 = data.readInt();
                    int _arg2 = data.readInt();
                    String[] _arg3 = data.createStringArray();
                    String[] _arg4 = data.createStringArray();
                    if (data.readInt() != 0) {
                        _arg5 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg5 = null;
                    }
                    _result = registerDeviceServer(_arg02, _arg13, _arg2, _arg3, _arg4, _arg5, data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterDeviceServer(android.media.midi.IMidiDeviceServer.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getServiceDeviceInfo(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 10:
                    MidiDeviceInfo _arg03;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (MidiDeviceInfo) MidiDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    MidiDeviceStatus _result3 = getDeviceStatus(_arg03);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 11:
                    MidiDeviceStatus _arg14;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = android.media.midi.IMidiDeviceServer.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg14 = (MidiDeviceStatus) MidiDeviceStatus.CREATOR.createFromParcel(data);
                    } else {
                        _arg14 = null;
                    }
                    setDeviceStatus(_arg02, _arg14);
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

    void closeDevice(IBinder iBinder, IBinder iBinder2) throws RemoteException;

    MidiDeviceStatus getDeviceStatus(MidiDeviceInfo midiDeviceInfo) throws RemoteException;

    MidiDeviceInfo[] getDevices() throws RemoteException;

    MidiDeviceInfo getServiceDeviceInfo(String str, String str2) throws RemoteException;

    void openBluetoothDevice(IBinder iBinder, BluetoothDevice bluetoothDevice, IMidiDeviceOpenCallback iMidiDeviceOpenCallback) throws RemoteException;

    void openDevice(IBinder iBinder, MidiDeviceInfo midiDeviceInfo, IMidiDeviceOpenCallback iMidiDeviceOpenCallback) throws RemoteException;

    MidiDeviceInfo registerDeviceServer(IMidiDeviceServer iMidiDeviceServer, int i, int i2, String[] strArr, String[] strArr2, Bundle bundle, int i3) throws RemoteException;

    void registerListener(IBinder iBinder, IMidiDeviceListener iMidiDeviceListener) throws RemoteException;

    void setDeviceStatus(IMidiDeviceServer iMidiDeviceServer, MidiDeviceStatus midiDeviceStatus) throws RemoteException;

    void unregisterDeviceServer(IMidiDeviceServer iMidiDeviceServer) throws RemoteException;

    void unregisterListener(IBinder iBinder, IMidiDeviceListener iMidiDeviceListener) throws RemoteException;
}
