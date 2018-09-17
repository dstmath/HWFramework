package com.android.server.location;

public interface IHwGpsLocationManager {
    boolean checkNtpTime(long j, long j2);

    InjectTimeRecord getInjectTime(long j);

    void setGpsTime(long j, long j2);
}
