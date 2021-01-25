package com.huawei.dmsdpsdk2;

import android.os.RemoteException;
import java.util.Map;

/* access modifiers changed from: package-private */
/* compiled from: DMSDPAdapter */
public interface DMSDPServiceWrapper {
    int connectDevice(int i, int i2, DMSDPDevice dMSDPDevice, Map map) throws RemoteException;

    int disconnectDevice(int i, int i2, DMSDPDevice dMSDPDevice) throws RemoteException;

    boolean hasInit() throws RemoteException;

    boolean hasNullService();

    int registerDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException;

    int registerDataListener(int i, DMSDPDevice dMSDPDevice, int i2, IDataListener iDataListener) throws RemoteException;

    int requestDeviceService(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException;

    int sendData(int i, DMSDPDevice dMSDPDevice, int i2, byte[] bArr) throws RemoteException;

    int startDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException;

    int startDiscover(int i, int i2, int i3, int i4, IDiscoverListener iDiscoverListener) throws RemoteException;

    int startScan(int i, int i2) throws RemoteException;

    int stopDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2) throws RemoteException;

    int stopDiscover(int i, int i2, IDiscoverListener iDiscoverListener) throws RemoteException;

    int stopScan(int i, int i2) throws RemoteException;

    int unRegisterDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException;

    int unRegisterDataListener(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException;

    int updateDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map map) throws RemoteException;
}
