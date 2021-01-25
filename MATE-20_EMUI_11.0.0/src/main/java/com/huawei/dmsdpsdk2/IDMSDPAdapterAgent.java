package com.huawei.dmsdpsdk2;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.IDMSDPListener;
import com.huawei.dmsdpsdk2.IDataListener;
import com.huawei.dmsdpsdk2.IDiscoverListener;
import com.huawei.dmsdpsdk2.ISecureFileListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IDMSDPAdapterAgent extends IInterface {
    int connectDevice(int i, int i2, DMSDPDevice dMSDPDevice, Map map) throws RemoteException;

    int deleteTrustDevice(int i, String str) throws RemoteException;

    int disconnectDevice(int i, int i2, DMSDPDevice dMSDPDevice) throws RemoteException;

    int getTrustDeviceList(int i, List<DMSDPDevice> list) throws RemoteException;

    boolean hasInit() throws RemoteException;

    int registerDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException;

    int registerDataListener(int i, DMSDPDevice dMSDPDevice, int i2, IDataListener iDataListener) throws RemoteException;

    void reportData(Map map) throws RemoteException;

    int requestDeviceService(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException;

    int sendData(int i, DMSDPDevice dMSDPDevice, int i2, byte[] bArr) throws RemoteException;

    int setDeviceInfo(int i, DeviceInfo deviceInfo) throws RemoteException;

    void setSecureFileListener(int i, ISecureFileListener iSecureFileListener) throws RemoteException;

    int startDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException;

    int startDiscover(int i, int i2, int i3, int i4, IDiscoverListener iDiscoverListener) throws RemoteException;

    int startScan(int i, int i2) throws RemoteException;

    int stopDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2) throws RemoteException;

    int stopDiscover(int i, int i2, IDiscoverListener iDiscoverListener) throws RemoteException;

    int stopScan(int i, int i2) throws RemoteException;

    int unRegisterDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException;

    int unRegisterDataListener(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException;

    int updateDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException;

    public static class Default implements IDMSDPAdapterAgent {
        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int startDiscover(int businessId, int protocol, int deviceFilter, int serviceFilter, IDiscoverListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int stopDiscover(int businessId, int protocol, IDiscoverListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int startScan(int businessId, int protocol) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int stopScan(int businessId, int protocol) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int connectDevice(int businessId, int channelType, DMSDPDevice device, Map params) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int disconnectDevice(int businessId, int channelType, DMSDPDevice device) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int requestDeviceService(int businessId, DMSDPDevice device, int serviceType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int startDeviceService(int businessId, DMSDPDeviceService serivce, int type, Map params) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int stopDeviceService(int businessId, DMSDPDeviceService service, int type) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int updateDeviceService(int businessId, DMSDPDeviceService service, int action, Map params) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int registerDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int unRegisterDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int registerDataListener(int businessId, DMSDPDevice device, int dataType, IDataListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int unRegisterDataListener(int businessId, DMSDPDevice device, int dataType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int sendData(int businessId, DMSDPDevice device, int dataType, byte[] data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int setDeviceInfo(int businessId, DeviceInfo deviceInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public boolean hasInit() throws RemoteException {
            return false;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int getTrustDeviceList(int businessId, List<DMSDPDevice> list) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public int deleteTrustDevice(int businessId, String deviceId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public void setSecureFileListener(int businessId, ISecureFileListener listener) throws RemoteException {
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
        public void reportData(Map reportDetails) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDMSDPAdapterAgent {
        private static final String DESCRIPTOR = "com.huawei.dmsdpsdk.IDMSDPAdapterAgent";
        static final int TRANSACTION_connectDevice = 5;
        static final int TRANSACTION_deleteTrustDevice = 19;
        static final int TRANSACTION_disconnectDevice = 6;
        static final int TRANSACTION_getTrustDeviceList = 18;
        static final int TRANSACTION_hasInit = 17;
        static final int TRANSACTION_registerDMSDPListener = 11;
        static final int TRANSACTION_registerDataListener = 13;
        static final int TRANSACTION_reportData = 21;
        static final int TRANSACTION_requestDeviceService = 7;
        static final int TRANSACTION_sendData = 15;
        static final int TRANSACTION_setDeviceInfo = 16;
        static final int TRANSACTION_setSecureFileListener = 20;
        static final int TRANSACTION_startDeviceService = 8;
        static final int TRANSACTION_startDiscover = 1;
        static final int TRANSACTION_startScan = 3;
        static final int TRANSACTION_stopDeviceService = 9;
        static final int TRANSACTION_stopDiscover = 2;
        static final int TRANSACTION_stopScan = 4;
        static final int TRANSACTION_unRegisterDMSDPListener = 12;
        static final int TRANSACTION_unRegisterDataListener = 14;
        static final int TRANSACTION_updateDeviceService = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDMSDPAdapterAgent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDMSDPAdapterAgent)) {
                return new Proxy(obj);
            }
            return (IDMSDPAdapterAgent) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DMSDPDevice _arg2;
            DMSDPDevice _arg22;
            DMSDPDevice _arg1;
            DMSDPDeviceService _arg12;
            DMSDPDeviceService _arg13;
            DMSDPDeviceService _arg14;
            DMSDPDevice _arg15;
            DMSDPDevice _arg16;
            DMSDPDevice _arg17;
            DeviceInfo _arg18;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = startDiscover(data.readInt(), data.readInt(), data.readInt(), data.readInt(), IDiscoverListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = stopDiscover(data.readInt(), data.readInt(), IDiscoverListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = startScan(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = stopScan(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg19 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = DMSDPDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result5 = connectDevice(_arg0, _arg19, _arg2, data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg110 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = DMSDPDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        int _result6 = disconnectDevice(_arg02, _arg110, _arg22);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = DMSDPDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result7 = requestDeviceService(_arg03, _arg1, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = DMSDPDeviceService.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        int _result8 = startDeviceService(_arg04, _arg12, data.readInt(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = DMSDPDeviceService.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        int _result9 = stopDeviceService(_arg05, _arg13, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = DMSDPDeviceService.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        int _result10 = updateDeviceService(_arg06, _arg14, data.readInt(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = registerDMSDPListener(data.readInt(), IDMSDPListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = unRegisterDMSDPListener(data.readInt(), IDMSDPListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = DMSDPDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        int _result13 = registerDataListener(_arg07, _arg15, data.readInt(), IDataListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = DMSDPDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        int _result14 = unRegisterDataListener(_arg08, _arg16, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg17 = DMSDPDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        int _result15 = sendData(_arg09, _arg17, data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg18 = DeviceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        int _result16 = setDeviceInfo(_arg010, _arg18);
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasInit = hasInit();
                        reply.writeNoException();
                        reply.writeInt(hasInit ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        ArrayList createTypedArrayList = data.createTypedArrayList(DMSDPDevice.CREATOR);
                        int _result17 = getTrustDeviceList(_arg011, createTypedArrayList);
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        reply.writeTypedList(createTypedArrayList);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = deleteTrustDevice(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        setSecureFileListener(data.readInt(), ISecureFileListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        reportData(data.readHashMap(getClass().getClassLoader()));
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
        public static class Proxy implements IDMSDPAdapterAgent {
            public static IDMSDPAdapterAgent sDefaultImpl;
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int startDiscover(int businessId, int protocol, int deviceFilter, int serviceFilter, IDiscoverListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeInt(protocol);
                    _data.writeInt(deviceFilter);
                    _data.writeInt(serviceFilter);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startDiscover(businessId, protocol, deviceFilter, serviceFilter, listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int stopDiscover(int businessId, int protocol, IDiscoverListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeInt(protocol);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopDiscover(businessId, protocol, listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int startScan(int businessId, int protocol) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeInt(protocol);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startScan(businessId, protocol);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int stopScan(int businessId, int protocol) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeInt(protocol);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopScan(businessId, protocol);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int connectDevice(int businessId, int channelType, DMSDPDevice device, Map params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeInt(channelType);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeMap(params);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connectDevice(businessId, channelType, device, params);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int disconnectDevice(int businessId, int channelType, DMSDPDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeInt(channelType);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnectDevice(businessId, channelType, device);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int requestDeviceService(int businessId, DMSDPDevice device, int serviceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(serviceType);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestDeviceService(businessId, device, serviceType);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int startDeviceService(int businessId, DMSDPDeviceService serivce, int type, Map params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    if (serivce != null) {
                        _data.writeInt(1);
                        serivce.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeMap(params);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startDeviceService(businessId, serivce, type, params);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int stopDeviceService(int businessId, DMSDPDeviceService service, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopDeviceService(businessId, service, type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int updateDeviceService(int businessId, DMSDPDeviceService service, int action, Map params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(action);
                    _data.writeMap(params);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateDeviceService(businessId, service, action, params);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int registerDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerDMSDPListener(businessId, listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int unRegisterDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterDMSDPListener(businessId, listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int registerDataListener(int businessId, DMSDPDevice device, int dataType, IDataListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(dataType);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerDataListener(businessId, device, dataType, listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int unRegisterDataListener(int businessId, DMSDPDevice device, int dataType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(dataType);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterDataListener(businessId, device, dataType);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int sendData(int businessId, DMSDPDevice device, int dataType, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(dataType);
                    _data.writeByteArray(data);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendData(businessId, device, dataType, data);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int setDeviceInfo(int businessId, DeviceInfo deviceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    if (deviceInfo != null) {
                        _data.writeInt(1);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceInfo(businessId, deviceInfo);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public boolean hasInit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasInit();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int getTrustDeviceList(int businessId, List<DMSDPDevice> remoteDevices) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeTypedList(remoteDevices);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTrustDeviceList(businessId, remoteDevices);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(remoteDevices, DMSDPDevice.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public int deleteTrustDevice(int businessId, String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeString(deviceId);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteTrustDevice(businessId, deviceId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public void setSecureFileListener(int businessId, ISecureFileListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSecureFileListener(businessId, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapterAgent
            public void reportData(Map reportDetails) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(reportDetails);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportData(reportDetails);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDMSDPAdapterAgent impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDMSDPAdapterAgent getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
