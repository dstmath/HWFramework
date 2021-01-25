package com.huawei.nearbysdk.closeRange;

import com.huawei.nearbysdk.BleScanLevel;

public interface CloseRangeInterface {
    boolean setFrequency(CloseRangeBusinessType closeRangeBusinessType, BleScanLevel bleScanLevel);

    boolean subscribeDevice(CloseRangeDeviceFilter closeRangeDeviceFilter, CloseRangeDeviceListener closeRangeDeviceListener);

    boolean subscribeEvent(CloseRangeEventFilter closeRangeEventFilter, CloseRangeEventListener closeRangeEventListener);

    boolean unSubscribeDevice(CloseRangeDeviceFilter closeRangeDeviceFilter);

    boolean unSubscribeEvent(CloseRangeEventFilter closeRangeEventFilter);
}
