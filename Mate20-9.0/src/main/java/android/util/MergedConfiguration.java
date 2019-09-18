package android.util;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.PrintWriter;

public class MergedConfiguration implements Parcelable {
    public static final Parcelable.Creator<MergedConfiguration> CREATOR = new Parcelable.Creator<MergedConfiguration>() {
        public MergedConfiguration createFromParcel(Parcel in) {
            return new MergedConfiguration(in);
        }

        public MergedConfiguration[] newArray(int size) {
            return new MergedConfiguration[size];
        }
    };
    private Configuration mGlobalConfig;
    private Configuration mMergedConfig;
    private Configuration mOverrideConfig;

    public MergedConfiguration() {
        this.mGlobalConfig = new Configuration();
        this.mOverrideConfig = new Configuration();
        this.mMergedConfig = new Configuration();
    }

    public MergedConfiguration(Configuration globalConfig, Configuration overrideConfig) {
        this.mGlobalConfig = new Configuration();
        this.mOverrideConfig = new Configuration();
        this.mMergedConfig = new Configuration();
        setConfiguration(globalConfig, overrideConfig);
    }

    public MergedConfiguration(Configuration globalConfig) {
        this.mGlobalConfig = new Configuration();
        this.mOverrideConfig = new Configuration();
        this.mMergedConfig = new Configuration();
        setGlobalConfiguration(globalConfig);
    }

    public MergedConfiguration(MergedConfiguration mergedConfiguration) {
        this.mGlobalConfig = new Configuration();
        this.mOverrideConfig = new Configuration();
        this.mMergedConfig = new Configuration();
        setConfiguration(mergedConfiguration.getGlobalConfiguration(), mergedConfiguration.getOverrideConfiguration());
    }

    private MergedConfiguration(Parcel in) {
        this.mGlobalConfig = new Configuration();
        this.mOverrideConfig = new Configuration();
        this.mMergedConfig = new Configuration();
        readFromParcel(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mGlobalConfig, flags);
        dest.writeParcelable(this.mOverrideConfig, flags);
        dest.writeParcelable(this.mMergedConfig, flags);
    }

    public void readFromParcel(Parcel source) {
        this.mGlobalConfig = (Configuration) source.readParcelable(Configuration.class.getClassLoader());
        this.mOverrideConfig = (Configuration) source.readParcelable(Configuration.class.getClassLoader());
        this.mMergedConfig = (Configuration) source.readParcelable(Configuration.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void setConfiguration(Configuration globalConfig, Configuration overrideConfig) {
        this.mGlobalConfig.setTo(globalConfig);
        this.mOverrideConfig.setTo(overrideConfig);
        updateMergedConfig();
    }

    public void setGlobalConfiguration(Configuration globalConfig) {
        this.mGlobalConfig.setTo(globalConfig);
        updateMergedConfig();
    }

    public void setOverrideConfiguration(Configuration overrideConfig) {
        this.mOverrideConfig.setTo(overrideConfig);
        updateMergedConfig();
    }

    public void setTo(MergedConfiguration config) {
        setConfiguration(config.mGlobalConfig, config.mOverrideConfig);
    }

    public void unset() {
        this.mGlobalConfig.unset();
        this.mOverrideConfig.unset();
        updateMergedConfig();
    }

    public Configuration getGlobalConfiguration() {
        return this.mGlobalConfig;
    }

    public Configuration getOverrideConfiguration() {
        return this.mOverrideConfig;
    }

    public Configuration getMergedConfiguration() {
        return this.mMergedConfig;
    }

    private void updateMergedConfig() {
        this.mMergedConfig.setTo(this.mGlobalConfig);
        this.mMergedConfig.updateFrom(this.mOverrideConfig);
    }

    public String toString() {
        return "{mGlobalConfig=" + this.mGlobalConfig + " mOverrideConfig=" + this.mOverrideConfig + "}";
    }

    public int hashCode() {
        return this.mMergedConfig.hashCode();
    }

    public boolean equals(Object that) {
        if (!(that instanceof MergedConfiguration)) {
            return false;
        }
        if (that == this) {
            return true;
        }
        return this.mMergedConfig.equals(((MergedConfiguration) that).mMergedConfig);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "mGlobalConfig=" + this.mGlobalConfig);
        pw.println(prefix + "mOverrideConfig=" + this.mOverrideConfig);
    }
}
