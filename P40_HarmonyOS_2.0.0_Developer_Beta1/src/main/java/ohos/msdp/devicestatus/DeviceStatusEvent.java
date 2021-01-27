package ohos.msdp.devicestatus;

import java.util.Locale;

public class DeviceStatusEvent {
    private int mDeviceStatus;
    private int mEventType;
    private long mTimestampNs;

    public DeviceStatusEvent(int i, int i2, long j) {
        this.mDeviceStatus = i;
        this.mEventType = i2;
        this.mTimestampNs = j;
    }

    public DeviceStatusEvent() {
    }

    public int getDeviceStatus() {
        return this.mDeviceStatus;
    }

    public void setDeviceStatus(int i) {
        this.mDeviceStatus = i;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public void setEventType(int i) {
        this.mEventType = i;
    }

    public long getTimestampNs() {
        return this.mTimestampNs;
    }

    public void setTimestampNs(long j) {
        this.mTimestampNs = j;
    }

    public String toString() {
        return String.format(Locale.ENGLISH, "DeviceStatus='%s',EventType='%s',TimestampNs='%s'", Integer.valueOf(this.mDeviceStatus), Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs));
    }
}
