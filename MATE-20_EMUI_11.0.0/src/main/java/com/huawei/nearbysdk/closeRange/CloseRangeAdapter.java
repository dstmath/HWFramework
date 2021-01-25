package com.huawei.nearbysdk.closeRange;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import com.huawei.nearbysdk.BleScanLevel;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.INearbyAdapter;
import java.util.HashMap;

public class CloseRangeAdapter implements CloseRangeInterface {
    private static final String TAG = "CloseRangeAdapter";
    private CloseRangeBusinessCounter businessCounter = new CloseRangeBusinessCounter();
    private HashMap<CloseRangeDeviceFilter, CloseRangeDeviceListenerTransport> deviceListenerMap = new HashMap<>();
    private HashMap<CloseRangeEventFilter, CloseRangeEventListenerTransport> eventListenerMap = new HashMap<>();
    private HandlerThread handlerThread = null;
    private INearbyAdapter nearbyService = null;

    public CloseRangeAdapter(HandlerThread handlerThread2) {
        this.handlerThread = handlerThread2;
    }

    public void setNearbyService(INearbyAdapter nearbyService2) {
        this.nearbyService = nearbyService2;
    }

    @Override // com.huawei.nearbysdk.closeRange.CloseRangeInterface
    public boolean subscribeEvent(CloseRangeEventFilter eventFilter, CloseRangeEventListener eventListener) {
        HwLog.d(TAG, "subscribeEvent");
        if (!eventFilterCheck(eventFilter) || eventListener == null) {
            HwLog.e(TAG, "null input");
            return false;
        } else if (this.nearbyService == null) {
            HwLog.e(TAG, "nearbyService is null. subscribe return false");
            return false;
        } else if (this.eventListenerMap.containsKey(eventFilter)) {
            HwLog.d(TAG, "device listener already registered && return");
            return false;
        } else {
            boolean result = false;
            Looper looper = getLooper();
            if (looper == null) {
                HwLog.e(TAG, "SubscribeEvent get null looper");
                return false;
            }
            CloseRangeEventListenerTransport transport = new CloseRangeEventListenerTransport(eventListener, looper);
            try {
                result = this.nearbyService.subscribeEvent(eventFilter, transport);
                if (result) {
                    this.eventListenerMap.put(eventFilter, transport);
                    this.businessCounter.increase(eventFilter.getBusinessType());
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "remote error " + e.getMessage());
            }
            return result;
        }
    }

    private boolean eventFilterCheck(CloseRangeEventFilter eventFilter) {
        if (eventFilter == null || eventFilter.getBusinessType() == null) {
            return false;
        }
        return true;
    }

    private Looper getLooper() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            return looper;
        }
        if (this.handlerThread != null) {
            return this.handlerThread.getLooper();
        }
        HwLog.e(TAG, "can not get looper");
        return null;
    }

    @Override // com.huawei.nearbysdk.closeRange.CloseRangeInterface
    public boolean unSubscribeEvent(CloseRangeEventFilter eventFilter) {
        HwLog.d(TAG, "unSubscribeEvent");
        boolean result = false;
        if (eventFilter == null) {
            HwLog.e(TAG, "null input");
            return false;
        } else if (this.nearbyService == null) {
            HwLog.e(TAG, "nearbyService is null");
            return false;
        } else if (!this.eventListenerMap.containsKey(eventFilter)) {
            HwLog.e(TAG, "not subscribe yet");
            return false;
        } else {
            try {
                result = this.nearbyService.unSubscribeEvent(eventFilter);
                if (result) {
                    this.eventListenerMap.remove(eventFilter);
                    this.businessCounter.decrease(eventFilter.getBusinessType());
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "remote error" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    @Override // com.huawei.nearbysdk.closeRange.CloseRangeInterface
    public boolean subscribeDevice(CloseRangeDeviceFilter deviceFilter, CloseRangeDeviceListener deviceListener) {
        HwLog.d(TAG, "subscribeDevice");
        if (!deviceFilterCheck(deviceFilter) || deviceListener == null) {
            HwLog.e(TAG, "null input");
            return false;
        } else if (this.nearbyService == null) {
            HwLog.e(TAG, "nearbyService is null. subscribe return false");
            return false;
        } else if (this.deviceListenerMap.containsKey(deviceFilter)) {
            HwLog.d(TAG, "device listener already registered && return");
            return false;
        } else {
            boolean result = false;
            Looper looper = getLooper();
            if (looper == null) {
                HwLog.e(TAG, "SubscribeEvent get null looper");
                return false;
            }
            CloseRangeDeviceListenerTransport transport = new CloseRangeDeviceListenerTransport(deviceListener, looper);
            try {
                result = this.nearbyService.subscribeDevice(deviceFilter, transport);
                if (result) {
                    this.deviceListenerMap.put(deviceFilter, transport);
                    this.businessCounter.increase(deviceFilter.getBusinessType());
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "remote error " + e.getMessage());
            }
            return result;
        }
    }

    private boolean deviceFilterCheck(CloseRangeDeviceFilter deviceFilter) {
        if (deviceFilter == null || deviceFilter.getBusinessType() == null || deviceFilter.getDeviceMAC() == null) {
            return false;
        }
        return true;
    }

    @Override // com.huawei.nearbysdk.closeRange.CloseRangeInterface
    public boolean unSubscribeDevice(CloseRangeDeviceFilter deviceFilter) {
        HwLog.d(TAG, "unSubscribeDevice");
        if (!deviceFilterCheck(deviceFilter)) {
            HwLog.e(TAG, "null input");
            return false;
        } else if (this.nearbyService == null) {
            HwLog.e(TAG, "nearbyService is null");
            return false;
        } else if (!this.deviceListenerMap.containsKey(deviceFilter)) {
            HwLog.e(TAG, "not subscribe yet");
            return false;
        } else {
            boolean result = false;
            try {
                result = this.nearbyService.unSubscribeDevice(deviceFilter);
                if (result) {
                    this.deviceListenerMap.remove(deviceFilter);
                    this.businessCounter.decrease(deviceFilter.getBusinessType());
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "remote error" + e.getMessage());
            }
            return result;
        }
    }

    @Override // com.huawei.nearbysdk.closeRange.CloseRangeInterface
    public boolean setFrequency(CloseRangeBusinessType type, BleScanLevel frequency) {
        HwLog.d(TAG, "setFrequency");
        if (type == null || frequency == null) {
            HwLog.e(TAG, "null input");
            return false;
        } else if (this.nearbyService == null) {
            HwLog.e(TAG, "nearbyService is null");
            return false;
        } else if (!this.businessCounter.containsType(type)) {
            HwLog.e(TAG, "no such business type");
            return false;
        } else {
            try {
                return this.nearbyService.setFrequency(type, frequency);
            } catch (RemoteException e) {
                HwLog.e(TAG, "remote error" + e.getLocalizedMessage());
                return false;
            }
        }
    }
}
