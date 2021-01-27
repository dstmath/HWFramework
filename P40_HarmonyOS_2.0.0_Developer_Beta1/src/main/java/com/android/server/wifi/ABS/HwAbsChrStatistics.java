package com.android.server.wifi.ABS;

public class HwAbsChrStatistics {
    public static final int ABS_CHR_ANTENNA_PREEMPTED_SCREEN_OFF_EVENT = 5;
    public static final int ABS_CHR_ANTENNA_PREEMPTED_SCREEN_ON_EVENT = 4;
    public static final int ABS_CHR_AP_INFO = 10;
    public static final int ABS_CHR_LONG_CONNECT_EVENT = 1;
    public static final int ABS_CHR_MAX_PING_PONG_TIMES = 9;
    public static final int ABS_CHR_MO_MT_CALL_EVENT = 6;
    public static final int ABS_CHR_PING_PONG_TIMES = 8;
    public static final int ABS_CHR_SEARCH_EVENT = 3;
    public static final int ABS_CHR_SHORT_CONNECT_EVENT = 2;
    public static final int ABS_CHR_SISO_TO_MIMO_EVENT = 7;
    protected int antennaPreemptedScreenOffEvent;
    protected int antennaPreemptedScreenOnEvent;
    protected long lastUploadTime;
    protected int longConnectEvent;
    protected int maxPingPongTimes;
    protected long mimoScreenOnTime;
    protected long mimoTime;
    protected int moMtCallEvent;
    protected int pingPongTimes;
    protected int rssiL0;
    protected int rssiL1;
    protected int rssiL2;
    protected int rssiL3;
    protected int rssiL4;
    protected int searchEvent;
    protected int shortConnectEvent;
    protected long sisoScreenOnTime;
    protected long sisoTime;
    protected int sisoToMimoEvent;

    public HwAbsChrStatistics() {
        this.longConnectEvent = 0;
        this.shortConnectEvent = 0;
        this.searchEvent = 0;
        this.antennaPreemptedScreenOnEvent = 0;
        this.antennaPreemptedScreenOffEvent = 0;
        this.moMtCallEvent = 0;
        this.sisoToMimoEvent = 0;
        this.pingPongTimes = 0;
        this.maxPingPongTimes = 0;
        this.sisoTime = 0;
        this.mimoTime = 0;
        this.mimoScreenOnTime = 0;
        this.sisoScreenOnTime = 0;
        this.lastUploadTime = 0;
        this.longConnectEvent = 0;
        this.shortConnectEvent = 0;
        this.searchEvent = 0;
        this.antennaPreemptedScreenOnEvent = 0;
        this.antennaPreemptedScreenOffEvent = 0;
        this.moMtCallEvent = 0;
        this.sisoToMimoEvent = 0;
        this.pingPongTimes = 0;
        this.maxPingPongTimes = 0;
        this.sisoTime = 0;
        this.mimoTime = 0;
        this.mimoScreenOnTime = 0;
        this.sisoScreenOnTime = 0;
        this.lastUploadTime = 0;
        this.rssiL0 = 0;
        this.rssiL1 = 0;
        this.rssiL2 = 0;
        this.rssiL3 = 0;
        this.rssiL4 = 0;
    }
}
