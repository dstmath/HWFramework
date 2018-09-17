package com.android.server.wifi.ABS;

public class HwABSCHRStatistics {
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
    public int antenna_preempted_screen_off_event;
    public int antenna_preempted_screen_on_event;
    public long last_upload_time;
    public int long_connect_event;
    public int max_ping_pong_times;
    public long mimo_screen_on_time;
    public long mimo_time;
    public int mo_mt_call_event;
    public int ping_pong_times;
    public int search_event;
    public int short_connect_event;
    public long siso_screen_on_time;
    public long siso_time;
    public int siso_to_mimo_event;

    public HwABSCHRStatistics() {
        this.long_connect_event = 0;
        this.short_connect_event = 0;
        this.search_event = 0;
        this.antenna_preempted_screen_on_event = 0;
        this.antenna_preempted_screen_off_event = 0;
        this.mo_mt_call_event = 0;
        this.siso_to_mimo_event = 0;
        this.ping_pong_times = 0;
        this.max_ping_pong_times = 0;
        this.siso_time = 0;
        this.mimo_time = 0;
        this.mimo_screen_on_time = 0;
        this.siso_screen_on_time = 0;
        this.last_upload_time = 0;
        this.long_connect_event = 0;
        this.short_connect_event = 0;
        this.search_event = 0;
        this.antenna_preempted_screen_on_event = 0;
        this.antenna_preempted_screen_off_event = 0;
        this.mo_mt_call_event = 0;
        this.siso_to_mimo_event = 0;
        this.ping_pong_times = 0;
        this.max_ping_pong_times = 0;
        this.siso_time = 0;
        this.mimo_time = 0;
        this.mimo_screen_on_time = 0;
        this.siso_screen_on_time = 0;
        this.last_upload_time = 0;
    }
}
