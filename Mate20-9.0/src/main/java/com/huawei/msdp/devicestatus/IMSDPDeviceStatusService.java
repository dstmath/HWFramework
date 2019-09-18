package com.huawei.msdp.devicestatus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack;

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
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result = getSupportDeviceStatus();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = registerDeviceStatusCallBack(data.readString(), IMSDPDeviceStatusChangedCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = freeDeviceStatusService(data.readString(), IMSDPDeviceStatusChangedCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result4 = enableDeviceStatusService(data.readString(), data.readString(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result5 = disableDeviceStatusService(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        HwMSDPDeviceStatusChangeEvent _result6 = getCurrentDeviceStatus(data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            parcel2.writeInt(1);
                            _result6.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
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

    boolean disableDeviceStatusService(String str, String str2, int i) throws RemoteException;

    boolean enableDeviceStatusService(String str, String str2, int i, long j) throws RemoteException;

    boolean freeDeviceStatusService(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException;

    HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus(String str) throws RemoteException;

    String[] getSupportDeviceStatus() throws RemoteException;

    boolean registerDeviceStatusCallBack(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException;
}
