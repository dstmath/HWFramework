package android.hardware.usb;

import android.app.PendingIntent;
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
        static final int TRANSACTION_allowUsbDebugging = 18;
        static final int TRANSACTION_clearDefaults = 14;
        static final int TRANSACTION_clearUsbDebuggingKeys = 20;
        static final int TRANSACTION_denyUsbDebugging = 19;
        static final int TRANSACTION_getCurrentAccessory = 3;
        static final int TRANSACTION_getDeviceList = 1;
        static final int TRANSACTION_getPortStatus = 22;
        static final int TRANSACTION_getPorts = 21;
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
        static final int TRANSACTION_setCurrentFunction = 16;
        static final int TRANSACTION_setDevicePackage = 5;
        static final int TRANSACTION_setPortRoles = 23;
        static final int TRANSACTION_setUsbDataUnlocked = 17;

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
                    this.mRemote.transact(Stub.TRANSACTION_getDeviceList, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        devices.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor openDevice(String deviceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceName);
                    this.mRemote.transact(Stub.TRANSACTION_openDevice, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UsbAccessory getCurrentAccessory() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UsbAccessory usbAccessory;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentAccessory, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        usbAccessory = (UsbAccessory) UsbAccessory.CREATOR.createFromParcel(_reply);
                    } else {
                        usbAccessory = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return usbAccessory;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor openAccessory(UsbAccessory accessory) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accessory != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        accessory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_openAccessory, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
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
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setDevicePackage, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        accessory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setAccessoryPackage, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasDevicePermission(UsbDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_hasDevicePermission, _data, _reply, 0);
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

            public boolean hasAccessoryPermission(UsbAccessory accessory) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accessory != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        accessory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_hasAccessoryPermission, _data, _reply, 0);
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

            public void requestDevicePermission(UsbDevice device, String packageName, PendingIntent pi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (pi != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        pi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_requestDevicePermission, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        accessory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (pi != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        pi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_requestAccessoryPermission, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_grantDevicePermission, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getDeviceList);
                        accessory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_grantAccessoryPermission, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_hasDefaults, _data, _reply, 0);
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

            public void clearDefaults(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_clearDefaults, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isFunctionEnabled, _data, _reply, 0);
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

            public void setCurrentFunction(String function) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(function);
                    this.mRemote.transact(Stub.TRANSACTION_setCurrentFunction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUsbDataUnlocked(boolean unlock) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (unlock) {
                        i = Stub.TRANSACTION_getDeviceList;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setUsbDataUnlocked, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void allowUsbDebugging(boolean alwaysAllow, String publicKey) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (alwaysAllow) {
                        i = Stub.TRANSACTION_getDeviceList;
                    }
                    _data.writeInt(i);
                    _data.writeString(publicKey);
                    this.mRemote.transact(Stub.TRANSACTION_allowUsbDebugging, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_denyUsbDebugging, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearUsbDebuggingKeys, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getPorts, _data, _reply, 0);
                    _reply.readException();
                    UsbPort[] _result = (UsbPort[]) _reply.createTypedArray(UsbPort.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UsbPortStatus getPortStatus(String portId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UsbPortStatus usbPortStatus;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(portId);
                    this.mRemote.transact(Stub.TRANSACTION_getPortStatus, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        usbPortStatus = (UsbPortStatus) UsbPortStatus.CREATOR.createFromParcel(_reply);
                    } else {
                        usbPortStatus = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return usbPortStatus;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_setPortRoles, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelFileDescriptor _result;
            UsbAccessory usbAccessory;
            UsbDevice usbDevice;
            boolean _result2;
            String _arg1;
            PendingIntent pendingIntent;
            switch (code) {
                case TRANSACTION_getDeviceList /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _arg0 = new Bundle();
                    getDeviceList(_arg0);
                    reply.writeNoException();
                    if (_arg0 != null) {
                        reply.writeInt(TRANSACTION_getDeviceList);
                        _arg0.writeToParcel(reply, TRANSACTION_getDeviceList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_openDevice /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = openDevice(data.readString());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getDeviceList);
                        _result.writeToParcel(reply, TRANSACTION_getDeviceList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getCurrentAccessory /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    UsbAccessory _result3 = getCurrentAccessory();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getDeviceList);
                        _result3.writeToParcel(reply, TRANSACTION_getDeviceList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_openAccessory /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        usbAccessory = (UsbAccessory) UsbAccessory.CREATOR.createFromParcel(data);
                    } else {
                        usbAccessory = null;
                    }
                    _result = openAccessory(usbAccessory);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getDeviceList);
                        _result.writeToParcel(reply, TRANSACTION_getDeviceList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setDevicePackage /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        usbDevice = (UsbDevice) UsbDevice.CREATOR.createFromParcel(data);
                    } else {
                        usbDevice = null;
                    }
                    setDevicePackage(usbDevice, data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAccessoryPackage /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        usbAccessory = (UsbAccessory) UsbAccessory.CREATOR.createFromParcel(data);
                    } else {
                        usbAccessory = null;
                    }
                    setAccessoryPackage(usbAccessory, data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_hasDevicePermission /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        usbDevice = (UsbDevice) UsbDevice.CREATOR.createFromParcel(data);
                    } else {
                        usbDevice = null;
                    }
                    _result2 = hasDevicePermission(usbDevice);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getDeviceList : 0);
                    return true;
                case TRANSACTION_hasAccessoryPermission /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        usbAccessory = (UsbAccessory) UsbAccessory.CREATOR.createFromParcel(data);
                    } else {
                        usbAccessory = null;
                    }
                    _result2 = hasAccessoryPermission(usbAccessory);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getDeviceList : 0);
                    return true;
                case TRANSACTION_requestDevicePermission /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        usbDevice = (UsbDevice) UsbDevice.CREATOR.createFromParcel(data);
                    } else {
                        usbDevice = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    requestDevicePermission(usbDevice, _arg1, pendingIntent);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestAccessoryPermission /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        usbAccessory = (UsbAccessory) UsbAccessory.CREATOR.createFromParcel(data);
                    } else {
                        usbAccessory = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    requestAccessoryPermission(usbAccessory, _arg1, pendingIntent);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_grantDevicePermission /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        usbDevice = (UsbDevice) UsbDevice.CREATOR.createFromParcel(data);
                    } else {
                        usbDevice = null;
                    }
                    grantDevicePermission(usbDevice, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_grantAccessoryPermission /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        usbAccessory = (UsbAccessory) UsbAccessory.CREATOR.createFromParcel(data);
                    } else {
                        usbAccessory = null;
                    }
                    grantAccessoryPermission(usbAccessory, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_hasDefaults /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = hasDefaults(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getDeviceList : 0);
                    return true;
                case TRANSACTION_clearDefaults /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearDefaults(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isFunctionEnabled /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isFunctionEnabled(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getDeviceList : 0);
                    return true;
                case TRANSACTION_setCurrentFunction /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCurrentFunction(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setUsbDataUnlocked /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUsbDataUnlocked(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_allowUsbDebugging /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    allowUsbDebugging(data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_denyUsbDebugging /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    denyUsbDebugging();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearUsbDebuggingKeys /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearUsbDebuggingKeys();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPorts /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    UsbPort[] _result4 = getPorts();
                    reply.writeNoException();
                    reply.writeTypedArray(_result4, TRANSACTION_getDeviceList);
                    return true;
                case TRANSACTION_getPortStatus /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    UsbPortStatus _result5 = getPortStatus(data.readString());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_getDeviceList);
                        _result5.writeToParcel(reply, TRANSACTION_getDeviceList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setPortRoles /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPortRoles(data.readString(), data.readInt(), data.readInt());
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

    void allowUsbDebugging(boolean z, String str) throws RemoteException;

    void clearDefaults(String str, int i) throws RemoteException;

    void clearUsbDebuggingKeys() throws RemoteException;

    void denyUsbDebugging() throws RemoteException;

    UsbAccessory getCurrentAccessory() throws RemoteException;

    void getDeviceList(Bundle bundle) throws RemoteException;

    UsbPortStatus getPortStatus(String str) throws RemoteException;

    UsbPort[] getPorts() throws RemoteException;

    void grantAccessoryPermission(UsbAccessory usbAccessory, int i) throws RemoteException;

    void grantDevicePermission(UsbDevice usbDevice, int i) throws RemoteException;

    boolean hasAccessoryPermission(UsbAccessory usbAccessory) throws RemoteException;

    boolean hasDefaults(String str, int i) throws RemoteException;

    boolean hasDevicePermission(UsbDevice usbDevice) throws RemoteException;

    boolean isFunctionEnabled(String str) throws RemoteException;

    ParcelFileDescriptor openAccessory(UsbAccessory usbAccessory) throws RemoteException;

    ParcelFileDescriptor openDevice(String str) throws RemoteException;

    void requestAccessoryPermission(UsbAccessory usbAccessory, String str, PendingIntent pendingIntent) throws RemoteException;

    void requestDevicePermission(UsbDevice usbDevice, String str, PendingIntent pendingIntent) throws RemoteException;

    void setAccessoryPackage(UsbAccessory usbAccessory, String str, int i) throws RemoteException;

    void setCurrentFunction(String str) throws RemoteException;

    void setDevicePackage(UsbDevice usbDevice, String str, int i) throws RemoteException;

    void setPortRoles(String str, int i, int i2) throws RemoteException;

    void setUsbDataUnlocked(boolean z) throws RemoteException;
}
