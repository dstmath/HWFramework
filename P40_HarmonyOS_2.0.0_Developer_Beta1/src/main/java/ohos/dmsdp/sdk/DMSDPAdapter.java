package ohos.dmsdp.sdk;

import android.content.Context;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.dmsdp.sdk.notification.NotificationData;
import ohos.dmsdp.sdk.sensor.SensorDataListener;
import ohos.dmsdp.sdk.sensor.VirtualSensor;
import ohos.dmsdp.sdk.vibrate.VirtualVibrator;

public class DMSDPAdapter {
    private static final int COLLECTION_SIZE = 8;
    private static final String TAG = "DMSDPServiceJar";
    protected final HashMap<DMSDPListener, DMSDPListenerTransport> mDMSDPListenerTransportMap = new HashMap<>(8);
    protected DMSDPServiceWrapper mDMSDPServiceWrapper;
    protected final HashMap<DataListener, DataListenerTransport> mDataListenerTransportMap = new HashMap<>(8);
    protected final HashMap<DiscoverListener, DiscoverListenerTransport> mDiscoverListenerTransportMap = new HashMap<>(8);

    public int delAuthDevice(int i) {
        return -16;
    }

    public int deleteTrustDevice(int i, String str) {
        return -16;
    }

    /* access modifiers changed from: protected */
    public DMSDPAdapter getAdapterObject() {
        return null;
    }

    public IInterface getDMSDPService() {
        return null;
    }

    /* access modifiers changed from: protected */
    public Object getLock() {
        return null;
    }

    public Looper getLooper() {
        return null;
    }

    public int getModemStatus(List<DMSDPVirtualDevice> list) {
        return -16;
    }

    public int getSensorList(String str, int i, List<VirtualSensor> list) {
        return -16;
    }

    public int getTrustDeviceList(int i, List<DMSDPDevice> list) {
        return -16;
    }

    public int getVibrateList(String str, List<VirtualVibrator> list) {
        return -16;
    }

    public int getVirtualCameraList(int i, List<String> list) {
        return -16;
    }

    public int keepChannelActive(String str, int i) {
        return -16;
    }

    public int queryAuthDevice(int i, List<DMSDPDevice> list) {
        return -16;
    }

    public int reportData(String str, long j, long j2, int i) {
        return -16;
    }

    public void reportData(Map<String, Object> map) {
    }

    public int sendHotWord(int i, String str) {
        return -16;
    }

    public int sendKeyEvent(int i, int i2, int i3) {
        return -16;
    }

    public int sendNotification(String str, int i, NotificationData notificationData, int i2) {
        return -16;
    }

    public int setDeviceInfo(int i, DeviceInfo deviceInfo) {
        return -16;
    }

    public void setSecureFileListener(int i, ISecureFileListener iSecureFileListener) {
    }

    public int setVideoSurface(int i, Surface surface) {
        return -16;
    }

    public int setVirtualDevicePolicy(int i, int i2, int i3) {
        return -16;
    }

    public int subscribeSensorDataListener(SensorDataListener sensorDataListener, VirtualSensor virtualSensor, int i) {
        return -16;
    }

    public int switchModem(String str, int i, String str2, int i2) {
        return -16;
    }

    public int unSubscribeSensorDataListener(SensorDataListener sensorDataListener) {
        return -16;
    }

    public int vibrate(String str, int i, long j) {
        return -16;
    }

    public int vibrateCancel(String str, int i) {
        return -16;
    }

    public int vibrateRepeat(String str, int i, long[] jArr, int i2) {
        return -16;
    }

    /* access modifiers changed from: protected */
    public boolean hasNullService() {
        return this.mDMSDPServiceWrapper.hasNullService();
    }

    protected DMSDPAdapter(DMSDPServiceWrapper dMSDPServiceWrapper) {
        this.mDMSDPServiceWrapper = dMSDPServiceWrapper;
    }

    public static int createInstance(Context context, DMSDPAdapterCallback dMSDPAdapterCallback) {
        return DMSDPAdapterProxy.createInstance(context, dMSDPAdapterCallback);
    }

    public static int createAgentInstance(Context context, DMSDPAdapterCallback dMSDPAdapterCallback) {
        HwLog.e(TAG, "enter create agent instance");
        return DMSDPAdapterAgent.createInstance(context, dMSDPAdapterCallback);
    }

    public static void releaseInstance() {
        DMSDPAdapterProxy.releaseInstance();
    }

    public static void disableVirtualAudio() {
        DMSDPAdapterProxy.disableVirtualAudio();
    }

    public static int sendHiSightMotionEvent(MotionEvent motionEvent) {
        return DMSDPAdapterAgent.sendHiSightMotionEvent(motionEvent);
    }

    public static int sendHiSightKeyEvent(KeyEvent keyEvent) {
        return DMSDPAdapterAgent.sendHiSightKeyEvent(keyEvent);
    }

    /* access modifiers changed from: protected */
    public DMSDPServiceWrapper getServiceWrapper() {
        return this.mDMSDPServiceWrapper;
    }

