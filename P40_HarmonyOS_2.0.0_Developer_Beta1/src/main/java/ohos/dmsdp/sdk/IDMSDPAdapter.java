package ohos.dmsdp.sdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ohos.dmsdp.sdk.IDMSDPListener;
import ohos.dmsdp.sdk.IDataListener;
import ohos.dmsdp.sdk.IDiscoverListener;
import ohos.dmsdp.sdk.notification.NotificationData;
import ohos.dmsdp.sdk.sensor.ISensorDataListener;
import ohos.dmsdp.sdk.sensor.VirtualSensor;
import ohos.dmsdp.sdk.vibrate.VirtualVibrator;

public interface IDMSDPAdapter extends IInterface {

    public static class Default implements IDMSDPAdapter {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int connectDevice(int i, int i2, DMSDPDevice dMSDPDevice, Map map) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int delAuthDevice(int i) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int disconnectDevice(int i, int i2, DMSDPDevice dMSDPDevice) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int getModemStatus(List<DMSDPVirtualDevice> list) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int getSensorList(String str, int i, List<VirtualSensor> list) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int getVibrateList(String str, List<VirtualVibrator> list) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int getVirtualCameraList(int i, List<String> list) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public boolean hasInit() throws RemoteException {
            return false;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int keepChannelActive(String str, int i) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int queryAuthDevice(int i, List<DMSDPDevice> list) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int registerDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int registerDataListener(int i, DMSDPDevice dMSDPDevice, int i2, IDataListener iDataListener) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public void reportData(String str, long j, long j2, int i) throws RemoteException {
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int requestDeviceService(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int sendData(int i, DMSDPDevice dMSDPDevice, int i2, byte[] bArr) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int sendNotification(String str, int i, NotificationData notificationData, int i2) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int setVirtualDevicePolicy(int i, int i2, int i3) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int startDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int startDiscover(int i, int i2, int i3, int i4, IDiscoverListener iDiscoverListener) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int startScan(int i, int i2) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int stopDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int stopDiscover(int i, int i2, IDiscoverListener iDiscoverListener) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int stopScan(int i, int i2) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int subscribeSensorDataListener(ISensorDataListener iSensorDataListener, VirtualSensor virtualSensor, int i) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int switchModem(String str, int i, String str2, int i2) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int unRegisterDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int unRegisterDataListener(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int unSubscribeSensorDataListener(ISensorDataListener iSensorDataListener) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int updateDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int vibrate(String str, int i, long j) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int vibrateCancel(String str, int i) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.IDMSDPAdapter
        public int vibrateRepeat(String str, int i, long[] jArr, int i2) throws RemoteException {
            return 0;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDMSDPAdapter asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IDMSDPAdapter)) {
                return new Proxy(iBinder);
            }
            return (IDMSDPAdapter) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                DMSDPDevice dMSDPDevice = null;
                NotificationData notificationData = null;
                VirtualSensor virtualSensor = null;
                DMSDPDevice dMSDPDevice2 = null;
                DMSDPDevice dMSDPDevice3 = null;
                DMSDPDevice dMSDPDevice4 = null;
                DMSDPDeviceService dMSDPDeviceService = null;
                DMSDPDeviceService dMSDPDeviceService2 = null;
                DMSDPDeviceService dMSDPDeviceService3 = null;
                DMSDPDevice dMSDPDevice5 = null;
                DMSDPDevice dMSDPDevice6 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int startDiscover = startDiscover(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), IDiscoverListener.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(startDiscover);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int stopDiscover = stopDiscover(parcel.readInt(), parcel.readInt(), IDiscoverListener.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(stopDiscover);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        int startScan = startScan(parcel.readInt(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(startScan);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        int stopScan = stopScan(parcel.readInt(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(stopScan);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt = parcel.readInt();
                        int readInt2 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            dMSDPDevice = DMSDPDevice.CREATOR.createFromParcel(parcel);
                        }
                        int connectDevice = connectDevice(readInt, readInt2, dMSDPDevice, parcel.readHashMap(getClass().getClassLoader()));
                        parcel2.writeNoException();
                        parcel2.writeInt(connectDevice);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt3 = parcel.readInt();
                        int readInt4 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            dMSDPDevice6 = DMSDPDevice.CREATOR.createFromParcel(parcel);
                        }
                        int disconnectDevice = disconnectDevice(readInt3, readInt4, dMSDPDevice6);
                        parcel2.writeNoException();
                        parcel2.writeInt(disconnectDevice);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt5 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            dMSDPDevice5 = DMSDPDevice.CREATOR.createFromParcel(parcel);
                        }
                        int requestDeviceService = requestDeviceService(readInt5, dMSDPDevice5, parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(requestDeviceService);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt6 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            dMSDPDeviceService3 = DMSDPDeviceService.CREATOR.createFromParcel(parcel);
                        }
                        int startDeviceService = startDeviceService(readInt6, dMSDPDeviceService3, parcel.readInt(), parcel.readHashMap(getClass().getClassLoader()));
                        parcel2.writeNoException();
                        parcel2.writeInt(startDeviceService);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt7 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            dMSDPDeviceService2 = DMSDPDeviceService.CREATOR.createFromParcel(parcel);
                        }
                        int stopDeviceService = stopDeviceService(readInt7, dMSDPDeviceService2, parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(stopDeviceService);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt8 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            dMSDPDeviceService = DMSDPDeviceService.CREATOR.createFromParcel(parcel);
                        }
                        int updateDeviceService = updateDeviceService(readInt8, dMSDPDeviceService, parcel.readInt(), parcel.readHashMap(getClass().getClassLoader()));
                        parcel2.writeNoException();
                        parcel2.writeInt(updateDeviceService);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        int registerDMSDPListener = registerDMSDPListener(parcel.readInt(), IDMSDPListener.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(registerDMSDPListener);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        int unRegisterDMSDPListener = unRegisterDMSDPListener(parcel.readInt(), IDMSDPListener.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(unRegisterDMSDPListener);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt9 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            dMSDPDevice4 = DMSDPDevice.CREATOR.createFromParcel(parcel);
                        }
                        int registerDataListener = registerDataListener(readInt9, dMSDPDevice4, parcel.readInt(), IDataListener.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(registerDataListener);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt10 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            dMSDPDevice3 = DMSDPDevice.CREATOR.createFromParcel(parcel);
                        }
                        int unRegisterDataListener = unRegisterDataListener(readInt10, dMSDPDevice3, parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(unRegisterDataListener);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt11 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            dMSDPDevice2 = DMSDPDevice.CREATOR.createFromParcel(parcel);
                        }
                        int sendData = sendData(readInt11, dMSDPDevice2, parcel.readInt(), parcel.createByteArray());
                        parcel2.writeNoException();
                        parcel2.writeInt(sendData);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean hasInit = hasInit();
                        parcel2.writeNoException();
                        parcel2.writeInt(hasInit ? 1 : 0);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt12 = parcel.readInt();
                        ArrayList<String> createStringArrayList = parcel.createStringArrayList();
                        int virtualCameraList = getVirtualCameraList(readInt12, createStringArrayList);
                        parcel2.writeNoException();
                        parcel2.writeInt(virtualCameraList);
                        parcel2.writeStringList(createStringArrayList);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt13 = parcel.readInt();
                        ArrayList createTypedArrayList = parcel.createTypedArrayList(DMSDPDevice.CREATOR);
                        int queryAuthDevice = queryAuthDevice(readInt13, createTypedArrayList);
                        parcel2.writeNoException();
                        parcel2.writeInt(queryAuthDevice);
                        parcel2.writeTypedList(createTypedArrayList);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        int delAuthDevice = delAuthDevice(parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(delAuthDevice);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int virtualDevicePolicy = setVirtualDevicePolicy(parcel.readInt(), parcel.readInt(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(virtualDevicePolicy);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        int switchModem = switchModem(parcel.readString(), parcel.readInt(), parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(switchModem);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        ArrayList createTypedArrayList2 = parcel.createTypedArrayList(DMSDPVirtualDevice.CREATOR);
                        int modemStatus = getModemStatus(createTypedArrayList2);
                        parcel2.writeNoException();
                        parcel2.writeInt(modemStatus);
                        parcel2.writeTypedList(createTypedArrayList2);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportData(parcel.readString(), parcel.readLong(), parcel.readLong(), parcel.readInt());
                        parcel2.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString = parcel.readString();
                        int readInt14 = parcel.readInt();
                        ArrayList createTypedArrayList3 = parcel.createTypedArrayList(VirtualSensor.CREATOR);
                        int sensorList = getSensorList(readString, readInt14, createTypedArrayList3);
                        parcel2.writeNoException();
                        parcel2.writeInt(sensorList);
                        parcel2.writeTypedList(createTypedArrayList3);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        ISensorDataListener asInterface = ISensorDataListener.Stub.asInterface(parcel.readStrongBinder());
                        if (parcel.readInt() != 0) {
                            virtualSensor = VirtualSensor.CREATOR.createFromParcel(parcel);
                        }
                        int subscribeSensorDataListener = subscribeSensorDataListener(asInterface, virtualSensor, parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(subscribeSensorDataListener);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        int unSubscribeSensorDataListener = unSubscribeSensorDataListener(ISensorDataListener.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(unSubscribeSensorDataListener);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString2 = parcel.readString();
                        ArrayList createTypedArrayList4 = parcel.createTypedArrayList(VirtualVibrator.CREATOR);
                        int vibrateList = getVibrateList(readString2, createTypedArrayList4);
                        parcel2.writeNoException();
                        parcel2.writeInt(vibrateList);
                        parcel2.writeTypedList(createTypedArrayList4);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        int vibrate = vibrate(parcel.readString(), parcel.readInt(), parcel.readLong());
                        parcel2.writeNoException();
                        parcel2.writeInt(vibrate);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        int vibrateRepeat = vibrateRepeat(parcel.readString(), parcel.readInt(), parcel.createLongArray(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(vibrateRepeat);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        int vibrateCancel = vibrateCancel(parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(vibrateCancel);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString3 = parcel.readString();
                        int readInt15 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            notificationData = NotificationData.CREATOR.createFromParcel(parcel);
                        }
                        int sendNotification = sendNotification(readString3, readInt15, notificationData, parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(sendNotification);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        int keepChannelActive = keepChannelActive(parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(keepChannelActive);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDMSDPAdapter {
            public static IDMSDPAdapter sDefaultImpl;
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int startDiscover(int i, int i2, int i3, int i4, IDiscoverListener iDiscoverListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeStrongBinder(iDiscoverListener != null ? iDiscoverListener.asBinder() : null);
                    if (!this.mRemote.transact(1, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startDiscover(i, i2, i3, i4, iDiscoverListener);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int stopDiscover(int i, int i2, IDiscoverListener iDiscoverListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeStrongBinder(iDiscoverListener != null ? iDiscoverListener.asBinder() : null);
                    if (!this.mRemote.transact(2, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopDiscover(i, i2, iDiscoverListener);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int startScan(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    if (!this.mRemote.transact(3, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startScan(i, i2);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int stopScan(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    if (!this.mRemote.transact(4, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopScan(i, i2);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int connectDevice(int i, int i2, DMSDPDevice dMSDPDevice, Map map) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeMap(map);
                    if (!this.mRemote.transact(5, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connectDevice(i, i2, dMSDPDevice, map);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int disconnectDevice(int i, int i2, DMSDPDevice dMSDPDevice) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnectDevice(i, i2, dMSDPDevice);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int requestDeviceService(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i2);
                    if (!this.mRemote.transact(7, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestDeviceService(i, dMSDPDevice, i2);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int startDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (dMSDPDeviceService != null) {
                        obtain.writeInt(1);
                        dMSDPDeviceService.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i2);
                    obtain.writeMap(map);
                    if (!this.mRemote.transact(8, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startDeviceService(i, dMSDPDeviceService, i2, map);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int stopDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (dMSDPDeviceService != null) {
                        obtain.writeInt(1);
                        dMSDPDeviceService.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i2);
                    if (!this.mRemote.transact(9, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopDeviceService(i, dMSDPDeviceService, i2);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int updateDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (dMSDPDeviceService != null) {
                        obtain.writeInt(1);
                        dMSDPDeviceService.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i2);
                    obtain.writeMap(map);
                    if (!this.mRemote.transact(10, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateDeviceService(i, dMSDPDeviceService, i2, map);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int registerDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeStrongBinder(iDMSDPListener != null ? iDMSDPListener.asBinder() : null);
                    if (!this.mRemote.transact(11, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerDMSDPListener(i, iDMSDPListener);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int unRegisterDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeStrongBinder(iDMSDPListener != null ? iDMSDPListener.asBinder() : null);
                    if (!this.mRemote.transact(12, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterDMSDPListener(i, iDMSDPListener);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int registerDataListener(int i, DMSDPDevice dMSDPDevice, int i2, IDataListener iDataListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i2);
                    obtain.writeStrongBinder(iDataListener != null ? iDataListener.asBinder() : null);
                    if (!this.mRemote.transact(13, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerDataListener(i, dMSDPDevice, i2, iDataListener);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int unRegisterDataListener(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i2);
                    if (!this.mRemote.transact(14, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterDataListener(i, dMSDPDevice, i2);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int sendData(int i, DMSDPDevice dMSDPDevice, int i2, byte[] bArr) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i2);
                    obtain.writeByteArray(bArr);
                    if (!this.mRemote.transact(15, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendData(i, dMSDPDevice, i2, bArr);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public boolean hasInit() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = false;
                    if (!this.mRemote.transact(16, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasInit();
                    }
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int getVirtualCameraList(int i, List<String> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeStringList(list);
                    if (!this.mRemote.transact(17, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVirtualCameraList(i, list);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.readStringList(list);
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int queryAuthDevice(int i, List<DMSDPDevice> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeTypedList(list);
                    if (!this.mRemote.transact(18, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryAuthDevice(i, list);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.readTypedList(list, DMSDPDevice.CREATOR);
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int delAuthDevice(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(19, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().delAuthDevice(i);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int setVirtualDevicePolicy(int i, int i2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    if (!this.mRemote.transact(20, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setVirtualDevicePolicy(i, i2, i3);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int switchModem(String str, int i, String str2, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeString(str2);
                    obtain.writeInt(i2);
                    if (!this.mRemote.transact(21, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().switchModem(str, i, str2, i2);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int getModemStatus(List<DMSDPVirtualDevice> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeTypedList(list);
                    if (!this.mRemote.transact(22, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModemStatus(list);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.readTypedList(list, DMSDPVirtualDevice.CREATOR);
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public void reportData(String str, long j, long j2, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeLong(j);
                    obtain.writeLong(j2);
                    obtain.writeInt(i);
                    if (this.mRemote.transact(23, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportData(str, j, j2, i);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int getSensorList(String str, int i, List<VirtualSensor> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeTypedList(list);
                    if (!this.mRemote.transact(24, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSensorList(str, i, list);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.readTypedList(list, VirtualSensor.CREATOR);
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int subscribeSensorDataListener(ISensorDataListener iSensorDataListener, VirtualSensor virtualSensor, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iSensorDataListener != null ? iSensorDataListener.asBinder() : null);
                    if (virtualSensor != null) {
                        obtain.writeInt(1);
                        virtualSensor.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(25, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().subscribeSensorDataListener(iSensorDataListener, virtualSensor, i);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int unSubscribeSensorDataListener(ISensorDataListener iSensorDataListener) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iSensorDataListener != null ? iSensorDataListener.asBinder() : null);
                    if (!this.mRemote.transact(26, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unSubscribeSensorDataListener(iSensorDataListener);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int getVibrateList(String str, List<VirtualVibrator> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeTypedList(list);
                    if (!this.mRemote.transact(27, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVibrateList(str, list);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.readTypedList(list, VirtualVibrator.CREATOR);
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int vibrate(String str, int i, long j) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    if (!this.mRemote.transact(28, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().vibrate(str, i, j);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int vibrateRepeat(String str, int i, long[] jArr, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeLongArray(jArr);
                    obtain.writeInt(i2);
                    if (!this.mRemote.transact(29, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().vibrateRepeat(str, i, jArr, i2);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int vibrateCancel(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(30, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().vibrateCancel(str, i);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int sendNotification(String str, int i, NotificationData notificationData, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    if (notificationData != null) {
                        obtain.writeInt(1);
                        notificationData.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i2);
                    if (!this.mRemote.transact(31, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendNotification(str, i, notificationData, i2);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPAdapter
            public int keepChannelActive(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(32, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().keepChannelActive(str, i);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDMSDPAdapter iDMSDPAdapter) {
            if (Proxy.sDefaultImpl != null || iDMSDPAdapter == null) {
                return false;
            }
            Proxy.sDefaultImpl = iDMSDPAdapter;
            return true;
        }

        public static IDMSDPAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
