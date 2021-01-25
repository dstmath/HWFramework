package com.huawei.dmsdpsdk2;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.IDMSDPListener;
import com.huawei.dmsdpsdk2.IDataListener;
import com.huawei.dmsdpsdk2.IDiscoverListener;
import com.huawei.dmsdpsdk2.notification.NotificationData;
import com.huawei.dmsdpsdk2.sensor.ISensorDataListener;
import com.huawei.dmsdpsdk2.sensor.VirtualSensor;
import com.huawei.dmsdpsdk2.vibrate.VirtualVibrator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IDMSDPAdapter extends IInterface {
    int connectDevice(int i, int i2, DMSDPDevice dMSDPDevice, Map map) throws RemoteException;

    int delAuthDevice(int i) throws RemoteException;

    int disconnectDevice(int i, int i2, DMSDPDevice dMSDPDevice) throws RemoteException;

    int getModemStatus(List<DMSDPVirtualDevice> list) throws RemoteException;

    int getSensorList(String str, int i, List<VirtualSensor> list) throws RemoteException;

    int getVibrateList(String str, List<VirtualVibrator> list) throws RemoteException;

    int getVirtualCameraList(int i, List<String> list) throws RemoteException;

    boolean hasInit() throws RemoteException;

    int keepChannelActive(String str, int i) throws RemoteException;

    int queryAuthDevice(int i, List<DMSDPDevice> list) throws RemoteException;

    int registerDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException;

    int registerDataListener(int i, DMSDPDevice dMSDPDevice, int i2, IDataListener iDataListener) throws RemoteException;

    void reportData(String str, long j, long j2, int i) throws RemoteException;

    int requestDeviceService(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException;

    int sendData(int i, DMSDPDevice dMSDPDevice, int i2, byte[] bArr) throws RemoteException;

    int sendNotification(String str, int i, NotificationData notificationData, int i2) throws RemoteException;

    int setVirtualDevicePolicy(int i, int i2, int i3) throws RemoteException;

    int startDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException;

    int startDiscover(int i, int i2, int i3, int i4, IDiscoverListener iDiscoverListener) throws RemoteException;

    int startScan(int i, int i2) throws RemoteException;

    int stopDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2) throws RemoteException;

    int stopDiscover(int i, int i2, IDiscoverListener iDiscoverListener) throws RemoteException;

    int stopScan(int i, int i2) throws RemoteException;

    int subscribeSensorDataListener(ISensorDataListener iSensorDataListener, VirtualSensor virtualSensor, int i) throws RemoteException;

    int switchModem(String str, int i, String str2, int i2) throws RemoteException;

    int unRegisterDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException;

    int unRegisterDataListener(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException;

    int unSubscribeSensorDataListener(ISensorDataListener iSensorDataListener) throws RemoteException;

    int updateDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException;

    int vibrate(String str, int i, long j) throws RemoteException;

    int vibrateCancel(String str, int i) throws RemoteException;

    int vibrateRepeat(String str, int i, long[] jArr, int i2) throws RemoteException;

    public static class Default implements IDMSDPAdapter {
        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int startDiscover(int businessId, int protocol, int deviceFilter, int serviceFilter, IDiscoverListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int stopDiscover(int businessId, int protocol, IDiscoverListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int startScan(int businessId, int protocol) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int stopScan(int businessId, int protocol) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int connectDevice(int businessId, int channelType, DMSDPDevice device, Map params) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int disconnectDevice(int businessId, int channelType, DMSDPDevice device) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int requestDeviceService(int businessId, DMSDPDevice device, int serviceType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int startDeviceService(int businessId, DMSDPDeviceService serivce, int type, Map params) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int stopDeviceService(int businessId, DMSDPDeviceService service, int type) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int updateDeviceService(int businessId, DMSDPDeviceService service, int action, Map params) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int registerDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int unRegisterDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int registerDataListener(int businessId, DMSDPDevice device, int dataType, IDataListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int unRegisterDataListener(int businessId, DMSDPDevice device, int dataType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int sendData(int businessId, DMSDPDevice device, int dataType, byte[] data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public boolean hasInit() throws RemoteException {
            return false;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int getVirtualCameraList(int businessId, List<String> list) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int queryAuthDevice(int businessId, List<DMSDPDevice> list) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int delAuthDevice(int businessId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int setVirtualDevicePolicy(int bussid, int module, int policy) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int switchModem(String deviceId, int mode, String varStr, int varInt) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int getModemStatus(List<DMSDPVirtualDevice> list) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public void reportData(String apiName, long callTime, long startTime, int result) throws RemoteException {
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int getSensorList(String deviceId, int sensorType, List<VirtualSensor> list) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int subscribeSensorDataListener(ISensorDataListener listener, VirtualSensor sensor, int rate) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int unSubscribeSensorDataListener(ISensorDataListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int getVibrateList(String deviceId, List<VirtualVibrator> list) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int vibrate(String deviceId, int vibrateId, long milliseconds) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int vibrateRepeat(String deviceId, int vibrateId, long[] pattern, int repeat) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int vibrateCancel(String deviceId, int vibrateId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int sendNotification(String deviceId, int notificationId, NotificationData notification, int operationMode) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
        public int keepChannelActive(String deviceId, int duration) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDMSDPAdapter {
        private static final String DESCRIPTOR = "com.huawei.dmsdpsdk.IDMSDPAdapter";
        static final int TRANSACTION_connectDevice = 5;
        static final int TRANSACTION_delAuthDevice = 19;
        static final int TRANSACTION_disconnectDevice = 6;
        static final int TRANSACTION_getModemStatus = 22;
        static final int TRANSACTION_getSensorList = 24;
        static final int TRANSACTION_getVibrateList = 27;
        static final int TRANSACTION_getVirtualCameraList = 17;
        static final int TRANSACTION_hasInit = 16;
        static final int TRANSACTION_keepChannelActive = 32;
        static final int TRANSACTION_queryAuthDevice = 18;
        static final int TRANSACTION_registerDMSDPListener = 11;
        static final int TRANSACTION_registerDataListener = 13;
        static final int TRANSACTION_reportData = 23;
        static final int TRANSACTION_requestDeviceService = 7;
        static final int TRANSACTION_sendData = 15;
        static final int TRANSACTION_sendNotification = 31;
        static final int TRANSACTION_setVirtualDevicePolicy = 20;
        static final int TRANSACTION_startDeviceService = 8;
        static final int TRANSACTION_startDiscover = 1;
        static final int TRANSACTION_startScan = 3;
        static final int TRANSACTION_stopDeviceService = 9;
        static final int TRANSACTION_stopDiscover = 2;
        static final int TRANSACTION_stopScan = 4;
        static final int TRANSACTION_subscribeSensorDataListener = 25;
        static final int TRANSACTION_switchModem = 21;
        static final int TRANSACTION_unRegisterDMSDPListener = 12;
        static final int TRANSACTION_unRegisterDataListener = 14;
        static final int TRANSACTION_unSubscribeSensorDataListener = 26;
        static final int TRANSACTION_updateDeviceService = 10;
        static final int TRANSACTION_vibrate = 28;
        static final int TRANSACTION_vibrateCancel = 30;
        static final int TRANSACTION_vibrateRepeat = 29;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDMSDPAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDMSDPAdapter)) {
                return new Proxy(obj);
            }
            return (IDMSDPAdapter) iin;
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
            VirtualSensor _arg18;
            NotificationData _arg23;
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
                        boolean hasInit = hasInit();
                        reply.writeNoException();
                        reply.writeInt(hasInit ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        List<String> _arg111 = data.createStringArrayList();
                        int _result16 = getVirtualCameraList(_arg010, _arg111);
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        reply.writeStringList(_arg111);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        ArrayList createTypedArrayList = data.createTypedArrayList(DMSDPDevice.CREATOR);
                        int _result17 = queryAuthDevice(_arg011, createTypedArrayList);
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        reply.writeTypedList(createTypedArrayList);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = delAuthDevice(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = setVirtualDevicePolicy(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = switchModem(data.readString(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        ArrayList createTypedArrayList2 = data.createTypedArrayList(DMSDPVirtualDevice.CREATOR);
                        int _result21 = getModemStatus(createTypedArrayList2);
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        reply.writeTypedList(createTypedArrayList2);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        reportData(data.readString(), data.readLong(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        int _arg112 = data.readInt();
                        ArrayList createTypedArrayList3 = data.createTypedArrayList(VirtualSensor.CREATOR);
                        int _result22 = getSensorList(_arg012, _arg112, createTypedArrayList3);
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        reply.writeTypedList(createTypedArrayList3);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        ISensorDataListener _arg013 = ISensorDataListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg18 = VirtualSensor.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        int _result23 = subscribeSensorDataListener(_arg013, _arg18, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = unSubscribeSensorDataListener(ISensorDataListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg014 = data.readString();
                        ArrayList createTypedArrayList4 = data.createTypedArrayList(VirtualVibrator.CREATOR);
                        int _result25 = getVibrateList(_arg014, createTypedArrayList4);
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        reply.writeTypedList(createTypedArrayList4);
                        return true;
                    case TRANSACTION_vibrate /* 28 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = vibrate(data.readString(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = vibrateRepeat(data.readString(), data.readInt(), data.createLongArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case TRANSACTION_vibrateCancel /* 30 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = vibrateCancel(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case TRANSACTION_sendNotification /* 31 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        int _arg113 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = NotificationData.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        int _result29 = sendNotification(_arg015, _arg113, _arg23, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        int _result30 = keepChannelActive(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result30);
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
        public static class Proxy implements IDMSDPAdapter {
            public static IDMSDPAdapter sDefaultImpl;
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public boolean hasInit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int getVirtualCameraList(int businessId, List<String> cameraIdList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeStringList(cameraIdList);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVirtualCameraList(businessId, cameraIdList);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readStringList(cameraIdList);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int queryAuthDevice(int businessId, List<DMSDPDevice> deviceList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeTypedList(deviceList);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryAuthDevice(businessId, deviceList);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(deviceList, DMSDPDevice.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int delAuthDevice(int businessId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().delAuthDevice(businessId);
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int setVirtualDevicePolicy(int bussid, int module, int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bussid);
                    _data.writeInt(module);
                    _data.writeInt(policy);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setVirtualDevicePolicy(bussid, module, policy);
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int switchModem(String deviceId, int mode, String varStr, int varInt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(mode);
                    _data.writeString(varStr);
                    _data.writeInt(varInt);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().switchModem(deviceId, mode, varStr, varInt);
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int getModemStatus(List<DMSDPVirtualDevice> statusList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(statusList);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModemStatus(statusList);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(statusList, DMSDPVirtualDevice.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public void reportData(String apiName, long callTime, long startTime, int result) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(apiName);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(callTime);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(startTime);
                        try {
                            _data.writeInt(result);
                            if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().reportData(apiName, callTime, startTime, result);
                            _reply.recycle();
                            _data.recycle();
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int getSensorList(String deviceId, int sensorType, List<VirtualSensor> sensorList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(sensorType);
                    _data.writeTypedList(sensorList);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSensorList(deviceId, sensorType, sensorList);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(sensorList, VirtualSensor.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int subscribeSensorDataListener(ISensorDataListener listener, VirtualSensor sensor, int rate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (sensor != null) {
                        _data.writeInt(1);
                        sensor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(rate);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().subscribeSensorDataListener(listener, sensor, rate);
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int unSubscribeSensorDataListener(ISensorDataListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unSubscribeSensorDataListener(listener);
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int getVibrateList(String deviceId, List<VirtualVibrator> vibrateList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeTypedList(vibrateList);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVibrateList(deviceId, vibrateList);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(vibrateList, VirtualVibrator.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int vibrate(String deviceId, int vibrateId, long milliseconds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(vibrateId);
                    _data.writeLong(milliseconds);
                    if (!this.mRemote.transact(Stub.TRANSACTION_vibrate, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().vibrate(deviceId, vibrateId, milliseconds);
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int vibrateRepeat(String deviceId, int vibrateId, long[] pattern, int repeat) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(vibrateId);
                    _data.writeLongArray(pattern);
                    _data.writeInt(repeat);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().vibrateRepeat(deviceId, vibrateId, pattern, repeat);
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int vibrateCancel(String deviceId, int vibrateId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(vibrateId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_vibrateCancel, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().vibrateCancel(deviceId, vibrateId);
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int sendNotification(String deviceId, int notificationId, NotificationData notification, int operationMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(notificationId);
                    if (notification != null) {
                        _data.writeInt(1);
                        notification.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(operationMode);
                    if (!this.mRemote.transact(Stub.TRANSACTION_sendNotification, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendNotification(deviceId, notificationId, notification, operationMode);
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

            @Override // com.huawei.dmsdpsdk2.IDMSDPAdapter
            public int keepChannelActive(String deviceId, int duration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(duration);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().keepChannelActive(deviceId, duration);
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
        }

        public static boolean setDefaultImpl(IDMSDPAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDMSDPAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
