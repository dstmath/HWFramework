package android.content.res;

import android.os.Parcel;

public class HwConfigurationDummy implements Comparable<Object>, IHwConfiguration {
    private static final int HASH_DEFAULT = 17;
    private static final int HASH_MODULUS = 31;
    private static final int MAX_SIZE = 128;

    public HwConfigurationDummy() {
    }

    public HwConfigurationDummy(IHwConfiguration config) {
    }

    @Override // android.content.res.IHwConfiguration
    public void setTo(IHwConfiguration config) {
    }

    @Override // java.lang.Object, android.content.res.IHwConfiguration
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Configuration dummy.");
        return sb.toString();
    }

    @Override // android.content.res.IHwConfiguration
    public void setToDefaults() {
    }

    @Override // android.content.res.IHwConfiguration
    @Deprecated
    public void makeDefault() {
    }

    @Override // android.content.res.IHwConfiguration
    public int updateFrom(IHwConfiguration delta) {
        return 0;
    }

    @Override // android.content.res.IHwConfiguration
    public int diff(IHwConfiguration delta) {
        return 0;
    }

    @Override // android.content.res.IHwConfiguration
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override // android.content.res.IHwConfiguration
    public void readFromParcel(Parcel source) {
    }

    @Override // java.lang.Comparable
    public int compareTo(Object that) {
        if (that instanceof HwConfigurationDummy) {
            return 0;
        }
        return -1;
    }

    @Override // android.content.res.IHwConfiguration
    public int compareTo(IHwConfiguration that) {
        if (that instanceof HwConfigurationDummy) {
            return 0;
        }
        return -1;
    }

    @Override // android.content.res.IHwConfiguration
    public boolean equals(IHwConfiguration that) {
        return compareTo(that) == 0;
    }

    @Override // java.lang.Object, android.content.res.IHwConfiguration
    public boolean equals(Object that) {
        return compareTo(that) == 0;
    }

    @Override // java.lang.Object, android.content.res.IHwConfiguration
    public int hashCode() {
        return (17 * 31) + 0;
    }

    @Override // android.content.res.IHwConfiguration
    public void setConfigItem(int mode, int val) {
    }

    @Override // android.content.res.IHwConfiguration
    public int getConfigItem(int mode) {
        return 0;
    }

    @Override // android.content.res.IHwConfiguration
    public void setDensityDPI(int dpi) {
    }
}