    /* access modifiers changed from: protected */
    public boolean validateInit() {
        return getAdapterObject() != null && !getAdapterObject().getServiceWrapper().hasNullService();
    }

    public int registerDMSDPListener(int i, DMSDPListener dMSDPListener) {
        synchronized (getLock()) {
            if (dMSDPListener == null) {
                HwLog.e(TAG, "registerDMSDPListener listener null");
                return -2;
            }
            if (validateInit()) {
                Looper looper = getAdapterObject().getLooper();
                if (looper != null) {
                    DMSDPListenerTransport dMSDPListenerTransport = this.mDMSDPListenerTransportMap.get(dMSDPListener);
                    if (dMSDPListenerTransport == null) {
                        dMSDPListenerTransport = new DMSDPListenerTransport(dMSDPListener, looper);
                        this.mDMSDPListenerTransportMap.put(dMSDPListener, dMSDPListenerTransport);
                    }
                    try {
                        return getAdapterObject().getServiceWrapper().registerDMSDPListener(i, dMSDPListenerTransport);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "registerDMSDPListener ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "registerDMSDPListener DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int unRegisterDMSDPListener(int i, DMSDPListener dMSDPListener) {
        synchronized (getLock()) {
            if (dMSDPListener == null) {
                HwLog.e(TAG, "unRegisterDMSDPListener listener null");
                return -2;
            } else if (!validateInit()) {
                HwLog.e(TAG, "unRegisterDMSDPListener DMSDPService is null, createInstance ERROR");
                return -2;
            } else {
                DMSDPListenerTransport dMSDPListenerTransport = this.mDMSDPListenerTransportMap.get(dMSDPListener);
                if (dMSDPListenerTransport == null) {
                    HwLog.d(TAG, "DMSDPListener was not register");
                    return -4;
                }
                try {
                    this.mDMSDPListenerTransportMap.remove(dMSDPListener);
                    return getAdapterObject().getServiceWrapper().unRegisterDMSDPListener(i, dMSDPListenerTransport);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "unRegisterDMSDPListener ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    public int startDiscover(int i, int i2, int i3, int i4, DiscoverListener discoverListener) {
        synchronized (getLock()) {
            HwLog.i(TAG, "startDiscover start");
            if (validateInit()) {
                Looper looper = getAdapterObject().getLooper();
                if (looper != null) {
                    if (i2 >= 1) {
                        if (i2 <= 255) {
                            if (i3 >= 0) {
                                if (i3 <= 255) {
                                    if (i4 >= 0) {
                                        if (i4 <= 511) {
                                            DiscoverListenerTransport discoverListenerTransport = this.mDiscoverListenerTransportMap.get(discoverListener);
                                            if (discoverListenerTransport == null) {
                                                discoverListenerTransport = new DiscoverListenerTransport(discoverListener, looper);
                                                this.mDiscoverListenerTransportMap.put(discoverListener, discoverListenerTransport);
                                            }
                                            try {
                                                return getAdapterObject().getServiceWrapper().startDiscover(i, i2, i3, i4, discoverListenerTransport);
                                            } catch (RemoteException e) {
                                                HwLog.e(TAG, "startDiscover ERROR:" + e.getLocalizedMessage());
                                                return -3;
                                            }
                                        }
                                    }
                                    HwLog.e(TAG, "startDiscover service filter is not valid");
                                    return -2;
                                }
                            }
                            HwLog.e(TAG, "startDiscover device filter is not valid");
                            return -2;
                        }
                    }
                    HwLog.e(TAG, "startDiscover protocol is not valid");
                    return -2;
                }
            }
            HwLog.e(TAG, "startDiscover DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int stopDiscover(int i, int i2, DiscoverListener discoverListener) {
        synchronized (getLock()) {
            HwLog.i(TAG, "stopDiscover start");
            if (!validateInit()) {
                HwLog.e(TAG, "stopDiscover DMSDPService is null, createInstance ERROR");
                return -2;
            }
            if (i2 >= 1) {
                if (i2 <= 255) {
                    DiscoverListenerTransport discoverListenerTransport = this.mDiscoverListenerTransportMap.get(discoverListener);
                    if (discoverListenerTransport == null) {
                        HwLog.d(TAG, "DiscoverListener was not register");
                        return -4;
                    }
                    if (i2 == 255) {
                        try {
                            this.mDiscoverListenerTransportMap.remove(discoverListener);
                        } catch (RemoteException e) {
                            HwLog.e(TAG, "stopDiscover ERROR:" + e.getLocalizedMessage());
                            return -3;
                        }
                    }
                    return getAdapterObject().mDMSDPServiceWrapper.stopDiscover(i, i2, discoverListenerTransport);
                }
            }
            HwLog.e(TAG, "stopDiscover protocol is not valid");
            return -2;
        }
    }

    public int startScan(int i, int i2) {
        synchronized (getLock()) {
            HwLog.i(TAG, "startScan start");
            if (!validateInit()) {
                HwLog.e(TAG, "startScan DMSDPService is null, createInstance ERROR");
                return -2;
            } else if (i2 < 1 || i2 > 255) {
                HwLog.e(TAG, "startScan protocol is not valid");
                return -2;
            } else {
                try {
                    return getAdapterObject().getServiceWrapper().startScan(i, i2);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "startScan ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    public int stopScan(int i, int i2) {
        synchronized (getLock()) {
            HwLog.i(TAG, "stopScan start");
            if (!validateInit()) {
                HwLog.e(TAG, "stopScan DMSDPService is null, createInstance ERROR");
                return -2;
            } else if (i2 < 1 || i2 > 255) {
                HwLog.e(TAG, "stopScan protocol is not valid");
                return -2;
            } else {
                try {
                    return getAdapterObject().getServiceWrapper().stopScan(i, i2);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "stopScan ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    public int connectDevice(int i, int i2, DMSDPDevice dMSDPDevice, Map<String, Object> map) {
        synchronized (getLock()) {
            HwLog.i(TAG, "connectDevice start.");
            if (!validateInit()) {
                HwLog.e(TAG, "connectDevice DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().connectDevice(i, i2, dMSDPDevice, map);
            } catch (RemoteException e) {
                HwLog.e(TAG, "connectDevice ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int disconnectDevice(int i, int i2, DMSDPDevice dMSDPDevice) {
        synchronized (getLock()) {
            HwLog.i(TAG, "disconnectDevice start");
            if (!validateInit()) {
                HwLog.e(TAG, "disconnectDevice DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().disconnectDevice(i, i2, dMSDPDevice);
            } catch (RemoteException e) {
                HwLog.e(TAG, "disconnectDevice ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int requestDeviceService(int i, DMSDPDevice dMSDPDevice, int i2) {
        synchronized (getLock()) {
            HwLog.i(TAG, "requestDeviceService start, serviceType:" + i2);
            if (!validateInit()) {
                HwLog.e(TAG, "requestDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            } else if (i2 < 1 || i2 > 409599) {
                HwLog.e(TAG, "requestDeviceService serviceType is not valid");
                return -2;
            } else {
                try {
                    return getAdapterObject().getServiceWrapper().requestDeviceService(i, dMSDPDevice, i2);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "requestDeviceService ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    public int startDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map<String, Object> map) {
        synchronized (getLock()) {
            HwLog.i(TAG, "startDeviceService start");
            if (getAdapterObject() == null || getAdapterObject().mDMSDPServiceWrapper.hasNullService()) {
                HwLog.e(TAG, "startDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().startDeviceService(i, dMSDPDeviceService, i2, map);
            } catch (RemoteException e) {
                HwLog.e(TAG, "startDeviceService ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int stopDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2) {
        synchronized (getLock()) {
            HwLog.i(TAG, "stopDeviceService start");
            if (getAdapterObject() == null || getAdapterObject().mDMSDPServiceWrapper.hasNullService()) {
                HwLog.e(TAG, "stopDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().stopDeviceService(i, dMSDPDeviceService, i2);
            } catch (RemoteException e) {
                HwLog.e(TAG, "stopDeviceService ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int updateDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map<String, Object> map) {
        synchronized (getLock()) {
            HwLog.i(TAG, "updateDeviceService");
            if (getAdapterObject() == null) {
                HwLog.e(TAG, "updateDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().updateDeviceService(i, dMSDPDeviceService, i2, map);
            } catch (RemoteException e) {
                HwLog.e(TAG, "updateDeviceService ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int registerDataListener(int i, DMSDPDevice dMSDPDevice, int i2, DataListener dataListener) {
        synchronized (getLock()) {
            if (dataListener == null) {
                HwLog.e(TAG, "registerDataListener listener null");
                return -2;
            }
            if (validateInit()) {
                Looper looper = getAdapterObject().getLooper();
                if (looper != null) {
                    DataListenerTransport dataListenerTransport = this.mDataListenerTransportMap.get(dataListener);
                    if (dataListenerTransport == null) {
                        dataListenerTransport = new DataListenerTransport(dataListener, looper);
                        this.mDataListenerTransportMap.put(dataListener, dataListenerTransport);
                    }
                    try {
                        return getAdapterObject().getServiceWrapper().registerDataListener(i, dMSDPDevice, i2, dataListenerTransport);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "registerDataListener ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "registerDataListener DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int unRegisterDataListener(int i, DMSDPDevice dMSDPDevice, int i2) {
        synchronized (getLock()) {
            if (getAdapterObject() == null) {
                HwLog.e(TAG, "unRegisterDMSDPListener DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().unRegisterDataListener(i, dMSDPDevice, i2);
            } catch (RemoteException e) {
                HwLog.e(TAG, "unRegisterDMSDPListener ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int sendData(int i, DMSDPDevice dMSDPDevice, int i2, byte[] bArr) {
        synchronized (getLock()) {
            HwLog.i(TAG, "sendData");
            if (getAdapterObject() == null) {
                HwLog.e(TAG, "sendData DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().sendData(i, dMSDPDevice, i2, bArr);
            } catch (RemoteException e) {
                HwLog.e(TAG, "sendData ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }
}
