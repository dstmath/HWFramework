package com.android.server;

import android.os.storage.IStorageManager;

public abstract class AbsStorageManagerService extends IStorageManager.Stub {
    public int startClean() {
        return 0;
    }

    public int stopClean() {
        return 0;
    }

    public int getNotificationLevel() {
        return 0;
    }

    public int getUndiscardInfo() {
        return 0;
    }

    public int getMaxTimeCost() {
        return 0;
    }

    public int getMinTimeCost() {
        return 0;
    }

    public int getPercentComplete() {
        return 0;
    }

    public void createUserKeyISec(int userId, int serialNumber, boolean ephemeral) {
    }

    public void destroyUserKeyISec(int userId) {
    }

    public void unlockUserKeyISec(int userId, int serialNumber, byte[] token, byte[] secret) {
    }

    public void lockUserKeyISec(int userId) {
    }

    public void unlockUserScreenISec(int userId, int serialNumber, byte[] token, byte[] secret, int type) {
    }

    public void lockUserScreenISec(int userId, int serialNumber) {
    }

    public int getPreLoadPolicyFlag(int userId, int serialNumber) {
        return 0;
    }

    public boolean setScreenStateFlag(int userId, int serialNumber, int flag) {
        return false;
    }

    public String getKeyDesc(int userId, int serialNumber, int sdpClass) {
        return null;
    }

    public void onLockedDiskAdd() {
    }

    public void onLockedDiskRemove() {
    }

    public void onLockedDiskChange() {
    }

    public long getPartitionInfo(String partitionName, int infoType) {
        return 0;
    }

    public void notifyDeviceStateToTelephony(String device, String state, String extras) {
    }
}
