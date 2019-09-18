package com.huawei.android.cover;

import android.cover.HallState;

public final class HallStateEx {
    public static final int SLIDE_HALL_CLOSE = 0;
    public static final int SLIDE_HALL_OPEN = 2;
    public static final int TYPE_HALL_COVER = 0;
    public static final int TYPE_HALL_SLIDE = 1;
    public int state;
    public long timestamp;
    public int type;

    public String toString() {
        return "HallStateEx{type=" + this.type + ", state=" + this.state + ", timestamp=" + this.timestamp + "}";
    }

    public HallStateEx() {
    }

    public HallStateEx(HallState hallState) {
        this.type = hallState.type;
        this.state = hallState.state;
        this.timestamp = hallState.timestamp;
    }
}
