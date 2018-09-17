package com.android.server.connectivity.tethering;

public interface IControlsTethering {
    public static final int STATE_AVAILABLE = 1;
    public static final int STATE_LOCAL_ONLY = 3;
    public static final int STATE_TETHERED = 2;
    public static final int STATE_UNAVAILABLE = 0;

    void notifyInterfaceStateChange(String str, TetherInterfaceStateMachine tetherInterfaceStateMachine, int i, int i2);
}
