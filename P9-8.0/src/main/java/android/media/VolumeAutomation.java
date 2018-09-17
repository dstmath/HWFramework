package android.media;

import android.media.VolumeShaper.Configuration;

public interface VolumeAutomation {
    VolumeShaper createVolumeShaper(Configuration configuration);
}
