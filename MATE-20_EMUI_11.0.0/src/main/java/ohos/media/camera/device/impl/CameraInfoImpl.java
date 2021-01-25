package ohos.media.camera.device.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import ohos.media.camera.device.CameraInfo;

public class CameraInfoImpl implements CameraInfo {
    private int availability;
    private final String cameraId;
    private final int facingType;
    private final Map<String, Integer> physicalCameraIdAvailabilityMap = new ConcurrentHashMap();
    private final Map<String, Integer> physicalCameraLinkTypeMap = new ConcurrentHashMap();

    public CameraInfoImpl(String str, int i, int i2) {
        this.cameraId = str;
        this.facingType = i;
        this.availability = i2;
    }

    protected CameraInfoImpl(CameraInfoImpl cameraInfoImpl) {
        this.cameraId = cameraInfoImpl.cameraId;
        this.facingType = cameraInfoImpl.facingType;
        this.availability = cameraInfoImpl.availability;
        this.physicalCameraIdAvailabilityMap.putAll(cameraInfoImpl.physicalCameraIdAvailabilityMap);
        this.physicalCameraLinkTypeMap.putAll(cameraInfoImpl.physicalCameraLinkTypeMap);
    }

    @Override // ohos.media.camera.device.CameraInfo
    public List<String> getPhysicalIdList() {
        return new ArrayList(this.physicalCameraIdAvailabilityMap.keySet());
    }

    public void putPhysicalIdMap(String str, int i) {
        this.physicalCameraIdAvailabilityMap.put(str, Integer.valueOf(i));
    }

    public boolean isPhysicalCameraAvailable(String str) {
        return this.physicalCameraIdAvailabilityMap.getOrDefault(str, -1).intValue() == 1;
    }

    @Override // ohos.media.camera.device.CameraInfo
    public String getLogicalId() {
        return this.cameraId;
    }

    public void setLogicalCameraAvailability(int i) {
        this.availability = i;
    }

    public boolean isLogicalCameraAvailable() {
        return this.availability == 1;
    }

    @Override // ohos.media.camera.device.CameraInfo
    @CameraInfo.FacingType
    public int getFacingType() {
        return this.facingType;
    }

    @Override // ohos.media.camera.device.CameraInfo
    public int getDeviceLinkType(String str) {
        return this.physicalCameraLinkTypeMap.getOrDefault(str, -1).intValue();
    }

    public void setDeviceLinkType(String str, int i) {
        this.physicalCameraLinkTypeMap.put(str, Integer.valueOf(i));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CameraInfoImpl cameraInfoImpl = (CameraInfoImpl) obj;
        return this.facingType == cameraInfoImpl.facingType && this.cameraId.equals(cameraInfoImpl.cameraId);
    }

    public int hashCode() {
        return Objects.hash(this.cameraId, Integer.valueOf(this.facingType));
    }

    public String toString() {
        return "CameraInfoImpl{cameraId='" + this.cameraId + "', facingType=" + this.facingType + ", physicalIds=" + this.physicalCameraIdAvailabilityMap.keySet() + ", availabilities=" + this.physicalCameraIdAvailabilityMap.values() + ", linkTypes=" + this.physicalCameraLinkTypeMap.values() + '}';
    }
}
