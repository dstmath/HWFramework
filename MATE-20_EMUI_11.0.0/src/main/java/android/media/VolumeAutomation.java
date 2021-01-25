package android.media;

import android.media.VolumeShaper;

public interface VolumeAutomation {
    VolumeShaper createVolumeShaper(VolumeShaper.Configuration configuration);
}
