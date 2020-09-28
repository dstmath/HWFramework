package com.huawei.airsharing.api;

import android.os.RemoteException;
import com.huawei.airsharing.api.IAidlMediaPlayerListener;

public class AidlMediaPlayerListenerStub extends IAidlMediaPlayerListener.Stub {
    @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
    public void onStateChanged(NotificationInfo info) throws RemoteException {
    }

    @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
    public void onMediaItemChanged(NotificationInfo info) throws RemoteException {
    }

    @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
    public void onError(NotificationInfo info) throws RemoteException {
    }

    @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
    public void onVolumeChanged(int currVolume) throws RemoteException {
    }

    @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
    public void onPositionChanged(int currPosition) throws RemoteException {
    }

    @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
    public void onRateChanged(float currRate) throws RemoteException {
    }

    @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
    public void onRepeatModeChanged(String repeatMode) throws RemoteException {
    }

    @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
    public void onBufferedPositionChanged(int bufferedPosition) throws RemoteException {
    }

    @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
    public void onVolumeMutedChanged(boolean isMute) throws RemoteException {
    }
}
