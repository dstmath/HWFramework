package android.media.midi;

import android.bluetooth.BluetoothDevice;
import android.media.midi.IMidiDeviceListener;
import android.media.midi.IMidiDeviceOpenCallback;
import android.media.midi.IMidiDeviceServer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMidiManager extends IInterface {
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

    public static class Default implements IMidiManager {
        @Override // android.media.midi.IMidiManager
        public MidiDeviceInfo[] getDevices() throws RemoteException {
            return null;
        }

        @Override // android.media.midi.IMidiManager
        public void registerListener(IBinder clientToken, IMidiDeviceListener listener) throws RemoteException {
        }

        @Override // android.media.midi.IMidiManager
        public void unregisterListener(IBinder clientToken, IMidiDeviceListener listener) throws RemoteException {
        }

        @Override // android.media.midi.IMidiManager
        public void openDevice(IBinder clientToken, MidiDeviceInfo device, IMidiDeviceOpenCallback callback) throws RemoteException {
        }

        @Override // android.media.midi.IMidiManager
        public void openBluetoothDevice(IBinder clientToken, BluetoothDevice bluetoothDevice, IMidiDeviceOpenCallback callback) throws RemoteException {
        }

        @Override // android.media.midi.IMidiManager
        public void closeDevice(IBinder clientToken, IBinder deviceToken) throws RemoteException {
        }

        @Override // android.media.midi.IMidiManager
        public MidiDeviceInfo registerDeviceServer(IMidiDeviceServer server, int numInputPorts, int numOutputPorts, String[] inputPortNames, String[] outputPortNames, Bundle properties, int type) throws RemoteException {
            return null;
        }

        @Override // android.media.midi.IMidiManager
        public void unregisterDeviceServer(IMidiDeviceServer server) throws RemoteException {
        }

        @Override // android.media.midi.IMidiManager
        public MidiDeviceInfo getServiceDeviceInfo(String packageName, String className) throws RemoteException {
            return null;
        }

        @Override // android.media.midi.IMidiManager
        public MidiDeviceStatus getDeviceStatus(MidiDeviceInfo deviceInfo) throws RemoteException {
            return null;
        }

        @Override // android.media.midi.IMidiManager
        public void setDeviceStatus(IMidiDeviceServer server, MidiDeviceStatus status) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getDevices";
                case 2:
                    return "registerListener";
                case 3:
                    return "unregisterListener";
                case 4:
                    return "openDevice";
                case 5:
                    return "openBluetoothDevice";
                case 6:
                    return "closeDevice";
                case 7:
                    return "registerDeviceServer";
                case 8:
                    return "unregisterDeviceServer";
                case 9:
                    return "getServiceDeviceInfo";
                case 10:
                    return "getDeviceStatus";
                case 11:
                    return "setDeviceStatus";
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
            MidiDeviceInfo _arg1;
            BluetoothDevice _arg12;
            Bundle _arg5;
            MidiDeviceInfo _arg0;
            MidiDeviceStatus _arg13;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        MidiDeviceInfo[] _result = getDevices();
                        reply.writeNoException();
                        reply.writeTypedArray(_result, 1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        registerListener(data.readStrongBinder(), IMidiDeviceListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterListener(data.readStrongBinder(), IMidiDeviceListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg02 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = MidiDeviceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        openDevice(_arg02, _arg1, IMidiDeviceOpenCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg03 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = BluetoothDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        openBluetoothDevice(_arg03, _arg12, IMidiDeviceOpenCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        closeDevice(data.readStrongBinder(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IMidiDeviceServer _arg04 = IMidiDeviceServer.Stub.asInterface(data.readStrongBinder());
                        int _arg14 = data.readInt();
                        int _arg2 = data.readInt();
                        String[] _arg3 = data.createStringArray();
                        String[] _arg4 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg5 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        MidiDeviceInfo _result2 = registerDeviceServer(_arg04, _arg14, _arg2, _arg3, _arg4, _arg5, data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterDeviceServer(IMidiDeviceServer.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        MidiDeviceInfo _result3 = getServiceDeviceInfo(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = MidiDeviceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        MidiDeviceStatus _result4 = getDeviceStatus(_arg0);
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        IMidiDeviceServer _arg05 = IMidiDeviceServer.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = MidiDeviceStatus.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        setDeviceStatus(_arg05, _arg13);
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
        public static class Proxy implements IMidiManager {
            public static IMidiManager sDefaultImpl;
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

            @Override // android.media.midi.IMidiManager
            public MidiDeviceInfo[] getDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDevices();
                    }
                    _reply.readException();
                    MidiDeviceInfo[] _result = (MidiDeviceInfo[]) _reply.createTypedArray(MidiDeviceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiManager
            public void registerListener(IBinder clientToken, IMidiDeviceListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(clientToken);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerListener(clientToken, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiManager
            public void unregisterListener(IBinder clientToken, IMidiDeviceListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(clientToken);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterListener(clientToken, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiManager
            public void openDevice(IBinder clientToken, MidiDeviceInfo device, IMidiDeviceOpenCallback callback) throws RemoteException {
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
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().openDevice(clientToken, device, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiManager
            public void openBluetoothDevice(IBinder clientToken, BluetoothDevice bluetoothDevice, IMidiDeviceOpenCallback callback) throws RemoteException {
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
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().openBluetoothDevice(clientToken, bluetoothDevice, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiManager
            public void closeDevice(IBinder clientToken, IBinder deviceToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(clientToken);
                    _data.writeStrongBinder(deviceToken);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closeDevice(clientToken, deviceToken);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiManager
            public MidiDeviceInfo registerDeviceServer(IMidiDeviceServer server, int numInputPorts, int numOutputPorts, String[] inputPortNames, String[] outputPortNames, Bundle properties, int type) throws RemoteException {
                Throwable th;
                MidiDeviceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(server != null ? server.asBinder() : null);
                    try {
                        _data.writeInt(numInputPorts);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(numOutputPorts);
                        try {
                            _data.writeStringArray(inputPortNames);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeStringArray(outputPortNames);
                            if (properties != null) {
                                _data.writeInt(1);
                                properties.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeInt(type);
                            if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = MidiDeviceInfo.CREATOR.createFromParcel(_reply);
                                } else {
                                    _result = null;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            MidiDeviceInfo registerDeviceServer = Stub.getDefaultImpl().registerDeviceServer(server, numInputPorts, numOutputPorts, inputPortNames, outputPortNames, properties, type);
                            _reply.recycle();
                            _data.recycle();
                            return registerDeviceServer;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.media.midi.IMidiManager
            public void unregisterDeviceServer(IMidiDeviceServer server) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(server != null ? server.asBinder() : null);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterDeviceServer(server);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.midi.IMidiManager
            public MidiDeviceInfo getServiceDeviceInfo(String packageName, String className) throws RemoteException {
                MidiDeviceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(className);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServiceDeviceInfo(packageName, className);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MidiDeviceInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.media.midi.IMidiManager
            public MidiDeviceStatus getDeviceStatus(MidiDeviceInfo deviceInfo) throws RemoteException {
                MidiDeviceStatus _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        _data.writeInt(1);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceStatus(deviceInfo);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MidiDeviceStatus.CREATOR.createFromParcel(_reply);
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

            @Override // android.media.midi.IMidiManager
            public void setDeviceStatus(IMidiDeviceServer server, MidiDeviceStatus status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(server != null ? server.asBinder() : null);
                    if (status != null) {
                        _data.writeInt(1);
                        status.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDeviceStatus(server, status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMidiManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMidiManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
