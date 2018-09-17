package android.content.res;

import android.os.Parcel;

public final class HwConfigurationDummy implements Comparable<Object>, IHwConfiguration {
    public HwConfigurationDummy(IHwConfiguration o) {
    }

    public void setTo(IHwConfiguration o) {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Configuration dummy.");
        return sb.toString();
    }

    public void setToDefaults() {
    }

    @Deprecated
    public void makeDefault() {
    }

    public int updateFrom(IHwConfiguration delta) {
        return 0;
    }

    public int diff(IHwConfiguration delta) {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    public void readFromParcel(Parcel source) {
    }

    public int compareTo(Object that) {
        if (that instanceof HwConfigurationDummy) {
            return 0;
        }
        return -1;
    }

    public int compareTo(IHwConfiguration that) {
        if (that instanceof HwConfigurationDummy) {
            return 0;
        }
        return -1;
    }

    public boolean equals(IHwConfiguration that) {
        return compareTo(that) == 0;
    }

    public boolean equals(Object that) {
        return compareTo(that) == 0;
    }

    public int hashCode() {
        return 527 + 0;
    }

    public void setConfigItem(int mode, int val) {
    }

    public int getConfigItem(int mode) {
        return 0;
    }

    public void setDensityDPI(int dpi) {
    }
}
