package com.android.internal.telephony.cat;

public enum EventCode {
    MT_CALL(0),
    CALL_CONNECTED(1),
    CALL_DISCONNECTED(2),
    LOACATION_STATUS(3),
    USER_ACTIVITY(4),
    IDLE_SCREEN_AVAILABLE(5),
    CARD_READER_STATUS(6),
    LANGUAGE_SELECTION(7),
    BROWSER_TERMINATION(8),
    DATA_AVAILABLE(9),
    CHANNEL_STATUS(10);
    
    private int mCode;

    private EventCode(int code) {
        this.mCode = code;
    }

    public int value() {
        return this.mCode;
    }

    public static EventCode fromInt(int value) {
        for (EventCode r : values()) {
            if (r.mCode == value) {
                return r;
            }
        }
        return null;
    }
}
