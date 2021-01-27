package com.huawei.haptic;

import android.util.ArrayMap;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;

@HwSystemApi
public class HapticWave {
    public static final int VERSION = 1;
    public final ArrayList<HapticChannel> mHapticChannels = new ArrayList<>();
    private ArrayMap<String, String> mMetadata = new ArrayMap<>();

    static HwHapticWave createHwHapticWave(HapticWave wave) {
        if (wave == null) {
            return null;
        }
        HwHapticWave hwHapticWave = new HwHapticWave();
        hwHapticWave.mVersion = 1;
        for (int i = 0; i < wave.mHapticChannels.size(); i++) {
            hwHapticWave.mHapticChannels.add(HapticChannel.createHwHapticChannel(wave.mHapticChannels.get(i)));
        }
        return hwHapticWave;
    }

    public void addHapticChannel(HapticChannel channel) {
        this.mHapticChannels.add(channel);
    }
}
