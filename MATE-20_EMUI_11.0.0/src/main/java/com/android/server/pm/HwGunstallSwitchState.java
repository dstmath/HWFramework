package com.android.server.pm;

public class HwGunstallSwitchState {
    private static final HwGunstallSwitchState INSTANCE = new HwGunstallSwitchState();
    private int mGunstallAppVersion = Integer.MAX_VALUE;
    private boolean mGunstallShowSwitch = false;
    private boolean mGunstallUpdateState = false;
    private int mGunstallUserState = 0;

    private HwGunstallSwitchState() {
    }

    public static HwGunstallSwitchState getInstance() {
        return INSTANCE;
    }

    public boolean getGunstallShowSwitch() {
        return this.mGunstallShowSwitch;
    }

    public int getGunstallUserState() {
        return this.mGunstallUserState;
    }

    public boolean getGunstallUpdateState() {
        return this.mGunstallUpdateState;
    }

    public int getGunstallAppVersion() {
        return this.mGunstallAppVersion;
    }

    public void setGunstallShowSwitch(boolean showSwitch) {
        this.mGunstallShowSwitch = showSwitch;
    }

    public void setGunstallUserState(int userState) {
        this.mGunstallUserState = userState;
    }

    public void setGunstallUpdateState(boolean updateState) {
        this.mGunstallUpdateState = updateState;
    }

    public void setGunstallAppVersion(int appVersion) {
        this.mGunstallAppVersion = appVersion;
    }
}
