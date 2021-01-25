package com.huawei.msdp.devicestatus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack;

public interface IMSDPDeviceStatusService extends IInterface {
    boolean disableDeviceStatusService(String str, String str2, int i) throws RemoteException;

    boolean enableDeviceStatusService(String str, String str2, int i, long j) throws RemoteException;

    boolean freeDeviceStatusService(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException;

    HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus(String str) throws RemoteException;

    String[] getSupportDeviceStatus() throws RemoteException;

    boolean registerDeviceStatusCallBack(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException;

    public static abstract class Stub extends Binder implements IMSDPDeviceStatusService {
        private static final String DESCRIPTOR = "com.huawei.msdp.devicestatus.IMSDPDeviceStatusService";
        static final int TRANSACTION_disableDeviceStatusService = 5;
        static final int TRANSACTION_enableDeviceStatusService = 4;
        static final int TRANSACTION_freeDeviceStatusService = 3;
        static final int TRANSACTION_getCurrentDeviceStatus = 6;
        static final int TRANSACTION_getSupportDeviceStatus = 1;
        static final int TRANSACTION_registerDeviceStatusCallBack = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMSDPDeviceStatusService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMSDPDeviceStatusService)) {
                return new Proxy(obj);
            }
            return (IMSDPDeviceStatusService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result = getSupportDeviceStatus();
                        reply.writeNoException();
                        reply.writeStringArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerDeviceStatusCallBack = registerDeviceStatusCallBack(data.readString(), IMSDPDeviceStatusChangedCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerDeviceStatusCallBack ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean freeDeviceStatusService = freeDeviceStatusService(data.readString(), IMSDPDeviceStatusChangedCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(freeDeviceStatusService ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableDeviceStatusService = enableDeviceStatusService(data.readString(), data.readString(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(enableDeviceStatusService ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableDeviceStatusService = disableDeviceStatusService(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disableDeviceStatusService ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        HwMSDPDeviceStatusChangeEvent _result2 = getCurrentDeviceStatus(data.readString());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMSDPDeviceStatusService {
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

            @Override // com.huawei.msdp.devicestatus.IMSDPDeviceStatusService
            public String[] getSupportDeviceStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.msdp.devicestatus.IMSDPDeviceStatusService
            public boolean registerDeviceStatusCallBack(String packageName, IMSDPDeviceStatusChangedCallBack mCallBack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(mCallBack != null ? mCallBack.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
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

            @Override // com.huawei.msdp.devicestatus.IMSDPDeviceStatusService
            public boolean freeDeviceStatusService(String packageName, IMSDPDeviceStatusChangedCallBack mCallBack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(mCallBack != null ? mCallBack.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
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

            @Override // com.huawei.msdp.devicestatus.IMSDPDeviceStatusService
            public boolean enableDeviceStatusService(String packageName, String deviceStatus, int eventType, long reportLatencyNs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(deviceStatus);
                    _data.writeInt(eventType);
                    _data.writeLong(reportLatencyNs);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
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

            @Override // com.huawei.msdp.devicestatus.IMSDPDeviceStatusService
            public boolean disableDeviceStatusService(String packageName, String deviceStatus, int eventType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(deviceStatus);
                    _data.writeInt(eventType);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
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

            @Override // com.huawei.msdp.devicestatus.IMSDPDeviceStatusService
            public HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus(String packageName) throws RemoteException {
                HwMSDPDeviceStatusChangeEvent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwMSDPDeviceStatusChangeEvent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
