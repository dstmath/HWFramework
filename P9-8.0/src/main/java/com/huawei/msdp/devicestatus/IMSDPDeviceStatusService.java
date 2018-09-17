package com.huawei.msdp.devicestatus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMSDPDeviceStatusService extends IInterface {

    public static abstract class Stub extends Binder implements IMSDPDeviceStatusService {
        private static final String DESCRIPTOR = "com.huawei.msdp.devicestatus.IMSDPDeviceStatusService";
        static final int TRANSACTION_disableDeviceStatusService = 5;
        static final int TRANSACTION_enableDeviceStatusService = 4;
        static final int TRANSACTION_freeDeviceStatusService = 3;
        static final int TRANSACTION_getCurrentDeviceStatus = 6;
        static final int TRANSACTION_getSupportDeviceStatus = 1;
        static final int TRANSACTION_registerDeviceStatusCallBack = 2;

        private static class Proxy implements IMSDPDeviceStatusService {
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

            public String[] getSupportDeviceStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerDeviceStatusCallBack(String packageName, IMSDPDeviceStatusChangedCallBack mCallBack) throws RemoteException {
                IBinder iBinder = null;
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (mCallBack != null) {
                        iBinder = mCallBack.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean freeDeviceStatusService(String packageName, IMSDPDeviceStatusChangedCallBack mCallBack) throws RemoteException {
                IBinder iBinder = null;
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (mCallBack != null) {
                        iBinder = mCallBack.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableDeviceStatusService(String packageName, String deviceStatus, int eventType, long reportLatencyNs) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(deviceStatus);
                    _data.writeInt(eventType);
                    _data.writeLong(reportLatencyNs);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableDeviceStatusService(String packageName, String deviceStatus, int eventType) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(deviceStatus);
                    _data.writeInt(eventType);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    HwMSDPDeviceStatusChangeEvent _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = null;
                    } else {
                        _result = (HwMSDPDeviceStatusChangeEvent) HwMSDPDeviceStatusChangeEvent.CREATOR.createFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMSDPDeviceStatusService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IMSDPDeviceStatusService)) {
                return (IMSDPDeviceStatusService) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result2 = getSupportDeviceStatus();
                    reply.writeNoException();
                    reply.writeStringArray(_result2);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerDeviceStatusCallBack(data.readString(), com.huawei.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(!_result ? 0 : 1);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = freeDeviceStatusService(data.readString(), com.huawei.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(!_result ? 0 : 1);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableDeviceStatusService(data.readString(), data.readString(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(!_result ? 0 : 1);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disableDeviceStatusService(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(!_result ? 0 : 1);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    HwMSDPDeviceStatusChangeEvent _result3 = getCurrentDeviceStatus(data.readString());
                    reply.writeNoException();
                    if (_result3 == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    }
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean disableDeviceStatusService(String str, String str2, int i) throws RemoteException;

    boolean enableDeviceStatusService(String str, String str2, int i, long j) throws RemoteException;

    boolean freeDeviceStatusService(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException;

    HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus(String str) throws RemoteException;

    String[] getSupportDeviceStatus() throws RemoteException;

    boolean registerDeviceStatusCallBack(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException;
}
