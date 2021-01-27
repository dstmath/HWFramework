package com.android.server;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import huawei.android.os.IHwGeneralManager;

public class DefaultHwGeneralService extends IHwGeneralManager.Stub {
    private static final int INVALID_VALUE = -1;

    public DefaultHwGeneralService(Context context, Handler handler) {
    }

    public int setSDLockPassword(String pw) {
        return -1;
    }

    public int clearSDLockPassword() {
        return -1;
    }

    public int unlockSDCard(String pw) {
        return -1;
    }

    public void eraseSDLock() {
    }

    public int getSDLockState() {
        return -1;
    }

    public String getSDCardId() {
        return null;
    }

    public void startFileBackup() {
    }

    public int forceIdle() {
        return -1;
    }

    public boolean isSupportForce() {
        return false;
    }

    public float getPressureLimit() {
        return -1.0f;
    }

    public boolean isCurveScreen() {
        return false;
    }

    public void playIvtEffect(String effectName) {
    }

    public void stopPlayEffect() {
    }

    public void pausePlayEffect(String effectName) {
    }

    public void resumePausedEffect(String effectName) {
    }

    public boolean isPlaying(String effectName) {
        return false;
    }

    public boolean startHaptic(int callerID, int ringtoneType, Uri uri) {
        return false;
    }

    public boolean hasHaptic(Uri uri) {
        return false;
    }

    public void stopHaptic() {
    }

    public void resetTouchWeight() {
    }

    public String getTouchWeightValue() {
        return null;
    }

    public boolean mkDataDir(String path) {
        return false;
    }

    public Messenger getTestService() {
        return null;
    }

    public int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) {
        return -1;
    }

    public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) {
        return -1;
    }

    public int setSdCardCryptdEnable(boolean isEnable, String volId) {
        return -1;
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        return -1;
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        return -1;
    }

    public int backupSecretkey() {
        return -1;
    }

    public boolean supportHwPush() {
        return false;
    }

    public long getPartitionInfo(String partitionName, int infoType) {
        return -1;
    }

    public String mountCifs(String source, String option, IBinder binder) {
        return null;
    }

    public void unmountCifs(String mountPoint) {
    }

    public int isSupportedCifs() {
        return -1;
    }

    public int getLocalDevStat(int dev) {
        return -1;
    }

    public String getDeviceId(int dev) {
        return null;
    }

    public int doSdcardCheckRW() {
        return -1;
    }

    public boolean isIsolatedStorageApp(int uid, String packageName) {
        return false;
    }

    public String[] getIsolatedStorageApps(int excludeFlag) {
        return null;
    }
}
