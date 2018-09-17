package android.view;

import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import java.util.Objects;

public class DisplayAdjustments {
    public static final DisplayAdjustments DEFAULT_DISPLAY_ADJUSTMENTS = new DisplayAdjustments();
    private volatile CompatibilityInfo mCompatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
    private Configuration mConfiguration;

    public DisplayAdjustments(Configuration configuration) {
        if (configuration == null) {
            configuration = Configuration.EMPTY;
        }
        this.mConfiguration = new Configuration(configuration);
    }

    public DisplayAdjustments(DisplayAdjustments daj) {
        setCompatibilityInfo(daj.mCompatInfo);
        this.mConfiguration = new Configuration(daj.mConfiguration != null ? daj.mConfiguration : Configuration.EMPTY);
    }

    public void setCompatibilityInfo(CompatibilityInfo compatInfo) {
        if (this == DEFAULT_DISPLAY_ADJUSTMENTS) {
            throw new IllegalArgumentException("setCompatbilityInfo: Cannot modify DEFAULT_DISPLAY_ADJUSTMENTS");
        } else if (compatInfo == null || (!compatInfo.isScalingRequired() && (compatInfo.supportsScreen() ^ 1) == 0)) {
            this.mCompatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        } else {
            this.mCompatInfo = compatInfo;
        }
    }

    public CompatibilityInfo getCompatibilityInfo() {
        return this.mCompatInfo;
    }

    public void setConfiguration(Configuration configuration) {
        if (this == DEFAULT_DISPLAY_ADJUSTMENTS) {
            throw new IllegalArgumentException("setConfiguration: Cannot modify DEFAULT_DISPLAY_ADJUSTMENTS");
        }
        Configuration configuration2 = this.mConfiguration;
        if (configuration == null) {
            configuration = Configuration.EMPTY;
        }
        configuration2.setTo(configuration);
    }

    public Configuration getConfiguration() {
        return this.mConfiguration;
    }

    public int hashCode() {
        return ((Objects.hashCode(this.mCompatInfo) + MetricsEvent.DIALOG_SUPPORT_PHONE) * 31) + Objects.hashCode(this.mConfiguration);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof DisplayAdjustments)) {
            return false;
        }
        DisplayAdjustments daj = (DisplayAdjustments) o;
        if (Objects.equals(daj.mCompatInfo, this.mCompatInfo)) {
            z = Objects.equals(daj.mConfiguration, this.mConfiguration);
        }
        return z;
    }
}
