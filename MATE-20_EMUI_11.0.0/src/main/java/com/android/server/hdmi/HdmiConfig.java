package com.android.server.hdmi;

final class HdmiConfig {
    static final int ADDRESS_ALLOCATION_RETRY = 3;
    static final int DEVICE_POLLING_RETRY = 1;
    static final boolean HIDE_DEVICES_BEHIND_LEGACY_SWITCH = true;
    static final int HOTPLUG_DETECTION_RETRY = 1;
    static final int IRT_MS = 300;
    static final int RETRANSMISSION_COUNT = 1;
    static final int TIMEOUT_MS = 2000;
    static final int TIMEOUT_RETRY = 5;

    private HdmiConfig() {
    }
}
