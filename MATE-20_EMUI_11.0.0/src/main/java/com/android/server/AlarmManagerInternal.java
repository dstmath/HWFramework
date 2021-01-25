package com.android.server;

public interface AlarmManagerInternal {

    public interface InFlightListener {
        void broadcastAlarmComplete(int i);

        void broadcastAlarmPending(int i);
    }

    boolean isIdling();

    void registerInFlightListener(InFlightListener inFlightListener);

    void removeAlarmsForUid(int i);
}
