package ohos.ai.engine.pluginlabel;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class PluginLabelInfo implements Sequenceable {
    private String cameraLabel = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    private String computationalResourceLabel = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    private String distanceLabel = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    private String regionLabel = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
    private String xpuLabel = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.regionLabel);
        parcel.writeString(this.computationalResourceLabel);
        parcel.writeString(this.xpuLabel);
        parcel.writeString(this.distanceLabel);
        parcel.writeString(this.cameraLabel);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.regionLabel = parcel.readString();
        this.computationalResourceLabel = parcel.readString();
        this.xpuLabel = parcel.readString();
        this.distanceLabel = parcel.readString();
        this.cameraLabel = parcel.readString();
        return true;
    }

    public void setRegionLabel(String str) {
        this.regionLabel = str;
    }

    public String getRegionLabel() {
        return this.regionLabel;
    }

    public void setComputationalResourceLabel(String str) {
        this.computationalResourceLabel = str;
    }

    public String getComputationalResourceLabel() {
        return this.computationalResourceLabel;
    }

    public void setXpuLabel(String str) {
        this.xpuLabel = str;
    }

    public String getXpuLabel() {
        return this.xpuLabel;
    }

    public void setDistanceLabel(String str) {
        this.distanceLabel = str;
    }

    public String getDistanceLabel() {
        return this.distanceLabel;
    }

    public void setCameraLabel(String str) {
        this.cameraLabel = str;
    }

    public String getCameraLabel() {
        return this.cameraLabel;
    }
}
