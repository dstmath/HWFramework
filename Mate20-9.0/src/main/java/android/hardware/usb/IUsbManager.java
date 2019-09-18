package android.hardware.usb;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IUsbManager extends IInterface {

    public static abstract class Stub extends Binder implements IUsbManager {
        private static final String DESCRIPTOR = "android.hardware.usb.IUsbManager";
        static final int TRANSACTION_allowUsbDebugging = 22;
        static final int TRANSACTION_clearDefaults = 14;
        static final int TRANSACTION_clearUsbDebuggingKeys = 24;
        static final int TRANSACTION_denyUsbDebugging = 23;
        static final int TRANSACTION_getControlFd = 21;
        static final int TRANSACTION_getCurrentAccessory = 3;
        static final int TRANSACTION_getCurrentFunctions = 18;
        static final int TRANSACTION_getDeviceList = 1;
        static final int TRANSACTION_getPortStatus = 26;
        static final int TRANSACTION_getPorts = 25;
        static final int TRANSACTION_getScreenUnlockedFunctions = 20;
        static final int TRANSACTION_grantAccessoryPermission = 12;
        static final int TRANSACTION_grantDevicePermission = 11;
        static final int TRANSACTION_hasAccessoryPermission = 8;
        static final int TRANSACTION_hasDefaults = 13;
        static final int TRANSACTION_hasDevicePermission = 7;
        static final int TRANSACTION_isFunctionEnabled = 15;
        static final int TRANSACTION_openAccessory = 4;
        static final int TRANSACTION_openDevice = 2;
        static final int TRANSACTION_requestAccessoryPermission = 10;
        static final int TRANSACTION_requestDevicePermission = 9;
        static final int TRANSACTION_setAccessoryPackage = 6;
        static final int TRANSACTION_setCurrentFunction = 17;
        static final int TRANSACTION_setCurrentFunctions = 16;
        static final int TRANSACTION_setDevicePackage = 5;
        static final int TRANSACTION_setPortRoles = 27;
        static final int TRANSACTION_setScreenUnlockedFunctions = 19;
        static final int TRANSACTION_setUsbDeviceConnectionHandler = 28;

        private static class Proxy implements IUsbManager {
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

            public void getDeviceList(Bundle devices) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        devices.readFromParcel(_reply);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor openDevice(String deviceName, String packageName) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceName);
                    _data.writeString(packageName);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UsbAccessory getCurrentAccessory() throws RemoteException {
                UsbAccessory _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UsbAccessory.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor openAccessory(UsbAccessory accessory) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accessory != null) {
                        _data.writeInt(1);
                        accessory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDevicePackage(UsbDevice device, String packageName, int userId) throws RemoteException {
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
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAccessoryPackage(UsbAccessory accessory, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accessory != null) {
                        _data.writeInt(1);
                        accessory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasDevicePermission(UsbDevice device, String packageName) throws RemoteException {
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
                    _data.writeString(packageName);
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

            public boolean hasAccessoryPermission(UsbAccessory accessory) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (accessory != null) {
                        _data.writeInt(1);
                        accessory.writeToParcel(_data, 0);
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

            public void requestDevicePermission(UsbDevice device, String packageName, PendingIntent pi) throws RemoteException {
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
                    _data.writeString(packageName);
                    if (pi != null) {
                        _data.writeInt(1);
                        pi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestAccessoryPermission(UsbAccessory accessory, String packageName, PendingIntent pi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accessory != null) {
                        _data.writeInt(1);
                        accessory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (pi != null) {
                        _data.writeInt(1);
                        pi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantDevicePermission(UsbDevice device, int uid) throws RemoteException {
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
                    _data.writeInt(uid);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void grantAccessoryPermission(UsbAccessory accessory, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accessory != null) {
                        _data.writeInt(1);
                        accessory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasDefaults(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
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

            public void clearDefaults(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFunctionEnabled(String function) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(function);
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

            public void setCurrentFunctions(long functions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(functions);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCurrentFunction(String function, boolean usbDataUnlocked) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(function);
                    _data.writeInt(usbDataUnlocked);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getCurrentFunctions() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setScreenUnlockedFunctions(long functions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(functions);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getScreenUnlockedFunctions() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getControlFd(long function) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(function);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void allowUsbDebugging(boolean alwaysAllow, String publicKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(alwaysAllow);
                    _data.writeString(publicKey);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void denyUsbDebugging() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearUsbDebuggingKeys() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UsbPort[] getPorts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return (UsbPort[]) _reply.createTypedArray(UsbPort.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UsbPortStatus getPortStatus(String portId) throws RemoteException {
                UsbPortStatus _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(portId);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UsbPortStatus.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPortRoles(String portId, int powerRole, int dataRole) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(portId);
                    _data.writeInt(powerRole);
                    _data.writeInt(dataRole);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUsbDeviceConnectionHandler(ComponentName usbDeviceConnectionHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (usbDeviceConnectionHandler != null) {
                        _data.writeInt(1);
                        usbDeviceConnectionHandler.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(28, _data, _reply, 0);
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

        public static IUsbManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUsbManager)) {
                return new Proxy(obj);
            }
            return (IUsbManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: android.hardware.usb.UsbAccessory} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v23, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v27, resolved type: android.app.PendingIntent} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v7 */
        /* JADX WARNING: type inference failed for: r3v11 */
        /* JADX WARNING: type inference failed for: r3v15 */
        /* JADX WARNING: type inference failed for: r3v19 */
        /* JADX WARNING: type inference failed for: r3v31 */
        /* JADX WARNING: type inference failed for: r3v35 */
        /* JADX WARNING: type inference failed for: r3v52 */
        /* JADX WARNING: type inference failed for: r3v55 */
        /* JADX WARNING: type inference failed for: r3v56 */
        /* JADX WARNING: type inference failed for: r3v57 */
        /* JADX WARNING: type inference failed for: r3v58 */
        /* JADX WARNING: type inference failed for: r3v59 */
        /* JADX WARNING: type inference failed for: r3v60 */
        /* JADX WARNING: type inference failed for: r3v61 */
        /* JADX WARNING: type inference failed for: r3v62 */
        /* JADX WARNING: type inference failed for: r3v63 */
        /* JADX WARNING: type inference failed for: r3v64 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            UsbDevice _arg0;
            UsbAccessory _arg02;
            if (code != 1598968902) {
                boolean _arg03 = false;
                ? _arg2 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _arg04 = new Bundle();
                        getDeviceList(_arg04);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg04.writeToParcel(reply, 1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result = openDevice(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        UsbAccessory _result2 = getCurrentAccessory();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = UsbAccessory.CREATOR.createFromParcel(data);
                        }
                        ParcelFileDescriptor _result3 = openAccessory(_arg2);
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = UsbDevice.CREATOR.createFromParcel(data);
                        }
                        setDevicePackage(_arg2, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = UsbAccessory.CREATOR.createFromParcel(data);
                        }
                        setAccessoryPackage(_arg2, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = UsbDevice.CREATOR.createFromParcel(data);
                        }
                        boolean _result4 = hasDevicePermission(_arg2, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = UsbAccessory.CREATOR.createFromParcel(data);
                        }
                        boolean _result5 = hasAccessoryPermission(_arg2);
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = UsbDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = PendingIntent.CREATOR.createFromParcel(data);
                        }
                        requestDevicePermission(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = UsbAccessory.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = PendingIntent.CREATOR.createFromParcel(data);
                        }
                        requestAccessoryPermission(_arg02, _arg12, _arg2);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = UsbDevice.CREATOR.createFromParcel(data);
                        }
                        grantDevicePermission(_arg2, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = UsbAccessory.CREATOR.createFromParcel(data);
                        }
                        grantAccessoryPermission(_arg2, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result6 = hasDefaults(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        clearDefaults(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result7 = isFunctionEnabled(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        setCurrentFunctions(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        setCurrentFunction(_arg05, _arg03);
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        long _result8 = getCurrentFunctions();
                        reply.writeNoException();
                        reply.writeLong(_result8);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        setScreenUnlockedFunctions(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        long _result9 = getScreenUnlockedFunctions();
                        reply.writeNoException();
                        reply.writeLong(_result9);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result10 = getControlFd(data.readLong());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        allowUsbDebugging(_arg03, data.readString());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        denyUsbDebugging();
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        clearUsbDebuggingKeys();
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        UsbPort[] _result11 = getPorts();
                        reply.writeNoException();
                        reply.writeTypedArray(_result11, 1);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        UsbPortStatus _result12 = getPortStatus(data.readString());
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        setPortRoles(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = ComponentName.CREATOR.createFromParcel(data);
                        }
                        setUsbDeviceConnectionHandler(_arg2);
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
    }

    void allowUsbDebugging(boolean z, String str) throws RemoteException;

    void clearDefaults(String str, int i) throws RemoteException;

    void clearUsbDebuggingKeys() throws RemoteException;

    void denyUsbDebugging() throws RemoteException;

    ParcelFileDescriptor getControlFd(long j) throws RemoteException;

    UsbAccessory getCurrentAccessory() throws RemoteException;

    long getCurrentFunctions() throws RemoteException;

    void getDeviceList(Bundle bundle) throws RemoteException;

    UsbPortStatus getPortStatus(String str) throws RemoteException;

    UsbPort[] getPorts() throws RemoteException;

    long getScreenUnlockedFunctions() throws RemoteException;

    void grantAccessoryPermission(UsbAccessory usbAccessory, int i) throws RemoteException;

    void grantDevicePermission(UsbDevice usbDevice, int i) throws RemoteException;

    boolean hasAccessoryPermission(UsbAccessory usbAccessory) throws RemoteException;

    boolean hasDefaults(String str, int i) throws RemoteException;

    boolean hasDevicePermission(UsbDevice usbDevice, String str) throws RemoteException;

    boolean isFunctionEnabled(String str) throws RemoteException;

    ParcelFileDescriptor openAccessory(UsbAccessory usbAccessory) throws RemoteException;

    ParcelFileDescriptor openDevice(String str, String str2) throws RemoteException;

    void requestAccessoryPermission(UsbAccessory usbAccessory, String str, PendingIntent pendingIntent) throws RemoteException;

    void requestDevicePermission(UsbDevice usbDevice, String str, PendingIntent pendingIntent) throws RemoteException;

    void setAccessoryPackage(UsbAccessory usbAccessory, String str, int i) throws RemoteException;

    void setCurrentFunction(String str, boolean z) throws RemoteException;

    void setCurrentFunctions(long j) throws RemoteException;

    void setDevicePackage(UsbDevice usbDevice, String str, int i) throws RemoteException;

    void setPortRoles(String str, int i, int i2) throws RemoteException;

    void setScreenUnlockedFunctions(long j) throws RemoteException;

    void setUsbDeviceConnectionHandler(ComponentName componentName) throws RemoteException;
}
