package com.android.server.audio;

import android.media.IAudioModeDispatcher;
import android.media.IVolumeChangeDispatcher;
import android.os.IBinder;
import java.util.Map;

public interface IHwAudioServiceEx {
    boolean bypassVolumeProcessForTV(int i, int i2, int i3, int i4);

    boolean checkMuteZenMode();

    boolean checkRecordActive(int i);

    void dipatchAudioModeChanged(int i);

    void dispatchVolumeChange(int i, int i2, String str, int i3);

    IBinder getDeviceSelectCallback();

    int getHwSafeUsbMediaVolumeIndex();

    int getRecordConcurrentType(String str);

    void hideHiResIconDueKilledAPP(boolean z, String str);

    boolean isHwKaraokeEffectEnable(String str);

    boolean isHwSafeUsbMediaVolumeEnabled();

    boolean isKaraokeWhiteListApp(String str);

    boolean isMultiAudioRecordEnable();

    boolean isSystemApp(int i);

    boolean isVirtualAudio(int i);

    boolean isVoiceRecordingEnable();

    void notifyHiResIcon(int i);

    void notifySendBroadcastForKaraoke(int i);

    void notifyStartDolbyDms(int i);

    void onRestoreDevices();

    void onSetSoundEffectState(int i, int i2);

    void processAudioServerRestart();

    boolean registerAudioDeviceSelectCallback(IBinder iBinder);

    void registerAudioModeCallback(IAudioModeDispatcher iAudioModeDispatcher);

    boolean registerVolumeChangeCallback(IVolumeChangeDispatcher iVolumeChangeDispatcher, String str, String str2);

    void removeKaraokeWhiteAppUIDByPkgName(String str);

    int removeVirtualAudio(String str, String str2, int i, Map<String, Object> map);

    void sendAudioRecordStateChangedIntent(String str, int i, int i2, String str2);

    void setBluetoothScoState(int i, int i2);

    void setBtScoForRecord(boolean z);

    boolean setDolbyEffect(int i);

    boolean setFmDeviceAvailable(int i, boolean z);

    void setGameForeground();

    void setHistenNaturalMode(boolean z, IBinder iBinder);

    void setKaraokeWhiteAppUIDByPkgName(String str);

    void setKaraokeWhiteListUID();

    void setMultiAudioRecordEnable(boolean z);

    int setSoundEffectState(boolean z, String str, boolean z2, String str2);

    void setSystemReady();

    void setVoiceRecordingEnable(boolean z);

    boolean setVolumeByPidStream(int i, int i2, float f, IBinder iBinder);

    int startVirtualAudio(String str, String str2, int i, Map<String, Object> map);

    boolean unregisterAudioDeviceSelectCallback(IBinder iBinder);

    void unregisterAudioModeCallback(IAudioModeDispatcher iAudioModeDispatcher);

    boolean unregisterVolumeChangeCallback(IVolumeChangeDispatcher iVolumeChangeDispatcher, String str, String str2);

    void updateTypeCNotify(int i, int i2, String str);
}
