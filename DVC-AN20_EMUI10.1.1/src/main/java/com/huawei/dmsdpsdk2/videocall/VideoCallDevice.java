package com.huawei.dmsdpsdk2.videocall;

import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DeviceParameterConst;
import java.util.ArrayList;

public class VideoCallDevice {
    private DMSDPDevice dmsdpDevice;
    private ArrayList<Integer> supportServices = new ArrayList<>();

    public VideoCallDevice(DMSDPDevice dmsdpDevice2) {
        this.dmsdpDevice = dmsdpDevice2;
        if ((dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTCAMERA_BOOLEAN) instanceof Boolean) && dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTCAMERA_BOOLEAN) != null && ((Boolean) dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTCAMERA_BOOLEAN)).booleanValue()) {
            this.supportServices.add(1);
        }
        if ((dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTMIC_BOOLEAN) instanceof Boolean) && dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTMIC_BOOLEAN) != null && ((Boolean) dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTMIC_BOOLEAN)).booleanValue()) {
            this.supportServices.add(2);
        }
        if ((dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTSPEAKER_BOOLEAN) instanceof Boolean) && dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTSPEAKER_BOOLEAN) != null && ((Boolean) dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTSPEAKER_BOOLEAN)).booleanValue()) {
            this.supportServices.add(4);
        }
        if ((dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTDISPLAY_BOOLEAN) instanceof Boolean) && dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTDISPLAY_BOOLEAN) != null && ((Boolean) dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTDISPLAY_BOOLEAN)).booleanValue()) {
            this.supportServices.add(8);
        }
        if ((dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTHFP_BOOLEAN) instanceof Boolean) && dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTHFP_BOOLEAN) != null && ((Boolean) dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTHFP_BOOLEAN)).booleanValue()) {
            this.supportServices.add(64);
        }
        if ((dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTA2DP_BOOLEAN) instanceof Boolean) && dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTA2DP_BOOLEAN) != null && ((Boolean) dmsdpDevice2.getProperties(DeviceParameterConst.DEVICE_SUPPORTA2DP_BOOLEAN)).booleanValue()) {
            this.supportServices.add(128);
        }
    }

    public VideoCallDevice(VideoCallDevice thatDevice) {
        this.dmsdpDevice = thatDevice.dmsdpDevice;
    }

    public ArrayList<Integer> getSupportServices() {
        return this.supportServices;
    }

    public DMSDPDevice getDmsdpDevice() {
        return this.dmsdpDevice;
    }

    public boolean equals(Object that) {
        if (that != null && (that instanceof VideoCallDevice)) {
            return this.dmsdpDevice.getDeviceId().equals(((VideoCallDevice) that).dmsdpDevice.getDeviceId());
        }
        return false;
    }

    public int hashCode() {
        return this.dmsdpDevice.getDeviceId().hashCode();
    }
}
