package com.huawei.msdp.devicestatus;

import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class HwMsdpDeviceStatusChangeEventAdapter {
    private HwMSDPDeviceStatusChangeEvent mHwMsdpDevStatusChangeEvent;

    private HwMsdpDeviceStatusChangeEventAdapter() {
    }

    public HwMsdpDeviceStatusChangeEventAdapter(HwMSDPDeviceStatusChangeEvent status) {
        this.mHwMsdpDevStatusChangeEvent = status;
    }

    public Iterable<HwMsdpDeviceStatusEventAdapter> getDeviceStatusRecognitionEvents() {
        Iterable<HwMSDPDeviceStatusEvent> events = this.mHwMsdpDevStatusChangeEvent.getDeviceStatusRecognitionEvents();
        if (events == null) {
            return null;
        }
        List<HwMsdpDeviceStatusEventAdapter> eventAdapters = new ArrayList<>();
        for (HwMSDPDeviceStatusEvent event : events) {
            eventAdapters.add(new HwMsdpDeviceStatusEventAdapter(event));
        }
        return eventAdapters;
    }
}
