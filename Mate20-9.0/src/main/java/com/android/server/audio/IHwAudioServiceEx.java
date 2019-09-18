package com.android.server.audio;

import android.media.IAudioModeDispatcher;

public interface IHwAudioServiceEx {
    boolean checkRecordActive(int i);

    void dipatchAudioModeChanged(int i);

    int getHwSafeUsbMediaVolumeIndex();

    int getRecordConcurrentType(String str);

    void hideHiResIconDueKilledAPP(boolean z, String str);

    boolean isHwKaraokeEffectEnable(String str);

    boolean isHwSafeUsbMediaVolumeEnabled();

    boolean isKaraokeWhiteListApp(String str);

    void notifyHiResIcon(int i);

    void notifyStartDolbyDms(int i);

    void onSetSoundEffectState(int i, int i2);

    void processAudioServerRestart();

    void registerAudioModeCallback(IAudioModeDispatcher iAudioModeDispatcher);

    void sendAudioRecordStateChangedIntent(String str, int i, int i2, String str2);

    boolean setDolbyEffect(int i);

    void setKaraokeWhiteAppUIDByPkgName(String str);

    int setSoundEffectState(boolean z, String str, boolean z2, String str2);

    void setSystemReady();

    void unregisterAudioModeCallback(IAudioModeDispatcher iAudioModeDispatcher);

    void updateMicIcon();

    void updateTypeCNotify(int i, int i2);
}
