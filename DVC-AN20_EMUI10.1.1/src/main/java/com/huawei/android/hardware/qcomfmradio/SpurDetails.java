package com.huawei.android.hardware.qcomfmradio;

/* access modifiers changed from: package-private */
public class SpurDetails {
    private byte FilterCoefficeint;
    private byte IsEnableSpur;
    private byte LsbOfIntegrationLength;
    private int RotationValue;
    private byte SpurLevel;

    public int getRotationValue() {
        return this.RotationValue;
    }

    public void setRotationValue(int rotationValue) {
        this.RotationValue = rotationValue;
    }

    public byte getLsbOfIntegrationLength() {
        return this.LsbOfIntegrationLength;
    }

    public void setLsbOfIntegrationLength(byte lsbOfIntegrationLength) {
        this.LsbOfIntegrationLength = lsbOfIntegrationLength;
    }

    public byte getFilterCoefficeint() {
        return this.FilterCoefficeint;
    }

    public void setFilterCoefficeint(byte filterCoefficeint) {
        this.FilterCoefficeint = filterCoefficeint;
    }

    public byte getIsEnableSpur() {
        return this.IsEnableSpur;
    }

    public void setIsEnableSpur(byte isEnableSpur) {
        this.IsEnableSpur = isEnableSpur;
    }

    public byte getSpurLevel() {
        return this.SpurLevel;
    }

    public void setSpurLevel(byte spurLevel) {
        this.SpurLevel = spurLevel;
    }

    SpurDetails(int RotationValue2, byte LsbOfIntegrationLength2, byte FilterCoefficeint2, byte IsEnableSpur2, byte SpurLevel2) {
        this.RotationValue = RotationValue2;
        this.LsbOfIntegrationLength = LsbOfIntegrationLength2;
        this.IsEnableSpur = IsEnableSpur2;
        this.SpurLevel = SpurLevel2;
    }

    public SpurDetails() {
    }

    public SpurDetails(SpurDetails spurDetails) {
        if (spurDetails != null) {
            this.RotationValue = spurDetails.RotationValue;
            this.LsbOfIntegrationLength = spurDetails.LsbOfIntegrationLength;
            this.IsEnableSpur = spurDetails.IsEnableSpur;
            this.SpurLevel = spurDetails.SpurLevel;
        }
    }
}
