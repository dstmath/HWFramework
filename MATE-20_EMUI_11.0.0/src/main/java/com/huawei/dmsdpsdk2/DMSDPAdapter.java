package com.huawei.dmsdpsdk2;

import android.content.Context;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.huawei.dmsdpsdk2.notification.NotificationData;
import com.huawei.dmsdpsdk2.sensor.SensorDataListener;
import com.huawei.dmsdpsdk2.sensor.VirtualSensor;
import com.huawei.dmsdpsdk2.vibrate.VirtualVibrator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DMSDPAdapter {
    private static final String TAG = "DMSDPServiceJar";
    protected final HashMap<DMSDPListener, DMSDPListenerTransport> mDMSDPListenerTransportMap = new HashMap<>(0);
    protected DMSDPServiceWrapper mDMSDPServiceWrapper;
    protected final HashMap<DataListener, DataListenerTransport> mDataListenerTransportMap = new HashMap<>(0);
    protected final HashMap<DiscoverListener, DiscoverListenerTransport> mDiscoverListenerTransportMap = new HashMap<>(0);

    /* access modifiers changed from: protected */
    public boolean hasNullService() {
        return this.mDMSDPServiceWrapper.hasNullService();
    }

    protected DMSDPAdapter(DMSDPServiceWrapper DMSDPServiceWrapper) {
        this.mDMSDPServiceWrapper = DMSDPServiceWrapper;
    }

    public static int createInstance(Context context, DMSDPAdapterCallback callback) {
        return DMSDPAdapterProxy.createInstance(context, callback);
    }

    public static void releaseInstance() {
        DMSDPAdapterProxy.releaseInstance();
    }

    public static void disableVirtualAudio() {
        DMSDPAdapterProxy.disableVirtualAudio();
    }

    public static int sendHiSightMotionEvent(MotionEvent event) {
        return DMSDPAdapterAgent.sendHiSightMotionEvent(event);
    }

    public static int sendHiSightKeyEvent(KeyEvent event) {
        return DMSDPAdapterAgent.sendHiSightKeyEvent(event);
    }

    public Looper getLooper() {
        return null;
    }

    public IInterface getDMSDPService() {
        return null;
    }

    /* access modifiers changed from: protected */
    public Object getLock() {
        return null;
    }

    /* access modifiers changed from: protected */
    public DMSDPAdapter getAdapterObject() {
        return null;
    }

    /* access modifiers changed from: protected */
    public DMSDPServiceWrapper getServiceWrapper() {
        return this.mDMSDPServiceWrapper;
    }

    /* access modifiers changed from: protected */
    public boolean validateInit() {
        if (getAdapterObject() == null || getAdapterObject().getServiceWrapper().hasNullService()) {
            return false;
        }
        return true;
    }

    public int registerDMSDPListener(int businessId, DMSDPListener listener) {
        synchronized (getLock()) {
            if (listener == null) {
                HwLog.e(TAG, "registerDMSDPListener listener null");
                return -2;
            }
            if (validateInit()) {
                Looper looper = getAdapterObject().getLooper();
                if (looper != null) {
                    DMSDPListenerTransport transport = this.mDMSDPListenerTransportMap.get(listener);
                    if (transport == null) {
                        transport = new DMSDPListenerTransport(listener, looper);
                        this.mDMSDPListenerTransportMap.put(listener, transport);
                    }
                    try {
                        return getAdapterObject().getServiceWrapper().registerDMSDPListener(businessId, transport);
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

    public int unRegisterDMSDPListener(int businessId, DMSDPListener listener) {
        synchronized (getLock()) {
            if (listener == null) {
                HwLog.e(TAG, "unRegisterDMSDPListener listener null");
                return -2;
            } else if (!validateInit()) {
                HwLog.e(TAG, "unRegisterDMSDPListener DMSDPService is null, createInstance ERROR");
                return -2;
            } else {
                DMSDPListenerTransport transport = this.mDMSDPListenerTransportMap.get(listener);
                if (transport == null) {
                    HwLog.d(TAG, "DMSDPListener was not register");
                    return -4;
                }
                try {
                    this.mDMSDPListenerTransportMap.remove(listener);
                    return getAdapterObject().getServiceWrapper().unRegisterDMSDPListener(businessId, transport);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "unRegisterDMSDPListener ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    public int startDiscover(int businessId, int protocol, int deviceFilter, int serviceFilter, DiscoverListener listener) {
        DiscoverListenerTransport transport;
        synchronized (getLock()) {
            HwLog.i(TAG, "startDiscover start");
            if (validateInit()) {
                Looper looper = getAdapterObject().getLooper();
                if (looper != null) {
                    if (protocol < 1) {
                        HwLog.e(TAG, "startDiscover protocol is not valid");
                        return -2;
                    } else if (deviceFilter < 0) {
                        HwLog.e(TAG, "startDiscover device filter is not valid");
                        return -2;
                    } else if (serviceFilter < 0) {
                        HwLog.e(TAG, "startDiscover service filter is not valid");
                        return -2;
                    } else {
                        DiscoverListenerTransport transport2 = this.mDiscoverListenerTransportMap.get(listener);
                        if (transport2 == null) {
                            DiscoverListenerTransport transport3 = new DiscoverListenerTransport(listener, looper);
                            this.mDiscoverListenerTransportMap.put(listener, transport3);
                            transport = transport3;
                        } else {
                            transport = transport2;
                        }
                        try {
                            return getAdapterObject().getServiceWrapper().startDiscover(businessId, protocol, deviceFilter, serviceFilter, transport);
                        } catch (RemoteException e) {
                            HwLog.e(TAG, "startDiscover ERROR:" + e.getLocalizedMessage());
                            return -3;
                        }
                    }
                }
            }
            HwLog.e(TAG, "startDiscover DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int stopDiscover(int businessId, int protocol, DiscoverListener listener) {
        synchronized (getLock()) {
            HwLog.i(TAG, "stopDiscover start");
            if (!validateInit()) {
                HwLog.e(TAG, "stopDiscover DMSDPService is null, createInstance ERROR");
                return -2;
            } else if (protocol < 1) {
                HwLog.e(TAG, "stopDiscover protocol is not valid");
                return -2;
            } else {
                DiscoverListenerTransport transport = this.mDiscoverListenerTransportMap.get(listener);
                if (transport == null) {
                    HwLog.d(TAG, "DiscoverListener was not register");
                    return -4;
                }
                if (protocol == 255) {
                    try {
                        this.mDiscoverListenerTransportMap.remove(listener);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "stopDiscover ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
                return getAdapterObject().mDMSDPServiceWrapper.stopDiscover(businessId, protocol, transport);
            }
        }
    }

    public int startScan(int businessId, int protocol) {
        synchronized (getLock()) {
            HwLog.i(TAG, "startScan start");
            if (!validateInit()) {
                HwLog.e(TAG, "startScan DMSDPService is null, createInstance ERROR");
                return -2;
            } else if (protocol < 1) {
                HwLog.e(TAG, "startScan protocol is not valid");
                return -2;
            } else {
                try {
                    return getAdapterObject().getServiceWrapper().startScan(businessId, protocol);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "startScan ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    public int stopScan(int businessId, int protocol) {
        synchronized (getLock()) {
            HwLog.i(TAG, "stopScan start");
            if (!validateInit()) {
                HwLog.e(TAG, "stopScan DMSDPService is null, createInstance ERROR");
                return -2;
            } else if (protocol < 1) {
                HwLog.e(TAG, "stopScan protocol is not valid");
                return -2;
            } else {
                try {
                    return getAdapterObject().getServiceWrapper().stopScan(businessId, protocol);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "stopScan ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    public int connectDevice(int businessId, int channelType, DMSDPDevice device, Map<String, Object> params) {
        synchronized (getLock()) {
            HwLog.i(TAG, "connectDevice start.");
            if (!validateInit()) {
                HwLog.e(TAG, "connectDevice DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().connectDevice(businessId, channelType, device, params);
            } catch (RemoteException e) {
                HwLog.e(TAG, "connectDevice ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int disconnectDevice(int businessId, int channelType, DMSDPDevice device) {
        synchronized (getLock()) {
            HwLog.i(TAG, "disconnectDevice start");
            if (!validateInit()) {
                HwLog.e(TAG, "disconnectDevice DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().disconnectDevice(businessId, channelType, device);
            } catch (RemoteException e) {
                HwLog.e(TAG, "disconnectDevice ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int requestDeviceService(int businessId, DMSDPDevice device, int serviceType) {
        synchronized (getLock()) {
            HwLog.i(TAG, "requestDeviceService start, serviceType:" + serviceType);
            if (!validateInit()) {
                HwLog.e(TAG, "requestDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            } else if (serviceType < 1) {
                HwLog.e(TAG, "requestDeviceService serviceType is not valid");
                return -2;
            } else {
                try {
                    return getAdapterObject().getServiceWrapper().requestDeviceService(businessId, device, serviceType);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "requestDeviceService ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    public int startDeviceService(int businessId, DMSDPDeviceService service, int type, Map<String, Object> params) {
        synchronized (getLock()) {
            HwLog.i(TAG, "startDeviceService start");
            if (getAdapterObject() == null || getAdapterObject().mDMSDPServiceWrapper.hasNullService()) {
                HwLog.e(TAG, "startDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().startDeviceService(businessId, service, type, params);
            } catch (RemoteException e) {
                HwLog.e(TAG, "startDeviceService ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int stopDeviceService(int businessId, DMSDPDeviceService service, int type) {
        synchronized (getLock()) {
            HwLog.i(TAG, "stopDeviceService start");
            if (getAdapterObject() == null || getAdapterObject().mDMSDPServiceWrapper.hasNullService()) {
                HwLog.e(TAG, "stopDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().stopDeviceService(businessId, service, type);
            } catch (RemoteException e) {
                HwLog.e(TAG, "stopDeviceService ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int updateDeviceService(int businessId, DMSDPDeviceService service, int action, Map<String, Object> params) {
        synchronized (getLock()) {
            HwLog.i(TAG, "updateDeviceService");
            if (getAdapterObject() == null) {
                HwLog.e(TAG, "updateDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().updateDeviceService(businessId, service, action, params);
            } catch (RemoteException e) {
                HwLog.e(TAG, "updateDeviceService ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int registerDataListener(int businessId, DMSDPDevice device, int dataType, DataListener listener) {
        synchronized (getLock()) {
            if (listener == null) {
                HwLog.e(TAG, "registerDataListener listener null");
                return -2;
            }
            if (validateInit()) {
                Looper looper = getAdapterObject().getLooper();
                if (looper != null) {
                    DataListenerTransport transport = this.mDataListenerTransportMap.get(listener);
                    if (transport == null) {
                        transport = new DataListenerTransport(listener, looper);
                        this.mDataListenerTransportMap.put(listener, transport);
                    }
                    try {
                        return getAdapterObject().getServiceWrapper().registerDataListener(businessId, device, dataType, transport);
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

    public int unRegisterDataListener(int businessId, DMSDPDevice device, int dataType) {
        synchronized (getLock()) {
            if (getAdapterObject() == null) {
                HwLog.e(TAG, "unRegisterDMSDPListener DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().unRegisterDataListener(businessId, device, dataType);
            } catch (RemoteException e) {
                HwLog.e(TAG, "unRegisterDMSDPListener ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int sendData(int businessId, DMSDPDevice device, int dataType, byte[] data) {
        synchronized (getLock()) {
            HwLog.i(TAG, "sendData");
            if (getAdapterObject() == null) {
                HwLog.e(TAG, "sendData DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return getAdapterObject().getServiceWrapper().sendData(businessId, device, dataType, data);
            } catch (RemoteException e) {
                HwLog.e(TAG, "sendData ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int queryAuthDevice(int businessId, List<DMSDPDevice> list) {
        return -16;
    }

    public int delAuthDevice(int businessId) {
        return -16;
    }

    public int getVirtualCameraList(int businessId, List<String> list) {
        return -16;
    }

    public int setVirtualDevicePolicy(int businessId, int module, int policy) {
        return -16;
    }

    public int switchModem(String deviceId, int mode, String varStr, int varInt) {
        return -16;
    }

    public int getModemStatus(List<DMSDPVirtualDevice> list) {
        return -16;
    }

    public int reportData(String apiName, long callTime, long startTime, int result) {
        return -16;
    }

    public int getSensorList(String deviceId, int sensorType, List<VirtualSensor> list) {
        return -16;
    }

    public int subscribeSensorDataListener(SensorDataListener listener, VirtualSensor sensor, int rate) {
        return -16;
    }

    public int unSubscribeSensorDataListener(SensorDataListener listener) {
        return -16;
    }

    public int getVibrateList(String deviceId, List<VirtualVibrator> list) {
        return -16;
    }

    public int vibrate(String deviceId, int vibrateId, long milliseconds) {
        return -16;
    }

    public int vibrateRepeat(String deviceId, int vibrateId, long[] pattern, int repeat) {
        return -16;
    }

    public int vibrateCancel(String deviceId, int vibrateId) {
        return -16;
    }

    public int sendNotification(String deviceId, int notificationId, NotificationData notification, int operationMode) {
        return -16;
    }

    public int sendKeyEvent(int businessId, int keyCode, int action) {
        return -16;
    }

    public int sendHotWord(int businessId, String hotWord) {
        return -16;
    }

    public int setDeviceInfo(int businessId, DeviceInfo deviceInfo) {
        return -16;
    }

    public int getTrustDeviceList(int businessId, List<DMSDPDevice> list) {
        return -16;
    }

    public int deleteTrustDevice(int businessId, String deviceId) {
        return -16;
    }

    public void setSecureFileListener(int businessId, ISecureFileListener listener) {
    }

    public void reportData(Map params) {
    }

    public int keepChannelActive(String deviceId, int duration) {
        return -16;
    }
}
