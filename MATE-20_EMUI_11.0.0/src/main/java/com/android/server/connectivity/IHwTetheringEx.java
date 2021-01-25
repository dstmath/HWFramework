package com.android.server.connectivity;

public interface IHwTetheringEx {
    public static final int ACTION_TETHERING_START = 1;
    public static final int ACTION_TETHERING_STOP = 2;

    default boolean isConflictWithUsbP2p(int actionType) {
        return false;
    }
}
