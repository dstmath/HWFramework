package com.huawei.airsharing.api;

public interface IPlayerManager extends IServerManager {
    int getVolume();

    boolean next();

    boolean pause();

    boolean play(PlayInfo playInfo);

    boolean playMediaItem(int i);

    boolean previous();

    boolean resume();

    boolean seek(int i);

    boolean setRepeatMode(ERepeatMode eRepeatMode);

    boolean setVolume(int i);

    boolean setVolumeMute(boolean z);

    boolean stop();

    boolean updatePlayInfo(PlayInfo playInfo);
}
